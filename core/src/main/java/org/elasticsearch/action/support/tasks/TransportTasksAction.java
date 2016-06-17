begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.tasks
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|tasks
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ResourceNotFoundException
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
name|NoSuchNodeException
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
name|TaskOperationFailure
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
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|service
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
name|common
operator|.
name|collect
operator|.
name|ImmutableOpenMap
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
name|Writeable
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
name|tasks
operator|.
name|Task
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
name|BaseTransportResponseHandler
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
name|NodeShouldNotConnectException
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
name|TransportException
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
name|TransportRequestOptions
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
name|ArrayList
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
name|atomic
operator|.
name|AtomicInteger
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_comment
comment|/**  * The base class for transport actions that are interacting with currently running tasks.  */
end_comment

begin_class
DECL|class|TransportTasksAction
specifier|public
specifier|abstract
class|class
name|TransportTasksAction
parameter_list|<
name|OperationTask
extends|extends
name|Task
parameter_list|,
name|TasksRequest
extends|extends
name|BaseTasksRequest
parameter_list|<
name|TasksRequest
parameter_list|>
parameter_list|,
name|TasksResponse
extends|extends
name|BaseTasksResponse
parameter_list|,
name|TaskResponse
extends|extends
name|Writeable
parameter_list|>
extends|extends
name|HandledTransportAction
argument_list|<
name|TasksRequest
argument_list|,
name|TasksResponse
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
DECL|field|requestSupplier
specifier|protected
specifier|final
name|Supplier
argument_list|<
name|TasksRequest
argument_list|>
name|requestSupplier
decl_stmt|;
DECL|field|responseSupplier
specifier|protected
specifier|final
name|Supplier
argument_list|<
name|TasksResponse
argument_list|>
name|responseSupplier
decl_stmt|;
DECL|field|transportNodeAction
specifier|protected
specifier|final
name|String
name|transportNodeAction
decl_stmt|;
DECL|method|TransportTasksAction
specifier|protected
name|TransportTasksAction
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
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|Supplier
argument_list|<
name|TasksRequest
argument_list|>
name|requestSupplier
parameter_list|,
name|Supplier
argument_list|<
name|TasksResponse
argument_list|>
name|responseSupplier
parameter_list|,
name|String
name|nodeExecutor
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
name|indexNameExpressionResolver
argument_list|,
name|requestSupplier
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
name|transportNodeAction
operator|=
name|actionName
operator|+
literal|"[n]"
expr_stmt|;
name|this
operator|.
name|requestSupplier
operator|=
name|requestSupplier
expr_stmt|;
name|this
operator|.
name|responseSupplier
operator|=
name|responseSupplier
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|transportNodeAction
argument_list|,
name|NodeTaskRequest
operator|::
operator|new
argument_list|,
name|nodeExecutor
argument_list|,
operator|new
name|NodeTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
specifier|final
name|void
name|doExecute
parameter_list|(
name|TasksRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|TasksResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"attempt to execute a transport tasks operation without a task"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"task parameter is required for this operation"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|Task
name|task
parameter_list|,
name|TasksRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|TasksResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|new
name|AsyncAction
argument_list|(
name|task
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|method|nodeOperation
specifier|private
name|NodeTasksResponse
name|nodeOperation
parameter_list|(
name|NodeTaskRequest
name|nodeTaskRequest
parameter_list|)
block|{
name|TasksRequest
name|request
init|=
name|nodeTaskRequest
operator|.
name|tasksRequest
decl_stmt|;
name|List
argument_list|<
name|TaskResponse
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TaskOperationFailure
argument_list|>
name|exceptions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|processTasks
argument_list|(
name|request
argument_list|,
name|task
lambda|->
block|{
try|try
block|{
name|TaskResponse
name|response
init|=
name|taskOperation
argument_list|(
name|request
argument_list|,
name|task
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|!=
literal|null
condition|)
block|{
name|results
operator|.
name|add
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|exceptions
operator|.
name|add
argument_list|(
operator|new
name|TaskOperationFailure
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|task
operator|.
name|getId
argument_list|()
argument_list|,
name|ex
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
operator|new
name|NodeTasksResponse
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|results
argument_list|,
name|exceptions
argument_list|)
return|;
block|}
DECL|method|filterNodeIds
specifier|protected
name|String
index|[]
name|filterNodeIds
parameter_list|(
name|DiscoveryNodes
name|nodes
parameter_list|,
name|String
index|[]
name|nodesIds
parameter_list|)
block|{
return|return
name|nodesIds
return|;
block|}
DECL|method|resolveNodes
specifier|protected
name|String
index|[]
name|resolveNodes
parameter_list|(
name|TasksRequest
name|request
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|getTaskId
argument_list|()
operator|.
name|isSet
argument_list|()
condition|)
block|{
return|return
operator|new
name|String
index|[]
block|{
name|request
operator|.
name|getTaskId
argument_list|()
operator|.
name|getNodeId
argument_list|()
block|}
return|;
block|}
else|else
block|{
return|return
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|resolveNodesIds
argument_list|(
name|request
operator|.
name|getNodesIds
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|processTasks
specifier|protected
name|void
name|processTasks
parameter_list|(
name|TasksRequest
name|request
parameter_list|,
name|Consumer
argument_list|<
name|OperationTask
argument_list|>
name|operation
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|getTaskId
argument_list|()
operator|.
name|isSet
argument_list|()
condition|)
block|{
comment|// we are only checking one task, we can optimize it
name|Task
name|task
init|=
name|taskManager
operator|.
name|getTask
argument_list|(
name|request
operator|.
name|getTaskId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|task
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|match
argument_list|(
name|task
argument_list|)
condition|)
block|{
name|operation
operator|.
name|accept
argument_list|(
operator|(
name|OperationTask
operator|)
name|task
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ResourceNotFoundException
argument_list|(
literal|"task [{}] doesn't support this operation"
argument_list|,
name|request
operator|.
name|getTaskId
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ResourceNotFoundException
argument_list|(
literal|"task [{}] is missing"
argument_list|,
name|request
operator|.
name|getTaskId
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
for|for
control|(
name|Task
name|task
range|:
name|taskManager
operator|.
name|getTasks
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|request
operator|.
name|match
argument_list|(
name|task
argument_list|)
condition|)
block|{
name|operation
operator|.
name|accept
argument_list|(
operator|(
name|OperationTask
operator|)
name|task
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|newResponse
specifier|protected
specifier|abstract
name|TasksResponse
name|newResponse
parameter_list|(
name|TasksRequest
name|request
parameter_list|,
name|List
argument_list|<
name|TaskResponse
argument_list|>
name|tasks
parameter_list|,
name|List
argument_list|<
name|TaskOperationFailure
argument_list|>
name|taskOperationFailures
parameter_list|,
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failedNodeExceptions
parameter_list|)
function_decl|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|newResponse
specifier|protected
name|TasksResponse
name|newResponse
parameter_list|(
name|TasksRequest
name|request
parameter_list|,
name|AtomicReferenceArray
name|responses
parameter_list|)
block|{
name|List
argument_list|<
name|TaskResponse
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failedNodeExceptions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TaskOperationFailure
argument_list|>
name|taskOperationFailures
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|response
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
name|response
operator|instanceof
name|FailedNodeException
condition|)
block|{
name|failedNodeExceptions
operator|.
name|add
argument_list|(
operator|(
name|FailedNodeException
operator|)
name|response
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|NodeTasksResponse
name|tasksResponse
init|=
operator|(
name|NodeTasksResponse
operator|)
name|response
decl_stmt|;
if|if
condition|(
name|tasksResponse
operator|.
name|results
operator|!=
literal|null
condition|)
block|{
name|tasks
operator|.
name|addAll
argument_list|(
name|tasksResponse
operator|.
name|results
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tasksResponse
operator|.
name|exceptions
operator|!=
literal|null
condition|)
block|{
name|taskOperationFailures
operator|.
name|addAll
argument_list|(
name|tasksResponse
operator|.
name|exceptions
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|newResponse
argument_list|(
name|request
argument_list|,
name|tasks
argument_list|,
name|taskOperationFailures
argument_list|,
name|failedNodeExceptions
argument_list|)
return|;
block|}
DECL|method|readTaskResponse
specifier|protected
specifier|abstract
name|TaskResponse
name|readTaskResponse
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|taskOperation
specifier|protected
specifier|abstract
name|TaskResponse
name|taskOperation
parameter_list|(
name|TasksRequest
name|request
parameter_list|,
name|OperationTask
name|task
parameter_list|)
function_decl|;
DECL|method|transportCompress
specifier|protected
name|boolean
name|transportCompress
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|accumulateExceptions
specifier|protected
specifier|abstract
name|boolean
name|accumulateExceptions
parameter_list|()
function_decl|;
DECL|class|AsyncAction
specifier|private
class|class
name|AsyncAction
block|{
DECL|field|request
specifier|private
specifier|final
name|TasksRequest
name|request
decl_stmt|;
DECL|field|nodesIds
specifier|private
specifier|final
name|String
index|[]
name|nodesIds
decl_stmt|;
DECL|field|nodes
specifier|private
specifier|final
name|DiscoveryNode
index|[]
name|nodes
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|TasksResponse
argument_list|>
name|listener
decl_stmt|;
DECL|field|responses
specifier|private
specifier|final
name|AtomicReferenceArray
argument_list|<
name|Object
argument_list|>
name|responses
decl_stmt|;
DECL|field|counter
specifier|private
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|task
specifier|private
specifier|final
name|Task
name|task
decl_stmt|;
DECL|method|AsyncAction
specifier|private
name|AsyncAction
parameter_list|(
name|Task
name|task
parameter_list|,
name|TasksRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|TasksResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|this
operator|.
name|task
operator|=
name|task
expr_stmt|;
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
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|String
index|[]
name|nodesIds
init|=
name|resolveNodes
argument_list|(
name|request
argument_list|,
name|clusterState
argument_list|)
decl_stmt|;
name|this
operator|.
name|nodesIds
operator|=
name|filterNodeIds
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|,
name|nodesIds
argument_list|)
expr_stmt|;
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|getNodes
argument_list|()
decl_stmt|;
name|this
operator|.
name|nodes
operator|=
operator|new
name|DiscoveryNode
index|[
name|nodesIds
operator|.
name|length
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
name|this
operator|.
name|nodesIds
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|nodes
index|[
name|i
index|]
operator|=
name|nodes
operator|.
name|get
argument_list|(
name|this
operator|.
name|nodesIds
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|responses
operator|=
operator|new
name|AtomicReferenceArray
argument_list|<>
argument_list|(
name|this
operator|.
name|nodesIds
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|start
specifier|private
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
name|nodesIds
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// nothing to do
try|try
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|newResponse
argument_list|(
name|request
argument_list|,
name|responses
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to generate empty response"
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|TransportRequestOptions
operator|.
name|Builder
name|builder
init|=
name|TransportRequestOptions
operator|.
name|builder
argument_list|()
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|getTimeout
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|withTimeout
argument_list|(
name|request
operator|.
name|getTimeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|withCompress
argument_list|(
name|transportCompress
argument_list|()
argument_list|)
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
name|nodesIds
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|String
name|nodeId
init|=
name|nodesIds
index|[
name|i
index|]
decl_stmt|;
specifier|final
name|int
name|idx
init|=
name|i
decl_stmt|;
specifier|final
name|DiscoveryNode
name|node
init|=
name|nodes
index|[
name|i
index|]
decl_stmt|;
try|try
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
name|onFailure
argument_list|(
name|idx
argument_list|,
name|nodeId
argument_list|,
operator|new
name|NoSuchNodeException
argument_list|(
name|nodeId
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|NodeTaskRequest
name|nodeRequest
init|=
operator|new
name|NodeTaskRequest
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|nodeRequest
operator|.
name|setParentTask
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|task
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|taskManager
operator|.
name|registerChildTask
argument_list|(
name|task
argument_list|,
name|node
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|transportNodeAction
argument_list|,
name|nodeRequest
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|NodeTasksResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|NodeTasksResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|NodeTasksResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|NodeTasksResponse
name|response
parameter_list|)
block|{
name|onOperation
argument_list|(
name|idx
argument_list|,
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
name|idx
argument_list|,
name|node
operator|.
name|getId
argument_list|()
argument_list|,
name|exp
argument_list|)
expr_stmt|;
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
block|}
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|onFailure
argument_list|(
name|idx
argument_list|,
name|nodeId
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|onOperation
specifier|private
name|void
name|onOperation
parameter_list|(
name|int
name|idx
parameter_list|,
name|NodeTasksResponse
name|nodeResponse
parameter_list|)
block|{
name|responses
operator|.
name|set
argument_list|(
name|idx
argument_list|,
name|nodeResponse
argument_list|)
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|incrementAndGet
argument_list|()
operator|==
name|responses
operator|.
name|length
argument_list|()
condition|)
block|{
name|finishHim
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|onFailure
specifier|private
name|void
name|onFailure
parameter_list|(
name|int
name|idx
parameter_list|,
name|String
name|nodeId
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
operator|&&
operator|!
operator|(
name|t
operator|instanceof
name|NodeShouldNotConnectException
operator|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to execute on node [{}]"
argument_list|,
name|t
argument_list|,
name|nodeId
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|accumulateExceptions
argument_list|()
condition|)
block|{
name|responses
operator|.
name|set
argument_list|(
name|idx
argument_list|,
operator|new
name|FailedNodeException
argument_list|(
name|nodeId
argument_list|,
literal|"Failed node ["
operator|+
name|nodeId
operator|+
literal|"]"
argument_list|,
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|counter
operator|.
name|incrementAndGet
argument_list|()
operator|==
name|responses
operator|.
name|length
argument_list|()
condition|)
block|{
name|finishHim
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|finishHim
specifier|private
name|void
name|finishHim
parameter_list|()
block|{
name|TasksResponse
name|finalResponse
decl_stmt|;
try|try
block|{
name|finalResponse
operator|=
name|newResponse
argument_list|(
name|request
argument_list|,
name|responses
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to combine responses from nodes"
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return;
block|}
name|listener
operator|.
name|onResponse
argument_list|(
name|finalResponse
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NodeTransportHandler
class|class
name|NodeTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|NodeTaskRequest
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
name|NodeTaskRequest
name|request
parameter_list|,
specifier|final
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|nodeOperation
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NodeTaskRequest
specifier|private
class|class
name|NodeTaskRequest
extends|extends
name|TransportRequest
block|{
DECL|field|tasksRequest
specifier|private
name|TasksRequest
name|tasksRequest
decl_stmt|;
DECL|method|NodeTaskRequest
specifier|protected
name|NodeTaskRequest
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
DECL|method|NodeTaskRequest
specifier|protected
name|NodeTaskRequest
parameter_list|(
name|TasksRequest
name|tasksRequest
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|tasksRequest
operator|=
name|tasksRequest
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
name|tasksRequest
operator|=
name|requestSupplier
operator|.
name|get
argument_list|()
expr_stmt|;
name|tasksRequest
operator|.
name|readFrom
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|tasksRequest
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NodeTasksResponse
specifier|private
class|class
name|NodeTasksResponse
extends|extends
name|TransportResponse
block|{
DECL|field|nodeId
specifier|protected
name|String
name|nodeId
decl_stmt|;
DECL|field|exceptions
specifier|protected
name|List
argument_list|<
name|TaskOperationFailure
argument_list|>
name|exceptions
decl_stmt|;
DECL|field|results
specifier|protected
name|List
argument_list|<
name|TaskResponse
argument_list|>
name|results
decl_stmt|;
DECL|method|NodeTasksResponse
specifier|public
name|NodeTasksResponse
parameter_list|()
block|{         }
DECL|method|NodeTasksResponse
specifier|public
name|NodeTasksResponse
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|List
argument_list|<
name|TaskResponse
argument_list|>
name|results
parameter_list|,
name|List
argument_list|<
name|TaskOperationFailure
argument_list|>
name|exceptions
parameter_list|)
block|{
name|this
operator|.
name|nodeId
operator|=
name|nodeId
expr_stmt|;
name|this
operator|.
name|results
operator|=
name|results
expr_stmt|;
name|this
operator|.
name|exceptions
operator|=
name|exceptions
expr_stmt|;
block|}
DECL|method|getNodeId
specifier|public
name|String
name|getNodeId
parameter_list|()
block|{
return|return
name|nodeId
return|;
block|}
DECL|method|getExceptions
specifier|public
name|List
argument_list|<
name|TaskOperationFailure
argument_list|>
name|getExceptions
parameter_list|()
block|{
return|return
name|exceptions
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
name|nodeId
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|int
name|resultsSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|results
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|resultsSize
argument_list|)
expr_stmt|;
for|for
control|(
init|;
name|resultsSize
operator|>
literal|0
condition|;
name|resultsSize
operator|--
control|)
block|{
specifier|final
name|TaskResponse
name|result
init|=
name|in
operator|.
name|readBoolean
argument_list|()
condition|?
name|readTaskResponse
argument_list|(
name|in
argument_list|)
else|:
literal|null
decl_stmt|;
name|results
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|int
name|taskFailures
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|exceptions
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|taskFailures
argument_list|)
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
name|taskFailures
condition|;
name|i
operator|++
control|)
block|{
name|exceptions
operator|.
name|add
argument_list|(
operator|new
name|TaskOperationFailure
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|exceptions
operator|=
literal|null
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
name|writeString
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|TaskResponse
name|result
range|:
name|results
control|)
block|{
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|result
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|exceptions
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|exceptions
operator|!=
literal|null
condition|)
block|{
name|int
name|taskFailures
init|=
name|exceptions
operator|.
name|size
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|taskFailures
argument_list|)
expr_stmt|;
for|for
control|(
name|TaskOperationFailure
name|exception
range|:
name|exceptions
control|)
block|{
name|exception
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
block|}
end_class

end_unit

