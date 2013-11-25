begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
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
name|ObjectIntOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectCursor
import|;
end_import

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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|block
operator|.
name|ClusterBlocks
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
name|DiscoveryNode
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
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|newArrayList
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|newHashMap
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
operator|.
name|newHashSet
import|;
end_import

begin_comment
comment|/**  * {@link RoutingNodes} represents a copy the routing information contained in  * the {@link ClusterState cluster state}.  */
end_comment

begin_class
DECL|class|RoutingNodes
specifier|public
class|class
name|RoutingNodes
implements|implements
name|Iterable
argument_list|<
name|RoutingNode
argument_list|>
block|{
DECL|field|metaData
specifier|private
specifier|final
name|MetaData
name|metaData
decl_stmt|;
DECL|field|blocks
specifier|private
specifier|final
name|ClusterBlocks
name|blocks
decl_stmt|;
DECL|field|routingTable
specifier|private
specifier|final
name|RoutingTable
name|routingTable
decl_stmt|;
DECL|field|nodesToShards
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|RoutingNode
argument_list|>
name|nodesToShards
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|unassigned
specifier|private
specifier|final
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|unassigned
init|=
name|newArrayList
argument_list|()
decl_stmt|;
DECL|field|ignoredUnassigned
specifier|private
specifier|final
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|ignoredUnassigned
init|=
name|newArrayList
argument_list|()
decl_stmt|;
DECL|field|replicaSets
specifier|private
specifier|final
name|Map
argument_list|<
name|ShardId
argument_list|,
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
argument_list|>
name|replicaSets
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|unassignedPrimaryCount
specifier|private
name|int
name|unassignedPrimaryCount
init|=
literal|0
decl_stmt|;
DECL|field|inactivePrimaryCount
specifier|private
name|int
name|inactivePrimaryCount
init|=
literal|0
decl_stmt|;
DECL|field|inactiveShardCount
specifier|private
name|int
name|inactiveShardCount
init|=
literal|0
decl_stmt|;
DECL|field|relocatingReplicaSets
name|Set
argument_list|<
name|ShardId
argument_list|>
name|relocatingReplicaSets
init|=
operator|new
name|HashSet
argument_list|<
name|ShardId
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|clearPostAllocationFlag
specifier|private
name|Set
argument_list|<
name|ShardId
argument_list|>
name|clearPostAllocationFlag
decl_stmt|;
DECL|field|nodesPerAttributeNames
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ObjectIntOpenHashMap
argument_list|<
name|String
argument_list|>
argument_list|>
name|nodesPerAttributeNames
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ObjectIntOpenHashMap
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
DECL|method|RoutingNodes
specifier|public
name|RoutingNodes
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|this
operator|.
name|metaData
operator|=
name|clusterState
operator|.
name|metaData
argument_list|()
expr_stmt|;
name|this
operator|.
name|blocks
operator|=
name|clusterState
operator|.
name|blocks
argument_list|()
expr_stmt|;
name|this
operator|.
name|routingTable
operator|=
name|clusterState
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
argument_list|>
name|nodesToShards
init|=
name|newHashMap
argument_list|()
decl_stmt|;
comment|// fill in the nodeToShards with the "live" nodes
for|for
control|(
name|ObjectCursor
argument_list|<
name|DiscoveryNode
argument_list|>
name|cursor
range|:
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|dataNodes
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|nodesToShards
operator|.
name|put
argument_list|(
name|cursor
operator|.
name|value
operator|.
name|id
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|MutableShardRouting
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// fill in the inverse of node -> shards allocated
comment|// also fill replicaSet information
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|routingTable
operator|.
name|indicesRouting
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardRoutingTable
name|indexShard
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shard
range|:
name|indexShard
control|)
block|{
comment|// to get all the shards belonging to an index, including the replicas,
comment|// we define a replica set and keep track of it. A replica set is identified
comment|// by the ShardId, as this is common for primary and replicas.
comment|// A replica Set might have one (and not more) replicas with the state of RELOCATING.
if|if
condition|(
name|shard
operator|.
name|assignedToNode
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|entries
init|=
name|nodesToShards
operator|.
name|get
argument_list|(
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|entries
operator|==
literal|null
condition|)
block|{
name|entries
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
name|nodesToShards
operator|.
name|put
argument_list|(
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|entries
argument_list|)
expr_stmt|;
block|}
name|MutableShardRouting
name|sr
init|=
operator|new
name|MutableShardRouting
argument_list|(
name|shard
argument_list|)
decl_stmt|;
name|entries
operator|.
name|add
argument_list|(
name|sr
argument_list|)
expr_stmt|;
name|addToReplicaSet
argument_list|(
name|sr
argument_list|)
expr_stmt|;
if|if
condition|(
name|shard
operator|.
name|relocating
argument_list|()
condition|)
block|{
name|entries
operator|=
name|nodesToShards
operator|.
name|get
argument_list|(
name|shard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
expr_stmt|;
name|relocatingReplicaSets
operator|.
name|add
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|entries
operator|==
literal|null
condition|)
block|{
name|entries
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
name|nodesToShards
operator|.
name|put
argument_list|(
name|shard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|,
name|entries
argument_list|)
expr_stmt|;
block|}
comment|// add the counterpart shard with relocatingNodeId reflecting the source from which
comment|// it's relocating from.
name|sr
operator|=
operator|new
name|MutableShardRouting
argument_list|(
name|shard
operator|.
name|index
argument_list|()
argument_list|,
name|shard
operator|.
name|id
argument_list|()
argument_list|,
name|shard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|,
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|shard
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|,
name|shard
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
name|entries
operator|.
name|add
argument_list|(
name|sr
argument_list|)
expr_stmt|;
name|addToReplicaSet
argument_list|(
name|sr
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|shard
operator|.
name|active
argument_list|()
condition|)
block|{
comment|// shards that are initializing without being relocated
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|inactivePrimaryCount
operator|++
expr_stmt|;
block|}
name|inactiveShardCount
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|MutableShardRouting
name|sr
init|=
operator|new
name|MutableShardRouting
argument_list|(
name|shard
argument_list|)
decl_stmt|;
name|addToReplicaSet
argument_list|(
name|sr
argument_list|)
expr_stmt|;
name|unassigned
operator|.
name|add
argument_list|(
name|sr
argument_list|)
expr_stmt|;
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|unassignedPrimaryCount
operator|++
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
argument_list|>
name|entry
range|:
name|nodesToShards
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|nodeId
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|this
operator|.
name|nodesToShards
operator|.
name|put
argument_list|(
name|nodeId
argument_list|,
operator|new
name|RoutingNode
argument_list|(
name|nodeId
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|nodeId
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|RoutingNode
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|nodesToShards
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|routingTable
specifier|public
name|RoutingTable
name|routingTable
parameter_list|()
block|{
return|return
name|routingTable
return|;
block|}
DECL|method|getRoutingTable
specifier|public
name|RoutingTable
name|getRoutingTable
parameter_list|()
block|{
return|return
name|routingTable
argument_list|()
return|;
block|}
DECL|method|metaData
specifier|public
name|MetaData
name|metaData
parameter_list|()
block|{
return|return
name|this
operator|.
name|metaData
return|;
block|}
DECL|method|getMetaData
specifier|public
name|MetaData
name|getMetaData
parameter_list|()
block|{
return|return
name|metaData
argument_list|()
return|;
block|}
DECL|method|blocks
specifier|public
name|ClusterBlocks
name|blocks
parameter_list|()
block|{
return|return
name|this
operator|.
name|blocks
return|;
block|}
DECL|method|getBlocks
specifier|public
name|ClusterBlocks
name|getBlocks
parameter_list|()
block|{
return|return
name|this
operator|.
name|blocks
return|;
block|}
DECL|method|requiredAverageNumberOfShardsPerNode
specifier|public
name|int
name|requiredAverageNumberOfShardsPerNode
parameter_list|()
block|{
name|int
name|totalNumberOfShards
init|=
literal|0
decl_stmt|;
comment|// we need to recompute to take closed shards into account
for|for
control|(
name|ObjectCursor
argument_list|<
name|IndexMetaData
argument_list|>
name|cursor
range|:
name|metaData
operator|.
name|indices
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|cursor
operator|.
name|value
decl_stmt|;
if|if
condition|(
name|indexMetaData
operator|.
name|state
argument_list|()
operator|==
name|IndexMetaData
operator|.
name|State
operator|.
name|OPEN
condition|)
block|{
name|totalNumberOfShards
operator|+=
name|indexMetaData
operator|.
name|totalNumberOfShards
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|totalNumberOfShards
operator|/
name|nodesToShards
operator|.
name|size
argument_list|()
return|;
block|}
DECL|method|hasUnassigned
specifier|public
name|boolean
name|hasUnassigned
parameter_list|()
block|{
return|return
operator|!
name|unassigned
operator|.
name|isEmpty
argument_list|()
return|;
block|}
DECL|method|ignoredUnassigned
specifier|public
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|ignoredUnassigned
parameter_list|()
block|{
return|return
name|this
operator|.
name|ignoredUnassigned
return|;
block|}
DECL|method|unassigned
specifier|public
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|unassigned
parameter_list|()
block|{
return|return
name|this
operator|.
name|unassigned
return|;
block|}
DECL|method|getUnassigned
specifier|public
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|getUnassigned
parameter_list|()
block|{
return|return
name|unassigned
argument_list|()
return|;
block|}
DECL|method|nodesToShards
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|RoutingNode
argument_list|>
name|nodesToShards
parameter_list|()
block|{
return|return
name|nodesToShards
return|;
block|}
DECL|method|getNodesToShards
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|RoutingNode
argument_list|>
name|getNodesToShards
parameter_list|()
block|{
return|return
name|nodesToShards
argument_list|()
return|;
block|}
comment|/**      * Clears the post allocation flag for the provided shard id. NOTE: this should be used cautiously      * since it will lead to data loss of the primary shard is not allocated, as it will allocate      * the primary shard on a node and *not* expect it to have an existing valid index there.      */
DECL|method|addClearPostAllocationFlag
specifier|public
name|void
name|addClearPostAllocationFlag
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
if|if
condition|(
name|clearPostAllocationFlag
operator|==
literal|null
condition|)
block|{
name|clearPostAllocationFlag
operator|=
name|Sets
operator|.
name|newHashSet
argument_list|()
expr_stmt|;
block|}
name|clearPostAllocationFlag
operator|.
name|add
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
block|}
DECL|method|getShardsToClearPostAllocationFlag
specifier|public
name|Iterable
argument_list|<
name|ShardId
argument_list|>
name|getShardsToClearPostAllocationFlag
parameter_list|()
block|{
if|if
condition|(
name|clearPostAllocationFlag
operator|==
literal|null
condition|)
block|{
return|return
name|ImmutableSet
operator|.
name|of
argument_list|()
return|;
block|}
return|return
name|clearPostAllocationFlag
return|;
block|}
DECL|method|node
specifier|public
name|RoutingNode
name|node
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
return|return
name|nodesToShards
operator|.
name|get
argument_list|(
name|nodeId
argument_list|)
return|;
block|}
DECL|method|nodesPerAttributesCounts
specifier|public
name|ObjectIntOpenHashMap
argument_list|<
name|String
argument_list|>
name|nodesPerAttributesCounts
parameter_list|(
name|String
name|attributeName
parameter_list|)
block|{
name|ObjectIntOpenHashMap
argument_list|<
name|String
argument_list|>
name|nodesPerAttributesCounts
init|=
name|nodesPerAttributeNames
operator|.
name|get
argument_list|(
name|attributeName
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodesPerAttributesCounts
operator|!=
literal|null
condition|)
block|{
return|return
name|nodesPerAttributesCounts
return|;
block|}
name|nodesPerAttributesCounts
operator|=
operator|new
name|ObjectIntOpenHashMap
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|this
control|)
block|{
name|String
name|attrValue
init|=
name|routingNode
operator|.
name|node
argument_list|()
operator|.
name|attributes
argument_list|()
operator|.
name|get
argument_list|(
name|attributeName
argument_list|)
decl_stmt|;
name|nodesPerAttributesCounts
operator|.
name|addTo
argument_list|(
name|attrValue
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|nodesPerAttributeNames
operator|.
name|put
argument_list|(
name|attributeName
argument_list|,
name|nodesPerAttributesCounts
argument_list|)
expr_stmt|;
return|return
name|nodesPerAttributesCounts
return|;
block|}
DECL|method|hasUnassignedPrimaries
specifier|public
name|boolean
name|hasUnassignedPrimaries
parameter_list|()
block|{
return|return
name|unassignedPrimaryCount
operator|>
literal|0
return|;
block|}
DECL|method|hasUnassignedShards
specifier|public
name|boolean
name|hasUnassignedShards
parameter_list|()
block|{
return|return
operator|!
name|unassigned
operator|.
name|isEmpty
argument_list|()
return|;
block|}
DECL|method|hasInactivePrimaries
specifier|public
name|boolean
name|hasInactivePrimaries
parameter_list|()
block|{
return|return
name|inactivePrimaryCount
operator|>
literal|0
return|;
block|}
DECL|method|hasInactiveShards
specifier|public
name|boolean
name|hasInactiveShards
parameter_list|()
block|{
return|return
name|inactiveShardCount
operator|>
literal|0
return|;
block|}
DECL|method|getRelocatingShardCount
specifier|public
name|int
name|getRelocatingShardCount
parameter_list|()
block|{
return|return
name|relocatingReplicaSets
operator|.
name|size
argument_list|()
return|;
block|}
DECL|method|findPrimaryForReplica
specifier|public
name|MutableShardRouting
name|findPrimaryForReplica
parameter_list|(
name|ShardRouting
name|shard
parameter_list|)
block|{
assert|assert
operator|!
name|shard
operator|.
name|primary
argument_list|()
assert|;
name|MutableShardRouting
name|primary
init|=
literal|null
decl_stmt|;
for|for
control|(
name|MutableShardRouting
name|shardRouting
range|:
name|shardsRoutingFor
argument_list|(
name|shard
argument_list|)
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
name|primary
operator|=
name|shardRouting
expr_stmt|;
break|break;
block|}
block|}
assert|assert
name|primary
operator|!=
literal|null
assert|;
return|return
name|primary
return|;
block|}
DECL|method|shardsRoutingFor
specifier|public
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shardsRoutingFor
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|)
block|{
return|return
name|shardsRoutingFor
argument_list|(
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
return|;
block|}
DECL|method|shardsRoutingFor
specifier|public
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shardsRoutingFor
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
name|ShardId
name|sid
init|=
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shards
init|=
name|replicaSetFor
argument_list|(
name|sid
argument_list|)
decl_stmt|;
assert|assert
name|shards
operator|!=
literal|null
assert|;
comment|// no need to check unassigned array, since the ShardRoutings are in the replica set.
return|return
name|shards
return|;
block|}
DECL|method|numberOfShardsOfType
specifier|public
name|int
name|numberOfShardsOfType
parameter_list|(
name|ShardRoutingState
name|state
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|this
control|)
block|{
name|count
operator|+=
name|routingNode
operator|.
name|numberOfShardsWithState
argument_list|(
name|state
argument_list|)
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
DECL|method|shards
specifier|public
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shards
parameter_list|(
name|Predicate
argument_list|<
name|MutableShardRouting
argument_list|>
name|predicate
parameter_list|)
block|{
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shards
init|=
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|this
control|)
block|{
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|nodeShards
init|=
name|routingNode
operator|.
name|shards
argument_list|()
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
name|nodeShards
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
name|nodeShards
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|predicate
operator|.
name|apply
argument_list|(
name|shardRouting
argument_list|)
condition|)
block|{
name|shards
operator|.
name|add
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|shards
return|;
block|}
DECL|method|shardsWithState
specifier|public
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shardsWithState
parameter_list|(
name|ShardRoutingState
modifier|...
name|state
parameter_list|)
block|{
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shards
init|=
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|this
control|)
block|{
name|shards
operator|.
name|addAll
argument_list|(
name|routingNode
operator|.
name|shardsWithState
argument_list|(
name|state
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|shards
return|;
block|}
DECL|method|shardsWithState
specifier|public
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shardsWithState
parameter_list|(
name|String
name|index
parameter_list|,
name|ShardRoutingState
modifier|...
name|state
parameter_list|)
block|{
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shards
init|=
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|this
control|)
block|{
name|shards
operator|.
name|addAll
argument_list|(
name|routingNode
operator|.
name|shardsWithState
argument_list|(
name|index
argument_list|,
name|state
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|shards
return|;
block|}
DECL|method|prettyPrint
specifier|public
name|String
name|prettyPrint
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"routing_nodes:\n"
argument_list|)
decl_stmt|;
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|this
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|routingNode
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"---- unassigned\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|MutableShardRouting
name|shardEntry
range|:
name|unassigned
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"--------"
argument_list|)
operator|.
name|append
argument_list|(
name|shardEntry
operator|.
name|shortSummary
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**      * calculates RoutingNodes statistics by iterating over all {@link MutableShardRouting}s      * in the cluster to ensure the {@link RoutingManager} book-keeping is correct.      * For performance reasons, this should only be called from test cases.      *      * @return true if all counts are the same, false if either of the book-keeping numbers is off.      */
DECL|method|assertShardStats
specifier|public
name|boolean
name|assertShardStats
parameter_list|()
block|{
name|int
name|unassignedPrimaryCount
init|=
literal|0
decl_stmt|;
name|int
name|inactivePrimaryCount
init|=
literal|0
decl_stmt|;
name|int
name|inactiveShardCount
init|=
literal|0
decl_stmt|;
name|int
name|totalShards
init|=
literal|0
decl_stmt|;
name|Set
argument_list|<
name|ShardId
argument_list|>
name|seenShards
init|=
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|RoutingNode
name|node
range|:
name|this
control|)
block|{
for|for
control|(
name|MutableShardRouting
name|shard
range|:
name|node
control|)
block|{
if|if
condition|(
operator|!
name|shard
operator|.
name|active
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|shard
operator|.
name|relocating
argument_list|()
condition|)
block|{
name|inactiveShardCount
operator|++
expr_stmt|;
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|inactivePrimaryCount
operator|++
expr_stmt|;
block|}
block|}
block|}
name|totalShards
operator|++
expr_stmt|;
name|seenShards
operator|.
name|add
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|MutableShardRouting
name|shard
range|:
name|unassigned
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|unassignedPrimaryCount
operator|++
expr_stmt|;
block|}
name|totalShards
operator|++
expr_stmt|;
name|seenShards
operator|.
name|add
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ShardId
name|shardId
range|:
name|seenShards
control|)
block|{
assert|assert
name|replicaSetFor
argument_list|(
name|shardId
argument_list|)
operator|!=
literal|null
assert|;
block|}
assert|assert
name|unassignedPrimaryCount
operator|==
literal|0
operator|||
name|hasUnassignedPrimaries
argument_list|()
assert|;
assert|assert
name|inactivePrimaryCount
operator|==
literal|0
operator|||
name|hasInactivePrimaries
argument_list|()
assert|;
assert|assert
name|inactiveShardCount
operator|==
literal|0
operator|||
name|hasInactiveShards
argument_list|()
assert|;
assert|assert
name|hasUnassignedPrimaries
argument_list|()
operator|||
name|unassignedPrimaryCount
operator|==
literal|0
assert|;
assert|assert
name|hasInactivePrimaries
argument_list|()
operator|||
name|inactivePrimaryCount
operator|==
literal|0
assert|;
assert|assert
name|hasInactiveShards
argument_list|()
operator|||
name|inactiveShardCount
operator|==
literal|0
assert|;
return|return
literal|true
return|;
block|}
comment|/**      * Assign a shard to a node. This will increment the inactiveShardCount counter      * and the inactivePrimaryCount counter if the shard is the primary.      * In case the shard is already assigned and started, it will be marked as       * relocating, which is accounted for, too, so the number of concurrent relocations      * can be retrieved easily.      * This method can be called several times for the same shard, only the first time      * will change the state.      *      * INITIALIZING => INITIALIZING      * UNASSIGNED   => INITIALIZING      * STARTED      => RELOCATING      * RELOCATING   => RELOCATING      *      * @param shard the shard to be assigned      * @param nodeId the nodeId this shard should initialize on or relocate from      */
DECL|method|assignShardToNode
specifier|public
name|void
name|assignShardToNode
parameter_list|(
name|MutableShardRouting
name|shard
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
comment|// state will not change if the shard is already initializing.
name|ShardRoutingState
name|oldState
init|=
name|shard
operator|.
name|state
argument_list|()
decl_stmt|;
name|shard
operator|.
name|assignToNode
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
name|node
argument_list|(
name|nodeId
argument_list|)
operator|.
name|add
argument_list|(
name|shard
argument_list|)
expr_stmt|;
if|if
condition|(
name|oldState
operator|==
name|ShardRoutingState
operator|.
name|UNASSIGNED
condition|)
block|{
name|inactiveShardCount
operator|++
expr_stmt|;
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|unassignedPrimaryCount
operator|--
expr_stmt|;
name|inactivePrimaryCount
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|ShardRoutingState
operator|.
name|RELOCATING
condition|)
block|{
comment|// this a HashSet. double add no worry.
name|relocatingReplicaSets
operator|.
name|add
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// possibly double/triple adding it to a replica set doesn't matter
comment|// but make sure we know about the shard.
name|addToReplicaSet
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
comment|/**      * Relocate a shard to another node.      *      * STARTED => RELOCATING      *      * @param shard the shard to relocate      * @param nodeId the node to relocate to      */
DECL|method|relocateShard
specifier|public
name|void
name|relocateShard
parameter_list|(
name|MutableShardRouting
name|shard
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
name|relocatingReplicaSets
operator|.
name|add
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|shard
operator|.
name|relocate
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
block|}
comment|/**      * Cancels the relocation of a shard.      *      * RELOCATING => STARTED      *      * @param shard the shard that was relocating previously and now should be started again.      */
DECL|method|cancelRelocationForShard
specifier|public
name|void
name|cancelRelocationForShard
parameter_list|(
name|MutableShardRouting
name|shard
parameter_list|)
block|{
name|relocatingReplicaSets
operator|.
name|remove
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|shard
operator|.
name|cancelRelocation
argument_list|()
expr_stmt|;
block|}
comment|/**      * Unassigns shard from a node.      * Both relocating and started shards that are deallocated need a new       * primary elected.      *      * RELOCATING   => null      * STARTED      => null      * INITIALIZING => null      *      * @param shard the shard to be unassigned.      */
DECL|method|deassignShard
specifier|public
name|void
name|deassignShard
parameter_list|(
name|MutableShardRouting
name|shard
parameter_list|)
block|{
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|ShardRoutingState
operator|.
name|RELOCATING
condition|)
block|{
name|cancelRelocationForShard
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
condition|)
name|unassignedPrimaryCount
operator|++
expr_stmt|;
name|shard
operator|.
name|deassignNode
argument_list|()
expr_stmt|;
block|}
comment|/**      * Mark a shard as started.      * Decreases the counters and marks a replication complete or failed,      * which is the same for accounting in this class.      *      * INITIALIZING => STARTED      * RELOCATIng   => STARTED      *      * @param shard the shard to be marked as started      */
DECL|method|markShardStarted
specifier|public
name|void
name|markShardStarted
parameter_list|(
name|MutableShardRouting
name|shard
parameter_list|)
block|{
if|if
condition|(
operator|!
name|relocatingReplicaSets
operator|.
name|contains
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
operator|&&
name|shard
operator|.
name|state
argument_list|()
operator|==
name|ShardRoutingState
operator|.
name|INITIALIZING
condition|)
block|{
name|inactiveShardCount
operator|--
expr_stmt|;
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|inactivePrimaryCount
operator|--
expr_stmt|;
block|}
block|}
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|ShardRoutingState
operator|.
name|INITIALIZING
operator|&&
name|shard
operator|.
name|relocatingNodeId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|relocatingReplicaSets
operator|.
name|remove
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|shard
operator|.
name|moveToStarted
argument_list|()
expr_stmt|;
block|}
comment|/**      * Return a list of shards belonging to a replica set      *       * @param shard the shard for which to retrieve the replica set      * @return an unmodifiable List of the replica set      */
DECL|method|replicaSetFor
specifier|public
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|replicaSetFor
parameter_list|(
name|MutableShardRouting
name|shard
parameter_list|)
block|{
return|return
name|replicaSetFor
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Return a list of shards belonging to a replica set      *       * @param shardId the {@link ShardId} for which to retrieve the replica set      * @return an unmodifiable List of the replica set      */
DECL|method|replicaSetFor
specifier|public
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|replicaSetFor
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|replicaSet
init|=
name|replicaSets
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
assert|assert
name|replicaSet
operator|!=
literal|null
assert|;
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|replicaSet
argument_list|)
return|;
block|}
comment|/**      * Let this class know about a shard, which it then sorts into       * its replica set. Package private as only {@link RoutingNodes}       * should notify this class of shards during initialization.      *      * @param shard the shard to be sorted into its replica set      */
DECL|method|addToReplicaSet
specifier|private
name|void
name|addToReplicaSet
parameter_list|(
name|MutableShardRouting
name|shard
parameter_list|)
block|{
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|replicaSet
init|=
name|replicaSets
operator|.
name|get
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|replicaSet
operator|==
literal|null
condition|)
block|{
name|replicaSet
operator|=
operator|new
name|ArrayList
argument_list|<
name|MutableShardRouting
argument_list|>
argument_list|()
expr_stmt|;
name|replicaSets
operator|.
name|put
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|replicaSet
argument_list|)
expr_stmt|;
block|}
name|replicaSet
operator|.
name|add
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
comment|/**      * marks a replica set as relocating.       *      * @param shard a member of the relocating replica set      */
DECL|method|markRelocating
specifier|private
name|void
name|markRelocating
parameter_list|(
name|MutableShardRouting
name|shard
parameter_list|)
block|{
name|relocatingReplicaSets
operator|.
name|add
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * swaps the status of a shard, making replicas primary and vice versa.      *       * @param shard the shard to have its primary status swapped.      */
DECL|method|changePrimaryStatusForShard
specifier|public
name|void
name|changePrimaryStatusForShard
parameter_list|(
name|MutableShardRouting
modifier|...
name|shards
parameter_list|)
block|{
for|for
control|(
name|MutableShardRouting
name|shard
range|:
name|shards
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|shard
operator|.
name|moveFromPrimary
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|shard
operator|.
name|moveToPrimary
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

