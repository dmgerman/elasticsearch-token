begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.warmer.put
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|warmer
operator|.
name|put
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
name|admin
operator|.
name|indices
operator|.
name|support
operator|.
name|BaseIndicesRequestBuilder
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
name|search
operator|.
name|SearchRequest
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
name|search
operator|.
name|SearchRequestBuilder
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
name|IndicesAdminClient
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
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PutWarmerRequestBuilder
specifier|public
class|class
name|PutWarmerRequestBuilder
extends|extends
name|BaseIndicesRequestBuilder
argument_list|<
name|PutWarmerRequest
argument_list|,
name|PutWarmerResponse
argument_list|>
block|{
DECL|method|PutWarmerRequestBuilder
specifier|public
name|PutWarmerRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|indicesClient
argument_list|,
operator|new
name|PutWarmerRequest
argument_list|()
operator|.
name|name
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|PutWarmerRequestBuilder
specifier|public
name|PutWarmerRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|)
block|{
name|super
argument_list|(
name|indicesClient
argument_list|,
operator|new
name|PutWarmerRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the name of the warmer.      */
DECL|method|setName
specifier|public
name|PutWarmerRequestBuilder
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|request
operator|.
name|name
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the search request to use to warm the index when applicable.      */
DECL|method|setSearchRequest
specifier|public
name|PutWarmerRequestBuilder
name|setSearchRequest
parameter_list|(
name|SearchRequest
name|searchRequest
parameter_list|)
block|{
name|request
operator|.
name|searchRequest
argument_list|(
name|searchRequest
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the search request to use to warm the index when applicable.      */
DECL|method|setSearchRequest
specifier|public
name|PutWarmerRequestBuilder
name|setSearchRequest
parameter_list|(
name|SearchRequestBuilder
name|searchRequest
parameter_list|)
block|{
name|request
operator|.
name|searchRequest
argument_list|(
name|searchRequest
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the master node timeout in case the master has not yet been discovered.      */
DECL|method|setMasterNodeTimeout
specifier|public
name|PutWarmerRequestBuilder
name|setMasterNodeTimeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|masterNodeTimeout
argument_list|(
name|timeout
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
name|PutWarmerResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|client
operator|.
name|putWarmer
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

