begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.warmer.delete
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
name|delete
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
name|admin
operator|.
name|indices
operator|.
name|IndicesAction
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|DeleteWarmerAction
specifier|public
class|class
name|DeleteWarmerAction
extends|extends
name|IndicesAction
argument_list|<
name|DeleteWarmerRequest
argument_list|,
name|DeleteWarmerResponse
argument_list|,
name|DeleteWarmerRequestBuilder
argument_list|>
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|DeleteWarmerAction
name|INSTANCE
init|=
operator|new
name|DeleteWarmerAction
argument_list|()
decl_stmt|;
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"indices/warmer/delete"
decl_stmt|;
DECL|method|DeleteWarmerAction
specifier|private
name|DeleteWarmerAction
parameter_list|()
block|{
name|super
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|public
name|DeleteWarmerResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|DeleteWarmerResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newRequestBuilder
specifier|public
name|DeleteWarmerRequestBuilder
name|newRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|client
parameter_list|)
block|{
return|return
operator|new
name|DeleteWarmerRequestBuilder
argument_list|(
name|client
argument_list|)
return|;
block|}
block|}
end_class

end_unit

