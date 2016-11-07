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
name|action
operator|.
name|support
operator|.
name|HandledTransportAction
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
name|Setting
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
name|Setting
operator|.
name|Property
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
name|index
operator|.
name|Index
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
name|SearchService
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
name|builder
operator|.
name|SearchSourceBuilder
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
name|tasks
operator|.
name|Task
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
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Executor
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchType
operator|.
name|QUERY_AND_FETCH
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
name|SearchType
operator|.
name|QUERY_THEN_FETCH
import|;
end_import

begin_class
DECL|class|TransportSearchAction
specifier|public
class|class
name|TransportSearchAction
extends|extends
name|HandledTransportAction
argument_list|<
name|SearchRequest
argument_list|,
name|SearchResponse
argument_list|>
block|{
comment|/** The maximum number of shards for a single search request. */
DECL|field|SHARD_COUNT_LIMIT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Long
argument_list|>
name|SHARD_COUNT_LIMIT_SETTING
init|=
name|Setting
operator|.
name|longSetting
argument_list|(
literal|"action.search.shard_count.limit"
argument_list|,
literal|1000L
argument_list|,
literal|1L
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
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
DECL|field|searchService
specifier|private
specifier|final
name|SearchService
name|searchService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportSearchAction
specifier|public
name|TransportSearchAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|SearchService
name|searchService
parameter_list|,
name|SearchTransportService
name|searchTransportService
parameter_list|,
name|SearchPhaseController
name|searchPhaseController
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|SearchAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|SearchRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchPhaseController
operator|=
name|searchPhaseController
expr_stmt|;
name|this
operator|.
name|searchTransportService
operator|=
name|searchTransportService
expr_stmt|;
name|SearchTransportService
operator|.
name|registerRequestHandler
argument_list|(
name|transportService
argument_list|,
name|searchService
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
block|}
DECL|method|buildPerIndexAliasFilter
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|AliasFilter
argument_list|>
name|buildPerIndexAliasFilter
parameter_list|(
name|SearchRequest
name|request
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|,
name|Index
index|[]
name|concreteIndices
parameter_list|)
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|AliasFilter
argument_list|>
name|aliasFilterMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Index
name|index
range|:
name|concreteIndices
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
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|AliasFilter
name|aliasFilter
init|=
name|searchService
operator|.
name|buildAliasFilter
argument_list|(
name|clusterState
argument_list|,
name|index
operator|.
name|getName
argument_list|()
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
name|aliasFilter
operator|!=
literal|null
assert|;
name|aliasFilterMap
operator|.
name|put
argument_list|(
name|index
operator|.
name|getUUID
argument_list|()
argument_list|,
name|aliasFilter
argument_list|)
expr_stmt|;
block|}
return|return
name|aliasFilterMap
return|;
block|}
DECL|method|resolveIndexBoosts
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|resolveIndexBoosts
parameter_list|(
name|SearchRequest
name|searchRequest
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
if|if
condition|(
name|searchRequest
operator|.
name|source
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
name|SearchSourceBuilder
name|source
init|=
name|searchRequest
operator|.
name|source
argument_list|()
decl_stmt|;
if|if
condition|(
name|source
operator|.
name|indexBoosts
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|concreteIndexBoosts
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|SearchSourceBuilder
operator|.
name|IndexBoost
name|ib
range|:
name|source
operator|.
name|indexBoosts
argument_list|()
control|)
block|{
name|Index
index|[]
name|concreteIndices
init|=
name|indexNameExpressionResolver
operator|.
name|concreteIndices
argument_list|(
name|clusterState
argument_list|,
name|searchRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|ib
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Index
name|concreteIndex
range|:
name|concreteIndices
control|)
block|{
name|concreteIndexBoosts
operator|.
name|putIfAbsent
argument_list|(
name|concreteIndex
operator|.
name|getUUID
argument_list|()
argument_list|,
name|ib
operator|.
name|getBoost
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|concreteIndexBoosts
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|Task
name|task
parameter_list|,
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
comment|// pure paranoia if time goes backwards we are at least positive
specifier|final
name|long
name|startTimeInMillis
init|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|clusterState
operator|.
name|blocks
argument_list|()
operator|.
name|globalBlockedRaiseException
argument_list|(
name|ClusterBlockLevel
operator|.
name|READ
argument_list|)
expr_stmt|;
comment|// TODO: I think startTime() should become part of ActionRequest and that should be used both for index name
comment|// date math expressions and $now in scripts. This way all apis will deal with now in the same way instead
comment|// of just for the _search api
name|Index
index|[]
name|indices
init|=
name|indexNameExpressionResolver
operator|.
name|concreteIndices
argument_list|(
name|clusterState
argument_list|,
name|searchRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|startTimeInMillis
argument_list|,
name|searchRequest
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|AliasFilter
argument_list|>
name|aliasFilter
init|=
name|buildPerIndexAliasFilter
argument_list|(
name|searchRequest
argument_list|,
name|clusterState
argument_list|,
name|indices
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|routingMap
init|=
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|clusterState
argument_list|,
name|searchRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|searchRequest
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
operator|new
name|String
index|[
name|indices
operator|.
name|length
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
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|concreteIndices
index|[
name|i
index|]
operator|=
name|indices
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|GroupShardsIterator
name|shardIterators
init|=
name|clusterService
operator|.
name|operationRouting
argument_list|()
operator|.
name|searchShards
argument_list|(
name|clusterState
argument_list|,
name|concreteIndices
argument_list|,
name|routingMap
argument_list|,
name|searchRequest
operator|.
name|preference
argument_list|()
argument_list|)
decl_stmt|;
name|failIfOverShardCountLimit
argument_list|(
name|clusterService
argument_list|,
name|shardIterators
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|concreteIndexBoosts
init|=
name|resolveIndexBoosts
argument_list|(
name|searchRequest
argument_list|,
name|clusterState
argument_list|)
decl_stmt|;
comment|// optimize search type for cases where there is only one shard group to search on
if|if
condition|(
name|shardIterators
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// if we only have one group, then we always want Q_A_F, no need for DFS, and no need to do THEN since we hit one shard
name|searchRequest
operator|.
name|searchType
argument_list|(
name|QUERY_AND_FETCH
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|searchRequest
operator|.
name|isSuggestOnly
argument_list|()
condition|)
block|{
comment|// disable request cache if we have only suggest
name|searchRequest
operator|.
name|requestCache
argument_list|(
literal|false
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|searchRequest
operator|.
name|searchType
argument_list|()
condition|)
block|{
case|case
name|DFS_QUERY_AND_FETCH
case|:
case|case
name|DFS_QUERY_THEN_FETCH
case|:
comment|// convert to Q_T_F if we have only suggest
name|searchRequest
operator|.
name|searchType
argument_list|(
name|QUERY_THEN_FETCH
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|searchAsyncAction
argument_list|(
operator|(
name|SearchTask
operator|)
name|task
argument_list|,
name|searchRequest
argument_list|,
name|shardIterators
argument_list|,
name|startTimeInMillis
argument_list|,
name|clusterState
argument_list|,
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|aliasFilter
argument_list|)
argument_list|,
name|concreteIndexBoosts
argument_list|,
name|listener
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
specifier|final
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"the task parameter is required"
argument_list|)
throw|;
block|}
DECL|method|searchAsyncAction
specifier|private
name|AbstractSearchAsyncAction
name|searchAsyncAction
parameter_list|(
name|SearchTask
name|task
parameter_list|,
name|SearchRequest
name|searchRequest
parameter_list|,
name|GroupShardsIterator
name|shardIterators
parameter_list|,
name|long
name|startTime
parameter_list|,
name|ClusterState
name|state
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
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|Function
argument_list|<
name|String
argument_list|,
name|DiscoveryNode
argument_list|>
name|nodesLookup
init|=
name|state
operator|.
name|nodes
argument_list|()
operator|::
name|get
decl_stmt|;
specifier|final
name|long
name|clusterStateVersion
init|=
name|state
operator|.
name|version
argument_list|()
decl_stmt|;
name|Executor
name|executor
init|=
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
decl_stmt|;
name|AbstractSearchAsyncAction
name|searchAsyncAction
decl_stmt|;
switch|switch
condition|(
name|searchRequest
operator|.
name|searchType
argument_list|()
condition|)
block|{
case|case
name|DFS_QUERY_THEN_FETCH
case|:
name|searchAsyncAction
operator|=
operator|new
name|SearchDfsQueryThenFetchAsyncAction
argument_list|(
name|logger
argument_list|,
name|searchTransportService
argument_list|,
name|nodesLookup
argument_list|,
name|aliasFilter
argument_list|,
name|concreteIndexBoosts
argument_list|,
name|searchPhaseController
argument_list|,
name|executor
argument_list|,
name|searchRequest
argument_list|,
name|listener
argument_list|,
name|shardIterators
argument_list|,
name|startTime
argument_list|,
name|clusterStateVersion
argument_list|,
name|task
argument_list|)
expr_stmt|;
break|break;
case|case
name|QUERY_THEN_FETCH
case|:
name|searchAsyncAction
operator|=
operator|new
name|SearchQueryThenFetchAsyncAction
argument_list|(
name|logger
argument_list|,
name|searchTransportService
argument_list|,
name|nodesLookup
argument_list|,
name|aliasFilter
argument_list|,
name|concreteIndexBoosts
argument_list|,
name|searchPhaseController
argument_list|,
name|executor
argument_list|,
name|searchRequest
argument_list|,
name|listener
argument_list|,
name|shardIterators
argument_list|,
name|startTime
argument_list|,
name|clusterStateVersion
argument_list|,
name|task
argument_list|)
expr_stmt|;
break|break;
case|case
name|DFS_QUERY_AND_FETCH
case|:
name|searchAsyncAction
operator|=
operator|new
name|SearchDfsQueryAndFetchAsyncAction
argument_list|(
name|logger
argument_list|,
name|searchTransportService
argument_list|,
name|nodesLookup
argument_list|,
name|aliasFilter
argument_list|,
name|concreteIndexBoosts
argument_list|,
name|searchPhaseController
argument_list|,
name|executor
argument_list|,
name|searchRequest
argument_list|,
name|listener
argument_list|,
name|shardIterators
argument_list|,
name|startTime
argument_list|,
name|clusterStateVersion
argument_list|,
name|task
argument_list|)
expr_stmt|;
break|break;
case|case
name|QUERY_AND_FETCH
case|:
name|searchAsyncAction
operator|=
operator|new
name|SearchQueryAndFetchAsyncAction
argument_list|(
name|logger
argument_list|,
name|searchTransportService
argument_list|,
name|nodesLookup
argument_list|,
name|aliasFilter
argument_list|,
name|concreteIndexBoosts
argument_list|,
name|searchPhaseController
argument_list|,
name|executor
argument_list|,
name|searchRequest
argument_list|,
name|listener
argument_list|,
name|shardIterators
argument_list|,
name|startTime
argument_list|,
name|clusterStateVersion
argument_list|,
name|task
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unknown search type: ["
operator|+
name|searchRequest
operator|.
name|searchType
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|searchAsyncAction
return|;
block|}
DECL|method|failIfOverShardCountLimit
specifier|private
name|void
name|failIfOverShardCountLimit
parameter_list|(
name|ClusterService
name|clusterService
parameter_list|,
name|int
name|shardCount
parameter_list|)
block|{
specifier|final
name|long
name|shardCountLimit
init|=
name|clusterService
operator|.
name|getClusterSettings
argument_list|()
operator|.
name|get
argument_list|(
name|SHARD_COUNT_LIMIT_SETTING
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardCount
operator|>
name|shardCountLimit
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Trying to query "
operator|+
name|shardCount
operator|+
literal|" shards, which is over the limit of "
operator|+
name|shardCountLimit
operator|+
literal|". This limit exists because querying many shards at the same time can make the "
operator|+
literal|"job of the coordinating node very CPU and/or memory intensive. It is usually a better idea to "
operator|+
literal|"have a smaller number of larger shards. Update ["
operator|+
name|SHARD_COUNT_LIMIT_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|"] to a greater value if you really want to query that many shards at the same time."
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

