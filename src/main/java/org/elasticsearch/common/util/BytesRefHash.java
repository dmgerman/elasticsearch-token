begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|BitMixer
import|;
end_import

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
name|BytesRef
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
name|lease
operator|.
name|Releasable
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
name|lease
operator|.
name|Releasables
import|;
end_import

begin_comment
comment|/**  *  Specialized hash table implementation similar to Lucene's BytesRefHash that maps  *  BytesRef values to ids. Collisions are resolved with open addressing and linear  *  probing, growth is smooth thanks to {@link BigArrays}, hashes are cached for faster  *  re-hashing and capacity is always a multiple of 2 for faster identification of buckets.  *  This class is not thread-safe.  */
end_comment

begin_class
DECL|class|BytesRefHash
specifier|public
specifier|final
class|class
name|BytesRefHash
extends|extends
name|AbstractHash
block|{
DECL|field|startOffsets
specifier|private
name|LongArray
name|startOffsets
decl_stmt|;
DECL|field|bytes
specifier|private
name|ByteArray
name|bytes
decl_stmt|;
DECL|field|hashes
specifier|private
name|IntArray
name|hashes
decl_stmt|;
comment|// we cache hashes for faster re-hashing
DECL|field|spare
specifier|private
specifier|final
name|BytesRef
name|spare
decl_stmt|;
comment|// Constructor with configurable capacity and default maximum load factor.
DECL|method|BytesRefHash
specifier|public
name|BytesRefHash
parameter_list|(
name|long
name|capacity
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|)
block|{
name|this
argument_list|(
name|capacity
argument_list|,
name|DEFAULT_MAX_LOAD_FACTOR
argument_list|,
name|bigArrays
argument_list|)
expr_stmt|;
block|}
comment|//Constructor with configurable capacity and load factor.
DECL|method|BytesRefHash
specifier|public
name|BytesRefHash
parameter_list|(
name|long
name|capacity
parameter_list|,
name|float
name|maxLoadFactor
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|)
block|{
name|super
argument_list|(
name|capacity
argument_list|,
name|maxLoadFactor
argument_list|,
name|bigArrays
argument_list|)
expr_stmt|;
name|startOffsets
operator|=
name|bigArrays
operator|.
name|newLongArray
argument_list|(
name|capacity
operator|+
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|startOffsets
operator|.
name|set
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|bytes
operator|=
name|bigArrays
operator|.
name|newByteArray
argument_list|(
name|capacity
operator|*
literal|3
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|hashes
operator|=
name|bigArrays
operator|.
name|newIntArray
argument_list|(
name|capacity
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|spare
operator|=
operator|new
name|BytesRef
argument_list|()
expr_stmt|;
block|}
comment|// BytesRef has a weak hashCode function so we try to improve it by rehashing using Murmur3
comment|// Feel free to remove rehashing if BytesRef gets a better hash function
DECL|method|rehash
specifier|private
specifier|static
name|int
name|rehash
parameter_list|(
name|int
name|hash
parameter_list|)
block|{
return|return
name|BitMixer
operator|.
name|mix32
argument_list|(
name|hash
argument_list|)
return|;
block|}
comment|/**      * Return the key at<code>0&lte; index&lte; capacity()</code>. The result is undefined if the slot is unused.      *<p color="red">Beware that the content of the {@link BytesRef} may become invalid as soon as {@link #close()} is called</p>      */
DECL|method|get
specifier|public
name|BytesRef
name|get
parameter_list|(
name|long
name|id
parameter_list|,
name|BytesRef
name|dest
parameter_list|)
block|{
specifier|final
name|long
name|startOffset
init|=
name|startOffsets
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
specifier|final
name|int
name|length
init|=
call|(
name|int
call|)
argument_list|(
name|startOffsets
operator|.
name|get
argument_list|(
name|id
operator|+
literal|1
argument_list|)
operator|-
name|startOffset
argument_list|)
decl_stmt|;
name|bytes
operator|.
name|get
argument_list|(
name|startOffset
argument_list|,
name|length
argument_list|,
name|dest
argument_list|)
expr_stmt|;
return|return
name|dest
return|;
block|}
comment|/**      * Get the id associated with<code>key</code>      */
DECL|method|find
specifier|public
name|long
name|find
parameter_list|(
name|BytesRef
name|key
parameter_list|,
name|int
name|code
parameter_list|)
block|{
specifier|final
name|long
name|slot
init|=
name|slot
argument_list|(
name|rehash
argument_list|(
name|code
argument_list|)
argument_list|,
name|mask
argument_list|)
decl_stmt|;
for|for
control|(
name|long
name|index
init|=
name|slot
init|;
condition|;
name|index
operator|=
name|nextSlot
argument_list|(
name|index
argument_list|,
name|mask
argument_list|)
control|)
block|{
specifier|final
name|long
name|id
init|=
name|id
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|id
operator|==
operator|-
literal|1L
operator|||
name|key
operator|.
name|bytesEquals
argument_list|(
name|get
argument_list|(
name|id
argument_list|,
name|spare
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|id
return|;
block|}
block|}
block|}
comment|/** Sugar for {@link #find(BytesRef, int) find(key, key.hashCode()} */
DECL|method|find
specifier|public
name|long
name|find
parameter_list|(
name|BytesRef
name|key
parameter_list|)
block|{
return|return
name|find
argument_list|(
name|key
argument_list|,
name|key
operator|.
name|hashCode
argument_list|()
argument_list|)
return|;
block|}
DECL|method|set
specifier|private
name|long
name|set
parameter_list|(
name|BytesRef
name|key
parameter_list|,
name|int
name|code
parameter_list|,
name|long
name|id
parameter_list|)
block|{
assert|assert
name|rehash
argument_list|(
name|key
operator|.
name|hashCode
argument_list|()
argument_list|)
operator|==
name|code
assert|;
assert|assert
name|size
operator|<
name|maxSize
assert|;
specifier|final
name|long
name|slot
init|=
name|slot
argument_list|(
name|code
argument_list|,
name|mask
argument_list|)
decl_stmt|;
for|for
control|(
name|long
name|index
init|=
name|slot
init|;
condition|;
name|index
operator|=
name|nextSlot
argument_list|(
name|index
argument_list|,
name|mask
argument_list|)
control|)
block|{
specifier|final
name|long
name|curId
init|=
name|id
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|curId
operator|==
operator|-
literal|1
condition|)
block|{
comment|// means unset
name|id
argument_list|(
name|index
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|append
argument_list|(
name|id
argument_list|,
name|key
argument_list|,
name|code
argument_list|)
expr_stmt|;
operator|++
name|size
expr_stmt|;
return|return
name|id
return|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|bytesEquals
argument_list|(
name|get
argument_list|(
name|curId
argument_list|,
name|spare
argument_list|)
argument_list|)
condition|)
block|{
return|return
operator|-
literal|1
operator|-
name|curId
return|;
block|}
block|}
block|}
DECL|method|append
specifier|private
name|void
name|append
parameter_list|(
name|long
name|id
parameter_list|,
name|BytesRef
name|key
parameter_list|,
name|int
name|code
parameter_list|)
block|{
assert|assert
name|size
operator|==
name|id
assert|;
specifier|final
name|long
name|startOffset
init|=
name|startOffsets
operator|.
name|get
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|bytes
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|bytes
argument_list|,
name|startOffset
operator|+
name|key
operator|.
name|length
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
name|startOffset
argument_list|,
name|key
operator|.
name|bytes
argument_list|,
name|key
operator|.
name|offset
argument_list|,
name|key
operator|.
name|length
argument_list|)
expr_stmt|;
name|startOffsets
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|startOffsets
argument_list|,
name|size
operator|+
literal|2
argument_list|)
expr_stmt|;
name|startOffsets
operator|.
name|set
argument_list|(
name|size
operator|+
literal|1
argument_list|,
name|startOffset
operator|+
name|key
operator|.
name|length
argument_list|)
expr_stmt|;
name|hashes
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|hashes
argument_list|,
name|id
operator|+
literal|1
argument_list|)
expr_stmt|;
name|hashes
operator|.
name|set
argument_list|(
name|id
argument_list|,
name|code
argument_list|)
expr_stmt|;
block|}
DECL|method|assertConsistent
specifier|private
name|boolean
name|assertConsistent
parameter_list|(
name|long
name|id
parameter_list|,
name|int
name|code
parameter_list|)
block|{
name|get
argument_list|(
name|id
argument_list|,
name|spare
argument_list|)
expr_stmt|;
return|return
name|rehash
argument_list|(
name|spare
operator|.
name|hashCode
argument_list|()
argument_list|)
operator|==
name|code
return|;
block|}
DECL|method|reset
specifier|private
name|void
name|reset
parameter_list|(
name|int
name|code
parameter_list|,
name|long
name|id
parameter_list|)
block|{
assert|assert
name|assertConsistent
argument_list|(
name|id
argument_list|,
name|code
argument_list|)
assert|;
specifier|final
name|long
name|slot
init|=
name|slot
argument_list|(
name|code
argument_list|,
name|mask
argument_list|)
decl_stmt|;
for|for
control|(
name|long
name|index
init|=
name|slot
init|;
condition|;
name|index
operator|=
name|nextSlot
argument_list|(
name|index
argument_list|,
name|mask
argument_list|)
control|)
block|{
specifier|final
name|long
name|curId
init|=
name|id
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|curId
operator|==
operator|-
literal|1
condition|)
block|{
comment|// means unset
name|id
argument_list|(
name|index
argument_list|,
name|id
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
comment|/**      * Try to add<code>key</code>. Return its newly allocated id if it wasn't in the hash table yet, or</code>-1-id</code>      * if it was already present in the hash table.      */
DECL|method|add
specifier|public
name|long
name|add
parameter_list|(
name|BytesRef
name|key
parameter_list|,
name|int
name|code
parameter_list|)
block|{
if|if
condition|(
name|size
operator|>=
name|maxSize
condition|)
block|{
assert|assert
name|size
operator|==
name|maxSize
assert|;
name|grow
argument_list|()
expr_stmt|;
block|}
assert|assert
name|size
operator|<
name|maxSize
assert|;
return|return
name|set
argument_list|(
name|key
argument_list|,
name|rehash
argument_list|(
name|code
argument_list|)
argument_list|,
name|size
argument_list|)
return|;
block|}
comment|/** Sugar to {@link #add(BytesRef, int) add(key, key.hashCode()}. */
DECL|method|add
specifier|public
name|long
name|add
parameter_list|(
name|BytesRef
name|key
parameter_list|)
block|{
return|return
name|add
argument_list|(
name|key
argument_list|,
name|key
operator|.
name|hashCode
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|removeAndAdd
specifier|protected
name|void
name|removeAndAdd
parameter_list|(
name|long
name|index
parameter_list|)
block|{
specifier|final
name|long
name|id
init|=
name|id
argument_list|(
name|index
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|id
operator|>=
literal|0
assert|;
specifier|final
name|int
name|code
init|=
name|hashes
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|reset
argument_list|(
name|code
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
init|(
name|Releasable
name|releasable
init|=
name|Releasables
operator|.
name|wrap
argument_list|(
name|bytes
argument_list|,
name|hashes
argument_list|,
name|startOffsets
argument_list|)
init|)
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

