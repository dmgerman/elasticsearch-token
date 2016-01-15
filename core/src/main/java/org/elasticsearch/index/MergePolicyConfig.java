begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|MergePolicy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|NoMergePolicy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|TieredMergePolicy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
operator|.
name|ESLogger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Setting
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|ByteSizeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|ByteSizeValue
import|;
end_import

begin_comment
comment|/**  * A shard in elasticsearch is a Lucene index, and a Lucene index is broken  * down into segments. Segments are internal storage elements in the index  * where the index data is stored, and are immutable up to delete markers.  * Segments are, periodically, merged into larger segments to keep the  * index size at bay and expunge deletes.  *  *<p>  * Merges select segments of approximately equal size, subject to an allowed  * number of segments per tier. The merge policy is able to merge  * non-adjacent segments, and separates how many segments are merged at once from how many  * segments are allowed per tier. It also does not over-merge (i.e., cascade merges).  *  *<p>  * All merge policy settings are<b>dynamic</b> and can be updated on a live index.  * The merge policy has the following settings:  *  *<ul>  *<li><code>index.merge.policy.expunge_deletes_allowed</code>:  *  *     When expungeDeletes is called, we only merge away a segment if its delete  *     percentage is over this threshold. Default is<code>10</code>.  *  *<li><code>index.merge.policy.floor_segment</code>:  *  *     Segments smaller than this are "rounded up" to this size, i.e. treated as  *     equal (floor) size for merge selection. This is to prevent frequent  *     flushing of tiny segments, thus preventing a long tail in the index. Default  *     is<code>2mb</code>.  *  *<li><code>index.merge.policy.max_merge_at_once</code>:  *  *     Maximum number of segments to be merged at a time during "normal" merging.  *     Default is<code>10</code>.  *  *<li><code>index.merge.policy.max_merge_at_once_explicit</code>:  *  *     Maximum number of segments to be merged at a time, during force merge or  *     expungeDeletes. Default is<code>30</code>.  *  *<li><code>index.merge.policy.max_merged_segment</code>:  *  *     Maximum sized segment to produce during normal merging (not explicit  *     force merge). This setting is approximate: the estimate of the merged  *     segment size is made by summing sizes of to-be-merged segments  *     (compensating for percent deleted docs). Default is<code>5gb</code>.  *  *<li><code>index.merge.policy.segments_per_tier</code>:  *  *     Sets the allowed number of segments per tier. Smaller values mean more  *     merging but fewer segments. Default is<code>10</code>. Note, this value needs to be  *&gt;= than the<code>max_merge_at_once</code> otherwise you'll force too many merges to  *     occur.  *  *<li><code>index.merge.policy.reclaim_deletes_weight</code>:  *  *     Controls how aggressively merges that reclaim more deletions are favored.  *     Higher values favor selecting merges that reclaim deletions. A value of  *<code>0.0</code> means deletions don't impact merge selection. Defaults to<code>2.0</code>.  *</ul>  *  *<p>  * For normal merging, the policy first computes a "budget" of how many  * segments are allowed to be in the index. If the index is over-budget,  * then the policy sorts segments by decreasing size (proportionally considering percent  * deletes), and then finds the least-cost merge. Merge cost is measured by  * a combination of the "skew" of the merge (size of largest seg divided by  * smallest seg), total merge size and pct deletes reclaimed, so that  * merges with lower skew, smaller size and those reclaiming more deletes,  * are favored.  *  *<p>  * If a merge will produce a segment that's larger than  *<code>max_merged_segment</code> then the policy will merge fewer segments (down to  * 1 at once, if that one has deletions) to keep the segment size under  * budget.  *  *<p>  * Note, this can mean that for large shards that holds many gigabytes of  * data, the default of<code>max_merged_segment</code> (<code>5gb</code>) can cause for many  * segments to be in an index, and causing searches to be slower. Use the  * indices segments API to see the segments that an index has, and  * possibly either increase the<code>max_merged_segment</code> or issue an optimize  * call for the index (try and aim to issue it on a low traffic time).  */
end_comment

