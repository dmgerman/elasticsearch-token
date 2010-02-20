begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.replication
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|replication
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
name|ActionResponse
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
name|PrimaryNotStartedActionException
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
name|BaseAction
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
name|TimeoutClusterStateListener
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
name|action
operator|.
name|shard
operator|.
name|ShardStateAction
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
name|Node
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
name|Nodes
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
name|ShardsIterator
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
name|IndexShardMissingException
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
name|IllegalIndexShardStateException
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
name|IndexShardNotStartedException
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
name|IndexMissingException
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
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|io
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
name|util
operator|.
name|io
operator|.
name|VoidStreamable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicInteger
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|TransportShardReplicationOperationAction
specifier|public
specifier|abstract
class|class
name|TransportShardReplicationOperationAction
parameter_list|<
name|Request
extends|extends
name|ShardReplicationOperationRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
extends|extends
name|BaseAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|field|transportService
specifier|protected
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|clusterService
specifier|protected
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|indicesService
specifier|protected
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|threadPool
specifier|protected
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|shardStateAction
specifier|protected
specifier|final
name|ShardStateAction
name|shardStateAction
decl_stmt|;
DECL|method|TransportShardReplicationOperationAction
specifier|protected
name|TransportShardReplicationOperationAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ShardStateAction
name|shardStateAction
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|shardStateAction
operator|=
name|shardStateAction
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|transportAction
argument_list|()
argument_list|,
operator|new
name|OperationTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|transportBackupAction
argument_list|()
argument_list|,
operator|new
name|BackupOperationTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|doExecute
annotation|@
name|Override
specifier|protected
name|void
name|doExecute
parameter_list|(
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
operator|new
name|AsyncShardOperationAction
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|method|newRequestInstance
specifier|protected
specifier|abstract
name|Request
name|newRequestInstance
parameter_list|()
function_decl|;
DECL|method|newResponseInstance
specifier|protected
specifier|abstract
name|Response
name|newResponseInstance
parameter_list|()
function_decl|;
DECL|method|transportAction
specifier|protected
specifier|abstract
name|String
name|transportAction
parameter_list|()
function_decl|;
DECL|method|shardOperationOnPrimary
specifier|protected
specifier|abstract
name|Response
name|shardOperationOnPrimary
parameter_list|(
name|ShardOperationRequest
name|shardRequest
parameter_list|)
function_decl|;
DECL|method|shardOperationOnBackup
specifier|protected
specifier|abstract
name|void
name|shardOperationOnBackup
parameter_list|(
name|ShardOperationRequest
name|shardRequest
parameter_list|)
function_decl|;
DECL|method|shards
specifier|protected
specifier|abstract
name|ShardsIterator
name|shards
parameter_list|(
name|Request
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
comment|/**      * Should the operations be performed on the backups as well. Defaults to<tt>false</tt> meaning operations      * will be executed on the backup.      */
DECL|method|ignoreBackups
specifier|protected
name|boolean
name|ignoreBackups
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|transportBackupAction
specifier|private
name|String
name|transportBackupAction
parameter_list|()
block|{
return|return
name|transportAction
argument_list|()
operator|+
literal|"/backup"
return|;
block|}
DECL|method|indexShard
specifier|protected
name|IndexShard
name|indexShard
parameter_list|(
name|ShardOperationRequest
name|shardRequest
parameter_list|)
block|{
return|return
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|shardRequest
operator|.
name|request
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|shardSafe
argument_list|(
name|shardRequest
operator|.
name|shardId
argument_list|)
return|;
block|}
DECL|class|OperationTransportHandler
specifier|private
class|class
name|OperationTransportHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|Request
argument_list|>
block|{
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|Request
name|newInstance
parameter_list|()
block|{
return|return
name|newRequestInstance
argument_list|()
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
specifier|final
name|Request
name|request
parameter_list|,
specifier|final
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
comment|// no need to have a threaded listener since we just send back a response
name|request
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// if we have a local operation, execute it on a thread since we don't spawn
name|request
operator|.
name|operationThreaded
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|execute
argument_list|(
name|request
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|Response
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|Response
name|result
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
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
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to send response for "
operator|+
name|transportAction
argument_list|()
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|spawn
annotation|@
name|Override
specifier|public
name|boolean
name|spawn
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
DECL|class|BackupOperationTransportHandler
specifier|private
class|class
name|BackupOperationTransportHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|ShardOperationRequest
argument_list|>
block|{
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|ShardOperationRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|ShardOperationRequest
argument_list|()
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ShardOperationRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|shardOperationOnBackup
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|VoidStreamable
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ShardOperationRequest
specifier|protected
class|class
name|ShardOperationRequest
implements|implements
name|Streamable
block|{
DECL|field|shardId
specifier|public
name|int
name|shardId
decl_stmt|;
DECL|field|request
specifier|public
name|Request
name|request
decl_stmt|;
DECL|method|ShardOperationRequest
specifier|public
name|ShardOperationRequest
parameter_list|()
block|{         }
DECL|method|ShardOperationRequest
specifier|public
name|ShardOperationRequest
parameter_list|(
name|int
name|shardId
parameter_list|,
name|Request
name|request
parameter_list|)
block|{
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|shardId
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|request
operator|=
name|newRequestInstance
argument_list|()
expr_stmt|;
name|request
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
name|request
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|AsyncShardOperationAction
specifier|private
class|class
name|AsyncShardOperationAction
block|{
DECL|field|listener
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
decl_stmt|;
DECL|field|request
specifier|private
specifier|final
name|Request
name|request
decl_stmt|;
DECL|field|nodes
specifier|private
name|Nodes
name|nodes
decl_stmt|;
DECL|field|shards
specifier|private
name|ShardsIterator
name|shards
decl_stmt|;
DECL|field|primaryOperationStarted
specifier|private
specifier|final
name|AtomicBoolean
name|primaryOperationStarted
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|method|AsyncShardOperationAction
specifier|private
name|AsyncShardOperationAction
parameter_list|(
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
block|}
DECL|method|start
specifier|public
name|void
name|start
parameter_list|()
block|{
name|start
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**          * Returns<tt>true</tt> if the action starting to be performed on the primary (or is done).          */
DECL|method|start
specifier|public
name|boolean
name|start
parameter_list|(
specifier|final
name|boolean
name|fromClusterEvent
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|nodes
operator|=
name|clusterState
operator|.
name|nodes
argument_list|()
expr_stmt|;
try|try
block|{
name|shards
operator|=
name|shards
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ReplicationShardOperationFailedException
argument_list|(
name|shards
operator|.
name|shardId
argument_list|()
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|boolean
name|foundPrimary
init|=
literal|false
decl_stmt|;
for|for
control|(
specifier|final
name|ShardRouting
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
if|if
condition|(
operator|!
name|shard
operator|.
name|active
argument_list|()
condition|)
block|{
name|retryPrimary
argument_list|(
name|fromClusterEvent
argument_list|,
name|shard
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|primaryOperationStarted
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|foundPrimary
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|shard
operator|.
name|currentNodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|nodes
operator|.
name|localNodeId
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|operationThreaded
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|performOnPrimary
argument_list|(
name|shard
operator|.
name|id
argument_list|()
argument_list|,
name|fromClusterEvent
argument_list|,
literal|true
argument_list|,
name|shard
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|performOnPrimary
argument_list|(
name|shard
operator|.
name|id
argument_list|()
argument_list|,
name|fromClusterEvent
argument_list|,
literal|false
argument_list|,
name|shard
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|Node
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|transportAction
argument_list|()
argument_list|,
name|request
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|Response
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Response
name|newInstance
parameter_list|()
block|{
return|return
name|newResponseInstance
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|Response
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|RemoteTransportException
name|exp
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|exp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|spawn
parameter_list|()
block|{
return|return
name|request
operator|.
name|listenerThreaded
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
comment|// we should never get here, but here we go
if|if
condition|(
operator|!
name|foundPrimary
condition|)
block|{
specifier|final
name|PrimaryNotStartedActionException
name|failure
init|=
operator|new
name|PrimaryNotStartedActionException
argument_list|(
name|shards
operator|.
name|shardId
argument_list|()
argument_list|,
literal|"Primary not found"
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
DECL|method|retryPrimary
specifier|private
name|void
name|retryPrimary
parameter_list|(
name|boolean
name|fromClusterEvent
parameter_list|,
specifier|final
name|ShardRouting
name|shard
parameter_list|)
block|{
if|if
condition|(
operator|!
name|fromClusterEvent
condition|)
block|{
comment|// make it threaded operation so we fork on the discovery listener thread
name|request
operator|.
name|operationThreaded
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|add
argument_list|(
name|request
operator|.
name|timeout
argument_list|()
argument_list|,
operator|new
name|TimeoutClusterStateListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|clusterChanged
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
if|if
condition|(
name|start
argument_list|(
literal|true
argument_list|)
condition|)
block|{
comment|// if we managed to start and perform the operation on the primary, we can remove this listener
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onTimeout
parameter_list|(
name|TimeValue
name|timeValue
parameter_list|)
block|{
specifier|final
name|PrimaryNotStartedActionException
name|failure
init|=
operator|new
name|PrimaryNotStartedActionException
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
literal|"Timeout waiting for ["
operator|+
name|timeValue
operator|+
literal|"]"
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|performOnPrimary
specifier|private
name|void
name|performOnPrimary
parameter_list|(
name|int
name|primaryShardId
parameter_list|,
name|boolean
name|fromDiscoveryListener
parameter_list|,
name|boolean
name|alreadyThreaded
parameter_list|,
specifier|final
name|ShardRouting
name|shard
parameter_list|)
block|{
try|try
block|{
name|Response
name|response
init|=
name|shardOperationOnPrimary
argument_list|(
operator|new
name|ShardOperationRequest
argument_list|(
name|primaryShardId
argument_list|,
name|request
argument_list|)
argument_list|)
decl_stmt|;
name|performBackups
argument_list|(
name|response
argument_list|,
name|alreadyThreaded
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexShardNotStartedException
name|e
parameter_list|)
block|{
comment|// still in recovery, retry (we know that its not UNASSIGNED OR INITIALIZING since we are checking it in the calling method)
name|retryPrimary
argument_list|(
name|fromDiscoveryListener
argument_list|,
name|shard
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ReplicationShardOperationFailedException
argument_list|(
name|shards
operator|.
name|shardId
argument_list|()
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|performBackups
specifier|private
name|void
name|performBackups
parameter_list|(
specifier|final
name|Response
name|response
parameter_list|,
name|boolean
name|alreadyThreaded
parameter_list|)
block|{
if|if
condition|(
name|ignoreBackups
argument_list|()
operator|||
name|shards
operator|.
name|size
argument_list|()
operator|==
literal|1
comment|/* no backups */
condition|)
block|{
if|if
condition|(
name|alreadyThreaded
operator|||
operator|!
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// initialize the counter
name|int
name|backupCounter
init|=
literal|0
decl_stmt|;
for|for
control|(
specifier|final
name|ShardRouting
name|shard
range|:
name|shards
operator|.
name|reset
argument_list|()
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
continue|continue;
block|}
name|backupCounter
operator|++
expr_stmt|;
comment|// if we are relocating the backup, we want to perform the index operation on both the relocating
comment|// shard and the target shard. This means that we won't loose index operations between end of recovery
comment|// and reassignment of the shard by the master node
if|if
condition|(
name|shard
operator|.
name|relocating
argument_list|()
condition|)
block|{
name|backupCounter
operator|++
expr_stmt|;
block|}
block|}
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|backupCounter
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|ShardRouting
name|shard
range|:
name|shards
operator|.
name|reset
argument_list|()
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
continue|continue;
block|}
comment|// we index on a backup that is initializing as well since we might not have got the event
comment|// yet that it was started. We will get an exception IllegalShardState exception if its not started
comment|// and that's fine, we will ignore it
if|if
condition|(
name|shard
operator|.
name|unassigned
argument_list|()
condition|)
block|{
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|alreadyThreaded
operator|||
operator|!
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
continue|continue;
block|}
name|performOnBackup
argument_list|(
name|response
argument_list|,
name|counter
argument_list|,
name|shard
argument_list|,
name|shard
operator|.
name|currentNodeId
argument_list|()
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
name|performOnBackup
argument_list|(
name|response
argument_list|,
name|counter
argument_list|,
name|shard
argument_list|,
name|shard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|performOnBackup
specifier|private
name|void
name|performOnBackup
parameter_list|(
specifier|final
name|Response
name|response
parameter_list|,
specifier|final
name|AtomicInteger
name|counter
parameter_list|,
specifier|final
name|ShardRouting
name|shard
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
specifier|final
name|ShardOperationRequest
name|shardRequest
init|=
operator|new
name|ShardOperationRequest
argument_list|(
name|shards
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|nodeId
operator|.
name|equals
argument_list|(
name|nodes
operator|.
name|localNodeId
argument_list|()
argument_list|)
condition|)
block|{
name|Node
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|nodeId
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|transportBackupAction
argument_list|()
argument_list|,
name|shardRequest
argument_list|,
operator|new
name|VoidTransportResponseHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|VoidStreamable
name|vResponse
parameter_list|)
block|{
name|finishIfPossible
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|RemoteTransportException
name|exp
parameter_list|)
block|{
if|if
condition|(
operator|!
name|ignoreBackupException
argument_list|(
name|exp
operator|.
name|unwrapCause
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to perform "
operator|+
name|transportAction
argument_list|()
operator|+
literal|" on backup "
operator|+
name|shards
operator|.
name|shardId
argument_list|()
argument_list|,
name|exp
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|shardFailed
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
name|finishIfPossible
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|finishIfPossible
parameter_list|()
block|{
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|spawn
parameter_list|()
block|{
comment|// don't spawn, we will call the listener on a thread pool if needed
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|request
operator|.
name|operationThreaded
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|shardOperationOnBackup
argument_list|(
name|shardRequest
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|ignoreBackupException
argument_list|(
name|e
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to perform "
operator|+
name|transportAction
argument_list|()
operator|+
literal|" on backup "
operator|+
name|shards
operator|.
name|shardId
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|shardFailed
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|shardOperationOnBackup
argument_list|(
name|shardRequest
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|ignoreBackupException
argument_list|(
name|e
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to perform "
operator|+
name|transportAction
argument_list|()
operator|+
literal|" on backup "
operator|+
name|shards
operator|.
name|shardId
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|shardFailed
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**          * Should an exception be ignored when the operation is performed on the backup. The exception          * is ignored if it is:          *          *<ul>          *<li><tt>IllegalIndexShardStateException</tt>: The shard has not yet moved to started mode (it is still recovering).          *<li><tt>IndexMissingException</tt>/<tt>IndexShardMissingException</tt>: The shard has not yet started to initialize on the target node.          *</ul>          */
DECL|method|ignoreBackupException
specifier|private
name|boolean
name|ignoreBackupException
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|IllegalIndexShardStateException
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|e
operator|instanceof
name|IndexMissingException
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|e
operator|instanceof
name|IndexShardMissingException
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

