begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search.type
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|type
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
name|ActionRunnable
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
name|ReduceSearchPhaseException
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
name|search
operator|.
name|SearchResponse
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
name|ActionFilters
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
name|ClusterService
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|EsRejectedExecutionException
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
name|SearchServiceListener
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
name|SearchServiceTransportAction
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
name|QueryFetchSearchResult
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportSearchDfsQueryAndFetchAction
specifier|public
class|class
name|TransportSearchDfsQueryAndFetchAction
extends|extends
name|TransportSearchTypeAction
block|{
annotation|@
name|Inject
DECL|method|TransportSearchDfsQueryAndFetchAction
specifier|public
name|TransportSearchDfsQueryAndFetchAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|SearchServiceTransportAction
name|searchService
parameter_list|,
name|SearchPhaseController
name|searchPhaseController
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|searchService
argument_list|,
name|searchPhaseController
argument_list|,
name|actionFilters
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|SearchRequest
name|searchRequest
parameter_list|,
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|new
name|AsyncAction
argument_list|(
name|searchRequest
argument_list|,
name|listener
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|class|AsyncAction
specifier|private
class|class
name|AsyncAction
extends|extends
name|BaseAsyncAction
argument_list|<
name|DfsSearchResult
argument_list|>
block|{
DECL|field|queryFetchResults
specifier|private
specifier|final
name|AtomicArray
argument_list|<
name|QueryFetchSearchResult
argument_list|>
name|queryFetchResults
decl_stmt|;
DECL|method|AsyncAction
specifier|private
name|AsyncAction
parameter_list|(
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
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
name|queryFetchResults
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
name|ShardSearchRequest
name|request
parameter_list|,
name|SearchServiceListener
argument_list|<
name|DfsSearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|searchService
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
name|executeSecondPhase
argument_list|(
name|entry
operator|.
name|index
argument_list|,
name|dfsResult
argument_list|,
name|counter
argument_list|,
name|node
argument_list|,
name|querySearchRequest
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|executeSecondPhase
name|void
name|executeSecondPhase
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
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|QuerySearchRequest
name|querySearchRequest
parameter_list|)
block|{
name|searchService
operator|.
name|sendExecuteFetch
argument_list|(
name|node
argument_list|,
name|querySearchRequest
argument_list|,
operator|new
name|SearchServiceListener
argument_list|<
name|QueryFetchSearchResult
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResult
parameter_list|(
name|QueryFetchSearchResult
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
name|queryFetchResults
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
name|onSecondPhaseFailure
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
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|onSecondPhaseFailure
name|void
name|onSecondPhaseFailure
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
name|boolean
name|useScroll
init|=
operator|!
name|useSlowScroll
operator|&&
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
name|queryFetchResults
argument_list|)
expr_stmt|;
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
name|queryFetchResults
argument_list|,
name|queryFetchResults
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
argument_list|,
literal|null
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
name|ReduceSearchPhaseException
name|failure
init|=
operator|new
name|ReduceSearchPhaseException
argument_list|(
literal|"query_fetch"
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
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

