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
name|InputStream
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
DECL|class|StreamInput
specifier|public
specifier|abstract
class|class
name|StreamInput
extends|extends
name|InputStream
block|{
comment|/**      * working arrays initialized on demand by readUTF      */
DECL|field|bytearr
specifier|private
name|byte
name|bytearr
index|[]
init|=
operator|new
name|byte
index|[
literal|80
index|]
decl_stmt|;
DECL|field|chararr
specifier|protected
name|char
name|chararr
index|[]
init|=
operator|new
name|char
index|[
literal|80
index|]
decl_stmt|;
comment|/**      * Reads and returns a single byte.      */
DECL|method|readByte
specifier|public
specifier|abstract
name|byte
name|readByte
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Reads a specified number of bytes into an array at the specified offset.      *      * @param b      the array to read bytes into      * @param offset the offset in the array to start storing bytes      * @param len    the number of bytes to read      */
DECL|method|readBytes
specifier|public
specifier|abstract
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
function_decl|;
DECL|method|readFully
specifier|public
name|void
name|readFully
parameter_list|(
name|byte
index|[]
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|readBytes
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|readShort
specifier|public
name|short
name|readShort
parameter_list|()
throws|throws
name|IOException
block|{
return|return
call|(
name|short
call|)
argument_list|(
operator|(
operator|(
name|readByte
argument_list|()
operator|&
literal|0xFF
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
name|readByte
argument_list|()
operator|&
literal|0xFF
operator|)
argument_list|)
return|;
block|}
comment|/**      * Reads four bytes and returns an int.      */
DECL|method|readInt
specifier|public
name|int
name|readInt
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|(
operator|(
name|readByte
argument_list|()
operator|&
literal|0xFF
operator|)
operator|<<
literal|24
operator|)
operator||
operator|(
operator|(
name|readByte
argument_list|()
operator|&
literal|0xFF
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
operator|(
name|readByte
argument_list|()
operator|&
literal|0xFF
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
name|readByte
argument_list|()
operator|&
literal|0xFF
operator|)
return|;
block|}
comment|/**      * Reads an int stored in variable-length format.  Reads between one and      * five bytes.  Smaller values take fewer bytes.  Negative numbers are not      * supported.      */
DECL|method|readVInt
specifier|public
name|int
name|readVInt
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
name|b
init|=
name|readByte
argument_list|()
decl_stmt|;
name|int
name|i
init|=
name|b
operator|&
literal|0x7F
decl_stmt|;
for|for
control|(
name|int
name|shift
init|=
literal|7
init|;
operator|(
name|b
operator|&
literal|0x80
operator|)
operator|!=
literal|0
condition|;
name|shift
operator|+=
literal|7
control|)
block|{
name|b
operator|=
name|readByte
argument_list|()
expr_stmt|;
name|i
operator||=
operator|(
name|b
operator|&
literal|0x7F
operator|)
operator|<<
name|shift
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
comment|/**      * Reads eight bytes and returns a long.      */
DECL|method|readLong
specifier|public
name|long
name|readLong
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|(
operator|(
operator|(
name|long
operator|)
name|readInt
argument_list|()
operator|)
operator|<<
literal|32
operator|)
operator||
operator|(
name|readInt
argument_list|()
operator|&
literal|0xFFFFFFFFL
operator|)
return|;
block|}
comment|/**      * Reads a long stored in variable-length format.  Reads between one and      * nine bytes.  Smaller values take fewer bytes.  Negative numbers are not      * supported.      */
DECL|method|readVLong
specifier|public
name|long
name|readVLong
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
name|b
init|=
name|readByte
argument_list|()
decl_stmt|;
name|long
name|i
init|=
name|b
operator|&
literal|0x7F
decl_stmt|;
for|for
control|(
name|int
name|shift
init|=
literal|7
init|;
operator|(
name|b
operator|&
literal|0x80
operator|)
operator|!=
literal|0
condition|;
name|shift
operator|+=
literal|7
control|)
block|{
name|b
operator|=
name|readByte
argument_list|()
expr_stmt|;
name|i
operator||=
operator|(
name|b
operator|&
literal|0x7FL
operator|)
operator|<<
name|shift
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
comment|// COPIED from DataInputStream
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
name|readInt
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
name|bytearr
operator|.
name|length
operator|<
name|utflen
condition|)
block|{
name|bytearr
operator|=
operator|new
name|byte
index|[
name|utflen
operator|*
literal|2
index|]
expr_stmt|;
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
name|this
operator|.
name|bytearr
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
literal|0
decl_stmt|;
name|int
name|chararr_count
init|=
literal|0
decl_stmt|;
name|readBytes
argument_list|(
name|bytearr
argument_list|,
literal|0
argument_list|,
name|utflen
argument_list|)
expr_stmt|;
while|while
condition|(
name|count
operator|<
name|utflen
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
name|utflen
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
name|utflen
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
name|utflen
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
DECL|method|readFloat
specifier|public
specifier|final
name|float
name|readFloat
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|Float
operator|.
name|intBitsToFloat
argument_list|(
name|readInt
argument_list|()
argument_list|)
return|;
block|}
DECL|method|readDouble
specifier|public
specifier|final
name|double
name|readDouble
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|Double
operator|.
name|longBitsToDouble
argument_list|(
name|readLong
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Reads a boolean.      */
DECL|method|readBoolean
specifier|public
specifier|final
name|boolean
name|readBoolean
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
name|ch
init|=
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|ch
operator|<
literal|0
condition|)
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
return|return
operator|(
name|ch
operator|!=
literal|0
operator|)
return|;
block|}
comment|/**      * Resets the stream.      */
DECL|method|reset
specifier|public
specifier|abstract
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Closes the stream to further operations.      */
DECL|method|close
specifier|public
specifier|abstract
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|//    // IS
comment|//
comment|//    @Override public int read() throws IOException {
comment|//        return readByte();
comment|//    }
comment|//
comment|//    // Here, we assume that we always can read the full byte array
comment|//
comment|//    @Override public int read(byte[] b, int off, int len) throws IOException {
comment|//        readBytes(b, off, len);
comment|//        return len;
comment|//    }
block|}
end_class

end_unit

