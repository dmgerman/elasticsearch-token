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
name|ByteBufUtil
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
name|handler
operator|.
name|codec
operator|.
name|TooLongFrameException
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
name|HttpMethod
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
name|HttpUtil
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
name|ElasticsearchException
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
name|Setting
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
name|unit
operator|.
name|ByteSizeValue
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
name|BindHttpException
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
name|HttpTransportSettings
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
name|cors
operator|.
name|Netty4CorsConfig
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
name|rest
operator|.
name|BytesRestResponse
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
name|RestChannel
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
name|RestRequest
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
name|io
operator|.
name|IOException
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
name|Charset
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
name|Arrays
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
name|AtomicReference
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
name|Collectors
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
name|Strings
operator|.
name|collectionToDelimitedString
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
name|HttpTransportSettings
operator|.
name|SETTING_CORS_ALLOW_CREDENTIALS
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
name|HttpTransportSettings
operator|.
name|SETTING_CORS_ALLOW_HEADERS
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
name|HttpTransportSettings
operator|.
name|SETTING_CORS_ALLOW_METHODS
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
name|HttpTransportSettings
operator|.
name|SETTING_CORS_ALLOW_ORIGIN
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
name|HttpTransportSettings
operator|.
name|SETTING_CORS_ENABLED
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
name|HttpTransportSettings
operator|.
name|SETTING_CORS_MAX_AGE
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
name|OK
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
name|containsString
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
name|instanceOf
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
name|is
import|;
end_import

begin_comment
comment|/**  * Tests for the {@link Netty4HttpServerTransport} class.  */
end_comment

