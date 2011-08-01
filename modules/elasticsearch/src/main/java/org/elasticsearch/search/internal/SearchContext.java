begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Filter
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
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|collect
operator|.
name|ImmutableList
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
name|collect
operator|.
name|Lists
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
name|field
operator|.
name|data
operator|.
name|FieldDataCache
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
name|filter
operator|.
name|FilterCache
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
name|id
operator|.
name|IdCache
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
name|query
operator|.
name|IndexQueryParserService
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
name|search
operator|.
name|nested
operator|.
name|BlockJoinQuery
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
name|service
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
name|facet
operator|.
name|SearchContextFacets
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
name|query
operator|.
name|QuerySearchResult
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|SearchContext
specifier|public
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
argument_list|<
name|SearchContext
argument_list|>
argument_list|()
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
DECL|field|id
specifier|private
specifier|final
name|long
name|id
decl_stmt|;
DECL|field|shardTarget
specifier|private
specifier|final
name|SearchShardTarget
name|shardTarget
decl_stmt|;
DECL|field|searchType
specifier|private
name|SearchType
name|searchType
decl_stmt|;
DECL|field|numberOfShards
specifier|private
specifier|final
name|int
name|numberOfShards
decl_stmt|;
DECL|field|engineSearcher
specifier|private
specifier|final
name|Engine
operator|.
name|Searcher
name|engineSearcher
decl_stmt|;
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|indexService
specifier|private
specifier|final
name|IndexService
name|indexService
decl_stmt|;
DECL|field|searcher
specifier|private
specifier|final
name|ContextIndexSearcher
name|searcher
decl_stmt|;
DECL|field|dfsResult
specifier|private
specifier|final
name|DfsSearchResult
name|dfsResult
decl_stmt|;
DECL|field|queryResult
specifier|private
specifier|final
name|QuerySearchResult
name|queryResult
decl_stmt|;
DECL|field|fetchResult
specifier|private
specifier|final
name|FetchSearchResult
name|fetchResult
decl_stmt|;
DECL|field|timeout
specifier|private
specifier|final
name|TimeValue
name|timeout
decl_stmt|;
DECL|field|queryBoost
specifier|private
name|float
name|queryBoost
init|=
literal|1.0f
decl_stmt|;
DECL|field|scroll
specifier|private
name|Scroll
name|scroll
decl_stmt|;
DECL|field|explain
specifier|private
name|boolean
name|explain
decl_stmt|;
DECL|field|version
specifier|private
name|boolean
name|version
init|=
literal|false
decl_stmt|;
comment|// by default, we don't return versions
DECL|field|fieldNames
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
decl_stmt|;
DECL|field|from
specifier|private
name|int
name|from
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|size
specifier|private
name|int
name|size
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|types
specifier|private
name|String
index|[]
name|types
decl_stmt|;
DECL|field|sort
specifier|private
name|Sort
name|sort
decl_stmt|;
DECL|field|minimumScore
specifier|private
name|Float
name|minimumScore
decl_stmt|;
DECL|field|trackScores
specifier|private
name|boolean
name|trackScores
init|=
literal|false
decl_stmt|;
comment|// when sorting, track scores as well...
DECL|field|originalQuery
specifier|private
name|ParsedQuery
name|originalQuery
decl_stmt|;
DECL|field|query
specifier|private
name|Query
name|query
decl_stmt|;
DECL|field|filter
specifier|private
name|Filter
name|filter
decl_stmt|;
DECL|field|aliasFilter
specifier|private
name|Filter
name|aliasFilter
decl_stmt|;
DECL|field|docIdsToLoad
specifier|private
name|int
index|[]
name|docIdsToLoad
decl_stmt|;
DECL|field|docsIdsToLoadFrom
specifier|private
name|int
name|docsIdsToLoadFrom
decl_stmt|;
DECL|field|docsIdsToLoadSize
specifier|private
name|int
name|docsIdsToLoadSize
decl_stmt|;
DECL|field|facets
specifier|private
name|SearchContextFacets
name|facets
decl_stmt|;
DECL|field|highlight
specifier|private
name|SearchContextHighlight
name|highlight
decl_stmt|;
DECL|field|scriptFields
specifier|private
name|ScriptFieldsContext
name|scriptFields
decl_stmt|;
DECL|field|searchLookup
specifier|private
name|SearchLookup
name|searchLookup
decl_stmt|;
DECL|field|queryRewritten
specifier|private
name|boolean
name|queryRewritten
decl_stmt|;
DECL|field|keepAlive
specifier|private
specifier|volatile
name|long
name|keepAlive
decl_stmt|;
DECL|field|lastAccessTime
specifier|private
specifier|volatile
name|long
name|lastAccessTime
decl_stmt|;
DECL|field|scopePhases
specifier|private
name|List
argument_list|<
name|ScopePhase
argument_list|>
name|scopePhases
init|=
literal|null
decl_stmt|;
DECL|field|nestedQueries
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|BlockJoinQuery
argument_list|>
name|nestedQueries
decl_stmt|;
DECL|method|SearchContext
specifier|public
name|SearchContext
parameter_list|(
name|long
name|id
parameter_list|,
name|SearchShardTarget
name|shardTarget
parameter_list|,
name|SearchType
name|searchType
parameter_list|,
name|int
name|numberOfShards
parameter_list|,
name|TimeValue
name|timeout
parameter_list|,
name|String
index|[]
name|types
parameter_list|,
name|Engine
operator|.
name|Searcher
name|engineSearcher
parameter_list|,
name|IndexService
name|indexService
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|searchType
operator|=
name|searchType
expr_stmt|;
name|this
operator|.
name|shardTarget
operator|=
name|shardTarget
expr_stmt|;
name|this
operator|.
name|numberOfShards
operator|=
name|numberOfShards
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
name|this
operator|.
name|engineSearcher
operator|=
name|engineSearcher
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|dfsResult
operator|=
operator|new
name|DfsSearchResult
argument_list|(
name|id
argument_list|,
name|shardTarget
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryResult
operator|=
operator|new
name|QuerySearchResult
argument_list|(
name|id
argument_list|,
name|shardTarget
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchResult
operator|=
operator|new
name|FetchSearchResult
argument_list|(
name|id
argument_list|,
name|shardTarget
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexService
operator|=
name|indexService
expr_stmt|;
name|this
operator|.
name|searcher
operator|=
operator|new
name|ContextIndexSearcher
argument_list|(
name|this
argument_list|,
name|engineSearcher
argument_list|)
expr_stmt|;
block|}
DECL|method|release
annotation|@
name|Override
specifier|public
name|boolean
name|release
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
comment|// clear and scope phase we  have
if|if
condition|(
name|scopePhases
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ScopePhase
name|scopePhase
range|:
name|scopePhases
control|)
block|{
name|scopePhase
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
comment|// we should close this searcher, since its a new one we create each time, and we use the IndexReader
try|try
block|{
name|searcher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore any exception here
block|}
name|engineSearcher
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
DECL|method|id
specifier|public
name|long
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
DECL|method|searchType
specifier|public
name|SearchType
name|searchType
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchType
return|;
block|}
DECL|method|searchType
specifier|public
name|SearchContext
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
DECL|method|shardTarget
specifier|public
name|SearchShardTarget
name|shardTarget
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardTarget
return|;
block|}
DECL|method|numberOfShards
specifier|public
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
name|this
operator|.
name|numberOfShards
return|;
block|}
DECL|method|hasTypes
specifier|public
name|boolean
name|hasTypes
parameter_list|()
block|{
return|return
name|types
operator|!=
literal|null
operator|&&
name|types
operator|.
name|length
operator|>
literal|0
return|;
block|}
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
DECL|method|queryBoost
specifier|public
name|float
name|queryBoost
parameter_list|()
block|{
return|return
name|queryBoost
return|;
block|}
DECL|method|queryBoost
specifier|public
name|SearchContext
name|queryBoost
parameter_list|(
name|float
name|queryBoost
parameter_list|)
block|{
name|this
operator|.
name|queryBoost
operator|=
name|queryBoost
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|scroll
specifier|public
name|Scroll
name|scroll
parameter_list|()
block|{
return|return
name|this
operator|.
name|scroll
return|;
block|}
DECL|method|scroll
specifier|public
name|SearchContext
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
DECL|method|facets
specifier|public
name|SearchContextFacets
name|facets
parameter_list|()
block|{
return|return
name|facets
return|;
block|}
DECL|method|facets
specifier|public
name|SearchContext
name|facets
parameter_list|(
name|SearchContextFacets
name|facets
parameter_list|)
block|{
name|this
operator|.
name|facets
operator|=
name|facets
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|highlight
specifier|public
name|SearchContextHighlight
name|highlight
parameter_list|()
block|{
return|return
name|highlight
return|;
block|}
DECL|method|highlight
specifier|public
name|void
name|highlight
parameter_list|(
name|SearchContextHighlight
name|highlight
parameter_list|)
block|{
name|this
operator|.
name|highlight
operator|=
name|highlight
expr_stmt|;
block|}
DECL|method|hasScriptFields
specifier|public
name|boolean
name|hasScriptFields
parameter_list|()
block|{
return|return
name|scriptFields
operator|!=
literal|null
return|;
block|}
DECL|method|scriptFields
specifier|public
name|ScriptFieldsContext
name|scriptFields
parameter_list|()
block|{
if|if
condition|(
name|scriptFields
operator|==
literal|null
condition|)
block|{
name|scriptFields
operator|=
operator|new
name|ScriptFieldsContext
argument_list|()
expr_stmt|;
block|}
return|return
name|this
operator|.
name|scriptFields
return|;
block|}
DECL|method|searcher
specifier|public
name|ContextIndexSearcher
name|searcher
parameter_list|()
block|{
return|return
name|this
operator|.
name|searcher
return|;
block|}
DECL|method|mapperService
specifier|public
name|MapperService
name|mapperService
parameter_list|()
block|{
return|return
name|indexService
operator|.
name|mapperService
argument_list|()
return|;
block|}
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
DECL|method|queryParserService
specifier|public
name|IndexQueryParserService
name|queryParserService
parameter_list|()
block|{
return|return
name|indexService
operator|.
name|queryParserService
argument_list|()
return|;
block|}
DECL|method|similarityService
specifier|public
name|SimilarityService
name|similarityService
parameter_list|()
block|{
return|return
name|indexService
operator|.
name|similarityService
argument_list|()
return|;
block|}
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
DECL|method|filterCache
specifier|public
name|FilterCache
name|filterCache
parameter_list|()
block|{
return|return
name|indexService
operator|.
name|cache
argument_list|()
operator|.
name|filter
argument_list|()
return|;
block|}
DECL|method|fieldDataCache
specifier|public
name|FieldDataCache
name|fieldDataCache
parameter_list|()
block|{
return|return
name|indexService
operator|.
name|cache
argument_list|()
operator|.
name|fieldData
argument_list|()
return|;
block|}
DECL|method|idCache
specifier|public
name|IdCache
name|idCache
parameter_list|()
block|{
return|return
name|indexService
operator|.
name|cache
argument_list|()
operator|.
name|idCache
argument_list|()
return|;
block|}
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
name|minimumScore
operator|=
name|minimumScore
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|minimumScore
specifier|public
name|Float
name|minimumScore
parameter_list|()
block|{
return|return
name|this
operator|.
name|minimumScore
return|;
block|}
DECL|method|sort
specifier|public
name|SearchContext
name|sort
parameter_list|(
name|Sort
name|sort
parameter_list|)
block|{
name|this
operator|.
name|sort
operator|=
name|sort
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|sort
specifier|public
name|Sort
name|sort
parameter_list|()
block|{
return|return
name|this
operator|.
name|sort
return|;
block|}
DECL|method|trackScores
specifier|public
name|SearchContext
name|trackScores
parameter_list|(
name|boolean
name|trackScores
parameter_list|)
block|{
name|this
operator|.
name|trackScores
operator|=
name|trackScores
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|trackScores
specifier|public
name|boolean
name|trackScores
parameter_list|()
block|{
return|return
name|this
operator|.
name|trackScores
return|;
block|}
DECL|method|parsedFilter
specifier|public
name|SearchContext
name|parsedFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|parsedFilter
specifier|public
name|Filter
name|parsedFilter
parameter_list|()
block|{
return|return
name|this
operator|.
name|filter
return|;
block|}
DECL|method|aliasFilter
specifier|public
name|SearchContext
name|aliasFilter
parameter_list|(
name|Filter
name|aliasFilter
parameter_list|)
block|{
name|this
operator|.
name|aliasFilter
operator|=
name|aliasFilter
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|aliasFilter
specifier|public
name|Filter
name|aliasFilter
parameter_list|()
block|{
return|return
name|aliasFilter
return|;
block|}
DECL|method|parsedQuery
specifier|public
name|SearchContext
name|parsedQuery
parameter_list|(
name|ParsedQuery
name|query
parameter_list|)
block|{
name|queryRewritten
operator|=
literal|false
expr_stmt|;
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
DECL|method|parsedQuery
specifier|public
name|ParsedQuery
name|parsedQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|originalQuery
return|;
block|}
comment|/**      * The query to execute, might be rewritten.      */
DECL|method|query
specifier|public
name|Query
name|query
parameter_list|()
block|{
return|return
name|this
operator|.
name|query
return|;
block|}
comment|/**      * Has the query been rewritten already?      */
DECL|method|queryRewritten
specifier|public
name|boolean
name|queryRewritten
parameter_list|()
block|{
return|return
name|queryRewritten
return|;
block|}
comment|/**      * Rewrites the query and updates it. Only happens once.      */
DECL|method|updateRewriteQuery
specifier|public
name|SearchContext
name|updateRewriteQuery
parameter_list|(
name|Query
name|rewriteQuery
parameter_list|)
block|{
name|query
operator|=
name|rewriteQuery
expr_stmt|;
name|queryRewritten
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|from
specifier|public
name|int
name|from
parameter_list|()
block|{
return|return
name|from
return|;
block|}
DECL|method|from
specifier|public
name|SearchContext
name|from
parameter_list|(
name|int
name|from
parameter_list|)
block|{
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
return|return
name|this
return|;
block|}
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
DECL|method|size
specifier|public
name|SearchContext
name|size
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
return|return
name|this
return|;
block|}
DECL|method|hasFieldNames
specifier|public
name|boolean
name|hasFieldNames
parameter_list|()
block|{
return|return
name|fieldNames
operator|!=
literal|null
return|;
block|}
DECL|method|fieldNames
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|()
block|{
if|if
condition|(
name|fieldNames
operator|==
literal|null
condition|)
block|{
name|fieldNames
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
block|}
return|return
name|fieldNames
return|;
block|}
DECL|method|emptyFieldNames
specifier|public
name|void
name|emptyFieldNames
parameter_list|()
block|{
name|this
operator|.
name|fieldNames
operator|=
name|ImmutableList
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
DECL|method|explain
specifier|public
name|boolean
name|explain
parameter_list|()
block|{
return|return
name|explain
return|;
block|}
DECL|method|explain
specifier|public
name|void
name|explain
parameter_list|(
name|boolean
name|explain
parameter_list|)
block|{
name|this
operator|.
name|explain
operator|=
name|explain
expr_stmt|;
block|}
DECL|method|version
specifier|public
name|boolean
name|version
parameter_list|()
block|{
return|return
name|version
return|;
block|}
DECL|method|version
specifier|public
name|void
name|version
parameter_list|(
name|boolean
name|version
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
DECL|method|docIdsToLoad
specifier|public
name|int
index|[]
name|docIdsToLoad
parameter_list|()
block|{
return|return
name|docIdsToLoad
return|;
block|}
DECL|method|docIdsToLoadFrom
specifier|public
name|int
name|docIdsToLoadFrom
parameter_list|()
block|{
return|return
name|docsIdsToLoadFrom
return|;
block|}
DECL|method|docIdsToLoadSize
specifier|public
name|int
name|docIdsToLoadSize
parameter_list|()
block|{
return|return
name|docsIdsToLoadSize
return|;
block|}
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
name|this
operator|.
name|docIdsToLoad
operator|=
name|docIdsToLoad
expr_stmt|;
name|this
operator|.
name|docsIdsToLoadFrom
operator|=
name|docsIdsToLoadFrom
expr_stmt|;
name|this
operator|.
name|docsIdsToLoadSize
operator|=
name|docsIdsToLoadSize
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|accessed
specifier|public
name|void
name|accessed
parameter_list|(
name|long
name|accessTime
parameter_list|)
block|{
name|this
operator|.
name|lastAccessTime
operator|=
name|accessTime
expr_stmt|;
block|}
DECL|method|lastAccessTime
specifier|public
name|long
name|lastAccessTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|lastAccessTime
return|;
block|}
DECL|method|keepAlive
specifier|public
name|long
name|keepAlive
parameter_list|()
block|{
return|return
name|this
operator|.
name|keepAlive
return|;
block|}
DECL|method|keepAlive
specifier|public
name|void
name|keepAlive
parameter_list|(
name|long
name|keepAlive
parameter_list|)
block|{
name|this
operator|.
name|keepAlive
operator|=
name|keepAlive
expr_stmt|;
block|}
DECL|method|lookup
specifier|public
name|SearchLookup
name|lookup
parameter_list|()
block|{
if|if
condition|(
name|searchLookup
operator|==
literal|null
condition|)
block|{
name|searchLookup
operator|=
operator|new
name|SearchLookup
argument_list|(
name|mapperService
argument_list|()
argument_list|,
name|fieldDataCache
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|searchLookup
return|;
block|}
DECL|method|dfsResult
specifier|public
name|DfsSearchResult
name|dfsResult
parameter_list|()
block|{
return|return
name|dfsResult
return|;
block|}
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
DECL|method|fetchResult
specifier|public
name|FetchSearchResult
name|fetchResult
parameter_list|()
block|{
return|return
name|fetchResult
return|;
block|}
DECL|method|scopePhases
specifier|public
name|List
argument_list|<
name|ScopePhase
argument_list|>
name|scopePhases
parameter_list|()
block|{
return|return
name|this
operator|.
name|scopePhases
return|;
block|}
DECL|method|addScopePhase
specifier|public
name|void
name|addScopePhase
parameter_list|(
name|ScopePhase
name|scopePhase
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|scopePhases
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|scopePhases
operator|=
operator|new
name|ArrayList
argument_list|<
name|ScopePhase
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|scopePhases
operator|.
name|add
argument_list|(
name|scopePhase
argument_list|)
expr_stmt|;
block|}
DECL|method|nestedQueries
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|BlockJoinQuery
argument_list|>
name|nestedQueries
parameter_list|()
block|{
return|return
name|this
operator|.
name|nestedQueries
return|;
block|}
DECL|method|addNestedQuery
specifier|public
name|void
name|addNestedQuery
parameter_list|(
name|String
name|scope
parameter_list|,
name|BlockJoinQuery
name|query
parameter_list|)
block|{
if|if
condition|(
name|nestedQueries
operator|==
literal|null
condition|)
block|{
name|nestedQueries
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|BlockJoinQuery
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|nestedQueries
operator|.
name|put
argument_list|(
name|scope
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

