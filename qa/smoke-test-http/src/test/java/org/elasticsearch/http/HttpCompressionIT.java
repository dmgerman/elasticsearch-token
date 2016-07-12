begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
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
name|HttpException
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
name|HttpHeaders
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
name|HttpResponseInterceptor
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
name|impl
operator|.
name|client
operator|.
name|HttpClients
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
name|protocol
operator|.
name|HttpContext
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
name|Response
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
name|RestClient
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
name|ESIntegTestCase
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
name|util
operator|.
name|Collections
import|;
end_import

begin_class
DECL|class|HttpCompressionIT
specifier|public
class|class
name|HttpCompressionIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|GZIP_ENCODING
specifier|private
specifier|static
specifier|final
name|String
name|GZIP_ENCODING
init|=
literal|"gzip"
decl_stmt|;
DECL|field|SAMPLE_DOCUMENT
specifier|private
specifier|static
specifier|final
name|StringEntity
name|SAMPLE_DOCUMENT
init|=
operator|new
name|StringEntity
argument_list|(
literal|"{\n"
operator|+
literal|"   \"name\": {\n"
operator|+
literal|"      \"first name\": \"Steve\",\n"
operator|+
literal|"      \"last name\": \"Jobs\"\n"
operator|+
literal|"   }\n"
operator|+
literal|"}"
argument_list|,
name|RestClient
operator|.
name|JSON_CONTENT_TYPE
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|ignoreExternalCluster
specifier|protected
name|boolean
name|ignoreExternalCluster
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|testCompressesResponseIfRequested
specifier|public
name|void
name|testCompressesResponseIfRequested
parameter_list|()
throws|throws
name|Exception
block|{
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// we need to intercept early, otherwise internal logic in HttpClient will just remove the header and we cannot verify it
name|ContentEncodingHeaderExtractor
name|headerExtractor
init|=
operator|new
name|ContentEncodingHeaderExtractor
argument_list|()
decl_stmt|;
try|try
init|(
name|RestClient
name|client
init|=
name|createRestClient
argument_list|(
name|HttpClients
operator|.
name|custom
argument_list|()
operator|.
name|addInterceptorFirst
argument_list|(
name|headerExtractor
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
init|)
block|{
try|try
init|(
name|Response
name|response
init|=
name|client
operator|.
name|performRequest
argument_list|(
literal|"GET"
argument_list|,
literal|"/"
argument_list|,
operator|new
name|BasicHeader
argument_list|(
name|HttpHeaders
operator|.
name|ACCEPT_ENCODING
argument_list|,
name|GZIP_ENCODING
argument_list|)
argument_list|)
init|)
block|{
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|headerExtractor
operator|.
name|hasContentEncodingHeader
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|GZIP_ENCODING
argument_list|,
name|headerExtractor
operator|.
name|getContentEncodingHeader
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testUncompressedResponseByDefault
specifier|public
name|void
name|testUncompressedResponseByDefault
parameter_list|()
throws|throws
name|Exception
block|{
name|ensureGreen
argument_list|()
expr_stmt|;
name|ContentEncodingHeaderExtractor
name|headerExtractor
init|=
operator|new
name|ContentEncodingHeaderExtractor
argument_list|()
decl_stmt|;
name|CloseableHttpClient
name|httpClient
init|=
name|HttpClients
operator|.
name|custom
argument_list|()
operator|.
name|disableContentCompression
argument_list|()
operator|.
name|addInterceptorFirst
argument_list|(
name|headerExtractor
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
init|(
name|RestClient
name|client
init|=
name|createRestClient
argument_list|(
name|httpClient
argument_list|)
init|)
block|{
try|try
init|(
name|Response
name|response
init|=
name|client
operator|.
name|performRequest
argument_list|(
literal|"GET"
argument_list|,
literal|"/"
argument_list|)
init|)
block|{
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|headerExtractor
operator|.
name|hasContentEncodingHeader
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testCanInterpretUncompressedRequest
specifier|public
name|void
name|testCanInterpretUncompressedRequest
parameter_list|()
throws|throws
name|Exception
block|{
name|ensureGreen
argument_list|()
expr_stmt|;
name|ContentEncodingHeaderExtractor
name|headerExtractor
init|=
operator|new
name|ContentEncodingHeaderExtractor
argument_list|()
decl_stmt|;
comment|// this disable content compression in both directions (request and response)
name|CloseableHttpClient
name|httpClient
init|=
name|HttpClients
operator|.
name|custom
argument_list|()
operator|.
name|disableContentCompression
argument_list|()
operator|.
name|addInterceptorFirst
argument_list|(
name|headerExtractor
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
init|(
name|RestClient
name|client
init|=
name|createRestClient
argument_list|(
name|httpClient
argument_list|)
init|)
block|{
try|try
init|(
name|Response
name|response
init|=
name|client
operator|.
name|performRequest
argument_list|(
literal|"POST"
argument_list|,
literal|"/company/employees/1"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|SAMPLE_DOCUMENT
argument_list|)
init|)
block|{
name|assertEquals
argument_list|(
literal|201
argument_list|,
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|headerExtractor
operator|.
name|hasContentEncodingHeader
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testCanInterpretCompressedRequest
specifier|public
name|void
name|testCanInterpretCompressedRequest
parameter_list|()
throws|throws
name|Exception
block|{
name|ensureGreen
argument_list|()
expr_stmt|;
name|ContentEncodingHeaderExtractor
name|headerExtractor
init|=
operator|new
name|ContentEncodingHeaderExtractor
argument_list|()
decl_stmt|;
comment|// we don't call #disableContentCompression() hence the client will send the content compressed
try|try
init|(
name|RestClient
name|client
init|=
name|createRestClient
argument_list|(
name|HttpClients
operator|.
name|custom
argument_list|()
operator|.
name|addInterceptorFirst
argument_list|(
name|headerExtractor
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
init|)
block|{
try|try
init|(
name|Response
name|response
init|=
name|client
operator|.
name|performRequest
argument_list|(
literal|"POST"
argument_list|,
literal|"/company/employees/2"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|SAMPLE_DOCUMENT
argument_list|)
init|)
block|{
name|assertEquals
argument_list|(
literal|201
argument_list|,
name|response
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
name|GZIP_ENCODING
argument_list|,
name|headerExtractor
operator|.
name|getContentEncodingHeader
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|ContentEncodingHeaderExtractor
specifier|private
specifier|static
class|class
name|ContentEncodingHeaderExtractor
implements|implements
name|HttpResponseInterceptor
block|{
DECL|field|contentEncodingHeader
specifier|private
name|Header
name|contentEncodingHeader
decl_stmt|;
annotation|@
name|Override
DECL|method|process
specifier|public
name|void
name|process
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpResponse
name|response
parameter_list|,
name|HttpContext
name|context
parameter_list|)
throws|throws
name|HttpException
throws|,
name|IOException
block|{
specifier|final
name|Header
index|[]
name|headers
init|=
name|response
operator|.
name|getHeaders
argument_list|(
name|HttpHeaders
operator|.
name|CONTENT_ENCODING
argument_list|)
decl_stmt|;
if|if
condition|(
name|headers
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|this
operator|.
name|contentEncodingHeader
operator|=
name|headers
index|[
literal|0
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|headers
operator|.
name|length
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Expected none or one content encoding header but got "
operator|+
name|headers
operator|.
name|length
operator|+
literal|" headers."
argument_list|)
throw|;
block|}
block|}
DECL|method|hasContentEncodingHeader
specifier|public
name|boolean
name|hasContentEncodingHeader
parameter_list|()
block|{
return|return
name|contentEncodingHeader
operator|!=
literal|null
return|;
block|}
DECL|method|getContentEncodingHeader
specifier|public
name|Header
name|getContentEncodingHeader
parameter_list|()
block|{
return|return
name|contentEncodingHeader
return|;
block|}
block|}
block|}
end_class

end_unit

