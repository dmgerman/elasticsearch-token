begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionRequestBuilder
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
name|Client
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
name|internal
operator|.
name|InternalClient
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
name|Nullable
import|;
end_import

begin_comment
comment|/**  * A multi get document action request builder.  */
end_comment

begin_class
DECL|class|MultiGetRequestBuilder
specifier|public
class|class
name|MultiGetRequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|MultiGetRequest
argument_list|,
name|MultiGetResponse
argument_list|,
name|MultiGetRequestBuilder
argument_list|>
block|{
DECL|method|MultiGetRequestBuilder
specifier|public
name|MultiGetRequestBuilder
parameter_list|(
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalClient
operator|)
name|client
argument_list|,
operator|new
name|MultiGetRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|MultiGetRequestBuilder
name|add
parameter_list|(
name|String
name|index
parameter_list|,
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|request
operator|.
name|add
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|add
specifier|public
name|MultiGetRequestBuilder
name|add
parameter_list|(
name|String
name|index
parameter_list|,
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
name|Iterable
argument_list|<
name|String
argument_list|>
name|ids
parameter_list|)
block|{
for|for
control|(
name|String
name|id
range|:
name|ids
control|)
block|{
name|request
operator|.
name|add
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|add
specifier|public
name|MultiGetRequestBuilder
name|add
parameter_list|(
name|String
name|index
parameter_list|,
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
name|String
modifier|...
name|ids
parameter_list|)
block|{
for|for
control|(
name|String
name|id
range|:
name|ids
control|)
block|{
name|request
operator|.
name|add
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|add
specifier|public
name|MultiGetRequestBuilder
name|add
parameter_list|(
name|MultiGetRequest
operator|.
name|Item
name|item
parameter_list|)
block|{
name|request
operator|.
name|add
argument_list|(
name|item
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to      *<tt>_local</tt> to prefer local shards,<tt>_primary</tt> to execute only on primary shards, or      * a custom value, which guarantees that the same order will be used across different requests.      */
DECL|method|setPreference
specifier|public
name|MultiGetRequestBuilder
name|setPreference
parameter_list|(
name|String
name|preference
parameter_list|)
block|{
name|request
operator|.
name|setPreference
argument_list|(
name|preference
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should a refresh be executed before this get operation causing the operation to      * return the latest value. Note, heavy get should not set this to<tt>true</tt>. Defaults      * to<tt>false</tt>.      */
DECL|method|setRefresh
specifier|public
name|MultiGetRequestBuilder
name|setRefresh
parameter_list|(
name|boolean
name|refresh
parameter_list|)
block|{
name|request
operator|.
name|setRefresh
argument_list|(
name|refresh
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setRealtime
specifier|public
name|MultiGetRequestBuilder
name|setRealtime
parameter_list|(
name|Boolean
name|realtime
parameter_list|)
block|{
name|request
operator|.
name|setRealtime
argument_list|(
name|realtime
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|ActionListener
argument_list|<
name|MultiGetResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|(
operator|(
name|Client
operator|)
name|client
operator|)
operator|.
name|multiGet
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

