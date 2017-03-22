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
name|ElasticsearchClient
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
argument_list|>
block|{
DECL|method|ValidateQueryRequestBuilder
specifier|public
name|ValidateQueryRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|ValidateQueryAction
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
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
comment|/**      * The query to validate.      *      * @see org.elasticsearch.index.query.QueryBuilders      */
DECL|method|setQuery
specifier|public
name|ValidateQueryRequestBuilder
name|setQuery
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
name|request
operator|.
name|query
argument_list|(
name|queryBuilder
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
comment|/**      * Indicates whether the query should be rewritten into primitive queries      */
DECL|method|setRewrite
specifier|public
name|ValidateQueryRequestBuilder
name|setRewrite
parameter_list|(
name|boolean
name|rewrite
parameter_list|)
block|{
name|request
operator|.
name|rewrite
argument_list|(
name|rewrite
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Indicates whether the query should be validated on all shards      */
DECL|method|setAllShards
specifier|public
name|ValidateQueryRequestBuilder
name|setAllShards
parameter_list|(
name|boolean
name|rewrite
parameter_list|)
block|{
name|request
operator|.
name|allShards
argument_list|(
name|rewrite
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

