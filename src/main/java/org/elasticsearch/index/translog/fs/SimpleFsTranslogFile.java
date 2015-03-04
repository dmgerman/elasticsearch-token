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
name|IOUtils
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
name|BytesReference
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
name|Channels
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
name|TranslogStream
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
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|file
operator|.
name|Files
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
name|Path
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

begin_class
DECL|class|SimpleFsTranslogFile
specifier|public
class|class
name|SimpleFsTranslogFile
implements|implements
name|FsTranslogFile
block|{
DECL|field|id
specifier|private
specifier|final
name|long
name|id
decl_stmt|;
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|channelReference
specifier|private
specifier|final
name|ChannelReference
name|channelReference
decl_stmt|;
DECL|field|closed
specifier|private
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|()
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
DECL|field|translogStream
specifier|private
specifier|final
name|TranslogStream
name|translogStream
decl_stmt|;
DECL|field|headerSize
specifier|private
specifier|final
name|int
name|headerSize
decl_stmt|;
DECL|field|operationCounter
specifier|private
specifier|volatile
name|int
name|operationCounter
init|=
literal|0
decl_stmt|;
DECL|field|lastPosition
specifier|private
specifier|volatile
name|long
name|lastPosition
init|=
literal|0
decl_stmt|;
DECL|field|lastWrittenPosition
specifier|private
specifier|volatile
name|long
name|lastWrittenPosition
init|=
literal|0
decl_stmt|;
DECL|field|lastSyncPosition
specifier|private
specifier|volatile
name|long
name|lastSyncPosition
init|=
literal|0
decl_stmt|;
DECL|method|SimpleFsTranslogFile
specifier|public
name|SimpleFsTranslogFile
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|long
name|id
parameter_list|,
name|ChannelReference
name|channelReference
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|channelReference
operator|=
name|channelReference
expr_stmt|;
name|this
operator|.
name|translogStream
operator|=
name|TranslogStreams
operator|.
name|translogStreamFor
argument_list|(
name|this
operator|.
name|channelReference
operator|.
name|file
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|headerSize
operator|=
name|this
operator|.
name|translogStream
operator|.
name|writeHeader
argument_list|(
name|channelReference
operator|.
name|channel
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastPosition
operator|+=
name|headerSize
expr_stmt|;
name|this
operator|.
name|lastWrittenPosition
operator|+=
name|headerSize
expr_stmt|;
name|this
operator|.
name|lastSyncPosition
operator|+=
name|headerSize
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|id
specifier|public
name|long
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
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
return|return
name|operationCounter
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
return|return
name|lastWrittenPosition
return|;
block|}
annotation|@
name|Override
DECL|method|add
specifier|public
name|Translog
operator|.
name|Location
name|add
parameter_list|(
name|BytesReference
name|data
parameter_list|)
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
name|long
name|position
init|=
name|lastPosition
decl_stmt|;
name|data
operator|.
name|writeTo
argument_list|(
name|channelReference
operator|.
name|channel
argument_list|()
argument_list|)
expr_stmt|;
name|lastPosition
operator|=
name|lastPosition
operator|+
name|data
operator|.
name|length
argument_list|()
expr_stmt|;
name|lastWrittenPosition
operator|=
name|lastWrittenPosition
operator|+
name|data
operator|.
name|length
argument_list|()
expr_stmt|;
name|operationCounter
operator|=
name|operationCounter
operator|+
literal|1
expr_stmt|;
return|return
operator|new
name|Translog
operator|.
name|Location
argument_list|(
name|id
argument_list|,
name|position
argument_list|,
name|data
operator|.
name|length
argument_list|()
argument_list|)
return|;
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
DECL|method|read
specifier|public
name|byte
index|[]
name|read
parameter_list|(
name|Translog
operator|.
name|Location
name|location
parameter_list|)
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
return|return
name|Channels
operator|.
name|readFromFileChannel
argument_list|(
name|channelReference
operator|.
name|channel
argument_list|()
argument_list|,
name|location
operator|.
name|translogLocation
argument_list|,
name|location
operator|.
name|size
argument_list|)
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
try|try
block|{
name|sync
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|channelReference
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Returns a snapshot on this file,<tt>null</tt> if it failed to snapshot.      */
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
if|if
condition|(
name|channelReference
operator|.
name|tryIncRef
argument_list|()
condition|)
block|{
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
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
name|FsChannelSnapshot
name|snapshot
init|=
operator|new
name|FsChannelSnapshot
argument_list|(
name|this
operator|.
name|id
argument_list|,
name|channelReference
argument_list|,
name|lastWrittenPosition
argument_list|,
name|operationCounter
argument_list|)
decl_stmt|;
name|snapshot
operator|.
name|seekTo
argument_list|(
name|this
operator|.
name|headerSize
argument_list|)
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
return|return
name|snapshot
return|;
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
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|TranslogException
argument_list|(
name|shardId
argument_list|,
literal|"failed to create snapshot"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|channelReference
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|syncNeeded
specifier|public
name|boolean
name|syncNeeded
parameter_list|()
block|{
return|return
name|lastWrittenPosition
operator|!=
name|lastSyncPosition
return|;
block|}
annotation|@
name|Override
DECL|method|getStream
specifier|public
name|TranslogStream
name|getStream
parameter_list|()
block|{
return|return
name|this
operator|.
name|translogStream
return|;
block|}
annotation|@
name|Override
DECL|method|getPath
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
return|return
name|channelReference
operator|.
name|file
argument_list|()
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
comment|// check if we really need to sync here...
if|if
condition|(
operator|!
name|syncNeeded
argument_list|()
condition|)
block|{
return|return;
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
name|lastSyncPosition
operator|=
name|lastWrittenPosition
expr_stmt|;
name|channelReference
operator|.
name|channel
argument_list|()
operator|.
name|force
argument_list|(
literal|false
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
DECL|method|reuse
specifier|public
name|void
name|reuse
parameter_list|(
name|FsTranslogFile
name|other
parameter_list|)
block|{
comment|// nothing to do there
block|}
annotation|@
name|Override
DECL|method|updateBufferSize
specifier|public
name|void
name|updateBufferSize
parameter_list|(
name|int
name|bufferSize
parameter_list|)
throws|throws
name|TranslogException
block|{
comment|// nothing to do here...
block|}
block|}
end_class

end_unit