begin_class
DECL|class|MergePolicyConfig
specifier|public
specifier|final
class|class
name|MergePolicyConfig
block|{
DECL|field|mergePolicy
specifier|private
specifier|final
name|TieredMergePolicy
name|mergePolicy
init|=
operator|new
name|TieredMergePolicy
argument_list|()
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|mergesEnabled
specifier|private
specifier|final
name|boolean
name|mergesEnabled
decl_stmt|;
DECL|field|DEFAULT_EXPUNGE_DELETES_ALLOWED
specifier|public
specifier|static
specifier|final
name|double
name|DEFAULT_EXPUNGE_DELETES_ALLOWED
init|=
literal|10d
decl_stmt|;
DECL|field|DEFAULT_FLOOR_SEGMENT
specifier|public
specifier|static
specifier|final
name|ByteSizeValue
name|DEFAULT_FLOOR_SEGMENT
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|2
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_MAX_MERGE_AT_ONCE
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_MERGE_AT_ONCE
init|=
literal|10
decl_stmt|;
DECL|field|DEFAULT_MAX_MERGE_AT_ONCE_EXPLICIT
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_MERGE_AT_ONCE_EXPLICIT
init|=
literal|30
decl_stmt|;
DECL|field|DEFAULT_MAX_MERGED_SEGMENT
specifier|public
specifier|static
specifier|final
name|ByteSizeValue
name|DEFAULT_MAX_MERGED_SEGMENT
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|5
argument_list|,
name|ByteSizeUnit
operator|.
name|GB
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_SEGMENTS_PER_TIER
specifier|public
specifier|static
specifier|final
name|double
name|DEFAULT_SEGMENTS_PER_TIER
init|=
literal|10.0d
decl_stmt|;
DECL|field|DEFAULT_RECLAIM_DELETES_WEIGHT
specifier|public
specifier|static
specifier|final
name|double
name|DEFAULT_RECLAIM_DELETES_WEIGHT
init|=
literal|2.0d
decl_stmt|;
DECL|field|INDEX_COMPOUND_FORMAT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Double
argument_list|>
name|INDEX_COMPOUND_FORMAT_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"index.compound_format"
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|TieredMergePolicy
operator|.
name|DEFAULT_NO_CFS_RATIO
argument_list|)
argument_list|,
name|MergePolicyConfig
operator|::
name|parseNoCFSRatio
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_MERGE_POLICY_EXPUNGE_DELETES_ALLOWED_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Double
argument_list|>
name|INDEX_MERGE_POLICY_EXPUNGE_DELETES_ALLOWED_SETTING
init|=
name|Setting
operator|.
name|doubleSetting
argument_list|(
literal|"index.merge.policy.expunge_deletes_allowed"
argument_list|,
name|DEFAULT_EXPUNGE_DELETES_ALLOWED
argument_list|,
literal|0.0d
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_MERGE_POLICY_FLOOR_SEGMENT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|ByteSizeValue
argument_list|>
name|INDEX_MERGE_POLICY_FLOOR_SEGMENT_SETTING
init|=
name|Setting
operator|.
name|byteSizeSetting
argument_list|(
literal|"index.merge.policy.floor_segment"
argument_list|,
name|DEFAULT_FLOOR_SEGMENT
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE_SETTING
init|=
name|Setting
operator|.
name|intSetting
argument_list|(
literal|"index.merge.policy.max_merge_at_once"
argument_list|,
name|DEFAULT_MAX_MERGE_AT_ONCE
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE_EXPLICIT
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE_EXPLICIT
init|=
literal|"index.merge.policy.max_merge_at_once_explicit"
decl_stmt|;
DECL|field|INDEX_MERGE_POLICY_MAX_MERGED_SEGMENT
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_MERGE_POLICY_MAX_MERGED_SEGMENT
init|=
literal|"index.merge.policy.max_merged_segment"
decl_stmt|;
DECL|field|INDEX_MERGE_POLICY_SEGMENTS_PER_TIER
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_MERGE_POLICY_SEGMENTS_PER_TIER
init|=
literal|"index.merge.policy.segments_per_tier"
decl_stmt|;
DECL|field|INDEX_MERGE_POLICY_RECLAIM_DELETES_WEIGHT
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_MERGE_POLICY_RECLAIM_DELETES_WEIGHT
init|=
literal|"index.merge.policy.reclaim_deletes_weight"
decl_stmt|;
DECL|field|INDEX_MERGE_ENABLED
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_MERGE_ENABLED
init|=
literal|"index.merge.enabled"
decl_stmt|;
DECL|method|MergePolicyConfig
name|MergePolicyConfig
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|IndexSettings
name|indexSettings
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|indexSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_COMPOUND_FORMAT_SETTING
argument_list|,
name|this
operator|::
name|setNoCFSRatio
argument_list|)
expr_stmt|;
name|indexSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_MERGE_POLICY_EXPUNGE_DELETES_ALLOWED_SETTING
argument_list|,
name|this
operator|::
name|expungeDeletesAllowed
argument_list|)
expr_stmt|;
name|indexSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_MERGE_POLICY_FLOOR_SEGMENT_SETTING
argument_list|,
name|this
operator|::
name|floorSegmentSetting
argument_list|)
expr_stmt|;
name|indexSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE_SETTING
argument_list|,
name|this
operator|::
name|maxMergesAtOnce
argument_list|)
expr_stmt|;
name|double
name|forceMergeDeletesPctAllowed
init|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_MERGE_POLICY_EXPUNGE_DELETES_ALLOWED_SETTING
argument_list|)
decl_stmt|;
comment|// percentage
name|ByteSizeValue
name|floorSegment
init|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_MERGE_POLICY_FLOOR_SEGMENT_SETTING
argument_list|)
decl_stmt|;
name|int
name|maxMergeAtOnce
init|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE_SETTING
argument_list|)
decl_stmt|;
name|int
name|maxMergeAtOnceExplicit
init|=
name|indexSettings
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsInt
argument_list|(
literal|"index.merge.policy.max_merge_at_once_explicit"
argument_list|,
name|DEFAULT_MAX_MERGE_AT_ONCE_EXPLICIT
argument_list|)
decl_stmt|;
comment|// TODO is this really a good default number for max_merge_segment, what happens for large indices, won't they end up with many segments?
name|ByteSizeValue
name|maxMergedSegment
init|=
name|indexSettings
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsBytesSize
argument_list|(
literal|"index.merge.policy.max_merged_segment"
argument_list|,
name|DEFAULT_MAX_MERGED_SEGMENT
argument_list|)
decl_stmt|;
name|double
name|segmentsPerTier
init|=
name|indexSettings
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsDouble
argument_list|(
literal|"index.merge.policy.segments_per_tier"
argument_list|,
name|DEFAULT_SEGMENTS_PER_TIER
argument_list|)
decl_stmt|;
name|double
name|reclaimDeletesWeight
init|=
name|indexSettings
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsDouble
argument_list|(
literal|"index.merge.policy.reclaim_deletes_weight"
argument_list|,
name|DEFAULT_RECLAIM_DELETES_WEIGHT
argument_list|)
decl_stmt|;
name|this
operator|.
name|mergesEnabled
operator|=
name|indexSettings
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_MERGE_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|mergesEnabled
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] is set to false, this should only be used in tests and can cause serious problems in production environments"
argument_list|,
name|INDEX_MERGE_ENABLED
argument_list|)
expr_stmt|;
block|}
name|maxMergeAtOnce
operator|=
name|adjustMaxMergeAtOnceIfNeeded
argument_list|(
name|maxMergeAtOnce
argument_list|,
name|segmentsPerTier
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setNoCFSRatio
argument_list|(
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_COMPOUND_FORMAT_SETTING
argument_list|)
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setForceMergeDeletesPctAllowed
argument_list|(
name|forceMergeDeletesPctAllowed
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setFloorSegmentMB
argument_list|(
name|floorSegment
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setMaxMergeAtOnce
argument_list|(
name|maxMergeAtOnce
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setMaxMergeAtOnceExplicit
argument_list|(
name|maxMergeAtOnceExplicit
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setMaxMergedSegmentMB
argument_list|(
name|maxMergedSegment
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setSegmentsPerTier
argument_list|(
name|segmentsPerTier
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setReclaimDeletesWeight
argument_list|(
name|reclaimDeletesWeight
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using [tiered] merge mergePolicy with expunge_deletes_allowed[{}], floor_segment[{}], max_merge_at_once[{}], max_merge_at_once_explicit[{}], max_merged_segment[{}], segments_per_tier[{}], reclaim_deletes_weight[{}]"
argument_list|,
name|forceMergeDeletesPctAllowed
argument_list|,
name|floorSegment
argument_list|,
name|maxMergeAtOnce
argument_list|,
name|maxMergeAtOnceExplicit
argument_list|,
name|maxMergedSegment
argument_list|,
name|segmentsPerTier
argument_list|,
name|reclaimDeletesWeight
argument_list|)
expr_stmt|;
block|}
DECL|method|maxMergesAtOnce
specifier|private
name|void
name|maxMergesAtOnce
parameter_list|(
name|Integer
name|maxMergeAtOnce
parameter_list|)
block|{
name|mergePolicy
operator|.
name|setMaxMergeAtOnce
argument_list|(
name|maxMergeAtOnce
argument_list|)
expr_stmt|;
block|}
DECL|method|floorSegmentSetting
specifier|private
name|void
name|floorSegmentSetting
parameter_list|(
name|ByteSizeValue
name|floorSegementSetting
parameter_list|)
block|{
name|mergePolicy
operator|.
name|setFloorSegmentMB
argument_list|(
name|floorSegementSetting
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|expungeDeletesAllowed
specifier|private
name|void
name|expungeDeletesAllowed
parameter_list|(
name|Double
name|value
parameter_list|)
block|{
name|mergePolicy
operator|.
name|setForceMergeDeletesPctAllowed
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
DECL|method|setNoCFSRatio
specifier|private
name|void
name|setNoCFSRatio
parameter_list|(
name|Double
name|noCFSRatio
parameter_list|)
block|{
name|mergePolicy
operator|.
name|setNoCFSRatio
argument_list|(
name|noCFSRatio
argument_list|)
expr_stmt|;
block|}
DECL|method|adjustMaxMergeAtOnceIfNeeded
specifier|private
name|int
name|adjustMaxMergeAtOnceIfNeeded
parameter_list|(
name|int
name|maxMergeAtOnce
parameter_list|,
name|double
name|segmentsPerTier
parameter_list|)
block|{
comment|// fixing maxMergeAtOnce, see TieredMergePolicy#setMaxMergeAtOnce
if|if
condition|(
operator|!
operator|(
name|segmentsPerTier
operator|>=
name|maxMergeAtOnce
operator|)
condition|)
block|{
name|int
name|newMaxMergeAtOnce
init|=
operator|(
name|int
operator|)
name|segmentsPerTier
decl_stmt|;
comment|// max merge at once should be at least 2
if|if
condition|(
name|newMaxMergeAtOnce
operator|<=
literal|1
condition|)
block|{
name|newMaxMergeAtOnce
operator|=
literal|2
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"changing max_merge_at_once from [{}] to [{}] because segments_per_tier [{}] has to be higher or equal to it"
argument_list|,
name|maxMergeAtOnce
argument_list|,
name|newMaxMergeAtOnce
argument_list|,
name|segmentsPerTier
argument_list|)
expr_stmt|;
name|maxMergeAtOnce
operator|=
name|newMaxMergeAtOnce
expr_stmt|;
block|}
return|return
name|maxMergeAtOnce
return|;
block|}
DECL|method|getMergePolicy
name|MergePolicy
name|getMergePolicy
parameter_list|()
block|{
return|return
name|mergesEnabled
condition|?
name|mergePolicy
else|:
name|NoMergePolicy
operator|.
name|INSTANCE
return|;
block|}
DECL|method|onRefreshSettings
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
specifier|final
name|double
name|oldSegmentsPerTier
init|=
name|mergePolicy
operator|.
name|getSegmentsPerTier
argument_list|()
decl_stmt|;
specifier|final
name|double
name|segmentsPerTier
init|=
name|settings
operator|.
name|getAsDouble
argument_list|(
name|INDEX_MERGE_POLICY_SEGMENTS_PER_TIER
argument_list|,
name|oldSegmentsPerTier
argument_list|)
decl_stmt|;
if|if
condition|(
name|segmentsPerTier
operator|!=
name|oldSegmentsPerTier
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [segments_per_tier] from [{}] to [{}]"
argument_list|,
name|oldSegmentsPerTier
argument_list|,
name|segmentsPerTier
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setSegmentsPerTier
argument_list|(
name|segmentsPerTier
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
name|oldMaxMergeAtOnceExplicit
init|=
name|mergePolicy
operator|.
name|getMaxMergeAtOnceExplicit
argument_list|()
decl_stmt|;
specifier|final
name|int
name|maxMergeAtOnceExplicit
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE_EXPLICIT
argument_list|,
name|oldMaxMergeAtOnceExplicit
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxMergeAtOnceExplicit
operator|!=
name|oldMaxMergeAtOnceExplicit
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [max_merge_at_once_explicit] from [{}] to [{}]"
argument_list|,
name|oldMaxMergeAtOnceExplicit
argument_list|,
name|maxMergeAtOnceExplicit
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setMaxMergeAtOnceExplicit
argument_list|(
name|maxMergeAtOnceExplicit
argument_list|)
expr_stmt|;
block|}
specifier|final
name|double
name|oldMaxMergedSegmentMB
init|=
name|mergePolicy
operator|.
name|getMaxMergedSegmentMB
argument_list|()
decl_stmt|;
specifier|final
name|ByteSizeValue
name|maxMergedSegment
init|=
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|INDEX_MERGE_POLICY_MAX_MERGED_SEGMENT
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxMergedSegment
operator|!=
literal|null
operator|&&
name|maxMergedSegment
operator|.
name|mbFrac
argument_list|()
operator|!=
name|oldMaxMergedSegmentMB
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [max_merged_segment] from [{}mb] to [{}]"
argument_list|,
name|oldMaxMergedSegmentMB
argument_list|,
name|maxMergedSegment
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setMaxMergedSegmentMB
argument_list|(
name|maxMergedSegment
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|double
name|oldReclaimDeletesWeight
init|=
name|mergePolicy
operator|.
name|getReclaimDeletesWeight
argument_list|()
decl_stmt|;
specifier|final
name|double
name|reclaimDeletesWeight
init|=
name|settings
operator|.
name|getAsDouble
argument_list|(
name|INDEX_MERGE_POLICY_RECLAIM_DELETES_WEIGHT
argument_list|,
name|oldReclaimDeletesWeight
argument_list|)
decl_stmt|;
if|if
condition|(
name|reclaimDeletesWeight
operator|!=
name|oldReclaimDeletesWeight
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [reclaim_deletes_weight] from [{}] to [{}]"
argument_list|,
name|oldReclaimDeletesWeight
argument_list|,
name|reclaimDeletesWeight
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setReclaimDeletesWeight
argument_list|(
name|reclaimDeletesWeight
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|parseNoCFSRatio
specifier|private
specifier|static
name|double
name|parseNoCFSRatio
parameter_list|(
name|String
name|noCFSRatio
parameter_list|)
block|{
name|noCFSRatio
operator|=
name|noCFSRatio
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|noCFSRatio
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
return|return
literal|1.0d
return|;
block|}
elseif|else
if|if
condition|(
name|noCFSRatio
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"false"
argument_list|)
condition|)
block|{
return|return
literal|0.0
return|;
block|}
else|else
block|{
try|try
block|{
name|double
name|value
init|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|noCFSRatio
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
argument_list|<
literal|0.0
operator|||
name|value
argument_list|>
literal|1.0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"NoCFSRatio must be in the interval [0..1] but was: ["
operator|+
name|value
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|value
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected a boolean or a value in the interval [0..1] but was: ["
operator|+
name|noCFSRatio
operator|+
literal|"]"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

