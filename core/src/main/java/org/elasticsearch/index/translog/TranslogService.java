begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
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
name|flush
operator|.
name|FlushRequest
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
name|settings
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
name|settings
operator|.
name|IndexSettingsService
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
name|*
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
name|ThreadLocalRandom
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueMillis
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TranslogService
specifier|public
class|class
name|TranslogService
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|Closeable
block|{
DECL|field|INDEX_TRANSLOG_FLUSH_INTERVAL
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_TRANSLOG_FLUSH_INTERVAL
init|=
literal|"index.translog.interval"
decl_stmt|;
DECL|field|INDEX_TRANSLOG_FLUSH_THRESHOLD_OPS
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_TRANSLOG_FLUSH_THRESHOLD_OPS
init|=
literal|"index.translog.flush_threshold_ops"
decl_stmt|;
DECL|field|INDEX_TRANSLOG_FLUSH_THRESHOLD_SIZE
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_TRANSLOG_FLUSH_THRESHOLD_SIZE
init|=
literal|"index.translog.flush_threshold_size"
decl_stmt|;
DECL|field|INDEX_TRANSLOG_FLUSH_THRESHOLD_PERIOD
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_TRANSLOG_FLUSH_THRESHOLD_PERIOD
init|=
literal|"index.translog.flush_threshold_period"
decl_stmt|;
DECL|field|INDEX_TRANSLOG_DISABLE_FLUSH
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_TRANSLOG_DISABLE_FLUSH
init|=
literal|"index.translog.disable_flush"
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|indexSettingsService
specifier|private
specifier|final
name|IndexSettingsService
name|indexSettingsService
decl_stmt|;
DECL|field|indexShard
specifier|private
specifier|final
name|IndexShard
name|indexShard
decl_stmt|;
DECL|field|interval
specifier|private
specifier|volatile
name|TimeValue
name|interval
decl_stmt|;
DECL|field|flushThresholdOperations
specifier|private
specifier|volatile
name|int
name|flushThresholdOperations
decl_stmt|;
DECL|field|flushThresholdSize
specifier|private
specifier|volatile
name|ByteSizeValue
name|flushThresholdSize
decl_stmt|;
DECL|field|flushThresholdPeriod
specifier|private
specifier|volatile
name|TimeValue
name|flushThresholdPeriod
decl_stmt|;
DECL|field|disableFlush
specifier|private
specifier|volatile
name|boolean
name|disableFlush
decl_stmt|;
DECL|field|future
specifier|private
specifier|volatile
name|ScheduledFuture
name|future
decl_stmt|;
DECL|field|applySettings
specifier|private
specifier|final
name|ApplySettings
name|applySettings
init|=
operator|new
name|ApplySettings
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|TranslogService
specifier|public
name|TranslogService
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|IndexSettingsService
name|indexSettingsService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|IndexShard
name|indexShard
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
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
name|indexSettingsService
operator|=
name|indexSettingsService
expr_stmt|;
name|this
operator|.
name|indexShard
operator|=
name|indexShard
expr_stmt|;
name|this
operator|.
name|flushThresholdOperations
operator|=
name|indexSettings
operator|.
name|getAsInt
argument_list|(
name|INDEX_TRANSLOG_FLUSH_THRESHOLD_OPS
argument_list|,
name|indexSettings
operator|.
name|getAsInt
argument_list|(
literal|"index.translog.flush_threshold"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|flushThresholdSize
operator|=
name|indexSettings
operator|.
name|getAsBytesSize
argument_list|(
name|INDEX_TRANSLOG_FLUSH_THRESHOLD_SIZE
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
name|this
operator|.
name|flushThresholdPeriod
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_TRANSLOG_FLUSH_THRESHOLD_PERIOD
argument_list|,
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|interval
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_TRANSLOG_FLUSH_INTERVAL
argument_list|,
name|timeValueMillis
argument_list|(
literal|5000
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|disableFlush
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_TRANSLOG_DISABLE_FLUSH
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"interval [{}], flush_threshold_ops [{}], flush_threshold_size [{}], flush_threshold_period [{}]"
argument_list|,
name|interval
argument_list|,
name|flushThresholdOperations
argument_list|,
name|flushThresholdSize
argument_list|,
name|flushThresholdPeriod
argument_list|)
expr_stmt|;
name|this
operator|.
name|future
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|interval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|TranslogBasedFlush
argument_list|()
argument_list|)
expr_stmt|;
name|indexSettingsService
operator|.
name|addListener
argument_list|(
name|applySettings
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
name|indexSettingsService
operator|.
name|removeListener
argument_list|(
name|applySettings
argument_list|)
expr_stmt|;
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|this
operator|.
name|future
argument_list|)
expr_stmt|;
block|}
DECL|class|ApplySettings
class|class
name|ApplySettings
implements|implements
name|IndexSettingsService
operator|.
name|Listener
block|{
annotation|@
name|Override
DECL|method|onRefreshSettings
specifier|public
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|int
name|flushThresholdOperations
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|INDEX_TRANSLOG_FLUSH_THRESHOLD_OPS
argument_list|,
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdOperations
argument_list|)
decl_stmt|;
if|if
condition|(
name|flushThresholdOperations
operator|!=
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdOperations
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating flush_threshold_ops from [{}] to [{}]"
argument_list|,
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdOperations
argument_list|,
name|flushThresholdOperations
argument_list|)
expr_stmt|;
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdOperations
operator|=
name|flushThresholdOperations
expr_stmt|;
block|}
name|ByteSizeValue
name|flushThresholdSize
init|=
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|INDEX_TRANSLOG_FLUSH_THRESHOLD_SIZE
argument_list|,
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdSize
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|flushThresholdSize
operator|.
name|equals
argument_list|(
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdSize
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating flush_threshold_size from [{}] to [{}]"
argument_list|,
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdSize
argument_list|,
name|flushThresholdSize
argument_list|)
expr_stmt|;
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdSize
operator|=
name|flushThresholdSize
expr_stmt|;
block|}
name|TimeValue
name|flushThresholdPeriod
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDEX_TRANSLOG_FLUSH_THRESHOLD_PERIOD
argument_list|,
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdPeriod
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|flushThresholdPeriod
operator|.
name|equals
argument_list|(
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdPeriod
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating flush_threshold_period from [{}] to [{}]"
argument_list|,
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdPeriod
argument_list|,
name|flushThresholdPeriod
argument_list|)
expr_stmt|;
name|TranslogService
operator|.
name|this
operator|.
name|flushThresholdPeriod
operator|=
name|flushThresholdPeriod
expr_stmt|;
block|}
name|TimeValue
name|interval
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDEX_TRANSLOG_FLUSH_INTERVAL
argument_list|,
name|TranslogService
operator|.
name|this
operator|.
name|interval
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|interval
operator|.
name|equals
argument_list|(
name|TranslogService
operator|.
name|this
operator|.
name|interval
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating interval from [{}] to [{}]"
argument_list|,
name|TranslogService
operator|.
name|this
operator|.
name|interval
argument_list|,
name|interval
argument_list|)
expr_stmt|;
name|TranslogService
operator|.
name|this
operator|.
name|interval
operator|=
name|interval
expr_stmt|;
block|}
name|boolean
name|disableFlush
init|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_TRANSLOG_DISABLE_FLUSH
argument_list|,
name|TranslogService
operator|.
name|this
operator|.
name|disableFlush
argument_list|)
decl_stmt|;
if|if
condition|(
name|disableFlush
operator|!=
name|TranslogService
operator|.
name|this
operator|.
name|disableFlush
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating disable_flush from [{}] to [{}]"
argument_list|,
name|TranslogService
operator|.
name|this
operator|.
name|disableFlush
argument_list|,
name|disableFlush
argument_list|)
expr_stmt|;
name|TranslogService
operator|.
name|this
operator|.
name|disableFlush
operator|=
name|disableFlush
expr_stmt|;
block|}
block|}
block|}
DECL|method|computeNextInterval
specifier|private
name|TimeValue
name|computeNextInterval
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|interval
operator|.
name|millis
argument_list|()
operator|+
operator|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
name|interval
operator|.
name|millis
argument_list|()
argument_list|)
operator|)
argument_list|)
return|;
block|}
DECL|class|TranslogBasedFlush
specifier|private
class|class
name|TranslogBasedFlush
implements|implements
name|Runnable
block|{
DECL|field|lastFlushTime
specifier|private
specifier|volatile
name|long
name|lastFlushTime
init|=
name|System
operator|.
name|currentTimeMillis
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
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
return|return;
block|}
comment|// flush is disabled, but still reschedule
if|if
condition|(
name|disableFlush
condition|)
block|{
name|reschedule
argument_list|()
expr_stmt|;
return|return;
block|}
name|Translog
name|translog
init|=
name|indexShard
operator|.
name|engine
argument_list|()
operator|.
name|getTranslog
argument_list|()
decl_stmt|;
if|if
condition|(
name|translog
operator|==
literal|null
condition|)
block|{
name|reschedule
argument_list|()
expr_stmt|;
return|return;
block|}
name|int
name|currentNumberOfOperations
init|=
name|translog
operator|.
name|totalOperations
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentNumberOfOperations
operator|==
literal|0
condition|)
block|{
name|reschedule
argument_list|()
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|flushThresholdOperations
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|currentNumberOfOperations
operator|>
name|flushThresholdOperations
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"flushing translog, operations [{}], breached [{}]"
argument_list|,
name|currentNumberOfOperations
argument_list|,
name|flushThresholdOperations
argument_list|)
expr_stmt|;
name|asyncFlushAndReschedule
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
if|if
condition|(
name|flushThresholdSize
operator|.
name|bytes
argument_list|()
operator|>
literal|0
condition|)
block|{
name|long
name|sizeInBytes
init|=
name|translog
operator|.
name|sizeInBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|sizeInBytes
operator|>
name|flushThresholdSize
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"flushing translog, size [{}], breached [{}]"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|sizeInBytes
argument_list|)
argument_list|,
name|flushThresholdSize
argument_list|)
expr_stmt|;
name|asyncFlushAndReschedule
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
if|if
condition|(
name|flushThresholdPeriod
operator|.
name|millis
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
operator|(
name|threadPool
operator|.
name|estimatedTimeInMillis
argument_list|()
operator|-
name|lastFlushTime
operator|)
operator|>
name|flushThresholdPeriod
operator|.
name|millis
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"flushing translog, last_flush_time [{}], breached [{}]"
argument_list|,
name|lastFlushTime
argument_list|,
name|flushThresholdPeriod
argument_list|)
expr_stmt|;
name|asyncFlushAndReschedule
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
name|reschedule
argument_list|()
expr_stmt|;
block|}
DECL|method|reschedule
specifier|private
name|void
name|reschedule
parameter_list|()
block|{
name|future
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|computeNextInterval
argument_list|()
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|asyncFlushAndReschedule
specifier|private
name|void
name|asyncFlushAndReschedule
parameter_list|()
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|FLUSH
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|indexShard
operator|.
name|flush
argument_list|(
operator|new
name|FlushRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalIndexShardStateException
name|e
parameter_list|)
block|{
comment|// we are being closed, or in created state, ignore
block|}
catch|catch
parameter_list|(
name|FlushNotAllowedEngineException
name|e
parameter_list|)
block|{
comment|// ignore this exception, we are not allowed to perform flush
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to flush shard on translog threshold"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|lastFlushTime
operator|=
name|threadPool
operator|.
name|estimatedTimeInMillis
argument_list|()
expr_stmt|;
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|!=
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
name|future
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|computeNextInterval
argument_list|()
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
name|TranslogBasedFlush
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
