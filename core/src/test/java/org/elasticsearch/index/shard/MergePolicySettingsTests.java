begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
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
name|Index
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
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|MergePolicySettingsTests
specifier|public
class|class
name|MergePolicySettingsTests
extends|extends
name|ESTestCase
block|{
DECL|field|shardId
specifier|protected
specifier|final
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
annotation|@
name|Test
DECL|method|testCompoundFileSettings
specifier|public
name|void
name|testCompoundFileSettings
parameter_list|()
throws|throws
name|IOException
block|{
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|build
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|build
argument_list|(
literal|0.5
argument_list|)
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|build
argument_list|(
literal|1.0
argument_list|)
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|build
argument_list|(
literal|"true"
argument_list|)
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|build
argument_list|(
literal|"True"
argument_list|)
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|build
argument_list|(
literal|"False"
argument_list|)
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|build
argument_list|(
literal|"false"
argument_list|)
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|build
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|build
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|build
argument_list|(
literal|0.0
argument_list|)
argument_list|)
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoMerges
specifier|public
name|void
name|testNoMerges
parameter_list|()
block|{
name|MergePolicyConfig
name|mp
init|=
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_ENABLED
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|instanceof
name|NoMergePolicy
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testUpdateSettings
specifier|public
name|void
name|testUpdateSettings
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|IndexSettingsService
name|service
init|=
operator|new
name|IndexSettingsService
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
decl_stmt|;
name|MergePolicyConfig
name|mp
init|=
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|build
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|build
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|build
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testTieredMergePolicySettingsUpdate
specifier|public
name|void
name|testTieredMergePolicySettingsUpdate
parameter_list|()
throws|throws
name|IOException
block|{
name|MergePolicyConfig
name|mp
init|=
operator|new
name|MergePolicyConfig
argument_list|(
name|logger
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|.
name|getNoCFSRatio
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getForceMergeDeletesPctAllowed
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_EXPUNGE_DELETES_ALLOWED
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_EXPUNGE_DELETES_ALLOWED
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_EXPUNGE_DELETES_ALLOWED
operator|+
literal|1.0d
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getForceMergeDeletesPctAllowed
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_EXPUNGE_DELETES_ALLOWED
operator|+
literal|1.0d
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getFloorSegmentMB
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_FLOOR_SEGMENT
operator|.
name|mbFrac
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_FLOOR_SEGMENT
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|MergePolicyConfig
operator|.
name|DEFAULT_FLOOR_SEGMENT
operator|.
name|mb
argument_list|()
operator|+
literal|1
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getFloorSegmentMB
argument_list|()
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|MergePolicyConfig
operator|.
name|DEFAULT_FLOOR_SEGMENT
operator|.
name|mb
argument_list|()
operator|+
literal|1
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
operator|.
name|mbFrac
argument_list|()
argument_list|,
literal|0.001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getMaxMergeAtOnce
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGE_AT_ONCE
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGE_AT_ONCE
operator|-
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getMaxMergeAtOnce
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGE_AT_ONCE
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getMaxMergeAtOnceExplicit
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGE_AT_ONCE_EXPLICIT
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE_EXPLICIT
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGE_AT_ONCE_EXPLICIT
operator|-
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getMaxMergeAtOnceExplicit
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGE_AT_ONCE_EXPLICIT
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getMaxMergedSegmentMB
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGED_SEGMENT
operator|.
name|mbFrac
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_MAX_MERGED_SEGMENT
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGED_SEGMENT
operator|.
name|bytes
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getMaxMergedSegmentMB
argument_list|()
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGED_SEGMENT
operator|.
name|bytes
argument_list|()
operator|+
literal|1
argument_list|)
operator|.
name|mbFrac
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getReclaimDeletesWeight
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_RECLAIM_DELETES_WEIGHT
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_RECLAIM_DELETES_WEIGHT
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_RECLAIM_DELETES_WEIGHT
operator|+
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getReclaimDeletesWeight
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_RECLAIM_DELETES_WEIGHT
operator|+
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getSegmentsPerTier
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_SEGMENTS_PER_TIER
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_SEGMENTS_PER_TIER
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_SEGMENTS_PER_TIER
operator|+
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getSegmentsPerTier
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_SEGMENTS_PER_TIER
operator|+
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|mp
operator|.
name|onRefreshSettings
argument_list|(
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
comment|// update without the settings and see if we stick to the values
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getForceMergeDeletesPctAllowed
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_EXPUNGE_DELETES_ALLOWED
operator|+
literal|1.0d
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getFloorSegmentMB
argument_list|()
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|MergePolicyConfig
operator|.
name|DEFAULT_FLOOR_SEGMENT
operator|.
name|mb
argument_list|()
operator|+
literal|1
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
operator|.
name|mbFrac
argument_list|()
argument_list|,
literal|0.001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getMaxMergeAtOnce
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGE_AT_ONCE
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getMaxMergeAtOnceExplicit
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGE_AT_ONCE_EXPLICIT
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getMaxMergedSegmentMB
argument_list|()
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|MergePolicyConfig
operator|.
name|DEFAULT_MAX_MERGED_SEGMENT
operator|.
name|bytes
argument_list|()
operator|+
literal|1
argument_list|)
operator|.
name|mbFrac
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getReclaimDeletesWeight
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_RECLAIM_DELETES_WEIGHT
operator|+
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|TieredMergePolicy
operator|)
name|mp
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getSegmentsPerTier
argument_list|()
argument_list|,
name|MergePolicyConfig
operator|.
name|DEFAULT_SEGMENTS_PER_TIER
operator|+
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
DECL|method|build
specifier|public
name|Settings
name|build
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|value
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|build
specifier|public
name|Settings
name|build
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|value
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|build
specifier|public
name|Settings
name|build
parameter_list|(
name|int
name|value
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|value
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|build
specifier|public
name|Settings
name|build
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|value
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit
