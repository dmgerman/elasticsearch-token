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
name|NodeIndexDeletedAction
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
name|IndexTemplateFilter
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
name|node
operator|.
name|DiscoveryNodeService
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
name|OperationRouting
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
name|UnassignedInfo
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
name|InternalClusterService
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
name|settings
operator|.
name|DynamicSettings
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
name|settings
operator|.
name|Validator
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
name|logging
operator|.
name|Loggers
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
name|ExtensionPoint
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
name|index
operator|.
name|IndexSettings
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
name|engine
operator|.
name|EngineConfig
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
name|indexing
operator|.
name|IndexingSlowLog
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
name|search
operator|.
name|stats
operator|.
name|SearchSlowLog
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
name|settings
operator|.
name|IndexDynamicSettings
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
name|IndexShard
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
name|MergePolicyConfig
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
name|MergeSchedulerConfig
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
name|store
operator|.
name|IndexStore
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
name|IndicesWarmer
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
name|cache
operator|.
name|request
operator|.
name|IndicesRequestCache
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
name|ttl
operator|.
name|IndicesTTLService
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
name|DefaultSearchContext
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
name|List
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
DECL|field|EVEN_SHARD_COUNT_ALLOCATOR
specifier|public
specifier|static
specifier|final
name|String
name|EVEN_SHARD_COUNT_ALLOCATOR
init|=
literal|"even_shard"
decl_stmt|;
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
DECL|field|SHARDS_ALLOCATOR_TYPE_KEY
specifier|public
specifier|static
specifier|final
name|String
name|SHARDS_ALLOCATOR_TYPE_KEY
init|=
literal|"cluster.routing.allocation.type"
decl_stmt|;
DECL|field|DEFAULT_ALLOCATION_DECIDERS
specifier|public
specifier|static
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
name|DEFAULT_ALLOCATION_DECIDERS
init|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|SameShardAllocationDecider
operator|.
name|class
argument_list|,
name|FilterAllocationDecider
operator|.
name|class
argument_list|,
name|ReplicaAfterPrimaryActiveAllocationDecider
operator|.
name|class
argument_list|,
name|ThrottlingAllocationDecider
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
name|AwarenessAllocationDecider
operator|.
name|class
argument_list|,
name|ShardsLimitAllocationDecider
operator|.
name|class
argument_list|,
name|NodeVersionAllocationDecider
operator|.
name|class
argument_list|,
name|DiskThresholdDecider
operator|.
name|class
argument_list|,
name|SnapshotInProgressAllocationDecider
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|indexDynamicSettings
specifier|private
specifier|final
name|DynamicSettings
operator|.
name|Builder
name|indexDynamicSettings
init|=
operator|new
name|DynamicSettings
operator|.
name|Builder
argument_list|()
decl_stmt|;
DECL|field|shardsAllocators
specifier|private
specifier|final
name|ExtensionPoint
operator|.
name|SelectedType
argument_list|<
name|ShardsAllocator
argument_list|>
name|shardsAllocators
init|=
operator|new
name|ExtensionPoint
operator|.
name|SelectedType
argument_list|<>
argument_list|(
literal|"shards_allocator"
argument_list|,
name|ShardsAllocator
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|allocationDeciders
specifier|private
specifier|final
name|ExtensionPoint
operator|.
name|ClassSet
argument_list|<
name|AllocationDecider
argument_list|>
name|allocationDeciders
init|=
operator|new
name|ExtensionPoint
operator|.
name|ClassSet
argument_list|<>
argument_list|(
literal|"allocation_decider"
argument_list|,
name|AllocationDecider
operator|.
name|class
argument_list|,
name|AllocationDeciders
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|indexTemplateFilters
specifier|private
specifier|final
name|ExtensionPoint
operator|.
name|ClassSet
argument_list|<
name|IndexTemplateFilter
argument_list|>
name|indexTemplateFilters
init|=
operator|new
name|ExtensionPoint
operator|.
name|ClassSet
argument_list|<>
argument_list|(
literal|"index_template_filter"
argument_list|,
name|IndexTemplateFilter
operator|.
name|class
argument_list|)
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
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
name|registerBuiltinIndexSettings
argument_list|()
expr_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
name|decider
range|:
name|ClusterModule
operator|.
name|DEFAULT_ALLOCATION_DECIDERS
control|)
block|{
name|registerAllocationDecider
argument_list|(
name|decider
argument_list|)
expr_stmt|;
block|}
name|registerShardsAllocator
argument_list|(
name|ClusterModule
operator|.
name|BALANCED_ALLOCATOR
argument_list|,
name|BalancedShardsAllocator
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerShardsAllocator
argument_list|(
name|ClusterModule
operator|.
name|EVEN_SHARD_COUNT_ALLOCATOR
argument_list|,
name|BalancedShardsAllocator
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|method|registerBuiltinIndexSettings
specifier|private
name|void
name|registerBuiltinIndexSettings
parameter_list|()
block|{
name|registerIndexDynamicSetting
argument_list|(
name|IndexStore
operator|.
name|INDEX_STORE_THROTTLE_MAX_BYTES_PER_SEC
argument_list|,
name|Validator
operator|.
name|BYTES_SIZE
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexStore
operator|.
name|INDEX_STORE_THROTTLE_TYPE
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergeSchedulerConfig
operator|.
name|MAX_THREAD_COUNT
argument_list|,
name|Validator
operator|.
name|NON_NEGATIVE_INTEGER
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergeSchedulerConfig
operator|.
name|MAX_MERGE_COUNT
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergeSchedulerConfig
operator|.
name|AUTO_THROTTLE
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|FilterAllocationDecider
operator|.
name|INDEX_ROUTING_REQUIRE_GROUP
operator|+
literal|"*"
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|FilterAllocationDecider
operator|.
name|INDEX_ROUTING_INCLUDE_GROUP
operator|+
literal|"*"
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|FilterAllocationDecider
operator|.
name|INDEX_ROUTING_EXCLUDE_GROUP
operator|+
literal|"*"
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|EnableAllocationDecider
operator|.
name|INDEX_ROUTING_ALLOCATION_ENABLE
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|EnableAllocationDecider
operator|.
name|INDEX_ROUTING_REBALANCE_ENABLE
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
name|Validator
operator|.
name|NON_NEGATIVE_INTEGER
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_AUTO_EXPAND_REPLICAS
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_READ_ONLY
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_BLOCKS_READ
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_BLOCKS_WRITE
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_BLOCKS_METADATA
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_SHARED_FS_ALLOW_RECOVERY_ON_ANY_NODE
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_PRIORITY
argument_list|,
name|Validator
operator|.
name|NON_NEGATIVE_INTEGER
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndicesTTLService
operator|.
name|INDEX_TTL_DISABLE_PURGE
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexShard
operator|.
name|INDEX_REFRESH_INTERVAL
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|PrimaryShardAllocator
operator|.
name|INDEX_RECOVERY_INITIAL_SHARDS
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|EngineConfig
operator|.
name|INDEX_GC_DELETES_SETTING
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexShard
operator|.
name|INDEX_FLUSH_ON_CLOSE
argument_list|,
name|Validator
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|EngineConfig
operator|.
name|INDEX_VERSION_MAP_SIZE
argument_list|,
name|Validator
operator|.
name|BYTES_SIZE_OR_PERCENTAGE
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexingSlowLog
operator|.
name|INDEX_INDEXING_SLOWLOG_THRESHOLD_INDEX_WARN
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexingSlowLog
operator|.
name|INDEX_INDEXING_SLOWLOG_THRESHOLD_INDEX_INFO
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexingSlowLog
operator|.
name|INDEX_INDEXING_SLOWLOG_THRESHOLD_INDEX_DEBUG
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexingSlowLog
operator|.
name|INDEX_INDEXING_SLOWLOG_THRESHOLD_INDEX_TRACE
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexingSlowLog
operator|.
name|INDEX_INDEXING_SLOWLOG_REFORMAT
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexingSlowLog
operator|.
name|INDEX_INDEXING_SLOWLOG_LEVEL
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexingSlowLog
operator|.
name|INDEX_INDEXING_SLOWLOG_MAX_SOURCE_CHARS_TO_LOG
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|SearchSlowLog
operator|.
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_WARN
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|SearchSlowLog
operator|.
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_INFO
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|SearchSlowLog
operator|.
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_DEBUG
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|SearchSlowLog
operator|.
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_QUERY_TRACE
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|SearchSlowLog
operator|.
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_WARN
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|SearchSlowLog
operator|.
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_INFO
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|SearchSlowLog
operator|.
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_DEBUG
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|SearchSlowLog
operator|.
name|INDEX_SEARCH_SLOWLOG_THRESHOLD_FETCH_TRACE
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|SearchSlowLog
operator|.
name|INDEX_SEARCH_SLOWLOG_REFORMAT
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|SearchSlowLog
operator|.
name|INDEX_SEARCH_SLOWLOG_LEVEL
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|ShardsLimitAllocationDecider
operator|.
name|INDEX_TOTAL_SHARDS_PER_NODE
argument_list|,
name|Validator
operator|.
name|INTEGER
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_EXPUNGE_DELETES_ALLOWED
argument_list|,
name|Validator
operator|.
name|DOUBLE
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_FLOOR_SEGMENT
argument_list|,
name|Validator
operator|.
name|BYTES_SIZE
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE
argument_list|,
name|Validator
operator|.
name|INTEGER_GTE_2
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_MAX_MERGE_AT_ONCE_EXPLICIT
argument_list|,
name|Validator
operator|.
name|INTEGER_GTE_2
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_MAX_MERGED_SEGMENT
argument_list|,
name|Validator
operator|.
name|BYTES_SIZE
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_SEGMENTS_PER_TIER
argument_list|,
name|Validator
operator|.
name|DOUBLE_GTE_2
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_POLICY_RECLAIM_DELETES_WEIGHT
argument_list|,
name|Validator
operator|.
name|NON_NEGATIVE_DOUBLE
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_COMPOUND_FORMAT
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexShard
operator|.
name|INDEX_TRANSLOG_FLUSH_THRESHOLD_SIZE
argument_list|,
name|Validator
operator|.
name|BYTES_SIZE
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndexSettings
operator|.
name|INDEX_TRANSLOG_DURABILITY
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndicesWarmer
operator|.
name|INDEX_WARMER_ENABLED
argument_list|,
name|Validator
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|IndicesRequestCache
operator|.
name|INDEX_CACHE_REQUEST_ENABLED
argument_list|,
name|Validator
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|UnassignedInfo
operator|.
name|INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|registerIndexDynamicSetting
argument_list|(
name|DefaultSearchContext
operator|.
name|MAX_RESULT_WINDOW
argument_list|,
name|Validator
operator|.
name|POSITIVE_INTEGER
argument_list|)
expr_stmt|;
block|}
DECL|method|registerIndexDynamicSetting
specifier|public
name|void
name|registerIndexDynamicSetting
parameter_list|(
name|String
name|setting
parameter_list|,
name|Validator
name|validator
parameter_list|)
block|{
name|indexDynamicSettings
operator|.
name|addSetting
argument_list|(
name|setting
argument_list|,
name|validator
argument_list|)
expr_stmt|;
block|}
DECL|method|registerAllocationDecider
specifier|public
name|void
name|registerAllocationDecider
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
name|allocationDecider
parameter_list|)
block|{
name|allocationDeciders
operator|.
name|registerExtension
argument_list|(
name|allocationDecider
argument_list|)
expr_stmt|;
block|}
DECL|method|registerShardsAllocator
specifier|public
name|void
name|registerShardsAllocator
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|ShardsAllocator
argument_list|>
name|clazz
parameter_list|)
block|{
name|shardsAllocators
operator|.
name|registerExtension
argument_list|(
name|name
argument_list|,
name|clazz
argument_list|)
expr_stmt|;
block|}
DECL|method|registerIndexTemplateFilter
specifier|public
name|void
name|registerIndexTemplateFilter
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|IndexTemplateFilter
argument_list|>
name|indexTemplateFilter
parameter_list|)
block|{
name|indexTemplateFilters
operator|.
name|registerExtension
argument_list|(
name|indexTemplateFilter
argument_list|)
expr_stmt|;
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
name|DynamicSettings
operator|.
name|class
argument_list|)
operator|.
name|annotatedWith
argument_list|(
name|IndexDynamicSettings
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|indexDynamicSettings
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// bind ShardsAllocator
name|String
name|shardsAllocatorType
init|=
name|shardsAllocators
operator|.
name|bindType
argument_list|(
name|binder
argument_list|()
argument_list|,
name|settings
argument_list|,
name|ClusterModule
operator|.
name|SHARDS_ALLOCATOR_TYPE_KEY
argument_list|,
name|ClusterModule
operator|.
name|BALANCED_ALLOCATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardsAllocatorType
operator|.
name|equals
argument_list|(
name|ClusterModule
operator|.
name|EVEN_SHARD_COUNT_ALLOCATOR
argument_list|)
condition|)
block|{
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|logger
operator|.
name|warn
argument_list|(
literal|"{} allocator has been removed in 2.0 using {} instead"
argument_list|,
name|ClusterModule
operator|.
name|EVEN_SHARD_COUNT_ALLOCATOR
argument_list|,
name|ClusterModule
operator|.
name|BALANCED_ALLOCATOR
argument_list|)
expr_stmt|;
block|}
name|allocationDeciders
operator|.
name|bind
argument_list|(
name|binder
argument_list|()
argument_list|)
expr_stmt|;
name|indexTemplateFilters
operator|.
name|bind
argument_list|(
name|binder
argument_list|()
argument_list|)
expr_stmt|;
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
name|DiscoveryNodeService
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
name|to
argument_list|(
name|InternalClusterService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|OperationRouting
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
name|asEagerSingleton
argument_list|()
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
name|NodeIndexDeletedAction
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
block|}
block|}
end_class

end_unit

