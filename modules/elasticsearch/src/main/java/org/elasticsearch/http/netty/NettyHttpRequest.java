begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
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
name|elasticsearch
operator|.
name|rest
operator|.
name|support
operator|.
name|AbstractRestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|support
operator|.
name|RestUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ChannelBufferInputStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
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
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpMethod
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|NettyHttpRequest
specifier|public
class|class
name|NettyHttpRequest
extends|extends
name|AbstractRestRequest
implements|implements
name|HttpRequest
block|{
DECL|field|request
specifier|private
specifier|final
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpRequest
name|request
decl_stmt|;
DECL|field|params
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
decl_stmt|;
DECL|field|path
specifier|private
name|String
name|path
decl_stmt|;
DECL|method|NettyHttpRequest
specifier|public
name|NettyHttpRequest
parameter_list|(
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpRequest
name|request
parameter_list|)
block|{
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|params
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|String
name|uri
init|=
name|request
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|int
name|pathEndPos
init|=
name|uri
operator|.
name|indexOf
argument_list|(
literal|'?'
argument_list|)
decl_stmt|;
if|if
condition|(
name|pathEndPos
operator|<
literal|0
condition|)
block|{
name|this
operator|.
name|path
operator|=
name|uri
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|path
operator|=
name|uri
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|pathEndPos
argument_list|)
expr_stmt|;
name|RestUtils
operator|.
name|decodeQueryString
argument_list|(
name|uri
argument_list|,
name|pathEndPos
operator|+
literal|1
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|method
annotation|@
name|Override
specifier|public
name|Method
name|method
parameter_list|()
block|{
name|HttpMethod
name|httpMethod
init|=
name|request
operator|.
name|getMethod
argument_list|()
decl_stmt|;
if|if
condition|(
name|httpMethod
operator|==
name|HttpMethod
operator|.
name|GET
condition|)
return|return
name|Method
operator|.
name|GET
return|;
if|if
condition|(
name|httpMethod
operator|==
name|HttpMethod
operator|.
name|POST
condition|)
return|return
name|Method
operator|.
name|POST
return|;
if|if
condition|(
name|httpMethod
operator|==
name|HttpMethod
operator|.
name|PUT
condition|)
return|return
name|Method
operator|.
name|PUT
return|;
if|if
condition|(
name|httpMethod
operator|==
name|HttpMethod
operator|.
name|DELETE
condition|)
return|return
name|Method
operator|.
name|DELETE
return|;
return|return
name|Method
operator|.
name|GET
return|;
block|}
DECL|method|uri
annotation|@
name|Override
specifier|public
name|String
name|uri
parameter_list|()
block|{
return|return
name|request
operator|.
name|getUri
argument_list|()
return|;
block|}
DECL|method|path
annotation|@
name|Override
specifier|public
name|String
name|path
parameter_list|()
block|{
return|return
name|path
return|;
block|}
DECL|method|params
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|()
block|{
return|return
name|params
return|;
block|}
DECL|method|hasContent
annotation|@
name|Override
specifier|public
name|boolean
name|hasContent
parameter_list|()
block|{
return|return
name|request
operator|.
name|getContent
argument_list|()
operator|.
name|readableBytes
argument_list|()
operator|>
literal|0
return|;
block|}
DECL|method|contentAsStream
annotation|@
name|Override
specifier|public
name|InputStream
name|contentAsStream
parameter_list|()
block|{
return|return
operator|new
name|ChannelBufferInputStream
argument_list|(
name|request
operator|.
name|getContent
argument_list|()
argument_list|)
return|;
block|}
DECL|method|contentAsBytes
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|contentAsBytes
parameter_list|()
block|{
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
name|request
operator|.
name|getContent
argument_list|()
operator|.
name|readableBytes
argument_list|()
index|]
decl_stmt|;
name|request
operator|.
name|getContent
argument_list|()
operator|.
name|getBytes
argument_list|(
name|request
operator|.
name|getContent
argument_list|()
operator|.
name|readerIndex
argument_list|()
argument_list|,
name|data
argument_list|)
expr_stmt|;
return|return
name|data
return|;
block|}
DECL|method|contentAsString
annotation|@
name|Override
specifier|public
name|String
name|contentAsString
parameter_list|()
block|{
return|return
name|request
operator|.
name|getContent
argument_list|()
operator|.
name|toString
argument_list|(
literal|"UTF-8"
argument_list|)
return|;
block|}
DECL|method|headerNames
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|headerNames
parameter_list|()
block|{
return|return
name|request
operator|.
name|getHeaderNames
argument_list|()
return|;
block|}
DECL|method|header
annotation|@
name|Override
specifier|public
name|String
name|header
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|request
operator|.
name|getHeader
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|headers
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|headers
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|request
operator|.
name|getHeaders
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|cookie
annotation|@
name|Override
specifier|public
name|String
name|cookie
parameter_list|()
block|{
return|return
name|request
operator|.
name|getHeader
argument_list|(
name|HttpHeaders
operator|.
name|Names
operator|.
name|COOKIE
argument_list|)
return|;
block|}
DECL|method|hasParam
annotation|@
name|Override
specifier|public
name|boolean
name|hasParam
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|params
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
return|;
block|}
DECL|method|param
annotation|@
name|Override
specifier|public
name|String
name|param
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|params
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
block|}
end_class

end_unit

