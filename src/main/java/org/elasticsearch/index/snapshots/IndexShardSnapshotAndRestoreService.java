begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.snapshots
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|snapshots
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
name|metadata
operator|.
name|SnapshotId
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
name|RestoreSource
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
name|SnapshotFailedEngineException
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
name|AbstractIndexShardComponent
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
name|repositories
operator|.
name|RepositoriesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|RestoreService
import|;
end_import

begin_comment
comment|/**  * Shard level snapshot and restore service  *<p/>  * Performs snapshot and restore operations on the shard level.  */
end_comment

begin_class
DECL|class|IndexShardSnapshotAndRestoreService
specifier|public
class|class
name|IndexShardSnapshotAndRestoreService
extends|extends
name|AbstractIndexShardComponent
block|{
DECL|field|indexShard
specifier|private
specifier|final
name|InternalIndexShard
name|indexShard
decl_stmt|;
DECL|field|repositoriesService
specifier|private
specifier|final
name|RepositoriesService
name|repositoriesService
decl_stmt|;
DECL|field|restoreService
specifier|private
specifier|final
name|RestoreService
name|restoreService
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndexShardSnapshotAndRestoreService
specifier|public
name|IndexShardSnapshotAndRestoreService
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|IndexShard
name|indexShard
parameter_list|,
name|RepositoriesService
name|repositoriesService
parameter_list|,
name|RestoreService
name|restoreService
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
name|indexShard
operator|=
operator|(
name|InternalIndexShard
operator|)
name|indexShard
expr_stmt|;
name|this
operator|.
name|repositoriesService
operator|=
name|repositoriesService
expr_stmt|;
name|this
operator|.
name|restoreService
operator|=
name|restoreService
expr_stmt|;
block|}
comment|/**      * Creates shard snapshot      *      * @param snapshotId     snapshot id      * @param snapshotStatus snapshot status      */
DECL|method|snapshot
specifier|public
name|void
name|snapshot
parameter_list|(
specifier|final
name|SnapshotId
name|snapshotId
parameter_list|,
specifier|final
name|IndexShardSnapshotStatus
name|snapshotStatus
parameter_list|)
block|{
name|IndexShardRepository
name|indexShardRepository
init|=
name|repositoriesService
operator|.
name|indexShardRepository
argument_list|(
name|snapshotId
operator|.
name|getRepository
argument_list|()
argument_list|)
decl_stmt|;
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
name|IndexShardSnapshotFailedException
argument_list|(
name|shardId
argument_list|,
literal|"snapshot should be performed only on primary"
argument_list|)
throw|;
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
throw|throw
operator|new
name|IndexShardSnapshotFailedException
argument_list|(
name|shardId
argument_list|,
literal|"cannot snapshot while relocating"
argument_list|)
throw|;
block|}
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CREATED
operator|||
name|indexShard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|RECOVERING
condition|)
block|{
comment|// shard has just been created, or still recovering
throw|throw
operator|new
name|IndexShardSnapshotFailedException
argument_list|(
name|shardId
argument_list|,
literal|"shard didn't fully recover yet"
argument_list|)
throw|;
block|}
try|try
block|{
name|SnapshotIndexCommit
name|snapshotIndexCommit
init|=
name|indexShard
operator|.
name|snapshotIndex
argument_list|()
decl_stmt|;
try|try
block|{
name|indexShardRepository
operator|.
name|snapshot
argument_list|(
name|snapshotId
argument_list|,
name|shardId
argument_list|,
name|snapshotIndexCommit
argument_list|,
name|snapshotStatus
argument_list|)
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
literal|"snapshot ("
argument_list|)
operator|.
name|append
argument_list|(
name|snapshotId
operator|.
name|getSnapshot
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|") completed to "
argument_list|)
operator|.
name|append
argument_list|(
name|indexShardRepository
argument_list|)
operator|.
name|append
argument_list|(
literal|", took ["
argument_list|)
operator|.
name|append
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|snapshotStatus
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
literal|"    index    : version ["
argument_list|)
operator|.
name|append
argument_list|(
name|snapshotStatus
operator|.
name|indexVersion
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"], number_of_files ["
argument_list|)
operator|.
name|append
argument_list|(
name|snapshotStatus
operator|.
name|numberOfFiles
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
name|snapshotStatus
operator|.
name|totalSize
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
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
block|}
finally|finally
block|{
name|snapshotIndexCommit
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SnapshotFailedEngineException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IndexShardSnapshotFailedException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IndexShardSnapshotFailedException
argument_list|(
name|shardId
argument_list|,
literal|"Failed to snapshot"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Restores shard from {@link RestoreSource} associated with this shard in routing table      *      * @param recoveryState recovery state      */
DECL|method|restore
specifier|public
name|void
name|restore
parameter_list|(
specifier|final
name|RecoveryState
name|recoveryState
parameter_list|)
block|{
name|RestoreSource
name|restoreSource
init|=
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|restoreSource
argument_list|()
decl_stmt|;
if|if
condition|(
name|restoreSource
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexShardRestoreFailedException
argument_list|(
name|shardId
argument_list|,
literal|"empty restore source"
argument_list|)
throw|;
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
literal|"[{}] restoring shard  [{}]"
argument_list|,
name|restoreSource
operator|.
name|snapshotId
argument_list|()
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|IndexShardRepository
name|indexShardRepository
init|=
name|repositoriesService
operator|.
name|indexShardRepository
argument_list|(
name|restoreSource
operator|.
name|snapshotId
argument_list|()
operator|.
name|getRepository
argument_list|()
argument_list|)
decl_stmt|;
name|ShardId
name|snapshotShardId
init|=
name|shardId
decl_stmt|;
if|if
condition|(
operator|!
name|shardId
operator|.
name|getIndex
argument_list|()
operator|.
name|equals
argument_list|(
name|restoreSource
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
name|snapshotShardId
operator|=
operator|new
name|ShardId
argument_list|(
name|restoreSource
operator|.
name|index
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indexShardRepository
operator|.
name|restore
argument_list|(
name|restoreSource
operator|.
name|snapshotId
argument_list|()
argument_list|,
name|shardId
argument_list|,
name|snapshotShardId
argument_list|,
name|recoveryState
argument_list|)
expr_stmt|;
name|restoreService
operator|.
name|indexShardRestoreCompleted
argument_list|(
name|restoreSource
operator|.
name|snapshotId
argument_list|()
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IndexShardRestoreFailedException
argument_list|(
name|shardId
argument_list|,
literal|"restore failed"
argument_list|,
name|t
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

