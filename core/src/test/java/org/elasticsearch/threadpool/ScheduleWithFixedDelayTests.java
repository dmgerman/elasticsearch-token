begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.threadpool
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
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
name|BaseFuture
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
name|test
operator|.
name|ESTestCase
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
operator|.
name|Cancellable
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
operator|.
name|Names
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
operator|.
name|ReschedulingRunnable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|CountDownLatch
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
name|ScheduledFuture
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
name|AtomicReference
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|instanceOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|isOneOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|sameInstance
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|times
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verifyNoMoreInteractions
import|;
end_import

begin_comment
comment|/**  * Unit tests for the scheduling of tasks with a fixed delay  */
end_comment

begin_class
DECL|class|ScheduleWithFixedDelayTests
specifier|public
class|class
name|ScheduleWithFixedDelayTests
extends|extends
name|ESTestCase
block|{
DECL|field|threadPool
specifier|private
name|ThreadPool
name|threadPool
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|threadPool
operator|=
operator|new
name|ThreadPool
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_NAME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"fixed delay tests"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|shutdown
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|Exception
block|{
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
DECL|method|testDoesNotRescheduleUntilExecutionFinished
specifier|public
name|void
name|testDoesNotRescheduleUntilExecutionFinished
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TimeValue
name|delay
init|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|100L
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|startLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|pauseLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ThreadPool
name|threadPool
init|=
name|mock
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|Runnable
name|runnable
init|=
parameter_list|()
lambda|->
block|{
comment|// notify that the runnable is started
name|startLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
comment|// wait for other thread to un-pause
name|pauseLatch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|ReschedulingRunnable
name|reschedulingRunnable
init|=
operator|new
name|ReschedulingRunnable
argument_list|(
name|runnable
argument_list|,
name|delay
argument_list|,
name|Names
operator|.
name|GENERIC
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
comment|// this call was made during construction of the runnable
name|verify
argument_list|(
name|threadPool
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|schedule
argument_list|(
name|delay
argument_list|,
name|Names
operator|.
name|GENERIC
argument_list|,
name|reschedulingRunnable
argument_list|)
expr_stmt|;
comment|// create a thread and start the runnable
name|Thread
name|runThread
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|reschedulingRunnable
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|runThread
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// wait for the runnable to be started and ensure the runnable hasn't used the threadpool again
name|startLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|verifyNoMoreInteractions
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
comment|// un-pause the runnable and allow it to complete execution
name|pauseLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|runThread
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// validate schedule was called again
name|verify
argument_list|(
name|threadPool
argument_list|,
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|schedule
argument_list|(
name|delay
argument_list|,
name|Names
operator|.
name|GENERIC
argument_list|,
name|reschedulingRunnable
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatRunnableIsRescheduled
specifier|public
name|void
name|testThatRunnableIsRescheduled
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|2
argument_list|,
literal|16
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Runnable
name|countingRunnable
init|=
parameter_list|()
lambda|->
block|{
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"sometimes we throw before counting down"
argument_list|)
throw|;
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"this shouldn't cause the test to fail!"
argument_list|)
throw|;
block|}
block|}
decl_stmt|;
name|Cancellable
name|cancellable
init|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|countingRunnable
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10L
argument_list|)
argument_list|,
name|Names
operator|.
name|GENERIC
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|cancellable
argument_list|)
expr_stmt|;
comment|// wait for the number of successful count down operations
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// cancel
name|cancellable
operator|.
name|cancel
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|cancellable
operator|.
name|isCancelled
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testCancellingRunnable
specifier|public
name|void
name|testCancellingRunnable
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|boolean
name|shouldThrow
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|2
argument_list|,
literal|16
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|doneLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Cancellable
argument_list|>
name|cancellableRef
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|AtomicBoolean
name|runAfterDone
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|Runnable
name|countingRunnable
init|=
parameter_list|()
lambda|->
block|{
if|if
condition|(
name|doneLatch
operator|.
name|getCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|runAfterDone
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|logger
operator|.
name|warn
argument_list|(
literal|"this runnable ran after it was cancelled"
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Cancellable
name|cancellable
init|=
name|cancellableRef
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|cancellable
operator|==
literal|null
condition|)
block|{
comment|// wait for the cancellable to be present before we really start so we can accurately know we cancelled
return|return;
block|}
comment|// rarely throw an exception before counting down
if|if
condition|(
name|shouldThrow
operator|&&
name|rarely
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"throw before count down"
argument_list|)
throw|;
block|}
specifier|final
name|int
name|count
init|=
name|counter
operator|.
name|decrementAndGet
argument_list|()
decl_stmt|;
comment|// see if we have counted down to zero or below yet. the exception throwing could make us count below zero
if|if
condition|(
name|count
operator|<=
literal|0
condition|)
block|{
name|cancellable
operator|.
name|cancel
argument_list|()
expr_stmt|;
name|doneLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
comment|// rarely throw an exception after execution
if|if
condition|(
name|shouldThrow
operator|&&
name|rarely
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"throw at end"
argument_list|)
throw|;
block|}
block|}
decl_stmt|;
name|Cancellable
name|cancellable
init|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|countingRunnable
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10L
argument_list|)
argument_list|,
name|Names
operator|.
name|GENERIC
argument_list|)
decl_stmt|;
name|cancellableRef
operator|.
name|set
argument_list|(
name|cancellable
argument_list|)
expr_stmt|;
comment|// wait for the runnable to finish
name|doneLatch
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// the runnable should have cancelled itself
name|assertTrue
argument_list|(
name|cancellable
operator|.
name|isCancelled
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|runAfterDone
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// rarely wait and make sure the runnable didn't run at the next interval
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|assertFalse
argument_list|(
name|awaitBusy
argument_list|(
name|runAfterDone
operator|::
name|get
argument_list|,
literal|1L
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testBlockingCallOnSchedulerThreadFails
specifier|public
name|void
name|testBlockingCallOnSchedulerThreadFails
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|BaseFuture
argument_list|<
name|Object
argument_list|>
name|future
init|=
operator|new
name|BaseFuture
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{}
decl_stmt|;
specifier|final
name|TestFuture
name|resultsFuture
init|=
operator|new
name|TestFuture
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|getWithTimeout
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
specifier|final
name|Runnable
name|runnable
init|=
parameter_list|()
lambda|->
block|{
try|try
block|{
name|Object
name|obj
decl_stmt|;
if|if
condition|(
name|getWithTimeout
condition|)
block|{
name|obj
operator|=
name|future
operator|.
name|get
argument_list|(
literal|1L
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|obj
operator|=
name|future
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|resultsFuture
operator|.
name|futureDone
argument_list|(
name|obj
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|resultsFuture
operator|.
name|futureDone
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|Cancellable
name|cancellable
init|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|runnable
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10L
argument_list|)
argument_list|,
name|Names
operator|.
name|SAME
argument_list|)
decl_stmt|;
name|Object
name|resultingObject
init|=
name|resultsFuture
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|resultingObject
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|resultingObject
argument_list|,
name|instanceOf
argument_list|(
name|Throwable
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Throwable
name|t
init|=
operator|(
name|Throwable
operator|)
name|resultingObject
decl_stmt|;
name|assertThat
argument_list|(
name|t
argument_list|,
name|instanceOf
argument_list|(
name|AssertionError
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|t
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Blocking"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cancellable
operator|.
name|isCancelled
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testBlockingCallOnNonSchedulerThreadAllowed
specifier|public
name|void
name|testBlockingCallOnNonSchedulerThreadAllowed
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TestFuture
name|future
init|=
operator|new
name|TestFuture
argument_list|()
decl_stmt|;
specifier|final
name|TestFuture
name|resultsFuture
init|=
operator|new
name|TestFuture
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|rethrow
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|getWithTimeout
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
specifier|final
name|Runnable
name|runnable
init|=
parameter_list|()
lambda|->
block|{
try|try
block|{
name|Object
name|obj
decl_stmt|;
if|if
condition|(
name|getWithTimeout
condition|)
block|{
name|obj
operator|=
name|future
operator|.
name|get
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|obj
operator|=
name|future
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|resultsFuture
operator|.
name|futureDone
argument_list|(
name|obj
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|resultsFuture
operator|.
name|futureDone
argument_list|(
name|t
argument_list|)
expr_stmt|;
if|if
condition|(
name|rethrow
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|t
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
specifier|final
name|Cancellable
name|cancellable
init|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|runnable
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10L
argument_list|)
argument_list|,
name|Names
operator|.
name|GENERIC
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|resultsFuture
operator|.
name|isDone
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|Object
name|o
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
name|future
operator|.
name|futureDone
argument_list|(
name|o
argument_list|)
expr_stmt|;
specifier|final
name|Object
name|resultingObject
init|=
name|resultsFuture
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resultingObject
argument_list|,
name|sameInstance
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cancellable
operator|.
name|isCancelled
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testOnRejectionCausesCancellation
specifier|public
name|void
name|testOnRejectionCausesCancellation
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TimeValue
name|delay
init|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10L
argument_list|)
decl_stmt|;
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
name|threadPool
operator|=
operator|new
name|ThreadPool
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_NAME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"fixed delay tests"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|schedule
parameter_list|(
name|TimeValue
name|delay
parameter_list|,
name|String
name|executor
parameter_list|,
name|Runnable
name|command
parameter_list|)
block|{
if|if
condition|(
name|command
operator|instanceof
name|ReschedulingRunnable
condition|)
block|{
operator|(
operator|(
name|ReschedulingRunnable
operator|)
name|command
operator|)
operator|.
name|onRejection
argument_list|(
operator|new
name|EsRejectedExecutionException
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
literal|"this should only be called with a rescheduling runnable in this test"
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
expr_stmt|;
name|Runnable
name|runnable
init|=
parameter_list|()
lambda|->
block|{}
decl_stmt|;
name|ReschedulingRunnable
name|reschedulingRunnable
init|=
operator|new
name|ReschedulingRunnable
argument_list|(
name|runnable
argument_list|,
name|delay
argument_list|,
name|Names
operator|.
name|GENERIC
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reschedulingRunnable
operator|.
name|isCancelled
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRunnableRunsAtMostOnceAfterCancellation
specifier|public
name|void
name|testRunnableRunsAtMostOnceAfterCancellation
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|iterations
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
literal|12
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|doneLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|iterations
argument_list|)
decl_stmt|;
specifier|final
name|Runnable
name|countingRunnable
init|=
parameter_list|()
lambda|->
block|{
name|counter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|doneLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
decl_stmt|;
specifier|final
name|Cancellable
name|cancellable
init|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|countingRunnable
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10L
argument_list|)
argument_list|,
name|Names
operator|.
name|GENERIC
argument_list|)
decl_stmt|;
name|doneLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|cancellable
operator|.
name|cancel
argument_list|()
expr_stmt|;
specifier|final
name|int
name|counterValue
init|=
name|counter
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|counterValue
argument_list|,
name|isOneOf
argument_list|(
name|iterations
argument_list|,
name|iterations
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|awaitBusy
argument_list|(
parameter_list|()
lambda|->
block|{
specifier|final
name|int
name|value
init|=
name|counter
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
name|value
operator|==
name|iterations
operator|||
name|value
operator|==
name|iterations
operator|+
literal|1
return|;
block|}
argument_list|,
literal|50L
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TestFuture
specifier|static
specifier|final
class|class
name|TestFuture
extends|extends
name|BaseFuture
argument_list|<
name|Object
argument_list|>
block|{
DECL|method|futureDone
name|boolean
name|futureDone
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|set
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

