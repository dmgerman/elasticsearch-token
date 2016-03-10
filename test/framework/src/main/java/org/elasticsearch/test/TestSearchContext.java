begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
package|;
end_package

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
name|Set
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
name|search
operator|.
name|Sort
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
name|cache
operator|.
name|recycler
operator|.
name|PageCacheRecycler
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
name|index
operator|.
name|IndexService
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
name|engine
operator|.
name|Engine
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
name|internal
operator|.
name|ContextIndexSearcher
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
name|ScrollContext
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
name|internal
operator|.
name|ShardSearchRequest
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
name|suggest
operator|.
name|SuggestionSearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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

begin_class
DECL|class|TestSearchContext
specifier|public
class|class
name|TestSearchContext
extends|extends
name|SearchContext
block|{
DECL|field|pageCacheRecycler
specifier|final
name|PageCacheRecycler
name|pageCacheRecycler
decl_stmt|;
DECL|field|bigArrays
specifier|final
name|BigArrays
name|bigArrays
decl_stmt|;
DECL|field|indexService
specifier|final
name|IndexService
name|indexService
decl_stmt|;
DECL|field|indexFieldDataService
specifier|final
name|IndexFieldDataService
name|indexFieldDataService
decl_stmt|;
DECL|field|fixedBitSetFilterCache
specifier|final
name|BitsetFilterCache
name|fixedBitSetFilterCache
decl_stmt|;
DECL|field|threadPool
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|queryCollectors
specifier|final
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
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|indexShard
specifier|final
name|IndexShard
name|indexShard
decl_stmt|;
DECL|field|timeEstimateCounter
specifier|final
name|Counter
name|timeEstimateCounter
init|=
name|Counter
operator|.
name|newCounter
argument_list|()
decl_stmt|;
DECL|field|queryResult
specifier|final
name|QuerySearchResult
name|queryResult
init|=
operator|new
name|QuerySearchResult
argument_list|()
decl_stmt|;
DECL|field|queryShardContext
specifier|final
name|QueryShardContext
name|queryShardContext
decl_stmt|;
DECL|field|scriptService
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|originalQuery
name|ParsedQuery
name|originalQuery
decl_stmt|;
DECL|field|postFilter
name|ParsedQuery
name|postFilter
decl_stmt|;
DECL|field|query
name|Query
name|query
decl_stmt|;
DECL|field|minScore
name|Float
name|minScore
decl_stmt|;
DECL|field|searcher
name|ContextIndexSearcher
name|searcher
decl_stmt|;
DECL|field|size
name|int
name|size
decl_stmt|;
DECL|field|terminateAfter
specifier|private
name|int
name|terminateAfter
init|=
name|DEFAULT_TERMINATE_AFTER
decl_stmt|;
DECL|field|aggregations
specifier|private
name|SearchContextAggregations
name|aggregations
decl_stmt|;
DECL|field|originNanoTime
specifier|private
specifier|final
name|long
name|originNanoTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
DECL|field|subPhaseContexts
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|FetchSubPhaseContext
argument_list|>
name|subPhaseContexts
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|TestSearchContext
specifier|public
name|TestSearchContext
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|,
name|PageCacheRecycler
name|pageCacheRecycler
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|IndexService
name|indexService
parameter_list|)
block|{
name|super
argument_list|(
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
expr_stmt|;
name|this
operator|.
name|pageCacheRecycler
operator|=
name|pageCacheRecycler
expr_stmt|;
name|this
operator|.
name|bigArrays
operator|=
name|bigArrays
operator|.
name|withCircuitBreaking
argument_list|()
expr_stmt|;
name|this
operator|.
name|indexService
operator|=
name|indexService
expr_stmt|;
name|this
operator|.
name|indexFieldDataService
operator|=
name|indexService
operator|.
name|fieldData
argument_list|()
expr_stmt|;
name|this
operator|.
name|fixedBitSetFilterCache
operator|=
name|indexService
operator|.
name|cache
argument_list|()
operator|.
name|bitsetFilterCache
argument_list|()
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|indexShard
operator|=
name|indexService
operator|.
name|getShardOrNull
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|queryShardContext
operator|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|()
expr_stmt|;
block|}
DECL|method|TestSearchContext
specifier|public
name|TestSearchContext
parameter_list|(
name|QueryShardContext
name|queryShardContext
parameter_list|)
block|{
name|super
argument_list|(
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
expr_stmt|;
name|this
operator|.
name|pageCacheRecycler
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|bigArrays
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|indexService
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|indexFieldDataService
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|fixedBitSetFilterCache
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|indexShard
operator|=
literal|null
expr_stmt|;
name|scriptService
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|queryShardContext
operator|=
name|queryShardContext
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|preProcess
specifier|public
name|void
name|preProcess
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|searchFilter
specifier|public
name|Query
name|searchFilter
parameter_list|(
name|String
index|[]
name|types
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|id
specifier|public
name|long
name|id
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|source
specifier|public
name|String
name|source
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|request
specifier|public
name|ShardSearchRequest
name|request
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|searchType
specifier|public
name|SearchType
name|searchType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|searchType
specifier|public
name|SearchContext
name|searchType
parameter_list|(
name|SearchType
name|searchType
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|shardTarget
specifier|public
name|SearchShardTarget
name|shardTarget
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|numberOfShards
specifier|public
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
annotation|@
name|Override
DECL|method|queryBoost
specifier|public
name|float
name|queryBoost
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|queryBoost
specifier|public
name|SearchContext
name|queryBoost
parameter_list|(
name|float
name|queryBoost
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getOriginNanoTime
specifier|public
name|long
name|getOriginNanoTime
parameter_list|()
block|{
return|return
name|originNanoTime
return|;
block|}
annotation|@
name|Override
DECL|method|nowInMillisImpl
specifier|protected
name|long
name|nowInMillisImpl
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|scrollContext
specifier|public
name|ScrollContext
name|scrollContext
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|scrollContext
specifier|public
name|SearchContext
name|scrollContext
parameter_list|(
name|ScrollContext
name|scrollContext
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|aggregations
specifier|public
name|SearchContextAggregations
name|aggregations
parameter_list|()
block|{
return|return
name|aggregations
return|;
block|}
annotation|@
name|Override
DECL|method|aggregations
specifier|public
name|SearchContext
name|aggregations
parameter_list|(
name|SearchContextAggregations
name|aggregations
parameter_list|)
block|{
name|this
operator|.
name|aggregations
operator|=
name|aggregations
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|getFetchSubPhaseContext
specifier|public
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
block|{
name|String
name|subPhaseName
init|=
name|contextFactory
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|subPhaseContexts
operator|.
name|get
argument_list|(
name|subPhaseName
argument_list|)
operator|==
literal|null
condition|)
block|{
name|subPhaseContexts
operator|.
name|put
argument_list|(
name|subPhaseName
argument_list|,
name|contextFactory
operator|.
name|newContextInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|SubPhaseContext
operator|)
name|subPhaseContexts
operator|.
name|get
argument_list|(
name|subPhaseName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|highlight
specifier|public
name|SearchContextHighlight
name|highlight
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|highlight
specifier|public
name|void
name|highlight
parameter_list|(
name|SearchContextHighlight
name|highlight
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|suggest
specifier|public
name|SuggestionSearchContext
name|suggest
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|suggest
specifier|public
name|void
name|suggest
parameter_list|(
name|SuggestionSearchContext
name|suggest
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|rescore
specifier|public
name|List
argument_list|<
name|RescoreSearchContext
argument_list|>
name|rescore
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|addRescore
specifier|public
name|void
name|addRescore
parameter_list|(
name|RescoreSearchContext
name|rescore
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|hasScriptFields
specifier|public
name|boolean
name|hasScriptFields
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|scriptFields
specifier|public
name|ScriptFieldsContext
name|scriptFields
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|sourceRequested
specifier|public
name|boolean
name|sourceRequested
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|hasFetchSourceContext
specifier|public
name|boolean
name|hasFetchSourceContext
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|fetchSourceContext
specifier|public
name|FetchSourceContext
name|fetchSourceContext
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|fetchSourceContext
specifier|public
name|SearchContext
name|fetchSourceContext
parameter_list|(
name|FetchSourceContext
name|fetchSourceContext
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|searcher
specifier|public
name|ContextIndexSearcher
name|searcher
parameter_list|()
block|{
return|return
name|searcher
return|;
block|}
DECL|method|setSearcher
specifier|public
name|void
name|setSearcher
parameter_list|(
name|Engine
operator|.
name|Searcher
name|searcher
parameter_list|)
block|{
name|this
operator|.
name|searcher
operator|=
operator|new
name|ContextIndexSearcher
argument_list|(
name|searcher
argument_list|,
name|indexService
operator|.
name|cache
argument_list|()
operator|.
name|query
argument_list|()
argument_list|,
name|indexShard
operator|.
name|getQueryCachingPolicy
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|indexShard
specifier|public
name|IndexShard
name|indexShard
parameter_list|()
block|{
return|return
name|indexShard
return|;
block|}
annotation|@
name|Override
DECL|method|mapperService
specifier|public
name|MapperService
name|mapperService
parameter_list|()
block|{
if|if
condition|(
name|indexService
operator|!=
literal|null
condition|)
block|{
return|return
name|indexService
operator|.
name|mapperService
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|analysisService
specifier|public
name|AnalysisService
name|analysisService
parameter_list|()
block|{
return|return
name|indexService
operator|.
name|analysisService
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|similarityService
specifier|public
name|SimilarityService
name|similarityService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|scriptService
specifier|public
name|ScriptService
name|scriptService
parameter_list|()
block|{
return|return
name|scriptService
return|;
block|}
annotation|@
name|Override
DECL|method|pageCacheRecycler
specifier|public
name|PageCacheRecycler
name|pageCacheRecycler
parameter_list|()
block|{
return|return
name|pageCacheRecycler
return|;
block|}
annotation|@
name|Override
DECL|method|bigArrays
specifier|public
name|BigArrays
name|bigArrays
parameter_list|()
block|{
return|return
name|bigArrays
return|;
block|}
annotation|@
name|Override
DECL|method|bitsetFilterCache
specifier|public
name|BitsetFilterCache
name|bitsetFilterCache
parameter_list|()
block|{
return|return
name|fixedBitSetFilterCache
return|;
block|}
annotation|@
name|Override
DECL|method|fieldData
specifier|public
name|IndexFieldDataService
name|fieldData
parameter_list|()
block|{
return|return
name|indexFieldDataService
return|;
block|}
annotation|@
name|Override
DECL|method|timeoutInMillis
specifier|public
name|long
name|timeoutInMillis
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|timeoutInMillis
specifier|public
name|void
name|timeoutInMillis
parameter_list|(
name|long
name|timeoutInMillis
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|terminateAfter
specifier|public
name|int
name|terminateAfter
parameter_list|()
block|{
return|return
name|terminateAfter
return|;
block|}
annotation|@
name|Override
DECL|method|terminateAfter
specifier|public
name|void
name|terminateAfter
parameter_list|(
name|int
name|terminateAfter
parameter_list|)
block|{
name|this
operator|.
name|terminateAfter
operator|=
name|terminateAfter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|minimumScore
specifier|public
name|SearchContext
name|minimumScore
parameter_list|(
name|float
name|minimumScore
parameter_list|)
block|{
name|this
operator|.
name|minScore
operator|=
name|minimumScore
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|minimumScore
specifier|public
name|Float
name|minimumScore
parameter_list|()
block|{
return|return
name|minScore
return|;
block|}
annotation|@
name|Override
DECL|method|sort
specifier|public
name|SearchContext
name|sort
parameter_list|(
name|Sort
name|sort
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|sort
specifier|public
name|Sort
name|sort
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|trackScores
specifier|public
name|SearchContext
name|trackScores
parameter_list|(
name|boolean
name|trackScores
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|trackScores
specifier|public
name|boolean
name|trackScores
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|searchAfter
specifier|public
name|SearchContext
name|searchAfter
parameter_list|(
name|FieldDoc
name|searchAfter
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|searchAfter
specifier|public
name|FieldDoc
name|searchAfter
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|parsedPostFilter
specifier|public
name|SearchContext
name|parsedPostFilter
parameter_list|(
name|ParsedQuery
name|postFilter
parameter_list|)
block|{
name|this
operator|.
name|postFilter
operator|=
name|postFilter
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|parsedPostFilter
specifier|public
name|ParsedQuery
name|parsedPostFilter
parameter_list|()
block|{
return|return
name|postFilter
return|;
block|}
annotation|@
name|Override
DECL|method|aliasFilter
specifier|public
name|Query
name|aliasFilter
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|parsedQuery
specifier|public
name|SearchContext
name|parsedQuery
parameter_list|(
name|ParsedQuery
name|query
parameter_list|)
block|{
name|this
operator|.
name|originalQuery
operator|=
name|query
expr_stmt|;
name|this
operator|.
name|query
operator|=
name|query
operator|.
name|query
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|parsedQuery
specifier|public
name|ParsedQuery
name|parsedQuery
parameter_list|()
block|{
return|return
name|originalQuery
return|;
block|}
annotation|@
name|Override
DECL|method|query
specifier|public
name|Query
name|query
parameter_list|()
block|{
return|return
name|query
return|;
block|}
annotation|@
name|Override
DECL|method|from
specifier|public
name|int
name|from
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|from
specifier|public
name|SearchContext
name|from
parameter_list|(
name|int
name|from
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
DECL|method|setSize
specifier|public
name|void
name|setSize
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|SearchContext
name|size
parameter_list|(
name|int
name|size
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|hasFieldNames
specifier|public
name|boolean
name|hasFieldNames
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|fieldNames
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|emptyFieldNames
specifier|public
name|void
name|emptyFieldNames
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|explain
specifier|public
name|boolean
name|explain
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|explain
specifier|public
name|void
name|explain
parameter_list|(
name|boolean
name|explain
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|groupStats
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|groupStats
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|groupStats
specifier|public
name|void
name|groupStats
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|groupStats
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|version
specifier|public
name|boolean
name|version
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|version
specifier|public
name|void
name|version
parameter_list|(
name|boolean
name|version
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|docIdsToLoad
specifier|public
name|int
index|[]
name|docIdsToLoad
parameter_list|()
block|{
return|return
operator|new
name|int
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
DECL|method|docIdsToLoadFrom
specifier|public
name|int
name|docIdsToLoadFrom
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|docIdsToLoadSize
specifier|public
name|int
name|docIdsToLoadSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|docIdsToLoad
specifier|public
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
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|accessed
specifier|public
name|void
name|accessed
parameter_list|(
name|long
name|accessTime
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|lastAccessTime
specifier|public
name|long
name|lastAccessTime
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|keepAlive
specifier|public
name|long
name|keepAlive
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|keepAlive
specifier|public
name|void
name|keepAlive
parameter_list|(
name|long
name|keepAlive
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|lookup
specifier|public
name|SearchLookup
name|lookup
parameter_list|()
block|{
return|return
operator|new
name|SearchLookup
argument_list|(
name|mapperService
argument_list|()
argument_list|,
name|fieldData
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|dfsResult
specifier|public
name|DfsSearchResult
name|dfsResult
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|queryResult
specifier|public
name|QuerySearchResult
name|queryResult
parameter_list|()
block|{
return|return
name|queryResult
return|;
block|}
annotation|@
name|Override
DECL|method|fetchResult
specifier|public
name|FetchSearchResult
name|fetchResult
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|fetchPhase
specifier|public
name|FetchPhase
name|fetchPhase
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|smartNameFieldType
specifier|public
name|MappedFieldType
name|smartNameFieldType
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|mapperService
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|mapperService
argument_list|()
operator|.
name|fullName
argument_list|(
name|name
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getObjectMapper
specifier|public
name|ObjectMapper
name|getObjectMapper
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|mapperService
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|mapperService
argument_list|()
operator|.
name|getObjectMapper
argument_list|(
name|name
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|public
name|void
name|doClose
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|timeEstimateCounter
specifier|public
name|Counter
name|timeEstimateCounter
parameter_list|()
block|{
return|return
name|timeEstimateCounter
return|;
block|}
annotation|@
name|Override
DECL|method|getProfilers
specifier|public
name|Profilers
name|getProfilers
parameter_list|()
block|{
return|return
literal|null
return|;
comment|// no profiling
block|}
annotation|@
name|Override
DECL|method|queryCollectors
specifier|public
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
block|{
return|return
name|queryCollectors
return|;
block|}
annotation|@
name|Override
DECL|method|getQueryShardContext
specifier|public
name|QueryShardContext
name|getQueryShardContext
parameter_list|()
block|{
return|return
name|queryShardContext
return|;
block|}
block|}
end_class

end_unit
