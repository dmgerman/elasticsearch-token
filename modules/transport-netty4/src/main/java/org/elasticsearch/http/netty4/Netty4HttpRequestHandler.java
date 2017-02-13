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
name|DefaultFullHttpRequest
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
name|transport
operator|.
name|netty4
operator|.
name|Netty4Utils
import|;
end_import

begin_class
annotation|@
name|ChannelHandler
operator|.
name|Sharable
DECL|class|Netty4HttpRequestHandler
class|class
name|Netty4HttpRequestHandler
extends|extends
name|SimpleChannelInboundHandler
argument_list|<
name|Object
argument_list|>
block|{
DECL|field|serverTransport
specifier|private
specifier|final
name|Netty4HttpServerTransport
name|serverTransport
decl_stmt|;
DECL|field|httpPipeliningEnabled
specifier|private
specifier|final
name|boolean
name|httpPipeliningEnabled
decl_stmt|;
DECL|field|detailedErrorsEnabled
specifier|private
specifier|final
name|boolean
name|detailedErrorsEnabled
decl_stmt|;
DECL|field|threadContext
specifier|private
specifier|final
name|ThreadContext
name|threadContext
decl_stmt|;
DECL|method|Netty4HttpRequestHandler
name|Netty4HttpRequestHandler
parameter_list|(
name|Netty4HttpServerTransport
name|serverTransport
parameter_list|,
name|boolean
name|detailedErrorsEnabled
parameter_list|,
name|ThreadContext
name|threadContext
parameter_list|)
block|{
name|this
operator|.
name|serverTransport
operator|=
name|serverTransport
expr_stmt|;
name|this
operator|.
name|httpPipeliningEnabled
operator|=
name|serverTransport
operator|.
name|pipelining
expr_stmt|;
name|this
operator|.
name|detailedErrorsEnabled
operator|=
name|detailedErrorsEnabled
expr_stmt|;
name|this
operator|.
name|threadContext
operator|=
name|threadContext
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
specifier|final
name|FullHttpRequest
name|request
decl_stmt|;
specifier|final
name|HttpPipelinedRequest
name|pipelinedRequest
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|httpPipeliningEnabled
operator|&&
name|msg
operator|instanceof
name|HttpPipelinedRequest
condition|)
block|{
name|pipelinedRequest
operator|=
operator|(
name|HttpPipelinedRequest
operator|)
name|msg
expr_stmt|;
name|request
operator|=
operator|(
name|FullHttpRequest
operator|)
name|pipelinedRequest
operator|.
name|last
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|pipelinedRequest
operator|=
literal|null
expr_stmt|;
name|request
operator|=
operator|(
name|FullHttpRequest
operator|)
name|msg
expr_stmt|;
block|}
specifier|final
name|FullHttpRequest
name|copy
init|=
operator|new
name|DefaultFullHttpRequest
argument_list|(
name|request
operator|.
name|protocolVersion
argument_list|()
argument_list|,
name|request
operator|.
name|method
argument_list|()
argument_list|,
name|request
operator|.
name|uri
argument_list|()
argument_list|,
name|Unpooled
operator|.
name|copiedBuffer
argument_list|(
name|request
operator|.
name|content
argument_list|()
argument_list|)
argument_list|,
name|request
operator|.
name|headers
argument_list|()
argument_list|,
name|request
operator|.
name|trailingHeaders
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Netty4HttpRequest
name|httpRequest
init|=
operator|new
name|Netty4HttpRequest
argument_list|(
name|serverTransport
operator|.
name|xContentRegistry
argument_list|,
name|copy
argument_list|,
name|ctx
operator|.
name|channel
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Netty4HttpChannel
name|channel
init|=
operator|new
name|Netty4HttpChannel
argument_list|(
name|serverTransport
argument_list|,
name|httpRequest
argument_list|,
name|pipelinedRequest
argument_list|,
name|detailedErrorsEnabled
argument_list|,
name|threadContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|decoderResult
argument_list|()
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
name|serverTransport
operator|.
name|dispatchRequest
argument_list|(
name|httpRequest
argument_list|,
name|channel
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|request
operator|.
name|decoderResult
argument_list|()
operator|.
name|isFailure
argument_list|()
assert|;
name|serverTransport
operator|.
name|dispatchBadRequest
argument_list|(
name|httpRequest
argument_list|,
name|channel
argument_list|,
name|request
operator|.
name|decoderResult
argument_list|()
operator|.
name|cause
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|Netty4Utils
operator|.
name|maybeDie
argument_list|(
name|cause
argument_list|)
expr_stmt|;
name|serverTransport
operator|.
name|exceptionCaught
argument_list|(
name|ctx
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

