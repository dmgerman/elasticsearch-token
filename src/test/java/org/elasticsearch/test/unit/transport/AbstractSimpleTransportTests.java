begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.unit.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|io
operator|.
name|stream
operator|.
name|VoidStreamable
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
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportRequestOptions
operator|.
name|options
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
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
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AbstractSimpleTransportTests
specifier|public
specifier|abstract
class|class
name|AbstractSimpleTransportTests
block|{
DECL|field|threadPool
specifier|protected
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|serviceA
specifier|protected
name|TransportService
name|serviceA
decl_stmt|;
DECL|field|serviceB
specifier|protected
name|TransportService
name|serviceB
decl_stmt|;
DECL|field|serviceANode
specifier|protected
name|DiscoveryNode
name|serviceANode
decl_stmt|;
DECL|field|serviceBNode
specifier|protected
name|DiscoveryNode
name|serviceBNode
decl_stmt|;
annotation|@
name|BeforeMethod
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|threadPool
operator|=
operator|new
name|ThreadPool
argument_list|()
expr_stmt|;
name|build
argument_list|()
expr_stmt|;
name|serviceA
operator|.
name|connectToNode
argument_list|(
name|serviceBNode
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|connectToNode
argument_list|(
name|serviceANode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterMethod
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|serviceA
operator|.
name|close
argument_list|()
expr_stmt|;
name|serviceB
operator|.
name|close
argument_list|()
expr_stmt|;
name|threadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
DECL|method|build
specifier|protected
specifier|abstract
name|void
name|build
parameter_list|()
function_decl|;
annotation|@
name|Test
DECL|method|testHelloWorld
specifier|public
name|void
name|testHelloWorld
parameter_list|()
block|{
name|serviceA
operator|.
name|registerHandler
argument_list|(
literal|"sayHello"
argument_list|,
operator|new
name|BaseTransportRequestHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|StringMessage
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"moshe"
argument_list|,
name|equalTo
argument_list|(
name|request
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|StringMessage
argument_list|(
literal|"hello "
operator|+
name|request
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|TransportFuture
argument_list|<
name|StringMessage
argument_list|>
name|res
init|=
name|serviceB
operator|.
name|submitRequest
argument_list|(
name|serviceANode
argument_list|,
literal|"sayHello"
argument_list|,
operator|new
name|StringMessage
argument_list|(
literal|"moshe"
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|StringMessage
name|response
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"hello moshe"
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|message
argument_list|)
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
name|exp
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"got exception instead of a response: "
operator|+
name|exp
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|StringMessage
name|message
init|=
name|res
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"hello moshe"
argument_list|,
name|equalTo
argument_list|(
name|message
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serviceA
operator|.
name|removeHandler
argument_list|(
literal|"sayHello"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testVoidMessageCompressed
specifier|public
name|void
name|testVoidMessageCompressed
parameter_list|()
block|{
name|serviceA
operator|.
name|registerHandler
argument_list|(
literal|"sayHello"
argument_list|,
operator|new
name|BaseTransportRequestHandler
argument_list|<
name|VoidStreamable
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VoidStreamable
name|newInstance
parameter_list|()
block|{
return|return
name|VoidStreamable
operator|.
name|INSTANCE
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|VoidStreamable
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|VoidStreamable
operator|.
name|INSTANCE
argument_list|,
name|TransportResponseOptions
operator|.
name|options
argument_list|()
operator|.
name|withCompress
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|TransportFuture
argument_list|<
name|VoidStreamable
argument_list|>
name|res
init|=
name|serviceB
operator|.
name|submitRequest
argument_list|(
name|serviceANode
argument_list|,
literal|"sayHello"
argument_list|,
name|VoidStreamable
operator|.
name|INSTANCE
argument_list|,
name|TransportRequestOptions
operator|.
name|options
argument_list|()
operator|.
name|withCompress
argument_list|(
literal|true
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|VoidStreamable
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VoidStreamable
name|newInstance
parameter_list|()
block|{
return|return
name|VoidStreamable
operator|.
name|INSTANCE
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|VoidStreamable
name|response
parameter_list|)
block|{             }
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
name|exp
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"got exception instead of a response: "
operator|+
name|exp
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|VoidStreamable
name|message
init|=
name|res
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serviceA
operator|.
name|removeHandler
argument_list|(
literal|"sayHello"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testHelloWorldCompressed
specifier|public
name|void
name|testHelloWorldCompressed
parameter_list|()
block|{
name|serviceA
operator|.
name|registerHandler
argument_list|(
literal|"sayHello"
argument_list|,
operator|new
name|BaseTransportRequestHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|StringMessage
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"moshe"
argument_list|,
name|equalTo
argument_list|(
name|request
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|StringMessage
argument_list|(
literal|"hello "
operator|+
name|request
operator|.
name|message
argument_list|)
argument_list|,
name|TransportResponseOptions
operator|.
name|options
argument_list|()
operator|.
name|withCompress
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|TransportFuture
argument_list|<
name|StringMessage
argument_list|>
name|res
init|=
name|serviceB
operator|.
name|submitRequest
argument_list|(
name|serviceANode
argument_list|,
literal|"sayHello"
argument_list|,
operator|new
name|StringMessage
argument_list|(
literal|"moshe"
argument_list|)
argument_list|,
name|TransportRequestOptions
operator|.
name|options
argument_list|()
operator|.
name|withCompress
argument_list|(
literal|true
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|StringMessage
name|response
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"hello moshe"
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|message
argument_list|)
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
name|exp
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"got exception instead of a response: "
operator|+
name|exp
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|StringMessage
name|message
init|=
name|res
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"hello moshe"
argument_list|,
name|equalTo
argument_list|(
name|message
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serviceA
operator|.
name|removeHandler
argument_list|(
literal|"sayHello"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testErrorMessage
specifier|public
name|void
name|testErrorMessage
parameter_list|()
block|{
name|serviceA
operator|.
name|registerHandler
argument_list|(
literal|"sayHelloException"
argument_list|,
operator|new
name|BaseTransportRequestHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|StringMessage
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|assertThat
argument_list|(
literal|"moshe"
argument_list|,
name|equalTo
argument_list|(
name|request
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"bad message !!!"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
name|TransportFuture
argument_list|<
name|StringMessage
argument_list|>
name|res
init|=
name|serviceB
operator|.
name|submitRequest
argument_list|(
name|serviceANode
argument_list|,
literal|"sayHelloException"
argument_list|,
operator|new
name|StringMessage
argument_list|(
literal|"moshe"
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|StringMessage
name|response
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"got response instead of exception"
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
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
name|assertThat
argument_list|(
literal|"bad message !!!"
argument_list|,
name|equalTo
argument_list|(
name|exp
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|res
operator|.
name|txGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"exception should be thrown"
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"bad message !!!"
argument_list|,
name|equalTo
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serviceA
operator|.
name|removeHandler
argument_list|(
literal|"sayHelloException"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDisconnectListener
specifier|public
name|void
name|testDisconnectListener
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
literal|1
argument_list|)
decl_stmt|;
name|TransportConnectionListener
name|disconnectListener
init|=
operator|new
name|TransportConnectionListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onNodeConnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Should not be called"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onNodeDisconnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|serviceA
operator|.
name|addConnectionListener
argument_list|(
name|disconnectListener
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|latch
operator|.
name|await
argument_list|(
literal|5
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
block|}
annotation|@
name|Test
DECL|method|testTimeoutSendExceptionWithNeverSendingBackResponse
specifier|public
name|void
name|testTimeoutSendExceptionWithNeverSendingBackResponse
parameter_list|()
throws|throws
name|Exception
block|{
name|serviceA
operator|.
name|registerHandler
argument_list|(
literal|"sayHelloTimeoutNoResponse"
argument_list|,
operator|new
name|BaseTransportRequestHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|StringMessage
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"moshe"
argument_list|,
name|equalTo
argument_list|(
name|request
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
comment|// don't send back a response
comment|//                try {
comment|//                    channel.sendResponse(new StringMessage("hello " + request.message));
comment|//                } catch (IOException e) {
comment|//                    e.printStackTrace();
comment|//                    assertThat(e.getMessage(), false, equalTo(true));
comment|//                }
block|}
block|}
argument_list|)
expr_stmt|;
name|TransportFuture
argument_list|<
name|StringMessage
argument_list|>
name|res
init|=
name|serviceB
operator|.
name|submitRequest
argument_list|(
name|serviceANode
argument_list|,
literal|"sayHelloTimeoutNoResponse"
argument_list|,
operator|new
name|StringMessage
argument_list|(
literal|"moshe"
argument_list|)
argument_list|,
name|options
argument_list|()
operator|.
name|withTimeout
argument_list|(
literal|100
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|StringMessage
name|response
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"got response instead of exception"
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
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
name|assertThat
argument_list|(
name|exp
argument_list|,
name|instanceOf
argument_list|(
name|ReceiveTimeoutTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|StringMessage
name|message
init|=
name|res
operator|.
name|txGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"exception should be thrown"
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
argument_list|,
name|instanceOf
argument_list|(
name|ReceiveTimeoutTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serviceA
operator|.
name|removeHandler
argument_list|(
literal|"sayHelloTimeoutNoResponse"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testTimeoutSendExceptionWithDelayedResponse
specifier|public
name|void
name|testTimeoutSendExceptionWithDelayedResponse
parameter_list|()
throws|throws
name|Exception
block|{
name|serviceA
operator|.
name|registerHandler
argument_list|(
literal|"sayHelloTimeoutDelayedResponse"
argument_list|,
operator|new
name|BaseTransportRequestHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|StringMessage
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
block|{
name|TimeValue
name|sleep
init|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|request
operator|.
name|message
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleep
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
comment|// ignore
block|}
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|StringMessage
argument_list|(
literal|"hello "
operator|+
name|request
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|TransportFuture
argument_list|<
name|StringMessage
argument_list|>
name|res
init|=
name|serviceB
operator|.
name|submitRequest
argument_list|(
name|serviceANode
argument_list|,
literal|"sayHelloTimeoutDelayedResponse"
argument_list|,
operator|new
name|StringMessage
argument_list|(
literal|"300ms"
argument_list|)
argument_list|,
name|options
argument_list|()
operator|.
name|withTimeout
argument_list|(
literal|100
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|StringMessage
name|response
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"got response instead of exception"
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
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
name|assertThat
argument_list|(
name|exp
argument_list|,
name|instanceOf
argument_list|(
name|ReceiveTimeoutTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|StringMessage
name|message
init|=
name|res
operator|.
name|txGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"exception should be thrown"
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
argument_list|,
name|instanceOf
argument_list|(
name|ReceiveTimeoutTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// sleep for 400 millis to make sure we get back the response
name|Thread
operator|.
name|sleep
argument_list|(
literal|400
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|counter
init|=
name|i
decl_stmt|;
comment|// now, try and send another request, this times, with a short timeout
name|res
operator|=
name|serviceB
operator|.
name|submitRequest
argument_list|(
name|serviceANode
argument_list|,
literal|"sayHelloTimeoutDelayedResponse"
argument_list|,
operator|new
name|StringMessage
argument_list|(
name|counter
operator|+
literal|"ms"
argument_list|)
argument_list|,
name|options
argument_list|()
operator|.
name|withTimeout
argument_list|(
literal|100
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|StringMessage
name|response
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"hello "
operator|+
name|counter
operator|+
literal|"ms"
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|message
argument_list|)
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
name|exp
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"got exception instead of a response for "
operator|+
name|counter
operator|+
literal|": "
operator|+
name|exp
operator|.
name|getDetailedMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|StringMessage
name|message
init|=
name|res
operator|.
name|txGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|message
operator|.
name|message
argument_list|,
name|equalTo
argument_list|(
literal|"hello "
operator|+
name|counter
operator|+
literal|"ms"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serviceA
operator|.
name|removeHandler
argument_list|(
literal|"sayHelloTimeoutDelayedResponse"
argument_list|)
expr_stmt|;
block|}
DECL|class|StringMessage
specifier|private
class|class
name|StringMessage
implements|implements
name|Streamable
block|{
DECL|field|message
specifier|private
name|String
name|message
decl_stmt|;
DECL|method|StringMessage
specifier|private
name|StringMessage
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|this
operator|.
name|message
operator|=
name|message
expr_stmt|;
block|}
DECL|method|StringMessage
specifier|private
name|StringMessage
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|message
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

