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
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matcher
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
name|ThreadPoolExecutor
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|anyOf
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
name|equalTo
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
name|lessThan
import|;
end_import

begin_comment
comment|/**  * Tests for EsExecutors and its components like EsAbortPolicy.  */
end_comment

begin_class
DECL|class|EsExecutorsTests
specifier|public
class|class
name|EsExecutorsTests
extends|extends
name|ESTestCase
block|{
DECL|method|randomTimeUnit
specifier|private
name|TimeUnit
name|randomTimeUnit
parameter_list|()
block|{
return|return
name|TimeUnit
operator|.
name|values
argument_list|()
index|[
name|between
argument_list|(
literal|0
argument_list|,
name|TimeUnit
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|1
argument_list|)
index|]
return|;
block|}
DECL|method|testFixedForcedExecution
specifier|public
name|void
name|testFixedForcedExecution
parameter_list|()
throws|throws
name|Exception
block|{
name|EsThreadPoolExecutor
name|executor
init|=
name|EsExecutors
operator|.
name|newFixed
argument_list|(
name|getTestName
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|wait
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|exec1Wait
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|executed1
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|executor
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
name|wait
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|executed1
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|exec1Wait
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|exec2Wait
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|executed2
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|executor
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
name|executed2
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|exec2Wait
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|AtomicBoolean
name|executed3
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|exec3Wait
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|executor
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
block|{
name|executed3
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|exec3Wait
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isForceExecution
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|t
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
name|wait
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|exec1Wait
operator|.
name|await
argument_list|()
expr_stmt|;
name|exec2Wait
operator|.
name|await
argument_list|()
expr_stmt|;
name|exec3Wait
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|executed1
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|executed2
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|executed3
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
DECL|method|testFixedRejected
specifier|public
name|void
name|testFixedRejected
parameter_list|()
throws|throws
name|Exception
block|{
name|EsThreadPoolExecutor
name|executor
init|=
name|EsExecutors
operator|.
name|newFixed
argument_list|(
name|getTestName
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|wait
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|exec1Wait
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|executed1
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|executor
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
name|wait
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|executed1
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|exec1Wait
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|exec2Wait
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|executed2
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|executor
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
name|executed2
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|exec2Wait
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|AtomicBoolean
name|executed3
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
try|try
block|{
name|executor
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
name|executed3
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be rejected..."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EsRejectedExecutionException
name|e
parameter_list|)
block|{
comment|// all is well
block|}
name|wait
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|exec1Wait
operator|.
name|await
argument_list|()
expr_stmt|;
name|exec2Wait
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|executed1
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|executed2
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|executed3
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|terminate
argument_list|(
name|executor
argument_list|)
expr_stmt|;
block|}
DECL|method|testScaleUp
specifier|public
name|void
name|testScaleUp
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|min
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|3
argument_list|)
decl_stmt|;
specifier|final
name|int
name|max
init|=
name|between
argument_list|(
name|min
operator|+
literal|1
argument_list|,
literal|6
argument_list|)
decl_stmt|;
specifier|final
name|ThreadBarrier
name|barrier
init|=
operator|new
name|ThreadBarrier
argument_list|(
name|max
operator|+
literal|1
argument_list|)
decl_stmt|;
name|ThreadPoolExecutor
name|pool
init|=
name|EsExecutors
operator|.
name|newScaling
argument_list|(
name|getTestName
argument_list|()
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|between
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
argument_list|,
name|randomTimeUnit
argument_list|()
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"Min property"
argument_list|,
name|pool
operator|.
name|getCorePoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|min
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Max property"
argument_list|,
name|pool
operator|.
name|getMaximumPoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|max
argument_list|)
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
name|max
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|pool
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
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|barrier
operator|.
name|reset
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|//wait until thread executes this task
comment|//otherwise, a task might be queued
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"wrong pool size"
argument_list|,
name|pool
operator|.
name|getPoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|max
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"wrong active size"
argument_list|,
name|pool
operator|.
name|getActiveCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|max
argument_list|)
argument_list|)
expr_stmt|;
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
name|terminate
argument_list|(
name|pool
argument_list|)
expr_stmt|;
block|}
DECL|method|testScaleDown
specifier|public
name|void
name|testScaleDown
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|min
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|3
argument_list|)
decl_stmt|;
specifier|final
name|int
name|max
init|=
name|between
argument_list|(
name|min
operator|+
literal|1
argument_list|,
literal|6
argument_list|)
decl_stmt|;
specifier|final
name|ThreadBarrier
name|barrier
init|=
operator|new
name|ThreadBarrier
argument_list|(
name|max
operator|+
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|ThreadPoolExecutor
name|pool
init|=
name|EsExecutors
operator|.
name|newScaling
argument_list|(
name|getTestName
argument_list|()
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|between
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"Min property"
argument_list|,
name|pool
operator|.
name|getCorePoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|min
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Max property"
argument_list|,
name|pool
operator|.
name|getMaximumPoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|max
argument_list|)
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
name|max
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|pool
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
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|barrier
operator|.
name|reset
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|//wait until thread executes this task
comment|//otherwise, a task might be queued
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"wrong pool size"
argument_list|,
name|pool
operator|.
name|getPoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|max
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"wrong active size"
argument_list|,
name|pool
operator|.
name|getActiveCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|max
argument_list|)
argument_list|)
expr_stmt|;
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertBusy
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
name|assertThat
argument_list|(
literal|"wrong active count"
argument_list|,
name|pool
operator|.
name|getActiveCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"idle threads didn't shrink below max. ("
operator|+
name|pool
operator|.
name|getPoolSize
argument_list|()
operator|+
literal|")"
argument_list|,
name|pool
operator|.
name|getPoolSize
argument_list|()
argument_list|,
name|lessThan
argument_list|(
name|max
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|terminate
argument_list|(
name|pool
argument_list|)
expr_stmt|;
block|}
DECL|method|testRejectionMessageAndShuttingDownFlag
specifier|public
name|void
name|testRejectionMessageAndShuttingDownFlag
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|int
name|pool
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|int
name|queue
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|int
name|actions
init|=
name|queue
operator|+
name|pool
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|EsThreadPoolExecutor
name|executor
init|=
name|EsExecutors
operator|.
name|newFixed
argument_list|(
name|getTestName
argument_list|()
argument_list|,
name|pool
argument_list|,
name|queue
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
literal|"dummy"
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|actions
condition|;
name|i
operator|++
control|)
block|{
name|executor
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
name|latch
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|executor
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
comment|// Doesn't matter is going to be rejected
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"dummy runnable"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Didn't get a rejection when we expected one."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EsRejectedExecutionException
name|e
parameter_list|)
block|{
name|assertFalse
argument_list|(
literal|"Thread pool registering as terminated when it isn't"
argument_list|,
name|e
operator|.
name|isExecutorShutdown
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|message
init|=
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"of dummy runnable"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"on EsThreadPoolExecutor[testRejectionMessage"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"queue capacity = "
operator|+
name|queue
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"[Running"
argument_list|)
argument_list|)
expr_stmt|;
comment|/*                  * While you'd expect all threads in the pool to be active when the queue gets long enough to cause rejections this isn't                  * always the case. Sometimes you'll see "active threads =<pool - 1>", presumably because one of those threads has finished                  * its current task but has yet to pick up another task. You too can reproduce this by adding the @Repeat annotation to this                  * test with something like 10000 iterations. I suspect you could see "active threads =<any natural number<= to pool>". So                  * that is what we assert.                  */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Matcher
argument_list|<
name|String
argument_list|>
index|[]
name|activeThreads
init|=
operator|new
name|Matcher
index|[
name|pool
operator|+
literal|1
index|]
decl_stmt|;
for|for
control|(
name|int
name|p
init|=
literal|0
init|;
name|p
operator|<=
name|pool
condition|;
name|p
operator|++
control|)
block|{
name|activeThreads
index|[
name|p
index|]
operator|=
name|containsString
argument_list|(
literal|"active threads = "
operator|+
name|p
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|message
argument_list|,
name|anyOf
argument_list|(
name|activeThreads
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"queued tasks = "
operator|+
name|queue
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"completed tasks = 0"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|terminate
argument_list|(
name|executor
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|executor
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
comment|// Doesn't matter is going to be rejected
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"dummy runnable"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Didn't get a rejection when we expected one."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EsRejectedExecutionException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Thread pool not registering as terminated when it is"
argument_list|,
name|e
operator|.
name|isExecutorShutdown
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|message
init|=
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"of dummy runnable"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"on EsThreadPoolExecutor["
operator|+
name|getTestName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"queue capacity = "
operator|+
name|queue
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"[Terminated"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"active threads = 0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"queued tasks = 0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|containsString
argument_list|(
literal|"completed tasks = "
operator|+
name|actions
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

