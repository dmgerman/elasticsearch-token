begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
package|;
end_package

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
name|cluster
operator|.
name|ClusterInfo
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
name|ClusterInfoService
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
name|metadata
operator|.
name|MetaData
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
name|node
operator|.
name|DiscoveryNodes
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
name|cluster
operator|.
name|routing
operator|.
name|RoutingTable
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
name|ClusterRebalanceAllocationDecider
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
name|test
operator|.
name|ESAllocationTestCase
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
name|Collections
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
name|*
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
name|nullValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RebalanceAfterActiveTests
specifier|public
class|class
name|RebalanceAfterActiveTests
extends|extends
name|ESAllocationTestCase
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|RebalanceAfterActiveTests
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
DECL|method|testRebalanceOnlyAfterAllShardsAreActive
specifier|public
name|void
name|testRebalanceOnlyAfterAllShardsAreActive
parameter_list|()
block|{
specifier|final
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[
literal|5
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
name|sizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|sizes
index|[
name|i
index|]
operator|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
name|AllocationService
name|strategy
init|=
name|createAllocationService
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.concurrent_recoveries"
argument_list|,
literal|10
argument_list|)
operator|.
name|put
argument_list|(
name|ClusterRebalanceAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_ALLOW_REBALANCE
argument_list|,
literal|"always"
argument_list|)
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.cluster_concurrent_rebalance"
argument_list|,
operator|-
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|ClusterInfoService
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterInfo
name|getClusterInfo
parameter_list|()
block|{
return|return
operator|new
name|ClusterInfo
argument_list|(
name|Collections
operator|.
name|EMPTY_MAP
argument_list|,
name|Collections
operator|.
name|EMPTY_MAP
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Long
name|getShardSize
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|index
argument_list|()
operator|.
name|equals
argument_list|(
literal|"test"
argument_list|)
condition|)
block|{
return|return
name|sizes
index|[
name|shardRouting
operator|.
name|getId
argument_list|()
index|]
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addListener
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{                     }
block|}
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Building initial routing table"
argument_list|)
expr_stmt|;
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test"
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|5
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|addAsNew
argument_list|(
name|metaData
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterName
operator|.
name|DEFAULT
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
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
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
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
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|UNASSIGNED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|UNASSIGNED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"start two nodes and fully start the shards"
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node1"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node2"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|RoutingTable
name|prevRoutingTable
init|=
name|routingTable
decl_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
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
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
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
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|primaryShard
argument_list|()
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|replicaShards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|UNASSIGNED
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"start all the primary shards, replicas will start initializing"
argument_list|)
expr_stmt|;
name|RoutingNodes
name|routingNodes
init|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
decl_stmt|;
name|prevRoutingTable
operator|=
name|routingTable
expr_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|routingNodes
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
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
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
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
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|primaryShard
argument_list|()
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|STARTED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|replicaShards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|replicaShards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|,
name|sizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"now, start 8 more nodes, and check that no rebalancing/relocation have happened"
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node3"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node4"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node5"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node6"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node7"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node8"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node9"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node10"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|prevRoutingTable
operator|=
name|routingTable
expr_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
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
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
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
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|primaryShard
argument_list|()
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|STARTED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|replicaShards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|replicaShards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|,
name|sizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"start the replica shards, rebalancing should start"
argument_list|)
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
expr_stmt|;
name|prevRoutingTable
operator|=
name|routingTable
expr_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|routingNodes
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
expr_stmt|;
comment|// we only allow one relocation at a time
name|assertThat
argument_list|(
name|routingTable
operator|.
name|shardsWithState
argument_list|(
name|STARTED
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|shardsWithState
argument_list|(
name|RELOCATING
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
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
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|num
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ShardRouting
name|routing
range|:
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|shards
argument_list|()
control|)
block|{
if|if
condition|(
name|routing
operator|.
name|state
argument_list|()
operator|==
name|RELOCATING
operator|||
name|routing
operator|.
name|state
argument_list|()
operator|==
name|INITIALIZING
condition|)
block|{
name|assertEquals
argument_list|(
name|routing
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|,
name|sizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|num
operator|++
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|num
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"complete relocation, other half of relocation should happen"
argument_list|)
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
expr_stmt|;
name|prevRoutingTable
operator|=
name|routingTable
expr_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|routingNodes
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
expr_stmt|;
comment|// we now only relocate 3, since 2 remain where they are!
name|assertThat
argument_list|(
name|routingTable
operator|.
name|shardsWithState
argument_list|(
name|STARTED
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|shardsWithState
argument_list|(
name|RELOCATING
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
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
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|ShardRouting
name|routing
range|:
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|i
argument_list|)
operator|.
name|shards
argument_list|()
control|)
block|{
if|if
condition|(
name|routing
operator|.
name|state
argument_list|()
operator|==
name|RELOCATING
operator|||
name|routing
operator|.
name|state
argument_list|()
operator|==
name|INITIALIZING
condition|)
block|{
name|assertEquals
argument_list|(
name|routing
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|,
name|sizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"complete relocation, thats it!"
argument_list|)
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
expr_stmt|;
name|prevRoutingTable
operator|=
name|routingTable
expr_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|routingNodes
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|shardsWithState
argument_list|(
name|STARTED
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure we have an even relocation
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|routingNodes
control|)
block|{
name|assertThat
argument_list|(
name|routingNode
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

