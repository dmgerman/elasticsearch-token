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
name|util
operator|.
name|SetOnce
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

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
name|ShardOperationFailedException
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
name|transport
operator|.
name|Transport
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
name|TimeUnit
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_class
DECL|class|AbstractSearchAsyncAction
specifier|abstract
class|class
name|AbstractSearchAsyncAction
parameter_list|<
name|Result
extends|extends
name|SearchPhaseResult
parameter_list|>
extends|extends
name|InitialSearchPhase
argument_list|<
name|Result
argument_list|>
implements|implements
name|SearchPhaseContext
block|{
DECL|field|DEFAULT_INDEX_BOOST
specifier|private
specifier|static
specifier|final
name|float
name|DEFAULT_INDEX_BOOST
init|=
literal|1.0f
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|searchTransportService
specifier|private
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
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
decl_stmt|;
DECL|field|request
specifier|private
specifier|final
name|SearchRequest
name|request
decl_stmt|;
comment|/**      * Used by subclasses to resolve node ids to DiscoveryNodes.      **/
DECL|field|nodeIdToConnection
specifier|private
specifier|final
name|Function
argument_list|<
name|String
argument_list|,
name|Transport
operator|.
name|Connection
argument_list|>
name|nodeIdToConnection
decl_stmt|;
DECL|field|task
specifier|private
specifier|final
name|SearchTask
name|task
decl_stmt|;
DECL|field|results
specifier|private
specifier|final
name|SearchPhaseResults
argument_list|<
name|Result
argument_list|>
name|results
decl_stmt|;
DECL|field|clusterStateVersion
specifier|private
specifier|final
name|long
name|clusterStateVersion
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
DECL|field|concreteIndexBoosts
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|concreteIndexBoosts
decl_stmt|;
DECL|field|shardFailures
specifier|private
specifier|final
name|SetOnce
argument_list|<
name|AtomicArray
argument_list|<
name|ShardSearchFailure
argument_list|>
argument_list|>
name|shardFailures
init|=
operator|new
name|SetOnce
argument_list|<>
argument_list|()
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
DECL|field|successfulOps
specifier|private
specifier|final
name|AtomicInteger
name|successfulOps
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|timeProvider
specifier|private
specifier|final
name|TransportSearchAction
operator|.
name|SearchTimeProvider
name|timeProvider
decl_stmt|;
DECL|method|AbstractSearchAsyncAction
specifier|protected
name|AbstractSearchAsyncAction
parameter_list|(
name|String
name|name
parameter_list|,
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
name|Transport
operator|.
name|Connection
argument_list|>
name|nodeIdToConnection
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|AliasFilter
argument_list|>
name|aliasFilter
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|concreteIndexBoosts
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
argument_list|<
name|SearchShardIterator
argument_list|>
name|shardsIts
parameter_list|,
name|TransportSearchAction
operator|.
name|SearchTimeProvider
name|timeProvider
parameter_list|,
name|long
name|clusterStateVersion
parameter_list|,
name|SearchTask
name|task
parameter_list|,
name|SearchPhaseResults
argument_list|<
name|Result
argument_list|>
name|resultConsumer
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|request
argument_list|,
name|shardsIts
argument_list|,
name|logger
argument_list|)
expr_stmt|;
name|this
operator|.
name|timeProvider
operator|=
name|timeProvider
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
name|nodeIdToConnection
operator|=
name|nodeIdToConnection
expr_stmt|;
name|this
operator|.
name|clusterStateVersion
operator|=
name|clusterStateVersion
expr_stmt|;
name|this
operator|.
name|concreteIndexBoosts
operator|=
name|concreteIndexBoosts
expr_stmt|;
name|this
operator|.
name|aliasFilter
operator|=
name|aliasFilter
expr_stmt|;
name|this
operator|.
name|results
operator|=
name|resultConsumer
expr_stmt|;
block|}
comment|/**      * Builds how long it took to execute the search.      */
DECL|method|buildTookInMillis
name|long
name|buildTookInMillis
parameter_list|()
block|{
return|return
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|timeProvider
operator|.
name|getRelativeCurrentNanos
argument_list|()
operator|-
name|timeProvider
operator|.
name|getRelativeStartNanos
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * This is the main entry point for a search. This method starts the search execution of the initial phase.      */
DECL|method|start
specifier|public
specifier|final
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
name|getNumShards
argument_list|()
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
name|executePhase
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executeNextPhase
specifier|public
specifier|final
name|void
name|executeNextPhase
parameter_list|(
name|SearchPhase
name|currentPhase
parameter_list|,
name|SearchPhase
name|nextPhase
parameter_list|)
block|{
comment|/* This is the main search phase transition where we move to the next phase. At this point we check if there is          * at least one successful operation left and if so we move to the next phase. If not we immediately fail the          * search phase as "all shards failed"*/
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
comment|// we have 0 successful results that means we shortcut stuff and return a failure
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
specifier|final
name|ShardOperationFailedException
index|[]
name|shardSearchFailures
init|=
name|ExceptionsHelper
operator|.
name|groupBy
argument_list|(
name|buildShardFailures
argument_list|()
argument_list|)
decl_stmt|;
name|Throwable
name|cause
init|=
name|shardSearchFailures
operator|.
name|length
operator|==
literal|0
condition|?
literal|null
else|:
name|ElasticsearchException
operator|.
name|guessRootCauses
argument_list|(
name|shardSearchFailures
index|[
literal|0
index|]
operator|.
name|getCause
argument_list|()
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
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
name|getName
argument_list|()
argument_list|)
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
name|onPhaseFailure
argument_list|(
name|currentPhase
argument_list|,
literal|"all shards failed"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
specifier|final
name|String
name|resultsFrom
init|=
name|results
operator|.
name|getSuccessfulResults
argument_list|()
operator|.
name|map
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getSearchShardTarget
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|","
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}] Moving to next phase: [{}], based on results from: {} (cluster state version: {})"
argument_list|,
name|currentPhase
operator|.
name|getName
argument_list|()
argument_list|,
name|nextPhase
operator|.
name|getName
argument_list|()
argument_list|,
name|resultsFrom
argument_list|,
name|clusterStateVersion
argument_list|)
expr_stmt|;
block|}
name|executePhase
argument_list|(
name|nextPhase
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|executePhase
specifier|private
name|void
name|executePhase
parameter_list|(
name|SearchPhase
name|phase
parameter_list|)
block|{
try|try
block|{
name|phase
operator|.
name|run
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
literal|"Failed to execute [{}] while moving to [{}] phase"
argument_list|,
name|request
argument_list|,
name|phase
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|onPhaseFailure
argument_list|(
name|phase
argument_list|,
literal|""
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|buildShardFailures
specifier|private
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
operator|.
name|get
argument_list|()
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
name|ShardSearchFailure
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
expr_stmt|;
block|}
return|return
name|failures
return|;
block|}
DECL|method|onShardFailure
specifier|public
specifier|final
name|void
name|onShardFailure
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
name|AtomicArray
argument_list|<
name|ShardSearchFailure
argument_list|>
name|shardFailures
init|=
name|this
operator|.
name|shardFailures
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// lazily create shard failures, so we can early build the empty shard failure list in most cases (no failures)
if|if
condition|(
name|shardFailures
operator|==
literal|null
condition|)
block|{
comment|// this is double checked locking but it's fine since SetOnce uses a volatile read internally
synchronized|synchronized
init|(
name|shardFailuresMutex
init|)
block|{
name|shardFailures
operator|=
name|this
operator|.
name|shardFailures
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// read again otherwise somebody else has created it?
if|if
condition|(
name|shardFailures
operator|==
literal|null
condition|)
block|{
comment|// still null so we are the first and create a new instance
name|shardFailures
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|getNumShards
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardFailures
operator|.
name|set
argument_list|(
name|shardFailures
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
if|if
condition|(
name|results
operator|.
name|hasResult
argument_list|(
name|shardIndex
argument_list|)
condition|)
block|{
assert|assert
name|failure
operator|==
literal|null
operator|:
literal|"shard failed before but shouldn't: "
operator|+
name|failure
assert|;
name|successfulOps
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
comment|// if this shard was successful before (initial phase) we have to adjust the counter
block|}
block|}
comment|/**      * This method should be called if a search phase failed to ensure all relevant search contexts and resources are released.      * this method will also notify the listener and sends back a failure to the user.      *      * @param exception the exception explaining or causing the phase failure      */
DECL|method|raisePhaseFailure
specifier|private
name|void
name|raisePhaseFailure
parameter_list|(
name|SearchPhaseExecutionException
name|exception
parameter_list|)
block|{
name|results
operator|.
name|getSuccessfulResults
argument_list|()
operator|.
name|forEach
argument_list|(
parameter_list|(
name|entry
parameter_list|)
lambda|->
block|{
try|try
block|{
name|SearchShardTarget
name|searchShardTarget
init|=
name|entry
operator|.
name|getSearchShardTarget
argument_list|()
decl_stmt|;
name|Transport
operator|.
name|Connection
name|connection
init|=
name|nodeIdToConnection
operator|.
name|apply
argument_list|(
name|searchShardTarget
operator|.
name|getNodeId
argument_list|()
argument_list|)
decl_stmt|;
name|sendReleaseSearchContext
argument_list|(
name|entry
operator|.
name|getRequestId
argument_list|()
argument_list|,
name|connection
argument_list|,
name|searchShardTarget
operator|.
name|getOriginalIndices
argument_list|()
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
name|exception
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
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onShardSuccess
specifier|public
specifier|final
name|void
name|onShardSuccess
parameter_list|(
name|Result
name|result
parameter_list|)
block|{
name|successfulOps
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|results
operator|.
name|consumeResult
argument_list|(
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
name|getSearchShardTarget
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
operator|.
name|get
argument_list|()
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
name|result
operator|.
name|getShardIndex
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|onPhaseDone
specifier|public
specifier|final
name|void
name|onPhaseDone
parameter_list|()
block|{
name|executeNextPhase
argument_list|(
name|this
argument_list|,
name|getNextPhase
argument_list|(
name|results
argument_list|,
name|this
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getNumShards
specifier|public
specifier|final
name|int
name|getNumShards
parameter_list|()
block|{
return|return
name|results
operator|.
name|getNumShards
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getLogger
specifier|public
specifier|final
name|Logger
name|getLogger
parameter_list|()
block|{
return|return
name|logger
return|;
block|}
annotation|@
name|Override
DECL|method|getTask
specifier|public
specifier|final
name|SearchTask
name|getTask
parameter_list|()
block|{
return|return
name|task
return|;
block|}
annotation|@
name|Override
DECL|method|getRequest
specifier|public
specifier|final
name|SearchRequest
name|getRequest
parameter_list|()
block|{
return|return
name|request
return|;
block|}
annotation|@
name|Override
DECL|method|buildSearchResponse
specifier|public
specifier|final
name|SearchResponse
name|buildSearchResponse
parameter_list|(
name|InternalSearchResponse
name|internalSearchResponse
parameter_list|,
name|String
name|scrollId
parameter_list|)
block|{
return|return
operator|new
name|SearchResponse
argument_list|(
name|internalSearchResponse
argument_list|,
name|scrollId
argument_list|,
name|getNumShards
argument_list|()
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
return|;
block|}
annotation|@
name|Override
DECL|method|onPhaseFailure
specifier|public
specifier|final
name|void
name|onPhaseFailure
parameter_list|(
name|SearchPhase
name|phase
parameter_list|,
name|String
name|msg
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|raisePhaseFailure
argument_list|(
operator|new
name|SearchPhaseExecutionException
argument_list|(
name|phase
operator|.
name|getName
argument_list|()
argument_list|,
name|msg
argument_list|,
name|cause
argument_list|,
name|buildShardFailures
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getConnection
specifier|public
specifier|final
name|Transport
operator|.
name|Connection
name|getConnection
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
return|return
name|nodeIdToConnection
operator|.
name|apply
argument_list|(
name|nodeId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getSearchTransport
specifier|public
specifier|final
name|SearchTransportService
name|getSearchTransport
parameter_list|()
block|{
return|return
name|searchTransportService
return|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
specifier|final
name|void
name|execute
parameter_list|(
name|Runnable
name|command
parameter_list|)
block|{
name|executor
operator|.
name|execute
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onResponse
specifier|public
specifier|final
name|void
name|onResponse
parameter_list|(
name|SearchResponse
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
specifier|final
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
DECL|method|buildShardSearchRequest
specifier|public
specifier|final
name|ShardSearchTransportRequest
name|buildShardSearchRequest
parameter_list|(
name|SearchShardIterator
name|shardIt
parameter_list|,
name|ShardRouting
name|shard
parameter_list|)
block|{
name|AliasFilter
name|filter
init|=
name|aliasFilter
operator|.
name|get
argument_list|(
name|shard
operator|.
name|index
argument_list|()
operator|.
name|getUUID
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
name|filter
operator|!=
literal|null
assert|;
name|float
name|indexBoost
init|=
name|concreteIndexBoosts
operator|.
name|getOrDefault
argument_list|(
name|shard
operator|.
name|index
argument_list|()
operator|.
name|getUUID
argument_list|()
argument_list|,
name|DEFAULT_INDEX_BOOST
argument_list|)
decl_stmt|;
return|return
operator|new
name|ShardSearchTransportRequest
argument_list|(
name|shardIt
operator|.
name|getOriginalIndices
argument_list|()
argument_list|,
name|request
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
name|getNumShards
argument_list|()
argument_list|,
name|filter
argument_list|,
name|indexBoost
argument_list|,
name|timeProvider
operator|.
name|getAbsoluteStartMillis
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Returns the next phase based on the results of the initial search phase      * @param results the results of the initial search phase. Each non null element in the result array represent a successfully      *                executed shard request      * @param context the search context for the next phase      */
DECL|method|getNextPhase
specifier|protected
specifier|abstract
name|SearchPhase
name|getNextPhase
parameter_list|(
name|SearchPhaseResults
argument_list|<
name|Result
argument_list|>
name|results
parameter_list|,
name|SearchPhaseContext
name|context
parameter_list|)
function_decl|;
block|}
end_class

end_unit

