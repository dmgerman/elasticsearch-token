begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|admin
operator|.
name|indices
operator|.
name|close
operator|.
name|TransportCloseIndexAction
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
name|common
operator|.
name|logging
operator|.
name|ESLoggerFactory
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
name|index
operator|.
name|store
operator|.
name|IndexStoreConfig
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
name|SearchService
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
name|Transport
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

begin_comment
comment|/**  * Encapsulates all valid cluster level settings.  */
end_comment

begin_class
DECL|class|ClusterSettings
specifier|public
specifier|final
class|class
name|ClusterSettings
extends|extends
name|AbstractScopedSettings
block|{
DECL|method|ClusterSettings
specifier|public
name|ClusterSettings
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Set
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|settingsSet
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|settingsSet
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|applySettings
specifier|public
specifier|synchronized
name|Settings
name|applySettings
parameter_list|(
name|Settings
name|newSettings
parameter_list|)
block|{
name|Settings
name|settings
init|=
name|super
operator|.
name|applySettings
argument_list|(
name|newSettings
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"logger."
argument_list|)
condition|)
block|{
name|String
name|component
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|substring
argument_list|(
literal|"logger."
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"_root"
operator|.
name|equals
argument_list|(
name|component
argument_list|)
condition|)
block|{
name|ESLoggerFactory
operator|.
name|getRootLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|component
argument_list|)
operator|.
name|setLevel
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to refresh settings for [{}]"
argument_list|,
name|e
argument_list|,
literal|"logger"
argument_list|)
expr_stmt|;
block|}
return|return
name|settings
return|;
block|}
comment|/**      * Returns<code>true</code> if the settings is a logger setting.      */
DECL|method|isLoggerSetting
specifier|public
name|boolean
name|isLoggerSetting
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|key
operator|.
name|startsWith
argument_list|(
literal|"logger."
argument_list|)
return|;
block|}
DECL|field|BUILT_IN_CLUSTER_SETTINGS
specifier|public
specifier|static
name|Set
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|BUILT_IN_CLUSTER_SETTINGS
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
name|AwarenessAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_AWARENESS_ATTRIBUTE_SETTING
argument_list|,
name|AwarenessAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_AWARENESS_FORCE_GROUP_SETTING
argument_list|,
name|BalancedShardsAllocator
operator|.
name|INDEX_BALANCE_FACTOR_SETTING
argument_list|,
name|BalancedShardsAllocator
operator|.
name|SHARD_BALANCE_FACTOR_SETTING
argument_list|,
name|BalancedShardsAllocator
operator|.
name|THRESHOLD_SETTING
argument_list|,
name|ClusterRebalanceAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_ALLOW_REBALANCE_SETTING
argument_list|,
name|ConcurrentRebalanceAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE_SETTING
argument_list|,
name|EnableAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_ENABLE_SETTING
argument_list|,
name|EnableAllocationDecider
operator|.
name|CLUSTER_ROUTING_REBALANCE_ENABLE_SETTING
argument_list|,
name|ZenDiscovery
operator|.
name|REJOIN_ON_MASTER_GONE_SETTING
argument_list|,
name|FilterAllocationDecider
operator|.
name|CLUSTER_ROUTING_INCLUDE_GROUP_SETTING
argument_list|,
name|FilterAllocationDecider
operator|.
name|CLUSTER_ROUTING_EXCLUDE_GROUP_SETTING
argument_list|,
name|FilterAllocationDecider
operator|.
name|CLUSTER_ROUTING_REQUIRE_GROUP_SETTING
argument_list|,
name|IndexStoreConfig
operator|.
name|INDICES_STORE_THROTTLE_TYPE_SETTING
argument_list|,
name|IndexStoreConfig
operator|.
name|INDICES_STORE_THROTTLE_MAX_BYTES_PER_SEC_SETTING
argument_list|,
name|IndicesTTLService
operator|.
name|INDICES_TTL_INTERVAL_SETTING
argument_list|,
name|MappingUpdatedAction
operator|.
name|INDICES_MAPPING_DYNAMIC_TIMEOUT_SETTING
argument_list|,
name|MetaData
operator|.
name|SETTING_READ_ONLY_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_MAX_BYTES_PER_SEC_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_RETRY_DELAY_STATE_SYNC_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_RETRY_DELAY_NETWORK_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_ACTIVITY_TIMEOUT_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_INTERNAL_ACTION_TIMEOUT_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_INTERNAL_LONG_ACTION_TIMEOUT_SETTING
argument_list|,
name|ThreadPool
operator|.
name|THREADPOOL_GROUP_SETTING
argument_list|,
name|ThrottlingAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES_SETTING
argument_list|,
name|ThrottlingAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_INCOMING_RECOVERIES_SETTING
argument_list|,
name|ThrottlingAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_OUTGOING_RECOVERIES_SETTING
argument_list|,
name|ThrottlingAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES_SETTING
argument_list|,
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_LOW_DISK_WATERMARK_SETTING
argument_list|,
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_HIGH_DISK_WATERMARK_SETTING
argument_list|,
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED_SETTING
argument_list|,
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_INCLUDE_RELOCATIONS_SETTING
argument_list|,
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_REROUTE_INTERVAL_SETTING
argument_list|,
name|InternalClusterInfoService
operator|.
name|INTERNAL_CLUSTER_INFO_UPDATE_INTERVAL_SETTING
argument_list|,
name|InternalClusterInfoService
operator|.
name|INTERNAL_CLUSTER_INFO_TIMEOUT_SETTING
argument_list|,
name|SnapshotInProgressAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_SNAPSHOT_RELOCATION_ENABLED_SETTING
argument_list|,
name|DestructiveOperations
operator|.
name|REQUIRES_NAME_SETTING
argument_list|,
name|DiscoverySettings
operator|.
name|PUBLISH_TIMEOUT_SETTING
argument_list|,
name|DiscoverySettings
operator|.
name|PUBLISH_DIFF_ENABLE_SETTING
argument_list|,
name|DiscoverySettings
operator|.
name|COMMIT_TIMEOUT_SETTING
argument_list|,
name|DiscoverySettings
operator|.
name|NO_MASTER_BLOCK_SETTING
argument_list|,
name|HierarchyCircuitBreakerService
operator|.
name|TOTAL_CIRCUIT_BREAKER_LIMIT_SETTING
argument_list|,
name|HierarchyCircuitBreakerService
operator|.
name|FIELDDATA_CIRCUIT_BREAKER_LIMIT_SETTING
argument_list|,
name|HierarchyCircuitBreakerService
operator|.
name|FIELDDATA_CIRCUIT_BREAKER_OVERHEAD_SETTING
argument_list|,
name|HierarchyCircuitBreakerService
operator|.
name|REQUEST_CIRCUIT_BREAKER_LIMIT_SETTING
argument_list|,
name|HierarchyCircuitBreakerService
operator|.
name|REQUEST_CIRCUIT_BREAKER_OVERHEAD_SETTING
argument_list|,
name|InternalClusterService
operator|.
name|CLUSTER_SERVICE_SLOW_TASK_LOGGING_THRESHOLD_SETTING
argument_list|,
name|SearchService
operator|.
name|DEFAULT_SEARCH_TIMEOUT_SETTING
argument_list|,
name|ElectMasterService
operator|.
name|DISCOVERY_ZEN_MINIMUM_MASTER_NODES_SETTING
argument_list|,
name|TransportService
operator|.
name|TRACE_LOG_EXCLUDE_SETTING
argument_list|,
name|TransportService
operator|.
name|TRACE_LOG_INCLUDE_SETTING
argument_list|,
name|TransportCloseIndexAction
operator|.
name|CLUSTER_INDICES_CLOSE_ENABLE_SETTING
argument_list|,
name|ShardsLimitAllocationDecider
operator|.
name|CLUSTER_TOTAL_SHARDS_PER_NODE_SETTING
argument_list|,
name|InternalClusterService
operator|.
name|CLUSTER_SERVICE_RECONNECT_INTERVAL_SETTING
argument_list|,
name|HierarchyCircuitBreakerService
operator|.
name|FIELDDATA_CIRCUIT_BREAKER_TYPE_SETTING
argument_list|,
name|HierarchyCircuitBreakerService
operator|.
name|REQUEST_CIRCUIT_BREAKER_TYPE_SETTING
argument_list|,
name|Transport
operator|.
name|TRANSPORT_PROFILES_SETTING
argument_list|,
name|Transport
operator|.
name|TRANSPORT_TCP_COMPRESS
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
block|}
end_class

end_unit

