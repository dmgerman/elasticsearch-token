begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
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
name|cursors
operator|.
name|ObjectObjectCursor
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
name|elasticsearch
operator|.
name|ElasticsearchTimeoutException
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
name|FailedNodeException
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
name|nodes
operator|.
name|BaseNodeResponse
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
name|nodes
operator|.
name|BaseNodesResponse
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
name|allocation
operator|.
name|RoutingAllocation
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
name|index
operator|.
name|shard
operator|.
name|ShardId
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
name|ReceiveTimeoutTransportException
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|AtomicLong
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptySet
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableSet
import|;
end_import

begin_comment
comment|/**  * Allows to asynchronously fetch shard related data from other nodes for allocation, without blocking  * the cluster update thread.  *<p>  * The async fetch logic maintains a map of which nodes are being fetched from in an async manner,  * and once the results are back, it makes sure to schedule a reroute to make sure those results will  * be taken into account.  */
end_comment

begin_class
DECL|class|AsyncShardFetch
specifier|public
specifier|abstract
class|class
name|AsyncShardFetch
parameter_list|<
name|T
extends|extends
name|BaseNodeResponse
parameter_list|>
implements|implements
name|Releasable
block|{
comment|/**      * An action that lists the relevant shard data that needs to be fetched.      */
DECL|interface|Lister
specifier|public
interface|interface
name|Lister
parameter_list|<
name|NodesResponse
extends|extends
name|BaseNodesResponse
parameter_list|<
name|NodeResponse
parameter_list|>
parameter_list|,
name|NodeResponse
extends|extends
name|BaseNodeResponse
parameter_list|>
block|{
DECL|method|list
name|void
name|list
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|DiscoveryNode
index|[]
name|nodes
parameter_list|,
name|ActionListener
argument_list|<
name|NodesResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
block|}
DECL|field|logger
specifier|protected
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|type
specifier|protected
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|shardId
specifier|protected
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|action
specifier|private
specifier|final
name|Lister
argument_list|<
name|BaseNodesResponse
argument_list|<
name|T
argument_list|>
argument_list|,
name|T
argument_list|>
name|action
decl_stmt|;
DECL|field|cache
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|NodeEntry
argument_list|<
name|T
argument_list|>
argument_list|>
name|cache
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|nodesToIgnore
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|nodesToIgnore
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|round
specifier|private
specifier|final
name|AtomicLong
name|round
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|closed
specifier|private
name|boolean
name|closed
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|AsyncShardFetch
specifier|protected
name|AsyncShardFetch
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|String
name|type
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|Lister
argument_list|<
name|?
extends|extends
name|BaseNodesResponse
argument_list|<
name|T
argument_list|>
argument_list|,
name|T
argument_list|>
name|action
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
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|action
operator|=
operator|(
name|Lister
argument_list|<
name|BaseNodesResponse
argument_list|<
name|T
argument_list|>
argument_list|,
name|T
argument_list|>
operator|)
name|action
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
block|{
name|this
operator|.
name|closed
operator|=
literal|true
expr_stmt|;
block|}
comment|/**      * Returns the number of async fetches that are currently ongoing.      */
DECL|method|getNumberOfInFlightFetches
specifier|public
specifier|synchronized
name|int
name|getNumberOfInFlightFetches
parameter_list|()
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|NodeEntry
argument_list|<
name|T
argument_list|>
name|nodeEntry
range|:
name|cache
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|nodeEntry
operator|.
name|isFetching
argument_list|()
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
return|return
name|count
return|;
block|}
comment|/**      * Fetches the data for the relevant shard. If there any ongoing async fetches going on, or new ones have      * been initiated by this call, the result will have no data.      *<p>      * The ignoreNodes are nodes that are supposed to be ignored for this round, since fetching is async, we need      * to keep them around and make sure we add them back when all the responses are fetched and returned.      */
DECL|method|fetchData
specifier|public
specifier|synchronized
name|FetchResult
argument_list|<
name|T
argument_list|>
name|fetchData
parameter_list|(
name|DiscoveryNodes
name|nodes
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|ignoreNodes
parameter_list|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|shardId
operator|+
literal|": can't fetch data on closed async fetch"
argument_list|)
throw|;
block|}
name|nodesToIgnore
operator|.
name|addAll
argument_list|(
name|ignoreNodes
argument_list|)
expr_stmt|;
name|fillShardCacheWithDataNodes
argument_list|(
name|cache
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|NodeEntry
argument_list|<
name|T
argument_list|>
argument_list|>
name|nodesToFetch
init|=
name|findNodesToFetch
argument_list|(
name|cache
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodesToFetch
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// mark all node as fetching and go ahead and async fetch them
comment|// use a unique round id to detect stale responses in processAsyncFetch
specifier|final
name|long
name|fetchingRound
init|=
name|round
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeEntry
argument_list|<
name|T
argument_list|>
name|nodeEntry
range|:
name|nodesToFetch
control|)
block|{
name|nodeEntry
operator|.
name|markAsFetching
argument_list|(
name|fetchingRound
argument_list|)
expr_stmt|;
block|}
name|DiscoveryNode
index|[]
name|discoNodesToFetch
init|=
name|nodesToFetch
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|NodeEntry
operator|::
name|getNodeId
argument_list|)
operator|.
name|map
argument_list|(
name|nodes
operator|::
name|get
argument_list|)
operator|.
name|toArray
argument_list|(
name|DiscoveryNode
index|[]
operator|::
operator|new
argument_list|)
decl_stmt|;
name|asyncFetch
argument_list|(
name|discoNodesToFetch
argument_list|,
name|fetchingRound
argument_list|)
expr_stmt|;
block|}
comment|// if we are still fetching, return null to indicate it
if|if
condition|(
name|hasAnyNodeFetching
argument_list|(
name|cache
argument_list|)
condition|)
block|{
return|return
operator|new
name|FetchResult
argument_list|<>
argument_list|(
name|shardId
argument_list|,
literal|null
argument_list|,
name|emptySet
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
comment|// nothing to fetch, yay, build the return value
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|T
argument_list|>
name|fetchData
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|failedNodes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|NodeEntry
argument_list|<
name|T
argument_list|>
argument_list|>
argument_list|>
name|it
init|=
name|cache
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|NodeEntry
argument_list|<
name|T
argument_list|>
argument_list|>
name|entry
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|nodeId
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|NodeEntry
argument_list|<
name|T
argument_list|>
name|nodeEntry
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|nodeId
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|nodeEntry
operator|.
name|isFailed
argument_list|()
condition|)
block|{
comment|// if its failed, remove it from the list of nodes, so if this run doesn't work
comment|// we try again next round to fetch it again
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
name|failedNodes
operator|.
name|add
argument_list|(
name|nodeEntry
operator|.
name|getNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|nodeEntry
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|fetchData
operator|.
name|put
argument_list|(
name|node
argument_list|,
name|nodeEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|allIgnoreNodes
init|=
name|unmodifiableSet
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|nodesToIgnore
argument_list|)
argument_list|)
decl_stmt|;
comment|// clear the nodes to ignore, we had a successful run in fetching everything we can
comment|// we need to try them if another full run is needed
name|nodesToIgnore
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// if at least one node failed, make sure to have a protective reroute
comment|// here, just case this round won't find anything, and we need to retry fetching data
if|if
condition|(
name|failedNodes
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
operator|||
name|allIgnoreNodes
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|reroute
argument_list|(
name|shardId
argument_list|,
literal|"nodes failed ["
operator|+
name|failedNodes
operator|.
name|size
argument_list|()
operator|+
literal|"], ignored ["
operator|+
name|allIgnoreNodes
operator|.
name|size
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|FetchResult
argument_list|<>
argument_list|(
name|shardId
argument_list|,
name|fetchData
argument_list|,
name|allIgnoreNodes
argument_list|)
return|;
block|}
block|}
comment|/**      * Called by the response handler of the async action to fetch data. Verifies that its still working      * on the same cache generation, otherwise the results are discarded. It then goes and fills the relevant data for      * the shard (response + failures), issuing a reroute at the end of it to make sure there will be another round      * of allocations taking this new data into account.      */
DECL|method|processAsyncFetch
specifier|protected
specifier|synchronized
name|void
name|processAsyncFetch
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|responses
parameter_list|,
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failures
parameter_list|,
name|long
name|fetchingRound
parameter_list|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
comment|// we are closed, no need to process this async fetch at all
name|logger
operator|.
name|trace
argument_list|(
literal|"{} ignoring fetched [{}] results, already closed"
argument_list|,
name|shardId
argument_list|,
name|type
argument_list|)
expr_stmt|;
return|return;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"{} processing fetched [{}] results"
argument_list|,
name|shardId
argument_list|,
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
name|responses
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|T
name|response
range|:
name|responses
control|)
block|{
name|NodeEntry
argument_list|<
name|T
argument_list|>
name|nodeEntry
init|=
name|cache
operator|.
name|get
argument_list|(
name|response
operator|.
name|getNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodeEntry
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|nodeEntry
operator|.
name|getFetchingRound
argument_list|()
operator|!=
name|fetchingRound
condition|)
block|{
assert|assert
name|nodeEntry
operator|.
name|getFetchingRound
argument_list|()
operator|>
name|fetchingRound
operator|:
literal|"node entries only replaced by newer rounds"
assert|;
name|logger
operator|.
name|trace
argument_list|(
literal|"{} received response for [{}] from node {} for an older fetching round (expected: {} but was: {})"
argument_list|,
name|shardId
argument_list|,
name|nodeEntry
operator|.
name|getNodeId
argument_list|()
argument_list|,
name|type
argument_list|,
name|nodeEntry
operator|.
name|getFetchingRound
argument_list|()
argument_list|,
name|fetchingRound
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nodeEntry
operator|.
name|isFailed
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} node {} has failed for [{}] (failure [{}])"
argument_list|,
name|shardId
argument_list|,
name|nodeEntry
operator|.
name|getNodeId
argument_list|()
argument_list|,
name|type
argument_list|,
name|nodeEntry
operator|.
name|getFailure
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// if the entry is there, for the right fetching round and not marked as failed already, process it
name|logger
operator|.
name|trace
argument_list|(
literal|"{} marking {} as done for [{}], result is [{}]"
argument_list|,
name|shardId
argument_list|,
name|nodeEntry
operator|.
name|getNodeId
argument_list|()
argument_list|,
name|type
argument_list|,
name|response
argument_list|)
expr_stmt|;
name|nodeEntry
operator|.
name|doneFetching
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|failures
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FailedNodeException
name|failure
range|:
name|failures
control|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} processing failure {} for [{}]"
argument_list|,
name|shardId
argument_list|,
name|failure
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|NodeEntry
argument_list|<
name|T
argument_list|>
name|nodeEntry
init|=
name|cache
operator|.
name|get
argument_list|(
name|failure
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodeEntry
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|nodeEntry
operator|.
name|getFetchingRound
argument_list|()
operator|!=
name|fetchingRound
condition|)
block|{
assert|assert
name|nodeEntry
operator|.
name|getFetchingRound
argument_list|()
operator|>
name|fetchingRound
operator|:
literal|"node entries only replaced by newer rounds"
assert|;
name|logger
operator|.
name|trace
argument_list|(
literal|"{} received failure for [{}] from node {} for an older fetching round (expected: {} but was: {})"
argument_list|,
name|shardId
argument_list|,
name|nodeEntry
operator|.
name|getNodeId
argument_list|()
argument_list|,
name|type
argument_list|,
name|nodeEntry
operator|.
name|getFetchingRound
argument_list|()
argument_list|,
name|fetchingRound
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nodeEntry
operator|.
name|isFailed
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// if the entry is there, for the right fetching round and not marked as failed already, process it
name|Throwable
name|unwrappedCause
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|failure
operator|.
name|getCause
argument_list|()
argument_list|)
decl_stmt|;
comment|// if the request got rejected or timed out, we need to try it again next time...
if|if
condition|(
name|unwrappedCause
operator|instanceof
name|EsRejectedExecutionException
operator|||
name|unwrappedCause
operator|instanceof
name|ReceiveTimeoutTransportException
operator|||
name|unwrappedCause
operator|instanceof
name|ElasticsearchTimeoutException
condition|)
block|{
name|nodeEntry
operator|.
name|restartFetching
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
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
literal|"{}: failed to list shard for {} on node [{}]"
argument_list|,
name|shardId
argument_list|,
name|type
argument_list|,
name|failure
operator|.
name|nodeId
argument_list|()
argument_list|)
argument_list|,
name|failure
argument_list|)
expr_stmt|;
name|nodeEntry
operator|.
name|doneFetching
argument_list|(
name|failure
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
name|reroute
argument_list|(
name|shardId
argument_list|,
literal|"post_response"
argument_list|)
expr_stmt|;
block|}
comment|/**      * Implement this in order to scheduled another round that causes a call to fetch data.      */
DECL|method|reroute
specifier|protected
specifier|abstract
name|void
name|reroute
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|reason
parameter_list|)
function_decl|;
comment|/**      * Fills the shard fetched data with new (data) nodes and a fresh NodeEntry, and removes from      * it nodes that are no longer part of the state.      */
DECL|method|fillShardCacheWithDataNodes
specifier|private
name|void
name|fillShardCacheWithDataNodes
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|NodeEntry
argument_list|<
name|T
argument_list|>
argument_list|>
name|shardCache
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|)
block|{
comment|// verify that all current data nodes are there
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|DiscoveryNode
argument_list|>
name|cursor
range|:
name|nodes
operator|.
name|getDataNodes
argument_list|()
control|)
block|{
name|DiscoveryNode
name|node
init|=
name|cursor
operator|.
name|value
decl_stmt|;
if|if
condition|(
name|shardCache
operator|.
name|containsKey
argument_list|(
name|node
operator|.
name|getId
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|shardCache
operator|.
name|put
argument_list|(
name|node
operator|.
name|getId
argument_list|()
argument_list|,
operator|new
name|NodeEntry
argument_list|<
name|T
argument_list|>
argument_list|(
name|node
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// remove nodes that are not longer part of the data nodes set
for|for
control|(
name|Iterator
argument_list|<
name|String
argument_list|>
name|it
init|=
name|shardCache
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|String
name|nodeId
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|nodes
operator|.
name|nodeExists
argument_list|(
name|nodeId
argument_list|)
operator|==
literal|false
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Finds all the nodes that need to be fetched. Those are nodes that have no      * data, and are not in fetch mode.      */
DECL|method|findNodesToFetch
specifier|private
name|List
argument_list|<
name|NodeEntry
argument_list|<
name|T
argument_list|>
argument_list|>
name|findNodesToFetch
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|NodeEntry
argument_list|<
name|T
argument_list|>
argument_list|>
name|shardCache
parameter_list|)
block|{
name|List
argument_list|<
name|NodeEntry
argument_list|<
name|T
argument_list|>
argument_list|>
name|nodesToFetch
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeEntry
argument_list|<
name|T
argument_list|>
name|nodeEntry
range|:
name|shardCache
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|nodeEntry
operator|.
name|hasData
argument_list|()
operator|==
literal|false
operator|&&
name|nodeEntry
operator|.
name|isFetching
argument_list|()
operator|==
literal|false
condition|)
block|{
name|nodesToFetch
operator|.
name|add
argument_list|(
name|nodeEntry
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|nodesToFetch
return|;
block|}
comment|/**      * Are there any nodes that are fetching data?      */
DECL|method|hasAnyNodeFetching
specifier|private
name|boolean
name|hasAnyNodeFetching
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|NodeEntry
argument_list|<
name|T
argument_list|>
argument_list|>
name|shardCache
parameter_list|)
block|{
for|for
control|(
name|NodeEntry
argument_list|<
name|T
argument_list|>
name|nodeEntry
range|:
name|shardCache
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|nodeEntry
operator|.
name|isFetching
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**      * Async fetches data for the provided shard with the set of nodes that need to be fetched from.      */
comment|// visible for testing
DECL|method|asyncFetch
name|void
name|asyncFetch
parameter_list|(
specifier|final
name|DiscoveryNode
index|[]
name|nodes
parameter_list|,
name|long
name|fetchingRound
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} fetching [{}] from {}"
argument_list|,
name|shardId
argument_list|,
name|type
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
name|action
operator|.
name|list
argument_list|(
name|shardId
argument_list|,
name|nodes
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|BaseNodesResponse
argument_list|<
name|T
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|BaseNodesResponse
argument_list|<
name|T
argument_list|>
name|response
parameter_list|)
block|{
name|processAsyncFetch
argument_list|(
name|response
operator|.
name|getNodes
argument_list|()
argument_list|,
name|response
operator|.
name|failures
argument_list|()
argument_list|,
name|fetchingRound
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
name|e
parameter_list|)
block|{
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|nodes
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|DiscoveryNode
name|node
range|:
name|nodes
control|)
block|{
name|failures
operator|.
name|add
argument_list|(
operator|new
name|FailedNodeException
argument_list|(
name|node
operator|.
name|getId
argument_list|()
argument_list|,
literal|"total failure in fetching"
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|processAsyncFetch
argument_list|(
literal|null
argument_list|,
name|failures
argument_list|,
name|fetchingRound
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * The result of a fetch operation. Make sure to first check {@link #hasData()} before      * fetching the actual data.      */
DECL|class|FetchResult
specifier|public
specifier|static
class|class
name|FetchResult
parameter_list|<
name|T
extends|extends
name|BaseNodeResponse
parameter_list|>
block|{
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|data
specifier|private
specifier|final
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|T
argument_list|>
name|data
decl_stmt|;
DECL|field|ignoreNodes
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|ignoreNodes
decl_stmt|;
DECL|method|FetchResult
specifier|public
name|FetchResult
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|T
argument_list|>
name|data
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|ignoreNodes
parameter_list|)
block|{
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|data
operator|=
name|data
expr_stmt|;
name|this
operator|.
name|ignoreNodes
operator|=
name|ignoreNodes
expr_stmt|;
block|}
comment|/**          * Does the result actually contain data? If not, then there are on going fetch          * operations happening, and it should wait for it.          */
DECL|method|hasData
specifier|public
name|boolean
name|hasData
parameter_list|()
block|{
return|return
name|data
operator|!=
literal|null
return|;
block|}
comment|/**          * Returns the actual data, note, make sure to check {@link #hasData()} first and          * only use this when there is an actual data.          */
DECL|method|getData
specifier|public
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|T
argument_list|>
name|getData
parameter_list|()
block|{
assert|assert
name|data
operator|!=
literal|null
operator|:
literal|"getData should only be called if there is data to be fetched, please check hasData first"
assert|;
return|return
name|this
operator|.
name|data
return|;
block|}
comment|/**          * Process any changes needed to the allocation based on this fetch result.          */
DECL|method|processAllocation
specifier|public
name|void
name|processAllocation
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
for|for
control|(
name|String
name|ignoreNode
range|:
name|ignoreNodes
control|)
block|{
name|allocation
operator|.
name|addIgnoreShardForNode
argument_list|(
name|shardId
argument_list|,
name|ignoreNode
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * A node entry, holding the state of the fetched data for a specific shard      * for a giving node.      */
DECL|class|NodeEntry
specifier|static
class|class
name|NodeEntry
parameter_list|<
name|T
parameter_list|>
block|{
DECL|field|nodeId
specifier|private
specifier|final
name|String
name|nodeId
decl_stmt|;
DECL|field|fetching
specifier|private
name|boolean
name|fetching
decl_stmt|;
annotation|@
name|Nullable
DECL|field|value
specifier|private
name|T
name|value
decl_stmt|;
DECL|field|valueSet
specifier|private
name|boolean
name|valueSet
decl_stmt|;
DECL|field|failure
specifier|private
name|Throwable
name|failure
decl_stmt|;
DECL|field|fetchingRound
specifier|private
name|long
name|fetchingRound
decl_stmt|;
DECL|method|NodeEntry
name|NodeEntry
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
name|this
operator|.
name|nodeId
operator|=
name|nodeId
expr_stmt|;
block|}
DECL|method|getNodeId
name|String
name|getNodeId
parameter_list|()
block|{
return|return
name|this
operator|.
name|nodeId
return|;
block|}
DECL|method|isFetching
name|boolean
name|isFetching
parameter_list|()
block|{
return|return
name|fetching
return|;
block|}
DECL|method|markAsFetching
name|void
name|markAsFetching
parameter_list|(
name|long
name|fetchingRound
parameter_list|)
block|{
assert|assert
name|fetching
operator|==
literal|false
operator|:
literal|"double marking a node as fetching"
assert|;
name|this
operator|.
name|fetching
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|fetchingRound
operator|=
name|fetchingRound
expr_stmt|;
block|}
DECL|method|doneFetching
name|void
name|doneFetching
parameter_list|(
name|T
name|value
parameter_list|)
block|{
assert|assert
name|fetching
operator|:
literal|"setting value but not in fetching mode"
assert|;
assert|assert
name|failure
operator|==
literal|null
operator|:
literal|"setting value when failure already set"
assert|;
name|this
operator|.
name|valueSet
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|fetching
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|doneFetching
name|void
name|doneFetching
parameter_list|(
name|Throwable
name|failure
parameter_list|)
block|{
assert|assert
name|fetching
operator|:
literal|"setting value but not in fetching mode"
assert|;
assert|assert
name|valueSet
operator|==
literal|false
operator|:
literal|"setting failure when already set value"
assert|;
assert|assert
name|failure
operator|!=
literal|null
operator|:
literal|"setting failure can't be null"
assert|;
name|this
operator|.
name|failure
operator|=
name|failure
expr_stmt|;
name|this
operator|.
name|fetching
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|restartFetching
name|void
name|restartFetching
parameter_list|()
block|{
assert|assert
name|fetching
operator|:
literal|"restarting fetching, but not in fetching mode"
assert|;
assert|assert
name|valueSet
operator|==
literal|false
operator|:
literal|"value can't be set when restarting fetching"
assert|;
assert|assert
name|failure
operator|==
literal|null
operator|:
literal|"failure can't be set when restarting fetching"
assert|;
name|this
operator|.
name|fetching
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|isFailed
name|boolean
name|isFailed
parameter_list|()
block|{
return|return
name|failure
operator|!=
literal|null
return|;
block|}
DECL|method|hasData
name|boolean
name|hasData
parameter_list|()
block|{
return|return
name|valueSet
operator|||
name|failure
operator|!=
literal|null
return|;
block|}
DECL|method|getFailure
name|Throwable
name|getFailure
parameter_list|()
block|{
assert|assert
name|hasData
argument_list|()
operator|:
literal|"getting failure when data has not been fetched"
assert|;
return|return
name|failure
return|;
block|}
annotation|@
name|Nullable
DECL|method|getValue
name|T
name|getValue
parameter_list|()
block|{
assert|assert
name|failure
operator|==
literal|null
operator|:
literal|"trying to fetch value, but its marked as failed, check isFailed"
assert|;
assert|assert
name|valueSet
operator|:
literal|"value is not set, hasn't been fetched yet"
assert|;
return|return
name|value
return|;
block|}
DECL|method|getFetchingRound
name|long
name|getFetchingRound
parameter_list|()
block|{
return|return
name|fetchingRound
return|;
block|}
block|}
block|}
end_class

end_unit

