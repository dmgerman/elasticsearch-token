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
name|elasticsearch
operator|.
name|ElasticsearchTimeoutException
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
name|node
operator|.
name|DiscoveryNode
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
name|ESLogger
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
name|Callback
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
name|IndexShardClosedException
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
name|ConcurrentMap
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
comment|/**  * This class holds a collection of all on going recoveries on the current node (i.e., the node is the target node  * of those recoveries). The class is used to guarantee concurrent semantics such that once a recoveries was done/cancelled/failed  * no other thread will be able to find it. Last, the {@link RecoveryRef} inner class verifies that recovery temporary files  * and store will only be cleared once on going usage is finished.  */
end_comment

begin_class
DECL|class|RecoveriesCollection
specifier|public
class|class
name|RecoveriesCollection
block|{
comment|/** This is the single source of truth for ongoing recoveries. If it's not here, it was canceled or done */
DECL|field|onGoingRecoveries
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|Long
argument_list|,
name|RecoveryTarget
argument_list|>
name|onGoingRecoveries
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|ensureClusterStateVersionCallback
specifier|private
specifier|final
name|Callback
argument_list|<
name|Long
argument_list|>
name|ensureClusterStateVersionCallback
decl_stmt|;
DECL|method|RecoveriesCollection
specifier|public
name|RecoveriesCollection
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|Callback
argument_list|<
name|Long
argument_list|>
name|ensureClusterStateVersionCallback
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|ensureClusterStateVersionCallback
operator|=
name|ensureClusterStateVersionCallback
expr_stmt|;
block|}
comment|/**      * Starts are new recovery for the given shard, source node and state      *      * @return the id of the new recovery.      */
DECL|method|startRecovery
specifier|public
name|long
name|startRecovery
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|,
name|DiscoveryNode
name|sourceNode
parameter_list|,
name|PeerRecoveryTargetService
operator|.
name|RecoveryListener
name|listener
parameter_list|,
name|TimeValue
name|activityTimeout
parameter_list|)
block|{
name|RecoveryTarget
name|status
init|=
operator|new
name|RecoveryTarget
argument_list|(
name|indexShard
argument_list|,
name|sourceNode
argument_list|,
name|listener
argument_list|,
name|ensureClusterStateVersionCallback
argument_list|)
decl_stmt|;
name|RecoveryTarget
name|existingStatus
init|=
name|onGoingRecoveries
operator|.
name|putIfAbsent
argument_list|(
name|status
operator|.
name|recoveryId
argument_list|()
argument_list|,
name|status
argument_list|)
decl_stmt|;
assert|assert
name|existingStatus
operator|==
literal|null
operator|:
literal|"found two RecoveryStatus instances with the same id"
assert|;
name|logger
operator|.
name|trace
argument_list|(
literal|"{} started recovery from {}, id [{}]"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|,
name|sourceNode
argument_list|,
name|status
operator|.
name|recoveryId
argument_list|()
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|schedule
argument_list|(
name|activityTimeout
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|,
operator|new
name|RecoveryMonitor
argument_list|(
name|status
operator|.
name|recoveryId
argument_list|()
argument_list|,
name|status
operator|.
name|lastAccessTime
argument_list|()
argument_list|,
name|activityTimeout
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|status
operator|.
name|recoveryId
argument_list|()
return|;
block|}
comment|/**      * Resets the recovery and performs a recovery restart on the currently recovering index shard      *      * @see IndexShard#performRecoveryRestart()      */
DECL|method|resetRecovery
specifier|public
name|void
name|resetRecovery
parameter_list|(
name|long
name|id
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|RecoveryRef
name|ref
init|=
name|getRecoverySafe
argument_list|(
name|id
argument_list|,
name|shardId
argument_list|)
init|)
block|{
comment|// instead of adding complicated state to RecoveryTarget we just flip the
comment|// target instance when we reset a recovery, that way we have only one cleanup
comment|// path on the RecoveryTarget and are always within the bounds of ref-counting
comment|// which is important since we verify files are on disk etc. after we have written them etc.
name|RecoveryTarget
name|status
init|=
name|ref
operator|.
name|status
argument_list|()
decl_stmt|;
name|RecoveryTarget
name|resetRecovery
init|=
name|status
operator|.
name|resetRecovery
argument_list|()
decl_stmt|;
if|if
condition|(
name|onGoingRecoveries
operator|.
name|replace
argument_list|(
name|id
argument_list|,
name|status
argument_list|,
name|resetRecovery
argument_list|)
operator|==
literal|false
condition|)
block|{
name|resetRecovery
operator|.
name|cancel
argument_list|(
literal|"replace failed"
argument_list|)
expr_stmt|;
comment|// this is important otherwise we leak a reference to the store
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"failed to replace recovery target"
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**      * gets the {@link RecoveryTarget } for a given id. The RecoveryStatus returned has it's ref count already incremented      * to make sure it's safe to use. However, you must call {@link RecoveryTarget#decRef()} when you are done with it, typically      * by using this method in a try-with-resources clause.      *<p>      * Returns null if recovery is not found      */
DECL|method|getRecovery
specifier|public
name|RecoveryRef
name|getRecovery
parameter_list|(
name|long
name|id
parameter_list|)
block|{
name|RecoveryTarget
name|status
init|=
name|onGoingRecoveries
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|!=
literal|null
operator|&&
name|status
operator|.
name|tryIncRef
argument_list|()
condition|)
block|{
return|return
operator|new
name|RecoveryRef
argument_list|(
name|status
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/** Similar to {@link #getRecovery(long)} but throws an exception if no recovery is found */
DECL|method|getRecoverySafe
specifier|public
name|RecoveryRef
name|getRecoverySafe
parameter_list|(
name|long
name|id
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
block|{
name|RecoveryRef
name|recoveryRef
init|=
name|getRecovery
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|recoveryRef
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexShardClosedException
argument_list|(
name|shardId
argument_list|)
throw|;
block|}
assert|assert
name|recoveryRef
operator|.
name|status
argument_list|()
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|shardId
argument_list|)
assert|;
return|return
name|recoveryRef
return|;
block|}
comment|/** cancel the recovery with the given id (if found) and remove it from the recovery collection */
DECL|method|cancelRecovery
specifier|public
name|boolean
name|cancelRecovery
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
name|RecoveryTarget
name|removed
init|=
name|onGoingRecoveries
operator|.
name|remove
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|boolean
name|cancelled
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|removed
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} canceled recovery from {}, id [{}] (reason [{}])"
argument_list|,
name|removed
operator|.
name|shardId
argument_list|()
argument_list|,
name|removed
operator|.
name|sourceNode
argument_list|()
argument_list|,
name|removed
operator|.
name|recoveryId
argument_list|()
argument_list|,
name|reason
argument_list|)
expr_stmt|;
name|removed
operator|.
name|cancel
argument_list|(
name|reason
argument_list|)
expr_stmt|;
name|cancelled
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|cancelled
return|;
block|}
comment|/**      * fail the recovery with the given id (if found) and remove it from the recovery collection      *      * @param id               id of the recovery to fail      * @param e                exception with reason for the failure      * @param sendShardFailure true a shard failed message should be sent to the master      */
DECL|method|failRecovery
specifier|public
name|void
name|failRecovery
parameter_list|(
name|long
name|id
parameter_list|,
name|RecoveryFailedException
name|e
parameter_list|,
name|boolean
name|sendShardFailure
parameter_list|)
block|{
name|RecoveryTarget
name|removed
init|=
name|onGoingRecoveries
operator|.
name|remove
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|removed
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} failing recovery from {}, id [{}]. Send shard failure: [{}]"
argument_list|,
name|removed
operator|.
name|shardId
argument_list|()
argument_list|,
name|removed
operator|.
name|sourceNode
argument_list|()
argument_list|,
name|removed
operator|.
name|recoveryId
argument_list|()
argument_list|,
name|sendShardFailure
argument_list|)
expr_stmt|;
name|removed
operator|.
name|fail
argument_list|(
name|e
argument_list|,
name|sendShardFailure
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** mark the recovery with the given id as done (if found) */
DECL|method|markRecoveryAsDone
specifier|public
name|void
name|markRecoveryAsDone
parameter_list|(
name|long
name|id
parameter_list|)
block|{
name|RecoveryTarget
name|removed
init|=
name|onGoingRecoveries
operator|.
name|remove
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|removed
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} marking recovery from {} as done, id [{}]"
argument_list|,
name|removed
operator|.
name|shardId
argument_list|()
argument_list|,
name|removed
operator|.
name|sourceNode
argument_list|()
argument_list|,
name|removed
operator|.
name|recoveryId
argument_list|()
argument_list|)
expr_stmt|;
name|removed
operator|.
name|markAsDone
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** the number of ongoing recoveries */
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|onGoingRecoveries
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**      * cancel all ongoing recoveries for the given shard      *      * @param reason       reason for cancellation      * @param shardId      shardId for which to cancel recoveries      * @return true if a recovery was cancelled      */
DECL|method|cancelRecoveriesForShard
specifier|public
name|boolean
name|cancelRecoveriesForShard
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
name|boolean
name|cancelled
init|=
literal|false
decl_stmt|;
for|for
control|(
name|RecoveryTarget
name|status
range|:
name|onGoingRecoveries
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|status
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|shardId
argument_list|)
condition|)
block|{
name|cancelled
operator||=
name|cancelRecovery
argument_list|(
name|status
operator|.
name|recoveryId
argument_list|()
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|cancelled
return|;
block|}
comment|/**      * a reference to {@link RecoveryTarget}, which implements {@link AutoCloseable}. closing the reference      * causes {@link RecoveryTarget#decRef()} to be called. This makes sure that the underlying resources      * will not be freed until {@link RecoveryRef#close()} is called.      */
DECL|class|RecoveryRef
specifier|public
specifier|static
class|class
name|RecoveryRef
implements|implements
name|AutoCloseable
block|{
DECL|field|status
specifier|private
specifier|final
name|RecoveryTarget
name|status
decl_stmt|;
DECL|field|closed
specifier|private
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|/**          * Important: {@link RecoveryTarget#tryIncRef()} should          * be *successfully* called on status before          */
DECL|method|RecoveryRef
specifier|public
name|RecoveryRef
parameter_list|(
name|RecoveryTarget
name|status
parameter_list|)
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
name|this
operator|.
name|status
operator|.
name|setLastAccessTime
argument_list|()
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
if|if
condition|(
name|closed
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|status
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|status
specifier|public
name|RecoveryTarget
name|status
parameter_list|()
block|{
return|return
name|status
return|;
block|}
block|}
DECL|class|RecoveryMonitor
specifier|private
class|class
name|RecoveryMonitor
extends|extends
name|AbstractRunnable
block|{
DECL|field|recoveryId
specifier|private
specifier|final
name|long
name|recoveryId
decl_stmt|;
DECL|field|checkInterval
specifier|private
specifier|final
name|TimeValue
name|checkInterval
decl_stmt|;
DECL|field|lastSeenAccessTime
specifier|private
name|long
name|lastSeenAccessTime
decl_stmt|;
DECL|method|RecoveryMonitor
specifier|private
name|RecoveryMonitor
parameter_list|(
name|long
name|recoveryId
parameter_list|,
name|long
name|lastSeenAccessTime
parameter_list|,
name|TimeValue
name|checkInterval
parameter_list|)
block|{
name|this
operator|.
name|recoveryId
operator|=
name|recoveryId
expr_stmt|;
name|this
operator|.
name|checkInterval
operator|=
name|checkInterval
expr_stmt|;
name|this
operator|.
name|lastSeenAccessTime
operator|=
name|lastSeenAccessTime
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"unexpected error while monitoring recovery [{}]"
argument_list|,
name|e
argument_list|,
name|recoveryId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doRun
specifier|protected
name|void
name|doRun
parameter_list|()
throws|throws
name|Exception
block|{
name|RecoveryTarget
name|status
init|=
name|onGoingRecoveries
operator|.
name|get
argument_list|(
name|recoveryId
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[monitor] no status found for [{}], shutting down"
argument_list|,
name|recoveryId
argument_list|)
expr_stmt|;
return|return;
block|}
name|long
name|accessTime
init|=
name|status
operator|.
name|lastAccessTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|accessTime
operator|==
name|lastSeenAccessTime
condition|)
block|{
name|String
name|message
init|=
literal|"no activity after ["
operator|+
name|checkInterval
operator|+
literal|"]"
decl_stmt|;
name|failRecovery
argument_list|(
name|recoveryId
argument_list|,
operator|new
name|RecoveryFailedException
argument_list|(
name|status
operator|.
name|state
argument_list|()
argument_list|,
name|message
argument_list|,
operator|new
name|ElasticsearchTimeoutException
argument_list|(
name|message
argument_list|)
argument_list|)
argument_list|,
literal|true
comment|// to be safe, we don't know what go stuck
argument_list|)
expr_stmt|;
return|return;
block|}
name|lastSeenAccessTime
operator|=
name|accessTime
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"[monitor] rescheduling check for [{}]. last access time is [{}]"
argument_list|,
name|recoveryId
argument_list|,
name|lastSeenAccessTime
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|schedule
argument_list|(
name|checkInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

