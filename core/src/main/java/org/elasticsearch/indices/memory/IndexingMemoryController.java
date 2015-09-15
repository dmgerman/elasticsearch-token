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
name|HashMap
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
name|concurrent
operator|.
name|ScheduledFuture
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

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
comment|/** Sets a floor on the per-shard index buffer size (default: 4 MB). */
DECL|field|MIN_SHARD_INDEX_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|MIN_SHARD_INDEX_BUFFER_SIZE_SETTING
init|=
literal|"indices.memory.min_shard_index_buffer_size"
decl_stmt|;
comment|/** Sets a ceiling on the per-shard index buffer size (default: 512 MB). */
DECL|field|MAX_SHARD_INDEX_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|MAX_SHARD_INDEX_BUFFER_SIZE_SETTING
init|=
literal|"indices.memory.max_shard_index_buffer_size"
decl_stmt|;
comment|/** How much heap (% or bytes) we will share across all actively indexing shards for the translog buffer (default: 1%). */
DECL|field|TRANSLOG_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|TRANSLOG_BUFFER_SIZE_SETTING
init|=
literal|"indices.memory.translog_buffer_size"
decl_stmt|;
comment|/** Only applies when<code>indices.memory.translog_buffer_size</code> is a %, to set a floor on the actual size in bytes (default: 256 KB). */
DECL|field|MIN_TRANSLOG_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|MIN_TRANSLOG_BUFFER_SIZE_SETTING
init|=
literal|"indices.memory.min_translog_buffer_size"
decl_stmt|;
comment|/** Only applies when<code>indices.memory.translog_buffer_size</code> is a %, to set a ceiling on the actual size in bytes (default: not set). */
DECL|field|MAX_TRANSLOG_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|MAX_TRANSLOG_BUFFER_SIZE_SETTING
init|=
literal|"indices.memory.max_translog_buffer_size"
decl_stmt|;
comment|/** Sets a floor on the per-shard translog buffer size (default: 2 KB). */
DECL|field|MIN_SHARD_TRANSLOG_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|MIN_SHARD_TRANSLOG_BUFFER_SIZE_SETTING
init|=
literal|"indices.memory.min_shard_translog_buffer_size"
decl_stmt|;
comment|/** Sets a ceiling on the per-shard translog buffer size (default: 64 KB). */
DECL|field|MAX_SHARD_TRANSLOG_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|MAX_SHARD_TRANSLOG_BUFFER_SIZE_SETTING
init|=
literal|"indices.memory.max_shard_translog_buffer_size"
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
comment|/** How frequently we check shards to find inactive ones (default: 30 seconds). */
DECL|field|SHARD_INACTIVE_INTERVAL_TIME_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|SHARD_INACTIVE_INTERVAL_TIME_SETTING
init|=
literal|"indices.memory.interval"
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
DECL|field|minShardIndexBufferSize
specifier|private
specifier|final
name|ByteSizeValue
name|minShardIndexBufferSize
decl_stmt|;
DECL|field|maxShardIndexBufferSize
specifier|private
specifier|final
name|ByteSizeValue
name|maxShardIndexBufferSize
decl_stmt|;
DECL|field|translogBuffer
specifier|private
specifier|final
name|ByteSizeValue
name|translogBuffer
decl_stmt|;
DECL|field|minShardTranslogBufferSize
specifier|private
specifier|final
name|ByteSizeValue
name|minShardTranslogBufferSize
decl_stmt|;
DECL|field|maxShardTranslogBufferSize
specifier|private
specifier|final
name|ByteSizeValue
name|maxShardTranslogBufferSize
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
name|minShardIndexBufferSize
operator|=
name|this
operator|.
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|MIN_SHARD_INDEX_BUFFER_SIZE_SETTING
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|4
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
expr_stmt|;
comment|// LUCENE MONITOR: Based on this thread, currently (based on Mike), having a large buffer does not make a lot of sense: https://issues.apache.org/jira/browse/LUCENE-2324?focusedCommentId=13005155&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-13005155
name|this
operator|.
name|maxShardIndexBufferSize
operator|=
name|this
operator|.
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|MAX_SHARD_INDEX_BUFFER_SIZE_SETTING
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|512
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
expr_stmt|;
name|ByteSizeValue
name|translogBuffer
decl_stmt|;
name|String
name|translogBufferSetting
init|=
name|this
operator|.
name|settings
operator|.
name|get
argument_list|(
name|TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"1%"
argument_list|)
decl_stmt|;
if|if
condition|(
name|translogBufferSetting
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
name|translogBufferSetting
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|translogBufferSetting
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|translogBuffer
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
name|minTranslogBuffer
init|=
name|this
operator|.
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|MIN_TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|256
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|maxTranslogBuffer
init|=
name|this
operator|.
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|MAX_TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|translogBuffer
operator|.
name|bytes
argument_list|()
operator|<
name|minTranslogBuffer
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|translogBuffer
operator|=
name|minTranslogBuffer
expr_stmt|;
block|}
if|if
condition|(
name|maxTranslogBuffer
operator|!=
literal|null
operator|&&
name|translogBuffer
operator|.
name|bytes
argument_list|()
operator|>
name|maxTranslogBuffer
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|translogBuffer
operator|=
name|maxTranslogBuffer
expr_stmt|;
block|}
block|}
else|else
block|{
name|translogBuffer
operator|=
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
name|translogBufferSetting
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|translogBuffer
operator|=
name|translogBuffer
expr_stmt|;
name|this
operator|.
name|minShardTranslogBufferSize
operator|=
name|this
operator|.
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|MIN_SHARD_TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|2
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxShardTranslogBufferSize
operator|=
name|this
operator|.
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|MAX_SHARD_TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|64
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
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
comment|// we need to have this relatively small to move a shard from inactive to active fast (enough)
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
name|SHARD_INACTIVE_INTERVAL_TIME_SETTING
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using indexing buffer size [{}], with {} [{}], {} [{}], {} [{}], {} [{}]"
argument_list|,
name|this
operator|.
name|indexingBuffer
argument_list|,
name|MIN_SHARD_INDEX_BUFFER_SIZE_SETTING
argument_list|,
name|this
operator|.
name|minShardIndexBufferSize
argument_list|,
name|MAX_SHARD_INDEX_BUFFER_SIZE_SETTING
argument_list|,
name|this
operator|.
name|maxShardIndexBufferSize
argument_list|,
name|SHARD_INACTIVE_TIME_SETTING
argument_list|,
name|this
operator|.
name|inactiveTime
argument_list|,
name|SHARD_INACTIVE_INTERVAL_TIME_SETTING
argument_list|,
name|this
operator|.
name|interval
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
comment|// its fine to run it on the scheduler thread, no busy work
name|this
operator|.
name|scheduler
operator|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
operator|new
name|ShardsIndicesStatusChecker
argument_list|()
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
DECL|class|ShardsIndicesStatusChecker
class|class
name|ShardsIndicesStatusChecker
implements|implements
name|Runnable
block|{
DECL|field|shardsIndicesStatus
specifier|private
specifier|final
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ShardIndexingStatus
argument_list|>
name|shardsIndicesStatus
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|EnumSet
argument_list|<
name|ShardStatusChangeType
argument_list|>
name|changes
init|=
name|purgeDeletedAndClosedShards
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|IndexShard
argument_list|>
name|activeToInactiveIndexingShards
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|activeShards
init|=
name|updateShardStatuses
argument_list|(
name|changes
argument_list|,
name|activeToInactiveIndexingShards
argument_list|)
decl_stmt|;
for|for
control|(
name|IndexShard
name|indexShard
range|:
name|activeToInactiveIndexingShards
control|)
block|{
comment|// update inactive indexing buffer size
try|try
block|{
name|indexShard
operator|.
name|markAsInactive
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EngineClosedException
name|e
parameter_list|)
block|{
comment|// ignore
name|logger
operator|.
name|trace
argument_list|(
literal|"ignore EngineClosedException while marking shard [{}][{}] as inactive"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FlushNotAllowedEngineException
name|e
parameter_list|)
block|{
comment|// ignore
name|logger
operator|.
name|trace
argument_list|(
literal|"ignore FlushNotAllowedException while marking shard [{}][{}] as inactive"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|changes
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// Something changed: recompute indexing buffers:
name|calcAndSetShardBuffers
argument_list|(
name|activeShards
argument_list|,
literal|"["
operator|+
name|changes
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**          * goes through all existing shards and check whether the changes their active status          *          * @return the current count of active shards          */
DECL|method|updateShardStatuses
specifier|private
name|int
name|updateShardStatuses
parameter_list|(
name|EnumSet
argument_list|<
name|ShardStatusChangeType
argument_list|>
name|changes
parameter_list|,
name|List
argument_list|<
name|IndexShard
argument_list|>
name|activeToInactiveIndexingShards
parameter_list|)
block|{
name|int
name|activeShards
init|=
literal|0
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
operator|!
name|CAN_UPDATE_INDEX_BUFFER_STATES
operator|.
name|contains
argument_list|(
name|indexShard
operator|.
name|state
argument_list|()
argument_list|)
condition|)
block|{
comment|// not ready to be updated yet
continue|continue;
block|}
if|if
condition|(
name|indexShard
operator|.
name|canIndex
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// shadow replica doesn't have an indexing buffer
continue|continue;
block|}
specifier|final
name|Translog
name|translog
decl_stmt|;
try|try
block|{
name|translog
operator|=
name|indexShard
operator|.
name|engine
argument_list|()
operator|.
name|getTranslog
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EngineClosedException
name|e
parameter_list|)
block|{
comment|// not ready yet to be checked for activity
continue|continue;
block|}
specifier|final
name|long
name|timeMS
init|=
name|threadPool
operator|.
name|estimatedTimeInMillis
argument_list|()
decl_stmt|;
name|ShardIndexingStatus
name|status
init|=
name|shardsIndicesStatus
operator|.
name|get
argument_list|(
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|==
literal|null
condition|)
block|{
name|status
operator|=
operator|new
name|ShardIndexingStatus
argument_list|()
expr_stmt|;
name|shardsIndicesStatus
operator|.
name|put
argument_list|(
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|changes
operator|.
name|add
argument_list|(
name|ShardStatusChangeType
operator|.
name|ADDED
argument_list|)
expr_stmt|;
block|}
comment|// consider shard inactive if it has same translogFileGeneration and no operations for a long time
if|if
condition|(
name|status
operator|.
name|translogId
operator|==
name|translog
operator|.
name|currentFileGeneration
argument_list|()
operator|&&
name|translog
operator|.
name|totalOperations
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|status
operator|.
name|timeMS
operator|==
operator|-
literal|1
condition|)
block|{
comment|// first time we noticed the shard become idle
name|status
operator|.
name|timeMS
operator|=
name|timeMS
expr_stmt|;
block|}
comment|// mark it as inactive only if enough time has passed
if|if
condition|(
name|status
operator|.
name|activeIndexing
operator|&&
operator|(
name|timeMS
operator|-
name|status
operator|.
name|timeMS
operator|)
operator|>
name|inactiveTime
operator|.
name|millis
argument_list|()
condition|)
block|{
comment|// inactive for this amount of time, mark it
name|activeToInactiveIndexingShards
operator|.
name|add
argument_list|(
name|indexShard
argument_list|)
expr_stmt|;
name|status
operator|.
name|activeIndexing
operator|=
literal|false
expr_stmt|;
name|changes
operator|.
name|add
argument_list|(
name|ShardStatusChangeType
operator|.
name|BECAME_INACTIVE
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"marking shard [{}][{}] as inactive (inactive_time[{}]) indexing wise, setting size to [{}]"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|inactiveTime
argument_list|,
name|EngineConfig
operator|.
name|INACTIVE_SHARD_INDEXING_BUFFER
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|status
operator|.
name|activeIndexing
condition|)
block|{
name|status
operator|.
name|activeIndexing
operator|=
literal|true
expr_stmt|;
name|changes
operator|.
name|add
argument_list|(
name|ShardStatusChangeType
operator|.
name|BECAME_ACTIVE
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"marking shard [{}][{}] as active indexing wise"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|status
operator|.
name|timeMS
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|status
operator|.
name|translogId
operator|=
name|translog
operator|.
name|currentFileGeneration
argument_list|()
expr_stmt|;
if|if
condition|(
name|status
operator|.
name|activeIndexing
condition|)
block|{
name|activeShards
operator|++
expr_stmt|;
block|}
block|}
block|}
return|return
name|activeShards
return|;
block|}
comment|/**          * purge any existing statuses that are no longer updated          *          * @return true if any change          */
DECL|method|purgeDeletedAndClosedShards
specifier|private
name|EnumSet
argument_list|<
name|ShardStatusChangeType
argument_list|>
name|purgeDeletedAndClosedShards
parameter_list|()
block|{
name|EnumSet
argument_list|<
name|ShardStatusChangeType
argument_list|>
name|changes
init|=
name|EnumSet
operator|.
name|noneOf
argument_list|(
name|ShardStatusChangeType
operator|.
name|class
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|ShardId
argument_list|>
name|statusShardIdIterator
init|=
name|shardsIndicesStatus
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|statusShardIdIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|ShardId
name|statusShardId
init|=
name|statusShardIdIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|statusShardId
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|remove
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
name|remove
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|IndexShard
name|indexShard
init|=
name|indexService
operator|.
name|shard
argument_list|(
name|statusShardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexShard
operator|==
literal|null
condition|)
block|{
name|remove
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|remove
operator|=
operator|!
name|CAN_UPDATE_INDEX_BUFFER_STATES
operator|.
name|contains
argument_list|(
name|indexShard
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|remove
condition|)
block|{
name|changes
operator|.
name|add
argument_list|(
name|ShardStatusChangeType
operator|.
name|DELETED
argument_list|)
expr_stmt|;
name|statusShardIdIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|changes
return|;
block|}
DECL|method|calcAndSetShardBuffers
specifier|private
name|void
name|calcAndSetShardBuffers
parameter_list|(
name|int
name|activeShards
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|activeShards
operator|==
literal|0
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"no active shards (reason={})"
argument_list|,
name|reason
argument_list|)
expr_stmt|;
return|return;
block|}
name|ByteSizeValue
name|shardIndexingBufferSize
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|indexingBuffer
operator|.
name|bytes
argument_list|()
operator|/
name|activeShards
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardIndexingBufferSize
operator|.
name|bytes
argument_list|()
operator|<
name|minShardIndexBufferSize
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|shardIndexingBufferSize
operator|=
name|minShardIndexBufferSize
expr_stmt|;
block|}
if|if
condition|(
name|shardIndexingBufferSize
operator|.
name|bytes
argument_list|()
operator|>
name|maxShardIndexBufferSize
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|shardIndexingBufferSize
operator|=
name|maxShardIndexBufferSize
expr_stmt|;
block|}
name|ByteSizeValue
name|shardTranslogBufferSize
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|translogBuffer
operator|.
name|bytes
argument_list|()
operator|/
name|activeShards
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardTranslogBufferSize
operator|.
name|bytes
argument_list|()
operator|<
name|minShardTranslogBufferSize
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|shardTranslogBufferSize
operator|=
name|minShardTranslogBufferSize
expr_stmt|;
block|}
if|if
condition|(
name|shardTranslogBufferSize
operator|.
name|bytes
argument_list|()
operator|>
name|maxShardTranslogBufferSize
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|shardTranslogBufferSize
operator|=
name|maxShardTranslogBufferSize
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"recalculating shard indexing buffer (reason={}), total is [{}] with [{}] active shards, each shard set to indexing=[{}], translog=[{}]"
argument_list|,
name|reason
argument_list|,
name|indexingBuffer
argument_list|,
name|activeShards
argument_list|,
name|shardIndexingBufferSize
argument_list|,
name|shardTranslogBufferSize
argument_list|)
expr_stmt|;
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
name|IndexShardState
name|state
init|=
name|indexShard
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|CAN_UPDATE_INDEX_BUFFER_STATES
operator|.
name|contains
argument_list|(
name|state
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"shard [{}] is not yet ready for index buffer update. index shard state: [{}]"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|,
name|state
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|ShardIndexingStatus
name|status
init|=
name|shardsIndicesStatus
operator|.
name|get
argument_list|(
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|==
literal|null
operator|||
name|status
operator|.
name|activeIndexing
condition|)
block|{
try|try
block|{
name|indexShard
operator|.
name|updateBufferSize
argument_list|(
name|shardIndexingBufferSize
argument_list|,
name|shardTranslogBufferSize
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EngineClosedException
name|e
parameter_list|)
block|{
comment|// ignore
continue|continue;
block|}
catch|catch
parameter_list|(
name|FlushNotAllowedEngineException
name|e
parameter_list|)
block|{
comment|// ignore
continue|continue;
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
literal|"failed to set shard {} index buffer to [{}]"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|,
name|shardIndexingBufferSize
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
DECL|enum|ShardStatusChangeType
specifier|private
specifier|static
enum|enum
name|ShardStatusChangeType
block|{
DECL|enum constant|ADDED
DECL|enum constant|DELETED
DECL|enum constant|BECAME_ACTIVE
DECL|enum constant|BECAME_INACTIVE
name|ADDED
block|,
name|DELETED
block|,
name|BECAME_ACTIVE
block|,
name|BECAME_INACTIVE
block|}
DECL|class|ShardIndexingStatus
specifier|private
specifier|static
class|class
name|ShardIndexingStatus
block|{
DECL|field|translogId
name|long
name|translogId
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|activeIndexing
name|boolean
name|activeIndexing
init|=
literal|true
decl_stmt|;
DECL|field|timeMS
name|long
name|timeMS
init|=
operator|-
literal|1
decl_stmt|;
comment|// contains the first time we saw this shard with no operations done on it
block|}
block|}
end_class

end_unit

