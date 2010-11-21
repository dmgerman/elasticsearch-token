begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|action
operator|.
name|support
operator|.
name|BaseAction
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
name|ClusterState
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
name|block
operator|.
name|ClusterBlockLevel
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
name|controller
operator|.
name|ShardDoc
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
name|InternalSearchRequest
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|type
operator|.
name|TransportSearchHelper
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportSearchTypeAction
specifier|public
specifier|abstract
class|class
name|TransportSearchTypeAction
extends|extends
name|BaseAction
argument_list|<
name|SearchRequest
argument_list|,
name|SearchResponse
argument_list|>
block|{
DECL|field|threadPool
specifier|protected
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|clusterService
specifier|protected
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|searchService
specifier|protected
specifier|final
name|SearchServiceTransportAction
name|searchService
decl_stmt|;
DECL|field|searchPhaseController
specifier|protected
specifier|final
name|SearchPhaseController
name|searchPhaseController
decl_stmt|;
DECL|field|searchCache
specifier|protected
specifier|final
name|TransportSearchCache
name|searchCache
decl_stmt|;
DECL|method|TransportSearchTypeAction
specifier|public
name|TransportSearchTypeAction
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
name|searchCache
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
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|searchCache
operator|=
name|searchCache
expr_stmt|;
name|this
operator|.
name|searchService
operator|=
name|searchService
expr_stmt|;
name|this
operator|.
name|searchPhaseController
operator|=
name|searchPhaseController
expr_stmt|;
block|}
DECL|class|BaseAsyncAction
specifier|protected
specifier|abstract
class|class
name|BaseAsyncAction
parameter_list|<
name|FirstResult
extends|extends
name|SearchPhaseResult
parameter_list|>
block|{
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
specifier|protected
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
DECL|field|nodes
specifier|protected
specifier|final
name|DiscoveryNodes
name|nodes
decl_stmt|;
DECL|field|expectedSuccessfulOps
specifier|protected
specifier|final
name|int
name|expectedSuccessfulOps
decl_stmt|;
DECL|field|expectedTotalOps
specifier|protected
specifier|final
name|int
name|expectedTotalOps
decl_stmt|;
DECL|field|successulOps
specifier|protected
specifier|final
name|AtomicInteger
name|successulOps
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|totalOps
specifier|protected
specifier|final
name|AtomicInteger
name|totalOps
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|shardFailures
specifier|protected
specifier|final
name|Collection
argument_list|<
name|ShardSearchFailure
argument_list|>
name|shardFailures
init|=
name|searchCache
operator|.
name|obtainShardFailures
argument_list|()
decl_stmt|;
DECL|field|sortedShardList
specifier|protected
specifier|volatile
name|ShardDoc
index|[]
name|sortedShardList
decl_stmt|;
DECL|method|BaseAsyncAction
specifier|protected
name|BaseAsyncAction
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
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|nodes
operator|=
name|clusterState
operator|.
name|nodes
argument_list|()
expr_stmt|;
name|request
operator|.
name|indices
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|concreteIndices
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|request
operator|.
name|indices
argument_list|()
control|)
block|{
name|clusterState
operator|.
name|blocks
argument_list|()
operator|.
name|indexBlockedRaiseException
argument_list|(
name|ClusterBlockLevel
operator|.
name|READ
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
name|shardsIts
operator|=
name|clusterService
operator|.
name|operationRouting
argument_list|()
operator|.
name|searchShards
argument_list|(
name|clusterState
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|,
name|request
operator|.
name|queryHint
argument_list|()
argument_list|,
name|request
operator|.
name|routing
argument_list|()
argument_list|)
expr_stmt|;
name|expectedSuccessfulOps
operator|=
name|shardsIts
operator|.
name|size
argument_list|()
expr_stmt|;
name|expectedTotalOps
operator|=
name|shardsIts
operator|.
name|totalSizeActive
argument_list|()
expr_stmt|;
if|if
condition|(
name|expectedSuccessfulOps
operator|==
literal|0
condition|)
block|{
comment|// not search shards to search on...
throw|throw
operator|new
name|SearchPhaseExecutionException
argument_list|(
literal|"initial"
argument_list|,
literal|"No indices / shards to search on, requested indices are "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|)
argument_list|,
name|buildShardFailures
argument_list|()
argument_list|)
throw|;
block|}
block|}
DECL|method|start
specifier|public
name|void
name|start
parameter_list|()
block|{
comment|// count the local operations, and perform the non local ones
name|int
name|localOperations
init|=
literal|0
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
specifier|final
name|ShardRouting
name|shard
init|=
name|shardIt
operator|.
name|nextActiveOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|shard
operator|.
name|currentNodeId
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
comment|// do the remote operation here, the localAsync flag is not relevant
name|performFirstPhase
argument_list|(
name|shardIt
operator|.
name|reset
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// really, no shards active in this group
name|onFirstPhaseResult
argument_list|(
name|shard
argument_list|,
name|shardIt
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
comment|// we have local operations, perform them now
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
name|operationThreading
argument_list|()
operator|==
name|SearchOperationThreading
operator|.
name|SINGLE_THREAD
condition|)
block|{
name|request
operator|.
name|beforeLocalFork
argument_list|()
expr_stmt|;
name|threadPool
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
name|ShardIterator
name|shardIt
range|:
name|shardsIts
control|)
block|{
specifier|final
name|ShardRouting
name|shard
init|=
name|shardIt
operator|.
name|reset
argument_list|()
operator|.
name|nextActiveOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|shard
operator|.
name|currentNodeId
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
name|performFirstPhase
argument_list|(
name|shardIt
operator|.
name|reset
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|operationThreading
argument_list|()
operator|==
name|SearchOperationThreading
operator|.
name|THREAD_PER_SHARD
decl_stmt|;
if|if
condition|(
name|localAsync
condition|)
block|{
name|request
operator|.
name|beforeLocalFork
argument_list|()
expr_stmt|;
block|}
for|for
control|(
specifier|final
name|ShardIterator
name|shardIt
range|:
name|shardsIts
control|)
block|{
specifier|final
name|ShardRouting
name|shard
init|=
name|shardIt
operator|.
name|reset
argument_list|()
operator|.
name|nextActiveOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|shard
operator|.
name|currentNodeId
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
if|if
condition|(
name|localAsync
condition|)
block|{
name|threadPool
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
name|performFirstPhase
argument_list|(
name|shardIt
operator|.
name|reset
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|performFirstPhase
argument_list|(
name|shardIt
operator|.
name|reset
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
block|}
DECL|method|performFirstPhase
specifier|private
name|void
name|performFirstPhase
parameter_list|(
specifier|final
name|ShardIterator
name|shardIt
parameter_list|)
block|{
specifier|final
name|ShardRouting
name|shard
init|=
name|shardIt
operator|.
name|nextActiveOrNull
argument_list|()
decl_stmt|;
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
name|shard
argument_list|,
name|shardIt
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
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
name|shard
argument_list|,
name|shardIt
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sendExecuteFirstPhase
argument_list|(
name|node
argument_list|,
name|internalSearchRequest
argument_list|(
name|shard
argument_list|,
name|shardsIts
operator|.
name|size
argument_list|()
argument_list|,
name|request
argument_list|)
argument_list|,
operator|new
name|SearchServiceListener
argument_list|<
name|FirstResult
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResult
parameter_list|(
name|FirstResult
name|result
parameter_list|)
block|{
name|onFirstPhaseResult
argument_list|(
name|shard
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
name|Throwable
name|t
parameter_list|)
block|{
name|onFirstPhaseResult
argument_list|(
name|shard
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
name|ShardRouting
name|shard
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
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|shard
operator|.
name|index
argument_list|()
argument_list|,
name|shard
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|processFirstPhaseResult
argument_list|(
name|shard
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// increment all the "future" shards to update the total ops since we some may work and some may not...
comment|// and when that happens, we break on total ops, so we must maintain them
while|while
condition|(
name|shardIt
operator|.
name|hasNextActive
argument_list|()
condition|)
block|{
name|totalOps
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|shardIt
operator|.
name|nextActive
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|successulOps
operator|.
name|incrementAndGet
argument_list|()
operator|==
name|expectedSuccessfulOps
operator|||
name|totalOps
operator|.
name|incrementAndGet
argument_list|()
operator|==
name|expectedTotalOps
condition|)
block|{
try|try
block|{
name|moveToSecondPhase
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
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
name|shard
operator|.
name|shortSummary
argument_list|()
operator|+
literal|": Failed to execute ["
operator|+
name|request
operator|+
literal|"] while moving to second phase"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
operator|+
literal|": Failed to execute ["
operator|+
name|request
operator|+
literal|"] while moving to second phase"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|invokeListener
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
block|}
DECL|method|onFirstPhaseResult
specifier|private
name|void
name|onFirstPhaseResult
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
specifier|final
name|ShardIterator
name|shardIt
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
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
comment|// e is null when there is no next active....
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
name|t
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
name|shard
operator|.
name|shortSummary
argument_list|()
operator|+
literal|": Failed to execute ["
operator|+
name|request
operator|+
literal|"]"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
operator|+
literal|": Failed to execute ["
operator|+
name|request
operator|+
literal|"]"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// no more shards, add a failure
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
comment|// no active shards
name|shardFailures
operator|.
name|add
argument_list|(
operator|new
name|ShardSearchFailure
argument_list|(
literal|"No active shards"
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|null
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shardFailures
operator|.
name|add
argument_list|(
operator|new
name|ShardSearchFailure
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|successulOps
operator|.
name|get
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// no successful ops, raise an exception
name|invokeListener
argument_list|(
operator|new
name|SearchPhaseExecutionException
argument_list|(
name|firstPhaseName
argument_list|()
argument_list|,
literal|"total failure"
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
name|moveToSecondPhase
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|invokeListener
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
block|}
else|else
block|{
if|if
condition|(
name|shardIt
operator|.
name|hasNextActive
argument_list|()
condition|)
block|{
comment|// trace log this exception
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
name|shard
operator|.
name|shortSummary
argument_list|()
operator|+
literal|": Failed to execute ["
operator|+
name|request
operator|+
literal|"]"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
operator|+
literal|": Failed to execute ["
operator|+
name|request
operator|+
literal|"]"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|performFirstPhase
argument_list|(
name|shardIt
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// no more shards active, add a failure
comment|// e is null when there is no next active....
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
name|t
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
name|shard
operator|.
name|shortSummary
argument_list|()
operator|+
literal|": Failed to execute ["
operator|+
name|request
operator|+
literal|"]"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
operator|+
literal|": Failed to execute ["
operator|+
name|request
operator|+
literal|"]"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
comment|// no active shards
name|shardFailures
operator|.
name|add
argument_list|(
operator|new
name|ShardSearchFailure
argument_list|(
literal|"No active shards"
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|null
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shardFailures
operator|.
name|add
argument_list|(
operator|new
name|ShardSearchFailure
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**          * Builds the shard failures, and releases the cache (meaning this should only be called once!).          */
DECL|method|buildShardFailures
specifier|protected
name|ShardSearchFailure
index|[]
name|buildShardFailures
parameter_list|()
block|{
return|return
name|TransportSearchHelper
operator|.
name|buildShardFailures
argument_list|(
name|shardFailures
argument_list|,
name|searchCache
argument_list|)
return|;
block|}
comment|/**          * Releases shard targets that are not used in the docsIdsToLoad.          */
DECL|method|releaseIrrelevantSearchContexts
specifier|protected
name|void
name|releaseIrrelevantSearchContexts
parameter_list|(
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QuerySearchResultProvider
argument_list|>
name|queryResults
parameter_list|,
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|ExtTIntArrayList
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
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|SearchShardTarget
argument_list|,
name|QuerySearchResultProvider
argument_list|>
name|entry
range|:
name|queryResults
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|docIdsToLoad
operator|.
name|containsKey
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
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
operator|!=
literal|null
condition|)
block|{
comment|// should not happen (==null) but safeguard anyhow
name|searchService
operator|.
name|sendFreeContext
argument_list|(
name|node
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|invokeListener
specifier|protected
name|void
name|invokeListener
parameter_list|(
specifier|final
name|SearchResponse
name|response
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|threadPool
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
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|invokeListener
specifier|protected
name|void
name|invokeListener
parameter_list|(
specifier|final
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|threadPool
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
name|listener
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
else|else
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
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
name|InternalSearchRequest
name|request
parameter_list|,
name|SearchServiceListener
argument_list|<
name|FirstResult
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|processFirstPhaseResult
specifier|protected
specifier|abstract
name|void
name|processFirstPhaseResult
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
name|FirstResult
name|result
parameter_list|)
function_decl|;
DECL|method|moveToSecondPhase
specifier|protected
specifier|abstract
name|void
name|moveToSecondPhase
parameter_list|()
function_decl|;
DECL|method|firstPhaseName
specifier|protected
specifier|abstract
name|String
name|firstPhaseName
parameter_list|()
function_decl|;
block|}
block|}
end_class

end_unit

