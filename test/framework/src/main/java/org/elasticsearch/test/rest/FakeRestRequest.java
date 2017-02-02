begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
package|;
end_package

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
name|common
operator|.
name|xcontent
operator|.
name|XContentType
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
name|Collections
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

begin_class
DECL|class|FakeRestRequest
specifier|public
class|class
name|FakeRestRequest
extends|extends
name|RestRequest
block|{
DECL|field|content
specifier|private
specifier|final
name|BytesReference
name|content
decl_stmt|;
DECL|field|method
specifier|private
specifier|final
name|Method
name|method
decl_stmt|;
DECL|field|remoteAddress
specifier|private
specifier|final
name|SocketAddress
name|remoteAddress
decl_stmt|;
DECL|method|FakeRestRequest
specifier|public
name|FakeRestRequest
parameter_list|()
block|{
name|this
argument_list|(
name|NamedXContentRegistry
operator|.
name|EMPTY
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|,
literal|null
argument_list|,
name|Method
operator|.
name|GET
argument_list|,
literal|"/"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|FakeRestRequest
specifier|private
name|FakeRestRequest
parameter_list|(
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|headers
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|BytesReference
name|content
parameter_list|,
name|Method
name|method
parameter_list|,
name|String
name|path
parameter_list|,
name|SocketAddress
name|remoteAddress
parameter_list|)
block|{
name|super
argument_list|(
name|xContentRegistry
argument_list|,
name|params
argument_list|,
name|path
argument_list|,
name|headers
argument_list|)
expr_stmt|;
name|this
operator|.
name|content
operator|=
name|content
expr_stmt|;
name|this
operator|.
name|method
operator|=
name|method
expr_stmt|;
name|this
operator|.
name|remoteAddress
operator|=
name|remoteAddress
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|method
specifier|public
name|Method
name|method
parameter_list|()
block|{
return|return
name|method
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
name|rawPath
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
operator|!=
literal|null
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
annotation|@
name|Override
DECL|method|getRemoteAddress
specifier|public
name|SocketAddress
name|getRemoteAddress
parameter_list|()
block|{
return|return
name|remoteAddress
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|xContentRegistry
specifier|private
specifier|final
name|NamedXContentRegistry
name|xContentRegistry
decl_stmt|;
DECL|field|headers
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|headers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
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
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|content
specifier|private
name|BytesReference
name|content
decl_stmt|;
DECL|field|path
specifier|private
name|String
name|path
init|=
literal|"/"
decl_stmt|;
DECL|field|method
specifier|private
name|Method
name|method
init|=
name|Method
operator|.
name|GET
decl_stmt|;
DECL|field|address
specifier|private
name|SocketAddress
name|address
init|=
literal|null
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|)
block|{
name|this
operator|.
name|xContentRegistry
operator|=
name|xContentRegistry
expr_stmt|;
block|}
DECL|method|withHeaders
specifier|public
name|Builder
name|withHeaders
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|headers
parameter_list|)
block|{
name|this
operator|.
name|headers
operator|=
name|headers
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|withParams
specifier|public
name|Builder
name|withParams
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|withContent
specifier|public
name|Builder
name|withContent
parameter_list|(
name|BytesReference
name|content
parameter_list|,
name|XContentType
name|xContentType
parameter_list|)
block|{
name|this
operator|.
name|content
operator|=
name|content
expr_stmt|;
if|if
condition|(
name|xContentType
operator|!=
literal|null
condition|)
block|{
name|headers
operator|.
name|put
argument_list|(
literal|"Content-Type"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|xContentType
operator|.
name|mediaType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|withPath
specifier|public
name|Builder
name|withPath
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
DECL|method|withMethod
specifier|public
name|Builder
name|withMethod
parameter_list|(
name|Method
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
DECL|method|withRemoteAddress
specifier|public
name|Builder
name|withRemoteAddress
parameter_list|(
name|SocketAddress
name|address
parameter_list|)
block|{
name|this
operator|.
name|address
operator|=
name|address
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|FakeRestRequest
name|build
parameter_list|()
block|{
return|return
operator|new
name|FakeRestRequest
argument_list|(
name|xContentRegistry
argument_list|,
name|headers
argument_list|,
name|params
argument_list|,
name|content
argument_list|,
name|method
argument_list|,
name|path
argument_list|,
name|address
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

