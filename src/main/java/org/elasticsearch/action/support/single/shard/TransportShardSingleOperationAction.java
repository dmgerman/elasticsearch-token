begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.single.shard
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
name|shard
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
name|NoShardAvailableActionException
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
name|TransportAction
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
name|TransportActions
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|TransportActions
operator|.
name|isShardNotAvailableException
import|;
end_import

begin_comment
comment|/**  * A base class for single shard read operations.  */
end_comment

begin_class
DECL|class|TransportShardSingleOperationAction
specifier|public
specifier|abstract
class|class
name|TransportShardSingleOperationAction
parameter_list|<
name|Request
extends|extends
name|SingleShardOperationRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
extends|extends
name|TransportAction
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
DECL|field|transportShardAction
specifier|final
name|String
name|transportShardAction
decl_stmt|;
DECL|field|executor
specifier|final
name|String
name|executor
decl_stmt|;
DECL|method|TransportShardSingleOperationAction
specifier|protected
name|TransportShardSingleOperationAction
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
parameter_list|,
name|String
name|executor
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
name|actionFilters
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
name|transportShardAction
operator|=
name|actionName
operator|+
literal|"[s]"
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
if|if
condition|(
operator|!
name|isSubAction
argument_list|()
condition|)
block|{
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|actionName
argument_list|,
name|request
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|TransportHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|transportShardAction
argument_list|,
name|request
argument_list|,
name|executor
argument_list|,
operator|new
name|ShardTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Tells whether the action is a main one or a subaction. Used to decide whether we need to register      * the main transport handler. In fact if the action is a subaction, its execute method      * will be called locally to its parent action.      */
DECL|method|isSubAction
specifier|protected
name|boolean
name|isSubAction
parameter_list|()
block|{
return|return
literal|false
return|;
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
DECL|method|shardOperation
specifier|protected
specifier|abstract
name|Response
name|shardOperation
parameter_list|(
name|Request
name|request
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
function_decl|;
DECL|method|newResponse
specifier|protected
specifier|abstract
name|Response
name|newResponse
parameter_list|()
function_decl|;
DECL|method|resolveIndex
specifier|protected
specifier|abstract
name|boolean
name|resolveIndex
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
name|READ
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
name|READ
argument_list|,
name|request
operator|.
name|concreteIndex
argument_list|()
argument_list|)
return|;
block|}
DECL|method|resolveRequest
specifier|protected
name|void
name|resolveRequest
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|InternalRequest
name|request
parameter_list|)
block|{      }
DECL|method|shards
specifier|protected
specifier|abstract
name|ShardIterator
name|shards
parameter_list|(
name|ClusterState
name|state
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
DECL|field|shardIt
specifier|private
specifier|final
name|ShardIterator
name|shardIt
decl_stmt|;
DECL|field|internalRequest
specifier|private
specifier|final
name|InternalRequest
name|internalRequest
decl_stmt|;
DECL|field|nodes
specifier|private
specifier|final
name|DiscoveryNodes
name|nodes
decl_stmt|;
DECL|field|lastFailure
specifier|private
specifier|volatile
name|Throwable
name|lastFailure
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
name|listener
operator|=
name|listener
expr_stmt|;
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"executing [{}] based on cluster state version [{}]"
argument_list|,
name|request
argument_list|,
name|clusterState
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|nodes
operator|=
name|clusterState
operator|.
name|nodes
argument_list|()
expr_stmt|;
name|ClusterBlockException
name|blockException
init|=
name|checkGlobalBlock
argument_list|(
name|clusterState
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockException
operator|!=
literal|null
condition|)
block|{
throw|throw
name|blockException
throw|;
block|}
name|String
name|concreteSingleIndex
decl_stmt|;
if|if
condition|(
name|resolveIndex
argument_list|()
condition|)
block|{
name|concreteSingleIndex
operator|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|concreteSingleIndex
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|concreteSingleIndex
operator|=
name|request
operator|.
name|index
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|internalRequest
operator|=
operator|new
name|InternalRequest
argument_list|(
name|request
argument_list|,
name|concreteSingleIndex
argument_list|)
expr_stmt|;
name|resolveRequest
argument_list|(
name|clusterState
argument_list|,
name|internalRequest
argument_list|)
expr_stmt|;
name|blockException
operator|=
name|checkRequestBlock
argument_list|(
name|clusterState
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
throw|throw
name|blockException
throw|;
block|}
name|this
operator|.
name|shardIt
operator|=
name|shards
argument_list|(
name|clusterState
argument_list|,
name|internalRequest
argument_list|)
expr_stmt|;
block|}
DECL|method|start
specifier|public
name|void
name|start
parameter_list|()
block|{
name|perform
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|onFailure
specifier|private
name|void
name|onFailure
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
operator|&&
name|e
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: failed to execute [{}]"
argument_list|,
name|e
argument_list|,
name|shardRouting
argument_list|,
name|internalRequest
operator|.
name|request
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|perform
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
DECL|method|perform
specifier|private
name|void
name|perform
parameter_list|(
annotation|@
name|Nullable
specifier|final
name|Throwable
name|currentFailure
parameter_list|)
block|{
name|Throwable
name|lastFailure
init|=
name|this
operator|.
name|lastFailure
decl_stmt|;
if|if
condition|(
name|lastFailure
operator|==
literal|null
operator|||
name|TransportActions
operator|.
name|isReadOverrideException
argument_list|(
name|currentFailure
argument_list|)
condition|)
block|{
name|lastFailure
operator|=
name|currentFailure
expr_stmt|;
name|this
operator|.
name|lastFailure
operator|=
name|currentFailure
expr_stmt|;
block|}
specifier|final
name|ShardRouting
name|shardRouting
init|=
name|shardIt
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|==
literal|null
condition|)
block|{
name|Throwable
name|failure
init|=
name|lastFailure
decl_stmt|;
if|if
condition|(
name|failure
operator|==
literal|null
operator|||
name|isShardNotAvailableException
argument_list|(
name|failure
argument_list|)
condition|)
block|{
name|failure
operator|=
operator|new
name|NoShardAvailableActionException
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
literal|null
argument_list|,
name|failure
argument_list|)
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
literal|"{}: failed to execute [{}]"
argument_list|,
name|failure
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
name|internalRequest
operator|.
name|request
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|listener
operator|.
name|onFailure
argument_list|(
name|failure
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|shardRouting
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
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"executing [{}] on shard [{}]"
argument_list|,
name|internalRequest
operator|.
name|request
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|operationThreaded
argument_list|()
condition|)
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
name|Response
name|response
init|=
name|shardOperation
argument_list|(
name|internalRequest
operator|.
name|request
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|onFailure
argument_list|(
name|shardRouting
argument_list|,
name|e
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
specifier|final
name|Response
name|response
init|=
name|shardOperation
argument_list|(
name|internalRequest
operator|.
name|request
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|onFailure
argument_list|(
name|shardRouting
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
name|shardRouting
operator|.
name|currentNodeId
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
name|onFailure
argument_list|(
name|shardRouting
argument_list|,
operator|new
name|NoShardAvailableActionException
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|internalRequest
operator|.
name|request
argument_list|()
operator|.
name|internalShardId
operator|=
name|shardRouting
operator|.
name|shardId
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|transportShardAction
argument_list|,
name|internalRequest
operator|.
name|request
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
specifier|final
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
name|onFailure
argument_list|(
name|shardRouting
argument_list|,
name|exp
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|class|TransportHandler
specifier|private
class|class
name|TransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|Request
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
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
name|Throwable
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
literal|"failed to send response for get"
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
block|}
DECL|class|ShardTransportHandler
specifier|private
class|class
name|ShardTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|Request
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
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
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"executing [{}] on shard [{}]"
argument_list|,
name|request
argument_list|,
name|request
operator|.
name|internalShardId
argument_list|)
expr_stmt|;
block|}
name|Response
name|response
init|=
name|shardOperation
argument_list|(
name|request
argument_list|,
name|request
operator|.
name|internalShardId
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
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
specifier|final
name|String
name|concreteIndex
decl_stmt|;
DECL|method|InternalRequest
name|InternalRequest
parameter_list|(
name|Request
name|request
parameter_list|,
name|String
name|concreteIndex
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
name|concreteIndex
operator|=
name|concreteIndex
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

