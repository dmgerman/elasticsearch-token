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
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|concurrent
operator|.
name|NotThreadSafe
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
name|Writer
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
comment|/**  * A similar class to {@link java.io.CharArrayWriter} allowing to get the underlying<tt>char[]</tt> buffer.  *  * @author kimchy (Shay Banon)  */
end_comment

begin_class
annotation|@
name|NotThreadSafe
DECL|class|FastCharArrayWriter
specifier|public
class|class
name|FastCharArrayWriter
extends|extends
name|Writer
block|{
comment|/**      * The buffer where data is stored.      */
DECL|field|buf
specifier|protected
name|char
name|buf
index|[]
decl_stmt|;
comment|/**      * The number of chars in the buffer.      */
DECL|field|count
specifier|protected
name|int
name|count
decl_stmt|;
comment|/**      * Creates a new CharArrayWriter.      */
DECL|method|FastCharArrayWriter
specifier|public
name|FastCharArrayWriter
parameter_list|()
block|{
name|this
argument_list|(
literal|32
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new CharArrayWriter with the specified initial size.      *      * @param initialSize an int specifying the initial buffer size.      * @throws IllegalArgumentException if initialSize is negative      */
DECL|method|FastCharArrayWriter
specifier|public
name|FastCharArrayWriter
parameter_list|(
name|int
name|initialSize
parameter_list|)
block|{
if|if
condition|(
name|initialSize
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
name|initialSize
argument_list|)
throw|;
block|}
name|buf
operator|=
operator|new
name|char
index|[
name|initialSize
index|]
expr_stmt|;
block|}
comment|/**      * Writes a character to the buffer.      */
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|int
name|c
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
name|char
operator|)
name|c
expr_stmt|;
name|count
operator|=
name|newcount
expr_stmt|;
block|}
comment|/**      * Writes characters to the buffer.      *      * @param c   the data to be written      * @param off the start offset in the data      * @param len the number of chars that are written      */
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|char
name|c
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
operator|(
name|off
operator|<
literal|0
operator|)
operator|||
operator|(
name|off
operator|>
name|c
operator|.
name|length
operator|)
operator|||
operator|(
name|len
operator|<
literal|0
operator|)
operator|||
operator|(
operator|(
name|off
operator|+
name|len
operator|)
operator|>
name|c
operator|.
name|length
operator|)
operator|||
operator|(
operator|(
name|off
operator|+
name|len
operator|)
operator|<
literal|0
operator|)
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|()
throw|;
block|}
elseif|else
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
name|c
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
comment|/**      * Write a portion of a string to the buffer.      *      * @param str String to be written from      * @param off Offset from which to start reading characters      * @param len Number of characters to be written      */
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|String
name|str
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
block|{
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
name|str
operator|.
name|getChars
argument_list|(
name|off
argument_list|,
name|off
operator|+
name|len
argument_list|,
name|buf
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|count
operator|=
name|newcount
expr_stmt|;
block|}
comment|/**      * Writes the contents of the buffer to another character stream.      *      * @param out the output stream to write to      * @throws java.io.IOException If an I/O error occurs.      */
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|Writer
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
comment|/**      * Appends the specified character sequence to this writer.      *<p/>      *<p> An invocation of this method of the form<tt>out.append(csq)</tt>      * behaves in exactly the same way as the invocation      *<p/>      *<pre>      *     out.write(csq.toString())</pre>      *<p/>      *<p> Depending on the specification of<tt>toString</tt> for the      * character sequence<tt>csq</tt>, the entire sequence may not be      * appended. For instance, invoking the<tt>toString</tt> method of a      * character buffer will return a subsequence whose content depends upon      * the buffer's position and limit.      *      * @param csq The character sequence to append.  If<tt>csq</tt> is      *<tt>null</tt>, then the four characters<tt>"null"</tt> are      *            appended to this writer.      * @return This writer      * @since 1.5      */
DECL|method|append
specifier|public
name|FastCharArrayWriter
name|append
parameter_list|(
name|CharSequence
name|csq
parameter_list|)
block|{
name|String
name|s
init|=
operator|(
name|csq
operator|==
literal|null
condition|?
literal|"null"
else|:
name|csq
operator|.
name|toString
argument_list|()
operator|)
decl_stmt|;
name|write
argument_list|(
name|s
argument_list|,
literal|0
argument_list|,
name|s
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Appends a subsequence of the specified character sequence to this writer.      *<p/>      *<p> An invocation of this method of the form<tt>out.append(csq, start,      * end)</tt> when<tt>csq</tt> is not<tt>null</tt>, behaves in      * exactly the same way as the invocation      *<p/>      *<pre>      *     out.write(csq.subSequence(start, end).toString())</pre>      *      * @param csq   The character sequence from which a subsequence will be      *              appended.  If<tt>csq</tt> is<tt>null</tt>, then characters      *              will be appended as if<tt>csq</tt> contained the four      *              characters<tt>"null"</tt>.      * @param start The index of the first character in the subsequence      * @param end   The index of the character following the last character in the      *              subsequence      * @return This writer      * @throws IndexOutOfBoundsException If<tt>start</tt> or<tt>end</tt> are negative,<tt>start</tt>      *                                   is greater than<tt>end</tt>, or<tt>end</tt> is greater than      *<tt>csq.length()</tt>      * @since 1.5      */
DECL|method|append
specifier|public
name|FastCharArrayWriter
name|append
parameter_list|(
name|CharSequence
name|csq
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
block|{
name|String
name|s
init|=
operator|(
name|csq
operator|==
literal|null
condition|?
literal|"null"
else|:
name|csq
operator|)
operator|.
name|subSequence
argument_list|(
name|start
argument_list|,
name|end
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|write
argument_list|(
name|s
argument_list|,
literal|0
argument_list|,
name|s
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Appends the specified character to this writer.      *<p/>      *<p> An invocation of this method of the form<tt>out.append(c)</tt>      * behaves in exactly the same way as the invocation      *<p/>      *<pre>      *     out.write(c)</pre>      *      * @param c The 16-bit character to append      * @return This writer      * @since 1.5      */
DECL|method|append
specifier|public
name|FastCharArrayWriter
name|append
parameter_list|(
name|char
name|c
parameter_list|)
block|{
name|write
argument_list|(
name|c
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Resets the buffer so that you can use it again without      * throwing away the already allocated buffer.      */
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
comment|/**      * Returns a copy of the input data.      *      * @return an array of chars copied from the input data.      */
DECL|method|toCharArray
specifier|public
name|char
name|toCharArray
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
comment|/**      * Returns the underlying char array. Note, use {@link #size()} in order to know the size of      * of the actual content within the array.      */
DECL|method|unsafeCharArray
specifier|public
name|char
index|[]
name|unsafeCharArray
parameter_list|()
block|{
return|return
name|buf
return|;
block|}
comment|/**      * Returns the current size of the buffer.      *      * @return an int representing the current size of the buffer.      */
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
comment|/**      * Converts input data to a string.      *      * @return the string.      */
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
comment|/**      * Converts the input data to a string with trimmed whitespaces.      */
DECL|method|toStringTrim
specifier|public
name|String
name|toStringTrim
parameter_list|()
block|{
name|int
name|st
init|=
literal|0
decl_stmt|;
name|int
name|len
init|=
name|count
decl_stmt|;
name|char
index|[]
name|val
init|=
name|buf
decl_stmt|;
comment|/* avoid getfield opcode */
while|while
condition|(
operator|(
name|st
operator|<
name|len
operator|)
operator|&&
operator|(
name|val
index|[
name|st
index|]
operator|<=
literal|' '
operator|)
condition|)
block|{
name|st
operator|++
expr_stmt|;
name|len
operator|--
expr_stmt|;
block|}
while|while
condition|(
operator|(
name|st
operator|<
name|len
operator|)
operator|&&
operator|(
name|val
index|[
name|len
operator|-
literal|1
index|]
operator|<=
literal|' '
operator|)
condition|)
block|{
name|len
operator|--
expr_stmt|;
block|}
return|return
operator|new
name|String
argument_list|(
name|buf
argument_list|,
name|st
argument_list|,
name|len
argument_list|)
return|;
block|}
comment|/**      * Flush the stream.      */
DECL|method|flush
specifier|public
name|void
name|flush
parameter_list|()
block|{     }
comment|/**      * Close the stream.  This method does not release the buffer, since its      * contents might still be required. Note: Invoking this method in this class      * will have no effect.      */
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{     }
block|}
end_class

end_unit

