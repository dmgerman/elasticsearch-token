begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty
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
name|base
operator|.
name|Charsets
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|recycler
operator|.
name|MockPageCacheRecycler
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
name|InetSocketTransportAddress
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
name|netty
operator|.
name|pipelining
operator|.
name|OrderedDownstreamChannelEvent
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
name|netty
operator|.
name|pipelining
operator|.
name|OrderedUpstreamMessageEvent
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
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ChannelBuffer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ChannelBuffers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
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
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelPipeline
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelPipelineFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|ExceptionEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|MessageEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|SimpleChannelUpstreamHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|DefaultHttpResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|QueryStringDecoder
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty
operator|.
name|NettyHttpClient
operator|.
name|returnHttpResponseBodies
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty
operator|.
name|NettyHttpServerTransport
operator|.
name|HttpChannelPipelineFactory
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

begin_import
import|import static
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpHeaders
operator|.
name|Names
operator|.
name|CONTENT_LENGTH
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|jboss
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
operator|.
name|OK
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|jboss
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
operator|.
name|HTTP_1_1
import|;
end_import

begin_comment
comment|/**  * This test just tests, if he pipelining works in general with out any connection the elasticsearch handler  */
end_comment

begin_class
DECL|class|NettyHttpServerPipeliningTest
specifier|public
class|class
name|NettyHttpServerPipeliningTest
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
DECL|field|mockPageCacheRecycler
specifier|private
name|MockPageCacheRecycler
name|mockPageCacheRecycler
decl_stmt|;
DECL|field|bigArrays
specifier|private
name|MockBigArrays
name|bigArrays
decl_stmt|;
DECL|field|httpServerTransport
specifier|private
name|CustomNettyHttpServerTransport
name|httpServerTransport
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
argument_list|)
expr_stmt|;
name|threadPool
operator|=
operator|new
name|ThreadPool
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|mockPageCacheRecycler
operator|=
operator|new
name|MockPageCacheRecycler
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|bigArrays
operator|=
operator|new
name|MockBigArrays
argument_list|(
name|mockPageCacheRecycler
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
if|if
condition|(
name|httpServerTransport
operator|!=
literal|null
condition|)
block|{
name|httpServerTransport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testThatHttpPipeliningWorksWhenEnabled
specifier|public
name|void
name|testThatHttpPipeliningWorksWhenEnabled
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"http.pipelining"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|httpServerTransport
operator|=
operator|new
name|CustomNettyHttpServerTransport
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|httpServerTransport
operator|.
name|start
argument_list|()
expr_stmt|;
name|InetSocketTransportAddress
name|transportAddress
init|=
operator|(
name|InetSocketTransportAddress
operator|)
name|httpServerTransport
operator|.
name|boundAddress
argument_list|()
operator|.
name|boundAddress
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|requests
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"/firstfast"
argument_list|,
literal|"/slow?sleep=500"
argument_list|,
literal|"/secondfast"
argument_list|,
literal|"/slow?sleep=1000"
argument_list|,
literal|"/thirdfast"
argument_list|)
decl_stmt|;
try|try
init|(
name|NettyHttpClient
name|nettyHttpClient
init|=
operator|new
name|NettyHttpClient
argument_list|()
init|)
block|{
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|responses
init|=
name|nettyHttpClient
operator|.
name|sendRequests
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
literal|"/firstfast"
argument_list|,
literal|"/slow?sleep=500"
argument_list|,
literal|"/secondfast"
argument_list|,
literal|"/slow?sleep=1000"
argument_list|,
literal|"/thirdfast"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testThatHttpPipeliningCanBeDisabled
specifier|public
name|void
name|testThatHttpPipeliningCanBeDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"http.pipelining"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|httpServerTransport
operator|=
operator|new
name|CustomNettyHttpServerTransport
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|httpServerTransport
operator|.
name|start
argument_list|()
expr_stmt|;
name|InetSocketTransportAddress
name|transportAddress
init|=
operator|(
name|InetSocketTransportAddress
operator|)
name|httpServerTransport
operator|.
name|boundAddress
argument_list|()
operator|.
name|boundAddress
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|requests
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"/slow?sleep=1000"
argument_list|,
literal|"/firstfast"
argument_list|,
literal|"/secondfast"
argument_list|,
literal|"/thirdfast"
argument_list|,
literal|"/slow?sleep=500"
argument_list|)
decl_stmt|;
try|try
init|(
name|NettyHttpClient
name|nettyHttpClient
init|=
operator|new
name|NettyHttpClient
argument_list|()
init|)
block|{
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|responses
init|=
name|nettyHttpClient
operator|.
name|sendRequests
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
name|returnHttpResponseBodies
argument_list|(
name|responses
argument_list|)
argument_list|)
decl_stmt|;
comment|// we cannot be sure about the order of the fast requests, but the slow ones should have to be last
name|assertThat
argument_list|(
name|responseBodies
argument_list|,
name|hasSize
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|responseBodies
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"/slow?sleep=500"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|responseBodies
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"/slow?sleep=1000"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|CustomNettyHttpServerTransport
class|class
name|CustomNettyHttpServerTransport
extends|extends
name|NettyHttpServerTransport
block|{
DECL|field|executorService
specifier|private
specifier|final
name|ExecutorService
name|executorService
decl_stmt|;
DECL|method|CustomNettyHttpServerTransport
specifier|public
name|CustomNettyHttpServerTransport
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|NettyHttpServerPipeliningTest
operator|.
name|this
operator|.
name|networkService
argument_list|,
name|NettyHttpServerPipeliningTest
operator|.
name|this
operator|.
name|bigArrays
argument_list|)
expr_stmt|;
name|this
operator|.
name|executorService
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configureServerChannelPipelineFactory
specifier|public
name|ChannelPipelineFactory
name|configureServerChannelPipelineFactory
parameter_list|()
block|{
return|return
operator|new
name|CustomHttpChannelPipelineFactory
argument_list|(
name|this
argument_list|,
name|executorService
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|stop
specifier|public
name|HttpServerTransport
name|stop
parameter_list|()
block|{
name|executorService
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
return|return
name|super
operator|.
name|stop
argument_list|()
return|;
block|}
block|}
DECL|class|CustomHttpChannelPipelineFactory
specifier|private
class|class
name|CustomHttpChannelPipelineFactory
extends|extends
name|HttpChannelPipelineFactory
block|{
DECL|field|executorService
specifier|private
specifier|final
name|ExecutorService
name|executorService
decl_stmt|;
DECL|method|CustomHttpChannelPipelineFactory
specifier|public
name|CustomHttpChannelPipelineFactory
parameter_list|(
name|NettyHttpServerTransport
name|transport
parameter_list|,
name|ExecutorService
name|executorService
parameter_list|)
block|{
name|super
argument_list|(
name|transport
argument_list|,
name|randomBoolean
argument_list|()
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
DECL|method|getPipeline
specifier|public
name|ChannelPipeline
name|getPipeline
parameter_list|()
throws|throws
name|Exception
block|{
name|ChannelPipeline
name|pipeline
init|=
name|super
operator|.
name|getPipeline
argument_list|()
decl_stmt|;
name|pipeline
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
return|return
name|pipeline
return|;
block|}
block|}
DECL|class|PossiblySlowUpstreamHandler
class|class
name|PossiblySlowUpstreamHandler
extends|extends
name|SimpleChannelUpstreamHandler
block|{
DECL|field|executorService
specifier|private
specifier|final
name|ExecutorService
name|executorService
decl_stmt|;
DECL|method|PossiblySlowUpstreamHandler
specifier|public
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
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
specifier|final
name|ChannelHandlerContext
name|ctx
parameter_list|,
specifier|final
name|MessageEvent
name|e
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
name|e
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
name|ExceptionEvent
name|e
parameter_list|)
block|{
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|e
operator|.
name|getChannel
argument_list|()
operator|.
name|close
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
DECL|field|e
specifier|private
name|MessageEvent
name|e
decl_stmt|;
DECL|method|PossiblySlowRunnable
specifier|public
name|PossiblySlowRunnable
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|MessageEvent
name|e
parameter_list|)
block|{
name|this
operator|.
name|ctx
operator|=
name|ctx
expr_stmt|;
name|this
operator|.
name|e
operator|=
name|e
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
name|HttpRequest
name|request
decl_stmt|;
name|OrderedUpstreamMessageEvent
name|oue
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|OrderedUpstreamMessageEvent
condition|)
block|{
name|oue
operator|=
operator|(
name|OrderedUpstreamMessageEvent
operator|)
name|e
expr_stmt|;
name|request
operator|=
operator|(
name|HttpRequest
operator|)
name|oue
operator|.
name|getMessage
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|request
operator|=
operator|(
name|HttpRequest
operator|)
name|e
operator|.
name|getMessage
argument_list|()
expr_stmt|;
block|}
name|ChannelBuffer
name|buffer
init|=
name|ChannelBuffers
operator|.
name|copiedBuffer
argument_list|(
name|request
operator|.
name|getUri
argument_list|()
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|DefaultHttpResponse
name|httpResponse
init|=
operator|new
name|DefaultHttpResponse
argument_list|(
name|HTTP_1_1
argument_list|,
name|OK
argument_list|)
decl_stmt|;
name|httpResponse
operator|.
name|headers
argument_list|()
operator|.
name|add
argument_list|(
name|CONTENT_LENGTH
argument_list|,
name|buffer
operator|.
name|readableBytes
argument_list|()
argument_list|)
expr_stmt|;
name|httpResponse
operator|.
name|setContent
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|QueryStringDecoder
name|decoder
init|=
operator|new
name|QueryStringDecoder
argument_list|(
name|request
operator|.
name|getUri
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|timeout
init|=
name|request
operator|.
name|getUri
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"/slow"
argument_list|)
operator|&&
name|decoder
operator|.
name|getParameters
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"sleep"
argument_list|)
condition|?
name|Integer
operator|.
name|valueOf
argument_list|(
name|decoder
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
literal|"sleep"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
else|:
literal|0
decl_stmt|;
if|if
condition|(
name|timeout
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
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
throw|throw
operator|new
name|RuntimeException
argument_list|()
throw|;
block|}
block|}
if|if
condition|(
name|oue
operator|!=
literal|null
condition|)
block|{
name|ctx
operator|.
name|sendDownstream
argument_list|(
operator|new
name|OrderedDownstreamChannelEvent
argument_list|(
name|oue
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
name|httpResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ctx
operator|.
name|getChannel
argument_list|()
operator|.
name|write
argument_list|(
name|httpResponse
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

