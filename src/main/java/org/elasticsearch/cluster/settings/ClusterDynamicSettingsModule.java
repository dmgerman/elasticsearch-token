begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|settings
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
name|support
operator|.
name|DestructiveOperations
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
name|InternalClusterInfoService
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
name|*
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
name|discovery
operator|.
name|DiscoverySettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|ZenDiscovery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|elect
operator|.
name|ElectMasterService
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
name|breaker
operator|.
name|HierarchyCircuitBreakerService
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
name|filter
operator|.
name|IndicesFilterCache
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
name|recovery
operator|.
name|RecoverySettings
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
name|IndicesStore
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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ClusterDynamicSettingsModule
specifier|public
class|class
name|ClusterDynamicSettingsModule
extends|extends
name|AbstractModule
block|{
DECL|field|clusterDynamicSettings
specifier|private
specifier|final
name|DynamicSettings
name|clusterDynamicSettings
decl_stmt|;
DECL|method|ClusterDynamicSettingsModule
specifier|public
name|ClusterDynamicSettingsModule
parameter_list|()
block|{
name|clusterDynamicSettings
operator|=
operator|new
name|DynamicSettings
argument_list|()
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|AwarenessAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_AWARENESS_ATTRIBUTES
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|AwarenessAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_AWARENESS_FORCE_GROUP
operator|+
literal|"*"
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|BalancedShardsAllocator
operator|.
name|SETTING_INDEX_BALANCE_FACTOR
argument_list|,
name|Validator
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|BalancedShardsAllocator
operator|.
name|SETTING_PRIMARY_BALANCE_FACTOR
argument_list|,
name|Validator
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|BalancedShardsAllocator
operator|.
name|SETTING_SHARD_BALANCE_FACTOR
argument_list|,
name|Validator
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|BalancedShardsAllocator
operator|.
name|SETTING_THRESHOLD
argument_list|,
name|Validator
operator|.
name|NON_NEGATIVE_FLOAT
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|ClusterRebalanceAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_ALLOW_REBALANCE
argument_list|,
name|ClusterRebalanceAllocationDecider
operator|.
name|ALLOCATION_ALLOW_REBALANCE_VALIDATOR
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|ConcurrentRebalanceAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE
argument_list|,
name|Validator
operator|.
name|INTEGER
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|EnableAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_ENABLE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|DisableAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_DISABLE_NEW_ALLOCATION
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|DisableAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_DISABLE_ALLOCATION
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|DisableAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_DISABLE_REPLICA_ALLOCATION
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|ElectMasterService
operator|.
name|DISCOVERY_ZEN_MINIMUM_MASTER_NODES
argument_list|,
name|Validator
operator|.
name|INTEGER
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|ZenDiscovery
operator|.
name|SETTING_REJOIN_ON_MASTER_GONE
argument_list|,
name|Validator
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|DiscoverySettings
operator|.
name|NO_MASTER_BLOCK
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|FilterAllocationDecider
operator|.
name|CLUSTER_ROUTING_INCLUDE_GROUP
operator|+
literal|"*"
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|FilterAllocationDecider
operator|.
name|CLUSTER_ROUTING_EXCLUDE_GROUP
operator|+
literal|"*"
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|FilterAllocationDecider
operator|.
name|CLUSTER_ROUTING_REQUIRE_GROUP
operator|+
literal|"*"
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|IndicesFilterCache
operator|.
name|INDICES_CACHE_FILTER_SIZE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|IndicesFilterCache
operator|.
name|INDICES_CACHE_FILTER_EXPIRE
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|IndicesStore
operator|.
name|INDICES_STORE_THROTTLE_TYPE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|IndicesStore
operator|.
name|INDICES_STORE_THROTTLE_MAX_BYTES_PER_SEC
argument_list|,
name|Validator
operator|.
name|BYTES_SIZE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|IndicesTTLService
operator|.
name|INDICES_TTL_INTERVAL
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|MappingUpdatedAction
operator|.
name|INDICES_MAPPING_ADDITIONAL_MAPPING_CHANGE_TIME
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|MetaData
operator|.
name|SETTING_READ_ONLY
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_FILE_CHUNK_SIZE
argument_list|,
name|Validator
operator|.
name|BYTES_SIZE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_TRANSLOG_OPS
argument_list|,
name|Validator
operator|.
name|INTEGER
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_TRANSLOG_SIZE
argument_list|,
name|Validator
operator|.
name|BYTES_SIZE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_COMPRESS
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_CONCURRENT_STREAMS
argument_list|,
name|Validator
operator|.
name|POSITIVE_INTEGER
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_CONCURRENT_SMALL_FILE_STREAMS
argument_list|,
name|Validator
operator|.
name|POSITIVE_INTEGER
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_MAX_BYTES_PER_SEC
argument_list|,
name|Validator
operator|.
name|BYTES_SIZE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_MAX_SIZE_PER_SEC
argument_list|,
name|Validator
operator|.
name|BYTES_SIZE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|ThreadPool
operator|.
name|THREADPOOL_GROUP
operator|+
literal|"*"
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|ThrottlingAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES
argument_list|,
name|Validator
operator|.
name|INTEGER
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|ThrottlingAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES
argument_list|,
name|Validator
operator|.
name|INTEGER
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_LOW_DISK_WATERMARK
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_HIGH_DISK_WATERMARK
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_INCLUDE_RELOCATIONS
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|InternalClusterInfoService
operator|.
name|INTERNAL_CLUSTER_INFO_UPDATE_INTERVAL
argument_list|,
name|Validator
operator|.
name|TIME
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|SnapshotInProgressAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_SNAPSHOT_RELOCATION_ENABLED
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|DestructiveOperations
operator|.
name|REQUIRES_NAME
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|DiscoverySettings
operator|.
name|PUBLISH_TIMEOUT
argument_list|,
name|Validator
operator|.
name|TIME_NON_NEGATIVE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|HierarchyCircuitBreakerService
operator|.
name|TOTAL_CIRCUIT_BREAKER_LIMIT_SETTING
argument_list|,
name|Validator
operator|.
name|MEMORY_SIZE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|HierarchyCircuitBreakerService
operator|.
name|FIELDDATA_CIRCUIT_BREAKER_LIMIT_SETTING
argument_list|,
name|Validator
operator|.
name|MEMORY_SIZE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|HierarchyCircuitBreakerService
operator|.
name|FIELDDATA_CIRCUIT_BREAKER_OVERHEAD_SETTING
argument_list|,
name|Validator
operator|.
name|NON_NEGATIVE_DOUBLE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|HierarchyCircuitBreakerService
operator|.
name|REQUEST_CIRCUIT_BREAKER_LIMIT_SETTING
argument_list|,
name|Validator
operator|.
name|MEMORY_SIZE
argument_list|)
expr_stmt|;
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|HierarchyCircuitBreakerService
operator|.
name|REQUEST_CIRCUIT_BREAKER_OVERHEAD_SETTING
argument_list|,
name|Validator
operator|.
name|NON_NEGATIVE_DOUBLE
argument_list|)
expr_stmt|;
block|}
DECL|method|addDynamicSettings
specifier|public
name|void
name|addDynamicSettings
parameter_list|(
name|String
modifier|...
name|settings
parameter_list|)
block|{
name|clusterDynamicSettings
operator|.
name|addDynamicSettings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
DECL|method|addDynamicSetting
specifier|public
name|void
name|addDynamicSetting
parameter_list|(
name|String
name|setting
parameter_list|,
name|Validator
name|validator
parameter_list|)
block|{
name|clusterDynamicSettings
operator|.
name|addDynamicSetting
argument_list|(
name|setting
argument_list|,
name|validator
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
name|ClusterDynamicSettings
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|clusterDynamicSettings
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

