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
name|ClusterName
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
name|AllocationId
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
name|ShardRoutingState
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
name|TestShardRouting
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
name|test
operator|.
name|ESAllocationTestCase
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

begin_class
DECL|class|StartedShardsRoutingTests
specifier|public
class|class
name|StartedShardsRoutingTests
extends|extends
name|ESAllocationTestCase
block|{
DECL|method|testStartedShardsMatching
specifier|public
name|void
name|testStartedShardsMatching
parameter_list|()
block|{
name|AllocationService
name|allocation
init|=
name|createAllocationService
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> building initial cluster state"
argument_list|)
expr_stmt|;
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
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
literal|3
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|Index
name|index
init|=
name|indexMetaData
operator|.
name|getIndex
argument_list|()
decl_stmt|;
name|ClusterState
operator|.
name|Builder
name|stateBuilder
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|ClusterName
operator|.
name|DEFAULT
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
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexMetaData
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|ShardRouting
name|initShard
init|=
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|index
argument_list|,
literal|0
argument_list|,
literal|"node1"
argument_list|,
literal|true
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
decl_stmt|;
specifier|final
name|ShardRouting
name|startedShard
init|=
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|index
argument_list|,
literal|1
argument_list|,
literal|"node2"
argument_list|,
literal|true
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
decl_stmt|;
specifier|final
name|ShardRouting
name|relocatingShard
init|=
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|index
argument_list|,
literal|2
argument_list|,
literal|"node1"
argument_list|,
literal|"node2"
argument_list|,
literal|true
argument_list|,
name|ShardRoutingState
operator|.
name|RELOCATING
argument_list|)
decl_stmt|;
name|stateBuilder
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
name|IndexRoutingTable
operator|.
name|builder
argument_list|(
name|index
argument_list|)
operator|.
name|addIndexShard
argument_list|(
operator|new
name|IndexShardRoutingTable
operator|.
name|Builder
argument_list|(
name|initShard
operator|.
name|shardId
argument_list|()
argument_list|)
operator|.
name|addShard
argument_list|(
name|initShard
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|addIndexShard
argument_list|(
operator|new
name|IndexShardRoutingTable
operator|.
name|Builder
argument_list|(
name|startedShard
operator|.
name|shardId
argument_list|()
argument_list|)
operator|.
name|addShard
argument_list|(
name|startedShard
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|addIndexShard
argument_list|(
operator|new
name|IndexShardRoutingTable
operator|.
name|Builder
argument_list|(
name|relocatingShard
operator|.
name|shardId
argument_list|()
argument_list|)
operator|.
name|addShard
argument_list|(
name|relocatingShard
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|ClusterState
name|state
init|=
name|stateBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> test starting of shard"
argument_list|)
expr_stmt|;
name|RoutingAllocation
operator|.
name|Result
name|result
init|=
name|allocation
operator|.
name|applyStartedShards
argument_list|(
name|state
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|initShard
operator|.
name|index
argument_list|()
argument_list|,
name|initShard
operator|.
name|id
argument_list|()
argument_list|,
name|initShard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|initShard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|,
name|initShard
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|,
name|initShard
operator|.
name|allocationId
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"failed to start "
operator|+
name|initShard
operator|+
literal|"\ncurrent routing table:"
operator|+
name|result
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|,
name|result
operator|.
name|changed
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|initShard
operator|+
literal|"isn't started \ncurrent routing table:"
operator|+
name|result
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|,
name|result
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|initShard
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|allShardsStarted
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> testing shard variants that shouldn't match the initializing shard"
argument_list|)
expr_stmt|;
name|result
operator|=
name|allocation
operator|.
name|applyStartedShards
argument_list|(
name|state
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|initShard
operator|.
name|index
argument_list|()
argument_list|,
name|initShard
operator|.
name|id
argument_list|()
argument_list|,
name|initShard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|initShard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|,
name|initShard
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"wrong allocation id flag shouldn't start shard "
operator|+
name|initShard
operator|+
literal|"\ncurrent routing table:"
operator|+
name|result
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|,
name|result
operator|.
name|changed
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|=
name|allocation
operator|.
name|applyStartedShards
argument_list|(
name|state
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|initShard
operator|.
name|index
argument_list|()
argument_list|,
name|initShard
operator|.
name|id
argument_list|()
argument_list|,
literal|"some_node"
argument_list|,
name|initShard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|initShard
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|,
name|AllocationId
operator|.
name|newTargetRelocation
argument_list|(
name|AllocationId
operator|.
name|newRelocation
argument_list|(
name|initShard
operator|.
name|allocationId
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"relocating shard from node shouldn't start shard "
operator|+
name|initShard
operator|+
literal|"\ncurrent routing table:"
operator|+
name|result
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|,
name|result
operator|.
name|changed
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> testing double starting"
argument_list|)
expr_stmt|;
name|result
operator|=
name|allocation
operator|.
name|applyStartedShards
argument_list|(
name|state
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|startedShard
operator|.
name|index
argument_list|()
argument_list|,
name|startedShard
operator|.
name|id
argument_list|()
argument_list|,
name|startedShard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|startedShard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|,
name|startedShard
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|,
name|startedShard
operator|.
name|allocationId
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"duplicate starting of the same shard should be ignored \ncurrent routing table:"
operator|+
name|result
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|,
name|result
operator|.
name|changed
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> testing starting of relocating shards"
argument_list|)
expr_stmt|;
specifier|final
name|AllocationId
name|targetAllocationId
init|=
name|AllocationId
operator|.
name|newTargetRelocation
argument_list|(
name|relocatingShard
operator|.
name|allocationId
argument_list|()
argument_list|)
decl_stmt|;
name|result
operator|=
name|allocation
operator|.
name|applyStartedShards
argument_list|(
name|state
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|relocatingShard
operator|.
name|index
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|id
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|,
name|targetAllocationId
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"failed to start "
operator|+
name|relocatingShard
operator|+
literal|"\ncurrent routing table:"
operator|+
name|result
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|,
name|result
operator|.
name|changed
argument_list|()
argument_list|)
expr_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|result
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
name|relocatingShard
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|getShards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|shardRouting
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"node2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardRouting
operator|.
name|relocatingNodeId
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> testing shard variants that shouldn't match the initializing relocating shard"
argument_list|)
expr_stmt|;
name|result
operator|=
name|allocation
operator|.
name|applyStartedShards
argument_list|(
name|state
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|relocatingShard
operator|.
name|index
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|id
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"wrong allocation id shouldn't start shard"
operator|+
name|relocatingShard
operator|+
literal|"\ncurrent routing table:"
operator|+
name|result
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|,
name|result
operator|.
name|changed
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|=
name|allocation
operator|.
name|applyStartedShards
argument_list|(
name|state
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
name|relocatingShard
operator|.
name|index
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|id
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|relocatingShard
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|,
name|relocatingShard
operator|.
name|allocationId
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"wrong allocation id shouldn't start shard even if relocatingId==shard.id"
operator|+
name|relocatingShard
operator|+
literal|"\ncurrent routing table:"
operator|+
name|result
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|,
name|result
operator|.
name|changed
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

