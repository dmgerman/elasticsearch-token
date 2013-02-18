begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|cluster
operator|.
name|routing
operator|.
name|ShardRouting
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
name|trove
operator|.
name|ExtTIntArrayList
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
name|fetch
operator|.
name|FetchSearchRequest
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportSearchQueryThenFetchAction
specifier|public
class|class
name|TransportSearchQueryThenFetchAction
extends|extends
name|TransportSearchTypeAction
block|{
annotation|@
name|Inject
DECL|method|TransportSearchQueryThenFetchAction
specifier|public
name|TransportSearchQueryThenFetchAction
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
name|TransportSearchCache
name|transportSearchCache
parameter_list|,
name|SearchServiceTransportAction
name|searchService
parameter_list|,
name|SearchPhaseController
name|searchPhaseController
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
name|transportSearchCache
argument_list|,
name|searchService
argument_list|,
name|searchPhaseController
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
name|QuerySearchResult
argument_list|>
block|{
DECL|field|queryResults
specifier|private
specifier|final
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QuerySearchResultProvider
argument_list|>
name|queryResults
init|=
name|searchCache
operator|.
name|obtainQueryResults
argument_list|()
decl_stmt|;
DECL|field|fetchResults
specifier|private
specifier|final
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|FetchSearchResult
argument_list|>
name|fetchResults
init|=
name|searchCache
operator|.
name|obtainFetchResults
argument_list|()
decl_stmt|;
DECL|field|docIdsToLoad
specifier|private
specifier|volatile
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|ExtTIntArrayList
argument_list|>
name|docIdsToLoad
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
literal|"query"
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
name|QuerySearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|searchService
operator|.
name|sendExecuteQuery
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
DECL|method|processFirstPhaseResult
specifier|protected
name|void
name|processFirstPhaseResult
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
name|QuerySearchResult
name|result
parameter_list|)
block|{
name|queryResults
operator|.
name|put
argument_list|(
name|result
operator|.
name|shardTarget
argument_list|()
argument_list|,
name|result
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
name|sortedShardList
operator|=
name|searchPhaseController
operator|.
name|sortDocs
argument_list|(
name|queryResults
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|ExtTIntArrayList
argument_list|>
name|docIdsToLoad
init|=
name|searchPhaseController
operator|.
name|docIdsToLoad
argument_list|(
name|sortedShardList
argument_list|)
decl_stmt|;
name|this
operator|.
name|docIdsToLoad
operator|=
name|docIdsToLoad
expr_stmt|;
if|if
condition|(
name|docIdsToLoad
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
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|docIdsToLoad
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|localOperations
init|=
literal|0
decl_stmt|;
for|for
control|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|SearchShardTarget
argument_list|,
name|ExtTIntArrayList
argument_list|>
name|entry
range|:
name|docIdsToLoad
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|nodes
operator|.
name|localNodeId
argument_list|()
argument_list|)
condition|)
block|{
name|localOperations
operator|++
expr_stmt|;
block|}
else|else
block|{
name|FetchSearchRequest
name|fetchSearchRequest
init|=
operator|new
name|FetchSearchRequest
argument_list|(
name|request
argument_list|,
name|queryResults
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|executeFetch
argument_list|(
name|entry
operator|.
name|getKey
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
if|if
condition|(
name|localOperations
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|getOperationThreading
argument_list|()
operator|==
name|SearchOperationThreading
operator|.
name|SINGLE_THREAD
condition|)
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
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
for|for
control|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|SearchShardTarget
argument_list|,
name|ExtTIntArrayList
argument_list|>
name|entry
range|:
name|docIdsToLoad
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|nodes
operator|.
name|localNodeId
argument_list|()
argument_list|)
condition|)
block|{
name|FetchSearchRequest
name|fetchSearchRequest
init|=
operator|new
name|FetchSearchRequest
argument_list|(
name|request
argument_list|,
name|queryResults
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|executeFetch
argument_list|(
name|entry
operator|.
name|getKey
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
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|boolean
name|localAsync
init|=
name|request
operator|.
name|getOperationThreading
argument_list|()
operator|==
name|SearchOperationThreading
operator|.
name|THREAD_PER_SHARD
decl_stmt|;
for|for
control|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|SearchShardTarget
argument_list|,
name|ExtTIntArrayList
argument_list|>
name|entry
range|:
name|docIdsToLoad
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|nodes
operator|.
name|localNodeId
argument_list|()
argument_list|)
condition|)
block|{
specifier|final
name|FetchSearchRequest
name|fetchSearchRequest
init|=
operator|new
name|FetchSearchRequest
argument_list|(
name|request
argument_list|,
name|queryResults
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|localAsync
condition|)
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
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|executeFetch
argument_list|(
name|entry
operator|.
name|getKey
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
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|executeFetch
argument_list|(
name|entry
operator|.
name|getKey
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
block|}
block|}
block|}
block|}
DECL|method|executeFetch
name|void
name|executeFetch
parameter_list|(
specifier|final
name|SearchShardTarget
name|shardTarget
parameter_list|,
specifier|final
name|AtomicInteger
name|counter
parameter_list|,
specifier|final
name|FetchSearchRequest
name|fetchSearchRequest
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|searchService
operator|.
name|sendExecuteFetch
argument_list|(
name|node
argument_list|,
name|fetchSearchRequest
argument_list|,
operator|new
name|SearchServiceListener
argument_list|<
name|FetchSearchResult
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResult
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
name|put
argument_list|(
name|result
operator|.
name|shardTarget
argument_list|()
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
name|AsyncAction
operator|.
name|this
operator|.
name|addShardFailure
argument_list|(
operator|new
name|ShardSearchFailure
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
name|successulOps
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
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|finishHim
name|void
name|finishHim
parameter_list|()
block|{
try|try
block|{
name|innerFinishHim
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|ReduceSearchPhaseException
name|failure
init|=
operator|new
name|ReduceSearchPhaseException
argument_list|(
literal|"fetch"
argument_list|,
literal|""
argument_list|,
name|e
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
name|listener
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
name|searchCache
operator|.
name|releaseQueryResults
argument_list|(
name|queryResults
argument_list|)
expr_stmt|;
name|searchCache
operator|.
name|releaseFetchResults
argument_list|(
name|fetchResults
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|innerFinishHim
name|void
name|innerFinishHim
parameter_list|()
throws|throws
name|Exception
block|{
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
name|getScroll
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
name|getSearchType
argument_list|()
argument_list|,
name|queryResults
operator|.
name|values
argument_list|()
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
name|successulOps
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
block|}
block|}
end_class

end_unit

