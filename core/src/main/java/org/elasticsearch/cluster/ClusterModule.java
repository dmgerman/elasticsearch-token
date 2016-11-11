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
name|action
operator|.
name|index
operator|.
name|MappingUpdatedAction
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
name|action
operator|.
name|index
operator|.
name|NodeMappingRefreshAction
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
name|action
operator|.
name|shard
operator|.
name|ShardStateAction
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
name|MetaDataCreateIndexService
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
name|MetaDataDeleteIndexService
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
name|MetaDataIndexAliasesService
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
name|MetaDataIndexStateService
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
name|MetaDataIndexTemplateService
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
name|MetaDataMappingService
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
name|MetaDataUpdateSettingsService
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
name|DelayedAllocationService
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
name|RoutingService
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
name|AbstractModule
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
name|plugins
operator|.
name|ClusterPlugin
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
name|TaskResultsService
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|Objects
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

begin_comment
comment|/**  * Configures classes and services that affect the entire cluster.  */
end_comment

begin_class
DECL|class|ClusterModule
specifier|public
class|class
name|ClusterModule
extends|extends
name|AbstractModule
block|{
DECL|field|BALANCED_ALLOCATOR
specifier|public
specifier|static
specifier|final
name|String
name|BALANCED_ALLOCATOR
init|=
literal|"balanced"
decl_stmt|;
comment|// default
DECL|field|SHARDS_ALLOCATOR_TYPE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|SHARDS_ALLOCATOR_TYPE_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cluster.routing.allocation.type"
argument_list|,
name|BALANCED_ALLOCATOR
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|indexNameExpressionResolver
specifier|private
specifier|final
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
decl_stmt|;
comment|// pkg private for tests
DECL|field|allocationDeciders
specifier|final
name|Collection
argument_list|<
name|AllocationDecider
argument_list|>
name|allocationDeciders
decl_stmt|;
DECL|field|shardsAllocator
specifier|final
name|ShardsAllocator
name|shardsAllocator
decl_stmt|;
comment|// pkg private so tests can mock
DECL|field|clusterInfoServiceImpl
name|Class
argument_list|<
name|?
extends|extends
name|ClusterInfoService
argument_list|>
name|clusterInfoServiceImpl
init|=
name|InternalClusterInfoService
operator|.
name|class
decl_stmt|;
DECL|method|ClusterModule
specifier|public
name|ClusterModule
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|List
argument_list|<
name|ClusterPlugin
argument_list|>
name|clusterPlugins
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
name|this
operator|.
name|allocationDeciders
operator|=
name|createAllocationDeciders
argument_list|(
name|settings
argument_list|,
name|clusterService
operator|.
name|getClusterSettings
argument_list|()
argument_list|,
name|clusterPlugins
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardsAllocator
operator|=
name|createShardsAllocator
argument_list|(
name|settings
argument_list|,
name|clusterService
operator|.
name|getClusterSettings
argument_list|()
argument_list|,
name|clusterPlugins
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|indexNameExpressionResolver
operator|=
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
DECL|method|getIndexNameExpressionResolver
specifier|public
name|IndexNameExpressionResolver
name|getIndexNameExpressionResolver
parameter_list|()
block|{
return|return
name|indexNameExpressionResolver
return|;
block|}
comment|// TODO: this is public so allocation benchmark can access the default deciders...can we do that in another way?
comment|/** Return a new {@link AllocationDecider} instance with builtin deciders as well as those from plugins. */
DECL|method|createAllocationDeciders
specifier|public
specifier|static
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
parameter_list|,
name|List
argument_list|<
name|ClusterPlugin
argument_list|>
name|clusterPlugins
parameter_list|)
block|{
comment|// collect deciders by class so that we can detect duplicates
name|Map
argument_list|<
name|Class
argument_list|,
name|AllocationDecider
argument_list|>
name|deciders
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|MaxRetryAllocationDecider
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|ReplicaAfterPrimaryActiveAllocationDecider
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|RebalanceOnlyWhenActiveAllocationDecider
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|ClusterRebalanceAllocationDecider
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|ConcurrentRebalanceAllocationDecider
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|EnableAllocationDecider
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|NodeVersionAllocationDecider
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|SnapshotInProgressAllocationDecider
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|FilterAllocationDecider
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|SameShardAllocationDecider
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|DiskThresholdDecider
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|ThrottlingAllocationDecider
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|ShardsLimitAllocationDecider
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
expr_stmt|;
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
operator|new
name|AwarenessAllocationDecider
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
expr_stmt|;
name|clusterPlugins
operator|.
name|stream
argument_list|()
operator|.
name|flatMap
argument_list|(
name|p
lambda|->
name|p
operator|.
name|createAllocationDeciders
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
operator|.
name|stream
argument_list|()
argument_list|)
operator|.
name|forEach
argument_list|(
name|d
lambda|->
name|addAllocationDecider
argument_list|(
name|deciders
argument_list|,
name|d
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|deciders
operator|.
name|values
argument_list|()
return|;
block|}
comment|/** Add the given allocation decider to the given deciders collection, erroring if the class name is already used. */
DECL|method|addAllocationDecider
specifier|private
specifier|static
name|void
name|addAllocationDecider
parameter_list|(
name|Map
argument_list|<
name|Class
argument_list|,
name|AllocationDecider
argument_list|>
name|deciders
parameter_list|,
name|AllocationDecider
name|decider
parameter_list|)
block|{
if|if
condition|(
name|deciders
operator|.
name|put
argument_list|(
name|decider
operator|.
name|getClass
argument_list|()
argument_list|,
name|decider
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot specify allocation decider ["
operator|+
name|decider
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"] twice"
argument_list|)
throw|;
block|}
block|}
DECL|method|createShardsAllocator
specifier|private
specifier|static
name|ShardsAllocator
name|createShardsAllocator
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|,
name|List
argument_list|<
name|ClusterPlugin
argument_list|>
name|clusterPlugins
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|ShardsAllocator
argument_list|>
argument_list|>
name|allocators
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|allocators
operator|.
name|put
argument_list|(
name|BALANCED_ALLOCATOR
argument_list|,
parameter_list|()
lambda|->
operator|new
name|BalancedShardsAllocator
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterPlugin
name|plugin
range|:
name|clusterPlugins
control|)
block|{
name|plugin
operator|.
name|getShardsAllocators
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
operator|.
name|forEach
argument_list|(
parameter_list|(
name|k
parameter_list|,
name|v
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|allocators
operator|.
name|put
argument_list|(
name|k
argument_list|,
name|v
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"ShardsAllocator ["
operator|+
name|k
operator|+
literal|"] already defined"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|String
name|allocatorName
init|=
name|SHARDS_ALLOCATOR_TYPE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Supplier
argument_list|<
name|ShardsAllocator
argument_list|>
name|allocatorSupplier
init|=
name|allocators
operator|.
name|get
argument_list|(
name|allocatorName
argument_list|)
decl_stmt|;
if|if
condition|(
name|allocatorSupplier
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown ShardsAllocator ["
operator|+
name|allocatorName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|allocatorSupplier
operator|.
name|get
argument_list|()
argument_list|,
literal|"ShardsAllocator factory for ["
operator|+
name|allocatorName
operator|+
literal|"] returned null"
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bind
argument_list|(
name|ClusterInfoService
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|clusterInfoServiceImpl
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|GatewayAllocator
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|AllocationService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|clusterService
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|NodeConnectionsService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|MetaDataCreateIndexService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|MetaDataDeleteIndexService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|MetaDataIndexStateService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|MetaDataMappingService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|MetaDataIndexAliasesService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|MetaDataUpdateSettingsService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|MetaDataIndexTemplateService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndexNameExpressionResolver
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|indexNameExpressionResolver
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|RoutingService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|DelayedAllocationService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|ShardStateAction
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|NodeMappingRefreshAction
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|MappingUpdatedAction
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|TaskResultsService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|AllocationDeciders
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
operator|new
name|AllocationDeciders
argument_list|(
name|settings
argument_list|,
name|allocationDeciders
argument_list|)
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|ShardsAllocator
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|shardsAllocator
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

