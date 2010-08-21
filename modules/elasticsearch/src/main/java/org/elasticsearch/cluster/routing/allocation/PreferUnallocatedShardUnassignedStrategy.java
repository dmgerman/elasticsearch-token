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
name|ExceptionsHelper
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
name|ActionListener
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
name|common
operator|.
name|blobstore
operator|.
name|BlobMetaData
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
name|collect
operator|.
name|ImmutableMap
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
name|index
operator|.
name|gateway
operator|.
name|blobstore
operator|.
name|BlobStoreIndexGateway
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
name|service
operator|.
name|InternalIndexService
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
name|index
operator|.
name|store
operator|.
name|IndexStore
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
name|IndicesService
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
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|ConnectTransportException
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
name|concurrent
operator|.
name|CountDownLatch
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|PreferUnallocatedShardUnassignedStrategy
specifier|public
class|class
name|PreferUnallocatedShardUnassignedStrategy
extends|extends
name|AbstractComponent
implements|implements
name|PreferUnallocatedStrategy
block|{
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|transportNodesListShardStoreMetaData
specifier|private
specifier|final
name|TransportNodesListShardStoreMetaData
name|transportNodesListShardStoreMetaData
decl_stmt|;
DECL|field|nodeAllocations
specifier|private
specifier|final
name|NodeAllocations
name|nodeAllocations
decl_stmt|;
DECL|method|PreferUnallocatedShardUnassignedStrategy
annotation|@
name|Inject
specifier|public
name|PreferUnallocatedShardUnassignedStrategy
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|TransportNodesListShardStoreMetaData
name|transportNodesListShardStoreMetaData
parameter_list|,
name|NodeAllocations
name|nodeAllocations
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|transportNodesListShardStoreMetaData
operator|=
name|transportNodesListShardStoreMetaData
expr_stmt|;
name|this
operator|.
name|nodeAllocations
operator|=
name|nodeAllocations
expr_stmt|;
block|}
DECL|method|prefetch
annotation|@
name|Override
specifier|public
name|void
name|prefetch
parameter_list|(
name|IndexMetaData
name|index
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|)
block|{
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|index
operator|.
name|numberOfShards
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|shardId
init|=
literal|0
init|;
name|shardId
operator|<
name|index
operator|.
name|numberOfShards
argument_list|()
condition|;
name|shardId
operator|++
control|)
block|{
name|transportNodesListShardStoreMetaData
operator|.
name|list
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
operator|.
name|index
argument_list|()
argument_list|,
name|shardId
argument_list|)
argument_list|,
literal|false
argument_list|,
name|nodes
operator|.
name|dataNodes
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|TransportNodesListShardStoreMetaData
operator|.
name|NodesStoreFilesMetaData
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|TransportNodesListShardStoreMetaData
operator|.
name|NodesStoreFilesMetaData
name|nodesStoreFilesMetaData
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
DECL|method|allocateUnassigned
specifier|public
name|boolean
name|allocateUnassigned
parameter_list|(
name|RoutingNodes
name|routingNodes
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|)
block|{
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|nodes
operator|.
name|dataNodes
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|changed
return|;
block|}
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
name|InternalIndexService
name|indexService
init|=
operator|(
name|InternalIndexService
operator|)
name|indicesService
operator|.
name|indexService
argument_list|(
name|shard
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// if the store is not persistent, it makes no sense to test for special allocation
if|if
condition|(
operator|!
name|indexService
operator|.
name|store
argument_list|()
operator|.
name|persistent
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|TransportNodesListShardStoreMetaData
operator|.
name|NodesStoreFilesMetaData
name|nodesStoreFilesMetaData
init|=
name|transportNodesListShardStoreMetaData
operator|.
name|list
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
literal|false
argument_list|,
name|nodes
operator|.
name|dataNodes
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|nodesStoreFilesMetaData
operator|.
name|failures
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|shard
operator|+
literal|": failures when trying to list stores on nodes:"
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
name|nodesStoreFilesMetaData
operator|.
name|failures
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Throwable
name|cause
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|nodesStoreFilesMetaData
operator|.
name|failures
argument_list|()
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|ConnectTransportException
condition|)
block|{
continue|continue;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"\n    -> "
argument_list|)
operator|.
name|append
argument_list|(
name|nodesStoreFilesMetaData
operator|.
name|failures
argument_list|()
index|[
name|i
index|]
operator|.
name|getDetailedMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
for|for
control|(
name|TransportNodesListShardStoreMetaData
operator|.
name|NodeStoreFilesMetaData
name|nodeStoreFilesMetaData
range|:
name|nodesStoreFilesMetaData
control|)
block|{
name|DiscoveryNode
name|discoNode
init|=
name|nodeStoreFilesMetaData
operator|.
name|node
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
name|IndexStore
operator|.
name|StoreFilesMetaData
name|storeFilesMetaData
init|=
name|nodeStoreFilesMetaData
operator|.
name|storeFilesMetaData
argument_list|()
decl_stmt|;
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
if|if
condition|(
operator|!
name|nodeAllocations
operator|.
name|canAllocate
argument_list|(
name|shard
argument_list|,
name|node
argument_list|,
name|routingNodes
argument_list|)
operator|.
name|allocate
argument_list|()
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
comment|// if its a primary, it will be recovered from the gateway, find one that is closet to it
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
operator|&&
name|indexService
operator|.
name|gateway
argument_list|()
operator|instanceof
name|BlobStoreIndexGateway
condition|)
block|{
name|BlobStoreIndexGateway
name|indexGateway
init|=
operator|(
name|BlobStoreIndexGateway
operator|)
name|indexService
operator|.
name|gateway
argument_list|()
decl_stmt|;
try|try
block|{
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|indexBlobsMetaData
init|=
name|indexGateway
operator|.
name|listIndexBlobs
argument_list|(
name|shard
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|shard
operator|+
literal|": checking for pre_allocation (gateway) on node "
operator|+
name|discoNode
operator|+
literal|"\n"
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    gateway_files:\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|BlobMetaData
name|md
range|:
name|indexBlobsMetaData
operator|.
name|values
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"        ["
argument_list|)
operator|.
name|append
argument_list|(
name|md
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"], size ["
argument_list|)
operator|.
name|append
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|md
operator|.
name|sizeInBytes
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"], md5 ["
argument_list|)
operator|.
name|append
argument_list|(
name|md
operator|.
name|md5
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"    node_files:\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|StoreFileMetaData
name|md
range|:
name|storeFilesMetaData
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"        ["
argument_list|)
operator|.
name|append
argument_list|(
name|md
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"], size ["
argument_list|)
operator|.
name|append
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|md
operator|.
name|sizeInBytes
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"], md5 ["
argument_list|)
operator|.
name|append
argument_list|(
name|md
operator|.
name|md5
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: checking for pre_allocation (gateway) on node [{}]\n   gateway files"
argument_list|,
name|shard
argument_list|,
name|discoNode
argument_list|,
name|indexBlobsMetaData
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|sizeMatched
init|=
literal|0
decl_stmt|;
for|for
control|(
name|StoreFileMetaData
name|storeFileMetaData
range|:
name|storeFilesMetaData
control|)
block|{
if|if
condition|(
name|indexBlobsMetaData
operator|.
name|containsKey
argument_list|(
name|storeFileMetaData
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|indexBlobsMetaData
operator|.
name|get
argument_list|(
name|storeFileMetaData
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|md5
argument_list|()
operator|.
name|equals
argument_list|(
name|storeFileMetaData
operator|.
name|md5
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: [{}] reusing file since it exists on remote node and on gateway (same md5) with size [{}]"
argument_list|,
name|shard
argument_list|,
name|storeFileMetaData
operator|.
name|name
argument_list|()
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|storeFileMetaData
operator|.
name|sizeInBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sizeMatched
operator|+=
name|storeFileMetaData
operator|.
name|sizeInBytes
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: [{}] ignore file since it exists on remote node and on gateway but has different md5, remote node [{}], gateway [{}]"
argument_list|,
name|shard
argument_list|,
name|storeFileMetaData
operator|.
name|name
argument_list|()
argument_list|,
name|storeFileMetaData
operator|.
name|md5
argument_list|()
argument_list|,
name|indexBlobsMetaData
operator|.
name|get
argument_list|(
name|storeFileMetaData
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|md5
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: [{}] exists on remote node, does not exists on gateway"
argument_list|,
name|shard
argument_list|,
name|storeFileMetaData
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
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
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: node elected for pre_allocation [{}], total_size_matched [{}]"
argument_list|,
name|shard
argument_list|,
name|discoNode
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|sizeMatched
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: node ignored for pre_allocation [{}], total_size_matched [{}] smaller than last_size_matched [{}]"
argument_list|,
name|shard
argument_list|,
name|discoNode
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|sizeMatched
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|lastSizeMatched
argument_list|)
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// failed, log and try and allocate based on size
name|logger
operator|.
name|debug
argument_list|(
literal|"Failed to guess allocation of primary based on gateway for "
operator|+
name|shard
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// if its backup, see if there is a primary that *is* allocated, and try and assign a location that is closest to it
comment|// note, since we replicate operations, this might not be the same (different flush intervals)
if|if
condition|(
operator|!
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|MutableShardRouting
name|primaryShard
init|=
name|routingNodes
operator|.
name|findPrimaryForReplica
argument_list|(
name|shard
argument_list|)
decl_stmt|;
if|if
condition|(
name|primaryShard
operator|!=
literal|null
operator|&&
name|primaryShard
operator|.
name|active
argument_list|()
condition|)
block|{
name|TransportNodesListShardStoreMetaData
operator|.
name|NodeStoreFilesMetaData
name|primaryNodeStoreFileMetaData
init|=
name|nodesStoreFilesMetaData
operator|.
name|nodesMap
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
name|primaryNodeStoreFileMetaData
operator|!=
literal|null
operator|&&
name|primaryNodeStoreFileMetaData
operator|.
name|storeFilesMetaData
argument_list|()
operator|!=
literal|null
operator|&&
name|primaryNodeStoreFileMetaData
operator|.
name|storeFilesMetaData
argument_list|()
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
name|IndexStore
operator|.
name|StoreFilesMetaData
name|primaryStoreFilesMetaData
init|=
name|primaryNodeStoreFileMetaData
operator|.
name|storeFilesMetaData
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFileMetaData
name|storeFileMetaData
range|:
name|storeFilesMetaData
control|)
block|{
if|if
condition|(
name|primaryStoreFilesMetaData
operator|.
name|fileExists
argument_list|(
name|storeFileMetaData
operator|.
name|name
argument_list|()
argument_list|)
operator|&&
name|primaryStoreFilesMetaData
operator|.
name|file
argument_list|(
name|storeFileMetaData
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|sizeInBytes
argument_list|()
operator|==
name|storeFileMetaData
operator|.
name|sizeInBytes
argument_list|()
condition|)
block|{
name|sizeMatched
operator|+=
name|storeFileMetaData
operator|.
name|sizeInBytes
argument_list|()
expr_stmt|;
block|}
block|}
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
continue|continue;
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
name|lastNodeMatched
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
block|}
block|}
return|return
name|changed
return|;
block|}
block|}
end_class

end_unit

