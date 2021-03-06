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
name|transport
operator|.
name|RemoteClusterService
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
name|ArrayList
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
name|HashSet
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
name|Set
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
name|BiFunction
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

begin_comment
comment|/**  * Abstract base class for scroll execution modes. This class encapsulates the basic logic to  * fan out to nodes and execute the query part of the scroll request. Subclasses can for instance  * run separate fetch phases etc.  */
end_comment

begin_class
DECL|class|SearchScrollAsyncAction
specifier|abstract
class|class
name|SearchScrollAsyncAction
parameter_list|<
name|T
extends|extends
name|SearchPhaseResult
parameter_list|>
implements|implements
name|Runnable
block|{
comment|/*      * Some random TODO:      * Today we still have a dedicated executing mode for scrolls while we could simplify this by implementing      * scroll like functionality (mainly syntactic sugar) as an ordinary search with search_after. We could even go further and      * make the scroll entirely stateless and encode the state per shard in the scroll ID.      *      * Today we also hold a context per shard but maybe      * we want the context per coordinating node such that we route the scroll to the same coordinator all the time and hold the context      * here? This would have the advantage that if we loose that node the entire scroll is deal not just one shard.      *      * Additionally there is the possibility to associate the scroll with a seq. id. such that we can talk to any replica as long as      * the shards engine hasn't advanced that seq. id yet. Such a resume is possible and best effort, it could be even a safety net since      * if you rely on indices being read-only things can change in-between without notification or it's hard to detect if there where any      * changes while scrolling. These are all options to improve the current situation which we can look into down the road      */
DECL|field|logger
specifier|protected
specifier|final
name|Logger
name|logger
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
DECL|field|scrollId
specifier|protected
specifier|final
name|ParsedScrollId
name|scrollId
decl_stmt|;
DECL|field|nodes
specifier|protected
specifier|final
name|DiscoveryNodes
name|nodes
decl_stmt|;
DECL|field|searchPhaseController
specifier|protected
specifier|final
name|SearchPhaseController
name|searchPhaseController
decl_stmt|;
DECL|field|request
specifier|protected
specifier|final
name|SearchScrollRequest
name|request
decl_stmt|;
DECL|field|searchTransportService
specifier|protected
specifier|final
name|SearchTransportService
name|searchTransportService
decl_stmt|;
DECL|field|startTime
specifier|private
specifier|final
name|long
name|startTime
decl_stmt|;
DECL|field|shardFailures
specifier|private
specifier|final
name|List
argument_list|<
name|ShardSearchFailure
argument_list|>
name|shardFailures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|successfulOps
specifier|private
specifier|final
name|AtomicInteger
name|successfulOps
decl_stmt|;
DECL|method|SearchScrollAsyncAction
specifier|protected
name|SearchScrollAsyncAction
parameter_list|(
name|ParsedScrollId
name|scrollId
parameter_list|,
name|Logger
name|logger
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|,
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|,
name|SearchPhaseController
name|searchPhaseController
parameter_list|,
name|SearchScrollRequest
name|request
parameter_list|,
name|SearchTransportService
name|searchTransportService
parameter_list|)
block|{
name|this
operator|.
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|scrollId
operator|=
name|scrollId
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
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|nodes
operator|=
name|nodes
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
name|searchTransportService
operator|=
name|searchTransportService
expr_stmt|;
block|}
comment|/**      * Builds how long it took to execute the search.      */
DECL|method|buildTookInMillis
specifier|private
name|long
name|buildTookInMillis
parameter_list|()
block|{
comment|// protect ourselves against time going backwards
comment|// negative values don't make sense and we want to be able to serialize that thing as a vLong
return|return
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
argument_list|)
return|;
block|}
DECL|method|run
specifier|public
specifier|final
name|void
name|run
parameter_list|()
block|{
specifier|final
name|ScrollIdForNode
index|[]
name|context
init|=
name|scrollId
operator|.
name|getContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|context
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
block|}
else|else
block|{
name|collectNodesAndRun
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|context
argument_list|)
argument_list|,
name|nodes
argument_list|,
name|searchTransportService
argument_list|,
name|ActionListener
operator|.
name|wrap
argument_list|(
name|lookup
lambda|->
name|run
argument_list|(
name|lookup
argument_list|,
name|context
argument_list|)
argument_list|,
name|listener
operator|::
name|onFailure
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * This method collects nodes from the remote clusters asynchronously if any of the scroll IDs references a remote cluster.      * Otherwise the action listener will be invoked immediately with a function based on the given discovery nodes.      */
DECL|method|collectNodesAndRun
specifier|static
name|void
name|collectNodesAndRun
parameter_list|(
specifier|final
name|Iterable
argument_list|<
name|ScrollIdForNode
argument_list|>
name|scrollIds
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|,
name|SearchTransportService
name|searchTransportService
parameter_list|,
name|ActionListener
argument_list|<
name|BiFunction
argument_list|<
name|String
argument_list|,
name|String
argument_list|,
name|DiscoveryNode
argument_list|>
argument_list|>
name|listener
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|clusters
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ScrollIdForNode
name|target
range|:
name|scrollIds
control|)
block|{
if|if
condition|(
name|target
operator|.
name|getClusterAlias
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|clusters
operator|.
name|add
argument_list|(
name|target
operator|.
name|getClusterAlias
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|clusters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// no remote clusters
name|listener
operator|.
name|onResponse
argument_list|(
parameter_list|(
name|cluster
parameter_list|,
name|node
parameter_list|)
lambda|->
name|nodes
operator|.
name|get
argument_list|(
name|node
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|RemoteClusterService
name|remoteClusterService
init|=
name|searchTransportService
operator|.
name|getRemoteClusterService
argument_list|()
decl_stmt|;
name|remoteClusterService
operator|.
name|collectNodes
argument_list|(
name|clusters
argument_list|,
name|ActionListener
operator|.
name|wrap
argument_list|(
name|nodeFunction
lambda|->
block|{
specifier|final
name|BiFunction
argument_list|<
name|String
argument_list|,
name|String
argument_list|,
name|DiscoveryNode
argument_list|>
name|clusterNodeLookup
init|=
parameter_list|(
name|clusterAlias
parameter_list|,
name|node
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|clusterAlias
operator|==
literal|null
condition|)
block|{
return|return
name|nodes
operator|.
name|get
argument_list|(
name|node
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|nodeFunction
operator|.
name|apply
argument_list|(
name|clusterAlias
argument_list|,
name|node
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
name|clusterNodeLookup
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|listener
operator|::
name|onFailure
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|run
specifier|private
name|void
name|run
parameter_list|(
name|BiFunction
argument_list|<
name|String
argument_list|,
name|String
argument_list|,
name|DiscoveryNode
argument_list|>
name|clusterNodeLookup
parameter_list|,
specifier|final
name|ScrollIdForNode
index|[]
name|context
parameter_list|)
block|{
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
specifier|final
name|int
name|shardIndex
init|=
name|i
decl_stmt|;
specifier|final
name|Transport
operator|.
name|Connection
name|connection
decl_stmt|;
try|try
block|{
name|DiscoveryNode
name|node
init|=
name|clusterNodeLookup
operator|.
name|apply
argument_list|(
name|target
operator|.
name|getClusterAlias
argument_list|()
argument_list|,
name|target
operator|.
name|getNode
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
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"node ["
operator|+
name|target
operator|.
name|getNode
argument_list|()
operator|+
literal|"] is not available"
argument_list|)
throw|;
block|}
name|connection
operator|=
name|getConnection
argument_list|(
name|target
operator|.
name|getClusterAlias
argument_list|()
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|onShardFailure
argument_list|(
literal|"query"
argument_list|,
name|counter
argument_list|,
name|target
operator|.
name|getScrollId
argument_list|()
argument_list|,
name|ex
argument_list|,
literal|null
argument_list|,
parameter_list|()
lambda|->
name|SearchScrollAsyncAction
operator|.
name|this
operator|.
name|moveToNextPhase
argument_list|(
name|clusterNodeLookup
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|InternalScrollSearchRequest
name|internalRequest
init|=
name|internalScrollSearchRequest
argument_list|(
name|target
operator|.
name|getScrollId
argument_list|()
argument_list|,
name|request
argument_list|)
decl_stmt|;
comment|// we can't create a SearchShardTarget here since we don't know the index and shard ID we are talking to
comment|// we only know the node and the search context ID. Yet, the response will contain the SearchShardTarget
comment|// from the target node instead...that's why we pass null here
name|SearchActionListener
argument_list|<
name|T
argument_list|>
name|searchActionListener
init|=
operator|new
name|SearchActionListener
argument_list|<
name|T
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
name|T
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
if|if
condition|(
name|target
operator|.
name|getClusterAlias
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// re-create the search target and add the cluster alias if there is any,
comment|// we need this down the road for subseq. phases
name|SearchShardTarget
name|searchShardTarget
init|=
name|response
operator|.
name|getSearchShardTarget
argument_list|()
decl_stmt|;
name|response
operator|.
name|setSearchShardTarget
argument_list|(
operator|new
name|SearchShardTarget
argument_list|(
name|searchShardTarget
operator|.
name|getNodeId
argument_list|()
argument_list|,
name|searchShardTarget
operator|.
name|getShardId
argument_list|()
argument_list|,
name|target
operator|.
name|getClusterAlias
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|innerOnResponse
parameter_list|(
name|T
name|result
parameter_list|)
block|{
assert|assert
name|shardIndex
operator|==
name|result
operator|.
name|getShardIndex
argument_list|()
operator|:
literal|"shard index mismatch: "
operator|+
name|shardIndex
operator|+
literal|" but got: "
operator|+
name|result
operator|.
name|getShardIndex
argument_list|()
assert|;
name|onFirstPhaseResult
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
name|countDown
argument_list|()
condition|)
block|{
name|SearchPhase
name|phase
init|=
name|moveToNextPhase
argument_list|(
name|clusterNodeLookup
argument_list|)
decl_stmt|;
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
comment|// we need to fail the entire request here - the entire phase just blew up
comment|// don't call onShardFailure or onFailure here since otherwise we'd countDown the counter
comment|// again which would result in an exception
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|SearchPhaseExecutionException
argument_list|(
name|phase
operator|.
name|getName
argument_list|()
argument_list|,
literal|"Phase failed"
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
name|onShardFailure
argument_list|(
literal|"query"
argument_list|,
name|counter
argument_list|,
name|target
operator|.
name|getScrollId
argument_list|()
argument_list|,
name|t
argument_list|,
literal|null
argument_list|,
parameter_list|()
lambda|->
name|SearchScrollAsyncAction
operator|.
name|this
operator|.
name|moveToNextPhase
argument_list|(
name|clusterNodeLookup
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|executeInitialPhase
argument_list|(
name|connection
argument_list|,
name|internalRequest
argument_list|,
name|searchActionListener
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|buildShardFailures
specifier|synchronized
name|ShardSearchFailure
index|[]
name|buildShardFailures
parameter_list|()
block|{
comment|// pkg private for testing
if|if
condition|(
name|shardFailures
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|ShardSearchFailure
operator|.
name|EMPTY_ARRAY
return|;
block|}
return|return
name|shardFailures
operator|.
name|toArray
argument_list|(
operator|new
name|ShardSearchFailure
index|[
name|shardFailures
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
specifier|synchronized
name|void
name|addShardFailure
parameter_list|(
name|ShardSearchFailure
name|failure
parameter_list|)
block|{
name|shardFailures
operator|.
name|add
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
DECL|method|executeInitialPhase
specifier|protected
specifier|abstract
name|void
name|executeInitialPhase
parameter_list|(
name|Transport
operator|.
name|Connection
name|connection
parameter_list|,
name|InternalScrollSearchRequest
name|internalRequest
parameter_list|,
name|SearchActionListener
argument_list|<
name|T
argument_list|>
name|searchActionListener
parameter_list|)
function_decl|;
DECL|method|moveToNextPhase
specifier|protected
specifier|abstract
name|SearchPhase
name|moveToNextPhase
parameter_list|(
name|BiFunction
argument_list|<
name|String
argument_list|,
name|String
argument_list|,
name|DiscoveryNode
argument_list|>
name|clusterNodeLookup
parameter_list|)
function_decl|;
DECL|method|onFirstPhaseResult
specifier|protected
specifier|abstract
name|void
name|onFirstPhaseResult
parameter_list|(
name|int
name|shardId
parameter_list|,
name|T
name|result
parameter_list|)
function_decl|;
DECL|method|sendResponsePhase
specifier|protected
name|SearchPhase
name|sendResponsePhase
parameter_list|(
name|SearchPhaseController
operator|.
name|ReducedQueryPhase
name|queryPhase
parameter_list|,
specifier|final
name|AtomicArray
argument_list|<
name|?
extends|extends
name|SearchPhaseResult
argument_list|>
name|fetchResults
parameter_list|)
block|{
return|return
operator|new
name|SearchPhase
argument_list|(
literal|"fetch"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|IOException
block|{
name|sendResponse
argument_list|(
name|queryPhase
argument_list|,
name|fetchResults
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
DECL|method|sendResponse
specifier|protected
specifier|final
name|void
name|sendResponse
parameter_list|(
name|SearchPhaseController
operator|.
name|ReducedQueryPhase
name|queryPhase
parameter_list|,
specifier|final
name|AtomicArray
argument_list|<
name|?
extends|extends
name|SearchPhaseResult
argument_list|>
name|fetchResults
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
comment|// the scroll ID never changes we always return the same ID. This ID contains all the shards and their context ids
comment|// such that we can talk to them abgain in the next roundtrip.
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
DECL|method|onShardFailure
specifier|protected
name|void
name|onShardFailure
parameter_list|(
name|String
name|phaseName
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
parameter_list|,
annotation|@
name|Nullable
name|SearchShardTarget
name|searchShardTarget
parameter_list|,
name|Supplier
argument_list|<
name|SearchPhase
argument_list|>
name|nextPhaseSupplier
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
literal|"[{}] Failed to execute {} phase"
argument_list|,
name|searchId
argument_list|,
name|phaseName
argument_list|)
argument_list|,
name|failure
argument_list|)
expr_stmt|;
block|}
name|addShardFailure
argument_list|(
operator|new
name|ShardSearchFailure
argument_list|(
name|failure
argument_list|,
name|searchShardTarget
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|successfulOperations
init|=
name|successfulOps
operator|.
name|decrementAndGet
argument_list|()
decl_stmt|;
assert|assert
name|successfulOperations
operator|>=
literal|0
operator|:
literal|"successfulOperations must be>= 0 but was: "
operator|+
name|successfulOperations
assert|;
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
name|phaseName
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
name|SearchPhase
name|phase
init|=
name|nextPhaseSupplier
operator|.
name|get
argument_list|()
decl_stmt|;
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
name|phase
operator|.
name|getName
argument_list|()
argument_list|,
literal|"Phase failed"
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
DECL|method|getConnection
specifier|protected
name|Transport
operator|.
name|Connection
name|getConnection
parameter_list|(
name|String
name|clusterAlias
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|)
block|{
return|return
name|searchTransportService
operator|.
name|getConnection
argument_list|(
name|clusterAlias
argument_list|,
name|node
argument_list|)
return|;
block|}
block|}
end_class

end_unit

