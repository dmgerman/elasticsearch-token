begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|ClusterName
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
name|EmptyClusterInfoService
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
name|ClusterStateHealth
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
name|IndexMetaData
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
name|RoutingNode
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
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|allocator
operator|.
name|BalancedShardsAllocator
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
name|decider
operator|.
name|AllocationDecider
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
name|decider
operator|.
name|AllocationDeciders
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
name|decider
operator|.
name|Decision
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
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESAllocationTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|gateway
operator|.
name|NoopGatewayAllocator
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
name|Collections
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  * This class of tests exercise various scenarios of  * primary shard allocation and assert the cluster health  * has the correct status based on those allocation decisions.  */
end_comment

begin_class
DECL|class|DecisionsImpactOnClusterHealthTests
specifier|public
class|class
name|DecisionsImpactOnClusterHealthTests
extends|extends
name|ESAllocationTestCase
block|{
DECL|method|testPrimaryShardNoDecisionOnIndexCreation
specifier|public
name|void
name|testPrimaryShardNoDecisionOnIndexCreation
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|indexName
init|=
literal|"test-idx"
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AllocationDecider
name|decider
init|=
operator|new
name|TestAllocateDecision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|)
decl_stmt|;
comment|// if deciders say NO to allocating a primary shard, then the cluster health should be RED
name|runAllocationTest
argument_list|(
name|settings
argument_list|,
name|indexName
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|decider
argument_list|)
argument_list|,
name|ClusterHealthStatus
operator|.
name|RED
argument_list|)
expr_stmt|;
block|}
DECL|method|testPrimaryShardThrottleDecisionOnIndexCreation
specifier|public
name|void
name|testPrimaryShardThrottleDecisionOnIndexCreation
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|indexName
init|=
literal|"test-idx"
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AllocationDecider
name|decider
init|=
operator|new
name|TestAllocateDecision
argument_list|(
name|Decision
operator|.
name|THROTTLE
argument_list|)
block|{
comment|// the only allocation decider that implements this is ShardsLimitAllocationDecider and it always
comment|// returns only YES or NO, never THROTTLE
annotation|@
name|Override
specifier|public
name|Decision
name|canAllocate
parameter_list|(
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
return|return
name|randomBoolean
argument_list|()
condition|?
name|Decision
operator|.
name|YES
else|:
name|Decision
operator|.
name|NO
return|;
block|}
block|}
decl_stmt|;
comment|// if deciders THROTTLE allocating a primary shard, stay in YELLOW state
name|runAllocationTest
argument_list|(
name|settings
argument_list|,
name|indexName
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|decider
argument_list|)
argument_list|,
name|ClusterHealthStatus
operator|.
name|YELLOW
argument_list|)
expr_stmt|;
block|}
DECL|method|testPrimaryShardYesDecisionOnIndexCreation
specifier|public
name|void
name|testPrimaryShardYesDecisionOnIndexCreation
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|indexName
init|=
literal|"test-idx"
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AllocationDecider
name|decider
init|=
operator|new
name|TestAllocateDecision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Decision
name|canAllocate
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
if|if
condition|(
name|node
operator|.
name|getByShardId
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return
name|Decision
operator|.
name|YES
return|;
block|}
else|else
block|{
return|return
name|Decision
operator|.
name|NO
return|;
block|}
block|}
block|}
decl_stmt|;
comment|// if deciders say YES to allocating primary shards, stay in YELLOW state
name|ClusterState
name|clusterState
init|=
name|runAllocationTest
argument_list|(
name|settings
argument_list|,
name|indexName
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|decider
argument_list|)
argument_list|,
name|ClusterHealthStatus
operator|.
name|YELLOW
argument_list|)
decl_stmt|;
comment|// make sure primaries are initialized
name|RoutingTable
name|routingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|routingTable
operator|.
name|index
argument_list|(
name|indexName
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
name|indexShardRoutingTable
operator|.
name|primaryShard
argument_list|()
operator|.
name|initializing
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|runAllocationTest
specifier|private
name|ClusterState
name|runAllocationTest
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|,
specifier|final
name|String
name|indexName
parameter_list|,
specifier|final
name|Set
argument_list|<
name|AllocationDecider
argument_list|>
name|allocationDeciders
parameter_list|,
specifier|final
name|ClusterHealthStatus
name|expectedStatus
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|String
name|clusterName
init|=
literal|"test-cluster"
decl_stmt|;
specifier|final
name|AllocationService
name|allocationService
init|=
name|newAllocationService
argument_list|(
name|settings
argument_list|,
name|allocationDeciders
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Building initial routing table"
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numShards
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexName
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
name|numShards
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|addAsNew
argument_list|(
name|metaData
operator|.
name|index
argument_list|(
name|indexName
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
name|clusterName
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> adding nodes"
argument_list|)
expr_stmt|;
comment|// we need at least as many nodes as shards for the THROTTLE case, because
comment|// once a shard has been throttled on a node, that node no longer accepts
comment|// any allocations on it
specifier|final
name|DiscoveryNodes
operator|.
name|Builder
name|discoveryNodes
init|=
name|DiscoveryNodes
operator|.
name|builder
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
name|numShards
condition|;
name|i
operator|++
control|)
block|{
name|discoveryNodes
operator|.
name|add
argument_list|(
name|newNode
argument_list|(
literal|"node"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|nodes
argument_list|(
name|discoveryNodes
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> do the reroute"
argument_list|)
expr_stmt|;
name|routingTable
operator|=
name|allocationService
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
literal|"reroute"
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> assert cluster health"
argument_list|)
expr_stmt|;
name|ClusterStateHealth
name|health
init|=
operator|new
name|ClusterStateHealth
argument_list|(
name|clusterState
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|health
operator|.
name|getStatus
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedStatus
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|clusterState
return|;
block|}
DECL|method|newAllocationService
specifier|private
specifier|static
name|AllocationService
name|newAllocationService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Set
argument_list|<
name|AllocationDecider
argument_list|>
name|deciders
parameter_list|)
block|{
return|return
operator|new
name|AllocationService
argument_list|(
name|settings
argument_list|,
operator|new
name|AllocationDeciders
argument_list|(
name|settings
argument_list|,
name|deciders
argument_list|)
argument_list|,
name|NoopGatewayAllocator
operator|.
name|INSTANCE
argument_list|,
operator|new
name|BalancedShardsAllocator
argument_list|(
name|settings
argument_list|)
argument_list|,
name|EmptyClusterInfoService
operator|.
name|INSTANCE
argument_list|)
return|;
block|}
block|}
end_class

end_unit

