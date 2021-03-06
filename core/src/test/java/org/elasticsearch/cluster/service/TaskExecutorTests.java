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
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterStateTaskConfig
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
name|ProcessClusterEventTimeoutException
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
name|lease
operator|.
name|Releasable
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
name|EsExecutors
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
name|TestThreadPool
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
name|AfterClass
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
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
operator|.
name|daemonThreadFactory
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
name|core
operator|.
name|Is
operator|.
name|is
import|;
end_import

begin_class
DECL|class|TaskExecutorTests
specifier|public
class|class
name|TaskExecutorTests
extends|extends
name|ESTestCase
block|{
DECL|field|threadPool
specifier|protected
specifier|static
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|threadExecutor
specifier|protected
name|PrioritizedEsThreadPoolExecutor
name|threadExecutor
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|createThreadPool
specifier|public
specifier|static
name|void
name|createThreadPool
parameter_list|()
block|{
name|threadPool
operator|=
operator|new
name|TestThreadPool
argument_list|(
name|getTestClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|stopThreadPool
specifier|public
specifier|static
name|void
name|stopThreadPool
parameter_list|()
block|{
if|if
condition|(
name|threadPool
operator|!=
literal|null
condition|)
block|{
name|threadPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|threadPool
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Before
DECL|method|setUpExecutor
specifier|public
name|void
name|setUpExecutor
parameter_list|()
block|{
name|threadExecutor
operator|=
name|EsExecutors
operator|.
name|newSinglePrioritizing
argument_list|(
literal|"test_thread"
argument_list|,
name|daemonThreadFactory
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"test_thread"
argument_list|)
argument_list|,
name|threadPool
operator|.
name|getThreadContext
argument_list|()
argument_list|,
name|threadPool
operator|.
name|scheduler
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|shutDownThreadExecutor
specifier|public
name|void
name|shutDownThreadExecutor
parameter_list|()
block|{
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|threadExecutor
argument_list|,
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
DECL|interface|TestListener
specifier|protected
interface|interface
name|TestListener
block|{
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
function_decl|;
DECL|method|processed
specifier|default
name|void
name|processed
parameter_list|(
name|String
name|source
parameter_list|)
block|{
comment|// do nothing by default
block|}
block|}
DECL|interface|TestExecutor
specifier|protected
interface|interface
name|TestExecutor
parameter_list|<
name|T
parameter_list|>
block|{
DECL|method|execute
name|void
name|execute
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|tasks
parameter_list|)
function_decl|;
DECL|method|describeTasks
specifier|default
name|String
name|describeTasks
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|tasks
parameter_list|)
block|{
return|return
name|tasks
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|T
operator|::
name|toString
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
block|{
if|if
condition|(
name|s1
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|s2
return|;
block|}
elseif|else
if|if
condition|(
name|s2
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|s1
return|;
block|}
else|else
block|{
return|return
name|s1
operator|+
literal|", "
operator|+
name|s2
return|;
block|}
block|}
argument_list|)
operator|.
name|orElse
argument_list|(
literal|""
argument_list|)
return|;
block|}
block|}
comment|/**      * Task class that works for single tasks as well as batching (see {@link TaskBatcherTests})      */
DECL|class|TestTask
specifier|protected
specifier|abstract
specifier|static
class|class
name|TestTask
implements|implements
name|TestExecutor
argument_list|<
name|TestTask
argument_list|>
implements|,
name|TestListener
implements|,
name|ClusterStateTaskConfig
block|{
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|List
argument_list|<
name|TestTask
argument_list|>
name|tasks
parameter_list|)
block|{
name|tasks
operator|.
name|forEach
argument_list|(
name|TestTask
operator|::
name|run
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Nullable
annotation|@
name|Override
DECL|method|timeout
specifier|public
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|priority
specifier|public
name|Priority
name|priority
parameter_list|()
block|{
return|return
name|Priority
operator|.
name|NORMAL
return|;
block|}
DECL|method|run
specifier|public
specifier|abstract
name|void
name|run
parameter_list|()
function_decl|;
block|}
DECL|class|UpdateTask
class|class
name|UpdateTask
extends|extends
name|SourcePrioritizedRunnable
block|{
DECL|field|testTask
specifier|final
name|TestTask
name|testTask
decl_stmt|;
DECL|method|UpdateTask
name|UpdateTask
parameter_list|(
name|String
name|source
parameter_list|,
name|TestTask
name|testTask
parameter_list|)
block|{
name|super
argument_list|(
name|testTask
operator|.
name|priority
argument_list|()
argument_list|,
name|source
argument_list|)
expr_stmt|;
name|this
operator|.
name|testTask
operator|=
name|testTask
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
name|logger
operator|.
name|trace
argument_list|(
literal|"will process {}"
argument_list|,
name|source
argument_list|)
expr_stmt|;
name|testTask
operator|.
name|execute
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|testTask
argument_list|)
argument_list|)
expr_stmt|;
name|testTask
operator|.
name|processed
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
block|}
comment|// can be overridden by TaskBatcherTests
DECL|method|submitTask
specifier|protected
name|void
name|submitTask
parameter_list|(
name|String
name|source
parameter_list|,
name|TestTask
name|testTask
parameter_list|)
block|{
name|SourcePrioritizedRunnable
name|task
init|=
operator|new
name|UpdateTask
argument_list|(
name|source
argument_list|,
name|testTask
argument_list|)
decl_stmt|;
name|TimeValue
name|timeout
init|=
name|testTask
operator|.
name|timeout
argument_list|()
decl_stmt|;
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
name|task
argument_list|,
name|timeout
argument_list|,
parameter_list|()
lambda|->
name|threadPool
operator|.
name|generic
argument_list|()
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"task [{}] timed out after [{}]"
argument_list|,
name|task
argument_list|,
name|timeout
argument_list|)
argument_list|;
name|testTask
operator|.
name|onFailure
argument_list|(
name|source
argument_list|,
operator|new
name|ProcessClusterEventTimeoutException
argument_list|(
name|timeout
argument_list|,
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|)
block|)
class|;
end_class

begin_block
unit|} else
block|{
name|threadExecutor
operator|.
name|execute
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
end_block

begin_function
unit|}       public
DECL|method|testTimedOutTaskCleanedUp
name|void
name|testTimedOutTaskCleanedUp
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|CountDownLatch
name|block
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|blockCompleted
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|TestTask
name|blockTask
init|=
operator|new
name|TestTask
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
name|block
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
name|blockCompleted
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
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
decl_stmt|;
name|submitTask
argument_list|(
literal|"block-task"
argument_list|,
name|blockTask
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|block2
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|TestTask
name|unblockTask
init|=
operator|new
name|TestTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|block2
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
name|block2
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
name|TimeValue
operator|.
name|ZERO
return|;
block|}
block|}
decl_stmt|;
name|submitTask
argument_list|(
literal|"unblock-task"
argument_list|,
name|unblockTask
argument_list|)
expr_stmt|;
name|block
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|block2
operator|.
name|await
argument_list|()
expr_stmt|;
name|blockCompleted
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
DECL|method|testTimeoutTask
specifier|public
name|void
name|testTimeoutTask
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|CountDownLatch
name|block
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|TestTask
name|test1
init|=
operator|new
name|TestTask
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
name|block
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
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
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
decl_stmt|;
name|submitTask
argument_list|(
literal|"block-task"
argument_list|,
name|test1
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|timedOut
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|executeCalled
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|TestTask
name|test2
init|=
operator|new
name|TestTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|2
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|executeCalled
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
name|timedOut
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|submitTask
argument_list|(
literal|"block-task"
argument_list|,
name|test2
argument_list|)
expr_stmt|;
name|timedOut
operator|.
name|await
argument_list|()
expr_stmt|;
name|block
operator|.
name|countDown
argument_list|()
expr_stmt|;
specifier|final
name|CountDownLatch
name|allProcessed
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|TestTask
name|test3
init|=
operator|new
name|TestTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|allProcessed
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
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
decl_stmt|;
name|submitTask
argument_list|(
literal|"block-task"
argument_list|,
name|test3
argument_list|)
expr_stmt|;
name|allProcessed
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// executed another task to double check that execute on the timed out update task is not called...
name|assertThat
argument_list|(
name|executeCalled
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
block|}
end_function

begin_class
DECL|class|TaskExecutor
specifier|static
class|class
name|TaskExecutor
implements|implements
name|TestExecutor
argument_list|<
name|Integer
argument_list|>
block|{
DECL|field|tasks
name|List
argument_list|<
name|Integer
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|tasks
parameter_list|)
block|{
name|this
operator|.
name|tasks
operator|.
name|addAll
argument_list|(
name|tasks
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_comment
comment|/**      * Note, this test can only work as long as we have a single thread executor executing the state update tasks!      */
end_comment

begin_function
DECL|method|testPrioritizedTasks
specifier|public
name|void
name|testPrioritizedTasks
parameter_list|()
throws|throws
name|Exception
block|{
name|BlockingTask
name|block
init|=
operator|new
name|BlockingTask
argument_list|(
name|Priority
operator|.
name|IMMEDIATE
argument_list|)
decl_stmt|;
name|submitTask
argument_list|(
literal|"test"
argument_list|,
name|block
argument_list|)
expr_stmt|;
name|int
name|taskCount
init|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
decl_stmt|;
comment|// will hold all the tasks in the order in which they were executed
name|List
argument_list|<
name|PrioritizedTask
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|taskCount
argument_list|)
decl_stmt|;
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|taskCount
argument_list|)
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
name|taskCount
condition|;
name|i
operator|++
control|)
block|{
name|Priority
name|priority
init|=
name|randomFrom
argument_list|(
name|Priority
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|PrioritizedTask
name|task
init|=
operator|new
name|PrioritizedTask
argument_list|(
name|priority
argument_list|,
name|latch
argument_list|,
name|tasks
argument_list|)
decl_stmt|;
name|submitTask
argument_list|(
literal|"test"
argument_list|,
name|task
argument_list|)
expr_stmt|;
block|}
name|block
operator|.
name|close
argument_list|()
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|Priority
name|prevPriority
init|=
literal|null
decl_stmt|;
for|for
control|(
name|PrioritizedTask
name|task
range|:
name|tasks
control|)
block|{
if|if
condition|(
name|prevPriority
operator|==
literal|null
condition|)
block|{
name|prevPriority
operator|=
name|task
operator|.
name|priority
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|task
operator|.
name|priority
argument_list|()
operator|.
name|sameOrAfter
argument_list|(
name|prevPriority
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_function

begin_class
DECL|class|BlockingTask
specifier|protected
specifier|static
class|class
name|BlockingTask
extends|extends
name|TestTask
implements|implements
name|Releasable
block|{
DECL|field|latch
specifier|private
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
DECL|field|priority
specifier|private
specifier|final
name|Priority
name|priority
decl_stmt|;
DECL|method|BlockingTask
name|BlockingTask
parameter_list|(
name|Priority
name|priority
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|priority
operator|=
name|priority
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
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{         }
annotation|@
name|Override
DECL|method|priority
specifier|public
name|Priority
name|priority
parameter_list|()
block|{
return|return
name|priority
return|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

begin_class
DECL|class|PrioritizedTask
specifier|protected
specifier|static
class|class
name|PrioritizedTask
extends|extends
name|TestTask
block|{
DECL|field|latch
specifier|private
specifier|final
name|CountDownLatch
name|latch
decl_stmt|;
DECL|field|tasks
specifier|private
specifier|final
name|List
argument_list|<
name|PrioritizedTask
argument_list|>
name|tasks
decl_stmt|;
DECL|field|priority
specifier|private
specifier|final
name|Priority
name|priority
decl_stmt|;
DECL|method|PrioritizedTask
specifier|private
name|PrioritizedTask
parameter_list|(
name|Priority
name|priority
parameter_list|,
name|CountDownLatch
name|latch
parameter_list|,
name|List
argument_list|<
name|PrioritizedTask
argument_list|>
name|tasks
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|latch
operator|=
name|latch
expr_stmt|;
name|this
operator|.
name|tasks
operator|=
name|tasks
expr_stmt|;
name|this
operator|.
name|priority
operator|=
name|priority
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
name|tasks
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|priority
specifier|public
name|Priority
name|priority
parameter_list|()
block|{
return|return
name|priority
return|;
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

unit|}
end_unit

