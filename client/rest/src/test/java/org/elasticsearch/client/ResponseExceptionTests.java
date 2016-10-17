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
name|HttpResponse
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
name|RequestLine
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
name|entity
operator|.
name|InputStreamEntity
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
name|BasicHttpResponse
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
name|BasicRequestLine
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
name|http
operator|.
name|util
operator|.
name|EntityUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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
name|StandardCharsets
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
name|assertNull
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
name|assertSame
import|;
end_import

begin_class
DECL|class|ResponseExceptionTests
specifier|public
class|class
name|ResponseExceptionTests
extends|extends
name|RestClientTestCase
block|{
DECL|method|testResponseException
specifier|public
name|void
name|testResponseException
parameter_list|()
throws|throws
name|IOException
block|{
name|ProtocolVersion
name|protocolVersion
init|=
operator|new
name|ProtocolVersion
argument_list|(
literal|"http"
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|StatusLine
name|statusLine
init|=
operator|new
name|BasicStatusLine
argument_list|(
name|protocolVersion
argument_list|,
literal|500
argument_list|,
literal|"Internal Server Error"
argument_list|)
decl_stmt|;
name|HttpResponse
name|httpResponse
init|=
operator|new
name|BasicHttpResponse
argument_list|(
name|statusLine
argument_list|)
decl_stmt|;
name|String
name|responseBody
init|=
literal|"{\"error\":{\"root_cause\": {}}}"
decl_stmt|;
name|boolean
name|hasBody
init|=
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasBody
condition|)
block|{
name|HttpEntity
name|entity
decl_stmt|;
if|if
condition|(
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|entity
operator|=
operator|new
name|StringEntity
argument_list|(
name|responseBody
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//test a non repeatable entity
name|entity
operator|=
operator|new
name|InputStreamEntity
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|responseBody
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|httpResponse
operator|.
name|setEntity
argument_list|(
name|entity
argument_list|)
expr_stmt|;
block|}
name|RequestLine
name|requestLine
init|=
operator|new
name|BasicRequestLine
argument_list|(
literal|"GET"
argument_list|,
literal|"/"
argument_list|,
name|protocolVersion
argument_list|)
decl_stmt|;
name|HttpHost
name|httpHost
init|=
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
decl_stmt|;
name|Response
name|response
init|=
operator|new
name|Response
argument_list|(
name|requestLine
argument_list|,
name|httpHost
argument_list|,
name|httpResponse
argument_list|)
decl_stmt|;
name|ResponseException
name|responseException
init|=
operator|new
name|ResponseException
argument_list|(
name|response
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|response
argument_list|,
name|responseException
operator|.
name|getResponse
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasBody
condition|)
block|{
name|assertEquals
argument_list|(
name|responseBody
argument_list|,
name|EntityUtils
operator|.
name|toString
argument_list|(
name|responseException
operator|.
name|getResponse
argument_list|()
operator|.
name|getEntity
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNull
argument_list|(
name|responseException
operator|.
name|getResponse
argument_list|()
operator|.
name|getEntity
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|message
init|=
name|response
operator|.
name|getRequestLine
argument_list|()
operator|.
name|getMethod
argument_list|()
operator|+
literal|" "
operator|+
name|response
operator|.
name|getHost
argument_list|()
operator|+
name|response
operator|.
name|getRequestLine
argument_list|()
operator|.
name|getUri
argument_list|()
operator|+
literal|": "
operator|+
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasBody
condition|)
block|{
name|message
operator|+=
literal|"\n"
operator|+
name|responseBody
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|message
argument_list|,
name|responseException
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
