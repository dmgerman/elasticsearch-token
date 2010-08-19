begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.strategy
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|strategy
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
DECL|class|ShardsRoutingStrategy
specifier|public
class|class
name|ShardsRoutingStrategy
extends|extends
name|AbstractComponent
block|{
DECL|field|preferUnallocatedStrategy
specifier|private
specifier|final
name|PreferUnallocatedStrategy
name|preferUnallocatedStrategy
decl_stmt|;
DECL|method|ShardsRoutingStrategy
specifier|public
name|ShardsRoutingStrategy
parameter_list|()
block|{
name|this
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|ShardsRoutingStrategy
annotation|@
name|Inject
specifier|public
name|ShardsRoutingStrategy
parameter_list|(
name|Settings
name|settings
parameter_list|,
annotation|@
name|Nullable
name|PreferUnallocatedStrategy
name|preferUnallocatedStrategy
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|preferUnallocatedStrategy
operator|=
name|preferUnallocatedStrategy
expr_stmt|;
block|}
DECL|method|preferUnallocatedStrategy
specifier|public
name|PreferUnallocatedStrategy
name|preferUnallocatedStrategy
parameter_list|()
block|{
return|return
name|preferUnallocatedStrategy
return|;
block|}
comment|/**      * Applies the started shards. Note, shards can be called several times within this method.      *      *<p>If the same instance of the routing table is returned, then no change has been made.      */
DECL|method|applyStartedShards
specifier|public
name|RoutingTable
name|applyStartedShards
parameter_list|(
name|ClusterState
name|clusterState
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
name|RoutingNodes
name|routingNodes
init|=
name|clusterState
operator|.
name|routingNodes
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|applyStartedShards
argument_list|(
name|routingNodes
argument_list|,
name|startedShardEntries
argument_list|)
condition|)
block|{
return|return
name|clusterState
operator|.
name|routingTable
argument_list|()
return|;
block|}
name|reroute
argument_list|(
name|routingNodes
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|)
expr_stmt|;
return|return
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
return|;
block|}
comment|/**      * Applies the failed shards. Note, shards can be called several times within this method.      *      *<p>If the same instance of the routing table is returned, then no change has been made.      */
DECL|method|applyFailedShards
specifier|public
name|RoutingTable
name|applyFailedShards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|Iterable
argument_list|<
name|?
extends|extends
name|ShardRouting
argument_list|>
name|failedShardEntries
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
if|if
condition|(
operator|!
name|applyFailedShards
argument_list|(
name|routingNodes
argument_list|,
name|failedShardEntries
argument_list|)
condition|)
block|{
return|return
name|clusterState
operator|.
name|routingTable
argument_list|()
return|;
block|}
comment|// If we reroute again, the failed shard will try and be assigned to the same node, which we do no do in the applyFailedShards
comment|//        reroute(routingNodes, clusterState.nodes());
return|return
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
return|;
block|}
comment|/**      * Reroutes the routing table based on the live nodes.      *      *<p>If the same instance of the routing table is returned, then no change has been made.      */
DECL|method|reroute
specifier|public
name|RoutingTable
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
if|if
condition|(
operator|!
name|reroute
argument_list|(
name|routingNodes
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|clusterState
operator|.
name|routingTable
argument_list|()
return|;
block|}
return|return
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
return|;
block|}
DECL|method|reroute
specifier|private
name|boolean
name|reroute
parameter_list|(
name|RoutingNodes
name|routingNodes
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|)
block|{
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|dataNodes
init|=
name|nodes
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
name|routingNodes
argument_list|,
name|dataNodes
argument_list|)
expr_stmt|;
comment|// create a sorted list of from nodes with least number of shards to the maximum ones
name|applyNewNodes
argument_list|(
name|routingNodes
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
name|routingNodes
argument_list|)
expr_stmt|;
comment|// now allocate all the unassigned to available nodes
if|if
condition|(
name|routingNodes
operator|.
name|hasUnassigned
argument_list|()
condition|)
block|{
if|if
condition|(
name|preferUnallocatedStrategy
operator|!=
literal|null
condition|)
block|{
name|changed
operator||=
name|preferUnallocatedStrategy
operator|.
name|allocateUnassigned
argument_list|(
name|routingNodes
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
block|}
name|changed
operator||=
name|allocateUnassigned
argument_list|(
name|routingNodes
argument_list|)
expr_stmt|;
comment|// elect primaries again, in case this is needed with unassigned allocation
name|changed
operator||=
name|electPrimaries
argument_list|(
name|routingNodes
argument_list|)
expr_stmt|;
block|}
comment|// rebalance
name|changed
operator||=
name|rebalance
argument_list|(
name|routingNodes
argument_list|)
expr_stmt|;
return|return
name|changed
return|;
block|}
DECL|method|rebalance
specifier|private
name|boolean
name|rebalance
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
name|List
argument_list|<
name|RoutingNode
argument_list|>
name|sortedNodesLeastToHigh
init|=
name|routingNodes
operator|.
name|sortedNodesLeastToHigh
argument_list|()
decl_stmt|;
if|if
condition|(
name|sortedNodesLeastToHigh
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|lowIndex
init|=
literal|0
decl_stmt|;
name|int
name|highIndex
init|=
name|sortedNodesLeastToHigh
operator|.
name|size
argument_list|()
operator|-
literal|1
decl_stmt|;
name|boolean
name|relocationPerformed
decl_stmt|;
do|do
block|{
name|relocationPerformed
operator|=
literal|false
expr_stmt|;
while|while
condition|(
name|lowIndex
operator|!=
name|highIndex
condition|)
block|{
name|RoutingNode
name|lowRoutingNode
init|=
name|sortedNodesLeastToHigh
operator|.
name|get
argument_list|(
name|lowIndex
argument_list|)
decl_stmt|;
name|RoutingNode
name|highRoutingNode
init|=
name|sortedNodesLeastToHigh
operator|.
name|get
argument_list|(
name|highIndex
argument_list|)
decl_stmt|;
name|int
name|averageNumOfShards
init|=
name|routingNodes
operator|.
name|requiredAverageNumberOfShardsPerNode
argument_list|()
decl_stmt|;
comment|// only active shards can be removed so must count only active ones.
if|if
condition|(
name|highRoutingNode
operator|.
name|numberOfOwningShards
argument_list|()
operator|<=
name|averageNumOfShards
condition|)
block|{
name|highIndex
operator|--
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|lowRoutingNode
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
operator|>=
name|averageNumOfShards
condition|)
block|{
name|lowIndex
operator|++
expr_stmt|;
continue|continue;
block|}
name|boolean
name|relocated
init|=
literal|false
decl_stmt|;
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|activeShards
init|=
name|highRoutingNode
operator|.
name|shardsWithState
argument_list|(
name|STARTED
argument_list|)
decl_stmt|;
for|for
control|(
name|MutableShardRouting
name|activeShard
range|:
name|activeShards
control|)
block|{
if|if
condition|(
name|lowRoutingNode
operator|.
name|canAllocate
argument_list|(
name|routingNodes
operator|.
name|metaData
argument_list|()
argument_list|,
name|routingNodes
operator|.
name|routingTable
argument_list|()
argument_list|)
operator|&&
name|lowRoutingNode
operator|.
name|canAllocate
argument_list|(
name|activeShard
argument_list|)
condition|)
block|{
name|changed
operator|=
literal|true
expr_stmt|;
name|lowRoutingNode
operator|.
name|add
argument_list|(
operator|new
name|MutableShardRouting
argument_list|(
name|activeShard
operator|.
name|index
argument_list|()
argument_list|,
name|activeShard
operator|.
name|id
argument_list|()
argument_list|,
name|lowRoutingNode
operator|.
name|nodeId
argument_list|()
argument_list|,
name|activeShard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|activeShard
operator|.
name|primary
argument_list|()
argument_list|,
name|INITIALIZING
argument_list|)
argument_list|)
expr_stmt|;
name|activeShard
operator|.
name|relocate
argument_list|(
name|lowRoutingNode
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
name|relocated
operator|=
literal|true
expr_stmt|;
name|relocationPerformed
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|relocated
condition|)
block|{
name|highIndex
operator|--
expr_stmt|;
block|}
block|}
block|}
do|while
condition|(
name|relocationPerformed
condition|)
do|;
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
DECL|method|allocateUnassigned
specifier|private
name|boolean
name|allocateUnassigned
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
name|List
argument_list|<
name|RoutingNode
argument_list|>
name|nodes
init|=
name|routingNodes
operator|.
name|sortedNodesLeastToHigh
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|MutableShardRouting
argument_list|>
name|unassignedIterator
init|=
name|routingNodes
operator|.
name|unassigned
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|int
name|lastNode
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|unassignedIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|MutableShardRouting
name|shard
init|=
name|unassignedIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
comment|// if its a backup, only allocate it if the primary is active
name|MutableShardRouting
name|primary
init|=
name|routingNodes
operator|.
name|findPrimaryForBackup
argument_list|(
name|shard
argument_list|)
decl_stmt|;
if|if
condition|(
name|primary
operator|==
literal|null
operator|||
operator|!
name|primary
operator|.
name|active
argument_list|()
condition|)
block|{
continue|continue;
block|}
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
name|nodes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|RoutingNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|lastNode
argument_list|)
decl_stmt|;
name|lastNode
operator|++
expr_stmt|;
if|if
condition|(
name|lastNode
operator|==
name|nodes
operator|.
name|size
argument_list|()
condition|)
name|lastNode
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|node
operator|.
name|canAllocate
argument_list|(
name|routingNodes
operator|.
name|metaData
argument_list|()
argument_list|,
name|routingNodes
operator|.
name|routingTable
argument_list|()
argument_list|)
operator|&&
name|node
operator|.
name|canAllocate
argument_list|(
name|shard
argument_list|)
condition|)
block|{
name|int
name|numberOfShardsToAllocate
init|=
name|routingNodes
operator|.
name|requiredAverageNumberOfShardsPerNode
argument_list|()
operator|-
name|node
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|numberOfShardsToAllocate
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|changed
operator|=
literal|true
expr_stmt|;
name|node
operator|.
name|add
argument_list|(
name|shard
argument_list|)
expr_stmt|;
name|unassignedIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
block|}
comment|// allocate all the unassigned shards above the average per node.
for|for
control|(
name|Iterator
argument_list|<
name|MutableShardRouting
argument_list|>
name|it
init|=
name|routingNodes
operator|.
name|unassigned
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|MutableShardRouting
name|shard
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
comment|// if its a backup, only allocate it if the primary is active
name|MutableShardRouting
name|primary
init|=
name|routingNodes
operator|.
name|findPrimaryForBackup
argument_list|(
name|shard
argument_list|)
decl_stmt|;
if|if
condition|(
name|primary
operator|==
literal|null
operator|||
operator|!
name|primary
operator|.
name|active
argument_list|()
condition|)
block|{
continue|continue;
block|}
block|}
comment|// go over the nodes and try and allocate the remaining ones
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
if|if
condition|(
name|routingNode
operator|.
name|canAllocate
argument_list|(
name|routingNodes
operator|.
name|metaData
argument_list|()
argument_list|,
name|routingNodes
operator|.
name|routingTable
argument_list|()
argument_list|)
operator|&&
name|routingNode
operator|.
name|canAllocate
argument_list|(
name|shard
argument_list|)
condition|)
block|{
name|changed
operator|=
literal|true
expr_stmt|;
name|routingNode
operator|.
name|add
argument_list|(
name|shard
argument_list|)
expr_stmt|;
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
break|break;
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
operator|.
name|id
argument_list|()
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
DECL|method|applyFailedShards
specifier|private
name|boolean
name|applyFailedShards
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
name|failedShardEntries
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
name|failedShard
range|:
name|failedShardEntries
control|)
block|{
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
name|routingNodes
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
name|routingNodes
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
continue|continue;
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
continue|continue;
block|}
else|else
block|{
name|dirty
operator|=
literal|true
expr_stmt|;
block|}
comment|// if in relocation no need to find a new target, just cancel the relocation.
if|if
condition|(
name|inRelocation
condition|)
block|{
continue|continue;
block|}
comment|// not in relocation so find a new target.
name|boolean
name|allocated
init|=
literal|false
decl_stmt|;
name|List
argument_list|<
name|RoutingNode
argument_list|>
name|sortedNodesLeastToHigh
init|=
name|routingNodes
operator|.
name|sortedNodesLeastToHigh
argument_list|()
decl_stmt|;
for|for
control|(
name|RoutingNode
name|target
range|:
name|sortedNodesLeastToHigh
control|)
block|{
if|if
condition|(
name|target
operator|.
name|canAllocate
argument_list|(
name|failedShard
argument_list|)
operator|&&
name|target
operator|.
name|canAllocate
argument_list|(
name|routingNodes
operator|.
name|metaData
argument_list|()
argument_list|,
name|routingNodes
operator|.
name|routingTable
argument_list|()
argument_list|)
operator|&&
operator|!
name|target
operator|.
name|nodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|failedShard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
condition|)
block|{
name|target
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
name|target
operator|.
name|nodeId
argument_list|()
argument_list|,
name|failedShard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|,
name|failedShard
operator|.
name|primary
argument_list|()
argument_list|,
name|INITIALIZING
argument_list|)
argument_list|)
expr_stmt|;
name|allocated
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|allocated
condition|)
block|{
comment|// we did not manage to allocate it, put it in the unassigned
name|routingNodes
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
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|dirty
return|;
block|}
block|}
end_class

end_unit

