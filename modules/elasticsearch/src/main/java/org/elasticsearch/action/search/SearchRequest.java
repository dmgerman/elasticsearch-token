begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this   * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|UnicodeUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchGenerationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|Bytes
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
name|Unicode
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
name|BytesStream
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
name|common
operator|.
name|xcontent
operator|.
name|XContentType
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
name|Arrays
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
name|action
operator|.
name|Actions
operator|.
name|*
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
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * A request to execute search against one or more indices (or all). Best created using  * {@link org.elasticsearch.client.Requests#searchRequest(String...)}.  *  *<p>Note, the search {@link #source(org.elasticsearch.search.builder.SearchSourceBuilder)}  * is required. The search source is the different search options, including facets and such.  *  *<p>There is an option to specify an addition search source using the {@link #extraSource(org.elasticsearch.search.builder.SearchSourceBuilder)}.  *  * @author kimchy (shay.banon)  * @see org.elasticsearch.client.Requests#searchRequest(String...)  * @see org.elasticsearch.client.Client#search(SearchRequest)  * @see SearchResponse  */
end_comment

begin_class
DECL|class|SearchRequest
specifier|public
class|class
name|SearchRequest
implements|implements
name|ActionRequest
block|{
DECL|field|contentType
specifier|private
specifier|static
specifier|final
name|XContentType
name|contentType
init|=
name|Requests
operator|.
name|CONTENT_TYPE
decl_stmt|;
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
DECL|field|queryHint
annotation|@
name|Nullable
specifier|private
name|String
name|queryHint
decl_stmt|;
DECL|field|routing
annotation|@
name|Nullable
specifier|private
name|String
name|routing
decl_stmt|;
DECL|field|preference
annotation|@
name|Nullable
specifier|private
name|String
name|preference
decl_stmt|;
DECL|field|source
specifier|private
name|byte
index|[]
name|source
decl_stmt|;
DECL|field|sourceOffset
specifier|private
name|int
name|sourceOffset
decl_stmt|;
DECL|field|sourceLength
specifier|private
name|int
name|sourceLength
decl_stmt|;
DECL|field|sourceUnsafe
specifier|private
name|boolean
name|sourceUnsafe
decl_stmt|;
DECL|field|extraSource
specifier|private
name|byte
index|[]
name|extraSource
decl_stmt|;
DECL|field|extraSourceOffset
specifier|private
name|int
name|extraSourceOffset
decl_stmt|;
DECL|field|extraSourceLength
specifier|private
name|int
name|extraSourceLength
decl_stmt|;
DECL|field|extraSourceUnsafe
specifier|private
name|boolean
name|extraSourceUnsafe
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
DECL|field|timeout
specifier|private
name|TimeValue
name|timeout
decl_stmt|;
DECL|field|listenerThreaded
specifier|private
name|boolean
name|listenerThreaded
init|=
literal|false
decl_stmt|;
DECL|field|operationThreading
specifier|private
name|SearchOperationThreading
name|operationThreading
init|=
name|SearchOperationThreading
operator|.
name|THREAD_PER_SHARD
decl_stmt|;
DECL|method|SearchRequest
specifier|public
name|SearchRequest
parameter_list|()
block|{     }
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
name|this
operator|.
name|indices
operator|=
name|indices
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
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
DECL|method|validate
annotation|@
name|Override
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
if|if
condition|(
name|source
operator|==
literal|null
operator|&&
name|extraSource
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"search source is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
comment|/**      * Internal.      */
DECL|method|beforeLocalFork
specifier|public
name|void
name|beforeLocalFork
parameter_list|()
block|{
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
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|source
argument_list|,
name|sourceOffset
argument_list|,
name|sourceOffset
operator|+
name|sourceLength
argument_list|)
expr_stmt|;
name|sourceOffset
operator|=
literal|0
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
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|extraSource
argument_list|,
name|extraSourceOffset
argument_list|,
name|extraSourceOffset
operator|+
name|extraSourceLength
argument_list|)
expr_stmt|;
name|extraSourceOffset
operator|=
literal|0
expr_stmt|;
name|extraSourceUnsafe
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|listenerThreaded
annotation|@
name|Override
specifier|public
name|boolean
name|listenerThreaded
parameter_list|()
block|{
return|return
name|listenerThreaded
return|;
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
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|listenerThreaded
annotation|@
name|Override
specifier|public
name|SearchRequest
name|listenerThreaded
parameter_list|(
name|boolean
name|listenerThreaded
parameter_list|)
block|{
name|this
operator|.
name|listenerThreaded
operator|=
name|listenerThreaded
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Controls the the search operation threading model.      */
DECL|method|operationThreading
specifier|public
name|SearchOperationThreading
name|operationThreading
parameter_list|()
block|{
return|return
name|this
operator|.
name|operationThreading
return|;
block|}
comment|/**      * Controls the the search operation threading model.      */
DECL|method|operationThreading
specifier|public
name|SearchRequest
name|operationThreading
parameter_list|(
name|SearchOperationThreading
name|operationThreading
parameter_list|)
block|{
name|this
operator|.
name|operationThreading
operator|=
name|operationThreading
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the string representation of the operation threading model. Can be one of      * "no_threads", "single_thread" and "thread_per_shard".      */
DECL|method|operationThreading
specifier|public
name|SearchRequest
name|operationThreading
parameter_list|(
name|String
name|operationThreading
parameter_list|)
block|{
return|return
name|operationThreading
argument_list|(
name|SearchOperationThreading
operator|.
name|fromString
argument_list|(
name|operationThreading
argument_list|,
name|this
operator|.
name|operationThreading
argument_list|)
argument_list|)
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
name|ElasticSearchIllegalArgumentException
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
name|BytesStream
name|bos
init|=
name|sourceBuilder
operator|.
name|buildAsUnsafeBytes
argument_list|(
name|Requests
operator|.
name|CONTENT_TYPE
argument_list|)
decl_stmt|;
name|this
operator|.
name|source
operator|=
name|bos
operator|.
name|underlyingBytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|sourceLength
operator|=
name|bos
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|sourceUnsafe
operator|=
literal|true
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
name|UnicodeUtil
operator|.
name|UTF8Result
name|result
init|=
name|Unicode
operator|.
name|fromStringAsUtf8
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|this
operator|.
name|source
operator|=
name|result
operator|.
name|result
expr_stmt|;
name|this
operator|.
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|sourceLength
operator|=
name|result
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|sourceUnsafe
operator|=
literal|true
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
name|contentType
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
name|ElasticSearchGenerationException
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
try|try
block|{
name|this
operator|.
name|source
operator|=
name|builder
operator|.
name|underlyingBytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|sourceLength
operator|=
name|builder
operator|.
name|underlyingBytesLength
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
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|builder
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|sourceOffset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|sourceLength
operator|=
name|length
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
name|byte
index|[]
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|method|sourceOffset
specifier|public
name|int
name|sourceOffset
parameter_list|()
block|{
return|return
name|sourceOffset
return|;
block|}
DECL|method|sourceLength
specifier|public
name|int
name|sourceLength
parameter_list|()
block|{
return|return
name|sourceLength
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
name|BytesStream
name|bos
init|=
name|sourceBuilder
operator|.
name|buildAsUnsafeBytes
argument_list|(
name|Requests
operator|.
name|CONTENT_TYPE
argument_list|)
decl_stmt|;
name|this
operator|.
name|extraSource
operator|=
name|bos
operator|.
name|underlyingBytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|extraSourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|extraSourceLength
operator|=
name|bos
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|extraSourceUnsafe
operator|=
literal|true
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
name|contentType
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
name|ElasticSearchGenerationException
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
try|try
block|{
name|this
operator|.
name|extraSource
operator|=
name|builder
operator|.
name|underlyingBytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|extraSourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|extraSourceLength
operator|=
name|builder
operator|.
name|underlyingBytesLength
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
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|builder
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
name|UnicodeUtil
operator|.
name|UTF8Result
name|result
init|=
name|Unicode
operator|.
name|fromStringAsUtf8
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|this
operator|.
name|extraSource
operator|=
name|result
operator|.
name|result
expr_stmt|;
name|this
operator|.
name|extraSourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|extraSourceLength
operator|=
name|result
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|extraSourceUnsafe
operator|=
literal|true
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
name|this
operator|.
name|extraSource
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|extraSourceOffset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|extraSourceLength
operator|=
name|length
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
comment|/**      * Additional search source to execute.      */
DECL|method|extraSource
specifier|public
name|byte
index|[]
name|extraSource
parameter_list|()
block|{
return|return
name|this
operator|.
name|extraSource
return|;
block|}
DECL|method|extraSourceOffset
specifier|public
name|int
name|extraSourceOffset
parameter_list|()
block|{
return|return
name|extraSourceOffset
return|;
block|}
DECL|method|extraSourceLength
specifier|public
name|int
name|extraSourceLength
parameter_list|()
block|{
return|return
name|extraSourceLength
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
comment|/**      * A query hint to optionally later be used when routing the request.      */
DECL|method|queryHint
specifier|public
name|SearchRequest
name|queryHint
parameter_list|(
name|String
name|queryHint
parameter_list|)
block|{
name|this
operator|.
name|queryHint
operator|=
name|queryHint
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A query hint to optionally later be used when routing the request.      */
DECL|method|queryHint
specifier|public
name|String
name|queryHint
parameter_list|()
block|{
return|return
name|queryHint
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
comment|/**      * An optional timeout to control how long search is allowed to take.      */
DECL|method|timeout
specifier|public
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
name|timeout
return|;
block|}
comment|/**      * An optional timeout to control how long search is allowed to take.      */
DECL|method|timeout
specifier|public
name|SearchRequest
name|timeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * An optional timeout to control how long search is allowed to take.      */
DECL|method|timeout
specifier|public
name|SearchRequest
name|timeout
parameter_list|(
name|String
name|timeout
parameter_list|)
block|{
return|return
name|timeout
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|timeout
argument_list|,
literal|null
argument_list|)
argument_list|)
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
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
name|operationThreading
operator|=
name|SearchOperationThreading
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
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
name|readUTF
argument_list|()
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
name|queryHint
operator|=
name|in
operator|.
name|readUTF
argument_list|()
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
name|routing
operator|=
name|in
operator|.
name|readUTF
argument_list|()
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
name|preference
operator|=
name|in
operator|.
name|readUTF
argument_list|()
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
name|scroll
operator|=
name|readScroll
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
name|timeout
operator|=
name|readTimeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|sourceUnsafe
operator|=
literal|false
expr_stmt|;
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|sourceLength
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|sourceLength
operator|==
literal|0
condition|)
block|{
name|source
operator|=
name|Bytes
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|source
operator|=
operator|new
name|byte
index|[
name|sourceLength
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
name|extraSourceUnsafe
operator|=
literal|false
expr_stmt|;
name|extraSourceOffset
operator|=
literal|0
expr_stmt|;
name|extraSourceLength
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|extraSourceLength
operator|==
literal|0
condition|)
block|{
name|extraSource
operator|=
name|Bytes
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|extraSource
operator|=
operator|new
name|byte
index|[
name|extraSourceLength
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|extraSource
argument_list|)
expr_stmt|;
block|}
name|int
name|typesSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|typesSize
operator|>
literal|0
condition|)
block|{
name|types
operator|=
operator|new
name|String
index|[
name|typesSize
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
name|typesSize
condition|;
name|i
operator|++
control|)
block|{
name|types
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
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
name|out
operator|.
name|writeByte
argument_list|(
name|operationThreading
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
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
name|writeUTF
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|queryHint
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
name|out
operator|.
name|writeUTF
argument_list|(
name|queryHint
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|routing
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
name|out
operator|.
name|writeUTF
argument_list|(
name|routing
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|preference
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
name|out
operator|.
name|writeUTF
argument_list|(
name|preference
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|timeout
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
name|timeout
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|sourceLength
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|source
argument_list|,
name|sourceOffset
argument_list|,
name|sourceLength
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|extraSource
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|extraSourceLength
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|extraSource
argument_list|,
name|extraSourceOffset
argument_list|,
name|extraSourceLength
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|types
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|type
range|:
name|types
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

