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
name|ChannelFutureListener
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
name|HttpHeaderValues
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
name|HttpHeaders
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
name|HttpResponse
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
name|cookie
operator|.
name|ServerCookieDecoder
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
name|cookie
operator|.
name|ServerCookieEncoder
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
name|bytes
operator|.
name|BytesReference
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
name|BytesStreamOutput
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
name|ReleasableBytesStreamOutput
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
name|cors
operator|.
name|Netty4CorsHandler
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
name|rest
operator|.
name|AbstractRestChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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
name|EnumMap
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
name|Map
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

begin_class
DECL|class|Netty4HttpChannel
specifier|final
class|class
name|Netty4HttpChannel
extends|extends
name|AbstractRestChannel
block|{
DECL|field|transport
specifier|private
specifier|final
name|Netty4HttpServerTransport
name|transport
decl_stmt|;
DECL|field|channel
specifier|private
specifier|final
name|Channel
name|channel
decl_stmt|;
DECL|field|nettyRequest
specifier|private
specifier|final
name|FullHttpRequest
name|nettyRequest
decl_stmt|;
DECL|field|pipelinedRequest
specifier|private
specifier|final
name|HttpPipelinedRequest
name|pipelinedRequest
decl_stmt|;
DECL|field|threadContext
specifier|private
specifier|final
name|ThreadContext
name|threadContext
decl_stmt|;
comment|/**      * @param transport             The corresponding<code>NettyHttpServerTransport</code> where this channel belongs to.      * @param request               The request that is handled by this channel.      * @param pipelinedRequest      If HTTP pipelining is enabled provide the corresponding pipelined request. May be null if      *                              HTTP pipelining is disabled.      * @param detailedErrorsEnabled true iff error messages should include stack traces.      * @param threadContext         the thread context for the channel      */
DECL|method|Netty4HttpChannel
name|Netty4HttpChannel
parameter_list|(
specifier|final
name|Netty4HttpServerTransport
name|transport
parameter_list|,
specifier|final
name|Netty4HttpRequest
name|request
parameter_list|,
specifier|final
name|HttpPipelinedRequest
name|pipelinedRequest
parameter_list|,
specifier|final
name|boolean
name|detailedErrorsEnabled
parameter_list|,
specifier|final
name|ThreadContext
name|threadContext
parameter_list|)
block|{
name|super
argument_list|(
name|request
argument_list|,
name|detailedErrorsEnabled
argument_list|)
expr_stmt|;
name|this
operator|.
name|transport
operator|=
name|transport
expr_stmt|;
name|this
operator|.
name|channel
operator|=
name|request
operator|.
name|getChannel
argument_list|()
expr_stmt|;
name|this
operator|.
name|nettyRequest
operator|=
name|request
operator|.
name|request
argument_list|()
expr_stmt|;
name|this
operator|.
name|pipelinedRequest
operator|=
name|pipelinedRequest
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
DECL|method|newBytesOutput
specifier|public
name|BytesStreamOutput
name|newBytesOutput
parameter_list|()
block|{
return|return
operator|new
name|ReleasableBytesStreamOutput
argument_list|(
name|transport
operator|.
name|bigArrays
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|sendResponse
specifier|public
name|void
name|sendResponse
parameter_list|(
name|RestResponse
name|response
parameter_list|)
block|{
comment|// if the response object was created upstream, then use it;
comment|// otherwise, create a new one
name|ByteBuf
name|buffer
init|=
name|Netty4Utils
operator|.
name|toByteBuf
argument_list|(
name|response
operator|.
name|content
argument_list|()
argument_list|)
decl_stmt|;
name|FullHttpResponse
name|resp
init|=
name|newResponse
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
name|resp
operator|.
name|setStatus
argument_list|(
name|getStatus
argument_list|(
name|response
operator|.
name|status
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Netty4CorsHandler
operator|.
name|setCorsResponseHeaders
argument_list|(
name|nettyRequest
argument_list|,
name|resp
argument_list|,
name|transport
operator|.
name|getCorsConfig
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|opaque
init|=
name|nettyRequest
operator|.
name|headers
argument_list|()
operator|.
name|get
argument_list|(
literal|"X-Opaque-Id"
argument_list|)
decl_stmt|;
if|if
condition|(
name|opaque
operator|!=
literal|null
condition|)
block|{
name|setHeaderField
argument_list|(
name|resp
argument_list|,
literal|"X-Opaque-Id"
argument_list|,
name|opaque
argument_list|)
expr_stmt|;
block|}
comment|// Add all custom headers
name|addCustomHeaders
argument_list|(
name|resp
argument_list|,
name|response
operator|.
name|getHeaders
argument_list|()
argument_list|)
expr_stmt|;
name|addCustomHeaders
argument_list|(
name|resp
argument_list|,
name|threadContext
operator|.
name|getResponseHeaders
argument_list|()
argument_list|)
expr_stmt|;
name|BytesReference
name|content
init|=
name|response
operator|.
name|content
argument_list|()
decl_stmt|;
name|boolean
name|release
init|=
name|content
operator|instanceof
name|Releasable
decl_stmt|;
try|try
block|{
comment|// If our response doesn't specify a content-type header, set one
name|setHeaderField
argument_list|(
name|resp
argument_list|,
name|HttpHeaderNames
operator|.
name|CONTENT_TYPE
operator|.
name|toString
argument_list|()
argument_list|,
name|response
operator|.
name|contentType
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// If our response has no content-length, calculate and set one
name|setHeaderField
argument_list|(
name|resp
argument_list|,
name|HttpHeaderNames
operator|.
name|CONTENT_LENGTH
operator|.
name|toString
argument_list|()
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|buffer
operator|.
name|readableBytes
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|addCookies
argument_list|(
name|resp
argument_list|)
expr_stmt|;
specifier|final
name|ChannelPromise
name|promise
init|=
name|channel
operator|.
name|newPromise
argument_list|()
decl_stmt|;
if|if
condition|(
name|release
condition|)
block|{
name|promise
operator|.
name|addListener
argument_list|(
name|f
lambda|->
operator|(
operator|(
name|Releasable
operator|)
name|content
operator|)
operator|.
name|close
argument_list|()
argument_list|)
expr_stmt|;
name|release
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|isCloseConnection
argument_list|()
condition|)
block|{
name|promise
operator|.
name|addListener
argument_list|(
name|ChannelFutureListener
operator|.
name|CLOSE
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|pipelinedRequest
operator|!=
literal|null
condition|)
block|{
name|channel
operator|.
name|writeAndFlush
argument_list|(
name|pipelinedRequest
operator|.
name|createHttpResponse
argument_list|(
name|resp
argument_list|,
name|promise
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|channel
operator|.
name|writeAndFlush
argument_list|(
name|resp
argument_list|,
name|promise
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|release
condition|)
block|{
operator|(
operator|(
name|Releasable
operator|)
name|content
operator|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|pipelinedRequest
operator|!=
literal|null
condition|)
block|{
name|pipelinedRequest
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|setHeaderField
specifier|private
name|void
name|setHeaderField
parameter_list|(
name|HttpResponse
name|resp
parameter_list|,
name|String
name|headerField
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|setHeaderField
argument_list|(
name|resp
argument_list|,
name|headerField
argument_list|,
name|value
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|setHeaderField
specifier|private
name|void
name|setHeaderField
parameter_list|(
name|HttpResponse
name|resp
parameter_list|,
name|String
name|headerField
parameter_list|,
name|String
name|value
parameter_list|,
name|boolean
name|override
parameter_list|)
block|{
if|if
condition|(
name|override
operator|||
operator|!
name|resp
operator|.
name|headers
argument_list|()
operator|.
name|contains
argument_list|(
name|headerField
argument_list|)
condition|)
block|{
name|resp
operator|.
name|headers
argument_list|()
operator|.
name|add
argument_list|(
name|headerField
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|addCookies
specifier|private
name|void
name|addCookies
parameter_list|(
name|HttpResponse
name|resp
parameter_list|)
block|{
if|if
condition|(
name|transport
operator|.
name|resetCookies
condition|)
block|{
name|String
name|cookieString
init|=
name|nettyRequest
operator|.
name|headers
argument_list|()
operator|.
name|get
argument_list|(
name|HttpHeaders
operator|.
name|Names
operator|.
name|COOKIE
argument_list|)
decl_stmt|;
if|if
condition|(
name|cookieString
operator|!=
literal|null
condition|)
block|{
name|Set
argument_list|<
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
name|cookie
operator|.
name|Cookie
argument_list|>
name|cookies
init|=
name|ServerCookieDecoder
operator|.
name|STRICT
operator|.
name|decode
argument_list|(
name|cookieString
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|cookies
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Reset the cookies if necessary.
name|resp
operator|.
name|headers
argument_list|()
operator|.
name|set
argument_list|(
name|HttpHeaderNames
operator|.
name|SET_COOKIE
argument_list|,
name|ServerCookieEncoder
operator|.
name|STRICT
operator|.
name|encode
argument_list|(
name|cookies
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|addCustomHeaders
specifier|private
name|void
name|addCustomHeaders
parameter_list|(
name|HttpResponse
name|response
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|customHeaders
parameter_list|)
block|{
if|if
condition|(
name|customHeaders
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|headerEntry
range|:
name|customHeaders
operator|.
name|entrySet
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|headerValue
range|:
name|headerEntry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|setHeaderField
argument_list|(
name|response
argument_list|,
name|headerEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|headerValue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// Determine if the request protocol version is HTTP 1.0
DECL|method|isHttp10
specifier|private
name|boolean
name|isHttp10
parameter_list|()
block|{
return|return
name|nettyRequest
operator|.
name|protocolVersion
argument_list|()
operator|.
name|equals
argument_list|(
name|HttpVersion
operator|.
name|HTTP_1_0
argument_list|)
return|;
block|}
comment|// Determine if the request connection should be closed on completion.
DECL|method|isCloseConnection
specifier|private
name|boolean
name|isCloseConnection
parameter_list|()
block|{
specifier|final
name|boolean
name|http10
init|=
name|isHttp10
argument_list|()
decl_stmt|;
return|return
name|HttpHeaderValues
operator|.
name|CLOSE
operator|.
name|equals
argument_list|(
name|nettyRequest
operator|.
name|headers
argument_list|()
operator|.
name|get
argument_list|(
name|HttpHeaderNames
operator|.
name|CONNECTION
argument_list|)
argument_list|)
operator|||
operator|(
name|http10
operator|&&
name|HttpHeaderValues
operator|.
name|KEEP_ALIVE
operator|.
name|equals
argument_list|(
name|nettyRequest
operator|.
name|headers
argument_list|()
operator|.
name|get
argument_list|(
name|HttpHeaderNames
operator|.
name|CONNECTION
argument_list|)
argument_list|)
operator|==
literal|false
operator|)
return|;
block|}
comment|// Create a new {@link HttpResponse} to transmit the response for the netty request.
DECL|method|newResponse
specifier|private
name|FullHttpResponse
name|newResponse
parameter_list|(
name|ByteBuf
name|buffer
parameter_list|)
block|{
specifier|final
name|boolean
name|http10
init|=
name|isHttp10
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|close
init|=
name|isCloseConnection
argument_list|()
decl_stmt|;
comment|// Build the response object.
specifier|final
name|HttpResponseStatus
name|status
init|=
name|HttpResponseStatus
operator|.
name|OK
decl_stmt|;
comment|// default to initialize
specifier|final
name|FullHttpResponse
name|response
decl_stmt|;
if|if
condition|(
name|http10
condition|)
block|{
name|response
operator|=
operator|new
name|DefaultFullHttpResponse
argument_list|(
name|HttpVersion
operator|.
name|HTTP_1_0
argument_list|,
name|status
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|close
condition|)
block|{
name|response
operator|.
name|headers
argument_list|()
operator|.
name|add
argument_list|(
name|HttpHeaderNames
operator|.
name|CONNECTION
argument_list|,
literal|"Keep-Alive"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|response
operator|=
operator|new
name|DefaultFullHttpResponse
argument_list|(
name|HttpVersion
operator|.
name|HTTP_1_1
argument_list|,
name|status
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
block|}
return|return
name|response
return|;
block|}
DECL|field|TOO_MANY_REQUESTS
specifier|private
specifier|static
specifier|final
name|HttpResponseStatus
name|TOO_MANY_REQUESTS
init|=
operator|new
name|HttpResponseStatus
argument_list|(
literal|429
argument_list|,
literal|"Too Many Requests"
argument_list|)
decl_stmt|;
DECL|field|MAP
specifier|private
specifier|static
name|Map
argument_list|<
name|RestStatus
argument_list|,
name|HttpResponseStatus
argument_list|>
name|MAP
decl_stmt|;
static|static
block|{
name|EnumMap
argument_list|<
name|RestStatus
argument_list|,
name|HttpResponseStatus
argument_list|>
name|map
init|=
operator|new
name|EnumMap
argument_list|<>
argument_list|(
name|RestStatus
operator|.
name|class
argument_list|)
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|CONTINUE
argument_list|,
name|HttpResponseStatus
operator|.
name|CONTINUE
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|SWITCHING_PROTOCOLS
argument_list|,
name|HttpResponseStatus
operator|.
name|SWITCHING_PROTOCOLS
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|OK
argument_list|,
name|HttpResponseStatus
operator|.
name|OK
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|CREATED
argument_list|,
name|HttpResponseStatus
operator|.
name|CREATED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|ACCEPTED
argument_list|,
name|HttpResponseStatus
operator|.
name|ACCEPTED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|NON_AUTHORITATIVE_INFORMATION
argument_list|,
name|HttpResponseStatus
operator|.
name|NON_AUTHORITATIVE_INFORMATION
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|NO_CONTENT
argument_list|,
name|HttpResponseStatus
operator|.
name|NO_CONTENT
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|RESET_CONTENT
argument_list|,
name|HttpResponseStatus
operator|.
name|RESET_CONTENT
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|PARTIAL_CONTENT
argument_list|,
name|HttpResponseStatus
operator|.
name|PARTIAL_CONTENT
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|MULTI_STATUS
argument_list|,
name|HttpResponseStatus
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|)
expr_stmt|;
comment|// no status for this??
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|MULTIPLE_CHOICES
argument_list|,
name|HttpResponseStatus
operator|.
name|MULTIPLE_CHOICES
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|MOVED_PERMANENTLY
argument_list|,
name|HttpResponseStatus
operator|.
name|MOVED_PERMANENTLY
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|FOUND
argument_list|,
name|HttpResponseStatus
operator|.
name|FOUND
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|SEE_OTHER
argument_list|,
name|HttpResponseStatus
operator|.
name|SEE_OTHER
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|NOT_MODIFIED
argument_list|,
name|HttpResponseStatus
operator|.
name|NOT_MODIFIED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|USE_PROXY
argument_list|,
name|HttpResponseStatus
operator|.
name|USE_PROXY
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|TEMPORARY_REDIRECT
argument_list|,
name|HttpResponseStatus
operator|.
name|TEMPORARY_REDIRECT
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|BAD_REQUEST
argument_list|,
name|HttpResponseStatus
operator|.
name|BAD_REQUEST
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|UNAUTHORIZED
argument_list|,
name|HttpResponseStatus
operator|.
name|UNAUTHORIZED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|PAYMENT_REQUIRED
argument_list|,
name|HttpResponseStatus
operator|.
name|PAYMENT_REQUIRED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|FORBIDDEN
argument_list|,
name|HttpResponseStatus
operator|.
name|FORBIDDEN
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|NOT_FOUND
argument_list|,
name|HttpResponseStatus
operator|.
name|NOT_FOUND
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|METHOD_NOT_ALLOWED
argument_list|,
name|HttpResponseStatus
operator|.
name|METHOD_NOT_ALLOWED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|NOT_ACCEPTABLE
argument_list|,
name|HttpResponseStatus
operator|.
name|NOT_ACCEPTABLE
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|PROXY_AUTHENTICATION
argument_list|,
name|HttpResponseStatus
operator|.
name|PROXY_AUTHENTICATION_REQUIRED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|REQUEST_TIMEOUT
argument_list|,
name|HttpResponseStatus
operator|.
name|REQUEST_TIMEOUT
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|CONFLICT
argument_list|,
name|HttpResponseStatus
operator|.
name|CONFLICT
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|GONE
argument_list|,
name|HttpResponseStatus
operator|.
name|GONE
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|LENGTH_REQUIRED
argument_list|,
name|HttpResponseStatus
operator|.
name|LENGTH_REQUIRED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|PRECONDITION_FAILED
argument_list|,
name|HttpResponseStatus
operator|.
name|PRECONDITION_FAILED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|REQUEST_ENTITY_TOO_LARGE
argument_list|,
name|HttpResponseStatus
operator|.
name|REQUEST_ENTITY_TOO_LARGE
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|REQUEST_URI_TOO_LONG
argument_list|,
name|HttpResponseStatus
operator|.
name|REQUEST_URI_TOO_LONG
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|UNSUPPORTED_MEDIA_TYPE
argument_list|,
name|HttpResponseStatus
operator|.
name|UNSUPPORTED_MEDIA_TYPE
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|REQUESTED_RANGE_NOT_SATISFIED
argument_list|,
name|HttpResponseStatus
operator|.
name|REQUESTED_RANGE_NOT_SATISFIABLE
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|EXPECTATION_FAILED
argument_list|,
name|HttpResponseStatus
operator|.
name|EXPECTATION_FAILED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|UNPROCESSABLE_ENTITY
argument_list|,
name|HttpResponseStatus
operator|.
name|BAD_REQUEST
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|LOCKED
argument_list|,
name|HttpResponseStatus
operator|.
name|BAD_REQUEST
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|FAILED_DEPENDENCY
argument_list|,
name|HttpResponseStatus
operator|.
name|BAD_REQUEST
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|TOO_MANY_REQUESTS
argument_list|,
name|TOO_MANY_REQUESTS
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|,
name|HttpResponseStatus
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|NOT_IMPLEMENTED
argument_list|,
name|HttpResponseStatus
operator|.
name|NOT_IMPLEMENTED
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|BAD_GATEWAY
argument_list|,
name|HttpResponseStatus
operator|.
name|BAD_GATEWAY
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|,
name|HttpResponseStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|GATEWAY_TIMEOUT
argument_list|,
name|HttpResponseStatus
operator|.
name|GATEWAY_TIMEOUT
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|RestStatus
operator|.
name|HTTP_VERSION_NOT_SUPPORTED
argument_list|,
name|HttpResponseStatus
operator|.
name|HTTP_VERSION_NOT_SUPPORTED
argument_list|)
expr_stmt|;
name|MAP
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
DECL|method|getStatus
specifier|private
specifier|static
name|HttpResponseStatus
name|getStatus
parameter_list|(
name|RestStatus
name|status
parameter_list|)
block|{
return|return
name|MAP
operator|.
name|getOrDefault
argument_list|(
name|status
argument_list|,
name|HttpResponseStatus
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|)
return|;
block|}
block|}
end_class

end_unit

