begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.node.tasks.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|tasks
operator|.
name|get
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
name|ExceptionsHelper
import|;
end_import

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
name|get
operator|.
name|GetRequest
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
name|get
operator|.
name|GetResponse
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
name|client
operator|.
name|Client
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
name|ParseFieldMatcher
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
name|common
operator|.
name|xcontent
operator|.
name|NamedXContentRegistry
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
name|xcontent
operator|.
name|XContentHelper
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
name|xcontent
operator|.
name|XContentParser
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
name|IndexNotFoundException
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
name|TaskResult
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
name|tasks
operator|.
name|TaskId
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
name|TaskInfo
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
name|TaskResultsService
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
name|TransportResponseHandler
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|tasks
operator|.
name|list
operator|.
name|TransportListTasksAction
operator|.
name|waitForCompletionTimeout
import|;
end_import

begin_comment
comment|/**  * Action to get a single task. If the task isn't running then it'll try to request the status from request index.  *  * The general flow is:  *<ul>  *<li>If this isn't being executed on the node to which the requested TaskId belongs then move to that node.  *<li>Look up the task and return it if it exists  *<li>If it doesn't then look up the task from the results index  *</ul>  */
end_comment

begin_class
DECL|class|TransportGetTaskAction
specifier|public
class|class
name|TransportGetTaskAction
extends|extends
name|HandledTransportAction
argument_list|<
name|GetTaskRequest
argument_list|,
name|GetTaskResponse
argument_list|>
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|xContentRegistry
specifier|private
specifier|final
name|NamedXContentRegistry
name|xContentRegistry
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportGetTaskAction
specifier|public
name|TransportGetTaskAction
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
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|Client
name|client
parameter_list|,
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|GetTaskAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|GetTaskRequest
operator|::
operator|new
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
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|xContentRegistry
operator|=
name|xContentRegistry
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|GetTaskRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|GetTaskResponse
argument_list|>
name|listener
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Task is required"
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
name|thisTask
parameter_list|,
name|GetTaskRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|GetTaskResponse
argument_list|>
name|listener
parameter_list|)
block|{
if|if
condition|(
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
operator|.
name|equals
argument_list|(
name|request
operator|.
name|getTaskId
argument_list|()
operator|.
name|getNodeId
argument_list|()
argument_list|)
condition|)
block|{
name|getRunningTaskFromNode
argument_list|(
name|thisTask
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|runOnNodeWithTaskIfPossible
argument_list|(
name|thisTask
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Executed on the coordinating node to forward execution of the remaining work to the node that matches that requested      * {@link TaskId#getNodeId()}. If the node isn't in the cluster then this will just proceed to      * {@link #getFinishedTaskFromIndex(Task, GetTaskRequest, ActionListener)} on this node.      */
DECL|method|runOnNodeWithTaskIfPossible
specifier|private
name|void
name|runOnNodeWithTaskIfPossible
parameter_list|(
name|Task
name|thisTask
parameter_list|,
name|GetTaskRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|GetTaskResponse
argument_list|>
name|listener
parameter_list|)
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
literal|false
argument_list|)
expr_stmt|;
name|DiscoveryNode
name|node
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|request
operator|.
name|getTaskId
argument_list|()
operator|.
name|getNodeId
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
comment|// Node is no longer part of the cluster! Try and look the task up from the results index.
name|getFinishedTaskFromIndex
argument_list|(
name|thisTask
argument_list|,
name|request
argument_list|,
name|ActionListener
operator|.
name|wrap
argument_list|(
name|listener
operator|::
name|onResponse
argument_list|,
name|e
lambda|->
block|{
if|if
condition|(
name|e
operator|instanceof
name|ResourceNotFoundException
condition|)
block|{
name|e
operator|=
operator|new
name|ResourceNotFoundException
argument_list|(
literal|"task ["
operator|+
name|request
operator|.
name|getTaskId
argument_list|()
operator|+
literal|"] belongs to the node ["
operator|+
name|request
operator|.
name|getTaskId
argument_list|()
operator|.
name|getNodeId
argument_list|()
operator|+
literal|"] which isn't part of the cluster and there is no record of the task"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|GetTaskRequest
name|nodeRequest
init|=
name|request
operator|.
name|nodeRequest
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|thisTask
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
name|taskManager
operator|.
name|registerChildTask
argument_list|(
name|thisTask
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
name|GetTaskAction
operator|.
name|NAME
argument_list|,
name|nodeRequest
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|TransportResponseHandler
argument_list|<
name|GetTaskResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|GetTaskResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|GetTaskResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|GetTaskResponse
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
comment|/**      * Executed on the node that should be running the task to find and return the running task. Falls back to      * {@link #getFinishedTaskFromIndex(Task, GetTaskRequest, ActionListener)} if the task isn't still running.      */
DECL|method|getRunningTaskFromNode
name|void
name|getRunningTaskFromNode
parameter_list|(
name|Task
name|thisTask
parameter_list|,
name|GetTaskRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|GetTaskResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|Task
name|runningTask
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
name|runningTask
operator|==
literal|null
condition|)
block|{
comment|// Task isn't running, go look in the task index
name|getFinishedTaskFromIndex
argument_list|(
name|thisTask
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|request
operator|.
name|getWaitForCompletion
argument_list|()
condition|)
block|{
comment|// Shift to the generic thread pool and let it wait for the task to complete so we don't block any important threads.
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
specifier|protected
name|void
name|doRun
parameter_list|()
throws|throws
name|Exception
block|{
name|taskManager
operator|.
name|waitForTaskCompletion
argument_list|(
name|runningTask
argument_list|,
name|waitForCompletionTimeout
argument_list|(
name|request
operator|.
name|getTimeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|waitedForCompletion
argument_list|(
name|thisTask
argument_list|,
name|request
argument_list|,
name|runningTask
operator|.
name|taskInfo
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
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
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|TaskInfo
name|info
init|=
name|runningTask
operator|.
name|taskInfo
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|GetTaskResponse
argument_list|(
operator|new
name|TaskResult
argument_list|(
literal|false
argument_list|,
name|info
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Called after waiting for the task to complete. Attempts to load the results of the task from the tasks index. If it isn't in the      * index then returns a snapshot of the task taken shortly after completion.      */
DECL|method|waitedForCompletion
name|void
name|waitedForCompletion
parameter_list|(
name|Task
name|thisTask
parameter_list|,
name|GetTaskRequest
name|request
parameter_list|,
name|TaskInfo
name|snapshotOfRunningTask
parameter_list|,
name|ActionListener
argument_list|<
name|GetTaskResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|getFinishedTaskFromIndex
argument_list|(
name|thisTask
argument_list|,
name|request
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|GetTaskResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|GetTaskResponse
name|response
parameter_list|)
block|{
comment|// We were able to load the task from the task index. Let's send that back.
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
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|/*                  * We couldn't load the task from the task index. Instead of 404 we should use the snapshot we took after it finished. If                  * the error isn't a 404 then we'll just throw it back to the user.                  */
if|if
condition|(
name|ExceptionsHelper
operator|.
name|unwrap
argument_list|(
name|e
argument_list|,
name|ResourceNotFoundException
operator|.
name|class
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|GetTaskResponse
argument_list|(
operator|new
name|TaskResult
argument_list|(
literal|true
argument_list|,
name|snapshotOfRunningTask
argument_list|)
argument_list|)
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
argument_list|)
expr_stmt|;
block|}
comment|/**      * Send a {@link GetRequest} to the tasks index looking for a persisted copy of the task completed task. It'll only be found only if the      * task's result was stored. Called on the node that once had the task if that node is still part of the cluster or on the      * coordinating node if the node is no longer part of the cluster.      */
DECL|method|getFinishedTaskFromIndex
name|void
name|getFinishedTaskFromIndex
parameter_list|(
name|Task
name|thisTask
parameter_list|,
name|GetTaskRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|GetTaskResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|GetRequest
name|get
init|=
operator|new
name|GetRequest
argument_list|(
name|TaskResultsService
operator|.
name|TASK_INDEX
argument_list|,
name|TaskResultsService
operator|.
name|TASK_TYPE
argument_list|,
name|request
operator|.
name|getTaskId
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|get
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
name|thisTask
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|get
argument_list|(
name|get
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|GetResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|GetResponse
name|getResponse
parameter_list|)
block|{
try|try
block|{
name|onGetFinishedTaskFromIndex
argument_list|(
name|getResponse
argument_list|,
name|listener
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
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|ExceptionsHelper
operator|.
name|unwrap
argument_list|(
name|e
argument_list|,
name|IndexNotFoundException
operator|.
name|class
argument_list|)
operator|!=
literal|null
condition|)
block|{
comment|// We haven't yet created the index for the task results so it can't be found.
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ResourceNotFoundException
argument_list|(
literal|"task [{}] isn't running and hasn't stored its results"
argument_list|,
name|e
argument_list|,
name|request
operator|.
name|getTaskId
argument_list|()
argument_list|)
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
argument_list|)
expr_stmt|;
block|}
comment|/**      * Called with the {@linkplain GetResponse} from loading the task from the results index. Called on the node that once had the task if      * that node is part of the cluster or on the coordinating node if the node wasn't part of the cluster.      */
DECL|method|onGetFinishedTaskFromIndex
name|void
name|onGetFinishedTaskFromIndex
parameter_list|(
name|GetResponse
name|response
parameter_list|,
name|ActionListener
argument_list|<
name|GetTaskResponse
argument_list|>
name|listener
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
literal|false
operator|==
name|response
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ResourceNotFoundException
argument_list|(
literal|"task [{}] isn't running and hasn't stored its results"
argument_list|,
name|response
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|response
operator|.
name|isSourceEmpty
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ElasticsearchException
argument_list|(
literal|"Stored task status for [{}] didn't contain any source!"
argument_list|,
name|response
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|xContentRegistry
argument_list|,
name|response
operator|.
name|getSourceAsBytesRef
argument_list|()
argument_list|)
init|)
block|{
name|TaskResult
name|result
init|=
name|TaskResult
operator|.
name|PARSER
operator|.
name|apply
argument_list|(
name|parser
argument_list|,
parameter_list|()
lambda|->
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|GetTaskResponse
argument_list|(
name|result
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

