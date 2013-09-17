begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway.local
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|local
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Predicate
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
name|util
operator|.
name|LuceneTestCase
operator|.
name|Slow
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
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthResponse
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
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthStatus
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
name|cluster
operator|.
name|state
operator|.
name|ClusterStateResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|Gateway
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|internal
operator|.
name|InternalNode
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
name|AbstractNodesTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|clusterHealthRequest
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
name|ImmutableSettings
operator|.
name|settingsBuilder
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
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchAllQuery
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
name|ElasticSearchAssertions
operator|.
name|assertHitCount
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
name|ElasticSearchAssertions
operator|.
name|assertNoFailures
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|QuorumLocalGatewayTests
specifier|public
class|class
name|QuorumLocalGatewayTests
extends|extends
name|AbstractNodesTests
block|{
annotation|@
name|After
DECL|method|cleanAndCloseNodes
specifier|public
name|void
name|cleanAndCloseNodes
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|node
argument_list|(
literal|"node"
operator|+
name|i
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|node
argument_list|(
literal|"node"
operator|+
name|i
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
comment|// since we store (by default) the index snapshot under the gateway, resetting it will reset the index data as well
operator|(
operator|(
name|InternalNode
operator|)
name|node
argument_list|(
literal|"node"
operator|+
name|i
argument_list|)
operator|)
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Gateway
operator|.
name|class
argument_list|)
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Slow
DECL|method|testChangeInitialShardsRecovery
specifier|public
name|void
name|testChangeInitialShardsRecovery
parameter_list|()
throws|throws
name|Exception
block|{
comment|// clean three nodes
name|logger
operator|.
name|info
argument_list|(
literal|"--> cleaning nodes"
argument_list|)
expr_stmt|;
name|buildNode
argument_list|(
literal|"node1"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|buildNode
argument_list|(
literal|"node2"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|buildNode
argument_list|(
literal|"node3"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|cleanAndCloseNodes
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting 3 nodes"
argument_list|)
expr_stmt|;
name|Node
name|node1
init|=
name|startNode
argument_list|(
literal|"node1"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Node
name|node2
init|=
name|startNode
argument_list|(
literal|"node2"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Node
name|node3
init|=
name|startNode
argument_list|(
literal|"node3"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexing..."
argument_list|)
expr_stmt|;
name|node1
operator|.
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|//We don't check for failures in the flush response: if we do we might get the following:
comment|// FlushNotAllowedEngineException[[test][1] recovery is in progress, flush [COMMIT_TRANSLOG] is not allowed]
name|node1
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|node1
operator|.
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|node1
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> running cluster_health (wait for the shards to startup)"
argument_list|)
expr_stmt|;
name|ClusterHealthResponse
name|clusterHealth
init|=
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForGreenStatus
argument_list|()
operator|.
name|waitForActiveShards
argument_list|(
literal|6
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> done cluster_health, status "
operator|+
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertHitCount
argument_list|(
name|node1
operator|.
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|2l
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> closing nodes"
argument_list|)
expr_stmt|;
name|closeAllNodes
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting 2 nodes back, should not do any recovery (less than quorum)"
argument_list|)
expr_stmt|;
name|node1
operator|=
name|startNode
argument_list|(
literal|"node1"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|node2
operator|=
name|startNode
argument_list|(
literal|"node2"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
name|ClusterStateResponse
name|clusterStateResponse
init|=
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|setMasterNodeTimeout
argument_list|(
literal|"500ms"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
operator|!
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|allPrimaryShardsActive
argument_list|()
return|;
block|}
block|}
argument_list|,
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> change the recovery.initial_shards setting, and make sure its recovered"
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"recovery.initial_shards"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> running cluster_health (wait for the shards to startup), 4 shards since we only have 2 nodes"
argument_list|)
expr_stmt|;
name|clusterHealth
operator|=
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForYellowStatus
argument_list|()
operator|.
name|waitForActiveShards
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> done cluster_health, status "
operator|+
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|YELLOW
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertHitCount
argument_list|(
name|node1
operator|.
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|2l
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
annotation|@
name|Slow
DECL|method|testQuorumRecovery
specifier|public
name|void
name|testQuorumRecovery
parameter_list|()
throws|throws
name|Exception
block|{
comment|// clean three nodes
name|logger
operator|.
name|info
argument_list|(
literal|"--> cleaning nodes"
argument_list|)
expr_stmt|;
name|buildNode
argument_list|(
literal|"node1"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|buildNode
argument_list|(
literal|"node2"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|buildNode
argument_list|(
literal|"node3"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|cleanAndCloseNodes
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting 3 nodes"
argument_list|)
expr_stmt|;
name|Node
name|node1
init|=
name|startNode
argument_list|(
literal|"node1"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Node
name|node2
init|=
name|startNode
argument_list|(
literal|"node2"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Node
name|node3
init|=
name|startNode
argument_list|(
literal|"node3"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexing..."
argument_list|)
expr_stmt|;
name|node1
operator|.
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|//We don't check for failures in the flush response: if we do we might get the following:
comment|// FlushNotAllowedEngineException[[test][1] recovery is in progress, flush [COMMIT_TRANSLOG] is not allowed]
name|node1
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|node1
operator|.
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|node1
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> running cluster_health (wait for the shards to startup)"
argument_list|)
expr_stmt|;
name|ClusterHealthResponse
name|clusterHealth
init|=
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForGreenStatus
argument_list|()
operator|.
name|waitForActiveShards
argument_list|(
literal|6
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> done cluster_health, status "
operator|+
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertHitCount
argument_list|(
name|node1
operator|.
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|2l
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> closing first node, and indexing more data to the second node"
argument_list|)
expr_stmt|;
name|closeNode
argument_list|(
literal|"node1"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> running cluster_health (wait for the shards to startup)"
argument_list|)
expr_stmt|;
name|ClusterHealthResponse
name|clusterHealth
init|=
name|client
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForYellowStatus
argument_list|()
operator|.
name|waitForNodes
argument_list|(
literal|"2"
argument_list|)
operator|.
name|waitForActiveShards
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> done cluster_health, status "
operator|+
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|clusterHealth
operator|.
name|isTimedOut
argument_list|()
operator|&&
name|clusterHealth
operator|.
name|getStatus
argument_list|()
operator|==
name|ClusterHealthStatus
operator|.
name|YELLOW
return|;
block|}
block|}
argument_list|,
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|node2
operator|.
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"value3"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|node2
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertHitCount
argument_list|(
name|node2
operator|.
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|3l
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> closing the second node and third node"
argument_list|)
expr_stmt|;
name|closeNode
argument_list|(
literal|"node2"
argument_list|)
expr_stmt|;
name|closeNode
argument_list|(
literal|"node3"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting the nodes back, verifying we got the latest version"
argument_list|)
expr_stmt|;
name|node1
operator|=
name|startNode
argument_list|(
literal|"node1"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|node2
operator|=
name|startNode
argument_list|(
literal|"node2"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|node3
operator|=
name|startNode
argument_list|(
literal|"node3"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> running cluster_health (wait for the shards to startup)"
argument_list|)
expr_stmt|;
name|clusterHealth
operator|=
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForGreenStatus
argument_list|()
operator|.
name|waitForActiveShards
argument_list|(
literal|6
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> done cluster_health, status "
operator|+
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertHitCount
argument_list|(
name|node1
operator|.
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|3l
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

