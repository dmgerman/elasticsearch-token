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
name|UTFDataFormatException
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|BytesStreamInput
specifier|public
class|class
name|BytesStreamInput
extends|extends
name|StreamInput
block|{
DECL|field|buf
specifier|protected
name|byte
name|buf
index|[]
decl_stmt|;
DECL|field|pos
specifier|protected
name|int
name|pos
decl_stmt|;
DECL|field|count
specifier|protected
name|int
name|count
decl_stmt|;
DECL|method|BytesStreamInput
specifier|public
name|BytesStreamInput
parameter_list|(
name|byte
name|buf
index|[]
parameter_list|)
block|{
name|this
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|BytesStreamInput
specifier|public
name|BytesStreamInput
parameter_list|(
name|byte
name|buf
index|[]
parameter_list|,
name|int
name|position
parameter_list|,
name|int
name|count
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|=
name|buf
expr_stmt|;
name|this
operator|.
name|pos
operator|=
name|position
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
block|}
DECL|method|position
specifier|public
name|int
name|position
parameter_list|()
block|{
return|return
name|this
operator|.
name|pos
return|;
block|}
DECL|method|read
annotation|@
name|Override
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
name|count
operator|)
condition|?
operator|(
name|buf
index|[
name|pos
operator|++
index|]
operator|&
literal|0xff
operator|)
else|:
operator|-
literal|1
return|;
block|}
DECL|method|read
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
block|}
elseif|else
if|if
condition|(
name|off
operator|<
literal|0
operator|||
name|len
argument_list|<
literal|0
operator|||
name|len
argument_list|>
name|b
operator|.
name|length
operator|-
name|off
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|()
throw|;
block|}
if|if
condition|(
name|pos
operator|>=
name|count
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|pos
operator|+
name|len
operator|>
name|count
condition|)
block|{
name|len
operator|=
name|count
operator|-
name|pos
expr_stmt|;
block|}
if|if
condition|(
name|len
operator|<=
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
name|System
operator|.
name|arraycopy
argument_list|(
name|buf
argument_list|,
name|pos
argument_list|,
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|len
expr_stmt|;
return|return
name|len
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
name|pos
operator|>=
name|count
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
block|}
return|return
name|buf
index|[
name|pos
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
if|if
condition|(
name|len
operator|==
literal|0
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|pos
operator|>=
name|count
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
block|}
if|if
condition|(
name|pos
operator|+
name|len
operator|>
name|count
condition|)
block|{
name|len
operator|=
name|count
operator|-
name|pos
expr_stmt|;
block|}
if|if
condition|(
name|len
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
block|}
name|System
operator|.
name|arraycopy
argument_list|(
name|buf
argument_list|,
name|pos
argument_list|,
name|b
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|len
expr_stmt|;
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
name|pos
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
comment|// nothing to do here...
block|}
DECL|method|readUTF
specifier|public
name|String
name|readUTF
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|utflen
init|=
name|readUnsignedShort
argument_list|()
decl_stmt|;
if|if
condition|(
name|utflen
operator|==
literal|0
condition|)
block|{
return|return
literal|""
return|;
block|}
if|if
condition|(
name|chararr
operator|.
name|length
operator|<
name|utflen
condition|)
block|{
name|chararr
operator|=
operator|new
name|char
index|[
name|utflen
operator|*
literal|2
index|]
expr_stmt|;
block|}
name|char
index|[]
name|chararr
init|=
name|this
operator|.
name|chararr
decl_stmt|;
name|byte
index|[]
name|bytearr
init|=
name|buf
decl_stmt|;
name|int
name|endPos
init|=
name|pos
operator|+
name|utflen
decl_stmt|;
name|int
name|c
decl_stmt|,
name|char2
decl_stmt|,
name|char3
decl_stmt|;
name|int
name|count
init|=
name|pos
decl_stmt|;
name|int
name|chararr_count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|count
operator|<
name|endPos
condition|)
block|{
name|c
operator|=
operator|(
name|int
operator|)
name|bytearr
index|[
name|count
index|]
operator|&
literal|0xff
expr_stmt|;
if|if
condition|(
name|c
operator|>
literal|127
condition|)
break|break;
name|count
operator|++
expr_stmt|;
name|chararr
index|[
name|chararr_count
operator|++
index|]
operator|=
operator|(
name|char
operator|)
name|c
expr_stmt|;
block|}
while|while
condition|(
name|count
operator|<
name|endPos
condition|)
block|{
name|c
operator|=
operator|(
name|int
operator|)
name|bytearr
index|[
name|count
index|]
operator|&
literal|0xff
expr_stmt|;
switch|switch
condition|(
name|c
operator|>>
literal|4
condition|)
block|{
case|case
literal|0
case|:
case|case
literal|1
case|:
case|case
literal|2
case|:
case|case
literal|3
case|:
case|case
literal|4
case|:
case|case
literal|5
case|:
case|case
literal|6
case|:
case|case
literal|7
case|:
comment|/* 0xxxxxxx*/
name|count
operator|++
expr_stmt|;
name|chararr
index|[
name|chararr_count
operator|++
index|]
operator|=
operator|(
name|char
operator|)
name|c
expr_stmt|;
break|break;
case|case
literal|12
case|:
case|case
literal|13
case|:
comment|/* 110x xxxx   10xx xxxx*/
name|count
operator|+=
literal|2
expr_stmt|;
if|if
condition|(
name|count
operator|>
name|endPos
condition|)
throw|throw
operator|new
name|UTFDataFormatException
argument_list|(
literal|"malformed input: partial character at end"
argument_list|)
throw|;
name|char2
operator|=
operator|(
name|int
operator|)
name|bytearr
index|[
name|count
operator|-
literal|1
index|]
expr_stmt|;
if|if
condition|(
operator|(
name|char2
operator|&
literal|0xC0
operator|)
operator|!=
literal|0x80
condition|)
throw|throw
operator|new
name|UTFDataFormatException
argument_list|(
literal|"malformed input around byte "
operator|+
name|count
argument_list|)
throw|;
name|chararr
index|[
name|chararr_count
operator|++
index|]
operator|=
call|(
name|char
call|)
argument_list|(
operator|(
operator|(
name|c
operator|&
literal|0x1F
operator|)
operator|<<
literal|6
operator|)
operator||
operator|(
name|char2
operator|&
literal|0x3F
operator|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|14
case|:
comment|/* 1110 xxxx  10xx xxxx  10xx xxxx */
name|count
operator|+=
literal|3
expr_stmt|;
if|if
condition|(
name|count
operator|>
name|endPos
condition|)
throw|throw
operator|new
name|UTFDataFormatException
argument_list|(
literal|"malformed input: partial character at end"
argument_list|)
throw|;
name|char2
operator|=
operator|(
name|int
operator|)
name|bytearr
index|[
name|count
operator|-
literal|2
index|]
expr_stmt|;
name|char3
operator|=
operator|(
name|int
operator|)
name|bytearr
index|[
name|count
operator|-
literal|1
index|]
expr_stmt|;
if|if
condition|(
operator|(
operator|(
name|char2
operator|&
literal|0xC0
operator|)
operator|!=
literal|0x80
operator|)
operator|||
operator|(
operator|(
name|char3
operator|&
literal|0xC0
operator|)
operator|!=
literal|0x80
operator|)
condition|)
throw|throw
operator|new
name|UTFDataFormatException
argument_list|(
literal|"malformed input around byte "
operator|+
operator|(
name|count
operator|-
literal|1
operator|)
argument_list|)
throw|;
name|chararr
index|[
name|chararr_count
operator|++
index|]
operator|=
call|(
name|char
call|)
argument_list|(
operator|(
operator|(
name|c
operator|&
literal|0x0F
operator|)
operator|<<
literal|12
operator|)
operator||
operator|(
operator|(
name|char2
operator|&
literal|0x3F
operator|)
operator|<<
literal|6
operator|)
operator||
operator|(
operator|(
name|char3
operator|&
literal|0x3F
operator|)
operator|<<
literal|0
operator|)
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|/* 10xx xxxx,  1111 xxxx */
throw|throw
operator|new
name|UTFDataFormatException
argument_list|(
literal|"malformed input around byte "
operator|+
name|count
argument_list|)
throw|;
block|}
block|}
name|pos
operator|+=
name|utflen
expr_stmt|;
comment|// The number of chars produced may be less than utflen
return|return
operator|new
name|String
argument_list|(
name|chararr
argument_list|,
literal|0
argument_list|,
name|chararr_count
argument_list|)
return|;
block|}
block|}
end_class

end_unit

