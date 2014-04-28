begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.plain
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|plain
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
name|index
operator|.
name|AtomicReader
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
name|BinaryDocValues
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
name|DocValues
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
name|ByteArrayDataInput
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
name|ArrayUtil
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
name|util
operator|.
name|ByteUtils
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
name|fielddata
operator|.
name|AbstractAtomicNumericFieldData
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
name|fielddata
operator|.
name|DoubleValues
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
name|fielddata
operator|.
name|IndexNumericFieldData
operator|.
name|NumericType
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
name|fielddata
operator|.
name|LongValues
import|;
end_import

begin_class
DECL|class|BinaryDVNumericAtomicFieldData
specifier|final
class|class
name|BinaryDVNumericAtomicFieldData
extends|extends
name|AbstractAtomicNumericFieldData
block|{
DECL|field|reader
specifier|private
specifier|final
name|AtomicReader
name|reader
decl_stmt|;
DECL|field|values
specifier|private
specifier|final
name|BinaryDocValues
name|values
decl_stmt|;
DECL|field|numericType
specifier|private
specifier|final
name|NumericType
name|numericType
decl_stmt|;
DECL|method|BinaryDVNumericAtomicFieldData
name|BinaryDVNumericAtomicFieldData
parameter_list|(
name|AtomicReader
name|reader
parameter_list|,
name|BinaryDocValues
name|values
parameter_list|,
name|NumericType
name|numericType
parameter_list|)
block|{
name|super
argument_list|(
name|numericType
operator|.
name|isFloatingPoint
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
operator|==
literal|null
condition|?
name|DocValues
operator|.
name|EMPTY_BINARY
else|:
name|values
expr_stmt|;
name|this
operator|.
name|numericType
operator|=
name|numericType
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLongValues
specifier|public
name|LongValues
name|getLongValues
parameter_list|()
block|{
if|if
condition|(
name|numericType
operator|.
name|isFloatingPoint
argument_list|()
condition|)
block|{
return|return
name|LongValues
operator|.
name|asLongValues
argument_list|(
name|getDoubleValues
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|new
name|LongValues
argument_list|(
literal|true
argument_list|)
block|{
specifier|final
name|BytesRef
name|bytes
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
specifier|final
name|ByteArrayDataInput
name|in
init|=
operator|new
name|ByteArrayDataInput
argument_list|()
decl_stmt|;
name|long
index|[]
name|longs
init|=
operator|new
name|long
index|[
literal|8
index|]
decl_stmt|;
name|int
name|i
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
name|int
name|valueCount
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|in
operator|.
name|reset
argument_list|(
name|bytes
operator|.
name|bytes
argument_list|,
name|bytes
operator|.
name|offset
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|in
operator|.
name|eof
argument_list|()
condition|)
block|{
comment|// first value uses vLong on top of zig-zag encoding, then deltas are encoded using vLong
name|long
name|previousValue
init|=
name|longs
index|[
literal|0
index|]
operator|=
name|ByteUtils
operator|.
name|zigZagDecode
argument_list|(
name|ByteUtils
operator|.
name|readVLong
argument_list|(
name|in
argument_list|)
argument_list|)
decl_stmt|;
name|valueCount
operator|=
literal|1
expr_stmt|;
while|while
condition|(
operator|!
name|in
operator|.
name|eof
argument_list|()
condition|)
block|{
name|longs
operator|=
name|ArrayUtil
operator|.
name|grow
argument_list|(
name|longs
argument_list|,
name|valueCount
operator|+
literal|1
argument_list|)
expr_stmt|;
name|previousValue
operator|=
name|longs
index|[
name|valueCount
operator|++
index|]
operator|=
name|previousValue
operator|+
name|ByteUtils
operator|.
name|readVLong
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|valueCount
operator|=
literal|0
expr_stmt|;
block|}
name|i
operator|=
literal|0
expr_stmt|;
return|return
name|valueCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|nextValue
parameter_list|()
block|{
assert|assert
name|i
operator|<
name|valueCount
assert|;
return|return
name|longs
index|[
name|i
operator|++
index|]
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|getDoubleValues
specifier|public
name|DoubleValues
name|getDoubleValues
parameter_list|()
block|{
if|if
condition|(
operator|!
name|numericType
operator|.
name|isFloatingPoint
argument_list|()
condition|)
block|{
return|return
name|DoubleValues
operator|.
name|asDoubleValues
argument_list|(
name|getLongValues
argument_list|()
argument_list|)
return|;
block|}
switch|switch
condition|(
name|numericType
condition|)
block|{
case|case
name|FLOAT
case|:
return|return
operator|new
name|DoubleValues
argument_list|(
literal|true
argument_list|)
block|{
specifier|final
name|BytesRef
name|bytes
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|int
name|i
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
name|int
name|valueCount
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
assert|assert
name|bytes
operator|.
name|length
operator|%
literal|4
operator|==
literal|0
assert|;
name|i
operator|=
literal|0
expr_stmt|;
return|return
name|valueCount
operator|=
name|bytes
operator|.
name|length
operator|/
literal|4
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|nextValue
parameter_list|()
block|{
assert|assert
name|i
operator|<
name|valueCount
assert|;
return|return
name|ByteUtils
operator|.
name|readFloatLE
argument_list|(
name|bytes
operator|.
name|bytes
argument_list|,
name|bytes
operator|.
name|offset
operator|+
name|i
operator|++
operator|*
literal|4
argument_list|)
return|;
block|}
block|}
return|;
case|case
name|DOUBLE
case|:
return|return
operator|new
name|DoubleValues
argument_list|(
literal|true
argument_list|)
block|{
specifier|final
name|BytesRef
name|bytes
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|int
name|i
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
name|int
name|valueCount
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
assert|assert
name|bytes
operator|.
name|length
operator|%
literal|8
operator|==
literal|0
assert|;
name|i
operator|=
literal|0
expr_stmt|;
return|return
name|valueCount
operator|=
name|bytes
operator|.
name|length
operator|/
literal|8
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|nextValue
parameter_list|()
block|{
assert|assert
name|i
operator|<
name|valueCount
assert|;
return|return
name|ByteUtils
operator|.
name|readDoubleLE
argument_list|(
name|bytes
operator|.
name|bytes
argument_list|,
name|bytes
operator|.
name|offset
operator|+
name|i
operator|++
operator|*
literal|8
argument_list|)
return|;
block|}
block|}
return|;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|isMultiValued
specifier|public
name|boolean
name|isMultiValued
parameter_list|()
block|{
return|return
literal|true
return|;
comment|// no way to know
block|}
annotation|@
name|Override
DECL|method|getNumberUniqueValues
specifier|public
name|long
name|getNumberUniqueValues
parameter_list|()
block|{
return|return
name|Long
operator|.
name|MAX_VALUE
return|;
comment|// no clue
block|}
annotation|@
name|Override
DECL|method|getMemorySizeInBytes
specifier|public
name|long
name|getMemorySizeInBytes
parameter_list|()
block|{
return|return
operator|-
literal|1
return|;
comment|// Lucene doesn't expose it
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// no-op
block|}
block|}
end_class

end_unit

