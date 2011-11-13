begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.io.stream
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|stream
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
name|compress
operator|.
name|lzf
operator|.
name|BufferRecycler
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
name|compress
operator|.
name|lzf
operator|.
name|ChunkDecoder
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
name|compress
operator|.
name|lzf
operator|.
name|LZFChunk
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
name|compress
operator|.
name|lzf
operator|.
name|util
operator|.
name|ChunkDecoderFactory
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|LZFStreamInput
specifier|public
class|class
name|LZFStreamInput
extends|extends
name|StreamInput
block|{
comment|/**      * Underlying decoder in use.      */
DECL|field|_decoder
specifier|private
specifier|final
name|ChunkDecoder
name|_decoder
decl_stmt|;
comment|/**      * Object that handles details of buffer recycling      */
DECL|field|_recycler
specifier|private
specifier|final
name|BufferRecycler
name|_recycler
decl_stmt|;
comment|/**      * stream to be decompressed      */
DECL|field|inputStream
specifier|protected
name|StreamInput
name|inputStream
decl_stmt|;
comment|/**      * Flag that indicates if we have already called 'inputStream.close()'      * (to avoid calling it multiple times)      */
DECL|field|inputStreamClosed
specifier|protected
name|boolean
name|inputStreamClosed
decl_stmt|;
comment|/**      * Flag that indicates whether we force full reads (reading of as many      * bytes as requested), or 'optimal' reads (up to as many as available,      * but at least one). Default is false, meaning that 'optimal' read      * is used.      */
DECL|field|_cfgFullReads
specifier|protected
name|boolean
name|_cfgFullReads
init|=
literal|true
decl_stmt|;
comment|// ES: ALWAYS TRUE since we need to throw EOF when doing readBytes
comment|/* the current buffer of compressed bytes (from which to decode) */
DECL|field|_inputBuffer
specifier|private
name|byte
index|[]
name|_inputBuffer
decl_stmt|;
comment|/* the buffer of uncompressed bytes from which content is read */
DECL|field|_decodedBytes
specifier|private
name|byte
index|[]
name|_decodedBytes
decl_stmt|;
comment|/* The current position (next char to output) in the uncompressed bytes buffer. */
DECL|field|bufferPosition
specifier|private
name|int
name|bufferPosition
init|=
literal|0
decl_stmt|;
comment|/* Length of the current uncompressed bytes buffer */
DECL|field|bufferLength
specifier|private
name|int
name|bufferLength
init|=
literal|0
decl_stmt|;
comment|// ES: added to support never closing just resetting
DECL|field|cached
specifier|private
specifier|final
name|boolean
name|cached
decl_stmt|;
DECL|method|LZFStreamInput
specifier|public
name|LZFStreamInput
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|boolean
name|cached
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|cached
operator|=
name|cached
expr_stmt|;
if|if
condition|(
name|cached
condition|)
block|{
name|_recycler
operator|=
operator|new
name|BufferRecycler
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|_recycler
operator|=
name|BufferRecycler
operator|.
name|instance
argument_list|()
expr_stmt|;
block|}
name|_decoder
operator|=
name|ChunkDecoderFactory
operator|.
name|optimalInstance
argument_list|()
expr_stmt|;
name|inputStream
operator|=
name|in
expr_stmt|;
name|inputStreamClosed
operator|=
literal|false
expr_stmt|;
name|_inputBuffer
operator|=
name|_recycler
operator|.
name|allocInputBuffer
argument_list|(
name|LZFChunk
operator|.
name|MAX_CHUNK_LEN
argument_list|)
expr_stmt|;
name|_decodedBytes
operator|=
name|_recycler
operator|.
name|allocDecodeBuffer
argument_list|(
name|LZFChunk
operator|.
name|MAX_CHUNK_LEN
argument_list|)
expr_stmt|;
block|}
comment|/**      * Method is overridden to report number of bytes that can now be read      * from decoded data buffer, without reading bytes from the underlying      * stream.      * Never throws an exception; returns number of bytes available without      * further reads from underlying source; -1 if stream has been closed, or      * 0 if an actual read (and possible blocking) is needed to find out.      */
annotation|@
name|Override
DECL|method|available
specifier|public
name|int
name|available
parameter_list|()
block|{
comment|// if closed, return -1;
if|if
condition|(
name|inputStreamClosed
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|left
init|=
operator|(
name|bufferLength
operator|-
name|bufferPosition
operator|)
decl_stmt|;
return|return
operator|(
name|left
operator|<=
literal|0
operator|)
condition|?
literal|0
else|:
name|left
return|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|readyBuffer
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|_decodedBytes
index|[
name|bufferPosition
operator|++
index|]
operator|&
literal|255
return|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|(
specifier|final
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|length
operator|<
literal|1
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
operator|!
name|readyBuffer
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
comment|// First let's read however much data we happen to have...
name|int
name|chunkLength
init|=
name|Math
operator|.
name|min
argument_list|(
name|bufferLength
operator|-
name|bufferPosition
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|_decodedBytes
argument_list|,
name|bufferPosition
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|chunkLength
argument_list|)
expr_stmt|;
name|bufferPosition
operator|+=
name|chunkLength
expr_stmt|;
if|if
condition|(
name|chunkLength
operator|==
name|length
operator|||
operator|!
name|_cfgFullReads
condition|)
block|{
return|return
name|chunkLength
return|;
block|}
comment|// Need more data, then
name|int
name|totalRead
init|=
name|chunkLength
decl_stmt|;
do|do
block|{
name|offset
operator|+=
name|chunkLength
expr_stmt|;
if|if
condition|(
operator|!
name|readyBuffer
argument_list|()
condition|)
block|{
break|break;
block|}
name|chunkLength
operator|=
name|Math
operator|.
name|min
argument_list|(
name|bufferLength
operator|-
name|bufferPosition
argument_list|,
operator|(
name|length
operator|-
name|totalRead
operator|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|_decodedBytes
argument_list|,
name|bufferPosition
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|chunkLength
argument_list|)
expr_stmt|;
name|bufferPosition
operator|+=
name|chunkLength
expr_stmt|;
name|totalRead
operator|+=
name|chunkLength
expr_stmt|;
block|}
do|while
condition|(
name|totalRead
operator|<
name|length
condition|)
do|;
return|return
name|totalRead
return|;
block|}
DECL|method|readByte
annotation|@
name|Override
specifier|public
name|byte
name|readByte
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|readyBuffer
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
block|}
return|return
name|_decodedBytes
index|[
name|bufferPosition
operator|++
index|]
return|;
block|}
DECL|method|readBytes
annotation|@
name|Override
specifier|public
name|void
name|readBytes
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|result
init|=
name|read
argument_list|(
name|b
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|<
name|len
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
block|}
block|}
DECL|method|reset
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|bufferPosition
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|bufferLength
operator|=
literal|0
expr_stmt|;
name|inputStream
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|inputStream
operator|=
name|in
expr_stmt|;
name|this
operator|.
name|bufferPosition
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|bufferLength
operator|=
literal|0
expr_stmt|;
block|}
comment|/**      * Expert!, resets to buffer start, without the need to decompress it again.      */
DECL|method|resetToBufferStart
specifier|public
name|void
name|resetToBufferStart
parameter_list|()
block|{
name|this
operator|.
name|bufferPosition
operator|=
literal|0
expr_stmt|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|cached
condition|)
block|{
name|reset
argument_list|()
expr_stmt|;
return|return;
block|}
name|bufferPosition
operator|=
name|bufferLength
operator|=
literal|0
expr_stmt|;
name|byte
index|[]
name|buf
init|=
name|_inputBuffer
decl_stmt|;
if|if
condition|(
name|buf
operator|!=
literal|null
condition|)
block|{
name|_inputBuffer
operator|=
literal|null
expr_stmt|;
name|_recycler
operator|.
name|releaseInputBuffer
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
name|buf
operator|=
name|_decodedBytes
expr_stmt|;
if|if
condition|(
name|buf
operator|!=
literal|null
condition|)
block|{
name|_decodedBytes
operator|=
literal|null
expr_stmt|;
name|_recycler
operator|.
name|releaseDecodeBuffer
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|inputStreamClosed
condition|)
block|{
name|inputStreamClosed
operator|=
literal|true
expr_stmt|;
name|inputStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/*    ///////////////////////////////////////////////////////////////////////    // Internal methods    ///////////////////////////////////////////////////////////////////////     */
comment|/**      * Fill the uncompressed bytes buffer by reading the underlying inputStream.      *      * @throws IOException      */
DECL|method|readyBuffer
specifier|protected
name|boolean
name|readyBuffer
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|bufferPosition
operator|<
name|bufferLength
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|inputStreamClosed
condition|)
block|{
return|return
literal|false
return|;
block|}
name|bufferLength
operator|=
name|_decoder
operator|.
name|decodeChunk
argument_list|(
name|inputStream
argument_list|,
name|_inputBuffer
argument_list|,
name|_decodedBytes
argument_list|)
expr_stmt|;
if|if
condition|(
name|bufferLength
operator|<
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
name|bufferPosition
operator|=
literal|0
expr_stmt|;
return|return
operator|(
name|bufferPosition
operator|<
name|bufferLength
operator|)
return|;
block|}
block|}
end_class

end_unit

