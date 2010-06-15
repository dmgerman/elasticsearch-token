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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|thread
operator|.
name|ThreadLocals
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|BytesStreamOutput
specifier|public
class|class
name|BytesStreamOutput
extends|extends
name|StreamOutput
block|{
comment|/**      * A thread local based cache of {@link BytesStreamOutput}.      */
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
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|BytesStreamOutput
argument_list|>
argument_list|>
name|cache
init|=
operator|new
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|BytesStreamOutput
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|BytesStreamOutput
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|BytesStreamOutput
argument_list|>
argument_list|(
operator|new
name|BytesStreamOutput
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|cacheHandles
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|HandlesStreamOutput
argument_list|>
argument_list|>
name|cacheHandles
init|=
operator|new
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|HandlesStreamOutput
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|HandlesStreamOutput
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|HandlesStreamOutput
argument_list|>
argument_list|(
operator|new
name|HandlesStreamOutput
argument_list|(
operator|new
name|BytesStreamOutput
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|/**          * Returns the cached thread local byte stream, with its internal stream cleared.          */
DECL|method|cached
specifier|public
specifier|static
name|BytesStreamOutput
name|cached
parameter_list|()
block|{
name|BytesStreamOutput
name|os
init|=
name|cache
operator|.
name|get
argument_list|()
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
DECL|method|cachedHandles
specifier|public
specifier|static
name|HandlesStreamOutput
name|cachedHandles
parameter_list|()
throws|throws
name|IOException
block|{
name|HandlesStreamOutput
name|os
init|=
name|cacheHandles
operator|.
name|get
argument_list|()
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
DECL|method|BytesStreamOutput
specifier|public
name|BytesStreamOutput
parameter_list|()
block|{
name|this
argument_list|(
literal|126
argument_list|)
expr_stmt|;
block|}
DECL|method|BytesStreamOutput
specifier|public
name|BytesStreamOutput
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|this
operator|.
name|buf
operator|=
operator|new
name|byte
index|[
name|size
index|]
expr_stmt|;
block|}
DECL|method|writeByte
annotation|@
name|Override
specifier|public
name|void
name|writeByte
parameter_list|(
name|byte
name|b
parameter_list|)
throws|throws
name|IOException
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
name|b
expr_stmt|;
name|count
operator|=
name|newcount
expr_stmt|;
block|}
DECL|method|writeBytes
annotation|@
name|Override
specifier|public
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
block|{
if|if
condition|(
name|length
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
name|length
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
name|offset
argument_list|,
name|buf
argument_list|,
name|count
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|count
operator|=
name|newcount
expr_stmt|;
block|}
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
DECL|method|flush
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
comment|// nothing to do there
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
comment|// nothing to do here
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
block|}
end_class

end_unit

