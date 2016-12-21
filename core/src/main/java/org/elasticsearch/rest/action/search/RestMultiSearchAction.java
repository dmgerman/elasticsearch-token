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
name|ElasticsearchParseException
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
name|MultiSearchRequest
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
name|ParseFieldMatcher
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
name|XContent
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
name|XContentFactory
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
name|RestToXContentListener
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
name|SearchRequestParsers
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|BiConsumer
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
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
operator|.
name|lenientNodeBooleanValue
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
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
operator|.
name|nodeStringArrayValue
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
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
operator|.
name|nodeStringValue
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

begin_class
DECL|class|RestMultiSearchAction
specifier|public
class|class
name|RestMultiSearchAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|allowExplicitIndex
specifier|private
specifier|final
name|boolean
name|allowExplicitIndex
decl_stmt|;
DECL|field|searchRequestParsers
specifier|private
specifier|final
name|SearchRequestParsers
name|searchRequestParsers
decl_stmt|;
annotation|@
name|Inject
DECL|method|RestMultiSearchAction
specifier|public
name|RestMultiSearchAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|SearchRequestParsers
name|searchRequestParsers
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchRequestParsers
operator|=
name|searchRequestParsers
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_msearch"
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
literal|"/_msearch"
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
literal|"/{index}/_msearch"
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
literal|"/{index}/_msearch"
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
literal|"/{index}/{type}/_msearch"
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
literal|"/{index}/{type}/_msearch"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|allowExplicitIndex
operator|=
name|MULTI_ALLOW_EXPLICIT_INDEX
operator|.
name|get
argument_list|(
name|settings
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
name|MultiSearchRequest
name|multiSearchRequest
init|=
name|parseRequest
argument_list|(
name|request
argument_list|,
name|allowExplicitIndex
argument_list|,
name|searchRequestParsers
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
return|return
name|channel
lambda|->
name|client
operator|.
name|multiSearch
argument_list|(
name|multiSearchRequest
argument_list|,
operator|new
name|RestToXContentListener
argument_list|<>
argument_list|(
name|channel
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Parses a {@link RestRequest} body and returns a {@link MultiSearchRequest}      */
DECL|method|parseRequest
specifier|public
specifier|static
name|MultiSearchRequest
name|parseRequest
parameter_list|(
name|RestRequest
name|restRequest
parameter_list|,
name|boolean
name|allowExplicitIndex
parameter_list|,
name|SearchRequestParsers
name|searchRequestParsers
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
throws|throws
name|IOException
block|{
name|MultiSearchRequest
name|multiRequest
init|=
operator|new
name|MultiSearchRequest
argument_list|()
decl_stmt|;
if|if
condition|(
name|restRequest
operator|.
name|hasParam
argument_list|(
literal|"max_concurrent_searches"
argument_list|)
condition|)
block|{
name|multiRequest
operator|.
name|maxConcurrentSearchRequests
argument_list|(
name|restRequest
operator|.
name|paramAsInt
argument_list|(
literal|"max_concurrent_searches"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|parseMultiLineRequest
argument_list|(
name|restRequest
argument_list|,
name|multiRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|allowExplicitIndex
argument_list|,
parameter_list|(
name|searchRequest
parameter_list|,
name|parser
parameter_list|)
lambda|->
block|{
try|try
block|{
specifier|final
name|QueryParseContext
name|queryParseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|searchRequestParsers
operator|.
name|queryParsers
argument_list|,
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
name|searchRequest
operator|.
name|source
argument_list|(
name|SearchSourceBuilder
operator|.
name|fromXContent
argument_list|(
name|queryParseContext
argument_list|,
name|searchRequestParsers
operator|.
name|aggParsers
argument_list|,
name|searchRequestParsers
operator|.
name|suggesters
argument_list|,
name|searchRequestParsers
operator|.
name|searchExtParsers
argument_list|)
argument_list|)
expr_stmt|;
name|multiRequest
operator|.
name|add
argument_list|(
name|searchRequest
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Exception when parsing search request"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|multiRequest
return|;
block|}
comment|/**      * Parses a multi-line {@link RestRequest} body, instanciating a {@link SearchRequest} for each line and applying the given consumer.      */
DECL|method|parseMultiLineRequest
specifier|public
specifier|static
name|void
name|parseMultiLineRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|IndicesOptions
name|indicesOptions
parameter_list|,
name|boolean
name|allowExplicitIndex
parameter_list|,
name|BiConsumer
argument_list|<
name|SearchRequest
argument_list|,
name|XContentParser
argument_list|>
name|consumer
parameter_list|)
throws|throws
name|IOException
block|{
name|String
index|[]
name|indices
init|=
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
decl_stmt|;
name|String
index|[]
name|types
init|=
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
decl_stmt|;
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
name|String
name|routing
init|=
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
decl_stmt|;
specifier|final
name|BytesReference
name|data
init|=
name|request
operator|.
name|contentOrSourceParam
argument_list|()
decl_stmt|;
name|XContent
name|xContent
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|int
name|from
init|=
literal|0
decl_stmt|;
name|int
name|length
init|=
name|data
operator|.
name|length
argument_list|()
decl_stmt|;
name|byte
name|marker
init|=
name|xContent
operator|.
name|streamSeparator
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|nextMarker
init|=
name|findNextMarker
argument_list|(
name|marker
argument_list|,
name|from
argument_list|,
name|data
argument_list|,
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|nextMarker
operator|==
operator|-
literal|1
condition|)
block|{
break|break;
block|}
comment|// support first line with \n
if|if
condition|(
name|nextMarker
operator|==
literal|0
condition|)
block|{
name|from
operator|=
name|nextMarker
operator|+
literal|1
expr_stmt|;
continue|continue;
block|}
name|SearchRequest
name|searchRequest
init|=
operator|new
name|SearchRequest
argument_list|()
decl_stmt|;
if|if
condition|(
name|indices
operator|!=
literal|null
condition|)
block|{
name|searchRequest
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indicesOptions
operator|!=
literal|null
condition|)
block|{
name|searchRequest
operator|.
name|indicesOptions
argument_list|(
name|indicesOptions
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|types
operator|!=
literal|null
operator|&&
name|types
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|searchRequest
operator|.
name|types
argument_list|(
name|types
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|routing
operator|!=
literal|null
condition|)
block|{
name|searchRequest
operator|.
name|routing
argument_list|(
name|routing
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|searchType
operator|!=
literal|null
condition|)
block|{
name|searchRequest
operator|.
name|searchType
argument_list|(
name|searchType
argument_list|)
expr_stmt|;
block|}
name|IndicesOptions
name|defaultOptions
init|=
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
decl_stmt|;
comment|// now parse the action
if|if
condition|(
name|nextMarker
operator|-
name|from
operator|>
literal|0
condition|)
block|{
try|try
init|(
name|XContentParser
name|parser
init|=
name|xContent
operator|.
name|createParser
argument_list|(
name|request
operator|.
name|getXContentRegistry
argument_list|()
argument_list|,
name|data
operator|.
name|slice
argument_list|(
name|from
argument_list|,
name|nextMarker
operator|-
name|from
argument_list|)
argument_list|)
init|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
name|parser
operator|.
name|map
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|source
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Object
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"index"
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|||
literal|"indices"
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|allowExplicitIndex
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"explicit index in multi search is not allowed"
argument_list|)
throw|;
block|}
name|searchRequest
operator|.
name|indices
argument_list|(
name|nodeStringArrayValue
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"type"
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|||
literal|"types"
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|searchRequest
operator|.
name|types
argument_list|(
name|nodeStringArrayValue
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"search_type"
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|||
literal|"searchType"
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|searchRequest
operator|.
name|searchType
argument_list|(
name|nodeStringValue
argument_list|(
name|value
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"request_cache"
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|||
literal|"requestCache"
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|searchRequest
operator|.
name|requestCache
argument_list|(
name|lenientNodeBooleanValue
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"preference"
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|searchRequest
operator|.
name|preference
argument_list|(
name|nodeStringValue
argument_list|(
name|value
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"routing"
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|searchRequest
operator|.
name|routing
argument_list|(
name|nodeStringValue
argument_list|(
name|value
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|defaultOptions
operator|=
name|IndicesOptions
operator|.
name|fromMap
argument_list|(
name|source
argument_list|,
name|defaultOptions
argument_list|)
expr_stmt|;
block|}
block|}
name|searchRequest
operator|.
name|indicesOptions
argument_list|(
name|defaultOptions
argument_list|)
expr_stmt|;
comment|// move pointers
name|from
operator|=
name|nextMarker
operator|+
literal|1
expr_stmt|;
comment|// now for the body
name|nextMarker
operator|=
name|findNextMarker
argument_list|(
name|marker
argument_list|,
name|from
argument_list|,
name|data
argument_list|,
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|nextMarker
operator|==
operator|-
literal|1
condition|)
block|{
break|break;
block|}
name|BytesReference
name|bytes
init|=
name|data
operator|.
name|slice
argument_list|(
name|from
argument_list|,
name|nextMarker
operator|-
name|from
argument_list|)
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytes
argument_list|)
operator|.
name|createParser
argument_list|(
name|request
operator|.
name|getXContentRegistry
argument_list|()
argument_list|,
name|bytes
argument_list|)
init|)
block|{
name|consumer
operator|.
name|accept
argument_list|(
name|searchRequest
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
comment|// move pointers
name|from
operator|=
name|nextMarker
operator|+
literal|1
expr_stmt|;
block|}
block|}
DECL|method|findNextMarker
specifier|private
specifier|static
name|int
name|findNextMarker
parameter_list|(
name|byte
name|marker
parameter_list|,
name|int
name|from
parameter_list|,
name|BytesReference
name|data
parameter_list|,
name|int
name|length
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
name|from
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|data
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
name|marker
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit

