begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.shards
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|shards
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
name|util
operator|.
name|CollectionUtil
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
name|master
operator|.
name|TransportMasterNodeReadAction
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
name|ClusterBlockException
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
name|health
operator|.
name|ClusterHealthStatus
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
name|health
operator|.
name|ClusterShardHealth
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
name|metadata
operator|.
name|MetaData
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
name|IndexRoutingTable
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
name|IndexShardRoutingTable
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
name|RoutingNodes
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
name|RoutingTable
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
name|collect
operator|.
name|ImmutableOpenIntMap
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
name|ImmutableOpenMap
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
name|logging
operator|.
name|ESLogger
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
name|CountDown
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|AsyncShardFetch
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|TransportNodesListGatewayStartedShards
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|TransportNodesListGatewayStartedShards
operator|.
name|NodeGatewayStartedShards
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
name|ArrayList
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|ConcurrentLinkedQueue
import|;
end_import

begin_comment
comment|/**  * Transport action that reads the cluster state for shards with the requested criteria (see {@link ClusterHealthStatus}) of specific indices  * and fetches store information from all the nodes using {@link TransportNodesListGatewayStartedShards}  */
end_comment

begin_class
DECL|class|TransportIndicesShardStoresAction
specifier|public
class|class
name|TransportIndicesShardStoresAction
extends|extends
name|TransportMasterNodeReadAction
argument_list|<
name|IndicesShardStoresRequest
argument_list|,
name|IndicesShardStoresResponse
argument_list|>
block|{
DECL|field|listShardStoresInfo
specifier|private
specifier|final
name|TransportNodesListGatewayStartedShards
name|listShardStoresInfo
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportIndicesShardStoresAction
specifier|public
name|TransportIndicesShardStoresAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|TransportNodesListGatewayStartedShards
name|listShardStoresInfo
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|IndicesShardStoresAction
operator|.
name|NAME
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|IndicesShardStoresRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|listShardStoresInfo
operator|=
name|listShardStoresInfo
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
specifier|protected
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|IndicesShardStoresResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|IndicesShardStoresResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|masterOperation
specifier|protected
name|void
name|masterOperation
parameter_list|(
name|IndicesShardStoresRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|,
name|ActionListener
argument_list|<
name|IndicesShardStoresResponse
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|RoutingTable
name|routingTables
init|=
name|state
operator|.
name|routingTable
argument_list|()
decl_stmt|;
specifier|final
name|RoutingNodes
name|routingNodes
init|=
name|state
operator|.
name|getRoutingNodes
argument_list|()
decl_stmt|;
specifier|final
name|String
index|[]
name|concreteIndices
init|=
name|indexNameExpressionResolver
operator|.
name|concreteIndices
argument_list|(
name|state
argument_list|,
name|request
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|ShardId
argument_list|>
name|shardIdsToFetch
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"using cluster state version [{}] to determine shards"
argument_list|,
name|state
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
comment|// collect relevant shard ids of the requested indices for fetching store infos
for|for
control|(
name|String
name|index
range|:
name|concreteIndices
control|)
block|{
name|IndexRoutingTable
name|indexShardRoutingTables
init|=
name|routingTables
operator|.
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexShardRoutingTables
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|IndexShardRoutingTable
name|routing
range|:
name|indexShardRoutingTables
control|)
block|{
name|ClusterShardHealth
name|shardHealth
init|=
operator|new
name|ClusterShardHealth
argument_list|(
name|routing
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|routing
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|shardStatuses
argument_list|()
operator|.
name|contains
argument_list|(
name|shardHealth
operator|.
name|getStatus
argument_list|()
argument_list|)
condition|)
block|{
name|shardIdsToFetch
operator|.
name|add
argument_list|(
name|routing
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// async fetch store infos from all the nodes
comment|// NOTE: instead of fetching shard store info one by one from every node (nShards * nNodes requests)
comment|// we could fetch all shard store info from every node once (nNodes requests)
comment|// we have to implement a TransportNodesAction instead of using TransportNodesListGatewayStartedShards
comment|// for fetching shard stores info, that operates on a list of shards instead of a single shard
operator|new
name|AsyncShardStoresInfoFetches
argument_list|(
name|state
operator|.
name|nodes
argument_list|()
argument_list|,
name|routingNodes
argument_list|,
name|state
operator|.
name|metaData
argument_list|()
argument_list|,
name|shardIdsToFetch
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
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|IndicesShardStoresRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indicesBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_READ
argument_list|,
name|indexNameExpressionResolver
operator|.
name|concreteIndices
argument_list|(
name|state
argument_list|,
name|request
argument_list|)
argument_list|)
return|;
block|}
DECL|class|AsyncShardStoresInfoFetches
specifier|private
class|class
name|AsyncShardStoresInfoFetches
block|{
DECL|field|nodes
specifier|private
specifier|final
name|DiscoveryNodes
name|nodes
decl_stmt|;
DECL|field|routingNodes
specifier|private
specifier|final
name|RoutingNodes
name|routingNodes
decl_stmt|;
DECL|field|metaData
specifier|private
specifier|final
name|MetaData
name|metaData
decl_stmt|;
DECL|field|shardIds
specifier|private
specifier|final
name|Set
argument_list|<
name|ShardId
argument_list|>
name|shardIds
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|IndicesShardStoresResponse
argument_list|>
name|listener
decl_stmt|;
DECL|field|expectedOps
specifier|private
name|CountDown
name|expectedOps
decl_stmt|;
DECL|field|fetchResponses
specifier|private
specifier|final
name|Queue
argument_list|<
name|InternalAsyncFetch
operator|.
name|Response
argument_list|>
name|fetchResponses
decl_stmt|;
DECL|method|AsyncShardStoresInfoFetches
name|AsyncShardStoresInfoFetches
parameter_list|(
name|DiscoveryNodes
name|nodes
parameter_list|,
name|RoutingNodes
name|routingNodes
parameter_list|,
name|MetaData
name|metaData
parameter_list|,
name|Set
argument_list|<
name|ShardId
argument_list|>
name|shardIds
parameter_list|,
name|ActionListener
argument_list|<
name|IndicesShardStoresResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|this
operator|.
name|nodes
operator|=
name|nodes
expr_stmt|;
name|this
operator|.
name|routingNodes
operator|=
name|routingNodes
expr_stmt|;
name|this
operator|.
name|metaData
operator|=
name|metaData
expr_stmt|;
name|this
operator|.
name|shardIds
operator|=
name|shardIds
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|fetchResponses
operator|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|expectedOps
operator|=
operator|new
name|CountDown
argument_list|(
name|shardIds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|start
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
name|shardIds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|IndicesShardStoresResponse
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|ShardId
name|shardId
range|:
name|shardIds
control|)
block|{
name|InternalAsyncFetch
name|fetch
init|=
operator|new
name|InternalAsyncFetch
argument_list|(
name|logger
argument_list|,
literal|"shard_stores"
argument_list|,
name|shardId
argument_list|,
name|listShardStoresInfo
argument_list|)
decl_stmt|;
name|fetch
operator|.
name|fetchData
argument_list|(
name|nodes
argument_list|,
name|metaData
argument_list|,
name|Collections
operator|.
expr|<
name|String
operator|>
name|emptySet
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|InternalAsyncFetch
specifier|private
class|class
name|InternalAsyncFetch
extends|extends
name|AsyncShardFetch
argument_list|<
name|NodeGatewayStartedShards
argument_list|>
block|{
DECL|method|InternalAsyncFetch
name|InternalAsyncFetch
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|String
name|type
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|TransportNodesListGatewayStartedShards
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|logger
argument_list|,
name|type
argument_list|,
name|shardId
argument_list|,
name|action
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|processAsyncFetch
specifier|protected
specifier|synchronized
name|void
name|processAsyncFetch
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|NodeGatewayStartedShards
index|[]
name|responses
parameter_list|,
name|FailedNodeException
index|[]
name|failures
parameter_list|)
block|{
name|fetchResponses
operator|.
name|add
argument_list|(
operator|new
name|Response
argument_list|(
name|shardId
argument_list|,
name|responses
argument_list|,
name|failures
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|expectedOps
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|finish
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|finish
name|void
name|finish
parameter_list|()
block|{
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|ImmutableOpenIntMap
argument_list|<
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
argument_list|>
argument_list|>
name|indicesStoreStatusesBuilder
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|Failure
argument_list|>
name|failureBuilder
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Response
name|fetchResponse
range|:
name|fetchResponses
control|)
block|{
name|ImmutableOpenIntMap
argument_list|<
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
argument_list|>
name|indexStoreStatuses
init|=
name|indicesStoreStatusesBuilder
operator|.
name|get
argument_list|(
name|fetchResponse
operator|.
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|ImmutableOpenIntMap
operator|.
name|Builder
argument_list|<
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
argument_list|>
name|indexShardsBuilder
decl_stmt|;
if|if
condition|(
name|indexStoreStatuses
operator|==
literal|null
condition|)
block|{
name|indexShardsBuilder
operator|=
name|ImmutableOpenIntMap
operator|.
name|builder
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|indexShardsBuilder
operator|=
name|ImmutableOpenIntMap
operator|.
name|builder
argument_list|(
name|indexStoreStatuses
argument_list|)
expr_stmt|;
block|}
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|storeStatuses
init|=
name|indexShardsBuilder
operator|.
name|get
argument_list|(
name|fetchResponse
operator|.
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|storeStatuses
operator|==
literal|null
condition|)
block|{
name|storeStatuses
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|NodeGatewayStartedShards
name|response
range|:
name|fetchResponse
operator|.
name|responses
control|)
block|{
if|if
condition|(
name|shardExistsInNode
argument_list|(
name|response
argument_list|)
condition|)
block|{
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
name|allocationStatus
init|=
name|getAllocationStatus
argument_list|(
name|fetchResponse
operator|.
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|,
name|fetchResponse
operator|.
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|response
operator|.
name|getNode
argument_list|()
argument_list|)
decl_stmt|;
name|storeStatuses
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|response
operator|.
name|getNode
argument_list|()
argument_list|,
name|response
operator|.
name|version
argument_list|()
argument_list|,
name|response
operator|.
name|allocationId
argument_list|()
argument_list|,
name|allocationStatus
argument_list|,
name|response
operator|.
name|storeException
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|CollectionUtil
operator|.
name|timSort
argument_list|(
name|storeStatuses
argument_list|)
expr_stmt|;
name|indexShardsBuilder
operator|.
name|put
argument_list|(
name|fetchResponse
operator|.
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|storeStatuses
argument_list|)
expr_stmt|;
name|indicesStoreStatusesBuilder
operator|.
name|put
argument_list|(
name|fetchResponse
operator|.
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|,
name|indexShardsBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|FailedNodeException
name|failure
range|:
name|fetchResponse
operator|.
name|failures
control|)
block|{
name|failureBuilder
operator|.
name|add
argument_list|(
operator|new
name|IndicesShardStoresResponse
operator|.
name|Failure
argument_list|(
name|failure
operator|.
name|nodeId
argument_list|()
argument_list|,
name|fetchResponse
operator|.
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|,
name|fetchResponse
operator|.
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|failure
operator|.
name|getCause
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|IndicesShardStoresResponse
argument_list|(
name|indicesStoreStatusesBuilder
operator|.
name|build
argument_list|()
argument_list|,
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|failureBuilder
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getAllocationStatus
specifier|private
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
name|getAllocationStatus
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardID
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|routingNodes
operator|.
name|node
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
control|)
block|{
name|ShardId
name|shardId
init|=
name|shardRouting
operator|.
name|shardId
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardId
operator|.
name|id
argument_list|()
operator|==
name|shardID
operator|&&
name|shardId
operator|.
name|getIndex
argument_list|()
operator|.
name|equals
argument_list|(
name|index
argument_list|)
condition|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
return|return
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|PRIMARY
return|;
block|}
elseif|else
if|if
condition|(
name|shardRouting
operator|.
name|assignedToNode
argument_list|()
condition|)
block|{
return|return
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|REPLICA
return|;
block|}
else|else
block|{
return|return
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|UNUSED
return|;
block|}
block|}
block|}
return|return
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|UNUSED
return|;
block|}
comment|/**              * A shard exists/existed in a node only if shard state file exists in the node              */
DECL|method|shardExistsInNode
specifier|private
name|boolean
name|shardExistsInNode
parameter_list|(
specifier|final
name|NodeGatewayStartedShards
name|response
parameter_list|)
block|{
return|return
name|response
operator|.
name|storeException
argument_list|()
operator|!=
literal|null
operator|||
name|response
operator|.
name|version
argument_list|()
operator|!=
operator|-
literal|1
operator|||
name|response
operator|.
name|allocationId
argument_list|()
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|reroute
specifier|protected
name|void
name|reroute
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
comment|// no-op
block|}
DECL|class|Response
specifier|public
class|class
name|Response
block|{
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|responses
specifier|private
specifier|final
name|NodeGatewayStartedShards
index|[]
name|responses
decl_stmt|;
DECL|field|failures
specifier|private
specifier|final
name|FailedNodeException
index|[]
name|failures
decl_stmt|;
DECL|method|Response
specifier|public
name|Response
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|NodeGatewayStartedShards
index|[]
name|responses
parameter_list|,
name|FailedNodeException
index|[]
name|failures
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
name|responses
operator|=
name|responses
expr_stmt|;
name|this
operator|.
name|failures
operator|=
name|failures
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

