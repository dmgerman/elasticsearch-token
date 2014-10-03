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
name|collect
operator|.
name|Tuple
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
name|type
operator|.
name|TransportSearchHelper
operator|.
name|internalScrollSearchRequest
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportSearchScrollQueryAndFetchAction
specifier|public
class|class
name|TransportSearchScrollQueryAndFetchAction
extends|extends
name|AbstractComponent
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|searchService
specifier|private
specifier|final
name|SearchServiceTransportAction
name|searchService
decl_stmt|;
DECL|field|searchPhaseController
specifier|private
specifier|final
name|SearchPhaseController
name|searchPhaseController
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportSearchScrollQueryAndFetchAction
specifier|public
name|TransportSearchScrollQueryAndFetchAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
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
name|clusterService
operator|=
name|clusterService
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
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|SearchScrollRequest
name|request
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
operator|new
name|AsyncAction
argument_list|(
name|request
argument_list|,
name|scrollId
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
name|AbstractAsyncAction
block|{
DECL|field|request
specifier|private
specifier|final
name|SearchScrollRequest
name|request
decl_stmt|;
DECL|field|useSlowScroll
specifier|private
specifier|volatile
name|boolean
name|useSlowScroll
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
DECL|field|queryFetchResults
specifier|private
specifier|final
name|AtomicArray
argument_list|<
name|QueryFetchSearchResult
argument_list|>
name|queryFetchResults
decl_stmt|;
DECL|field|successfulOps
specifier|private
specifier|final
name|AtomicInteger
name|successfulOps
decl_stmt|;
DECL|field|counter
specifier|private
specifier|final
name|AtomicInteger
name|counter
decl_stmt|;
DECL|method|AsyncAction
specifier|private
name|AsyncAction
parameter_list|(
name|SearchScrollRequest
name|request
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
name|counter
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
name|queryFetchResults
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
specifier|protected
specifier|final
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
comment|// we do our best to return the shard failures, but its ok if its not fully concurrently safe
comment|// we simply try and return as much as possible
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
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|Tuple
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
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
name|Tuple
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
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
name|v1
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
if|if
condition|(
name|node
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|ParsedScrollId
operator|.
name|SCROLL_SEARCH_AFTER_MINIMUM_VERSION
argument_list|)
condition|)
block|{
name|useSlowScroll
operator|=
literal|true
expr_stmt|;
block|}
name|executePhase
argument_list|(
name|i
argument_list|,
name|node
argument_list|,
name|target
operator|.
name|v2
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
literal|"Node ["
operator|+
name|target
operator|.
name|v1
argument_list|()
operator|+
literal|"] not available for scroll request ["
operator|+
name|scrollId
operator|.
name|getSource
argument_list|()
operator|+
literal|"]"
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
for|for
control|(
name|Tuple
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|target
range|:
name|scrollId
operator|.
name|getContext
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
name|target
operator|.
name|v1
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
literal|"Node ["
operator|+
name|target
operator|.
name|v1
argument_list|()
operator|+
literal|"] not available for scroll request ["
operator|+
name|scrollId
operator|.
name|getSource
argument_list|()
operator|+
literal|"]"
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
else|else
block|{                 }
block|}
block|}
DECL|method|executePhase
name|void
name|executePhase
parameter_list|(
specifier|final
name|int
name|shardIndex
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
name|searchService
operator|.
name|sendExecuteFetch
argument_list|(
name|node
argument_list|,
name|internalRequest
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
name|onPhaseFailure
argument_list|(
name|t
argument_list|,
name|searchId
argument_list|,
name|shardIndex
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|onPhaseFailure
specifier|private
name|void
name|onPhaseFailure
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|long
name|searchId
parameter_list|,
name|int
name|shardIndex
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
name|searchId
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
name|t
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
literal|"query_fetch"
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
name|finishHim
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|finishHim
specifier|private
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
literal|"fetch"
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
DECL|method|innerFinishHim
specifier|private
name|void
name|innerFinishHim
parameter_list|()
throws|throws
name|Exception
block|{
name|ScoreDoc
index|[]
name|sortedShardList
init|=
name|searchPhaseController
operator|.
name|sortDocs
argument_list|(
operator|!
name|useSlowScroll
argument_list|,
name|queryFetchResults
argument_list|)
decl_stmt|;
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
block|}
block|}
end_class

end_unit

