begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|IOUtils
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
name|ExceptionsHelper
import|;
end_import

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
name|test
operator|.
name|transport
operator|.
name|MockTransportService
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
name|Before
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

begin_class
DECL|class|TransportActionProxyTests
specifier|public
class|class
name|TransportActionProxyTests
extends|extends
name|ESTestCase
block|{
DECL|field|threadPool
specifier|protected
name|ThreadPool
name|threadPool
decl_stmt|;
comment|// we use always a non-alpha or beta version here otherwise minimumCompatibilityVersion will be different for the two used versions
DECL|field|CURRENT_VERSION
specifier|private
specifier|static
specifier|final
name|Version
name|CURRENT_VERSION
init|=
name|Version
operator|.
name|fromString
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|Version
operator|.
name|CURRENT
operator|.
name|major
argument_list|)
operator|+
literal|".0.0"
argument_list|)
decl_stmt|;
DECL|field|version0
specifier|protected
specifier|static
specifier|final
name|Version
name|version0
init|=
name|CURRENT_VERSION
operator|.
name|minimumCompatibilityVersion
argument_list|()
decl_stmt|;
DECL|field|nodeA
specifier|protected
name|DiscoveryNode
name|nodeA
decl_stmt|;
DECL|field|serviceA
specifier|protected
name|MockTransportService
name|serviceA
decl_stmt|;
DECL|field|version1
specifier|protected
specifier|static
specifier|final
name|Version
name|version1
init|=
name|Version
operator|.
name|fromId
argument_list|(
name|CURRENT_VERSION
operator|.
name|id
operator|+
literal|1
argument_list|)
decl_stmt|;
DECL|field|nodeB
specifier|protected
name|DiscoveryNode
name|nodeB
decl_stmt|;
DECL|field|serviceB
specifier|protected
name|MockTransportService
name|serviceB
decl_stmt|;
DECL|field|nodeC
specifier|protected
name|DiscoveryNode
name|nodeC
decl_stmt|;
DECL|field|serviceC
specifier|protected
name|MockTransportService
name|serviceC
decl_stmt|;
annotation|@
name|Override
annotation|@
name|Before
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|threadPool
operator|=
operator|new
name|TestThreadPool
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|serviceA
operator|=
name|buildService
argument_list|(
name|version0
argument_list|)
expr_stmt|;
comment|// this one supports dynamic tracer updates
name|nodeA
operator|=
name|serviceA
operator|.
name|getLocalDiscoNode
argument_list|()
expr_stmt|;
name|serviceB
operator|=
name|buildService
argument_list|(
name|version1
argument_list|)
expr_stmt|;
comment|// this one doesn't support dynamic tracer updates
name|nodeB
operator|=
name|serviceB
operator|.
name|getLocalDiscoNode
argument_list|()
expr_stmt|;
name|serviceC
operator|=
name|buildService
argument_list|(
name|version1
argument_list|)
expr_stmt|;
comment|// this one doesn't support dynamic tracer updates
name|nodeC
operator|=
name|serviceC
operator|.
name|getLocalDiscoNode
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|serviceA
argument_list|,
name|serviceB
argument_list|,
name|serviceC
argument_list|,
parameter_list|()
lambda|->
block|{
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
argument_list|)
expr_stmt|;
block|}
DECL|method|buildService
specifier|private
name|MockTransportService
name|buildService
parameter_list|(
specifier|final
name|Version
name|version
parameter_list|)
block|{
name|MockTransportService
name|service
init|=
name|MockTransportService
operator|.
name|createNewService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|version
argument_list|,
name|threadPool
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|service
operator|.
name|start
argument_list|()
expr_stmt|;
name|service
operator|.
name|acceptIncomingRequests
argument_list|()
expr_stmt|;
return|return
name|service
return|;
block|}
DECL|method|testSendMessage
specifier|public
name|void
name|testSendMessage
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|serviceA
operator|.
name|registerRequestHandler
argument_list|(
literal|"/test"
argument_list|,
name|SimpleTestRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
parameter_list|(
name|request
parameter_list|,
name|channel
parameter_list|)
lambda|->
block|{
name|assertEquals
argument_list|(
name|request
operator|.
name|sourceNode
argument_list|,
literal|"TS_A"
argument_list|)
expr_stmt|;
name|SimpleTestResponse
name|response
init|=
operator|new
name|SimpleTestResponse
argument_list|()
decl_stmt|;
name|response
operator|.
name|targetNode
operator|=
literal|"TS_A"
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|TransportActionProxy
operator|.
name|registerProxyAction
argument_list|(
name|serviceA
argument_list|,
literal|"/test"
argument_list|,
name|SimpleTestResponse
operator|::
operator|new
argument_list|)
expr_stmt|;
name|serviceA
operator|.
name|connectToNode
argument_list|(
name|nodeB
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|registerRequestHandler
argument_list|(
literal|"/test"
argument_list|,
name|SimpleTestRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
parameter_list|(
name|request
parameter_list|,
name|channel
parameter_list|)
lambda|->
block|{
name|assertEquals
argument_list|(
name|request
operator|.
name|sourceNode
argument_list|,
literal|"TS_A"
argument_list|)
expr_stmt|;
name|SimpleTestResponse
name|response
init|=
operator|new
name|SimpleTestResponse
argument_list|()
decl_stmt|;
name|response
operator|.
name|targetNode
operator|=
literal|"TS_B"
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|TransportActionProxy
operator|.
name|registerProxyAction
argument_list|(
name|serviceB
argument_list|,
literal|"/test"
argument_list|,
name|SimpleTestResponse
operator|::
operator|new
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|connectToNode
argument_list|(
name|nodeC
argument_list|)
expr_stmt|;
name|serviceC
operator|.
name|registerRequestHandler
argument_list|(
literal|"/test"
argument_list|,
name|SimpleTestRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
parameter_list|(
name|request
parameter_list|,
name|channel
parameter_list|)
lambda|->
block|{
name|assertEquals
argument_list|(
name|request
operator|.
name|sourceNode
argument_list|,
literal|"TS_A"
argument_list|)
expr_stmt|;
name|SimpleTestResponse
name|response
init|=
operator|new
name|SimpleTestResponse
argument_list|()
decl_stmt|;
name|response
operator|.
name|targetNode
operator|=
literal|"TS_C"
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|TransportActionProxy
operator|.
name|registerProxyAction
argument_list|(
name|serviceC
argument_list|,
literal|"/test"
argument_list|,
name|SimpleTestResponse
operator|::
operator|new
argument_list|)
expr_stmt|;
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|serviceA
operator|.
name|sendRequest
argument_list|(
name|nodeB
argument_list|,
name|TransportActionProxy
operator|.
name|getProxyAction
argument_list|(
literal|"/test"
argument_list|)
argument_list|,
name|TransportActionProxy
operator|.
name|wrapRequest
argument_list|(
name|nodeC
argument_list|,
operator|new
name|SimpleTestRequest
argument_list|(
literal|"TS_A"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|TransportResponseHandler
argument_list|<
name|SimpleTestResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|SimpleTestResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|SimpleTestResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|SimpleTestResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|assertEquals
argument_list|(
literal|"TS_C"
argument_list|,
name|response
operator|.
name|targetNode
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
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
try|try
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|exp
argument_list|)
throw|;
block|}
finally|finally
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
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
name|SAME
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
DECL|method|testException
specifier|public
name|void
name|testException
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|serviceA
operator|.
name|registerRequestHandler
argument_list|(
literal|"/test"
argument_list|,
name|SimpleTestRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
parameter_list|(
name|request
parameter_list|,
name|channel
parameter_list|)
lambda|->
block|{
name|assertEquals
argument_list|(
name|request
operator|.
name|sourceNode
argument_list|,
literal|"TS_A"
argument_list|)
expr_stmt|;
name|SimpleTestResponse
name|response
init|=
operator|new
name|SimpleTestResponse
argument_list|()
decl_stmt|;
name|response
operator|.
name|targetNode
operator|=
literal|"TS_A"
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|TransportActionProxy
operator|.
name|registerProxyAction
argument_list|(
name|serviceA
argument_list|,
literal|"/test"
argument_list|,
name|SimpleTestResponse
operator|::
operator|new
argument_list|)
expr_stmt|;
name|serviceA
operator|.
name|connectToNode
argument_list|(
name|nodeB
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|registerRequestHandler
argument_list|(
literal|"/test"
argument_list|,
name|SimpleTestRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
parameter_list|(
name|request
parameter_list|,
name|channel
parameter_list|)
lambda|->
block|{
name|assertEquals
argument_list|(
name|request
operator|.
name|sourceNode
argument_list|,
literal|"TS_A"
argument_list|)
expr_stmt|;
name|SimpleTestResponse
name|response
init|=
operator|new
name|SimpleTestResponse
argument_list|()
decl_stmt|;
name|response
operator|.
name|targetNode
operator|=
literal|"TS_B"
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|TransportActionProxy
operator|.
name|registerProxyAction
argument_list|(
name|serviceB
argument_list|,
literal|"/test"
argument_list|,
name|SimpleTestResponse
operator|::
operator|new
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|connectToNode
argument_list|(
name|nodeC
argument_list|)
expr_stmt|;
name|serviceC
operator|.
name|registerRequestHandler
argument_list|(
literal|"/test"
argument_list|,
name|SimpleTestRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
parameter_list|(
name|request
parameter_list|,
name|channel
parameter_list|)
lambda|->
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"greetings from TS_C"
argument_list|)
throw|;
block|}
argument_list|)
expr_stmt|;
name|TransportActionProxy
operator|.
name|registerProxyAction
argument_list|(
name|serviceC
argument_list|,
literal|"/test"
argument_list|,
name|SimpleTestResponse
operator|::
operator|new
argument_list|)
expr_stmt|;
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|serviceA
operator|.
name|sendRequest
argument_list|(
name|nodeB
argument_list|,
name|TransportActionProxy
operator|.
name|getProxyAction
argument_list|(
literal|"/test"
argument_list|)
argument_list|,
name|TransportActionProxy
operator|.
name|wrapRequest
argument_list|(
name|nodeC
argument_list|,
operator|new
name|SimpleTestRequest
argument_list|(
literal|"TS_A"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|TransportResponseHandler
argument_list|<
name|SimpleTestResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|SimpleTestResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|SimpleTestResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|SimpleTestResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|fail
argument_list|(
literal|"expected exception"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
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
try|try
block|{
name|Throwable
name|cause
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|exp
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"greetings from TS_C"
argument_list|,
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
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
name|SAME
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
DECL|class|SimpleTestRequest
specifier|public
specifier|static
class|class
name|SimpleTestRequest
extends|extends
name|TransportRequest
block|{
DECL|field|sourceNode
name|String
name|sourceNode
decl_stmt|;
DECL|method|SimpleTestRequest
specifier|public
name|SimpleTestRequest
parameter_list|(
name|String
name|sourceNode
parameter_list|)
block|{
name|this
operator|.
name|sourceNode
operator|=
name|sourceNode
expr_stmt|;
block|}
DECL|method|SimpleTestRequest
specifier|public
name|SimpleTestRequest
parameter_list|()
block|{}
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|sourceNode
operator|=
name|in
operator|.
name|readString
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|sourceNode
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SimpleTestResponse
specifier|public
specifier|static
class|class
name|SimpleTestResponse
extends|extends
name|TransportResponse
block|{
DECL|field|targetNode
name|String
name|targetNode
decl_stmt|;
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|targetNode
operator|=
name|in
operator|.
name|readString
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|targetNode
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testGetAction
specifier|public
name|void
name|testGetAction
parameter_list|()
block|{
name|String
name|action
init|=
literal|"foo/bar"
decl_stmt|;
name|String
name|proxyAction
init|=
name|TransportActionProxy
operator|.
name|getProxyAction
argument_list|(
name|action
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|proxyAction
operator|.
name|endsWith
argument_list|(
name|action
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"internal:transport/proxy/foo/bar"
argument_list|,
name|proxyAction
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnwrap
specifier|public
name|void
name|testUnwrap
parameter_list|()
block|{
name|TransportRequest
name|transportRequest
init|=
name|TransportActionProxy
operator|.
name|wrapRequest
argument_list|(
name|nodeA
argument_list|,
name|TransportService
operator|.
name|HandshakeRequest
operator|.
name|INSTANCE
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|transportRequest
operator|instanceof
name|TransportActionProxy
operator|.
name|ProxyRequest
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|TransportService
operator|.
name|HandshakeRequest
operator|.
name|INSTANCE
argument_list|,
name|TransportActionProxy
operator|.
name|unwrapRequest
argument_list|(
name|transportRequest
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

