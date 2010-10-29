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
name|ChunkEncoder
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
DECL|class|LZFStreamOutput
specifier|public
class|class
name|LZFStreamOutput
extends|extends
name|StreamOutput
block|{
DECL|field|out
specifier|private
name|StreamOutput
name|out
decl_stmt|;
DECL|field|outputBuffer
specifier|private
specifier|final
name|byte
index|[]
name|outputBuffer
init|=
operator|new
name|byte
index|[
name|LZFChunk
operator|.
name|MAX_CHUNK_LEN
index|]
decl_stmt|;
DECL|field|encoder
specifier|private
specifier|final
name|ChunkEncoder
name|encoder
init|=
operator|new
name|ChunkEncoder
argument_list|(
name|LZFChunk
operator|.
name|MAX_CHUNK_LEN
argument_list|)
decl_stmt|;
DECL|field|position
specifier|private
name|int
name|position
init|=
literal|0
decl_stmt|;
DECL|method|LZFStreamOutput
specifier|public
name|LZFStreamOutput
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
block|}
DECL|method|write
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|int
name|singleByte
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|position
operator|>=
name|outputBuffer
operator|.
name|length
condition|)
block|{
name|writeCompressedBlock
argument_list|()
expr_stmt|;
block|}
name|outputBuffer
index|[
name|position
operator|++
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|singleByte
operator|&
literal|0xff
argument_list|)
expr_stmt|;
block|}
DECL|method|writeByte
annotation|@
name|Override
specifier|public
name|void
name|writeByte
parameter_list|(
name|byte
name|b
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|position
operator|>=
name|outputBuffer
operator|.
name|length
condition|)
block|{
name|writeCompressedBlock
argument_list|()
expr_stmt|;
block|}
name|outputBuffer
index|[
name|position
operator|++
index|]
operator|=
name|b
expr_stmt|;
block|}
DECL|method|writeBytes
annotation|@
name|Override
specifier|public
name|void
name|writeBytes
parameter_list|(
name|byte
index|[]
name|b
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
name|int
name|inputCursor
init|=
name|offset
decl_stmt|;
name|int
name|remainingBytes
init|=
name|length
decl_stmt|;
while|while
condition|(
name|remainingBytes
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|position
operator|>=
name|outputBuffer
operator|.
name|length
condition|)
block|{
name|writeCompressedBlock
argument_list|()
expr_stmt|;
block|}
name|int
name|chunkLength
init|=
operator|(
name|remainingBytes
operator|>
operator|(
name|outputBuffer
operator|.
name|length
operator|-
name|position
operator|)
operator|)
condition|?
name|outputBuffer
operator|.
name|length
operator|-
name|position
else|:
name|remainingBytes
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|b
argument_list|,
name|inputCursor
argument_list|,
name|outputBuffer
argument_list|,
name|position
argument_list|,
name|chunkLength
argument_list|)
expr_stmt|;
name|position
operator|+=
name|chunkLength
expr_stmt|;
name|remainingBytes
operator|-=
name|chunkLength
expr_stmt|;
name|inputCursor
operator|+=
name|chunkLength
expr_stmt|;
block|}
block|}
DECL|method|flush
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|writeCompressedBlock
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
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
try|try
block|{
name|flush
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|position
operator|=
literal|0
expr_stmt|;
name|out
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
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
DECL|method|wrappedOut
specifier|public
name|StreamOutput
name|wrappedOut
parameter_list|()
block|{
return|return
name|this
operator|.
name|out
return|;
block|}
comment|/**      * Compress and write the current block to the OutputStream      */
DECL|method|writeCompressedBlock
specifier|private
name|void
name|writeCompressedBlock
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|position
operator|>
literal|0
condition|)
block|{
name|encoder
operator|.
name|encodeChunk
argument_list|(
name|out
argument_list|,
name|outputBuffer
argument_list|,
literal|0
argument_list|,
name|position
argument_list|)
expr_stmt|;
name|position
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

