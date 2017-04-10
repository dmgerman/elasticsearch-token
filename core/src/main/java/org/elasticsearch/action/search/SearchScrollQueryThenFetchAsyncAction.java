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
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|node
operator|.
name|DiscoveryNodes
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
name|CountDown
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
name|ShardFetchRequest
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
name|InternalScrollSearchRequest
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
name|ScrollQuerySearchResult
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|search
operator|.
name|TransportSearchHelper
operator|.
name|internalScrollSearchRequest
import|;
end_import

begin_class
DECL|class|SearchScrollQueryThenFetchAsyncAction
specifier|final
class|class
name|SearchScrollQueryThenFetchAsyncAction
extends|extends
name|AbstractAsyncAction
block|{
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|task
specifier|private
specifier|final
name|SearchTask
name|task
decl_stmt|;
DECL|field|searchTransportService
specifier|private
specifier|final
name|SearchTransportService
name|searchTransportService
decl_stmt|;
DECL|field|searchPhaseController
specifier|private
specifier|final
name|SearchPhaseController
name|searchPhaseController
decl_stmt|;
DECL|field|request
specifier|private
specifier|final
name|SearchScrollRequest
name|request
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
decl_stmt|;
DECL|field|scrollId
specifier|private
specifier|final
name|ParsedScrollId
name|scrollId
decl_stmt|;
DECL|field|nodes
specifier|private
specifier|final
name|DiscoveryNodes
name|nodes
decl_stmt|;
DECL|field|shardFailures
specifier|private
specifier|volatile
name|AtomicArray
argument_list|<
name|ShardSearchFailure
argument_list|>
name|shardFailures
decl_stmt|;
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
DECL|field|successfulOps
specifier|private
specifier|final
name|AtomicInteger
name|successfulOps
decl_stmt|;
DECL|method|SearchScrollQueryThenFetchAsyncAction
name|SearchScrollQueryThenFetchAsyncAction
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|SearchTransportService
name|searchTransportService
parameter_list|,
name|SearchPhaseController
name|searchPhaseController
parameter_list|,
name|SearchScrollRequest
name|request
parameter_list|,
name|SearchTask
name|task
parameter_list|,
name|ParsedScrollId
name|scrollId
parameter_list|,
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|searchTransportService
operator|=
name|searchTransportService
expr_stmt|;
name|this
operator|.
name|searchPhaseController
operator|=
name|searchPhaseController
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|task
operator|=
name|task
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|scrollId
operator|=
name|scrollId
expr_stmt|;
name|this
operator|.
name|nodes
operator|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
expr_stmt|;
name|this
operator|.
name|successfulOps
operator|=
operator|new
name|AtomicInteger
argument_list|(
name|scrollId
operator|.
name|getContext
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryResults
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|scrollId
operator|.
name|getContext
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchResults
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|scrollId
operator|.
name|getContext
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|buildShardFailures
specifier|private
name|ShardSearchFailure
index|[]
name|buildShardFailures
parameter_list|()
block|{
if|if
condition|(
name|shardFailures
operator|==
literal|null
condition|)
block|{
return|return
name|ShardSearchFailure
operator|.
name|EMPTY_ARRAY
return|;
block|}
name|List
argument_list|<
name|ShardSearchFailure
argument_list|>
name|failures
init|=
name|shardFailures
operator|.
name|asList
argument_list|()
decl_stmt|;
return|return
name|failures
operator|.
name|toArray
argument_list|(
operator|new
name|ShardSearchFailure
index|[
name|failures
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
comment|// we do our best to return the shard failures, but its ok if its not fully concurrently safe
comment|// we simply try and return as much as possible
DECL|method|addShardFailure
specifier|private
name|void
name|addShardFailure
parameter_list|(
specifier|final
name|int
name|shardIndex
parameter_list|,
name|ShardSearchFailure
name|failure
parameter_list|)
block|{
if|if
condition|(
name|shardFailures
operator|==
literal|null
condition|)
block|{
name|shardFailures
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|scrollId
operator|.
name|getContext
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
name|shardFailures
operator|.
name|set
argument_list|(
name|shardIndex
argument_list|,
name|failure
argument_list|)
expr_stmt|;
block|}
DECL|method|start
specifier|public
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
name|scrollId
operator|.
name|getContext
argument_list|()
operator|.
name|length
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
literal|"no nodes to search on"
argument_list|,
name|ShardSearchFailure
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|CountDown
name|counter
init|=
operator|new
name|CountDown
argument_list|(
name|scrollId
operator|.
name|getContext
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
name|ScrollIdForNode
index|[]
name|context
init|=
name|scrollId
operator|.
name|getContext
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
name|context
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ScrollIdForNode
name|target
init|=
name|context
index|[
name|i
index|]
decl_stmt|;
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|target
operator|.
name|getNode
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|!=
literal|null
condition|)
block|{
name|executeQueryPhase
argument_list|(
name|i
argument_list|,
name|counter
argument_list|,
name|node
argument_list|,
name|target
operator|.
name|getScrollId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
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
literal|"Node [{}] not available for scroll request [{}]"
argument_list|,
name|target
operator|.
name|getNode
argument_list|()
argument_list|,
name|scrollId
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|successfulOps
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|countDown
argument_list|()
condition|)
block|{
try|try
block|{
name|executeFetchPhase
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
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
literal|"Fetch failed"
argument_list|,
name|e
argument_list|,
name|ShardSearchFailure
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
block|}
block|}
DECL|method|executeQueryPhase
specifier|private
name|void
name|executeQueryPhase
parameter_list|(
specifier|final
name|int
name|shardIndex
parameter_list|,
specifier|final
name|CountDown
name|counter
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|long
name|searchId
parameter_list|)
block|{
name|InternalScrollSearchRequest
name|internalRequest
init|=
name|internalScrollSearchRequest
argument_list|(
name|searchId
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|searchTransportService
operator|.
name|sendExecuteScrollQuery
argument_list|(
name|node
argument_list|,
name|internalRequest
argument_list|,
name|task
argument_list|,
operator|new
name|SearchActionListener
argument_list|<
name|ScrollQuerySearchResult
argument_list|>
argument_list|(
literal|null
argument_list|,
name|shardIndex
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|setSearchShardTarget
parameter_list|(
name|ScrollQuerySearchResult
name|response
parameter_list|)
block|{
comment|// don't do this - it's part of the response...
assert|assert
name|response
operator|.
name|getSearchShardTarget
argument_list|()
operator|!=
literal|null
operator|:
literal|"search shard target must not be null"
assert|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|innerOnResponse
parameter_list|(
name|ScrollQuerySearchResult
name|result
parameter_list|)
block|{
name|queryResults
operator|.
name|setOnce
argument_list|(
name|result
operator|.
name|getShardIndex
argument_list|()
argument_list|,
name|result
operator|.
name|queryResult
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|countDown
argument_list|()
condition|)
block|{
try|try
block|{
name|executeFetchPhase
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|t
parameter_list|)
block|{
name|onQueryPhaseFailure
argument_list|(
name|shardIndex
argument_list|,
name|counter
argument_list|,
name|searchId
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|onQueryPhaseFailure
name|void
name|onQueryPhaseFailure
parameter_list|(
specifier|final
name|int
name|shardIndex
parameter_list|,
specifier|final
name|CountDown
name|counter
parameter_list|,
specifier|final
name|long
name|searchId
parameter_list|,
name|Exception
name|failure
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
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"[{}] Failed to execute query phase"
argument_list|,
name|searchId
argument_list|)
argument_list|,
name|failure
argument_list|)
expr_stmt|;
block|}
name|addShardFailure
argument_list|(
name|shardIndex
argument_list|,
operator|new
name|ShardSearchFailure
argument_list|(
name|failure
argument_list|)
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
name|countDown
argument_list|()
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
name|failure
argument_list|,
name|buildShardFailures
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|executeFetchPhase
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|addSuppressed
argument_list|(
name|failure
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|SearchPhaseExecutionException
argument_list|(
literal|"query"
argument_list|,
literal|"Fetch failed"
argument_list|,
name|e
argument_list|,
name|ShardSearchFailure
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|executeFetchPhase
specifier|private
name|void
name|executeFetchPhase
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|SearchPhaseController
operator|.
name|ReducedQueryPhase
name|reducedQueryPhase
init|=
name|searchPhaseController
operator|.
name|reducedQueryPhase
argument_list|(
name|queryResults
operator|.
name|asList
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|reducedQueryPhase
operator|.
name|scoreDocs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|finishHim
argument_list|(
name|reducedQueryPhase
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|IntArrayList
index|[]
name|docIdsToLoad
init|=
name|searchPhaseController
operator|.
name|fillDocIdsToLoad
argument_list|(
name|queryResults
operator|.
name|length
argument_list|()
argument_list|,
name|reducedQueryPhase
operator|.
name|scoreDocs
argument_list|)
decl_stmt|;
specifier|final
name|ScoreDoc
index|[]
name|lastEmittedDocPerShard
init|=
name|searchPhaseController
operator|.
name|getLastEmittedDocPerShard
argument_list|(
name|reducedQueryPhase
argument_list|,
name|queryResults
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|CountDown
name|counter
init|=
operator|new
name|CountDown
argument_list|(
name|docIdsToLoad
operator|.
name|length
argument_list|)
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
name|docIdsToLoad
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|index
init|=
name|i
decl_stmt|;
specifier|final
name|IntArrayList
name|docIds
init|=
name|docIdsToLoad
index|[
name|index
index|]
decl_stmt|;
if|if
condition|(
name|docIds
operator|!=
literal|null
condition|)
block|{
specifier|final
name|QuerySearchResult
name|querySearchResult
init|=
name|queryResults
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|ScoreDoc
name|lastEmittedDoc
init|=
name|lastEmittedDocPerShard
index|[
name|index
index|]
decl_stmt|;
name|ShardFetchRequest
name|shardFetchRequest
init|=
operator|new
name|ShardFetchRequest
argument_list|(
name|querySearchResult
operator|.
name|getRequestId
argument_list|()
argument_list|,
name|docIds
argument_list|,
name|lastEmittedDoc
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|querySearchResult
operator|.
name|getSearchShardTarget
argument_list|()
operator|.
name|getNodeId
argument_list|()
argument_list|)
decl_stmt|;
name|searchTransportService
operator|.
name|sendExecuteFetchScroll
argument_list|(
name|node
argument_list|,
name|shardFetchRequest
argument_list|,
name|task
argument_list|,
operator|new
name|SearchActionListener
argument_list|<
name|FetchSearchResult
argument_list|>
argument_list|(
name|querySearchResult
operator|.
name|getSearchShardTarget
argument_list|()
argument_list|,
name|index
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|innerOnResponse
parameter_list|(
name|FetchSearchResult
name|response
parameter_list|)
block|{
name|fetchResults
operator|.
name|setOnce
argument_list|(
name|response
operator|.
name|getShardIndex
argument_list|()
argument_list|,
name|response
argument_list|)
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|finishHim
argument_list|(
name|reducedQueryPhase
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
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
literal|"Failed to execute fetch phase"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
name|successfulOps
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|finishHim
argument_list|(
name|reducedQueryPhase
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// the counter is set to the total size of docIdsToLoad which can have null values so we have to count them down too
if|if
condition|(
name|counter
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|finishHim
argument_list|(
name|reducedQueryPhase
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|finishHim
specifier|private
name|void
name|finishHim
parameter_list|(
name|SearchPhaseController
operator|.
name|ReducedQueryPhase
name|queryPhase
parameter_list|)
block|{
try|try
block|{
specifier|final
name|InternalSearchResponse
name|internalResponse
init|=
name|searchPhaseController
operator|.
name|merge
argument_list|(
literal|true
argument_list|,
name|queryPhase
argument_list|,
name|fetchResults
operator|.
name|asList
argument_list|()
argument_list|,
name|fetchResults
operator|::
name|get
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
name|request
operator|.
name|scrollId
argument_list|()
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
name|this
operator|.
name|scrollId
operator|.
name|getContext
argument_list|()
operator|.
name|length
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
catch|catch
parameter_list|(
name|Exception
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
literal|"fetch"
argument_list|,
literal|"inner finish failed"
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
block|}
end_class

end_unit

