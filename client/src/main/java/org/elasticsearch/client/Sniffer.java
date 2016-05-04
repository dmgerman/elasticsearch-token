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
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|JsonFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|JsonParser
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|JsonToken
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
name|HttpGet
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
name|net
operator|.
name|URI
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
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * Calls nodes info api and returns a list of http hosts extracted from it  */
end_comment

begin_comment
comment|//TODO this could potentially a call to _cat/nodes (although it doesn't support timeout param), but how would we handle bw comp with 2.x?
end_comment

begin_class
DECL|class|Sniffer
specifier|final
class|class
name|Sniffer
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
name|Sniffer
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
DECL|field|sniffRequestConfig
specifier|private
specifier|final
name|RequestConfig
name|sniffRequestConfig
decl_stmt|;
DECL|field|sniffRequestTimeout
specifier|private
specifier|final
name|int
name|sniffRequestTimeout
decl_stmt|;
DECL|field|scheme
specifier|private
specifier|final
name|String
name|scheme
decl_stmt|;
DECL|field|jsonFactory
specifier|private
specifier|final
name|JsonFactory
name|jsonFactory
decl_stmt|;
DECL|method|Sniffer
name|Sniffer
parameter_list|(
name|CloseableHttpClient
name|client
parameter_list|,
name|RequestConfig
name|sniffRequestConfig
parameter_list|,
name|int
name|sniffRequestTimeout
parameter_list|,
name|String
name|scheme
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
name|sniffRequestConfig
argument_list|,
literal|"sniffRequestConfig cannot be null"
argument_list|)
expr_stmt|;
if|if
condition|(
name|sniffRequestTimeout
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"sniffRequestTimeout must be greater than 0"
argument_list|)
throw|;
block|}
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|scheme
argument_list|,
literal|"scheme cannot be null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|sniffRequestConfig
operator|=
name|sniffRequestConfig
expr_stmt|;
name|this
operator|.
name|sniffRequestTimeout
operator|=
name|sniffRequestTimeout
expr_stmt|;
name|this
operator|.
name|scheme
operator|=
name|scheme
expr_stmt|;
name|this
operator|.
name|jsonFactory
operator|=
operator|new
name|JsonFactory
argument_list|()
expr_stmt|;
block|}
DECL|method|sniffNodes
name|List
argument_list|<
name|HttpHost
argument_list|>
name|sniffNodes
parameter_list|(
name|HttpHost
name|host
parameter_list|)
throws|throws
name|IOException
block|{
name|HttpGet
name|httpGet
init|=
operator|new
name|HttpGet
argument_list|(
literal|"/_nodes/http?timeout="
operator|+
name|sniffRequestTimeout
operator|+
literal|"ms"
argument_list|)
decl_stmt|;
name|httpGet
operator|.
name|setConfig
argument_list|(
name|sniffRequestConfig
argument_list|)
expr_stmt|;
try|try
init|(
name|CloseableHttpResponse
name|response
init|=
name|client
operator|.
name|execute
argument_list|(
name|host
argument_list|,
name|httpGet
argument_list|)
init|)
block|{
name|StatusLine
name|statusLine
init|=
name|response
operator|.
name|getStatusLine
argument_list|()
decl_stmt|;
if|if
condition|(
name|statusLine
operator|.
name|getStatusCode
argument_list|()
operator|>=
literal|300
condition|)
block|{
name|RequestLogger
operator|.
name|log
argument_list|(
name|logger
argument_list|,
literal|"sniff failed"
argument_list|,
name|httpGet
operator|.
name|getRequestLine
argument_list|()
argument_list|,
name|host
argument_list|,
name|statusLine
argument_list|)
expr_stmt|;
name|EntityUtils
operator|.
name|consume
argument_list|(
name|response
operator|.
name|getEntity
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ElasticsearchResponseException
argument_list|(
name|httpGet
operator|.
name|getRequestLine
argument_list|()
argument_list|,
name|host
argument_list|,
name|statusLine
argument_list|)
throw|;
block|}
else|else
block|{
name|List
argument_list|<
name|HttpHost
argument_list|>
name|nodes
init|=
name|readHosts
argument_list|(
name|response
operator|.
name|getEntity
argument_list|()
argument_list|)
decl_stmt|;
name|RequestLogger
operator|.
name|log
argument_list|(
name|logger
argument_list|,
literal|"sniff succeeded"
argument_list|,
name|httpGet
operator|.
name|getRequestLine
argument_list|()
argument_list|,
name|host
argument_list|,
name|statusLine
argument_list|)
expr_stmt|;
return|return
name|nodes
return|;
block|}
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
literal|"sniff failed"
argument_list|,
name|httpGet
operator|.
name|getRequestLine
argument_list|()
argument_list|,
name|host
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
DECL|method|readHosts
specifier|private
name|List
argument_list|<
name|HttpHost
argument_list|>
name|readHosts
parameter_list|(
name|HttpEntity
name|entity
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|InputStream
name|inputStream
init|=
name|entity
operator|.
name|getContent
argument_list|()
init|)
block|{
name|JsonParser
name|parser
init|=
name|jsonFactory
operator|.
name|createParser
argument_list|(
name|inputStream
argument_list|)
decl_stmt|;
if|if
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"expected data to start with an object"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|HttpHost
argument_list|>
name|hosts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"nodes"
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|getCurrentName
argument_list|()
argument_list|)
condition|)
block|{
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
name|JsonToken
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
assert|assert
name|token
operator|==
name|JsonToken
operator|.
name|START_OBJECT
assert|;
name|String
name|nodeId
init|=
name|parser
operator|.
name|getCurrentName
argument_list|()
decl_stmt|;
name|HttpHost
name|sniffedHost
init|=
name|readNode
argument_list|(
name|nodeId
argument_list|,
name|parser
argument_list|,
name|this
operator|.
name|scheme
argument_list|)
decl_stmt|;
if|if
condition|(
name|sniffedHost
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"adding node ["
operator|+
name|nodeId
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|hosts
operator|.
name|add
argument_list|(
name|sniffedHost
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
name|hosts
return|;
block|}
block|}
DECL|method|readNode
specifier|private
specifier|static
name|HttpHost
name|readNode
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|JsonParser
name|parser
parameter_list|,
name|String
name|scheme
parameter_list|)
throws|throws
name|IOException
block|{
name|HttpHost
name|httpHost
init|=
literal|null
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|FIELD_NAME
condition|)
block|{
name|fieldName
operator|=
name|parser
operator|.
name|getCurrentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parser
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"http"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|VALUE_STRING
operator|&&
literal|"publish_address"
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|getCurrentName
argument_list|()
argument_list|)
condition|)
block|{
name|URI
name|boundAddressAsURI
init|=
name|URI
operator|.
name|create
argument_list|(
name|scheme
operator|+
literal|"://"
operator|+
name|parser
operator|.
name|getValueAsString
argument_list|()
argument_list|)
decl_stmt|;
name|httpHost
operator|=
operator|new
name|HttpHost
argument_list|(
name|boundAddressAsURI
operator|.
name|getHost
argument_list|()
argument_list|,
name|boundAddressAsURI
operator|.
name|getPort
argument_list|()
argument_list|,
name|boundAddressAsURI
operator|.
name|getScheme
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parser
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|//http section is not present if http is not enabled on the node, ignore such nodes
if|if
condition|(
name|httpHost
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"skipping node ["
operator|+
name|nodeId
operator|+
literal|"] with http disabled"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|httpHost
return|;
block|}
block|}
end_class

end_unit

