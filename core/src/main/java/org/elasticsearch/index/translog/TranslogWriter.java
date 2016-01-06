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
name|apache
operator|.
name|lucene
operator|.
name|codecs
operator|.
name|CodecUtil
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
name|AlreadyClosedException
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
name|OutputStreamDataOutput
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
name|BytesRef
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|RamUsageEstimator
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
name|Callback
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
name|BufferedOutputStream
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

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|FileChannel
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
name|OpenOption
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
name|nio
operator|.
name|file
operator|.
name|StandardOpenOption
import|;
end_import

begin_class
DECL|class|TranslogWriter
specifier|public
class|class
name|TranslogWriter
extends|extends
name|TranslogReader
block|{
DECL|field|TRANSLOG_CODEC
specifier|public
specifier|static
specifier|final
name|String
name|TRANSLOG_CODEC
init|=
literal|"translog"
decl_stmt|;
DECL|field|VERSION_CHECKSUMS
specifier|public
specifier|static
specifier|final
name|int
name|VERSION_CHECKSUMS
init|=
literal|1
decl_stmt|;
DECL|field|VERSION_CHECKPOINTS
specifier|public
specifier|static
specifier|final
name|int
name|VERSION_CHECKPOINTS
init|=
literal|2
decl_stmt|;
comment|// since 2.0 we have checkpoints?
DECL|field|VERSION
specifier|public
specifier|static
specifier|final
name|int
name|VERSION
init|=
name|VERSION_CHECKPOINTS
decl_stmt|;
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
comment|/* the offset in bytes that was written when the file was last synced*/
DECL|field|lastSyncedOffset
specifier|private
specifier|volatile
name|long
name|lastSyncedOffset
decl_stmt|;
comment|/* the number of translog operations written to this file */
DECL|field|operationCounter
specifier|private
specifier|volatile
name|int
name|operationCounter
decl_stmt|;
comment|/* if we hit an exception that we can't recover from we assign it to this var and ship it with every AlreadyClosedException we throw */
DECL|field|tragedy
specifier|private
specifier|volatile
name|Throwable
name|tragedy
decl_stmt|;
comment|/* A buffered outputstream what writes to the writers channel */
DECL|field|outputStream
specifier|private
specifier|final
name|OutputStream
name|outputStream
decl_stmt|;
comment|/* the total offset of this file including the bytes written to the file as well as into the buffer */
DECL|field|totalOffset
specifier|private
specifier|volatile
name|long
name|totalOffset
decl_stmt|;
DECL|method|TranslogWriter
specifier|public
name|TranslogWriter
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
name|ByteSizeValue
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|generation
argument_list|,
name|channelReference
argument_list|,
name|channelReference
operator|.
name|getChannel
argument_list|()
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|outputStream
operator|=
operator|new
name|BufferedChannelOutputStream
argument_list|(
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|Channels
operator|.
name|newOutputStream
argument_list|(
name|channelReference
operator|.
name|getChannel
argument_list|()
argument_list|)
argument_list|,
name|bufferSize
operator|.
name|bytesAsInt
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastSyncedOffset
operator|=
name|channelReference
operator|.
name|getChannel
argument_list|()
operator|.
name|position
argument_list|()
expr_stmt|;
name|totalOffset
operator|=
name|lastSyncedOffset
expr_stmt|;
block|}
DECL|method|create
specifier|public
specifier|static
name|TranslogWriter
name|create
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|translogUUID
parameter_list|,
name|long
name|fileGeneration
parameter_list|,
name|Path
name|file
parameter_list|,
name|Callback
argument_list|<
name|ChannelReference
argument_list|>
name|onClose
parameter_list|,
name|ChannelFactory
name|channelFactory
parameter_list|,
name|ByteSizeValue
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|(
name|translogUUID
argument_list|)
decl_stmt|;
specifier|final
name|int
name|headerLength
init|=
name|CodecUtil
operator|.
name|headerLength
argument_list|(
name|TRANSLOG_CODEC
argument_list|)
operator|+
name|ref
operator|.
name|length
operator|+
name|RamUsageEstimator
operator|.
name|NUM_BYTES_INT
decl_stmt|;
specifier|final
name|FileChannel
name|channel
init|=
name|channelFactory
operator|.
name|open
argument_list|(
name|file
argument_list|)
decl_stmt|;
try|try
block|{
comment|// This OutputStreamDataOutput is intentionally not closed because
comment|// closing it will close the FileChannel
specifier|final
name|OutputStreamDataOutput
name|out
init|=
operator|new
name|OutputStreamDataOutput
argument_list|(
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|Channels
operator|.
name|newOutputStream
argument_list|(
name|channel
argument_list|)
argument_list|)
decl_stmt|;
name|CodecUtil
operator|.
name|writeHeader
argument_list|(
name|out
argument_list|,
name|TRANSLOG_CODEC
argument_list|,
name|VERSION
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|ref
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|ref
operator|.
name|bytes
argument_list|,
name|ref
operator|.
name|offset
argument_list|,
name|ref
operator|.
name|length
argument_list|)
expr_stmt|;
name|channel
operator|.
name|force
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|writeCheckpoint
argument_list|(
name|headerLength
argument_list|,
literal|0
argument_list|,
name|file
operator|.
name|getParent
argument_list|()
argument_list|,
name|fileGeneration
argument_list|,
name|StandardOpenOption
operator|.
name|WRITE
argument_list|)
expr_stmt|;
specifier|final
name|TranslogWriter
name|writer
init|=
operator|new
name|TranslogWriter
argument_list|(
name|shardId
argument_list|,
name|fileGeneration
argument_list|,
operator|new
name|ChannelReference
argument_list|(
name|file
argument_list|,
name|fileGeneration
argument_list|,
name|channel
argument_list|,
name|onClose
argument_list|)
argument_list|,
name|bufferSize
argument_list|)
decl_stmt|;
return|return
name|writer
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
block|{
name|IOUtils
operator|.
name|closeWhileHandlingException
argument_list|(
name|channel
argument_list|)
expr_stmt|;
try|try
block|{
name|Files
operator|.
name|delete
argument_list|(
name|file
argument_list|)
expr_stmt|;
comment|// remove the file as well
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|throwable
operator|.
name|addSuppressed
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
throw|throw
name|throwable
throw|;
block|}
block|}
comment|/** If this {@code TranslogWriter} was closed as a side-effect of a tragic exception,      *  e.g. disk full while flushing a new segment, this returns the root cause exception.      *  Otherwise (no tragic exception has occurred) it returns null. */
DECL|method|getTragicException
specifier|public
name|Throwable
name|getTragicException
parameter_list|()
block|{
return|return
name|tragedy
return|;
block|}
DECL|method|closeWithTragicEvent
specifier|private
specifier|synchronized
specifier|final
name|void
name|closeWithTragicEvent
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|throwable
operator|!=
literal|null
operator|:
literal|"throwable must not be null in a tragic event"
assert|;
if|if
condition|(
name|tragedy
operator|==
literal|null
condition|)
block|{
name|tragedy
operator|=
name|throwable
expr_stmt|;
block|}
else|else
block|{
name|tragedy
operator|.
name|addSuppressed
argument_list|(
name|throwable
argument_list|)
expr_stmt|;
block|}
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**      * add the given bytes to the translog and return the location they were written at      */
DECL|method|add
specifier|public
specifier|synchronized
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
name|ensureOpen
argument_list|()
expr_stmt|;
specifier|final
name|long
name|offset
init|=
name|totalOffset
decl_stmt|;
try|try
block|{
name|data
operator|.
name|writeTo
argument_list|(
name|outputStream
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
name|totalOffset
operator|+=
name|data
operator|.
name|length
argument_list|()
expr_stmt|;
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
comment|/**      * write all buffered ops to disk and fsync file      */
DECL|method|sync
specifier|public
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
synchronized|synchronized
init|(
name|this
init|)
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
name|outputStream
operator|.
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
block|}
comment|/**      * returns true if there are buffered ops      */
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
DECL|method|totalOperations
specifier|public
name|int
name|totalOperations
parameter_list|()
block|{
return|return
name|operationCounter
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
name|totalOffset
return|;
block|}
comment|/**      * returns a new reader that follows the current writes (most importantly allows making      * repeated snapshots that includes new content)      */
DECL|method|newReaderFromWriter
specifier|public
name|TranslogReader
name|newReaderFromWriter
parameter_list|()
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
name|channelReference
operator|.
name|incRef
argument_list|()
expr_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
specifier|final
name|TranslogReader
name|reader
init|=
operator|new
name|InnerReader
argument_list|(
name|this
operator|.
name|generation
argument_list|,
name|firstOperationOffset
argument_list|,
name|channelReference
argument_list|)
decl_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
return|return
name|reader
return|;
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
comment|/**      * returns a new immutable reader which only exposes the current written operation *      */
DECL|method|immutableReader
specifier|public
name|ImmutableTranslogReader
name|immutableReader
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
synchronized|synchronized
init|(
name|this
init|)
block|{
try|try
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
name|outputStream
operator|.
name|flush
argument_list|()
expr_stmt|;
name|ImmutableTranslogReader
name|reader
init|=
operator|new
name|ImmutableTranslogReader
argument_list|(
name|this
operator|.
name|generation
argument_list|,
name|channelReference
argument_list|,
name|firstOperationOffset
argument_list|,
name|getWrittenOffset
argument_list|()
argument_list|,
name|operationCounter
argument_list|)
decl_stmt|;
name|channelReference
operator|.
name|incRef
argument_list|()
expr_stmt|;
comment|// for new reader
return|return
name|reader
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
literal|"exception while creating an immutable reader"
argument_list|,
name|e
argument_list|)
throw|;
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
else|else
block|{
throw|throw
operator|new
name|TranslogException
argument_list|(
name|shardId
argument_list|,
literal|"can't increment channel ["
operator|+
name|channelReference
operator|+
literal|"] ref count"
argument_list|)
throw|;
block|}
block|}
DECL|method|assertBytesAtLocation
name|boolean
name|assertBytesAtLocation
parameter_list|(
name|Translog
operator|.
name|Location
name|location
parameter_list|,
name|BytesReference
name|expectedBytes
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBuffer
name|buffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|location
operator|.
name|size
argument_list|)
decl_stmt|;
name|readBytes
argument_list|(
name|buffer
argument_list|,
name|location
operator|.
name|translogLocation
argument_list|)
expr_stmt|;
return|return
operator|new
name|BytesArray
argument_list|(
name|buffer
operator|.
name|array
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|expectedBytes
argument_list|)
return|;
block|}
DECL|method|getWrittenOffset
specifier|private
name|long
name|getWrittenOffset
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|channelReference
operator|.
name|getChannel
argument_list|()
operator|.
name|position
argument_list|()
return|;
block|}
comment|/**      * this class is used when one wants a reference to this file which exposes all recently written operation.      * as such it needs access to the internals of the current reader      */
DECL|class|InnerReader
specifier|final
class|class
name|InnerReader
extends|extends
name|TranslogReader
block|{
DECL|method|InnerReader
specifier|public
name|InnerReader
parameter_list|(
name|long
name|generation
parameter_list|,
name|long
name|fistOperationOffset
parameter_list|,
name|ChannelReference
name|channelReference
parameter_list|)
block|{
name|super
argument_list|(
name|generation
argument_list|,
name|channelReference
argument_list|,
name|fistOperationOffset
argument_list|)
expr_stmt|;
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
name|TranslogWriter
operator|.
name|this
operator|.
name|sizeInBytes
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|totalOperations
specifier|public
name|int
name|totalOperations
parameter_list|()
block|{
return|return
name|TranslogWriter
operator|.
name|this
operator|.
name|totalOperations
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|readBytes
specifier|protected
name|void
name|readBytes
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|,
name|long
name|position
parameter_list|)
throws|throws
name|IOException
block|{
name|TranslogWriter
operator|.
name|this
operator|.
name|readBytes
argument_list|(
name|buffer
argument_list|,
name|position
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Syncs the translog up to at least the given offset unless already synced      *      * @return<code>true</code> if this call caused an actual sync operation      */
DECL|method|syncUpTo
specifier|public
name|boolean
name|syncUpTo
parameter_list|(
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|lastSyncedOffset
operator|<
name|offset
condition|)
block|{
name|sync
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
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
if|if
condition|(
name|position
operator|+
name|targetBuffer
operator|.
name|remaining
argument_list|()
operator|>
name|getWrittenOffset
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
comment|// we only flush here if it's really really needed - try to minimize the impact of the read operation
comment|// in some cases ie. a tragic event we might still be able to read the relevant value
comment|// which is not really important in production but some test can make most strict assumptions
comment|// if we don't fail in this call unless absolutely necessary.
if|if
condition|(
name|position
operator|+
name|targetBuffer
operator|.
name|remaining
argument_list|()
operator|>
name|getWrittenOffset
argument_list|()
condition|)
block|{
name|outputStream
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|// we don't have to have a lock here because we only write ahead to the file, so all writes has been complete
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
DECL|method|checkpoint
specifier|private
specifier|synchronized
name|void
name|checkpoint
parameter_list|(
name|long
name|lastSyncPosition
parameter_list|,
name|int
name|operationCounter
parameter_list|,
name|ChannelReference
name|channelReference
parameter_list|)
throws|throws
name|IOException
block|{
name|channelReference
operator|.
name|getChannel
argument_list|()
operator|.
name|force
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|writeCheckpoint
argument_list|(
name|lastSyncPosition
argument_list|,
name|operationCounter
argument_list|,
name|channelReference
operator|.
name|getPath
argument_list|()
operator|.
name|getParent
argument_list|()
argument_list|,
name|channelReference
operator|.
name|getGeneration
argument_list|()
argument_list|,
name|StandardOpenOption
operator|.
name|WRITE
argument_list|)
expr_stmt|;
block|}
DECL|method|writeCheckpoint
specifier|private
specifier|static
name|void
name|writeCheckpoint
parameter_list|(
name|long
name|syncPosition
parameter_list|,
name|int
name|numOperations
parameter_list|,
name|Path
name|translogFile
parameter_list|,
name|long
name|generation
parameter_list|,
name|OpenOption
modifier|...
name|options
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|checkpointFile
init|=
name|translogFile
operator|.
name|resolve
argument_list|(
name|Translog
operator|.
name|CHECKPOINT_FILE_NAME
argument_list|)
decl_stmt|;
name|Checkpoint
name|checkpoint
init|=
operator|new
name|Checkpoint
argument_list|(
name|syncPosition
argument_list|,
name|numOperations
argument_list|,
name|generation
argument_list|)
decl_stmt|;
name|Checkpoint
operator|.
name|write
argument_list|(
name|checkpointFile
argument_list|,
name|checkpoint
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
DECL|class|ChannelFactory
specifier|static
class|class
name|ChannelFactory
block|{
DECL|field|DEFAULT
specifier|static
specifier|final
name|ChannelFactory
name|DEFAULT
init|=
operator|new
name|ChannelFactory
argument_list|()
decl_stmt|;
comment|// only for testing until we have a disk-full FileSystemt
DECL|method|open
specifier|public
name|FileChannel
name|open
parameter_list|(
name|Path
name|file
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|FileChannel
operator|.
name|open
argument_list|(
name|file
argument_list|,
name|StandardOpenOption
operator|.
name|WRITE
argument_list|,
name|StandardOpenOption
operator|.
name|READ
argument_list|,
name|StandardOpenOption
operator|.
name|CREATE_NEW
argument_list|)
return|;
block|}
block|}
DECL|method|ensureOpen
specifier|protected
specifier|final
name|void
name|ensureOpen
parameter_list|()
block|{
if|if
condition|(
name|isClosed
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AlreadyClosedException
argument_list|(
literal|"translog ["
operator|+
name|getGeneration
argument_list|()
operator|+
literal|"] is already closed"
argument_list|,
name|tragedy
argument_list|)
throw|;
block|}
block|}
DECL|class|BufferedChannelOutputStream
specifier|private
specifier|final
class|class
name|BufferedChannelOutputStream
extends|extends
name|BufferedOutputStream
block|{
DECL|method|BufferedChannelOutputStream
specifier|public
name|BufferedChannelOutputStream
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|int
name|size
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|out
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|flush
specifier|public
specifier|synchronized
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|count
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
name|super
operator|.
name|flush
argument_list|()
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
comment|// the stream is intentionally not closed because
comment|// closing it will close the FileChannel
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"never close this stream"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

