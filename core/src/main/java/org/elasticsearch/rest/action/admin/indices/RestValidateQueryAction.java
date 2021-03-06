begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.indices
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
name|client
operator|.
name|node
operator|.
name|NodeClient
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
name|ParsingException
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
name|BaseRestHandler
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
name|BytesRestResponse
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
name|RestChannel
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
name|RestController
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
name|rest
operator|.
name|RestResponse
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
name|RestBuilderListener
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|RestActions
operator|.
name|buildBroadcastShardsHeader
import|;
end_import

begin_class
DECL|class|RestValidateQueryAction
specifier|public
class|class
name|RestValidateQueryAction
extends|extends
name|BaseRestHandler
block|{
DECL|method|RestValidateQueryAction
specifier|public
name|RestValidateQueryAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
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
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"validate_query_action"
return|;
block|}
annotation|@
name|Override
DECL|method|prepareRequest
specifier|public
name|RestChannelConsumer
name|prepareRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
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
name|validateQueryRequest
operator|.
name|explain
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"explain"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
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
name|validateQueryRequest
operator|.
name|rewrite
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"rewrite"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|validateQueryRequest
operator|.
name|allShards
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"all_shards"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|Exception
name|bodyParsingException
init|=
literal|null
decl_stmt|;
try|try
block|{
name|request
operator|.
name|withContentOrSourceParamParserOrNull
argument_list|(
name|parser
lambda|->
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|validateQueryRequest
operator|.
name|query
argument_list|(
name|RestActions
operator|.
name|getQueryContent
argument_list|(
name|parser
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"q"
argument_list|)
condition|)
block|{
name|validateQueryRequest
operator|.
name|query
argument_list|(
name|RestActions
operator|.
name|urlParamsToQueryBuilder
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|bodyParsingException
operator|=
name|e
expr_stmt|;
block|}
specifier|final
name|Exception
name|finalBodyParsingException
init|=
name|bodyParsingException
decl_stmt|;
return|return
name|channel
lambda|->
block|{
if|if
condition|(
name|finalBodyParsingException
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|finalBodyParsingException
operator|instanceof
name|ParsingException
condition|)
block|{
name|handleException
argument_list|(
name|validateQueryRequest
argument_list|,
operator|(
operator|(
name|ParsingException
operator|)
name|finalBodyParsingException
operator|)
operator|.
name|getDetailedMessage
argument_list|()
argument_list|,
name|channel
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|handleException
argument_list|(
name|validateQueryRequest
argument_list|,
name|finalBodyParsingException
operator|.
name|getMessage
argument_list|()
argument_list|,
name|channel
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
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
name|VALID_FIELD
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
name|EXPLANATIONS_FIELD
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
name|INDEX_FIELD
argument_list|,
name|explanation
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|explanation
operator|.
name|getShard
argument_list|()
operator|>=
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|SHARD_FIELD
argument_list|,
name|explanation
operator|.
name|getShard
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|VALID_FIELD
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
name|ERROR_FIELD
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
name|EXPLANATION_FIELD
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
return|;
block|}
DECL|method|handleException
specifier|private
name|void
name|handleException
parameter_list|(
specifier|final
name|ValidateQueryRequest
name|request
parameter_list|,
specifier|final
name|String
name|message
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|)
throws|throws
name|IOException
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|buildErrorResponse
argument_list|(
name|channel
operator|.
name|newBuilder
argument_list|()
argument_list|,
name|message
argument_list|,
name|request
operator|.
name|explain
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|buildErrorResponse
specifier|private
specifier|static
name|BytesRestResponse
name|buildErrorResponse
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|String
name|error
parameter_list|,
name|boolean
name|explain
parameter_list|)
throws|throws
name|IOException
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
name|VALID_FIELD
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|explain
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|ERROR_FIELD
argument_list|,
name|error
argument_list|)
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
DECL|field|INDEX_FIELD
specifier|private
specifier|static
specifier|final
name|String
name|INDEX_FIELD
init|=
literal|"index"
decl_stmt|;
DECL|field|SHARD_FIELD
specifier|private
specifier|static
specifier|final
name|String
name|SHARD_FIELD
init|=
literal|"shard"
decl_stmt|;
DECL|field|VALID_FIELD
specifier|private
specifier|static
specifier|final
name|String
name|VALID_FIELD
init|=
literal|"valid"
decl_stmt|;
DECL|field|EXPLANATIONS_FIELD
specifier|private
specifier|static
specifier|final
name|String
name|EXPLANATIONS_FIELD
init|=
literal|"explanations"
decl_stmt|;
DECL|field|ERROR_FIELD
specifier|private
specifier|static
specifier|final
name|String
name|ERROR_FIELD
init|=
literal|"error"
decl_stmt|;
DECL|field|EXPLANATION_FIELD
specifier|private
specifier|static
specifier|final
name|String
name|EXPLANATION_FIELD
init|=
literal|"explanation"
decl_stmt|;
block|}
end_class

end_unit

