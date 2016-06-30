begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
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
name|search
operator|.
name|Collector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|FieldDoc
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Query
import|;
end_import

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
name|Counter
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
name|lease
operator|.
name|Releasable
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
name|lease
operator|.
name|Releasables
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
name|util
operator|.
name|BigArrays
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
name|util
operator|.
name|iterable
operator|.
name|Iterables
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
name|analysis
operator|.
name|AnalysisService
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
name|cache
operator|.
name|bitset
operator|.
name|BitsetFilterCache
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
name|fielddata
operator|.
name|IndexFieldDataService
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
name|mapper
operator|.
name|MappedFieldType
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
name|mapper
operator|.
name|MapperService
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
name|mapper
operator|.
name|object
operator|.
name|ObjectMapper
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
name|ParsedQuery
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
name|QueryShardContext
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
name|shard
operator|.
name|IndexShard
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
name|similarity
operator|.
name|SimilarityService
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
name|SearchShardTarget
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
name|SearchContextAggregations
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
name|dfs
operator|.
name|DfsSearchResult
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
name|FetchPhase
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
name|FetchSearchResult
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
name|FetchSubPhase
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
name|FetchSubPhaseContext
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
name|InnerHitsContext
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
name|script
operator|.
name|ScriptFieldsContext
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
name|source
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
name|highlight
operator|.
name|SearchContextHighlight
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
name|lookup
operator|.
name|SearchLookup
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
name|profile
operator|.
name|Profilers
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
name|query
operator|.
name|QuerySearchResult
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
name|RescoreSearchContext
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
name|SortAndFormats
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
name|SuggestionSearchContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_class
DECL|class|SearchContext
specifier|public
specifier|abstract
class|class
name|SearchContext
implements|implements
name|Releasable
block|{
DECL|field|current
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|SearchContext
argument_list|>
name|current
init|=
operator|new
name|ThreadLocal
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|DEFAULT_TERMINATE_AFTER
specifier|public
specifier|final
specifier|static
name|int
name|DEFAULT_TERMINATE_AFTER
init|=
literal|0
decl_stmt|;
DECL|method|setCurrent
specifier|public
specifier|static
name|void
name|setCurrent
parameter_list|(
name|SearchContext
name|value
parameter_list|)
block|{
name|current
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
DECL|method|removeCurrent
specifier|public
specifier|static
name|void
name|removeCurrent
parameter_list|()
block|{
name|current
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
DECL|method|current
specifier|public
specifier|static
name|SearchContext
name|current
parameter_list|()
block|{
return|return
name|current
operator|.
name|get
argument_list|()
return|;
block|}
DECL|field|clearables
specifier|private
name|Map
argument_list|<
name|Lifetime
argument_list|,
name|List
argument_list|<
name|Releasable
argument_list|>
argument_list|>
name|clearables
init|=
literal|null
decl_stmt|;
DECL|field|closed
specifier|private
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
DECL|field|innerHitsContext
specifier|private
name|InnerHitsContext
name|innerHitsContext
decl_stmt|;
DECL|field|parseFieldMatcher
specifier|protected
specifier|final
name|ParseFieldMatcher
name|parseFieldMatcher
decl_stmt|;
DECL|method|SearchContext
specifier|protected
name|SearchContext
parameter_list|(
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
block|{
name|this
operator|.
name|parseFieldMatcher
operator|=
name|parseFieldMatcher
expr_stmt|;
block|}
DECL|method|parseFieldMatcher
specifier|public
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|()
block|{
return|return
name|parseFieldMatcher
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
specifier|final
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|closed
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
comment|// prevent double release
try|try
block|{
name|clearReleasables
argument_list|(
name|Lifetime
operator|.
name|CONTEXT
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|doClose
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|field|nowInMillisUsed
specifier|private
name|boolean
name|nowInMillisUsed
decl_stmt|;
DECL|method|doClose
specifier|protected
specifier|abstract
name|void
name|doClose
parameter_list|()
function_decl|;
comment|/**      * Should be called before executing the main query and after all other parameters have been set.      */
DECL|method|preProcess
specifier|public
specifier|abstract
name|void
name|preProcess
parameter_list|()
function_decl|;
DECL|method|searchFilter
specifier|public
specifier|abstract
name|Query
name|searchFilter
parameter_list|(
name|String
index|[]
name|types
parameter_list|)
function_decl|;
DECL|method|id
specifier|public
specifier|abstract
name|long
name|id
parameter_list|()
function_decl|;
DECL|method|source
specifier|public
specifier|abstract
name|String
name|source
parameter_list|()
function_decl|;
DECL|method|request
specifier|public
specifier|abstract
name|ShardSearchRequest
name|request
parameter_list|()
function_decl|;
DECL|method|searchType
specifier|public
specifier|abstract
name|SearchType
name|searchType
parameter_list|()
function_decl|;
DECL|method|shardTarget
specifier|public
specifier|abstract
name|SearchShardTarget
name|shardTarget
parameter_list|()
function_decl|;
DECL|method|numberOfShards
specifier|public
specifier|abstract
name|int
name|numberOfShards
parameter_list|()
function_decl|;
DECL|method|queryBoost
specifier|public
specifier|abstract
name|float
name|queryBoost
parameter_list|()
function_decl|;
DECL|method|queryBoost
specifier|public
specifier|abstract
name|SearchContext
name|queryBoost
parameter_list|(
name|float
name|queryBoost
parameter_list|)
function_decl|;
DECL|method|getOriginNanoTime
specifier|public
specifier|abstract
name|long
name|getOriginNanoTime
parameter_list|()
function_decl|;
DECL|method|nowInMillis
specifier|public
specifier|final
name|long
name|nowInMillis
parameter_list|()
block|{
name|nowInMillisUsed
operator|=
literal|true
expr_stmt|;
return|return
name|nowInMillisImpl
argument_list|()
return|;
block|}
DECL|method|nowCallable
specifier|public
specifier|final
name|Callable
argument_list|<
name|Long
argument_list|>
name|nowCallable
parameter_list|()
block|{
return|return
operator|new
name|Callable
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Long
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|nowInMillis
argument_list|()
return|;
block|}
block|}
return|;
block|}
empty_stmt|;
DECL|method|nowInMillisUsed
specifier|public
specifier|final
name|boolean
name|nowInMillisUsed
parameter_list|()
block|{
return|return
name|nowInMillisUsed
return|;
block|}
DECL|method|resetNowInMillisUsed
specifier|public
specifier|final
name|void
name|resetNowInMillisUsed
parameter_list|()
block|{
name|this
operator|.
name|nowInMillisUsed
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|nowInMillisImpl
specifier|protected
specifier|abstract
name|long
name|nowInMillisImpl
parameter_list|()
function_decl|;
DECL|method|scrollContext
specifier|public
specifier|abstract
name|ScrollContext
name|scrollContext
parameter_list|()
function_decl|;
DECL|method|scrollContext
specifier|public
specifier|abstract
name|SearchContext
name|scrollContext
parameter_list|(
name|ScrollContext
name|scroll
parameter_list|)
function_decl|;
DECL|method|aggregations
specifier|public
specifier|abstract
name|SearchContextAggregations
name|aggregations
parameter_list|()
function_decl|;
DECL|method|aggregations
specifier|public
specifier|abstract
name|SearchContext
name|aggregations
parameter_list|(
name|SearchContextAggregations
name|aggregations
parameter_list|)
function_decl|;
DECL|method|getFetchSubPhaseContext
specifier|public
specifier|abstract
parameter_list|<
name|SubPhaseContext
extends|extends
name|FetchSubPhaseContext
parameter_list|>
name|SubPhaseContext
name|getFetchSubPhaseContext
parameter_list|(
name|FetchSubPhase
operator|.
name|ContextFactory
argument_list|<
name|SubPhaseContext
argument_list|>
name|contextFactory
parameter_list|)
function_decl|;
DECL|method|highlight
specifier|public
specifier|abstract
name|SearchContextHighlight
name|highlight
parameter_list|()
function_decl|;
DECL|method|highlight
specifier|public
specifier|abstract
name|void
name|highlight
parameter_list|(
name|SearchContextHighlight
name|highlight
parameter_list|)
function_decl|;
DECL|method|innerHits
specifier|public
name|InnerHitsContext
name|innerHits
parameter_list|()
block|{
if|if
condition|(
name|innerHitsContext
operator|==
literal|null
condition|)
block|{
name|innerHitsContext
operator|=
operator|new
name|InnerHitsContext
argument_list|()
expr_stmt|;
block|}
return|return
name|innerHitsContext
return|;
block|}
DECL|method|suggest
specifier|public
specifier|abstract
name|SuggestionSearchContext
name|suggest
parameter_list|()
function_decl|;
DECL|method|suggest
specifier|public
specifier|abstract
name|void
name|suggest
parameter_list|(
name|SuggestionSearchContext
name|suggest
parameter_list|)
function_decl|;
comment|/**      * @return list of all rescore contexts.  empty if there aren't any.      */
DECL|method|rescore
specifier|public
specifier|abstract
name|List
argument_list|<
name|RescoreSearchContext
argument_list|>
name|rescore
parameter_list|()
function_decl|;
DECL|method|addRescore
specifier|public
specifier|abstract
name|void
name|addRescore
parameter_list|(
name|RescoreSearchContext
name|rescore
parameter_list|)
function_decl|;
DECL|method|hasScriptFields
specifier|public
specifier|abstract
name|boolean
name|hasScriptFields
parameter_list|()
function_decl|;
DECL|method|scriptFields
specifier|public
specifier|abstract
name|ScriptFieldsContext
name|scriptFields
parameter_list|()
function_decl|;
comment|/**      * A shortcut function to see whether there is a fetchSourceContext and it says the source is requested.      */
DECL|method|sourceRequested
specifier|public
specifier|abstract
name|boolean
name|sourceRequested
parameter_list|()
function_decl|;
DECL|method|hasFetchSourceContext
specifier|public
specifier|abstract
name|boolean
name|hasFetchSourceContext
parameter_list|()
function_decl|;
DECL|method|fetchSourceContext
specifier|public
specifier|abstract
name|FetchSourceContext
name|fetchSourceContext
parameter_list|()
function_decl|;
DECL|method|fetchSourceContext
specifier|public
specifier|abstract
name|SearchContext
name|fetchSourceContext
parameter_list|(
name|FetchSourceContext
name|fetchSourceContext
parameter_list|)
function_decl|;
DECL|method|searcher
specifier|public
specifier|abstract
name|ContextIndexSearcher
name|searcher
parameter_list|()
function_decl|;
DECL|method|indexShard
specifier|public
specifier|abstract
name|IndexShard
name|indexShard
parameter_list|()
function_decl|;
DECL|method|mapperService
specifier|public
specifier|abstract
name|MapperService
name|mapperService
parameter_list|()
function_decl|;
DECL|method|analysisService
specifier|public
specifier|abstract
name|AnalysisService
name|analysisService
parameter_list|()
function_decl|;
DECL|method|similarityService
specifier|public
specifier|abstract
name|SimilarityService
name|similarityService
parameter_list|()
function_decl|;
DECL|method|scriptService
specifier|public
specifier|abstract
name|ScriptService
name|scriptService
parameter_list|()
function_decl|;
DECL|method|bigArrays
specifier|public
specifier|abstract
name|BigArrays
name|bigArrays
parameter_list|()
function_decl|;
DECL|method|bitsetFilterCache
specifier|public
specifier|abstract
name|BitsetFilterCache
name|bitsetFilterCache
parameter_list|()
function_decl|;
DECL|method|fieldData
specifier|public
specifier|abstract
name|IndexFieldDataService
name|fieldData
parameter_list|()
function_decl|;
DECL|method|timeout
specifier|public
specifier|abstract
name|TimeValue
name|timeout
parameter_list|()
function_decl|;
DECL|method|timeout
specifier|public
specifier|abstract
name|void
name|timeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
function_decl|;
DECL|method|terminateAfter
specifier|public
specifier|abstract
name|int
name|terminateAfter
parameter_list|()
function_decl|;
DECL|method|terminateAfter
specifier|public
specifier|abstract
name|void
name|terminateAfter
parameter_list|(
name|int
name|terminateAfter
parameter_list|)
function_decl|;
DECL|method|minimumScore
specifier|public
specifier|abstract
name|SearchContext
name|minimumScore
parameter_list|(
name|float
name|minimumScore
parameter_list|)
function_decl|;
DECL|method|minimumScore
specifier|public
specifier|abstract
name|Float
name|minimumScore
parameter_list|()
function_decl|;
DECL|method|sort
specifier|public
specifier|abstract
name|SearchContext
name|sort
parameter_list|(
name|SortAndFormats
name|sort
parameter_list|)
function_decl|;
DECL|method|sort
specifier|public
specifier|abstract
name|SortAndFormats
name|sort
parameter_list|()
function_decl|;
DECL|method|trackScores
specifier|public
specifier|abstract
name|SearchContext
name|trackScores
parameter_list|(
name|boolean
name|trackScores
parameter_list|)
function_decl|;
DECL|method|trackScores
specifier|public
specifier|abstract
name|boolean
name|trackScores
parameter_list|()
function_decl|;
DECL|method|searchAfter
specifier|public
specifier|abstract
name|SearchContext
name|searchAfter
parameter_list|(
name|FieldDoc
name|searchAfter
parameter_list|)
function_decl|;
DECL|method|searchAfter
specifier|public
specifier|abstract
name|FieldDoc
name|searchAfter
parameter_list|()
function_decl|;
DECL|method|parsedPostFilter
specifier|public
specifier|abstract
name|SearchContext
name|parsedPostFilter
parameter_list|(
name|ParsedQuery
name|postFilter
parameter_list|)
function_decl|;
DECL|method|parsedPostFilter
specifier|public
specifier|abstract
name|ParsedQuery
name|parsedPostFilter
parameter_list|()
function_decl|;
DECL|method|aliasFilter
specifier|public
specifier|abstract
name|Query
name|aliasFilter
parameter_list|()
function_decl|;
DECL|method|parsedQuery
specifier|public
specifier|abstract
name|SearchContext
name|parsedQuery
parameter_list|(
name|ParsedQuery
name|query
parameter_list|)
function_decl|;
DECL|method|parsedQuery
specifier|public
specifier|abstract
name|ParsedQuery
name|parsedQuery
parameter_list|()
function_decl|;
comment|/**      * The query to execute, might be rewritten.      */
DECL|method|query
specifier|public
specifier|abstract
name|Query
name|query
parameter_list|()
function_decl|;
DECL|method|from
specifier|public
specifier|abstract
name|int
name|from
parameter_list|()
function_decl|;
DECL|method|from
specifier|public
specifier|abstract
name|SearchContext
name|from
parameter_list|(
name|int
name|from
parameter_list|)
function_decl|;
DECL|method|size
specifier|public
specifier|abstract
name|int
name|size
parameter_list|()
function_decl|;
DECL|method|size
specifier|public
specifier|abstract
name|SearchContext
name|size
parameter_list|(
name|int
name|size
parameter_list|)
function_decl|;
DECL|method|hasFieldNames
specifier|public
specifier|abstract
name|boolean
name|hasFieldNames
parameter_list|()
function_decl|;
DECL|method|fieldNames
specifier|public
specifier|abstract
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|()
function_decl|;
DECL|method|emptyFieldNames
specifier|public
specifier|abstract
name|void
name|emptyFieldNames
parameter_list|()
function_decl|;
DECL|method|explain
specifier|public
specifier|abstract
name|boolean
name|explain
parameter_list|()
function_decl|;
DECL|method|explain
specifier|public
specifier|abstract
name|void
name|explain
parameter_list|(
name|boolean
name|explain
parameter_list|)
function_decl|;
annotation|@
name|Nullable
DECL|method|groupStats
specifier|public
specifier|abstract
name|List
argument_list|<
name|String
argument_list|>
name|groupStats
parameter_list|()
function_decl|;
DECL|method|groupStats
specifier|public
specifier|abstract
name|void
name|groupStats
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|groupStats
parameter_list|)
function_decl|;
DECL|method|version
specifier|public
specifier|abstract
name|boolean
name|version
parameter_list|()
function_decl|;
DECL|method|version
specifier|public
specifier|abstract
name|void
name|version
parameter_list|(
name|boolean
name|version
parameter_list|)
function_decl|;
DECL|method|docIdsToLoad
specifier|public
specifier|abstract
name|int
index|[]
name|docIdsToLoad
parameter_list|()
function_decl|;
DECL|method|docIdsToLoadFrom
specifier|public
specifier|abstract
name|int
name|docIdsToLoadFrom
parameter_list|()
function_decl|;
DECL|method|docIdsToLoadSize
specifier|public
specifier|abstract
name|int
name|docIdsToLoadSize
parameter_list|()
function_decl|;
DECL|method|docIdsToLoad
specifier|public
specifier|abstract
name|SearchContext
name|docIdsToLoad
parameter_list|(
name|int
index|[]
name|docIdsToLoad
parameter_list|,
name|int
name|docsIdsToLoadFrom
parameter_list|,
name|int
name|docsIdsToLoadSize
parameter_list|)
function_decl|;
DECL|method|accessed
specifier|public
specifier|abstract
name|void
name|accessed
parameter_list|(
name|long
name|accessTime
parameter_list|)
function_decl|;
DECL|method|lastAccessTime
specifier|public
specifier|abstract
name|long
name|lastAccessTime
parameter_list|()
function_decl|;
DECL|method|keepAlive
specifier|public
specifier|abstract
name|long
name|keepAlive
parameter_list|()
function_decl|;
DECL|method|keepAlive
specifier|public
specifier|abstract
name|void
name|keepAlive
parameter_list|(
name|long
name|keepAlive
parameter_list|)
function_decl|;
DECL|method|lookup
specifier|public
specifier|abstract
name|SearchLookup
name|lookup
parameter_list|()
function_decl|;
DECL|method|dfsResult
specifier|public
specifier|abstract
name|DfsSearchResult
name|dfsResult
parameter_list|()
function_decl|;
DECL|method|queryResult
specifier|public
specifier|abstract
name|QuerySearchResult
name|queryResult
parameter_list|()
function_decl|;
DECL|method|fetchPhase
specifier|public
specifier|abstract
name|FetchPhase
name|fetchPhase
parameter_list|()
function_decl|;
DECL|method|fetchResult
specifier|public
specifier|abstract
name|FetchSearchResult
name|fetchResult
parameter_list|()
function_decl|;
comment|/**      * Return a handle over the profilers for the current search request, or {@code null} if profiling is not enabled.      */
DECL|method|getProfilers
specifier|public
specifier|abstract
name|Profilers
name|getProfilers
parameter_list|()
function_decl|;
comment|/**      * Schedule the release of a resource. The time when {@link Releasable#close()} will be called on this object      * is function of the provided {@link Lifetime}.      */
DECL|method|addReleasable
specifier|public
name|void
name|addReleasable
parameter_list|(
name|Releasable
name|releasable
parameter_list|,
name|Lifetime
name|lifetime
parameter_list|)
block|{
if|if
condition|(
name|clearables
operator|==
literal|null
condition|)
block|{
name|clearables
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|Releasable
argument_list|>
name|releasables
init|=
name|clearables
operator|.
name|get
argument_list|(
name|lifetime
argument_list|)
decl_stmt|;
if|if
condition|(
name|releasables
operator|==
literal|null
condition|)
block|{
name|releasables
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|clearables
operator|.
name|put
argument_list|(
name|lifetime
argument_list|,
name|releasables
argument_list|)
expr_stmt|;
block|}
name|releasables
operator|.
name|add
argument_list|(
name|releasable
argument_list|)
expr_stmt|;
block|}
DECL|method|clearReleasables
specifier|public
name|void
name|clearReleasables
parameter_list|(
name|Lifetime
name|lifetime
parameter_list|)
block|{
if|if
condition|(
name|clearables
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|List
argument_list|<
name|Releasable
argument_list|>
argument_list|>
name|releasables
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Lifetime
name|lc
range|:
name|Lifetime
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|lc
operator|.
name|compareTo
argument_list|(
name|lifetime
argument_list|)
operator|>
literal|0
condition|)
block|{
break|break;
block|}
name|List
argument_list|<
name|Releasable
argument_list|>
name|remove
init|=
name|clearables
operator|.
name|remove
argument_list|(
name|lc
argument_list|)
decl_stmt|;
if|if
condition|(
name|remove
operator|!=
literal|null
condition|)
block|{
name|releasables
operator|.
name|add
argument_list|(
name|remove
argument_list|)
expr_stmt|;
block|}
block|}
name|Releasables
operator|.
name|close
argument_list|(
name|Iterables
operator|.
name|flatten
argument_list|(
name|releasables
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * @return true if the request contains only suggest      */
DECL|method|hasOnlySuggest
specifier|public
specifier|final
name|boolean
name|hasOnlySuggest
parameter_list|()
block|{
return|return
name|request
argument_list|()
operator|.
name|source
argument_list|()
operator|!=
literal|null
operator|&&
name|request
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|isSuggestOnly
argument_list|()
return|;
block|}
comment|/**      * Looks up the given field, but does not restrict to fields in the types set on this context.      */
DECL|method|smartNameFieldType
specifier|public
specifier|abstract
name|MappedFieldType
name|smartNameFieldType
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
DECL|method|getObjectMapper
specifier|public
specifier|abstract
name|ObjectMapper
name|getObjectMapper
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
DECL|method|timeEstimateCounter
specifier|public
specifier|abstract
name|Counter
name|timeEstimateCounter
parameter_list|()
function_decl|;
comment|/** Return a view of the additional query collectors that should be run for this context. */
DECL|method|queryCollectors
specifier|public
specifier|abstract
name|Map
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Collector
argument_list|>
name|queryCollectors
parameter_list|()
function_decl|;
comment|/**      * The life time of an object that is used during search execution.      */
DECL|enum|Lifetime
specifier|public
enum|enum
name|Lifetime
block|{
comment|/**          * This life time is for objects that only live during collection time.          */
DECL|enum constant|COLLECTION
name|COLLECTION
block|,
comment|/**          * This life time is for objects that need to live until the end of the current search phase.          */
DECL|enum constant|PHASE
name|PHASE
block|,
comment|/**          * This life time is for objects that need to live until the search context they are attached to is destroyed.          */
DECL|enum constant|CONTEXT
name|CONTEXT
block|}
DECL|method|getQueryShardContext
specifier|public
specifier|abstract
name|QueryShardContext
name|getQueryShardContext
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|result
init|=
operator|new
name|StringBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|shardTarget
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|searchType
argument_list|()
operator|!=
name|SearchType
operator|.
name|DEFAULT
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|"searchType=["
argument_list|)
operator|.
name|append
argument_list|(
name|searchType
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scrollContext
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|"scroll=["
argument_list|)
operator|.
name|append
argument_list|(
name|scrollContext
argument_list|()
operator|.
name|scroll
operator|.
name|keepAlive
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|append
argument_list|(
literal|" query=["
argument_list|)
operator|.
name|append
argument_list|(
name|query
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

