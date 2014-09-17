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
name|test
operator|.
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
comment|/**  */
end_comment

begin_class
DECL|class|EsExecutorsTests
specifier|public
class|class
name|EsExecutorsTests
extends|extends
name|ElasticsearchTestCase
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
annotation|@
name|Test
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
annotation|@
name|Test
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
name|executor
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
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
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
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
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

