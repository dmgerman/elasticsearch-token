begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.gnu.trove
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gnu
operator|.
name|trove
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Externalizable
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
name|ObjectInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutput
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
comment|//////////////////////////////////////////////////
end_comment

begin_comment
comment|// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
end_comment

begin_comment
comment|//////////////////////////////////////////////////
end_comment

begin_comment
comment|/**  * An open addressed Map implementation for long keys and double values.  *<p/>  * Created: Sun Nov  4 08:52:45 2001  *  * @author Eric D. Friedman  */
end_comment

begin_class
DECL|class|TLongDoubleHashMap
specifier|public
class|class
name|TLongDoubleHashMap
extends|extends
name|TLongHash
implements|implements
name|Externalizable
block|{
DECL|field|serialVersionUID
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
DECL|field|PUT_ALL_PROC
specifier|private
specifier|final
name|TLongDoubleProcedure
name|PUT_ALL_PROC
init|=
operator|new
name|TLongDoubleProcedure
argument_list|()
block|{
specifier|public
name|boolean
name|execute
parameter_list|(
name|long
name|key
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
comment|/**      * the values of the map      */
DECL|field|_values
specifier|protected
specifier|transient
name|double
index|[]
name|_values
decl_stmt|;
comment|/**      * Creates a new<code>TLongDoubleHashMap</code> instance with the default      * capacity and load factor.      */
DECL|method|TLongDoubleHashMap
specifier|public
name|TLongDoubleHashMap
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**      * Creates a new<code>TLongDoubleHashMap</code> instance with a prime      * capacity equal to or greater than<tt>initialCapacity</tt> and      * with the default load factor.      *      * @param initialCapacity an<code>int</code> value      */
DECL|method|TLongDoubleHashMap
specifier|public
name|TLongDoubleHashMap
parameter_list|(
name|int
name|initialCapacity
parameter_list|)
block|{
name|super
argument_list|(
name|initialCapacity
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new<code>TLongDoubleHashMap</code> instance with a prime      * capacity equal to or greater than<tt>initialCapacity</tt> and      * with the specified load factor.      *      * @param initialCapacity an<code>int</code> value      * @param loadFactor      a<code>float</code> value      */
DECL|method|TLongDoubleHashMap
specifier|public
name|TLongDoubleHashMap
parameter_list|(
name|int
name|initialCapacity
parameter_list|,
name|float
name|loadFactor
parameter_list|)
block|{
name|super
argument_list|(
name|initialCapacity
argument_list|,
name|loadFactor
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new<code>TLongDoubleHashMap</code> instance with the default      * capacity and load factor.      *      * @param strategy used to compute hash codes and to compare keys.      */
DECL|method|TLongDoubleHashMap
specifier|public
name|TLongDoubleHashMap
parameter_list|(
name|TLongHashingStrategy
name|strategy
parameter_list|)
block|{
name|super
argument_list|(
name|strategy
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new<code>TLongDoubleHashMap</code> instance whose capacity      * is the next highest prime above<tt>initialCapacity + 1</tt>      * unless that value is already prime.      *      * @param initialCapacity an<code>int</code> value      * @param strategy        used to compute hash codes and to compare keys.      */
DECL|method|TLongDoubleHashMap
specifier|public
name|TLongDoubleHashMap
parameter_list|(
name|int
name|initialCapacity
parameter_list|,
name|TLongHashingStrategy
name|strategy
parameter_list|)
block|{
name|super
argument_list|(
name|initialCapacity
argument_list|,
name|strategy
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new<code>TLongDoubleHashMap</code> instance with a prime      * value at or near the specified capacity and load factor.      *      * @param initialCapacity used to find a prime capacity for the table.      * @param loadFactor      used to calculate the threshold over which      *                        rehashing takes place.      * @param strategy        used to compute hash codes and to compare keys.      */
DECL|method|TLongDoubleHashMap
specifier|public
name|TLongDoubleHashMap
parameter_list|(
name|int
name|initialCapacity
parameter_list|,
name|float
name|loadFactor
parameter_list|,
name|TLongHashingStrategy
name|strategy
parameter_list|)
block|{
name|super
argument_list|(
name|initialCapacity
argument_list|,
name|loadFactor
argument_list|,
name|strategy
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return a deep clone of this collection      */
DECL|method|clone
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|TLongDoubleHashMap
name|m
init|=
operator|(
name|TLongDoubleHashMap
operator|)
name|super
operator|.
name|clone
argument_list|()
decl_stmt|;
name|m
operator|.
name|_values
operator|=
operator|(
name|double
index|[]
operator|)
name|this
operator|.
name|_values
operator|.
name|clone
argument_list|()
expr_stmt|;
return|return
name|m
return|;
block|}
comment|/**      * @return a TLongDoubleIterator with access to this map's keys and values      */
DECL|method|iterator
specifier|public
name|TLongDoubleIterator
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|TLongDoubleIterator
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**      * initializes the hashtable to a prime capacity which is at least      *<tt>initialCapacity + 1</tt>.      *      * @param initialCapacity an<code>int</code> value      * @return the actual capacity chosen      */
DECL|method|setUp
specifier|protected
name|int
name|setUp
parameter_list|(
name|int
name|initialCapacity
parameter_list|)
block|{
name|int
name|capacity
decl_stmt|;
name|capacity
operator|=
name|super
operator|.
name|setUp
argument_list|(
name|initialCapacity
argument_list|)
expr_stmt|;
name|_values
operator|=
operator|new
name|double
index|[
name|capacity
index|]
expr_stmt|;
return|return
name|capacity
return|;
block|}
comment|/**      * Inserts a key/value pair into the map.      *      * @param key   an<code>long</code> value      * @param value an<code>double</code> value      * @return the previous value associated with<tt>key</tt>,      *         or (long)0 if none was found.      */
DECL|method|put
specifier|public
name|double
name|put
parameter_list|(
name|long
name|key
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|int
name|index
init|=
name|insertionIndex
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|doPut
argument_list|(
name|key
argument_list|,
name|value
argument_list|,
name|index
argument_list|)
return|;
block|}
comment|/**      * Inserts a key/value pair into the map if the specified key is not already      * associated with a value.      *      * @param key   an<code>long</code> value      * @param value an<code>double</code> value      * @return the previous value associated with<tt>key</tt>,      *         or (long)0 if none was found.      */
DECL|method|putIfAbsent
specifier|public
name|double
name|putIfAbsent
parameter_list|(
name|long
name|key
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|int
name|index
init|=
name|insertionIndex
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|<
literal|0
condition|)
return|return
name|_values
index|[
operator|-
name|index
operator|-
literal|1
index|]
return|;
return|return
name|doPut
argument_list|(
name|key
argument_list|,
name|value
argument_list|,
name|index
argument_list|)
return|;
block|}
DECL|method|doPut
specifier|private
name|double
name|doPut
parameter_list|(
name|long
name|key
parameter_list|,
name|double
name|value
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|byte
name|previousState
decl_stmt|;
name|double
name|previous
init|=
operator|(
name|double
operator|)
literal|0
decl_stmt|;
name|boolean
name|isNewMapping
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|index
operator|<
literal|0
condition|)
block|{
name|index
operator|=
operator|-
name|index
operator|-
literal|1
expr_stmt|;
name|previous
operator|=
name|_values
index|[
name|index
index|]
expr_stmt|;
name|isNewMapping
operator|=
literal|false
expr_stmt|;
block|}
name|previousState
operator|=
name|_states
index|[
name|index
index|]
expr_stmt|;
name|_set
index|[
name|index
index|]
operator|=
name|key
expr_stmt|;
name|_states
index|[
name|index
index|]
operator|=
name|FULL
expr_stmt|;
name|_values
index|[
name|index
index|]
operator|=
name|value
expr_stmt|;
if|if
condition|(
name|isNewMapping
condition|)
block|{
name|postInsertHook
argument_list|(
name|previousState
operator|==
name|FREE
argument_list|)
expr_stmt|;
block|}
return|return
name|previous
return|;
block|}
comment|/**      * Put all the entries from the given map into this map.      *      * @param map The map from which entries will be obtained to put into this map.      */
DECL|method|putAll
specifier|public
name|void
name|putAll
parameter_list|(
name|TLongDoubleHashMap
name|map
parameter_list|)
block|{
name|map
operator|.
name|forEachEntry
argument_list|(
name|PUT_ALL_PROC
argument_list|)
expr_stmt|;
block|}
comment|/**      * rehashes the map to the new capacity.      *      * @param newCapacity an<code>int</code> value      */
DECL|method|rehash
specifier|protected
name|void
name|rehash
parameter_list|(
name|int
name|newCapacity
parameter_list|)
block|{
name|int
name|oldCapacity
init|=
name|_set
operator|.
name|length
decl_stmt|;
name|long
name|oldKeys
index|[]
init|=
name|_set
decl_stmt|;
name|double
name|oldVals
index|[]
init|=
name|_values
decl_stmt|;
name|byte
name|oldStates
index|[]
init|=
name|_states
decl_stmt|;
name|_set
operator|=
operator|new
name|long
index|[
name|newCapacity
index|]
expr_stmt|;
name|_values
operator|=
operator|new
name|double
index|[
name|newCapacity
index|]
expr_stmt|;
name|_states
operator|=
operator|new
name|byte
index|[
name|newCapacity
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|oldCapacity
init|;
name|i
operator|--
operator|>
literal|0
condition|;
control|)
block|{
if|if
condition|(
name|oldStates
index|[
name|i
index|]
operator|==
name|FULL
condition|)
block|{
name|long
name|o
init|=
name|oldKeys
index|[
name|i
index|]
decl_stmt|;
name|int
name|index
init|=
name|insertionIndex
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|_set
index|[
name|index
index|]
operator|=
name|o
expr_stmt|;
name|_values
index|[
name|index
index|]
operator|=
name|oldVals
index|[
name|i
index|]
expr_stmt|;
name|_states
index|[
name|index
index|]
operator|=
name|FULL
expr_stmt|;
block|}
block|}
block|}
comment|/**      * retrieves the value for<tt>key</tt>      *      * @param key an<code>long</code> value      * @return the value of<tt>key</tt> or (long)0 if no such mapping exists.      */
DECL|method|get
specifier|public
name|double
name|get
parameter_list|(
name|long
name|key
parameter_list|)
block|{
name|int
name|index
init|=
name|index
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|index
operator|<
literal|0
condition|?
operator|(
name|double
operator|)
literal|0
else|:
name|_values
index|[
name|index
index|]
return|;
block|}
comment|/**      * Empties the map.      */
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|super
operator|.
name|clear
argument_list|()
expr_stmt|;
name|long
index|[]
name|keys
init|=
name|_set
decl_stmt|;
name|double
index|[]
name|vals
init|=
name|_values
decl_stmt|;
name|byte
index|[]
name|states
init|=
name|_states
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|_set
argument_list|,
literal|0
argument_list|,
name|_set
operator|.
name|length
argument_list|,
operator|(
name|long
operator|)
literal|0
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|_values
argument_list|,
literal|0
argument_list|,
name|_values
operator|.
name|length
argument_list|,
operator|(
name|double
operator|)
literal|0
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|_states
argument_list|,
literal|0
argument_list|,
name|_states
operator|.
name|length
argument_list|,
name|FREE
argument_list|)
expr_stmt|;
block|}
comment|/**      * Deletes a key/value pair from the map.      *      * @param key an<code>long</code> value      * @return an<code>double</code> value, or (long)0 if no mapping for key exists      */
DECL|method|remove
specifier|public
name|double
name|remove
parameter_list|(
name|long
name|key
parameter_list|)
block|{
name|double
name|prev
init|=
operator|(
name|double
operator|)
literal|0
decl_stmt|;
name|int
name|index
init|=
name|index
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|>=
literal|0
condition|)
block|{
name|prev
operator|=
name|_values
index|[
name|index
index|]
expr_stmt|;
name|removeAt
argument_list|(
name|index
argument_list|)
expr_stmt|;
comment|// clear key,state; adjust size
block|}
return|return
name|prev
return|;
block|}
comment|/**      * Compares this map with another map for equality of their stored      * entries.      *      * @param other an<code>Object</code> value      * @return a<code>boolean</code> value      */
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|TLongDoubleHashMap
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TLongDoubleHashMap
name|that
init|=
operator|(
name|TLongDoubleHashMap
operator|)
name|other
decl_stmt|;
if|if
condition|(
name|that
operator|.
name|size
argument_list|()
operator|!=
name|this
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|forEachEntry
argument_list|(
operator|new
name|EqProcedure
argument_list|(
name|that
argument_list|)
argument_list|)
return|;
block|}
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|HashProcedure
name|p
init|=
operator|new
name|HashProcedure
argument_list|()
decl_stmt|;
name|forEachEntry
argument_list|(
name|p
argument_list|)
expr_stmt|;
return|return
name|p
operator|.
name|getHashCode
argument_list|()
return|;
block|}
DECL|class|HashProcedure
specifier|private
specifier|final
class|class
name|HashProcedure
implements|implements
name|TLongDoubleProcedure
block|{
DECL|field|h
specifier|private
name|int
name|h
init|=
literal|0
decl_stmt|;
DECL|method|getHashCode
specifier|public
name|int
name|getHashCode
parameter_list|()
block|{
return|return
name|h
return|;
block|}
DECL|method|execute
specifier|public
specifier|final
name|boolean
name|execute
parameter_list|(
name|long
name|key
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|h
operator|+=
operator|(
name|_hashingStrategy
operator|.
name|computeHashCode
argument_list|(
name|key
argument_list|)
operator|^
name|HashFunctions
operator|.
name|hash
argument_list|(
name|value
argument_list|)
operator|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
DECL|class|EqProcedure
specifier|private
specifier|static
specifier|final
class|class
name|EqProcedure
implements|implements
name|TLongDoubleProcedure
block|{
DECL|field|_otherMap
specifier|private
specifier|final
name|TLongDoubleHashMap
name|_otherMap
decl_stmt|;
DECL|method|EqProcedure
name|EqProcedure
parameter_list|(
name|TLongDoubleHashMap
name|otherMap
parameter_list|)
block|{
name|_otherMap
operator|=
name|otherMap
expr_stmt|;
block|}
DECL|method|execute
specifier|public
specifier|final
name|boolean
name|execute
parameter_list|(
name|long
name|key
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|int
name|index
init|=
name|_otherMap
operator|.
name|index
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|>=
literal|0
operator|&&
name|eq
argument_list|(
name|value
argument_list|,
name|_otherMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**          * Compare two doubles for equality.          */
DECL|method|eq
specifier|private
specifier|final
name|boolean
name|eq
parameter_list|(
name|double
name|v1
parameter_list|,
name|double
name|v2
parameter_list|)
block|{
return|return
name|v1
operator|==
name|v2
return|;
block|}
block|}
comment|/**      * removes the mapping at<tt>index</tt> from the map.      *      * @param index an<code>int</code> value      */
DECL|method|removeAt
specifier|protected
name|void
name|removeAt
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|_values
index|[
name|index
index|]
operator|=
operator|(
name|double
operator|)
literal|0
expr_stmt|;
name|super
operator|.
name|removeAt
argument_list|(
name|index
argument_list|)
expr_stmt|;
comment|// clear key, state; adjust size
block|}
comment|/**      * Returns the values of the map.      *      * @return a<code>Collection</code> value      */
DECL|method|getValues
specifier|public
name|double
index|[]
name|getValues
parameter_list|()
block|{
name|double
index|[]
name|vals
init|=
operator|new
name|double
index|[
name|size
argument_list|()
index|]
decl_stmt|;
name|double
index|[]
name|v
init|=
name|_values
decl_stmt|;
name|byte
index|[]
name|states
init|=
name|_states
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|v
operator|.
name|length
init|,
name|j
init|=
literal|0
init|;
name|i
operator|--
operator|>
literal|0
condition|;
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
operator|==
name|FULL
condition|)
block|{
name|vals
index|[
name|j
operator|++
index|]
operator|=
name|v
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
return|return
name|vals
return|;
block|}
comment|/**      * returns the keys of the map.      *      * @return a<code>Set</code> value      */
DECL|method|keys
specifier|public
name|long
index|[]
name|keys
parameter_list|()
block|{
name|long
index|[]
name|keys
init|=
operator|new
name|long
index|[
name|size
argument_list|()
index|]
decl_stmt|;
name|long
index|[]
name|k
init|=
name|_set
decl_stmt|;
name|byte
index|[]
name|states
init|=
name|_states
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|k
operator|.
name|length
init|,
name|j
init|=
literal|0
init|;
name|i
operator|--
operator|>
literal|0
condition|;
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
operator|==
name|FULL
condition|)
block|{
name|keys
index|[
name|j
operator|++
index|]
operator|=
name|k
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
return|return
name|keys
return|;
block|}
comment|/**      * returns the keys of the map.      *      * @param a the array into which the elements of the list are to      *          be stored, if it is big enough; otherwise, a new array of the      *          same type is allocated for this purpose.      * @return a<code>Set</code> value      */
DECL|method|keys
specifier|public
name|long
index|[]
name|keys
parameter_list|(
name|long
index|[]
name|a
parameter_list|)
block|{
name|int
name|size
init|=
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|a
operator|.
name|length
operator|<
name|size
condition|)
block|{
name|a
operator|=
operator|(
name|long
index|[]
operator|)
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Array
operator|.
name|newInstance
argument_list|(
name|a
operator|.
name|getClass
argument_list|()
operator|.
name|getComponentType
argument_list|()
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
name|long
index|[]
name|k
init|=
operator|(
name|long
index|[]
operator|)
name|_set
decl_stmt|;
name|byte
index|[]
name|states
init|=
name|_states
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|k
operator|.
name|length
init|,
name|j
init|=
literal|0
init|;
name|i
operator|--
operator|>
literal|0
condition|;
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
operator|==
name|FULL
condition|)
block|{
name|a
index|[
name|j
operator|++
index|]
operator|=
name|k
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
return|return
name|a
return|;
block|}
comment|/**      * checks for the presence of<tt>val</tt> in the values of the map.      *      * @param val an<code>double</code> value      * @return a<code>boolean</code> value      */
DECL|method|containsValue
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|double
name|val
parameter_list|)
block|{
name|byte
index|[]
name|states
init|=
name|_states
decl_stmt|;
name|double
index|[]
name|vals
init|=
name|_values
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|vals
operator|.
name|length
init|;
name|i
operator|--
operator|>
literal|0
condition|;
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
operator|==
name|FULL
operator|&&
name|val
operator|==
name|vals
index|[
name|i
index|]
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**      * checks for the present of<tt>key</tt> in the keys of the map.      *      * @param key an<code>long</code> value      * @return a<code>boolean</code> value      */
DECL|method|containsKey
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|long
name|key
parameter_list|)
block|{
return|return
name|contains
argument_list|(
name|key
argument_list|)
return|;
block|}
comment|/**      * Executes<tt>procedure</tt> for each key in the map.      *      * @param procedure a<code>TLongProcedure</code> value      * @return false if the loop over the keys terminated because      *         the procedure returned false for some key.      */
DECL|method|forEachKey
specifier|public
name|boolean
name|forEachKey
parameter_list|(
name|TLongProcedure
name|procedure
parameter_list|)
block|{
return|return
name|forEach
argument_list|(
name|procedure
argument_list|)
return|;
block|}
comment|/**      * Executes<tt>procedure</tt> for each value in the map.      *      * @param procedure a<code>TDoubleProcedure</code> value      * @return false if the loop over the values terminated because      *         the procedure returned false for some value.      */
DECL|method|forEachValue
specifier|public
name|boolean
name|forEachValue
parameter_list|(
name|TDoubleProcedure
name|procedure
parameter_list|)
block|{
name|byte
index|[]
name|states
init|=
name|_states
decl_stmt|;
name|double
index|[]
name|values
init|=
name|_values
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|values
operator|.
name|length
init|;
name|i
operator|--
operator|>
literal|0
condition|;
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
operator|==
name|FULL
operator|&&
operator|!
name|procedure
operator|.
name|execute
argument_list|(
name|values
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Executes<tt>procedure</tt> for each key/value entry in the      * map.      *      * @param procedure a<code>TOLongDoubleProcedure</code> value      * @return false if the loop over the entries terminated because      *         the procedure returned false for some entry.      */
DECL|method|forEachEntry
specifier|public
name|boolean
name|forEachEntry
parameter_list|(
name|TLongDoubleProcedure
name|procedure
parameter_list|)
block|{
name|byte
index|[]
name|states
init|=
name|_states
decl_stmt|;
name|long
index|[]
name|keys
init|=
name|_set
decl_stmt|;
name|double
index|[]
name|values
init|=
name|_values
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|keys
operator|.
name|length
init|;
name|i
operator|--
operator|>
literal|0
condition|;
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
operator|==
name|FULL
operator|&&
operator|!
name|procedure
operator|.
name|execute
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
name|values
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Retains only those entries in the map for which the procedure      * returns a true value.      *      * @param procedure determines which entries to keep      * @return true if the map was modified.      */
DECL|method|retainEntries
specifier|public
name|boolean
name|retainEntries
parameter_list|(
name|TLongDoubleProcedure
name|procedure
parameter_list|)
block|{
name|boolean
name|modified
init|=
literal|false
decl_stmt|;
name|byte
index|[]
name|states
init|=
name|_states
decl_stmt|;
name|long
index|[]
name|keys
init|=
name|_set
decl_stmt|;
name|double
index|[]
name|values
init|=
name|_values
decl_stmt|;
comment|// Temporarily disable compaction. This is a fix for bug #1738760
name|tempDisableAutoCompaction
argument_list|()
expr_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
name|keys
operator|.
name|length
init|;
name|i
operator|--
operator|>
literal|0
condition|;
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
operator|==
name|FULL
operator|&&
operator|!
name|procedure
operator|.
name|execute
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
name|values
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|removeAt
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|modified
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|reenableAutoCompaction
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|modified
return|;
block|}
comment|/**      * Transform the values in this map using<tt>function</tt>.      *      * @param function a<code>TDoubleFunction</code> value      */
DECL|method|transformValues
specifier|public
name|void
name|transformValues
parameter_list|(
name|TDoubleFunction
name|function
parameter_list|)
block|{
name|byte
index|[]
name|states
init|=
name|_states
decl_stmt|;
name|double
index|[]
name|values
init|=
name|_values
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|values
operator|.
name|length
init|;
name|i
operator|--
operator|>
literal|0
condition|;
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
operator|==
name|FULL
condition|)
block|{
name|values
index|[
name|i
index|]
operator|=
name|function
operator|.
name|execute
argument_list|(
name|values
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Increments the primitive value mapped to key by 1      *      * @param key the key of the value to increment      * @return true if a mapping was found and modified.      */
DECL|method|increment
specifier|public
name|boolean
name|increment
parameter_list|(
name|long
name|key
parameter_list|)
block|{
return|return
name|adjustValue
argument_list|(
name|key
argument_list|,
operator|(
name|double
operator|)
literal|1
argument_list|)
return|;
block|}
comment|/**      * Adjusts the primitive value mapped to key.      *      * @param key    the key of the value to increment      * @param amount the amount to adjust the value by.      * @return true if a mapping was found and modified.      */
DECL|method|adjustValue
specifier|public
name|boolean
name|adjustValue
parameter_list|(
name|long
name|key
parameter_list|,
name|double
name|amount
parameter_list|)
block|{
name|int
name|index
init|=
name|index
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|<
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
name|_values
index|[
name|index
index|]
operator|+=
name|amount
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
comment|/**      * Adjusts the primitive value mapped to the key if the key is present in the map.      * Otherwise, the<tt>initial_value</tt> is put in the map.      *      * @param key           the key of the value to increment      * @param adjust_amount the amount to adjust the value by      * @param put_amount    the value put into the map if the key is not initial present      * @return the value present in the map after the adjustment or put operation      * @since 2.0b1      */
DECL|method|adjustOrPutValue
specifier|public
name|double
name|adjustOrPutValue
parameter_list|(
specifier|final
name|long
name|key
parameter_list|,
specifier|final
name|double
name|adjust_amount
parameter_list|,
specifier|final
name|double
name|put_amount
parameter_list|)
block|{
name|int
name|index
init|=
name|insertionIndex
argument_list|(
name|key
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|isNewMapping
decl_stmt|;
specifier|final
name|double
name|newValue
decl_stmt|;
if|if
condition|(
name|index
operator|<
literal|0
condition|)
block|{
name|index
operator|=
operator|-
name|index
operator|-
literal|1
expr_stmt|;
name|newValue
operator|=
operator|(
name|_values
index|[
name|index
index|]
operator|+=
name|adjust_amount
operator|)
expr_stmt|;
name|isNewMapping
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|newValue
operator|=
operator|(
name|_values
index|[
name|index
index|]
operator|=
name|put_amount
operator|)
expr_stmt|;
name|isNewMapping
operator|=
literal|true
expr_stmt|;
block|}
name|byte
name|previousState
init|=
name|_states
index|[
name|index
index|]
decl_stmt|;
name|_set
index|[
name|index
index|]
operator|=
name|key
expr_stmt|;
name|_states
index|[
name|index
index|]
operator|=
name|FULL
expr_stmt|;
if|if
condition|(
name|isNewMapping
condition|)
block|{
name|postInsertHook
argument_list|(
name|previousState
operator|==
name|FREE
argument_list|)
expr_stmt|;
block|}
return|return
name|newValue
return|;
block|}
DECL|method|writeExternal
specifier|public
name|void
name|writeExternal
parameter_list|(
name|ObjectOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
comment|// VERSION
name|out
operator|.
name|writeByte
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// NUMBER OF ENTRIES
name|out
operator|.
name|writeInt
argument_list|(
name|_size
argument_list|)
expr_stmt|;
comment|// ENTRIES
name|SerializationProcedure
name|writeProcedure
init|=
operator|new
name|SerializationProcedure
argument_list|(
name|out
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|forEachEntry
argument_list|(
name|writeProcedure
argument_list|)
condition|)
block|{
throw|throw
name|writeProcedure
operator|.
name|exception
throw|;
block|}
block|}
DECL|method|readExternal
specifier|public
name|void
name|readExternal
parameter_list|(
name|ObjectInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
comment|// VERSION
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
comment|// NUMBER OF ENTRIES
name|int
name|size
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|setUp
argument_list|(
name|size
argument_list|)
expr_stmt|;
comment|// ENTRIES
while|while
condition|(
name|size
operator|--
operator|>
literal|0
condition|)
block|{
name|long
name|key
init|=
name|in
operator|.
name|readLong
argument_list|()
decl_stmt|;
name|double
name|val
init|=
name|in
operator|.
name|readDouble
argument_list|()
decl_stmt|;
name|put
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
specifier|final
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"{"
argument_list|)
decl_stmt|;
name|forEachEntry
argument_list|(
operator|new
name|TLongDoubleProcedure
argument_list|()
block|{
specifier|private
name|boolean
name|first
init|=
literal|true
decl_stmt|;
specifier|public
name|boolean
name|execute
parameter_list|(
name|long
name|key
parameter_list|,
name|double
name|value
parameter_list|)
block|{
if|if
condition|(
name|first
condition|)
name|first
operator|=
literal|false
expr_stmt|;
else|else
name|buf
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

begin_comment
comment|// TLongDoubleHashMap
end_comment

end_unit

