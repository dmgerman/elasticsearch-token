begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http.netty4
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty4
package|;
end_package

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ByteBuf
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|Unpooled
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channel
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelHandler
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelHandlerContext
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelPromise
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|SimpleChannelInboundHandler
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|DefaultFullHttpResponse
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|FullHttpRequest
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|FullHttpResponse
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpHeaderNames
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpResponseStatus
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpVersion
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
name|network
operator|.
name|NetworkService
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
name|TransportAddress
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
name|MockBigArrays
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
name|ThreadContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|HttpServerTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|NullDispatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty4
operator|.
name|pipelining
operator|.
name|HttpPipelinedRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|breaker
operator|.
name|NoneCircuitBreakerService
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
name|Before
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|Collection
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
name|ExecutorService
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
name|Executors
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|RegexMatcher
operator|.
name|matches
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
name|Matchers
operator|.
name|contains
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
name|hasSize
import|;
end_import

begin_comment
comment|/**  * This test just tests, if he pipelining works in general with out any connection the Elasticsearch handler  */
end_comment

begin_class
DECL|class|Netty4HttpServerPipeliningTests
specifier|public
class|class
name|Netty4HttpServerPipeliningTests
extends|extends
name|ESTestCase
block|{
DECL|field|networkService
specifier|private
name|NetworkService
name|networkService
decl_stmt|;
DECL|field|threadPool
specifier|private
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|bigArrays
specifier|private
name|MockBigArrays
name|bigArrays
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|networkService
operator|=
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
name|threadPool
operator|=
operator|new
name|TestThreadPool
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|bigArrays
operator|=
operator|new
name|MockBigArrays
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|NoneCircuitBreakerService
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
block|}
block|}
DECL|method|testThatHttpPipeliningWorksWhenEnabled
specifier|public
name|void
name|testThatHttpPipeliningWorksWhenEnabled
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"http.pipelining"
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
literal|"http.port"
argument_list|,
literal|"0"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
init|(
name|HttpServerTransport
name|httpServerTransport
init|=
operator|new
name|CustomNettyHttpServerTransport
argument_list|(
name|settings
argument_list|)
init|)
block|{
name|httpServerTransport
operator|.
name|start
argument_list|()
expr_stmt|;
specifier|final
name|TransportAddress
name|transportAddress
init|=
name|randomFrom
argument_list|(
name|httpServerTransport
operator|.
name|boundAddress
argument_list|()
operator|.
name|boundAddresses
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numberOfRequests
init|=
name|randomIntBetween
argument_list|(
literal|4
argument_list|,
literal|16
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|requests
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numberOfRequests
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
name|numberOfRequests
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|requests
operator|.
name|add
argument_list|(
literal|"/slow/"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|requests
operator|.
name|add
argument_list|(
literal|"/"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
try|try
init|(
name|Netty4HttpClient
name|nettyHttpClient
init|=
operator|new
name|Netty4HttpClient
argument_list|()
init|)
block|{
name|Collection
argument_list|<
name|FullHttpResponse
argument_list|>
name|responses
init|=
name|nettyHttpClient
operator|.
name|get
argument_list|(
name|transportAddress
operator|.
name|address
argument_list|()
argument_list|,
name|requests
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[]
block|{}
argument_list|)
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|responseBodies
init|=
name|Netty4HttpClient
operator|.
name|returnHttpResponseBodies
argument_list|(
name|responses
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|responseBodies
argument_list|,
name|contains
argument_list|(
name|requests
operator|.
name|toArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testThatHttpPipeliningCanBeDisabled
specifier|public
name|void
name|testThatHttpPipeliningCanBeDisabled
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"http.pipelining"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"http.port"
argument_list|,
literal|"0"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
init|(
name|HttpServerTransport
name|httpServerTransport
init|=
operator|new
name|CustomNettyHttpServerTransport
argument_list|(
name|settings
argument_list|)
init|)
block|{
name|httpServerTransport
operator|.
name|start
argument_list|()
expr_stmt|;
specifier|final
name|TransportAddress
name|transportAddress
init|=
name|randomFrom
argument_list|(
name|httpServerTransport
operator|.
name|boundAddress
argument_list|()
operator|.
name|boundAddresses
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numberOfRequests
init|=
name|randomIntBetween
argument_list|(
literal|4
argument_list|,
literal|16
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|Integer
argument_list|>
name|slowIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|requests
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numberOfRequests
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
name|numberOfRequests
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|requests
operator|.
name|add
argument_list|(
literal|"/slow/"
operator|+
name|i
argument_list|)
expr_stmt|;
name|slowIds
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|requests
operator|.
name|add
argument_list|(
literal|"/"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
try|try
init|(
name|Netty4HttpClient
name|nettyHttpClient
init|=
operator|new
name|Netty4HttpClient
argument_list|()
init|)
block|{
name|Collection
argument_list|<
name|FullHttpResponse
argument_list|>
name|responses
init|=
name|nettyHttpClient
operator|.
name|get
argument_list|(
name|transportAddress
operator|.
name|address
argument_list|()
argument_list|,
name|requests
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[]
block|{}
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|responseBodies
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Netty4HttpClient
operator|.
name|returnHttpResponseBodies
argument_list|(
name|responses
argument_list|)
argument_list|)
decl_stmt|;
comment|// we can not be sure about the order of the responses, but the slow ones should come last
name|assertThat
argument_list|(
name|responseBodies
argument_list|,
name|hasSize
argument_list|(
name|numberOfRequests
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
name|numberOfRequests
operator|-
name|slowIds
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|responseBodies
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|matches
argument_list|(
literal|"/\\d+"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Set
argument_list|<
name|Integer
argument_list|>
name|ids
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
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
name|slowIds
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|String
name|response
init|=
name|responseBodies
operator|.
name|get
argument_list|(
name|numberOfRequests
operator|-
name|slowIds
operator|.
name|size
argument_list|()
operator|+
name|i
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
argument_list|,
name|matches
argument_list|(
literal|"/slow/\\d+"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ids
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|response
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
index|[
literal|2
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|slowIds
argument_list|,
name|equalTo
argument_list|(
name|ids
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|CustomNettyHttpServerTransport
class|class
name|CustomNettyHttpServerTransport
extends|extends
name|Netty4HttpServerTransport
block|{
DECL|field|executorService
specifier|private
specifier|final
name|ExecutorService
name|executorService
init|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|()
decl_stmt|;
DECL|method|CustomNettyHttpServerTransport
name|CustomNettyHttpServerTransport
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|Netty4HttpServerPipeliningTests
operator|.
name|this
operator|.
name|networkService
argument_list|,
name|Netty4HttpServerPipeliningTests
operator|.
name|this
operator|.
name|bigArrays
argument_list|,
name|Netty4HttpServerPipeliningTests
operator|.
name|this
operator|.
name|threadPool
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
operator|new
name|NullDispatcher
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configureServerChannelHandler
specifier|public
name|ChannelHandler
name|configureServerChannelHandler
parameter_list|()
block|{
return|return
operator|new
name|CustomHttpChannelHandler
argument_list|(
name|this
argument_list|,
name|executorService
argument_list|,
name|Netty4HttpServerPipeliningTests
operator|.
name|this
operator|.
name|threadPool
operator|.
name|getThreadContext
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{
name|executorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|super
operator|.
name|doClose
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|CustomHttpChannelHandler
specifier|private
class|class
name|CustomHttpChannelHandler
extends|extends
name|Netty4HttpServerTransport
operator|.
name|HttpChannelHandler
block|{
DECL|field|executorService
specifier|private
specifier|final
name|ExecutorService
name|executorService
decl_stmt|;
DECL|method|CustomHttpChannelHandler
name|CustomHttpChannelHandler
parameter_list|(
name|Netty4HttpServerTransport
name|transport
parameter_list|,
name|ExecutorService
name|executorService
parameter_list|,
name|ThreadContext
name|threadContext
parameter_list|)
block|{
name|super
argument_list|(
name|transport
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|threadContext
argument_list|)
expr_stmt|;
name|this
operator|.
name|executorService
operator|=
name|executorService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|initChannel
specifier|protected
name|void
name|initChannel
parameter_list|(
name|Channel
name|ch
parameter_list|)
throws|throws
name|Exception
block|{
name|super
operator|.
name|initChannel
argument_list|(
name|ch
argument_list|)
expr_stmt|;
name|ch
operator|.
name|pipeline
argument_list|()
operator|.
name|replace
argument_list|(
literal|"handler"
argument_list|,
literal|"handler"
argument_list|,
operator|new
name|PossiblySlowUpstreamHandler
argument_list|(
name|executorService
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|PossiblySlowUpstreamHandler
class|class
name|PossiblySlowUpstreamHandler
extends|extends
name|SimpleChannelInboundHandler
argument_list|<
name|Object
argument_list|>
block|{
DECL|field|executorService
specifier|private
specifier|final
name|ExecutorService
name|executorService
decl_stmt|;
DECL|method|PossiblySlowUpstreamHandler
name|PossiblySlowUpstreamHandler
parameter_list|(
name|ExecutorService
name|executorService
parameter_list|)
block|{
name|this
operator|.
name|executorService
operator|=
name|executorService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|channelRead0
specifier|protected
name|void
name|channelRead0
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|)
throws|throws
name|Exception
block|{
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|PossiblySlowRunnable
argument_list|(
name|ctx
argument_list|,
name|msg
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|exceptionCaught
specifier|public
name|void
name|exceptionCaught
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Throwable
name|cause
parameter_list|)
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Caught exception"
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|channel
argument_list|()
operator|.
name|close
argument_list|()
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|PossiblySlowRunnable
class|class
name|PossiblySlowRunnable
implements|implements
name|Runnable
block|{
DECL|field|ctx
specifier|private
name|ChannelHandlerContext
name|ctx
decl_stmt|;
DECL|field|pipelinedRequest
specifier|private
name|HttpPipelinedRequest
name|pipelinedRequest
decl_stmt|;
DECL|field|fullHttpRequest
specifier|private
name|FullHttpRequest
name|fullHttpRequest
decl_stmt|;
DECL|method|PossiblySlowRunnable
name|PossiblySlowRunnable
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|)
block|{
name|this
operator|.
name|ctx
operator|=
name|ctx
expr_stmt|;
if|if
condition|(
name|msg
operator|instanceof
name|HttpPipelinedRequest
condition|)
block|{
name|this
operator|.
name|pipelinedRequest
operator|=
operator|(
name|HttpPipelinedRequest
operator|)
name|msg
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|msg
operator|instanceof
name|FullHttpRequest
condition|)
block|{
name|this
operator|.
name|fullHttpRequest
operator|=
operator|(
name|FullHttpRequest
operator|)
name|msg
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
specifier|final
name|String
name|uri
decl_stmt|;
if|if
condition|(
name|pipelinedRequest
operator|!=
literal|null
operator|&&
name|pipelinedRequest
operator|.
name|last
argument_list|()
operator|instanceof
name|FullHttpRequest
condition|)
block|{
name|uri
operator|=
operator|(
operator|(
name|FullHttpRequest
operator|)
name|pipelinedRequest
operator|.
name|last
argument_list|()
operator|)
operator|.
name|uri
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|uri
operator|=
name|fullHttpRequest
operator|.
name|uri
argument_list|()
expr_stmt|;
block|}
specifier|final
name|ByteBuf
name|buffer
init|=
name|Unpooled
operator|.
name|copiedBuffer
argument_list|(
name|uri
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
specifier|final
name|DefaultFullHttpResponse
name|httpResponse
init|=
operator|new
name|DefaultFullHttpResponse
argument_list|(
name|HttpVersion
operator|.
name|HTTP_1_1
argument_list|,
name|HttpResponseStatus
operator|.
name|OK
argument_list|,
name|buffer
argument_list|)
decl_stmt|;
name|httpResponse
operator|.
name|headers
argument_list|()
operator|.
name|add
argument_list|(
name|HttpHeaderNames
operator|.
name|CONTENT_LENGTH
argument_list|,
name|buffer
operator|.
name|readableBytes
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|slow
init|=
name|uri
operator|.
name|matches
argument_list|(
literal|"/slow/\\d+"
argument_list|)
decl_stmt|;
if|if
condition|(
name|slow
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|500
argument_list|,
literal|1000
argument_list|)
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
assert|assert
name|uri
operator|.
name|matches
argument_list|(
literal|"/\\d+"
argument_list|)
assert|;
block|}
specifier|final
name|ChannelPromise
name|promise
init|=
name|ctx
operator|.
name|newPromise
argument_list|()
decl_stmt|;
specifier|final
name|Object
name|msg
decl_stmt|;
if|if
condition|(
name|pipelinedRequest
operator|!=
literal|null
condition|)
block|{
name|msg
operator|=
name|pipelinedRequest
operator|.
name|createHttpResponse
argument_list|(
name|httpResponse
argument_list|,
name|promise
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|msg
operator|=
name|httpResponse
expr_stmt|;
block|}
name|ctx
operator|.
name|writeAndFlush
argument_list|(
name|msg
argument_list|,
name|promise
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

