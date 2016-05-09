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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|HttpEntityEnclosingRequestBase
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
name|utils
operator|.
name|URIBuilder
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
name|Closeable
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
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|Objects
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
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
import|;
end_import

begin_class
DECL|class|RestClient
specifier|public
specifier|final
class|class
name|RestClient
implements|implements
name|Closeable
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|Log
name|logger
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RestClient
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|CloseableHttpClient
name|client
decl_stmt|;
DECL|field|connectionPool
specifier|private
specifier|final
name|ConnectionPool
name|connectionPool
decl_stmt|;
DECL|field|maxRetryTimeout
specifier|private
specifier|final
name|long
name|maxRetryTimeout
decl_stmt|;
DECL|method|RestClient
specifier|public
name|RestClient
parameter_list|(
name|CloseableHttpClient
name|client
parameter_list|,
name|ConnectionPool
name|connectionPool
parameter_list|,
name|long
name|maxRetryTimeout
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|client
argument_list|,
literal|"client cannot be null"
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|connectionPool
argument_list|,
literal|"connectionPool cannot be null"
argument_list|)
expr_stmt|;
if|if
condition|(
name|maxRetryTimeout
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"maxRetryTimeout must be greater than 0"
argument_list|)
throw|;
block|}
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|connectionPool
operator|=
name|connectionPool
expr_stmt|;
name|this
operator|.
name|maxRetryTimeout
operator|=
name|maxRetryTimeout
expr_stmt|;
block|}
DECL|method|performRequest
specifier|public
name|ElasticsearchResponse
name|performRequest
parameter_list|(
name|String
name|method
parameter_list|,
name|String
name|endpoint
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|,
name|HttpEntity
name|entity
parameter_list|)
throws|throws
name|IOException
block|{
name|URI
name|uri
init|=
name|buildUri
argument_list|(
name|endpoint
argument_list|,
name|params
argument_list|)
decl_stmt|;
name|HttpRequestBase
name|request
init|=
name|createHttpRequest
argument_list|(
name|method
argument_list|,
name|uri
argument_list|,
name|entity
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Connection
argument_list|>
name|connectionIterator
init|=
name|connectionPool
operator|.
name|nextConnection
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|connectionIterator
operator|.
name|hasNext
argument_list|()
operator|==
literal|false
condition|)
block|{
name|Connection
name|connection
init|=
name|connectionPool
operator|.
name|lastResortConnection
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"no healthy nodes available, trying "
operator|+
name|connection
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|performRequest
argument_list|(
name|request
argument_list|,
name|Stream
operator|.
name|of
argument_list|(
name|connection
argument_list|)
operator|.
name|iterator
argument_list|()
argument_list|)
return|;
block|}
return|return
name|performRequest
argument_list|(
name|request
argument_list|,
name|connectionIterator
argument_list|)
return|;
block|}
DECL|method|performRequest
specifier|private
name|ElasticsearchResponse
name|performRequest
parameter_list|(
name|HttpRequestBase
name|request
parameter_list|,
name|Iterator
argument_list|<
name|Connection
argument_list|>
name|connectionIterator
parameter_list|)
throws|throws
name|IOException
block|{
comment|//we apply a soft margin so that e.g. if a request took 59 seconds and timeout is set to 60 we don't do another attempt
name|long
name|retryTimeout
init|=
name|Math
operator|.
name|round
argument_list|(
name|this
operator|.
name|maxRetryTimeout
operator|/
operator|(
name|float
operator|)
literal|100
operator|*
literal|98
argument_list|)
decl_stmt|;
name|IOException
name|lastSeenException
init|=
literal|null
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
while|while
condition|(
name|connectionIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Connection
name|connection
init|=
name|connectionIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastSeenException
operator|!=
literal|null
condition|)
block|{
name|long
name|timeElapsed
init|=
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTime
argument_list|)
decl_stmt|;
name|long
name|timeout
init|=
name|retryTimeout
operator|-
name|timeElapsed
decl_stmt|;
if|if
condition|(
name|timeout
operator|<=
literal|0
condition|)
block|{
name|IOException
name|retryTimeoutException
init|=
operator|new
name|IOException
argument_list|(
literal|"request retries exceeded max retry timeout ["
operator|+
name|retryTimeout
operator|+
literal|"]"
argument_list|)
decl_stmt|;
name|retryTimeoutException
operator|.
name|addSuppressed
argument_list|(
name|lastSeenException
argument_list|)
expr_stmt|;
throw|throw
name|retryTimeoutException
throw|;
block|}
block|}
name|CloseableHttpResponse
name|response
decl_stmt|;
try|try
block|{
name|response
operator|=
name|client
operator|.
name|execute
argument_list|(
name|connection
operator|.
name|getHost
argument_list|()
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|RequestLogger
operator|.
name|log
argument_list|(
name|logger
argument_list|,
literal|"request failed"
argument_list|,
name|request
argument_list|,
name|connection
operator|.
name|getHost
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|connectionPool
operator|.
name|onFailure
argument_list|(
name|connection
argument_list|)
expr_stmt|;
name|lastSeenException
operator|=
name|addSuppressedException
argument_list|(
name|lastSeenException
argument_list|,
name|e
argument_list|)
expr_stmt|;
continue|continue;
block|}
finally|finally
block|{
name|request
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
name|int
name|statusCode
init|=
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
decl_stmt|;
comment|//TODO make ignore status code configurable. rest-spec and tests support that parameter (ignore_missing)
if|if
condition|(
name|statusCode
operator|<
literal|300
operator|||
operator|(
name|request
operator|.
name|getMethod
argument_list|()
operator|.
name|equals
argument_list|(
name|HttpHead
operator|.
name|METHOD_NAME
argument_list|)
operator|&&
name|statusCode
operator|==
literal|404
operator|)
condition|)
block|{
name|RequestLogger
operator|.
name|log
argument_list|(
name|logger
argument_list|,
literal|"request succeeded"
argument_list|,
name|request
argument_list|,
name|connection
operator|.
name|getHost
argument_list|()
argument_list|,
name|response
argument_list|)
expr_stmt|;
name|connectionPool
operator|.
name|onSuccess
argument_list|(
name|connection
argument_list|)
expr_stmt|;
return|return
operator|new
name|ElasticsearchResponse
argument_list|(
name|request
operator|.
name|getRequestLine
argument_list|()
argument_list|,
name|connection
operator|.
name|getHost
argument_list|()
argument_list|,
name|response
argument_list|)
return|;
block|}
else|else
block|{
name|RequestLogger
operator|.
name|log
argument_list|(
name|logger
argument_list|,
literal|"request failed"
argument_list|,
name|request
argument_list|,
name|connection
operator|.
name|getHost
argument_list|()
argument_list|,
name|response
argument_list|)
expr_stmt|;
name|String
name|responseBody
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|getEntity
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|responseBody
operator|=
name|EntityUtils
operator|.
name|toString
argument_list|(
name|response
operator|.
name|getEntity
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ElasticsearchResponseException
name|elasticsearchResponseException
init|=
operator|new
name|ElasticsearchResponseException
argument_list|(
name|request
operator|.
name|getRequestLine
argument_list|()
argument_list|,
name|connection
operator|.
name|getHost
argument_list|()
argument_list|,
name|response
operator|.
name|getStatusLine
argument_list|()
argument_list|,
name|responseBody
argument_list|)
decl_stmt|;
name|lastSeenException
operator|=
name|addSuppressedException
argument_list|(
name|lastSeenException
argument_list|,
name|elasticsearchResponseException
argument_list|)
expr_stmt|;
comment|//clients don't retry on 500 because elasticsearch still misuses it instead of 400 in some places
if|if
condition|(
name|statusCode
operator|==
literal|502
operator|||
name|statusCode
operator|==
literal|503
operator|||
name|statusCode
operator|==
literal|504
condition|)
block|{
name|connectionPool
operator|.
name|onFailure
argument_list|(
name|connection
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//don't retry and call onSuccess as the error should be a request problem, the node is alive
name|connectionPool
operator|.
name|onSuccess
argument_list|(
name|connection
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
assert|assert
name|lastSeenException
operator|!=
literal|null
assert|;
throw|throw
name|lastSeenException
throw|;
block|}
DECL|method|addSuppressedException
specifier|private
specifier|static
name|IOException
name|addSuppressedException
parameter_list|(
name|IOException
name|suppressedException
parameter_list|,
name|IOException
name|currentException
parameter_list|)
block|{
if|if
condition|(
name|suppressedException
operator|!=
literal|null
condition|)
block|{
name|currentException
operator|.
name|addSuppressed
argument_list|(
name|suppressedException
argument_list|)
expr_stmt|;
block|}
return|return
name|currentException
return|;
block|}
DECL|method|createHttpRequest
specifier|private
specifier|static
name|HttpRequestBase
name|createHttpRequest
parameter_list|(
name|String
name|method
parameter_list|,
name|URI
name|uri
parameter_list|,
name|HttpEntity
name|entity
parameter_list|)
block|{
switch|switch
condition|(
name|method
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
condition|)
block|{
case|case
name|HttpDeleteWithEntity
operator|.
name|METHOD_NAME
case|:
name|HttpDeleteWithEntity
name|httpDeleteWithEntity
init|=
operator|new
name|HttpDeleteWithEntity
argument_list|(
name|uri
argument_list|)
decl_stmt|;
name|addRequestBody
argument_list|(
name|httpDeleteWithEntity
argument_list|,
name|entity
argument_list|)
expr_stmt|;
return|return
name|httpDeleteWithEntity
return|;
case|case
name|HttpGetWithEntity
operator|.
name|METHOD_NAME
case|:
name|HttpGetWithEntity
name|httpGetWithEntity
init|=
operator|new
name|HttpGetWithEntity
argument_list|(
name|uri
argument_list|)
decl_stmt|;
name|addRequestBody
argument_list|(
name|httpGetWithEntity
argument_list|,
name|entity
argument_list|)
expr_stmt|;
return|return
name|httpGetWithEntity
return|;
case|case
name|HttpHead
operator|.
name|METHOD_NAME
case|:
if|if
condition|(
name|entity
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"HEAD with body is not supported"
argument_list|)
throw|;
block|}
return|return
operator|new
name|HttpHead
argument_list|(
name|uri
argument_list|)
return|;
case|case
name|HttpPost
operator|.
name|METHOD_NAME
case|:
name|HttpPost
name|httpPost
init|=
operator|new
name|HttpPost
argument_list|(
name|uri
argument_list|)
decl_stmt|;
name|addRequestBody
argument_list|(
name|httpPost
argument_list|,
name|entity
argument_list|)
expr_stmt|;
return|return
name|httpPost
return|;
case|case
name|HttpPut
operator|.
name|METHOD_NAME
case|:
name|HttpPut
name|httpPut
init|=
operator|new
name|HttpPut
argument_list|(
name|uri
argument_list|)
decl_stmt|;
name|addRequestBody
argument_list|(
name|httpPut
argument_list|,
name|entity
argument_list|)
expr_stmt|;
return|return
name|httpPut
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"http method not supported: "
operator|+
name|method
argument_list|)
throw|;
block|}
block|}
DECL|method|addRequestBody
specifier|private
specifier|static
name|void
name|addRequestBody
parameter_list|(
name|HttpEntityEnclosingRequestBase
name|httpRequest
parameter_list|,
name|HttpEntity
name|entity
parameter_list|)
block|{
if|if
condition|(
name|entity
operator|!=
literal|null
condition|)
block|{
name|httpRequest
operator|.
name|setEntity
argument_list|(
name|entity
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|buildUri
specifier|private
specifier|static
name|URI
name|buildUri
parameter_list|(
name|String
name|path
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
try|try
block|{
name|URIBuilder
name|uriBuilder
init|=
operator|new
name|URIBuilder
argument_list|(
name|path
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|param
range|:
name|params
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|uriBuilder
operator|.
name|addParameter
argument_list|(
name|param
operator|.
name|getKey
argument_list|()
argument_list|,
name|param
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|uriBuilder
operator|.
name|build
argument_list|()
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
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|connectionPool
operator|.
name|close
argument_list|()
expr_stmt|;
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

