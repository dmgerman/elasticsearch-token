begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|RateLimiter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|RateLimiter
operator|.
name|SimpleRateLimiter
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
name|component
operator|.
name|AbstractComponent
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
name|Inject
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
name|unit
operator|.
name|ByteSizeUnit
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
name|unit
operator|.
name|ByteSizeValue
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
name|unit
operator|.
name|TimeValue
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
name|concurrent
operator|.
name|EsExecutors
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
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadPoolExecutor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|RecoverySettings
specifier|public
class|class
name|RecoverySettings
extends|extends
name|AbstractComponent
implements|implements
name|Closeable
block|{
DECL|field|INDICES_RECOVERY_CONCURRENT_STREAMS_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|INDICES_RECOVERY_CONCURRENT_STREAMS_SETTING
init|=
name|Setting
operator|.
name|intSetting
argument_list|(
literal|"indices.recovery.concurrent_streams"
argument_list|,
literal|3
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
DECL|field|INDICES_RECOVERY_CONCURRENT_SMALL_FILE_STREAMS_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|INDICES_RECOVERY_CONCURRENT_SMALL_FILE_STREAMS_SETTING
init|=
name|Setting
operator|.
name|intSetting
argument_list|(
literal|"indices.recovery.concurrent_small_file_streams"
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
DECL|field|INDICES_RECOVERY_MAX_BYTES_PER_SEC_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|ByteSizeValue
argument_list|>
name|INDICES_RECOVERY_MAX_BYTES_PER_SEC_SETTING
init|=
name|Setting
operator|.
name|byteSizeSetting
argument_list|(
literal|"indices.recovery.max_bytes_per_sec"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|40
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
comment|/**      * how long to wait before retrying after issues cause by cluster state syncing between nodes      * i.e., local node is not yet known on remote node, remote shard not yet started etc.      */
DECL|field|INDICES_RECOVERY_RETRY_DELAY_STATE_SYNC_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDICES_RECOVERY_RETRY_DELAY_STATE_SYNC_SETTING
init|=
name|Setting
operator|.
name|positiveTimeSetting
argument_list|(
literal|"indices.recovery.retry_delay_state_sync"
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|500
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
comment|/** how long to wait before retrying after network related issues */
DECL|field|INDICES_RECOVERY_RETRY_DELAY_NETWORK_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDICES_RECOVERY_RETRY_DELAY_NETWORK_SETTING
init|=
name|Setting
operator|.
name|positiveTimeSetting
argument_list|(
literal|"indices.recovery.retry_delay_network"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|5
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
comment|/** timeout value to use for requests made as part of the recovery process */
DECL|field|INDICES_RECOVERY_INTERNAL_ACTION_TIMEOUT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDICES_RECOVERY_INTERNAL_ACTION_TIMEOUT_SETTING
init|=
name|Setting
operator|.
name|positiveTimeSetting
argument_list|(
literal|"indices.recovery.internal_action_timeout"
argument_list|,
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|15
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
comment|/**      * timeout value to use for requests made as part of the recovery process that are expected to take long time.      * defaults to twice `indices.recovery.internal_action_timeout`.      */
DECL|field|INDICES_RECOVERY_INTERNAL_LONG_ACTION_TIMEOUT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDICES_RECOVERY_INTERNAL_LONG_ACTION_TIMEOUT_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
literal|"indices.recovery.internal_action_long_timeout"
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|INDICES_RECOVERY_INTERNAL_ACTION_TIMEOUT_SETTING
operator|.
name|get
argument_list|(
name|s
argument_list|)
operator|.
name|millis
argument_list|()
operator|*
literal|2
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
comment|/**      * recoveries that don't show any activity for more then this interval will be failed.      * defaults to `indices.recovery.internal_action_long_timeout`      */
DECL|field|INDICES_RECOVERY_ACTIVITY_TIMEOUT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDICES_RECOVERY_ACTIVITY_TIMEOUT_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
literal|"indices.recovery.recovery_activity_timeout"
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|INDICES_RECOVERY_INTERNAL_LONG_ACTION_TIMEOUT_SETTING
operator|.
name|getRaw
argument_list|(
name|s
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
DECL|field|SMALL_FILE_CUTOFF_BYTES
specifier|public
specifier|static
specifier|final
name|long
name|SMALL_FILE_CUTOFF_BYTES
init|=
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"5mb"
argument_list|,
literal|"SMALL_FILE_CUTOFF_BYTES"
argument_list|)
operator|.
name|bytes
argument_list|()
decl_stmt|;
DECL|field|DEFAULT_CHUNK_SIZE
specifier|public
specifier|static
specifier|final
name|ByteSizeValue
name|DEFAULT_CHUNK_SIZE
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|512
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
decl_stmt|;
DECL|field|concurrentStreams
specifier|private
specifier|volatile
name|int
name|concurrentStreams
decl_stmt|;
DECL|field|concurrentSmallFileStreams
specifier|private
specifier|volatile
name|int
name|concurrentSmallFileStreams
decl_stmt|;
DECL|field|concurrentStreamPool
specifier|private
specifier|final
name|ThreadPoolExecutor
name|concurrentStreamPool
decl_stmt|;
DECL|field|concurrentSmallFileStreamPool
specifier|private
specifier|final
name|ThreadPoolExecutor
name|concurrentSmallFileStreamPool
decl_stmt|;
DECL|field|maxBytesPerSec
specifier|private
specifier|volatile
name|ByteSizeValue
name|maxBytesPerSec
decl_stmt|;
DECL|field|rateLimiter
specifier|private
specifier|volatile
name|SimpleRateLimiter
name|rateLimiter
decl_stmt|;
DECL|field|retryDelayStateSync
specifier|private
specifier|volatile
name|TimeValue
name|retryDelayStateSync
decl_stmt|;
DECL|field|retryDelayNetwork
specifier|private
specifier|volatile
name|TimeValue
name|retryDelayNetwork
decl_stmt|;
DECL|field|activityTimeout
specifier|private
specifier|volatile
name|TimeValue
name|activityTimeout
decl_stmt|;
DECL|field|internalActionTimeout
specifier|private
specifier|volatile
name|TimeValue
name|internalActionTimeout
decl_stmt|;
DECL|field|internalActionLongTimeout
specifier|private
specifier|volatile
name|TimeValue
name|internalActionLongTimeout
decl_stmt|;
DECL|field|chunkSize
specifier|private
specifier|volatile
name|ByteSizeValue
name|chunkSize
init|=
name|DEFAULT_CHUNK_SIZE
decl_stmt|;
annotation|@
name|Inject
DECL|method|RecoverySettings
specifier|public
name|RecoverySettings
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|retryDelayStateSync
operator|=
name|INDICES_RECOVERY_RETRY_DELAY_STATE_SYNC_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
comment|// doesn't have to be fast as nodes are reconnected every 10s by default (see InternalClusterService.ReconnectToNodes)
comment|// and we want to give the master time to remove a faulty node
name|this
operator|.
name|retryDelayNetwork
operator|=
name|INDICES_RECOVERY_RETRY_DELAY_NETWORK_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|internalActionTimeout
operator|=
name|INDICES_RECOVERY_INTERNAL_ACTION_TIMEOUT_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|internalActionLongTimeout
operator|=
name|INDICES_RECOVERY_INTERNAL_LONG_ACTION_TIMEOUT_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|activityTimeout
operator|=
name|INDICES_RECOVERY_ACTIVITY_TIMEOUT_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|concurrentStreams
operator|=
name|INDICES_RECOVERY_CONCURRENT_STREAMS_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|concurrentStreamPool
operator|=
name|EsExecutors
operator|.
name|newScaling
argument_list|(
literal|"recovery_stream"
argument_list|,
literal|0
argument_list|,
name|concurrentStreams
argument_list|,
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"[recovery_stream]"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|concurrentSmallFileStreams
operator|=
name|INDICES_RECOVERY_CONCURRENT_SMALL_FILE_STREAMS_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|concurrentSmallFileStreamPool
operator|=
name|EsExecutors
operator|.
name|newScaling
argument_list|(
literal|"small_file_recovery_stream"
argument_list|,
literal|0
argument_list|,
name|concurrentSmallFileStreams
argument_list|,
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"[small_file_recovery_stream]"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxBytesPerSec
operator|=
name|INDICES_RECOVERY_MAX_BYTES_PER_SEC_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
if|if
condition|(
name|maxBytesPerSec
operator|.
name|bytes
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|rateLimiter
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|rateLimiter
operator|=
operator|new
name|SimpleRateLimiter
argument_list|(
name|maxBytesPerSec
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"using max_bytes_per_sec[{}], concurrent_streams [{}]"
argument_list|,
name|maxBytesPerSec
argument_list|,
name|concurrentStreams
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDICES_RECOVERY_CONCURRENT_STREAMS_SETTING
argument_list|,
name|this
operator|::
name|setConcurrentStreams
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDICES_RECOVERY_CONCURRENT_SMALL_FILE_STREAMS_SETTING
argument_list|,
name|this
operator|::
name|setConcurrentSmallFileStreams
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDICES_RECOVERY_MAX_BYTES_PER_SEC_SETTING
argument_list|,
name|this
operator|::
name|setMaxBytesPerSec
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDICES_RECOVERY_RETRY_DELAY_STATE_SYNC_SETTING
argument_list|,
name|this
operator|::
name|setRetryDelayStateSync
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDICES_RECOVERY_RETRY_DELAY_NETWORK_SETTING
argument_list|,
name|this
operator|::
name|setRetryDelayNetwork
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDICES_RECOVERY_INTERNAL_ACTION_TIMEOUT_SETTING
argument_list|,
name|this
operator|::
name|setInternalActionTimeout
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDICES_RECOVERY_INTERNAL_LONG_ACTION_TIMEOUT_SETTING
argument_list|,
name|this
operator|::
name|setInternalActionLongTimeout
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|INDICES_RECOVERY_ACTIVITY_TIMEOUT_SETTING
argument_list|,
name|this
operator|::
name|setActivityTimeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|concurrentStreamPool
argument_list|,
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|concurrentSmallFileStreamPool
argument_list|,
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
DECL|method|concurrentStreamPool
specifier|public
name|ThreadPoolExecutor
name|concurrentStreamPool
parameter_list|()
block|{
return|return
name|concurrentStreamPool
return|;
block|}
DECL|method|concurrentSmallFileStreamPool
specifier|public
name|ThreadPoolExecutor
name|concurrentSmallFileStreamPool
parameter_list|()
block|{
return|return
name|concurrentSmallFileStreamPool
return|;
block|}
DECL|method|rateLimiter
specifier|public
name|RateLimiter
name|rateLimiter
parameter_list|()
block|{
return|return
name|rateLimiter
return|;
block|}
DECL|method|retryDelayNetwork
specifier|public
name|TimeValue
name|retryDelayNetwork
parameter_list|()
block|{
return|return
name|retryDelayNetwork
return|;
block|}
DECL|method|retryDelayStateSync
specifier|public
name|TimeValue
name|retryDelayStateSync
parameter_list|()
block|{
return|return
name|retryDelayStateSync
return|;
block|}
DECL|method|activityTimeout
specifier|public
name|TimeValue
name|activityTimeout
parameter_list|()
block|{
return|return
name|activityTimeout
return|;
block|}
DECL|method|internalActionTimeout
specifier|public
name|TimeValue
name|internalActionTimeout
parameter_list|()
block|{
return|return
name|internalActionTimeout
return|;
block|}
DECL|method|internalActionLongTimeout
specifier|public
name|TimeValue
name|internalActionLongTimeout
parameter_list|()
block|{
return|return
name|internalActionLongTimeout
return|;
block|}
DECL|method|getChunkSize
specifier|public
name|ByteSizeValue
name|getChunkSize
parameter_list|()
block|{
return|return
name|chunkSize
return|;
block|}
DECL|method|setChunkSize
name|void
name|setChunkSize
parameter_list|(
name|ByteSizeValue
name|chunkSize
parameter_list|)
block|{
comment|// only settable for tests
if|if
condition|(
name|chunkSize
operator|.
name|bytesAsInt
argument_list|()
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"chunkSize must be> 0"
argument_list|)
throw|;
block|}
name|this
operator|.
name|chunkSize
operator|=
name|chunkSize
expr_stmt|;
block|}
DECL|method|setConcurrentStreams
specifier|private
name|void
name|setConcurrentStreams
parameter_list|(
name|int
name|concurrentStreams
parameter_list|)
block|{
name|this
operator|.
name|concurrentStreams
operator|=
name|concurrentStreams
expr_stmt|;
name|concurrentStreamPool
operator|.
name|setMaximumPoolSize
argument_list|(
name|concurrentStreams
argument_list|)
expr_stmt|;
block|}
DECL|method|setRetryDelayStateSync
specifier|public
name|void
name|setRetryDelayStateSync
parameter_list|(
name|TimeValue
name|retryDelayStateSync
parameter_list|)
block|{
name|this
operator|.
name|retryDelayStateSync
operator|=
name|retryDelayStateSync
expr_stmt|;
block|}
DECL|method|setRetryDelayNetwork
specifier|public
name|void
name|setRetryDelayNetwork
parameter_list|(
name|TimeValue
name|retryDelayNetwork
parameter_list|)
block|{
name|this
operator|.
name|retryDelayNetwork
operator|=
name|retryDelayNetwork
expr_stmt|;
block|}
DECL|method|setActivityTimeout
specifier|public
name|void
name|setActivityTimeout
parameter_list|(
name|TimeValue
name|activityTimeout
parameter_list|)
block|{
name|this
operator|.
name|activityTimeout
operator|=
name|activityTimeout
expr_stmt|;
block|}
DECL|method|setInternalActionTimeout
specifier|public
name|void
name|setInternalActionTimeout
parameter_list|(
name|TimeValue
name|internalActionTimeout
parameter_list|)
block|{
name|this
operator|.
name|internalActionTimeout
operator|=
name|internalActionTimeout
expr_stmt|;
block|}
DECL|method|setInternalActionLongTimeout
specifier|public
name|void
name|setInternalActionLongTimeout
parameter_list|(
name|TimeValue
name|internalActionLongTimeout
parameter_list|)
block|{
name|this
operator|.
name|internalActionLongTimeout
operator|=
name|internalActionLongTimeout
expr_stmt|;
block|}
DECL|method|setMaxBytesPerSec
specifier|private
name|void
name|setMaxBytesPerSec
parameter_list|(
name|ByteSizeValue
name|maxBytesPerSec
parameter_list|)
block|{
name|this
operator|.
name|maxBytesPerSec
operator|=
name|maxBytesPerSec
expr_stmt|;
if|if
condition|(
name|maxBytesPerSec
operator|.
name|bytes
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|rateLimiter
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|rateLimiter
operator|!=
literal|null
condition|)
block|{
name|rateLimiter
operator|.
name|setMBPerSec
argument_list|(
name|maxBytesPerSec
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rateLimiter
operator|=
operator|new
name|SimpleRateLimiter
argument_list|(
name|maxBytesPerSec
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|setConcurrentSmallFileStreams
specifier|private
name|void
name|setConcurrentSmallFileStreams
parameter_list|(
name|int
name|concurrentSmallFileStreams
parameter_list|)
block|{
name|this
operator|.
name|concurrentSmallFileStreams
operator|=
name|concurrentSmallFileStreams
expr_stmt|;
name|concurrentSmallFileStreamPool
operator|.
name|setMaximumPoolSize
argument_list|(
name|concurrentSmallFileStreams
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

