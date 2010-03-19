begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.io.stream
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|Unicode
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|StreamOutput
specifier|public
specifier|abstract
class|class
name|StreamOutput
extends|extends
name|OutputStream
block|{
comment|/**      * Writes a single byte.      */
DECL|method|writeByte
specifier|public
specifier|abstract
name|void
name|writeByte
parameter_list|(
name|byte
name|b
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Writes an array of bytes.      *      * @param b the bytes to write      */
DECL|method|writeBytes
specifier|public
name|void
name|writeBytes
parameter_list|(
name|byte
index|[]
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|writeBytes
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
comment|/**      * Writes an array of bytes.      *      * @param b      the bytes to write      * @param length the number of bytes to write      */
DECL|method|writeBytes
specifier|public
name|void
name|writeBytes
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|writeBytes
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**      * Writes an array of bytes.      *      * @param b      the bytes to write      * @param offset the offset in the byte array      * @param length the number of bytes to write      */
DECL|method|writeBytes
specifier|public
specifier|abstract
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
name|length
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|writeShort
specifier|public
specifier|final
name|void
name|writeShort
parameter_list|(
name|short
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|writeByte
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|v
argument_list|)
expr_stmt|;
block|}
comment|/**      * Writes an int as four bytes.      */
DECL|method|writeInt
specifier|public
name|void
name|writeInt
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|writeByte
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|i
operator|>>
literal|24
argument_list|)
argument_list|)
expr_stmt|;
name|writeByte
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|i
operator|>>
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|writeByte
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|i
operator|>>
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
expr_stmt|;
block|}
comment|/**      * Writes an int in a variable-length format.  Writes between one and      * five bytes.  Smaller values take fewer bytes.  Negative numbers are not      * supported.      */
DECL|method|writeVInt
specifier|public
name|void
name|writeVInt
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
operator|(
name|i
operator|&
operator|~
literal|0x7F
operator|)
operator|!=
literal|0
condition|)
block|{
name|writeByte
argument_list|(
call|(
name|byte
call|)
argument_list|(
operator|(
name|i
operator|&
literal|0x7f
operator|)
operator||
literal|0x80
argument_list|)
argument_list|)
expr_stmt|;
name|i
operator|>>>=
literal|7
expr_stmt|;
block|}
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
expr_stmt|;
block|}
comment|/**      * Writes a long as eight bytes.      */
DECL|method|writeLong
specifier|public
name|void
name|writeLong
parameter_list|(
name|long
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|writeInt
argument_list|(
call|(
name|int
call|)
argument_list|(
name|i
operator|>>
literal|32
argument_list|)
argument_list|)
expr_stmt|;
name|writeInt
argument_list|(
operator|(
name|int
operator|)
name|i
argument_list|)
expr_stmt|;
block|}
comment|/**      * Writes an long in a variable-length format.  Writes between one and five      * bytes.  Smaller values take fewer bytes.  Negative numbers are not      * supported.      */
DECL|method|writeVLong
specifier|public
name|void
name|writeVLong
parameter_list|(
name|long
name|i
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
operator|(
name|i
operator|&
operator|~
literal|0x7F
operator|)
operator|!=
literal|0
condition|)
block|{
name|writeByte
argument_list|(
call|(
name|byte
call|)
argument_list|(
operator|(
name|i
operator|&
literal|0x7f
operator|)
operator||
literal|0x80
argument_list|)
argument_list|)
expr_stmt|;
name|i
operator|>>>=
literal|7
expr_stmt|;
block|}
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
expr_stmt|;
block|}
comment|/**      * Writes a string.      */
DECL|method|writeUTF
specifier|public
name|void
name|writeUTF
parameter_list|(
name|String
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|UnicodeUtil
operator|.
name|UTF8Result
name|utf8Result
init|=
name|Unicode
operator|.
name|unsafeFromStringAsUtf8
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|writeVInt
argument_list|(
name|utf8Result
operator|.
name|length
argument_list|)
expr_stmt|;
name|writeBytes
argument_list|(
name|utf8Result
operator|.
name|result
argument_list|,
literal|0
argument_list|,
name|utf8Result
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|writeFloat
specifier|public
name|void
name|writeFloat
parameter_list|(
name|float
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|writeInt
argument_list|(
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|writeDouble
specifier|public
name|void
name|writeDouble
parameter_list|(
name|double
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|writeLong
argument_list|(
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|field|ZERO
specifier|private
specifier|static
name|byte
name|ZERO
init|=
literal|0
decl_stmt|;
DECL|field|ONE
specifier|private
specifier|static
name|byte
name|ONE
init|=
literal|1
decl_stmt|;
comment|/**      * Writes a boolean.      */
DECL|method|writeBoolean
specifier|public
name|void
name|writeBoolean
parameter_list|(
name|boolean
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|writeByte
argument_list|(
name|b
condition|?
name|ONE
else|:
name|ZERO
argument_list|)
expr_stmt|;
block|}
comment|/**      * Forces any buffered output to be written.      */
DECL|method|flush
specifier|public
specifier|abstract
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Closes this stream to further operations.      */
DECL|method|close
specifier|public
specifier|abstract
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|write
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|b
argument_list|)
expr_stmt|;
block|}
DECL|method|write
annotation|@
name|Override
specifier|public
name|void
name|write
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
name|writeBytes
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

