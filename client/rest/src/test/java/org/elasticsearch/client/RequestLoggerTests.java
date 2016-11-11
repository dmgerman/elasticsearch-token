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
name|RandomNumbers
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
name|HttpEntityEnclosingRequest
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
name|client
operator|.
name|methods
operator|.
name|HttpHead
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
name|HttpOptions
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
name|HttpPatch
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
name|HttpPost
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
name|HttpPut
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
name|HttpRequestBase
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
name|HttpTrace
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
name|nio
operator|.
name|entity
operator|.
name|NByteArrayEntity
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
name|nio
operator|.
name|entity
operator|.
name|NStringEntity
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|assertThat
import|;
end_import

begin_class
DECL|class|RequestLoggerTests
specifier|public
class|class
name|RequestLoggerTests
extends|extends
name|RestClientTestCase
block|{
DECL|method|testTraceRequest
specifier|public
name|void
name|testTraceRequest
parameter_list|()
throws|throws
name|IOException
throws|,
name|URISyntaxException
block|{
name|HttpHost
name|host
init|=
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|,
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
literal|"http"
else|:
literal|"https"
argument_list|)
decl_stmt|;
name|String
name|expectedEndpoint
init|=
literal|"/index/type/_api"
decl_stmt|;
name|URI
name|uri
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|uri
operator|=
operator|new
name|URI
argument_list|(
name|expectedEndpoint
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|uri
operator|=
operator|new
name|URI
argument_list|(
literal|"index/type/_api"
argument_list|)
expr_stmt|;
block|}
name|HttpRequestBase
name|request
decl_stmt|;
name|int
name|requestType
init|=
name|RandomNumbers
operator|.
name|randomIntBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|7
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|requestType
condition|)
block|{
case|case
literal|0
case|:
name|request
operator|=
operator|new
name|HttpGetWithEntity
argument_list|(
name|uri
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|request
operator|=
operator|new
name|HttpPost
argument_list|(
name|uri
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|request
operator|=
operator|new
name|HttpPut
argument_list|(
name|uri
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|request
operator|=
operator|new
name|HttpDeleteWithEntity
argument_list|(
name|uri
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|request
operator|=
operator|new
name|HttpHead
argument_list|(
name|uri
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|request
operator|=
operator|new
name|HttpTrace
argument_list|(
name|uri
argument_list|)
expr_stmt|;
break|break;
case|case
literal|6
case|:
name|request
operator|=
operator|new
name|HttpOptions
argument_list|(
name|uri
argument_list|)
expr_stmt|;
break|break;
case|case
literal|7
case|:
name|request
operator|=
operator|new
name|HttpPatch
argument_list|(
name|uri
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
name|String
name|expected
init|=
literal|"curl -iX "
operator|+
name|request
operator|.
name|getMethod
argument_list|()
operator|+
literal|" '"
operator|+
name|host
operator|+
name|expectedEndpoint
operator|+
literal|"'"
decl_stmt|;
name|boolean
name|hasBody
init|=
name|request
operator|instanceof
name|HttpEntityEnclosingRequest
operator|&&
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
name|String
name|requestBody
init|=
literal|"{ \"field\": \"value\" }"
decl_stmt|;
if|if
condition|(
name|hasBody
condition|)
block|{
name|expected
operator|+=
literal|" -d '"
operator|+
name|requestBody
operator|+
literal|"'"
expr_stmt|;
name|HttpEntityEnclosingRequest
name|enclosingRequest
init|=
operator|(
name|HttpEntityEnclosingRequest
operator|)
name|request
decl_stmt|;
name|HttpEntity
name|entity
decl_stmt|;
switch|switch
condition|(
name|RandomNumbers
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
condition|)
block|{
case|case
literal|0
case|:
name|entity
operator|=
operator|new
name|StringEntity
argument_list|(
name|requestBody
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|entity
operator|=
operator|new
name|InputStreamEntity
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|requestBody
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
break|break;
case|case
literal|2
case|:
name|entity
operator|=
operator|new
name|NStringEntity
argument_list|(
name|requestBody
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|entity
operator|=
operator|new
name|NByteArrayEntity
argument_list|(
name|requestBody
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
name|enclosingRequest
operator|.
name|setEntity
argument_list|(
name|entity
argument_list|)
expr_stmt|;
block|}
name|String
name|traceRequest
init|=
name|RequestLogger
operator|.
name|buildTraceRequest
argument_list|(
name|request
argument_list|,
name|host
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|traceRequest
argument_list|,
name|equalTo
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasBody
condition|)
block|{
comment|//check that the body is still readable as most entities are not repeatable
name|String
name|body
init|=
name|EntityUtils
operator|.
name|toString
argument_list|(
operator|(
operator|(
name|HttpEntityEnclosingRequest
operator|)
name|request
operator|)
operator|.
name|getEntity
argument_list|()
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|body
argument_list|,
name|equalTo
argument_list|(
name|requestBody
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testTraceResponse
specifier|public
name|void
name|testTraceResponse
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
literal|"HTTP"
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|int
name|statusCode
init|=
name|RandomNumbers
operator|.
name|randomIntBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
literal|200
argument_list|,
literal|599
argument_list|)
decl_stmt|;
name|String
name|reasonPhrase
init|=
literal|"REASON"
decl_stmt|;
name|BasicStatusLine
name|statusLine
init|=
operator|new
name|BasicStatusLine
argument_list|(
name|protocolVersion
argument_list|,
name|statusCode
argument_list|,
name|reasonPhrase
argument_list|)
decl_stmt|;
name|String
name|expected
init|=
literal|"# "
operator|+
name|statusLine
operator|.
name|toString
argument_list|()
decl_stmt|;
name|BasicHttpResponse
name|httpResponse
init|=
operator|new
name|BasicHttpResponse
argument_list|(
name|statusLine
argument_list|)
decl_stmt|;
name|int
name|numHeaders
init|=
name|RandomNumbers
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
name|httpResponse
operator|.
name|setHeader
argument_list|(
literal|"header"
operator|+
name|i
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|expected
operator|+=
literal|"\n# header"
operator|+
name|i
operator|+
literal|": value"
expr_stmt|;
block|}
name|expected
operator|+=
literal|"\n#"
expr_stmt|;
name|boolean
name|hasBody
init|=
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
name|String
name|responseBody
init|=
literal|"{\n  \"field\": \"value\"\n}"
decl_stmt|;
if|if
condition|(
name|hasBody
condition|)
block|{
name|expected
operator|+=
literal|"\n# {"
expr_stmt|;
name|expected
operator|+=
literal|"\n#   \"field\": \"value\""
expr_stmt|;
name|expected
operator|+=
literal|"\n# }"
expr_stmt|;
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
name|String
name|traceResponse
init|=
name|RequestLogger
operator|.
name|buildTraceResponse
argument_list|(
name|httpResponse
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|traceResponse
argument_list|,
name|equalTo
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasBody
condition|)
block|{
comment|//check that the body is still readable as most entities are not repeatable
name|String
name|body
init|=
name|EntityUtils
operator|.
name|toString
argument_list|(
name|httpResponse
operator|.
name|getEntity
argument_list|()
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|body
argument_list|,
name|equalTo
argument_list|(
name|responseBody
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

