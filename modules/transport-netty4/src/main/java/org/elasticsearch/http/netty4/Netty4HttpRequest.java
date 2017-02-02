begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http.netty4
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty4
package|;
end_package

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channel
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|FullHttpRequest
import|;
end_import

begin_import
import|import
name|io
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
name|io
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|bytes
operator|.
name|BytesArray
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
name|bytes
operator|.
name|BytesReference
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
name|xcontent
operator|.
name|NamedXContentRegistry
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
name|RestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty4
operator|.
name|Netty4Utils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_class
DECL|class|Netty4HttpRequest
specifier|public
class|class
name|Netty4HttpRequest
extends|extends
name|RestRequest
block|{
DECL|field|request
specifier|private
specifier|final
name|FullHttpRequest
name|request
decl_stmt|;
DECL|field|channel
specifier|private
specifier|final
name|Channel
name|channel
decl_stmt|;
DECL|field|content
specifier|private
specifier|final
name|BytesReference
name|content
decl_stmt|;
DECL|method|Netty4HttpRequest
name|Netty4HttpRequest
parameter_list|(
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
name|FullHttpRequest
name|request
parameter_list|,
name|Channel
name|channel
parameter_list|)
block|{
name|super
argument_list|(
name|xContentRegistry
argument_list|,
name|request
operator|.
name|uri
argument_list|()
argument_list|,
operator|new
name|HttpHeadersMap
argument_list|(
name|request
operator|.
name|headers
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|channel
operator|=
name|channel
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|content
argument_list|()
operator|.
name|isReadable
argument_list|()
condition|)
block|{
name|this
operator|.
name|content
operator|=
name|Netty4Utils
operator|.
name|toBytesReference
argument_list|(
name|request
operator|.
name|content
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|content
operator|=
name|BytesArray
operator|.
name|EMPTY
expr_stmt|;
block|}
block|}
DECL|method|request
specifier|public
name|FullHttpRequest
name|request
parameter_list|()
block|{
return|return
name|this
operator|.
name|request
return|;
block|}
annotation|@
name|Override
DECL|method|method
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
name|method
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
if|if
condition|(
name|httpMethod
operator|==
name|HttpMethod
operator|.
name|HEAD
condition|)
block|{
return|return
name|Method
operator|.
name|HEAD
return|;
block|}
if|if
condition|(
name|httpMethod
operator|==
name|HttpMethod
operator|.
name|OPTIONS
condition|)
block|{
return|return
name|Method
operator|.
name|OPTIONS
return|;
block|}
return|return
name|Method
operator|.
name|GET
return|;
block|}
annotation|@
name|Override
DECL|method|uri
specifier|public
name|String
name|uri
parameter_list|()
block|{
return|return
name|request
operator|.
name|uri
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|hasContent
specifier|public
name|boolean
name|hasContent
parameter_list|()
block|{
return|return
name|content
operator|.
name|length
argument_list|()
operator|>
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|content
specifier|public
name|BytesReference
name|content
parameter_list|()
block|{
return|return
name|content
return|;
block|}
comment|/**      * Returns the remote address where this rest request channel is "connected to".  The      * returned {@link SocketAddress} is supposed to be down-cast into more      * concrete type such as {@link java.net.InetSocketAddress} to retrieve      * the detailed information.      */
annotation|@
name|Override
DECL|method|getRemoteAddress
specifier|public
name|SocketAddress
name|getRemoteAddress
parameter_list|()
block|{
return|return
name|channel
operator|.
name|remoteAddress
argument_list|()
return|;
block|}
comment|/**      * Returns the local address where this request channel is bound to.  The returned      * {@link SocketAddress} is supposed to be down-cast into more concrete      * type such as {@link java.net.InetSocketAddress} to retrieve the detailed      * information.      */
annotation|@
name|Override
DECL|method|getLocalAddress
specifier|public
name|SocketAddress
name|getLocalAddress
parameter_list|()
block|{
return|return
name|channel
operator|.
name|localAddress
argument_list|()
return|;
block|}
DECL|method|getChannel
specifier|public
name|Channel
name|getChannel
parameter_list|()
block|{
return|return
name|channel
return|;
block|}
comment|/**      * A wrapper of {@link HttpHeaders} that implements a map to prevent copying unnecessarily. This class does not support modifications      * and due to the underlying implementation, it performs case insensitive lookups of key to values.      *      * It is important to note that this implementation does have some downsides in that each invocation of the      * {@link #values()} and {@link #entrySet()} methods will perform a copy of the values in the HttpHeaders rather than returning a      * view of the underlying values.      */
DECL|class|HttpHeadersMap
specifier|private
specifier|static
class|class
name|HttpHeadersMap
implements|implements
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
block|{
DECL|field|httpHeaders
specifier|private
specifier|final
name|HttpHeaders
name|httpHeaders
decl_stmt|;
DECL|method|HttpHeadersMap
specifier|private
name|HttpHeadersMap
parameter_list|(
name|HttpHeaders
name|httpHeaders
parameter_list|)
block|{
name|this
operator|.
name|httpHeaders
operator|=
name|httpHeaders
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|httpHeaders
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|httpHeaders
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|containsKey
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|key
operator|instanceof
name|String
operator|&&
name|httpHeaders
operator|.
name|contains
argument_list|(
operator|(
name|String
operator|)
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|containsValue
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|value
operator|instanceof
name|List
operator|&&
name|httpHeaders
operator|.
name|names
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|httpHeaders
operator|::
name|getAll
argument_list|)
operator|.
name|anyMatch
argument_list|(
name|value
operator|::
name|equals
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|key
operator|instanceof
name|String
condition|?
name|httpHeaders
operator|.
name|getAll
argument_list|(
operator|(
name|String
operator|)
name|key
argument_list|)
else|:
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|put
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|put
parameter_list|(
name|String
name|key
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"modifications are not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|remove
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"modifications are not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|putAll
specifier|public
name|void
name|putAll
parameter_list|(
name|Map
argument_list|<
name|?
extends|extends
name|String
argument_list|,
name|?
extends|extends
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|m
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"modifications are not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"modifications are not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|keySet
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|keySet
parameter_list|()
block|{
return|return
name|httpHeaders
operator|.
name|names
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|values
specifier|public
name|Collection
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|values
parameter_list|()
block|{
return|return
name|httpHeaders
operator|.
name|names
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|k
lambda|->
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|httpHeaders
operator|.
name|getAll
argument_list|(
name|k
argument_list|)
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|entrySet
specifier|public
name|Set
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
name|httpHeaders
operator|.
name|names
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|k
lambda|->
operator|new
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
block|{
block|@Override                 public String getKey(
argument_list|)
block|{
return|return
name|k
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getValue
parameter_list|()
block|{
return|return
name|httpHeaders
operator|.
name|getAll
argument_list|(
name|k
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|setValue
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"modifications are not supported"
argument_list|)
throw|;
block|}
block|}
block|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_class

unit|} }
end_unit

