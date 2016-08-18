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
name|allocation
operator|.
name|command
operator|.
name|AllocationCommands
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
name|command
operator|.
name|MoveAllocationCommand
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
name|cluster
operator|.
name|ESAllocationTestCase
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ExpectedShardSizeAllocationTests
specifier|public
class|class
name|ExpectedShardSizeAllocationTests
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
name|ExpectedShardSizeAllocationTests
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|testInitializingHasExpectedSize
specifier|public
name|void
name|testInitializingHasExpectedSize
parameter_list|()
block|{
specifier|final
name|long
name|byteSize
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|AllocationService
name|strategy
init|=
name|createAllocationService
argument_list|(
name|Settings
operator|.
name|EMPTY
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
argument_list|()
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
name|getIndexName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"test"
argument_list|)
operator|&&
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|byteSize
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
block|{             }
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
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
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
name|CLUSTER_NAME_SETTING
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
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
name|logger
operator|.
name|info
argument_list|(
literal|"Adding one node and performing rerouting"
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
name|add
argument_list|(
name|newNode
argument_list|(
literal|"node1"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
literal|"reroute"
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
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|numberOfShardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|byteSize
argument_list|,
name|clusterState
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Start the primary shard"
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
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|numberOfShardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|unassigned
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Add another one node and reroute"
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
name|add
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
name|routingTable
operator|=
name|strategy
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
literal|"reroute"
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
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|numberOfShardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|byteSize
argument_list|,
name|clusterState
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testExpectedSizeOnMove
specifier|public
name|void
name|testExpectedSizeOnMove
parameter_list|()
block|{
specifier|final
name|long
name|byteSize
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
specifier|final
name|AllocationService
name|allocation
init|=
name|createAllocationService
argument_list|(
name|Settings
operator|.
name|EMPTY
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
argument_list|()
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
name|getIndexName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"test"
argument_list|)
operator|&&
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|byteSize
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
block|{             }
block|}
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"creating an index with 1 shard, no replica"
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
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
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
name|CLUSTER_NAME_SETTING
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
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
name|logger
operator|.
name|info
argument_list|(
literal|"adding two nodes and performing rerouting"
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
name|add
argument_list|(
name|newNode
argument_list|(
literal|"node1"
argument_list|)
argument_list|)
operator|.
name|add
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
name|RoutingAllocation
operator|.
name|Result
name|rerouteResult
init|=
name|allocation
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
literal|"reroute"
argument_list|)
decl_stmt|;
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
name|rerouteResult
operator|.
name|routingTable
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"start primary shard"
argument_list|)
expr_stmt|;
name|rerouteResult
operator|=
name|allocation
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
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
name|routingTable
argument_list|(
name|rerouteResult
operator|.
name|routingTable
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"move the shard"
argument_list|)
expr_stmt|;
name|String
name|existingNodeId
init|=
name|clusterState
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
literal|0
argument_list|)
operator|.
name|primaryShard
argument_list|()
operator|.
name|currentNodeId
argument_list|()
decl_stmt|;
name|String
name|toNodeId
decl_stmt|;
if|if
condition|(
literal|"node1"
operator|.
name|equals
argument_list|(
name|existingNodeId
argument_list|)
condition|)
block|{
name|toNodeId
operator|=
literal|"node2"
expr_stmt|;
block|}
else|else
block|{
name|toNodeId
operator|=
literal|"node1"
expr_stmt|;
block|}
name|rerouteResult
operator|=
name|allocation
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
operator|new
name|AllocationCommands
argument_list|(
operator|new
name|MoveAllocationCommand
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|,
name|existingNodeId
argument_list|,
name|toNodeId
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rerouteResult
operator|.
name|changed
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
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|rerouteResult
operator|.
name|routingTable
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|existingNodeId
argument_list|)
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|state
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|RELOCATING
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|toNodeId
argument_list|)
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|state
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|existingNodeId
argument_list|)
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|,
name|byteSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|toNodeId
argument_list|)
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|,
name|byteSize
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"finish moving the shard"
argument_list|)
expr_stmt|;
name|rerouteResult
operator|=
name|allocation
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
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
name|routingTable
argument_list|(
name|rerouteResult
operator|.
name|routingTable
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|existingNodeId
argument_list|)
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
name|assertThat
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|toNodeId
argument_list|)
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
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
name|assertEquals
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|toNodeId
argument_list|)
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getExpectedShardSize
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

