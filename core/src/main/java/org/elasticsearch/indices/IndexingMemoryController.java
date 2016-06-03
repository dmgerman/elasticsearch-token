begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
package|;
end_package

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
name|AbstractRunnable
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
name|FutureUtils
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
name|Engine
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
name|EngineClosedException
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
name|FlushNotAllowedEngineException
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
name|IndexShardState
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
name|IndexingOperationListener
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
name|translog
operator|.
name|Translog
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
operator|.
name|JvmInfo
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|PriorityQueue
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledFuture
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
name|atomic
operator|.
name|AtomicLong
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
name|locks
operator|.
name|ReentrantLock
import|;
end_import

begin_class
DECL|class|IndexingMemoryController
specifier|public
class|class
name|IndexingMemoryController
extends|extends
name|AbstractComponent
implements|implements
name|IndexingOperationListener
implements|,
name|Closeable
block|{
comment|/** How much heap (% or bytes) we will share across all actively indexing shards on this node (default: 10%). */
DECL|field|INDEX_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|ByteSizeValue
argument_list|>
name|INDEX_BUFFER_SIZE_SETTING
init|=
name|Setting
operator|.
name|byteSizeSetting
argument_list|(
literal|"indices.memory.index_buffer_size"
argument_list|,
literal|"10%"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/** Only applies when<code>indices.memory.index_buffer_size</code> is a %, to set a floor on the actual size in bytes (default: 48 MB). */
DECL|field|MIN_INDEX_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|ByteSizeValue
argument_list|>
name|MIN_INDEX_BUFFER_SIZE_SETTING
init|=
name|Setting
operator|.
name|byteSizeSetting
argument_list|(
literal|"indices.memory.min_index_buffer_size"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|48
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|0
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/** Only applies when<code>indices.memory.index_buffer_size</code> is a %, to set a ceiling on the actual size in bytes (default: not set). */
DECL|field|MAX_INDEX_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|ByteSizeValue
argument_list|>
name|MAX_INDEX_BUFFER_SIZE_SETTING
init|=
name|Setting
operator|.
name|byteSizeSetting
argument_list|(
literal|"indices.memory.max_index_buffer_size"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/** If we see no indexing operations after this much time for a given shard, we consider that shard inactive (default: 5 minutes). */
DECL|field|SHARD_INACTIVE_TIME_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|SHARD_INACTIVE_TIME_SETTING
init|=
name|Setting
operator|.
name|positiveTimeSetting
argument_list|(
literal|"indices.memory.shard_inactive_time"
argument_list|,
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|5
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/** How frequently we check indexing memory usage (default: 5 seconds). */
DECL|field|SHARD_MEMORY_INTERVAL_TIME_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|SHARD_MEMORY_INTERVAL_TIME_SETTING
init|=
name|Setting
operator|.
name|positiveTimeSetting
argument_list|(
literal|"indices.memory.interval"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|5
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|indexShards
specifier|private
specifier|final
name|Iterable
argument_list|<
name|IndexShard
argument_list|>
name|indexShards
decl_stmt|;
DECL|field|indexingBuffer
specifier|private
specifier|final
name|ByteSizeValue
name|indexingBuffer
decl_stmt|;
DECL|field|inactiveTime
specifier|private
specifier|final
name|TimeValue
name|inactiveTime
decl_stmt|;
DECL|field|interval
specifier|private
specifier|final
name|TimeValue
name|interval
decl_stmt|;
comment|/** Contains shards currently being throttled because we can't write segments quickly enough */
DECL|field|throttled
specifier|private
specifier|final
name|Set
argument_list|<
name|IndexShard
argument_list|>
name|throttled
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|scheduler
specifier|private
specifier|final
name|ScheduledFuture
name|scheduler
decl_stmt|;
DECL|field|CAN_WRITE_INDEX_BUFFER_STATES
specifier|private
specifier|static
specifier|final
name|EnumSet
argument_list|<
name|IndexShardState
argument_list|>
name|CAN_WRITE_INDEX_BUFFER_STATES
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|IndexShardState
operator|.
name|RECOVERING
argument_list|,
name|IndexShardState
operator|.
name|POST_RECOVERY
argument_list|,
name|IndexShardState
operator|.
name|STARTED
argument_list|,
name|IndexShardState
operator|.
name|RELOCATED
argument_list|)
decl_stmt|;
DECL|field|statusChecker
specifier|private
specifier|final
name|ShardsIndicesStatusChecker
name|statusChecker
decl_stmt|;
DECL|method|IndexingMemoryController
name|IndexingMemoryController
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|Iterable
argument_list|<
name|IndexShard
argument_list|>
name|indexServices
parameter_list|)
block|{
name|this
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|indexServices
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|getMem
argument_list|()
operator|.
name|getHeapMax
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|IndexingMemoryController
name|IndexingMemoryController
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|Iterable
argument_list|<
name|IndexShard
argument_list|>
name|indexServices
parameter_list|,
name|long
name|jvmMemoryInBytes
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexShards
operator|=
name|indexServices
expr_stmt|;
name|ByteSizeValue
name|indexingBuffer
init|=
name|INDEX_BUFFER_SIZE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|String
name|indexingBufferSetting
init|=
name|settings
operator|.
name|get
argument_list|(
name|INDEX_BUFFER_SIZE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
comment|// null means we used the default (10%)
if|if
condition|(
name|indexingBufferSetting
operator|==
literal|null
operator|||
name|indexingBufferSetting
operator|.
name|endsWith
argument_list|(
literal|"%"
argument_list|)
condition|)
block|{
comment|// We only apply the min/max when % value was used for the index buffer:
name|ByteSizeValue
name|minIndexingBuffer
init|=
name|MIN_INDEX_BUFFER_SIZE_SETTING
operator|.
name|get
argument_list|(
name|this
operator|.
name|settings
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|maxIndexingBuffer
init|=
name|MAX_INDEX_BUFFER_SIZE_SETTING
operator|.
name|get
argument_list|(
name|this
operator|.
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexingBuffer
operator|.
name|bytes
argument_list|()
operator|<
name|minIndexingBuffer
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|indexingBuffer
operator|=
name|minIndexingBuffer
expr_stmt|;
block|}
if|if
condition|(
name|maxIndexingBuffer
operator|.
name|bytes
argument_list|()
operator|!=
operator|-
literal|1
operator|&&
name|indexingBuffer
operator|.
name|bytes
argument_list|()
operator|>
name|maxIndexingBuffer
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|indexingBuffer
operator|=
name|maxIndexingBuffer
expr_stmt|;
block|}
block|}
name|this
operator|.
name|indexingBuffer
operator|=
name|indexingBuffer
expr_stmt|;
name|this
operator|.
name|inactiveTime
operator|=
name|SHARD_INACTIVE_TIME_SETTING
operator|.
name|get
argument_list|(
name|this
operator|.
name|settings
argument_list|)
expr_stmt|;
comment|// we need to have this relatively small to free up heap quickly enough
name|this
operator|.
name|interval
operator|=
name|SHARD_MEMORY_INTERVAL_TIME_SETTING
operator|.
name|get
argument_list|(
name|this
operator|.
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|statusChecker
operator|=
operator|new
name|ShardsIndicesStatusChecker
argument_list|()
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using indexing buffer size [{}] with {} [{}], {} [{}]"
argument_list|,
name|this
operator|.
name|indexingBuffer
argument_list|,
name|SHARD_INACTIVE_TIME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|this
operator|.
name|inactiveTime
argument_list|,
name|SHARD_MEMORY_INTERVAL_TIME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|this
operator|.
name|interval
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduler
operator|=
name|scheduleTask
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
comment|// Need to save this so we can later launch async "write indexing buffer to disk" on shards:
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
block|}
DECL|method|scheduleTask
specifier|protected
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|scheduleTask
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|)
block|{
comment|// it's fine to run it on the scheduler thread, no busy work
return|return
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|statusChecker
argument_list|,
name|interval
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|scheduler
argument_list|)
expr_stmt|;
block|}
comment|/**      * returns the current budget for the total amount of indexing buffers of      * active shards on this node      */
DECL|method|indexingBufferSize
name|ByteSizeValue
name|indexingBufferSize
parameter_list|()
block|{
return|return
name|indexingBuffer
return|;
block|}
DECL|method|availableShards
specifier|protected
name|List
argument_list|<
name|IndexShard
argument_list|>
name|availableShards
parameter_list|()
block|{
name|List
argument_list|<
name|IndexShard
argument_list|>
name|availableShards
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexShard
name|shard
range|:
name|indexShards
control|)
block|{
comment|// shadow replica doesn't have an indexing buffer
if|if
condition|(
name|shard
operator|.
name|canIndex
argument_list|()
operator|&&
name|CAN_WRITE_INDEX_BUFFER_STATES
operator|.
name|contains
argument_list|(
name|shard
operator|.
name|state
argument_list|()
argument_list|)
condition|)
block|{
name|availableShards
operator|.
name|add
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|availableShards
return|;
block|}
comment|/** returns how much heap this shard is using for its indexing buffer */
DECL|method|getIndexBufferRAMBytesUsed
specifier|protected
name|long
name|getIndexBufferRAMBytesUsed
parameter_list|(
name|IndexShard
name|shard
parameter_list|)
block|{
return|return
name|shard
operator|.
name|getIndexBufferRAMBytesUsed
argument_list|()
return|;
block|}
comment|/** returns how many bytes this shard is currently writing to disk */
DECL|method|getShardWritingBytes
specifier|protected
name|long
name|getShardWritingBytes
parameter_list|(
name|IndexShard
name|shard
parameter_list|)
block|{
return|return
name|shard
operator|.
name|getWritingBytes
argument_list|()
return|;
block|}
comment|/** ask this shard to refresh, in the background, to free up heap */
DECL|method|writeIndexingBufferAsync
specifier|protected
name|void
name|writeIndexingBufferAsync
parameter_list|(
name|IndexShard
name|shard
parameter_list|)
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|REFRESH
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|AbstractRunnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|doRun
parameter_list|()
block|{
name|shard
operator|.
name|writeIndexingBuffer
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to write indexing buffer for shard [{}]; ignoring"
argument_list|,
name|t
argument_list|,
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/** force checker to run now */
DECL|method|forceCheck
name|void
name|forceCheck
parameter_list|()
block|{
name|statusChecker
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
comment|/** called by IndexShard to record that this many bytes were written to translog */
DECL|method|bytesWritten
specifier|public
name|void
name|bytesWritten
parameter_list|(
name|int
name|bytes
parameter_list|)
block|{
name|statusChecker
operator|.
name|bytesWritten
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
comment|/** Asks this shard to throttle indexing to one thread */
DECL|method|activateThrottling
specifier|protected
name|void
name|activateThrottling
parameter_list|(
name|IndexShard
name|shard
parameter_list|)
block|{
name|shard
operator|.
name|activateThrottling
argument_list|()
expr_stmt|;
block|}
comment|/** Asks this shard to stop throttling indexing to one thread */
DECL|method|deactivateThrottling
specifier|protected
name|void
name|deactivateThrottling
parameter_list|(
name|IndexShard
name|shard
parameter_list|)
block|{
name|shard
operator|.
name|deactivateThrottling
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|postIndex
specifier|public
name|void
name|postIndex
parameter_list|(
name|Engine
operator|.
name|Index
name|index
parameter_list|,
name|boolean
name|created
parameter_list|)
block|{
name|recordOperationBytes
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|postDelete
specifier|public
name|void
name|postDelete
parameter_list|(
name|Engine
operator|.
name|Delete
name|delete
parameter_list|)
block|{
name|recordOperationBytes
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
DECL|method|recordOperationBytes
specifier|private
name|void
name|recordOperationBytes
parameter_list|(
name|Engine
operator|.
name|Operation
name|op
parameter_list|)
block|{
name|bytesWritten
argument_list|(
name|op
operator|.
name|sizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|class|ShardAndBytesUsed
specifier|private
specifier|static
specifier|final
class|class
name|ShardAndBytesUsed
implements|implements
name|Comparable
argument_list|<
name|ShardAndBytesUsed
argument_list|>
block|{
DECL|field|bytesUsed
specifier|final
name|long
name|bytesUsed
decl_stmt|;
DECL|field|shard
specifier|final
name|IndexShard
name|shard
decl_stmt|;
DECL|method|ShardAndBytesUsed
specifier|public
name|ShardAndBytesUsed
parameter_list|(
name|long
name|bytesUsed
parameter_list|,
name|IndexShard
name|shard
parameter_list|)
block|{
name|this
operator|.
name|bytesUsed
operator|=
name|bytesUsed
expr_stmt|;
name|this
operator|.
name|shard
operator|=
name|shard
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|ShardAndBytesUsed
name|other
parameter_list|)
block|{
comment|// Sort larger shards first:
return|return
name|Long
operator|.
name|compare
argument_list|(
name|other
operator|.
name|bytesUsed
argument_list|,
name|bytesUsed
argument_list|)
return|;
block|}
block|}
comment|/** not static because we need access to many fields/methods from our containing class (IMC): */
DECL|class|ShardsIndicesStatusChecker
specifier|final
class|class
name|ShardsIndicesStatusChecker
implements|implements
name|Runnable
block|{
DECL|field|bytesWrittenSinceCheck
specifier|final
name|AtomicLong
name|bytesWrittenSinceCheck
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|runLock
specifier|final
name|ReentrantLock
name|runLock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
comment|/** Shard calls this on each indexing/delete op */
DECL|method|bytesWritten
specifier|public
name|void
name|bytesWritten
parameter_list|(
name|int
name|bytes
parameter_list|)
block|{
name|long
name|totalBytes
init|=
name|bytesWrittenSinceCheck
operator|.
name|addAndGet
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
assert|assert
name|totalBytes
operator|>=
literal|0
assert|;
while|while
condition|(
name|totalBytes
operator|>
name|indexingBuffer
operator|.
name|bytes
argument_list|()
operator|/
literal|30
condition|)
block|{
if|if
condition|(
name|runLock
operator|.
name|tryLock
argument_list|()
condition|)
block|{
try|try
block|{
comment|// Must pull this again because it may have changed since we first checked:
name|totalBytes
operator|=
name|bytesWrittenSinceCheck
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|totalBytes
operator|>
name|indexingBuffer
operator|.
name|bytes
argument_list|()
operator|/
literal|30
condition|)
block|{
name|bytesWrittenSinceCheck
operator|.
name|addAndGet
argument_list|(
operator|-
name|totalBytes
argument_list|)
expr_stmt|;
comment|// NOTE: this is only an approximate check, because bytes written is to the translog, vs indexing memory buffer which is
comment|// typically smaller but can be larger in extreme cases (many unique terms).  This logic is here only as a safety against
comment|// thread starvation or too infrequent checking, to ensure we are still checking periodically, in proportion to bytes
comment|// processed by indexing:
name|runUnlocked
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|runLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
comment|// Must get it again since other threads could have increased it while we were in runUnlocked
name|totalBytes
operator|=
name|bytesWrittenSinceCheck
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// Another thread beat us to it: let them do all the work, yay!
break|break;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|runLock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|runUnlocked
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|runLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|runUnlocked
specifier|private
name|void
name|runUnlocked
parameter_list|()
block|{
comment|// NOTE: even if we hit an errant exc here, our ThreadPool.scheduledWithFixedDelay will log the exception and re-invoke us
comment|// again, on schedule
comment|// First pass to sum up how much heap all shards' indexing buffers are using now, and how many bytes they are currently moving
comment|// to disk:
name|long
name|totalBytesUsed
init|=
literal|0
decl_stmt|;
name|long
name|totalBytesWriting
init|=
literal|0
decl_stmt|;
for|for
control|(
name|IndexShard
name|shard
range|:
name|availableShards
argument_list|()
control|)
block|{
comment|// Give shard a chance to transition to inactive so sync'd flush can happen:
name|checkIdle
argument_list|(
name|shard
argument_list|,
name|inactiveTime
operator|.
name|nanos
argument_list|()
argument_list|)
expr_stmt|;
comment|// How many bytes this shard is currently (async'd) moving from heap to disk:
name|long
name|shardWritingBytes
init|=
name|getShardWritingBytes
argument_list|(
name|shard
argument_list|)
decl_stmt|;
comment|// How many heap bytes this shard is currently using
name|long
name|shardBytesUsed
init|=
name|getIndexBufferRAMBytesUsed
argument_list|(
name|shard
argument_list|)
decl_stmt|;
name|shardBytesUsed
operator|-=
name|shardWritingBytes
expr_stmt|;
name|totalBytesWriting
operator|+=
name|shardWritingBytes
expr_stmt|;
comment|// If the refresh completed just after we pulled shardWritingBytes and before we pulled shardBytesUsed, then we could
comment|// have a negative value here.  So we just skip this shard since that means it's now using very little heap:
if|if
condition|(
name|shardBytesUsed
operator|<
literal|0
condition|)
block|{
continue|continue;
block|}
name|totalBytesUsed
operator|+=
name|shardBytesUsed
expr_stmt|;
block|}
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"total indexing heap bytes used [{}] vs {} [{}], currently writing bytes [{}]"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|totalBytesUsed
argument_list|)
argument_list|,
name|INDEX_BUFFER_SIZE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|indexingBuffer
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|totalBytesWriting
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// If we are using more than 50% of our budget across both indexing buffer and bytes we are still moving to disk, then we now
comment|// throttle the top shards to send back-pressure to ongoing indexing:
name|boolean
name|doThrottle
init|=
operator|(
name|totalBytesWriting
operator|+
name|totalBytesUsed
operator|)
operator|>
literal|1.5
operator|*
name|indexingBuffer
operator|.
name|bytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|totalBytesUsed
operator|>
name|indexingBuffer
operator|.
name|bytes
argument_list|()
condition|)
block|{
comment|// OK we are now over-budget; fill the priority queue and ask largest shard(s) to refresh:
name|PriorityQueue
argument_list|<
name|ShardAndBytesUsed
argument_list|>
name|queue
init|=
operator|new
name|PriorityQueue
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexShard
name|shard
range|:
name|availableShards
argument_list|()
control|)
block|{
comment|// How many bytes this shard is currently (async'd) moving from heap to disk:
name|long
name|shardWritingBytes
init|=
name|getShardWritingBytes
argument_list|(
name|shard
argument_list|)
decl_stmt|;
comment|// How many heap bytes this shard is currently using
name|long
name|shardBytesUsed
init|=
name|getIndexBufferRAMBytesUsed
argument_list|(
name|shard
argument_list|)
decl_stmt|;
comment|// Only count up bytes not already being refreshed:
name|shardBytesUsed
operator|-=
name|shardWritingBytes
expr_stmt|;
comment|// If the refresh completed just after we pulled shardWritingBytes and before we pulled shardBytesUsed, then we could
comment|// have a negative value here.  So we just skip this shard since that means it's now using very little heap:
if|if
condition|(
name|shardBytesUsed
operator|<
literal|0
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|shardBytesUsed
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|shardWritingBytes
operator|!=
literal|0
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"shard [{}] is using [{}] heap, writing [{}] heap"
argument_list|,
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|shardBytesUsed
argument_list|,
name|shardWritingBytes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"shard [{}] is using [{}] heap, not writing any bytes"
argument_list|,
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|shardBytesUsed
argument_list|)
expr_stmt|;
block|}
block|}
name|queue
operator|.
name|add
argument_list|(
operator|new
name|ShardAndBytesUsed
argument_list|(
name|shardBytesUsed
argument_list|,
name|shard
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"now write some indexing buffers: total indexing heap bytes used [{}] vs {} [{}], currently writing bytes [{}], [{}] shards with non-zero indexing buffer"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|totalBytesUsed
argument_list|)
argument_list|,
name|INDEX_BUFFER_SIZE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|indexingBuffer
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|totalBytesWriting
argument_list|)
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|totalBytesUsed
operator|>
name|indexingBuffer
operator|.
name|bytes
argument_list|()
operator|&&
name|queue
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|ShardAndBytesUsed
name|largest
init|=
name|queue
operator|.
name|poll
argument_list|()
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"write indexing buffer to disk for shard [{}] to free up its [{}] indexing buffer"
argument_list|,
name|largest
operator|.
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|largest
operator|.
name|bytesUsed
argument_list|)
argument_list|)
expr_stmt|;
name|writeIndexingBufferAsync
argument_list|(
name|largest
operator|.
name|shard
argument_list|)
expr_stmt|;
name|totalBytesUsed
operator|-=
name|largest
operator|.
name|bytesUsed
expr_stmt|;
if|if
condition|(
name|doThrottle
operator|&&
name|throttled
operator|.
name|contains
argument_list|(
name|largest
operator|.
name|shard
argument_list|)
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"now throttling indexing for shard [{}]: segment writing can't keep up"
argument_list|,
name|largest
operator|.
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|throttled
operator|.
name|add
argument_list|(
name|largest
operator|.
name|shard
argument_list|)
expr_stmt|;
name|activateThrottling
argument_list|(
name|largest
operator|.
name|shard
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|doThrottle
operator|==
literal|false
condition|)
block|{
for|for
control|(
name|IndexShard
name|shard
range|:
name|throttled
control|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"stop throttling indexing for shard [{}]"
argument_list|,
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|deactivateThrottling
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
name|throttled
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**      * ask this shard to check now whether it is inactive, and reduces its indexing buffer if so.      */
DECL|method|checkIdle
specifier|protected
name|void
name|checkIdle
parameter_list|(
name|IndexShard
name|shard
parameter_list|,
name|long
name|inactiveTimeNS
parameter_list|)
block|{
try|try
block|{
name|shard
operator|.
name|checkIdle
argument_list|(
name|inactiveTimeNS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EngineClosedException
decl||
name|FlushNotAllowedEngineException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"ignore exception while checking if shard {} is inactive"
argument_list|,
name|e
argument_list|,
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

