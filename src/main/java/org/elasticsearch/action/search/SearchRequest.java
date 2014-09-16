begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
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
name|ElasticsearchGenerationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|ActionRequest
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
name|ActionRequestValidationException
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
name|IndicesRequest
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
name|Requests
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
name|BytesArray
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|script
operator|.
name|ScriptService
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
name|Collections
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|Scroll
operator|.
name|readScroll
import|;
end_import

begin_comment
comment|/**  * A request to execute search against one or more indices (or all). Best created using  * {@link org.elasticsearch.client.Requests#searchRequest(String...)}.  *<p/>  *<p>Note, the search {@link #source(org.elasticsearch.search.builder.SearchSourceBuilder)}  * is required. The search source is the different search options, including aggregations and such.  *<p/>  *<p>There is an option to specify an addition search source using the {@link #extraSource(org.elasticsearch.search.builder.SearchSourceBuilder)}.  *  * @see org.elasticsearch.client.Requests#searchRequest(String...)  * @see org.elasticsearch.client.Client#search(SearchRequest)  * @see SearchResponse  */
end_comment

begin_class
DECL|class|SearchRequest
specifier|public
class|class
name|SearchRequest
extends|extends
name|ActionRequest
argument_list|<
name|SearchRequest
argument_list|>
implements|implements
name|IndicesRequest
block|{
DECL|field|searchType
specifier|private
name|SearchType
name|searchType
init|=
name|SearchType
operator|.
name|DEFAULT
decl_stmt|;
DECL|field|indices
specifier|private
name|String
index|[]
name|indices
decl_stmt|;
annotation|@
name|Nullable
DECL|field|routing
specifier|private
name|String
name|routing
decl_stmt|;
annotation|@
name|Nullable
DECL|field|preference
specifier|private
name|String
name|preference
decl_stmt|;
DECL|field|templateSource
specifier|private
name|BytesReference
name|templateSource
decl_stmt|;
DECL|field|templateSourceUnsafe
specifier|private
name|boolean
name|templateSourceUnsafe
decl_stmt|;
DECL|field|templateName
specifier|private
name|String
name|templateName
decl_stmt|;
DECL|field|templateType
specifier|private
name|ScriptService
operator|.
name|ScriptType
name|templateType
decl_stmt|;
DECL|field|templateParams
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateParams
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
DECL|field|source
specifier|private
name|BytesReference
name|source
decl_stmt|;
DECL|field|sourceUnsafe
specifier|private
name|boolean
name|sourceUnsafe
decl_stmt|;
DECL|field|extraSource
specifier|private
name|BytesReference
name|extraSource
decl_stmt|;
DECL|field|extraSourceUnsafe
specifier|private
name|boolean
name|extraSourceUnsafe
decl_stmt|;
DECL|field|queryCache
specifier|private
name|Boolean
name|queryCache
decl_stmt|;
DECL|field|scroll
specifier|private
name|Scroll
name|scroll
decl_stmt|;
DECL|field|types
specifier|private
name|String
index|[]
name|types
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|DEFAULT_INDICES_OPTIONS
specifier|public
specifier|static
specifier|final
name|IndicesOptions
name|DEFAULT_INDICES_OPTIONS
init|=
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
decl_stmt|;
DECL|field|indicesOptions
specifier|private
name|IndicesOptions
name|indicesOptions
init|=
name|DEFAULT_INDICES_OPTIONS
decl_stmt|;
DECL|method|SearchRequest
specifier|public
name|SearchRequest
parameter_list|()
block|{     }
comment|/**      * Copy constructor that creates a new search request that is a copy of the one provided as an argument.      * The new request will inherit though headers and context from the original request that caused it.      */
DECL|method|SearchRequest
specifier|public
name|SearchRequest
parameter_list|(
name|SearchRequest
name|searchRequest
parameter_list|,
name|ActionRequest
name|originalRequest
parameter_list|)
block|{
name|super
argument_list|(
name|originalRequest
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchType
operator|=
name|searchRequest
operator|.
name|searchType
expr_stmt|;
name|this
operator|.
name|indices
operator|=
name|searchRequest
operator|.
name|indices
expr_stmt|;
name|this
operator|.
name|routing
operator|=
name|searchRequest
operator|.
name|routing
expr_stmt|;
name|this
operator|.
name|preference
operator|=
name|searchRequest
operator|.
name|preference
expr_stmt|;
name|this
operator|.
name|templateSource
operator|=
name|searchRequest
operator|.
name|templateSource
expr_stmt|;
name|this
operator|.
name|templateSourceUnsafe
operator|=
name|searchRequest
operator|.
name|templateSourceUnsafe
expr_stmt|;
name|this
operator|.
name|templateName
operator|=
name|searchRequest
operator|.
name|templateName
expr_stmt|;
name|this
operator|.
name|templateType
operator|=
name|searchRequest
operator|.
name|templateType
expr_stmt|;
name|this
operator|.
name|templateParams
operator|=
name|searchRequest
operator|.
name|templateParams
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|searchRequest
operator|.
name|source
expr_stmt|;
name|this
operator|.
name|sourceUnsafe
operator|=
name|searchRequest
operator|.
name|sourceUnsafe
expr_stmt|;
name|this
operator|.
name|extraSource
operator|=
name|searchRequest
operator|.
name|extraSource
expr_stmt|;
name|this
operator|.
name|extraSourceUnsafe
operator|=
name|searchRequest
operator|.
name|extraSourceUnsafe
expr_stmt|;
name|this
operator|.
name|queryCache
operator|=
name|searchRequest
operator|.
name|queryCache
expr_stmt|;
name|this
operator|.
name|scroll
operator|=
name|searchRequest
operator|.
name|scroll
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|searchRequest
operator|.
name|types
expr_stmt|;
name|this
operator|.
name|indicesOptions
operator|=
name|searchRequest
operator|.
name|indicesOptions
expr_stmt|;
block|}
comment|/**      * Constructs a new search request starting from the provided request, meaning that it will      * inherit its headers and context      */
DECL|method|SearchRequest
specifier|public
name|SearchRequest
parameter_list|(
name|ActionRequest
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs a new search request against the indices. No indices provided here means that search      * will run against all indices.      */
DECL|method|SearchRequest
specifier|public
name|SearchRequest
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs a new search request against the provided indices with the given search source.      */
DECL|method|SearchRequest
specifier|public
name|SearchRequest
parameter_list|(
name|String
index|[]
name|indices
parameter_list|,
name|byte
index|[]
name|source
parameter_list|)
block|{
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
name|this
operator|.
name|source
operator|=
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
literal|null
decl_stmt|;
comment|// no need to check, we resolve to match all query
comment|//        if (source == null&& extraSource == null) {
comment|//            validationException = addValidationError("search source is missing", validationException);
comment|//        }
return|return
name|validationException
return|;
block|}
DECL|method|beforeStart
specifier|public
name|void
name|beforeStart
parameter_list|()
block|{
comment|// we always copy over if needed, the reason is that a request might fail while being search remotely
comment|// and then we need to keep the buffer around
if|if
condition|(
name|source
operator|!=
literal|null
operator|&&
name|sourceUnsafe
condition|)
block|{
name|source
operator|=
name|source
operator|.
name|copyBytesArray
argument_list|()
expr_stmt|;
name|sourceUnsafe
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|extraSource
operator|!=
literal|null
operator|&&
name|extraSourceUnsafe
condition|)
block|{
name|extraSource
operator|=
name|extraSource
operator|.
name|copyBytesArray
argument_list|()
expr_stmt|;
name|extraSourceUnsafe
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|templateSource
operator|!=
literal|null
operator|&&
name|templateSourceUnsafe
condition|)
block|{
name|templateSource
operator|=
name|templateSource
operator|.
name|copyBytesArray
argument_list|()
expr_stmt|;
name|templateSourceUnsafe
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|/**      * Sets the indices the search will be executed on.      */
DECL|method|indices
specifier|public
name|SearchRequest
name|indices
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
if|if
condition|(
name|indices
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"indices must not be null"
argument_list|)
throw|;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|indices
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"indices["
operator|+
name|i
operator|+
literal|"] must not be null"
argument_list|)
throw|;
block|}
block|}
block|}
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|indicesOptions
specifier|public
name|IndicesOptions
name|indicesOptions
parameter_list|()
block|{
return|return
name|indicesOptions
return|;
block|}
DECL|method|indicesOptions
specifier|public
name|SearchRequest
name|indicesOptions
parameter_list|(
name|IndicesOptions
name|indicesOptions
parameter_list|)
block|{
name|this
operator|.
name|indicesOptions
operator|=
name|indicesOptions
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The document types to execute the search against. Defaults to be executed against      * all types.      */
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|types
return|;
block|}
comment|/**      * The document types to execute the search against. Defaults to be executed against      * all types.      */
DECL|method|types
specifier|public
name|SearchRequest
name|types
parameter_list|(
name|String
modifier|...
name|types
parameter_list|)
block|{
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A comma separated list of routing values to control the shards the search will be executed on.      */
DECL|method|routing
specifier|public
name|String
name|routing
parameter_list|()
block|{
return|return
name|this
operator|.
name|routing
return|;
block|}
comment|/**      * A comma separated list of routing values to control the shards the search will be executed on.      */
DECL|method|routing
specifier|public
name|SearchRequest
name|routing
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|routing
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The routing values to control the shards that the search will be executed on.      */
DECL|method|routing
specifier|public
name|SearchRequest
name|routing
parameter_list|(
name|String
modifier|...
name|routings
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|Strings
operator|.
name|arrayToCommaDelimitedString
argument_list|(
name|routings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to      *<tt>_local</tt> to prefer local shards,<tt>_primary</tt> to execute only on primary shards, or      * a custom value, which guarantees that the same order will be used across different requests.      */
DECL|method|preference
specifier|public
name|SearchRequest
name|preference
parameter_list|(
name|String
name|preference
parameter_list|)
block|{
name|this
operator|.
name|preference
operator|=
name|preference
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|preference
specifier|public
name|String
name|preference
parameter_list|()
block|{
return|return
name|this
operator|.
name|preference
return|;
block|}
comment|/**      * The search type to execute, defaults to {@link SearchType#DEFAULT}.      */
DECL|method|searchType
specifier|public
name|SearchRequest
name|searchType
parameter_list|(
name|SearchType
name|searchType
parameter_list|)
block|{
name|this
operator|.
name|searchType
operator|=
name|searchType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The a string representation search type to execute, defaults to {@link SearchType#DEFAULT}. Can be      * one of "dfs_query_then_fetch"/"dfsQueryThenFetch", "dfs_query_and_fetch"/"dfsQueryAndFetch",      * "query_then_fetch"/"queryThenFetch", and "query_and_fetch"/"queryAndFetch".      */
DECL|method|searchType
specifier|public
name|SearchRequest
name|searchType
parameter_list|(
name|String
name|searchType
parameter_list|)
throws|throws
name|ElasticsearchIllegalArgumentException
block|{
return|return
name|searchType
argument_list|(
name|SearchType
operator|.
name|fromString
argument_list|(
name|searchType
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * The source of the search request.      */
DECL|method|source
specifier|public
name|SearchRequest
name|source
parameter_list|(
name|SearchSourceBuilder
name|sourceBuilder
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|sourceBuilder
operator|.
name|buildAsBytes
argument_list|(
name|Requests
operator|.
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
name|this
operator|.
name|sourceUnsafe
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The source of the search request. Consider using either {@link #source(byte[])} or      * {@link #source(org.elasticsearch.search.builder.SearchSourceBuilder)}.      */
DECL|method|source
specifier|public
name|SearchRequest
name|source
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|this
operator|.
name|sourceUnsafe
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The source of the search request in the form of a map.      */
DECL|method|source
specifier|public
name|SearchRequest
name|source
parameter_list|(
name|Map
name|source
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|Requests
operator|.
name|CONTENT_TYPE
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|source
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|source
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|source
specifier|public
name|SearchRequest
name|source
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|builder
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|sourceUnsafe
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The search source to execute.      */
DECL|method|source
specifier|public
name|SearchRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
return|return
name|source
argument_list|(
name|source
argument_list|,
literal|0
argument_list|,
name|source
operator|.
name|length
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**      * The search source to execute.      */
DECL|method|source
specifier|public
name|SearchRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|source
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**      * The search source to execute.      */
DECL|method|source
specifier|public
name|SearchRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|boolean
name|unsafe
parameter_list|)
block|{
return|return
name|source
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|,
name|unsafe
argument_list|)
return|;
block|}
comment|/**      * The search source to execute.      */
DECL|method|source
specifier|public
name|SearchRequest
name|source
parameter_list|(
name|BytesReference
name|source
parameter_list|,
name|boolean
name|unsafe
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|sourceUnsafe
operator|=
name|unsafe
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The search source to execute.      */
DECL|method|source
specifier|public
name|BytesReference
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
comment|/**      * The search source template to execute.      */
DECL|method|templateSource
specifier|public
name|BytesReference
name|templateSource
parameter_list|()
block|{
return|return
name|templateSource
return|;
block|}
comment|/**      * Allows to provide additional source that will be used as well.      */
DECL|method|extraSource
specifier|public
name|SearchRequest
name|extraSource
parameter_list|(
name|SearchSourceBuilder
name|sourceBuilder
parameter_list|)
block|{
if|if
condition|(
name|sourceBuilder
operator|==
literal|null
condition|)
block|{
name|extraSource
operator|=
literal|null
expr_stmt|;
return|return
name|this
return|;
block|}
name|this
operator|.
name|extraSource
operator|=
name|sourceBuilder
operator|.
name|buildAsBytes
argument_list|(
name|Requests
operator|.
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
name|this
operator|.
name|extraSourceUnsafe
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|extraSource
specifier|public
name|SearchRequest
name|extraSource
parameter_list|(
name|Map
name|extraSource
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|Requests
operator|.
name|CONTENT_TYPE
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|extraSource
argument_list|)
expr_stmt|;
return|return
name|extraSource
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|source
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|extraSource
specifier|public
name|SearchRequest
name|extraSource
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|extraSource
operator|=
name|builder
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|extraSourceUnsafe
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Allows to provide additional source that will use used as well.      */
DECL|method|extraSource
specifier|public
name|SearchRequest
name|extraSource
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|this
operator|.
name|extraSource
operator|=
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|this
operator|.
name|extraSourceUnsafe
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Allows to provide additional source that will be used as well.      */
DECL|method|extraSource
specifier|public
name|SearchRequest
name|extraSource
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
return|return
name|extraSource
argument_list|(
name|source
argument_list|,
literal|0
argument_list|,
name|source
operator|.
name|length
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**      * Allows to provide additional source that will be used as well.      */
DECL|method|extraSource
specifier|public
name|SearchRequest
name|extraSource
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|extraSource
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**      * Allows to provide additional source that will be used as well.      */
DECL|method|extraSource
specifier|public
name|SearchRequest
name|extraSource
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|boolean
name|unsafe
parameter_list|)
block|{
return|return
name|extraSource
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|,
name|unsafe
argument_list|)
return|;
block|}
comment|/**      * Allows to provide additional source that will be used as well.      */
DECL|method|extraSource
specifier|public
name|SearchRequest
name|extraSource
parameter_list|(
name|BytesReference
name|source
parameter_list|,
name|boolean
name|unsafe
parameter_list|)
block|{
name|this
operator|.
name|extraSource
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|extraSourceUnsafe
operator|=
name|unsafe
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Allows to provide template as source.      */
DECL|method|templateSource
specifier|public
name|SearchRequest
name|templateSource
parameter_list|(
name|BytesReference
name|template
parameter_list|,
name|boolean
name|unsafe
parameter_list|)
block|{
name|this
operator|.
name|templateSource
operator|=
name|template
expr_stmt|;
name|this
operator|.
name|templateSourceUnsafe
operator|=
name|unsafe
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The template of the search request.      */
DECL|method|templateSource
specifier|public
name|SearchRequest
name|templateSource
parameter_list|(
name|String
name|template
parameter_list|)
block|{
name|this
operator|.
name|templateSource
operator|=
operator|new
name|BytesArray
argument_list|(
name|template
argument_list|)
expr_stmt|;
name|this
operator|.
name|templateSourceUnsafe
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The name of the stored template      */
DECL|method|templateName
specifier|public
name|void
name|templateName
parameter_list|(
name|String
name|templateName
parameter_list|)
block|{
name|this
operator|.
name|templateName
operator|=
name|templateName
expr_stmt|;
block|}
DECL|method|templateType
specifier|public
name|void
name|templateType
parameter_list|(
name|ScriptService
operator|.
name|ScriptType
name|templateType
parameter_list|)
block|{
name|this
operator|.
name|templateType
operator|=
name|templateType
expr_stmt|;
block|}
comment|/**      * Template parameters used for rendering      */
DECL|method|templateParams
specifier|public
name|void
name|templateParams
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
name|this
operator|.
name|templateParams
operator|=
name|params
expr_stmt|;
block|}
comment|/**      * The name of the stored template      */
DECL|method|templateName
specifier|public
name|String
name|templateName
parameter_list|()
block|{
return|return
name|templateName
return|;
block|}
comment|/**      * The name of the stored template      */
DECL|method|templateType
specifier|public
name|ScriptService
operator|.
name|ScriptType
name|templateType
parameter_list|()
block|{
return|return
name|templateType
return|;
block|}
comment|/**      * Template parameters used for rendering      */
DECL|method|templateParams
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateParams
parameter_list|()
block|{
return|return
name|templateParams
return|;
block|}
comment|/**      * Additional search source to execute.      */
DECL|method|extraSource
specifier|public
name|BytesReference
name|extraSource
parameter_list|()
block|{
return|return
name|this
operator|.
name|extraSource
return|;
block|}
comment|/**      * The tye of search to execute.      */
DECL|method|searchType
specifier|public
name|SearchType
name|searchType
parameter_list|()
block|{
return|return
name|searchType
return|;
block|}
comment|/**      * The indices      */
annotation|@
name|Override
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
return|return
name|indices
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request.      */
DECL|method|scroll
specifier|public
name|Scroll
name|scroll
parameter_list|()
block|{
return|return
name|scroll
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request.      */
DECL|method|scroll
specifier|public
name|SearchRequest
name|scroll
parameter_list|(
name|Scroll
name|scroll
parameter_list|)
block|{
name|this
operator|.
name|scroll
operator|=
name|scroll
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request for the specified timeout.      */
DECL|method|scroll
specifier|public
name|SearchRequest
name|scroll
parameter_list|(
name|TimeValue
name|keepAlive
parameter_list|)
block|{
return|return
name|scroll
argument_list|(
operator|new
name|Scroll
argument_list|(
name|keepAlive
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request for the specified timeout.      */
DECL|method|scroll
specifier|public
name|SearchRequest
name|scroll
parameter_list|(
name|String
name|keepAlive
parameter_list|)
block|{
return|return
name|scroll
argument_list|(
operator|new
name|Scroll
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|keepAlive
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Sets if this request should use the query cache or not, assuming that it can (for      * example, if "now" is used, it will never be cached). By default (not set, or null,      * will default to the index level setting if query cache is enabled or not).      */
DECL|method|queryCache
specifier|public
name|SearchRequest
name|queryCache
parameter_list|(
name|Boolean
name|queryCache
parameter_list|)
block|{
name|this
operator|.
name|queryCache
operator|=
name|queryCache
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|queryCache
specifier|public
name|Boolean
name|queryCache
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryCache
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_1_2_0
argument_list|)
condition|)
block|{
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
comment|// backward comp. for operation threading
block|}
name|searchType
operator|=
name|SearchType
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|indices
operator|=
operator|new
name|String
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indices
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
name|routing
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|preference
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|scroll
operator|=
name|readScroll
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|sourceUnsafe
operator|=
literal|false
expr_stmt|;
name|source
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|extraSourceUnsafe
operator|=
literal|false
expr_stmt|;
name|extraSource
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|types
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|indicesOptions
operator|=
name|IndicesOptions
operator|.
name|readIndicesOptions
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_1_0
argument_list|)
condition|)
block|{
name|templateSourceUnsafe
operator|=
literal|false
expr_stmt|;
name|templateSource
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|templateName
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_3_0
argument_list|)
condition|)
block|{
name|templateType
operator|=
name|ScriptService
operator|.
name|ScriptType
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|templateParams
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
condition|)
block|{
name|queryCache
operator|=
name|in
operator|.
name|readOptionalBoolean
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_1_2_0
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
expr_stmt|;
comment|// operation threading
block|}
name|out
operator|.
name|writeByte
argument_list|(
name|searchType
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|indices
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|routing
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|preference
argument_list|)
expr_stmt|;
if|if
condition|(
name|scroll
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scroll
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBytesReference
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|extraSource
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|types
argument_list|)
expr_stmt|;
name|indicesOptions
operator|.
name|writeIndicesOptions
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_1_0
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeBytesReference
argument_list|(
name|templateSource
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|templateName
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_3_0
argument_list|)
condition|)
block|{
name|ScriptService
operator|.
name|ScriptType
operator|.
name|writeTo
argument_list|(
name|templateType
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
name|boolean
name|existTemplateParams
init|=
name|templateParams
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|existTemplateParams
argument_list|)
expr_stmt|;
if|if
condition|(
name|existTemplateParams
condition|)
block|{
name|out
operator|.
name|writeGenericValue
argument_list|(
name|templateParams
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeOptionalBoolean
argument_list|(
name|queryCache
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

