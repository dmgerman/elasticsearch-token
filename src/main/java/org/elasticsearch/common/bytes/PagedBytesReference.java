begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.bytes
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|bytes
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|CharsRef
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
name|UnicodeUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|StreamInput
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
name|BigArrays
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
name|ByteArray
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
name|channels
operator|.
name|GatheringByteChannel
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * A page based bytes reference, internally holding the bytes in a paged  * data structure.  */
end_comment

begin_class
DECL|class|PagedBytesReference
specifier|public
class|class
name|PagedBytesReference
implements|implements
name|BytesReference
block|{
DECL|field|PAGE_SIZE
specifier|private
specifier|static
specifier|final
name|int
name|PAGE_SIZE
init|=
name|BigArrays
operator|.
name|BYTE_PAGE_SIZE
decl_stmt|;
DECL|field|NIO_GATHERING_LIMIT
specifier|private
specifier|static
specifier|final
name|int
name|NIO_GATHERING_LIMIT
init|=
literal|524288
decl_stmt|;
DECL|field|bigarrays
specifier|private
specifier|final
name|BigArrays
name|bigarrays
decl_stmt|;
DECL|field|bytearray
specifier|protected
specifier|final
name|ByteArray
name|bytearray
decl_stmt|;
DECL|field|offset
specifier|private
specifier|final
name|int
name|offset
decl_stmt|;
DECL|field|length
specifier|private
specifier|final
name|int
name|length
decl_stmt|;
DECL|field|hash
specifier|private
name|int
name|hash
init|=
literal|0
decl_stmt|;
DECL|method|PagedBytesReference
specifier|public
name|PagedBytesReference
parameter_list|(
name|BigArrays
name|bigarrays
parameter_list|,
name|ByteArray
name|bytearray
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
argument_list|(
name|bigarrays
argument_list|,
name|bytearray
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|PagedBytesReference
specifier|public
name|PagedBytesReference
parameter_list|(
name|BigArrays
name|bigarrays
parameter_list|,
name|ByteArray
name|bytearray
parameter_list|,
name|int
name|from
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|bigarrays
operator|=
name|bigarrays
expr_stmt|;
name|this
operator|.
name|bytearray
operator|=
name|bytearray
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|from
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|byte
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|bytearray
operator|.
name|get
argument_list|(
name|offset
operator|+
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|length
specifier|public
name|int
name|length
parameter_list|()
block|{
return|return
name|length
return|;
block|}
annotation|@
name|Override
DECL|method|slice
specifier|public
name|BytesReference
name|slice
parameter_list|(
name|int
name|from
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|from
argument_list|<
literal|0
operator|||
operator|(
name|from
operator|+
name|length
operator|)
argument_list|>
name|length
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"can't slice a buffer with length ["
operator|+
name|length
argument_list|()
operator|+
literal|"], with slice parameters from ["
operator|+
name|from
operator|+
literal|"], length ["
operator|+
name|length
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
operator|new
name|PagedBytesReference
argument_list|(
name|bigarrays
argument_list|,
name|bytearray
argument_list|,
name|offset
operator|+
name|from
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|streamInput
specifier|public
name|StreamInput
name|streamInput
parameter_list|()
block|{
return|return
operator|new
name|PagedBytesReferenceStreamInput
argument_list|(
name|bytearray
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
comment|// nothing to do
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|int
name|written
init|=
literal|0
decl_stmt|;
comment|// are we a slice?
if|if
condition|(
name|offset
operator|!=
literal|0
condition|)
block|{
comment|// remaining size of page fragment at offset
name|int
name|fragmentSize
init|=
name|Math
operator|.
name|min
argument_list|(
name|length
argument_list|,
name|PAGE_SIZE
operator|-
operator|(
name|offset
operator|%
name|PAGE_SIZE
operator|)
argument_list|)
decl_stmt|;
name|bytearray
operator|.
name|get
argument_list|(
name|offset
argument_list|,
name|fragmentSize
argument_list|,
name|ref
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|ref
operator|.
name|bytes
argument_list|,
name|ref
operator|.
name|offset
argument_list|,
name|fragmentSize
argument_list|)
expr_stmt|;
name|written
operator|+=
name|fragmentSize
expr_stmt|;
block|}
comment|// handle remainder of pages + trailing fragment
while|while
condition|(
name|written
operator|<
name|length
condition|)
block|{
name|int
name|remaining
init|=
name|length
operator|-
name|written
decl_stmt|;
name|int
name|bulkSize
init|=
operator|(
name|remaining
operator|>
name|PAGE_SIZE
operator|)
condition|?
name|PAGE_SIZE
else|:
name|remaining
decl_stmt|;
name|bytearray
operator|.
name|get
argument_list|(
name|offset
operator|+
name|written
argument_list|,
name|bulkSize
argument_list|,
name|ref
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|ref
operator|.
name|bytes
argument_list|,
name|ref
operator|.
name|offset
argument_list|,
name|bulkSize
argument_list|)
expr_stmt|;
name|written
operator|+=
name|bulkSize
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|GatheringByteChannel
name|channel
parameter_list|)
throws|throws
name|IOException
block|{
comment|// nothing to do
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|int
name|currentLength
init|=
name|length
decl_stmt|;
name|int
name|currentOffset
init|=
name|offset
decl_stmt|;
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
while|while
condition|(
name|currentLength
operator|>
literal|0
condition|)
block|{
comment|// try to align to the underlying pages while writing, so no new arrays will be created.
name|int
name|fragmentSize
init|=
name|Math
operator|.
name|min
argument_list|(
name|currentLength
argument_list|,
name|PAGE_SIZE
operator|-
operator|(
name|currentOffset
operator|%
name|PAGE_SIZE
operator|)
argument_list|)
decl_stmt|;
name|boolean
name|newArray
init|=
name|bytearray
operator|.
name|get
argument_list|(
name|currentOffset
argument_list|,
name|fragmentSize
argument_list|,
name|ref
argument_list|)
decl_stmt|;
assert|assert
operator|!
name|newArray
operator|:
literal|"PagedBytesReference failed to align with underlying bytearray. offset ["
operator|+
name|currentOffset
operator|+
literal|"], size ["
operator|+
name|fragmentSize
operator|+
literal|"]"
assert|;
name|Channels
operator|.
name|writeToChannel
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
argument_list|,
name|channel
argument_list|)
expr_stmt|;
name|currentLength
operator|-=
name|ref
operator|.
name|length
expr_stmt|;
name|currentOffset
operator|+=
name|ref
operator|.
name|length
expr_stmt|;
block|}
assert|assert
name|currentLength
operator|==
literal|0
assert|;
block|}
annotation|@
name|Override
DECL|method|toBytes
specifier|public
name|byte
index|[]
name|toBytes
parameter_list|()
block|{
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|BytesRef
operator|.
name|EMPTY_BYTES
return|;
block|}
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|bytearray
operator|.
name|get
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|ref
argument_list|)
expr_stmt|;
comment|// undo the single-page optimization by ByteArray.get(), otherwise
comment|// a materialized stream will contain traling garbage/zeros
name|byte
index|[]
name|result
init|=
name|ref
operator|.
name|bytes
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|length
operator|!=
name|length
operator|||
name|ref
operator|.
name|offset
operator|!=
literal|0
condition|)
block|{
name|result
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|result
argument_list|,
name|ref
operator|.
name|offset
argument_list|,
name|ref
operator|.
name|offset
operator|+
name|length
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|toBytesArray
specifier|public
name|BytesArray
name|toBytesArray
parameter_list|()
block|{
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|bytearray
operator|.
name|get
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|ref
argument_list|)
expr_stmt|;
return|return
operator|new
name|BytesArray
argument_list|(
name|ref
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|copyBytesArray
specifier|public
name|BytesArray
name|copyBytesArray
parameter_list|()
block|{
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|boolean
name|copied
init|=
name|bytearray
operator|.
name|get
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|ref
argument_list|)
decl_stmt|;
if|if
condition|(
name|copied
condition|)
block|{
comment|// BigArray has materialized for us, no need to do it again
return|return
operator|new
name|BytesArray
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
return|;
block|}
else|else
block|{
comment|// here we need to copy the bytes even when shared
name|byte
index|[]
name|copy
init|=
name|Arrays
operator|.
name|copyOfRange
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
name|offset
operator|+
name|ref
operator|.
name|length
argument_list|)
decl_stmt|;
return|return
operator|new
name|BytesArray
argument_list|(
name|copy
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|toChannelBuffer
specifier|public
name|ChannelBuffer
name|toChannelBuffer
parameter_list|()
block|{
comment|// nothing to do
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|ChannelBuffers
operator|.
name|EMPTY_BUFFER
return|;
block|}
name|ChannelBuffer
index|[]
name|buffers
decl_stmt|;
name|ChannelBuffer
name|currentBuffer
init|=
literal|null
decl_stmt|;
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
comment|// are we a slice?
if|if
condition|(
name|offset
operator|!=
literal|0
condition|)
block|{
comment|// remaining size of page fragment at offset
name|int
name|fragmentSize
init|=
name|Math
operator|.
name|min
argument_list|(
name|length
argument_list|,
name|PAGE_SIZE
operator|-
operator|(
name|offset
operator|%
name|PAGE_SIZE
operator|)
argument_list|)
decl_stmt|;
name|bytearray
operator|.
name|get
argument_list|(
name|offset
argument_list|,
name|fragmentSize
argument_list|,
name|ref
argument_list|)
expr_stmt|;
name|currentBuffer
operator|=
name|ChannelBuffers
operator|.
name|wrappedBuffer
argument_list|(
name|ref
operator|.
name|bytes
argument_list|,
name|ref
operator|.
name|offset
argument_list|,
name|fragmentSize
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|fragmentSize
expr_stmt|;
block|}
comment|// no need to create a composite buffer for a single page
if|if
condition|(
name|pos
operator|==
name|length
operator|&&
name|currentBuffer
operator|!=
literal|null
condition|)
block|{
return|return
name|currentBuffer
return|;
block|}
comment|// a slice> pagesize will likely require extra buffers for initial/trailing fragments
name|int
name|numBuffers
init|=
name|countRequiredBuffers
argument_list|(
operator|(
name|currentBuffer
operator|!=
literal|null
condition|?
literal|1
else|:
literal|0
operator|)
argument_list|,
name|length
operator|-
name|pos
argument_list|)
decl_stmt|;
name|buffers
operator|=
operator|new
name|ChannelBuffer
index|[
name|numBuffers
index|]
expr_stmt|;
name|int
name|bufferSlot
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|currentBuffer
operator|!=
literal|null
condition|)
block|{
name|buffers
index|[
name|bufferSlot
index|]
operator|=
name|currentBuffer
expr_stmt|;
name|bufferSlot
operator|++
expr_stmt|;
block|}
comment|// handle remainder of pages + trailing fragment
while|while
condition|(
name|pos
operator|<
name|length
condition|)
block|{
name|int
name|remaining
init|=
name|length
operator|-
name|pos
decl_stmt|;
name|int
name|bulkSize
init|=
operator|(
name|remaining
operator|>
name|PAGE_SIZE
operator|)
condition|?
name|PAGE_SIZE
else|:
name|remaining
decl_stmt|;
name|bytearray
operator|.
name|get
argument_list|(
name|offset
operator|+
name|pos
argument_list|,
name|bulkSize
argument_list|,
name|ref
argument_list|)
expr_stmt|;
name|currentBuffer
operator|=
name|ChannelBuffers
operator|.
name|wrappedBuffer
argument_list|(
name|ref
operator|.
name|bytes
argument_list|,
name|ref
operator|.
name|offset
argument_list|,
name|bulkSize
argument_list|)
expr_stmt|;
name|buffers
index|[
name|bufferSlot
index|]
operator|=
name|currentBuffer
expr_stmt|;
name|bufferSlot
operator|++
expr_stmt|;
name|pos
operator|+=
name|bulkSize
expr_stmt|;
block|}
comment|// this would indicate that our numBuffer calculation is off by one.
assert|assert
operator|(
name|numBuffers
operator|==
name|bufferSlot
operator|)
assert|;
comment|// we can use gathering writes from the ChannelBuffers, but only if they are
comment|// moderately small to prevent OOMs due to DirectBuffer allocations.
return|return
name|ChannelBuffers
operator|.
name|wrappedBuffer
argument_list|(
name|length
operator|<=
name|NIO_GATHERING_LIMIT
argument_list|,
name|buffers
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hasArray
specifier|public
name|boolean
name|hasArray
parameter_list|()
block|{
return|return
operator|(
name|offset
operator|+
name|length
operator|<=
name|PAGE_SIZE
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|array
specifier|public
name|byte
index|[]
name|array
parameter_list|()
block|{
if|if
condition|(
name|hasArray
argument_list|()
condition|)
block|{
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|BytesRef
operator|.
name|EMPTY_BYTES
return|;
block|}
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|bytearray
operator|.
name|get
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|ref
argument_list|)
expr_stmt|;
return|return
name|ref
operator|.
name|bytes
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"array not available"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|arrayOffset
specifier|public
name|int
name|arrayOffset
parameter_list|()
block|{
if|if
condition|(
name|hasArray
argument_list|()
condition|)
block|{
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|bytearray
operator|.
name|get
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|ref
argument_list|)
expr_stmt|;
return|return
name|ref
operator|.
name|offset
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"array not available"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|toUtf8
specifier|public
name|String
name|toUtf8
parameter_list|()
block|{
if|if
condition|(
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|""
return|;
block|}
name|byte
index|[]
name|bytes
init|=
name|toBytes
argument_list|()
decl_stmt|;
specifier|final
name|CharsRef
name|ref
init|=
operator|new
name|CharsRef
argument_list|(
name|length
argument_list|)
decl_stmt|;
name|UnicodeUtil
operator|.
name|UTF8toUTF16
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|ref
argument_list|)
expr_stmt|;
return|return
name|ref
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toBytesRef
specifier|public
name|BytesRef
name|toBytesRef
parameter_list|()
block|{
name|BytesRef
name|bref
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
comment|// if length<= pagesize this will dereference the page, or materialize the byte[]
name|bytearray
operator|.
name|get
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|bref
argument_list|)
expr_stmt|;
return|return
name|bref
return|;
block|}
annotation|@
name|Override
DECL|method|copyBytesRef
specifier|public
name|BytesRef
name|copyBytesRef
parameter_list|()
block|{
name|byte
index|[]
name|bytes
init|=
name|toBytes
argument_list|()
decl_stmt|;
return|return
operator|new
name|BytesRef
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
if|if
condition|(
name|hash
operator|==
literal|0
condition|)
block|{
comment|// TODO: delegate to BigArrays via:
comment|// hash = bigarrays.hashCode(bytearray);
comment|// and for slices:
comment|// hash = bigarrays.hashCode(bytearray, offset, length);
name|int
name|tmphash
init|=
literal|1
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
name|tmphash
operator|=
literal|31
operator|*
name|tmphash
operator|+
name|bytearray
operator|.
name|get
argument_list|(
name|offset
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|hash
operator|=
name|tmphash
expr_stmt|;
block|}
return|return
name|hash
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|PagedBytesReference
operator|)
condition|)
block|{
return|return
name|BytesReference
operator|.
name|Helper
operator|.
name|bytesEqual
argument_list|(
name|this
argument_list|,
operator|(
name|BytesReference
operator|)
name|obj
argument_list|)
return|;
block|}
name|PagedBytesReference
name|other
init|=
operator|(
name|PagedBytesReference
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|length
operator|!=
name|other
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// TODO: delegate to BigArrays via:
comment|// return bigarrays.equals(bytearray, other.bytearray);
comment|// and for slices:
comment|// return bigarrays.equals(bytearray, start, other.bytearray, otherstart, len);
name|ByteArray
name|otherArray
init|=
name|other
operator|.
name|bytearray
decl_stmt|;
name|int
name|otherOffset
init|=
name|other
operator|.
name|offset
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
if|if
condition|(
name|bytearray
operator|.
name|get
argument_list|(
name|offset
operator|+
name|i
argument_list|)
operator|!=
name|otherArray
operator|.
name|get
argument_list|(
name|otherOffset
operator|+
name|i
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
DECL|method|countRequiredBuffers
specifier|private
name|int
name|countRequiredBuffers
parameter_list|(
name|int
name|initialCount
parameter_list|,
name|int
name|numBytes
parameter_list|)
block|{
name|int
name|numBuffers
init|=
name|initialCount
decl_stmt|;
comment|// an "estimate" of how many pages remain - rounded down
name|int
name|pages
init|=
name|numBytes
operator|/
name|PAGE_SIZE
decl_stmt|;
comment|// a remaining fragment< pagesize needs at least one buffer
name|numBuffers
operator|+=
operator|(
name|pages
operator|==
literal|0
operator|)
condition|?
literal|1
else|:
name|pages
expr_stmt|;
comment|// a remainder that is not a multiple of pagesize also needs an extra buffer
name|numBuffers
operator|+=
operator|(
name|pages
operator|>
literal|0
operator|&&
name|numBytes
operator|%
name|PAGE_SIZE
operator|>
literal|0
operator|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
return|return
name|numBuffers
return|;
block|}
DECL|class|PagedBytesReferenceStreamInput
specifier|private
specifier|static
class|class
name|PagedBytesReferenceStreamInput
extends|extends
name|StreamInput
block|{
DECL|field|bytearray
specifier|private
specifier|final
name|ByteArray
name|bytearray
decl_stmt|;
DECL|field|ref
specifier|private
specifier|final
name|BytesRef
name|ref
decl_stmt|;
DECL|field|offset
specifier|private
specifier|final
name|int
name|offset
decl_stmt|;
DECL|field|length
specifier|private
specifier|final
name|int
name|length
decl_stmt|;
DECL|field|pos
specifier|private
name|int
name|pos
decl_stmt|;
DECL|method|PagedBytesReferenceStreamInput
specifier|public
name|PagedBytesReferenceStreamInput
parameter_list|(
name|ByteArray
name|bytearray
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|bytearray
operator|=
name|bytearray
expr_stmt|;
name|this
operator|.
name|ref
operator|=
operator|new
name|BytesRef
argument_list|()
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|pos
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|offset
operator|+
name|length
operator|>
name|bytearray
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|(
literal|"offset+length>= bytearray.size()"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|readByte
specifier|public
name|byte
name|readByte
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|pos
operator|>=
name|length
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
block|}
return|return
name|bytearray
operator|.
name|get
argument_list|(
name|offset
operator|+
name|pos
operator|++
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readBytes
specifier|public
name|void
name|readBytes
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|bOffset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|len
operator|>
name|offset
operator|+
name|length
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|(
literal|"Cannot read "
operator|+
name|len
operator|+
literal|" bytes from stream with length "
operator|+
name|length
operator|+
literal|" at pos "
operator|+
name|pos
argument_list|)
throw|;
block|}
name|read
argument_list|(
name|b
argument_list|,
name|bOffset
argument_list|,
name|len
argument_list|)
expr_stmt|;
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
return|return
operator|(
name|pos
operator|<
name|length
operator|)
condition|?
name|bytearray
operator|.
name|get
argument_list|(
name|offset
operator|+
name|pos
operator|++
argument_list|)
else|:
operator|-
literal|1
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
name|b
parameter_list|,
specifier|final
name|int
name|bOffset
parameter_list|,
specifier|final
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|len
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
name|pos
operator|>=
name|offset
operator|+
name|length
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
specifier|final
name|int
name|numBytesToCopy
init|=
name|Math
operator|.
name|min
argument_list|(
name|len
argument_list|,
name|length
operator|-
name|pos
argument_list|)
decl_stmt|;
comment|// copy the full lenth or the remaining part
comment|// current offset into the underlying ByteArray
name|long
name|byteArrayOffset
init|=
name|offset
operator|+
name|pos
decl_stmt|;
comment|// bytes already copied
name|int
name|copiedBytes
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|copiedBytes
operator|<
name|numBytesToCopy
condition|)
block|{
name|long
name|pageFragment
init|=
name|PAGE_SIZE
operator|-
operator|(
name|byteArrayOffset
operator|%
name|PAGE_SIZE
operator|)
decl_stmt|;
comment|// how much can we read until hitting N*PAGE_SIZE?
name|int
name|bulkSize
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|pageFragment
argument_list|,
name|numBytesToCopy
operator|-
name|copiedBytes
argument_list|)
decl_stmt|;
comment|// we cannot copy more than a page fragment
name|boolean
name|copied
init|=
name|bytearray
operator|.
name|get
argument_list|(
name|byteArrayOffset
argument_list|,
name|bulkSize
argument_list|,
name|ref
argument_list|)
decl_stmt|;
comment|// get the fragment
assert|assert
operator|(
name|copied
operator|==
literal|false
operator|)
assert|;
comment|// we should never ever get back a materialized byte[]
name|System
operator|.
name|arraycopy
argument_list|(
name|ref
operator|.
name|bytes
argument_list|,
name|ref
operator|.
name|offset
argument_list|,
name|b
argument_list|,
name|bOffset
operator|+
name|copiedBytes
argument_list|,
name|bulkSize
argument_list|)
expr_stmt|;
comment|// copy fragment contents
name|copiedBytes
operator|+=
name|bulkSize
expr_stmt|;
comment|// count how much we copied
name|byteArrayOffset
operator|+=
name|bulkSize
expr_stmt|;
comment|// advance ByteArray index
block|}
name|pos
operator|+=
name|copiedBytes
expr_stmt|;
comment|// finally advance our stream position
return|return
name|copiedBytes
return|;
block|}
annotation|@
name|Override
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|pos
operator|=
literal|0
expr_stmt|;
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
comment|// do nothing
block|}
block|}
block|}
end_class

end_unit

