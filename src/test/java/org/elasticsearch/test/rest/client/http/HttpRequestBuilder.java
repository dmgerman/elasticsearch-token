begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.client.http
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|client
operator|.
name|http
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|*
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
name|elasticsearch
operator|.
name|client
operator|.
name|support
operator|.
name|Headers
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
name|logging
operator|.
name|ESLogger
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
name|Loggers
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
name|http
operator|.
name|HttpServerTransport
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
name|UnsupportedEncodingException
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
name|net
operator|.
name|URLEncoder
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
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Executable builder for an http request  * Holds an {@link org.apache.http.client.HttpClient} that is used to send the built http request  */
end_comment

begin_class
DECL|class|HttpRequestBuilder
specifier|public
class|class
name|HttpRequestBuilder
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|HttpRequestBuilder
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_CHARSET
specifier|static
specifier|final
name|Charset
name|DEFAULT_CHARSET
init|=
name|Charset
operator|.
name|forName
argument_list|(
literal|"utf-8"
argument_list|)
decl_stmt|;
DECL|field|httpClient
specifier|private
specifier|final
name|CloseableHttpClient
name|httpClient
decl_stmt|;
DECL|field|protocol
specifier|private
name|String
name|protocol
init|=
literal|"http"
decl_stmt|;
DECL|field|host
specifier|private
name|String
name|host
decl_stmt|;
DECL|field|port
specifier|private
name|int
name|port
decl_stmt|;
DECL|field|path
specifier|private
name|String
name|path
init|=
literal|""
decl_stmt|;
DECL|field|params
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|headers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|method
specifier|private
name|String
name|method
init|=
name|HttpGetWithEntity
operator|.
name|METHOD_NAME
decl_stmt|;
DECL|field|body
specifier|private
name|String
name|body
decl_stmt|;
DECL|method|HttpRequestBuilder
specifier|public
name|HttpRequestBuilder
parameter_list|(
name|CloseableHttpClient
name|httpClient
parameter_list|)
block|{
name|this
operator|.
name|httpClient
operator|=
name|httpClient
expr_stmt|;
block|}
DECL|method|host
specifier|public
name|HttpRequestBuilder
name|host
parameter_list|(
name|String
name|host
parameter_list|)
block|{
name|this
operator|.
name|host
operator|=
name|host
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|httpTransport
specifier|public
name|HttpRequestBuilder
name|httpTransport
parameter_list|(
name|HttpServerTransport
name|httpServerTransport
parameter_list|)
block|{
name|InetSocketTransportAddress
name|transportAddress
init|=
operator|(
name|InetSocketTransportAddress
operator|)
name|httpServerTransport
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
decl_stmt|;
return|return
name|host
argument_list|(
name|transportAddress
operator|.
name|address
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|)
operator|.
name|port
argument_list|(
name|transportAddress
operator|.
name|address
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
return|;
block|}
DECL|method|port
specifier|public
name|HttpRequestBuilder
name|port
parameter_list|(
name|int
name|port
parameter_list|)
block|{
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|path
specifier|public
name|HttpRequestBuilder
name|path
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|addParam
specifier|public
name|HttpRequestBuilder
name|addParam
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
try|try
block|{
comment|//manually url encode params, since URI does it only partially (e.g. '+' stays as is)
name|this
operator|.
name|params
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|URLEncoder
operator|.
name|encode
argument_list|(
name|value
argument_list|,
literal|"utf-8"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|addHeaders
specifier|public
name|HttpRequestBuilder
name|addHeaders
parameter_list|(
name|Headers
name|headers
parameter_list|)
block|{
for|for
control|(
name|String
name|header
range|:
name|headers
operator|.
name|headers
argument_list|()
operator|.
name|names
argument_list|()
control|)
block|{
name|this
operator|.
name|headers
operator|.
name|put
argument_list|(
name|header
argument_list|,
name|headers
operator|.
name|headers
argument_list|()
operator|.
name|get
argument_list|(
name|header
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|addHeader
specifier|public
name|HttpRequestBuilder
name|addHeader
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|this
operator|.
name|headers
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|protocol
specifier|public
name|HttpRequestBuilder
name|protocol
parameter_list|(
name|String
name|protocol
parameter_list|)
block|{
name|this
operator|.
name|protocol
operator|=
name|protocol
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|method
specifier|public
name|HttpRequestBuilder
name|method
parameter_list|(
name|String
name|method
parameter_list|)
block|{
name|this
operator|.
name|method
operator|=
name|method
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|body
specifier|public
name|HttpRequestBuilder
name|body
parameter_list|(
name|String
name|body
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|body
argument_list|)
condition|)
block|{
name|this
operator|.
name|body
operator|=
name|body
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|execute
specifier|public
name|HttpResponse
name|execute
parameter_list|()
throws|throws
name|IOException
block|{
name|HttpUriRequest
name|httpUriRequest
init|=
name|buildRequest
argument_list|()
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|StringBuilder
name|stringBuilder
init|=
operator|new
name|StringBuilder
argument_list|(
name|httpUriRequest
operator|.
name|getMethod
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
operator|.
name|append
argument_list|(
name|httpUriRequest
operator|.
name|getURI
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|body
argument_list|)
condition|)
block|{
name|stringBuilder
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
operator|.
name|append
argument_list|(
name|body
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"sending request \n{}"
argument_list|,
name|stringBuilder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|this
operator|.
name|headers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|httpUriRequest
operator|.
name|addHeader
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|CloseableHttpResponse
name|closeableHttpResponse
init|=
name|httpClient
operator|.
name|execute
argument_list|(
name|httpUriRequest
argument_list|)
init|)
block|{
name|HttpResponse
name|httpResponse
init|=
operator|new
name|HttpResponse
argument_list|(
name|httpUriRequest
argument_list|,
name|closeableHttpResponse
argument_list|)
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"got response \n{}\n{}"
argument_list|,
name|closeableHttpResponse
argument_list|,
name|httpResponse
operator|.
name|hasBody
argument_list|()
condition|?
name|httpResponse
operator|.
name|getBody
argument_list|()
else|:
literal|""
argument_list|)
expr_stmt|;
return|return
name|httpResponse
return|;
block|}
block|}
DECL|method|buildRequest
specifier|private
name|HttpUriRequest
name|buildRequest
parameter_list|()
block|{
if|if
condition|(
name|HttpGetWithEntity
operator|.
name|METHOD_NAME
operator|.
name|equalsIgnoreCase
argument_list|(
name|method
argument_list|)
condition|)
block|{
return|return
name|addOptionalBody
argument_list|(
operator|new
name|HttpGetWithEntity
argument_list|(
name|buildUri
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|HttpHead
operator|.
name|METHOD_NAME
operator|.
name|equalsIgnoreCase
argument_list|(
name|method
argument_list|)
condition|)
block|{
name|checkBodyNotSupported
argument_list|()
expr_stmt|;
return|return
operator|new
name|HttpHead
argument_list|(
name|buildUri
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|HttpOptions
operator|.
name|METHOD_NAME
operator|.
name|equalsIgnoreCase
argument_list|(
name|method
argument_list|)
condition|)
block|{
name|checkBodyNotSupported
argument_list|()
expr_stmt|;
return|return
operator|new
name|HttpOptions
argument_list|(
name|buildUri
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|HttpDeleteWithEntity
operator|.
name|METHOD_NAME
operator|.
name|equalsIgnoreCase
argument_list|(
name|method
argument_list|)
condition|)
block|{
return|return
name|addOptionalBody
argument_list|(
operator|new
name|HttpDeleteWithEntity
argument_list|(
name|buildUri
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|HttpPut
operator|.
name|METHOD_NAME
operator|.
name|equalsIgnoreCase
argument_list|(
name|method
argument_list|)
condition|)
block|{
return|return
name|addOptionalBody
argument_list|(
operator|new
name|HttpPut
argument_list|(
name|buildUri
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|HttpPost
operator|.
name|METHOD_NAME
operator|.
name|equalsIgnoreCase
argument_list|(
name|method
argument_list|)
condition|)
block|{
return|return
name|addOptionalBody
argument_list|(
operator|new
name|HttpPost
argument_list|(
name|buildUri
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"method ["
operator|+
name|method
operator|+
literal|"] not supported"
argument_list|)
throw|;
block|}
DECL|method|buildUri
specifier|private
name|URI
name|buildUri
parameter_list|()
block|{
try|try
block|{
comment|//url encode rules for path and query params are different. We use URI to encode the path, but we manually encode each query param through URLEncoder.
name|URI
name|uri
init|=
operator|new
name|URI
argument_list|(
name|protocol
argument_list|,
literal|null
argument_list|,
name|host
argument_list|,
name|port
argument_list|,
name|path
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|//String concatenation FTW. If we use the nicer multi argument URI constructor query parameters will get only partially encoded
comment|//(e.g. '+' will stay as is) hence when trying to properly encode params manually they will end up double encoded (+ becomes %252B instead of %2B).
name|StringBuilder
name|uriBuilder
init|=
operator|new
name|StringBuilder
argument_list|(
name|protocol
argument_list|)
operator|.
name|append
argument_list|(
literal|"://"
argument_list|)
operator|.
name|append
argument_list|(
name|host
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|port
argument_list|)
operator|.
name|append
argument_list|(
name|uri
operator|.
name|getRawPath
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|params
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|uriBuilder
operator|.
name|append
argument_list|(
literal|"?"
argument_list|)
operator|.
name|append
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
literal|'&'
argument_list|)
operator|.
name|withKeyValueSeparator
argument_list|(
literal|"="
argument_list|)
operator|.
name|join
argument_list|(
name|params
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|URI
operator|.
name|create
argument_list|(
name|uriBuilder
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unable to build uri"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|addOptionalBody
specifier|private
name|HttpEntityEnclosingRequestBase
name|addOptionalBody
parameter_list|(
name|HttpEntityEnclosingRequestBase
name|requestBase
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|body
argument_list|)
condition|)
block|{
name|requestBase
operator|.
name|setEntity
argument_list|(
operator|new
name|StringEntity
argument_list|(
name|body
argument_list|,
name|DEFAULT_CHARSET
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|requestBase
return|;
block|}
DECL|method|checkBodyNotSupported
specifier|private
name|void
name|checkBodyNotSupported
parameter_list|()
block|{
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|body
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"request body not supported with head request"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|stringBuilder
init|=
operator|new
name|StringBuilder
argument_list|(
name|method
argument_list|)
operator|.
name|append
argument_list|(
literal|" '"
argument_list|)
operator|.
name|append
argument_list|(
name|host
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|port
argument_list|)
operator|.
name|append
argument_list|(
name|path
argument_list|)
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|params
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|stringBuilder
operator|.
name|append
argument_list|(
literal|", params="
argument_list|)
operator|.
name|append
argument_list|(
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|body
argument_list|)
condition|)
block|{
name|stringBuilder
operator|.
name|append
argument_list|(
literal|", body=\n"
argument_list|)
operator|.
name|append
argument_list|(
name|body
argument_list|)
expr_stmt|;
block|}
return|return
name|stringBuilder
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

