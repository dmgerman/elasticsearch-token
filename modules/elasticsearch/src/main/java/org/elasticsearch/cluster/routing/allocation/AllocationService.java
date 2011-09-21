begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|node
operator|.
name|DiscoveryNode
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
name|MutableShardRouting
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
name|allocator
operator|.
name|ShardsAllocators
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
name|AllocationDeciders
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
name|component
operator|.
name|AbstractComponent
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
name|node
operator|.
name|settings
operator|.
name|NodeSettingsService
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
name|Iterator
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
name|Set
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
name|collect
operator|.
name|Sets
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AllocationService
specifier|public
class|class
name|AllocationService
extends|extends
name|AbstractComponent
block|{
DECL|field|allocationDeciders
specifier|private
specifier|final
name|AllocationDeciders
name|allocationDeciders
decl_stmt|;
DECL|field|shardsAllocators
specifier|private
specifier|final
name|ShardsAllocators
name|shardsAllocators
decl_stmt|;
DECL|method|AllocationService
specifier|public
name|AllocationService
parameter_list|()
block|{
name|this
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
DECL|method|AllocationService
specifier|public
name|AllocationService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
argument_list|(
name|settings
argument_list|,
operator|new
name|AllocationDeciders
argument_list|(
name|settings
argument_list|,
operator|new
name|NodeSettingsService
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ShardsAllocators
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|AllocationService
annotation|@
name|Inject
specifier|public
name|AllocationService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|AllocationDeciders
name|allocationDeciders
parameter_list|,
name|ShardsAllocators
name|shardsAllocators
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|allocationDeciders
operator|=
name|allocationDeciders
expr_stmt|;
name|this
operator|.
name|shardsAllocators
operator|=
name|shardsAllocators
expr_stmt|;
block|}
comment|/**      * Applies the started shards. Note, shards can be called several times within this method.      *      *<p>If the same instance of the routing table is returned, then no change has been made.      */
DECL|method|applyStartedShards
specifier|public
name|RoutingAllocation
operator|.
name|Result
name|applyStartedShards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|ShardRouting
argument_list|>
name|startedShards
parameter_list|)
block|{
name|RoutingNodes
name|routingNodes
init|=
name|clusterState
operator|.
name|routingNodes
argument_list|()
decl_stmt|;
name|StartedRerouteAllocation
name|allocation
init|=
operator|new
name|StartedRerouteAllocation
argument_list|(
name|allocationDeciders
argument_list|,
name|routingNodes
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|,
name|startedShards
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
name|applyStartedShards
argument_list|(
name|routingNodes
argument_list|,
name|startedShards
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|changed
condition|)
block|{
return|return
operator|new
name|RoutingAllocation
operator|.
name|Result
argument_list|(
literal|false
argument_list|,
name|clusterState
operator|.
name|routingTable
argument_list|()
argument_list|,
name|allocation
operator|.
name|explanation
argument_list|()
argument_list|)
return|;
block|}
name|shardsAllocators
operator|.
name|applyStartedShards
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
name|reroute
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
return|return
operator|new
name|RoutingAllocation
operator|.
name|Result
argument_list|(
literal|true
argument_list|,
operator|new
name|RoutingTable
operator|.
name|Builder
argument_list|()
operator|.
name|updateNodes
argument_list|(
name|routingNodes
argument_list|)
operator|.
name|build
argument_list|()
operator|.
name|validateRaiseException
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
argument_list|)
argument_list|,
name|allocation
operator|.
name|explanation
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Applies the failed shards. Note, shards can be called several times within this method.      *      *<p>If the same instance of the routing table is returned, then no change has been made.      */
DECL|method|applyFailedShard
specifier|public
name|RoutingAllocation
operator|.
name|Result
name|applyFailedShard
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|ShardRouting
name|failedShard
parameter_list|)
block|{
name|RoutingNodes
name|routingNodes
init|=
name|clusterState
operator|.
name|routingNodes
argument_list|()
decl_stmt|;
name|FailedRerouteAllocation
name|allocation
init|=
operator|new
name|FailedRerouteAllocation
argument_list|(
name|allocationDeciders
argument_list|,
name|routingNodes
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|,
name|failedShard
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
name|applyFailedShard
argument_list|(
name|allocation
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|changed
condition|)
block|{
return|return
operator|new
name|RoutingAllocation
operator|.
name|Result
argument_list|(
literal|false
argument_list|,
name|clusterState
operator|.
name|routingTable
argument_list|()
argument_list|,
name|allocation
operator|.
name|explanation
argument_list|()
argument_list|)
return|;
block|}
name|shardsAllocators
operator|.
name|applyFailedShards
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
name|reroute
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
return|return
operator|new
name|RoutingAllocation
operator|.
name|Result
argument_list|(
literal|true
argument_list|,
operator|new
name|RoutingTable
operator|.
name|Builder
argument_list|()
operator|.
name|updateNodes
argument_list|(
name|routingNodes
argument_list|)
operator|.
name|build
argument_list|()
operator|.
name|validateRaiseException
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
argument_list|)
argument_list|,
name|allocation
operator|.
name|explanation
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Reroutes the routing table based on the live nodes.      *      *<p>If the same instance of the routing table is returned, then no change has been made.      */
DECL|method|reroute
specifier|public
name|RoutingAllocation
operator|.
name|Result
name|reroute
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|RoutingNodes
name|routingNodes
init|=
name|clusterState
operator|.
name|routingNodes
argument_list|()
decl_stmt|;
name|RoutingAllocation
name|allocation
init|=
operator|new
name|RoutingAllocation
argument_list|(
name|allocationDeciders
argument_list|,
name|routingNodes
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|reroute
argument_list|(
name|allocation
argument_list|)
condition|)
block|{
return|return
operator|new
name|RoutingAllocation
operator|.
name|Result
argument_list|(
literal|false
argument_list|,
name|clusterState
operator|.
name|routingTable
argument_list|()
argument_list|,
name|allocation
operator|.
name|explanation
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|new
name|RoutingAllocation
operator|.
name|Result
argument_list|(
literal|true
argument_list|,
operator|new
name|RoutingTable
operator|.
name|Builder
argument_list|()
operator|.
name|updateNodes
argument_list|(
name|routingNodes
argument_list|)
operator|.
name|build
argument_list|()
operator|.
name|validateRaiseException
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
argument_list|)
argument_list|,
name|allocation
operator|.
name|explanation
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Only handles reroute but *without* any reassignment of unassigned shards or rebalancing. Does      * make sure to handle removed nodes, but only moved the shards to UNASSIGNED, does not reassign      * them.      */
DECL|method|rerouteWithNoReassign
specifier|public
name|RoutingAllocation
operator|.
name|Result
name|rerouteWithNoReassign
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|RoutingNodes
name|routingNodes
init|=
name|clusterState
operator|.
name|routingNodes
argument_list|()
decl_stmt|;
name|RoutingAllocation
name|allocation
init|=
operator|new
name|RoutingAllocation
argument_list|(
name|allocationDeciders
argument_list|,
name|routingNodes
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|)
decl_stmt|;
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|dataNodes
init|=
name|allocation
operator|.
name|nodes
argument_list|()
operator|.
name|dataNodes
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
comment|// first, clear from the shards any node id they used to belong to that is now dead
name|changed
operator||=
name|deassociateDeadNodes
argument_list|(
name|allocation
operator|.
name|routingNodes
argument_list|()
argument_list|,
name|dataNodes
argument_list|)
expr_stmt|;
comment|// create a sorted list of from nodes with least number of shards to the maximum ones
name|applyNewNodes
argument_list|(
name|allocation
operator|.
name|routingNodes
argument_list|()
argument_list|,
name|dataNodes
argument_list|)
expr_stmt|;
comment|// elect primaries *before* allocating unassigned, so backups of primaries that failed
comment|// will be moved to primary state and not wait for primaries to be allocated and recovered (*from gateway*)
name|changed
operator||=
name|electPrimaries
argument_list|(
name|allocation
operator|.
name|routingNodes
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|changed
condition|)
block|{
return|return
operator|new
name|RoutingAllocation
operator|.
name|Result
argument_list|(
literal|false
argument_list|,
name|clusterState
operator|.
name|routingTable
argument_list|()
argument_list|,
name|allocation
operator|.
name|explanation
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|new
name|RoutingAllocation
operator|.
name|Result
argument_list|(
literal|true
argument_list|,
operator|new
name|RoutingTable
operator|.
name|Builder
argument_list|()
operator|.
name|updateNodes
argument_list|(
name|routingNodes
argument_list|)
operator|.
name|build
argument_list|()
operator|.
name|validateRaiseException
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
argument_list|)
argument_list|,
name|allocation
operator|.
name|explanation
argument_list|()
argument_list|)
return|;
block|}
DECL|method|reroute
specifier|private
name|boolean
name|reroute
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|dataNodes
init|=
name|allocation
operator|.
name|nodes
argument_list|()
operator|.
name|dataNodes
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
comment|// first, clear from the shards any node id they used to belong to that is now dead
name|changed
operator||=
name|deassociateDeadNodes
argument_list|(
name|allocation
operator|.
name|routingNodes
argument_list|()
argument_list|,
name|dataNodes
argument_list|)
expr_stmt|;
comment|// create a sorted list of from nodes with least number of shards to the maximum ones
name|applyNewNodes
argument_list|(
name|allocation
operator|.
name|routingNodes
argument_list|()
argument_list|,
name|dataNodes
argument_list|)
expr_stmt|;
comment|// elect primaries *before* allocating unassigned, so backups of primaries that failed
comment|// will be moved to primary state and not wait for primaries to be allocated and recovered (*from gateway*)
name|changed
operator||=
name|electPrimaries
argument_list|(
name|allocation
operator|.
name|routingNodes
argument_list|()
argument_list|)
expr_stmt|;
comment|// now allocate all the unassigned to available nodes
if|if
condition|(
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|hasUnassigned
argument_list|()
condition|)
block|{
name|changed
operator||=
name|shardsAllocators
operator|.
name|allocateUnassigned
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
comment|// elect primaries again, in case this is needed with unassigned allocation
name|changed
operator||=
name|electPrimaries
argument_list|(
name|allocation
operator|.
name|routingNodes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// move shards that no longer can be allocated
name|changed
operator||=
name|moveShards
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
comment|// rebalance
name|changed
operator||=
name|shardsAllocators
operator|.
name|rebalance
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
return|return
name|changed
return|;
block|}
DECL|method|moveShards
specifier|private
name|boolean
name|moveShards
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
comment|// create a copy of the shards interleaving between nodes, and check if they can remain
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shards
init|=
operator|new
name|ArrayList
argument_list|<
name|MutableShardRouting
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
name|boolean
name|found
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|found
condition|)
block|{
name|found
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|allocation
operator|.
name|routingNodes
argument_list|()
control|)
block|{
if|if
condition|(
name|index
operator|>=
name|routingNode
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|found
operator|=
literal|true
expr_stmt|;
name|shards
operator|.
name|add
argument_list|(
name|routingNode
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|index
operator|++
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|shards
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|MutableShardRouting
name|shardRouting
init|=
name|shards
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|// we can only move started shards...
if|if
condition|(
operator|!
name|shardRouting
operator|.
name|started
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|RoutingNode
name|routingNode
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|allocation
operator|.
name|deciders
argument_list|()
operator|.
name|canRemain
argument_list|(
name|shardRouting
argument_list|,
name|routingNode
argument_list|,
name|allocation
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}] allocated on [{}], but can no longer be allocated on it, moving..."
argument_list|,
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|id
argument_list|()
argument_list|,
name|routingNode
operator|.
name|node
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|moved
init|=
name|shardsAllocators
operator|.
name|move
argument_list|(
name|shardRouting
argument_list|,
name|routingNode
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|moved
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}] can't move"
argument_list|,
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|changed
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
return|return
name|changed
return|;
block|}
DECL|method|electPrimaries
specifier|private
name|boolean
name|electPrimaries
parameter_list|(
name|RoutingNodes
name|routingNodes
parameter_list|)
block|{
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
for|for
control|(
name|MutableShardRouting
name|shardEntry
range|:
name|routingNodes
operator|.
name|unassigned
argument_list|()
control|)
block|{
if|if
condition|(
name|shardEntry
operator|.
name|primary
argument_list|()
operator|&&
operator|!
name|shardEntry
operator|.
name|assignedToNode
argument_list|()
condition|)
block|{
name|boolean
name|elected
init|=
literal|false
decl_stmt|;
comment|// primary and not assigned, go over and find a replica that is assigned and active (since it might be relocating)
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|routingNodes
operator|.
name|nodesToShards
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|MutableShardRouting
name|shardEntry2
range|:
name|routingNode
operator|.
name|shards
argument_list|()
control|)
block|{
if|if
condition|(
name|shardEntry
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|shardEntry2
operator|.
name|shardId
argument_list|()
argument_list|)
operator|&&
name|shardEntry2
operator|.
name|active
argument_list|()
condition|)
block|{
assert|assert
name|shardEntry2
operator|.
name|assignedToNode
argument_list|()
assert|;
assert|assert
operator|!
name|shardEntry2
operator|.
name|primary
argument_list|()
assert|;
name|changed
operator|=
literal|true
expr_stmt|;
name|shardEntry
operator|.
name|moveFromPrimary
argument_list|()
expr_stmt|;
name|shardEntry2
operator|.
name|moveToPrimary
argument_list|()
expr_stmt|;
name|elected
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|elected
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
return|return
name|changed
return|;
block|}
comment|/**      * Applies the new nodes to the routing nodes and returns them (just the      * new nodes);      *      * @param liveNodes currently live nodes.      */
DECL|method|applyNewNodes
specifier|private
name|void
name|applyNewNodes
parameter_list|(
name|RoutingNodes
name|routingNodes
parameter_list|,
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|liveNodes
parameter_list|)
block|{
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|liveNodes
control|)
block|{
if|if
condition|(
operator|!
name|routingNodes
operator|.
name|nodesToShards
argument_list|()
operator|.
name|containsKey
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
name|RoutingNode
name|routingNode
init|=
operator|new
name|RoutingNode
argument_list|(
name|node
argument_list|)
decl_stmt|;
name|routingNodes
operator|.
name|nodesToShards
argument_list|()
operator|.
name|put
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|,
name|routingNode
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|deassociateDeadNodes
specifier|private
name|boolean
name|deassociateDeadNodes
parameter_list|(
name|RoutingNodes
name|routingNodes
parameter_list|,
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|liveNodes
parameter_list|)
block|{
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|liveNodeIds
init|=
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|liveNode
range|:
name|liveNodes
control|)
block|{
name|liveNodeIds
operator|.
name|add
argument_list|(
name|liveNode
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|nodeIdsToRemove
init|=
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|routingNodes
control|)
block|{
for|for
control|(
name|Iterator
argument_list|<
name|MutableShardRouting
argument_list|>
name|shardsIterator
init|=
name|routingNode
operator|.
name|shards
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|shardsIterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|MutableShardRouting
name|shardRoutingEntry
init|=
name|shardsIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardRoutingEntry
operator|.
name|assignedToNode
argument_list|()
condition|)
block|{
comment|// we store the relocation state here since when we call de-assign node
comment|// later on, we will loose this state
name|boolean
name|relocating
init|=
name|shardRoutingEntry
operator|.
name|relocating
argument_list|()
decl_stmt|;
name|String
name|relocatingNodeId
init|=
name|shardRoutingEntry
operator|.
name|relocatingNodeId
argument_list|()
decl_stmt|;
comment|// is this the destination shard that we are relocating an existing shard to?
comment|// we know this since it has a relocating node id (the node we relocate from) and our state is INITIALIZING (and not RELOCATING)
name|boolean
name|isRelocationDestinationShard
init|=
name|relocatingNodeId
operator|!=
literal|null
operator|&&
name|shardRoutingEntry
operator|.
name|initializing
argument_list|()
decl_stmt|;
name|boolean
name|currentNodeIsDead
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|!
name|liveNodeIds
operator|.
name|contains
argument_list|(
name|shardRoutingEntry
operator|.
name|currentNodeId
argument_list|()
argument_list|)
condition|)
block|{
name|changed
operator|=
literal|true
expr_stmt|;
name|nodeIdsToRemove
operator|.
name|add
argument_list|(
name|shardRoutingEntry
operator|.
name|currentNodeId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isRelocationDestinationShard
condition|)
block|{
name|routingNodes
operator|.
name|unassigned
argument_list|()
operator|.
name|add
argument_list|(
name|shardRoutingEntry
argument_list|)
expr_stmt|;
block|}
name|shardRoutingEntry
operator|.
name|deassignNode
argument_list|()
expr_stmt|;
name|currentNodeIsDead
operator|=
literal|true
expr_stmt|;
name|shardsIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
comment|// move source shard back to active state and cancel relocation mode.
if|if
condition|(
name|relocating
operator|&&
operator|!
name|liveNodeIds
operator|.
name|contains
argument_list|(
name|relocatingNodeId
argument_list|)
condition|)
block|{
name|nodeIdsToRemove
operator|.
name|add
argument_list|(
name|relocatingNodeId
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|currentNodeIsDead
condition|)
block|{
name|changed
operator|=
literal|true
expr_stmt|;
name|shardRoutingEntry
operator|.
name|cancelRelocation
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|isRelocationDestinationShard
operator|&&
operator|!
name|liveNodeIds
operator|.
name|contains
argument_list|(
name|relocatingNodeId
argument_list|)
condition|)
block|{
name|changed
operator|=
literal|true
expr_stmt|;
name|shardsIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
for|for
control|(
name|String
name|nodeIdToRemove
range|:
name|nodeIdsToRemove
control|)
block|{
name|routingNodes
operator|.
name|nodesToShards
argument_list|()
operator|.
name|remove
argument_list|(
name|nodeIdToRemove
argument_list|)
expr_stmt|;
block|}
return|return
name|changed
return|;
block|}
DECL|method|applyStartedShards
specifier|private
name|boolean
name|applyStartedShards
parameter_list|(
name|RoutingNodes
name|routingNodes
parameter_list|,
name|Iterable
argument_list|<
name|?
extends|extends
name|ShardRouting
argument_list|>
name|startedShardEntries
parameter_list|)
block|{
name|boolean
name|dirty
init|=
literal|false
decl_stmt|;
comment|// apply shards might be called several times with the same shard, ignore it
for|for
control|(
name|ShardRouting
name|startedShard
range|:
name|startedShardEntries
control|)
block|{
assert|assert
name|startedShard
operator|.
name|state
argument_list|()
operator|==
name|INITIALIZING
assert|;
comment|// retrieve the relocating node id before calling moveToStarted().
name|String
name|relocatingNodeId
init|=
literal|null
decl_stmt|;
name|RoutingNode
name|currentRoutingNode
init|=
name|routingNodes
operator|.
name|nodesToShards
argument_list|()
operator|.
name|get
argument_list|(
name|startedShard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentRoutingNode
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|MutableShardRouting
name|shard
range|:
name|currentRoutingNode
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|startedShard
operator|.
name|shardId
argument_list|()
argument_list|)
condition|)
block|{
name|relocatingNodeId
operator|=
name|shard
operator|.
name|relocatingNodeId
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|shard
operator|.
name|started
argument_list|()
condition|)
block|{
name|dirty
operator|=
literal|true
expr_stmt|;
name|shard
operator|.
name|moveToStarted
argument_list|()
expr_stmt|;
block|}
break|break;
block|}
block|}
block|}
comment|// startedShard is the current state of the shard (post relocation for example)
comment|// this means that after relocation, the state will be started and the currentNodeId will be
comment|// the node we relocated to
if|if
condition|(
name|relocatingNodeId
operator|==
literal|null
condition|)
continue|continue;
name|RoutingNode
name|sourceRoutingNode
init|=
name|routingNodes
operator|.
name|nodesToShards
argument_list|()
operator|.
name|get
argument_list|(
name|relocatingNodeId
argument_list|)
decl_stmt|;
if|if
condition|(
name|sourceRoutingNode
operator|!=
literal|null
condition|)
block|{
name|Iterator
argument_list|<
name|MutableShardRouting
argument_list|>
name|shardsIter
init|=
name|sourceRoutingNode
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|shardsIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|MutableShardRouting
name|shard
init|=
name|shardsIter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|shard
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|startedShard
operator|.
name|shardId
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|shard
operator|.
name|relocating
argument_list|()
condition|)
block|{
name|dirty
operator|=
literal|true
expr_stmt|;
name|shardsIter
operator|.
name|remove
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
block|}
return|return
name|dirty
return|;
block|}
comment|/**      * Applies the relevant logic to handle a failed shard. Returns<tt>true</tt> if changes happened that      * require relocation.      */
DECL|method|applyFailedShard
specifier|private
name|boolean
name|applyFailedShard
parameter_list|(
name|FailedRerouteAllocation
name|allocation
parameter_list|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|allocation
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
name|allocation
operator|.
name|failedShard
argument_list|()
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ShardRouting
name|failedShard
init|=
name|allocation
operator|.
name|failedShard
argument_list|()
decl_stmt|;
name|boolean
name|shardDirty
init|=
literal|false
decl_stmt|;
name|boolean
name|inRelocation
init|=
name|failedShard
operator|.
name|relocatingNodeId
argument_list|()
operator|!=
literal|null
decl_stmt|;
if|if
condition|(
name|inRelocation
condition|)
block|{
name|RoutingNode
name|routingNode
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|nodesToShards
argument_list|()
operator|.
name|get
argument_list|(
name|failedShard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|routingNode
operator|!=
literal|null
condition|)
block|{
name|Iterator
argument_list|<
name|MutableShardRouting
argument_list|>
name|shards
init|=
name|routingNode
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|shards
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|MutableShardRouting
name|shard
init|=
name|shards
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|shard
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|failedShard
operator|.
name|shardId
argument_list|()
argument_list|)
condition|)
block|{
name|shardDirty
operator|=
literal|true
expr_stmt|;
name|shard
operator|.
name|deassignNode
argument_list|()
expr_stmt|;
name|shards
operator|.
name|remove
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
name|String
name|nodeId
init|=
name|inRelocation
condition|?
name|failedShard
operator|.
name|relocatingNodeId
argument_list|()
else|:
name|failedShard
operator|.
name|currentNodeId
argument_list|()
decl_stmt|;
name|RoutingNode
name|currentRoutingNode
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|nodesToShards
argument_list|()
operator|.
name|get
argument_list|(
name|nodeId
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentRoutingNode
operator|==
literal|null
condition|)
block|{
comment|// already failed (might be called several times for the same shard)
return|return
literal|false
return|;
block|}
name|Iterator
argument_list|<
name|MutableShardRouting
argument_list|>
name|shards
init|=
name|currentRoutingNode
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|shards
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|MutableShardRouting
name|shard
init|=
name|shards
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|shard
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|failedShard
operator|.
name|shardId
argument_list|()
argument_list|)
condition|)
block|{
name|shardDirty
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|!
name|inRelocation
condition|)
block|{
name|shard
operator|.
name|deassignNode
argument_list|()
expr_stmt|;
name|shards
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|shard
operator|.
name|cancelRelocation
argument_list|()
expr_stmt|;
block|}
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|shardDirty
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// make sure we ignore this shard on the relevant node
name|allocation
operator|.
name|addIgnoreShardForNode
argument_list|(
name|failedShard
operator|.
name|shardId
argument_list|()
argument_list|,
name|failedShard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
expr_stmt|;
comment|// if in relocation no need to find a new target, just cancel the relocation.
if|if
condition|(
name|inRelocation
condition|)
block|{
return|return
literal|true
return|;
comment|// lets true, so we reroute in this case
block|}
comment|// add the failed shard to the unassigned shards
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|unassigned
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|MutableShardRouting
argument_list|(
name|failedShard
operator|.
name|index
argument_list|()
argument_list|,
name|failedShard
operator|.
name|id
argument_list|()
argument_list|,
literal|null
argument_list|,
name|failedShard
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|,
name|failedShard
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

