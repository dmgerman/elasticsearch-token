begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|ByteBufferBytesReference
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
name|test
operator|.
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ByteBufferBackedChannelBuffer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ChannelBuffer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|io
operator|.
name|RandomAccessFile
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
name|MappedByteBuffer
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
name|channels
operator|.
name|FileLock
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
name|ReadableByteChannel
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
name|WritableByteChannel
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
DECL|class|ChannelsTests
specifier|public
class|class
name|ChannelsTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|field|randomBytes
name|byte
index|[]
name|randomBytes
decl_stmt|;
DECL|field|fileChannel
name|FileChannel
name|fileChannel
decl_stmt|;
annotation|@
name|Override
annotation|@
name|Before
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|Path
name|tmpFile
init|=
name|createTempFile
argument_list|()
decl_stmt|;
name|FileChannel
name|randomAccessFile
init|=
name|FileChannel
operator|.
name|open
argument_list|(
name|tmpFile
argument_list|,
name|StandardOpenOption
operator|.
name|READ
argument_list|,
name|StandardOpenOption
operator|.
name|WRITE
argument_list|)
decl_stmt|;
name|fileChannel
operator|=
operator|new
name|MockFileChannel
argument_list|(
name|randomAccessFile
argument_list|)
expr_stmt|;
name|randomBytes
operator|=
name|randomUnicodeOfLength
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100000
argument_list|)
argument_list|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|After
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|fileChannel
operator|.
name|close
argument_list|()
expr_stmt|;
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testReadWriteThoughArrays
specifier|public
name|void
name|testReadWriteThoughArrays
parameter_list|()
throws|throws
name|Exception
block|{
name|Channels
operator|.
name|writeToChannel
argument_list|(
name|randomBytes
argument_list|,
name|fileChannel
argument_list|)
expr_stmt|;
name|byte
index|[]
name|readBytes
init|=
name|Channels
operator|.
name|readFromFileChannel
argument_list|(
name|fileChannel
argument_list|,
literal|0
argument_list|,
name|randomBytes
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"read bytes didn't match written bytes"
argument_list|,
name|randomBytes
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|readBytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testPartialReadWriteThroughArrays
specifier|public
name|void
name|testPartialReadWriteThroughArrays
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|length
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|randomBytes
operator|.
name|length
operator|/
literal|2
argument_list|)
decl_stmt|;
name|int
name|offset
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|randomBytes
operator|.
name|length
operator|-
name|length
argument_list|)
decl_stmt|;
name|Channels
operator|.
name|writeToChannel
argument_list|(
name|randomBytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|fileChannel
argument_list|)
expr_stmt|;
name|int
name|lengthToRead
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|int
name|offsetToRead
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|length
operator|-
name|lengthToRead
argument_list|)
decl_stmt|;
name|byte
index|[]
name|readBytes
init|=
operator|new
name|byte
index|[
name|randomBytes
operator|.
name|length
index|]
decl_stmt|;
name|Channels
operator|.
name|readFromFileChannel
argument_list|(
name|fileChannel
argument_list|,
name|offsetToRead
argument_list|,
name|readBytes
argument_list|,
name|offset
operator|+
name|offsetToRead
argument_list|,
name|lengthToRead
argument_list|)
expr_stmt|;
name|BytesReference
name|source
init|=
operator|new
name|BytesArray
argument_list|(
name|randomBytes
argument_list|,
name|offset
operator|+
name|offsetToRead
argument_list|,
name|lengthToRead
argument_list|)
decl_stmt|;
name|BytesReference
name|read
init|=
operator|new
name|BytesArray
argument_list|(
name|readBytes
argument_list|,
name|offset
operator|+
name|offsetToRead
argument_list|,
name|lengthToRead
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"read bytes didn't match written bytes"
argument_list|,
name|source
operator|.
name|toBytes
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|read
operator|.
name|toBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|EOFException
operator|.
name|class
argument_list|)
DECL|method|testBufferReadPastEOFWithException
specifier|public
name|void
name|testBufferReadPastEOFWithException
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|bytesToWrite
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|randomBytes
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Channels
operator|.
name|writeToChannel
argument_list|(
name|randomBytes
argument_list|,
literal|0
argument_list|,
name|bytesToWrite
argument_list|,
name|fileChannel
argument_list|)
expr_stmt|;
name|Channels
operator|.
name|readFromFileChannel
argument_list|(
name|fileChannel
argument_list|,
literal|0
argument_list|,
name|bytesToWrite
operator|+
literal|1
operator|+
name|randomInt
argument_list|(
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testBufferReadPastEOFWithoutException
specifier|public
name|void
name|testBufferReadPastEOFWithoutException
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|bytesToWrite
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|randomBytes
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Channels
operator|.
name|writeToChannel
argument_list|(
name|randomBytes
argument_list|,
literal|0
argument_list|,
name|bytesToWrite
argument_list|,
name|fileChannel
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|bytesToWrite
operator|+
literal|1
operator|+
name|randomInt
argument_list|(
literal|1000
argument_list|)
index|]
decl_stmt|;
name|int
name|read
init|=
name|Channels
operator|.
name|readFromFileChannel
argument_list|(
name|fileChannel
argument_list|,
literal|0
argument_list|,
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|read
argument_list|,
name|Matchers
operator|.
name|lessThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testReadWriteThroughBuffers
specifier|public
name|void
name|testReadWriteThroughBuffers
parameter_list|()
throws|throws
name|IOException
block|{
name|ByteBuffer
name|source
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|source
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|randomBytes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|source
operator|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|randomBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
name|randomBytes
argument_list|)
expr_stmt|;
name|source
operator|.
name|flip
argument_list|()
expr_stmt|;
block|}
name|Channels
operator|.
name|writeToChannel
argument_list|(
name|source
argument_list|,
name|fileChannel
argument_list|)
expr_stmt|;
name|ByteBuffer
name|copy
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|copy
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|randomBytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|copy
operator|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|randomBytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
name|int
name|read
init|=
name|Channels
operator|.
name|readFromFileChannel
argument_list|(
name|fileChannel
argument_list|,
literal|0
argument_list|,
name|copy
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|read
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|randomBytes
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|copyBytes
init|=
operator|new
name|byte
index|[
name|read
index|]
decl_stmt|;
name|copy
operator|.
name|flip
argument_list|()
expr_stmt|;
name|copy
operator|.
name|get
argument_list|(
name|copyBytes
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"read bytes didn't match written bytes"
argument_list|,
name|randomBytes
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|copyBytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testPartialReadWriteThroughBuffers
specifier|public
name|void
name|testPartialReadWriteThroughBuffers
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|length
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|randomBytes
operator|.
name|length
operator|/
literal|2
argument_list|)
decl_stmt|;
name|int
name|offset
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|randomBytes
operator|.
name|length
operator|-
name|length
argument_list|)
decl_stmt|;
name|ByteBuffer
name|source
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|source
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|randomBytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|source
operator|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|length
argument_list|)
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
name|randomBytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|source
operator|.
name|flip
argument_list|()
expr_stmt|;
block|}
name|Channels
operator|.
name|writeToChannel
argument_list|(
name|source
argument_list|,
name|fileChannel
argument_list|)
expr_stmt|;
name|int
name|lengthToRead
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|int
name|offsetToRead
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|length
operator|-
name|lengthToRead
argument_list|)
decl_stmt|;
name|ByteBuffer
name|copy
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|copy
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|lengthToRead
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|copy
operator|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|lengthToRead
argument_list|)
expr_stmt|;
block|}
name|int
name|read
init|=
name|Channels
operator|.
name|readFromFileChannel
argument_list|(
name|fileChannel
argument_list|,
name|offsetToRead
argument_list|,
name|copy
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|read
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|lengthToRead
argument_list|)
argument_list|)
expr_stmt|;
name|copy
operator|.
name|flip
argument_list|()
expr_stmt|;
name|BytesReference
name|sourceRef
init|=
operator|new
name|BytesArray
argument_list|(
name|randomBytes
argument_list|,
name|offset
operator|+
name|offsetToRead
argument_list|,
name|lengthToRead
argument_list|)
decl_stmt|;
name|BytesReference
name|copyRef
init|=
operator|new
name|ByteBufferBytesReference
argument_list|(
name|copy
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"read bytes didn't match written bytes"
argument_list|,
name|sourceRef
operator|.
name|equals
argument_list|(
name|copyRef
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testWriteFromChannel
specifier|public
name|void
name|testWriteFromChannel
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|length
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|randomBytes
operator|.
name|length
operator|/
literal|2
argument_list|)
decl_stmt|;
name|int
name|offset
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|randomBytes
operator|.
name|length
operator|-
name|length
argument_list|)
decl_stmt|;
name|ByteBuffer
name|byteBuffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|randomBytes
argument_list|)
decl_stmt|;
name|ChannelBuffer
name|source
init|=
operator|new
name|ByteBufferBackedChannelBuffer
argument_list|(
name|byteBuffer
argument_list|)
decl_stmt|;
name|Channels
operator|.
name|writeToChannel
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|fileChannel
argument_list|)
expr_stmt|;
name|BytesReference
name|copyRef
init|=
operator|new
name|BytesArray
argument_list|(
name|Channels
operator|.
name|readFromFileChannel
argument_list|(
name|fileChannel
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
argument_list|)
decl_stmt|;
name|BytesReference
name|sourceRef
init|=
operator|new
name|BytesArray
argument_list|(
name|randomBytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"read bytes didn't match written bytes"
argument_list|,
name|sourceRef
operator|.
name|equals
argument_list|(
name|copyRef
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|MockFileChannel
class|class
name|MockFileChannel
extends|extends
name|FileChannel
block|{
DECL|field|delegate
name|FileChannel
name|delegate
decl_stmt|;
DECL|method|MockFileChannel
specifier|public
name|MockFileChannel
parameter_list|(
name|FileChannel
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|(
name|ByteBuffer
name|dst
parameter_list|)
throws|throws
name|IOException
block|{
comment|// delay buffer read..
name|int
name|willActuallyRead
init|=
name|randomInt
argument_list|(
name|dst
operator|.
name|remaining
argument_list|()
argument_list|)
decl_stmt|;
name|ByteBuffer
name|mockDst
init|=
name|dst
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|mockDst
operator|.
name|limit
argument_list|(
name|mockDst
operator|.
name|position
argument_list|()
operator|+
name|willActuallyRead
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|delegate
operator|.
name|read
argument_list|(
name|mockDst
argument_list|)
return|;
block|}
finally|finally
block|{
name|dst
operator|.
name|position
argument_list|(
name|mockDst
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|long
name|read
parameter_list|(
name|ByteBuffer
index|[]
name|dsts
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
return|return
name|delegate
operator|.
name|read
argument_list|(
name|dsts
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|write
specifier|public
name|int
name|write
parameter_list|(
name|ByteBuffer
name|src
parameter_list|)
throws|throws
name|IOException
block|{
comment|// delay buffer write..
name|int
name|willActuallyWrite
init|=
name|randomInt
argument_list|(
name|src
operator|.
name|remaining
argument_list|()
argument_list|)
decl_stmt|;
name|ByteBuffer
name|mockSrc
init|=
name|src
operator|.
name|duplicate
argument_list|()
decl_stmt|;
name|mockSrc
operator|.
name|limit
argument_list|(
name|mockSrc
operator|.
name|position
argument_list|()
operator|+
name|willActuallyWrite
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|delegate
operator|.
name|write
argument_list|(
name|mockSrc
argument_list|)
return|;
block|}
finally|finally
block|{
name|src
operator|.
name|position
argument_list|(
name|mockSrc
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|write
specifier|public
name|long
name|write
parameter_list|(
name|ByteBuffer
index|[]
name|srcs
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
return|return
name|delegate
operator|.
name|write
argument_list|(
name|srcs
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|position
specifier|public
name|long
name|position
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|position
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|position
specifier|public
name|FileChannel
name|position
parameter_list|(
name|long
name|newPosition
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|position
argument_list|(
name|newPosition
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|long
name|size
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|truncate
specifier|public
name|FileChannel
name|truncate
parameter_list|(
name|long
name|size
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|truncate
argument_list|(
name|size
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|force
specifier|public
name|void
name|force
parameter_list|(
name|boolean
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|force
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|transferTo
specifier|public
name|long
name|transferTo
parameter_list|(
name|long
name|position
parameter_list|,
name|long
name|count
parameter_list|,
name|WritableByteChannel
name|target
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|transferTo
argument_list|(
name|position
argument_list|,
name|count
argument_list|,
name|target
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|transferFrom
specifier|public
name|long
name|transferFrom
parameter_list|(
name|ReadableByteChannel
name|src
parameter_list|,
name|long
name|position
parameter_list|,
name|long
name|count
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|transferFrom
argument_list|(
name|src
argument_list|,
name|position
argument_list|,
name|count
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|(
name|ByteBuffer
name|dst
parameter_list|,
name|long
name|position
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|read
argument_list|(
name|dst
argument_list|,
name|position
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|write
specifier|public
name|int
name|write
parameter_list|(
name|ByteBuffer
name|src
parameter_list|,
name|long
name|position
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|write
argument_list|(
name|src
argument_list|,
name|position
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|map
specifier|public
name|MappedByteBuffer
name|map
parameter_list|(
name|MapMode
name|mode
parameter_list|,
name|long
name|position
parameter_list|,
name|long
name|size
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|map
argument_list|(
name|mode
argument_list|,
name|position
argument_list|,
name|size
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|lock
specifier|public
name|FileLock
name|lock
parameter_list|(
name|long
name|position
parameter_list|,
name|long
name|size
parameter_list|,
name|boolean
name|shared
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|lock
argument_list|(
name|position
argument_list|,
name|size
argument_list|,
name|shared
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|tryLock
specifier|public
name|FileLock
name|tryLock
parameter_list|(
name|long
name|position
parameter_list|,
name|long
name|size
parameter_list|,
name|boolean
name|shared
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|tryLock
argument_list|(
name|position
argument_list|,
name|size
argument_list|,
name|shared
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|implCloseChannel
specifier|protected
name|void
name|implCloseChannel
parameter_list|()
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
