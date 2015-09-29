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
name|ExceptionsHelper
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
name|ActionRequestBuilder
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
name|ElasticsearchClient
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
name|XContentHelper
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
name|script
operator|.
name|Script
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
name|Template
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
name|aggregations
operator|.
name|AbstractAggregationBuilder
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
name|innerhits
operator|.
name|InnerHitsBuilder
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
name|rescore
operator|.
name|RescoreBuilder
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
name|List
import|;
end_import

begin_comment
comment|/**  * A search action request builder.  */
end_comment

begin_class
DECL|class|SearchRequestBuilder
specifier|public
class|class
name|SearchRequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|SearchRequest
argument_list|,
name|SearchResponse
argument_list|,
name|SearchRequestBuilder
argument_list|>
block|{
DECL|field|sourceBuilder
specifier|private
name|SearchSourceBuilder
name|sourceBuilder
decl_stmt|;
DECL|method|SearchRequestBuilder
specifier|public
name|SearchRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|SearchAction
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
name|SearchRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the indices the search will be executed on.      */
DECL|method|setIndices
specifier|public
name|SearchRequestBuilder
name|setIndices
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|request
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The document types to execute the search against. Defaults to be executed against      * all types.      */
DECL|method|setTypes
specifier|public
name|SearchRequestBuilder
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
comment|/**      * The search type to execute, defaults to {@link org.elasticsearch.action.search.SearchType#DEFAULT}.      */
DECL|method|setSearchType
specifier|public
name|SearchRequestBuilder
name|setSearchType
parameter_list|(
name|SearchType
name|searchType
parameter_list|)
block|{
name|request
operator|.
name|searchType
argument_list|(
name|searchType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The a string representation search type to execute, defaults to {@link SearchType#DEFAULT}. Can be      * one of "dfs_query_then_fetch"/"dfsQueryThenFetch", "dfs_query_and_fetch"/"dfsQueryAndFetch",      * "query_then_fetch"/"queryThenFetch", and "query_and_fetch"/"queryAndFetch".      */
DECL|method|setSearchType
specifier|public
name|SearchRequestBuilder
name|setSearchType
parameter_list|(
name|String
name|searchType
parameter_list|)
block|{
name|request
operator|.
name|searchType
argument_list|(
name|searchType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request.      */
DECL|method|setScroll
specifier|public
name|SearchRequestBuilder
name|setScroll
parameter_list|(
name|Scroll
name|scroll
parameter_list|)
block|{
name|request
operator|.
name|scroll
argument_list|(
name|scroll
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request for the specified timeout.      */
DECL|method|setScroll
specifier|public
name|SearchRequestBuilder
name|setScroll
parameter_list|(
name|TimeValue
name|keepAlive
parameter_list|)
block|{
name|request
operator|.
name|scroll
argument_list|(
name|keepAlive
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request for the specified timeout.      */
DECL|method|setScroll
specifier|public
name|SearchRequestBuilder
name|setScroll
parameter_list|(
name|String
name|keepAlive
parameter_list|)
block|{
name|request
operator|.
name|scroll
argument_list|(
name|keepAlive
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * An optional timeout to control how long search is allowed to take.      */
DECL|method|setTimeout
specifier|public
name|SearchRequestBuilder
name|setTimeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|timeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * An optional document count, upon collecting which the search      * query will early terminate      */
DECL|method|setTerminateAfter
specifier|public
name|SearchRequestBuilder
name|setTerminateAfter
parameter_list|(
name|int
name|terminateAfter
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|terminateAfter
argument_list|(
name|terminateAfter
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A comma separated list of routing values to control the shards the search will be executed on.      */
DECL|method|setRouting
specifier|public
name|SearchRequestBuilder
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
comment|/**      * The routing values to control the shards that the search will be executed on.      */
DECL|method|setRouting
specifier|public
name|SearchRequestBuilder
name|setRouting
parameter_list|(
name|String
modifier|...
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
comment|/**      * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to      *<tt>_local</tt> to prefer local shards,<tt>_primary</tt> to execute only on primary shards, or      * a custom value, which guarantees that the same order will be used across different requests.      */
DECL|method|setPreference
specifier|public
name|SearchRequestBuilder
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
comment|/**      * Specifies what type of requested indices to ignore and wildcard indices expressions.      *<p>      * For example indices that don't exist.      */
DECL|method|setIndicesOptions
specifier|public
name|SearchRequestBuilder
name|setIndicesOptions
parameter_list|(
name|IndicesOptions
name|indicesOptions
parameter_list|)
block|{
name|request
argument_list|()
operator|.
name|indicesOptions
argument_list|(
name|indicesOptions
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Constructs a new search source builder with a search query.      *      * @see org.elasticsearch.index.query.QueryBuilders      */
DECL|method|setQuery
specifier|public
name|SearchRequestBuilder
name|setQuery
parameter_list|(
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|queryBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
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
comment|/**      * Sets a filter that will be executed after the query has been executed and only has affect on the search hits      * (not aggregations). This filter is always executed as last filtering mechanism.      */
DECL|method|setPostFilter
specifier|public
name|SearchRequestBuilder
name|setPostFilter
parameter_list|(
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|postFilter
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|postFilter
argument_list|(
name|postFilter
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the minimum score below which docs will be filtered out.      */
DECL|method|setMinScore
specifier|public
name|SearchRequestBuilder
name|setMinScore
parameter_list|(
name|float
name|minScore
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|minScore
argument_list|(
name|minScore
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * From index to start the search from. Defaults to<tt>0</tt>.      */
DECL|method|setFrom
specifier|public
name|SearchRequestBuilder
name|setFrom
parameter_list|(
name|int
name|from
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|from
argument_list|(
name|from
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The number of search hits to return. Defaults to<tt>10</tt>.      */
DECL|method|setSize
specifier|public
name|SearchRequestBuilder
name|setSize
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|size
argument_list|(
name|size
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should each {@link org.elasticsearch.search.SearchHit} be returned with an      * explanation of the hit (ranking).      */
DECL|method|setExplain
specifier|public
name|SearchRequestBuilder
name|setExplain
parameter_list|(
name|boolean
name|explain
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
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
comment|/**      * Should each {@link org.elasticsearch.search.SearchHit} be returned with its      * version.      */
DECL|method|setVersion
specifier|public
name|SearchRequestBuilder
name|setVersion
parameter_list|(
name|boolean
name|version
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|version
argument_list|(
name|version
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the boost a specific index will receive when the query is executeed against it.      *      * @param index      The index to apply the boost against      * @param indexBoost The boost to apply to the index      */
DECL|method|addIndexBoost
specifier|public
name|SearchRequestBuilder
name|addIndexBoost
parameter_list|(
name|String
name|index
parameter_list|,
name|float
name|indexBoost
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|indexBoost
argument_list|(
name|index
argument_list|,
name|indexBoost
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The stats groups this request will be aggregated under.      */
DECL|method|setStats
specifier|public
name|SearchRequestBuilder
name|setStats
parameter_list|(
name|String
modifier|...
name|statsGroups
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|stats
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|statsGroups
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The stats groups this request will be aggregated under.      */
DECL|method|setStats
specifier|public
name|SearchRequestBuilder
name|setStats
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|statsGroups
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|stats
argument_list|(
name|statsGroups
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets no fields to be loaded, resulting in only id and type to be returned per field.      */
DECL|method|setNoFields
specifier|public
name|SearchRequestBuilder
name|setNoFields
parameter_list|()
block|{
name|sourceBuilder
argument_list|()
operator|.
name|noFields
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Indicates whether the response should contain the stored _source for every hit      */
DECL|method|setFetchSource
specifier|public
name|SearchRequestBuilder
name|setFetchSource
parameter_list|(
name|boolean
name|fetch
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|fetchSource
argument_list|(
name|fetch
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Indicate that _source should be returned with every hit, with an "include" and/or "exclude" set which can include simple wildcard      * elements.      *      * @param include An optional include (optionally wildcarded) pattern to filter the returned _source      * @param exclude An optional exclude (optionally wildcarded) pattern to filter the returned _source      */
DECL|method|setFetchSource
specifier|public
name|SearchRequestBuilder
name|setFetchSource
parameter_list|(
annotation|@
name|Nullable
name|String
name|include
parameter_list|,
annotation|@
name|Nullable
name|String
name|exclude
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|fetchSource
argument_list|(
name|include
argument_list|,
name|exclude
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Indicate that _source should be returned with every hit, with an "include" and/or "exclude" set which can include simple wildcard      * elements.      *      * @param includes An optional list of include (optionally wildcarded) pattern to filter the returned _source      * @param excludes An optional list of exclude (optionally wildcarded) pattern to filter the returned _source      */
DECL|method|setFetchSource
specifier|public
name|SearchRequestBuilder
name|setFetchSource
parameter_list|(
annotation|@
name|Nullable
name|String
index|[]
name|includes
parameter_list|,
annotation|@
name|Nullable
name|String
index|[]
name|excludes
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|fetchSource
argument_list|(
name|includes
argument_list|,
name|excludes
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field to load and return (note, it must be stored) as part of the search request.      * If none are specified, the source of the document will be return.      */
DECL|method|addField
specifier|public
name|SearchRequestBuilder
name|addField
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|field
argument_list|(
name|field
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field data based field to load and return. The field does not have to be stored,      * but its recommended to use non analyzed or numeric fields.      *      * @param name The field to get from the field data cache      */
DECL|method|addFieldDataField
specifier|public
name|SearchRequestBuilder
name|addFieldDataField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|fieldDataField
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a script based field to load and return. The field does not have to be stored,      * but its recommended to use non analyzed or numeric fields.      *      * @param name   The name that will represent this value in the return hit      * @param script The script to use      */
DECL|method|addScriptField
specifier|public
name|SearchRequestBuilder
name|addScriptField
parameter_list|(
name|String
name|name
parameter_list|,
name|Script
name|script
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|scriptField
argument_list|(
name|name
argument_list|,
name|script
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a sort against the given field name and the sort ordering.      *      * @param field The name of the field      * @param order The sort ordering      */
DECL|method|addSort
specifier|public
name|SearchRequestBuilder
name|addSort
parameter_list|(
name|String
name|field
parameter_list|,
name|SortOrder
name|order
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|sort
argument_list|(
name|field
argument_list|,
name|order
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a generic sort builder.      *      * @see org.elasticsearch.search.sort.SortBuilders      */
DECL|method|addSort
specifier|public
name|SearchRequestBuilder
name|addSort
parameter_list|(
name|SortBuilder
name|sort
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|sort
argument_list|(
name|sort
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Applies when sorting, and controls if scores will be tracked as well. Defaults to      *<tt>false</tt>.      */
DECL|method|setTrackScores
specifier|public
name|SearchRequestBuilder
name|setTrackScores
parameter_list|(
name|boolean
name|trackScores
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|trackScores
argument_list|(
name|trackScores
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds the fields to load and return as part of the search request. If none are specified,      * the source of the document will be returned.      */
DECL|method|addFields
specifier|public
name|SearchRequestBuilder
name|addFields
parameter_list|(
name|String
modifier|...
name|fields
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|fields
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|fields
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds an get to the search operation.      */
DECL|method|addAggregation
specifier|public
name|SearchRequestBuilder
name|addAggregation
parameter_list|(
name|AbstractAggregationBuilder
name|aggregation
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|aggregation
argument_list|(
name|aggregation
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|highlighter
specifier|public
name|SearchRequestBuilder
name|highlighter
parameter_list|(
name|HighlightBuilder
name|highlightBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|highlighter
argument_list|(
name|highlightBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Delegates to      * {@link org.elasticsearch.search.suggest.SuggestBuilder#addSuggestion(org.elasticsearch.search.suggest.SuggestBuilder.SuggestionBuilder)}      * .      */
DECL|method|suggest
specifier|public
name|SearchRequestBuilder
name|suggest
parameter_list|(
name|SuggestBuilder
name|suggestBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|suggest
argument_list|(
name|suggestBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|innerHits
specifier|public
name|SearchRequestBuilder
name|innerHits
parameter_list|(
name|InnerHitsBuilder
name|innerHitsBuilder
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|innerHits
argument_list|(
name|innerHitsBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Clears all rescorers on the builder and sets the first one.  To use multiple rescore windows use      * {@link #addRescorer(org.elasticsearch.search.rescore.RescoreBuilder.Rescorer, int)}.      *      * @param rescorer rescorer configuration      * @return this for chaining      */
DECL|method|setRescorer
specifier|public
name|SearchRequestBuilder
name|setRescorer
parameter_list|(
name|RescoreBuilder
operator|.
name|Rescorer
name|rescorer
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|clearRescorers
argument_list|()
expr_stmt|;
return|return
name|addRescorer
argument_list|(
name|rescorer
argument_list|)
return|;
block|}
comment|/**      * Clears all rescorers on the builder and sets the first one.  To use multiple rescore windows use      * {@link #addRescorer(org.elasticsearch.search.rescore.RescoreBuilder.Rescorer, int)}.      *      * @param rescorer rescorer configuration      * @param window   rescore window      * @return this for chaining      */
DECL|method|setRescorer
specifier|public
name|SearchRequestBuilder
name|setRescorer
parameter_list|(
name|RescoreBuilder
operator|.
name|Rescorer
name|rescorer
parameter_list|,
name|int
name|window
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|clearRescorers
argument_list|()
expr_stmt|;
return|return
name|addRescorer
argument_list|(
name|rescorer
argument_list|,
name|window
argument_list|)
return|;
block|}
comment|/**      * Adds a new rescorer.      *      * @param rescorer rescorer configuration      * @return this for chaining      */
DECL|method|addRescorer
specifier|public
name|SearchRequestBuilder
name|addRescorer
parameter_list|(
name|RescoreBuilder
operator|.
name|Rescorer
name|rescorer
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|addRescorer
argument_list|(
operator|new
name|RescoreBuilder
argument_list|()
operator|.
name|rescorer
argument_list|(
name|rescorer
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a new rescorer.      *      * @param rescorer rescorer configuration      * @param window   rescore window      * @return this for chaining      */
DECL|method|addRescorer
specifier|public
name|SearchRequestBuilder
name|addRescorer
parameter_list|(
name|RescoreBuilder
operator|.
name|Rescorer
name|rescorer
parameter_list|,
name|int
name|window
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|addRescorer
argument_list|(
operator|new
name|RescoreBuilder
argument_list|()
operator|.
name|rescorer
argument_list|(
name|rescorer
argument_list|)
operator|.
name|windowSize
argument_list|(
name|window
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Clears all rescorers from the builder.      *      * @return this for chaining      */
DECL|method|clearRescorers
specifier|public
name|SearchRequestBuilder
name|clearRescorers
parameter_list|()
block|{
name|sourceBuilder
argument_list|()
operator|.
name|clearRescorers
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the rescore window for all rescorers that don't specify a window when added.      *      * @param window rescore window      * @return this for chaining      */
DECL|method|setRescoreWindow
specifier|public
name|SearchRequestBuilder
name|setRescoreWindow
parameter_list|(
name|int
name|window
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|defaultRescoreWindowSize
argument_list|(
name|window
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the source of the request as a SearchSourceBuilder.      */
DECL|method|setSource
specifier|public
name|SearchRequestBuilder
name|setSource
parameter_list|(
name|SearchSourceBuilder
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
comment|/**      * template stuff      */
DECL|method|setTemplate
specifier|public
name|SearchRequestBuilder
name|setTemplate
parameter_list|(
name|Template
name|template
parameter_list|)
block|{
name|request
operator|.
name|template
argument_list|(
name|template
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets if this request should use the request cache or not, assuming that it can (for      * example, if "now" is used, it will never be cached). By default (not set, or null,      * will default to the index level setting if request cache is enabled or not).      */
DECL|method|setRequestCache
specifier|public
name|SearchRequestBuilder
name|setRequestCache
parameter_list|(
name|Boolean
name|requestCache
parameter_list|)
block|{
name|request
operator|.
name|requestCache
argument_list|(
name|requestCache
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the source builder to be used with this request. Note, any operations done      * on this require builder before are discarded as this internal builder replaces      * what has been built up until this point.      */
DECL|method|internalBuilder
specifier|public
name|SearchRequestBuilder
name|internalBuilder
parameter_list|(
name|SearchSourceBuilder
name|sourceBuilder
parameter_list|)
block|{
name|this
operator|.
name|sourceBuilder
operator|=
name|sourceBuilder
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the internal search source builder used to construct the request.      */
DECL|method|internalBuilder
specifier|public
name|SearchSourceBuilder
name|internalBuilder
parameter_list|()
block|{
return|return
name|sourceBuilder
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|sourceBuilder
operator|!=
literal|null
condition|)
block|{
return|return
name|sourceBuilder
operator|.
name|toString
argument_list|()
return|;
block|}
if|if
condition|(
name|request
operator|.
name|source
argument_list|()
operator|!=
literal|null
condition|)
block|{
try|try
block|{
return|return
name|XContentHelper
operator|.
name|toString
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|"{ \"error\" : \""
operator|+
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"\"}"
return|;
block|}
block|}
return|return
operator|new
name|SearchSourceBuilder
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|request
specifier|public
name|SearchRequest
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
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|request
return|;
block|}
annotation|@
name|Override
DECL|method|beforeExecute
specifier|protected
name|SearchRequest
name|beforeExecute
parameter_list|(
name|SearchRequest
name|request
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
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|request
return|;
block|}
DECL|method|sourceBuilder
specifier|private
name|SearchSourceBuilder
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
name|SearchSourceBuilder
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

