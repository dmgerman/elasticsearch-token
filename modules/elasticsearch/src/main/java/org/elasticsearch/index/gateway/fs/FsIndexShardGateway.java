begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.gateway.fs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|gateway
operator|.
name|fs
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
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|IndexInput
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
name|IndexShardGatewaySnapshotFailedException
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
name|SizeUnit
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
name|SizeValue
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
name|guice
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
name|util
operator|.
name|io
operator|.
name|stream
operator|.
name|DataInputStreamInput
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
name|io
operator|.
name|stream
operator|.
name|DataOutputStreamOutput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilenameFilter
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
name|io
operator|.
name|RandomAccessFile
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
name|concurrent
operator|.
name|CountDownLatch
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
name|AtomicReference
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
operator|.
name|TranslogStreams
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
operator|.
name|FileSystemUtils
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|Directories
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|FsIndexShardGateway
specifier|public
class|class
name|FsIndexShardGateway
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
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|store
specifier|private
specifier|final
name|Store
name|store
decl_stmt|;
DECL|field|location
specifier|private
specifier|final
name|File
name|location
decl_stmt|;
DECL|field|locationIndex
specifier|private
specifier|final
name|File
name|locationIndex
decl_stmt|;
DECL|field|locationTranslog
specifier|private
specifier|final
name|File
name|locationTranslog
decl_stmt|;
DECL|method|FsIndexShardGateway
annotation|@
name|Inject
specifier|public
name|FsIndexShardGateway
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
name|FsIndexGateway
name|fsIndexGateway
parameter_list|,
name|IndexShard
name|indexShard
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
name|store
operator|=
name|store
expr_stmt|;
name|this
operator|.
name|location
operator|=
operator|new
name|File
argument_list|(
name|fsIndexGateway
operator|.
name|indexGatewayHome
argument_list|()
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|locationIndex
operator|=
operator|new
name|File
argument_list|(
name|location
argument_list|,
literal|"index"
argument_list|)
expr_stmt|;
name|this
operator|.
name|locationTranslog
operator|=
operator|new
name|File
argument_list|(
name|location
argument_list|,
literal|"translog"
argument_list|)
expr_stmt|;
name|locationIndex
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|locationTranslog
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
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
literal|true
return|;
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
literal|"fs["
operator|+
name|location
operator|+
literal|"]"
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
block|{
if|if
condition|(
name|delete
condition|)
block|{
name|deleteRecursively
argument_list|(
name|location
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|recover
annotation|@
name|Override
specifier|public
name|RecoveryStatus
name|recover
parameter_list|()
throws|throws
name|IndexShardGatewayRecoveryException
block|{
name|RecoveryStatus
operator|.
name|Index
name|recoveryStatusIndex
init|=
name|recoverIndex
argument_list|()
decl_stmt|;
name|RecoveryStatus
operator|.
name|Translog
name|recoveryStatusTranslog
init|=
name|recoverTranslog
argument_list|()
decl_stmt|;
return|return
operator|new
name|RecoveryStatus
argument_list|(
name|recoveryStatusIndex
argument_list|,
name|recoveryStatusTranslog
argument_list|)
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
name|long
name|totalTimeStart
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|boolean
name|indexDirty
init|=
literal|false
decl_stmt|;
name|boolean
name|translogDirty
init|=
literal|false
decl_stmt|;
specifier|final
name|SnapshotIndexCommit
name|snapshotIndexCommit
init|=
name|snapshot
operator|.
name|indexCommit
argument_list|()
decl_stmt|;
specifier|final
name|Translog
operator|.
name|Snapshot
name|translogSnapshot
init|=
name|snapshot
operator|.
name|translogSnapshot
argument_list|()
decl_stmt|;
name|int
name|indexNumberOfFiles
init|=
literal|0
decl_stmt|;
name|long
name|indexTotalFilesSize
init|=
literal|0
decl_stmt|;
name|long
name|indexTime
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|snapshot
operator|.
name|indexChanged
argument_list|()
condition|)
block|{
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|indexDirty
operator|=
literal|true
expr_stmt|;
comment|// snapshot into the index
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|snapshotIndexCommit
operator|.
name|getFiles
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|lastException
init|=
operator|new
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|fileName
range|:
name|snapshotIndexCommit
operator|.
name|getFiles
argument_list|()
control|)
block|{
comment|// don't copy over the segments file, it will be copied over later on as part of the
comment|// final snapshot phase
if|if
condition|(
name|fileName
operator|.
name|equals
argument_list|(
name|snapshotIndexCommit
operator|.
name|getSegmentsFileName
argument_list|()
argument_list|)
condition|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
continue|continue;
block|}
name|IndexInput
name|indexInput
init|=
literal|null
decl_stmt|;
try|try
block|{
name|indexInput
operator|=
name|snapshotIndexCommit
operator|.
name|getDirectory
argument_list|()
operator|.
name|openInput
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
name|File
name|snapshotFile
init|=
operator|new
name|File
argument_list|(
name|locationIndex
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshotFile
operator|.
name|exists
argument_list|()
operator|&&
operator|(
name|snapshotFile
operator|.
name|length
argument_list|()
operator|==
name|indexInput
operator|.
name|length
argument_list|()
operator|)
condition|)
block|{
comment|// we assume its the same one, no need to copy
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
continue|continue;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Failed to verify file equality based on length, copying..."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|indexInput
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|indexInput
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
name|indexNumberOfFiles
operator|++
expr_stmt|;
try|try
block|{
name|indexTotalFilesSize
operator|+=
name|snapshotIndexCommit
operator|.
name|getDirectory
argument_list|()
operator|.
name|fileLength
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore...
block|}
name|threadPool
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
name|copyFromDirectory
argument_list|(
name|snapshotIndexCommit
operator|.
name|getDirectory
argument_list|()
argument_list|,
name|fileName
argument_list|,
operator|new
name|File
argument_list|(
name|locationIndex
argument_list|,
name|fileName
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|lastException
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|lastException
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lastException
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexShardGatewaySnapshotFailedException
argument_list|(
name|shardId
argument_list|()
argument_list|,
literal|"Failed to perform snapshot (index files)"
argument_list|,
name|lastException
operator|.
name|get
argument_list|()
argument_list|)
throw|;
block|}
name|indexTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|time
expr_stmt|;
block|}
comment|// we reopen the RAF each snapshot and not keep an open one since we want to make sure we
comment|// can sync it to disk later on (close it as well)
name|File
name|translogFile
init|=
operator|new
name|File
argument_list|(
name|locationTranslog
argument_list|,
literal|"translog-"
operator|+
name|translogSnapshot
operator|.
name|translogId
argument_list|()
argument_list|)
decl_stmt|;
name|RandomAccessFile
name|translogRaf
init|=
literal|null
decl_stmt|;
comment|// if we have a different trnaslogId we want to flush the full translog to a new file (based on the translogId).
comment|// If we still work on existing translog, just append the latest translog operations
name|int
name|translogNumberOfOperations
init|=
literal|0
decl_stmt|;
name|long
name|translogTime
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|snapshot
operator|.
name|newTranslogCreated
argument_list|()
condition|)
block|{
name|translogDirty
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|translogRaf
operator|=
operator|new
name|RandomAccessFile
argument_list|(
name|translogFile
argument_list|,
literal|"rw"
argument_list|)
expr_stmt|;
name|StreamOutput
name|out
init|=
operator|new
name|DataOutputStreamOutput
argument_list|(
name|translogRaf
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// write the number of operations header with -1 currently
for|for
control|(
name|Translog
operator|.
name|Operation
name|operation
range|:
name|translogSnapshot
control|)
block|{
name|translogNumberOfOperations
operator|++
expr_stmt|;
name|writeTranslogOperation
argument_list|(
name|out
argument_list|,
name|operation
argument_list|)
expr_stmt|;
block|}
name|translogTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|time
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|translogRaf
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
throw|throw
operator|new
name|IndexShardGatewaySnapshotFailedException
argument_list|(
name|shardId
argument_list|()
argument_list|,
literal|"Failed to snapshot translog into ["
operator|+
name|translogFile
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|snapshot
operator|.
name|sameTranslogNewOperations
argument_list|()
condition|)
block|{
name|translogDirty
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|translogRaf
operator|=
operator|new
name|RandomAccessFile
argument_list|(
name|translogFile
argument_list|,
literal|"rw"
argument_list|)
expr_stmt|;
comment|// seek to the end, since we append
name|translogRaf
operator|.
name|seek
argument_list|(
name|translogRaf
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|StreamOutput
name|out
init|=
operator|new
name|DataOutputStreamOutput
argument_list|(
name|translogRaf
argument_list|)
decl_stmt|;
for|for
control|(
name|Translog
operator|.
name|Operation
name|operation
range|:
name|translogSnapshot
operator|.
name|skipTo
argument_list|(
name|snapshot
operator|.
name|lastTranslogSize
argument_list|()
argument_list|)
control|)
block|{
name|translogNumberOfOperations
operator|++
expr_stmt|;
name|writeTranslogOperation
argument_list|(
name|out
argument_list|,
name|operation
argument_list|)
expr_stmt|;
block|}
name|translogTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|time
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|translogRaf
operator|!=
literal|null
condition|)
block|{
name|translogRaf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
throw|throw
operator|new
name|IndexShardGatewaySnapshotFailedException
argument_list|(
name|shardId
argument_list|()
argument_list|,
literal|"Failed to append snapshot translog into ["
operator|+
name|translogFile
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// now write the segments file and update the translog header
try|try
block|{
if|if
condition|(
name|indexDirty
condition|)
block|{
name|indexNumberOfFiles
operator|++
expr_stmt|;
name|indexTotalFilesSize
operator|+=
name|snapshotIndexCommit
operator|.
name|getDirectory
argument_list|()
operator|.
name|fileLength
argument_list|(
name|snapshotIndexCommit
operator|.
name|getSegmentsFileName
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|copyFromDirectory
argument_list|(
name|snapshotIndexCommit
operator|.
name|getDirectory
argument_list|()
argument_list|,
name|snapshotIndexCommit
operator|.
name|getSegmentsFileName
argument_list|()
argument_list|,
operator|new
name|File
argument_list|(
name|locationIndex
argument_list|,
name|snapshotIndexCommit
operator|.
name|getSegmentsFileName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|indexTime
operator|+=
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|time
operator|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|translogRaf
operator|!=
literal|null
condition|)
block|{
name|translogRaf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
throw|throw
operator|new
name|IndexShardGatewaySnapshotFailedException
argument_list|(
name|shardId
argument_list|()
argument_list|,
literal|"Failed to finalize index snapshot into ["
operator|+
operator|new
name|File
argument_list|(
name|locationIndex
argument_list|,
name|snapshotIndexCommit
operator|.
name|getSegmentsFileName
argument_list|()
argument_list|)
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
try|try
block|{
if|if
condition|(
name|translogDirty
condition|)
block|{
name|translogRaf
operator|.
name|seek
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|translogRaf
operator|.
name|writeInt
argument_list|(
name|translogSnapshot
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|translogRaf
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// now, sync the translog
name|syncFile
argument_list|(
name|translogFile
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|translogRaf
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|translogRaf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
block|}
throw|throw
operator|new
name|IndexShardGatewaySnapshotFailedException
argument_list|(
name|shardId
argument_list|()
argument_list|,
literal|"Failed to finalize snapshot into ["
operator|+
name|translogFile
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// delete the old translog
if|if
condition|(
name|snapshot
operator|.
name|newTranslogCreated
argument_list|()
condition|)
block|{
operator|new
name|File
argument_list|(
name|locationTranslog
argument_list|,
literal|"translog-"
operator|+
name|snapshot
operator|.
name|lastTranslogId
argument_list|()
argument_list|)
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
comment|// delete files that no longer exists in the index
if|if
condition|(
name|indexDirty
condition|)
block|{
name|File
index|[]
name|existingFiles
init|=
name|locationIndex
operator|.
name|listFiles
argument_list|()
decl_stmt|;
for|for
control|(
name|File
name|existingFile
range|:
name|existingFiles
control|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|fileName
range|:
name|snapshotIndexCommit
operator|.
name|getFiles
argument_list|()
control|)
block|{
if|if
condition|(
name|existingFile
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|fileName
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
name|existingFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|SnapshotStatus
argument_list|(
operator|new
name|TimeValue
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|totalTimeStart
argument_list|)
argument_list|,
operator|new
name|SnapshotStatus
operator|.
name|Index
argument_list|(
name|indexNumberOfFiles
argument_list|,
operator|new
name|SizeValue
argument_list|(
name|indexTotalFilesSize
argument_list|)
argument_list|,
operator|new
name|TimeValue
argument_list|(
name|indexTime
argument_list|)
argument_list|)
argument_list|,
operator|new
name|SnapshotStatus
operator|.
name|Translog
argument_list|(
name|translogNumberOfOperations
argument_list|,
operator|new
name|TimeValue
argument_list|(
name|translogTime
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
DECL|method|recoverIndex
specifier|private
name|RecoveryStatus
operator|.
name|Index
name|recoverIndex
parameter_list|()
throws|throws
name|IndexShardGatewayRecoveryException
block|{
name|File
index|[]
name|files
init|=
name|locationIndex
operator|.
name|listFiles
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|files
operator|.
name|length
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|lastException
init|=
operator|new
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|File
name|file
range|:
name|files
control|)
block|{
name|threadPool
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
name|copyToDirectory
argument_list|(
name|file
argument_list|,
name|store
operator|.
name|directory
argument_list|()
argument_list|,
name|file
operator|.
name|getName
argument_list|()
argument_list|)
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
name|debug
argument_list|(
literal|"Failed to read ["
operator|+
name|file
operator|+
literal|"] into ["
operator|+
name|store
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|lastException
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|lastException
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lastException
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexShardGatewayRecoveryException
argument_list|(
name|shardId
argument_list|()
argument_list|,
literal|"Failed to recover index files"
argument_list|,
name|lastException
operator|.
name|get
argument_list|()
argument_list|)
throw|;
block|}
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|files
control|)
block|{
name|totalSize
operator|+=
name|file
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
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
name|store
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
name|store
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
return|return
operator|new
name|RecoveryStatus
operator|.
name|Index
argument_list|(
name|version
argument_list|,
name|files
operator|.
name|length
argument_list|,
operator|new
name|SizeValue
argument_list|(
name|totalSize
argument_list|,
name|SizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|)
return|;
block|}
DECL|method|recoverTranslog
specifier|private
name|RecoveryStatus
operator|.
name|Translog
name|recoverTranslog
parameter_list|()
throws|throws
name|IndexShardGatewayRecoveryException
block|{
name|RandomAccessFile
name|raf
init|=
literal|null
decl_stmt|;
try|try
block|{
name|long
name|recoveryTranslogId
init|=
name|findLatestTranslogId
argument_list|(
name|locationTranslog
argument_list|)
decl_stmt|;
if|if
condition|(
name|recoveryTranslogId
operator|==
operator|-
literal|1
condition|)
block|{
comment|// no recovery file found, start the shard and bail
name|indexShard
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
operator|new
name|RecoveryStatus
operator|.
name|Translog
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
operator|new
name|SizeValue
argument_list|(
literal|0
argument_list|,
name|SizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|)
return|;
block|}
name|File
name|recoveryTranslogFile
init|=
operator|new
name|File
argument_list|(
name|locationTranslog
argument_list|,
literal|"translog-"
operator|+
name|recoveryTranslogId
argument_list|)
decl_stmt|;
name|raf
operator|=
operator|new
name|RandomAccessFile
argument_list|(
name|recoveryTranslogFile
argument_list|,
literal|"r"
argument_list|)
expr_stmt|;
name|int
name|numberOfOperations
init|=
name|raf
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|Translog
operator|.
name|Operation
argument_list|>
name|operations
init|=
name|newArrayListWithExpectedSize
argument_list|(
name|numberOfOperations
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numberOfOperations
condition|;
name|i
operator|++
control|)
block|{
name|operations
operator|.
name|add
argument_list|(
name|readTranslogOperation
argument_list|(
operator|new
name|DataInputStreamInput
argument_list|(
name|raf
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexShard
operator|.
name|performRecovery
argument_list|(
name|operations
argument_list|)
expr_stmt|;
return|return
operator|new
name|RecoveryStatus
operator|.
name|Translog
argument_list|(
name|recoveryTranslogId
argument_list|,
name|operations
operator|.
name|size
argument_list|()
argument_list|,
operator|new
name|SizeValue
argument_list|(
name|recoveryTranslogFile
operator|.
name|length
argument_list|()
argument_list|,
name|SizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
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
literal|"Failed to perform recovery of translog"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|raf
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|raf
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
DECL|method|findLatestTranslogId
specifier|private
name|long
name|findLatestTranslogId
parameter_list|(
name|File
name|location
parameter_list|)
block|{
name|File
index|[]
name|files
init|=
name|location
operator|.
name|listFiles
argument_list|(
operator|new
name|FilenameFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|File
name|dir
parameter_list|,
name|String
name|name
parameter_list|)
block|{
return|return
name|name
operator|.
name|startsWith
argument_list|(
literal|"translog-"
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|long
name|index
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|files
control|)
block|{
name|String
name|name
init|=
name|file
operator|.
name|getName
argument_list|()
decl_stmt|;
name|RandomAccessFile
name|raf
init|=
literal|null
decl_stmt|;
try|try
block|{
name|raf
operator|=
operator|new
name|RandomAccessFile
argument_list|(
name|file
argument_list|,
literal|"r"
argument_list|)
expr_stmt|;
comment|// if header is -1, then its not properly written, ignore it
if|if
condition|(
name|raf
operator|.
name|readInt
argument_list|()
operator|==
operator|-
literal|1
condition|)
block|{
continue|continue;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// broken file, continue
continue|continue;
block|}
finally|finally
block|{
try|try
block|{
name|raf
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
name|long
name|fileIndex
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|name
operator|.
name|substring
argument_list|(
name|name
operator|.
name|indexOf
argument_list|(
literal|'-'
argument_list|)
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileIndex
operator|>=
name|index
condition|)
block|{
name|index
operator|=
name|fileIndex
expr_stmt|;
block|}
block|}
return|return
name|index
return|;
block|}
block|}
end_class

end_unit

