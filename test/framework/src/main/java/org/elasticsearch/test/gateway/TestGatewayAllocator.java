begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|gateway
package|;
end_package

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
name|ShardRouting
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
name|FailedShard
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
name|GatewayAllocator
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
name|PrimaryShardAllocator
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
name|ReplicaShardAllocator
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
name|indices
operator|.
name|store
operator|.
name|TransportNodesListShardStoreMetaData
operator|.
name|NodeStoreFilesMetaData
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
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  * A gateway allocator implementation that keeps an in memory list of started shard allocation  * that are used as replies to the, normally async, fetch data requests. The in memory list  * is adapted when shards are started and failed.  *  * Nodes leaving and joining the cluster do not change the list of shards the class tracks but  * rather serves as a filter to what is returned by fetch data. Concretely - fetch data will  * only return shards that were started on nodes that are currently part of the cluster.  *  * For now only primary shard related data is fetched. Replica request always get an empty response.  *  *  * This class is useful to use in unit tests that require the functionality of {@link GatewayAllocator} but do  * not have all the infrastructure required to use it.  */
end_comment

begin_class
DECL|class|TestGatewayAllocator
specifier|public
class|class
name|TestGatewayAllocator
extends|extends
name|GatewayAllocator
block|{
DECL|field|knownAllocations
name|Map
argument_list|<
name|String
comment|/* node id */
argument_list|,
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ShardRouting
argument_list|>
argument_list|>
name|knownAllocations
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|currentNodes
name|DiscoveryNodes
name|currentNodes
init|=
name|DiscoveryNodes
operator|.
name|EMPTY_NODES
decl_stmt|;
DECL|field|primaryShardAllocator
name|PrimaryShardAllocator
name|primaryShardAllocator
init|=
operator|new
name|PrimaryShardAllocator
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|AsyncShardFetch
operator|.
name|FetchResult
argument_list|<
name|NodeGatewayStartedShards
argument_list|>
name|fetchData
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
comment|// for now always return immediately what we know
specifier|final
name|ShardId
name|shardId
init|=
name|shard
operator|.
name|shardId
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|ignoreNodes
init|=
name|allocation
operator|.
name|getIgnoreNodes
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|NodeGatewayStartedShards
argument_list|>
name|foundShards
init|=
name|knownAllocations
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|flatMap
argument_list|(
name|shardMap
lambda|->
name|shardMap
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|ks
lambda|->
name|ks
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|shardId
argument_list|)
argument_list|)
operator|.
name|filter
argument_list|(
name|ks
lambda|->
name|ignoreNodes
operator|.
name|contains
argument_list|(
name|ks
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|==
literal|false
argument_list|)
operator|.
name|filter
argument_list|(
name|ks
lambda|->
name|currentNodes
operator|.
name|nodeExists
argument_list|(
name|ks
operator|.
name|currentNodeId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|routing
lambda|->
name|currentNodes
operator|.
name|get
argument_list|(
name|routing
operator|.
name|currentNodeId
argument_list|()
argument_list|)
argument_list|,
name|routing
lambda|->
operator|new
name|NodeGatewayStartedShards
argument_list|(
name|currentNodes
operator|.
name|get
argument_list|(
name|routing
operator|.
name|currentNodeId
argument_list|()
argument_list|)
argument_list|,
name|routing
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|routing
operator|.
name|primary
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|AsyncShardFetch
operator|.
name|FetchResult
argument_list|<>
argument_list|(
name|shardId
argument_list|,
name|foundShards
argument_list|,
name|ignoreNodes
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|replicaShardAllocator
name|ReplicaShardAllocator
name|replicaShardAllocator
init|=
operator|new
name|ReplicaShardAllocator
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|AsyncShardFetch
operator|.
name|FetchResult
argument_list|<
name|NodeStoreFilesMetaData
argument_list|>
name|fetchData
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
comment|// for now, just pretend no node has data
specifier|final
name|ShardId
name|shardId
init|=
name|shard
operator|.
name|shardId
argument_list|()
decl_stmt|;
return|return
operator|new
name|AsyncShardFetch
operator|.
name|FetchResult
argument_list|<>
argument_list|(
name|shardId
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|allocation
operator|.
name|getIgnoreNodes
argument_list|(
name|shardId
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|hasInitiatedFetching
parameter_list|(
name|ShardRouting
name|shard
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
DECL|method|TestGatewayAllocator
specifier|public
name|TestGatewayAllocator
parameter_list|()
block|{
name|super
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|applyStartedShards
specifier|public
name|void
name|applyStartedShards
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|,
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|startedShards
parameter_list|)
block|{
name|currentNodes
operator|=
name|allocation
operator|.
name|nodes
argument_list|()
expr_stmt|;
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|shards
argument_list|(
name|ShardRouting
operator|::
name|active
argument_list|)
operator|.
name|forEach
argument_list|(
name|this
operator|::
name|addKnownAllocation
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|applyFailedShards
specifier|public
name|void
name|applyFailedShards
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|,
name|List
argument_list|<
name|FailedShard
argument_list|>
name|failedShards
parameter_list|)
block|{
name|currentNodes
operator|=
name|allocation
operator|.
name|nodes
argument_list|()
expr_stmt|;
for|for
control|(
name|FailedShard
name|failedShard
range|:
name|failedShards
control|)
block|{
specifier|final
name|ShardRouting
name|failedRouting
init|=
name|failedShard
operator|.
name|getRoutingEntry
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ShardRouting
argument_list|>
name|nodeAllocations
init|=
name|knownAllocations
operator|.
name|get
argument_list|(
name|failedRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodeAllocations
operator|!=
literal|null
condition|)
block|{
name|nodeAllocations
operator|.
name|remove
argument_list|(
name|failedRouting
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|nodeAllocations
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|knownAllocations
operator|.
name|remove
argument_list|(
name|failedRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|allocateUnassigned
specifier|public
name|void
name|allocateUnassigned
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
name|currentNodes
operator|=
name|allocation
operator|.
name|nodes
argument_list|()
expr_stmt|;
name|innerAllocatedUnassigned
argument_list|(
name|allocation
argument_list|,
name|primaryShardAllocator
argument_list|,
name|replicaShardAllocator
argument_list|)
expr_stmt|;
block|}
comment|/**      * manually add a specific shard to the allocations the gateway keeps track of      */
DECL|method|addKnownAllocation
specifier|public
name|void
name|addKnownAllocation
parameter_list|(
name|ShardRouting
name|shard
parameter_list|)
block|{
name|knownAllocations
operator|.
name|computeIfAbsent
argument_list|(
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|id
lambda|->
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|shard
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

