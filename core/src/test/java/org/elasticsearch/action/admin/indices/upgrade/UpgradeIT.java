begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.upgrade
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|upgrade
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|segments
operator|.
name|IndexSegments
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|segments
operator|.
name|IndexShardSegments
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|segments
operator|.
name|IndicesSegmentResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|segments
operator|.
name|ShardSegments
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|upgrade
operator|.
name|get
operator|.
name|IndexUpgradeStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|upgrade
operator|.
name|get
operator|.
name|UpgradeStatusResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
operator|.
name|IndexRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|decider
operator|.
name|ConcurrentRebalanceAllocationDecider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|decider
operator|.
name|EnableAllocationDecider
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
name|logging
operator|.
name|Loggers
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
name|index
operator|.
name|engine
operator|.
name|Segment
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
name|ESBackcompatTestCase
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
name|ESIntegTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertNoFailures
import|;
end_import

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|)
comment|// test scope since we set cluster wide settings
DECL|class|UpgradeIT
specifier|public
class|class
name|UpgradeIT
extends|extends
name|ESBackcompatTestCase
block|{
annotation|@
name|BeforeClass
DECL|method|checkUpgradeVersion
specifier|public
specifier|static
name|void
name|checkUpgradeVersion
parameter_list|()
block|{
specifier|final
name|boolean
name|luceneVersionMatches
init|=
operator|(
name|globalCompatibilityVersion
argument_list|()
operator|.
name|luceneVersion
operator|.
name|major
operator|==
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|major
operator|&&
name|globalCompatibilityVersion
argument_list|()
operator|.
name|luceneVersion
operator|.
name|minor
operator|==
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|minor
operator|)
decl_stmt|;
name|assumeFalse
argument_list|(
literal|"lucene versions must be different to run upgrade test"
argument_list|,
name|luceneVersionMatches
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|minExternalNodes
specifier|protected
name|int
name|minExternalNodes
parameter_list|()
block|{
return|return
literal|2
return|;
block|}
annotation|@
name|Override
DECL|method|maximumNumberOfReplicas
specifier|protected
name|int
name|maximumNumberOfReplicas
parameter_list|()
block|{
return|return
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|backwardsCluster
argument_list|()
operator|.
name|numBackwardsDataNodes
argument_list|()
argument_list|,
name|backwardsCluster
argument_list|()
operator|.
name|numNewDataNodes
argument_list|()
argument_list|)
operator|-
literal|1
argument_list|)
return|;
block|}
DECL|method|testUpgrade
specifier|public
name|void
name|testUpgrade
parameter_list|()
throws|throws
name|Exception
block|{
comment|// allow the cluster to rebalance quickly - 2 concurrent rebalance are default we can do higher
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|ConcurrentRebalanceAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setPersistentSettings
argument_list|(
name|builder
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|int
name|numIndexes
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|String
index|[]
name|indexNames
init|=
operator|new
name|String
index|[
name|numIndexes
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numIndexes
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|String
name|indexName
init|=
literal|"test"
operator|+
name|i
decl_stmt|;
name|indexNames
index|[
name|i
index|]
operator|=
name|indexName
expr_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.routing.allocation.exclude._name"
argument_list|,
name|backwardsCluster
argument_list|()
operator|.
name|newNodePattern
argument_list|()
argument_list|)
comment|// don't allow any merges so that we can check segments are upgraded
comment|// by the upgrader, and not just regular merging
operator|.
name|put
argument_list|(
literal|"index.merge.policy.segments_per_tier"
argument_list|,
literal|1000000f
argument_list|)
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|indexName
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|assertAllShardsOnNodes
argument_list|(
name|indexName
argument_list|,
name|backwardsCluster
argument_list|()
operator|.
name|backwardsNodePattern
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|100
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|docs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numDocs
condition|;
operator|++
name|j
control|)
block|{
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
decl_stmt|;
name|docs
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|indexName
argument_list|,
literal|"type1"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"sometext"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|docs
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|flush
argument_list|(
name|indexName
argument_list|)
operator|.
name|getFailedShards
argument_list|()
argument_list|)
expr_stmt|;
comment|// index more docs that won't be flushed
name|numDocs
operator|=
name|scaledRandomIntBetween
argument_list|(
literal|100
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|docs
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numDocs
condition|;
operator|++
name|j
control|)
block|{
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
decl_stmt|;
name|docs
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|indexName
argument_list|,
literal|"type2"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"someothertext"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|docs
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"--> Upgrading nodes"
argument_list|)
expr_stmt|;
name|backwardsCluster
argument_list|()
operator|.
name|allowOnAllNodes
argument_list|(
name|indexNames
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// disable allocation entirely until all nodes are upgraded
name|builder
operator|=
name|Settings
operator|.
name|builder
argument_list|()
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|EnableAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_ENABLE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|EnableAllocationDecider
operator|.
name|Allocation
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|builder
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|backwardsCluster
argument_list|()
operator|.
name|upgradeAllNodes
argument_list|()
expr_stmt|;
name|builder
operator|=
name|Settings
operator|.
name|builder
argument_list|()
expr_stmt|;
comment|// disable rebalanceing entirely for the time being otherwise we might get relocations / rebalance from nodes with old segments
name|builder
operator|.
name|put
argument_list|(
name|EnableAllocationDecider
operator|.
name|CLUSTER_ROUTING_REBALANCE_ENABLE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|EnableAllocationDecider
operator|.
name|Rebalance
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|EnableAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_ENABLE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|EnableAllocationDecider
operator|.
name|Allocation
operator|.
name|ALL
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|builder
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> Nodes upgrade complete"
argument_list|)
expr_stmt|;
name|logSegmentsState
argument_list|()
expr_stmt|;
name|assertNotUpgraded
argument_list|(
name|client
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|indexToUpgrade
init|=
literal|"test"
operator|+
name|randomInt
argument_list|(
name|numIndexes
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|// This test fires up another node running an older version of ES, but because wire protocol changes across major ES versions, it
comment|// means we can never generate ancient segments in this test (unless Lucene major version bumps but ES major version does not):
name|assertFalse
argument_list|(
name|hasAncientSegments
argument_list|(
name|client
argument_list|()
argument_list|,
name|indexToUpgrade
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> Running upgrade on index {}"
argument_list|,
name|indexToUpgrade
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpgrade
argument_list|(
name|indexToUpgrade
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|awaitBusy
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
block|{
return|return
name|isUpgraded
argument_list|(
name|client
argument_list|()
argument_list|,
name|indexToUpgrade
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|ExceptionsHelper
operator|.
name|convertToRuntime
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> Single index upgrade complete"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> Running upgrade on the rest of the indexes"
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpgrade
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|logSegmentsState
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> Full upgrade complete"
argument_list|)
expr_stmt|;
name|assertUpgraded
argument_list|(
name|client
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|assertNotUpgraded
specifier|public
specifier|static
name|void
name|assertNotUpgraded
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
modifier|...
name|index
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|IndexUpgradeStatus
name|status
range|:
name|getUpgradeStatus
argument_list|(
name|client
argument_list|,
name|index
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should not be zero sized"
argument_list|,
name|status
operator|.
name|getTotalBytes
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
comment|// TODO: it would be better for this to be strictly greater, but sometimes an extra flush
comment|// mysteriously happens after the second round of docs are indexed
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should have recovered some segments from transaction log"
argument_list|,
name|status
operator|.
name|getTotalBytes
argument_list|()
operator|>=
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should need upgrading"
argument_list|,
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertNoAncientSegments
specifier|public
specifier|static
name|void
name|assertNoAncientSegments
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
modifier|...
name|index
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|IndexUpgradeStatus
name|status
range|:
name|getUpgradeStatus
argument_list|(
name|client
argument_list|,
name|index
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should not be zero sized"
argument_list|,
name|status
operator|.
name|getTotalBytes
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
comment|// TODO: it would be better for this to be strictly greater, but sometimes an extra flush
comment|// mysteriously happens after the second round of docs are indexed
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should not have any ancient segments"
argument_list|,
name|status
operator|.
name|getToUpgradeBytesAncient
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should have recovered some segments from transaction log"
argument_list|,
name|status
operator|.
name|getTotalBytes
argument_list|()
operator|>=
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should need upgrading"
argument_list|,
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Returns true if there are any ancient segments. */
DECL|method|hasAncientSegments
specifier|public
specifier|static
name|boolean
name|hasAncientSegments
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
name|index
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|IndexUpgradeStatus
name|status
range|:
name|getUpgradeStatus
argument_list|(
name|client
argument_list|,
name|index
argument_list|)
control|)
block|{
if|if
condition|(
name|status
operator|.
name|getToUpgradeBytesAncient
argument_list|()
operator|!=
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/** Returns true if there are any old but not ancient segments. */
DECL|method|hasOldButNotAncientSegments
specifier|public
specifier|static
name|boolean
name|hasOldButNotAncientSegments
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
name|index
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|IndexUpgradeStatus
name|status
range|:
name|getUpgradeStatus
argument_list|(
name|client
argument_list|,
name|index
argument_list|)
control|)
block|{
if|if
condition|(
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
operator|>
name|status
operator|.
name|getToUpgradeBytesAncient
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|assertUpgraded
specifier|public
specifier|static
name|void
name|assertUpgraded
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
modifier|...
name|index
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|IndexUpgradeStatus
name|status
range|:
name|getUpgradeStatus
argument_list|(
name|client
argument_list|,
name|index
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should not be zero sized"
argument_list|,
name|status
operator|.
name|getTotalBytes
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should be upgraded"
argument_list|,
literal|0
argument_list|,
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// double check using the segments api that all segments are actually upgraded
name|IndicesSegmentResponse
name|segsRsp
decl_stmt|;
if|if
condition|(
name|index
operator|==
literal|null
condition|)
block|{
name|segsRsp
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareSegments
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|segsRsp
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareSegments
argument_list|(
name|index
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|IndexSegments
name|indexSegments
range|:
name|segsRsp
operator|.
name|getIndices
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardSegments
name|shard
range|:
name|indexSegments
control|)
block|{
for|for
control|(
name|ShardSegments
name|segs
range|:
name|shard
operator|.
name|getShards
argument_list|()
control|)
block|{
for|for
control|(
name|Segment
name|seg
range|:
name|segs
operator|.
name|getSegments
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
literal|"Index "
operator|+
name|indexSegments
operator|.
name|getIndex
argument_list|()
operator|+
literal|" has unupgraded segment "
operator|+
name|seg
operator|.
name|toString
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|major
argument_list|,
name|seg
operator|.
name|version
operator|.
name|major
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Index "
operator|+
name|indexSegments
operator|.
name|getIndex
argument_list|()
operator|+
literal|" has unupgraded segment "
operator|+
name|seg
operator|.
name|toString
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|minor
argument_list|,
name|seg
operator|.
name|version
operator|.
name|minor
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|isUpgraded
specifier|static
name|boolean
name|isUpgraded
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
name|index
parameter_list|)
throws|throws
name|Exception
block|{
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|UpgradeIT
operator|.
name|class
argument_list|)
decl_stmt|;
name|int
name|toUpgrade
init|=
literal|0
decl_stmt|;
for|for
control|(
name|IndexUpgradeStatus
name|status
range|:
name|getUpgradeStatus
argument_list|(
name|client
argument_list|,
name|index
argument_list|)
control|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Index: {}, total: {}, toUpgrade: {}"
argument_list|,
name|status
operator|.
name|getIndex
argument_list|()
argument_list|,
name|status
operator|.
name|getTotalBytes
argument_list|()
argument_list|,
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
argument_list|)
expr_stmt|;
name|toUpgrade
operator|+=
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
expr_stmt|;
block|}
return|return
name|toUpgrade
operator|==
literal|0
return|;
block|}
DECL|class|UpgradeStatus
specifier|static
class|class
name|UpgradeStatus
block|{
DECL|field|indexName
specifier|public
specifier|final
name|String
name|indexName
decl_stmt|;
DECL|field|totalBytes
specifier|public
specifier|final
name|int
name|totalBytes
decl_stmt|;
DECL|field|toUpgradeBytes
specifier|public
specifier|final
name|int
name|toUpgradeBytes
decl_stmt|;
DECL|field|toUpgradeBytesAncient
specifier|public
specifier|final
name|int
name|toUpgradeBytesAncient
decl_stmt|;
DECL|method|UpgradeStatus
specifier|public
name|UpgradeStatus
parameter_list|(
name|String
name|indexName
parameter_list|,
name|int
name|totalBytes
parameter_list|,
name|int
name|toUpgradeBytes
parameter_list|,
name|int
name|toUpgradeBytesAncient
parameter_list|)
block|{
name|this
operator|.
name|indexName
operator|=
name|indexName
expr_stmt|;
name|this
operator|.
name|totalBytes
operator|=
name|totalBytes
expr_stmt|;
name|this
operator|.
name|toUpgradeBytes
operator|=
name|toUpgradeBytes
expr_stmt|;
name|this
operator|.
name|toUpgradeBytesAncient
operator|=
name|toUpgradeBytesAncient
expr_stmt|;
assert|assert
name|toUpgradeBytesAncient
operator|<=
name|toUpgradeBytes
assert|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|getUpgradeStatus
specifier|static
name|Collection
argument_list|<
name|IndexUpgradeStatus
argument_list|>
name|getUpgradeStatus
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
modifier|...
name|indices
parameter_list|)
throws|throws
name|Exception
block|{
name|UpgradeStatusResponse
name|upgradeStatusResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpgradeStatus
argument_list|(
name|indices
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|upgradeStatusResponse
argument_list|)
expr_stmt|;
return|return
name|upgradeStatusResponse
operator|.
name|getIndices
argument_list|()
operator|.
name|values
argument_list|()
return|;
block|}
block|}
end_class

end_unit

