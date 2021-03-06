begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|store
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
name|IndexInput
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|containsString
import|;
end_import

begin_class
DECL|class|ByteArrayIndexInputTests
specifier|public
class|class
name|ByteArrayIndexInputTests
extends|extends
name|ESTestCase
block|{
DECL|method|testRandomReads
specifier|public
name|void
name|testRandomReads
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|input
init|=
name|randomUnicodeOfLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1000
argument_list|)
argument_list|)
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|ByteArrayIndexInput
name|indexInput
init|=
operator|new
name|ByteArrayIndexInput
argument_list|(
literal|"test"
argument_list|,
name|input
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|input
operator|.
name|length
argument_list|,
name|indexInput
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|indexInput
operator|.
name|getFilePointer
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|output
init|=
name|randomReadAndSlice
argument_list|(
name|indexInput
argument_list|,
name|input
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|input
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRandomOverflow
specifier|public
name|void
name|testRandomOverflow
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|input
init|=
name|randomUnicodeOfLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1000
argument_list|)
argument_list|)
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|ByteArrayIndexInput
name|indexInput
init|=
operator|new
name|ByteArrayIndexInput
argument_list|(
literal|"test"
argument_list|,
name|input
argument_list|)
decl_stmt|;
name|int
name|firstReadLen
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|input
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|randomReadAndSlice
argument_list|(
name|indexInput
argument_list|,
name|firstReadLen
argument_list|)
expr_stmt|;
name|int
name|bytesLeft
init|=
name|input
operator|.
name|length
operator|-
name|firstReadLen
decl_stmt|;
try|try
block|{
comment|// read using int size
name|int
name|secondReadLen
init|=
name|bytesLeft
operator|+
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|indexInput
operator|.
name|readBytes
argument_list|(
operator|new
name|byte
index|[
name|secondReadLen
index|]
argument_list|,
literal|0
argument_list|,
name|secondReadLen
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"EOF"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testSeekOverflow
specifier|public
name|void
name|testSeekOverflow
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|input
init|=
name|randomUnicodeOfLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1000
argument_list|)
argument_list|)
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|ByteArrayIndexInput
name|indexInput
init|=
operator|new
name|ByteArrayIndexInput
argument_list|(
literal|"test"
argument_list|,
name|input
argument_list|)
decl_stmt|;
name|int
name|firstReadLen
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|input
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|randomReadAndSlice
argument_list|(
name|indexInput
argument_list|,
name|firstReadLen
argument_list|)
expr_stmt|;
try|try
block|{
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|indexInput
operator|.
name|seek
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
operator|+
literal|4L
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|indexInput
operator|.
name|seek
argument_list|(
operator|-
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|int
name|seek
init|=
name|input
operator|.
name|length
operator|+
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|indexInput
operator|.
name|seek
argument_list|(
name|seek
argument_list|)
expr_stmt|;
break|break;
default|default:
name|fail
argument_list|()
expr_stmt|;
block|}
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"EOF"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"negative position"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|randomReadAndSlice
specifier|private
name|byte
index|[]
name|randomReadAndSlice
parameter_list|(
name|IndexInput
name|indexInput
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|readPos
init|=
operator|(
name|int
operator|)
name|indexInput
operator|.
name|getFilePointer
argument_list|()
decl_stmt|;
name|byte
index|[]
name|output
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
while|while
condition|(
name|readPos
operator|<
name|length
condition|)
block|{
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
comment|// Read by one byte at a time
name|output
index|[
name|readPos
operator|++
index|]
operator|=
name|indexInput
operator|.
name|readByte
argument_list|()
expr_stmt|;
break|break;
case|case
literal|1
case|:
comment|// Read several bytes into target
name|int
name|len
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|length
operator|-
name|readPos
argument_list|)
decl_stmt|;
name|indexInput
operator|.
name|readBytes
argument_list|(
name|output
argument_list|,
name|readPos
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|readPos
operator|+=
name|len
expr_stmt|;
break|break;
case|case
literal|2
case|:
comment|// Read several bytes into 0-offset target
name|len
operator|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|length
operator|-
name|readPos
argument_list|)
expr_stmt|;
name|byte
index|[]
name|temp
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|indexInput
operator|.
name|readBytes
argument_list|(
name|temp
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|temp
argument_list|,
literal|0
argument_list|,
name|output
argument_list|,
name|readPos
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|readPos
operator|+=
name|len
expr_stmt|;
break|break;
case|case
literal|3
case|:
comment|// Read using slice
name|len
operator|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|length
operator|-
name|readPos
argument_list|)
expr_stmt|;
name|IndexInput
name|slice
init|=
name|indexInput
operator|.
name|slice
argument_list|(
literal|"slice ("
operator|+
name|readPos
operator|+
literal|", "
operator|+
name|len
operator|+
literal|") of "
operator|+
name|indexInput
operator|.
name|toString
argument_list|()
argument_list|,
name|readPos
argument_list|,
name|len
argument_list|)
decl_stmt|;
name|temp
operator|=
name|randomReadAndSlice
argument_list|(
name|slice
argument_list|,
name|len
argument_list|)
expr_stmt|;
comment|// assert that position in the original input didn't change
name|assertEquals
argument_list|(
name|readPos
argument_list|,
name|indexInput
operator|.
name|getFilePointer
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|temp
argument_list|,
literal|0
argument_list|,
name|output
argument_list|,
name|readPos
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|readPos
operator|+=
name|len
expr_stmt|;
name|indexInput
operator|.
name|seek
argument_list|(
name|readPos
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|readPos
argument_list|,
name|indexInput
operator|.
name|getFilePointer
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
name|fail
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|readPos
argument_list|,
name|indexInput
operator|.
name|getFilePointer
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|output
return|;
block|}
block|}
end_class

end_unit

