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
name|io
operator|.
name|stream
operator|.
name|ByteBufferStreamInput
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

begin_comment
comment|/**  * A base class for all classes that allows reading ops from translog files  */
end_comment

begin_class
DECL|class|BaseTranslogReader
specifier|public
specifier|abstract
class|class
name|BaseTranslogReader
implements|implements
name|Comparable
argument_list|<
name|BaseTranslogReader
argument_list|>
block|{
DECL|field|generation
specifier|protected
specifier|final
name|long
name|generation
decl_stmt|;
DECL|field|channel
specifier|protected
specifier|final
name|FileChannel
name|channel
decl_stmt|;
DECL|field|path
specifier|protected
specifier|final
name|Path
name|path
decl_stmt|;
DECL|field|firstOperationOffset
specifier|protected
specifier|final
name|long
name|firstOperationOffset
decl_stmt|;
DECL|method|BaseTranslogReader
specifier|public
name|BaseTranslogReader
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
parameter_list|)
block|{
assert|assert
name|Translog
operator|.
name|parseIdFromFileName
argument_list|(
name|path
argument_list|)
operator|==
name|generation
operator|:
literal|"generation missmatch. Path: "
operator|+
name|Translog
operator|.
name|parseIdFromFileName
argument_list|(
name|path
argument_list|)
operator|+
literal|" but generation: "
operator|+
name|generation
assert|;
name|this
operator|.
name|generation
operator|=
name|generation
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|channel
operator|=
name|channel
expr_stmt|;
name|this
operator|.
name|firstOperationOffset
operator|=
name|firstOperationOffset
expr_stmt|;
block|}
DECL|method|getGeneration
specifier|public
name|long
name|getGeneration
parameter_list|()
block|{
return|return
name|this
operator|.
name|generation
return|;
block|}
DECL|method|sizeInBytes
specifier|public
specifier|abstract
name|long
name|sizeInBytes
parameter_list|()
function_decl|;
DECL|method|totalOperations
specifier|abstract
specifier|public
name|int
name|totalOperations
parameter_list|()
function_decl|;
DECL|method|getFirstOperationOffset
specifier|public
specifier|final
name|long
name|getFirstOperationOffset
parameter_list|()
block|{
return|return
name|firstOperationOffset
return|;
block|}
DECL|method|read
specifier|public
name|Translog
operator|.
name|Operation
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
assert|assert
name|location
operator|.
name|generation
operator|==
name|generation
operator|:
literal|"read location's translog generation ["
operator|+
name|location
operator|.
name|generation
operator|+
literal|"] is not ["
operator|+
name|generation
operator|+
literal|"]"
assert|;
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
try|try
init|(
name|BufferedChecksumStreamInput
name|checksumStreamInput
init|=
name|checksummedStream
argument_list|(
name|buffer
argument_list|,
name|location
operator|.
name|translogLocation
argument_list|,
name|location
operator|.
name|size
argument_list|,
literal|null
argument_list|)
init|)
block|{
return|return
name|read
argument_list|(
name|checksumStreamInput
argument_list|)
return|;
block|}
block|}
comment|/** read the size of the op (i.e., number of bytes, including the op size) written at the given position */
DECL|method|readSize
specifier|protected
specifier|final
name|int
name|readSize
parameter_list|(
name|ByteBuffer
name|reusableBuffer
parameter_list|,
name|long
name|position
parameter_list|)
block|{
comment|// read op size from disk
assert|assert
name|reusableBuffer
operator|.
name|capacity
argument_list|()
operator|>=
literal|4
operator|:
literal|"reusable buffer must have capacity>=4 when reading opSize. got ["
operator|+
name|reusableBuffer
operator|.
name|capacity
argument_list|()
operator|+
literal|"]"
assert|;
try|try
block|{
name|reusableBuffer
operator|.
name|clear
argument_list|()
expr_stmt|;
name|reusableBuffer
operator|.
name|limit
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|readBytes
argument_list|(
name|reusableBuffer
argument_list|,
name|position
argument_list|)
expr_stmt|;
name|reusableBuffer
operator|.
name|flip
argument_list|()
expr_stmt|;
comment|// Add an extra 4 to account for the operation size integer itself
specifier|final
name|int
name|size
init|=
name|reusableBuffer
operator|.
name|getInt
argument_list|()
operator|+
literal|4
decl_stmt|;
specifier|final
name|long
name|maxSize
init|=
name|sizeInBytes
argument_list|()
operator|-
name|position
decl_stmt|;
if|if
condition|(
name|size
argument_list|<
literal|0
operator|||
name|size
argument_list|>
name|maxSize
condition|)
block|{
throw|throw
operator|new
name|TranslogCorruptedException
argument_list|(
literal|"operation size is corrupted must be [0.."
operator|+
name|maxSize
operator|+
literal|"] but was: "
operator|+
name|size
argument_list|)
throw|;
block|}
return|return
name|size
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
literal|"unexpected exception reading from translog snapshot of "
operator|+
name|this
operator|.
name|path
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|newSnapshot
specifier|public
name|Translog
operator|.
name|Snapshot
name|newSnapshot
parameter_list|()
block|{
return|return
operator|new
name|TranslogSnapshot
argument_list|(
name|generation
argument_list|,
name|channel
argument_list|,
name|path
argument_list|,
name|firstOperationOffset
argument_list|,
name|sizeInBytes
argument_list|()
argument_list|,
name|totalOperations
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * reads an operation at the given position and returns it. The buffer length is equal to the number      * of bytes reads.      */
DECL|method|checksummedStream
specifier|protected
specifier|final
name|BufferedChecksumStreamInput
name|checksummedStream
parameter_list|(
name|ByteBuffer
name|reusableBuffer
parameter_list|,
name|long
name|position
parameter_list|,
name|int
name|opSize
parameter_list|,
name|BufferedChecksumStreamInput
name|reuse
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|ByteBuffer
name|buffer
decl_stmt|;
if|if
condition|(
name|reusableBuffer
operator|.
name|capacity
argument_list|()
operator|>=
name|opSize
condition|)
block|{
name|buffer
operator|=
name|reusableBuffer
expr_stmt|;
block|}
else|else
block|{
name|buffer
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|opSize
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|clear
argument_list|()
expr_stmt|;
name|buffer
operator|.
name|limit
argument_list|(
name|opSize
argument_list|)
expr_stmt|;
name|readBytes
argument_list|(
name|buffer
argument_list|,
name|position
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|flip
argument_list|()
expr_stmt|;
return|return
operator|new
name|BufferedChecksumStreamInput
argument_list|(
operator|new
name|ByteBufferStreamInput
argument_list|(
name|buffer
argument_list|)
argument_list|,
name|reuse
argument_list|)
return|;
block|}
DECL|method|read
specifier|protected
name|Translog
operator|.
name|Operation
name|read
parameter_list|(
name|BufferedChecksumStreamInput
name|inStream
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|Translog
operator|.
name|readOperation
argument_list|(
name|inStream
argument_list|)
return|;
block|}
comment|/**      * reads bytes at position into the given buffer, filling it.      */
DECL|method|readBytes
specifier|abstract
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
function_decl|;
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"translog ["
operator|+
name|generation
operator|+
literal|"]["
operator|+
name|path
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|BaseTranslogReader
name|o
parameter_list|)
block|{
return|return
name|Long
operator|.
name|compare
argument_list|(
name|getGeneration
argument_list|()
argument_list|,
name|o
operator|.
name|getGeneration
argument_list|()
argument_list|)
return|;
block|}
DECL|method|path
specifier|public
name|Path
name|path
parameter_list|()
block|{
return|return
name|path
return|;
block|}
block|}
end_class

end_unit

