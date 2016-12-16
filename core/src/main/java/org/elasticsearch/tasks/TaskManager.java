begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.tasks
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|ElasticsearchTimeoutException
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
name|ClusterStateApplier
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
name|ConcurrentCollections
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
name|ConcurrentMapLong
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Map
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
name|ConcurrentHashMap
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
name|AtomicLong
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueMillis
import|;
end_import

begin_comment
comment|/**  * Task Manager service for keeping track of currently running tasks on the nodes  */
end_comment

begin_class
DECL|class|TaskManager
specifier|public
class|class
name|TaskManager
extends|extends
name|AbstractComponent
implements|implements
name|ClusterStateApplier
block|{
DECL|field|WAIT_FOR_COMPLETION_POLL
specifier|private
specifier|static
specifier|final
name|TimeValue
name|WAIT_FOR_COMPLETION_POLL
init|=
name|timeValueMillis
argument_list|(
literal|100
argument_list|)
decl_stmt|;
DECL|field|tasks
specifier|private
specifier|final
name|ConcurrentMapLong
argument_list|<
name|Task
argument_list|>
name|tasks
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMapLongWithAggressiveConcurrency
argument_list|()
decl_stmt|;
DECL|field|cancellableTasks
specifier|private
specifier|final
name|ConcurrentMapLong
argument_list|<
name|CancellableTaskHolder
argument_list|>
name|cancellableTasks
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMapLongWithAggressiveConcurrency
argument_list|()
decl_stmt|;
DECL|field|taskIdGenerator
specifier|private
specifier|final
name|AtomicLong
name|taskIdGenerator
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|banedParents
specifier|private
specifier|final
name|Map
argument_list|<
name|TaskId
argument_list|,
name|String
argument_list|>
name|banedParents
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|taskResultsService
specifier|private
name|TaskResultsService
name|taskResultsService
decl_stmt|;
DECL|field|lastDiscoveryNodes
specifier|private
name|DiscoveryNodes
name|lastDiscoveryNodes
init|=
name|DiscoveryNodes
operator|.
name|EMPTY_NODES
decl_stmt|;
DECL|method|TaskManager
specifier|public
name|TaskManager
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
DECL|method|setTaskResultsService
specifier|public
name|void
name|setTaskResultsService
parameter_list|(
name|TaskResultsService
name|taskResultsService
parameter_list|)
block|{
assert|assert
name|this
operator|.
name|taskResultsService
operator|==
literal|null
assert|;
name|this
operator|.
name|taskResultsService
operator|=
name|taskResultsService
expr_stmt|;
block|}
comment|/**      * Registers a task without parent task      *<p>      * Returns the task manager tracked task or null if the task doesn't support the task manager      */
DECL|method|register
specifier|public
name|Task
name|register
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|action
parameter_list|,
name|TransportRequest
name|request
parameter_list|)
block|{
name|Task
name|task
init|=
name|request
operator|.
name|createTask
argument_list|(
name|taskIdGenerator
operator|.
name|incrementAndGet
argument_list|()
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|request
operator|.
name|getParentTask
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|task
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
assert|assert
name|task
operator|.
name|getParentTaskId
argument_list|()
operator|.
name|equals
argument_list|(
name|request
operator|.
name|getParentTask
argument_list|()
argument_list|)
operator|:
literal|"Request [ "
operator|+
name|request
operator|+
literal|"] didn't preserve it parentTaskId"
assert|;
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
literal|"register {} [{}] [{}] [{}]"
argument_list|,
name|task
operator|.
name|getId
argument_list|()
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|task
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|task
operator|instanceof
name|CancellableTask
condition|)
block|{
name|registerCancellableTask
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Task
name|previousTask
init|=
name|tasks
operator|.
name|put
argument_list|(
name|task
operator|.
name|getId
argument_list|()
argument_list|,
name|task
argument_list|)
decl_stmt|;
assert|assert
name|previousTask
operator|==
literal|null
assert|;
block|}
return|return
name|task
return|;
block|}
DECL|method|registerCancellableTask
specifier|private
name|void
name|registerCancellableTask
parameter_list|(
name|Task
name|task
parameter_list|)
block|{
name|CancellableTask
name|cancellableTask
init|=
operator|(
name|CancellableTask
operator|)
name|task
decl_stmt|;
name|CancellableTaskHolder
name|holder
init|=
operator|new
name|CancellableTaskHolder
argument_list|(
name|cancellableTask
argument_list|)
decl_stmt|;
name|CancellableTaskHolder
name|oldHolder
init|=
name|cancellableTasks
operator|.
name|put
argument_list|(
name|task
operator|.
name|getId
argument_list|()
argument_list|,
name|holder
argument_list|)
decl_stmt|;
assert|assert
name|oldHolder
operator|==
literal|null
assert|;
comment|// Check if this task was banned before we start it
if|if
condition|(
name|task
operator|.
name|getParentTaskId
argument_list|()
operator|.
name|isSet
argument_list|()
operator|&&
name|banedParents
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|String
name|reason
init|=
name|banedParents
operator|.
name|get
argument_list|(
name|task
operator|.
name|getParentTaskId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|reason
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|holder
operator|.
name|cancel
argument_list|(
name|reason
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Task cancelled before it started: "
operator|+
name|reason
argument_list|)
throw|;
block|}
finally|finally
block|{
comment|// let's clean up the registration
name|unregister
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * Cancels a task      *<p>      * Returns a set of nodes with child tasks where this task should be cancelled if cancellation was successful, null otherwise.      */
DECL|method|cancel
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|cancel
parameter_list|(
name|CancellableTask
name|task
parameter_list|,
name|String
name|reason
parameter_list|,
name|Consumer
argument_list|<
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|listener
parameter_list|)
block|{
name|CancellableTaskHolder
name|holder
init|=
name|cancellableTasks
operator|.
name|get
argument_list|(
name|task
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|holder
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"cancelling task with id {}"
argument_list|,
name|task
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|holder
operator|.
name|cancel
argument_list|(
name|reason
argument_list|,
name|listener
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**      * Unregister the task      */
DECL|method|unregister
specifier|public
name|Task
name|unregister
parameter_list|(
name|Task
name|task
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"unregister task for id: {}"
argument_list|,
name|task
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|task
operator|instanceof
name|CancellableTask
condition|)
block|{
name|CancellableTaskHolder
name|holder
init|=
name|cancellableTasks
operator|.
name|remove
argument_list|(
name|task
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|holder
operator|!=
literal|null
condition|)
block|{
name|holder
operator|.
name|finish
argument_list|()
expr_stmt|;
return|return
name|holder
operator|.
name|getTask
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
return|return
name|tasks
operator|.
name|remove
argument_list|(
name|task
operator|.
name|getId
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**      * Stores the task failure      */
DECL|method|storeResult
specifier|public
parameter_list|<
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|storeResult
parameter_list|(
name|Task
name|task
parameter_list|,
name|Exception
name|error
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|DiscoveryNode
name|localNode
init|=
name|lastDiscoveryNodes
operator|.
name|getLocalNode
argument_list|()
decl_stmt|;
if|if
condition|(
name|localNode
operator|==
literal|null
condition|)
block|{
comment|// too early to store anything, shouldn't really be here - just pass the error along
name|listener
operator|.
name|onFailure
argument_list|(
name|error
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|TaskResult
name|taskResult
decl_stmt|;
try|try
block|{
name|taskResult
operator|=
name|task
operator|.
name|result
argument_list|(
name|localNode
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"couldn't store error {}"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|error
argument_list|)
argument_list|)
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|ex
argument_list|)
expr_stmt|;
return|return;
block|}
name|taskResultsService
operator|.
name|storeResult
argument_list|(
name|taskResult
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|Void
name|aVoid
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|error
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
name|logger
operator|.
name|warn
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"couldn't store error {}"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|error
argument_list|)
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
comment|/**      * Stores the task result      */
DECL|method|storeResult
specifier|public
parameter_list|<
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|storeResult
parameter_list|(
name|Task
name|task
parameter_list|,
name|Response
name|response
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|DiscoveryNode
name|localNode
init|=
name|lastDiscoveryNodes
operator|.
name|getLocalNode
argument_list|()
decl_stmt|;
if|if
condition|(
name|localNode
operator|==
literal|null
condition|)
block|{
comment|// too early to store anything, shouldn't really be here - just pass the response along
name|logger
operator|.
name|warn
argument_list|(
literal|"couldn't store response {}, the node didn't join the cluster yet"
argument_list|,
name|response
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|TaskResult
name|taskResult
decl_stmt|;
try|try
block|{
name|taskResult
operator|=
name|task
operator|.
name|result
argument_list|(
name|localNode
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"couldn't store response {}"
argument_list|,
name|response
argument_list|)
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|ex
argument_list|)
expr_stmt|;
return|return;
block|}
name|taskResultsService
operator|.
name|storeResult
argument_list|(
name|taskResult
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|Void
name|aVoid
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
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"couldn't store response {}"
argument_list|,
name|response
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
comment|/**      * Returns the list of currently running tasks on the node      */
DECL|method|getTasks
specifier|public
name|Map
argument_list|<
name|Long
argument_list|,
name|Task
argument_list|>
name|getTasks
parameter_list|()
block|{
name|HashMap
argument_list|<
name|Long
argument_list|,
name|Task
argument_list|>
name|taskHashMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|this
operator|.
name|tasks
argument_list|)
decl_stmt|;
for|for
control|(
name|CancellableTaskHolder
name|holder
range|:
name|cancellableTasks
operator|.
name|values
argument_list|()
control|)
block|{
name|taskHashMap
operator|.
name|put
argument_list|(
name|holder
operator|.
name|getTask
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|holder
operator|.
name|getTask
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|taskHashMap
argument_list|)
return|;
block|}
comment|/**      * Returns the list of currently running tasks on the node that can be cancelled      */
DECL|method|getCancellableTasks
specifier|public
name|Map
argument_list|<
name|Long
argument_list|,
name|CancellableTask
argument_list|>
name|getCancellableTasks
parameter_list|()
block|{
name|HashMap
argument_list|<
name|Long
argument_list|,
name|CancellableTask
argument_list|>
name|taskHashMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|CancellableTaskHolder
name|holder
range|:
name|cancellableTasks
operator|.
name|values
argument_list|()
control|)
block|{
name|taskHashMap
operator|.
name|put
argument_list|(
name|holder
operator|.
name|getTask
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|holder
operator|.
name|getTask
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|taskHashMap
argument_list|)
return|;
block|}
comment|/**      * Returns a task with given id, or null if the task is not found.      */
DECL|method|getTask
specifier|public
name|Task
name|getTask
parameter_list|(
name|long
name|id
parameter_list|)
block|{
name|Task
name|task
init|=
name|tasks
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|task
operator|!=
literal|null
condition|)
block|{
return|return
name|task
return|;
block|}
else|else
block|{
return|return
name|getCancellableTask
argument_list|(
name|id
argument_list|)
return|;
block|}
block|}
comment|/**      * Returns a cancellable task with given id, or null if the task is not found.      */
DECL|method|getCancellableTask
specifier|public
name|CancellableTask
name|getCancellableTask
parameter_list|(
name|long
name|id
parameter_list|)
block|{
name|CancellableTaskHolder
name|holder
init|=
name|cancellableTasks
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|holder
operator|!=
literal|null
condition|)
block|{
return|return
name|holder
operator|.
name|getTask
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**      * Returns the number of currently banned tasks.      *<p>      * Will be used in task manager stats and for debugging.      */
DECL|method|getBanCount
specifier|public
name|int
name|getBanCount
parameter_list|()
block|{
return|return
name|banedParents
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**      * Bans all tasks with the specified parent task from execution, cancels all tasks that are currently executing.      *<p>      * This method is called when a parent task that has children is cancelled.      */
DECL|method|setBan
specifier|public
name|void
name|setBan
parameter_list|(
name|TaskId
name|parentTaskId
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"setting ban for the parent task {} {}"
argument_list|,
name|parentTaskId
argument_list|,
name|reason
argument_list|)
expr_stmt|;
comment|// Set the ban first, so the newly created tasks cannot be registered
synchronized|synchronized
init|(
name|banedParents
init|)
block|{
if|if
condition|(
name|lastDiscoveryNodes
operator|.
name|nodeExists
argument_list|(
name|parentTaskId
operator|.
name|getNodeId
argument_list|()
argument_list|)
condition|)
block|{
comment|// Only set the ban if the node is the part of the cluster
name|banedParents
operator|.
name|put
argument_list|(
name|parentTaskId
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Now go through already running tasks and cancel them
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|CancellableTaskHolder
argument_list|>
name|taskEntry
range|:
name|cancellableTasks
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|CancellableTaskHolder
name|holder
init|=
name|taskEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|holder
operator|.
name|hasParent
argument_list|(
name|parentTaskId
argument_list|)
condition|)
block|{
name|holder
operator|.
name|cancel
argument_list|(
name|reason
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Removes the ban for the specified parent task.      *<p>      * This method is called when a previously banned task finally cancelled      */
DECL|method|removeBan
specifier|public
name|void
name|removeBan
parameter_list|(
name|TaskId
name|parentTaskId
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"removing ban for the parent task {}"
argument_list|,
name|parentTaskId
argument_list|)
expr_stmt|;
name|banedParents
operator|.
name|remove
argument_list|(
name|parentTaskId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|applyClusterState
specifier|public
name|void
name|applyClusterState
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
name|lastDiscoveryNodes
operator|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|getNodes
argument_list|()
expr_stmt|;
if|if
condition|(
name|event
operator|.
name|nodesRemoved
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|banedParents
init|)
block|{
name|lastDiscoveryNodes
operator|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|getNodes
argument_list|()
expr_stmt|;
comment|// Remove all bans that were registered by nodes that are no longer in the cluster state
name|Iterator
argument_list|<
name|TaskId
argument_list|>
name|banIterator
init|=
name|banedParents
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|banIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|TaskId
name|taskId
init|=
name|banIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastDiscoveryNodes
operator|.
name|nodeExists
argument_list|(
name|taskId
operator|.
name|getNodeId
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Removing ban for the parent [{}] on the node [{}], reason: the parent node is gone"
argument_list|,
name|taskId
argument_list|,
name|event
operator|.
name|state
argument_list|()
operator|.
name|getNodes
argument_list|()
operator|.
name|getLocalNode
argument_list|()
argument_list|)
expr_stmt|;
name|banIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|// Cancel cancellable tasks for the nodes that are gone
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|CancellableTaskHolder
argument_list|>
name|taskEntry
range|:
name|cancellableTasks
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|CancellableTaskHolder
name|holder
init|=
name|taskEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|CancellableTask
name|task
init|=
name|holder
operator|.
name|getTask
argument_list|()
decl_stmt|;
name|TaskId
name|parentTaskId
init|=
name|task
operator|.
name|getParentTaskId
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentTaskId
operator|.
name|isSet
argument_list|()
operator|&&
name|lastDiscoveryNodes
operator|.
name|nodeExists
argument_list|(
name|parentTaskId
operator|.
name|getNodeId
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|task
operator|.
name|cancelOnParentLeaving
argument_list|()
condition|)
block|{
name|holder
operator|.
name|cancel
argument_list|(
literal|"Coordinating node ["
operator|+
name|parentTaskId
operator|.
name|getNodeId
argument_list|()
operator|+
literal|"] left the cluster"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|registerChildTask
specifier|public
name|void
name|registerChildTask
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|node
parameter_list|)
block|{
if|if
condition|(
name|task
operator|==
literal|null
operator|||
name|task
operator|instanceof
name|CancellableTask
operator|==
literal|false
condition|)
block|{
comment|// We don't have a cancellable task - not much we can do here
return|return;
block|}
name|CancellableTaskHolder
name|holder
init|=
name|cancellableTasks
operator|.
name|get
argument_list|(
name|task
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|holder
operator|!=
literal|null
condition|)
block|{
name|holder
operator|.
name|registerChildTaskNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Blocks the calling thread, waiting for the task to vanish from the TaskManager.      */
DECL|method|waitForTaskCompletion
specifier|public
name|void
name|waitForTaskCompletion
parameter_list|(
name|Task
name|task
parameter_list|,
name|long
name|untilInNanos
parameter_list|)
block|{
while|while
condition|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|untilInNanos
operator|<
literal|0
condition|)
block|{
if|if
condition|(
name|getTask
argument_list|(
name|task
operator|.
name|getId
argument_list|()
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|WAIT_FOR_COMPLETION_POLL
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Interrupted waiting for completion of [{}]"
argument_list|,
name|e
argument_list|,
name|task
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|ElasticsearchTimeoutException
argument_list|(
literal|"Timed out waiting for completion of [{}]"
argument_list|,
name|task
argument_list|)
throw|;
block|}
DECL|class|CancellableTaskHolder
specifier|private
specifier|static
class|class
name|CancellableTaskHolder
block|{
DECL|field|TASK_FINISHED_MARKER
specifier|private
specifier|static
specifier|final
name|String
name|TASK_FINISHED_MARKER
init|=
literal|"task finished"
decl_stmt|;
DECL|field|task
specifier|private
specifier|final
name|CancellableTask
name|task
decl_stmt|;
DECL|field|nodesWithChildTasks
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|nodesWithChildTasks
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|cancellationReason
specifier|private
specifier|volatile
name|String
name|cancellationReason
init|=
literal|null
decl_stmt|;
DECL|field|cancellationListener
specifier|private
specifier|volatile
name|Consumer
argument_list|<
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|cancellationListener
init|=
literal|null
decl_stmt|;
DECL|method|CancellableTaskHolder
specifier|public
name|CancellableTaskHolder
parameter_list|(
name|CancellableTask
name|task
parameter_list|)
block|{
name|this
operator|.
name|task
operator|=
name|task
expr_stmt|;
block|}
comment|/**          * Marks task as cancelled.          *<p>          * Returns a set of nodes with child tasks where this task should be cancelled if cancellation was successful, null otherwise.          */
DECL|method|cancel
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|cancel
parameter_list|(
name|String
name|reason
parameter_list|,
name|Consumer
argument_list|<
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|listener
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|nodes
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
assert|assert
name|reason
operator|!=
literal|null
assert|;
if|if
condition|(
name|cancellationReason
operator|==
literal|null
condition|)
block|{
name|cancellationReason
operator|=
name|reason
expr_stmt|;
name|cancellationListener
operator|=
name|listener
expr_stmt|;
name|nodes
operator|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|nodesWithChildTasks
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Already cancelled by somebody else
name|nodes
operator|=
literal|null
expr_stmt|;
block|}
block|}
if|if
condition|(
name|nodes
operator|!=
literal|null
condition|)
block|{
name|task
operator|.
name|cancel
argument_list|(
name|reason
argument_list|)
expr_stmt|;
block|}
return|return
name|nodes
return|;
block|}
comment|/**          * Marks task as cancelled.          *<p>          * Returns a set of nodes with child tasks where this task should be cancelled if cancellation was successful, null otherwise.          */
DECL|method|cancel
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|cancel
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
return|return
name|cancel
argument_list|(
name|reason
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**          * Marks task as finished.          */
DECL|method|finish
specifier|public
name|void
name|finish
parameter_list|()
block|{
name|Consumer
argument_list|<
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|listener
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|nodes
init|=
literal|null
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|cancellationReason
operator|!=
literal|null
condition|)
block|{
comment|// The task was cancelled, we need to notify the listener
if|if
condition|(
name|cancellationListener
operator|!=
literal|null
condition|)
block|{
name|listener
operator|=
name|cancellationListener
expr_stmt|;
name|nodes
operator|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|nodesWithChildTasks
argument_list|)
expr_stmt|;
name|cancellationListener
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
name|cancellationReason
operator|=
name|TASK_FINISHED_MARKER
expr_stmt|;
block|}
block|}
comment|// We need to call the listener outside of the synchronised section to avoid potential bottle necks
comment|// in the listener synchronization
if|if
condition|(
name|listener
operator|!=
literal|null
condition|)
block|{
name|listener
operator|.
name|accept
argument_list|(
name|nodes
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|hasParent
specifier|public
name|boolean
name|hasParent
parameter_list|(
name|TaskId
name|parentTaskId
parameter_list|)
block|{
return|return
name|task
operator|.
name|getParentTaskId
argument_list|()
operator|.
name|equals
argument_list|(
name|parentTaskId
argument_list|)
return|;
block|}
DECL|method|getTask
specifier|public
name|CancellableTask
name|getTask
parameter_list|()
block|{
return|return
name|task
return|;
block|}
DECL|method|registerChildTaskNode
specifier|public
specifier|synchronized
name|void
name|registerChildTaskNode
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
if|if
condition|(
name|cancellationReason
operator|==
literal|null
condition|)
block|{
name|nodesWithChildTasks
operator|.
name|add
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|TaskCancelledException
argument_list|(
literal|"cannot register child task request, the task is already cancelled"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

