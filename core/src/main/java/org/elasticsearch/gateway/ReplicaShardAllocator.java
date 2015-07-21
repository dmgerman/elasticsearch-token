begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
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
name|cursors
operator|.
name|ObjectCursor
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
name|ClusterChangedEvent
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
name|RoutingAllocation
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
name|Decision
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
name|common
operator|.
name|unit
operator|.
name|ByteSizeValue
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
name|unit
operator|.
name|TimeValue
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
name|store
operator|.
name|StoreFileMetaData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|store
operator|.
name|TransportNodesListShardStoreMetaData
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
name|Map
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ReplicaShardAllocator
specifier|public
specifier|abstract
class|class
name|ReplicaShardAllocator
extends|extends
name|AbstractComponent
block|{
DECL|method|ReplicaShardAllocator
specifier|public
name|ReplicaShardAllocator
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
DECL|method|allocateUnassigned
specifier|public
name|boolean
name|allocateUnassigned
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
specifier|final
name|RoutingNodes
name|routingNodes
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
decl_stmt|;
specifier|final
name|MetaData
name|metaData
init|=
name|routingNodes
operator|.
name|metaData
argument_list|()
decl_stmt|;
specifier|final
name|RoutingNodes
operator|.
name|UnassignedShards
operator|.
name|UnassignedIterator
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
while|while
condition|(
name|unassignedIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|ShardRouting
name|shard
init|=
name|unassignedIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// pre-check if it can be allocated to any node that currently exists, so we won't list the store for it for nothing
name|boolean
name|canBeAllocatedToAtLeastOneNode
init|=
literal|false
decl_stmt|;
for|for
control|(
name|ObjectCursor
argument_list|<
name|DiscoveryNode
argument_list|>
name|cursor
range|:
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
control|)
block|{
name|RoutingNode
name|node
init|=
name|routingNodes
operator|.
name|node
argument_list|(
name|cursor
operator|.
name|value
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// if we can't allocate it on a node, ignore it, for example, this handles
comment|// cases for only allocating a replica after a primary
name|Decision
name|decision
init|=
name|allocation
operator|.
name|deciders
argument_list|()
operator|.
name|canAllocate
argument_list|(
name|shard
argument_list|,
name|node
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
if|if
condition|(
name|decision
operator|.
name|type
argument_list|()
operator|==
name|Decision
operator|.
name|Type
operator|.
name|YES
condition|)
block|{
name|canBeAllocatedToAtLeastOneNode
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|canBeAllocatedToAtLeastOneNode
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: ignoring allocation, can't be allocated on any node"
argument_list|,
name|shard
argument_list|)
expr_stmt|;
name|unassignedIterator
operator|.
name|removeAndIgnore
argument_list|()
expr_stmt|;
continue|continue;
block|}
name|AsyncShardFetch
operator|.
name|FetchResult
argument_list|<
name|TransportNodesListShardStoreMetaData
operator|.
name|NodeStoreFilesMetaData
argument_list|>
name|shardStores
init|=
name|fetchData
argument_list|(
name|shard
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardStores
operator|.
name|hasData
argument_list|()
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: ignoring allocation, still fetching shard stores"
argument_list|,
name|shard
argument_list|)
expr_stmt|;
name|unassignedIterator
operator|.
name|removeAndIgnore
argument_list|()
expr_stmt|;
continue|continue;
comment|// still fetching
block|}
name|long
name|lastSizeMatched
init|=
literal|0
decl_stmt|;
name|DiscoveryNode
name|lastDiscoNodeMatched
init|=
literal|null
decl_stmt|;
name|RoutingNode
name|lastNodeMatched
init|=
literal|null
decl_stmt|;
name|boolean
name|hasReplicaData
init|=
literal|false
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|metaData
operator|.
name|index
argument_list|(
name|shard
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|DiscoveryNode
argument_list|,
name|TransportNodesListShardStoreMetaData
operator|.
name|NodeStoreFilesMetaData
argument_list|>
name|nodeStoreEntry
range|:
name|shardStores
operator|.
name|getData
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|DiscoveryNode
name|discoNode
init|=
name|nodeStoreEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|TransportNodesListShardStoreMetaData
operator|.
name|StoreFilesMetaData
name|storeFilesMetaData
init|=
name|nodeStoreEntry
operator|.
name|getValue
argument_list|()
operator|.
name|storeFilesMetaData
argument_list|()
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: checking node [{}]"
argument_list|,
name|shard
argument_list|,
name|discoNode
argument_list|)
expr_stmt|;
if|if
condition|(
name|storeFilesMetaData
operator|==
literal|null
condition|)
block|{
comment|// already allocated on that node...
continue|continue;
block|}
name|RoutingNode
name|node
init|=
name|routingNodes
operator|.
name|node
argument_list|(
name|discoNode
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// check if we can allocate on that node...
comment|// we only check for NO, since if this node is THROTTLING and it has enough "same data"
comment|// then we will try and assign it next time
name|Decision
name|decision
init|=
name|allocation
operator|.
name|deciders
argument_list|()
operator|.
name|canAllocate
argument_list|(
name|shard
argument_list|,
name|node
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
if|if
condition|(
name|decision
operator|.
name|type
argument_list|()
operator|==
name|Decision
operator|.
name|Type
operator|.
name|NO
condition|)
block|{
continue|continue;
block|}
comment|// if it is already allocated, we can't assign to it...
if|if
condition|(
name|storeFilesMetaData
operator|.
name|allocated
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|hasReplicaData
operator||=
name|storeFilesMetaData
operator|.
name|iterator
argument_list|()
operator|.
name|hasNext
argument_list|()
expr_stmt|;
name|ShardRouting
name|primaryShard
init|=
name|routingNodes
operator|.
name|activePrimary
argument_list|(
name|shard
argument_list|)
decl_stmt|;
if|if
condition|(
name|primaryShard
operator|!=
literal|null
condition|)
block|{
assert|assert
name|primaryShard
operator|.
name|active
argument_list|()
assert|;
name|DiscoveryNode
name|primaryNode
init|=
name|allocation
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|primaryShard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|primaryNode
operator|!=
literal|null
condition|)
block|{
name|TransportNodesListShardStoreMetaData
operator|.
name|NodeStoreFilesMetaData
name|primaryNodeFilesStore
init|=
name|shardStores
operator|.
name|getData
argument_list|()
operator|.
name|get
argument_list|(
name|primaryNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|primaryNodeFilesStore
operator|!=
literal|null
condition|)
block|{
name|TransportNodesListShardStoreMetaData
operator|.
name|StoreFilesMetaData
name|primaryNodeStore
init|=
name|primaryNodeFilesStore
operator|.
name|storeFilesMetaData
argument_list|()
decl_stmt|;
if|if
condition|(
name|primaryNodeStore
operator|!=
literal|null
operator|&&
name|primaryNodeStore
operator|.
name|allocated
argument_list|()
condition|)
block|{
name|long
name|sizeMatched
init|=
literal|0
decl_stmt|;
name|String
name|primarySyncId
init|=
name|primaryNodeStore
operator|.
name|syncId
argument_list|()
decl_stmt|;
name|String
name|replicaSyncId
init|=
name|storeFilesMetaData
operator|.
name|syncId
argument_list|()
decl_stmt|;
comment|// see if we have a sync id we can make use of
if|if
condition|(
name|replicaSyncId
operator|!=
literal|null
operator|&&
name|replicaSyncId
operator|.
name|equals
argument_list|(
name|primarySyncId
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: node [{}] has same sync id {} as primary"
argument_list|,
name|shard
argument_list|,
name|discoNode
operator|.
name|name
argument_list|()
argument_list|,
name|replicaSyncId
argument_list|)
expr_stmt|;
name|lastNodeMatched
operator|=
name|node
expr_stmt|;
name|lastSizeMatched
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
name|lastDiscoNodeMatched
operator|=
name|discoNode
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|StoreFileMetaData
name|storeFileMetaData
range|:
name|storeFilesMetaData
control|)
block|{
name|String
name|metaDataFileName
init|=
name|storeFileMetaData
operator|.
name|name
argument_list|()
decl_stmt|;
if|if
condition|(
name|primaryNodeStore
operator|.
name|fileExists
argument_list|(
name|metaDataFileName
argument_list|)
operator|&&
name|primaryNodeStore
operator|.
name|file
argument_list|(
name|metaDataFileName
argument_list|)
operator|.
name|isSame
argument_list|(
name|storeFileMetaData
argument_list|)
condition|)
block|{
name|sizeMatched
operator|+=
name|storeFileMetaData
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: node [{}] has [{}/{}] bytes of re-usable data"
argument_list|,
name|shard
argument_list|,
name|discoNode
operator|.
name|name
argument_list|()
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|sizeMatched
argument_list|)
argument_list|,
name|sizeMatched
argument_list|)
expr_stmt|;
if|if
condition|(
name|sizeMatched
operator|>
name|lastSizeMatched
condition|)
block|{
name|lastSizeMatched
operator|=
name|sizeMatched
expr_stmt|;
name|lastDiscoNodeMatched
operator|=
name|discoNode
expr_stmt|;
name|lastNodeMatched
operator|=
name|node
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|lastNodeMatched
operator|!=
literal|null
condition|)
block|{
comment|// we only check on THROTTLE since we checked before before on NO
name|Decision
name|decision
init|=
name|allocation
operator|.
name|deciders
argument_list|()
operator|.
name|canAllocate
argument_list|(
name|shard
argument_list|,
name|lastNodeMatched
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
if|if
condition|(
name|decision
operator|.
name|type
argument_list|()
operator|==
name|Decision
operator|.
name|Type
operator|.
name|THROTTLE
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}]: throttling allocation [{}] to [{}] in order to reuse its unallocated persistent store with total_size [{}]"
argument_list|,
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
argument_list|,
name|lastDiscoNodeMatched
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|lastSizeMatched
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// we are throttling this, but we have enough to allocate to this node, ignore it for now
name|unassignedIterator
operator|.
name|removeAndIgnore
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}]: allocating [{}] to [{}] in order to reuse its unallocated persistent store with total_size [{}]"
argument_list|,
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
argument_list|,
name|lastDiscoNodeMatched
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|lastSizeMatched
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// we found a match
name|changed
operator|=
literal|true
expr_stmt|;
name|unassignedIterator
operator|.
name|initialize
argument_list|(
name|lastNodeMatched
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|hasReplicaData
operator|==
literal|false
condition|)
block|{
comment|// if we didn't manage to find *any* data (regardless of matching sizes), check if the allocation
comment|// of the replica shard needs to be delayed, and if so, add it to the ignore unassigned list
comment|// note: we only care about replica in delayed allocation, since if we have an unassigned primary it
comment|//       will anyhow wait to find an existing copy of the shard to be allocated
comment|// note: the other side of the equation is scheduling a reroute in a timely manner, which happens in the RoutingService
name|long
name|delay
init|=
name|shard
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getDelayAllocationExpirationIn
argument_list|(
name|settings
argument_list|,
name|indexMetaData
operator|.
name|getSettings
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|delay
operator|>
literal|0
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}]: delaying allocation of [{}] for [{}]"
argument_list|,
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
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|delay
argument_list|)
argument_list|)
expr_stmt|;
comment|/**                      * mark it as changed, since we want to kick a publishing to schedule future allocation,                      * see {@link org.elasticsearch.cluster.routing.RoutingService#clusterChanged(ClusterChangedEvent)}).                      */
name|changed
operator|=
literal|true
expr_stmt|;
name|unassignedIterator
operator|.
name|removeAndIgnore
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
name|changed
return|;
block|}
DECL|method|fetchData
specifier|protected
specifier|abstract
name|AsyncShardFetch
operator|.
name|FetchResult
argument_list|<
name|TransportNodesListShardStoreMetaData
operator|.
name|NodeStoreFilesMetaData
argument_list|>
name|fetchData
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
function_decl|;
block|}
end_class

end_unit

