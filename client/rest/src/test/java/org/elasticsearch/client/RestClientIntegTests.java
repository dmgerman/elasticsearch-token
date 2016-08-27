begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomInts
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomStrings
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|net
operator|.
name|httpserver
operator|.
name|Headers
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|net
operator|.
name|httpserver
operator|.
name|HttpContext
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|net
operator|.
name|httpserver
operator|.
name|HttpExchange
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|net
operator|.
name|httpserver
operator|.
name|HttpHandler
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|net
operator|.
name|httpserver
operator|.
name|HttpServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|Consts
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|Header
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpHost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|entity
operator|.
name|StringEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|message
operator|.
name|BasicHeader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|util
operator|.
name|EntityUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|mojo
operator|.
name|animal_sniffer
operator|.
name|IgnoreJRERequirement
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
name|HashMap
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArrayList
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
name|client
operator|.
name|RestClientTestUtil
operator|.
name|getAllStatusCodes
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|RestClientTestUtil
operator|.
name|getHttpMethods
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|RestClientTestUtil
operator|.
name|randomStatusCode
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
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertThat
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_comment
comment|/**  * Integration test to check interaction between {@link RestClient} and {@link org.apache.http.client.HttpClient}.  * Works against a real http server, one single host.  */
end_comment

begin_comment
comment|//animal-sniffer doesn't like our usage of com.sun.net.httpserver.* classes
end_comment

