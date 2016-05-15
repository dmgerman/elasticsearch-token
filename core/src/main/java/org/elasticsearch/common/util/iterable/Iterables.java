begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util.iterable
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|iterable
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|StreamSupport
import|;
end_import

begin_class
DECL|class|Iterables
specifier|public
class|class
name|Iterables
block|{
DECL|method|Iterables
specifier|public
name|Iterables
parameter_list|()
block|{     }
DECL|method|concat
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Iterable
argument_list|<
name|T
argument_list|>
name|concat
parameter_list|(
name|Iterable
argument_list|<
name|T
argument_list|>
modifier|...
name|inputs
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
return|return
operator|new
name|ConcatenatedIterable
argument_list|(
name|inputs
argument_list|)
return|;
block|}
DECL|class|ConcatenatedIterable
specifier|static
class|class
name|ConcatenatedIterable
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Iterable
argument_list|<
name|T
argument_list|>
block|{
DECL|field|inputs
specifier|private
specifier|final
name|Iterable
argument_list|<
name|T
argument_list|>
index|[]
name|inputs
decl_stmt|;
DECL|method|ConcatenatedIterable
name|ConcatenatedIterable
parameter_list|(
name|Iterable
argument_list|<
name|T
argument_list|>
index|[]
name|inputs
parameter_list|)
block|{
name|this
operator|.
name|inputs
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|inputs
argument_list|,
name|inputs
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|T
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Stream
operator|.
name|of
argument_list|(
name|inputs
argument_list|)
operator|.
name|map
argument_list|(
name|it
lambda|->
name|StreamSupport
operator|.
name|stream
argument_list|(
name|it
operator|.
name|spliterator
argument_list|()
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|reduce
argument_list|(
name|Stream
operator|::
name|concat
argument_list|)
operator|.
name|orElseGet
argument_list|(
name|Stream
operator|::
name|empty
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
comment|/** Flattens the two level {@code Iterable} into a single {@code Iterable}.  Note that this pre-caches the values from the outer {@code      *  Iterable}, but not the values from the inner one. */
DECL|method|flatten
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Iterable
argument_list|<
name|T
argument_list|>
name|flatten
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Iterable
argument_list|<
name|T
argument_list|>
argument_list|>
name|inputs
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
return|return
operator|new
name|FlattenedIterables
argument_list|<>
argument_list|(
name|inputs
argument_list|)
return|;
block|}
DECL|class|FlattenedIterables
specifier|static
class|class
name|FlattenedIterables
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Iterable
argument_list|<
name|T
argument_list|>
block|{
DECL|field|inputs
specifier|private
specifier|final
name|Iterable
argument_list|<
name|?
extends|extends
name|Iterable
argument_list|<
name|T
argument_list|>
argument_list|>
name|inputs
decl_stmt|;
DECL|method|FlattenedIterables
name|FlattenedIterables
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Iterable
argument_list|<
name|T
argument_list|>
argument_list|>
name|inputs
parameter_list|)
block|{
name|List
argument_list|<
name|Iterable
argument_list|<
name|T
argument_list|>
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Iterable
argument_list|<
name|T
argument_list|>
name|iterable
range|:
name|inputs
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|iterable
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|inputs
operator|=
name|list
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|T
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|StreamSupport
operator|.
name|stream
argument_list|(
name|inputs
operator|.
name|spliterator
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|flatMap
argument_list|(
name|s
lambda|->
name|StreamSupport
operator|.
name|stream
argument_list|(
name|s
operator|.
name|spliterator
argument_list|()
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
DECL|method|allElementsAreEqual
specifier|public
specifier|static
name|boolean
name|allElementsAreEqual
parameter_list|(
name|Iterable
argument_list|<
name|?
argument_list|>
name|left
parameter_list|,
name|Iterable
argument_list|<
name|?
argument_list|>
name|right
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|left
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|right
argument_list|)
expr_stmt|;
if|if
condition|(
name|left
operator|instanceof
name|Collection
operator|&&
name|right
operator|instanceof
name|Collection
condition|)
block|{
name|Collection
name|collection1
init|=
operator|(
name|Collection
operator|)
name|left
decl_stmt|;
name|Collection
name|collection2
init|=
operator|(
name|Collection
operator|)
name|right
decl_stmt|;
if|if
condition|(
name|collection1
operator|.
name|size
argument_list|()
operator|!=
name|collection2
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
name|Iterator
argument_list|<
name|?
argument_list|>
name|leftIt
init|=
name|left
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|?
argument_list|>
name|rightIt
init|=
name|right
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|leftIt
operator|.
name|hasNext
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|rightIt
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Object
name|o1
init|=
name|leftIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|Object
name|o2
init|=
name|rightIt
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|Objects
operator|.
name|equals
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
condition|)
block|{
continue|continue;
block|}
return|return
literal|false
return|;
block|}
return|return
operator|!
name|rightIt
operator|.
name|hasNext
argument_list|()
return|;
block|}
block|}
DECL|method|getFirst
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|getFirst
parameter_list|(
name|Iterable
argument_list|<
name|T
argument_list|>
name|collection
parameter_list|,
name|T
name|defaultValue
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|collection
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|T
argument_list|>
name|iterator
init|=
name|collection
operator|.
name|iterator
argument_list|()
decl_stmt|;
return|return
name|iterator
operator|.
name|hasNext
argument_list|()
condition|?
name|iterator
operator|.
name|next
argument_list|()
else|:
name|defaultValue
return|;
block|}
DECL|method|get
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|get
parameter_list|(
name|Iterable
argument_list|<
name|T
argument_list|>
name|iterable
parameter_list|,
name|int
name|position
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|iterable
argument_list|)
expr_stmt|;
if|if
condition|(
name|position
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"position>= 0"
argument_list|)
throw|;
block|}
if|if
condition|(
name|iterable
operator|instanceof
name|List
condition|)
block|{
name|List
argument_list|<
name|T
argument_list|>
name|list
init|=
operator|(
name|List
argument_list|<
name|T
argument_list|>
operator|)
name|iterable
decl_stmt|;
if|if
condition|(
name|position
operator|>=
name|list
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|position
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|list
operator|.
name|get
argument_list|(
name|position
argument_list|)
return|;
block|}
else|else
block|{
name|Iterator
argument_list|<
name|T
argument_list|>
name|it
init|=
name|iterable
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|position
condition|;
name|index
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|position
argument_list|)
argument_list|)
throw|;
block|}
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|position
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|it
operator|.
name|next
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

