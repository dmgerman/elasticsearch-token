begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|store
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|ActionFuture
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
name|FailedNodeException
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
name|support
operator|.
name|nodes
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
name|ClusterService
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
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|threadpool
operator|.
name|ThreadPool
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
name|TransportService
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
name|io
operator|.
name|IOException
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReferenceArray
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportNodesListShardStoreMetaData
specifier|public
class|class
name|TransportNodesListShardStoreMetaData
extends|extends
name|TransportNodesOperationAction
argument_list|<
name|TransportNodesListShardStoreMetaData
operator|.
name|Request
argument_list|,
name|TransportNodesListShardStoreMetaData
operator|.
name|NodesStoreFilesMetaData
argument_list|,
name|TransportNodesListShardStoreMetaData
operator|.
name|NodeRequest
argument_list|,
name|TransportNodesListShardStoreMetaData
operator|.
name|NodeStoreFilesMetaData
argument_list|>
block|{
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|method|TransportNodesListShardStoreMetaData
annotation|@
name|Inject
specifier|public
name|TransportNodesListShardStoreMetaData
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|clusterName
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
block|}
DECL|method|list
specifier|public
name|ActionFuture
argument_list|<
name|NodesStoreFilesMetaData
argument_list|>
name|list
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|boolean
name|onlyUnallocated
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|nodesIds
parameter_list|,
annotation|@
name|Nullable
name|TimeValue
name|timeout
parameter_list|)
block|{
return|return
name|execute
argument_list|(
operator|new
name|Request
argument_list|(
name|shardId
argument_list|,
name|onlyUnallocated
argument_list|,
name|nodesIds
argument_list|)
operator|.
name|timeout
argument_list|(
name|timeout
argument_list|)
argument_list|)
return|;
block|}
DECL|method|transportAction
annotation|@
name|Override
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
literal|"/cluster/nodes/indices/shard/store"
return|;
block|}
DECL|method|transportNodeAction
annotation|@
name|Override
specifier|protected
name|String
name|transportNodeAction
parameter_list|()
block|{
return|return
literal|"/cluster/nodes/indices/shard/store/node"
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|Request
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|Request
argument_list|()
return|;
block|}
DECL|method|newNodeRequest
annotation|@
name|Override
specifier|protected
name|NodeRequest
name|newNodeRequest
parameter_list|()
block|{
return|return
operator|new
name|NodeRequest
argument_list|()
return|;
block|}
DECL|method|newNodeRequest
annotation|@
name|Override
specifier|protected
name|NodeRequest
name|newNodeRequest
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|Request
name|request
parameter_list|)
block|{
return|return
operator|new
name|NodeRequest
argument_list|(
name|nodeId
argument_list|,
name|request
operator|.
name|shardId
argument_list|,
name|request
operator|.
name|unallocated
argument_list|)
return|;
block|}
DECL|method|newNodeResponse
annotation|@
name|Override
specifier|protected
name|NodeStoreFilesMetaData
name|newNodeResponse
parameter_list|()
block|{
return|return
operator|new
name|NodeStoreFilesMetaData
argument_list|()
return|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|NodesStoreFilesMetaData
name|newResponse
parameter_list|(
name|Request
name|request
parameter_list|,
name|AtomicReferenceArray
name|responses
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|NodeStoreFilesMetaData
argument_list|>
name|nodeStoreFilesMetaDatas
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failures
init|=
name|Lists
operator|.
name|newArrayList
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
name|responses
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|resp
init|=
name|responses
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|resp
operator|instanceof
name|NodeStoreFilesMetaData
condition|)
block|{
comment|// will also filter out null response for unallocated ones
name|nodeStoreFilesMetaDatas
operator|.
name|add
argument_list|(
operator|(
name|NodeStoreFilesMetaData
operator|)
name|resp
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|resp
operator|instanceof
name|FailedNodeException
condition|)
block|{
name|failures
operator|.
name|add
argument_list|(
operator|(
name|FailedNodeException
operator|)
name|resp
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|NodesStoreFilesMetaData
argument_list|(
name|clusterName
argument_list|,
name|nodeStoreFilesMetaDatas
operator|.
name|toArray
argument_list|(
operator|new
name|NodeStoreFilesMetaData
index|[
name|nodeStoreFilesMetaDatas
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|failures
operator|.
name|toArray
argument_list|(
operator|new
name|FailedNodeException
index|[
name|failures
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
DECL|method|nodeOperation
annotation|@
name|Override
specifier|protected
name|NodeStoreFilesMetaData
name|nodeOperation
parameter_list|(
name|NodeRequest
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|InternalIndexService
name|indexService
init|=
operator|(
name|InternalIndexService
operator|)
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|unallocated
operator|&&
name|indexService
operator|.
name|hasShard
argument_list|(
name|request
operator|.
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
return|return
operator|new
name|NodeStoreFilesMetaData
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
try|try
block|{
return|return
operator|new
name|NodeStoreFilesMetaData
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
argument_list|,
name|indexService
operator|.
name|store
argument_list|()
operator|.
name|listStoreMetaData
argument_list|(
name|request
operator|.
name|shardId
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"Failed to list store metadata for shard ["
operator|+
name|request
operator|.
name|shardId
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|accumulateExceptions
annotation|@
name|Override
specifier|protected
name|boolean
name|accumulateExceptions
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|class|Request
specifier|static
class|class
name|Request
extends|extends
name|NodesOperationRequest
block|{
DECL|field|shardId
specifier|private
name|ShardId
name|shardId
decl_stmt|;
DECL|field|unallocated
specifier|private
name|boolean
name|unallocated
decl_stmt|;
DECL|method|Request
specifier|public
name|Request
parameter_list|()
block|{         }
DECL|method|Request
specifier|public
name|Request
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|boolean
name|unallocated
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|nodesIds
parameter_list|)
block|{
name|super
argument_list|(
name|nodesIds
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|nodesIds
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|unallocated
operator|=
name|unallocated
expr_stmt|;
block|}
DECL|method|Request
specifier|public
name|Request
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|boolean
name|unallocated
parameter_list|,
name|String
modifier|...
name|nodesIds
parameter_list|)
block|{
name|super
argument_list|(
name|nodesIds
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|unallocated
operator|=
name|unallocated
expr_stmt|;
block|}
DECL|method|timeout
annotation|@
name|Override
specifier|public
name|Request
name|timeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|super
operator|.
name|timeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|shardId
operator|=
name|ShardId
operator|.
name|readShardId
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|unallocated
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|shardId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|unallocated
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NodesStoreFilesMetaData
specifier|public
specifier|static
class|class
name|NodesStoreFilesMetaData
extends|extends
name|NodesOperationResponse
argument_list|<
name|NodeStoreFilesMetaData
argument_list|>
block|{
DECL|field|failures
specifier|private
name|FailedNodeException
index|[]
name|failures
decl_stmt|;
DECL|method|NodesStoreFilesMetaData
name|NodesStoreFilesMetaData
parameter_list|()
block|{         }
DECL|method|NodesStoreFilesMetaData
specifier|public
name|NodesStoreFilesMetaData
parameter_list|(
name|ClusterName
name|clusterName
parameter_list|,
name|NodeStoreFilesMetaData
index|[]
name|nodes
parameter_list|,
name|FailedNodeException
index|[]
name|failures
parameter_list|)
block|{
name|super
argument_list|(
name|clusterName
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
name|this
operator|.
name|failures
operator|=
name|failures
expr_stmt|;
block|}
DECL|method|failures
specifier|public
name|FailedNodeException
index|[]
name|failures
parameter_list|()
block|{
return|return
name|failures
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|nodes
operator|=
operator|new
name|NodeStoreFilesMetaData
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
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
name|nodes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|nodes
index|[
name|i
index|]
operator|=
name|NodeStoreFilesMetaData
operator|.
name|readListShardStoreNodeOperationResponse
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|nodes
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|NodeStoreFilesMetaData
name|response
range|:
name|nodes
control|)
block|{
name|response
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|NodeRequest
specifier|static
class|class
name|NodeRequest
extends|extends
name|NodeOperationRequest
block|{
DECL|field|shardId
specifier|private
name|ShardId
name|shardId
decl_stmt|;
DECL|field|unallocated
specifier|private
name|boolean
name|unallocated
decl_stmt|;
DECL|method|NodeRequest
name|NodeRequest
parameter_list|()
block|{         }
DECL|method|NodeRequest
name|NodeRequest
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|boolean
name|unallocated
parameter_list|)
block|{
name|super
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|unallocated
operator|=
name|unallocated
expr_stmt|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|shardId
operator|=
name|ShardId
operator|.
name|readShardId
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|unallocated
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|shardId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|unallocated
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NodeStoreFilesMetaData
specifier|public
specifier|static
class|class
name|NodeStoreFilesMetaData
extends|extends
name|NodeOperationResponse
block|{
DECL|field|storeFilesMetaData
specifier|private
name|IndexStore
operator|.
name|StoreFilesMetaData
name|storeFilesMetaData
decl_stmt|;
DECL|method|NodeStoreFilesMetaData
name|NodeStoreFilesMetaData
parameter_list|()
block|{         }
DECL|method|NodeStoreFilesMetaData
specifier|public
name|NodeStoreFilesMetaData
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|IndexStore
operator|.
name|StoreFilesMetaData
name|storeFilesMetaData
parameter_list|)
block|{
name|super
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|this
operator|.
name|storeFilesMetaData
operator|=
name|storeFilesMetaData
expr_stmt|;
block|}
DECL|method|storeFilesMetaData
specifier|public
name|IndexStore
operator|.
name|StoreFilesMetaData
name|storeFilesMetaData
parameter_list|()
block|{
return|return
name|storeFilesMetaData
return|;
block|}
DECL|method|readListShardStoreNodeOperationResponse
specifier|public
specifier|static
name|NodeStoreFilesMetaData
name|readListShardStoreNodeOperationResponse
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|NodeStoreFilesMetaData
name|resp
init|=
operator|new
name|NodeStoreFilesMetaData
argument_list|()
decl_stmt|;
name|resp
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|resp
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|storeFilesMetaData
operator|=
name|IndexStore
operator|.
name|StoreFilesMetaData
operator|.
name|readStoreFilesMetaData
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|storeFilesMetaData
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|storeFilesMetaData
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

