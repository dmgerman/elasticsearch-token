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
name|netty
operator|.
name|cors
operator|.
name|CorsHandler
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
name|Channel
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
name|ChannelConfig
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
name|ChannelFactory
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
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|DefaultHttpHeaders
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
name|HttpHeaders
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
name|net
operator|.
name|SocketAddress
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
name|List
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
name|is
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|nullValue
import|;
end_import

begin_class
DECL|class|NettyHttpChannelTests
specifier|public
class|class
name|NettyHttpChannelTests
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
DECL|field|httpServerTransport
specifier|private
name|NettyHttpServerTransport
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
name|MockPageCacheRecycler
name|mockPageCacheRecycler
init|=
operator|new
name|MockPageCacheRecycler
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
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
DECL|method|testCorsEnabledWithoutAllowOrigins
specifier|public
name|void
name|testCorsEnabledWithoutAllowOrigins
parameter_list|()
block|{
comment|// Set up a HTTP transport with only the CORS enabled setting
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
name|HttpTransportSettings
operator|.
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
name|HttpResponse
name|response
init|=
name|execRequestWithCors
argument_list|(
name|settings
argument_list|,
literal|"remote-host"
argument_list|,
literal|"request-host"
argument_list|)
decl_stmt|;
comment|// inspect response and validate
name|assertThat
argument_list|(
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testCorsEnabledWithAllowOrigins
specifier|public
name|void
name|testCorsEnabledWithAllowOrigins
parameter_list|()
block|{
specifier|final
name|String
name|originValue
init|=
literal|"remote-host"
decl_stmt|;
comment|// create a http transport with CORS enabled and allow origin configured
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
name|originValue
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HttpResponse
name|response
init|=
name|execRequestWithCors
argument_list|(
name|settings
argument_list|,
name|originValue
argument_list|,
literal|"request-host"
argument_list|)
decl_stmt|;
comment|// inspect response and validate
name|assertThat
argument_list|(
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|allowedOrigins
init|=
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|allowedOrigins
argument_list|,
name|is
argument_list|(
name|originValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCorsAllowOriginWithSameHost
specifier|public
name|void
name|testCorsAllowOriginWithSameHost
parameter_list|()
block|{
name|String
name|originValue
init|=
literal|"remote-host"
decl_stmt|;
name|String
name|host
init|=
literal|"remote-host"
decl_stmt|;
comment|// create a http transport with CORS enabled
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
name|HttpResponse
name|response
init|=
name|execRequestWithCors
argument_list|(
name|settings
argument_list|,
name|originValue
argument_list|,
name|host
argument_list|)
decl_stmt|;
comment|// inspect response and validate
name|assertThat
argument_list|(
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|allowedOrigins
init|=
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|allowedOrigins
argument_list|,
name|is
argument_list|(
name|originValue
argument_list|)
argument_list|)
expr_stmt|;
name|originValue
operator|=
literal|"http://"
operator|+
name|originValue
expr_stmt|;
name|response
operator|=
name|execRequestWithCors
argument_list|(
name|settings
argument_list|,
name|originValue
argument_list|,
name|host
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|allowedOrigins
operator|=
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|allowedOrigins
argument_list|,
name|is
argument_list|(
name|originValue
argument_list|)
argument_list|)
expr_stmt|;
name|originValue
operator|=
name|originValue
operator|+
literal|":5555"
expr_stmt|;
name|host
operator|=
name|host
operator|+
literal|":5555"
expr_stmt|;
name|response
operator|=
name|execRequestWithCors
argument_list|(
name|settings
argument_list|,
name|originValue
argument_list|,
name|host
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|allowedOrigins
operator|=
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|allowedOrigins
argument_list|,
name|is
argument_list|(
name|originValue
argument_list|)
argument_list|)
expr_stmt|;
name|originValue
operator|=
name|originValue
operator|.
name|replace
argument_list|(
literal|"http"
argument_list|,
literal|"https"
argument_list|)
expr_stmt|;
name|response
operator|=
name|execRequestWithCors
argument_list|(
name|settings
argument_list|,
name|originValue
argument_list|,
name|host
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|allowedOrigins
operator|=
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|allowedOrigins
argument_list|,
name|is
argument_list|(
name|originValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatStringLiteralWorksOnMatch
specifier|public
name|void
name|testThatStringLiteralWorksOnMatch
parameter_list|()
block|{
specifier|final
name|String
name|originValue
init|=
literal|"remote-host"
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
name|originValue
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_CORS_ALLOW_METHODS
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"get, options, post"
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
name|HttpResponse
name|response
init|=
name|execRequestWithCors
argument_list|(
name|settings
argument_list|,
name|originValue
argument_list|,
literal|"request-host"
argument_list|)
decl_stmt|;
comment|// inspect response and validate
name|assertThat
argument_list|(
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|allowedOrigins
init|=
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|allowedOrigins
argument_list|,
name|is
argument_list|(
name|originValue
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|ACCESS_CONTROL_ALLOW_CREDENTIALS
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"true"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatAnyOriginWorks
specifier|public
name|void
name|testThatAnyOriginWorks
parameter_list|()
block|{
specifier|final
name|String
name|originValue
init|=
name|CorsHandler
operator|.
name|ANY_ORIGIN
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
name|originValue
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HttpResponse
name|response
init|=
name|execRequestWithCors
argument_list|(
name|settings
argument_list|,
name|originValue
argument_list|,
literal|"request-host"
argument_list|)
decl_stmt|;
comment|// inspect response and validate
name|assertThat
argument_list|(
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|allowedOrigins
init|=
name|response
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
name|ACCESS_CONTROL_ALLOW_ORIGIN
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|allowedOrigins
argument_list|,
name|is
argument_list|(
name|originValue
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|ACCESS_CONTROL_ALLOW_CREDENTIALS
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testHeadersSet
specifier|public
name|void
name|testHeadersSet
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|httpServerTransport
operator|=
operator|new
name|NettyHttpServerTransport
argument_list|(
name|settings
argument_list|,
name|networkService
argument_list|,
name|bigArrays
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|HttpRequest
name|httpRequest
init|=
operator|new
name|TestHttpRequest
argument_list|()
decl_stmt|;
name|httpRequest
operator|.
name|headers
argument_list|()
operator|.
name|add
argument_list|(
name|HttpHeaders
operator|.
name|Names
operator|.
name|ORIGIN
argument_list|,
literal|"remote"
argument_list|)
expr_stmt|;
name|WriteCapturingChannel
name|writeCapturingChannel
init|=
operator|new
name|WriteCapturingChannel
argument_list|()
decl_stmt|;
name|NettyHttpRequest
name|request
init|=
operator|new
name|NettyHttpRequest
argument_list|(
name|httpRequest
argument_list|,
name|writeCapturingChannel
argument_list|)
decl_stmt|;
comment|// send a response
name|NettyHttpChannel
name|channel
init|=
operator|new
name|NettyHttpChannel
argument_list|(
name|httpServerTransport
argument_list|,
name|request
argument_list|,
literal|null
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
decl_stmt|;
name|TestReponse
name|resp
init|=
operator|new
name|TestReponse
argument_list|()
decl_stmt|;
specifier|final
name|String
name|customHeader
init|=
literal|"custom-header"
decl_stmt|;
specifier|final
name|String
name|customHeaderValue
init|=
literal|"xyz"
decl_stmt|;
name|resp
operator|.
name|addHeader
argument_list|(
name|customHeader
argument_list|,
name|customHeaderValue
argument_list|)
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|resp
argument_list|)
expr_stmt|;
comment|// inspect what was written
name|List
argument_list|<
name|Object
argument_list|>
name|writtenObjects
init|=
name|writeCapturingChannel
operator|.
name|getWrittenObjects
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|writtenObjects
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|HttpResponse
name|response
init|=
operator|(
name|HttpResponse
operator|)
name|writtenObjects
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|headers
argument_list|()
operator|.
name|get
argument_list|(
literal|"non-existent-header"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|headers
argument_list|()
operator|.
name|get
argument_list|(
name|customHeader
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|customHeaderValue
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|CONTENT_LENGTH
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|resp
operator|.
name|content
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|CONTENT_TYPE
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|resp
operator|.
name|contentType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|execRequestWithCors
specifier|private
name|HttpResponse
name|execRequestWithCors
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|,
specifier|final
name|String
name|originValue
parameter_list|,
specifier|final
name|String
name|host
parameter_list|)
block|{
comment|// construct request and send it over the transport layer
name|httpServerTransport
operator|=
operator|new
name|NettyHttpServerTransport
argument_list|(
name|settings
argument_list|,
name|networkService
argument_list|,
name|bigArrays
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|HttpRequest
name|httpRequest
init|=
operator|new
name|TestHttpRequest
argument_list|()
decl_stmt|;
name|httpRequest
operator|.
name|headers
argument_list|()
operator|.
name|add
argument_list|(
name|HttpHeaders
operator|.
name|Names
operator|.
name|ORIGIN
argument_list|,
name|originValue
argument_list|)
expr_stmt|;
name|httpRequest
operator|.
name|headers
argument_list|()
operator|.
name|add
argument_list|(
name|HttpHeaders
operator|.
name|Names
operator|.
name|HOST
argument_list|,
name|host
argument_list|)
expr_stmt|;
name|WriteCapturingChannel
name|writeCapturingChannel
init|=
operator|new
name|WriteCapturingChannel
argument_list|()
decl_stmt|;
name|NettyHttpRequest
name|request
init|=
operator|new
name|NettyHttpRequest
argument_list|(
name|httpRequest
argument_list|,
name|writeCapturingChannel
argument_list|)
decl_stmt|;
name|NettyHttpChannel
name|channel
init|=
operator|new
name|NettyHttpChannel
argument_list|(
name|httpServerTransport
argument_list|,
name|request
argument_list|,
literal|null
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|TestReponse
argument_list|()
argument_list|)
expr_stmt|;
comment|// get the response
name|List
argument_list|<
name|Object
argument_list|>
name|writtenObjects
init|=
name|writeCapturingChannel
operator|.
name|getWrittenObjects
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|writtenObjects
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|(
name|HttpResponse
operator|)
name|writtenObjects
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
DECL|class|WriteCapturingChannel
specifier|private
specifier|static
class|class
name|WriteCapturingChannel
implements|implements
name|Channel
block|{
DECL|field|writtenObjects
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|writtenObjects
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|getId
specifier|public
name|Integer
name|getId
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getFactory
specifier|public
name|ChannelFactory
name|getFactory
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getParent
specifier|public
name|Channel
name|getParent
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getConfig
specifier|public
name|ChannelConfig
name|getConfig
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getPipeline
specifier|public
name|ChannelPipeline
name|getPipeline
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|isOpen
specifier|public
name|boolean
name|isOpen
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|isBound
specifier|public
name|boolean
name|isBound
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|isConnected
specifier|public
name|boolean
name|isConnected
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getLocalAddress
specifier|public
name|SocketAddress
name|getLocalAddress
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getRemoteAddress
specifier|public
name|SocketAddress
name|getRemoteAddress
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|write
specifier|public
name|ChannelFuture
name|write
parameter_list|(
name|Object
name|message
parameter_list|)
block|{
name|writtenObjects
operator|.
name|add
argument_list|(
name|message
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|write
specifier|public
name|ChannelFuture
name|write
parameter_list|(
name|Object
name|message
parameter_list|,
name|SocketAddress
name|remoteAddress
parameter_list|)
block|{
name|writtenObjects
operator|.
name|add
argument_list|(
name|message
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|bind
specifier|public
name|ChannelFuture
name|bind
parameter_list|(
name|SocketAddress
name|localAddress
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|connect
specifier|public
name|ChannelFuture
name|connect
parameter_list|(
name|SocketAddress
name|remoteAddress
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|disconnect
specifier|public
name|ChannelFuture
name|disconnect
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|unbind
specifier|public
name|ChannelFuture
name|unbind
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|ChannelFuture
name|close
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getCloseFuture
specifier|public
name|ChannelFuture
name|getCloseFuture
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getInterestOps
specifier|public
name|int
name|getInterestOps
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|isReadable
specifier|public
name|boolean
name|isReadable
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|isWritable
specifier|public
name|boolean
name|isWritable
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|setInterestOps
specifier|public
name|ChannelFuture
name|setInterestOps
parameter_list|(
name|int
name|interestOps
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|setReadable
specifier|public
name|ChannelFuture
name|setReadable
parameter_list|(
name|boolean
name|readable
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getUserDefinedWritability
specifier|public
name|boolean
name|getUserDefinedWritability
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|setUserDefinedWritability
specifier|public
name|void
name|setUserDefinedWritability
parameter_list|(
name|int
name|index
parameter_list|,
name|boolean
name|isWritable
parameter_list|)
block|{          }
annotation|@
name|Override
DECL|method|getAttachment
specifier|public
name|Object
name|getAttachment
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|setAttachment
specifier|public
name|void
name|setAttachment
parameter_list|(
name|Object
name|attachment
parameter_list|)
block|{          }
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|Channel
name|o
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
DECL|method|getWrittenObjects
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|getWrittenObjects
parameter_list|()
block|{
return|return
name|writtenObjects
return|;
block|}
block|}
DECL|class|TestHttpRequest
specifier|private
specifier|static
class|class
name|TestHttpRequest
implements|implements
name|HttpRequest
block|{
DECL|field|headers
specifier|private
name|HttpHeaders
name|headers
init|=
operator|new
name|DefaultHttpHeaders
argument_list|()
decl_stmt|;
DECL|field|content
specifier|private
name|ChannelBuffer
name|content
init|=
name|ChannelBuffers
operator|.
name|EMPTY_BUFFER
decl_stmt|;
annotation|@
name|Override
DECL|method|getMethod
specifier|public
name|HttpMethod
name|getMethod
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|setMethod
specifier|public
name|void
name|setMethod
parameter_list|(
name|HttpMethod
name|method
parameter_list|)
block|{          }
annotation|@
name|Override
DECL|method|getUri
specifier|public
name|String
name|getUri
parameter_list|()
block|{
return|return
literal|""
return|;
block|}
annotation|@
name|Override
DECL|method|setUri
specifier|public
name|void
name|setUri
parameter_list|(
name|String
name|uri
parameter_list|)
block|{          }
annotation|@
name|Override
DECL|method|getProtocolVersion
specifier|public
name|HttpVersion
name|getProtocolVersion
parameter_list|()
block|{
return|return
name|HttpVersion
operator|.
name|HTTP_1_1
return|;
block|}
annotation|@
name|Override
DECL|method|setProtocolVersion
specifier|public
name|void
name|setProtocolVersion
parameter_list|(
name|HttpVersion
name|version
parameter_list|)
block|{          }
annotation|@
name|Override
DECL|method|headers
specifier|public
name|HttpHeaders
name|headers
parameter_list|()
block|{
return|return
name|headers
return|;
block|}
annotation|@
name|Override
DECL|method|getContent
specifier|public
name|ChannelBuffer
name|getContent
parameter_list|()
block|{
return|return
name|content
return|;
block|}
annotation|@
name|Override
DECL|method|setContent
specifier|public
name|void
name|setContent
parameter_list|(
name|ChannelBuffer
name|content
parameter_list|)
block|{
name|this
operator|.
name|content
operator|=
name|content
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isChunked
specifier|public
name|boolean
name|isChunked
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|setChunked
specifier|public
name|void
name|setChunked
parameter_list|(
name|boolean
name|chunked
parameter_list|)
block|{          }
block|}
DECL|class|TestReponse
specifier|private
specifier|static
class|class
name|TestReponse
extends|extends
name|RestResponse
block|{
annotation|@
name|Override
DECL|method|contentType
specifier|public
name|String
name|contentType
parameter_list|()
block|{
return|return
literal|"text"
return|;
block|}
annotation|@
name|Override
DECL|method|content
specifier|public
name|BytesReference
name|content
parameter_list|()
block|{
return|return
name|BytesArray
operator|.
name|EMPTY
return|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
return|return
name|RestStatus
operator|.
name|OK
return|;
block|}
block|}
block|}
end_class

end_unit

