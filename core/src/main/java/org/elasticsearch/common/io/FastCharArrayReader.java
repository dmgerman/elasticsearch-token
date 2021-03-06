begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.io
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|Reader
import|;
end_import

begin_class
DECL|class|FastCharArrayReader
specifier|public
class|class
name|FastCharArrayReader
extends|extends
name|Reader
block|{
comment|/**      * The character buffer.      */
DECL|field|buf
specifier|protected
name|char
name|buf
index|[]
decl_stmt|;
comment|/**      * The current buffer position.      */
DECL|field|pos
specifier|protected
name|int
name|pos
decl_stmt|;
comment|/**      * The position of mark in buffer.      */
DECL|field|markedPos
specifier|protected
name|int
name|markedPos
init|=
literal|0
decl_stmt|;
comment|/**      * The index of the end of this buffer.  There is not valid      * data at or beyond this index.      */
DECL|field|count
specifier|protected
name|int
name|count
decl_stmt|;
comment|/**      * Creates a CharArrayReader from the specified array of chars.      *      * @param buf Input buffer (not copied)      */
DECL|method|FastCharArrayReader
specifier|public
name|FastCharArrayReader
parameter_list|(
name|char
name|buf
index|[]
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
literal|0
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|buf
operator|.
name|length
expr_stmt|;
block|}
comment|/**      * Creates a CharArrayReader from the specified array of chars.      *<p>      * The resulting reader will start reading at the given      *<tt>offset</tt>.  The total number of<tt>char</tt> values that can be      * read from this reader will be either<tt>length</tt> or      *<tt>buf.length-offset</tt>, whichever is smaller.      *      * @param buf    Input buffer (not copied)      * @param offset Offset of the first char to read      * @param length Number of chars to read      * @throws IllegalArgumentException If<tt>offset</tt> is negative or greater than      *<tt>buf.length</tt>, or if<tt>length</tt> is negative, or if      *                                  the sum of these two values is negative.      */
DECL|method|FastCharArrayReader
specifier|public
name|FastCharArrayReader
parameter_list|(
name|char
name|buf
index|[]
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
operator|(
name|offset
operator|<
literal|0
operator|)
operator|||
operator|(
name|offset
operator|>
name|buf
operator|.
name|length
operator|)
operator|||
operator|(
name|length
operator|<
literal|0
operator|)
operator|||
operator|(
operator|(
name|offset
operator|+
name|length
operator|)
operator|<
literal|0
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
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
name|offset
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|Math
operator|.
name|min
argument_list|(
name|offset
operator|+
name|length
argument_list|,
name|buf
operator|.
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|markedPos
operator|=
name|offset
expr_stmt|;
block|}
comment|/**      * Checks to make sure that the stream has not been closed      */
DECL|method|ensureOpen
specifier|private
name|void
name|ensureOpen
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|buf
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Stream closed"
argument_list|)
throw|;
block|}
comment|/**      * Reads a single character.      *      * @throws IOException If an I/O error occurs      */
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
name|ensureOpen
argument_list|()
expr_stmt|;
if|if
condition|(
name|pos
operator|>=
name|count
condition|)
return|return
operator|-
literal|1
return|;
else|else
return|return
name|buf
index|[
name|pos
operator|++
index|]
return|;
block|}
comment|/**      * Reads characters into a portion of an array.      *      * @param b   Destination buffer      * @param off Offset at which to start storing characters      * @param len Maximum number of characters to read      * @return The actual number of characters read, or -1 if      *         the end of the stream has been reached      * @throws IOException If an I/O error occurs      */
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|(
name|char
name|b
index|[]
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
name|ensureOpen
argument_list|()
expr_stmt|;
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
name|b
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
name|b
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
return|return
literal|0
return|;
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
comment|/**      * Skips characters.  Returns the number of characters that were skipped.      *<p>      * The<code>n</code> parameter may be negative, even though the      *<code>skip</code> method of the {@link Reader} superclass throws      * an exception in this case. If<code>n</code> is negative, then      * this method does nothing and returns<code>0</code>.      *      * @param n The number of characters to skip      * @return The number of characters actually skipped      * @throws IOException If the stream is closed, or an I/O error occurs      */
annotation|@
name|Override
DECL|method|skip
specifier|public
name|long
name|skip
parameter_list|(
name|long
name|n
parameter_list|)
throws|throws
name|IOException
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
if|if
condition|(
name|pos
operator|+
name|n
operator|>
name|count
condition|)
block|{
name|n
operator|=
name|count
operator|-
name|pos
expr_stmt|;
block|}
if|if
condition|(
name|n
operator|<
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
name|pos
operator|+=
name|n
expr_stmt|;
return|return
name|n
return|;
block|}
comment|/**      * Tells whether this stream is ready to be read.  Character-array readers      * are always ready to be read.      *      * @throws IOException If an I/O error occurs      */
annotation|@
name|Override
DECL|method|ready
specifier|public
name|boolean
name|ready
parameter_list|()
throws|throws
name|IOException
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
return|return
operator|(
name|count
operator|-
name|pos
operator|)
operator|>
literal|0
return|;
block|}
comment|/**      * Tells whether this stream supports the mark() operation, which it does.      */
annotation|@
name|Override
DECL|method|markSupported
specifier|public
name|boolean
name|markSupported
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**      * Marks the present position in the stream.  Subsequent calls to reset()      * will reposition the stream to this point.      *      * @param readAheadLimit Limit on the number of characters that may be      *                       read while still preserving the mark.  Because      *                       the stream's input comes from a character array,      *                       there is no actual limit; hence this argument is      *                       ignored.      * @throws IOException If an I/O error occurs      */
annotation|@
name|Override
DECL|method|mark
specifier|public
name|void
name|mark
parameter_list|(
name|int
name|readAheadLimit
parameter_list|)
throws|throws
name|IOException
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
name|markedPos
operator|=
name|pos
expr_stmt|;
block|}
comment|/**      * Resets the stream to the most recent mark, or to the beginning if it has      * never been marked.      *      * @throws IOException If an I/O error occurs      */
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
name|ensureOpen
argument_list|()
expr_stmt|;
name|pos
operator|=
name|markedPos
expr_stmt|;
block|}
comment|/**      * Closes the stream and releases any system resources associated with      * it.  Once the stream has been closed, further read(), ready(),      * mark(), reset(), or skip() invocations will throw an IOException.      * Closing a previously closed stream has no effect.      */
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|buf
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit

