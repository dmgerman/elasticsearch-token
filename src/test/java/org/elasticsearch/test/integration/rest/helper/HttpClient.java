begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.rest.helper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|rest
operator|.
name|helper
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|transport
operator|.
name|InetSocketTransportAddress
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
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
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
name|net
operator|.
name|MalformedURLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_class
DECL|class|HttpClient
specifier|public
class|class
name|HttpClient
block|{
DECL|field|baseUrl
specifier|private
specifier|final
name|URL
name|baseUrl
decl_stmt|;
DECL|method|HttpClient
specifier|public
name|HttpClient
parameter_list|(
name|TransportAddress
name|transportAddress
parameter_list|)
block|{
name|InetSocketAddress
name|address
init|=
operator|(
operator|(
name|InetSocketTransportAddress
operator|)
name|transportAddress
operator|)
operator|.
name|address
argument_list|()
decl_stmt|;
try|try
block|{
name|baseUrl
operator|=
operator|new
name|URL
argument_list|(
literal|"http"
argument_list|,
name|address
operator|.
name|getHostName
argument_list|()
argument_list|,
name|address
operator|.
name|getPort
argument_list|()
argument_list|,
literal|"/"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedURLException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|""
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|HttpClient
specifier|public
name|HttpClient
parameter_list|(
name|String
name|url
parameter_list|)
block|{
try|try
block|{
name|baseUrl
operator|=
operator|new
name|URL
argument_list|(
name|url
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedURLException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|""
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|HttpClient
specifier|public
name|HttpClient
parameter_list|(
name|URL
name|url
parameter_list|)
block|{
name|baseUrl
operator|=
name|url
expr_stmt|;
block|}
DECL|method|request
specifier|public
name|HttpClientResponse
name|request
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|request
argument_list|(
literal|"GET"
argument_list|,
name|path
argument_list|)
return|;
block|}
DECL|method|request
specifier|public
name|HttpClientResponse
name|request
parameter_list|(
name|String
name|method
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|URL
name|url
decl_stmt|;
try|try
block|{
name|url
operator|=
operator|new
name|URL
argument_list|(
name|baseUrl
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedURLException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"Cannot parse "
operator|+
name|path
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|HttpURLConnection
name|urlConnection
decl_stmt|;
try|try
block|{
name|urlConnection
operator|=
operator|(
name|HttpURLConnection
operator|)
name|url
operator|.
name|openConnection
argument_list|()
expr_stmt|;
name|urlConnection
operator|.
name|setRequestMethod
argument_list|(
name|method
argument_list|)
expr_stmt|;
name|urlConnection
operator|.
name|connect
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|""
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|int
name|errorCode
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|errorCode
operator|=
name|urlConnection
operator|.
name|getResponseCode
argument_list|()
expr_stmt|;
name|InputStream
name|inputStream
init|=
name|urlConnection
operator|.
name|getInputStream
argument_list|()
decl_stmt|;
name|String
name|body
init|=
literal|null
decl_stmt|;
try|try
block|{
name|body
operator|=
name|Streams
operator|.
name|copyToString
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|inputStream
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"problem reading error stream"
argument_list|,
name|e1
argument_list|)
throw|;
block|}
return|return
operator|new
name|HttpClientResponse
argument_list|(
name|body
argument_list|,
name|errorCode
argument_list|,
literal|null
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|InputStream
name|errStream
init|=
name|urlConnection
operator|.
name|getErrorStream
argument_list|()
decl_stmt|;
name|String
name|body
init|=
literal|null
decl_stmt|;
try|try
block|{
name|body
operator|=
name|Streams
operator|.
name|copyToString
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|errStream
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"problem reading error stream"
argument_list|,
name|e1
argument_list|)
throw|;
block|}
return|return
operator|new
name|HttpClientResponse
argument_list|(
name|body
argument_list|,
name|errorCode
argument_list|,
name|e
argument_list|)
return|;
block|}
finally|finally
block|{
name|urlConnection
operator|.
name|disconnect
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

