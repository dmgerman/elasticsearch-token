begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
package|;
end_package

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
name|ActionRequest
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
name|ActionRequestValidationException
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
name|tasks
operator|.
name|TaskManager
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
name|Comparator
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
name|List
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
name|CopyOnWriteArrayList
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
name|ExecutionException
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
name|AtomicInteger
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
name|IntStream
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
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
name|CoreMatchers
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
name|CoreMatchers
operator|.
name|notNullValue
import|;
end_import

begin_class
DECL|class|TransportActionFilterChainTests
specifier|public
class|class
name|TransportActionFilterChainTests
extends|extends
name|ESTestCase
block|{
DECL|field|counter
specifier|private
name|AtomicInteger
name|counter
decl_stmt|;
annotation|@
name|Before
DECL|method|init
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|Exception
block|{
name|counter
operator|=
operator|new
name|AtomicInteger
argument_list|()
expr_stmt|;
block|}
DECL|method|testActionFiltersRequest
specifier|public
name|void
name|testActionFiltersRequest
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
name|int
name|numFilters
init|=
name|randomInt
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|orders
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|numFilters
argument_list|)
decl_stmt|;
while|while
condition|(
name|orders
operator|.
name|size
argument_list|()
operator|<
name|numFilters
condition|)
block|{
name|orders
operator|.
name|add
argument_list|(
name|randomInt
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|ActionFilter
argument_list|>
name|filters
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|order
range|:
name|orders
control|)
block|{
name|filters
operator|.
name|add
argument_list|(
operator|new
name|RequestTestFilter
argument_list|(
name|order
argument_list|,
name|randomFrom
argument_list|(
name|RequestOperation
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|actionName
init|=
name|randomAsciiOfLength
argument_list|(
name|randomInt
argument_list|(
literal|30
argument_list|)
argument_list|)
decl_stmt|;
name|ActionFilters
name|actionFilters
init|=
operator|new
name|ActionFilters
argument_list|(
name|filters
argument_list|)
decl_stmt|;
name|TransportAction
argument_list|<
name|TestRequest
argument_list|,
name|TestResponse
argument_list|>
name|transportAction
init|=
operator|new
name|TransportAction
argument_list|<
name|TestRequest
argument_list|,
name|TestResponse
argument_list|>
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|actionName
argument_list|,
literal|null
argument_list|,
name|actionFilters
argument_list|,
literal|null
argument_list|,
operator|new
name|TaskManager
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|doExecute
parameter_list|(
name|TestRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|TestResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|TestResponse
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|ArrayList
argument_list|<
name|ActionFilter
argument_list|>
name|actionFiltersByOrder
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|filters
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|actionFiltersByOrder
argument_list|,
operator|new
name|Comparator
argument_list|<
name|ActionFilter
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|ActionFilter
name|o1
parameter_list|,
name|ActionFilter
name|o2
parameter_list|)
block|{
return|return
name|Integer
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|order
argument_list|()
argument_list|,
name|o2
operator|.
name|order
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ActionFilter
argument_list|>
name|expectedActionFilters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|errorExpected
init|=
literal|false
decl_stmt|;
for|for
control|(
name|ActionFilter
name|filter
range|:
name|actionFiltersByOrder
control|)
block|{
name|RequestTestFilter
name|testFilter
init|=
operator|(
name|RequestTestFilter
operator|)
name|filter
decl_stmt|;
name|expectedActionFilters
operator|.
name|add
argument_list|(
name|testFilter
argument_list|)
expr_stmt|;
if|if
condition|(
name|testFilter
operator|.
name|callback
operator|==
name|RequestOperation
operator|.
name|LISTENER_FAILURE
condition|)
block|{
name|errorExpected
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
name|testFilter
operator|.
name|callback
operator|==
name|RequestOperation
operator|.
name|CONTINUE_PROCESSING
operator|)
condition|)
block|{
break|break;
block|}
block|}
name|PlainListenableActionFuture
argument_list|<
name|TestResponse
argument_list|>
name|future
init|=
operator|new
name|PlainListenableActionFuture
argument_list|<>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|transportAction
operator|.
name|execute
argument_list|(
operator|new
name|TestRequest
argument_list|()
argument_list|,
name|future
argument_list|)
expr_stmt|;
try|try
block|{
name|assertThat
argument_list|(
name|future
operator|.
name|get
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"shouldn't get here if an error is expected"
argument_list|,
name|errorExpected
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"shouldn't get here if an error is not expected "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|errorExpected
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|RequestTestFilter
argument_list|>
name|testFiltersByLastExecution
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ActionFilter
name|actionFilter
range|:
name|actionFilters
operator|.
name|filters
argument_list|()
control|)
block|{
name|testFiltersByLastExecution
operator|.
name|add
argument_list|(
operator|(
name|RequestTestFilter
operator|)
name|actionFilter
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|testFiltersByLastExecution
argument_list|,
operator|new
name|Comparator
argument_list|<
name|RequestTestFilter
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|RequestTestFilter
name|o1
parameter_list|,
name|RequestTestFilter
name|o2
parameter_list|)
block|{
return|return
name|Integer
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|executionToken
argument_list|,
name|o2
operator|.
name|executionToken
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|RequestTestFilter
argument_list|>
name|finalTestFilters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ActionFilter
name|filter
range|:
name|testFiltersByLastExecution
control|)
block|{
name|RequestTestFilter
name|testFilter
init|=
operator|(
name|RequestTestFilter
operator|)
name|filter
decl_stmt|;
name|finalTestFilters
operator|.
name|add
argument_list|(
name|testFilter
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|testFilter
operator|.
name|callback
operator|==
name|RequestOperation
operator|.
name|CONTINUE_PROCESSING
operator|)
condition|)
block|{
break|break;
block|}
block|}
name|assertThat
argument_list|(
name|finalTestFilters
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedActionFilters
operator|.
name|size
argument_list|()
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
name|finalTestFilters
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|RequestTestFilter
name|testFilter
init|=
name|finalTestFilters
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|testFilter
argument_list|,
name|equalTo
argument_list|(
name|expectedActionFilters
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|testFilter
operator|.
name|runs
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|testFilter
operator|.
name|lastActionName
argument_list|,
name|equalTo
argument_list|(
name|actionName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testTooManyContinueProcessingRequest
specifier|public
name|void
name|testTooManyContinueProcessingRequest
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
specifier|final
name|int
name|additionalContinueCount
init|=
name|randomInt
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|RequestTestFilter
name|testFilter
init|=
operator|new
name|RequestTestFilter
argument_list|(
name|randomInt
argument_list|()
argument_list|,
operator|new
name|RequestCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|execute
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|actionFilterChain
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|additionalContinueCount
condition|;
name|i
operator|++
control|)
block|{
name|actionFilterChain
operator|.
name|proceed
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ActionFilter
argument_list|>
name|filters
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|filters
operator|.
name|add
argument_list|(
name|testFilter
argument_list|)
expr_stmt|;
name|String
name|actionName
init|=
name|randomAsciiOfLength
argument_list|(
name|randomInt
argument_list|(
literal|30
argument_list|)
argument_list|)
decl_stmt|;
name|ActionFilters
name|actionFilters
init|=
operator|new
name|ActionFilters
argument_list|(
name|filters
argument_list|)
decl_stmt|;
name|TransportAction
argument_list|<
name|TestRequest
argument_list|,
name|TestResponse
argument_list|>
name|transportAction
init|=
operator|new
name|TransportAction
argument_list|<
name|TestRequest
argument_list|,
name|TestResponse
argument_list|>
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|actionName
argument_list|,
literal|null
argument_list|,
name|actionFilters
argument_list|,
literal|null
argument_list|,
operator|new
name|TaskManager
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|doExecute
parameter_list|(
name|TestRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|TestResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|TestResponse
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|additionalContinueCount
operator|+
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|responses
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Throwable
argument_list|>
name|failures
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|transportAction
operator|.
name|execute
argument_list|(
operator|new
name|TestRequest
argument_list|()
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|TestResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|TestResponse
name|testResponse
parameter_list|)
block|{
name|responses
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|latch
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
name|Exception
name|e
parameter_list|)
block|{
name|failures
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|latch
operator|.
name|await
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
name|fail
argument_list|(
literal|"timeout waiting for the filter to notify the listener as many times as expected"
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|testFilter
operator|.
name|runs
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|testFilter
operator|.
name|lastActionName
argument_list|,
name|equalTo
argument_list|(
name|actionName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|responses
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|failures
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|additionalContinueCount
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Throwable
name|failure
range|:
name|failures
control|)
block|{
name|assertThat
argument_list|(
name|failure
argument_list|,
name|instanceOf
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|RequestTestFilter
specifier|private
class|class
name|RequestTestFilter
implements|implements
name|ActionFilter
block|{
DECL|field|callback
specifier|private
specifier|final
name|RequestCallback
name|callback
decl_stmt|;
DECL|field|order
specifier|private
specifier|final
name|int
name|order
decl_stmt|;
DECL|field|runs
name|AtomicInteger
name|runs
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|lastActionName
specifier|volatile
name|String
name|lastActionName
decl_stmt|;
DECL|field|executionToken
specifier|volatile
name|int
name|executionToken
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
comment|//the filters that don't run will go last in the sorted list
DECL|method|RequestTestFilter
name|RequestTestFilter
parameter_list|(
name|int
name|order
parameter_list|,
name|RequestCallback
name|callback
parameter_list|)
block|{
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
name|this
operator|.
name|callback
operator|=
name|callback
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|order
specifier|public
name|int
name|order
parameter_list|()
block|{
return|return
name|order
return|;
block|}
annotation|@
name|Override
DECL|method|apply
specifier|public
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|apply
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|chain
parameter_list|)
block|{
name|this
operator|.
name|runs
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|this
operator|.
name|lastActionName
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|executionToken
operator|=
name|counter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|this
operator|.
name|callback
operator|.
name|execute
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|listener
argument_list|,
name|chain
argument_list|)
expr_stmt|;
block|}
block|}
DECL|enum|RequestOperation
specifier|private
specifier|static
enum|enum
name|RequestOperation
implements|implements
name|RequestCallback
block|{
DECL|enum constant|CONTINUE_PROCESSING
name|CONTINUE_PROCESSING
block|{
annotation|@
name|Override
specifier|public
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|execute
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|actionFilterChain
parameter_list|)
block|{
name|actionFilterChain
operator|.
name|proceed
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
block|,
DECL|enum constant|LISTENER_RESPONSE
name|LISTENER_RESPONSE
block|{
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// Safe because its all we test with
specifier|public
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|execute
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|actionFilterChain
parameter_list|)
block|{
operator|(
operator|(
name|ActionListener
argument_list|<
name|TestResponse
argument_list|>
operator|)
name|listener
operator|)
operator|.
name|onResponse
argument_list|(
operator|new
name|TestResponse
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|,
DECL|enum constant|LISTENER_FAILURE
name|LISTENER_FAILURE
block|{
annotation|@
name|Override
specifier|public
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|execute
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|actionFilterChain
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ElasticsearchTimeoutException
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|interface|RequestCallback
specifier|private
interface|interface
name|RequestCallback
block|{
DECL|method|execute
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|execute
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|actionFilterChain
parameter_list|)
function_decl|;
block|}
DECL|class|TestRequest
specifier|public
specifier|static
class|class
name|TestRequest
extends|extends
name|ActionRequest
block|{
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|class|TestResponse
specifier|private
specifier|static
class|class
name|TestResponse
extends|extends
name|ActionResponse
block|{      }
block|}
end_class

end_unit

