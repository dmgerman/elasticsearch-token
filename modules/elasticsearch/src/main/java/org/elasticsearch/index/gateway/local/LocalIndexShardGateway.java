begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.gateway.local
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|gateway
operator|.
name|local
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
name|index
operator|.
name|IndexReader
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
name|InputStreamStreamInput
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
name|gateway
operator|.
name|none
operator|.
name|NoneGateway
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
name|gateway
operator|.
name|IndexShardGateway
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
name|gateway
operator|.
name|IndexShardGatewayRecoveryException
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
name|gateway
operator|.
name|RecoveryStatus
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
name|gateway
operator|.
name|SnapshotStatus
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
name|index
operator|.
name|translog
operator|.
name|TranslogStreams
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
name|fs
operator|.
name|FsTranslog
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|LocalIndexShardGateway
specifier|public
class|class
name|LocalIndexShardGateway
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|IndexShardGateway
block|{
DECL|field|indexShard
specifier|private
specifier|final
name|InternalIndexShard
name|indexShard
decl_stmt|;
DECL|field|recoveryStatus
specifier|private
specifier|final
name|RecoveryStatus
name|recoveryStatus
init|=
operator|new
name|RecoveryStatus
argument_list|()
decl_stmt|;
DECL|method|LocalIndexShardGateway
annotation|@
name|Inject
specifier|public
name|LocalIndexShardGateway
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
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"local"
return|;
block|}
DECL|method|recoveryStatus
annotation|@
name|Override
specifier|public
name|RecoveryStatus
name|recoveryStatus
parameter_list|()
block|{
return|return
name|recoveryStatus
return|;
block|}
DECL|method|recover
annotation|@
name|Override
specifier|public
name|void
name|recover
parameter_list|(
name|RecoveryStatus
name|recoveryStatus
parameter_list|)
throws|throws
name|IndexShardGatewayRecoveryException
block|{
name|recoveryStatus
argument_list|()
operator|.
name|index
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
comment|// read the gateway data persisted
name|long
name|version
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
if|if
condition|(
name|IndexReader
operator|.
name|indexExists
argument_list|(
name|indexShard
operator|.
name|store
argument_list|()
operator|.
name|directory
argument_list|()
argument_list|)
condition|)
block|{
name|version
operator|=
name|IndexReader
operator|.
name|getCurrentVersion
argument_list|(
name|indexShard
operator|.
name|store
argument_list|()
operator|.
name|directory
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
throw|throw
operator|new
name|IndexShardGatewayRecoveryException
argument_list|(
name|shardId
argument_list|()
argument_list|,
literal|"Failed to fetch index version after copying it over"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|recoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|updateVersion
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|recoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|time
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|recoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
name|recoveryStatus
operator|.
name|translog
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
if|if
condition|(
name|version
operator|==
operator|-
literal|1
condition|)
block|{
comment|// no translog files, bail
name|indexShard
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// no index, just start the shard and bail
name|recoveryStatus
operator|.
name|translog
argument_list|()
operator|.
name|time
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|recoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// move an existing translog, if exists, to "recovering" state, and start reading from it
name|FsTranslog
name|translog
init|=
operator|(
name|FsTranslog
operator|)
name|indexShard
operator|.
name|translog
argument_list|()
decl_stmt|;
name|File
name|recoveringTranslogFile
init|=
operator|new
name|File
argument_list|(
name|translog
operator|.
name|location
argument_list|()
argument_list|,
literal|"translog-"
operator|+
name|version
operator|+
literal|".recovering"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|recoveringTranslogFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|File
name|translogFile
init|=
operator|new
name|File
argument_list|(
name|translog
operator|.
name|location
argument_list|()
argument_list|,
literal|"translog-"
operator|+
name|version
argument_list|)
decl_stmt|;
if|if
condition|(
name|translogFile
operator|.
name|exists
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|translogFile
operator|.
name|renameTo
argument_list|(
name|recoveringTranslogFile
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
if|if
condition|(
operator|!
name|recoveringTranslogFile
operator|.
name|exists
argument_list|()
condition|)
block|{
comment|// no translog to recovery from, start and bail
comment|// no translog files, bail
name|indexShard
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// no index, just start the shard and bail
name|recoveryStatus
operator|.
name|translog
argument_list|()
operator|.
name|time
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|recoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// recover from the translog file
name|indexShard
operator|.
name|performRecoveryPrepareForTranslog
argument_list|()
expr_stmt|;
try|try
block|{
name|InputStreamStreamInput
name|si
init|=
operator|new
name|InputStreamStreamInput
argument_list|(
operator|new
name|FileInputStream
argument_list|(
name|recoveringTranslogFile
argument_list|)
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|opSize
init|=
name|si
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|Translog
operator|.
name|Operation
name|operation
init|=
name|TranslogStreams
operator|.
name|readTranslogOperation
argument_list|(
name|si
argument_list|)
decl_stmt|;
name|recoveryStatus
operator|.
name|translog
argument_list|()
operator|.
name|addTranslogOperations
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|performRecoveryOperation
argument_list|(
name|operation
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|EOFException
name|e
parameter_list|)
block|{
comment|// ignore this exception, its fine
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore this as well
block|}
name|indexShard
operator|.
name|performRecoveryFinalization
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|recoveringTranslogFile
operator|.
name|delete
argument_list|()
expr_stmt|;
name|recoveryStatus
operator|.
name|translog
argument_list|()
operator|.
name|time
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|recoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|type
annotation|@
name|Override
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|NoneGateway
operator|.
name|TYPE
return|;
block|}
DECL|method|snapshot
annotation|@
name|Override
specifier|public
name|SnapshotStatus
name|snapshot
parameter_list|(
name|Snapshot
name|snapshot
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
DECL|method|lastSnapshotStatus
annotation|@
name|Override
specifier|public
name|SnapshotStatus
name|lastSnapshotStatus
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|method|currentSnapshotStatus
annotation|@
name|Override
specifier|public
name|SnapshotStatus
name|currentSnapshotStatus
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|method|requiresSnapshotScheduling
annotation|@
name|Override
specifier|public
name|boolean
name|requiresSnapshotScheduling
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|delete
parameter_list|)
block|{     }
block|}
end_class

end_unit

