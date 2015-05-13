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
name|ElasticsearchException
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
name|ClusterService
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
name|action
operator|.
name|index
operator|.
name|MappingUpdatedAction
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
name|lease
operator|.
name|Releasables
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
name|transport
operator|.
name|TransportService
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

begin_comment
comment|/**  * A recovery handler that skips phase 1 as well as sending the snapshot. During phase 3 the shard is marked  * as relocated an closed to ensure that the engine is closed and the target can acquire the IW write lock.  */
end_comment

begin_class
DECL|class|SharedFSRecoverySourceHandler
specifier|public
class|class
name|SharedFSRecoverySourceHandler
extends|extends
name|RecoverySourceHandler
block|{
DECL|field|shard
specifier|private
specifier|final
name|IndexShard
name|shard
decl_stmt|;
DECL|field|request
specifier|private
specifier|final
name|StartRecoveryRequest
name|request
decl_stmt|;
DECL|field|EMPTY_VIEW
specifier|private
specifier|static
specifier|final
name|Translog
operator|.
name|View
name|EMPTY_VIEW
init|=
operator|new
name|EmptyView
argument_list|()
decl_stmt|;
DECL|method|SharedFSRecoverySourceHandler
specifier|public
name|SharedFSRecoverySourceHandler
parameter_list|(
name|IndexShard
name|shard
parameter_list|,
name|StartRecoveryRequest
name|request
parameter_list|,
name|RecoverySettings
name|recoverySettings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|MappingUpdatedAction
name|mappingUpdatedAction
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
name|super
argument_list|(
name|shard
argument_list|,
name|request
argument_list|,
name|recoverySettings
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|indicesService
argument_list|,
name|mappingUpdatedAction
argument_list|,
name|logger
argument_list|)
expr_stmt|;
name|this
operator|.
name|shard
operator|=
name|shard
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|recoverToTarget
specifier|public
name|RecoveryResponse
name|recoverToTarget
parameter_list|()
block|{
name|boolean
name|engineClosed
init|=
literal|false
decl_stmt|;
try|try
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} recovery [phase1] to {}: skipping phase 1 for shared filesystem"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isPrimaryRelocation
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[phase1] closing engine on primary for shared filesystem recovery"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// if we relocate we need to close the engine in order to open a new
comment|// IndexWriter on the other end of the relocation
name|engineClosed
operator|=
literal|true
expr_stmt|;
name|shard
operator|.
name|engine
argument_list|()
operator|.
name|flushAndClose
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
name|warn
argument_list|(
literal|"close engine failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|shard
operator|.
name|failShard
argument_list|(
literal|"failed to close engine (phase1)"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|prepareTargetForTranslog
argument_list|(
name|EMPTY_VIEW
argument_list|)
expr_stmt|;
name|finalizeRecovery
argument_list|()
expr_stmt|;
return|return
name|response
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|engineClosed
condition|)
block|{
comment|// If the relocation fails then the primary is closed and can't be
comment|// used anymore... (because it's closed) that's a problem, so in
comment|// that case, fail the shard to reallocate a new IndexShard and
comment|// create a new IndexWriter
name|logger
operator|.
name|info
argument_list|(
literal|"recovery failed for primary shadow shard, failing shard"
argument_list|)
expr_stmt|;
name|shard
operator|.
name|failShard
argument_list|(
literal|"primary relocation failed on shared filesystem"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"recovery failed on shared filesystem"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
throw|throw
name|t
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|sendSnapshot
specifier|protected
name|int
name|sendSnapshot
parameter_list|(
name|Translog
operator|.
name|Snapshot
name|snapshot
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} skipping recovery of translog snapshot on shared filesystem to: {}"
argument_list|,
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
DECL|method|isPrimaryRelocation
specifier|private
name|boolean
name|isPrimaryRelocation
parameter_list|()
block|{
return|return
name|request
operator|.
name|recoveryType
argument_list|()
operator|==
name|RecoveryState
operator|.
name|Type
operator|.
name|RELOCATION
operator|&&
name|shard
operator|.
name|routingEntry
argument_list|()
operator|.
name|primary
argument_list|()
return|;
block|}
comment|/**      * An empty view since we don't recover from translog even in the shared FS case      */
DECL|class|EmptyView
specifier|private
specifier|static
class|class
name|EmptyView
implements|implements
name|Translog
operator|.
name|View
block|{
annotation|@
name|Override
DECL|method|totalOperations
specifier|public
name|int
name|totalOperations
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|sizeInBytes
specifier|public
name|long
name|sizeInBytes
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|snapshot
specifier|public
name|Translog
operator|.
name|Snapshot
name|snapshot
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|minTranslogGeneration
specifier|public
name|long
name|minTranslogGeneration
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{         }
block|}
block|}
end_class

end_unit

