begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store.memory
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|memory
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
name|store
operator|.
name|IndexOutput
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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|MemoryIndexOutput
specifier|public
class|class
name|MemoryIndexOutput
extends|extends
name|IndexOutput
block|{
DECL|field|dir
specifier|private
specifier|final
name|MemoryDirectory
name|dir
decl_stmt|;
DECL|field|file
specifier|private
specifier|final
name|MemoryFile
name|file
decl_stmt|;
DECL|field|buffers
specifier|private
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|buffers
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|currentBuffer
specifier|private
name|byte
index|[]
name|currentBuffer
decl_stmt|;
DECL|field|currentBufferIndex
specifier|private
name|int
name|currentBufferIndex
decl_stmt|;
DECL|field|bufferPosition
specifier|private
name|int
name|bufferPosition
decl_stmt|;
DECL|field|bufferStart
specifier|private
name|long
name|bufferStart
decl_stmt|;
DECL|field|bufferLength
specifier|private
name|int
name|bufferLength
decl_stmt|;
DECL|method|MemoryIndexOutput
specifier|public
name|MemoryIndexOutput
parameter_list|(
name|MemoryDirectory
name|dir
parameter_list|,
name|MemoryFile
name|file
parameter_list|)
block|{
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
name|this
operator|.
name|file
operator|=
name|file
expr_stmt|;
comment|// make sure that we switch to the
comment|// first needed buffer lazily
name|currentBufferIndex
operator|=
operator|-
literal|1
expr_stmt|;
name|currentBuffer
operator|=
literal|null
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
name|bufferPosition
operator|==
name|bufferLength
condition|)
block|{
name|currentBufferIndex
operator|++
expr_stmt|;
name|switchCurrentBuffer
argument_list|()
expr_stmt|;
block|}
name|currentBuffer
index|[
name|bufferPosition
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
name|len
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
name|len
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|bufferPosition
operator|==
name|bufferLength
condition|)
block|{
name|currentBufferIndex
operator|++
expr_stmt|;
name|switchCurrentBuffer
argument_list|()
expr_stmt|;
block|}
name|int
name|remainInBuffer
init|=
name|currentBuffer
operator|.
name|length
operator|-
name|bufferPosition
decl_stmt|;
name|int
name|bytesToCopy
init|=
name|len
operator|<
name|remainInBuffer
condition|?
name|len
else|:
name|remainInBuffer
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|b
argument_list|,
name|offset
argument_list|,
name|currentBuffer
argument_list|,
name|bufferPosition
argument_list|,
name|bytesToCopy
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|bytesToCopy
expr_stmt|;
name|len
operator|-=
name|bytesToCopy
expr_stmt|;
name|bufferPosition
operator|+=
name|bytesToCopy
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
name|file
operator|.
name|lastModified
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|setFileLength
argument_list|()
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
name|flush
argument_list|()
expr_stmt|;
name|file
operator|.
name|buffers
argument_list|(
name|buffers
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[
name|buffers
operator|.
name|size
argument_list|()
index|]
index|[]
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getFilePointer
annotation|@
name|Override
specifier|public
name|long
name|getFilePointer
parameter_list|()
block|{
return|return
name|currentBufferIndex
operator|<
literal|0
condition|?
literal|0
else|:
name|bufferStart
operator|+
name|bufferPosition
return|;
block|}
DECL|method|seek
annotation|@
name|Override
specifier|public
name|void
name|seek
parameter_list|(
name|long
name|pos
parameter_list|)
throws|throws
name|IOException
block|{
comment|// set the file length in case we seek back
comment|// and flush() has not been called yet
name|setFileLength
argument_list|()
expr_stmt|;
if|if
condition|(
name|pos
operator|<
name|bufferStart
operator|||
name|pos
operator|>=
name|bufferStart
operator|+
name|bufferLength
condition|)
block|{
name|currentBufferIndex
operator|=
call|(
name|int
call|)
argument_list|(
name|pos
operator|/
name|dir
operator|.
name|bufferSizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
name|switchCurrentBuffer
argument_list|()
expr_stmt|;
block|}
name|bufferPosition
operator|=
call|(
name|int
call|)
argument_list|(
name|pos
operator|%
name|dir
operator|.
name|bufferSizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|length
annotation|@
name|Override
specifier|public
name|long
name|length
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|file
operator|.
name|length
argument_list|()
return|;
block|}
DECL|method|switchCurrentBuffer
specifier|private
name|void
name|switchCurrentBuffer
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|currentBufferIndex
operator|==
name|buffers
operator|.
name|size
argument_list|()
condition|)
block|{
name|currentBuffer
operator|=
name|dir
operator|.
name|acquireBuffer
argument_list|()
expr_stmt|;
name|buffers
operator|.
name|add
argument_list|(
name|currentBuffer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|currentBuffer
operator|=
name|buffers
operator|.
name|get
argument_list|(
name|currentBufferIndex
argument_list|)
expr_stmt|;
block|}
name|bufferPosition
operator|=
literal|0
expr_stmt|;
name|bufferStart
operator|=
operator|(
name|long
operator|)
name|dir
operator|.
name|bufferSizeInBytes
argument_list|()
operator|*
operator|(
name|long
operator|)
name|currentBufferIndex
expr_stmt|;
name|bufferLength
operator|=
name|currentBuffer
operator|.
name|length
expr_stmt|;
block|}
DECL|method|setFileLength
specifier|private
name|void
name|setFileLength
parameter_list|()
block|{
name|long
name|pointer
init|=
name|bufferStart
operator|+
name|bufferPosition
decl_stmt|;
if|if
condition|(
name|pointer
operator|>
name|file
operator|.
name|length
argument_list|()
condition|)
block|{
name|file
operator|.
name|length
argument_list|(
name|pointer
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

