begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty3
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty3
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
name|bytes
operator|.
name|AbstractBytesReferenceTestCase
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
name|stream
operator|.
name|ReleasableBytesStreamOutput
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
name|jboss
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ChannelBuffers
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

begin_class
DECL|class|ChannelBufferBytesReferenceTests
specifier|public
class|class
name|ChannelBufferBytesReferenceTests
extends|extends
name|AbstractBytesReferenceTestCase
block|{
annotation|@
name|Override
DECL|method|newBytesReference
specifier|protected
name|BytesReference
name|newBytesReference
parameter_list|(
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|ReleasableBytesStreamOutput
name|out
init|=
operator|new
name|ReleasableBytesStreamOutput
argument_list|(
name|length
argument_list|,
name|bigarrays
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|1
operator|<<
literal|8
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|out
operator|.
name|size
argument_list|()
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|BytesReference
name|ref
init|=
name|out
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|ref
operator|.
name|length
argument_list|()
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|BytesRef
name|bytesRef
init|=
name|ref
operator|.
name|toBytesRef
argument_list|()
decl_stmt|;
specifier|final
name|ChannelBuffer
name|channelBuffer
init|=
name|ChannelBuffers
operator|.
name|wrappedBuffer
argument_list|(
name|bytesRef
operator|.
name|bytes
argument_list|,
name|bytesRef
operator|.
name|offset
argument_list|,
name|bytesRef
operator|.
name|length
argument_list|)
decl_stmt|;
return|return
name|Netty3Utils
operator|.
name|toBytesReference
argument_list|(
name|channelBuffer
argument_list|)
return|;
block|}
DECL|method|testSliceOnAdvancedBuffer
specifier|public
name|void
name|testSliceOnAdvancedBuffer
parameter_list|()
throws|throws
name|IOException
block|{
name|BytesReference
name|bytesReference
init|=
name|newBytesReference
argument_list|(
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|3
operator|*
name|PAGE_SIZE
argument_list|)
argument_list|)
decl_stmt|;
name|BytesRef
name|bytesRef
init|=
name|bytesReference
operator|.
name|toBytesRef
argument_list|()
decl_stmt|;
name|ChannelBuffer
name|channelBuffer
init|=
name|ChannelBuffers
operator|.
name|wrappedBuffer
argument_list|(
name|bytesRef
operator|.
name|bytes
argument_list|,
name|bytesRef
operator|.
name|offset
argument_list|,
name|bytesRef
operator|.
name|length
argument_list|)
decl_stmt|;
name|int
name|numBytesToRead
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numBytesToRead
condition|;
name|i
operator|++
control|)
block|{
name|channelBuffer
operator|.
name|readByte
argument_list|()
expr_stmt|;
block|}
name|BytesReference
name|other
init|=
name|Netty3Utils
operator|.
name|toBytesReference
argument_list|(
name|channelBuffer
argument_list|)
decl_stmt|;
name|BytesReference
name|slice
init|=
name|bytesReference
operator|.
name|slice
argument_list|(
name|numBytesToRead
argument_list|,
name|bytesReference
operator|.
name|length
argument_list|()
operator|-
name|numBytesToRead
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|other
argument_list|,
name|slice
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|other
operator|.
name|slice
argument_list|(
literal|3
argument_list|,
literal|1
argument_list|)
argument_list|,
name|slice
operator|.
name|slice
argument_list|(
literal|3
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testImmutable
specifier|public
name|void
name|testImmutable
parameter_list|()
throws|throws
name|IOException
block|{
name|BytesReference
name|bytesReference
init|=
name|newBytesReference
argument_list|(
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|3
operator|*
name|PAGE_SIZE
argument_list|)
argument_list|)
decl_stmt|;
name|BytesRef
name|bytesRef
init|=
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|bytesReference
operator|.
name|toBytesRef
argument_list|()
argument_list|)
decl_stmt|;
name|ChannelBuffer
name|channelBuffer
init|=
name|ChannelBuffers
operator|.
name|wrappedBuffer
argument_list|(
name|bytesRef
operator|.
name|bytes
argument_list|,
name|bytesRef
operator|.
name|offset
argument_list|,
name|bytesRef
operator|.
name|length
argument_list|)
decl_stmt|;
name|ChannelBufferBytesReference
name|channelBufferBytesReference
init|=
operator|new
name|ChannelBufferBytesReference
argument_list|(
name|channelBuffer
argument_list|,
name|bytesRef
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|channelBufferBytesReference
argument_list|,
name|bytesReference
argument_list|)
expr_stmt|;
name|channelBuffer
operator|.
name|readInt
argument_list|()
expr_stmt|;
comment|// this advances the index of the channel buffer
name|assertEquals
argument_list|(
name|channelBufferBytesReference
argument_list|,
name|bytesReference
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|bytesRef
argument_list|,
name|channelBufferBytesReference
operator|.
name|toBytesRef
argument_list|()
argument_list|)
expr_stmt|;
name|BytesRef
name|unicodeBytes
init|=
operator|new
name|BytesRef
argument_list|(
name|randomUnicodeOfCodepointLength
argument_list|(
literal|100
argument_list|)
argument_list|)
decl_stmt|;
name|channelBuffer
operator|=
name|ChannelBuffers
operator|.
name|wrappedBuffer
argument_list|(
name|unicodeBytes
operator|.
name|bytes
argument_list|,
name|unicodeBytes
operator|.
name|offset
argument_list|,
name|unicodeBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|channelBufferBytesReference
operator|=
operator|new
name|ChannelBufferBytesReference
argument_list|(
name|channelBuffer
argument_list|,
name|unicodeBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|String
name|utf8ToString
init|=
name|channelBufferBytesReference
operator|.
name|utf8ToString
argument_list|()
decl_stmt|;
name|channelBuffer
operator|.
name|readInt
argument_list|()
expr_stmt|;
comment|// this advances the index of the channel buffer
name|assertEquals
argument_list|(
name|utf8ToString
argument_list|,
name|channelBufferBytesReference
operator|.
name|utf8ToString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

