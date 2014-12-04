begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.controller
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|controller
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntArrayList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectObjectOpenHashMap
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
name|index
operator|.
name|Term
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
name|*
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
name|common
operator|.
name|collect
operator|.
name|HppcMaps
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
name|component
operator|.
name|AbstractComponent
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
name|lucene
operator|.
name|Lucene
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
name|concurrent
operator|.
name|AtomicArray
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
name|aggregations
operator|.
name|InternalAggregation
operator|.
name|ReduceContext
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
name|InternalAggregations
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
name|AggregatedDfs
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
name|FetchSearchResultProvider
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
name|InternalSearchHit
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
name|InternalSearchHits
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
name|InternalSearchResponse
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
name|query
operator|.
name|QuerySearchResultProvider
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
name|Suggest
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
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SearchPhaseController
specifier|public
class|class
name|SearchPhaseController
extends|extends
name|AbstractComponent
block|{
DECL|field|QUERY_RESULT_ORDERING
specifier|public
specifier|static
specifier|final
name|Comparator
argument_list|<
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
argument_list|>
name|QUERY_RESULT_ORDERING
init|=
operator|new
name|Comparator
argument_list|<
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
name|o1
parameter_list|,
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
name|o2
parameter_list|)
block|{
name|int
name|i
init|=
name|o1
operator|.
name|value
operator|.
name|shardTarget
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|value
operator|.
name|shardTarget
argument_list|()
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|o1
operator|.
name|value
operator|.
name|shardTarget
argument_list|()
operator|.
name|shardId
argument_list|()
operator|-
name|o2
operator|.
name|value
operator|.
name|shardTarget
argument_list|()
operator|.
name|shardId
argument_list|()
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
block|}
empty_stmt|;
DECL|field|EMPTY_DOCS
specifier|public
specifier|static
specifier|final
name|ScoreDoc
index|[]
name|EMPTY_DOCS
init|=
operator|new
name|ScoreDoc
index|[
literal|0
index|]
decl_stmt|;
DECL|field|bigArrays
specifier|private
specifier|final
name|BigArrays
name|bigArrays
decl_stmt|;
DECL|field|optimizeSingleShard
specifier|private
specifier|final
name|boolean
name|optimizeSingleShard
decl_stmt|;
DECL|field|scriptService
specifier|private
name|ScriptService
name|scriptService
decl_stmt|;
annotation|@
name|Inject
DECL|method|SearchPhaseController
specifier|public
name|SearchPhaseController
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|bigArrays
operator|=
name|bigArrays
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|optimizeSingleShard
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"optimize_single_shard"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|optimizeSingleShard
specifier|public
name|boolean
name|optimizeSingleShard
parameter_list|()
block|{
return|return
name|optimizeSingleShard
return|;
block|}
DECL|method|aggregateDfs
specifier|public
name|AggregatedDfs
name|aggregateDfs
parameter_list|(
name|AtomicArray
argument_list|<
name|DfsSearchResult
argument_list|>
name|results
parameter_list|)
block|{
name|ObjectObjectOpenHashMap
argument_list|<
name|Term
argument_list|,
name|TermStatistics
argument_list|>
name|termStatistics
init|=
name|HppcMaps
operator|.
name|newNoNullKeysMap
argument_list|()
decl_stmt|;
name|ObjectObjectOpenHashMap
argument_list|<
name|String
argument_list|,
name|CollectionStatistics
argument_list|>
name|fieldStatistics
init|=
name|HppcMaps
operator|.
name|newNoNullKeysMap
argument_list|()
decl_stmt|;
name|long
name|aggMaxDoc
init|=
literal|0
decl_stmt|;
for|for
control|(
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|DfsSearchResult
argument_list|>
name|lEntry
range|:
name|results
operator|.
name|asList
argument_list|()
control|)
block|{
specifier|final
name|Term
index|[]
name|terms
init|=
name|lEntry
operator|.
name|value
operator|.
name|terms
argument_list|()
decl_stmt|;
specifier|final
name|TermStatistics
index|[]
name|stats
init|=
name|lEntry
operator|.
name|value
operator|.
name|termStatistics
argument_list|()
decl_stmt|;
assert|assert
name|terms
operator|.
name|length
operator|==
name|stats
operator|.
name|length
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|terms
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
assert|assert
name|terms
index|[
name|i
index|]
operator|!=
literal|null
assert|;
name|TermStatistics
name|existing
init|=
name|termStatistics
operator|.
name|get
argument_list|(
name|terms
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|existing
operator|!=
literal|null
condition|)
block|{
assert|assert
name|terms
index|[
name|i
index|]
operator|.
name|bytes
argument_list|()
operator|.
name|equals
argument_list|(
name|existing
operator|.
name|term
argument_list|()
argument_list|)
assert|;
comment|// totalTermFrequency is an optional statistic we need to check if either one or both
comment|// are set to -1 which means not present and then set it globally to -1
name|termStatistics
operator|.
name|put
argument_list|(
name|terms
index|[
name|i
index|]
argument_list|,
operator|new
name|TermStatistics
argument_list|(
name|existing
operator|.
name|term
argument_list|()
argument_list|,
name|existing
operator|.
name|docFreq
argument_list|()
operator|+
name|stats
index|[
name|i
index|]
operator|.
name|docFreq
argument_list|()
argument_list|,
name|optionalSum
argument_list|(
name|existing
operator|.
name|totalTermFreq
argument_list|()
argument_list|,
name|stats
index|[
name|i
index|]
operator|.
name|totalTermFreq
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|termStatistics
operator|.
name|put
argument_list|(
name|terms
index|[
name|i
index|]
argument_list|,
name|stats
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|boolean
index|[]
name|states
init|=
name|lEntry
operator|.
name|value
operator|.
name|fieldStatistics
argument_list|()
operator|.
name|allocated
decl_stmt|;
specifier|final
name|Object
index|[]
name|keys
init|=
name|lEntry
operator|.
name|value
operator|.
name|fieldStatistics
argument_list|()
operator|.
name|keys
decl_stmt|;
specifier|final
name|Object
index|[]
name|values
init|=
name|lEntry
operator|.
name|value
operator|.
name|fieldStatistics
argument_list|()
operator|.
name|values
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|states
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
condition|)
block|{
name|String
name|key
init|=
operator|(
name|String
operator|)
name|keys
index|[
name|i
index|]
decl_stmt|;
name|CollectionStatistics
name|value
init|=
operator|(
name|CollectionStatistics
operator|)
name|values
index|[
name|i
index|]
decl_stmt|;
assert|assert
name|key
operator|!=
literal|null
assert|;
name|CollectionStatistics
name|existing
init|=
name|fieldStatistics
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|existing
operator|!=
literal|null
condition|)
block|{
name|CollectionStatistics
name|merged
init|=
operator|new
name|CollectionStatistics
argument_list|(
name|key
argument_list|,
name|existing
operator|.
name|maxDoc
argument_list|()
operator|+
name|value
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|optionalSum
argument_list|(
name|existing
operator|.
name|docCount
argument_list|()
argument_list|,
name|value
operator|.
name|docCount
argument_list|()
argument_list|)
argument_list|,
name|optionalSum
argument_list|(
name|existing
operator|.
name|sumTotalTermFreq
argument_list|()
argument_list|,
name|value
operator|.
name|sumTotalTermFreq
argument_list|()
argument_list|)
argument_list|,
name|optionalSum
argument_list|(
name|existing
operator|.
name|sumDocFreq
argument_list|()
argument_list|,
name|value
operator|.
name|sumDocFreq
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|fieldStatistics
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|merged
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fieldStatistics
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|aggMaxDoc
operator|+=
name|lEntry
operator|.
name|value
operator|.
name|maxDoc
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|AggregatedDfs
argument_list|(
name|termStatistics
argument_list|,
name|fieldStatistics
argument_list|,
name|aggMaxDoc
argument_list|)
return|;
block|}
DECL|method|optionalSum
specifier|private
specifier|static
name|long
name|optionalSum
parameter_list|(
name|long
name|left
parameter_list|,
name|long
name|right
parameter_list|)
block|{
return|return
name|Math
operator|.
name|min
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
operator|==
operator|-
literal|1
condition|?
operator|-
literal|1
else|:
name|left
operator|+
name|right
return|;
block|}
comment|/**      * @param ignoreFrom Whether to ignore the from and sort all hits in each shard result.      *                   Enabled only for scroll search, because that only retrieves hits of length 'size' in the query phase.      * @param resultsArr Shard result holder      */
DECL|method|sortDocs
specifier|public
name|ScoreDoc
index|[]
name|sortDocs
parameter_list|(
name|boolean
name|ignoreFrom
parameter_list|,
name|AtomicArray
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
name|resultsArr
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|?
extends|extends
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
argument_list|>
name|results
init|=
name|resultsArr
operator|.
name|asList
argument_list|()
decl_stmt|;
if|if
condition|(
name|results
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|EMPTY_DOCS
return|;
block|}
if|if
condition|(
name|optimizeSingleShard
condition|)
block|{
name|boolean
name|canOptimize
init|=
literal|false
decl_stmt|;
name|QuerySearchResult
name|result
init|=
literal|null
decl_stmt|;
name|int
name|shardIndex
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|results
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|canOptimize
operator|=
literal|true
expr_stmt|;
name|result
operator|=
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
operator|.
name|queryResult
argument_list|()
expr_stmt|;
name|shardIndex
operator|=
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|index
expr_stmt|;
block|}
else|else
block|{
comment|// lets see if we only got hits from a single shard, if so, we can optimize...
for|for
control|(
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
name|entry
range|:
name|results
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|value
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
operator|.
name|scoreDocs
operator|.
name|length
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
comment|// we already have one, can't really optimize
name|canOptimize
operator|=
literal|false
expr_stmt|;
break|break;
block|}
name|canOptimize
operator|=
literal|true
expr_stmt|;
name|result
operator|=
name|entry
operator|.
name|value
operator|.
name|queryResult
argument_list|()
expr_stmt|;
name|shardIndex
operator|=
name|entry
operator|.
name|index
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|canOptimize
condition|)
block|{
name|int
name|offset
init|=
name|result
operator|.
name|from
argument_list|()
decl_stmt|;
if|if
condition|(
name|ignoreFrom
condition|)
block|{
name|offset
operator|=
literal|0
expr_stmt|;
block|}
name|ScoreDoc
index|[]
name|scoreDocs
init|=
name|result
operator|.
name|topDocs
argument_list|()
operator|.
name|scoreDocs
decl_stmt|;
if|if
condition|(
name|scoreDocs
operator|.
name|length
operator|==
literal|0
operator|||
name|scoreDocs
operator|.
name|length
operator|<
name|offset
condition|)
block|{
return|return
name|EMPTY_DOCS
return|;
block|}
name|int
name|resultDocsSize
init|=
name|result
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|scoreDocs
operator|.
name|length
operator|-
name|offset
operator|)
operator|<
name|resultDocsSize
condition|)
block|{
name|resultDocsSize
operator|=
name|scoreDocs
operator|.
name|length
operator|-
name|offset
expr_stmt|;
block|}
name|ScoreDoc
index|[]
name|docs
init|=
operator|new
name|ScoreDoc
index|[
name|resultDocsSize
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|resultDocsSize
condition|;
name|i
operator|++
control|)
block|{
name|ScoreDoc
name|scoreDoc
init|=
name|scoreDocs
index|[
name|offset
operator|+
name|i
index|]
decl_stmt|;
name|scoreDoc
operator|.
name|shardIndex
operator|=
name|shardIndex
expr_stmt|;
name|docs
index|[
name|i
index|]
operator|=
name|scoreDoc
expr_stmt|;
block|}
return|return
name|docs
return|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
index|[]
name|sortedResults
init|=
name|results
operator|.
name|toArray
argument_list|(
operator|new
name|AtomicArray
operator|.
name|Entry
index|[
name|results
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|sortedResults
argument_list|,
name|QUERY_RESULT_ORDERING
argument_list|)
expr_stmt|;
name|QuerySearchResultProvider
name|firstResult
init|=
name|sortedResults
index|[
literal|0
index|]
operator|.
name|value
decl_stmt|;
specifier|final
name|Sort
name|sort
decl_stmt|;
if|if
condition|(
name|firstResult
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
operator|instanceof
name|TopFieldDocs
condition|)
block|{
name|TopFieldDocs
name|firstTopDocs
init|=
operator|(
name|TopFieldDocs
operator|)
name|firstResult
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
decl_stmt|;
name|sort
operator|=
operator|new
name|Sort
argument_list|(
name|firstTopDocs
operator|.
name|fields
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sort
operator|=
literal|null
expr_stmt|;
block|}
name|int
name|topN
init|=
name|firstResult
operator|.
name|queryResult
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// Need to use the length of the resultsArr array, since the slots will be based on the position in the resultsArr array
name|TopDocs
index|[]
name|shardTopDocs
init|=
operator|new
name|TopDocs
index|[
name|resultsArr
operator|.
name|length
argument_list|()
index|]
decl_stmt|;
if|if
condition|(
name|firstResult
operator|.
name|includeFetch
argument_list|()
condition|)
block|{
comment|// if we did both query and fetch on the same go, we have fetched all the docs from each shards already, use them...
comment|// this is also important since we shortcut and fetch only docs from "from" and up to "size"
name|topN
operator|*=
name|sortedResults
operator|.
name|length
expr_stmt|;
block|}
for|for
control|(
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
name|sortedResult
range|:
name|sortedResults
control|)
block|{
name|TopDocs
name|topDocs
init|=
name|sortedResult
operator|.
name|value
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
decl_stmt|;
comment|// the 'index' field is the position in the resultsArr atomic array
name|shardTopDocs
index|[
name|sortedResult
operator|.
name|index
index|]
operator|=
name|topDocs
expr_stmt|;
block|}
name|int
name|from
init|=
name|firstResult
operator|.
name|queryResult
argument_list|()
operator|.
name|from
argument_list|()
decl_stmt|;
if|if
condition|(
name|ignoreFrom
condition|)
block|{
name|from
operator|=
literal|0
expr_stmt|;
block|}
comment|// TopDocs#merge can't deal with null shard TopDocs
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|shardTopDocs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|shardTopDocs
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
name|shardTopDocs
index|[
name|i
index|]
operator|=
name|Lucene
operator|.
name|EMPTY_TOP_DOCS
expr_stmt|;
block|}
block|}
name|TopDocs
name|mergedTopDocs
init|=
name|TopDocs
operator|.
name|merge
argument_list|(
name|sort
argument_list|,
name|from
argument_list|,
name|topN
argument_list|,
name|shardTopDocs
argument_list|)
decl_stmt|;
return|return
name|mergedTopDocs
operator|.
name|scoreDocs
return|;
block|}
DECL|method|getLastEmittedDocPerShard
specifier|public
name|ScoreDoc
index|[]
name|getLastEmittedDocPerShard
parameter_list|(
name|SearchRequest
name|request
parameter_list|,
name|ScoreDoc
index|[]
name|sortedShardList
parameter_list|,
name|int
name|numShards
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|scroll
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|getLastEmittedDocPerShard
argument_list|(
name|sortedShardList
argument_list|,
name|numShards
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|method|getLastEmittedDocPerShard
specifier|public
name|ScoreDoc
index|[]
name|getLastEmittedDocPerShard
parameter_list|(
name|ScoreDoc
index|[]
name|sortedShardList
parameter_list|,
name|int
name|numShards
parameter_list|)
block|{
name|ScoreDoc
index|[]
name|lastEmittedDocPerShard
init|=
operator|new
name|ScoreDoc
index|[
name|numShards
index|]
decl_stmt|;
for|for
control|(
name|ScoreDoc
name|scoreDoc
range|:
name|sortedShardList
control|)
block|{
name|lastEmittedDocPerShard
index|[
name|scoreDoc
operator|.
name|shardIndex
index|]
operator|=
name|scoreDoc
expr_stmt|;
block|}
return|return
name|lastEmittedDocPerShard
return|;
block|}
comment|/**      * Builds an array, with potential null elements, with docs to load.      */
DECL|method|fillDocIdsToLoad
specifier|public
name|void
name|fillDocIdsToLoad
parameter_list|(
name|AtomicArray
argument_list|<
name|IntArrayList
argument_list|>
name|docsIdsToLoad
parameter_list|,
name|ScoreDoc
index|[]
name|shardDocs
parameter_list|)
block|{
for|for
control|(
name|ScoreDoc
name|shardDoc
range|:
name|shardDocs
control|)
block|{
name|IntArrayList
name|list
init|=
name|docsIdsToLoad
operator|.
name|get
argument_list|(
name|shardDoc
operator|.
name|shardIndex
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
name|list
operator|=
operator|new
name|IntArrayList
argument_list|()
expr_stmt|;
comment|// can't be shared!, uses unsafe on it later on
name|docsIdsToLoad
operator|.
name|set
argument_list|(
name|shardDoc
operator|.
name|shardIndex
argument_list|,
name|list
argument_list|)
expr_stmt|;
block|}
name|list
operator|.
name|add
argument_list|(
name|shardDoc
operator|.
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|merge
specifier|public
name|InternalSearchResponse
name|merge
parameter_list|(
name|ScoreDoc
index|[]
name|sortedDocs
parameter_list|,
name|AtomicArray
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
name|queryResultsArr
parameter_list|,
name|AtomicArray
argument_list|<
name|?
extends|extends
name|FetchSearchResultProvider
argument_list|>
name|fetchResultsArr
parameter_list|)
block|{
name|List
argument_list|<
name|?
extends|extends
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
argument_list|>
name|queryResults
init|=
name|queryResultsArr
operator|.
name|asList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|FetchSearchResultProvider
argument_list|>
argument_list|>
name|fetchResults
init|=
name|fetchResultsArr
operator|.
name|asList
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryResults
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|InternalSearchResponse
operator|.
name|empty
argument_list|()
return|;
block|}
name|QuerySearchResult
name|firstResult
init|=
name|queryResults
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
operator|.
name|queryResult
argument_list|()
decl_stmt|;
name|boolean
name|sorted
init|=
literal|false
decl_stmt|;
name|int
name|sortScoreIndex
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|firstResult
operator|.
name|topDocs
argument_list|()
operator|instanceof
name|TopFieldDocs
condition|)
block|{
name|sorted
operator|=
literal|true
expr_stmt|;
name|TopFieldDocs
name|fieldDocs
init|=
operator|(
name|TopFieldDocs
operator|)
name|firstResult
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fieldDocs
operator|.
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|fieldDocs
operator|.
name|fields
index|[
name|i
index|]
operator|.
name|getType
argument_list|()
operator|==
name|SortField
operator|.
name|Type
operator|.
name|SCORE
condition|)
block|{
name|sortScoreIndex
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
comment|// count the total (we use the query result provider here, since we might not get any hits (we scrolled past them))
name|long
name|totalHits
init|=
literal|0
decl_stmt|;
name|float
name|maxScore
init|=
name|Float
operator|.
name|NEGATIVE_INFINITY
decl_stmt|;
name|boolean
name|timedOut
init|=
literal|false
decl_stmt|;
name|Boolean
name|terminatedEarly
init|=
literal|null
decl_stmt|;
for|for
control|(
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
name|entry
range|:
name|queryResults
control|)
block|{
name|QuerySearchResult
name|result
init|=
name|entry
operator|.
name|value
operator|.
name|queryResult
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|searchTimedOut
argument_list|()
condition|)
block|{
name|timedOut
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|result
operator|.
name|terminatedEarly
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|terminatedEarly
operator|==
literal|null
condition|)
block|{
name|terminatedEarly
operator|=
name|result
operator|.
name|terminatedEarly
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|result
operator|.
name|terminatedEarly
argument_list|()
condition|)
block|{
name|terminatedEarly
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|totalHits
operator|+=
name|result
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
expr_stmt|;
if|if
condition|(
operator|!
name|Float
operator|.
name|isNaN
argument_list|(
name|result
operator|.
name|topDocs
argument_list|()
operator|.
name|getMaxScore
argument_list|()
argument_list|)
condition|)
block|{
name|maxScore
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxScore
argument_list|,
name|result
operator|.
name|topDocs
argument_list|()
operator|.
name|getMaxScore
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|Float
operator|.
name|isInfinite
argument_list|(
name|maxScore
argument_list|)
condition|)
block|{
name|maxScore
operator|=
name|Float
operator|.
name|NaN
expr_stmt|;
block|}
comment|// clean the fetch counter
for|for
control|(
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|FetchSearchResultProvider
argument_list|>
name|entry
range|:
name|fetchResults
control|)
block|{
name|entry
operator|.
name|value
operator|.
name|fetchResult
argument_list|()
operator|.
name|initCounter
argument_list|()
expr_stmt|;
block|}
comment|// merge hits
name|List
argument_list|<
name|InternalSearchHit
argument_list|>
name|hits
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fetchResults
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|ScoreDoc
name|shardDoc
range|:
name|sortedDocs
control|)
block|{
name|FetchSearchResultProvider
name|fetchResultProvider
init|=
name|fetchResultsArr
operator|.
name|get
argument_list|(
name|shardDoc
operator|.
name|shardIndex
argument_list|)
decl_stmt|;
if|if
condition|(
name|fetchResultProvider
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|FetchSearchResult
name|fetchResult
init|=
name|fetchResultProvider
operator|.
name|fetchResult
argument_list|()
decl_stmt|;
name|int
name|index
init|=
name|fetchResult
operator|.
name|counterGetAndIncrement
argument_list|()
decl_stmt|;
if|if
condition|(
name|index
operator|<
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|internalHits
argument_list|()
operator|.
name|length
condition|)
block|{
name|InternalSearchHit
name|searchHit
init|=
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|internalHits
argument_list|()
index|[
name|index
index|]
decl_stmt|;
name|searchHit
operator|.
name|score
argument_list|(
name|shardDoc
operator|.
name|score
argument_list|)
expr_stmt|;
name|searchHit
operator|.
name|shard
argument_list|(
name|fetchResult
operator|.
name|shardTarget
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|sorted
condition|)
block|{
name|FieldDoc
name|fieldDoc
init|=
operator|(
name|FieldDoc
operator|)
name|shardDoc
decl_stmt|;
name|searchHit
operator|.
name|sortValues
argument_list|(
name|fieldDoc
operator|.
name|fields
argument_list|)
expr_stmt|;
if|if
condition|(
name|sortScoreIndex
operator|!=
operator|-
literal|1
condition|)
block|{
name|searchHit
operator|.
name|score
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|fieldDoc
operator|.
name|fields
index|[
name|sortScoreIndex
index|]
operator|)
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|hits
operator|.
name|add
argument_list|(
name|searchHit
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// merge suggest results
name|Suggest
name|suggest
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|queryResults
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Suggest
operator|.
name|Suggestion
argument_list|>
argument_list|>
name|groupedSuggestions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|hasSuggestions
init|=
literal|false
decl_stmt|;
for|for
control|(
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
name|entry
range|:
name|queryResults
control|)
block|{
name|Suggest
name|shardResult
init|=
name|entry
operator|.
name|value
operator|.
name|queryResult
argument_list|()
operator|.
name|queryResult
argument_list|()
operator|.
name|suggest
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardResult
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|hasSuggestions
operator|=
literal|true
expr_stmt|;
name|Suggest
operator|.
name|group
argument_list|(
name|groupedSuggestions
argument_list|,
name|shardResult
argument_list|)
expr_stmt|;
block|}
name|suggest
operator|=
name|hasSuggestions
condition|?
operator|new
name|Suggest
argument_list|(
name|Suggest
operator|.
name|Fields
operator|.
name|SUGGEST
argument_list|,
name|Suggest
operator|.
name|reduce
argument_list|(
name|groupedSuggestions
argument_list|)
argument_list|)
else|:
literal|null
expr_stmt|;
block|}
comment|// merge addAggregation
name|InternalAggregations
name|aggregations
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|queryResults
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|firstResult
operator|.
name|aggregations
argument_list|()
operator|!=
literal|null
operator|&&
name|firstResult
operator|.
name|aggregations
argument_list|()
operator|.
name|asList
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|InternalAggregations
argument_list|>
name|aggregationsList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|queryResults
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
name|entry
range|:
name|queryResults
control|)
block|{
name|aggregationsList
operator|.
name|add
argument_list|(
operator|(
name|InternalAggregations
operator|)
name|entry
operator|.
name|value
operator|.
name|queryResult
argument_list|()
operator|.
name|aggregations
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|aggregations
operator|=
name|InternalAggregations
operator|.
name|reduce
argument_list|(
name|aggregationsList
argument_list|,
operator|new
name|ReduceContext
argument_list|(
literal|null
argument_list|,
name|bigArrays
argument_list|,
name|scriptService
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|InternalSearchHits
name|searchHits
init|=
operator|new
name|InternalSearchHits
argument_list|(
name|hits
operator|.
name|toArray
argument_list|(
operator|new
name|InternalSearchHit
index|[
name|hits
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|totalHits
argument_list|,
name|maxScore
argument_list|)
decl_stmt|;
return|return
operator|new
name|InternalSearchResponse
argument_list|(
name|searchHits
argument_list|,
name|aggregations
argument_list|,
name|suggest
argument_list|,
name|timedOut
argument_list|,
name|terminatedEarly
argument_list|)
return|;
block|}
block|}
end_class

end_unit

