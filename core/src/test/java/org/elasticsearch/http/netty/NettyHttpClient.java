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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|ByteSizeUnit
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
name|ByteSizeValue
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
name|bootstrap
operator|.
name|ClientBootstrap
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
name|ChannelFuture
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
name|Channels
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
name|channel
operator|.
name|socket
operator|.
name|nio
operator|.
name|NioClientSocketChannelFactory
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
name|DefaultHttpRequest
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
name|HttpChunkAggregator
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
name|HttpClientCodec
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
name|HttpMethod
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
name|net
operator|.
name|SocketAddress
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
name|HOST
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
comment|/**  * Tiny helper  */
end_comment

begin_class
DECL|class|NettyHttpClient
specifier|public
class|class
name|NettyHttpClient
implements|implements
name|Closeable
block|{
DECL|method|returnHttpResponseBodies
specifier|public
specifier|static
name|Collection
argument_list|<
name|String
argument_list|>
name|returnHttpResponseBodies
parameter_list|(
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|responses
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|responses
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HttpResponse
name|response
range|:
name|responses
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|response
operator|.
name|getContent
argument_list|()
operator|.
name|toString
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
DECL|method|returnOpaqueIds
specifier|public
specifier|static
name|Collection
argument_list|<
name|String
argument_list|>
name|returnOpaqueIds
parameter_list|(
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|responses
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|responses
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HttpResponse
name|response
range|:
name|responses
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|response
operator|.
name|headers
argument_list|()
operator|.
name|get
argument_list|(
literal|"X-Opaque-Id"
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
DECL|field|clientBootstrap
specifier|private
specifier|final
name|ClientBootstrap
name|clientBootstrap
decl_stmt|;
DECL|method|NettyHttpClient
specifier|public
name|NettyHttpClient
parameter_list|()
block|{
name|clientBootstrap
operator|=
operator|new
name|ClientBootstrap
argument_list|(
operator|new
name|NioClientSocketChannelFactory
argument_list|()
argument_list|)
expr_stmt|;
empty_stmt|;
block|}
DECL|method|sendRequests
specifier|public
specifier|synchronized
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|sendRequests
parameter_list|(
name|SocketAddress
name|remoteAddress
parameter_list|,
name|String
modifier|...
name|uris
parameter_list|)
throws|throws
name|InterruptedException
block|{
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|uris
operator|.
name|length
argument_list|)
decl_stmt|;
specifier|final
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|content
init|=
name|Collections
operator|.
name|synchronizedList
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|HttpResponse
argument_list|>
argument_list|(
name|uris
operator|.
name|length
argument_list|)
argument_list|)
decl_stmt|;
name|clientBootstrap
operator|.
name|setPipelineFactory
argument_list|(
operator|new
name|CountDownLatchPipelineFactory
argument_list|(
name|latch
argument_list|,
name|content
argument_list|)
argument_list|)
expr_stmt|;
name|ChannelFuture
name|channelFuture
init|=
literal|null
decl_stmt|;
try|try
block|{
name|channelFuture
operator|=
name|clientBootstrap
operator|.
name|connect
argument_list|(
name|remoteAddress
argument_list|)
expr_stmt|;
name|channelFuture
operator|.
name|await
argument_list|(
literal|1000
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
name|uris
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|HttpRequest
name|httpRequest
init|=
operator|new
name|DefaultHttpRequest
argument_list|(
name|HTTP_1_1
argument_list|,
name|HttpMethod
operator|.
name|GET
argument_list|,
name|uris
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|httpRequest
operator|.
name|headers
argument_list|()
operator|.
name|add
argument_list|(
name|HOST
argument_list|,
literal|"localhost"
argument_list|)
expr_stmt|;
name|httpRequest
operator|.
name|headers
argument_list|()
operator|.
name|add
argument_list|(
literal|"X-Opaque-ID"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|channelFuture
operator|.
name|getChannel
argument_list|()
operator|.
name|write
argument_list|(
name|httpRequest
argument_list|)
expr_stmt|;
block|}
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|channelFuture
operator|!=
literal|null
condition|)
block|{
name|channelFuture
operator|.
name|getChannel
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|content
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|clientBootstrap
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|clientBootstrap
operator|.
name|releaseExternalResources
argument_list|()
expr_stmt|;
block|}
comment|/**      * helper factory which adds returned data to a list and uses a count down latch to decide when done      */
DECL|class|CountDownLatchPipelineFactory
specifier|public
specifier|static
class|class
name|CountDownLatchPipelineFactory
implements|implements
name|ChannelPipelineFactory
block|{
DECL|field|latch
specifier|private
specifier|final
name|CountDownLatch
name|latch
decl_stmt|;
DECL|field|content
specifier|private
specifier|final
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|content
decl_stmt|;
DECL|method|CountDownLatchPipelineFactory
specifier|public
name|CountDownLatchPipelineFactory
parameter_list|(
name|CountDownLatch
name|latch
parameter_list|,
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|content
parameter_list|)
block|{
name|this
operator|.
name|latch
operator|=
name|latch
expr_stmt|;
name|this
operator|.
name|content
operator|=
name|content
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
specifier|final
name|int
name|maxBytes
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|100
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
operator|.
name|bytesAsInt
argument_list|()
decl_stmt|;
return|return
name|Channels
operator|.
name|pipeline
argument_list|(
operator|new
name|HttpClientCodec
argument_list|()
argument_list|,
operator|new
name|HttpChunkAggregator
argument_list|(
name|maxBytes
argument_list|)
argument_list|,
operator|new
name|SimpleChannelUpstreamHandler
argument_list|()
block|{
annotation|@
name|Override
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
block|{
specifier|final
name|Object
name|message
init|=
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
if|if
condition|(
name|message
operator|instanceof
name|HttpResponse
condition|)
block|{
name|HttpResponse
name|response
init|=
operator|(
name|HttpResponse
operator|)
name|message
decl_stmt|;
name|content
operator|.
name|add
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
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
name|exceptionCaught
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|ExceptionEvent
name|e
parameter_list|)
throws|throws
name|Exception
block|{
name|super
operator|.
name|exceptionCaught
argument_list|(
name|ctx
argument_list|,
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
return|;
block|}
block|}
block|}
end_class

end_unit

