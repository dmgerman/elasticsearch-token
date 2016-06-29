begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|collect
operator|.
name|Tuple
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
name|ESTestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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
name|util
operator|.
name|ArrayList
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|StreamTests
specifier|public
class|class
name|StreamTests
extends|extends
name|ESTestCase
block|{
DECL|method|testRandomVLongSerialization
specifier|public
name|void
name|testRandomVLongSerialization
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1024
condition|;
name|i
operator|++
control|)
block|{
name|long
name|write
init|=
name|randomLong
argument_list|()
decl_stmt|;
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeZLong
argument_list|(
name|write
argument_list|)
expr_stmt|;
name|long
name|read
init|=
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
operator|.
name|readZLong
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|write
argument_list|,
name|read
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSpecificVLongSerialization
specifier|public
name|void
name|testSpecificVLongSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Tuple
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|values
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Tuple
argument_list|<>
argument_list|(
literal|0L
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|}
argument_list|)
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
operator|-
literal|1L
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
literal|1L
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|2
block|}
argument_list|)
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
operator|-
literal|2L
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|3
block|}
argument_list|)
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
literal|2L
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|4
block|}
argument_list|)
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
literal|1
block|}
argument_list|)
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|-
literal|2
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
operator|-
literal|1
block|,
literal|1
block|}
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Tuple
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
name|value
range|:
name|values
control|)
block|{
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeZLong
argument_list|(
name|value
operator|.
name|v1
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|value
operator|.
name|v1
argument_list|()
argument_list|)
argument_list|,
name|value
operator|.
name|v2
argument_list|()
argument_list|,
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|toBytes
argument_list|()
argument_list|)
expr_stmt|;
name|ByteBufferBytesReference
name|bytes
init|=
operator|new
name|ByteBufferBytesReference
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|value
operator|.
name|v2
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|toString
argument_list|(
name|value
operator|.
name|v2
argument_list|()
argument_list|)
argument_list|,
operator|(
name|long
operator|)
name|value
operator|.
name|v1
argument_list|()
argument_list|,
name|bytes
operator|.
name|streamInput
argument_list|()
operator|.
name|readZLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testLinkedHashMap
specifier|public
name|void
name|testLinkedHashMap
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|size
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1024
argument_list|)
decl_stmt|;
name|boolean
name|accessOrder
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Tuple
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|write
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|(
name|size
argument_list|,
literal|0.75f
argument_list|,
name|accessOrder
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|int
name|value
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|write
operator|.
name|put
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|accessOrder
condition|)
block|{
comment|// randomize access order
name|Collections
operator|.
name|shuffle
argument_list|(
name|list
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Tuple
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|entry
range|:
name|list
control|)
block|{
comment|// touch the entries to set the access order
name|write
operator|.
name|get
argument_list|(
name|entry
operator|.
name|v1
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeGenericValue
argument_list|(
name|write
argument_list|)
expr_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|read
init|=
operator|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
operator|)
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
operator|.
name|readGenericValue
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|size
argument_list|,
name|read
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|entry
range|:
name|read
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|v1
argument_list|()
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|v2
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
block|}
block|}
DECL|method|testFilterStreamInputDelegatesAvailable
specifier|public
name|void
name|testFilterStreamInputDelegatesAvailable
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|length
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1024
argument_list|)
decl_stmt|;
name|StreamInput
name|delegate
init|=
name|StreamInput
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[
name|length
index|]
argument_list|)
decl_stmt|;
name|FilterStreamInput
name|filterInputStream
init|=
operator|new
name|FilterStreamInput
argument_list|(
name|delegate
argument_list|)
block|{}
decl_stmt|;
name|assertEquals
argument_list|(
name|filterInputStream
operator|.
name|available
argument_list|()
argument_list|,
name|length
argument_list|)
expr_stmt|;
comment|// read some bytes
specifier|final
name|int
name|bytesToRead
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|filterInputStream
operator|.
name|readBytes
argument_list|(
operator|new
name|byte
index|[
name|bytesToRead
index|]
argument_list|,
literal|0
argument_list|,
name|bytesToRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|filterInputStream
operator|.
name|available
argument_list|()
argument_list|,
name|length
operator|-
name|bytesToRead
argument_list|)
expr_stmt|;
block|}
DECL|method|testInputStreamStreamInputDelegatesAvailable
specifier|public
name|void
name|testInputStreamStreamInputDelegatesAvailable
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|length
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1024
argument_list|)
decl_stmt|;
name|ByteArrayInputStream
name|is
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
operator|new
name|byte
index|[
name|length
index|]
argument_list|)
decl_stmt|;
name|InputStreamStreamInput
name|streamInput
init|=
operator|new
name|InputStreamStreamInput
argument_list|(
name|is
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|streamInput
operator|.
name|available
argument_list|()
argument_list|,
name|length
argument_list|)
expr_stmt|;
comment|// read some bytes
specifier|final
name|int
name|bytesToRead
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|streamInput
operator|.
name|readBytes
argument_list|(
operator|new
name|byte
index|[
name|bytesToRead
index|]
argument_list|,
literal|0
argument_list|,
name|bytesToRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|streamInput
operator|.
name|available
argument_list|()
argument_list|,
name|length
operator|-
name|bytesToRead
argument_list|)
expr_stmt|;
block|}
DECL|method|testWritableArrays
specifier|public
name|void
name|testWritableArrays
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
index|[]
name|strings
init|=
name|generateRandomStringArray
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|WriteableString
index|[]
name|sourceArray
init|=
name|Arrays
operator|.
name|stream
argument_list|(
name|strings
argument_list|)
operator|.
operator|<
name|WriteableString
operator|>
name|map
argument_list|(
name|WriteableString
operator|::
operator|new
argument_list|)
operator|.
name|toArray
argument_list|(
name|WriteableString
index|[]
operator|::
operator|new
argument_list|)
decl_stmt|;
name|WriteableString
index|[]
name|targetArray
decl_stmt|;
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|sourceArray
operator|=
literal|null
expr_stmt|;
block|}
name|out
operator|.
name|writeOptionalArray
argument_list|(
name|sourceArray
argument_list|)
expr_stmt|;
name|targetArray
operator|=
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
operator|.
name|readOptionalArray
argument_list|(
name|WriteableString
operator|::
operator|new
argument_list|,
name|WriteableString
index|[]
operator|::
operator|new
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeArray
argument_list|(
name|sourceArray
argument_list|)
expr_stmt|;
name|targetArray
operator|=
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
operator|.
name|readArray
argument_list|(
name|WriteableString
operator|::
operator|new
argument_list|,
name|WriteableString
index|[]
operator|::
operator|new
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|targetArray
argument_list|,
name|equalTo
argument_list|(
name|sourceArray
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|WriteableString
specifier|final
specifier|static
class|class
name|WriteableString
implements|implements
name|Writeable
block|{
DECL|field|string
specifier|final
name|String
name|string
decl_stmt|;
DECL|method|WriteableString
specifier|public
name|WriteableString
parameter_list|(
name|String
name|string
parameter_list|)
block|{
name|this
operator|.
name|string
operator|=
name|string
expr_stmt|;
block|}
DECL|method|WriteableString
specifier|public
name|WriteableString
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|WriteableString
name|that
init|=
operator|(
name|WriteableString
operator|)
name|o
decl_stmt|;
return|return
name|string
operator|.
name|equals
argument_list|(
name|that
operator|.
name|string
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
return|return
name|string
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|string
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

