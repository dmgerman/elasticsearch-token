begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.memory
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|memory
package|;
end_package

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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Nullable
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
name|AbstractLifecycleComponent
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
name|IndexService
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
name|ShardId
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
name|IndicesService
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

begin_class
DECL|class|IndexingMemoryController
specifier|public
class|class
name|IndexingMemoryController
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|IndexingMemoryController
argument_list|>
block|{
comment|/** How much heap (% or bytes) we will share across all actively indexing shards on this node (default: 10%). */
DECL|field|INDEX_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_BUFFER_SIZE_SETTING
init|=
literal|"indices.memory.index_buffer_size"
decl_stmt|;
comment|/** Only applies when<code>indices.memory.index_buffer_size</code> is a %, to set a floor on the actual size in bytes (default: 48 MB). */
DECL|field|MIN_INDEX_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|MIN_INDEX_BUFFER_SIZE_SETTING
init|=
literal|"indices.memory.min_index_buffer_size"
decl_stmt|;
comment|/** Only applies when<code>indices.memory.index_buffer_size</code> is a %, to set a ceiling on the actual size in bytes (default: not set). */
DECL|field|MAX_INDEX_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|MAX_INDEX_BUFFER_SIZE_SETTING
init|=
literal|"indices.memory.max_index_buffer_size"
decl_stmt|;
comment|/** If we see no indexing operations after this much time for a given shard, we consider that shard inactive (default: 5 minutes). */
DECL|field|SHARD_INACTIVE_TIME_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|SHARD_INACTIVE_TIME_SETTING
init|=
literal|"indices.memory.shard_inactive_time"
decl_stmt|;
comment|/** How frequently we check indexing memory usage (default: 5 seconds). */
DECL|field|SHARD_MEMORY_INTERVAL_TIME_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|SHARD_MEMORY_INTERVAL_TIME_SETTING
init|=
literal|"indices.memory.interval"
decl_stmt|;
comment|/** Hardwired translog buffer size */
DECL|field|SHARD_TRANSLOG_BUFFER
specifier|public
specifier|static
specifier|final
name|ByteSizeValue
name|SHARD_TRANSLOG_BUFFER
init|=
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"8kb"
argument_list|,
literal|"SHARD_TRANSLOG_BUFFER"
argument_list|)
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
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
DECL|field|scheduler
specifier|private
specifier|volatile
name|ScheduledFuture
name|scheduler
decl_stmt|;
DECL|field|CAN_UPDATE_INDEX_BUFFER_STATES
specifier|private
specifier|static
specifier|final
name|EnumSet
argument_list|<
name|IndexShardState
argument_list|>
name|CAN_UPDATE_INDEX_BUFFER_STATES
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
comment|/** How many bytes we are currently moving to disk by the engine to refresh */
DECL|field|bytesRefreshingNow
specifier|private
specifier|final
name|AtomicLong
name|bytesRefreshingNow
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|refreshingBytes
specifier|private
specifier|final
name|Map
argument_list|<
name|ShardId
argument_list|,
name|Long
argument_list|>
name|refreshingBytes
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndexingMemoryController
specifier|public
name|IndexingMemoryController
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|this
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|indicesService
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
comment|// for testing
DECL|method|IndexingMemoryController
specifier|protected
name|IndexingMemoryController
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|IndicesService
name|indicesService
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
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|ByteSizeValue
name|indexingBuffer
decl_stmt|;
name|String
name|indexingBufferSetting
init|=
name|this
operator|.
name|settings
operator|.
name|get
argument_list|(
name|INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"10%"
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexingBufferSetting
operator|.
name|endsWith
argument_list|(
literal|"%"
argument_list|)
condition|)
block|{
name|double
name|percent
init|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|indexingBufferSetting
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|indexingBufferSetting
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|indexingBuffer
operator|=
operator|new
name|ByteSizeValue
argument_list|(
call|(
name|long
call|)
argument_list|(
operator|(
operator|(
name|double
operator|)
name|jvmMemoryInBytes
operator|)
operator|*
operator|(
name|percent
operator|/
literal|100
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|ByteSizeValue
name|minIndexingBuffer
init|=
name|this
operator|.
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|MIN_INDEX_BUFFER_SIZE_SETTING
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
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|maxIndexingBuffer
init|=
name|this
operator|.
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|MAX_INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|null
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
operator|!=
literal|null
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
else|else
block|{
name|indexingBuffer
operator|=
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
name|indexingBufferSetting
argument_list|,
name|INDEX_BUFFER_SIZE_SETTING
argument_list|)
expr_stmt|;
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
name|this
operator|.
name|settings
operator|.
name|getAsTime
argument_list|(
name|SHARD_INACTIVE_TIME_SETTING
argument_list|,
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
comment|// we need to have this relatively small to free up heap quickly enough
name|this
operator|.
name|interval
operator|=
name|this
operator|.
name|settings
operator|.
name|getAsTime
argument_list|(
name|SHARD_MEMORY_INTERVAL_TIME_SETTING
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|5
argument_list|)
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
argument_list|,
name|this
operator|.
name|inactiveTime
argument_list|,
name|SHARD_MEMORY_INTERVAL_TIME_SETTING
argument_list|,
name|this
operator|.
name|interval
argument_list|)
expr_stmt|;
block|}
DECL|method|addRefreshingBytes
specifier|public
name|void
name|addRefreshingBytes
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|long
name|numBytes
parameter_list|)
block|{
name|refreshingBytes
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|numBytes
argument_list|)
expr_stmt|;
block|}
DECL|method|removeRefreshingBytes
specifier|public
name|void
name|removeRefreshingBytes
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|long
name|numBytes
parameter_list|)
block|{
name|boolean
name|result
init|=
name|refreshingBytes
operator|.
name|remove
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
assert|assert
name|result
assert|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
comment|// it's fine to run it on the scheduler thread, no busy work
name|this
operator|.
name|scheduler
operator|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|statusChecker
argument_list|,
name|interval
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|scheduler
argument_list|)
expr_stmt|;
name|scheduler
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{     }
comment|/**      * returns the current budget for the total amount of indexing buffers of      * active shards on this node      */
DECL|method|indexingBufferSize
specifier|public
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
name|ShardId
argument_list|>
name|availableShards
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|ShardId
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
name|IndexService
name|indexService
range|:
name|indicesService
control|)
block|{
for|for
control|(
name|IndexShard
name|indexShard
range|:
name|indexService
control|)
block|{
if|if
condition|(
name|shardAvailable
argument_list|(
name|indexShard
argument_list|)
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|list
return|;
block|}
comment|/** returns true if shard exists and is availabe for updates */
DECL|method|shardAvailable
specifier|protected
name|boolean
name|shardAvailable
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
return|return
name|shardAvailable
argument_list|(
name|getShard
argument_list|(
name|shardId
argument_list|)
argument_list|)
return|;
block|}
comment|/** returns how much heap this shard is using for its indexing buffer */
DECL|method|getIndexBufferRAMBytesUsed
specifier|protected
name|long
name|getIndexBufferRAMBytesUsed
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|IndexShard
name|shard
init|=
name|getShard
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|shard
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|shard
operator|.
name|getIndexBufferRAMBytesUsed
argument_list|()
return|;
block|}
comment|/** ask this shard to refresh, in the background, to free up heap */
DECL|method|refreshShardAsync
specifier|protected
name|void
name|refreshShardAsync
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|IndexShard
name|shard
init|=
name|getShard
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|shard
operator|.
name|refreshAsync
argument_list|(
literal|"memory"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** returns true if shard exists and is availabe for updates */
DECL|method|shardAvailable
specifier|protected
name|boolean
name|shardAvailable
parameter_list|(
annotation|@
name|Nullable
name|IndexShard
name|shard
parameter_list|)
block|{
comment|// shadow replica doesn't have an indexing buffer
return|return
name|shard
operator|!=
literal|null
operator|&&
name|shard
operator|.
name|canIndex
argument_list|()
operator|&&
name|CAN_UPDATE_INDEX_BUFFER_STATES
operator|.
name|contains
argument_list|(
name|shard
operator|.
name|state
argument_list|()
argument_list|)
return|;
block|}
comment|/** ask this shard to check now whether it is inactive, and reduces its indexing and translog buffers if so.  returns Boolean.TRUE if      *  it did deactive, Boolean.FALSE if it did not, and null if the shard is unknown */
DECL|method|checkIdle
specifier|protected
name|void
name|checkIdle
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|long
name|inactiveTimeNS
parameter_list|)
block|{
specifier|final
name|IndexShard
name|shard
init|=
name|getShard
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|shard
operator|.
name|checkIdle
argument_list|(
name|inactiveTimeNS
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** gets an {@link IndexShard} instance for the given shard. returns null if the shard doesn't exist */
DECL|method|getShard
specifier|protected
name|IndexShard
name|getShard
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|!=
literal|null
condition|)
block|{
name|IndexShard
name|indexShard
init|=
name|indexService
operator|.
name|getShardOrNull
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|indexShard
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/** check if any shards active status changed, now. */
DECL|method|forceCheck
specifier|public
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
DECL|field|startMS
name|long
name|startMS
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
DECL|class|ShardAndBytesUsed
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
DECL|field|shardId
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|method|ShardAndBytesUsed
specifier|public
name|ShardAndBytesUsed
parameter_list|(
name|long
name|bytesUsed
parameter_list|,
name|ShardId
name|shardId
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
name|shardId
operator|=
name|shardId
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
DECL|class|ShardsIndicesStatusChecker
class|class
name|ShardsIndicesStatusChecker
implements|implements
name|Runnable
block|{
DECL|field|bytesWrittenSinceCheck
name|long
name|bytesWrittenSinceCheck
decl_stmt|;
DECL|method|bytesWritten
specifier|public
specifier|synchronized
name|void
name|bytesWritten
parameter_list|(
name|int
name|bytes
parameter_list|)
block|{
name|bytesWrittenSinceCheck
operator|+=
name|bytes
expr_stmt|;
if|if
condition|(
name|bytesWrittenSinceCheck
operator|>
name|indexingBuffer
operator|.
name|bytes
argument_list|()
operator|/
literal|20
condition|)
block|{
comment|// NOTE: this is only an approximate check, because bytes written is to the translog, vs indexing memory buffer which is
comment|// typically smaller.  But this logic is here only as a safety against thread starvation or too infrequent checking,
comment|// to ensure we are still checking in proportion to bytes processed by indexing:
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
operator|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startMS
operator|)
operator|/
literal|1000.0
operator|)
operator|+
literal|": NOW CHECK xlog="
operator|+
name|bytesWrittenSinceCheck
argument_list|)
expr_stmt|;
name|run
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
specifier|synchronized
name|void
name|run
parameter_list|()
block|{
comment|// nocommit add defensive try/catch-everything here?  bad if an errant EngineClosedExc kills off this thread!!
comment|// Fast check to sum up how much heap all shards' indexing buffers are using now:
name|long
name|totalBytesUsed
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ShardId
name|shardId
range|:
name|availableShards
argument_list|()
control|)
block|{
name|Long
name|refreshingBytes
init|=
name|refreshingBytes
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
comment|// Give shard a chance to transition to inactive so sync'd flush can happen:
name|checkIdle
argument_list|(
name|shardId
argument_list|,
name|inactiveTime
operator|.
name|nanos
argument_list|()
argument_list|)
expr_stmt|;
comment|// nocommit explain why order is important here!
name|Long
name|refreshingBytes
init|=
name|refreshingBytes
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|long
name|shardBytesUsed
init|=
name|getIndexBufferRAMBytesUsed
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|refreshingBytes
operator|!=
literal|null
condition|)
block|{
comment|// Only count up bytes not already being refreshed:
name|shardBytesUsed
operator|-=
name|refreshingBytes
expr_stmt|;
comment|// If the refresh completed just after we pulled refreshingBytes and before we pulled index buffer bytes, then we could
comment|// have a negative value here:
if|if
condition|(
name|shardBytesUsed
operator|<
literal|0
condition|)
block|{
continue|continue;
block|}
block|}
name|totalBytesUsed
operator|+=
name|shardBytesUsed
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"IMC:   "
operator|+
name|shardId
operator|+
literal|" using "
operator|+
operator|(
name|shardBytesUsed
operator|/
literal|1024.
operator|/
literal|1024.
operator|)
operator|+
literal|" MB"
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
operator|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startMS
operator|)
operator|/
literal|1000.0
operator|)
operator|+
literal|": TOT="
operator|+
name|totalBytesUsed
operator|+
literal|" vs "
operator|+
name|indexingBuffer
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|totalBytesUsed
operator|-
name|bytesRefreshingNow
operator|.
name|get
argument_list|()
operator|>
name|indexingBuffer
operator|.
name|bytes
argument_list|()
condition|)
block|{
comment|// OK we are using too much; make a queue and ask largest shard(s) to refresh:
name|logger
operator|.
name|debug
argument_list|(
literal|"now refreshing some shards: total indexing bytes used [{}] vs index_buffer_size [{}]"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|totalBytesUsed
argument_list|)
argument_list|,
name|indexingBuffer
argument_list|)
expr_stmt|;
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
name|ShardId
name|shardId
range|:
name|availableShards
argument_list|()
control|)
block|{
comment|// nocommit explain why order is important here!
name|Long
name|refreshingBytes
init|=
name|refreshingBytes
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|long
name|shardBytesUsed
init|=
name|getIndexBufferRAMBytesUsed
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|refreshingBytes
operator|!=
literal|null
condition|)
block|{
comment|// Only count up bytes not already being refreshed:
name|shardBytesUsed
operator|-=
name|refreshingBytes
expr_stmt|;
comment|// If the refresh completed just after we pulled refreshingBytes and before we pulled index buffer bytes, then we could
comment|// have a negative value here:
if|if
condition|(
name|shardBytesUsed
operator|<
literal|0
condition|)
block|{
continue|continue;
block|}
block|}
if|if
condition|(
name|shardBytesUsed
operator|>
literal|0
condition|)
block|{
name|queue
operator|.
name|add
argument_list|(
operator|new
name|ShardAndBytesUsed
argument_list|(
name|shardBytesUsed
argument_list|,
name|shardId
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"IMC: write "
operator|+
name|largest
operator|.
name|shardId
operator|+
literal|": "
operator|+
operator|(
name|largest
operator|.
name|bytesUsed
operator|/
literal|1024.
operator|/
literal|1024.
operator|)
operator|+
literal|" MB"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"refresh shard [{}] to free up its [{}] indexing buffer"
argument_list|,
name|largest
operator|.
name|shardId
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
name|refreshShardAsync
argument_list|(
name|largest
operator|.
name|shardId
argument_list|)
expr_stmt|;
name|totalBytesUsed
operator|-=
name|largest
operator|.
name|bytesUsed
expr_stmt|;
block|}
block|}
name|bytesWrittenSinceCheck
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

