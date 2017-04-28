begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
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
name|ShardAllocationDecision
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
name|AwarenessAllocationDecider
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
name|ClusterRebalanceAllocationDecider
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
name|ConcurrentRebalanceAllocationDecider
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
name|DiskThresholdDecider
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
name|EnableAllocationDecider
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
name|FilterAllocationDecider
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
name|MaxRetryAllocationDecider
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
name|NodeVersionAllocationDecider
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
name|RebalanceOnlyWhenActiveAllocationDecider
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
name|ReplicaAfterPrimaryActiveAllocationDecider
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
name|SameShardAllocationDecider
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
name|ShardsLimitAllocationDecider
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
name|SnapshotInProgressAllocationDecider
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
name|ThrottlingAllocationDecider
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
name|ModuleTestCase
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
name|IndexScopedSettings
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
name|common
operator|.
name|settings
operator|.
name|SettingsModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|ClusterPlugin
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
name|Collections
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
name|function
operator|.
name|Supplier
import|;
end_import

begin_class
DECL|class|ClusterModuleTests
specifier|public
class|class
name|ClusterModuleTests
extends|extends
name|ModuleTestCase
block|{
DECL|field|clusterService
specifier|private
name|ClusterService
name|clusterService
init|=
operator|new
name|ClusterService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|ClusterSettings
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
DECL|class|FakeAllocationDecider
specifier|static
class|class
name|FakeAllocationDecider
extends|extends
name|AllocationDecider
block|{
DECL|method|FakeAllocationDecider
specifier|protected
name|FakeAllocationDecider
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|FakeShardsAllocator
specifier|static
class|class
name|FakeShardsAllocator
implements|implements
name|ShardsAllocator
block|{
annotation|@
name|Override
DECL|method|allocate
specifier|public
name|void
name|allocate
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
comment|// noop
block|}
annotation|@
name|Override
DECL|method|decideShardAllocation
specifier|public
name|ShardAllocationDecision
name|decideShardAllocation
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"explain API not supported on FakeShardsAllocator"
argument_list|)
throw|;
block|}
block|}
DECL|method|testRegisterClusterDynamicSettingDuplicate
specifier|public
name|void
name|testRegisterClusterDynamicSettingDuplicate
parameter_list|()
block|{
try|try
block|{
operator|new
name|SettingsModule
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|EnableAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_ENABLE_SETTING
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Cannot register setting ["
operator|+
name|EnableAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_ENABLE_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|"] twice"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRegisterClusterDynamicSetting
specifier|public
name|void
name|testRegisterClusterDynamicSetting
parameter_list|()
block|{
name|SettingsModule
name|module
init|=
operator|new
name|SettingsModule
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"foo.bar"
argument_list|,
literal|false
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
argument_list|)
decl_stmt|;
name|assertInstanceBinding
argument_list|(
name|module
argument_list|,
name|ClusterSettings
operator|.
name|class
argument_list|,
name|service
lambda|->
name|service
operator|.
name|isDynamicSetting
argument_list|(
literal|"foo.bar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegisterIndexDynamicSettingDuplicate
specifier|public
name|void
name|testRegisterIndexDynamicSettingDuplicate
parameter_list|()
block|{
try|try
block|{
operator|new
name|SettingsModule
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|EnableAllocationDecider
operator|.
name|INDEX_ROUTING_ALLOCATION_ENABLE_SETTING
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Cannot register setting ["
operator|+
name|EnableAllocationDecider
operator|.
name|INDEX_ROUTING_ALLOCATION_ENABLE_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|"] twice"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRegisterIndexDynamicSetting
specifier|public
name|void
name|testRegisterIndexDynamicSetting
parameter_list|()
block|{
name|SettingsModule
name|module
init|=
operator|new
name|SettingsModule
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"index.foo.bar"
argument_list|,
literal|false
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|IndexScope
argument_list|)
argument_list|)
decl_stmt|;
name|assertInstanceBinding
argument_list|(
name|module
argument_list|,
name|IndexScopedSettings
operator|.
name|class
argument_list|,
name|service
lambda|->
name|service
operator|.
name|isDynamicSetting
argument_list|(
literal|"index.foo.bar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegisterAllocationDeciderDuplicate
specifier|public
name|void
name|testRegisterAllocationDeciderDuplicate
parameter_list|()
block|{
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|ClusterModule
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|clusterService
argument_list|,
name|Collections
operator|.
block|<ClusterPlugin>singletonList(new ClusterPlugin(
argument_list|)
block|{                     @
name|Override
specifier|public
name|Collection
argument_list|<
name|AllocationDecider
argument_list|>
name|createAllocationDeciders
argument_list|(
name|Settings
name|settings
argument_list|,
name|ClusterSettings
name|clusterSettings
argument_list|)
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|EnableAllocationDecider
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
return|;
block|}
expr|}
block|))
init|)
decl_stmt|;
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Cannot specify allocation decider ["
operator|+
name|EnableAllocationDecider
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"] twice"
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegisterAllocationDecider
specifier|public
name|void
name|testRegisterAllocationDecider
parameter_list|()
block|{
name|ClusterModule
name|module
init|=
operator|new
name|ClusterModule
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|clusterService
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|ClusterPlugin
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|AllocationDecider
argument_list|>
name|createAllocationDeciders
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|FakeAllocationDecider
argument_list|(
name|settings
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|module
operator|.
name|allocationDeciders
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|d
lambda|->
name|d
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|FakeAllocationDecider
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|newClusterModuleWithShardsAllocator
specifier|private
name|ClusterModule
name|newClusterModuleWithShardsAllocator
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|name
parameter_list|,
name|Supplier
argument_list|<
name|ShardsAllocator
argument_list|>
name|supplier
parameter_list|)
block|{
return|return
operator|new
name|ClusterModule
argument_list|(
name|settings
argument_list|,
name|clusterService
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|ClusterPlugin
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|ShardsAllocator
argument_list|>
argument_list|>
name|getShardsAllocators
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|singletonMap
argument_list|(
name|name
argument_list|,
name|supplier
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
return|;
block|}
DECL|method|testRegisterShardsAllocator
specifier|public
name|void
name|testRegisterShardsAllocator
parameter_list|()
block|{
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
name|ClusterModule
operator|.
name|SHARDS_ALLOCATOR_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"custom"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterModule
name|module
init|=
name|newClusterModuleWithShardsAllocator
argument_list|(
name|settings
argument_list|,
literal|"custom"
argument_list|,
name|FakeShardsAllocator
operator|::
operator|new
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|FakeShardsAllocator
operator|.
name|class
argument_list|,
name|module
operator|.
name|shardsAllocator
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegisterShardsAllocatorAlreadyRegistered
specifier|public
name|void
name|testRegisterShardsAllocatorAlreadyRegistered
parameter_list|()
block|{
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|newClusterModuleWithShardsAllocator
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|ClusterModule
operator|.
name|BALANCED_ALLOCATOR
argument_list|,
name|FakeShardsAllocator
operator|::
operator|new
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"ShardsAllocator ["
operator|+
name|ClusterModule
operator|.
name|BALANCED_ALLOCATOR
operator|+
literal|"] already defined"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnknownShardsAllocator
specifier|public
name|void
name|testUnknownShardsAllocator
parameter_list|()
block|{
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
name|ClusterModule
operator|.
name|SHARDS_ALLOCATOR_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"dne"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|ClusterModule
argument_list|(
name|settings
argument_list|,
name|clusterService
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Unknown ShardsAllocator [dne]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testShardsAllocatorFactoryNull
specifier|public
name|void
name|testShardsAllocatorFactoryNull
parameter_list|()
block|{
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
name|ClusterModule
operator|.
name|SHARDS_ALLOCATOR_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"bad"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NullPointerException
name|e
init|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|newClusterModuleWithShardsAllocator
argument_list|(
name|settings
argument_list|,
literal|"bad"
argument_list|,
parameter_list|()
lambda|->
literal|null
argument_list|)
argument_list|)
decl_stmt|;
block|}
comment|// makes sure that the allocation deciders are setup in the correct order, such that the
comment|// slower allocation deciders come last and we can exit early if there is a NO decision without
comment|// running them. If the order of the deciders is changed for a valid reason, the order should be
comment|// changed in the test too.
DECL|method|testAllocationDeciderOrder
specifier|public
name|void
name|testAllocationDeciderOrder
parameter_list|()
block|{
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
argument_list|>
name|expectedDeciders
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|MaxRetryAllocationDecider
operator|.
name|class
argument_list|,
name|ReplicaAfterPrimaryActiveAllocationDecider
operator|.
name|class
argument_list|,
name|RebalanceOnlyWhenActiveAllocationDecider
operator|.
name|class
argument_list|,
name|ClusterRebalanceAllocationDecider
operator|.
name|class
argument_list|,
name|ConcurrentRebalanceAllocationDecider
operator|.
name|class
argument_list|,
name|EnableAllocationDecider
operator|.
name|class
argument_list|,
name|NodeVersionAllocationDecider
operator|.
name|class
argument_list|,
name|SnapshotInProgressAllocationDecider
operator|.
name|class
argument_list|,
name|FilterAllocationDecider
operator|.
name|class
argument_list|,
name|SameShardAllocationDecider
operator|.
name|class
argument_list|,
name|DiskThresholdDecider
operator|.
name|class
argument_list|,
name|ThrottlingAllocationDecider
operator|.
name|class
argument_list|,
name|ShardsLimitAllocationDecider
operator|.
name|class
argument_list|,
name|AwarenessAllocationDecider
operator|.
name|class
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|AllocationDecider
argument_list|>
name|deciders
init|=
name|ClusterModule
operator|.
name|createAllocationDeciders
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|ClusterSettings
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|AllocationDecider
argument_list|>
name|iter
init|=
name|deciders
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|int
name|idx
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|AllocationDecider
name|decider
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertSame
argument_list|(
name|decider
operator|.
name|getClass
argument_list|()
argument_list|,
name|expectedDeciders
operator|.
name|get
argument_list|(
name|idx
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

