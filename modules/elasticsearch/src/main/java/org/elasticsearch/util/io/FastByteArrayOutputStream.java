begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.io
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
package|;
end_package

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
name|io
operator|.
name|UnsupportedEncodingException
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
comment|/**  * Similar to {@link java.io.ByteArrayOutputStream} just not synced.  *  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|FastByteArrayOutputStream
specifier|public
class|class
name|FastByteArrayOutputStream
extends|extends
name|OutputStream
block|{
comment|/**      * A thread local based cache of {@link FastByteArrayOutputStream}.      */
DECL|class|Cached
specifier|public
specifier|static
class|class
name|Cached
block|{
DECL|field|cache
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|FastByteArrayOutputStream
argument_list|>
name|cache
init|=
operator|new
name|ThreadLocal
argument_list|<
name|FastByteArrayOutputStream
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|FastByteArrayOutputStream
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|FastByteArrayOutputStream
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|/**          * Returns the cached thread local byte stream, with its internal stream cleared.          */
DECL|method|cached
specifier|public
specifier|static
name|FastByteArrayOutputStream
name|cached
parameter_list|()
block|{
name|FastByteArrayOutputStream
name|os
init|=
name|cache
operator|.
name|get
argument_list|()
decl_stmt|;
name|os
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|os
return|;
block|}
block|}
comment|/**      * The buffer where data is stored.      */
DECL|field|buf
specifier|protected
name|byte
name|buf
index|[]
decl_stmt|;
comment|/**      * The number of valid bytes in the buffer.      */
DECL|field|count
specifier|protected
name|int
name|count
decl_stmt|;
comment|/**      * Creates a new byte array output stream. The buffer capacity is      * initially 32 bytes, though its size increases if necessary.      */
DECL|method|FastByteArrayOutputStream
specifier|public
name|FastByteArrayOutputStream
parameter_list|()
block|{
name|this
argument_list|(
literal|32
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new byte array output stream, with a buffer capacity of      * the specified size, in bytes.      *      * @param size the initial size.      * @throws IllegalArgumentException if size is negative.      */
DECL|method|FastByteArrayOutputStream
specifier|public
name|FastByteArrayOutputStream
parameter_list|(
name|int
name|size
parameter_list|)
block|{
if|if
condition|(
name|size
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Negative initial size: "
operator|+
name|size
argument_list|)
throw|;
block|}
name|buf
operator|=
operator|new
name|byte
index|[
name|size
index|]
expr_stmt|;
block|}
comment|/**      * Writes the specified byte to this byte array output stream.      *      * @param b the byte to be written.      */
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
block|{
name|int
name|newcount
init|=
name|count
operator|+
literal|1
decl_stmt|;
if|if
condition|(
name|newcount
operator|>
name|buf
operator|.
name|length
condition|)
block|{
name|buf
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|buf
argument_list|,
name|Math
operator|.
name|max
argument_list|(
name|buf
operator|.
name|length
operator|<<
literal|1
argument_list|,
name|newcount
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|buf
index|[
name|count
index|]
operator|=
operator|(
name|byte
operator|)
name|b
expr_stmt|;
name|count
operator|=
name|newcount
expr_stmt|;
block|}
comment|/**      * Writes<code>len</code> bytes from the specified byte array      * starting at offset<code>off</code> to this byte array output stream.      *      *<b>NO checks for bounds, parameters must be ok!</b>      *      * @param b   the data.      * @param off the start offset in the data.      * @param len the number of bytes to write.      */
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|byte
name|b
index|[]
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
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
name|int
name|newcount
init|=
name|count
operator|+
name|len
decl_stmt|;
if|if
condition|(
name|newcount
operator|>
name|buf
operator|.
name|length
condition|)
block|{
name|buf
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|buf
argument_list|,
name|Math
operator|.
name|max
argument_list|(
name|buf
operator|.
name|length
operator|<<
literal|1
argument_list|,
name|newcount
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|arraycopy
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|buf
argument_list|,
name|count
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|count
operator|=
name|newcount
expr_stmt|;
block|}
comment|/**      * Writes the complete contents of this byte array output stream to      * the specified output stream argument, as if by calling the output      * stream's write method using<code>out.write(buf, 0, count)</code>.      *      * @param out the output stream to which to write the data.      * @throws IOException if an I/O error occurs.      */
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
comment|/**      * Resets the<code>count</code> field of this byte array output      * stream to zero, so that all currently accumulated output in the      * output stream is discarded. The output stream can be used again,      * reusing the already allocated buffer space.      *      * @see java.io.ByteArrayInputStream#count      */
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|count
operator|=
literal|0
expr_stmt|;
block|}
comment|/**      * Creates a newly allocated byte array. Its size is the current      * size of this output stream and the valid contents of the buffer      * have been copied into it.      *      * @return the current contents of this output stream, as a byte array.      * @see java.io.ByteArrayOutputStream#size()      */
DECL|method|copiedByteArray
specifier|public
name|byte
name|copiedByteArray
argument_list|()
index|[]
block|{
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|buf
argument_list|,
name|count
argument_list|)
return|;
block|}
comment|/**      * Returns the underlying byte array. Note, use {@link #size()} in order to know      * the length of it.      */
DECL|method|unsafeByteArray
specifier|public
name|byte
index|[]
name|unsafeByteArray
parameter_list|()
block|{
return|return
name|buf
return|;
block|}
comment|/**      * Returns the current size of the buffer.      *      * @return the value of the<code>count</code> field, which is the number      *         of valid bytes in this output stream.      * @see java.io.ByteArrayOutputStream#count      */
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|count
return|;
block|}
comment|/**      * Converts the buffer's contents into a string decoding bytes using the      * platform's default character set. The length of the new<tt>String</tt>      * is a function of the character set, and hence may not be equal to the      * size of the buffer.      *<p/>      *<p> This method always replaces malformed-input and unmappable-character      * sequences with the default replacement string for the platform's      * default character set. The {@linkplain java.nio.charset.CharsetDecoder}      * class should be used when more control over the decoding process is      * required.      *      * @return String decoded from the buffer's contents.      * @since JDK1.1      */
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
operator|new
name|String
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|count
argument_list|)
return|;
block|}
comment|/**      * Converts the buffer's contents into a string by decoding the bytes using      * the specified {@link java.nio.charset.Charset charsetName}. The length of      * the new<tt>String</tt> is a function of the charset, and hence may not be      * equal to the length of the byte array.      *<p/>      *<p> This method always replaces malformed-input and unmappable-character      * sequences with this charset's default replacement string. The {@link      * java.nio.charset.CharsetDecoder} class should be used when more control      * over the decoding process is required.      *      * @param charsetName the name of a supported      *                    {@linkplain java.nio.charset.Charset</code>charset<code>}      * @return String decoded from the buffer's contents.      * @throws java.io.UnsupportedEncodingException      *          If the named charset is not supported      * @since JDK1.1      */
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|charsetName
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
return|return
operator|new
name|String
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|count
argument_list|,
name|charsetName
argument_list|)
return|;
block|}
comment|/**      * Closing a<tt>ByteArrayOutputStream</tt> has no effect. The methods in      * this class can be called after the stream has been closed without      * generating an<tt>IOException</tt>.      *<p/>      */
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{     }
block|}
end_class

end_unit

