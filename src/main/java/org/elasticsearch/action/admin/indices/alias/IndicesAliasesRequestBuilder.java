begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.alias
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
name|alias
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
name|support
operator|.
name|master
operator|.
name|MasterNodeOperationRequestBuilder
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
name|client
operator|.
name|internal
operator|.
name|InternalIndicesAdminClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|AliasAction
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|FilterBuilder
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
comment|/**  *  */
end_comment

begin_class
DECL|class|IndicesAliasesRequestBuilder
specifier|public
class|class
name|IndicesAliasesRequestBuilder
extends|extends
name|MasterNodeOperationRequestBuilder
argument_list|<
name|IndicesAliasesRequest
argument_list|,
name|IndicesAliasesResponse
argument_list|,
name|IndicesAliasesRequestBuilder
argument_list|>
block|{
DECL|method|IndicesAliasesRequestBuilder
specifier|public
name|IndicesAliasesRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalIndicesAdminClient
operator|)
name|indicesClient
argument_list|,
operator|new
name|IndicesAliasesRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds an alias to the index.      *      * @param index The index      * @param alias The alias      */
DECL|method|addAlias
specifier|public
name|IndicesAliasesRequestBuilder
name|addAlias
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
name|request
operator|.
name|addAlias
argument_list|(
name|index
argument_list|,
name|alias
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds an alias to the index.      *      * @param index  The index      * @param alias  The alias      * @param filter The filter      */
DECL|method|addAlias
specifier|public
name|IndicesAliasesRequestBuilder
name|addAlias
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|alias
parameter_list|,
name|String
name|filter
parameter_list|)
block|{
name|request
operator|.
name|addAlias
argument_list|(
name|index
argument_list|,
name|alias
argument_list|,
name|filter
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds an alias to the index.      *      * @param index  The index      * @param alias  The alias      * @param filter The filter      */
DECL|method|addAlias
specifier|public
name|IndicesAliasesRequestBuilder
name|addAlias
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|alias
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|filter
parameter_list|)
block|{
name|request
operator|.
name|addAlias
argument_list|(
name|index
argument_list|,
name|alias
argument_list|,
name|filter
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds an alias to the index.      *      * @param index         The index      * @param alias         The alias      * @param filterBuilder The filter      */
DECL|method|addAlias
specifier|public
name|IndicesAliasesRequestBuilder
name|addAlias
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|alias
parameter_list|,
name|FilterBuilder
name|filterBuilder
parameter_list|)
block|{
name|request
operator|.
name|addAlias
argument_list|(
name|index
argument_list|,
name|alias
argument_list|,
name|filterBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds an alias action to the request.      *      * @param aliasAction The alias Action      */
DECL|method|addAliasAction
specifier|public
name|IndicesAliasesRequestBuilder
name|addAliasAction
parameter_list|(
name|AliasAction
name|aliasAction
parameter_list|)
block|{
name|request
operator|.
name|addAliasAction
argument_list|(
name|aliasAction
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Removes an alias to the index.      *      * @param index The index      * @param alias The alias      */
DECL|method|removeAlias
specifier|public
name|IndicesAliasesRequestBuilder
name|removeAlias
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
name|request
operator|.
name|removeAlias
argument_list|(
name|index
argument_list|,
name|alias
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets operation timeout.      *      * @param timeout      */
DECL|method|setTimeout
specifier|public
name|IndicesAliasesRequestBuilder
name|setTimeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|setTimeout
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
name|IndicesAliasesResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|(
operator|(
name|IndicesAdminClient
operator|)
name|client
operator|)
operator|.
name|aliases
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

