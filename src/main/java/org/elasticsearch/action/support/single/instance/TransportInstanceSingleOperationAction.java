begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.single.instance
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|single
operator|.
name|instance
package|;
end_package

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
name|UnavailableShardsException
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
name|HandledTransportAction
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
name|TransportAction
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
name|ClusterStateObserver
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
name|ClusterBlockException
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
name|ClusterBlockLevel
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
name|ShardIterator
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
name|common
operator|.
name|Nullable
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
name|node
operator|.
name|NodeClosedException
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportInstanceSingleOperationAction
specifier|public
specifier|abstract
class|class
name|TransportInstanceSingleOperationAction
parameter_list|<
name|Request
extends|extends
name|InstanceShardOperationRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
extends|extends
name|HandledTransportAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|field|clusterService
specifier|protected
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|transportService
specifier|protected
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|executor
specifier|final
name|String
name|executor
decl_stmt|;
DECL|method|TransportInstanceSingleOperationAction
specifier|protected
name|TransportInstanceSingleOperationAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|actionName
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
name|ActionFilters
name|actionFilters
parameter_list|,
name|Class
argument_list|<
name|Request
argument_list|>
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|actionName
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
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
name|AsyncSingleAction
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
DECL|method|executor
specifier|protected
specifier|abstract
name|String
name|executor
parameter_list|()
function_decl|;
DECL|method|shardOperation
specifier|protected
specifier|abstract
name|void
name|shardOperation
parameter_list|(
name|InternalRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|newResponse
specifier|protected
specifier|abstract
name|Response
name|newResponse
parameter_list|()
function_decl|;
DECL|method|checkGlobalBlock
specifier|protected
name|ClusterBlockException
name|checkGlobalBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|globalBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|WRITE
argument_list|)
return|;
block|}
DECL|method|checkRequestBlock
specifier|protected
name|ClusterBlockException
name|checkRequestBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|InternalRequest
name|request
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indexBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|WRITE
argument_list|,
name|request
operator|.
name|concreteIndex
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Resolves the request. If the resolve means a different execution, then return false      * here to indicate not to continue and execute this request.      */
DECL|method|resolveRequest
specifier|protected
specifier|abstract
name|boolean
name|resolveRequest
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|InternalRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|retryOnFailure
specifier|protected
name|boolean
name|retryOnFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|transportOptions
specifier|protected
name|TransportRequestOptions
name|transportOptions
parameter_list|()
block|{
return|return
name|TransportRequestOptions
operator|.
name|EMPTY
return|;
block|}
comment|/**      * Should return an iterator with a single shard!      */
DECL|method|shards
specifier|protected
specifier|abstract
name|ShardIterator
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|InternalRequest
name|request
parameter_list|)
function_decl|;
DECL|class|AsyncSingleAction
class|class
name|AsyncSingleAction
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
DECL|field|internalRequest
specifier|private
specifier|final
name|InternalRequest
name|internalRequest
decl_stmt|;
DECL|field|observer
specifier|private
specifier|volatile
name|ClusterStateObserver
name|observer
decl_stmt|;
DECL|field|shardIt
specifier|private
name|ShardIterator
name|shardIt
decl_stmt|;
DECL|field|nodes
specifier|private
name|DiscoveryNodes
name|nodes
decl_stmt|;
DECL|field|operationStarted
specifier|private
specifier|final
name|AtomicBoolean
name|operationStarted
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|method|AsyncSingleAction
specifier|private
name|AsyncSingleAction
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
name|internalRequest
operator|=
operator|new
name|InternalRequest
argument_list|(
name|request
argument_list|)
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
name|this
operator|.
name|observer
operator|=
operator|new
name|ClusterStateObserver
argument_list|(
name|clusterService
argument_list|,
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|timeout
argument_list|()
argument_list|,
name|logger
argument_list|)
expr_stmt|;
name|doStart
argument_list|()
expr_stmt|;
block|}
DECL|method|doStart
specifier|protected
name|boolean
name|doStart
parameter_list|()
block|{
name|nodes
operator|=
name|observer
operator|.
name|observedState
argument_list|()
operator|.
name|nodes
argument_list|()
expr_stmt|;
try|try
block|{
name|ClusterBlockException
name|blockException
init|=
name|checkGlobalBlock
argument_list|(
name|observer
operator|.
name|observedState
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockException
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|blockException
operator|.
name|retryable
argument_list|()
condition|)
block|{
name|retry
argument_list|(
name|blockException
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
else|else
block|{
throw|throw
name|blockException
throw|;
block|}
block|}
name|internalRequest
operator|.
name|concreteIndex
argument_list|(
name|observer
operator|.
name|observedState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|concreteSingleIndex
argument_list|(
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|index
argument_list|()
argument_list|,
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// check if we need to execute, and if not, return
if|if
condition|(
operator|!
name|resolveRequest
argument_list|(
name|observer
operator|.
name|observedState
argument_list|()
argument_list|,
name|internalRequest
argument_list|,
name|listener
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|blockException
operator|=
name|checkRequestBlock
argument_list|(
name|observer
operator|.
name|observedState
argument_list|()
argument_list|,
name|internalRequest
argument_list|)
expr_stmt|;
if|if
condition|(
name|blockException
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|blockException
operator|.
name|retryable
argument_list|()
condition|)
block|{
name|retry
argument_list|(
name|blockException
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
else|else
block|{
throw|throw
name|blockException
throw|;
block|}
block|}
name|shardIt
operator|=
name|shards
argument_list|(
name|observer
operator|.
name|observedState
argument_list|()
argument_list|,
name|internalRequest
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|// no shardIt, might be in the case between index gateway recovery and shardIt initialization
if|if
condition|(
name|shardIt
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|retry
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// this transport only make sense with an iterator that returns a single shard routing (like primary)
assert|assert
name|shardIt
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|ShardRouting
name|shard
init|=
name|shardIt
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
assert|assert
name|shard
operator|!=
literal|null
assert|;
if|if
condition|(
operator|!
name|shard
operator|.
name|active
argument_list|()
condition|)
block|{
name|retry
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|operationStarted
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
literal|true
return|;
block|}
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|shardId
operator|=
name|shardIt
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
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
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|beforeLocalFork
argument_list|()
expr_stmt|;
try|try
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|executor
argument_list|)
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
name|shardOperation
argument_list|(
name|internalRequest
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|retryOnFailure
argument_list|(
name|e
argument_list|)
condition|)
block|{
name|operationStarted
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// we already marked it as started when we executed it (removed the listener) so pass false
comment|// to re-add to the cluster listener
name|retry
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|retryOnFailure
argument_list|(
name|e
argument_list|)
condition|)
block|{
name|retry
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|DiscoveryNode
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
name|actionName
argument_list|,
name|internalRequest
operator|.
name|request
argument_list|()
argument_list|,
name|transportOptions
argument_list|()
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
name|newResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
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
name|TransportException
name|exp
parameter_list|)
block|{
comment|// if we got disconnected from the node, or the node / shard is not in the right state (being closed)
if|if
condition|(
name|exp
operator|.
name|unwrapCause
argument_list|()
operator|instanceof
name|ConnectTransportException
operator|||
name|exp
operator|.
name|unwrapCause
argument_list|()
operator|instanceof
name|NodeClosedException
operator|||
name|retryOnFailure
argument_list|(
name|exp
argument_list|)
condition|)
block|{
name|operationStarted
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// we already marked it as started when we executed it (removed the listener) so pass false
comment|// to re-add to the cluster listener
name|retry
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|exp
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
DECL|method|retry
name|void
name|retry
parameter_list|(
specifier|final
annotation|@
name|Nullable
name|Throwable
name|failure
parameter_list|)
block|{
if|if
condition|(
name|observer
operator|.
name|isTimedOut
argument_list|()
condition|)
block|{
comment|// we running as a last attempt after a timeout has happened. don't retry
return|return;
block|}
comment|// make it threaded operation so we fork on the discovery listener thread
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|beforeLocalFork
argument_list|()
expr_stmt|;
name|observer
operator|.
name|waitForNextChange
argument_list|(
operator|new
name|ClusterStateObserver
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onNewClusterState
parameter_list|(
name|ClusterState
name|state
parameter_list|)
block|{
name|doStart
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onClusterServiceClose
parameter_list|()
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|NodeClosedException
argument_list|(
name|nodes
operator|.
name|localNode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onTimeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
comment|// just to be on the safe side, see if we can start it now?
if|if
condition|(
operator|!
name|doStart
argument_list|()
condition|)
block|{
name|Throwable
name|listenFailure
init|=
name|failure
decl_stmt|;
if|if
condition|(
name|listenFailure
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|shardIt
operator|==
literal|null
condition|)
block|{
name|listenFailure
operator|=
operator|new
name|UnavailableShardsException
argument_list|(
operator|new
name|ShardId
argument_list|(
name|internalRequest
operator|.
name|concreteIndex
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
literal|"Timeout waiting for ["
operator|+
name|timeout
operator|+
literal|"], request: "
operator|+
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listenFailure
operator|=
operator|new
name|UnavailableShardsException
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
literal|"["
operator|+
name|shardIt
operator|.
name|size
argument_list|()
operator|+
literal|"] shardIt, ["
operator|+
name|shardIt
operator|.
name|sizeActive
argument_list|()
operator|+
literal|"] active : Timeout waiting for ["
operator|+
name|timeout
operator|+
literal|"], request: "
operator|+
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|listener
operator|.
name|onFailure
argument_list|(
name|listenFailure
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|,
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|timeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Internal request class that gets built on each node. Holds the original request plus additional info.      */
DECL|class|InternalRequest
specifier|protected
class|class
name|InternalRequest
block|{
DECL|field|request
specifier|final
name|Request
name|request
decl_stmt|;
DECL|field|concreteIndex
name|String
name|concreteIndex
decl_stmt|;
DECL|method|InternalRequest
name|InternalRequest
parameter_list|(
name|Request
name|request
parameter_list|)
block|{
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
block|}
DECL|method|request
specifier|public
name|Request
name|request
parameter_list|()
block|{
return|return
name|request
return|;
block|}
DECL|method|concreteIndex
name|void
name|concreteIndex
parameter_list|(
name|String
name|concreteIndex
parameter_list|)
block|{
name|this
operator|.
name|concreteIndex
operator|=
name|concreteIndex
expr_stmt|;
block|}
DECL|method|concreteIndex
specifier|public
name|String
name|concreteIndex
parameter_list|()
block|{
return|return
name|concreteIndex
return|;
block|}
block|}
block|}
end_class

end_unit

