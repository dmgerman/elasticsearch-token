begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
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
name|ElasticsearchException
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
name|ActionFilters
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|env
operator|.
name|NodeEnvironment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|AsyncShardFetch
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
name|IndexService
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
name|IndexShard
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
name|shard
operator|.
name|ShardPath
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
name|IndexStoreModule
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
name|Store
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
comment|/**  *  */
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
implements|implements
name|AsyncShardFetch
operator|.
name|List
argument_list|<
name|TransportNodesListShardStoreMetaData
operator|.
name|NodesStoreFilesMetaData
argument_list|,
name|TransportNodesListShardStoreMetaData
operator|.
name|NodeStoreFilesMetaData
argument_list|>
block|{
DECL|field|ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|ACTION_NAME
init|=
literal|"internal:cluster/nodes/indices/shard/store"
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|nodeEnv
specifier|private
specifier|final
name|NodeEnvironment
name|nodeEnv
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportNodesListShardStoreMetaData
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
parameter_list|,
name|NodeEnvironment
name|nodeEnv
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|ACTION_NAME
argument_list|,
name|clusterName
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|Request
operator|.
name|class
argument_list|,
name|NodeRequest
operator|.
name|class
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|FETCH_SHARD_STORE
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
name|nodeEnv
operator|=
name|nodeEnv
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|list
specifier|public
name|void
name|list
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|IndexMetaData
name|indexMetaData
parameter_list|,
name|String
index|[]
name|nodesIds
parameter_list|,
name|ActionListener
argument_list|<
name|NodesStoreFilesMetaData
argument_list|>
name|listener
parameter_list|)
block|{
name|execute
argument_list|(
operator|new
name|Request
argument_list|(
name|shardId
argument_list|,
literal|false
argument_list|,
name|nodesIds
argument_list|)
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newNodeRequest
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
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newNodeResponse
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
annotation|@
name|Override
DECL|method|newResponse
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
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"unknown response type [{}], expected NodeStoreFilesMetaData or FailedNodeException"
argument_list|,
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
annotation|@
name|Override
DECL|method|nodeOperation
specifier|protected
name|NodeStoreFilesMetaData
name|nodeOperation
parameter_list|(
name|NodeRequest
name|request
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|unallocated
condition|)
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
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
name|indexService
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|NodeStoreFilesMetaData
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
if|if
condition|(
operator|!
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
name|localNode
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
name|IndexMetaData
name|metaData
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|index
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
name|metaData
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|NodeStoreFilesMetaData
argument_list|(
name|clusterService
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
name|localNode
argument_list|()
argument_list|,
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
name|ElasticsearchException
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
DECL|method|listStoreMetaData
specifier|private
name|StoreFilesMetaData
name|listStoreMetaData
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
throws|throws
name|IOException
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"listing store meta data for {}"
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|long
name|startTimeNS
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|boolean
name|exists
init|=
literal|false
decl_stmt|;
try|try
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
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
name|indexService
operator|!=
literal|null
condition|)
block|{
name|IndexShard
name|indexShard
init|=
name|indexService
operator|.
name|shard
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexShard
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Store
name|store
init|=
name|indexShard
operator|.
name|store
argument_list|()
decl_stmt|;
name|store
operator|.
name|incRef
argument_list|()
expr_stmt|;
try|try
block|{
name|exists
operator|=
literal|true
expr_stmt|;
return|return
operator|new
name|StoreFilesMetaData
argument_list|(
literal|true
argument_list|,
name|shardId
argument_list|,
name|store
operator|.
name|getMetadataOrEmpty
argument_list|()
argument_list|)
return|;
block|}
finally|finally
block|{
name|store
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|// try and see if we an list unallocated
name|IndexMetaData
name|metaData
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
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
name|metaData
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|StoreFilesMetaData
argument_list|(
literal|false
argument_list|,
name|shardId
argument_list|,
name|Store
operator|.
name|MetadataSnapshot
operator|.
name|EMPTY
argument_list|)
return|;
block|}
name|String
name|storeType
init|=
name|metaData
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
name|IndexStoreModule
operator|.
name|STORE_TYPE
argument_list|,
literal|"fs"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|storeType
operator|.
name|contains
argument_list|(
literal|"fs"
argument_list|)
condition|)
block|{
return|return
operator|new
name|StoreFilesMetaData
argument_list|(
literal|false
argument_list|,
name|shardId
argument_list|,
name|Store
operator|.
name|MetadataSnapshot
operator|.
name|EMPTY
argument_list|)
return|;
block|}
specifier|final
name|ShardPath
name|shardPath
init|=
name|ShardPath
operator|.
name|loadShardPath
argument_list|(
name|logger
argument_list|,
name|nodeEnv
argument_list|,
name|shardId
argument_list|,
name|metaData
operator|.
name|settings
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardPath
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|StoreFilesMetaData
argument_list|(
literal|false
argument_list|,
name|shardId
argument_list|,
name|Store
operator|.
name|MetadataSnapshot
operator|.
name|EMPTY
argument_list|)
return|;
block|}
return|return
operator|new
name|StoreFilesMetaData
argument_list|(
literal|false
argument_list|,
name|shardId
argument_list|,
name|Store
operator|.
name|readMetadataSnapshot
argument_list|(
name|shardPath
operator|.
name|resolveIndex
argument_list|()
argument_list|,
name|logger
argument_list|)
argument_list|)
return|;
block|}
finally|finally
block|{
name|TimeValue
name|took
init|=
operator|new
name|TimeValue
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTimeNS
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
decl_stmt|;
if|if
condition|(
name|exists
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} loaded store meta data (took [{}])"
argument_list|,
name|shardId
argument_list|,
name|took
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} didn't find any store meta data to load (took [{}])"
argument_list|,
name|shardId
argument_list|,
name|took
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|accumulateExceptions
specifier|protected
name|boolean
name|accumulateExceptions
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|class|StoreFilesMetaData
specifier|public
specifier|static
class|class
name|StoreFilesMetaData
implements|implements
name|Iterable
argument_list|<
name|StoreFileMetaData
argument_list|>
implements|,
name|Streamable
block|{
comment|// here also trasmit sync id, else recovery will not use sync id because of stupid gateway allocator every now and then...
DECL|field|allocated
specifier|private
name|boolean
name|allocated
decl_stmt|;
DECL|field|shardId
specifier|private
name|ShardId
name|shardId
decl_stmt|;
DECL|field|metadataSnapshot
name|Store
operator|.
name|MetadataSnapshot
name|metadataSnapshot
decl_stmt|;
DECL|method|StoreFilesMetaData
name|StoreFilesMetaData
parameter_list|()
block|{         }
DECL|method|StoreFilesMetaData
specifier|public
name|StoreFilesMetaData
parameter_list|(
name|boolean
name|allocated
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|Store
operator|.
name|MetadataSnapshot
name|metadataSnapshot
parameter_list|)
block|{
name|this
operator|.
name|allocated
operator|=
name|allocated
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|metadataSnapshot
operator|=
name|metadataSnapshot
expr_stmt|;
block|}
DECL|method|allocated
specifier|public
name|boolean
name|allocated
parameter_list|()
block|{
return|return
name|allocated
return|;
block|}
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardId
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|StoreFileMetaData
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|metadataSnapshot
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|fileExists
specifier|public
name|boolean
name|fileExists
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|metadataSnapshot
operator|.
name|asMap
argument_list|()
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|file
specifier|public
name|StoreFileMetaData
name|file
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|metadataSnapshot
operator|.
name|asMap
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|readStoreFilesMetaData
specifier|public
specifier|static
name|StoreFilesMetaData
name|readStoreFilesMetaData
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|StoreFilesMetaData
name|md
init|=
operator|new
name|StoreFilesMetaData
argument_list|()
decl_stmt|;
name|md
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|md
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
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
name|allocated
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
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
name|this
operator|.
name|metadataSnapshot
operator|=
operator|new
name|Store
operator|.
name|MetadataSnapshot
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
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
name|out
operator|.
name|writeBoolean
argument_list|(
name|allocated
argument_list|)
expr_stmt|;
name|shardId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|metadataSnapshot
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
DECL|method|syncId
specifier|public
name|String
name|syncId
parameter_list|()
block|{
return|return
name|metadataSnapshot
operator|.
name|getSyncId
argument_list|()
return|;
block|}
block|}
DECL|class|Request
specifier|static
class|class
name|Request
extends|extends
name|NodesOperationRequest
argument_list|<
name|Request
argument_list|>
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
annotation|@
name|Override
DECL|method|readFrom
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
annotation|@
name|Override
DECL|method|writeTo
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
annotation|@
name|Override
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
annotation|@
name|Override
DECL|method|readFrom
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
annotation|@
name|Override
DECL|method|writeTo
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
name|TransportNodesListShardStoreMetaData
operator|.
name|Request
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|request
argument_list|,
name|nodeId
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|request
operator|.
name|shardId
expr_stmt|;
name|this
operator|.
name|unallocated
operator|=
name|request
operator|.
name|unallocated
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
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
annotation|@
name|Override
DECL|method|writeTo
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
annotation|@
name|Override
DECL|method|readFrom
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
name|StoreFilesMetaData
operator|.
name|readStoreFilesMetaData
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeTo
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

