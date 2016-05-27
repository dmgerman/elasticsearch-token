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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|ScoreDoc
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
name|ActionRunnable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexNameExpressionResolver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|service
operator|.
name|ClusterService
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
name|logging
operator|.
name|ESLogger
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
name|action
operator|.
name|SearchTransportService
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
name|controller
operator|.
name|SearchPhaseController
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
name|ShardFetchSearchRequest
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
name|internal
operator|.
name|ShardSearchTransportRequest
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
name|QuerySearchRequest
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
name|threadpool
operator|.
name|ThreadPool
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_class
DECL|class|SearchDfsQueryThenFetchAsyncAction
class|class
name|SearchDfsQueryThenFetchAsyncAction
extends|extends
name|AbstractSearchAsyncAction
argument_list|<
name|DfsSearchResult
argument_list|>
block|{
DECL|field|queryResults
specifier|final
name|AtomicArray
argument_list|<
name|QuerySearchResult
argument_list|>
name|queryResults
decl_stmt|;
DECL|field|fetchResults
specifier|final
name|AtomicArray
argument_list|<
name|FetchSearchResult
argument_list|>
name|fetchResults
decl_stmt|;
DECL|field|docIdsToLoad
specifier|final
name|AtomicArray
argument_list|<
name|IntArrayList
argument_list|>
name|docIdsToLoad
decl_stmt|;
DECL|method|SearchDfsQueryThenFetchAsyncAction
name|SearchDfsQueryThenFetchAsyncAction
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|SearchTransportService
name|searchTransportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|SearchPhaseController
name|searchPhaseController
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|SearchRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|super
argument_list|(
name|logger
argument_list|,
name|searchTransportService
argument_list|,
name|clusterService
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|searchPhaseController
argument_list|,
name|threadPool
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
name|queryResults
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|firstResults
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|fetchResults
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|firstResults
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|docIdsToLoad
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|firstResults
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|firstPhaseName
specifier|protected
name|String
name|firstPhaseName
parameter_list|()
block|{
return|return
literal|"dfs"
return|;
block|}
annotation|@
name|Override
DECL|method|sendExecuteFirstPhase
specifier|protected
name|void
name|sendExecuteFirstPhase
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|ShardSearchTransportRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|DfsSearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|searchTransportService
operator|.
name|sendExecuteDfs
argument_list|(
name|node
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|moveToSecondPhase
specifier|protected
name|void
name|moveToSecondPhase
parameter_list|()
block|{
specifier|final
name|AggregatedDfs
name|dfs
init|=
name|searchPhaseController
operator|.
name|aggregateDfs
argument_list|(
name|firstResults
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|firstResults
operator|.
name|asList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|DfsSearchResult
argument_list|>
name|entry
range|:
name|firstResults
operator|.
name|asList
argument_list|()
control|)
block|{
name|DfsSearchResult
name|dfsResult
init|=
name|entry
operator|.
name|value
decl_stmt|;
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|dfsResult
operator|.
name|shardTarget
argument_list|()
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
name|QuerySearchRequest
name|querySearchRequest
init|=
operator|new
name|QuerySearchRequest
argument_list|(
name|request
argument_list|,
name|dfsResult
operator|.
name|id
argument_list|()
argument_list|,
name|dfs
argument_list|)
decl_stmt|;
name|executeQuery
argument_list|(
name|entry
operator|.
name|index
argument_list|,
name|dfsResult
argument_list|,
name|counter
argument_list|,
name|querySearchRequest
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|executeQuery
name|void
name|executeQuery
parameter_list|(
specifier|final
name|int
name|shardIndex
parameter_list|,
specifier|final
name|DfsSearchResult
name|dfsResult
parameter_list|,
specifier|final
name|AtomicInteger
name|counter
parameter_list|,
specifier|final
name|QuerySearchRequest
name|querySearchRequest
parameter_list|,
specifier|final
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|searchTransportService
operator|.
name|sendExecuteQuery
argument_list|(
name|node
argument_list|,
name|querySearchRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|QuerySearchResult
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|QuerySearchResult
name|result
parameter_list|)
block|{
name|result
operator|.
name|shardTarget
argument_list|(
name|dfsResult
operator|.
name|shardTarget
argument_list|()
argument_list|)
expr_stmt|;
name|queryResults
operator|.
name|set
argument_list|(
name|shardIndex
argument_list|,
name|result
argument_list|)
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|executeFetchPhase
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
try|try
block|{
name|onQueryFailure
argument_list|(
name|t
argument_list|,
name|querySearchRequest
argument_list|,
name|shardIndex
argument_list|,
name|dfsResult
argument_list|,
name|counter
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// the query might not have been executed at all (for example because thread pool rejected
comment|// execution) and the search context that was created in dfs phase might not be released.
comment|// release it again to be in the safe side
name|sendReleaseSearchContext
argument_list|(
name|querySearchRequest
operator|.
name|id
argument_list|()
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|onQueryFailure
name|void
name|onQueryFailure
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|QuerySearchRequest
name|querySearchRequest
parameter_list|,
name|int
name|shardIndex
parameter_list|,
name|DfsSearchResult
name|dfsResult
parameter_list|,
name|AtomicInteger
name|counter
parameter_list|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] Failed to execute query phase"
argument_list|,
name|t
argument_list|,
name|querySearchRequest
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|addShardFailure
argument_list|(
name|shardIndex
argument_list|,
name|dfsResult
operator|.
name|shardTarget
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|successfulOps
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|successfulOps
operator|.
name|get
argument_list|()
operator|==
literal|0
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|SearchPhaseExecutionException
argument_list|(
literal|"query"
argument_list|,
literal|"all shards failed"
argument_list|,
name|buildShardFailures
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|executeFetchPhase
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|executeFetchPhase
name|void
name|executeFetchPhase
parameter_list|()
block|{
try|try
block|{
name|innerExecuteFetchPhase
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ReduceSearchPhaseException
argument_list|(
literal|"query"
argument_list|,
literal|""
argument_list|,
name|e
argument_list|,
name|buildShardFailures
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|innerExecuteFetchPhase
name|void
name|innerExecuteFetchPhase
parameter_list|()
throws|throws
name|Exception
block|{
name|boolean
name|useScroll
init|=
name|request
operator|.
name|scroll
argument_list|()
operator|!=
literal|null
decl_stmt|;
name|sortedShardList
operator|=
name|searchPhaseController
operator|.
name|sortDocs
argument_list|(
name|useScroll
argument_list|,
name|queryResults
argument_list|)
expr_stmt|;
name|searchPhaseController
operator|.
name|fillDocIdsToLoad
argument_list|(
name|docIdsToLoad
argument_list|,
name|sortedShardList
argument_list|)
expr_stmt|;
if|if
condition|(
name|docIdsToLoad
operator|.
name|asList
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|finishHim
argument_list|()
expr_stmt|;
return|return;
block|}
specifier|final
name|ScoreDoc
index|[]
name|lastEmittedDocPerShard
init|=
name|searchPhaseController
operator|.
name|getLastEmittedDocPerShard
argument_list|(
name|request
argument_list|,
name|sortedShardList
argument_list|,
name|firstResults
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|docIdsToLoad
operator|.
name|asList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|IntArrayList
argument_list|>
name|entry
range|:
name|docIdsToLoad
operator|.
name|asList
argument_list|()
control|)
block|{
name|QuerySearchResult
name|queryResult
init|=
name|queryResults
operator|.
name|get
argument_list|(
name|entry
operator|.
name|index
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|queryResult
operator|.
name|shardTarget
argument_list|()
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
name|ShardFetchSearchRequest
name|fetchSearchRequest
init|=
name|createFetchRequest
argument_list|(
name|queryResult
argument_list|,
name|entry
argument_list|,
name|lastEmittedDocPerShard
argument_list|)
decl_stmt|;
name|executeFetch
argument_list|(
name|entry
operator|.
name|index
argument_list|,
name|queryResult
operator|.
name|shardTarget
argument_list|()
argument_list|,
name|counter
argument_list|,
name|fetchSearchRequest
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|executeFetch
name|void
name|executeFetch
parameter_list|(
specifier|final
name|int
name|shardIndex
parameter_list|,
specifier|final
name|SearchShardTarget
name|shardTarget
parameter_list|,
specifier|final
name|AtomicInteger
name|counter
parameter_list|,
specifier|final
name|ShardFetchSearchRequest
name|fetchSearchRequest
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|searchTransportService
operator|.
name|sendExecuteFetch
argument_list|(
name|node
argument_list|,
name|fetchSearchRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|FetchSearchResult
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|FetchSearchResult
name|result
parameter_list|)
block|{
name|result
operator|.
name|shardTarget
argument_list|(
name|shardTarget
argument_list|)
expr_stmt|;
name|fetchResults
operator|.
name|set
argument_list|(
name|shardIndex
argument_list|,
name|result
argument_list|)
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|finishHim
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// the search context might not be cleared on the node where the fetch was executed for example
comment|// because the action was rejected by the thread pool. in this case we need to send a dedicated
comment|// request to clear the search context. by setting docIdsToLoad to null, the context will be cleared
comment|// in TransportSearchTypeAction.releaseIrrelevantSearchContexts() after the search request is done.
name|docIdsToLoad
operator|.
name|set
argument_list|(
name|shardIndex
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|onFetchFailure
argument_list|(
name|t
argument_list|,
name|fetchSearchRequest
argument_list|,
name|shardIndex
argument_list|,
name|shardTarget
argument_list|,
name|counter
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|onFetchFailure
name|void
name|onFetchFailure
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|ShardFetchSearchRequest
name|fetchSearchRequest
parameter_list|,
name|int
name|shardIndex
parameter_list|,
name|SearchShardTarget
name|shardTarget
parameter_list|,
name|AtomicInteger
name|counter
parameter_list|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] Failed to execute fetch phase"
argument_list|,
name|t
argument_list|,
name|fetchSearchRequest
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|addShardFailure
argument_list|(
name|shardIndex
argument_list|,
name|shardTarget
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|successfulOps
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|finishHim
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|finishHim
specifier|private
name|void
name|finishHim
parameter_list|()
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|ActionRunnable
argument_list|<
name|SearchResponse
argument_list|>
argument_list|(
name|listener
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|doRun
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|InternalSearchResponse
name|internalResponse
init|=
name|searchPhaseController
operator|.
name|merge
argument_list|(
name|sortedShardList
argument_list|,
name|queryResults
argument_list|,
name|fetchResults
argument_list|)
decl_stmt|;
name|String
name|scrollId
init|=
literal|null
decl_stmt|;
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
name|scrollId
operator|=
name|TransportSearchHelper
operator|.
name|buildScrollId
argument_list|(
name|request
operator|.
name|searchType
argument_list|()
argument_list|,
name|firstResults
argument_list|)
expr_stmt|;
block|}
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|SearchResponse
argument_list|(
name|internalResponse
argument_list|,
name|scrollId
argument_list|,
name|expectedSuccessfulOps
argument_list|,
name|successfulOps
operator|.
name|get
argument_list|()
argument_list|,
name|buildTookInMillis
argument_list|()
argument_list|,
name|buildShardFailures
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|releaseIrrelevantSearchContexts
argument_list|(
name|queryResults
argument_list|,
name|docIdsToLoad
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
try|try
block|{
name|ReduceSearchPhaseException
name|failure
init|=
operator|new
name|ReduceSearchPhaseException
argument_list|(
literal|"merge"
argument_list|,
literal|""
argument_list|,
name|t
argument_list|,
name|buildShardFailures
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to reduce search"
argument_list|,
name|failure
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|onFailure
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|releaseIrrelevantSearchContexts
argument_list|(
name|queryResults
argument_list|,
name|docIdsToLoad
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

