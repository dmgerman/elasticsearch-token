begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
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
name|IndexOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|StopWatch
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
name|io
operator|.
name|stream
operator|.
name|VoidStreamable
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
name|ConcurrentCollections
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
name|IndexShardMissingException
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
name|RecoveryEngineException
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
name|service
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
name|index
operator|.
name|shard
operator|.
name|service
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
name|service
operator|.
name|InternalIndexShard
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
name|IndexMissingException
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
name|IndicesLifecycle
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
name|indices
operator|.
name|recovery
operator|.
name|throttler
operator|.
name|RecoveryThrottler
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
name|*
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|ConcurrentMap
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
name|*
import|;
end_import

begin_comment
comment|/**  * The recovery target handles recoveries of peer shards of the shard+node to recover to.  *  *<p>Note, it can be safely assumed that there will only be a single recovery per shard (index+id) and  * not several of them (since we don't allocate several shard replicas to the same node).  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|RecoveryTarget
specifier|public
class|class
name|RecoveryTarget
extends|extends
name|AbstractComponent
block|{
DECL|class|Actions
specifier|public
specifier|static
class|class
name|Actions
block|{
DECL|field|FILE_CHUNK
specifier|public
specifier|static
specifier|final
name|String
name|FILE_CHUNK
init|=
literal|"index/shard/recovery/fileChunk"
decl_stmt|;
DECL|field|CLEAN_FILES
specifier|public
specifier|static
specifier|final
name|String
name|CLEAN_FILES
init|=
literal|"index/shard/recovery/cleanFiles"
decl_stmt|;
DECL|field|TRANSLOG_OPS
specifier|public
specifier|static
specifier|final
name|String
name|TRANSLOG_OPS
init|=
literal|"index/shard/recovery/translogOps"
decl_stmt|;
DECL|field|PREPARE_TRANSLOG
specifier|public
specifier|static
specifier|final
name|String
name|PREPARE_TRANSLOG
init|=
literal|"index/shard/recovery/prepareTranslog"
decl_stmt|;
DECL|field|FINALIZE
specifier|public
specifier|static
specifier|final
name|String
name|FINALIZE
init|=
literal|"index/shard/recovery/finalize"
decl_stmt|;
block|}
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|recoveryThrottler
specifier|private
specifier|final
name|RecoveryThrottler
name|recoveryThrottler
decl_stmt|;
DECL|field|onGoingRecoveries
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|ShardId
argument_list|,
name|OnGoingRecovery
argument_list|>
name|onGoingRecoveries
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|method|RecoveryTarget
annotation|@
name|Inject
specifier|public
name|RecoveryTarget
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|IndicesLifecycle
name|indicesLifecycle
parameter_list|,
name|RecoveryThrottler
name|recoveryThrottler
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
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|recoveryThrottler
operator|=
name|recoveryThrottler
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|Actions
operator|.
name|FILE_CHUNK
argument_list|,
operator|new
name|FileChunkTransportRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|Actions
operator|.
name|CLEAN_FILES
argument_list|,
operator|new
name|CleanFilesRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|Actions
operator|.
name|PREPARE_TRANSLOG
argument_list|,
operator|new
name|PrepareForTranslogOperationsRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|Actions
operator|.
name|TRANSLOG_OPS
argument_list|,
operator|new
name|TranslogOperationsRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|Actions
operator|.
name|FINALIZE
argument_list|,
operator|new
name|FinalizeRecoveryRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
name|indicesLifecycle
operator|.
name|addListener
argument_list|(
operator|new
name|IndicesLifecycle
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|beforeIndexShardClosed
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|Nullable
name|IndexShard
name|indexShard
parameter_list|,
name|boolean
name|delete
parameter_list|)
block|{
name|removeAndCleanOnGoingRecovery
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|startRecovery
specifier|public
name|void
name|startRecovery
parameter_list|(
specifier|final
name|StartRecoveryRequest
name|request
parameter_list|,
name|boolean
name|fromRetry
parameter_list|,
specifier|final
name|RecoveryListener
name|listener
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|sourceNode
argument_list|()
operator|==
literal|null
condition|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|false
argument_list|,
literal|"No node to recovery from, retry on next cluster state update"
argument_list|)
expr_stmt|;
return|return;
block|}
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
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
operator|==
literal|null
condition|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|false
argument_list|,
literal|"index missing, stop recovery"
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|InternalIndexShard
name|shard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indexService
operator|.
name|shard
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shard
operator|==
literal|null
condition|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|false
argument_list|,
literal|"shard missing, stop recovery"
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|fromRetry
condition|)
block|{
try|try
block|{
name|shard
operator|.
name|recovering
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalIndexShardStateException
name|e
parameter_list|)
block|{
comment|// that's fine, since we might be called concurrently, just ignore this, we are already recovering
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|false
argument_list|,
literal|"already in recovering process, "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|false
argument_list|,
literal|"shard closed, stop recovery"
argument_list|)
expr_stmt|;
return|return;
block|}
name|threadPool
operator|.
name|cached
argument_list|()
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
name|doRecovery
argument_list|(
name|shard
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|doRecovery
specifier|private
name|void
name|doRecovery
parameter_list|(
specifier|final
name|InternalIndexShard
name|shard
parameter_list|,
specifier|final
name|StartRecoveryRequest
name|request
parameter_list|,
specifier|final
name|RecoveryListener
name|listener
parameter_list|)
block|{
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|false
argument_list|,
literal|"shard closed, stop recovery"
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|recoveryThrottler
operator|.
name|tryRecovery
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
literal|"peer recovery target"
argument_list|)
condition|)
block|{
name|listener
operator|.
name|onRetryRecovery
argument_list|(
name|recoveryThrottler
operator|.
name|throttleInterval
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] starting recovery from {}"
argument_list|,
name|request
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
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|sourceNode
argument_list|()
argument_list|)
expr_stmt|;
name|onGoingRecoveries
operator|.
name|put
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
operator|new
name|OnGoingRecovery
argument_list|()
argument_list|)
expr_stmt|;
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
decl_stmt|;
name|RecoveryResponse
name|recoveryStatus
init|=
name|transportService
operator|.
name|submitRequest
argument_list|(
name|request
operator|.
name|sourceNode
argument_list|()
argument_list|,
name|RecoverySource
operator|.
name|Actions
operator|.
name|START_RECOVERY
argument_list|,
name|request
argument_list|,
operator|new
name|FutureTransportResponseHandler
argument_list|<
name|RecoveryResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|RecoveryResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|RecoveryResponse
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|txGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|recoveryStatus
operator|.
name|retry
condition|)
block|{
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|false
argument_list|,
literal|"shard closed, stop recovery"
argument_list|)
expr_stmt|;
return|return;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] retrying recovery in [{}], source shard is busy"
argument_list|,
name|request
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
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|recoveryThrottler
operator|.
name|throttleInterval
argument_list|()
argument_list|)
expr_stmt|;
name|removeAndCleanOnGoingRecovery
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onRetryRecovery
argument_list|(
name|recoveryThrottler
operator|.
name|throttleInterval
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"recovery completed from "
argument_list|)
operator|.
name|append
argument_list|(
name|request
operator|.
name|sourceNode
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", took["
argument_list|)
operator|.
name|append
argument_list|(
name|stopWatch
operator|.
name|totalTime
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"   phase1: recovered_files ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryStatus
operator|.
name|phase1FileNames
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
operator|.
name|append
argument_list|(
literal|" with total_size of ["
argument_list|)
operator|.
name|append
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|recoveryStatus
operator|.
name|phase1TotalSize
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
operator|.
name|append
argument_list|(
literal|", took ["
argument_list|)
operator|.
name|append
argument_list|(
name|timeValueMillis
argument_list|(
name|recoveryStatus
operator|.
name|phase1Time
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"], throttling_wait ["
argument_list|)
operator|.
name|append
argument_list|(
name|timeValueMillis
argument_list|(
name|recoveryStatus
operator|.
name|phase1ThrottlingWaitTime
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"         : reusing_files   ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryStatus
operator|.
name|phase1ExistingFileNames
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] with total_size of ["
argument_list|)
operator|.
name|append
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|recoveryStatus
operator|.
name|phase1ExistingTotalSize
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"   phase2: recovered ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryStatus
operator|.
name|phase2Operations
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
operator|.
name|append
argument_list|(
literal|" transaction log operations"
argument_list|)
operator|.
name|append
argument_list|(
literal|", took ["
argument_list|)
operator|.
name|append
argument_list|(
name|timeValueMillis
argument_list|(
name|recoveryStatus
operator|.
name|phase2Time
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"   phase3: recovered ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryStatus
operator|.
name|phase3Operations
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
operator|.
name|append
argument_list|(
literal|" transaction log operations"
argument_list|)
operator|.
name|append
argument_list|(
literal|", took ["
argument_list|)
operator|.
name|append
argument_list|(
name|timeValueMillis
argument_list|(
name|recoveryStatus
operator|.
name|phase3Time
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|removeAndCleanOnGoingRecovery
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onRecoveryDone
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|removeAndCleanOnGoingRecovery
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|false
argument_list|,
literal|"shard closed, stop recovery"
argument_list|)
expr_stmt|;
return|return;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] recovery from [{}] failed"
argument_list|,
name|e
argument_list|,
name|request
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
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|sourceNode
argument_list|()
argument_list|)
expr_stmt|;
name|Throwable
name|cause
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|RecoveryEngineException
condition|)
block|{
comment|// unwrap an exception that was thrown as part of the recovery
name|cause
operator|=
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
comment|// do it twice, in case we have double transport exception
name|cause
operator|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|cause
argument_list|)
expr_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|RecoveryEngineException
condition|)
block|{
comment|// unwrap an exception that was thrown as part of the recovery
name|cause
operator|=
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|cause
operator|instanceof
name|IndexShardNotStartedException
operator|||
name|cause
operator|instanceof
name|IndexMissingException
operator|||
name|cause
operator|instanceof
name|IndexShardMissingException
condition|)
block|{
name|listener
operator|.
name|onRetryRecovery
argument_list|(
name|recoveryThrottler
operator|.
name|throttleInterval
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|cause
operator|instanceof
name|ConnectTransportException
condition|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|true
argument_list|,
literal|"source node disconnected"
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|cause
operator|instanceof
name|IndexShardClosedException
condition|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|true
argument_list|,
literal|"source node disconnected"
argument_list|)
expr_stmt|;
return|return;
block|}
name|listener
operator|.
name|onRecoveryFailure
argument_list|(
operator|new
name|RecoveryFailedException
argument_list|(
name|request
argument_list|,
name|e
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|recoveryThrottler
operator|.
name|recoveryDone
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
literal|"peer recovery target"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|interface|RecoveryListener
specifier|public
specifier|static
interface|interface
name|RecoveryListener
block|{
DECL|method|onRecoveryDone
name|void
name|onRecoveryDone
parameter_list|()
function_decl|;
DECL|method|onRetryRecovery
name|void
name|onRetryRecovery
parameter_list|(
name|TimeValue
name|retryAfter
parameter_list|)
function_decl|;
DECL|method|onIgnoreRecovery
name|void
name|onIgnoreRecovery
parameter_list|(
name|boolean
name|cleanShard
parameter_list|,
name|String
name|reason
parameter_list|)
function_decl|;
DECL|method|onRecoveryFailure
name|void
name|onRecoveryFailure
parameter_list|(
name|RecoveryFailedException
name|e
parameter_list|,
name|boolean
name|sendShardFailure
parameter_list|)
function_decl|;
block|}
DECL|method|removeAndCleanOnGoingRecovery
specifier|private
name|void
name|removeAndCleanOnGoingRecovery
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
comment|// clean it from the on going recoveries since it is being closed
name|OnGoingRecovery
name|onGoingRecovery
init|=
name|onGoingRecoveries
operator|.
name|remove
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|onGoingRecovery
operator|!=
literal|null
condition|)
block|{
comment|// clean open index outputs
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|IndexOutput
argument_list|>
name|entry
range|:
name|onGoingRecovery
operator|.
name|openIndexOutputs
operator|.
name|entrySet
argument_list|()
control|)
block|{
synchronized|synchronized
init|(
name|entry
operator|.
name|getValue
argument_list|()
init|)
block|{
try|try
block|{
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
block|}
DECL|class|OnGoingRecovery
specifier|static
class|class
name|OnGoingRecovery
block|{
DECL|field|openIndexOutputs
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|IndexOutput
argument_list|>
name|openIndexOutputs
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
block|}
DECL|class|PrepareForTranslogOperationsRequestHandler
class|class
name|PrepareForTranslogOperationsRequestHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|RecoveryPrepareForTranslogOperationsRequest
argument_list|>
block|{
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|RecoveryPrepareForTranslogOperationsRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|RecoveryPrepareForTranslogOperationsRequest
argument_list|()
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|RecoveryPrepareForTranslogOperationsRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|InternalIndexShard
name|shard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|shardSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|shard
operator|.
name|performRecoveryPrepareForTranslog
argument_list|()
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|VoidStreamable
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|FinalizeRecoveryRequestHandler
class|class
name|FinalizeRecoveryRequestHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|RecoveryFinalizeRecoveryRequest
argument_list|>
block|{
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|RecoveryFinalizeRecoveryRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|RecoveryFinalizeRecoveryRequest
argument_list|()
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|RecoveryFinalizeRecoveryRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|InternalIndexShard
name|shard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|shardSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|shard
operator|.
name|performRecoveryFinalization
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|VoidStreamable
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TranslogOperationsRequestHandler
class|class
name|TranslogOperationsRequestHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|RecoveryTranslogOperationsRequest
argument_list|>
block|{
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|RecoveryTranslogOperationsRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|RecoveryTranslogOperationsRequest
argument_list|()
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|RecoveryTranslogOperationsRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|InternalIndexShard
name|shard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|shardSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Translog
operator|.
name|Operation
name|operation
range|:
name|request
operator|.
name|operations
argument_list|()
control|)
block|{
name|shard
operator|.
name|performRecoveryOperation
argument_list|(
name|operation
argument_list|)
expr_stmt|;
block|}
name|channel
operator|.
name|sendResponse
argument_list|(
name|VoidStreamable
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|CleanFilesRequestHandler
class|class
name|CleanFilesRequestHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|RecoveryCleanFilesRequest
argument_list|>
block|{
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|RecoveryCleanFilesRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|RecoveryCleanFilesRequest
argument_list|()
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|RecoveryCleanFilesRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|InternalIndexShard
name|shard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|shardSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|existingFile
range|:
name|shard
operator|.
name|store
argument_list|()
operator|.
name|directory
argument_list|()
operator|.
name|listAll
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|request
operator|.
name|snapshotFiles
argument_list|()
operator|.
name|contains
argument_list|(
name|existingFile
argument_list|)
condition|)
block|{
name|shard
operator|.
name|store
argument_list|()
operator|.
name|directory
argument_list|()
operator|.
name|deleteFile
argument_list|(
name|existingFile
argument_list|)
expr_stmt|;
block|}
block|}
name|channel
operator|.
name|sendResponse
argument_list|(
name|VoidStreamable
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|FileChunkTransportRequestHandler
class|class
name|FileChunkTransportRequestHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|RecoveryFileChunkRequest
argument_list|>
block|{
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|RecoveryFileChunkRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|RecoveryFileChunkRequest
argument_list|()
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
specifier|final
name|RecoveryFileChunkRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|InternalIndexShard
name|shard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|shardSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|OnGoingRecovery
name|onGoingRecovery
init|=
name|onGoingRecoveries
operator|.
name|get
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|onGoingRecovery
operator|==
literal|null
condition|)
block|{
comment|// shard is getting closed on us
throw|throw
operator|new
name|IndexShardClosedException
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
throw|;
block|}
name|IndexOutput
name|indexOutput
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|position
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// first request
name|indexOutput
operator|=
name|onGoingRecovery
operator|.
name|openIndexOutputs
operator|.
name|remove
argument_list|(
name|request
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexOutput
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|indexOutput
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
name|indexOutput
operator|=
name|shard
operator|.
name|store
argument_list|()
operator|.
name|directory
argument_list|()
operator|.
name|createOutput
argument_list|(
name|request
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|onGoingRecovery
operator|.
name|openIndexOutputs
operator|.
name|put
argument_list|(
name|request
operator|.
name|name
argument_list|()
argument_list|,
name|indexOutput
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|indexOutput
operator|=
name|onGoingRecovery
operator|.
name|openIndexOutputs
operator|.
name|get
argument_list|(
name|request
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexOutput
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"No ongoing output file to write to, request: "
operator|+
name|request
argument_list|)
throw|;
block|}
synchronized|synchronized
init|(
name|indexOutput
init|)
block|{
try|try
block|{
name|indexOutput
operator|.
name|writeBytes
argument_list|(
name|request
operator|.
name|content
argument_list|()
argument_list|,
name|request
operator|.
name|contentLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexOutput
operator|.
name|getFilePointer
argument_list|()
operator|==
name|request
operator|.
name|length
argument_list|()
condition|)
block|{
comment|// we are done
name|indexOutput
operator|.
name|close
argument_list|()
expr_stmt|;
name|onGoingRecovery
operator|.
name|openIndexOutputs
operator|.
name|remove
argument_list|(
name|request
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|onGoingRecovery
operator|.
name|openIndexOutputs
operator|.
name|remove
argument_list|(
name|request
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|indexOutput
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
name|channel
operator|.
name|sendResponse
argument_list|(
name|VoidStreamable
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

