begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indexlifecycle
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indexlifecycle
package|;
end_package

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
name|indices
operator|.
name|create
operator|.
name|CreateIndexResponse
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
name|delete
operator|.
name|DeleteIndexResponse
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
name|routing
operator|.
name|RoutingNode
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
name|RoutingNodes
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
name|Discovery
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
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|StreamSupport
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRoutingState
operator|.
name|INITIALIZING
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRoutingState
operator|.
name|RELOCATING
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRoutingState
operator|.
name|STARTED
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
name|ESIntegTestCase
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
name|anyOf
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
name|containsInAnyOrder
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|not
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
name|nullValue
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
DECL|class|IndexLifecycleActionIT
specifier|public
class|class
name|IndexLifecycleActionIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Test
DECL|method|testIndexLifecycleActionsWith11Shards1Backup
specifier|public
name|void
name|testIndexLifecycleActionsWith11Shards1Backup
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|11
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// start one server
name|logger
operator|.
name|info
argument_list|(
literal|"Starting sever1"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|server_1
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settings
argument_list|)
decl_stmt|;
specifier|final
name|String
name|node1
init|=
name|getLocalNodeId
argument_list|(
name|server_1
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Creating index [test]"
argument_list|)
expr_stmt|;
name|CreateIndexResponse
name|createIndexResponse
init|=
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
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|createIndexResponse
operator|.
name|isAcknowledged
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
literal|"Running Cluster Health"
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForYellowStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Done Cluster Health, status "
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
name|ClusterState
name|clusterState
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
operator|.
name|getState
argument_list|()
decl_stmt|;
name|RoutingNode
name|routingNodeEntry1
init|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry1
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Starting server2"
argument_list|)
expr_stmt|;
comment|// start another server
name|String
name|server_2
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settings
argument_list|)
decl_stmt|;
comment|// first wait for 2 nodes in the cluster
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
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
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForGreenStatus
argument_list|()
operator|.
name|waitForNodes
argument_list|(
literal|"2"
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
literal|"Done Cluster Health, status "
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
specifier|final
name|String
name|node2
init|=
name|getLocalNodeId
argument_list|(
name|server_2
argument_list|)
decl_stmt|;
comment|// explicitly call reroute, so shards will get relocated to the new node (we delay it in ES in case other nodes join)
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareReroute
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForGreenStatus
argument_list|()
operator|.
name|waitForNodes
argument_list|(
literal|"2"
argument_list|)
operator|.
name|waitForRelocatingShards
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|actionGet
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
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getNumberOfDataNodes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getInitializingShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getUnassignedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getRelocatingShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getActiveShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|22
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getActivePrimaryShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
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
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|assertNodesPresent
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
argument_list|,
name|node1
argument_list|,
name|node2
argument_list|)
expr_stmt|;
name|routingNodeEntry1
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node1
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry1
operator|.
name|numberOfShardsWithState
argument_list|(
name|RELOCATING
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry1
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|RoutingNode
name|routingNodeEntry2
init|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node2
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry2
operator|.
name|numberOfShardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry2
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Starting server3"
argument_list|)
expr_stmt|;
comment|// start another server
name|String
name|server_3
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settings
argument_list|)
decl_stmt|;
comment|// first wait for 3 nodes in the cluster
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
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForGreenStatus
argument_list|()
operator|.
name|waitForNodes
argument_list|(
literal|"3"
argument_list|)
argument_list|)
operator|.
name|actionGet
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
specifier|final
name|String
name|node3
init|=
name|getLocalNodeId
argument_list|(
name|server_3
argument_list|)
decl_stmt|;
comment|// explicitly call reroute, so shards will get relocated to the new node (we delay it in ES in case other nodes join)
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareReroute
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForGreenStatus
argument_list|()
operator|.
name|waitForNodes
argument_list|(
literal|"3"
argument_list|)
operator|.
name|waitForRelocatingShards
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|actionGet
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
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getNumberOfDataNodes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getInitializingShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getUnassignedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getRelocatingShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getActiveShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|22
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getActivePrimaryShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
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
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|assertNodesPresent
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
argument_list|,
name|node1
argument_list|,
name|node2
argument_list|,
name|node3
argument_list|)
expr_stmt|;
name|routingNodeEntry1
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node1
argument_list|)
expr_stmt|;
name|routingNodeEntry2
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node2
argument_list|)
expr_stmt|;
name|RoutingNode
name|routingNodeEntry3
init|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node3
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry1
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
operator|+
name|routingNodeEntry2
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
operator|+
name|routingNodeEntry3
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|22
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry1
operator|.
name|numberOfShardsWithState
argument_list|(
name|RELOCATING
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry1
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry2
operator|.
name|numberOfShardsWithState
argument_list|(
name|RELOCATING
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry2
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry3
operator|.
name|numberOfShardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry3
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Closing server1"
argument_list|)
expr_stmt|;
comment|// kill the first server
name|internalCluster
argument_list|()
operator|.
name|stopRandomNode
argument_list|(
name|InternalTestCluster
operator|.
name|nameFilter
argument_list|(
name|server_1
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify health
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
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
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForGreenStatus
argument_list|()
operator|.
name|waitForNodes
argument_list|(
literal|"2"
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
literal|"Done Cluster Health, status "
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
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareReroute
argument_list|()
operator|.
name|get
argument_list|()
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
name|health
argument_list|(
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForGreenStatus
argument_list|()
operator|.
name|waitForRelocatingShards
argument_list|(
literal|0
argument_list|)
operator|.
name|waitForNodes
argument_list|(
literal|"2"
argument_list|)
argument_list|)
operator|.
name|actionGet
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
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getRelocatingShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getActiveShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|22
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getActivePrimaryShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
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
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|assertNodesPresent
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
argument_list|,
name|node3
argument_list|,
name|node2
argument_list|)
expr_stmt|;
name|routingNodeEntry2
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node2
argument_list|)
expr_stmt|;
name|routingNodeEntry3
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node3
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry2
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
operator|+
name|routingNodeEntry3
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|22
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry2
operator|.
name|numberOfShardsWithState
argument_list|(
name|RELOCATING
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry2
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry3
operator|.
name|numberOfShardsWithState
argument_list|(
name|RELOCATING
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry3
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Deleting index [test]"
argument_list|)
expr_stmt|;
comment|// last, lets delete the index
name|DeleteIndexResponse
name|deleteIndexResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|deleteIndexResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
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
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|assertNodesPresent
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
argument_list|,
name|node3
argument_list|,
name|node2
argument_list|)
expr_stmt|;
name|routingNodeEntry2
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry2
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|routingNodeEntry3
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|node3
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodeEntry3
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getLocalNodeId
specifier|private
name|String
name|getLocalNodeId
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Discovery
name|discovery
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Discovery
operator|.
name|class
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|String
name|nodeId
init|=
name|discovery
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|nodeId
argument_list|,
name|not
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|nodeId
return|;
block|}
DECL|method|assertNodesPresent
specifier|private
name|void
name|assertNodesPresent
parameter_list|(
name|RoutingNodes
name|routingNodes
parameter_list|,
name|String
modifier|...
name|nodes
parameter_list|)
block|{
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|keySet
init|=
name|StreamSupport
operator|.
name|stream
argument_list|(
name|routingNodes
operator|.
name|spliterator
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|map
argument_list|(
parameter_list|(
name|p
parameter_list|)
lambda|->
operator|(
name|p
operator|.
name|nodeId
argument_list|()
operator|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|keySet
argument_list|,
name|containsInAnyOrder
argument_list|(
name|nodes
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

