begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.merge.policy
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|merge
operator|.
name|policy
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
name|SegmentInfos
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
name|ElasticSearchException
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
name|inject
operator|.
name|Inject
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|settings
operator|.
name|IndexSettingsService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|AbstractIndexShardComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|Store
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArraySet
import|;
end_import

begin_class
DECL|class|TieredMergePolicyProvider
specifier|public
class|class
name|TieredMergePolicyProvider
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|MergePolicyProvider
argument_list|<
name|TieredMergePolicy
argument_list|>
block|{
DECL|field|indexSettingsService
specifier|private
specifier|final
name|IndexSettingsService
name|indexSettingsService
decl_stmt|;
DECL|field|policies
specifier|private
specifier|final
name|Set
argument_list|<
name|CustomTieredMergePolicyProvider
argument_list|>
name|policies
init|=
operator|new
name|CopyOnWriteArraySet
argument_list|<
name|CustomTieredMergePolicyProvider
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|compoundFormat
specifier|private
specifier|volatile
name|boolean
name|compoundFormat
decl_stmt|;
DECL|field|forceMergeDeletesPctAllowed
specifier|private
specifier|volatile
name|double
name|forceMergeDeletesPctAllowed
decl_stmt|;
DECL|field|floorSegment
specifier|private
specifier|volatile
name|ByteSizeValue
name|floorSegment
decl_stmt|;
DECL|field|maxMergeAtOnce
specifier|private
specifier|volatile
name|int
name|maxMergeAtOnce
decl_stmt|;
DECL|field|maxMergeAtOnceExplicit
specifier|private
specifier|volatile
name|int
name|maxMergeAtOnceExplicit
decl_stmt|;
DECL|field|maxMergedSegment
specifier|private
specifier|volatile
name|ByteSizeValue
name|maxMergedSegment
decl_stmt|;
DECL|field|segmentsPerTier
specifier|private
specifier|volatile
name|double
name|segmentsPerTier
decl_stmt|;
DECL|field|reclaimDeletesWeight
specifier|private
specifier|volatile
name|double
name|reclaimDeletesWeight
decl_stmt|;
DECL|field|asyncMerge
specifier|private
name|boolean
name|asyncMerge
decl_stmt|;
DECL|field|applySettings
specifier|private
specifier|final
name|ApplySettings
name|applySettings
init|=
operator|new
name|ApplySettings
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|TieredMergePolicyProvider
specifier|public
name|TieredMergePolicyProvider
parameter_list|(
name|Store
name|store
parameter_list|,
name|IndexSettingsService
name|indexSettingsService
parameter_list|)
block|{
name|super
argument_list|(
name|store
operator|.
name|shardId
argument_list|()
argument_list|,
name|store
operator|.
name|indexSettings
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexSettingsService
operator|=
name|indexSettingsService
expr_stmt|;
name|this
operator|.
name|compoundFormat
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|store
operator|.
name|suggestUseCompoundFile
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|asyncMerge
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"index.merge.async"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|forceMergeDeletesPctAllowed
operator|=
name|componentSettings
operator|.
name|getAsDouble
argument_list|(
literal|"expunge_deletes_allowed"
argument_list|,
literal|10d
argument_list|)
expr_stmt|;
comment|// percentage
name|this
operator|.
name|floorSegment
operator|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"floor_segment"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|2
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxMergeAtOnce
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"max_merge_at_once"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxMergeAtOnceExplicit
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"max_merge_at_once_explicit"
argument_list|,
literal|30
argument_list|)
expr_stmt|;
comment|// TODO is this really a good default number for max_merge_segment, what happens for large indices, won't they end up with many segments?
name|this
operator|.
name|maxMergedSegment
operator|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"max_merged_segment"
argument_list|,
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"max_merge_segment"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|5
argument_list|,
name|ByteSizeUnit
operator|.
name|GB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|segmentsPerTier
operator|=
name|componentSettings
operator|.
name|getAsDouble
argument_list|(
literal|"segments_per_tier"
argument_list|,
literal|10.0d
argument_list|)
expr_stmt|;
name|this
operator|.
name|reclaimDeletesWeight
operator|=
name|componentSettings
operator|.
name|getAsDouble
argument_list|(
literal|"reclaim_deletes_weight"
argument_list|,
literal|2.0d
argument_list|)
expr_stmt|;
name|fixSettingsIfNeeded
argument_list|()
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using [tiered] merge policy with expunge_deletes_allowed[{}], floor_segment[{}], max_merge_at_once[{}], max_merge_at_once_explicit[{}], max_merged_segment[{}], segments_per_tier[{}], reclaim_deletes_weight[{}], async_merge[{}]"
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
argument_list|,
name|asyncMerge
argument_list|)
expr_stmt|;
name|indexSettingsService
operator|.
name|addListener
argument_list|(
name|applySettings
argument_list|)
expr_stmt|;
block|}
DECL|method|fixSettingsIfNeeded
specifier|private
name|void
name|fixSettingsIfNeeded
parameter_list|()
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
literal|"[tiered] merge policy changing max_merge_at_once from [{}] to [{}] because segments_per_tier [{}] has to be higher or equal to it"
argument_list|,
name|maxMergeAtOnce
argument_list|,
name|newMaxMergeAtOnce
argument_list|,
name|segmentsPerTier
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxMergeAtOnce
operator|=
name|newMaxMergeAtOnce
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|newMergePolicy
specifier|public
name|TieredMergePolicy
name|newMergePolicy
parameter_list|()
block|{
name|CustomTieredMergePolicyProvider
name|mergePolicy
decl_stmt|;
if|if
condition|(
name|asyncMerge
condition|)
block|{
name|mergePolicy
operator|=
operator|new
name|EnableMergeTieredMergePolicyProvider
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mergePolicy
operator|=
operator|new
name|CustomTieredMergePolicyProvider
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
name|mergePolicy
operator|.
name|setUseCompoundFile
argument_list|(
name|compoundFormat
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
return|return
name|mergePolicy
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|delete
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|indexSettingsService
operator|.
name|removeListener
argument_list|(
name|applySettings
argument_list|)
expr_stmt|;
block|}
DECL|field|INDEX_MERGE_POLICY_EXPUNGE_DELETES_ALLOWED
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_MERGE_POLICY_EXPUNGE_DELETES_ALLOWED
init|=
literal|"index.merge.policy.expunge_deletes_allowed"
decl_stmt|;
DECL|field|INDEX_MERGE_POLICY_FLOOR_SEGMENT
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_MERGE_POLICY_FLOOR_SEGMENT
init|=
literal|"index.merge.policy.floor_segment"
decl_stmt|;
DECL|field|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE
init|=
literal|"index.merge.policy.max_merge_at_once"
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
DECL|field|INDEX_COMPOUND_FORMAT
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_COMPOUND_FORMAT
init|=
literal|"index.compound_format"
decl_stmt|;
DECL|class|ApplySettings
class|class
name|ApplySettings
implements|implements
name|IndexSettingsService
operator|.
name|Listener
block|{
annotation|@
name|Override
DECL|method|onRefreshSettings
specifier|public
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|double
name|expungeDeletesPctAllowed
init|=
name|settings
operator|.
name|getAsDouble
argument_list|(
name|INDEX_MERGE_POLICY_EXPUNGE_DELETES_ALLOWED
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|forceMergeDeletesPctAllowed
argument_list|)
decl_stmt|;
if|if
condition|(
name|expungeDeletesPctAllowed
operator|!=
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|forceMergeDeletesPctAllowed
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [expunge_deletes_allowed] from [{}] to [{}]"
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|forceMergeDeletesPctAllowed
argument_list|,
name|expungeDeletesPctAllowed
argument_list|)
expr_stmt|;
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|forceMergeDeletesPctAllowed
operator|=
name|expungeDeletesPctAllowed
expr_stmt|;
for|for
control|(
name|CustomTieredMergePolicyProvider
name|policy
range|:
name|policies
control|)
block|{
name|policy
operator|.
name|setForceMergeDeletesPctAllowed
argument_list|(
name|expungeDeletesPctAllowed
argument_list|)
expr_stmt|;
block|}
block|}
name|ByteSizeValue
name|floorSegment
init|=
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|INDEX_MERGE_POLICY_FLOOR_SEGMENT
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|floorSegment
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|floorSegment
operator|.
name|equals
argument_list|(
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|floorSegment
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [floor_segment] from [{}] to [{}]"
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|floorSegment
argument_list|,
name|floorSegment
argument_list|)
expr_stmt|;
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|floorSegment
operator|=
name|floorSegment
expr_stmt|;
for|for
control|(
name|CustomTieredMergePolicyProvider
name|policy
range|:
name|policies
control|)
block|{
name|policy
operator|.
name|setFloorSegmentMB
argument_list|(
name|floorSegment
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|maxMergeAtOnce
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergeAtOnce
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxMergeAtOnce
operator|!=
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergeAtOnce
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [max_merge_at_once] from [{}] to [{}]"
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergeAtOnce
argument_list|,
name|maxMergeAtOnce
argument_list|)
expr_stmt|;
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergeAtOnce
operator|=
name|maxMergeAtOnce
expr_stmt|;
for|for
control|(
name|CustomTieredMergePolicyProvider
name|policy
range|:
name|policies
control|)
block|{
name|policy
operator|.
name|setMaxMergeAtOnce
argument_list|(
name|maxMergeAtOnce
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|maxMergeAtOnceExplicit
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE_EXPLICIT
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergeAtOnceExplicit
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxMergeAtOnceExplicit
operator|!=
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergeAtOnceExplicit
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [max_merge_at_once_explicit] from [{}] to [{}]"
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergeAtOnceExplicit
argument_list|,
name|maxMergeAtOnceExplicit
argument_list|)
expr_stmt|;
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergeAtOnceExplicit
operator|=
name|maxMergeAtOnceExplicit
expr_stmt|;
for|for
control|(
name|CustomTieredMergePolicyProvider
name|policy
range|:
name|policies
control|)
block|{
name|policy
operator|.
name|setMaxMergeAtOnceExplicit
argument_list|(
name|maxMergeAtOnceExplicit
argument_list|)
expr_stmt|;
block|}
block|}
name|ByteSizeValue
name|maxMergedSegment
init|=
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|INDEX_MERGE_POLICY_MAX_MERGED_SEGMENT
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergedSegment
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|maxMergedSegment
operator|.
name|equals
argument_list|(
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergedSegment
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [max_merged_segment] from [{}] to [{}]"
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergedSegment
argument_list|,
name|maxMergedSegment
argument_list|)
expr_stmt|;
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|maxMergedSegment
operator|=
name|maxMergedSegment
expr_stmt|;
for|for
control|(
name|CustomTieredMergePolicyProvider
name|policy
range|:
name|policies
control|)
block|{
name|policy
operator|.
name|setFloorSegmentMB
argument_list|(
name|maxMergedSegment
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|double
name|segmentsPerTier
init|=
name|settings
operator|.
name|getAsDouble
argument_list|(
name|INDEX_MERGE_POLICY_SEGMENTS_PER_TIER
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|segmentsPerTier
argument_list|)
decl_stmt|;
if|if
condition|(
name|segmentsPerTier
operator|!=
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|segmentsPerTier
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [segments_per_tier] from [{}] to [{}]"
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|segmentsPerTier
argument_list|,
name|segmentsPerTier
argument_list|)
expr_stmt|;
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|segmentsPerTier
operator|=
name|segmentsPerTier
expr_stmt|;
for|for
control|(
name|CustomTieredMergePolicyProvider
name|policy
range|:
name|policies
control|)
block|{
name|policy
operator|.
name|setSegmentsPerTier
argument_list|(
name|segmentsPerTier
argument_list|)
expr_stmt|;
block|}
block|}
name|double
name|reclaimDeletesWeight
init|=
name|settings
operator|.
name|getAsDouble
argument_list|(
name|INDEX_MERGE_POLICY_RECLAIM_DELETES_WEIGHT
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|reclaimDeletesWeight
argument_list|)
decl_stmt|;
if|if
condition|(
name|reclaimDeletesWeight
operator|!=
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|reclaimDeletesWeight
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [reclaim_deletes_weight] from [{}] to [{}]"
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|reclaimDeletesWeight
argument_list|,
name|reclaimDeletesWeight
argument_list|)
expr_stmt|;
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|reclaimDeletesWeight
operator|=
name|reclaimDeletesWeight
expr_stmt|;
for|for
control|(
name|CustomTieredMergePolicyProvider
name|policy
range|:
name|policies
control|)
block|{
name|policy
operator|.
name|setReclaimDeletesWeight
argument_list|(
name|reclaimDeletesWeight
argument_list|)
expr_stmt|;
block|}
block|}
name|boolean
name|compoundFormat
init|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|compoundFormat
argument_list|)
decl_stmt|;
if|if
condition|(
name|compoundFormat
operator|!=
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|compoundFormat
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating index.compound_format from [{}] to [{}]"
argument_list|,
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|compoundFormat
argument_list|,
name|compoundFormat
argument_list|)
expr_stmt|;
name|TieredMergePolicyProvider
operator|.
name|this
operator|.
name|compoundFormat
operator|=
name|compoundFormat
expr_stmt|;
for|for
control|(
name|CustomTieredMergePolicyProvider
name|policy
range|:
name|policies
control|)
block|{
name|policy
operator|.
name|setUseCompoundFile
argument_list|(
name|compoundFormat
argument_list|)
expr_stmt|;
block|}
block|}
name|fixSettingsIfNeeded
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|CustomTieredMergePolicyProvider
specifier|public
specifier|static
class|class
name|CustomTieredMergePolicyProvider
extends|extends
name|TieredMergePolicy
block|{
DECL|field|provider
specifier|private
specifier|final
name|TieredMergePolicyProvider
name|provider
decl_stmt|;
DECL|method|CustomTieredMergePolicyProvider
specifier|public
name|CustomTieredMergePolicyProvider
parameter_list|(
name|TieredMergePolicyProvider
name|provider
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|provider
operator|=
name|provider
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
name|provider
operator|.
name|policies
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|EnableMergeTieredMergePolicyProvider
specifier|public
specifier|static
class|class
name|EnableMergeTieredMergePolicyProvider
extends|extends
name|CustomTieredMergePolicyProvider
block|{
DECL|method|EnableMergeTieredMergePolicyProvider
specifier|public
name|EnableMergeTieredMergePolicyProvider
parameter_list|(
name|TieredMergePolicyProvider
name|provider
parameter_list|)
block|{
name|super
argument_list|(
name|provider
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|findMerges
specifier|public
name|MergePolicy
operator|.
name|MergeSpecification
name|findMerges
parameter_list|(
name|MergeTrigger
name|trigger
parameter_list|,
name|SegmentInfos
name|infos
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we don't enable merges while indexing documents, we do them in the background
if|if
condition|(
name|trigger
operator|==
name|MergeTrigger
operator|.
name|SEGMENT_FLUSH
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|super
operator|.
name|findMerges
argument_list|(
name|trigger
argument_list|,
name|infos
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

