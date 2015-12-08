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
name|*
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
name|Settings
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
name|TransportService
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

begin_comment
comment|/**  * Encapsulates all valid cluster level settings.  */
end_comment

begin_class
DECL|class|ClusterSettings
specifier|public
specifier|final
class|class
name|ClusterSettings
block|{
DECL|field|groupSettings
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|groupSettings
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|keySettings
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|keySettings
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|ClusterSettings
specifier|public
name|ClusterSettings
parameter_list|(
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
for|for
control|(
name|Setting
argument_list|<
name|?
argument_list|>
name|entry
range|:
name|settingsSet
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getScope
argument_list|()
operator|!=
name|Setting
operator|.
name|Scope
operator|.
name|Cluster
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Setting must be a cluster setting but was: "
operator|+
name|entry
operator|.
name|getScope
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|entry
operator|.
name|isGroupSetting
argument_list|()
condition|)
block|{
name|groupSettings
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|keySettings
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|ClusterSettings
specifier|public
name|ClusterSettings
parameter_list|()
block|{
name|this
argument_list|(
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the {@link Setting} for the given key or<code>null</code> if the setting can not be found.      */
DECL|method|get
specifier|public
name|Setting
name|get
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|Setting
argument_list|<
name|?
argument_list|>
name|setting
init|=
name|keySettings
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|setting
operator|==
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|entry
range|:
name|groupSettings
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|match
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
name|entry
operator|.
name|getValue
argument_list|()
return|;
block|}
block|}
block|}
else|else
block|{
return|return
name|setting
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**      * Returns<code>true</code> if the setting for the given key is dynamically updateable. Otherwise<code>false</code>.      */
DECL|method|hasDynamicSetting
specifier|public
name|boolean
name|hasDynamicSetting
parameter_list|(
name|String
name|key
parameter_list|)
block|{
specifier|final
name|Setting
name|setting
init|=
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|setting
operator|!=
literal|null
operator|&&
name|setting
operator|.
name|isDynamic
argument_list|()
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
comment|/**      * Returns a settings object that contains all clustersettings that are not      * already set in the given source. The diff contains either the default value for each      * setting or the settings value in the given default settings.      */
DECL|method|diff
specifier|public
name|Settings
name|diff
parameter_list|(
name|Settings
name|source
parameter_list|,
name|Settings
name|defaultSettings
parameter_list|)
block|{
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Setting
argument_list|<
name|?
argument_list|>
name|setting
range|:
name|keySettings
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|setting
operator|.
name|exists
argument_list|(
name|source
argument_list|)
operator|==
literal|false
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|setting
operator|.
name|getKey
argument_list|()
argument_list|,
name|setting
operator|.
name|getRaw
argument_list|(
name|defaultSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
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
name|INDICES_RECOVERY_FILE_CHUNK_SIZE_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_TRANSLOG_OPS_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_TRANSLOG_SIZE_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_COMPRESS_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_CONCURRENT_STREAMS_SETTING
argument_list|,
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_CONCURRENT_SMALL_FILE_STREAMS_SETTING
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
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
block|}
end_class

end_unit

