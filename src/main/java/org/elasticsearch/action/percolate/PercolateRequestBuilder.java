begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.percolate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|percolate
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
name|get
operator|.
name|GetRequest
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
name|Client
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
name|InternalClient
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
name|XContentType
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
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilder
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
name|facet
operator|.
name|FacetBuilder
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
name|highlight
operator|.
name|HighlightBuilder
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
name|SortBuilder
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
DECL|class|PercolateRequestBuilder
specifier|public
class|class
name|PercolateRequestBuilder
extends|extends
name|BroadcastOperationRequestBuilder
argument_list|<
name|PercolateRequest
argument_list|,
name|PercolateResponse
argument_list|,
name|PercolateRequestBuilder
argument_list|>
block|{
DECL|field|sourceBuilder
specifier|private
name|PercolateSourceBuilder
name|sourceBuilder
decl_stmt|;
DECL|method|PercolateRequestBuilder
specifier|public
name|PercolateRequestBuilder
parameter_list|(
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalClient
operator|)
name|client
argument_list|,
operator|new
name|PercolateRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the type of the document to percolate.      */
DECL|method|setDocumentType
specifier|public
name|PercolateRequestBuilder
name|setDocumentType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|request
operator|.
name|documentType
argument_list|(
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A comma separated list of routing values to control the shards the search will be executed on.      */
DECL|method|setRouting
specifier|public
name|PercolateRequestBuilder
name|setRouting
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
name|request
operator|.
name|routing
argument_list|(
name|routing
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * List of routing values to control the shards the search will be executed on.      */
DECL|method|setRouting
specifier|public
name|PercolateRequestBuilder
name|setRouting
parameter_list|(
name|String
modifier|...
name|routings
parameter_list|)
block|{
name|request
operator|.
name|routing
argument_list|(
name|Strings
operator|.
name|arrayToCommaDelimitedString
argument_list|(
name|routings
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to      *<tt>_local</tt> to prefer local shards,<tt>_primary</tt> to execute only on primary shards, or      * a custom value, which guarantees that the same order will be used across different requests.      */
DECL|method|setPreference
specifier|public
name|PercolateRequestBuilder
name|setPreference
parameter_list|(
name|String
name|preference
parameter_list|)
block|{
name|request
operator|.
name|preference
argument_list|(
name|preference
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Enables percolating an existing document. Instead of specifying the source of the document to percolate, define      * a get request that will fetch a document and use its source.      */
DECL|method|setGetRequest
specifier|public
name|PercolateRequestBuilder
name|setGetRequest
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|)
block|{
name|request
operator|.
name|getRequest
argument_list|(
name|getRequest
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Whether only to return total count and don't keep track of the matches (Count percolation).      */
DECL|method|setOnlyCount
specifier|public
name|PercolateRequestBuilder
name|setOnlyCount
parameter_list|(
name|boolean
name|onlyCount
parameter_list|)
block|{
name|request
operator|.
name|onlyCount
argument_list|(
name|onlyCount
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Limits the maximum number of percolate query matches to be returned.      */
DECL|method|setSize
specifier|public
name|PercolateRequestBuilder
name|setSize
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|setSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Similar as {@link #setScore(boolean)}, but whether to sort by the score descending.      */
DECL|method|setSortByScore
specifier|public
name|PercolateRequestBuilder
name|setSortByScore
parameter_list|(
name|boolean
name|sort
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|setSort
argument_list|(
name|sort
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds      */
DECL|method|addSort
specifier|public
name|PercolateRequestBuilder
name|addSort
parameter_list|(
name|SortBuilder
name|sort
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|addSort
argument_list|(
name|sort
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Whether to compute a score for each match and include it in the response. The score is based on      * {@link #setPercolateQuery(QueryBuilder)}}.      */
DECL|method|setScore
specifier|public
name|PercolateRequestBuilder
name|setScore
parameter_list|(
name|boolean
name|score
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|setTrackScores
argument_list|(
name|score
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets a query to reduce the number of percolate queries to be evaluated and score the queries that match based      * on this query.      */
DECL|method|setPercolateDoc
specifier|public
name|PercolateRequestBuilder
name|setPercolateDoc
parameter_list|(
name|PercolateSourceBuilder
operator|.
name|DocBuilder
name|docBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|setDoc
argument_list|(
name|docBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets a query to reduce the number of percolate queries to be evaluated and score the queries that match based      * on this query.      */
DECL|method|setPercolateQuery
specifier|public
name|PercolateRequestBuilder
name|setPercolateQuery
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|setQueryBuilder
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets a filter to reduce the number of percolate queries to be evaluated.      */
DECL|method|setPercolateFilter
specifier|public
name|PercolateRequestBuilder
name|setPercolateFilter
parameter_list|(
name|FilterBuilder
name|filterBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|setFilterBuilder
argument_list|(
name|filterBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Enables highlighting for the percolate document. Per matched percolate query highlight the percolate document.      */
DECL|method|setHighlightBuilder
specifier|public
name|PercolateRequestBuilder
name|setHighlightBuilder
parameter_list|(
name|HighlightBuilder
name|highlightBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|setHighlightBuilder
argument_list|(
name|highlightBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add a facet definition.      */
DECL|method|addFacet
specifier|public
name|PercolateRequestBuilder
name|addFacet
parameter_list|(
name|FacetBuilder
name|facetBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|addFacet
argument_list|(
name|facetBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add a aggregation definition.      */
DECL|method|addAggregation
specifier|public
name|PercolateRequestBuilder
name|addAggregation
parameter_list|(
name|AggregationBuilder
name|aggregationBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|addAggregation
argument_list|(
name|aggregationBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the raw percolate request body.      */
DECL|method|setSource
specifier|public
name|PercolateRequestBuilder
name|setSource
parameter_list|(
name|PercolateSourceBuilder
name|source
parameter_list|)
block|{
name|sourceBuilder
operator|=
name|source
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setSource
specifier|public
name|PercolateRequestBuilder
name|setSource
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
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
DECL|method|setSource
specifier|public
name|PercolateRequestBuilder
name|setSource
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|,
name|XContentType
name|contentType
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|source
argument_list|,
name|contentType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setSource
specifier|public
name|PercolateRequestBuilder
name|setSource
parameter_list|(
name|String
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
DECL|method|setSource
specifier|public
name|PercolateRequestBuilder
name|setSource
parameter_list|(
name|XContentBuilder
name|sourceBuilder
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|sourceBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setSource
specifier|public
name|PercolateRequestBuilder
name|setSource
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|request
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
DECL|method|setSource
specifier|public
name|PercolateRequestBuilder
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
DECL|method|setSource
specifier|public
name|PercolateRequestBuilder
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
DECL|method|setSource
specifier|public
name|PercolateRequestBuilder
name|setSource
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
name|request
operator|.
name|source
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setSource
specifier|public
name|PercolateRequestBuilder
name|setSource
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
name|request
operator|.
name|source
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|unsafe
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|sourceBuilder
specifier|private
name|PercolateSourceBuilder
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
name|PercolateSourceBuilder
argument_list|()
expr_stmt|;
block|}
return|return
name|sourceBuilder
return|;
block|}
annotation|@
name|Override
DECL|method|request
specifier|public
name|PercolateRequest
name|request
parameter_list|()
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
return|return
name|request
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
name|PercolateResponse
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
operator|(
operator|(
name|Client
operator|)
name|client
operator|)
operator|.
name|percolate
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

