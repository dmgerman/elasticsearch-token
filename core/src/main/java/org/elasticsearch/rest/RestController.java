begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
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
name|client
operator|.
name|node
operator|.
name|NodeClient
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
name|Strings
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
name|breaker
operator|.
name|CircuitBreaker
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
name|BytesArray
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
name|component
operator|.
name|AbstractComponent
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
name|Streams
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
name|logging
operator|.
name|DeprecationLogger
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
name|path
operator|.
name|PathTrie
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentType
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
name|indices
operator|.
name|breaker
operator|.
name|CircuitBreakerService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|io
operator|.
name|InputStream
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
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|atomic
operator|.
name|AtomicBoolean
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
name|Supplier
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
name|UnaryOperator
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|BAD_REQUEST
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|FORBIDDEN
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|INTERNAL_SERVER_ERROR
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|NOT_ACCEPTABLE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|OK
import|;
end_import

begin_class
DECL|class|RestController
specifier|public
class|class
name|RestController
extends|extends
name|AbstractComponent
implements|implements
name|HttpServerTransport
operator|.
name|Dispatcher
block|{
DECL|field|getHandlers
specifier|private
specifier|final
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|getHandlers
init|=
operator|new
name|PathTrie
argument_list|<>
argument_list|(
name|RestUtils
operator|.
name|REST_DECODER
argument_list|)
decl_stmt|;
DECL|field|postHandlers
specifier|private
specifier|final
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|postHandlers
init|=
operator|new
name|PathTrie
argument_list|<>
argument_list|(
name|RestUtils
operator|.
name|REST_DECODER
argument_list|)
decl_stmt|;
DECL|field|putHandlers
specifier|private
specifier|final
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|putHandlers
init|=
operator|new
name|PathTrie
argument_list|<>
argument_list|(
name|RestUtils
operator|.
name|REST_DECODER
argument_list|)
decl_stmt|;
DECL|field|deleteHandlers
specifier|private
specifier|final
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|deleteHandlers
init|=
operator|new
name|PathTrie
argument_list|<>
argument_list|(
name|RestUtils
operator|.
name|REST_DECODER
argument_list|)
decl_stmt|;
DECL|field|headHandlers
specifier|private
specifier|final
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|headHandlers
init|=
operator|new
name|PathTrie
argument_list|<>
argument_list|(
name|RestUtils
operator|.
name|REST_DECODER
argument_list|)
decl_stmt|;
DECL|field|optionsHandlers
specifier|private
specifier|final
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|optionsHandlers
init|=
operator|new
name|PathTrie
argument_list|<>
argument_list|(
name|RestUtils
operator|.
name|REST_DECODER
argument_list|)
decl_stmt|;
DECL|field|handlerWrapper
specifier|private
specifier|final
name|UnaryOperator
argument_list|<
name|RestHandler
argument_list|>
name|handlerWrapper
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|NodeClient
name|client
decl_stmt|;
DECL|field|circuitBreakerService
specifier|private
specifier|final
name|CircuitBreakerService
name|circuitBreakerService
decl_stmt|;
comment|/** Rest headers that are copied to internal requests made during a rest request. */
DECL|field|headersToCopy
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|headersToCopy
decl_stmt|;
DECL|method|RestController
specifier|public
name|RestController
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|headersToCopy
parameter_list|,
name|UnaryOperator
argument_list|<
name|RestHandler
argument_list|>
name|handlerWrapper
parameter_list|,
name|NodeClient
name|client
parameter_list|,
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|headersToCopy
operator|=
name|headersToCopy
expr_stmt|;
if|if
condition|(
name|handlerWrapper
operator|==
literal|null
condition|)
block|{
name|handlerWrapper
operator|=
name|h
lambda|->
name|h
expr_stmt|;
comment|// passthrough if no wrapper set
block|}
name|this
operator|.
name|handlerWrapper
operator|=
name|handlerWrapper
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|circuitBreakerService
operator|=
name|circuitBreakerService
expr_stmt|;
block|}
comment|/**      * Registers a REST handler to be executed when the provided {@code method} and {@code path} match the request.      *      * @param method GET, POST, etc.      * @param path Path to handle (e.g., "/{index}/{type}/_bulk")      * @param handler The handler to actually execute      * @param deprecationMessage The message to log and send as a header in the response      * @param logger The existing deprecation logger to use      */
DECL|method|registerAsDeprecatedHandler
specifier|public
name|void
name|registerAsDeprecatedHandler
parameter_list|(
name|RestRequest
operator|.
name|Method
name|method
parameter_list|,
name|String
name|path
parameter_list|,
name|RestHandler
name|handler
parameter_list|,
name|String
name|deprecationMessage
parameter_list|,
name|DeprecationLogger
name|logger
parameter_list|)
block|{
assert|assert
operator|(
name|handler
operator|instanceof
name|DeprecationRestHandler
operator|)
operator|==
literal|false
assert|;
name|registerHandler
argument_list|(
name|method
argument_list|,
name|path
argument_list|,
operator|new
name|DeprecationRestHandler
argument_list|(
name|handler
argument_list|,
name|deprecationMessage
argument_list|,
name|logger
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Registers a REST handler to be executed when the provided {@code method} and {@code path} match the request, or when provided      * with {@code deprecatedMethod} and {@code deprecatedPath}. Expected usage:      *<pre><code>      * // remove deprecation in next major release      * controller.registerWithDeprecatedHandler(POST, "/_forcemerge", this,      *                                          POST, "/_optimize", deprecationLogger);      * controller.registerWithDeprecatedHandler(POST, "/{index}/_forcemerge", this,      *                                          POST, "/{index}/_optimize", deprecationLogger);      *</code></pre>      *<p>      * The registered REST handler ({@code method} with {@code path}) is a normal REST handler that is not deprecated and it is      * replacing the deprecated REST handler ({@code deprecatedMethod} with {@code deprecatedPath}) that is using the<em>same</em>      * {@code handler}.      *<p>      * Deprecated REST handlers without a direct replacement should be deprecated directly using {@link #registerAsDeprecatedHandler}      * and a specific message.      *      * @param method GET, POST, etc.      * @param path Path to handle (e.g., "/_forcemerge")      * @param handler The handler to actually execute      * @param deprecatedMethod GET, POST, etc.      * @param deprecatedPath<em>Deprecated</em> path to handle (e.g., "/_optimize")      * @param logger The existing deprecation logger to use      */
DECL|method|registerWithDeprecatedHandler
specifier|public
name|void
name|registerWithDeprecatedHandler
parameter_list|(
name|RestRequest
operator|.
name|Method
name|method
parameter_list|,
name|String
name|path
parameter_list|,
name|RestHandler
name|handler
parameter_list|,
name|RestRequest
operator|.
name|Method
name|deprecatedMethod
parameter_list|,
name|String
name|deprecatedPath
parameter_list|,
name|DeprecationLogger
name|logger
parameter_list|)
block|{
comment|// e.g., [POST /_optimize] is deprecated! Use [POST /_forcemerge] instead.
specifier|final
name|String
name|deprecationMessage
init|=
literal|"["
operator|+
name|deprecatedMethod
operator|.
name|name
argument_list|()
operator|+
literal|" "
operator|+
name|deprecatedPath
operator|+
literal|"] is deprecated! Use ["
operator|+
name|method
operator|.
name|name
argument_list|()
operator|+
literal|" "
operator|+
name|path
operator|+
literal|"] instead."
decl_stmt|;
name|registerHandler
argument_list|(
name|method
argument_list|,
name|path
argument_list|,
name|handler
argument_list|)
expr_stmt|;
name|registerAsDeprecatedHandler
argument_list|(
name|deprecatedMethod
argument_list|,
name|deprecatedPath
argument_list|,
name|handler
argument_list|,
name|deprecationMessage
argument_list|,
name|logger
argument_list|)
expr_stmt|;
block|}
comment|/**      * Registers a REST handler to be executed when the provided method and path match the request.      *      * @param method GET, POST, etc.      * @param path Path to handle (e.g., "/{index}/{type}/_bulk")      * @param handler The handler to actually execute      */
DECL|method|registerHandler
specifier|public
name|void
name|registerHandler
parameter_list|(
name|RestRequest
operator|.
name|Method
name|method
parameter_list|,
name|String
name|path
parameter_list|,
name|RestHandler
name|handler
parameter_list|)
block|{
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|handlers
init|=
name|getHandlersForMethod
argument_list|(
name|method
argument_list|)
decl_stmt|;
if|if
condition|(
name|handlers
operator|!=
literal|null
condition|)
block|{
name|handlers
operator|.
name|insert
argument_list|(
name|path
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't handle ["
operator|+
name|method
operator|+
literal|"] for path ["
operator|+
name|path
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
comment|/**      * @param request The current request. Must not be null.      * @return true iff the circuit breaker limit must be enforced for processing this request.      */
DECL|method|canTripCircuitBreaker
specifier|public
name|boolean
name|canTripCircuitBreaker
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
name|RestHandler
name|handler
init|=
name|getHandler
argument_list|(
name|request
argument_list|)
decl_stmt|;
return|return
operator|(
name|handler
operator|!=
literal|null
operator|)
condition|?
name|handler
operator|.
name|canTripCircuitBreaker
argument_list|()
else|:
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|dispatchRequest
specifier|public
name|void
name|dispatchRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|,
name|ThreadContext
name|threadContext
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|rawPath
argument_list|()
operator|.
name|equals
argument_list|(
literal|"/favicon.ico"
argument_list|)
condition|)
block|{
name|handleFavicon
argument_list|(
name|request
argument_list|,
name|channel
argument_list|)
expr_stmt|;
return|return;
block|}
name|RestChannel
name|responseChannel
init|=
name|channel
decl_stmt|;
try|try
block|{
specifier|final
name|int
name|contentLength
init|=
name|request
operator|.
name|hasContent
argument_list|()
condition|?
name|request
operator|.
name|content
argument_list|()
operator|.
name|length
argument_list|()
else|:
literal|0
decl_stmt|;
assert|assert
name|contentLength
operator|>=
literal|0
operator|:
literal|"content length was negative, how is that possible?"
assert|;
specifier|final
name|RestHandler
name|handler
init|=
name|getHandler
argument_list|(
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|contentLength
operator|>
literal|0
operator|&&
name|hasContentType
argument_list|(
name|request
argument_list|,
name|handler
argument_list|)
operator|==
literal|false
condition|)
block|{
name|sendContentTypeErrorMessage
argument_list|(
name|request
argument_list|,
name|responseChannel
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|contentLength
operator|>
literal|0
operator|&&
name|handler
operator|!=
literal|null
operator|&&
name|handler
operator|.
name|supportsContentStream
argument_list|()
operator|&&
name|request
operator|.
name|getXContentType
argument_list|()
operator|!=
name|XContentType
operator|.
name|JSON
operator|&&
name|request
operator|.
name|getXContentType
argument_list|()
operator|!=
name|XContentType
operator|.
name|SMILE
condition|)
block|{
name|responseChannel
operator|.
name|sendResponse
argument_list|(
name|BytesRestResponse
operator|.
name|createSimpleErrorResponse
argument_list|(
name|RestStatus
operator|.
name|NOT_ACCEPTABLE
argument_list|,
literal|"Content-Type ["
operator|+
name|request
operator|.
name|getXContentType
argument_list|()
operator|+
literal|"] does not support stream parsing. Use JSON or SMILE instead"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|canTripCircuitBreaker
argument_list|(
name|request
argument_list|)
condition|)
block|{
name|inFlightRequestsBreaker
argument_list|(
name|circuitBreakerService
argument_list|)
operator|.
name|addEstimateBytesAndMaybeBreak
argument_list|(
name|contentLength
argument_list|,
literal|"<http_request>"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|inFlightRequestsBreaker
argument_list|(
name|circuitBreakerService
argument_list|)
operator|.
name|addWithoutBreaking
argument_list|(
name|contentLength
argument_list|)
expr_stmt|;
block|}
comment|// iff we could reserve bytes for the request we need to send the response also over this channel
name|responseChannel
operator|=
operator|new
name|ResourceHandlingHttpChannel
argument_list|(
name|channel
argument_list|,
name|circuitBreakerService
argument_list|,
name|contentLength
argument_list|)
expr_stmt|;
name|dispatchRequest
argument_list|(
name|request
argument_list|,
name|responseChannel
argument_list|,
name|client
argument_list|,
name|threadContext
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|responseChannel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BytesRestResponse
argument_list|(
name|channel
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|inner
parameter_list|)
block|{
name|inner
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|logger
operator|.
name|error
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"failed to send failure response for uri [{}]"
argument_list|,
name|request
operator|.
name|uri
argument_list|()
argument_list|)
argument_list|,
name|inner
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|dispatchBadRequest
specifier|public
name|void
name|dispatchBadRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|,
specifier|final
name|ThreadContext
name|threadContext
parameter_list|,
specifier|final
name|Throwable
name|cause
parameter_list|)
block|{
try|try
block|{
specifier|final
name|Exception
name|e
decl_stmt|;
if|if
condition|(
name|cause
operator|==
literal|null
condition|)
block|{
name|e
operator|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"unknown cause"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cause
operator|instanceof
name|Exception
condition|)
block|{
name|e
operator|=
operator|(
name|Exception
operator|)
name|cause
expr_stmt|;
block|}
else|else
block|{
name|e
operator|=
operator|new
name|ElasticsearchException
argument_list|(
name|cause
argument_list|)
expr_stmt|;
block|}
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BytesRestResponse
argument_list|(
name|channel
argument_list|,
name|BAD_REQUEST
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
specifier|final
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|cause
operator|!=
literal|null
condition|)
block|{
name|e
operator|.
name|addSuppressed
argument_list|(
name|cause
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to send bad request response"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BytesRestResponse
argument_list|(
name|INTERNAL_SERVER_ERROR
argument_list|,
name|BytesRestResponse
operator|.
name|TEXT_CONTENT_TYPE
argument_list|,
name|BytesArray
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|dispatchRequest
name|void
name|dispatchRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|,
name|ThreadContext
name|threadContext
parameter_list|,
specifier|final
name|RestHandler
name|handler
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|checkRequestParameters
argument_list|(
name|request
argument_list|,
name|channel
argument_list|)
operator|==
literal|false
condition|)
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|BytesRestResponse
operator|.
name|createSimpleErrorResponse
argument_list|(
name|BAD_REQUEST
argument_list|,
literal|"error traces in responses are disabled."
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
init|(
name|ThreadContext
operator|.
name|StoredContext
name|ignored
init|=
name|threadContext
operator|.
name|stashContext
argument_list|()
init|)
block|{
for|for
control|(
name|String
name|key
range|:
name|headersToCopy
control|)
block|{
name|String
name|httpHeader
init|=
name|request
operator|.
name|header
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|httpHeader
operator|!=
literal|null
condition|)
block|{
name|threadContext
operator|.
name|putHeader
argument_list|(
name|key
argument_list|,
name|httpHeader
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|handler
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|method
argument_list|()
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|OPTIONS
condition|)
block|{
comment|// when we have OPTIONS request, simply send OK by default (with the Access Control Origin header which gets automatically added)
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BytesRestResponse
argument_list|(
name|OK
argument_list|,
name|BytesRestResponse
operator|.
name|TEXT_CONTENT_TYPE
argument_list|,
name|BytesArray
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|String
name|msg
init|=
literal|"No handler found for uri ["
operator|+
name|request
operator|.
name|uri
argument_list|()
operator|+
literal|"] and method ["
operator|+
name|request
operator|.
name|method
argument_list|()
operator|+
literal|"]"
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BytesRestResponse
argument_list|(
name|BAD_REQUEST
argument_list|,
name|msg
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
specifier|final
name|RestHandler
name|wrappedHandler
init|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|handlerWrapper
operator|.
name|apply
argument_list|(
name|handler
argument_list|)
argument_list|)
decl_stmt|;
name|wrappedHandler
operator|.
name|handleRequest
argument_list|(
name|request
argument_list|,
name|channel
argument_list|,
name|client
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * If a request contains content, this method will return {@code true} if the {@code Content-Type} header is present, matches an      * {@link XContentType} or the handler supports a content stream and the content type header is for newline delimited JSON,      */
DECL|method|hasContentType
specifier|private
name|boolean
name|hasContentType
parameter_list|(
specifier|final
name|RestRequest
name|restRequest
parameter_list|,
specifier|final
name|RestHandler
name|restHandler
parameter_list|)
block|{
if|if
condition|(
name|restRequest
operator|.
name|getXContentType
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|restHandler
operator|!=
literal|null
operator|&&
name|restHandler
operator|.
name|supportsContentStream
argument_list|()
operator|&&
name|restRequest
operator|.
name|header
argument_list|(
literal|"Content-Type"
argument_list|)
operator|!=
literal|null
condition|)
block|{
specifier|final
name|String
name|lowercaseMediaType
init|=
name|restRequest
operator|.
name|header
argument_list|(
literal|"Content-Type"
argument_list|)
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
comment|// we also support newline delimited JSON: http://specs.okfnlabs.org/ndjson/
if|if
condition|(
name|lowercaseMediaType
operator|.
name|equals
argument_list|(
literal|"application/x-ndjson"
argument_list|)
condition|)
block|{
name|restRequest
operator|.
name|setXContentType
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
DECL|method|sendContentTypeErrorMessage
specifier|private
name|void
name|sendContentTypeErrorMessage
parameter_list|(
name|RestRequest
name|restRequest
parameter_list|,
name|RestChannel
name|channel
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|contentTypeHeader
init|=
name|restRequest
operator|.
name|getAllHeaderValues
argument_list|(
literal|"Content-Type"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|errorMessage
decl_stmt|;
if|if
condition|(
name|contentTypeHeader
operator|==
literal|null
condition|)
block|{
name|errorMessage
operator|=
literal|"Content-Type header is missing"
expr_stmt|;
block|}
else|else
block|{
name|errorMessage
operator|=
literal|"Content-Type header ["
operator|+
name|Strings
operator|.
name|collectionToCommaDelimitedString
argument_list|(
name|restRequest
operator|.
name|getAllHeaderValues
argument_list|(
literal|"Content-Type"
argument_list|)
argument_list|)
operator|+
literal|"] is not supported"
expr_stmt|;
block|}
name|channel
operator|.
name|sendResponse
argument_list|(
name|BytesRestResponse
operator|.
name|createSimpleErrorResponse
argument_list|(
name|NOT_ACCEPTABLE
argument_list|,
name|errorMessage
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Checks the request parameters against enabled settings for error trace support      * @return true if the request does not have any parameters that conflict with system settings      */
DECL|method|checkRequestParameters
name|boolean
name|checkRequestParameters
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|)
block|{
comment|// error_trace cannot be used when we disable detailed errors
comment|// we consume the error_trace parameter first to ensure that it is always consumed
if|if
condition|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"error_trace"
argument_list|,
literal|false
argument_list|)
operator|&&
name|channel
operator|.
name|detailedErrorsEnabled
argument_list|()
operator|==
literal|false
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
DECL|method|getHandler
specifier|private
name|RestHandler
name|getHandler
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
name|String
name|path
init|=
name|getPath
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|handlers
init|=
name|getHandlersForMethod
argument_list|(
name|request
operator|.
name|method
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|handlers
operator|!=
literal|null
condition|)
block|{
return|return
name|handlers
operator|.
name|retrieve
argument_list|(
name|path
argument_list|,
name|request
operator|.
name|params
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|method|getHandlersForMethod
specifier|private
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|getHandlersForMethod
parameter_list|(
name|RestRequest
operator|.
name|Method
name|method
parameter_list|)
block|{
if|if
condition|(
name|method
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|GET
condition|)
block|{
return|return
name|getHandlers
return|;
block|}
elseif|else
if|if
condition|(
name|method
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|POST
condition|)
block|{
return|return
name|postHandlers
return|;
block|}
elseif|else
if|if
condition|(
name|method
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|PUT
condition|)
block|{
return|return
name|putHandlers
return|;
block|}
elseif|else
if|if
condition|(
name|method
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|DELETE
condition|)
block|{
return|return
name|deleteHandlers
return|;
block|}
elseif|else
if|if
condition|(
name|method
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|HEAD
condition|)
block|{
return|return
name|headHandlers
return|;
block|}
elseif|else
if|if
condition|(
name|method
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|OPTIONS
condition|)
block|{
return|return
name|optionsHandlers
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|method|getPath
specifier|private
name|String
name|getPath
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
comment|// we use rawPath since we don't want to decode it while processing the path resolution
comment|// so we can handle things like:
comment|// my_index/my_type/http%3A%2F%2Fwww.google.com
return|return
name|request
operator|.
name|rawPath
argument_list|()
return|;
block|}
DECL|method|handleFavicon
name|void
name|handleFavicon
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|method
argument_list|()
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|GET
condition|)
block|{
try|try
block|{
try|try
init|(
name|InputStream
name|stream
init|=
name|getClass
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
literal|"/config/favicon.ico"
argument_list|)
init|)
block|{
name|ByteArrayOutputStream
name|out
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|Streams
operator|.
name|copy
argument_list|(
name|stream
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|BytesRestResponse
name|restResponse
init|=
operator|new
name|BytesRestResponse
argument_list|(
name|RestStatus
operator|.
name|OK
argument_list|,
literal|"image/x-icon"
argument_list|,
name|out
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|restResponse
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BytesRestResponse
argument_list|(
name|INTERNAL_SERVER_ERROR
argument_list|,
name|BytesRestResponse
operator|.
name|TEXT_CONTENT_TYPE
argument_list|,
name|BytesArray
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BytesRestResponse
argument_list|(
name|FORBIDDEN
argument_list|,
name|BytesRestResponse
operator|.
name|TEXT_CONTENT_TYPE
argument_list|,
name|BytesArray
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ResourceHandlingHttpChannel
specifier|private
specifier|static
specifier|final
class|class
name|ResourceHandlingHttpChannel
implements|implements
name|RestChannel
block|{
DECL|field|delegate
specifier|private
specifier|final
name|RestChannel
name|delegate
decl_stmt|;
DECL|field|circuitBreakerService
specifier|private
specifier|final
name|CircuitBreakerService
name|circuitBreakerService
decl_stmt|;
DECL|field|contentLength
specifier|private
specifier|final
name|int
name|contentLength
decl_stmt|;
DECL|field|closed
specifier|private
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|method|ResourceHandlingHttpChannel
name|ResourceHandlingHttpChannel
parameter_list|(
name|RestChannel
name|delegate
parameter_list|,
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|,
name|int
name|contentLength
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
name|this
operator|.
name|circuitBreakerService
operator|=
name|circuitBreakerService
expr_stmt|;
name|this
operator|.
name|contentLength
operator|=
name|contentLength
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newBuilder
specifier|public
name|XContentBuilder
name|newBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|newBuilder
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newErrorBuilder
specifier|public
name|XContentBuilder
name|newErrorBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|newErrorBuilder
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newBuilder
specifier|public
name|XContentBuilder
name|newBuilder
parameter_list|(
annotation|@
name|Nullable
name|XContentType
name|xContentType
parameter_list|,
name|boolean
name|useFiltering
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|newBuilder
argument_list|(
name|xContentType
argument_list|,
name|useFiltering
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|bytesOutput
specifier|public
name|BytesStreamOutput
name|bytesOutput
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|bytesOutput
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|request
specifier|public
name|RestRequest
name|request
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|request
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|detailedErrorsEnabled
specifier|public
name|boolean
name|detailedErrorsEnabled
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|detailedErrorsEnabled
argument_list|()
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
name|close
argument_list|()
expr_stmt|;
name|delegate
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
DECL|method|close
specifier|private
name|void
name|close
parameter_list|()
block|{
comment|// attempt to close once atomically
if|if
condition|(
name|closed
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Channel is already closed"
argument_list|)
throw|;
block|}
name|inFlightRequestsBreaker
argument_list|(
name|circuitBreakerService
argument_list|)
operator|.
name|addWithoutBreaking
argument_list|(
operator|-
name|contentLength
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|inFlightRequestsBreaker
specifier|private
specifier|static
name|CircuitBreaker
name|inFlightRequestsBreaker
parameter_list|(
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|)
block|{
comment|// We always obtain a fresh breaker to reflect changes to the breaker configuration.
return|return
name|circuitBreakerService
operator|.
name|getBreaker
argument_list|(
name|CircuitBreaker
operator|.
name|IN_FLIGHT_REQUESTS
argument_list|)
return|;
block|}
block|}
end_class

end_unit

