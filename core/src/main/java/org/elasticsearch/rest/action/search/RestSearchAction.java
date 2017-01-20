begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|search
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
name|SearchType
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
name|XContentParser
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
name|QueryParseContext
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
name|RestStatusToXContentListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|Scroll
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|builder
operator|.
name|SearchSourceBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|StoredFieldsContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|FetchSourceContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
operator|.
name|SortOrder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|SuggestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|term
operator|.
name|TermSuggestionBuilder
operator|.
name|SuggestMode
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
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|parseTimeValue
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
name|search
operator|.
name|suggest
operator|.
name|SuggestBuilders
operator|.
name|termSuggestion
import|;
end_import

begin_class
DECL|class|RestSearchAction
specifier|public
class|class
name|RestSearchAction
extends|extends
name|BaseRestHandler
block|{
DECL|method|RestSearchAction
specifier|public
name|RestSearchAction
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
literal|"/_search"
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
literal|"/_search"
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
literal|"/{index}/_search"
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
literal|"/{index}/_search"
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
literal|"/{index}/{type}/_search"
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
literal|"/{index}/{type}/_search"
argument_list|,
name|this
argument_list|)
expr_stmt|;
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
name|SearchRequest
name|searchRequest
init|=
operator|new
name|SearchRequest
argument_list|()
decl_stmt|;
name|request
operator|.
name|withContentOrSourceParamParserOrNull
argument_list|(
name|parser
lambda|->
name|parseSearchRequest
argument_list|(
name|searchRequest
argument_list|,
name|request
argument_list|,
name|parser
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|channel
lambda|->
name|client
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|,
operator|new
name|RestStatusToXContentListener
argument_list|<>
argument_list|(
name|channel
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Parses the rest request on top of the SearchRequest, preserving values that are not overridden by the rest request.      *      * @param requestContentParser body of the request to read. This method does not attempt to read the body from the {@code request}      *        parameter      */
DECL|method|parseSearchRequest
specifier|public
specifier|static
name|void
name|parseSearchRequest
parameter_list|(
name|SearchRequest
name|searchRequest
parameter_list|,
name|RestRequest
name|request
parameter_list|,
name|XContentParser
name|requestContentParser
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|searchRequest
operator|.
name|source
argument_list|()
operator|==
literal|null
condition|)
block|{
name|searchRequest
operator|.
name|source
argument_list|(
operator|new
name|SearchSourceBuilder
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|searchRequest
operator|.
name|indices
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
expr_stmt|;
if|if
condition|(
name|requestContentParser
operator|!=
literal|null
condition|)
block|{
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|requestContentParser
argument_list|)
decl_stmt|;
name|searchRequest
operator|.
name|source
argument_list|()
operator|.
name|parseXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
comment|// do not allow 'query_and_fetch' or 'dfs_query_and_fetch' search types
comment|// from the REST layer. these modes are an internal optimization and should
comment|// not be specified explicitly by the user.
name|String
name|searchType
init|=
name|request
operator|.
name|param
argument_list|(
literal|"search_type"
argument_list|)
decl_stmt|;
if|if
condition|(
name|SearchType
operator|.
name|fromString
argument_list|(
name|searchType
argument_list|)
operator|.
name|equals
argument_list|(
name|SearchType
operator|.
name|QUERY_AND_FETCH
argument_list|)
operator|||
name|SearchType
operator|.
name|fromString
argument_list|(
name|searchType
argument_list|)
operator|.
name|equals
argument_list|(
name|SearchType
operator|.
name|DFS_QUERY_AND_FETCH
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unsupported search type ["
operator|+
name|searchType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
else|else
block|{
name|searchRequest
operator|.
name|searchType
argument_list|(
name|searchType
argument_list|)
expr_stmt|;
block|}
name|parseSearchSource
argument_list|(
name|searchRequest
operator|.
name|source
argument_list|()
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|searchRequest
operator|.
name|requestCache
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"request_cache"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|scroll
init|=
name|request
operator|.
name|param
argument_list|(
literal|"scroll"
argument_list|)
decl_stmt|;
if|if
condition|(
name|scroll
operator|!=
literal|null
condition|)
block|{
name|searchRequest
operator|.
name|scroll
argument_list|(
operator|new
name|Scroll
argument_list|(
name|parseTimeValue
argument_list|(
name|scroll
argument_list|,
literal|null
argument_list|,
literal|"scroll"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|searchRequest
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
name|searchRequest
operator|.
name|routing
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
name|searchRequest
operator|.
name|preference
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"preference"
argument_list|)
argument_list|)
expr_stmt|;
name|searchRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|fromRequest
argument_list|(
name|request
argument_list|,
name|searchRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Parses the rest request on top of the SearchSourceBuilder, preserving      * values that are not overridden by the rest request.      */
DECL|method|parseSearchSource
specifier|private
specifier|static
name|void
name|parseSearchSource
parameter_list|(
specifier|final
name|SearchSourceBuilder
name|searchSourceBuilder
parameter_list|,
name|RestRequest
name|request
parameter_list|)
block|{
name|QueryBuilder
name|queryBuilder
init|=
name|RestActions
operator|.
name|urlParamsToQueryBuilder
argument_list|(
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|!=
literal|null
condition|)
block|{
name|searchSourceBuilder
operator|.
name|query
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
block|}
name|int
name|from
init|=
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"from"
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|from
operator|!=
operator|-
literal|1
condition|)
block|{
name|searchSourceBuilder
operator|.
name|from
argument_list|(
name|from
argument_list|)
expr_stmt|;
block|}
name|int
name|size
init|=
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"size"
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|size
operator|!=
operator|-
literal|1
condition|)
block|{
name|searchSourceBuilder
operator|.
name|size
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"explain"
argument_list|)
condition|)
block|{
name|searchSourceBuilder
operator|.
name|explain
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"explain"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"version"
argument_list|)
condition|)
block|{
name|searchSourceBuilder
operator|.
name|version
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"version"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"timeout"
argument_list|)
condition|)
block|{
name|searchSourceBuilder
operator|.
name|timeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"timeout"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"terminate_after"
argument_list|)
condition|)
block|{
name|int
name|terminateAfter
init|=
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"terminate_after"
argument_list|,
name|SearchContext
operator|.
name|DEFAULT_TERMINATE_AFTER
argument_list|)
decl_stmt|;
if|if
condition|(
name|terminateAfter
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"terminateAfter must be> 0"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|terminateAfter
operator|>
literal|0
condition|)
block|{
name|searchSourceBuilder
operator|.
name|terminateAfter
argument_list|(
name|terminateAfter
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|request
operator|.
name|param
argument_list|(
literal|"fields"
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The parameter ["
operator|+
name|SearchSourceBuilder
operator|.
name|FIELDS_FIELD
operator|+
literal|"] is no longer supported, please use ["
operator|+
name|SearchSourceBuilder
operator|.
name|STORED_FIELDS_FIELD
operator|+
literal|"] to retrieve stored fields or _source filtering "
operator|+
literal|"if the field is not stored"
argument_list|)
throw|;
block|}
name|StoredFieldsContext
name|storedFieldsContext
init|=
name|StoredFieldsContext
operator|.
name|fromRestRequest
argument_list|(
name|SearchSourceBuilder
operator|.
name|STORED_FIELDS_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|storedFieldsContext
operator|!=
literal|null
condition|)
block|{
name|searchSourceBuilder
operator|.
name|storedFields
argument_list|(
name|storedFieldsContext
argument_list|)
expr_stmt|;
block|}
name|String
name|sDocValueFields
init|=
name|request
operator|.
name|param
argument_list|(
literal|"docvalue_fields"
argument_list|)
decl_stmt|;
if|if
condition|(
name|sDocValueFields
operator|==
literal|null
condition|)
block|{
name|sDocValueFields
operator|=
name|request
operator|.
name|param
argument_list|(
literal|"fielddata_fields"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sDocValueFields
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|sDocValueFields
argument_list|)
condition|)
block|{
name|String
index|[]
name|sFields
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|sDocValueFields
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|sFields
control|)
block|{
name|searchSourceBuilder
operator|.
name|docValueField
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|FetchSourceContext
name|fetchSourceContext
init|=
name|FetchSourceContext
operator|.
name|parseFromRestRequest
argument_list|(
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|fetchSourceContext
operator|!=
literal|null
condition|)
block|{
name|searchSourceBuilder
operator|.
name|fetchSource
argument_list|(
name|fetchSourceContext
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"track_scores"
argument_list|)
condition|)
block|{
name|searchSourceBuilder
operator|.
name|trackScores
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"track_scores"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|sSorts
init|=
name|request
operator|.
name|param
argument_list|(
literal|"sort"
argument_list|)
decl_stmt|;
if|if
condition|(
name|sSorts
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|sorts
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|sSorts
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|sort
range|:
name|sorts
control|)
block|{
name|int
name|delimiter
init|=
name|sort
operator|.
name|lastIndexOf
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|delimiter
operator|!=
operator|-
literal|1
condition|)
block|{
name|String
name|sortField
init|=
name|sort
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|delimiter
argument_list|)
decl_stmt|;
name|String
name|reverse
init|=
name|sort
operator|.
name|substring
argument_list|(
name|delimiter
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"asc"
operator|.
name|equals
argument_list|(
name|reverse
argument_list|)
condition|)
block|{
name|searchSourceBuilder
operator|.
name|sort
argument_list|(
name|sortField
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"desc"
operator|.
name|equals
argument_list|(
name|reverse
argument_list|)
condition|)
block|{
name|searchSourceBuilder
operator|.
name|sort
argument_list|(
name|sortField
argument_list|,
name|SortOrder
operator|.
name|DESC
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|searchSourceBuilder
operator|.
name|sort
argument_list|(
name|sort
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|String
name|sStats
init|=
name|request
operator|.
name|param
argument_list|(
literal|"stats"
argument_list|)
decl_stmt|;
if|if
condition|(
name|sStats
operator|!=
literal|null
condition|)
block|{
name|searchSourceBuilder
operator|.
name|stats
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|sStats
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|suggestField
init|=
name|request
operator|.
name|param
argument_list|(
literal|"suggest_field"
argument_list|)
decl_stmt|;
if|if
condition|(
name|suggestField
operator|!=
literal|null
condition|)
block|{
name|String
name|suggestText
init|=
name|request
operator|.
name|param
argument_list|(
literal|"suggest_text"
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"q"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|suggestSize
init|=
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"suggest_size"
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|String
name|suggestMode
init|=
name|request
operator|.
name|param
argument_list|(
literal|"suggest_mode"
argument_list|)
decl_stmt|;
name|searchSourceBuilder
operator|.
name|suggest
argument_list|(
operator|new
name|SuggestBuilder
argument_list|()
operator|.
name|addSuggestion
argument_list|(
name|suggestField
argument_list|,
name|termSuggestion
argument_list|(
name|suggestField
argument_list|)
operator|.
name|text
argument_list|(
name|suggestText
argument_list|)
operator|.
name|size
argument_list|(
name|suggestSize
argument_list|)
operator|.
name|suggestMode
argument_list|(
name|SuggestMode
operator|.
name|resolve
argument_list|(
name|suggestMode
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

