begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|store
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
name|cluster
operator|.
name|ClusterService
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
name|ClusterState
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
name|ClusterStateUpdateTask
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
name|metadata
operator|.
name|IndexMetaData
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
name|*
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
name|Priority
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
name|ImmutableSettings
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
name|discovery
operator|.
name|DiscoveryService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|NodeEnvironment
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
name|ShardId
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
name|ElasticsearchIntegrationTest
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
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
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
name|InternalTestCluster
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|createIndexRequest
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
name|test
operator|.
name|ElasticsearchIntegrationTest
operator|.
name|Scope
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
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|IndicesStoreIntegrationTests
specifier|public
class|class
name|IndicesStoreIntegrationTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|SETTINGS
specifier|private
specifier|static
specifier|final
name|Settings
name|SETTINGS
init|=
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
decl_stmt|;
annotation|@
name|Test
DECL|method|shardsCleanup
specifier|public
name|void
name|shardsCleanup
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|node_1
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|SETTINGS
argument_list|)
decl_stmt|;
specifier|final
name|String
name|node_2
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|SETTINGS
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> creating index [test] with one shard and on replica"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|create
argument_list|(
name|createIndexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|settings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.numberOfReplicas"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.numberOfShards"
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> making sure that shard and its replica are allocated on node_1 and node_2"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardDirectory
argument_list|(
name|node_1
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardDirectory
argument_list|(
name|node_2
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|exists
argument_list|()
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
literal|"--> starting node server3"
argument_list|)
expr_stmt|;
name|String
name|node_3
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|SETTINGS
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> running cluster_health"
argument_list|)
expr_stmt|;
name|ClusterHealthResponse
name|clusterHealth
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"3"
argument_list|)
operator|.
name|setWaitForRelocatingShards
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> making sure that shard is not allocated on server3"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|waitForShardDeletion
argument_list|(
name|node_3
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|File
name|server2Shard
init|=
name|shardDirectory
argument_list|(
name|node_2
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> stopping node node_2"
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|stopRandomNode
argument_list|(
name|InternalTestCluster
operator|.
name|nameFilter
argument_list|(
name|node_2
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> running cluster_health"
argument_list|)
expr_stmt|;
name|clusterHealth
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"2"
argument_list|)
operator|.
name|setWaitForRelocatingShards
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
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
name|server2Shard
operator|.
name|exists
argument_list|()
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
literal|"--> making sure that shard and its replica exist on server1, server2 and server3"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardDirectory
argument_list|(
name|node_1
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|server2Shard
operator|.
name|exists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardDirectory
argument_list|(
name|node_3
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|exists
argument_list|()
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
literal|"--> starting node node_4"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|node_4
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|SETTINGS
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> running cluster_health"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> making sure that shard and its replica are allocated on server1 and server3 but not on server2"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardDirectory
argument_list|(
name|node_1
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardDirectory
argument_list|(
name|node_3
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|waitForShardDeletion
argument_list|(
name|node_4
argument_list|,
literal|"test"
argument_list|,
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|TestLogging
argument_list|(
literal|"indices.store:TRACE"
argument_list|)
DECL|method|testShardActiveElseWhere
specifier|public
name|void
name|testShardActiveElseWhere
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|node_1
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|SETTINGS
argument_list|)
decl_stmt|;
name|String
name|node_2
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|SETTINGS
argument_list|)
decl_stmt|;
specifier|final
name|String
name|node_1_id
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|DiscoveryService
operator|.
name|class
argument_list|,
name|node_1
argument_list|)
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
decl_stmt|;
specifier|final
name|String
name|node_2_id
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|DiscoveryService
operator|.
name|class
argument_list|,
name|node_2
argument_list|)
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numShards
init|=
name|scaledRandomIntBetween
argument_list|(
literal|2
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|numShards
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ClusterStateResponse
name|stateResponse
init|=
name|client
argument_list|()
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
name|get
argument_list|()
decl_stmt|;
name|RoutingNode
name|routingNode
init|=
name|stateResponse
operator|.
name|getState
argument_list|()
operator|.
name|routingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node_2_id
argument_list|)
decl_stmt|;
name|int
index|[]
name|node2Shards
init|=
operator|new
name|int
index|[
name|routingNode
operator|.
name|numberOfOwningShards
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|MutableShardRouting
name|mutableShardRouting
range|:
name|routingNode
control|)
block|{
name|node2Shards
index|[
name|i
operator|++
index|]
operator|=
name|mutableShardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Node 2 has shards: {}"
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|node2Shards
argument_list|)
argument_list|)
expr_stmt|;
name|waitNoPendingTasksOnAll
argument_list|()
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|,
name|node_2
argument_list|)
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"test"
argument_list|,
name|Priority
operator|.
name|IMMEDIATE
argument_list|,
operator|new
name|ClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
throws|throws
name|Exception
block|{
name|IndexRoutingTable
operator|.
name|Builder
name|indexRoutingTableBuilder
init|=
name|IndexRoutingTable
operator|.
name|builder
argument_list|(
literal|"test"
argument_list|)
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
name|numShards
condition|;
name|i
operator|++
control|)
block|{
name|indexRoutingTableBuilder
operator|.
name|addIndexShard
argument_list|(
operator|new
name|IndexShardRoutingTable
operator|.
name|Builder
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
name|i
argument_list|)
argument_list|,
literal|false
argument_list|)
operator|.
name|addShard
argument_list|(
operator|new
name|ImmutableShardRouting
argument_list|(
literal|"test"
argument_list|,
name|i
argument_list|,
name|node_1_id
argument_list|,
literal|true
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|add
argument_list|(
name|indexRoutingTableBuilder
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{             }
block|}
argument_list|)
expr_stmt|;
name|waitNoPendingTasksOnAll
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Checking if shards aren't removed"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|shard
range|:
name|node2Shards
control|)
block|{
name|assertTrue
argument_list|(
name|waitForShardDeletion
argument_list|(
name|node_2
argument_list|,
literal|"test"
argument_list|,
name|shard
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|shardDirectory
specifier|private
name|File
name|shardDirectory
parameter_list|(
name|String
name|server
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shard
parameter_list|)
block|{
name|NodeEnvironment
name|env
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|NodeEnvironment
operator|.
name|class
argument_list|,
name|server
argument_list|)
decl_stmt|;
return|return
name|env
operator|.
name|shardLocations
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|shard
argument_list|)
argument_list|)
index|[
literal|0
index|]
return|;
block|}
DECL|method|waitForShardDeletion
specifier|private
name|boolean
name|waitForShardDeletion
parameter_list|(
specifier|final
name|String
name|server
parameter_list|,
specifier|final
name|String
name|index
parameter_list|,
specifier|final
name|int
name|shard
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
operator|!
name|shardDirectory
argument_list|(
name|server
argument_list|,
name|index
argument_list|,
name|shard
argument_list|)
operator|.
name|exists
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|shardDirectory
argument_list|(
name|server
argument_list|,
name|index
argument_list|,
name|shard
argument_list|)
operator|.
name|exists
argument_list|()
return|;
block|}
block|}
end_class

end_unit

