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
name|action
operator|.
name|NoShardAvailableActionException
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
name|TransportActions
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
name|GroupShardsIterator
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
name|ShardIterator
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
name|SearchPhaseResult
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
name|AliasFilter
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
name|Executor
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
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_class
DECL|class|AbstractSearchAsyncAction
specifier|abstract
class|class
name|AbstractSearchAsyncAction
parameter_list|<
name|FirstResult
extends|extends
name|SearchPhaseResult
parameter_list|>
extends|extends
name|AbstractAsyncAction
block|{
DECL|field|logger
specifier|protected
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|searchTransportService
specifier|protected
specifier|final
name|SearchTransportService
name|searchTransportService
decl_stmt|;
DECL|field|executor
specifier|private
specifier|final
name|Executor
name|executor
decl_stmt|;
DECL|field|listener
specifier|protected
specifier|final
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
decl_stmt|;
DECL|field|shardsIts
specifier|private
specifier|final
name|GroupShardsIterator
name|shardsIts
decl_stmt|;
DECL|field|request
specifier|protected
specifier|final
name|SearchRequest
name|request
decl_stmt|;
comment|/** Used by subclasses to resolve node ids to DiscoveryNodes. **/
DECL|field|nodeIdToDiscoveryNode
specifier|protected
specifier|final
name|Function
argument_list|<
name|String
argument_list|,
name|DiscoveryNode
argument_list|>
name|nodeIdToDiscoveryNode
decl_stmt|;
DECL|field|task
specifier|protected
specifier|final
name|SearchTask
name|task
decl_stmt|;
DECL|field|expectedSuccessfulOps
specifier|protected
specifier|final
name|int
name|expectedSuccessfulOps
decl_stmt|;
DECL|field|expectedTotalOps
specifier|private
specifier|final
name|int
name|expectedTotalOps
decl_stmt|;
DECL|field|successfulOps
specifier|protected
specifier|final
name|AtomicInteger
name|successfulOps
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|totalOps
specifier|private
specifier|final
name|AtomicInteger
name|totalOps
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|firstResults
specifier|protected
specifier|final
name|AtomicArray
argument_list|<
name|FirstResult
argument_list|>
name|firstResults
decl_stmt|;
DECL|field|aliasFilter
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|AliasFilter
argument_list|>
name|aliasFilter
decl_stmt|;
DECL|field|clusterStateVersion
specifier|private
specifier|final
name|long
name|clusterStateVersion
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
DECL|field|shardFailuresMutex
specifier|private
specifier|final
name|Object
name|shardFailuresMutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|sortedShardDocs
specifier|protected
specifier|volatile
name|ScoreDoc
index|[]
name|sortedShardDocs
decl_stmt|;
DECL|method|AbstractSearchAsyncAction
specifier|protected
name|AbstractSearchAsyncAction
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|SearchTransportService
name|searchTransportService
parameter_list|,
name|Function
argument_list|<
name|String
argument_list|,
name|DiscoveryNode
argument_list|>
name|nodeIdToDiscoveryNode
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|AliasFilter
argument_list|>
name|aliasFilter
parameter_list|,
name|Executor
name|executor
parameter_list|,
name|SearchRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|,
name|GroupShardsIterator
name|shardsIts
parameter_list|,
name|long
name|startTime
parameter_list|,
name|long
name|clusterStateVersion
parameter_list|,
name|SearchTask
name|task
parameter_list|)
block|{
name|super
argument_list|(
name|startTime
argument_list|)
expr_stmt|;
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
name|executor
operator|=
name|executor
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
name|nodeIdToDiscoveryNode
operator|=
name|nodeIdToDiscoveryNode
expr_stmt|;
name|this
operator|.
name|clusterStateVersion
operator|=
name|clusterStateVersion
expr_stmt|;
name|this
operator|.
name|shardsIts
operator|=
name|shardsIts
expr_stmt|;
name|expectedSuccessfulOps
operator|=
name|shardsIts
operator|.
name|size
argument_list|()
expr_stmt|;
comment|// we need to add 1 for non active partition, since we count it in the total!
name|expectedTotalOps
operator|=
name|shardsIts
operator|.
name|totalSizeWith1ForEmpty
argument_list|()
expr_stmt|;
name|firstResults
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|shardsIts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|aliasFilter
operator|=
name|aliasFilter
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
name|expectedSuccessfulOps
operator|==
literal|0
condition|)
block|{
comment|//no search shards to search on, bail with empty response
comment|//(it happens with search across _all with no indices around and consistent with broadcast operations)
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|SearchResponse
argument_list|(
name|InternalSearchResponse
operator|.
name|empty
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|buildTookInMillis
argument_list|()
argument_list|,
name|ShardSearchFailure
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|int
name|shardIndex
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
specifier|final
name|ShardIterator
name|shardIt
range|:
name|shardsIts
control|)
block|{
name|shardIndex
operator|++
expr_stmt|;
specifier|final
name|ShardRouting
name|shard
init|=
name|shardIt
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|performFirstPhase
argument_list|(
name|shardIndex
argument_list|,
name|shardIt
argument_list|,
name|shard
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// really, no shards active in this group
name|onFirstPhaseResult
argument_list|(
name|shardIndex
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|shardIt
argument_list|,
operator|new
name|NoShardAvailableActionException
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|performFirstPhase
name|void
name|performFirstPhase
parameter_list|(
specifier|final
name|int
name|shardIndex
parameter_list|,
specifier|final
name|ShardIterator
name|shardIt
parameter_list|,
specifier|final
name|ShardRouting
name|shard
parameter_list|)
block|{
if|if
condition|(
name|shard
operator|==
literal|null
condition|)
block|{
comment|// no more active shards... (we should not really get here, but just for safety)
name|onFirstPhaseResult
argument_list|(
name|shardIndex
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|shardIt
argument_list|,
operator|new
name|NoShardAvailableActionException
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|DiscoveryNode
name|node
init|=
name|nodeIdToDiscoveryNode
operator|.
name|apply
argument_list|(
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
name|onFirstPhaseResult
argument_list|(
name|shardIndex
argument_list|,
name|shard
argument_list|,
literal|null
argument_list|,
name|shardIt
argument_list|,
operator|new
name|NoShardAvailableActionException
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|AliasFilter
name|filter
init|=
name|this
operator|.
name|aliasFilter
operator|.
name|get
argument_list|(
name|shard
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|ShardSearchTransportRequest
name|transportRequest
init|=
operator|new
name|ShardSearchTransportRequest
argument_list|(
name|request
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
name|shardsIts
operator|.
name|size
argument_list|()
argument_list|,
name|filter
argument_list|,
name|startTime
argument_list|()
argument_list|)
decl_stmt|;
name|sendExecuteFirstPhase
argument_list|(
name|node
argument_list|,
name|transportRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|FirstResult
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|FirstResult
name|result
parameter_list|)
block|{
name|onFirstPhaseResult
argument_list|(
name|shardIndex
argument_list|,
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|result
argument_list|,
name|shardIt
argument_list|)
expr_stmt|;
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
name|onFirstPhaseResult
argument_list|(
name|shardIndex
argument_list|,
name|shard
argument_list|,
name|node
operator|.
name|getId
argument_list|()
argument_list|,
name|shardIt
argument_list|,
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
DECL|method|onFirstPhaseResult
specifier|private
name|void
name|onFirstPhaseResult
parameter_list|(
name|int
name|shardIndex
parameter_list|,
name|String
name|nodeId
parameter_list|,
name|FirstResult
name|result
parameter_list|,
name|ShardIterator
name|shardIt
parameter_list|)
block|{
name|result
operator|.
name|shardTarget
argument_list|(
operator|new
name|SearchShardTarget
argument_list|(
name|nodeId
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|processFirstPhaseResult
argument_list|(
name|shardIndex
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// we need to increment successful ops first before we compare the exit condition otherwise if we
comment|// are fast we could concurrently update totalOps but then preempt one of the threads which can
comment|// cause the successor to read a wrong value from successfulOps if second phase is very fast ie. count etc.
name|successfulOps
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
comment|// increment all the "future" shards to update the total ops since we some may work and some may not...
comment|// and when that happens, we break on total ops, so we must maintain them
specifier|final
name|int
name|xTotalOps
init|=
name|totalOps
operator|.
name|addAndGet
argument_list|(
name|shardIt
operator|.
name|remaining
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|xTotalOps
operator|==
name|expectedTotalOps
condition|)
block|{
try|try
block|{
name|innerMoveToSecondPhase
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
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
literal|"{}: Failed to execute [{}] while moving to second phase"
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|raiseEarlyFailure
argument_list|(
operator|new
name|ReduceSearchPhaseException
argument_list|(
name|firstPhaseName
argument_list|()
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
elseif|else
if|if
condition|(
name|xTotalOps
operator|>
name|expectedTotalOps
condition|)
block|{
name|raiseEarlyFailure
argument_list|(
operator|new
name|IllegalStateException
argument_list|(
literal|"unexpected higher total ops ["
operator|+
name|xTotalOps
operator|+
literal|"] compared "
operator|+
literal|"to expected ["
operator|+
name|expectedTotalOps
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|onFirstPhaseResult
specifier|private
name|void
name|onFirstPhaseResult
parameter_list|(
specifier|final
name|int
name|shardIndex
parameter_list|,
annotation|@
name|Nullable
name|ShardRouting
name|shard
parameter_list|,
annotation|@
name|Nullable
name|String
name|nodeId
parameter_list|,
specifier|final
name|ShardIterator
name|shardIt
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
comment|// we always add the shard failure for a specific shard instance
comment|// we do make sure to clean it on a successful response from a shard
name|SearchShardTarget
name|shardTarget
init|=
operator|new
name|SearchShardTarget
argument_list|(
name|nodeId
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
name|addShardFailure
argument_list|(
name|shardIndex
argument_list|,
name|shardTarget
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|totalOps
operator|.
name|incrementAndGet
argument_list|()
operator|==
name|expectedTotalOps
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|e
operator|!=
literal|null
operator|&&
operator|!
name|TransportActions
operator|.
name|isShardNotAvailableException
argument_list|(
name|e
argument_list|)
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
literal|"{}: Failed to execute [{}]"
argument_list|,
name|shard
operator|!=
literal|null
condition|?
name|shard
operator|.
name|shortSummary
argument_list|()
else|:
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
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
literal|"{}: Failed to execute [{}]"
argument_list|,
name|shard
argument_list|,
name|request
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|ShardSearchFailure
index|[]
name|shardSearchFailures
init|=
name|buildShardFailures
argument_list|()
decl_stmt|;
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
literal|"All shards failed for phase: [{}]"
argument_list|,
name|firstPhaseName
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// no successful ops, raise an exception
name|raiseEarlyFailure
argument_list|(
operator|new
name|SearchPhaseExecutionException
argument_list|(
name|firstPhaseName
argument_list|()
argument_list|,
literal|"all shards failed"
argument_list|,
name|e
argument_list|,
name|shardSearchFailures
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|innerMoveToSecondPhase
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|inner
parameter_list|)
block|{
name|inner
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|raiseEarlyFailure
argument_list|(
operator|new
name|ReduceSearchPhaseException
argument_list|(
name|firstPhaseName
argument_list|()
argument_list|,
literal|""
argument_list|,
name|inner
argument_list|,
name|shardSearchFailures
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
specifier|final
name|ShardRouting
name|nextShard
init|=
name|shardIt
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|lastShard
init|=
name|nextShard
operator|==
literal|null
decl_stmt|;
comment|// trace log this exception
name|logger
operator|.
name|trace
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
literal|"{}: Failed to execute [{}] lastShard [{}]"
argument_list|,
name|shard
operator|!=
literal|null
condition|?
name|shard
operator|.
name|shortSummary
argument_list|()
else|:
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
argument_list|,
name|lastShard
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|lastShard
condition|)
block|{
try|try
block|{
name|performFirstPhase
argument_list|(
name|shardIndex
argument_list|,
name|shardIt
argument_list|,
name|nextShard
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|inner
parameter_list|)
block|{
name|inner
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|onFirstPhaseResult
argument_list|(
name|shardIndex
argument_list|,
name|shard
argument_list|,
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|shardIt
argument_list|,
name|inner
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// no more shards active, add a failure
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
operator|&&
operator|!
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
comment|// do not double log this exception
if|if
condition|(
name|e
operator|!=
literal|null
operator|&&
operator|!
name|TransportActions
operator|.
name|isShardNotAvailableException
argument_list|(
name|e
argument_list|)
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
literal|"{}: Failed to execute [{}] lastShard [{}]"
argument_list|,
name|shard
operator|!=
literal|null
condition|?
name|shard
operator|.
name|shortSummary
argument_list|()
else|:
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
argument_list|,
name|lastShard
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|buildShardFailures
specifier|protected
specifier|final
name|ShardSearchFailure
index|[]
name|buildShardFailures
parameter_list|()
block|{
name|AtomicArray
argument_list|<
name|ShardSearchFailure
argument_list|>
name|shardFailures
init|=
name|this
operator|.
name|shardFailures
decl_stmt|;
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
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|ShardSearchFailure
argument_list|>
argument_list|>
name|entries
init|=
name|shardFailures
operator|.
name|asList
argument_list|()
decl_stmt|;
name|ShardSearchFailure
index|[]
name|failures
init|=
operator|new
name|ShardSearchFailure
index|[
name|entries
operator|.
name|size
argument_list|()
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
name|failures
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|failures
index|[
name|i
index|]
operator|=
name|entries
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|value
expr_stmt|;
block|}
return|return
name|failures
return|;
block|}
DECL|method|addShardFailure
specifier|protected
specifier|final
name|void
name|addShardFailure
parameter_list|(
specifier|final
name|int
name|shardIndex
parameter_list|,
annotation|@
name|Nullable
name|SearchShardTarget
name|shardTarget
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
comment|// we don't aggregate shard failures on non active shards (but do keep the header counts right)
if|if
condition|(
name|TransportActions
operator|.
name|isShardNotAvailableException
argument_list|(
name|e
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// lazily create shard failures, so we can early build the empty shard failure list in most cases (no failures)
if|if
condition|(
name|shardFailures
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|shardFailuresMutex
init|)
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
name|shardsIts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|ShardSearchFailure
name|failure
init|=
name|shardFailures
operator|.
name|get
argument_list|(
name|shardIndex
argument_list|)
decl_stmt|;
if|if
condition|(
name|failure
operator|==
literal|null
condition|)
block|{
name|shardFailures
operator|.
name|set
argument_list|(
name|shardIndex
argument_list|,
operator|new
name|ShardSearchFailure
argument_list|(
name|e
argument_list|,
name|shardTarget
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// the failure is already present, try and not override it with an exception that is less meaningless
comment|// for example, getting illegal shard state
if|if
condition|(
name|TransportActions
operator|.
name|isReadOverrideException
argument_list|(
name|e
argument_list|)
condition|)
block|{
name|shardFailures
operator|.
name|set
argument_list|(
name|shardIndex
argument_list|,
operator|new
name|ShardSearchFailure
argument_list|(
name|e
argument_list|,
name|shardTarget
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|raiseEarlyFailure
specifier|private
name|void
name|raiseEarlyFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
for|for
control|(
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|FirstResult
argument_list|>
name|entry
range|:
name|firstResults
operator|.
name|asList
argument_list|()
control|)
block|{
try|try
block|{
name|DiscoveryNode
name|node
init|=
name|nodeIdToDiscoveryNode
operator|.
name|apply
argument_list|(
name|entry
operator|.
name|value
operator|.
name|shardTarget
argument_list|()
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
name|sendReleaseSearchContext
argument_list|(
name|entry
operator|.
name|value
operator|.
name|id
argument_list|()
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|inner
parameter_list|)
block|{
name|inner
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"failed to release context"
argument_list|,
name|inner
argument_list|)
expr_stmt|;
block|}
block|}
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**      * Releases shard targets that are not used in the docsIdsToLoad.      */
DECL|method|releaseIrrelevantSearchContexts
specifier|protected
name|void
name|releaseIrrelevantSearchContexts
parameter_list|(
name|AtomicArray
argument_list|<
name|?
extends|extends
name|QuerySearchResultProvider
argument_list|>
name|queryResults
parameter_list|,
name|AtomicArray
argument_list|<
name|IntArrayList
argument_list|>
name|docIdsToLoad
parameter_list|)
block|{
if|if
condition|(
name|docIdsToLoad
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// we only release search context that we did not fetch from if we are not scrolling
if|if
condition|(
name|request
operator|.
name|scroll
argument_list|()
operator|==
literal|null
condition|)
block|{
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
operator|.
name|asList
argument_list|()
control|)
block|{
name|QuerySearchResult
name|queryResult
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
name|queryResult
operator|.
name|hasHits
argument_list|()
operator|&&
name|docIdsToLoad
operator|.
name|get
argument_list|(
name|entry
operator|.
name|index
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// but none of them made it to the global top docs
try|try
block|{
name|DiscoveryNode
name|node
init|=
name|nodeIdToDiscoveryNode
operator|.
name|apply
argument_list|(
name|entry
operator|.
name|value
operator|.
name|queryResult
argument_list|()
operator|.
name|shardTarget
argument_list|()
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
name|sendReleaseSearchContext
argument_list|(
name|entry
operator|.
name|value
operator|.
name|queryResult
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"failed to release context"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|sendReleaseSearchContext
specifier|protected
name|void
name|sendReleaseSearchContext
parameter_list|(
name|long
name|contextId
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|!=
literal|null
condition|)
block|{
name|searchTransportService
operator|.
name|sendFreeContext
argument_list|(
name|node
argument_list|,
name|contextId
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|createFetchRequest
specifier|protected
name|ShardFetchSearchRequest
name|createFetchRequest
parameter_list|(
name|QuerySearchResult
name|queryResult
parameter_list|,
name|AtomicArray
operator|.
name|Entry
argument_list|<
name|IntArrayList
argument_list|>
name|entry
parameter_list|,
name|ScoreDoc
index|[]
name|lastEmittedDocPerShard
parameter_list|)
block|{
specifier|final
name|ScoreDoc
name|lastEmittedDoc
init|=
operator|(
name|lastEmittedDocPerShard
operator|!=
literal|null
operator|)
condition|?
name|lastEmittedDocPerShard
index|[
name|entry
operator|.
name|index
index|]
else|:
literal|null
decl_stmt|;
return|return
operator|new
name|ShardFetchSearchRequest
argument_list|(
name|request
argument_list|,
name|queryResult
operator|.
name|id
argument_list|()
argument_list|,
name|entry
operator|.
name|value
argument_list|,
name|lastEmittedDoc
argument_list|)
return|;
block|}
DECL|method|sendExecuteFirstPhase
specifier|protected
specifier|abstract
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
name|FirstResult
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|processFirstPhaseResult
specifier|protected
specifier|final
name|void
name|processFirstPhaseResult
parameter_list|(
name|int
name|shardIndex
parameter_list|,
name|FirstResult
name|result
parameter_list|)
block|{
name|firstResults
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
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"got first-phase result from {}"
argument_list|,
name|result
operator|!=
literal|null
condition|?
name|result
operator|.
name|shardTarget
argument_list|()
else|:
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// clean a previous error on this shard group (note, this code will be serialized on the same shardIndex value level
comment|// so its ok concurrency wise to miss potentially the shard failures being created because of another failure
comment|// in the #addShardFailure, because by definition, it will happen on *another* shardIndex
name|AtomicArray
argument_list|<
name|ShardSearchFailure
argument_list|>
name|shardFailures
init|=
name|this
operator|.
name|shardFailures
decl_stmt|;
if|if
condition|(
name|shardFailures
operator|!=
literal|null
condition|)
block|{
name|shardFailures
operator|.
name|set
argument_list|(
name|shardIndex
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|innerMoveToSecondPhase
specifier|final
name|void
name|innerMoveToSecondPhase
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|boolean
name|hadOne
init|=
literal|false
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
name|firstResults
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|FirstResult
name|result
init|=
name|firstResults
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
continue|continue;
comment|// failure
block|}
if|if
condition|(
name|hadOne
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hadOne
operator|=
literal|true
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|result
operator|.
name|shardTarget
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"Moving to second phase, based on results from: {} (cluster state version: {})"
argument_list|,
name|sb
argument_list|,
name|clusterStateVersion
argument_list|)
expr_stmt|;
block|}
name|moveToSecondPhase
argument_list|()
expr_stmt|;
block|}
DECL|method|moveToSecondPhase
specifier|protected
specifier|abstract
name|void
name|moveToSecondPhase
parameter_list|()
throws|throws
name|Exception
function_decl|;
DECL|method|firstPhaseName
specifier|protected
specifier|abstract
name|String
name|firstPhaseName
parameter_list|()
function_decl|;
DECL|method|getExecutor
specifier|protected
name|Executor
name|getExecutor
parameter_list|()
block|{
return|return
name|executor
return|;
block|}
block|}
end_class

end_unit

