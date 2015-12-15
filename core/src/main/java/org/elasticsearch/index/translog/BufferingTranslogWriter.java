begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
package|;
end_package

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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ReleasableLock
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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|BufferingTranslogWriter
specifier|public
specifier|final
class|class
name|BufferingTranslogWriter
extends|extends
name|TranslogWriter
block|{
DECL|field|buffer
specifier|private
name|byte
index|[]
name|buffer
decl_stmt|;
DECL|field|bufferCount
specifier|private
name|int
name|bufferCount
decl_stmt|;
DECL|field|bufferOs
specifier|private
name|WrapperOutputStream
name|bufferOs
init|=
operator|new
name|WrapperOutputStream
argument_list|()
decl_stmt|;
comment|/* the total offset of this file including the bytes written to the file as well as into the buffer */
DECL|field|totalOffset
specifier|private
specifier|volatile
name|long
name|totalOffset
decl_stmt|;
DECL|method|BufferingTranslogWriter
specifier|public
name|BufferingTranslogWriter
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|long
name|generation
parameter_list|,
name|ChannelReference
name|channelReference
parameter_list|,
name|int
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|generation
argument_list|,
name|channelReference
argument_list|)
expr_stmt|;
name|this
operator|.
name|buffer
operator|=
operator|new
name|byte
index|[
name|bufferSize
index|]
expr_stmt|;
name|this
operator|.
name|totalOffset
operator|=
name|writtenOffset
expr_stmt|;
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
try|try
init|(
name|ReleasableLock
name|lock
init|=
name|writeLock
operator|.
name|acquire
argument_list|()
init|)
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
specifier|final
name|long
name|offset
init|=
name|totalOffset
decl_stmt|;
if|if
condition|(
name|data
operator|.
name|length
argument_list|()
operator|>=
name|buffer
operator|.
name|length
condition|)
block|{
name|flush
argument_list|()
expr_stmt|;
comment|// we use the channel to write, since on windows, writing to the RAF might not be reflected
comment|// when reading through the channel
try|try
block|{
name|data
operator|.
name|writeTo
argument_list|(
name|channel
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
name|closeWithTragicEvent
argument_list|(
name|ex
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
name|writtenOffset
operator|+=
name|data
operator|.
name|length
argument_list|()
expr_stmt|;
name|totalOffset
operator|+=
name|data
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|data
operator|.
name|length
argument_list|()
operator|>
name|buffer
operator|.
name|length
operator|-
name|bufferCount
condition|)
block|{
name|flush
argument_list|()
expr_stmt|;
block|}
name|data
operator|.
name|writeTo
argument_list|(
name|bufferOs
argument_list|)
expr_stmt|;
name|totalOffset
operator|+=
name|data
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|operationCounter
operator|++
expr_stmt|;
return|return
operator|new
name|Translog
operator|.
name|Location
argument_list|(
name|generation
argument_list|,
name|offset
argument_list|,
name|data
operator|.
name|length
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|flush
specifier|protected
specifier|final
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
assert|assert
name|writeLock
operator|.
name|isHeldByCurrentThread
argument_list|()
assert|;
if|if
condition|(
name|bufferCount
operator|>
literal|0
condition|)
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
comment|// we use the channel to write, since on windows, writing to the RAF might not be reflected
comment|// when reading through the channel
specifier|final
name|int
name|bufferSize
init|=
name|bufferCount
decl_stmt|;
try|try
block|{
name|Channels
operator|.
name|writeToChannel
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|bufferSize
argument_list|,
name|channel
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
name|closeWithTragicEvent
argument_list|(
name|ex
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
name|writtenOffset
operator|+=
name|bufferSize
expr_stmt|;
name|bufferCount
operator|=
literal|0
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|readBytes
specifier|protected
name|void
name|readBytes
parameter_list|(
name|ByteBuffer
name|targetBuffer
parameter_list|,
name|long
name|position
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|ReleasableLock
name|lock
init|=
name|readLock
operator|.
name|acquire
argument_list|()
init|)
block|{
if|if
condition|(
name|position
operator|>=
name|writtenOffset
condition|)
block|{
assert|assert
name|targetBuffer
operator|.
name|hasArray
argument_list|()
operator|:
literal|"buffer must have array"
assert|;
specifier|final
name|int
name|sourcePosition
init|=
call|(
name|int
call|)
argument_list|(
name|position
operator|-
name|writtenOffset
argument_list|)
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|buffer
argument_list|,
name|sourcePosition
argument_list|,
name|targetBuffer
operator|.
name|array
argument_list|()
argument_list|,
name|targetBuffer
operator|.
name|position
argument_list|()
argument_list|,
name|targetBuffer
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
name|targetBuffer
operator|.
name|position
argument_list|(
name|targetBuffer
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
comment|// we don't have to have a read lock here because we only write ahead to the file, so all writes has been complete
comment|// for the requested location.
name|Channels
operator|.
name|readFromFileChannelWithEofException
argument_list|(
name|channel
argument_list|,
name|position
argument_list|,
name|targetBuffer
argument_list|)
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
return|return
name|totalOffset
operator|!=
name|lastSyncedOffset
return|;
block|}
annotation|@
name|Override
DECL|method|sync
specifier|public
specifier|synchronized
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|syncNeeded
argument_list|()
condition|)
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
comment|// this call gives a better exception that the incRef if we are closed by a tragic event
name|channelReference
operator|.
name|incRef
argument_list|()
expr_stmt|;
try|try
block|{
specifier|final
name|long
name|offsetToSync
decl_stmt|;
specifier|final
name|int
name|opsCounter
decl_stmt|;
try|try
init|(
name|ReleasableLock
name|lock
init|=
name|writeLock
operator|.
name|acquire
argument_list|()
init|)
block|{
name|flush
argument_list|()
expr_stmt|;
name|offsetToSync
operator|=
name|totalOffset
expr_stmt|;
name|opsCounter
operator|=
name|operationCounter
expr_stmt|;
block|}
comment|// we can do this outside of the write lock but we have to protect from
comment|// concurrent syncs
name|ensureOpen
argument_list|()
expr_stmt|;
comment|// just for kicks - the checkpoint happens or not either way
try|try
block|{
name|checkpoint
argument_list|(
name|offsetToSync
argument_list|,
name|opsCounter
argument_list|,
name|channelReference
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
name|closeWithTragicEvent
argument_list|(
name|ex
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
name|lastSyncedOffset
operator|=
name|offsetToSync
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
DECL|class|WrapperOutputStream
class|class
name|WrapperOutputStream
extends|extends
name|OutputStream
block|{
annotation|@
name|Override
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|buffer
index|[
name|bufferCount
operator|++
index|]
operator|=
operator|(
name|byte
operator|)
name|b
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we do safety checked when we decide to use this stream...
name|System
operator|.
name|arraycopy
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|buffer
argument_list|,
name|bufferCount
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|bufferCount
operator|+=
name|len
expr_stmt|;
block|}
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
name|totalOffset
return|;
block|}
block|}
end_class

end_unit

