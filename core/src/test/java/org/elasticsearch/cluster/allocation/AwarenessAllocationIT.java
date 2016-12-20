begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.allocation
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|allocation
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectIntHashMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
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
name|IndexRoutingTable
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
name|IndexShardRoutingTable
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
name|ShardRouting
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
name|AwarenessAllocationDecider
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
name|discovery
operator|.
name|zen
operator|.
name|ZenDiscovery
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
name|java
operator|.
name|util
operator|.
name|List
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
name|equalTo
import|;
end_import

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|,
name|minNumDataNodes
operator|=
literal|2
argument_list|)
DECL|class|AwarenessAllocationIT
specifier|public
class|class
name|AwarenessAllocationIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|AwarenessAllocationIT
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|numberOfReplicas
specifier|protected
name|int
name|numberOfReplicas
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
DECL|method|testSimpleAwareness
specifier|public
name|void
name|testSimpleAwareness
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|commonSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.awareness.attributes"
argument_list|,
literal|"rack_id"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting 2 nodes on the same rack"
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNodes
argument_list|(
literal|2
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|commonSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.attr.rack_id"
argument_list|,
literal|"rack_1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test1"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test2"
argument_list|)
expr_stmt|;
name|NumShards
name|test1
init|=
name|getNumShards
argument_list|(
literal|"test1"
argument_list|)
decl_stmt|;
name|NumShards
name|test2
init|=
name|getNumShards
argument_list|(
literal|"test2"
argument_list|)
decl_stmt|;
comment|//no replicas will be allocated as both indices end up on a single node
specifier|final
name|int
name|totalPrimaries
init|=
name|test1
operator|.
name|numPrimaries
operator|+
name|test2
operator|.
name|numPrimaries
decl_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting 1 node on a different rack"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|node3
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|commonSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.attr.rack_id"
argument_list|,
literal|"rack_2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
comment|// On slow machines the initial relocation might be delayed
name|assertThat
argument_list|(
name|awaitBusy
argument_list|(
parameter_list|()
lambda|->
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for no relocation"
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
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"3"
argument_list|)
operator|.
name|setWaitForNoRelocatingShards
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|clusterHealth
operator|.
name|isTimedOut
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> checking current state"
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
comment|// verify that we have all the primaries on node3
name|ObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|counts
init|=
operator|new
name|ObjectIntHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|clusterState
operator|.
name|routingTable
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
name|counts
operator|.
name|addTo
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|counts
operator|.
name|get
argument_list|(
name|node3
argument_list|)
operator|==
name|totalPrimaries
return|;
block|}
argument_list|,
literal|10
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
block|}
DECL|method|testAwarenessZones
specifier|public
name|void
name|testAwarenessZones
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|commonSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|AwarenessAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_AWARENESS_FORCE_GROUP_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|"zone.values"
argument_list|,
literal|"a,b"
argument_list|)
operator|.
name|put
argument_list|(
name|AwarenessAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_AWARENESS_ATTRIBUTE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"zone"
argument_list|)
operator|.
name|put
argument_list|(
name|ZenDiscovery
operator|.
name|JOIN_TIMEOUT_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"10s"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting 4 nodes on different zones"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|internalCluster
argument_list|()
operator|.
name|startNodes
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|commonSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.attr.zone"
argument_list|,
literal|"a"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|commonSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.attr.zone"
argument_list|,
literal|"b"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|commonSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.attr.zone"
argument_list|,
literal|"b"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|commonSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.attr.zone"
argument_list|,
literal|"a"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|A_0
init|=
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|B_0
init|=
name|nodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|B_1
init|=
name|nodes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|String
name|A_1
init|=
name|nodes
operator|.
name|get
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for nodes to form a cluster"
argument_list|)
expr_stmt|;
name|ClusterHealthResponse
name|health
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
literal|"4"
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
name|health
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
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|5
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for shards to be allocated"
argument_list|)
expr_stmt|;
name|health
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNoRelocatingShards
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|health
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
name|ObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|counts
init|=
operator|new
name|ObjectIntHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|clusterState
operator|.
name|routingTable
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
name|counts
operator|.
name|addTo
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|A_1
argument_list|)
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|B_1
argument_list|)
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|A_0
argument_list|)
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|B_0
argument_list|)
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAwarenessZonesIncrementalNodes
specifier|public
name|void
name|testAwarenessZonesIncrementalNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|commonSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.awareness.force.zone.values"
argument_list|,
literal|"a,b"
argument_list|)
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.awareness.attributes"
argument_list|,
literal|"zone"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting 2 nodes on zones 'a'& 'b'"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|internalCluster
argument_list|()
operator|.
name|startNodes
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|commonSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.attr.zone"
argument_list|,
literal|"a"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|commonSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.attr.zone"
argument_list|,
literal|"b"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|A_0
init|=
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|B_0
init|=
name|nodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|5
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ClusterHealthResponse
name|health
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
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"2"
argument_list|)
operator|.
name|setWaitForNoRelocatingShards
argument_list|(
literal|true
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
name|health
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
name|ObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|counts
init|=
operator|new
name|ObjectIntHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|clusterState
operator|.
name|routingTable
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
name|counts
operator|.
name|addTo
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|A_0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|B_0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting another node in zone 'b'"
argument_list|)
expr_stmt|;
name|String
name|B_1
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|commonSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.attr.zone"
argument_list|,
literal|"b"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|health
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"3"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|health
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
name|health
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"3"
argument_list|)
operator|.
name|setWaitForActiveShards
argument_list|(
literal|10
argument_list|)
operator|.
name|setWaitForNoRelocatingShards
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|health
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|counts
operator|=
operator|new
name|ObjectIntHashMap
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|clusterState
operator|.
name|routingTable
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
name|counts
operator|.
name|addTo
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|A_0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|B_0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|B_1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|noZoneNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|()
decl_stmt|;
name|health
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"4"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|health
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
name|health
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"4"
argument_list|)
operator|.
name|setWaitForActiveShards
argument_list|(
literal|10
argument_list|)
operator|.
name|setWaitForNoRelocatingShards
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|health
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|counts
operator|=
operator|new
name|ObjectIntHashMap
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|clusterState
operator|.
name|routingTable
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
name|counts
operator|.
name|addTo
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|A_0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|B_0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|B_1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|containsKey
argument_list|(
name|noZoneNode
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.awareness.attributes"
argument_list|,
literal|""
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|health
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"4"
argument_list|)
operator|.
name|setWaitForActiveShards
argument_list|(
literal|10
argument_list|)
operator|.
name|setWaitForNoRelocatingShards
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|health
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|counts
operator|=
operator|new
name|ObjectIntHashMap
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|clusterState
operator|.
name|routingTable
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
name|counts
operator|.
name|addTo
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|A_0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|B_0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|B_1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|counts
operator|.
name|get
argument_list|(
name|noZoneNode
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

