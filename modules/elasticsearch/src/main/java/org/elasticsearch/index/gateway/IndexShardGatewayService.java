begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|gateway
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
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
name|ElasticSearchIllegalStateException
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
name|deletionpolicy
operator|.
name|SnapshotIndexCommit
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
name|EngineException
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
name|store
operator|.
name|Store
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
name|util
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
name|util
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
name|util
operator|.
name|settings
operator|.
name|Settings
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
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|IndexShardGatewayService
specifier|public
class|class
name|IndexShardGatewayService
extends|extends
name|AbstractIndexShardComponent
block|{
DECL|field|snapshotOnClose
specifier|private
specifier|final
name|boolean
name|snapshotOnClose
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|indexShard
specifier|private
specifier|final
name|InternalIndexShard
name|indexShard
decl_stmt|;
DECL|field|shardGateway
specifier|private
specifier|final
name|IndexShardGateway
name|shardGateway
decl_stmt|;
DECL|field|store
specifier|private
specifier|final
name|Store
name|store
decl_stmt|;
DECL|field|lastIndexVersion
specifier|private
specifier|volatile
name|long
name|lastIndexVersion
decl_stmt|;
DECL|field|lastTranslogId
specifier|private
specifier|volatile
name|long
name|lastTranslogId
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|lastTranslogSize
specifier|private
specifier|volatile
name|int
name|lastTranslogSize
decl_stmt|;
DECL|field|recovered
specifier|private
specifier|final
name|AtomicBoolean
name|recovered
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|field|snapshotInterval
specifier|private
specifier|final
name|TimeValue
name|snapshotInterval
decl_stmt|;
DECL|field|snapshotScheduleFuture
specifier|private
specifier|volatile
name|ScheduledFuture
name|snapshotScheduleFuture
decl_stmt|;
DECL|method|IndexShardGatewayService
annotation|@
name|Inject
specifier|public
name|IndexShardGatewayService
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|IndexShard
name|indexShard
parameter_list|,
name|IndexShardGateway
name|shardGateway
parameter_list|,
name|Store
name|store
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
name|indexShard
operator|=
operator|(
name|InternalIndexShard
operator|)
name|indexShard
expr_stmt|;
name|this
operator|.
name|shardGateway
operator|=
name|shardGateway
expr_stmt|;
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|this
operator|.
name|snapshotOnClose
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"snapshotOnClose"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|snapshotInterval
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"snapshotInterval"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Should be called when the shard routing state has changed (note, after the state has been set on the shard).      */
DECL|method|routingStateChanged
specifier|public
name|void
name|routingStateChanged
parameter_list|()
block|{
name|scheduleSnapshotIfNeeded
argument_list|()
expr_stmt|;
block|}
comment|/**      * Recovers the state of the shard from the gateway.      */
DECL|method|recover
specifier|public
specifier|synchronized
name|void
name|recover
parameter_list|()
throws|throws
name|IndexShardGatewayRecoveryException
throws|,
name|IgnoreGatewayRecoveryException
block|{
if|if
condition|(
name|recovered
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|primary
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Trying to recover when the shard is in backup state"
argument_list|)
throw|;
block|}
comment|// clear the store, we are going to recover into it
try|try
block|{
name|store
operator|.
name|deleteContent
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Failed to delete store before recovery from gateway"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|indexShard
operator|.
name|recovering
argument_list|()
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Starting recovery from {}"
argument_list|,
name|shardGateway
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
name|RecoveryStatus
name|recoveryStatus
init|=
name|shardGateway
operator|.
name|recover
argument_list|()
decl_stmt|;
comment|// update the last up to date values
name|indexShard
operator|.
name|snapshot
argument_list|(
operator|new
name|Engine
operator|.
name|SnapshotHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|snapshot
parameter_list|(
name|SnapshotIndexCommit
name|snapshotIndexCommit
parameter_list|,
name|Translog
operator|.
name|Snapshot
name|translogSnapshot
parameter_list|)
throws|throws
name|EngineException
block|{
name|lastIndexVersion
operator|=
name|snapshotIndexCommit
operator|.
name|getVersion
argument_list|()
expr_stmt|;
name|lastTranslogId
operator|=
name|translogSnapshot
operator|.
name|translogId
argument_list|()
expr_stmt|;
name|lastTranslogSize
operator|=
name|translogSnapshot
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// start the shard if the gateway has not started it already
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|!=
name|IndexShardState
operator|.
name|STARTED
condition|)
block|{
name|indexShard
operator|.
name|start
argument_list|()
expr_stmt|;
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
literal|"Recovery completed from "
argument_list|)
operator|.
name|append
argument_list|(
name|shardGateway
argument_list|)
operator|.
name|append
argument_list|(
literal|", took ["
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
literal|"    Index    : numberOfFiles      ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|numberOfFiles
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] with totalSize ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|totalSize
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
literal|"    Translog : numberOfOperations ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryStatus
operator|.
name|translog
argument_list|()
operator|.
name|numberOfOperations
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] with totalSize ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryStatus
operator|.
name|translog
argument_list|()
operator|.
name|totalSize
argument_list|()
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
comment|// refresh the shard
name|indexShard
operator|.
name|refresh
argument_list|(
operator|new
name|Engine
operator|.
name|Refresh
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|scheduleSnapshotIfNeeded
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IgnoreGatewayRecoveryException
argument_list|(
name|shardId
argument_list|,
literal|"Already recovered"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Snapshots the given shard into the gateway.      */
DECL|method|snapshot
specifier|public
specifier|synchronized
name|void
name|snapshot
parameter_list|()
throws|throws
name|IndexShardGatewaySnapshotFailedException
block|{
if|if
condition|(
operator|!
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|primary
argument_list|()
condition|)
block|{
return|return;
comment|//            throw new IndexShardGatewaySnapshotNotAllowedException(shardId, "Snapshot not allowed on non primary shard");
block|}
if|if
condition|(
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|relocating
argument_list|()
condition|)
block|{
comment|// do not snapshot when in the process of relocation of primaries so we won't get conflicts
return|return;
block|}
try|try
block|{
name|indexShard
operator|.
name|snapshot
argument_list|(
operator|new
name|Engine
operator|.
name|SnapshotHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|snapshot
parameter_list|(
name|SnapshotIndexCommit
name|snapshotIndexCommit
parameter_list|,
name|Translog
operator|.
name|Snapshot
name|translogSnapshot
parameter_list|)
throws|throws
name|EngineException
block|{
if|if
condition|(
name|lastIndexVersion
operator|!=
name|snapshotIndexCommit
operator|.
name|getVersion
argument_list|()
operator|||
name|lastTranslogId
operator|!=
name|translogSnapshot
operator|.
name|translogId
argument_list|()
operator|||
name|lastTranslogSize
operator|!=
name|translogSnapshot
operator|.
name|size
argument_list|()
condition|)
block|{
name|shardGateway
operator|.
name|snapshot
argument_list|(
name|snapshotIndexCommit
argument_list|,
name|translogSnapshot
argument_list|)
expr_stmt|;
name|lastIndexVersion
operator|=
name|snapshotIndexCommit
operator|.
name|getVersion
argument_list|()
expr_stmt|;
name|lastTranslogId
operator|=
name|translogSnapshot
operator|.
name|translogId
argument_list|()
expr_stmt|;
name|lastTranslogSize
operator|=
name|translogSnapshot
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalIndexShardStateException
name|e
parameter_list|)
block|{
comment|// ignore, that's fine
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
literal|"Failed to snapshot on close"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|snapshotScheduleFuture
operator|!=
literal|null
condition|)
block|{
name|snapshotScheduleFuture
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|snapshotScheduleFuture
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|snapshotOnClose
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Snapshotting on close ..."
argument_list|)
expr_stmt|;
name|snapshot
argument_list|()
expr_stmt|;
block|}
name|shardGateway
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|scheduleSnapshotIfNeeded
specifier|private
specifier|synchronized
name|void
name|scheduleSnapshotIfNeeded
parameter_list|()
block|{
if|if
condition|(
operator|!
name|shardGateway
operator|.
name|requiresSnapshotScheduling
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|primary
argument_list|()
condition|)
block|{
comment|// we only do snapshotting on the primary shard
return|return;
block|}
if|if
condition|(
operator|!
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|started
argument_list|()
condition|)
block|{
comment|// we only schedule when the cluster assumes we have started
return|return;
block|}
if|if
condition|(
name|snapshotScheduleFuture
operator|!=
literal|null
condition|)
block|{
comment|// we are already scheduling this one, ignore
return|return;
block|}
if|if
condition|(
name|snapshotInterval
operator|.
name|millis
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// we need to schedule snapshot
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Scheduling snapshot every [{}]"
argument_list|,
name|snapshotInterval
argument_list|)
expr_stmt|;
block|}
name|snapshotScheduleFuture
operator|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
operator|new
name|SnapshotRunnable
argument_list|()
argument_list|,
name|snapshotInterval
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SnapshotRunnable
specifier|private
class|class
name|SnapshotRunnable
implements|implements
name|Runnable
block|{
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|snapshot
argument_list|()
expr_stmt|;
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
literal|"Failed to snapshot"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

