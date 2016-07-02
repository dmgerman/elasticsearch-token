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

begin_comment
comment|/**  * A character stream whose source is a string that is<b>not thread safe</b>  *<p>  * (shay.banon  * )  */
end_comment

begin_class
DECL|class|FastStringReader
specifier|public
class|class
name|FastStringReader
extends|extends
name|Reader
implements|implements
name|CharSequence
block|{
DECL|field|str
specifier|private
name|String
name|str
decl_stmt|;
DECL|field|length
specifier|private
name|int
name|length
decl_stmt|;
DECL|field|next
specifier|private
name|int
name|next
init|=
literal|0
decl_stmt|;
DECL|field|mark
specifier|private
name|int
name|mark
init|=
literal|0
decl_stmt|;
comment|/**      * Creates a new string reader.      *      * @param s String providing the character stream.      */
DECL|method|FastStringReader
specifier|public
name|FastStringReader
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|this
operator|.
name|str
operator|=
name|s
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|s
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
comment|/**      * Check to make sure that the stream has not been closed      */
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
name|length
operator|==
operator|-
literal|1
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Stream closed"
argument_list|)
throw|;
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
DECL|method|charAt
specifier|public
name|char
name|charAt
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|str
operator|.
name|charAt
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|subSequence
specifier|public
name|CharSequence
name|subSequence
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
block|{
return|return
name|str
operator|.
name|subSequence
argument_list|(
name|start
argument_list|,
name|end
argument_list|)
return|;
block|}
comment|/**      * Reads a single character.      *      * @return The character read, or -1 if the end of the stream has been      *         reached      * @throws IOException If an I/O error occurs      */
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
name|next
operator|>=
name|length
condition|)
return|return
operator|-
literal|1
return|;
return|return
name|str
operator|.
name|charAt
argument_list|(
name|next
operator|++
argument_list|)
return|;
block|}
comment|/**      * Reads characters into a portion of an array.      *      * @param cbuf Destination buffer      * @param off  Offset at which to start writing characters      * @param len  Maximum number of characters to read      * @return The number of characters read, or -1 if the end of the      *         stream has been reached      * @throws IOException If an I/O error occurs      */
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|(
name|char
name|cbuf
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
name|next
operator|>=
name|length
condition|)
return|return
operator|-
literal|1
return|;
name|int
name|n
init|=
name|Math
operator|.
name|min
argument_list|(
name|length
operator|-
name|next
argument_list|,
name|len
argument_list|)
decl_stmt|;
name|str
operator|.
name|getChars
argument_list|(
name|next
argument_list|,
name|next
operator|+
name|n
argument_list|,
name|cbuf
argument_list|,
name|off
argument_list|)
expr_stmt|;
name|next
operator|+=
name|n
expr_stmt|;
return|return
name|n
return|;
block|}
comment|/**      * Skips the specified number of characters in the stream. Returns      * the number of characters that were skipped.      *<p>      * The<code>ns</code> parameter may be negative, even though the      *<code>skip</code> method of the {@link Reader} superclass throws      * an exception in this case. Negative values of<code>ns</code> cause the      * stream to skip backwards. Negative return values indicate a skip      * backwards. It is not possible to skip backwards past the beginning of      * the string.      *<p>      * If the entire string has been read or skipped, then this method has      * no effect and always returns 0.      *      * @throws IOException If an I/O error occurs      */
annotation|@
name|Override
DECL|method|skip
specifier|public
name|long
name|skip
parameter_list|(
name|long
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
if|if
condition|(
name|next
operator|>=
name|length
condition|)
return|return
literal|0
return|;
comment|// Bound skip by beginning and end of the source
name|long
name|n
init|=
name|Math
operator|.
name|min
argument_list|(
name|length
operator|-
name|next
argument_list|,
name|ns
argument_list|)
decl_stmt|;
name|n
operator|=
name|Math
operator|.
name|max
argument_list|(
operator|-
name|next
argument_list|,
name|n
argument_list|)
expr_stmt|;
name|next
operator|+=
name|n
expr_stmt|;
return|return
name|n
return|;
block|}
comment|/**      * Tells whether this stream is ready to be read.      *      * @return True if the next read() is guaranteed not to block for input      * @throws IOException If the stream is closed      */
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
literal|true
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
comment|/**      * Marks the present position in the stream.  Subsequent calls to reset()      * will reposition the stream to this point.      *      * @param readAheadLimit Limit on the number of characters that may be      *                       read while still preserving the mark.  Because      *                       the stream's input comes from a string, there      *                       is no actual limit, so this argument must not      *                       be negative, but is otherwise ignored.      * @throws IllegalArgumentException If readAheadLimit is&lt; 0      * @throws IOException              If an I/O error occurs      */
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
if|if
condition|(
name|readAheadLimit
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Read-ahead limit< 0"
argument_list|)
throw|;
block|}
name|ensureOpen
argument_list|()
expr_stmt|;
name|mark
operator|=
name|next
expr_stmt|;
block|}
comment|/**      * Resets the stream to the most recent mark, or to the beginning of the      * string if it has never been marked.      *      * @throws IOException If an I/O error occurs      */
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
name|next
operator|=
name|mark
expr_stmt|;
block|}
comment|/**      * Closes the stream and releases any system resources associated with      * it. Once the stream has been closed, further read(),      * ready(), mark(), or reset() invocations will throw an IOException.      * Closing a previously closed stream has no effect.      */
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|length
operator|=
operator|-
literal|1
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|str
return|;
block|}
block|}
end_class

end_unit

