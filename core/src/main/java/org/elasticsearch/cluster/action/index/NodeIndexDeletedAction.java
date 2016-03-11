begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.action.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|action
operator|.
name|index
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|LockObtainFailedException
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|AbstractRunnable
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
name|index
operator|.
name|IndexSettings
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
name|EmptyTransportResponseHandler
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
name|TransportChannel
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
name|TransportRequest
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
name|TransportRequestHandler
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
name|TransportResponse
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
name|List
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
name|CopyOnWriteArrayList
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NodeIndexDeletedAction
specifier|public
class|class
name|NodeIndexDeletedAction
extends|extends
name|AbstractComponent
block|{
DECL|field|INDEX_DELETED_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_DELETED_ACTION_NAME
init|=
literal|"internal:cluster/node/index/deleted"
decl_stmt|;
DECL|field|INDEX_STORE_DELETED_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_STORE_DELETED_ACTION_NAME
init|=
literal|"internal:cluster/node/index_store/deleted"
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|listeners
specifier|private
specifier|final
name|List
argument_list|<
name|Listener
argument_list|>
name|listeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
annotation|@
name|Inject
DECL|method|NodeIndexDeletedAction
specifier|public
name|NodeIndexDeletedAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
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
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|INDEX_DELETED_ACTION_NAME
argument_list|,
name|NodeIndexDeletedMessage
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|NodeIndexDeletedTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|INDEX_STORE_DELETED_ACTION_NAME
argument_list|,
name|NodeIndexStoreDeletedMessage
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|NodeIndexStoreDeletedTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|remove
specifier|public
name|void
name|remove
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|nodeIndexDeleted
specifier|public
name|void
name|nodeIndexDeleted
parameter_list|(
specifier|final
name|ClusterState
name|clusterState
parameter_list|,
specifier|final
name|String
name|index
parameter_list|,
specifier|final
name|IndexSettings
name|indexSettings
parameter_list|,
specifier|final
name|String
name|nodeId
parameter_list|)
block|{
specifier|final
name|DiscoveryNodes
name|nodes
init|=
name|clusterState
operator|.
name|nodes
argument_list|()
decl_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
argument_list|,
name|INDEX_DELETED_ACTION_NAME
argument_list|,
operator|new
name|NodeIndexDeletedMessage
argument_list|(
name|index
argument_list|,
name|nodeId
argument_list|)
argument_list|,
name|EmptyTransportResponseHandler
operator|.
name|INSTANCE_SAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|nodes
operator|.
name|localNode
argument_list|()
operator|.
name|isDataNode
argument_list|()
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}] not acking store deletion (not a data node)"
argument_list|,
name|index
argument_list|)
expr_stmt|;
return|return;
block|}
name|threadPool
operator|.
name|generic
argument_list|()
operator|.
name|execute
argument_list|(
operator|new
name|AbstractRunnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to ack index store deleted for index"
argument_list|,
name|t
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doRun
parameter_list|()
throws|throws
name|Exception
block|{
name|lockIndexAndAck
argument_list|(
name|index
argument_list|,
name|nodes
argument_list|,
name|nodeId
argument_list|,
name|clusterState
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|lockIndexAndAck
specifier|private
name|void
name|lockIndexAndAck
parameter_list|(
name|String
name|index
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|,
name|String
name|nodeId
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|,
name|IndexSettings
name|indexSettings
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
comment|// we are waiting until we can lock the index / all shards on the node and then we ack the delete of the store to the
comment|// master. If we can't acquire the locks here immediately there might be a shard of this index still holding on to the lock
comment|// due to a "currently canceled recovery" or so. The shard will delete itself BEFORE the lock is released so it's guaranteed to be
comment|// deleted by the time we get the lock
name|indicesService
operator|.
name|processPendingDeletes
argument_list|(
name|indexSettings
operator|.
name|getIndex
argument_list|()
argument_list|,
name|indexSettings
argument_list|,
operator|new
name|TimeValue
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
argument_list|,
name|INDEX_STORE_DELETED_ACTION_NAME
argument_list|,
operator|new
name|NodeIndexStoreDeletedMessage
argument_list|(
name|index
argument_list|,
name|nodeId
argument_list|)
argument_list|,
name|EmptyTransportResponseHandler
operator|.
name|INSTANCE_SAME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockObtainFailedException
name|exc
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to lock all shards for index - timed out after 30 seconds"
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to lock all shards for index - interrupted"
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
block|}
DECL|interface|Listener
specifier|public
interface|interface
name|Listener
block|{
DECL|method|onNodeIndexDeleted
name|void
name|onNodeIndexDeleted
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|nodeId
parameter_list|)
function_decl|;
DECL|method|onNodeIndexStoreDeleted
name|void
name|onNodeIndexStoreDeleted
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|nodeId
parameter_list|)
function_decl|;
block|}
DECL|class|NodeIndexDeletedTransportHandler
specifier|private
class|class
name|NodeIndexDeletedTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|NodeIndexDeletedMessage
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|NodeIndexDeletedMessage
name|message
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|Listener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|onNodeIndexDeleted
argument_list|(
name|message
operator|.
name|index
argument_list|,
name|message
operator|.
name|nodeId
argument_list|)
expr_stmt|;
block|}
name|channel
operator|.
name|sendResponse
argument_list|(
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NodeIndexStoreDeletedTransportHandler
specifier|private
class|class
name|NodeIndexStoreDeletedTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|NodeIndexStoreDeletedMessage
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|NodeIndexStoreDeletedMessage
name|message
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|Listener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|onNodeIndexStoreDeleted
argument_list|(
name|message
operator|.
name|index
argument_list|,
name|message
operator|.
name|nodeId
argument_list|)
expr_stmt|;
block|}
name|channel
operator|.
name|sendResponse
argument_list|(
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NodeIndexDeletedMessage
specifier|public
specifier|static
class|class
name|NodeIndexDeletedMessage
extends|extends
name|TransportRequest
block|{
DECL|field|index
name|String
name|index
decl_stmt|;
DECL|field|nodeId
name|String
name|nodeId
decl_stmt|;
DECL|method|NodeIndexDeletedMessage
specifier|public
name|NodeIndexDeletedMessage
parameter_list|()
block|{         }
DECL|method|NodeIndexDeletedMessage
name|NodeIndexDeletedMessage
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|nodeId
operator|=
name|nodeId
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
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|nodeId
argument_list|)
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
name|index
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|nodeId
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|NodeIndexStoreDeletedMessage
specifier|public
specifier|static
class|class
name|NodeIndexStoreDeletedMessage
extends|extends
name|TransportRequest
block|{
DECL|field|index
name|String
name|index
decl_stmt|;
DECL|field|nodeId
name|String
name|nodeId
decl_stmt|;
DECL|method|NodeIndexStoreDeletedMessage
specifier|public
name|NodeIndexStoreDeletedMessage
parameter_list|()
block|{         }
DECL|method|NodeIndexStoreDeletedMessage
name|NodeIndexStoreDeletedMessage
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|nodeId
operator|=
name|nodeId
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
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|nodeId
argument_list|)
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
name|index
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|nodeId
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

