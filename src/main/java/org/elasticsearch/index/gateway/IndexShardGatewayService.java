begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterService
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
name|snapshots
operator|.
name|IndexShardSnapshotAndRestoreService
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
name|RecoveryState
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
name|io
operator|.
name|IOException
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
DECL|class|IndexShardGatewayService
specifier|public
class|class
name|IndexShardGatewayService
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|Closeable
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|indexShard
specifier|private
specifier|final
name|IndexShard
name|indexShard
decl_stmt|;
DECL|field|shardGateway
specifier|private
specifier|final
name|IndexShardGateway
name|shardGateway
decl_stmt|;
DECL|field|snapshotService
specifier|private
specifier|final
name|IndexShardSnapshotAndRestoreService
name|snapshotService
decl_stmt|;
DECL|field|recoveryState
specifier|private
name|RecoveryState
name|recoveryState
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndexShardGatewayService
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
name|IndexShardSnapshotAndRestoreService
name|snapshotService
parameter_list|,
name|ClusterService
name|clusterService
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
name|snapshotService
operator|=
name|snapshotService
expr_stmt|;
name|this
operator|.
name|recoveryState
operator|=
operator|new
name|RecoveryState
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
name|this
operator|.
name|recoveryState
operator|.
name|setType
argument_list|(
name|RecoveryState
operator|.
name|Type
operator|.
name|GATEWAY
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
block|}
comment|/**      * Should be called when the shard routing state has changed (note, after the state has been set on the shard).      */
DECL|method|routingStateChanged
specifier|public
name|void
name|routingStateChanged
parameter_list|()
block|{     }
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
DECL|method|onIgnoreRecovery
name|void
name|onIgnoreRecovery
parameter_list|(
name|String
name|reason
parameter_list|)
function_decl|;
DECL|method|onRecoveryFailed
name|void
name|onRecoveryFailed
parameter_list|(
name|IndexShardGatewayRecoveryException
name|e
parameter_list|)
function_decl|;
block|}
DECL|method|recoveryState
specifier|public
name|RecoveryState
name|recoveryState
parameter_list|()
block|{
if|if
condition|(
name|recoveryState
operator|.
name|getTimer
argument_list|()
operator|.
name|startTime
argument_list|()
operator|>
literal|0
operator|&&
name|recoveryState
operator|.
name|getStage
argument_list|()
operator|!=
name|RecoveryState
operator|.
name|Stage
operator|.
name|DONE
condition|)
block|{
name|recoveryState
operator|.
name|getTimer
argument_list|()
operator|.
name|time
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|recoveryState
operator|.
name|getTimer
argument_list|()
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|recoveryState
return|;
block|}
comment|/**      * Recovers the state of the shard from the gateway.      */
DECL|method|recover
specifier|public
name|void
name|recover
parameter_list|(
specifier|final
name|boolean
name|indexShouldExists
parameter_list|,
specifier|final
name|RecoveryListener
name|listener
parameter_list|)
throws|throws
name|IndexShardGatewayRecoveryException
throws|,
name|IgnoreGatewayRecoveryException
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
comment|// got closed on us, just ignore this recovery
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|"shard closed"
argument_list|)
expr_stmt|;
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
name|listener
operator|.
name|onRecoveryFailed
argument_list|(
operator|new
name|IndexShardGatewayRecoveryException
argument_list|(
name|shardId
argument_list|,
literal|"Trying to recover when the shard is in backup state"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
if|if
condition|(
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|restoreSource
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|indexShard
operator|.
name|recovering
argument_list|(
literal|"from snapshot"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|indexShard
operator|.
name|recovering
argument_list|(
literal|"from gateway"
argument_list|)
expr_stmt|;
block|}
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
name|threadPool
operator|.
name|generic
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
name|recoveryState
operator|.
name|getTimer
argument_list|()
operator|.
name|startTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setTargetNode
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setStage
argument_list|(
name|RecoveryState
operator|.
name|Stage
operator|.
name|INIT
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setPrimary
argument_list|(
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|primary
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|restoreSource
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"restoring from {} ..."
argument_list|,
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|restoreSource
argument_list|()
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setType
argument_list|(
name|RecoveryState
operator|.
name|Type
operator|.
name|SNAPSHOT
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setRestoreSource
argument_list|(
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|restoreSource
argument_list|()
argument_list|)
expr_stmt|;
name|snapshotService
operator|.
name|restore
argument_list|(
name|recoveryState
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"starting recovery from {} ..."
argument_list|,
name|shardGateway
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setType
argument_list|(
name|RecoveryState
operator|.
name|Type
operator|.
name|GATEWAY
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setSourceNode
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|)
expr_stmt|;
name|shardGateway
operator|.
name|recover
argument_list|(
name|indexShouldExists
argument_list|,
name|recoveryState
argument_list|)
expr_stmt|;
block|}
comment|// start the shard if the gateway has not started it already. Note that if the gateway
comment|// moved shard to POST_RECOVERY, it may have been started as well if:
comment|// 1) master sent a new cluster state indicating shard is initializing
comment|// 2) IndicesClusterStateService#applyInitializingShard will send a shard started event
comment|// 3) Master will mark shard as started and this will be processed locally.
name|IndexShardState
name|shardState
init|=
name|indexShard
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardState
operator|!=
name|IndexShardState
operator|.
name|POST_RECOVERY
operator|&&
name|shardState
operator|!=
name|IndexShardState
operator|.
name|STARTED
condition|)
block|{
name|indexShard
operator|.
name|postRecovery
argument_list|(
literal|"post recovery from gateway"
argument_list|)
expr_stmt|;
block|}
comment|// refresh the shard
name|indexShard
operator|.
name|refresh
argument_list|(
literal|"post_gateway"
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|getTimer
argument_list|()
operator|.
name|time
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|recoveryState
operator|.
name|getTimer
argument_list|()
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setStage
argument_list|(
name|RecoveryState
operator|.
name|Stage
operator|.
name|DONE
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
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
literal|"recovery completed from "
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
name|timeValueMillis
argument_list|(
name|recoveryState
operator|.
name|getTimer
argument_list|()
operator|.
name|time
argument_list|()
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
literal|"    index    : files           ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryState
operator|.
name|getIndex
argument_list|()
operator|.
name|totalFileCount
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] with total_size ["
argument_list|)
operator|.
name|append
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|recoveryState
operator|.
name|getIndex
argument_list|()
operator|.
name|totalByteCount
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"], took["
argument_list|)
operator|.
name|append
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|recoveryState
operator|.
name|getIndex
argument_list|()
operator|.
name|time
argument_list|()
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
literal|"             : recovered_files ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryState
operator|.
name|getIndex
argument_list|()
operator|.
name|numberOfRecoveredFiles
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] with total_size ["
argument_list|)
operator|.
name|append
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|recoveryState
operator|.
name|getIndex
argument_list|()
operator|.
name|recoveredTotalSize
argument_list|()
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
literal|"             : reusing_files   ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryState
operator|.
name|getIndex
argument_list|()
operator|.
name|reusedFileCount
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] with total_size ["
argument_list|)
operator|.
name|append
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|recoveryState
operator|.
name|getIndex
argument_list|()
operator|.
name|reusedByteCount
argument_list|()
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
literal|"    start    : took ["
argument_list|)
operator|.
name|append
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|recoveryState
operator|.
name|getStart
argument_list|()
operator|.
name|time
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"], check_index ["
argument_list|)
operator|.
name|append
argument_list|(
name|timeValueMillis
argument_list|(
name|recoveryState
operator|.
name|getStart
argument_list|()
operator|.
name|checkIndexTime
argument_list|()
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
literal|"    translog : number_of_operations ["
argument_list|)
operator|.
name|append
argument_list|(
name|recoveryState
operator|.
name|getTranslog
argument_list|()
operator|.
name|currentTranslogOperations
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"], took ["
argument_list|)
operator|.
name|append
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|recoveryState
operator|.
name|getTranslog
argument_list|()
operator|.
name|time
argument_list|()
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
name|trace
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
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
literal|"recovery completed from [{}], took [{}]"
argument_list|,
name|shardGateway
argument_list|,
name|timeValueMillis
argument_list|(
name|recoveryState
operator|.
name|getTimer
argument_list|()
operator|.
name|time
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|listener
operator|.
name|onRecoveryDone
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexShardGatewayRecoveryException
name|e
parameter_list|)
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
comment|// got closed on us, just ignore this recovery
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|"shard closed"
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IndexShardClosedException
operator|)
operator|||
operator|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IndexShardNotStartedException
operator|)
condition|)
block|{
comment|// got closed on us, just ignore this recovery
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|"shard closed"
argument_list|)
expr_stmt|;
return|return;
block|}
name|listener
operator|.
name|onRecoveryFailed
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexShardClosedException
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|"shard closed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexShardNotStartedException
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|"shard closed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
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
comment|// got closed on us, just ignore this recovery
name|listener
operator|.
name|onIgnoreRecovery
argument_list|(
literal|"shard closed"
argument_list|)
expr_stmt|;
return|return;
block|}
name|listener
operator|.
name|onRecoveryFailed
argument_list|(
operator|new
name|IndexShardGatewayRecoveryException
argument_list|(
name|shardId
argument_list|,
literal|"failed recovery"
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|shardGateway
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

