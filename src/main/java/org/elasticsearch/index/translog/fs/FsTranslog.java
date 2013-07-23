begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog.fs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
operator|.
name|fs
package|;
end_package

begin_import
import|import
name|jsr166y
operator|.
name|ThreadLocalRandom
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|FileSystemUtils
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
name|BytesStreamOutput
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
name|env
operator|.
name|NodeEnvironment
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
name|TranslogException
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|ClosedChannelException
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
name|locks
operator|.
name|ReadWriteLock
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
name|locks
operator|.
name|ReentrantReadWriteLock
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FsTranslog
specifier|public
class|class
name|FsTranslog
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|Translog
block|{
DECL|field|INDEX_TRANSLOG_FS_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_TRANSLOG_FS_TYPE
init|=
literal|"index.translog.fs.type"
decl_stmt|;
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
name|FsTranslogFile
operator|.
name|Type
name|type
init|=
name|FsTranslogFile
operator|.
name|Type
operator|.
name|fromString
argument_list|(
name|settings
operator|.
name|get
argument_list|(
name|INDEX_TRANSLOG_FS_TYPE
argument_list|,
name|FsTranslog
operator|.
name|this
operator|.
name|type
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|!=
name|FsTranslog
operator|.
name|this
operator|.
name|type
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating type from [{}] to [{}]"
argument_list|,
name|FsTranslog
operator|.
name|this
operator|.
name|type
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|FsTranslog
operator|.
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
block|}
block|}
DECL|field|indexSettingsService
specifier|private
specifier|final
name|IndexSettingsService
name|indexSettingsService
decl_stmt|;
DECL|field|rwl
specifier|private
specifier|final
name|ReadWriteLock
name|rwl
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
DECL|field|locations
specifier|private
specifier|final
name|File
index|[]
name|locations
decl_stmt|;
DECL|field|current
specifier|private
specifier|volatile
name|FsTranslogFile
name|current
decl_stmt|;
DECL|field|trans
specifier|private
specifier|volatile
name|FsTranslogFile
name|trans
decl_stmt|;
DECL|field|type
specifier|private
name|FsTranslogFile
operator|.
name|Type
name|type
decl_stmt|;
DECL|field|syncOnEachOperation
specifier|private
name|boolean
name|syncOnEachOperation
init|=
literal|false
decl_stmt|;
DECL|field|bufferSize
specifier|private
specifier|volatile
name|int
name|bufferSize
decl_stmt|;
DECL|field|transientBufferSize
specifier|private
specifier|volatile
name|int
name|transientBufferSize
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
DECL|method|FsTranslog
specifier|public
name|FsTranslog
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
name|NodeEnvironment
name|nodeEnv
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
name|indexSettingsService
operator|=
name|indexSettingsService
expr_stmt|;
name|File
index|[]
name|shardLocations
init|=
name|nodeEnv
operator|.
name|shardLocations
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|this
operator|.
name|locations
operator|=
operator|new
name|File
index|[
name|shardLocations
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|shardLocations
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|locations
index|[
name|i
index|]
operator|=
operator|new
name|File
argument_list|(
name|shardLocations
index|[
name|i
index|]
argument_list|,
literal|"translog"
argument_list|)
expr_stmt|;
name|FileSystemUtils
operator|.
name|mkdirs
argument_list|(
name|locations
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|type
operator|=
name|FsTranslogFile
operator|.
name|Type
operator|.
name|fromString
argument_list|(
name|componentSettings
operator|.
name|get
argument_list|(
literal|"type"
argument_list|,
name|FsTranslogFile
operator|.
name|Type
operator|.
name|BUFFERED
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|bufferSize
operator|=
operator|(
name|int
operator|)
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"buffer_size"
argument_list|,
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"64k"
argument_list|)
argument_list|)
operator|.
name|bytes
argument_list|()
expr_stmt|;
comment|// Not really interesting, updated by IndexingMemoryController...
name|this
operator|.
name|transientBufferSize
operator|=
operator|(
name|int
operator|)
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"transient_buffer_size"
argument_list|,
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"8k"
argument_list|)
argument_list|)
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|indexSettingsService
operator|.
name|addListener
argument_list|(
name|applySettings
argument_list|)
expr_stmt|;
block|}
DECL|method|FsTranslog
specifier|public
name|FsTranslog
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|File
name|location
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
name|indexSettingsService
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|locations
operator|=
operator|new
name|File
index|[]
block|{
name|location
block|}
expr_stmt|;
name|FileSystemUtils
operator|.
name|mkdirs
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|FsTranslogFile
operator|.
name|Type
operator|.
name|fromString
argument_list|(
name|componentSettings
operator|.
name|get
argument_list|(
literal|"type"
argument_list|,
name|FsTranslogFile
operator|.
name|Type
operator|.
name|BUFFERED
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|closeWithDelete
specifier|public
name|void
name|closeWithDelete
parameter_list|()
block|{
name|close
argument_list|(
literal|true
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
throws|throws
name|ElasticSearchException
block|{
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|updateBuffer
specifier|public
name|void
name|updateBuffer
parameter_list|(
name|ByteSizeValue
name|bufferSize
parameter_list|)
block|{
name|this
operator|.
name|bufferSize
operator|=
name|bufferSize
operator|.
name|bytesAsInt
argument_list|()
expr_stmt|;
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|FsTranslogFile
name|current1
init|=
name|this
operator|.
name|current
decl_stmt|;
if|if
condition|(
name|current1
operator|!=
literal|null
condition|)
block|{
name|current1
operator|.
name|updateBufferSize
argument_list|(
name|this
operator|.
name|bufferSize
argument_list|)
expr_stmt|;
block|}
name|current1
operator|=
name|this
operator|.
name|trans
expr_stmt|;
if|if
condition|(
name|current1
operator|!=
literal|null
condition|)
block|{
name|current1
operator|.
name|updateBufferSize
argument_list|(
name|this
operator|.
name|bufferSize
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|close
specifier|private
name|void
name|close
parameter_list|(
name|boolean
name|delete
parameter_list|)
block|{
if|if
condition|(
name|indexSettingsService
operator|!=
literal|null
condition|)
block|{
name|indexSettingsService
operator|.
name|removeListener
argument_list|(
name|applySettings
argument_list|)
expr_stmt|;
block|}
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|FsTranslogFile
name|current1
init|=
name|this
operator|.
name|current
decl_stmt|;
if|if
condition|(
name|current1
operator|!=
literal|null
condition|)
block|{
name|current1
operator|.
name|close
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
name|current1
operator|=
name|this
operator|.
name|trans
expr_stmt|;
if|if
condition|(
name|current1
operator|!=
literal|null
condition|)
block|{
name|current1
operator|.
name|close
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|locations
specifier|public
name|File
index|[]
name|locations
parameter_list|()
block|{
return|return
name|locations
return|;
block|}
annotation|@
name|Override
DECL|method|currentId
specifier|public
name|long
name|currentId
parameter_list|()
block|{
name|FsTranslogFile
name|current1
init|=
name|this
operator|.
name|current
decl_stmt|;
if|if
condition|(
name|current1
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|current1
operator|.
name|id
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|estimatedNumberOfOperations
specifier|public
name|int
name|estimatedNumberOfOperations
parameter_list|()
block|{
name|FsTranslogFile
name|current1
init|=
name|this
operator|.
name|current
decl_stmt|;
if|if
condition|(
name|current1
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|current1
operator|.
name|estimatedNumberOfOperations
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|memorySizeInBytes
specifier|public
name|long
name|memorySizeInBytes
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|translogSizeInBytes
specifier|public
name|long
name|translogSizeInBytes
parameter_list|()
block|{
name|FsTranslogFile
name|current1
init|=
name|this
operator|.
name|current
decl_stmt|;
if|if
condition|(
name|current1
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|current1
operator|.
name|translogSizeInBytes
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|clearUnreferenced
specifier|public
name|void
name|clearUnreferenced
parameter_list|()
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
for|for
control|(
name|File
name|location
range|:
name|locations
control|)
block|{
name|File
index|[]
name|files
init|=
name|location
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|files
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|File
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
name|file
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"translog-"
operator|+
name|current
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|trans
operator|!=
literal|null
operator|&&
name|file
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"translog-"
operator|+
name|trans
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
try|try
block|{
name|file
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
block|}
finally|finally
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|newTranslog
specifier|public
name|void
name|newTranslog
parameter_list|(
name|long
name|id
parameter_list|)
throws|throws
name|TranslogException
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|FsTranslogFile
name|newFile
decl_stmt|;
name|long
name|size
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|File
name|location
init|=
literal|null
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|locations
control|)
block|{
name|long
name|currentFree
init|=
name|file
operator|.
name|getFreeSpace
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentFree
operator|<
name|size
condition|)
block|{
name|size
operator|=
name|currentFree
expr_stmt|;
name|location
operator|=
name|file
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFree
operator|==
name|size
operator|&&
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|location
operator|=
name|file
expr_stmt|;
block|}
block|}
try|try
block|{
name|newFile
operator|=
name|type
operator|.
name|create
argument_list|(
name|shardId
argument_list|,
name|id
argument_list|,
operator|new
name|RafReference
argument_list|(
operator|new
name|File
argument_list|(
name|location
argument_list|,
literal|"translog-"
operator|+
name|id
argument_list|)
argument_list|)
argument_list|,
name|bufferSize
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|TranslogException
argument_list|(
name|shardId
argument_list|,
literal|"failed to create new translog file"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|FsTranslogFile
name|old
init|=
name|current
decl_stmt|;
name|current
operator|=
name|newFile
expr_stmt|;
if|if
condition|(
name|old
operator|!=
literal|null
condition|)
block|{
comment|// we might create a new translog overriding the current translog id
name|boolean
name|delete
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|old
operator|.
name|id
argument_list|()
operator|==
name|id
condition|)
block|{
name|delete
operator|=
literal|false
expr_stmt|;
block|}
name|old
operator|.
name|close
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|newTransientTranslog
specifier|public
name|void
name|newTransientTranslog
parameter_list|(
name|long
name|id
parameter_list|)
throws|throws
name|TranslogException
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
assert|assert
name|this
operator|.
name|trans
operator|==
literal|null
assert|;
name|long
name|size
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|File
name|location
init|=
literal|null
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|locations
control|)
block|{
name|long
name|currentFree
init|=
name|file
operator|.
name|getFreeSpace
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentFree
operator|<
name|size
condition|)
block|{
name|size
operator|=
name|currentFree
expr_stmt|;
name|location
operator|=
name|file
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFree
operator|==
name|size
operator|&&
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|location
operator|=
name|file
expr_stmt|;
block|}
block|}
name|this
operator|.
name|trans
operator|=
name|type
operator|.
name|create
argument_list|(
name|shardId
argument_list|,
name|id
argument_list|,
operator|new
name|RafReference
argument_list|(
operator|new
name|File
argument_list|(
name|location
argument_list|,
literal|"translog-"
operator|+
name|id
argument_list|)
argument_list|)
argument_list|,
name|transientBufferSize
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|TranslogException
argument_list|(
name|shardId
argument_list|,
literal|"failed to create new translog file"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|makeTransientCurrent
specifier|public
name|void
name|makeTransientCurrent
parameter_list|()
block|{
name|FsTranslogFile
name|old
decl_stmt|;
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
assert|assert
name|this
operator|.
name|trans
operator|!=
literal|null
assert|;
name|old
operator|=
name|current
expr_stmt|;
name|this
operator|.
name|current
operator|=
name|this
operator|.
name|trans
expr_stmt|;
name|this
operator|.
name|trans
operator|=
literal|null
expr_stmt|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
name|old
operator|.
name|close
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|current
operator|.
name|reuse
argument_list|(
name|old
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|revertTransient
specifier|public
name|void
name|revertTransient
parameter_list|()
block|{
name|FsTranslogFile
name|old
decl_stmt|;
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|old
operator|=
name|trans
expr_stmt|;
name|this
operator|.
name|trans
operator|=
literal|null
expr_stmt|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
name|old
operator|.
name|close
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|read
specifier|public
name|byte
index|[]
name|read
parameter_list|(
name|Location
name|location
parameter_list|)
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|FsTranslogFile
name|trans
init|=
name|this
operator|.
name|trans
decl_stmt|;
if|if
condition|(
name|trans
operator|!=
literal|null
operator|&&
name|trans
operator|.
name|id
argument_list|()
operator|==
name|location
operator|.
name|translogId
condition|)
block|{
try|try
block|{
return|return
name|trans
operator|.
name|read
argument_list|(
name|location
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
if|if
condition|(
name|current
operator|.
name|id
argument_list|()
operator|==
name|location
operator|.
name|translogId
condition|)
block|{
try|try
block|{
return|return
name|current
operator|.
name|read
argument_list|(
name|location
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|add
specifier|public
name|Location
name|add
parameter_list|(
name|Operation
name|operation
parameter_list|)
throws|throws
name|TranslogException
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// marker for the size...
name|TranslogStreams
operator|.
name|writeTranslogOperation
argument_list|(
name|out
argument_list|,
name|operation
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|out
operator|.
name|size
argument_list|()
decl_stmt|;
name|out
operator|.
name|seek
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|size
operator|-
literal|4
argument_list|)
expr_stmt|;
name|Location
name|location
init|=
name|current
operator|.
name|add
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|array
argument_list|()
argument_list|,
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|size
argument_list|)
decl_stmt|;
if|if
condition|(
name|syncOnEachOperation
condition|)
block|{
name|current
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
name|FsTranslogFile
name|trans
init|=
name|this
operator|.
name|trans
decl_stmt|;
if|if
condition|(
name|trans
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|location
operator|=
name|trans
operator|.
name|add
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|array
argument_list|()
argument_list|,
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClosedChannelException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
return|return
name|location
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
name|TranslogException
argument_list|(
name|shardId
argument_list|,
literal|"Failed to write operation ["
operator|+
name|operation
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|snapshot
specifier|public
name|FsChannelSnapshot
name|snapshot
parameter_list|()
throws|throws
name|TranslogException
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|FsChannelSnapshot
name|snapshot
init|=
name|current
operator|.
name|snapshot
argument_list|()
decl_stmt|;
if|if
condition|(
name|snapshot
operator|!=
literal|null
condition|)
block|{
return|return
name|snapshot
return|;
block|}
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|snapshot
specifier|public
name|Snapshot
name|snapshot
parameter_list|(
name|Snapshot
name|snapshot
parameter_list|)
block|{
name|FsChannelSnapshot
name|snap
init|=
name|snapshot
argument_list|()
decl_stmt|;
if|if
condition|(
name|snap
operator|.
name|translogId
argument_list|()
operator|==
name|snapshot
operator|.
name|translogId
argument_list|()
condition|)
block|{
name|snap
operator|.
name|seekForward
argument_list|(
name|snapshot
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|snap
return|;
block|}
annotation|@
name|Override
DECL|method|sync
specifier|public
name|void
name|sync
parameter_list|()
block|{
name|FsTranslogFile
name|current1
init|=
name|this
operator|.
name|current
decl_stmt|;
if|if
condition|(
name|current1
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|current1
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|syncNeeded
specifier|public
name|boolean
name|syncNeeded
parameter_list|()
block|{
name|FsTranslogFile
name|current1
init|=
name|this
operator|.
name|current
decl_stmt|;
return|return
name|current1
operator|!=
literal|null
operator|&&
name|current1
operator|.
name|syncNeeded
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|syncOnEachOperation
specifier|public
name|void
name|syncOnEachOperation
parameter_list|(
name|boolean
name|syncOnEachOperation
parameter_list|)
block|{
name|this
operator|.
name|syncOnEachOperation
operator|=
name|syncOnEachOperation
expr_stmt|;
if|if
condition|(
name|syncOnEachOperation
condition|)
block|{
name|type
operator|=
name|FsTranslogFile
operator|.
name|Type
operator|.
name|SIMPLE
expr_stmt|;
block|}
else|else
block|{
name|type
operator|=
name|FsTranslogFile
operator|.
name|Type
operator|.
name|BUFFERED
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

