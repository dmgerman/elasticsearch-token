begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.indices.validate.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
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
name|admin
operator|.
name|indices
operator|.
name|validate
operator|.
name|query
operator|.
name|QueryExplanation
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
name|validate
operator|.
name|query
operator|.
name|ValidateQueryRequest
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
name|validate
operator|.
name|query
operator|.
name|ValidateQueryResponse
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
name|IndicesOptions
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
name|common
operator|.
name|Strings
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
name|inject
operator|.
name|Inject
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
name|settings
operator|.
name|Settings
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
name|XContentBuilder
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
name|*
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
name|action
operator|.
name|support
operator|.
name|RestActions
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
name|action
operator|.
name|support
operator|.
name|RestBuilderListener
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|GET
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|POST
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|OK
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|support
operator|.
name|RestActions
operator|.
name|buildBroadcastShardsHeader
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestValidateQueryAction
specifier|public
class|class
name|RestValidateQueryAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestValidateQueryAction
specifier|public
name|RestValidateQueryAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|controller
argument_list|,
name|client
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_validate/query"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/_validate/query"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/_validate/query"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/_validate/query"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/_validate/query"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/_validate/query"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|,
specifier|final
name|Client
name|client
parameter_list|)
block|{
name|ValidateQueryRequest
name|validateQueryRequest
init|=
operator|new
name|ValidateQueryRequest
argument_list|(
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|validateQueryRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|fromRequest
argument_list|(
name|request
argument_list|,
name|validateQueryRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|RestActions
operator|.
name|hasBodyContent
argument_list|(
name|request
argument_list|)
condition|)
block|{
name|validateQueryRequest
operator|.
name|source
argument_list|(
name|RestActions
operator|.
name|getRestContent
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|QuerySourceBuilder
name|querySourceBuilder
init|=
name|RestActions
operator|.
name|parseQuerySource
argument_list|(
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|querySourceBuilder
operator|!=
literal|null
condition|)
block|{
name|validateQueryRequest
operator|.
name|source
argument_list|(
name|querySourceBuilder
argument_list|)
expr_stmt|;
block|}
block|}
name|validateQueryRequest
operator|.
name|types
argument_list|(
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"type"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"explain"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|validateQueryRequest
operator|.
name|explain
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|validateQueryRequest
operator|.
name|explain
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"rewrite"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|validateQueryRequest
operator|.
name|rewrite
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|validateQueryRequest
operator|.
name|rewrite
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|validateQuery
argument_list|(
name|validateQueryRequest
argument_list|,
operator|new
name|RestBuilderListener
argument_list|<
name|ValidateQueryResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|RestResponse
name|buildResponse
parameter_list|(
name|ValidateQueryResponse
name|response
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|Exception
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"valid"
argument_list|,
name|response
operator|.
name|isValid
argument_list|()
argument_list|)
expr_stmt|;
name|buildBroadcastShardsHeader
argument_list|(
name|builder
argument_list|,
name|request
argument_list|,
name|response
argument_list|)
expr_stmt|;
if|if
condition|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"explanations"
argument_list|)
expr_stmt|;
for|for
control|(
name|QueryExplanation
name|explanation
range|:
name|response
operator|.
name|getQueryExplanation
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|explanation
operator|.
name|getIndex
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
name|explanation
operator|.
name|getIndex
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"valid"
argument_list|,
name|explanation
operator|.
name|isValid
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|explanation
operator|.
name|getError
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"error"
argument_list|,
name|explanation
operator|.
name|getError
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|explanation
operator|.
name|getExplanation
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"explanation"
argument_list|,
name|explanation
operator|.
name|getExplanation
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|OK
argument_list|,
name|builder
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
