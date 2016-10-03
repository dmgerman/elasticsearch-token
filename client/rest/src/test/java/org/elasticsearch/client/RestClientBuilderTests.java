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
name|client
operator|.
name|config
operator|.
name|RequestConfig
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
name|nio
operator|.
name|client
operator|.
name|HttpAsyncClientBuilder
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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|fail
import|;
end_import

begin_class
DECL|class|RestClientBuilderTests
specifier|public
class|class
name|RestClientBuilderTests
extends|extends
name|RestClientTestCase
block|{
DECL|method|testBuild
specifier|public
name|void
name|testBuild
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|(
operator|(
name|HttpHost
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"hosts must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"no hosts provided"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"host cannot be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|RestClient
name|restClient
init|=
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
name|assertNotNull
argument_list|(
name|restClient
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
operator|.
name|setMaxRetryTimeoutMillis
argument_list|(
name|randomIntBetween
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"maxRetryTimeoutMillis must be greater than 0"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
operator|.
name|setDefaultHeaders
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"defaultHeaders must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
operator|.
name|setDefaultHeaders
argument_list|(
operator|new
name|Header
index|[]
block|{
literal|null
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"default header must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
operator|.
name|setFailureListener
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"failureListener must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
operator|.
name|setHttpClientConfigCallback
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"httpClientConfigCallback must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
operator|.
name|setRequestConfigCallback
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"requestConfigCallback must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|numNodes
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|HttpHost
index|[]
name|hosts
init|=
operator|new
name|HttpHost
index|[
name|numNodes
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
name|numNodes
condition|;
name|i
operator|++
control|)
block|{
name|hosts
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
name|RestClientBuilder
name|builder
init|=
name|RestClient
operator|.
name|builder
argument_list|(
name|hosts
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setHttpClientConfigCallback
argument_list|(
operator|new
name|RestClientBuilder
operator|.
name|HttpClientConfigCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|HttpAsyncClientBuilder
name|customizeHttpClient
parameter_list|(
name|HttpAsyncClientBuilder
name|httpClientBuilder
parameter_list|)
block|{
return|return
name|httpClientBuilder
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setRequestConfigCallback
argument_list|(
operator|new
name|RestClientBuilder
operator|.
name|RequestConfigCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|RequestConfig
operator|.
name|Builder
name|customizeRequestConfig
parameter_list|(
name|RequestConfig
operator|.
name|Builder
name|requestConfigBuilder
parameter_list|)
block|{
return|return
name|requestConfigBuilder
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|numHeaders
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
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
name|headers
index|[
name|i
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
literal|"header"
operator|+
name|i
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setDefaultHeaders
argument_list|(
name|headers
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setMaxRetryTimeoutMillis
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|String
name|pathPrefix
init|=
operator|(
name|randomBoolean
argument_list|()
condition|?
literal|"/"
else|:
literal|""
operator|)
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|)
decl_stmt|;
while|while
condition|(
name|pathPrefix
operator|.
name|length
argument_list|()
operator|<
literal|20
operator|&&
name|randomBoolean
argument_list|()
condition|)
block|{
name|pathPrefix
operator|+=
literal|"/"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|6
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setPathPrefix
argument_list|(
name|pathPrefix
operator|+
operator|(
name|randomBoolean
argument_list|()
condition|?
literal|"/"
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|RestClient
name|restClient
init|=
name|builder
operator|.
name|build
argument_list|()
init|)
block|{
name|assertNotNull
argument_list|(
name|restClient
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSetPathPrefixNull
specifier|public
name|void
name|testSetPathPrefixNull
parameter_list|()
block|{
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
operator|.
name|setPathPrefix
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"pathPrefix set to null should fail!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
specifier|final
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"pathPrefix must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSetPathPrefixEmpty
specifier|public
name|void
name|testSetPathPrefixEmpty
parameter_list|()
block|{
name|assertSetPathPrefixThrows
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|assertSetPathPrefixThrows
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
DECL|method|testSetPathPrefixMalformed
specifier|public
name|void
name|testSetPathPrefixMalformed
parameter_list|()
block|{
name|assertSetPathPrefixThrows
argument_list|(
literal|"//"
argument_list|)
expr_stmt|;
name|assertSetPathPrefixThrows
argument_list|(
literal|"base/path//"
argument_list|)
expr_stmt|;
block|}
DECL|method|assertSetPathPrefixThrows
specifier|private
specifier|static
name|void
name|assertSetPathPrefixThrows
parameter_list|(
specifier|final
name|String
name|pathPrefix
parameter_list|)
block|{
try|try
block|{
name|RestClient
operator|.
name|builder
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
operator|.
name|setPathPrefix
argument_list|(
name|pathPrefix
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"path prefix ["
operator|+
name|pathPrefix
operator|+
literal|"] should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
specifier|final
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
name|pathPrefix
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

