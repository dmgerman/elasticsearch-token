begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.gcommon.collect
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
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
name|gcommon
operator|.
name|annotations
operator|.
name|GwtCompatible
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|checkNotNull
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_comment
comment|/**  * An immutable collection. Does not permit null elements.  *  *<p><b>Note</b>: Although this class is not final, it cannot be subclassed  * outside of this package as it has no public or protected constructors. Thus,  * instances of this type are guaranteed to be immutable.  *  * @author Jesse Wilson  */
end_comment

begin_class
annotation|@
name|GwtCompatible
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
comment|// we're overriding default serialization
DECL|class|ImmutableCollection
specifier|public
specifier|abstract
class|class
name|ImmutableCollection
parameter_list|<
name|E
parameter_list|>
implements|implements
name|Collection
argument_list|<
name|E
argument_list|>
implements|,
name|Serializable
block|{
DECL|field|EMPTY_IMMUTABLE_COLLECTION
specifier|static
specifier|final
name|ImmutableCollection
argument_list|<
name|Object
argument_list|>
name|EMPTY_IMMUTABLE_COLLECTION
init|=
operator|new
name|EmptyImmutableCollection
argument_list|()
decl_stmt|;
DECL|method|ImmutableCollection
name|ImmutableCollection
parameter_list|()
block|{}
comment|/**    * Returns an unmodifiable iterator across the elements in this collection.    */
DECL|method|iterator
specifier|public
specifier|abstract
name|UnmodifiableIterator
argument_list|<
name|E
argument_list|>
name|iterator
parameter_list|()
function_decl|;
DECL|method|toArray
specifier|public
name|Object
index|[]
name|toArray
parameter_list|()
block|{
name|Object
index|[]
name|newArray
init|=
operator|new
name|Object
index|[
name|size
argument_list|()
index|]
decl_stmt|;
return|return
name|toArray
argument_list|(
name|newArray
argument_list|)
return|;
block|}
DECL|method|toArray
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
index|[]
name|toArray
parameter_list|(
name|T
index|[]
name|other
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
name|other
operator|.
name|length
operator|<
name|size
condition|)
block|{
name|other
operator|=
name|ObjectArrays
operator|.
name|newArray
argument_list|(
name|other
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|other
operator|.
name|length
operator|>
name|size
condition|)
block|{
name|other
index|[
name|size
index|]
operator|=
literal|null
expr_stmt|;
block|}
comment|// Writes will produce ArrayStoreException when the toArray() doc requires.
name|Object
index|[]
name|otherAsObjectArray
init|=
name|other
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|E
name|element
range|:
name|this
control|)
block|{
name|otherAsObjectArray
index|[
name|index
operator|++
index|]
operator|=
name|element
expr_stmt|;
block|}
return|return
name|other
return|;
block|}
DECL|method|contains
specifier|public
name|boolean
name|contains
parameter_list|(
annotation|@
name|Nullable
name|Object
name|object
parameter_list|)
block|{
if|if
condition|(
name|object
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|E
name|element
range|:
name|this
control|)
block|{
if|if
condition|(
name|element
operator|.
name|equals
argument_list|(
name|object
argument_list|)
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
DECL|method|containsAll
specifier|public
name|boolean
name|containsAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|targets
parameter_list|)
block|{
for|for
control|(
name|Object
name|target
range|:
name|targets
control|)
block|{
if|if
condition|(
operator|!
name|contains
argument_list|(
name|target
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
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|size
argument_list|()
operator|==
literal|0
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|size
argument_list|()
operator|*
literal|16
argument_list|)
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
decl_stmt|;
name|Collections2
operator|.
name|standardJoiner
operator|.
name|appendTo
argument_list|(
name|sb
argument_list|,
name|this
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Guaranteed to throw an exception and leave the collection unmodified.    *    * @throws UnsupportedOperationException always    */
DECL|method|add
specifier|public
specifier|final
name|boolean
name|add
parameter_list|(
name|E
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Guaranteed to throw an exception and leave the collection unmodified.    *    * @throws UnsupportedOperationException always    */
DECL|method|remove
specifier|public
specifier|final
name|boolean
name|remove
parameter_list|(
name|Object
name|object
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Guaranteed to throw an exception and leave the collection unmodified.    *    * @throws UnsupportedOperationException always    */
DECL|method|addAll
specifier|public
specifier|final
name|boolean
name|addAll
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|E
argument_list|>
name|newElements
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Guaranteed to throw an exception and leave the collection unmodified.    *    * @throws UnsupportedOperationException always    */
DECL|method|removeAll
specifier|public
specifier|final
name|boolean
name|removeAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|oldElements
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Guaranteed to throw an exception and leave the collection unmodified.    *    * @throws UnsupportedOperationException always    */
DECL|method|retainAll
specifier|public
specifier|final
name|boolean
name|retainAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|elementsToKeep
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Guaranteed to throw an exception and leave the collection unmodified.    *    * @throws UnsupportedOperationException always    */
DECL|method|clear
specifier|public
specifier|final
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
DECL|class|EmptyImmutableCollection
specifier|private
specifier|static
class|class
name|EmptyImmutableCollection
extends|extends
name|ImmutableCollection
argument_list|<
name|Object
argument_list|>
block|{
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
DECL|method|isEmpty
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|contains
annotation|@
name|Override
specifier|public
name|boolean
name|contains
parameter_list|(
annotation|@
name|Nullable
name|Object
name|object
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|UnmodifiableIterator
argument_list|<
name|Object
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Iterators
operator|.
name|EMPTY_ITERATOR
return|;
block|}
DECL|field|EMPTY_ARRAY
specifier|private
specifier|static
specifier|final
name|Object
index|[]
name|EMPTY_ARRAY
init|=
operator|new
name|Object
index|[
literal|0
index|]
decl_stmt|;
DECL|method|toArray
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|toArray
parameter_list|()
block|{
return|return
name|EMPTY_ARRAY
return|;
block|}
DECL|method|toArray
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
index|[]
name|toArray
parameter_list|(
name|T
index|[]
name|array
parameter_list|)
block|{
if|if
condition|(
name|array
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|array
index|[
literal|0
index|]
operator|=
literal|null
expr_stmt|;
block|}
return|return
name|array
return|;
block|}
block|}
DECL|class|ArrayImmutableCollection
specifier|private
specifier|static
class|class
name|ArrayImmutableCollection
parameter_list|<
name|E
parameter_list|>
extends|extends
name|ImmutableCollection
argument_list|<
name|E
argument_list|>
block|{
DECL|field|elements
specifier|private
specifier|final
name|E
index|[]
name|elements
decl_stmt|;
DECL|method|ArrayImmutableCollection
name|ArrayImmutableCollection
parameter_list|(
name|E
index|[]
name|elements
parameter_list|)
block|{
name|this
operator|.
name|elements
operator|=
name|elements
expr_stmt|;
block|}
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|elements
operator|.
name|length
return|;
block|}
DECL|method|isEmpty
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|UnmodifiableIterator
argument_list|<
name|E
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Iterators
operator|.
name|forArray
argument_list|(
name|elements
argument_list|)
return|;
block|}
block|}
comment|/*    * Serializes ImmutableCollections as their logical contents. This ensures    * that implementation types do not leak into the serialized representation.    */
DECL|class|SerializedForm
specifier|private
specifier|static
class|class
name|SerializedForm
implements|implements
name|Serializable
block|{
DECL|field|elements
specifier|final
name|Object
index|[]
name|elements
decl_stmt|;
DECL|method|SerializedForm
name|SerializedForm
parameter_list|(
name|Object
index|[]
name|elements
parameter_list|)
block|{
name|this
operator|.
name|elements
operator|=
name|elements
expr_stmt|;
block|}
DECL|method|readResolve
name|Object
name|readResolve
parameter_list|()
block|{
return|return
name|elements
operator|.
name|length
operator|==
literal|0
condition|?
name|EMPTY_IMMUTABLE_COLLECTION
else|:
operator|new
name|ArrayImmutableCollection
argument_list|<
name|Object
argument_list|>
argument_list|(
name|Platform
operator|.
name|clone
argument_list|(
name|elements
argument_list|)
argument_list|)
return|;
block|}
DECL|field|serialVersionUID
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|0
decl_stmt|;
block|}
DECL|method|writeReplace
name|Object
name|writeReplace
parameter_list|()
block|{
return|return
operator|new
name|SerializedForm
argument_list|(
name|toArray
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Abstract base class for builders of {@link ImmutableCollection} types.    */
DECL|class|Builder
specifier|abstract
specifier|static
class|class
name|Builder
parameter_list|<
name|E
parameter_list|>
block|{
comment|/**      * Adds {@code element} to the {@code ImmutableCollection} being built.      *      *<p>Note that each builder class covariantly returns its own type from      * this method.      *      * @param element the element to add      * @return this {@code Builder} instance      * @throws NullPointerException if {@code element} is null      */
DECL|method|add
specifier|public
specifier|abstract
name|Builder
argument_list|<
name|E
argument_list|>
name|add
parameter_list|(
name|E
name|element
parameter_list|)
function_decl|;
comment|/**      * Adds each element of {@code elements} to the {@code ImmutableCollection}      * being built.      *      *<p>Note that each builder class overrides this method in order to      * covariantly return its own type.      *      * @param elements the elements to add      * @return this {@code Builder} instance      * @throws NullPointerException if {@code elements} is null or contains a      *     null element      */
DECL|method|add
specifier|public
name|Builder
argument_list|<
name|E
argument_list|>
name|add
parameter_list|(
name|E
modifier|...
name|elements
parameter_list|)
block|{
name|checkNotNull
argument_list|(
name|elements
argument_list|)
expr_stmt|;
comment|// for GWT
for|for
control|(
name|E
name|element
range|:
name|elements
control|)
block|{
name|add
argument_list|(
name|element
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Adds each element of {@code elements} to the {@code ImmutableCollection}      * being built.      *      *<p>Note that each builder class overrides this method in order to      * covariantly return its own type.      *      * @param elements the elements to add      * @return this {@code Builder} instance      * @throws NullPointerException if {@code elements} is null or contains a      *     null element      */
DECL|method|addAll
specifier|public
name|Builder
argument_list|<
name|E
argument_list|>
name|addAll
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|E
argument_list|>
name|elements
parameter_list|)
block|{
name|checkNotNull
argument_list|(
name|elements
argument_list|)
expr_stmt|;
comment|// for GWT
for|for
control|(
name|E
name|element
range|:
name|elements
control|)
block|{
name|add
argument_list|(
name|element
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Adds each element of {@code elements} to the {@code ImmutableCollection}      * being built.      *      *<p>Note that each builder class overrides this method in order to      * covariantly return its own type.      *      * @param elements the elements to add      * @return this {@code Builder} instance      * @throws NullPointerException if {@code elements} is null or contains a      *     null element      */
DECL|method|addAll
specifier|public
name|Builder
argument_list|<
name|E
argument_list|>
name|addAll
parameter_list|(
name|Iterator
argument_list|<
name|?
extends|extends
name|E
argument_list|>
name|elements
parameter_list|)
block|{
name|checkNotNull
argument_list|(
name|elements
argument_list|)
expr_stmt|;
comment|// for GWT
while|while
condition|(
name|elements
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|add
argument_list|(
name|elements
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Returns a newly-created {@code ImmutableCollection} of the appropriate      * type, containing the elements provided to this builder.      *      *<p>Note that each builder class covariantly returns the appropriate type      * of {@code ImmutableCollection} from this method.      */
DECL|method|build
specifier|public
specifier|abstract
name|ImmutableCollection
argument_list|<
name|E
argument_list|>
name|build
parameter_list|()
function_decl|;
block|}
block|}
end_class

end_unit

