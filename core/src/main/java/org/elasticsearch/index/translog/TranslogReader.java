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
name|index
operator|.
name|CorruptIndexException
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
name|index
operator|.
name|IndexFormatTooNewException
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
name|index
operator|.
name|IndexFormatTooOldException
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
name|InputStreamDataInput
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
name|io
operator|.
name|stream
operator|.
name|InputStreamStreamInput
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
name|EOFException
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

begin_comment
comment|/**  * an immutable translog filereader  */
end_comment

begin_class
DECL|class|TranslogReader
specifier|public
class|class
name|TranslogReader
extends|extends
name|BaseTranslogReader
implements|implements
name|Closeable
block|{
DECL|field|LUCENE_CODEC_HEADER_BYTE
specifier|private
specifier|static
specifier|final
name|byte
name|LUCENE_CODEC_HEADER_BYTE
init|=
literal|0x3f
decl_stmt|;
DECL|field|UNVERSIONED_TRANSLOG_HEADER_BYTE
specifier|private
specifier|static
specifier|final
name|byte
name|UNVERSIONED_TRANSLOG_HEADER_BYTE
init|=
literal|0x00
decl_stmt|;
DECL|field|totalOperations
specifier|private
specifier|final
name|int
name|totalOperations
decl_stmt|;
DECL|field|length
specifier|protected
specifier|final
name|long
name|length
decl_stmt|;
DECL|field|closed
specifier|protected
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|/**      * Create a reader of translog file channel. The length parameter should be consistent with totalOperations and point      * at the end of the last operation in this snapshot.      */
DECL|method|TranslogReader
specifier|public
name|TranslogReader
parameter_list|(
name|long
name|generation
parameter_list|,
name|FileChannel
name|channel
parameter_list|,
name|Path
name|path
parameter_list|,
name|long
name|firstOperationOffset
parameter_list|,
name|long
name|length
parameter_list|,
name|int
name|totalOperations
parameter_list|)
block|{
name|super
argument_list|(
name|generation
argument_list|,
name|channel
argument_list|,
name|path
argument_list|,
name|firstOperationOffset
argument_list|)
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|totalOperations
operator|=
name|totalOperations
expr_stmt|;
block|}
comment|/**      * Given a file, opens an {@link TranslogReader}, taking of checking and validating the file header.      */
DECL|method|open
specifier|public
specifier|static
name|TranslogReader
name|open
parameter_list|(
name|FileChannel
name|channel
parameter_list|,
name|Path
name|path
parameter_list|,
name|Checkpoint
name|checkpoint
parameter_list|,
name|String
name|translogUUID
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|InputStreamStreamInput
name|headerStream
init|=
operator|new
name|InputStreamStreamInput
argument_list|(
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|Channels
operator|.
name|newInputStream
argument_list|(
name|channel
argument_list|)
argument_list|)
decl_stmt|;
comment|// don't close
comment|// Lucene's CodecUtil writes a magic number of 0x3FD76C17 with the
comment|// header, in binary this looks like:
comment|//
comment|// binary: 0011 1111 1101 0111 0110 1100 0001 0111
comment|// hex   :    3    f    d    7    6    c    1    7
comment|//
comment|// With version 0 of the translog, the first byte is the
comment|// Operation.Type, which will always be between 0-4, so we know if
comment|// we grab the first byte, it can be:
comment|// 0x3f => Lucene's magic number, so we can assume it's version 1 or later
comment|// 0x00 => version 0 of the translog
comment|//
comment|// otherwise the first byte of the translog is corrupted and we
comment|// should bail
name|byte
name|b1
init|=
name|headerStream
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|b1
operator|==
name|LUCENE_CODEC_HEADER_BYTE
condition|)
block|{
comment|// Read 3 more bytes, meaning a whole integer has been read
name|byte
name|b2
init|=
name|headerStream
operator|.
name|readByte
argument_list|()
decl_stmt|;
name|byte
name|b3
init|=
name|headerStream
operator|.
name|readByte
argument_list|()
decl_stmt|;
name|byte
name|b4
init|=
name|headerStream
operator|.
name|readByte
argument_list|()
decl_stmt|;
comment|// Convert the 4 bytes that were read into an integer
name|int
name|header
init|=
operator|(
operator|(
name|b1
operator|&
literal|0xFF
operator|)
operator|<<
literal|24
operator|)
operator|+
operator|(
operator|(
name|b2
operator|&
literal|0xFF
operator|)
operator|<<
literal|16
operator|)
operator|+
operator|(
operator|(
name|b3
operator|&
literal|0xFF
operator|)
operator|<<
literal|8
operator|)
operator|+
operator|(
operator|(
name|b4
operator|&
literal|0xFF
operator|)
operator|<<
literal|0
operator|)
decl_stmt|;
comment|// We confirm CodecUtil's CODEC_MAGIC number (0x3FD76C17)
comment|// ourselves here, because it allows us to read the first
comment|// byte separately
if|if
condition|(
name|header
operator|!=
name|CodecUtil
operator|.
name|CODEC_MAGIC
condition|)
block|{
throw|throw
operator|new
name|TranslogCorruptedException
argument_list|(
literal|"translog looks like version 1 or later, but has corrupted header. path:"
operator|+
name|path
argument_list|)
throw|;
block|}
comment|// Confirm the rest of the header using CodecUtil, extracting
comment|// the translog version
name|int
name|version
init|=
name|CodecUtil
operator|.
name|checkHeaderNoMagic
argument_list|(
operator|new
name|InputStreamDataInput
argument_list|(
name|headerStream
argument_list|)
argument_list|,
name|TranslogWriter
operator|.
name|TRANSLOG_CODEC
argument_list|,
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|version
condition|)
block|{
case|case
name|TranslogWriter
operator|.
name|VERSION_CHECKSUMS
case|:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"pre-2.0 translog found ["
operator|+
name|path
operator|+
literal|"]"
argument_list|)
throw|;
case|case
name|TranslogWriter
operator|.
name|VERSION_CHECKPOINTS
case|:
assert|assert
name|path
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|endsWith
argument_list|(
name|Translog
operator|.
name|TRANSLOG_FILE_SUFFIX
argument_list|)
operator|:
literal|"new file ends with old suffix: "
operator|+
name|path
assert|;
assert|assert
name|checkpoint
operator|.
name|numOps
operator|>=
literal|0
operator|:
literal|"expected at least 0 operatin but got: "
operator|+
name|checkpoint
operator|.
name|numOps
assert|;
assert|assert
name|checkpoint
operator|.
name|offset
operator|<=
name|channel
operator|.
name|size
argument_list|()
operator|:
literal|"checkpoint is inconsistent with channel length: "
operator|+
name|channel
operator|.
name|size
argument_list|()
operator|+
literal|" "
operator|+
name|checkpoint
assert|;
name|int
name|len
init|=
name|headerStream
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|len
operator|>
name|channel
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|TranslogCorruptedException
argument_list|(
literal|"uuid length can't be larger than the translog"
argument_list|)
throw|;
block|}
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|(
name|len
argument_list|)
decl_stmt|;
name|ref
operator|.
name|length
operator|=
name|len
expr_stmt|;
name|headerStream
operator|.
name|read
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
name|BytesRef
name|uuidBytes
init|=
operator|new
name|BytesRef
argument_list|(
name|translogUUID
argument_list|)
decl_stmt|;
if|if
condition|(
name|uuidBytes
operator|.
name|bytesEquals
argument_list|(
name|ref
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|TranslogCorruptedException
argument_list|(
literal|"expected shard UUID "
operator|+
name|uuidBytes
operator|+
literal|" but got: "
operator|+
name|ref
operator|+
literal|" this translog file belongs to a different translog. path:"
operator|+
name|path
argument_list|)
throw|;
block|}
return|return
operator|new
name|TranslogReader
argument_list|(
name|checkpoint
operator|.
name|generation
argument_list|,
name|channel
argument_list|,
name|path
argument_list|,
name|ref
operator|.
name|length
operator|+
name|CodecUtil
operator|.
name|headerLength
argument_list|(
name|TranslogWriter
operator|.
name|TRANSLOG_CODEC
argument_list|)
operator|+
name|Integer
operator|.
name|BYTES
argument_list|,
name|checkpoint
operator|.
name|offset
argument_list|,
name|checkpoint
operator|.
name|numOps
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|TranslogCorruptedException
argument_list|(
literal|"No known translog stream version: "
operator|+
name|version
operator|+
literal|" path:"
operator|+
name|path
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|b1
operator|==
name|UNVERSIONED_TRANSLOG_HEADER_BYTE
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"pre-1.4 translog found ["
operator|+
name|path
operator|+
literal|"]"
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|TranslogCorruptedException
argument_list|(
literal|"Invalid first byte in translog file, got: "
operator|+
name|Long
operator|.
name|toHexString
argument_list|(
name|b1
argument_list|)
operator|+
literal|", expected 0x00 or 0x3f. path:"
operator|+
name|path
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|CorruptIndexException
decl||
name|IndexFormatTooOldException
decl||
name|IndexFormatTooNewException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|TranslogCorruptedException
argument_list|(
literal|"Translog header corrupted. path:"
operator|+
name|path
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|sizeInBytes
specifier|public
name|long
name|sizeInBytes
parameter_list|()
block|{
return|return
name|length
return|;
block|}
DECL|method|totalOperations
specifier|public
name|int
name|totalOperations
parameter_list|()
block|{
return|return
name|totalOperations
return|;
block|}
comment|/**      * reads an operation at the given position into the given buffer.      */
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
if|if
condition|(
name|position
operator|>=
name|length
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"read requested past EOF. pos ["
operator|+
name|position
operator|+
literal|"] end: ["
operator|+
name|length
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|position
operator|<
name|firstOperationOffset
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"read requested before position of first ops. pos ["
operator|+
name|position
operator|+
literal|"] first op on: ["
operator|+
name|firstOperationOffset
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Channels
operator|.
name|readFromFileChannelWithEofException
argument_list|(
name|channel
argument_list|,
name|position
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
block|}
DECL|method|getInfo
specifier|public
name|Checkpoint
name|getInfo
parameter_list|()
block|{
return|return
operator|new
name|Checkpoint
argument_list|(
name|length
argument_list|,
name|totalOperations
argument_list|,
name|getGeneration
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
specifier|final
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
name|channel
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|isClosed
specifier|protected
specifier|final
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|closed
operator|.
name|get
argument_list|()
return|;
block|}
DECL|method|ensureOpen
specifier|protected
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
name|toString
argument_list|()
operator|+
literal|" is already closed"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

