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
name|HttpRequest
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
name|ProtocolVersion
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
name|StatusLine
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
name|client
operator|.
name|methods
operator|.
name|CloseableHttpResponse
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
name|client
operator|.
name|methods
operator|.
name|HttpUriRequest
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
name|conn
operator|.
name|ConnectTimeoutException
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
name|impl
operator|.
name|client
operator|.
name|CloseableHttpClient
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
name|BasicStatusLine
import|;
end_import

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
name|LuceneTestCase
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
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
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
name|net
operator|.
name|SocketTimeoutException
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|RestClientTestUtil
operator|.
name|randomErrorNoRetryStatusCode
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
name|randomErrorRetryStatusCode
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
name|randomHttpMethod
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
name|randomOkStatusCode
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
name|CoreMatchers
operator|.
name|instanceOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_comment
comment|/**  * Tests for {@link RestClient} behaviour against multiple hosts: fail-over, blacklisting etc.  * Relies on a mock http client to intercept requests and return desired responses based on request path.  */
end_comment

begin_class
DECL|class|RestClientMultipleHostsTests
specifier|public
class|class
name|RestClientMultipleHostsTests
extends|extends
name|LuceneTestCase
block|{
DECL|field|restClient
specifier|private
name|RestClient
name|restClient
decl_stmt|;
DECL|field|httpHosts
specifier|private
name|HttpHost
index|[]
name|httpHosts
decl_stmt|;
DECL|field|failureListener
specifier|private
name|TrackingFailureListener
name|failureListener
decl_stmt|;
annotation|@
name|Before
DECL|method|createRestClient
specifier|public
name|void
name|createRestClient
parameter_list|()
throws|throws
name|IOException
block|{
name|CloseableHttpClient
name|httpClient
init|=
name|mock
argument_list|(
name|CloseableHttpClient
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|httpClient
operator|.
name|execute
argument_list|(
name|any
argument_list|(
name|HttpHost
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|HttpRequest
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|CloseableHttpResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|CloseableHttpResponse
name|answer
parameter_list|(
name|InvocationOnMock
name|invocationOnMock
parameter_list|)
throws|throws
name|Throwable
block|{
name|HttpHost
name|httpHost
init|=
operator|(
name|HttpHost
operator|)
name|invocationOnMock
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|HttpUriRequest
name|request
init|=
operator|(
name|HttpUriRequest
operator|)
name|invocationOnMock
operator|.
name|getArguments
argument_list|()
index|[
literal|1
index|]
decl_stmt|;
comment|//return the desired status code or exception depending on the path
if|if
condition|(
name|request
operator|.
name|getURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|.
name|equals
argument_list|(
literal|"/soe"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SocketTimeoutException
argument_list|(
name|httpHost
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|getURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|.
name|equals
argument_list|(
literal|"/coe"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ConnectTimeoutException
argument_list|(
name|httpHost
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|getURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|.
name|equals
argument_list|(
literal|"/ioe"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|httpHost
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|int
name|statusCode
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|request
operator|.
name|getURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|StatusLine
name|statusLine
init|=
operator|new
name|BasicStatusLine
argument_list|(
operator|new
name|ProtocolVersion
argument_list|(
literal|"http"
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
argument_list|,
name|statusCode
argument_list|,
literal|""
argument_list|)
decl_stmt|;
return|return
operator|new
name|CloseableBasicHttpResponse
argument_list|(
name|statusLine
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|int
name|numHosts
init|=
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|2
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|httpHosts
operator|=
operator|new
name|HttpHost
index|[
name|numHosts
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
name|numHosts
condition|;
name|i
operator|++
control|)
block|{
name|httpHosts
index|[
name|i
index|]
operator|=
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|failureListener
operator|=
operator|new
name|TrackingFailureListener
argument_list|()
expr_stmt|;
name|restClient
operator|=
name|RestClient
operator|.
name|builder
argument_list|(
name|httpHosts
argument_list|)
operator|.
name|setHttpClient
argument_list|(
name|httpClient
argument_list|)
operator|.
name|setFailureListener
argument_list|(
name|failureListener
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
DECL|method|testRoundRobinOkStatusCodes
specifier|public
name|void
name|testRoundRobinOkStatusCodes
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numIters
init|=
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|5
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
name|numIters
condition|;
name|i
operator|++
control|)
block|{
name|Set
argument_list|<
name|HttpHost
argument_list|>
name|hostsSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|hostsSet
argument_list|,
name|httpHosts
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|httpHosts
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|int
name|statusCode
init|=
name|randomOkStatusCode
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|Response
name|response
init|=
name|restClient
operator|.
name|performRequest
argument_list|(
name|randomHttpMethod
argument_list|(
name|random
argument_list|()
argument_list|)
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
literal|null
argument_list|)
init|)
block|{
name|assertThat
argument_list|(
name|response
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
name|assertTrue
argument_list|(
literal|"host not found: "
operator|+
name|response
operator|.
name|getHost
argument_list|()
argument_list|,
name|hostsSet
operator|.
name|remove
argument_list|(
name|response
operator|.
name|getHost
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"every host should have been used but some weren't: "
operator|+
name|hostsSet
argument_list|,
literal|0
argument_list|,
name|hostsSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|failureListener
operator|.
name|assertNotCalled
argument_list|()
expr_stmt|;
block|}
DECL|method|testRoundRobinNoRetryErrors
specifier|public
name|void
name|testRoundRobinNoRetryErrors
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numIters
init|=
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|5
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
name|numIters
condition|;
name|i
operator|++
control|)
block|{
name|Set
argument_list|<
name|HttpHost
argument_list|>
name|hostsSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|hostsSet
argument_list|,
name|httpHosts
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|httpHosts
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|String
name|method
init|=
name|randomHttpMethod
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|statusCode
init|=
name|randomErrorNoRetryStatusCode
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|Response
name|response
init|=
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
literal|null
argument_list|)
init|)
block|{
if|if
condition|(
name|method
operator|.
name|equals
argument_list|(
literal|"HEAD"
argument_list|)
operator|&&
name|statusCode
operator|==
literal|404
condition|)
block|{
comment|//no exception gets thrown although we got a 404
name|assertThat
argument_list|(
name|response
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
name|assertThat
argument_list|(
name|response
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
name|assertTrue
argument_list|(
literal|"host not found: "
operator|+
name|response
operator|.
name|getHost
argument_list|()
argument_list|,
name|hostsSet
operator|.
name|remove
argument_list|(
name|response
operator|.
name|getHost
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
literal|"request should have failed"
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
if|if
condition|(
name|method
operator|.
name|equals
argument_list|(
literal|"HEAD"
argument_list|)
operator|&&
name|statusCode
operator|==
literal|404
condition|)
block|{
throw|throw
name|e
throw|;
block|}
name|Response
name|response
init|=
name|e
operator|.
name|getResponse
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
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
name|assertTrue
argument_list|(
literal|"host not found: "
operator|+
name|response
operator|.
name|getHost
argument_list|()
argument_list|,
name|hostsSet
operator|.
name|remove
argument_list|(
name|response
operator|.
name|getHost
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|e
operator|.
name|getSuppressed
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"every host should have been used but some weren't: "
operator|+
name|hostsSet
argument_list|,
literal|0
argument_list|,
name|hostsSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|failureListener
operator|.
name|assertNotCalled
argument_list|()
expr_stmt|;
block|}
DECL|method|testRoundRobinRetryErrors
specifier|public
name|void
name|testRoundRobinRetryErrors
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|retryEndpoint
init|=
name|randomErrorRetryEndpoint
argument_list|()
decl_stmt|;
try|try
block|{
name|restClient
operator|.
name|performRequest
argument_list|(
name|randomHttpMethod
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|,
name|retryEndpoint
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
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"request should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
name|Set
argument_list|<
name|HttpHost
argument_list|>
name|hostsSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|hostsSet
argument_list|,
name|httpHosts
argument_list|)
expr_stmt|;
comment|//first request causes all the hosts to be blacklisted, the returned exception holds one suppressed exception each
name|failureListener
operator|.
name|assertCalled
argument_list|(
name|httpHosts
argument_list|)
expr_stmt|;
do|do
block|{
name|Response
name|response
init|=
name|e
operator|.
name|getResponse
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|retryEndpoint
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"host ["
operator|+
name|response
operator|.
name|getHost
argument_list|()
operator|+
literal|"] not found, most likely used multiple times"
argument_list|,
name|hostsSet
operator|.
name|remove
argument_list|(
name|response
operator|.
name|getHost
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|e
operator|.
name|getSuppressed
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getSuppressed
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|Throwable
name|suppressed
init|=
name|e
operator|.
name|getSuppressed
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|suppressed
argument_list|,
name|instanceOf
argument_list|(
name|ResponseException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
operator|(
name|ResponseException
operator|)
name|suppressed
expr_stmt|;
block|}
else|else
block|{
name|e
operator|=
literal|null
expr_stmt|;
block|}
block|}
do|while
condition|(
name|e
operator|!=
literal|null
condition|)
do|;
name|assertEquals
argument_list|(
literal|"every host should have been used but some weren't: "
operator|+
name|hostsSet
argument_list|,
literal|0
argument_list|,
name|hostsSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Set
argument_list|<
name|HttpHost
argument_list|>
name|hostsSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|hostsSet
argument_list|,
name|httpHosts
argument_list|)
expr_stmt|;
comment|//first request causes all the hosts to be blacklisted, the returned exception holds one suppressed exception each
name|failureListener
operator|.
name|assertCalled
argument_list|(
name|httpHosts
argument_list|)
expr_stmt|;
do|do
block|{
name|HttpHost
name|httpHost
init|=
name|HttpHost
operator|.
name|create
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"host ["
operator|+
name|httpHost
operator|+
literal|"] not found, most likely used multiple times"
argument_list|,
name|hostsSet
operator|.
name|remove
argument_list|(
name|httpHost
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|e
operator|.
name|getSuppressed
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getSuppressed
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|Throwable
name|suppressed
init|=
name|e
operator|.
name|getSuppressed
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|suppressed
argument_list|,
name|instanceOf
argument_list|(
name|IOException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
operator|(
name|IOException
operator|)
name|suppressed
expr_stmt|;
block|}
else|else
block|{
name|e
operator|=
literal|null
expr_stmt|;
block|}
block|}
do|while
condition|(
name|e
operator|!=
literal|null
condition|)
do|;
name|assertEquals
argument_list|(
literal|"every host should have been used but some weren't: "
operator|+
name|hostsSet
argument_list|,
literal|0
argument_list|,
name|hostsSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|numIters
init|=
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|2
argument_list|,
literal|5
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|numIters
condition|;
name|i
operator|++
control|)
block|{
comment|//check that one different host is resurrected at each new attempt
name|Set
argument_list|<
name|HttpHost
argument_list|>
name|hostsSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|hostsSet
argument_list|,
name|httpHosts
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|httpHosts
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|retryEndpoint
operator|=
name|randomErrorRetryEndpoint
argument_list|()
expr_stmt|;
try|try
block|{
name|restClient
operator|.
name|performRequest
argument_list|(
name|randomHttpMethod
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|,
name|retryEndpoint
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
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"request should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
name|Response
name|response
init|=
name|e
operator|.
name|getResponse
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|retryEndpoint
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"host ["
operator|+
name|response
operator|.
name|getHost
argument_list|()
operator|+
literal|"] not found, most likely used multiple times"
argument_list|,
name|hostsSet
operator|.
name|remove
argument_list|(
name|response
operator|.
name|getHost
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|//after the first request, all hosts are blacklisted, a single one gets resurrected each time
name|failureListener
operator|.
name|assertCalled
argument_list|(
name|response
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|e
operator|.
name|getSuppressed
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|HttpHost
name|httpHost
init|=
name|HttpHost
operator|.
name|create
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"host ["
operator|+
name|httpHost
operator|+
literal|"] not found, most likely used multiple times"
argument_list|,
name|hostsSet
operator|.
name|remove
argument_list|(
name|httpHost
argument_list|)
argument_list|)
expr_stmt|;
comment|//after the first request, all hosts are blacklisted, a single one gets resurrected each time
name|failureListener
operator|.
name|assertCalled
argument_list|(
name|httpHost
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|e
operator|.
name|getSuppressed
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"every host should have been used but some weren't: "
operator|+
name|hostsSet
argument_list|,
literal|0
argument_list|,
name|hostsSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
comment|//mark one host back alive through a successful request and check that all requests after that are sent to it
name|HttpHost
name|selectedHost
init|=
literal|null
decl_stmt|;
name|int
name|iters
init|=
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|2
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|y
init|=
literal|0
init|;
name|y
operator|<
name|iters
condition|;
name|y
operator|++
control|)
block|{
name|int
name|statusCode
init|=
name|randomErrorNoRetryStatusCode
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|Response
name|response
decl_stmt|;
try|try
init|(
name|Response
name|esResponse
init|=
name|restClient
operator|.
name|performRequest
argument_list|(
name|randomHttpMethod
argument_list|(
name|random
argument_list|()
argument_list|)
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
literal|null
argument_list|)
init|)
block|{
name|response
operator|=
name|esResponse
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
name|response
operator|=
name|e
operator|.
name|getResponse
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|response
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
if|if
condition|(
name|selectedHost
operator|==
literal|null
condition|)
block|{
name|selectedHost
operator|=
name|response
operator|.
name|getHost
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|response
operator|.
name|getHost
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|selectedHost
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|failureListener
operator|.
name|assertNotCalled
argument_list|()
expr_stmt|;
comment|//let the selected host catch up on number of failures, it gets selected a consecutive number of times as it's the one
comment|//selected to be retried earlier (due to lower number of failures) till all the hosts have the same number of failures
for|for
control|(
name|int
name|y
init|=
literal|0
init|;
name|y
operator|<
name|i
operator|+
literal|1
condition|;
name|y
operator|++
control|)
block|{
name|retryEndpoint
operator|=
name|randomErrorRetryEndpoint
argument_list|()
expr_stmt|;
try|try
block|{
name|restClient
operator|.
name|performRequest
argument_list|(
name|randomHttpMethod
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|,
name|retryEndpoint
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
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"request should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
name|Response
name|response
init|=
name|e
operator|.
name|getResponse
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|retryEndpoint
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHost
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|selectedHost
argument_list|)
argument_list|)
expr_stmt|;
name|failureListener
operator|.
name|assertCalled
argument_list|(
name|selectedHost
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|HttpHost
name|httpHost
init|=
name|HttpHost
operator|.
name|create
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|httpHost
argument_list|,
name|equalTo
argument_list|(
name|selectedHost
argument_list|)
argument_list|)
expr_stmt|;
name|failureListener
operator|.
name|assertCalled
argument_list|(
name|selectedHost
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|randomErrorRetryEndpoint
specifier|private
specifier|static
name|String
name|randomErrorRetryEndpoint
parameter_list|()
block|{
switch|switch
condition|(
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|3
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
return|return
literal|"/"
operator|+
name|randomErrorRetryStatusCode
argument_list|(
name|random
argument_list|()
argument_list|)
return|;
case|case
literal|1
case|:
return|return
literal|"/coe"
return|;
case|case
literal|2
case|:
return|return
literal|"/soe"
return|;
case|case
literal|3
case|:
return|return
literal|"/ioe"
return|;
block|}
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