begin_class
annotation|@
name|IgnoreJRERequirement
DECL|class|RestClientIntegTests
specifier|public
class|class
name|RestClientIntegTests
extends|extends
name|RestClientTestCase
block|{
DECL|field|httpServer
specifier|private
specifier|static
name|HttpServer
name|httpServer
decl_stmt|;
DECL|field|restClient
specifier|private
specifier|static
name|RestClient
name|restClient
decl_stmt|;
DECL|field|defaultHeaders
specifier|private
specifier|static
name|Header
index|[]
name|defaultHeaders
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|startHttpServer
specifier|public
specifier|static
name|void
name|startHttpServer
parameter_list|()
throws|throws
name|Exception
block|{
name|httpServer
operator|=
name|HttpServer
operator|.
name|create
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|InetAddress
operator|.
name|getLoopbackAddress
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|httpServer
operator|.
name|start
argument_list|()
expr_stmt|;
comment|//returns a different status code depending on the path
for|for
control|(
name|int
name|statusCode
range|:
name|getAllStatusCodes
argument_list|()
control|)
block|{
name|createStatusCodeContext
argument_list|(
name|httpServer
argument_list|,
name|statusCode
argument_list|)
expr_stmt|;
block|}
name|int
name|numHeaders
init|=
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|defaultHeaders
operator|=
operator|new
name|Header
index|[
name|numHeaders
index|]
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
name|numHeaders
condition|;
name|i
operator|++
control|)
block|{
name|String
name|headerName
init|=
literal|"Header-default"
operator|+
operator|(
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
name|i
else|:
literal|""
operator|)
decl_stmt|;
name|String
name|headerValue
init|=
name|RandomStrings
operator|.
name|randomAsciiOfLengthBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
literal|3
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|defaultHeaders
index|[
name|i
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
name|headerName
argument_list|,
name|headerValue
argument_list|)
expr_stmt|;
block|}
name|restClient
operator|=
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
name|httpServer
operator|.
name|getAddress
argument_list|()
operator|.
name|getHostString
argument_list|()
argument_list|,
name|httpServer
operator|.
name|getAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setDefaultHeaders
argument_list|(
name|defaultHeaders
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
DECL|method|createStatusCodeContext
specifier|private
specifier|static
name|void
name|createStatusCodeContext
parameter_list|(
name|HttpServer
name|httpServer
parameter_list|,
specifier|final
name|int
name|statusCode
parameter_list|)
block|{
name|httpServer
operator|.
name|createContext
argument_list|(
literal|"/"
operator|+
name|statusCode
argument_list|,
operator|new
name|ResponseHandler
argument_list|(
name|statusCode
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//animal-sniffer doesn't like our usage of com.sun.net.httpserver.* classes
annotation|@
name|IgnoreJRERequirement
DECL|class|ResponseHandler
specifier|private
specifier|static
class|class
name|ResponseHandler
implements|implements
name|HttpHandler
block|{
DECL|field|statusCode
specifier|private
specifier|final
name|int
name|statusCode
decl_stmt|;
DECL|method|ResponseHandler
name|ResponseHandler
parameter_list|(
name|int
name|statusCode
parameter_list|)
block|{
name|this
operator|.
name|statusCode
operator|=
name|statusCode
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handle
specifier|public
name|void
name|handle
parameter_list|(
name|HttpExchange
name|httpExchange
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|body
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
try|try
init|(
name|InputStreamReader
name|reader
init|=
operator|new
name|InputStreamReader
argument_list|(
name|httpExchange
operator|.
name|getRequestBody
argument_list|()
argument_list|,
name|Consts
operator|.
name|UTF_8
argument_list|)
init|)
block|{
name|char
index|[]
name|buffer
init|=
operator|new
name|char
index|[
literal|256
index|]
decl_stmt|;
name|int
name|read
decl_stmt|;
while|while
condition|(
operator|(
name|read
operator|=
name|reader
operator|.
name|read
argument_list|(
name|buffer
argument_list|)
operator|)
operator|!=
operator|-
literal|1
condition|)
block|{
name|body
operator|.
name|append
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|read
argument_list|)
expr_stmt|;
block|}
block|}
name|Headers
name|requestHeaders
init|=
name|httpExchange
operator|.
name|getRequestHeaders
argument_list|()
decl_stmt|;
name|Headers
name|responseHeaders
init|=
name|httpExchange
operator|.
name|getResponseHeaders
argument_list|()
decl_stmt|;
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
name|header
range|:
name|requestHeaders
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|responseHeaders
operator|.
name|put
argument_list|(
name|header
operator|.
name|getKey
argument_list|()
argument_list|,
name|header
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|httpExchange
operator|.
name|getRequestBody
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
name|httpExchange
operator|.
name|sendResponseHeaders
argument_list|(
name|statusCode
argument_list|,
name|body
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|?
operator|-
literal|1
else|:
name|body
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|body
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
try|try
init|(
name|OutputStream
name|out
init|=
name|httpExchange
operator|.
name|getResponseBody
argument_list|()
init|)
block|{
name|out
operator|.
name|write
argument_list|(
name|body
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|Consts
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|httpExchange
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|AfterClass
DECL|method|stopHttpServers
specifier|public
specifier|static
name|void
name|stopHttpServers
parameter_list|()
throws|throws
name|IOException
block|{
name|restClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|restClient
operator|=
literal|null
expr_stmt|;
name|httpServer
operator|.
name|stop
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|httpServer
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * End to end test for headers. We test it explicitly against a real http client as there are different ways      * to set/add headers to the {@link org.apache.http.client.HttpClient}.      * Exercises the test http server ability to send back whatever headers it received.      */
DECL|method|testHeaders
specifier|public
name|void
name|testHeaders
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|String
name|method
range|:
name|getHttpMethods
argument_list|()
control|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|standardHeaders
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"Connection"
argument_list|,
literal|"Host"
argument_list|,
literal|"User-agent"
argument_list|,
literal|"Date"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|method
operator|.
name|equals
argument_list|(
literal|"HEAD"
argument_list|)
operator|==
literal|false
condition|)
block|{
name|standardHeaders
operator|.
name|add
argument_list|(
literal|"Content-length"
argument_list|)
expr_stmt|;
block|}
name|int
name|numHeaders
init|=
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|expectedHeaders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Header
name|defaultHeader
range|:
name|defaultHeaders
control|)
block|{
name|expectedHeaders
operator|.
name|put
argument_list|(
name|defaultHeader
operator|.
name|getName
argument_list|()
argument_list|,
name|defaultHeader
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Header
index|[]
name|headers
init|=
operator|new
name|Header
index|[
name|numHeaders
index|]
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
name|numHeaders
condition|;
name|i
operator|++
control|)
block|{
name|String
name|headerName
init|=
literal|"Header"
operator|+
operator|(
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
name|i
else|:
literal|""
operator|)
decl_stmt|;
name|String
name|headerValue
init|=
name|RandomStrings
operator|.
name|randomAsciiOfLengthBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
literal|3
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|headers
index|[
name|i
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
name|headerName
argument_list|,
name|headerValue
argument_list|)
expr_stmt|;
name|expectedHeaders
operator|.
name|put
argument_list|(
name|headerName
argument_list|,
name|headerValue
argument_list|)
expr_stmt|;
block|}
name|int
name|statusCode
init|=
name|randomStatusCode
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|Response
name|esResponse
decl_stmt|;
try|try
block|{
name|esResponse
operator|=
name|restClient
operator|.
name|performRequest
argument_list|(
name|method
argument_list|,
literal|"/"
operator|+
name|statusCode
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|emptyMap
argument_list|()
argument_list|,
operator|(
name|HttpEntity
operator|)
literal|null
argument_list|,
name|headers
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
name|esResponse
operator|=
name|e
operator|.
name|getResponse
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|esResponse
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|statusCode
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Header
name|responseHeader
range|:
name|esResponse
operator|.
name|getHeaders
argument_list|()
control|)
block|{
if|if
condition|(
name|responseHeader
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"Header"
argument_list|)
condition|)
block|{
name|String
name|headerValue
init|=
name|expectedHeaders
operator|.
name|remove
argument_list|(
name|responseHeader
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"found response header ["
operator|+
name|responseHeader
operator|.
name|getName
argument_list|()
operator|+
literal|"] that wasn't originally sent"
argument_list|,
name|headerValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
literal|"unknown header was returned "
operator|+
name|responseHeader
operator|.
name|getName
argument_list|()
argument_list|,
name|standardHeaders
operator|.
name|remove
argument_list|(
name|responseHeader
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"some headers that were sent weren't returned: "
operator|+
name|expectedHeaders
argument_list|,
literal|0
argument_list|,
name|expectedHeaders
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"some expected standard headers weren't returned: "
operator|+
name|standardHeaders
argument_list|,
literal|0
argument_list|,
name|standardHeaders
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * End to end test for delete with body. We test it explicitly as it is not supported      * out of the box by {@link org.apache.http.client.HttpClient}.      * Exercises the test http server ability to send back whatever body it received.      */
DECL|method|testDeleteWithBody
specifier|public
name|void
name|testDeleteWithBody
parameter_list|()
throws|throws
name|IOException
block|{
name|bodyTest
argument_list|(
literal|"DELETE"
argument_list|)
expr_stmt|;
block|}
comment|/**      * End to end test for get with body. We test it explicitly as it is not supported      * out of the box by {@link org.apache.http.client.HttpClient}.      * Exercises the test http server ability to send back whatever body it received.      */
DECL|method|testGetWithBody
specifier|public
name|void
name|testGetWithBody
parameter_list|()
throws|throws
name|IOException
block|{
name|bodyTest
argument_list|(
literal|"GET"
argument_list|)
expr_stmt|;
block|}
comment|/**      * Ensure that pathPrefix works as expected even when the path does not exist.      */
DECL|method|testPathPrefixUnknownPath
specifier|public
name|void
name|testPathPrefixUnknownPath
parameter_list|()
throws|throws
name|IOException
block|{
comment|// guarantee no other test setup collides with this one and lets it sneak through
specifier|final
name|String
name|uniqueContextSuffix
init|=
literal|"/testPathPrefixUnknownPath"
decl_stmt|;
specifier|final
name|String
name|pathPrefix
init|=
literal|"dne/"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
operator|+
literal|"/"
decl_stmt|;
specifier|final
name|int
name|statusCode
init|=
name|randomStatusCode
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
specifier|final
name|RestClient
name|client
init|=
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
name|httpServer
operator|.
name|getAddress
argument_list|()
operator|.
name|getHostString
argument_list|()
argument_list|,
name|httpServer
operator|.
name|getAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setPathPrefix
argument_list|(
operator|(
name|randomBoolean
argument_list|()
condition|?
literal|"/"
else|:
literal|""
operator|)
operator|+
name|pathPrefix
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
for|for
control|(
specifier|final
name|String
name|method
range|:
name|getHttpMethods
argument_list|()
control|)
block|{
name|Response
name|esResponse
decl_stmt|;
try|try
block|{
name|esResponse
operator|=
name|client
operator|.
name|performRequest
argument_list|(
name|method
argument_list|,
literal|"/"
operator|+
name|statusCode
operator|+
name|uniqueContextSuffix
argument_list|)
expr_stmt|;
if|if
condition|(
literal|"HEAD"
operator|.
name|equals
argument_list|(
name|method
argument_list|)
operator|==
literal|false
condition|)
block|{
name|fail
argument_list|(
literal|"only HEAD requests should not throw an exception; 404 is expected"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
name|esResponse
operator|=
name|e
operator|.
name|getResponse
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|esResponse
operator|.
name|getRequestLine
argument_list|()
operator|.
name|getUri
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"/"
operator|+
name|pathPrefix
operator|+
name|statusCode
operator|+
name|uniqueContextSuffix
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|esResponse
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|404
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Ensure that pathPrefix works as expected.      */
DECL|method|testPathPrefix
specifier|public
name|void
name|testPathPrefix
parameter_list|()
throws|throws
name|IOException
block|{
comment|// guarantee no other test setup collides with this one and lets it sneak through
specifier|final
name|String
name|uniqueContextSuffix
init|=
literal|"/testPathPrefix"
decl_stmt|;
specifier|final
name|String
name|pathPrefix
init|=
literal|"base/"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
operator|+
literal|"/"
decl_stmt|;
specifier|final
name|int
name|statusCode
init|=
name|randomStatusCode
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|HttpContext
name|context
init|=
name|httpServer
operator|.
name|createContext
argument_list|(
literal|"/"
operator|+
name|pathPrefix
operator|+
name|statusCode
operator|+
name|uniqueContextSuffix
argument_list|,
operator|new
name|ResponseHandler
argument_list|(
name|statusCode
argument_list|)
argument_list|)
decl_stmt|;
try|try
init|(
specifier|final
name|RestClient
name|client
init|=
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
name|httpServer
operator|.
name|getAddress
argument_list|()
operator|.
name|getHostString
argument_list|()
argument_list|,
name|httpServer
operator|.
name|getAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setPathPrefix
argument_list|(
operator|(
name|randomBoolean
argument_list|()
condition|?
literal|"/"
else|:
literal|""
operator|)
operator|+
name|pathPrefix
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
for|for
control|(
specifier|final
name|String
name|method
range|:
name|getHttpMethods
argument_list|()
control|)
block|{
name|Response
name|esResponse
decl_stmt|;
try|try
block|{
name|esResponse
operator|=
name|client
operator|.
name|performRequest
argument_list|(
name|method
argument_list|,
literal|"/"
operator|+
name|statusCode
operator|+
name|uniqueContextSuffix
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
name|esResponse
operator|=
name|e
operator|.
name|getResponse
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|esResponse
operator|.
name|getRequestLine
argument_list|()
operator|.
name|getUri
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"/"
operator|+
name|pathPrefix
operator|+
name|statusCode
operator|+
name|uniqueContextSuffix
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|esResponse
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|statusCode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|httpServer
operator|.
name|removeContext
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|bodyTest
specifier|private
name|void
name|bodyTest
parameter_list|(
name|String
name|method
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|requestBody
init|=
literal|"{ \"field\": \"value\" }"
decl_stmt|;
name|StringEntity
name|entity
init|=
operator|new
name|StringEntity
argument_list|(
name|requestBody
argument_list|)
decl_stmt|;
name|int
name|statusCode
init|=
name|randomStatusCode
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|Response
name|esResponse
decl_stmt|;
try|try
block|{
name|esResponse
operator|=
name|restClient
operator|.
name|performRequest
argument_list|(
name|method
argument_list|,
literal|"/"
operator|+
name|statusCode
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|emptyMap
argument_list|()
argument_list|,
name|entity
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
name|esResponse
operator|=
name|e
operator|.
name|getResponse
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|statusCode
argument_list|,
name|esResponse
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|requestBody
argument_list|,
name|EntityUtils
operator|.
name|toString
argument_list|(
name|esResponse
operator|.
name|getEntity
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAsyncRequests
specifier|public
name|void
name|testAsyncRequests
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numRequests
init|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|numRequests
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TestResponse
argument_list|>
name|responses
init|=
operator|new
name|CopyOnWriteArrayList
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
name|numRequests
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|String
name|method
init|=
name|RestClientTestUtil
operator|.
name|randomHttpMethod
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|statusCode
init|=
name|randomStatusCode
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|restClient
operator|.
name|performRequestAsync
argument_list|(
name|method
argument_list|,
literal|"/"
operator|+
name|statusCode
argument_list|,
operator|new
name|ResponseListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Response
name|response
parameter_list|)
block|{
name|responses
operator|.
name|add
argument_list|(
operator|new
name|TestResponse
argument_list|(
name|method
argument_list|,
name|statusCode
argument_list|,
name|response
argument_list|)
argument_list|)
expr_stmt|;
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
name|onFailure
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
name|responses
operator|.
name|add
argument_list|(
operator|new
name|TestResponse
argument_list|(
name|method
argument_list|,
name|statusCode
argument_list|,
name|exception
argument_list|)
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
expr_stmt|;
block|}
name|assertTrue
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
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numRequests
argument_list|,
name|responses
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|TestResponse
name|response
range|:
name|responses
control|)
block|{
name|assertEquals
argument_list|(
name|response
operator|.
name|method
argument_list|,
name|response
operator|.
name|getResponse
argument_list|()
operator|.
name|getRequestLine
argument_list|()
operator|.
name|getMethod
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|statusCode
argument_list|,
name|response
operator|.
name|getResponse
argument_list|()
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TestResponse
specifier|private
specifier|static
class|class
name|TestResponse
block|{
DECL|field|method
specifier|private
specifier|final
name|String
name|method
decl_stmt|;
DECL|field|statusCode
specifier|private
specifier|final
name|int
name|statusCode
decl_stmt|;
DECL|field|response
specifier|private
specifier|final
name|Object
name|response
decl_stmt|;
DECL|method|TestResponse
name|TestResponse
parameter_list|(
name|String
name|method
parameter_list|,
name|int
name|statusCode
parameter_list|,
name|Object
name|response
parameter_list|)
block|{
name|this
operator|.
name|method
operator|=
name|method
expr_stmt|;
name|this
operator|.
name|statusCode
operator|=
name|statusCode
expr_stmt|;
name|this
operator|.
name|response
operator|=
name|response
expr_stmt|;
block|}
DECL|method|getResponse
name|Response
name|getResponse
parameter_list|()
block|{
if|if
condition|(
name|response
operator|instanceof
name|Response
condition|)
block|{
return|return
operator|(
name|Response
operator|)
name|response
return|;
block|}
if|if
condition|(
name|response
operator|instanceof
name|ResponseException
condition|)
block|{
return|return
operator|(
operator|(
name|ResponseException
operator|)
name|response
operator|)
operator|.
name|getResponse
argument_list|()
return|;
block|}
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"unexpected response "
operator|+
name|response
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

