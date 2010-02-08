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
comment|/**  * An open addressed set implementation for short primitives.  *  * @author Eric D. Friedman  * @author Rob Eden  */
end_comment

begin_class
DECL|class|TShortHashSet
specifier|public
class|class
name|TShortHashSet
extends|extends
name|TShortHash
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
comment|/**      * Creates a new<code>TShortHashSet</code> instance with the default      * capacity and load factor.      */
DECL|method|TShortHashSet
specifier|public
name|TShortHashSet
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**      * Creates a new<code>TShortHashSet</code> instance with a prime      * capacity equal to or greater than<tt>initialCapacity</tt> and      * with the default load factor.      *      * @param initialCapacity an<code>int</code> value      */
DECL|method|TShortHashSet
specifier|public
name|TShortHashSet
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
comment|/**      * Creates a new<code>TShortHashSet</code> instance with a prime      * capacity equal to or greater than<tt>initialCapacity</tt> and      * with the specified load factor.      *      * @param initialCapacity an<code>int</code> value      * @param loadFactor      a<code>float</code> value      */
DECL|method|TShortHashSet
specifier|public
name|TShortHashSet
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
comment|/**      * Creates a new<code>TShortHashSet</code> instance containing the      * elements of<tt>array</tt>.      *      * @param array an array of<code>short</code> primitives      */
DECL|method|TShortHashSet
specifier|public
name|TShortHashSet
parameter_list|(
name|short
index|[]
name|array
parameter_list|)
block|{
name|this
argument_list|(
name|array
operator|.
name|length
argument_list|)
expr_stmt|;
name|addAll
argument_list|(
name|array
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new<code>TShortHash</code> instance with the default      * capacity and load factor.      *      * @param strategy used to compute hash codes and to compare keys.      */
DECL|method|TShortHashSet
specifier|public
name|TShortHashSet
parameter_list|(
name|TShortHashingStrategy
name|strategy
parameter_list|)
block|{
name|super
argument_list|(
name|strategy
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new<code>TShortHash</code> instance whose capacity      * is the next highest prime above<tt>initialCapacity + 1</tt>      * unless that value is already prime.      *      * @param initialCapacity an<code>int</code> value      * @param strategy        used to compute hash codes and to compare keys.      */
DECL|method|TShortHashSet
specifier|public
name|TShortHashSet
parameter_list|(
name|int
name|initialCapacity
parameter_list|,
name|TShortHashingStrategy
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
comment|/**      * Creates a new<code>TShortHash</code> instance with a prime      * value at or near the specified capacity and load factor.      *      * @param initialCapacity used to find a prime capacity for the table.      * @param loadFactor      used to calculate the threshold over which      *                        rehashing takes place.      * @param strategy        used to compute hash codes and to compare keys.      */
DECL|method|TShortHashSet
specifier|public
name|TShortHashSet
parameter_list|(
name|int
name|initialCapacity
parameter_list|,
name|float
name|loadFactor
parameter_list|,
name|TShortHashingStrategy
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
comment|/**      * Creates a new<code>TShortHashSet</code> instance containing the      * elements of<tt>array</tt>.      *      * @param array    an array of<code>short</code> primitives      * @param strategy used to compute hash codes and to compare keys.      */
DECL|method|TShortHashSet
specifier|public
name|TShortHashSet
parameter_list|(
name|short
index|[]
name|array
parameter_list|,
name|TShortHashingStrategy
name|strategy
parameter_list|)
block|{
name|this
argument_list|(
name|array
operator|.
name|length
argument_list|,
name|strategy
argument_list|)
expr_stmt|;
name|addAll
argument_list|(
name|array
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return a TShortIterator with access to the values in this set      */
DECL|method|iterator
specifier|public
name|TShortIterator
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|TShortIterator
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**      * Inserts a value into the set.      *      * @param val an<code>short</code> value      * @return true if the set was modified by the add operation      */
DECL|method|add
specifier|public
name|boolean
name|add
parameter_list|(
name|short
name|val
parameter_list|)
block|{
name|int
name|index
init|=
name|insertionIndex
argument_list|(
name|val
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
comment|// already present in set, nothing to add
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
name|val
expr_stmt|;
name|_states
index|[
name|index
index|]
operator|=
name|FULL
expr_stmt|;
name|postInsertHook
argument_list|(
name|previousState
operator|==
name|FREE
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
comment|// yes, we added something
block|}
comment|/**      * Expands the set to accommodate new values.      *      * @param newCapacity an<code>int</code> value      */
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
name|short
name|oldSet
index|[]
init|=
name|_set
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
name|short
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
name|short
name|o
init|=
name|oldSet
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
comment|/**      * Returns a new array containing the values in the set.      *      * @return an<code>short[]</code> value      */
DECL|method|toArray
specifier|public
name|short
index|[]
name|toArray
parameter_list|()
block|{
name|short
index|[]
name|result
init|=
operator|new
name|short
index|[
name|size
argument_list|()
index|]
decl_stmt|;
name|short
index|[]
name|set
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
name|states
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
name|result
index|[
name|j
operator|++
index|]
operator|=
name|set
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**      * Empties the set.      */
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
name|short
index|[]
name|set
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
name|set
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
name|set
index|[
name|i
index|]
operator|=
operator|(
name|short
operator|)
literal|0
expr_stmt|;
name|states
index|[
name|i
index|]
operator|=
name|FREE
expr_stmt|;
block|}
block|}
comment|/**      * Compares this set with another set for equality of their stored      * entries.      *      * @param other an<code>Object</code> value      * @return a<code>boolean</code> value      */
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
name|TShortHashSet
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
specifier|final
name|TShortHashSet
name|that
init|=
operator|(
name|TShortHashSet
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
name|forEach
argument_list|(
operator|new
name|TShortProcedure
argument_list|()
block|{
specifier|public
specifier|final
name|boolean
name|execute
parameter_list|(
name|short
name|value
parameter_list|)
block|{
return|return
name|that
operator|.
name|contains
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
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
name|forEach
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
name|TShortProcedure
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
name|short
name|key
parameter_list|)
block|{
name|h
operator|+=
name|_hashingStrategy
operator|.
name|computeHashCode
argument_list|(
name|key
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
comment|/**      * Removes<tt>val</tt> from the set.      *      * @param val an<code>short</code> value      * @return true if the set was modified by the remove operation.      */
DECL|method|remove
specifier|public
name|boolean
name|remove
parameter_list|(
name|short
name|val
parameter_list|)
block|{
name|int
name|index
init|=
name|index
argument_list|(
name|val
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|>=
literal|0
condition|)
block|{
name|removeAt
argument_list|(
name|index
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**      * Tests the set to determine if all of the elements in      *<tt>array</tt> are present.      *      * @param array an<code>array</code> of short primitives.      * @return true if all elements were present in the set.      */
DECL|method|containsAll
specifier|public
name|boolean
name|containsAll
parameter_list|(
name|short
index|[]
name|array
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
name|array
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
operator|!
name|contains
argument_list|(
name|array
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
comment|/**      * Adds all of the elements in<tt>array</tt> to the set.      *      * @param array an<code>array</code> of short primitives.      * @return true if the set was modified by the add all operation.      */
DECL|method|addAll
specifier|public
name|boolean
name|addAll
parameter_list|(
name|short
index|[]
name|array
parameter_list|)
block|{
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|array
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
name|add
argument_list|(
name|array
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|changed
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|changed
return|;
block|}
comment|/**      * Removes all of the elements in<tt>array</tt> from the set.      *      * @param array an<code>array</code> of short primitives.      * @return true if the set was modified by the remove all operation.      */
DECL|method|removeAll
specifier|public
name|boolean
name|removeAll
parameter_list|(
name|short
index|[]
name|array
parameter_list|)
block|{
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|array
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
name|remove
argument_list|(
name|array
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|changed
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|changed
return|;
block|}
comment|/**      * Removes any values in the set which are not contained in      *<tt>array</tt>.      *      * @param array an<code>array</code> of short primitives.      * @return true if the set was modified by the retain all operation      */
DECL|method|retainAll
specifier|public
name|boolean
name|retainAll
parameter_list|(
name|short
index|[]
name|array
parameter_list|)
block|{
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|array
argument_list|)
expr_stmt|;
name|short
index|[]
name|set
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
name|set
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
operator|(
name|Arrays
operator|.
name|binarySearch
argument_list|(
name|array
argument_list|,
name|set
index|[
name|i
index|]
argument_list|)
operator|<
literal|0
operator|)
condition|)
block|{
name|remove
argument_list|(
name|set
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|changed
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|changed
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
name|forEach
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
comment|// ENTRIES
name|setUp
argument_list|(
name|size
argument_list|)
expr_stmt|;
while|while
condition|(
name|size
operator|--
operator|>
literal|0
condition|)
block|{
name|short
name|val
init|=
name|in
operator|.
name|readShort
argument_list|()
decl_stmt|;
name|add
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

begin_comment
comment|// TShortHashSet
end_comment

end_unit

