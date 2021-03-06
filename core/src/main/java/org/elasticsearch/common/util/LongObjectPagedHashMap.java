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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_comment
comment|/**  * A hash table from native longs to objects. This implementation resolves collisions  * using open-addressing and does not support null values. This class is not thread-safe.  */
end_comment

begin_class
DECL|class|LongObjectPagedHashMap
specifier|public
class|class
name|LongObjectPagedHashMap
parameter_list|<
name|T
parameter_list|>
extends|extends
name|AbstractPagedHashMap
implements|implements
name|Iterable
argument_list|<
name|LongObjectPagedHashMap
operator|.
name|Cursor
argument_list|<
name|T
argument_list|>
argument_list|>
block|{
DECL|field|keys
specifier|private
name|LongArray
name|keys
decl_stmt|;
DECL|field|values
specifier|private
name|ObjectArray
argument_list|<
name|T
argument_list|>
name|values
decl_stmt|;
DECL|method|LongObjectPagedHashMap
specifier|public
name|LongObjectPagedHashMap
parameter_list|(
name|BigArrays
name|bigArrays
parameter_list|)
block|{
name|this
argument_list|(
literal|16
argument_list|,
name|bigArrays
argument_list|)
expr_stmt|;
block|}
DECL|method|LongObjectPagedHashMap
specifier|public
name|LongObjectPagedHashMap
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
DECL|method|LongObjectPagedHashMap
specifier|public
name|LongObjectPagedHashMap
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
name|keys
operator|=
name|bigArrays
operator|.
name|newLongArray
argument_list|(
name|capacity
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|values
operator|=
name|bigArrays
operator|.
name|newObjectArray
argument_list|(
name|capacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Get the value that is associated with<code>key</code> or null if<code>key</code>      * was not present in the hash table.      */
DECL|method|get
specifier|public
name|T
name|get
parameter_list|(
name|long
name|key
parameter_list|)
block|{
for|for
control|(
name|long
name|i
init|=
name|slot
argument_list|(
name|hash
argument_list|(
name|key
argument_list|)
argument_list|,
name|mask
argument_list|)
init|;
condition|;
name|i
operator|=
name|nextSlot
argument_list|(
name|i
argument_list|,
name|mask
argument_list|)
control|)
block|{
specifier|final
name|T
name|value
init|=
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|keys
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
name|key
condition|)
block|{
return|return
name|value
return|;
block|}
block|}
block|}
comment|/**      * Put this new (key, value) pair into this hash table and return the value      * that was previously associated with<code>key</code> or null in case of      * an insertion.      */
DECL|method|put
specifier|public
name|T
name|put
parameter_list|(
name|long
name|key
parameter_list|,
name|T
name|value
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
name|value
argument_list|)
return|;
block|}
comment|/**      * Remove the entry which has this key in the hash table and return the      * associated value or null if there was no entry associated with this key.      */
DECL|method|remove
specifier|public
name|T
name|remove
parameter_list|(
name|long
name|key
parameter_list|)
block|{
for|for
control|(
name|long
name|i
init|=
name|slot
argument_list|(
name|hash
argument_list|(
name|key
argument_list|)
argument_list|,
name|mask
argument_list|)
init|;
condition|;
name|i
operator|=
name|nextSlot
argument_list|(
name|i
argument_list|,
name|mask
argument_list|)
control|)
block|{
specifier|final
name|T
name|previous
init|=
name|values
operator|.
name|set
argument_list|(
name|i
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|previous
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|keys
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
name|key
condition|)
block|{
operator|--
name|size
expr_stmt|;
for|for
control|(
name|long
name|j
init|=
name|nextSlot
argument_list|(
name|i
argument_list|,
name|mask
argument_list|)
init|;
name|used
argument_list|(
name|j
argument_list|)
condition|;
name|j
operator|=
name|nextSlot
argument_list|(
name|j
argument_list|,
name|mask
argument_list|)
control|)
block|{
name|removeAndAdd
argument_list|(
name|j
argument_list|)
expr_stmt|;
block|}
return|return
name|previous
return|;
block|}
else|else
block|{
comment|// repair and continue
name|values
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|previous
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|set
specifier|private
name|T
name|set
parameter_list|(
name|long
name|key
parameter_list|,
name|T
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Null values are not supported"
argument_list|)
throw|;
block|}
for|for
control|(
name|long
name|i
init|=
name|slot
argument_list|(
name|hash
argument_list|(
name|key
argument_list|)
argument_list|,
name|mask
argument_list|)
init|;
condition|;
name|i
operator|=
name|nextSlot
argument_list|(
name|i
argument_list|,
name|mask
argument_list|)
control|)
block|{
specifier|final
name|T
name|previous
init|=
name|values
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|previous
operator|==
literal|null
condition|)
block|{
comment|// slot was free
name|keys
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|key
argument_list|)
expr_stmt|;
operator|++
name|size
expr_stmt|;
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|key
operator|==
name|keys
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|)
block|{
comment|// we just updated the value
return|return
name|previous
return|;
block|}
else|else
block|{
comment|// not the right key, repair and continue
name|values
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|previous
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Cursor
argument_list|<
name|T
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|Cursor
argument_list|<
name|T
argument_list|>
argument_list|>
argument_list|()
block|{
name|boolean
name|cached
decl_stmt|;
specifier|final
name|Cursor
argument_list|<
name|T
argument_list|>
name|cursor
decl_stmt|;
block|{
name|cursor
operator|=
operator|new
name|Cursor
argument_list|<>
argument_list|()
expr_stmt|;
name|cursor
operator|.
name|index
operator|=
operator|-
literal|1
expr_stmt|;
name|cached
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
operator|!
name|cached
condition|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
operator|++
name|cursor
operator|.
name|index
expr_stmt|;
if|if
condition|(
name|cursor
operator|.
name|index
operator|>=
name|capacity
argument_list|()
condition|)
block|{
break|break;
block|}
elseif|else
if|if
condition|(
name|used
argument_list|(
name|cursor
operator|.
name|index
argument_list|)
condition|)
block|{
name|cursor
operator|.
name|key
operator|=
name|keys
operator|.
name|get
argument_list|(
name|cursor
operator|.
name|index
argument_list|)
expr_stmt|;
name|cursor
operator|.
name|value
operator|=
name|values
operator|.
name|get
argument_list|(
name|cursor
operator|.
name|index
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|cached
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|cursor
operator|.
name|index
operator|<
name|capacity
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cursor
argument_list|<
name|T
argument_list|>
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
name|cached
operator|=
literal|false
expr_stmt|;
return|return
name|cursor
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|keys
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|resize
specifier|protected
name|void
name|resize
parameter_list|(
name|long
name|capacity
parameter_list|)
block|{
name|keys
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|keys
argument_list|,
name|capacity
argument_list|)
expr_stmt|;
name|values
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|values
argument_list|,
name|capacity
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|used
specifier|protected
name|boolean
name|used
parameter_list|(
name|long
name|bucket
parameter_list|)
block|{
return|return
name|values
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
operator|!=
literal|null
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
name|key
init|=
name|keys
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
specifier|final
name|T
name|value
init|=
name|values
operator|.
name|set
argument_list|(
name|index
argument_list|,
literal|null
argument_list|)
decl_stmt|;
operator|--
name|size
expr_stmt|;
specifier|final
name|T
name|removed
init|=
name|set
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
decl_stmt|;
assert|assert
name|removed
operator|==
literal|null
assert|;
block|}
DECL|class|Cursor
specifier|public
specifier|static
specifier|final
class|class
name|Cursor
parameter_list|<
name|T
parameter_list|>
block|{
DECL|field|index
specifier|public
name|long
name|index
decl_stmt|;
DECL|field|key
specifier|public
name|long
name|key
decl_stmt|;
DECL|field|value
specifier|public
name|T
name|value
decl_stmt|;
block|}
block|}
end_class

end_unit

