begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|ClusterInfoService
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
name|ClusterModule
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
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|AllocationService
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
name|FailedRerouteAllocation
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
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|StartedRerouteAllocation
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
name|allocator
operator|.
name|ShardsAllocator
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
name|Randomness
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
name|ClusterSettings
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
name|transport
operator|.
name|DummyTransportAddress
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
name|ReplicaShardAllocator
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
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
name|Random
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
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRoutingState
operator|.
name|INITIALIZING
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|CollectionUtils
operator|.
name|arrayAsArrayList
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|is
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ESAllocationTestCase
specifier|public
specifier|abstract
class|class
name|ESAllocationTestCase
extends|extends
name|ESTestCase
block|{
DECL|method|createAllocationService
specifier|public
specifier|static
name|MockAllocationService
name|createAllocationService
parameter_list|()
block|{
return|return
name|createAllocationService
argument_list|(
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
return|;
block|}
DECL|method|createAllocationService
specifier|public
specifier|static
name|MockAllocationService
name|createAllocationService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
name|createAllocationService
argument_list|(
name|settings
argument_list|,
name|getRandom
argument_list|()
argument_list|)
return|;
block|}
DECL|method|createAllocationService
specifier|public
specifier|static
name|MockAllocationService
name|createAllocationService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Random
name|random
parameter_list|)
block|{
return|return
name|createAllocationService
argument_list|(
name|settings
argument_list|,
operator|new
name|ClusterSettings
argument_list|(
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
argument_list|,
name|random
argument_list|)
return|;
block|}
DECL|method|createAllocationService
specifier|public
specifier|static
name|MockAllocationService
name|createAllocationService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|,
name|Random
name|random
parameter_list|)
block|{
return|return
operator|new
name|MockAllocationService
argument_list|(
name|settings
argument_list|,
name|randomAllocationDeciders
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|,
name|random
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
DECL|method|createAllocationService
specifier|public
specifier|static
name|MockAllocationService
name|createAllocationService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterInfoService
name|clusterInfoService
parameter_list|)
block|{
return|return
operator|new
name|MockAllocationService
argument_list|(
name|settings
argument_list|,
name|randomAllocationDeciders
argument_list|(
name|settings
argument_list|,
operator|new
name|ClusterSettings
argument_list|(
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
argument_list|,
name|getRandom
argument_list|()
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
name|clusterInfoService
argument_list|)
return|;
block|}
DECL|method|createAllocationService
specifier|public
specifier|static
name|MockAllocationService
name|createAllocationService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|GatewayAllocator
name|gatewayAllocator
parameter_list|)
block|{
return|return
operator|new
name|MockAllocationService
argument_list|(
name|settings
argument_list|,
name|randomAllocationDeciders
argument_list|(
name|settings
argument_list|,
operator|new
name|ClusterSettings
argument_list|(
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
argument_list|,
name|getRandom
argument_list|()
argument_list|)
argument_list|,
name|gatewayAllocator
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
DECL|method|randomAllocationDeciders
specifier|public
specifier|static
name|AllocationDeciders
name|randomAllocationDeciders
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|,
name|Random
name|random
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
argument_list|>
name|defaultAllocationDeciders
init|=
name|ClusterModule
operator|.
name|DEFAULT_ALLOCATION_DECIDERS
decl_stmt|;
specifier|final
name|List
argument_list|<
name|AllocationDecider
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
name|deciderClass
range|:
name|ClusterModule
operator|.
name|DEFAULT_ALLOCATION_DECIDERS
control|)
block|{
try|try
block|{
try|try
block|{
name|Constructor
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
name|constructor
init|=
name|deciderClass
operator|.
name|getConstructor
argument_list|(
name|Settings
operator|.
name|class
argument_list|,
name|ClusterSettings
operator|.
name|class
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|constructor
operator|.
name|newInstance
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
name|Constructor
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
name|constructor
init|=
literal|null
decl_stmt|;
name|constructor
operator|=
name|deciderClass
operator|.
name|getConstructor
argument_list|(
name|Settings
operator|.
name|class
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
name|constructor
operator|.
name|newInstance
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
name|assertThat
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|defaultAllocationDeciders
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|AllocationDecider
name|d
range|:
name|list
control|)
block|{
name|assertThat
argument_list|(
name|defaultAllocationDeciders
operator|.
name|contains
argument_list|(
name|d
operator|.
name|getClass
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Randomness
operator|.
name|shuffle
argument_list|(
name|list
argument_list|)
expr_stmt|;
return|return
operator|new
name|AllocationDeciders
argument_list|(
name|settings
argument_list|,
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|AllocationDecider
index|[
name|list
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
DECL|field|MASTER_DATA_ROLES
specifier|protected
specifier|static
name|Set
argument_list|<
name|DiscoveryNode
operator|.
name|Role
argument_list|>
name|MASTER_DATA_ROLES
init|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|DiscoveryNode
operator|.
name|Role
operator|.
name|MASTER
argument_list|,
name|DiscoveryNode
operator|.
name|Role
operator|.
name|DATA
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
DECL|method|newNode
specifier|protected
specifier|static
name|DiscoveryNode
name|newNode
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
return|return
name|newNode
argument_list|(
name|nodeId
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
DECL|method|newNode
specifier|protected
specifier|static
name|DiscoveryNode
name|newNode
parameter_list|(
name|String
name|nodeName
parameter_list|,
name|String
name|nodeId
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
parameter_list|)
block|{
return|return
operator|new
name|DiscoveryNode
argument_list|(
name|nodeName
argument_list|,
name|nodeId
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|attributes
argument_list|,
name|MASTER_DATA_ROLES
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
DECL|method|newNode
specifier|protected
specifier|static
name|DiscoveryNode
name|newNode
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
parameter_list|)
block|{
return|return
operator|new
name|DiscoveryNode
argument_list|(
name|nodeId
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|attributes
argument_list|,
name|MASTER_DATA_ROLES
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
DECL|method|newNode
specifier|protected
specifier|static
name|DiscoveryNode
name|newNode
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|Set
argument_list|<
name|DiscoveryNode
operator|.
name|Role
argument_list|>
name|roles
parameter_list|)
block|{
return|return
operator|new
name|DiscoveryNode
argument_list|(
name|nodeId
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|roles
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
DECL|method|newNode
specifier|protected
specifier|static
name|DiscoveryNode
name|newNode
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|DiscoveryNode
argument_list|(
name|nodeId
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|MASTER_DATA_ROLES
argument_list|,
name|version
argument_list|)
return|;
block|}
DECL|method|startRandomInitializingShard
specifier|protected
specifier|static
name|ClusterState
name|startRandomInitializingShard
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|AllocationService
name|strategy
parameter_list|)
block|{
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|initializingShards
init|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
decl_stmt|;
if|if
condition|(
name|initializingShards
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|clusterState
return|;
block|}
name|RoutingTable
name|routingTable
init|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|arrayAsArrayList
argument_list|(
name|initializingShards
operator|.
name|get
argument_list|(
name|randomInt
argument_list|(
name|initializingShards
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|routingTable
argument_list|()
decl_stmt|;
return|return
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
return|;
block|}
DECL|method|yesAllocationDeciders
specifier|protected
specifier|static
name|AllocationDeciders
name|yesAllocationDeciders
parameter_list|()
block|{
return|return
operator|new
name|AllocationDeciders
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|AllocationDecider
index|[]
block|{
operator|new
name|TestAllocateDecision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|)
block|}
argument_list|)
return|;
block|}
DECL|method|noAllocationDeciders
specifier|protected
specifier|static
name|AllocationDeciders
name|noAllocationDeciders
parameter_list|()
block|{
return|return
operator|new
name|AllocationDeciders
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|AllocationDecider
index|[]
block|{
operator|new
name|TestAllocateDecision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|)
block|}
argument_list|)
return|;
block|}
DECL|method|throttleAllocationDeciders
specifier|protected
specifier|static
name|AllocationDeciders
name|throttleAllocationDeciders
parameter_list|()
block|{
return|return
operator|new
name|AllocationDeciders
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|AllocationDecider
index|[]
block|{
operator|new
name|TestAllocateDecision
argument_list|(
name|Decision
operator|.
name|THROTTLE
argument_list|)
block|}
argument_list|)
return|;
block|}
DECL|class|TestAllocateDecision
specifier|public
specifier|static
class|class
name|TestAllocateDecision
extends|extends
name|AllocationDecider
block|{
DECL|field|decision
specifier|private
specifier|final
name|Decision
name|decision
decl_stmt|;
DECL|method|TestAllocateDecision
specifier|public
name|TestAllocateDecision
parameter_list|(
name|Decision
name|decision
parameter_list|)
block|{
name|super
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|this
operator|.
name|decision
operator|=
name|decision
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|canAllocate
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
return|return
name|decision
return|;
block|}
annotation|@
name|Override
DECL|method|canAllocate
specifier|public
name|Decision
name|canAllocate
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
return|return
name|decision
return|;
block|}
annotation|@
name|Override
DECL|method|canAllocate
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
name|decision
return|;
block|}
block|}
comment|/** A lock {@link AllocationService} allowing tests to override time */
DECL|class|MockAllocationService
specifier|protected
specifier|static
class|class
name|MockAllocationService
extends|extends
name|AllocationService
block|{
DECL|field|nanoTimeOverride
specifier|private
name|Long
name|nanoTimeOverride
init|=
literal|null
decl_stmt|;
DECL|method|MockAllocationService
specifier|public
name|MockAllocationService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|AllocationDeciders
name|allocationDeciders
parameter_list|,
name|GatewayAllocator
name|gatewayAllocator
parameter_list|,
name|ShardsAllocator
name|shardsAllocator
parameter_list|,
name|ClusterInfoService
name|clusterInfoService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|allocationDeciders
argument_list|,
name|gatewayAllocator
argument_list|,
name|shardsAllocator
argument_list|,
name|clusterInfoService
argument_list|)
expr_stmt|;
block|}
DECL|method|setNanoTimeOverride
specifier|public
name|void
name|setNanoTimeOverride
parameter_list|(
name|long
name|nanoTime
parameter_list|)
block|{
name|this
operator|.
name|nanoTimeOverride
operator|=
name|nanoTime
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|currentNanoTime
specifier|protected
name|long
name|currentNanoTime
parameter_list|()
block|{
return|return
name|nanoTimeOverride
operator|==
literal|null
condition|?
name|super
operator|.
name|currentNanoTime
argument_list|()
else|:
name|nanoTimeOverride
return|;
block|}
block|}
comment|/**      * Mocks behavior in ReplicaShardAllocator to remove delayed shards from list of unassigned shards so they don't get reassigned yet.      */
DECL|class|DelayedShardsMockGatewayAllocator
specifier|protected
specifier|static
class|class
name|DelayedShardsMockGatewayAllocator
extends|extends
name|GatewayAllocator
block|{
DECL|field|replicaShardAllocator
specifier|private
specifier|final
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
name|TransportNodesListShardStoreMetaData
operator|.
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
return|return
operator|new
name|AsyncShardFetch
operator|.
name|FetchResult
argument_list|<>
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
expr|<
name|String
operator|>
name|emptySet
argument_list|()
argument_list|,
name|Collections
operator|.
expr|<
name|String
operator|>
name|emptySet
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|method|DelayedShardsMockGatewayAllocator
specifier|public
name|DelayedShardsMockGatewayAllocator
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
name|StartedRerouteAllocation
name|allocation
parameter_list|)
block|{}
annotation|@
name|Override
DECL|method|applyFailedShards
specifier|public
name|void
name|applyFailedShards
parameter_list|(
name|FailedRerouteAllocation
name|allocation
parameter_list|)
block|{}
annotation|@
name|Override
DECL|method|allocateUnassigned
specifier|public
name|boolean
name|allocateUnassigned
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
specifier|final
name|RoutingNodes
operator|.
name|UnassignedShards
operator|.
name|UnassignedIterator
name|unassignedIterator
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|unassigned
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|unassignedIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|ShardRouting
name|shard
init|=
name|unassignedIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|allocation
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|shard
operator|.
name|getIndexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
operator|||
name|shard
operator|.
name|allocatedPostIndexCreate
argument_list|(
name|indexMetaData
argument_list|)
operator|==
literal|false
condition|)
block|{
continue|continue;
block|}
name|changed
operator||=
name|replicaShardAllocator
operator|.
name|ignoreUnassignedIfDelayed
argument_list|(
name|unassignedIterator
argument_list|,
name|shard
argument_list|)
expr_stmt|;
block|}
return|return
name|changed
return|;
block|}
block|}
block|}
end_class

end_unit

