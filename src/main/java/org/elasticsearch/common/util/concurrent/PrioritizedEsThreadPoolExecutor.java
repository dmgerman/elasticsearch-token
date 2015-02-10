begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util.concurrent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
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
name|Queue
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
name|AtomicLong
import|;
end_import

begin_comment
comment|/**  * A prioritizing executor which uses a priority queue as a work queue. The jobs that will be submitted will be treated  * as {@link PrioritizedRunnable} and/or {@link PrioritizedCallable}, those tasks that are not instances of these two will  * be wrapped and assign a default {@link Priority#NORMAL} priority.  *<p/>  * Note, if two tasks have the same priority, the first to arrive will be executed first (FIFO style).  */
end_comment

begin_class
DECL|class|PrioritizedEsThreadPoolExecutor
specifier|public
class|class
name|PrioritizedEsThreadPoolExecutor
extends|extends
name|EsThreadPoolExecutor
block|{
DECL|field|insertionOrder
specifier|private
name|AtomicLong
name|insertionOrder
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|current
specifier|private
name|Queue
argument_list|<
name|Runnable
argument_list|>
name|current
init|=
name|ConcurrentCollections
operator|.
name|newQueue
argument_list|()
decl_stmt|;
DECL|method|PrioritizedEsThreadPoolExecutor
name|PrioritizedEsThreadPoolExecutor
parameter_list|(
name|int
name|corePoolSize
parameter_list|,
name|int
name|maximumPoolSize
parameter_list|,
name|long
name|keepAliveTime
parameter_list|,
name|TimeUnit
name|unit
parameter_list|,
name|ThreadFactory
name|threadFactory
parameter_list|)
block|{
name|super
argument_list|(
name|corePoolSize
argument_list|,
name|maximumPoolSize
argument_list|,
name|keepAliveTime
argument_list|,
name|unit
argument_list|,
operator|new
name|PriorityBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
name|threadFactory
argument_list|)
expr_stmt|;
block|}
DECL|method|getPending
specifier|public
name|Pending
index|[]
name|getPending
parameter_list|()
block|{
name|List
argument_list|<
name|Pending
argument_list|>
name|pending
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|addPending
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|current
argument_list|)
argument_list|,
name|pending
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|addPending
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|getQueue
argument_list|()
argument_list|)
argument_list|,
name|pending
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
name|pending
operator|.
name|toArray
argument_list|(
operator|new
name|Pending
index|[
name|pending
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
DECL|method|addPending
specifier|private
name|void
name|addPending
parameter_list|(
name|List
argument_list|<
name|Runnable
argument_list|>
name|runnables
parameter_list|,
name|List
argument_list|<
name|Pending
argument_list|>
name|pending
parameter_list|,
name|boolean
name|executing
parameter_list|)
block|{
for|for
control|(
name|Runnable
name|runnable
range|:
name|runnables
control|)
block|{
if|if
condition|(
name|runnable
operator|instanceof
name|TieBreakingPrioritizedRunnable
condition|)
block|{
name|TieBreakingPrioritizedRunnable
name|t
init|=
operator|(
name|TieBreakingPrioritizedRunnable
operator|)
name|runnable
decl_stmt|;
name|pending
operator|.
name|add
argument_list|(
operator|new
name|Pending
argument_list|(
name|t
operator|.
name|runnable
argument_list|,
name|t
operator|.
name|priority
argument_list|()
argument_list|,
name|t
operator|.
name|insertionOrder
argument_list|,
name|executing
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|runnable
operator|instanceof
name|PrioritizedFutureTask
condition|)
block|{
name|PrioritizedFutureTask
name|t
init|=
operator|(
name|PrioritizedFutureTask
operator|)
name|runnable
decl_stmt|;
name|pending
operator|.
name|add
argument_list|(
operator|new
name|Pending
argument_list|(
name|t
operator|.
name|task
argument_list|,
name|t
operator|.
name|priority
argument_list|,
name|t
operator|.
name|insertionOrder
argument_list|,
name|executing
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|beforeExecute
specifier|protected
name|void
name|beforeExecute
parameter_list|(
name|Thread
name|t
parameter_list|,
name|Runnable
name|r
parameter_list|)
block|{
name|current
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|afterExecute
specifier|protected
name|void
name|afterExecute
parameter_list|(
name|Runnable
name|r
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|current
operator|.
name|remove
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|Runnable
name|command
parameter_list|,
specifier|final
name|ScheduledExecutorService
name|timer
parameter_list|,
specifier|final
name|TimeValue
name|timeout
parameter_list|,
specifier|final
name|Runnable
name|timeoutCallback
parameter_list|)
block|{
if|if
condition|(
name|command
operator|instanceof
name|PrioritizedRunnable
condition|)
block|{
name|command
operator|=
operator|new
name|TieBreakingPrioritizedRunnable
argument_list|(
operator|(
name|PrioritizedRunnable
operator|)
name|command
argument_list|,
name|insertionOrder
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|command
operator|instanceof
name|PrioritizedFutureTask
operator|)
condition|)
block|{
comment|// it might be a callable wrapper...
name|command
operator|=
operator|new
name|TieBreakingPrioritizedRunnable
argument_list|(
name|command
argument_list|,
name|Priority
operator|.
name|NORMAL
argument_list|,
name|insertionOrder
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|timeout
operator|.
name|nanos
argument_list|()
operator|>=
literal|0
condition|)
block|{
if|if
condition|(
name|command
operator|instanceof
name|TieBreakingPrioritizedRunnable
condition|)
block|{
operator|(
operator|(
name|TieBreakingPrioritizedRunnable
operator|)
name|command
operator|)
operator|.
name|scheduleTimeout
argument_list|(
name|timer
argument_list|,
name|timeoutCallback
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We really shouldn't be here. The only way we can get here if somebody created PrioritizedFutureTask
comment|// and passed it to execute, which doesn't make much sense
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Execute with timeout is not supported for future tasks"
argument_list|)
throw|;
block|}
block|}
name|super
operator|.
name|execute
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|Runnable
name|command
parameter_list|)
block|{
if|if
condition|(
name|command
operator|instanceof
name|PrioritizedRunnable
condition|)
block|{
name|command
operator|=
operator|new
name|TieBreakingPrioritizedRunnable
argument_list|(
operator|(
name|PrioritizedRunnable
operator|)
name|command
argument_list|,
name|insertionOrder
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|command
operator|instanceof
name|PrioritizedFutureTask
operator|)
condition|)
block|{
comment|// it might be a callable wrapper...
name|command
operator|=
operator|new
name|TieBreakingPrioritizedRunnable
argument_list|(
name|command
argument_list|,
name|Priority
operator|.
name|NORMAL
argument_list|,
name|insertionOrder
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|execute
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newTaskFor
specifier|protected
parameter_list|<
name|T
parameter_list|>
name|RunnableFuture
argument_list|<
name|T
argument_list|>
name|newTaskFor
parameter_list|(
name|Runnable
name|runnable
parameter_list|,
name|T
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|runnable
operator|instanceof
name|PrioritizedRunnable
operator|)
condition|)
block|{
name|runnable
operator|=
name|PrioritizedRunnable
operator|.
name|wrap
argument_list|(
name|runnable
argument_list|,
name|Priority
operator|.
name|NORMAL
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|PrioritizedFutureTask
argument_list|<>
argument_list|(
operator|(
name|PrioritizedRunnable
operator|)
name|runnable
argument_list|,
name|value
argument_list|,
name|insertionOrder
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newTaskFor
specifier|protected
parameter_list|<
name|T
parameter_list|>
name|RunnableFuture
argument_list|<
name|T
argument_list|>
name|newTaskFor
parameter_list|(
name|Callable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|callable
operator|instanceof
name|PrioritizedCallable
operator|)
condition|)
block|{
name|callable
operator|=
name|PrioritizedCallable
operator|.
name|wrap
argument_list|(
name|callable
argument_list|,
name|Priority
operator|.
name|NORMAL
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|PrioritizedFutureTask
argument_list|<>
argument_list|(
operator|(
name|PrioritizedCallable
argument_list|<
name|T
argument_list|>
operator|)
name|callable
argument_list|,
name|insertionOrder
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
return|;
block|}
DECL|class|Pending
specifier|public
specifier|static
class|class
name|Pending
block|{
DECL|field|task
specifier|public
specifier|final
name|Object
name|task
decl_stmt|;
DECL|field|priority
specifier|public
specifier|final
name|Priority
name|priority
decl_stmt|;
DECL|field|insertionOrder
specifier|public
specifier|final
name|long
name|insertionOrder
decl_stmt|;
DECL|field|executing
specifier|public
specifier|final
name|boolean
name|executing
decl_stmt|;
DECL|method|Pending
specifier|public
name|Pending
parameter_list|(
name|Object
name|task
parameter_list|,
name|Priority
name|priority
parameter_list|,
name|long
name|insertionOrder
parameter_list|,
name|boolean
name|executing
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
name|priority
operator|=
name|priority
expr_stmt|;
name|this
operator|.
name|insertionOrder
operator|=
name|insertionOrder
expr_stmt|;
name|this
operator|.
name|executing
operator|=
name|executing
expr_stmt|;
block|}
block|}
DECL|class|TieBreakingPrioritizedRunnable
specifier|private
specifier|final
class|class
name|TieBreakingPrioritizedRunnable
extends|extends
name|PrioritizedRunnable
block|{
DECL|field|runnable
specifier|private
name|Runnable
name|runnable
decl_stmt|;
DECL|field|insertionOrder
specifier|private
specifier|final
name|long
name|insertionOrder
decl_stmt|;
DECL|field|timeoutFuture
specifier|private
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|timeoutFuture
decl_stmt|;
DECL|method|TieBreakingPrioritizedRunnable
name|TieBreakingPrioritizedRunnable
parameter_list|(
name|PrioritizedRunnable
name|runnable
parameter_list|,
name|long
name|insertionOrder
parameter_list|)
block|{
name|this
argument_list|(
name|runnable
argument_list|,
name|runnable
operator|.
name|priority
argument_list|()
argument_list|,
name|insertionOrder
argument_list|)
expr_stmt|;
block|}
DECL|method|TieBreakingPrioritizedRunnable
name|TieBreakingPrioritizedRunnable
parameter_list|(
name|Runnable
name|runnable
parameter_list|,
name|Priority
name|priority
parameter_list|,
name|long
name|insertionOrder
parameter_list|)
block|{
name|super
argument_list|(
name|priority
argument_list|)
expr_stmt|;
name|this
operator|.
name|runnable
operator|=
name|runnable
expr_stmt|;
name|this
operator|.
name|insertionOrder
operator|=
name|insertionOrder
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
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|timeoutFuture
argument_list|)
expr_stmt|;
name|runAndClean
argument_list|(
name|runnable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|PrioritizedRunnable
name|pr
parameter_list|)
block|{
name|int
name|res
init|=
name|super
operator|.
name|compareTo
argument_list|(
name|pr
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|!=
literal|0
operator|||
operator|!
operator|(
name|pr
operator|instanceof
name|TieBreakingPrioritizedRunnable
operator|)
condition|)
block|{
return|return
name|res
return|;
block|}
return|return
name|insertionOrder
operator|<
operator|(
operator|(
name|TieBreakingPrioritizedRunnable
operator|)
name|pr
operator|)
operator|.
name|insertionOrder
condition|?
operator|-
literal|1
else|:
literal|1
return|;
block|}
DECL|method|scheduleTimeout
specifier|public
name|void
name|scheduleTimeout
parameter_list|(
name|ScheduledExecutorService
name|timer
parameter_list|,
specifier|final
name|Runnable
name|timeoutCallback
parameter_list|,
name|TimeValue
name|timeValue
parameter_list|)
block|{
name|timeoutFuture
operator|=
name|timer
operator|.
name|schedule
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
if|if
condition|(
name|remove
argument_list|(
name|TieBreakingPrioritizedRunnable
operator|.
name|this
argument_list|)
condition|)
block|{
name|runAndClean
argument_list|(
name|timeoutCallback
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|,
name|timeValue
operator|.
name|nanos
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
expr_stmt|;
block|}
comment|/**          * Timeout callback might remain in the timer scheduling queue for some time and it might hold          * the pointers to other objects. As a result it's possible to run out of memory if a large number of          * tasks are executed          */
DECL|method|runAndClean
specifier|private
name|void
name|runAndClean
parameter_list|(
name|Runnable
name|run
parameter_list|)
block|{
try|try
block|{
name|run
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|runnable
operator|=
literal|null
expr_stmt|;
name|timeoutFuture
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
DECL|class|PrioritizedFutureTask
specifier|private
specifier|final
class|class
name|PrioritizedFutureTask
parameter_list|<
name|T
parameter_list|>
extends|extends
name|FutureTask
argument_list|<
name|T
argument_list|>
implements|implements
name|Comparable
argument_list|<
name|PrioritizedFutureTask
argument_list|>
block|{
DECL|field|task
specifier|final
name|Object
name|task
decl_stmt|;
DECL|field|priority
specifier|final
name|Priority
name|priority
decl_stmt|;
DECL|field|insertionOrder
specifier|final
name|long
name|insertionOrder
decl_stmt|;
DECL|method|PrioritizedFutureTask
specifier|public
name|PrioritizedFutureTask
parameter_list|(
name|PrioritizedRunnable
name|runnable
parameter_list|,
name|T
name|value
parameter_list|,
name|long
name|insertionOrder
parameter_list|)
block|{
name|super
argument_list|(
name|runnable
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|this
operator|.
name|task
operator|=
name|runnable
expr_stmt|;
name|this
operator|.
name|priority
operator|=
name|runnable
operator|.
name|priority
argument_list|()
expr_stmt|;
name|this
operator|.
name|insertionOrder
operator|=
name|insertionOrder
expr_stmt|;
block|}
DECL|method|PrioritizedFutureTask
specifier|public
name|PrioritizedFutureTask
parameter_list|(
name|PrioritizedCallable
argument_list|<
name|T
argument_list|>
name|callable
parameter_list|,
name|long
name|insertionOrder
parameter_list|)
block|{
name|super
argument_list|(
name|callable
argument_list|)
expr_stmt|;
name|this
operator|.
name|task
operator|=
name|callable
expr_stmt|;
name|this
operator|.
name|priority
operator|=
name|callable
operator|.
name|priority
argument_list|()
expr_stmt|;
name|this
operator|.
name|insertionOrder
operator|=
name|insertionOrder
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|PrioritizedFutureTask
name|pft
parameter_list|)
block|{
name|int
name|res
init|=
name|priority
operator|.
name|compareTo
argument_list|(
name|pft
operator|.
name|priority
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|!=
literal|0
condition|)
block|{
return|return
name|res
return|;
block|}
return|return
name|insertionOrder
operator|<
name|pft
operator|.
name|insertionOrder
condition|?
operator|-
literal|1
else|:
literal|1
return|;
block|}
block|}
block|}
end_class

end_unit

