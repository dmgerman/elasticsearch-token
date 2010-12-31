begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.master
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|master
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
name|discovery
operator|.
name|MasterNotDiscoveredException
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

begin_comment
comment|/**  * A base class for operations that needs to be performed on the master node.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportMasterNodeOperationAction
specifier|public
specifier|abstract
class|class
name|TransportMasterNodeOperationAction
parameter_list|<
name|Request
extends|extends
name|MasterNodeOperationRequest
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
DECL|field|threadPool
specifier|protected
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|method|TransportMasterNodeOperationAction
specifier|protected
name|TransportMasterNodeOperationAction
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
name|ThreadPool
name|threadPool
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
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|transportAction
argument_list|()
argument_list|,
operator|new
name|TransportHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|transportAction
specifier|protected
specifier|abstract
name|String
name|transportAction
parameter_list|()
function_decl|;
DECL|method|newRequest
specifier|protected
specifier|abstract
name|Request
name|newRequest
parameter_list|()
function_decl|;
DECL|method|newResponse
specifier|protected
specifier|abstract
name|Response
name|newResponse
parameter_list|()
function_decl|;
DECL|method|masterOperation
specifier|protected
specifier|abstract
name|Response
name|masterOperation
parameter_list|(
name|Request
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|localExecute
specifier|protected
name|boolean
name|localExecute
parameter_list|(
name|Request
name|request
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|Request
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
DECL|method|processBeforeDelegationToMaster
specifier|protected
name|void
name|processBeforeDelegationToMaster
parameter_list|(
name|Request
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{      }
DECL|method|doExecute
annotation|@
name|Override
specifier|protected
name|void
name|doExecute
parameter_list|(
specifier|final
name|Request
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|innerExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|innerExecute
specifier|private
name|void
name|innerExecute
parameter_list|(
specifier|final
name|Request
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
specifier|final
name|boolean
name|retrying
parameter_list|)
block|{
specifier|final
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
specifier|final
name|DiscoveryNodes
name|nodes
init|=
name|clusterState
operator|.
name|nodes
argument_list|()
decl_stmt|;
if|if
condition|(
name|nodes
operator|.
name|localNodeMaster
argument_list|()
operator|||
name|localExecute
argument_list|(
name|request
argument_list|)
condition|)
block|{
comment|// check for block, if blocked, retry, else, execute locally
specifier|final
name|ClusterBlockException
name|blockException
init|=
name|checkBlock
argument_list|(
name|request
argument_list|,
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
if|if
condition|(
operator|!
name|blockException
operator|.
name|retryable
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|blockException
argument_list|)
expr_stmt|;
return|return;
block|}
name|clusterService
operator|.
name|add
argument_list|(
name|request
operator|.
name|masterNodeTimeout
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
name|postAdded
parameter_list|()
block|{
name|ClusterBlockException
name|blockException
init|=
name|checkBlock
argument_list|(
name|request
argument_list|,
name|clusterState
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockException
operator|==
literal|null
operator|||
operator|!
name|blockException
operator|.
name|retryable
argument_list|()
condition|)
block|{
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|innerExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onClose
parameter_list|()
block|{
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|blockException
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
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|blockException
argument_list|)
expr_stmt|;
block|}
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
name|ClusterBlockException
name|blockException
init|=
name|checkBlock
argument_list|(
name|request
argument_list|,
name|event
operator|.
name|state
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockException
operator|==
literal|null
operator|||
operator|!
name|blockException
operator|.
name|retryable
argument_list|()
condition|)
block|{
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|innerExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
literal|false
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
name|Response
name|response
init|=
name|masterOperation
argument_list|(
name|request
argument_list|,
name|clusterState
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
name|Exception
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
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|nodes
operator|.
name|masterNode
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|retrying
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|MasterNotDiscoveredException
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|clusterService
operator|.
name|add
argument_list|(
name|request
operator|.
name|masterNodeTimeout
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
name|postAdded
parameter_list|()
block|{
name|ClusterState
name|clusterStateV2
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
name|clusterStateV2
operator|.
name|nodes
argument_list|()
operator|.
name|masterNodeId
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// now we have a master, try and execute it...
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|innerExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onClose
parameter_list|()
block|{
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
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
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|MasterNotDiscoveredException
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|event
operator|.
name|nodesDelta
argument_list|()
operator|.
name|masterNodeChanged
argument_list|()
condition|)
block|{
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|innerExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|processBeforeDelegationToMaster
argument_list|(
name|request
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|nodes
operator|.
name|masterNode
argument_list|()
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
name|newResponse
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
specifier|final
name|TransportException
name|exp
parameter_list|)
block|{
if|if
condition|(
name|exp
operator|.
name|unwrapCause
argument_list|()
operator|instanceof
name|ConnectTransportException
condition|)
block|{
comment|// we want to retry here a bit to see if a new master is elected
name|clusterService
operator|.
name|add
argument_list|(
name|request
operator|.
name|masterNodeTimeout
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
name|postAdded
parameter_list|()
block|{
name|ClusterState
name|clusterStateV2
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|masterNodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|clusterStateV2
operator|.
name|nodes
argument_list|()
operator|.
name|masterNodeId
argument_list|()
argument_list|)
condition|)
block|{
comment|// master changes while adding the listener, try here
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|innerExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onClose
parameter_list|()
block|{
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
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
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|MasterNotDiscoveredException
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|event
operator|.
name|nodesDelta
argument_list|()
operator|.
name|masterNodeChanged
argument_list|()
condition|)
block|{
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|innerExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
literal|false
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
block|}
DECL|class|TransportHandler
specifier|private
class|class
name|TransportHandler
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
name|newRequest
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
specifier|final
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
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeMaster
argument_list|()
operator|||
name|localExecute
argument_list|(
name|request
argument_list|)
condition|)
block|{
name|checkBlock
argument_list|(
name|request
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
name|Response
name|response
init|=
name|masterOperation
argument_list|(
name|request
argument_list|,
name|clusterState
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
else|else
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
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
name|newResponse
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
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed to send response"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
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
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|exp
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed to send response"
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
block|}
block|}
block|}
end_class

end_unit

