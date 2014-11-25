begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Accountable
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
name|util
operator|.
name|IOUtils
import|;
end_import

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
name|common
operator|.
name|bytes
operator|.
name|BytesArray
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
name|bytes
operator|.
name|ReleasableBytesReference
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
name|BytesStreamInput
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
name|ReleasableBytesStreamOutput
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
name|util
operator|.
name|BigArrays
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
name|*
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
name|nio
operator|.
name|file
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
DECL|field|TRANSLOG_FILE_PREFIX
specifier|private
specifier|static
specifier|final
name|String
name|TRANSLOG_FILE_PREFIX
init|=
literal|"translog-"
decl_stmt|;
DECL|field|PARSE_ID_PATTERN
specifier|private
specifier|static
specifier|final
name|Pattern
name|PARSE_ID_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|TRANSLOG_FILE_PREFIX
operator|+
literal|"(\\d+).*"
argument_list|)
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
DECL|field|bigArrays
specifier|private
specifier|final
name|BigArrays
name|bigArrays
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
name|Path
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
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|)
throws|throws
name|IOException
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
name|this
operator|.
name|bigArrays
operator|=
name|bigArrays
expr_stmt|;
name|Path
index|[]
name|shardLocations
init|=
name|nodeEnv
operator|.
name|shardPaths
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|this
operator|.
name|locations
operator|=
operator|new
name|Path
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
name|shardLocations
index|[
name|i
index|]
operator|.
name|resolve
argument_list|(
literal|"translog"
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectories
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
name|Path
name|location
parameter_list|)
throws|throws
name|IOException
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
name|Path
index|[]
block|{
name|location
block|}
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|bigArrays
operator|=
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
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
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
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
name|IOUtils
operator|.
name|close
argument_list|(
name|this
operator|.
name|trans
argument_list|,
name|this
operator|.
name|current
argument_list|)
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
block|}
annotation|@
name|Override
DECL|method|locations
specifier|public
name|Path
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
DECL|method|ramBytesUsed
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|getChildResources
specifier|public
name|Iterable
argument_list|<
name|?
extends|extends
name|Accountable
argument_list|>
name|getChildResources
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
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
name|Path
name|location
range|:
name|locations
control|)
block|{
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|location
argument_list|,
name|TRANSLOG_FILE_PREFIX
operator|+
literal|"[0-9]*"
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|file
range|:
name|stream
control|)
block|{
if|if
condition|(
name|isReferencedTranslogFile
argument_list|(
name|file
argument_list|)
operator|==
literal|false
condition|)
block|{
try|try
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"delete unreferenced translog file: "
operator|+
name|file
argument_list|)
expr_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to delete "
operator|+
name|file
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to clear unreferenced files "
argument_list|,
name|ex
argument_list|)
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
throws|,
name|IOException
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
name|Path
name|location
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Path
name|file
range|:
name|locations
control|)
block|{
name|long
name|currentFree
init|=
name|Files
operator|.
name|getFileStore
argument_list|(
name|file
argument_list|)
operator|.
name|getUsableSpace
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
name|InternalChannelReference
argument_list|(
name|location
operator|.
name|resolve
argument_list|(
name|getPath
argument_list|(
name|id
argument_list|)
argument_list|)
argument_list|,
name|StandardOpenOption
operator|.
name|READ
argument_list|,
name|StandardOpenOption
operator|.
name|WRITE
argument_list|,
name|StandardOpenOption
operator|.
name|CREATE_NEW
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
name|IOUtils
operator|.
name|close
argument_list|(
name|old
argument_list|)
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
name|Path
name|location
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Path
name|file
range|:
name|locations
control|)
block|{
name|long
name|currentFree
init|=
name|Files
operator|.
name|getFileStore
argument_list|(
name|file
argument_list|)
operator|.
name|getUsableSpace
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
name|InternalChannelReference
argument_list|(
name|location
operator|.
name|resolve
argument_list|(
name|getPath
argument_list|(
name|id
argument_list|)
argument_list|)
argument_list|,
name|StandardOpenOption
operator|.
name|READ
argument_list|,
name|StandardOpenOption
operator|.
name|WRITE
argument_list|,
name|StandardOpenOption
operator|.
name|CREATE_NEW
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
throws|throws
name|IOException
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
argument_list|()
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
throws|throws
name|IOException
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
specifier|final
name|FsTranslogFile
name|toClose
init|=
name|this
operator|.
name|trans
decl_stmt|;
name|this
operator|.
name|trans
operator|=
literal|null
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|toClose
argument_list|)
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
block|}
comment|/**      * Returns the translog that should be read for the specified location. If      * the transient or current translog does not match, returns null      */
DECL|method|translogForLocation
specifier|private
name|FsTranslogFile
name|translogForLocation
parameter_list|(
name|Location
name|location
parameter_list|)
block|{
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
return|return
name|this
operator|.
name|trans
return|;
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
return|return
name|this
operator|.
name|current
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**      * Read the Operation object from the given location, returns null if the      * Operation could not be read.      */
annotation|@
name|Override
DECL|method|read
specifier|public
name|Translog
operator|.
name|Operation
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
name|translog
init|=
name|translogForLocation
argument_list|(
name|location
argument_list|)
decl_stmt|;
if|if
condition|(
name|translog
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
name|data
init|=
name|translog
operator|.
name|read
argument_list|(
name|location
argument_list|)
decl_stmt|;
try|try
init|(
name|BytesStreamInput
name|in
init|=
operator|new
name|BytesStreamInput
argument_list|(
name|data
argument_list|,
literal|false
argument_list|)
init|)
block|{
comment|// Return the Operation using the current version of the
comment|// stream based on which translog is being read
return|return
name|translog
operator|.
name|getStream
argument_list|()
operator|.
name|read
argument_list|(
name|in
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to read source from translog location "
operator|+
name|location
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
name|boolean
name|released
init|=
literal|false
decl_stmt|;
name|ReleasableBytesStreamOutput
name|out
init|=
literal|null
decl_stmt|;
try|try
block|{
name|out
operator|=
operator|new
name|ReleasableBytesStreamOutput
argument_list|(
name|bigArrays
argument_list|)
expr_stmt|;
name|TranslogStreams
operator|.
name|writeTranslogOperation
argument_list|(
name|out
argument_list|,
name|operation
argument_list|)
expr_stmt|;
name|ReleasableBytesReference
name|bytes
init|=
name|out
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|Location
name|location
init|=
name|current
operator|.
name|add
argument_list|(
name|bytes
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
assert|assert
operator|new
name|BytesArray
argument_list|(
name|current
operator|.
name|read
argument_list|(
name|location
argument_list|)
argument_list|)
operator|.
name|equals
argument_list|(
name|bytes
argument_list|)
assert|;
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
name|bytes
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
name|Releasables
operator|.
name|close
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|released
operator|=
literal|true
expr_stmt|;
return|return
name|location
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
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
if|if
condition|(
operator|!
name|released
operator|&&
name|out
operator|!=
literal|null
condition|)
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|seekTo
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
throws|throws
name|IOException
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
try|try
block|{
name|current1
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// if we switches translots (!=), then this failure is not relevant
comment|// we are working on a new translog
if|if
condition|(
name|this
operator|.
name|current
operator|==
name|current1
condition|)
block|{
throw|throw
name|e
throw|;
block|}
block|}
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
annotation|@
name|Override
DECL|method|getPath
specifier|public
name|Path
name|getPath
parameter_list|(
name|long
name|translogId
parameter_list|)
block|{
return|return
name|Paths
operator|.
name|get
argument_list|(
name|TRANSLOG_FILE_PREFIX
operator|+
name|translogId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|stats
specifier|public
name|TranslogStats
name|stats
parameter_list|()
block|{
return|return
operator|new
name|TranslogStats
argument_list|(
name|estimatedNumberOfOperations
argument_list|()
argument_list|,
name|translogSizeInBytes
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|findLargestPresentTranslogId
specifier|public
name|long
name|findLargestPresentTranslogId
parameter_list|()
throws|throws
name|IOException
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
name|long
name|maxId
init|=
name|this
operator|.
name|currentId
argument_list|()
decl_stmt|;
for|for
control|(
name|Path
name|location
range|:
name|locations
argument_list|()
control|)
block|{
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|location
argument_list|,
name|TRANSLOG_FILE_PREFIX
operator|+
literal|"[0-9]*"
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|translogFile
range|:
name|stream
control|)
block|{
try|try
block|{
specifier|final
name|String
name|fileName
init|=
name|translogFile
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|final
name|Matcher
name|matcher
init|=
name|PARSE_ID_PATTERN
operator|.
name|matcher
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|matches
argument_list|()
condition|)
block|{
name|maxId
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxId
argument_list|,
name|Long
operator|.
name|parseLong
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Couldn't parse translog id from file "
operator|+
name|translogFile
operator|+
literal|" skipping"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|maxId
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
DECL|method|isReferencedTranslogFile
specifier|private
name|boolean
name|isReferencedTranslogFile
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
specifier|final
name|FsTranslogFile
name|theCurrent
init|=
name|this
operator|.
name|current
decl_stmt|;
specifier|final
name|FsTranslogFile
name|theTrans
init|=
name|this
operator|.
name|trans
decl_stmt|;
return|return
operator|(
name|theCurrent
operator|!=
literal|null
operator|&&
name|theCurrent
operator|.
name|getPath
argument_list|()
operator|.
name|equals
argument_list|(
name|file
argument_list|)
operator|)
operator|||
operator|(
name|theTrans
operator|!=
literal|null
operator|&&
name|theTrans
operator|.
name|getPath
argument_list|()
operator|.
name|equals
argument_list|(
name|file
argument_list|)
operator|)
return|;
block|}
DECL|class|InternalChannelReference
specifier|private
specifier|final
class|class
name|InternalChannelReference
extends|extends
name|ChannelReference
block|{
DECL|method|InternalChannelReference
specifier|public
name|InternalChannelReference
parameter_list|(
name|Path
name|file
parameter_list|,
name|OpenOption
modifier|...
name|openOptions
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|file
argument_list|,
name|openOptions
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|closeInternal
specifier|protected
name|void
name|closeInternal
parameter_list|()
block|{
name|super
operator|.
name|closeInternal
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
if|if
condition|(
name|isReferencedTranslogFile
argument_list|(
name|file
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// if the given path is not the current we can safely delete the file since all references are released
name|logger
operator|.
name|trace
argument_list|(
literal|"delete translog file - not referenced and not current anymore {}"
argument_list|,
name|file
argument_list|()
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|deleteFilesIgnoringExceptions
argument_list|(
name|file
argument_list|()
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
block|}
block|}
end_class

end_unit

