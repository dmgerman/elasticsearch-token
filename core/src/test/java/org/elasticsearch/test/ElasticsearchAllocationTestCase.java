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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

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
name|allocator
operator|.
name|ShardsAllocators
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
name|AllocationDecidersModule
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
name|common
operator|.
name|transport
operator|.
name|TransportAddress
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
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|settings
operator|.
name|NodeSettingsService
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
name|*
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|newArrayList
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
DECL|class|ElasticsearchAllocationTestCase
specifier|public
specifier|abstract
class|class
name|ElasticsearchAllocationTestCase
extends|extends
name|ElasticsearchTestCase
block|{
DECL|method|createAllocationService
specifier|public
specifier|static
name|AllocationService
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
name|AllocationService
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
name|AllocationService
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
name|NodeSettingsService
argument_list|(
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
name|random
argument_list|)
return|;
block|}
DECL|method|createAllocationService
specifier|public
specifier|static
name|AllocationService
name|createAllocationService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|,
name|Random
name|random
parameter_list|)
block|{
return|return
operator|new
name|AllocationService
argument_list|(
name|settings
argument_list|,
name|randomAllocationDeciders
argument_list|(
name|settings
argument_list|,
name|nodeSettingsService
argument_list|,
name|random
argument_list|)
argument_list|,
operator|new
name|ShardsAllocators
argument_list|(
name|settings
argument_list|,
name|NoopGatewayAllocator
operator|.
name|INSTANCE
argument_list|)
argument_list|,
name|ClusterInfoService
operator|.
name|EMPTY
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
name|NodeSettingsService
name|nodeSettingsService
parameter_list|,
name|Random
name|random
parameter_list|)
block|{
specifier|final
name|ImmutableSet
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
name|AllocationDecidersModule
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
name|defaultAllocationDeciders
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
name|NodeSettingsService
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
name|nodeSettingsService
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
name|Collections
operator|.
name|shuffle
argument_list|(
name|list
argument_list|,
name|random
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
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
DECL|method|newNode
specifier|public
specifier|static
name|DiscoveryNode
name|newNode
parameter_list|(
name|String
name|nodeId
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
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
DECL|method|newNode
specifier|public
specifier|static
name|DiscoveryNode
name|newNode
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|TransportAddress
name|address
parameter_list|)
block|{
return|return
operator|new
name|DiscoveryNode
argument_list|(
name|nodeId
argument_list|,
name|address
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
DECL|method|newNode
specifier|public
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
literal|""
argument_list|,
name|nodeId
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|attributes
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
DECL|method|newNode
specifier|public
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
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
DECL|method|newNode
specifier|public
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
name|version
argument_list|)
return|;
block|}
DECL|method|startRandomInitializingShard
specifier|public
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
name|routingNodes
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
name|newArrayList
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
specifier|public
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
specifier|public
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
specifier|public
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
block|}
end_class

end_unit

