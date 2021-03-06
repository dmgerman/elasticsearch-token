begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.service
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|service
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
name|Logger
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
name|Priority
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
name|EsRejectedExecutionException
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
name|PrioritizedEsThreadPoolExecutor
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
name|IdentityHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|Map
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
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  * Batching support for {@link PrioritizedEsThreadPoolExecutor}  * Tasks that share the same batching key are batched (see {@link BatchedTask#batchingKey})  */
end_comment

begin_class
DECL|class|TaskBatcher
specifier|public
specifier|abstract
class|class
name|TaskBatcher
block|{
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|threadExecutor
specifier|private
specifier|final
name|PrioritizedEsThreadPoolExecutor
name|threadExecutor
decl_stmt|;
comment|// package visible for tests
DECL|field|tasksPerBatchingKey
specifier|final
name|Map
argument_list|<
name|Object
argument_list|,
name|LinkedHashSet
argument_list|<
name|BatchedTask
argument_list|>
argument_list|>
name|tasksPerBatchingKey
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|TaskBatcher
specifier|public
name|TaskBatcher
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|PrioritizedEsThreadPoolExecutor
name|threadExecutor
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|threadExecutor
operator|=
name|threadExecutor
expr_stmt|;
block|}
DECL|method|submitTasks
specifier|public
name|void
name|submitTasks
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|BatchedTask
argument_list|>
name|tasks
parameter_list|,
annotation|@
name|Nullable
name|TimeValue
name|timeout
parameter_list|)
throws|throws
name|EsRejectedExecutionException
block|{
if|if
condition|(
name|tasks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
specifier|final
name|BatchedTask
name|firstTask
init|=
name|tasks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|tasks
operator|.
name|stream
argument_list|()
operator|.
name|allMatch
argument_list|(
name|t
lambda|->
name|t
operator|.
name|batchingKey
operator|==
name|firstTask
operator|.
name|batchingKey
argument_list|)
operator|:
literal|"tasks submitted in a batch should share the same batching key: "
operator|+
name|tasks
assert|;
comment|// convert to an identity map to check for dups based on task identity
specifier|final
name|Map
argument_list|<
name|Object
argument_list|,
name|BatchedTask
argument_list|>
name|tasksIdentity
init|=
name|tasks
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|BatchedTask
operator|::
name|getTask
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
parameter_list|(
name|a
parameter_list|,
name|b
parameter_list|)
lambda|->
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"cannot add duplicate task: "
operator|+
name|a
argument_list|)
throw|;
block|}
argument_list|,
name|IdentityHashMap
operator|::
operator|new
argument_list|)
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|tasksPerBatchingKey
init|)
block|{
name|LinkedHashSet
argument_list|<
name|BatchedTask
argument_list|>
name|existingTasks
init|=
name|tasksPerBatchingKey
operator|.
name|computeIfAbsent
argument_list|(
name|firstTask
operator|.
name|batchingKey
argument_list|,
name|k
lambda|->
operator|new
name|LinkedHashSet
argument_list|<>
argument_list|(
name|tasks
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|BatchedTask
name|existing
range|:
name|existingTasks
control|)
block|{
comment|// check that there won't be two tasks with the same identity for the same batching key
name|BatchedTask
name|duplicateTask
init|=
name|tasksIdentity
operator|.
name|get
argument_list|(
name|existing
operator|.
name|getTask
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|duplicateTask
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"task ["
operator|+
name|duplicateTask
operator|.
name|describeTasks
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|existing
argument_list|)
argument_list|)
operator|+
literal|"] with source ["
operator|+
name|duplicateTask
operator|.
name|source
operator|+
literal|"] is already queued"
argument_list|)
throw|;
block|}
block|}
name|existingTasks
operator|.
name|addAll
argument_list|(
name|tasks
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|timeout
operator|!=
literal|null
condition|)
block|{
name|threadExecutor
operator|.
name|execute
argument_list|(
name|firstTask
argument_list|,
name|timeout
argument_list|,
parameter_list|()
lambda|->
name|onTimeoutInternal
argument_list|(
name|tasks
argument_list|,
name|timeout
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|threadExecutor
operator|.
name|execute
argument_list|(
name|firstTask
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|onTimeoutInternal
specifier|private
name|void
name|onTimeoutInternal
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|BatchedTask
argument_list|>
name|tasks
parameter_list|,
name|TimeValue
name|timeout
parameter_list|)
block|{
specifier|final
name|ArrayList
argument_list|<
name|BatchedTask
argument_list|>
name|toRemove
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|BatchedTask
name|task
range|:
name|tasks
control|)
block|{
if|if
condition|(
name|task
operator|.
name|processed
operator|.
name|getAndSet
argument_list|(
literal|true
argument_list|)
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"task [{}] timed out after [{}]"
argument_list|,
name|task
operator|.
name|source
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
name|toRemove
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|toRemove
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|BatchedTask
name|firstTask
init|=
name|toRemove
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Object
name|batchingKey
init|=
name|firstTask
operator|.
name|batchingKey
decl_stmt|;
assert|assert
name|tasks
operator|.
name|stream
argument_list|()
operator|.
name|allMatch
argument_list|(
name|t
lambda|->
name|t
operator|.
name|batchingKey
operator|==
name|batchingKey
argument_list|)
operator|:
literal|"tasks submitted in a batch should share the same batching key: "
operator|+
name|tasks
assert|;
synchronized|synchronized
init|(
name|tasksPerBatchingKey
init|)
block|{
name|LinkedHashSet
argument_list|<
name|BatchedTask
argument_list|>
name|existingTasks
init|=
name|tasksPerBatchingKey
operator|.
name|get
argument_list|(
name|batchingKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingTasks
operator|!=
literal|null
condition|)
block|{
name|existingTasks
operator|.
name|removeAll
argument_list|(
name|toRemove
argument_list|)
expr_stmt|;
if|if
condition|(
name|existingTasks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tasksPerBatchingKey
operator|.
name|remove
argument_list|(
name|batchingKey
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|onTimeout
argument_list|(
name|toRemove
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Action to be implemented by the specific batching implementation.      * All tasks have the same batching key.      */
DECL|method|onTimeout
specifier|protected
specifier|abstract
name|void
name|onTimeout
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|BatchedTask
argument_list|>
name|tasks
parameter_list|,
name|TimeValue
name|timeout
parameter_list|)
function_decl|;
DECL|method|runIfNotProcessed
name|void
name|runIfNotProcessed
parameter_list|(
name|BatchedTask
name|updateTask
parameter_list|)
block|{
comment|// if this task is already processed, it shouldn't execute other tasks with same batching key that arrived later,
comment|// to give other tasks with different batching key a chance to execute.
if|if
condition|(
name|updateTask
operator|.
name|processed
operator|.
name|get
argument_list|()
operator|==
literal|false
condition|)
block|{
specifier|final
name|List
argument_list|<
name|BatchedTask
argument_list|>
name|toExecute
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|BatchedTask
argument_list|>
argument_list|>
name|processTasksBySource
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|tasksPerBatchingKey
init|)
block|{
name|LinkedHashSet
argument_list|<
name|BatchedTask
argument_list|>
name|pending
init|=
name|tasksPerBatchingKey
operator|.
name|remove
argument_list|(
name|updateTask
operator|.
name|batchingKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|pending
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BatchedTask
name|task
range|:
name|pending
control|)
block|{
if|if
condition|(
name|task
operator|.
name|processed
operator|.
name|getAndSet
argument_list|(
literal|true
argument_list|)
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"will process {}"
argument_list|,
name|task
argument_list|)
expr_stmt|;
name|toExecute
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
name|processTasksBySource
operator|.
name|computeIfAbsent
argument_list|(
name|task
operator|.
name|source
argument_list|,
name|s
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"skipping {}, already processed"
argument_list|,
name|task
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|toExecute
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
specifier|final
name|String
name|tasksSummary
init|=
name|processTasksBySource
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|entry
lambda|->
block|{
name|String
name|tasks
init|=
name|updateTask
operator|.
name|describeTasks
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|tasks
operator|.
name|isEmpty
argument_list|()
condition|?
name|entry
operator|.
name|getKey
argument_list|()
else|:
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"["
operator|+
name|tasks
operator|+
literal|"]"
return|;
block|}
argument_list|)
operator|.
name|reduce
argument_list|(
parameter_list|(
name|s1
parameter_list|,
name|s2
parameter_list|)
lambda|->
name|s1
operator|+
literal|", "
operator|+
name|s2
argument_list|)
operator|.
name|orElse
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|run
argument_list|(
name|updateTask
operator|.
name|batchingKey
argument_list|,
name|toExecute
argument_list|,
name|tasksSummary
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Action to be implemented by the specific batching implementation      * All tasks have the given batching key.      */
DECL|method|run
specifier|protected
specifier|abstract
name|void
name|run
parameter_list|(
name|Object
name|batchingKey
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|BatchedTask
argument_list|>
name|tasks
parameter_list|,
name|String
name|tasksSummary
parameter_list|)
function_decl|;
comment|/**      * Represents a runnable task that supports batching.      * Implementors of TaskBatcher can subclass this to add a payload to the task.      */
DECL|class|BatchedTask
specifier|protected
specifier|abstract
class|class
name|BatchedTask
extends|extends
name|SourcePrioritizedRunnable
block|{
comment|/**          * whether the task has been processed already          */
DECL|field|processed
specifier|protected
specifier|final
name|AtomicBoolean
name|processed
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
comment|/**          * the object that is used as batching key          */
DECL|field|batchingKey
specifier|protected
specifier|final
name|Object
name|batchingKey
decl_stmt|;
comment|/**          * the task object that is wrapped          */
DECL|field|task
specifier|protected
specifier|final
name|Object
name|task
decl_stmt|;
DECL|method|BatchedTask
specifier|protected
name|BatchedTask
parameter_list|(
name|Priority
name|priority
parameter_list|,
name|String
name|source
parameter_list|,
name|Object
name|batchingKey
parameter_list|,
name|Object
name|task
parameter_list|)
block|{
name|super
argument_list|(
name|priority
argument_list|,
name|source
argument_list|)
expr_stmt|;
name|this
operator|.
name|batchingKey
operator|=
name|batchingKey
expr_stmt|;
name|this
operator|.
name|task
operator|=
name|task
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|runIfNotProcessed
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|taskDescription
init|=
name|describeTasks
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|this
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|taskDescription
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|"["
operator|+
name|source
operator|+
literal|"]"
return|;
block|}
else|else
block|{
return|return
literal|"["
operator|+
name|source
operator|+
literal|"["
operator|+
name|taskDescription
operator|+
literal|"]]"
return|;
block|}
block|}
DECL|method|describeTasks
specifier|public
specifier|abstract
name|String
name|describeTasks
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|BatchedTask
argument_list|>
name|tasks
parameter_list|)
function_decl|;
DECL|method|getTask
specifier|public
name|Object
name|getTask
parameter_list|()
block|{
return|return
name|task
return|;
block|}
block|}
block|}
end_class

end_unit