begin_class
DECL|class|Netty4HttpServerTransportTests
specifier|public
class|class
name|Netty4HttpServerTransportTests
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
name|threadPool
operator|=
literal|null
expr_stmt|;
name|networkService
operator|=
literal|null
expr_stmt|;
name|bigArrays
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testCorsConfig
specifier|public
name|void
name|testCorsConfig
parameter_list|()
block|{
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|methods
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"get"
argument_list|,
literal|"options"
argument_list|,
literal|"post"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|headers
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"Content-Type"
argument_list|,
literal|"Content-Length"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|String
name|suffix
init|=
name|randomBoolean
argument_list|()
condition|?
literal|" "
else|:
literal|""
decl_stmt|;
comment|// sometimes have a leading whitespace between comma delimited elements
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
name|SETTING_CORS_ENABLED
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_CORS_ALLOW_ORIGIN
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"*"
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_CORS_ALLOW_METHODS
operator|.
name|getKey
argument_list|()
argument_list|,
name|collectionToDelimitedString
argument_list|(
name|methods
argument_list|,
literal|","
argument_list|,
name|suffix
argument_list|,
literal|""
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_CORS_ALLOW_HEADERS
operator|.
name|getKey
argument_list|()
argument_list|,
name|collectionToDelimitedString
argument_list|(
name|headers
argument_list|,
literal|","
argument_list|,
name|suffix
argument_list|,
literal|""
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_CORS_ALLOW_CREDENTIALS
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|Netty4CorsConfig
name|corsConfig
init|=
name|Netty4HttpServerTransport
operator|.
name|buildCorsConfig
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|corsConfig
operator|.
name|isAnyOriginSupported
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|headers
argument_list|,
name|corsConfig
operator|.
name|allowedRequestHeaders
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|methods
argument_list|,
name|corsConfig
operator|.
name|allowedRequestMethods
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|HttpMethod
operator|::
name|name
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCorsConfigWithDefaults
specifier|public
name|void
name|testCorsConfigWithDefaults
parameter_list|()
block|{
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|methods
init|=
name|Strings
operator|.
name|commaDelimitedListToSet
argument_list|(
name|SETTING_CORS_ALLOW_METHODS
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|headers
init|=
name|Strings
operator|.
name|commaDelimitedListToSet
argument_list|(
name|SETTING_CORS_ALLOW_HEADERS
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|long
name|maxAge
init|=
name|SETTING_CORS_MAX_AGE
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
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
name|SETTING_CORS_ENABLED
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|Netty4CorsConfig
name|corsConfig
init|=
name|Netty4HttpServerTransport
operator|.
name|buildCorsConfig
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|corsConfig
operator|.
name|isAnyOriginSupported
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|corsConfig
operator|.
name|origins
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|headers
argument_list|,
name|corsConfig
operator|.
name|allowedRequestHeaders
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|methods
argument_list|,
name|corsConfig
operator|.
name|allowedRequestMethods
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|HttpMethod
operator|::
name|name
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|maxAge
argument_list|,
name|corsConfig
operator|.
name|maxAge
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|corsConfig
operator|.
name|isCredentialsAllowed
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test that {@link Netty4HttpServerTransport} supports the "Expect: 100-continue" HTTP header      */
DECL|method|testExpectContinueHeader
specifier|public
name|void
name|testExpectContinueHeader
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|HttpServerTransport
operator|.
name|Dispatcher
name|dispatcher
init|=
operator|new
name|HttpServerTransport
operator|.
name|Dispatcher
argument_list|()
block|{
annotation|@
name|Override
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
operator|new
name|BytesArray
argument_list|(
literal|"done"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dispatchBadRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|,
name|ThreadContext
name|threadContext
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
decl_stmt|;
try|try
init|(
name|Netty4HttpServerTransport
name|transport
init|=
operator|new
name|Netty4HttpServerTransport
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|networkService
argument_list|,
name|bigArrays
argument_list|,
name|threadPool
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
name|dispatcher
argument_list|)
init|)
block|{
name|transport
operator|.
name|start
argument_list|()
expr_stmt|;
name|TransportAddress
name|remoteAddress
init|=
name|randomFrom
argument_list|(
name|transport
operator|.
name|boundAddress
argument_list|()
operator|.
name|boundAddresses
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|Netty4HttpClient
name|client
init|=
operator|new
name|Netty4HttpClient
argument_list|()
init|)
block|{
name|FullHttpRequest
name|request
init|=
operator|new
name|DefaultFullHttpRequest
argument_list|(
name|HttpVersion
operator|.
name|HTTP_1_1
argument_list|,
name|HttpMethod
operator|.
name|POST
argument_list|,
literal|"/"
argument_list|)
decl_stmt|;
name|HttpUtil
operator|.
name|set100ContinueExpected
argument_list|(
name|request
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HttpUtil
operator|.
name|setContentLength
argument_list|(
name|request
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|FullHttpResponse
name|response
init|=
name|client
operator|.
name|post
argument_list|(
name|remoteAddress
operator|.
name|address
argument_list|()
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|status
argument_list|()
argument_list|,
name|is
argument_list|(
name|HttpResponseStatus
operator|.
name|CONTINUE
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|=
operator|new
name|DefaultFullHttpRequest
argument_list|(
name|HttpVersion
operator|.
name|HTTP_1_1
argument_list|,
name|HttpMethod
operator|.
name|POST
argument_list|,
literal|"/"
argument_list|,
name|Unpooled
operator|.
name|EMPTY_BUFFER
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|post
argument_list|(
name|remoteAddress
operator|.
name|address
argument_list|()
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|status
argument_list|()
argument_list|,
name|is
argument_list|(
name|HttpResponseStatus
operator|.
name|OK
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|String
argument_list|(
name|ByteBufUtil
operator|.
name|getBytes
argument_list|(
name|response
operator|.
name|content
argument_list|()
argument_list|)
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"done"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testBindUnavailableAddress
specifier|public
name|void
name|testBindUnavailableAddress
parameter_list|()
block|{
try|try
init|(
name|Netty4HttpServerTransport
name|transport
init|=
operator|new
name|Netty4HttpServerTransport
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|networkService
argument_list|,
name|bigArrays
argument_list|,
name|threadPool
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
operator|new
name|NullDispatcher
argument_list|()
argument_list|)
init|)
block|{
name|transport
operator|.
name|start
argument_list|()
expr_stmt|;
name|TransportAddress
name|remoteAddress
init|=
name|randomFrom
argument_list|(
name|transport
operator|.
name|boundAddress
argument_list|()
operator|.
name|boundAddresses
argument_list|()
argument_list|)
decl_stmt|;
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
literal|"http.port"
argument_list|,
name|remoteAddress
operator|.
name|getPort
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
init|(
name|Netty4HttpServerTransport
name|otherTransport
init|=
operator|new
name|Netty4HttpServerTransport
argument_list|(
name|settings
argument_list|,
name|networkService
argument_list|,
name|bigArrays
argument_list|,
name|threadPool
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
operator|new
name|NullDispatcher
argument_list|()
argument_list|)
init|)
block|{
name|BindHttpException
name|bindHttpException
init|=
name|expectThrows
argument_list|(
name|BindHttpException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|otherTransport
operator|.
name|start
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Failed to bind to ["
operator|+
name|remoteAddress
operator|.
name|getPort
argument_list|()
operator|+
literal|"]"
argument_list|,
name|bindHttpException
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testBadRequest
specifier|public
name|void
name|testBadRequest
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|causeReference
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|HttpServerTransport
operator|.
name|Dispatcher
name|dispatcher
init|=
operator|new
name|HttpServerTransport
operator|.
name|Dispatcher
argument_list|()
block|{
annotation|@
name|Override
specifier|public
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
name|ThreadContext
name|threadContext
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
annotation|@
name|Override
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
name|causeReference
operator|.
name|set
argument_list|(
name|cause
argument_list|)
expr_stmt|;
try|try
block|{
specifier|final
name|ElasticsearchException
name|e
init|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"you sent a bad request and you should feel bad"
argument_list|)
decl_stmt|;
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
decl_stmt|;
specifier|final
name|Settings
name|settings
decl_stmt|;
specifier|final
name|int
name|maxInitialLineLength
decl_stmt|;
specifier|final
name|Setting
argument_list|<
name|ByteSizeValue
argument_list|>
name|httpMaxInitialLineLengthSetting
init|=
name|HttpTransportSettings
operator|.
name|SETTING_HTTP_MAX_INITIAL_LINE_LENGTH
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|maxInitialLineLength
operator|=
name|httpMaxInitialLineLengthSetting
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
operator|.
name|bytesAsInt
argument_list|()
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|EMPTY
expr_stmt|;
block|}
else|else
block|{
name|maxInitialLineLength
operator|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|8192
argument_list|)
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|httpMaxInitialLineLengthSetting
operator|.
name|getKey
argument_list|()
argument_list|,
name|maxInitialLineLength
operator|+
literal|"b"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
try|try
init|(
name|Netty4HttpServerTransport
name|transport
init|=
operator|new
name|Netty4HttpServerTransport
argument_list|(
name|settings
argument_list|,
name|networkService
argument_list|,
name|bigArrays
argument_list|,
name|threadPool
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
name|dispatcher
argument_list|)
init|)
block|{
name|transport
operator|.
name|start
argument_list|()
expr_stmt|;
specifier|final
name|TransportAddress
name|remoteAddress
init|=
name|randomFrom
argument_list|(
name|transport
operator|.
name|boundAddress
operator|.
name|boundAddresses
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|Netty4HttpClient
name|client
init|=
operator|new
name|Netty4HttpClient
argument_list|()
init|)
block|{
specifier|final
name|String
name|url
init|=
literal|"/"
operator|+
operator|new
name|String
argument_list|(
operator|new
name|byte
index|[
name|maxInitialLineLength
index|]
argument_list|,
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|FullHttpRequest
name|request
init|=
operator|new
name|DefaultFullHttpRequest
argument_list|(
name|HttpVersion
operator|.
name|HTTP_1_1
argument_list|,
name|HttpMethod
operator|.
name|GET
argument_list|,
name|url
argument_list|)
decl_stmt|;
specifier|final
name|FullHttpResponse
name|response
init|=
name|client
operator|.
name|post
argument_list|(
name|remoteAddress
operator|.
name|address
argument_list|()
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|HttpResponseStatus
operator|.
name|BAD_REQUEST
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|String
argument_list|(
name|response
operator|.
name|content
argument_list|()
operator|.
name|array
argument_list|()
argument_list|,
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"you sent a bad request and you should feel bad"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertNotNull
argument_list|(
name|causeReference
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|causeReference
operator|.
name|get
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|TooLongFrameException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

