begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|transport
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|client
operator|.
name|support
operator|.
name|Headers
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
name|ClusterName
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
name|transport
operator|.
name|LocalTransportAddress
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
name|Closeable
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
name|nullValue
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
name|lessThanOrEqualTo
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
name|notNullValue
import|;
end_import

begin_class
DECL|class|TransportClientNodesServiceTests
specifier|public
class|class
name|TransportClientNodesServiceTests
extends|extends
name|ESTestCase
block|{
DECL|class|TestIteration
specifier|private
specifier|static
class|class
name|TestIteration
implements|implements
name|Closeable
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|transport
specifier|private
specifier|final
name|FailAndRetryMockTransport
argument_list|<
name|TestResponse
argument_list|>
name|transport
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|transportClientNodesService
specifier|private
specifier|final
name|TransportClientNodesService
name|transportClientNodesService
decl_stmt|;
DECL|field|nodesCount
specifier|private
specifier|final
name|int
name|nodesCount
decl_stmt|;
DECL|method|TestIteration
name|TestIteration
parameter_list|()
block|{
name|threadPool
operator|=
operator|new
name|ThreadPool
argument_list|(
literal|"transport-client-nodes-service-tests"
argument_list|)
expr_stmt|;
name|transport
operator|=
operator|new
name|FailAndRetryMockTransport
argument_list|<
name|TestResponse
argument_list|>
argument_list|(
name|getRandom
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getLocalAddresses
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|EMPTY_LIST
return|;
block|}
annotation|@
name|Override
specifier|protected
name|TestResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|TestResponse
argument_list|()
return|;
block|}
block|}
expr_stmt|;
name|transportService
operator|=
operator|new
name|TransportService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|transport
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|start
argument_list|()
expr_stmt|;
name|transportClientNodesService
operator|=
operator|new
name|TransportClientNodesService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|ClusterName
operator|.
name|DEFAULT
argument_list|,
name|transportService
argument_list|,
name|threadPool
argument_list|,
name|Headers
operator|.
name|EMPTY
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|nodesCount
operator|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
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
name|nodesCount
condition|;
name|i
operator|++
control|)
block|{
name|transportClientNodesService
operator|.
name|addTransportAddresses
argument_list|(
operator|new
name|LocalTransportAddress
argument_list|(
literal|"node"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|transport
operator|.
name|endConnectMode
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|transportService
operator|.
name|stop
argument_list|()
expr_stmt|;
name|transportClientNodesService
operator|.
name|close
argument_list|()
expr_stmt|;
try|try
block|{
name|terminate
argument_list|(
name|threadPool
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
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|testListenerFailures
specifier|public
name|void
name|testListenerFailures
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|int
name|iters
init|=
name|iterations
argument_list|(
literal|10
argument_list|,
literal|100
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
name|iters
condition|;
name|i
operator|++
control|)
block|{
try|try
init|(
specifier|final
name|TestIteration
name|iteration
init|=
operator|new
name|TestIteration
argument_list|()
init|)
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
specifier|final
name|AtomicInteger
name|finalFailures
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|finalFailure
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|TestResponse
argument_list|>
name|response
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|ActionListener
argument_list|<
name|TestResponse
argument_list|>
name|actionListener
init|=
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
name|response
operator|.
name|set
argument_list|(
name|testResponse
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
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|finalFailures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|finalFailure
operator|.
name|set
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
decl_stmt|;
specifier|final
name|AtomicInteger
name|preSendFailures
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|iteration
operator|.
name|transportClientNodesService
operator|.
name|execute
argument_list|(
operator|new
name|TransportClientNodesService
operator|.
name|NodeListenerCallback
argument_list|<
name|TestResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|doWithNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|TestResponse
argument_list|>
name|retryListener
parameter_list|)
block|{
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|preSendFailures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
comment|//throw whatever exception that is not a subclass of ConnectTransportException
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
name|iteration
operator|.
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
literal|"action"
argument_list|,
operator|new
name|TestRequest
argument_list|()
argument_list|,
operator|new
name|TransportRequestOptions
argument_list|()
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|TestResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TestResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|TestResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|TestResponse
name|response
parameter_list|)
block|{
name|retryListener
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
name|retryListener
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
name|randomBoolean
argument_list|()
condition|?
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
else|:
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|actionListener
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|latch
operator|.
name|await
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|//there can be only either one failure that causes the request to fail straightaway or success
name|assertThat
argument_list|(
name|preSendFailures
operator|.
name|get
argument_list|()
operator|+
name|iteration
operator|.
name|transport
operator|.
name|failures
argument_list|()
operator|+
name|iteration
operator|.
name|transport
operator|.
name|successes
argument_list|()
argument_list|,
name|lessThanOrEqualTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|iteration
operator|.
name|transport
operator|.
name|successes
argument_list|()
operator|==
literal|1
condition|)
block|{
name|assertThat
argument_list|(
name|finalFailures
operator|.
name|get
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
name|finalFailure
operator|.
name|get
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|get
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|finalFailures
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
name|finalFailure
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
name|response
operator|.
name|get
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|preSendFailures
operator|.
name|get
argument_list|()
operator|==
literal|0
operator|&&
name|iteration
operator|.
name|transport
operator|.
name|failures
argument_list|()
operator|==
literal|0
condition|)
block|{
name|assertThat
argument_list|(
name|finalFailure
operator|.
name|get
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|NoNodeAvailableException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|iteration
operator|.
name|transport
operator|.
name|triedNodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|iteration
operator|.
name|nodesCount
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|iteration
operator|.
name|transport
operator|.
name|triedNodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|iteration
operator|.
name|transport
operator|.
name|connectTransportExceptions
argument_list|()
operator|+
name|iteration
operator|.
name|transport
operator|.
name|failures
argument_list|()
operator|+
name|iteration
operator|.
name|transport
operator|.
name|successes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|TestRequest
specifier|public
specifier|static
class|class
name|TestRequest
extends|extends
name|TransportRequest
block|{      }
DECL|class|TestResponse
specifier|private
specifier|static
class|class
name|TestResponse
extends|extends
name|TransportResponse
block|{      }
block|}
end_class

end_unit

