begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.validate.query
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
name|validate
operator|.
name|query
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
name|QuerySourceBuilder
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
name|broadcast
operator|.
name|BroadcastOperationRequestBuilder
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
name|index
operator|.
name|query
operator|.
name|QueryBuilder
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ValidateQueryRequestBuilder
specifier|public
class|class
name|ValidateQueryRequestBuilder
extends|extends
name|BroadcastOperationRequestBuilder
argument_list|<
name|ValidateQueryRequest
argument_list|,
name|ValidateQueryResponse
argument_list|,
name|ValidateQueryRequestBuilder
argument_list|,
name|IndicesAdminClient
argument_list|>
block|{
DECL|field|sourceBuilder
specifier|private
name|QuerySourceBuilder
name|sourceBuilder
decl_stmt|;
DECL|method|ValidateQueryRequestBuilder
specifier|public
name|ValidateQueryRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
operator|new
name|ValidateQueryRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * The types of documents the query will run against. Defaults to all types.      */
DECL|method|setTypes
specifier|public
name|ValidateQueryRequestBuilder
name|setTypes
parameter_list|(
name|String
modifier|...
name|types
parameter_list|)
block|{
name|request
operator|.
name|types
argument_list|(
name|types
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The query source to validate.      *      * @see org.elasticsearch.index.query.QueryBuilders      */
DECL|method|setQuery
specifier|public
name|ValidateQueryRequestBuilder
name|setQuery
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|setQuery
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The source to validate.      *      * @see org.elasticsearch.index.query.QueryBuilders      */
DECL|method|setSource
specifier|public
name|ValidateQueryRequestBuilder
name|setSource
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|request
argument_list|()
operator|.
name|source
argument_list|(
name|source
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The source to validate.      *      * @see org.elasticsearch.index.query.QueryBuilders      */
DECL|method|setSource
specifier|public
name|ValidateQueryRequestBuilder
name|setSource
parameter_list|(
name|BytesReference
name|source
parameter_list|,
name|boolean
name|unsafe
parameter_list|)
block|{
name|request
argument_list|()
operator|.
name|source
argument_list|(
name|source
argument_list|,
name|unsafe
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The source to validate.      *      * @see org.elasticsearch.index.query.QueryBuilders      */
DECL|method|setSource
specifier|public
name|ValidateQueryRequestBuilder
name|setSource
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Indicates if detailed information about the query should be returned.      *      * @see org.elasticsearch.index.query.QueryBuilders      */
DECL|method|setExplain
specifier|public
name|ValidateQueryRequestBuilder
name|setExplain
parameter_list|(
name|boolean
name|explain
parameter_list|)
block|{
name|request
operator|.
name|explain
argument_list|(
name|explain
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
name|ValidateQueryResponse
argument_list|>
name|listener
parameter_list|)
block|{
if|if
condition|(
name|sourceBuilder
operator|!=
literal|null
condition|)
block|{
name|request
operator|.
name|source
argument_list|(
name|sourceBuilder
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|validateQuery
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|sourceBuilder
specifier|private
name|QuerySourceBuilder
name|sourceBuilder
parameter_list|()
block|{
if|if
condition|(
name|sourceBuilder
operator|==
literal|null
condition|)
block|{
name|sourceBuilder
operator|=
operator|new
name|QuerySourceBuilder
argument_list|()
expr_stmt|;
block|}
return|return
name|sourceBuilder
return|;
block|}
block|}
end_class

end_unit

