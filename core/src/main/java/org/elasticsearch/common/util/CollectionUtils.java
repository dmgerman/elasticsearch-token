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
name|DoubleArrayList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|FloatArrayList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|LongArrayList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectArrayList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterators
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|*
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
name|inject
operator|.
name|Module
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_comment
comment|/** Collections-related utility methods. */
end_comment

begin_enum
DECL|enum|CollectionUtils
specifier|public
enum|enum
name|CollectionUtils
block|{
DECL|enum constant|CollectionUtils
name|CollectionUtils
block|;
DECL|method|sort
specifier|public
specifier|static
name|void
name|sort
parameter_list|(
name|LongArrayList
name|list
parameter_list|)
block|{
name|sort
argument_list|(
name|list
operator|.
name|buffer
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|sort
specifier|public
specifier|static
name|void
name|sort
parameter_list|(
specifier|final
name|long
index|[]
name|array
parameter_list|,
name|int
name|len
parameter_list|)
block|{
operator|new
name|IntroSorter
argument_list|()
block|{
name|long
name|pivot
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|swap
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
specifier|final
name|long
name|tmp
init|=
name|array
index|[
name|i
index|]
decl_stmt|;
name|array
index|[
name|i
index|]
operator|=
name|array
index|[
name|j
index|]
expr_stmt|;
name|array
index|[
name|j
index|]
operator|=
name|tmp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|compare
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
return|return
name|Long
operator|.
name|compare
argument_list|(
name|array
index|[
name|i
index|]
argument_list|,
name|array
index|[
name|j
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setPivot
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|pivot
operator|=
name|array
index|[
name|i
index|]
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|comparePivot
parameter_list|(
name|int
name|j
parameter_list|)
block|{
return|return
name|Long
operator|.
name|compare
argument_list|(
name|pivot
argument_list|,
name|array
index|[
name|j
index|]
argument_list|)
return|;
block|}
block|}
operator|.
name|sort
argument_list|(
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
DECL|method|sortAndDedup
specifier|public
specifier|static
name|void
name|sortAndDedup
parameter_list|(
name|LongArrayList
name|list
parameter_list|)
block|{
name|list
operator|.
name|elementsCount
operator|=
name|sortAndDedup
argument_list|(
name|list
operator|.
name|buffer
argument_list|,
name|list
operator|.
name|elementsCount
argument_list|)
expr_stmt|;
block|}
comment|/** Sort and deduplicate values in-place, then return the unique element count. */
DECL|method|sortAndDedup
specifier|public
specifier|static
name|int
name|sortAndDedup
parameter_list|(
name|long
index|[]
name|array
parameter_list|,
name|int
name|len
parameter_list|)
block|{
if|if
condition|(
name|len
operator|<=
literal|1
condition|)
block|{
return|return
name|len
return|;
block|}
name|sort
argument_list|(
name|array
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|int
name|uniqueCount
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|len
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|array
index|[
name|i
index|]
operator|!=
name|array
index|[
name|i
operator|-
literal|1
index|]
condition|)
block|{
name|array
index|[
name|uniqueCount
operator|++
index|]
operator|=
name|array
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
return|return
name|uniqueCount
return|;
block|}
DECL|method|sort
specifier|public
specifier|static
name|void
name|sort
parameter_list|(
name|FloatArrayList
name|list
parameter_list|)
block|{
name|sort
argument_list|(
name|list
operator|.
name|buffer
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|sort
specifier|public
specifier|static
name|void
name|sort
parameter_list|(
specifier|final
name|float
index|[]
name|array
parameter_list|,
name|int
name|len
parameter_list|)
block|{
operator|new
name|IntroSorter
argument_list|()
block|{
name|float
name|pivot
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|swap
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
specifier|final
name|float
name|tmp
init|=
name|array
index|[
name|i
index|]
decl_stmt|;
name|array
index|[
name|i
index|]
operator|=
name|array
index|[
name|j
index|]
expr_stmt|;
name|array
index|[
name|j
index|]
operator|=
name|tmp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|compare
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
return|return
name|Float
operator|.
name|compare
argument_list|(
name|array
index|[
name|i
index|]
argument_list|,
name|array
index|[
name|j
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setPivot
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|pivot
operator|=
name|array
index|[
name|i
index|]
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|comparePivot
parameter_list|(
name|int
name|j
parameter_list|)
block|{
return|return
name|Float
operator|.
name|compare
argument_list|(
name|pivot
argument_list|,
name|array
index|[
name|j
index|]
argument_list|)
return|;
block|}
block|}
operator|.
name|sort
argument_list|(
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
DECL|method|sortAndDedup
specifier|public
specifier|static
name|void
name|sortAndDedup
parameter_list|(
name|FloatArrayList
name|list
parameter_list|)
block|{
name|list
operator|.
name|elementsCount
operator|=
name|sortAndDedup
argument_list|(
name|list
operator|.
name|buffer
argument_list|,
name|list
operator|.
name|elementsCount
argument_list|)
expr_stmt|;
block|}
comment|/** Sort and deduplicate values in-place, then return the unique element count. */
DECL|method|sortAndDedup
specifier|public
specifier|static
name|int
name|sortAndDedup
parameter_list|(
name|float
index|[]
name|array
parameter_list|,
name|int
name|len
parameter_list|)
block|{
if|if
condition|(
name|len
operator|<=
literal|1
condition|)
block|{
return|return
name|len
return|;
block|}
name|sort
argument_list|(
name|array
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|int
name|uniqueCount
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|len
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|Float
operator|.
name|compare
argument_list|(
name|array
index|[
name|i
index|]
argument_list|,
name|array
index|[
name|i
operator|-
literal|1
index|]
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|array
index|[
name|uniqueCount
operator|++
index|]
operator|=
name|array
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
return|return
name|uniqueCount
return|;
block|}
DECL|method|sort
specifier|public
specifier|static
name|void
name|sort
parameter_list|(
name|DoubleArrayList
name|list
parameter_list|)
block|{
name|sort
argument_list|(
name|list
operator|.
name|buffer
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|sort
specifier|public
specifier|static
name|void
name|sort
parameter_list|(
specifier|final
name|double
index|[]
name|array
parameter_list|,
name|int
name|len
parameter_list|)
block|{
operator|new
name|IntroSorter
argument_list|()
block|{
name|double
name|pivot
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|swap
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
specifier|final
name|double
name|tmp
init|=
name|array
index|[
name|i
index|]
decl_stmt|;
name|array
index|[
name|i
index|]
operator|=
name|array
index|[
name|j
index|]
expr_stmt|;
name|array
index|[
name|j
index|]
operator|=
name|tmp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|compare
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
return|return
name|Double
operator|.
name|compare
argument_list|(
name|array
index|[
name|i
index|]
argument_list|,
name|array
index|[
name|j
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setPivot
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|pivot
operator|=
name|array
index|[
name|i
index|]
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|comparePivot
parameter_list|(
name|int
name|j
parameter_list|)
block|{
return|return
name|Double
operator|.
name|compare
argument_list|(
name|pivot
argument_list|,
name|array
index|[
name|j
index|]
argument_list|)
return|;
block|}
block|}
operator|.
name|sort
argument_list|(
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
DECL|method|sortAndDedup
specifier|public
specifier|static
name|void
name|sortAndDedup
parameter_list|(
name|DoubleArrayList
name|list
parameter_list|)
block|{
name|list
operator|.
name|elementsCount
operator|=
name|sortAndDedup
argument_list|(
name|list
operator|.
name|buffer
argument_list|,
name|list
operator|.
name|elementsCount
argument_list|)
expr_stmt|;
block|}
comment|/** Sort and deduplicate values in-place, then return the unique element count. */
DECL|method|sortAndDedup
specifier|public
specifier|static
name|int
name|sortAndDedup
parameter_list|(
name|double
index|[]
name|array
parameter_list|,
name|int
name|len
parameter_list|)
block|{
if|if
condition|(
name|len
operator|<=
literal|1
condition|)
block|{
return|return
name|len
return|;
block|}
name|sort
argument_list|(
name|array
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|int
name|uniqueCount
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|len
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|array
index|[
name|i
index|]
argument_list|,
name|array
index|[
name|i
operator|-
literal|1
index|]
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|array
index|[
name|uniqueCount
operator|++
index|]
operator|=
name|array
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
return|return
name|uniqueCount
return|;
block|}
comment|/**      * Checks if the given array contains any elements.      *      * @param array The array to check      *      * @return false if the array contains an element, true if not or the array is null.      */
DECL|method|isEmpty
specifier|public
specifier|static
name|boolean
name|isEmpty
parameter_list|(
name|Object
index|[]
name|array
parameter_list|)
block|{
return|return
name|array
operator|==
literal|null
operator|||
name|array
operator|.
name|length
operator|==
literal|0
return|;
block|}
comment|/**      * Return a rotated view of the given list with the given distance.      */
DECL|method|rotate
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|rotate
parameter_list|(
specifier|final
name|List
argument_list|<
name|T
argument_list|>
name|list
parameter_list|,
name|int
name|distance
parameter_list|)
block|{
if|if
condition|(
name|list
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|list
return|;
block|}
name|int
name|d
init|=
name|distance
operator|%
name|list
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|d
operator|<
literal|0
condition|)
block|{
name|d
operator|+=
name|list
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|d
operator|==
literal|0
condition|)
block|{
return|return
name|list
return|;
block|}
return|return
operator|new
name|RotatedList
argument_list|<>
argument_list|(
name|list
argument_list|,
name|d
argument_list|)
return|;
block|}
DECL|method|sortAndDedup
specifier|public
specifier|static
name|void
name|sortAndDedup
parameter_list|(
specifier|final
name|ObjectArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|array
parameter_list|)
block|{
name|int
name|len
init|=
name|array
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|len
operator|>
literal|1
condition|)
block|{
name|sort
argument_list|(
name|array
argument_list|)
expr_stmt|;
name|int
name|uniqueCount
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|len
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|array
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|array
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
argument_list|)
condition|)
block|{
name|array
operator|.
name|set
argument_list|(
name|uniqueCount
operator|++
argument_list|,
name|array
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|array
operator|.
name|elementsCount
operator|=
name|uniqueCount
expr_stmt|;
block|}
block|}
DECL|method|sort
specifier|public
specifier|static
name|void
name|sort
parameter_list|(
specifier|final
name|ObjectArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|array
parameter_list|)
block|{
operator|new
name|IntroSorter
argument_list|()
block|{
name|byte
index|[]
name|pivot
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|swap
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
specifier|final
name|byte
index|[]
name|tmp
init|=
name|array
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|array
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|array
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
name|array
operator|.
name|set
argument_list|(
name|j
argument_list|,
name|tmp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|compare
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
return|return
name|compare
argument_list|(
name|array
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|array
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setPivot
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|pivot
operator|=
name|array
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|comparePivot
parameter_list|(
name|int
name|j
parameter_list|)
block|{
return|return
name|compare
argument_list|(
name|pivot
argument_list|,
name|array
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|int
name|compare
parameter_list|(
name|byte
index|[]
name|left
parameter_list|,
name|byte
index|[]
name|right
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
operator|,
name|j
operator|=
literal|0
condition|;
name|i
operator|<
name|left
operator|.
name|length
operator|&&
name|j
operator|<
name|right
operator|.
name|length
incr|;
control|i++
operator|,
control|j++)
block|{
name|int
name|a
init|=
name|left
index|[
name|i
index|]
operator|&
literal|0xFF
decl_stmt|;
name|int
name|b
init|=
name|right
index|[
name|j
index|]
operator|&
literal|0xFF
decl_stmt|;
if|if
condition|(
name|a
operator|!=
name|b
condition|)
block|{
return|return
name|a
operator|-
name|b
return|;
block|}
block|}
return|return
name|left
operator|.
name|length
operator|-
name|right
operator|.
name|length
return|;
block|}
block|}
operator|.
name|sort
argument_list|(
literal|0
argument_list|,
name|array
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|class|RotatedList
specifier|private
specifier|static
class|class
name|RotatedList
parameter_list|<
name|T
parameter_list|>
extends|extends
name|AbstractList
argument_list|<
name|T
argument_list|>
implements|implements
name|RandomAccess
block|{
DECL|field|in
specifier|private
specifier|final
name|List
argument_list|<
name|T
argument_list|>
name|in
decl_stmt|;
DECL|field|distance
specifier|private
specifier|final
name|int
name|distance
decl_stmt|;
DECL|method|RotatedList
specifier|public
name|RotatedList
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|list
parameter_list|,
name|int
name|distance
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|distance
operator|>=
literal|0
operator|&&
name|distance
operator|<
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|list
operator|instanceof
name|RandomAccess
argument_list|)
expr_stmt|;
name|this
operator|.
name|in
operator|=
name|list
expr_stmt|;
name|this
operator|.
name|distance
operator|=
name|distance
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|T
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|int
name|idx
init|=
name|distance
operator|+
name|index
decl_stmt|;
if|if
condition|(
name|idx
operator|<
literal|0
operator|||
name|idx
operator|>=
name|in
operator|.
name|size
argument_list|()
condition|)
block|{
name|idx
operator|-=
name|in
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|in
operator|.
name|get
argument_list|(
name|idx
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|in
operator|.
name|size
argument_list|()
return|;
block|}
block|}
block|;
DECL|method|sort
specifier|public
specifier|static
name|void
name|sort
parameter_list|(
specifier|final
name|BytesRefArray
name|bytes
parameter_list|,
specifier|final
name|int
index|[]
name|indices
parameter_list|)
block|{
name|sort
argument_list|(
operator|new
name|BytesRefBuilder
argument_list|()
argument_list|,
operator|new
name|BytesRefBuilder
argument_list|()
argument_list|,
name|bytes
argument_list|,
name|indices
argument_list|)
expr_stmt|;
block|}
DECL|method|sort
specifier|private
specifier|static
name|void
name|sort
parameter_list|(
specifier|final
name|BytesRefBuilder
name|scratch
parameter_list|,
specifier|final
name|BytesRefBuilder
name|scratch1
parameter_list|,
specifier|final
name|BytesRefArray
name|bytes
parameter_list|,
specifier|final
name|int
index|[]
name|indices
parameter_list|)
block|{
specifier|final
name|int
name|numValues
init|=
name|bytes
operator|.
name|size
argument_list|()
decl_stmt|;
assert|assert
name|indices
operator|.
name|length
operator|>=
name|numValues
assert|;
if|if
condition|(
name|numValues
operator|>
literal|1
condition|)
block|{
operator|new
name|InPlaceMergeSorter
argument_list|()
block|{
specifier|final
name|Comparator
argument_list|<
name|BytesRef
argument_list|>
name|comparator
init|=
name|BytesRef
operator|.
name|getUTF8SortedAsUnicodeComparator
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|int
name|compare
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
return|return
name|comparator
operator|.
name|compare
argument_list|(
name|bytes
operator|.
name|get
argument_list|(
name|scratch
argument_list|,
name|indices
index|[
name|i
index|]
argument_list|)
argument_list|,
name|bytes
operator|.
name|get
argument_list|(
name|scratch1
argument_list|,
name|indices
index|[
name|j
index|]
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|swap
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
name|int
name|value_i
init|=
name|indices
index|[
name|i
index|]
decl_stmt|;
name|indices
index|[
name|i
index|]
operator|=
name|indices
index|[
name|j
index|]
expr_stmt|;
name|indices
index|[
name|j
index|]
operator|=
name|value_i
expr_stmt|;
block|}
block|}
operator|.
name|sort
argument_list|(
literal|0
argument_list|,
name|numValues
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|sortAndDedup
specifier|public
specifier|static
name|int
name|sortAndDedup
parameter_list|(
specifier|final
name|BytesRefArray
name|bytes
parameter_list|,
specifier|final
name|int
index|[]
name|indices
parameter_list|)
block|{
specifier|final
name|BytesRefBuilder
name|scratch
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
specifier|final
name|BytesRefBuilder
name|scratch1
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numValues
init|=
name|bytes
operator|.
name|size
argument_list|()
decl_stmt|;
assert|assert
name|indices
operator|.
name|length
operator|>=
name|numValues
assert|;
if|if
condition|(
name|numValues
operator|<=
literal|1
condition|)
block|{
return|return
name|numValues
return|;
block|}
name|sort
argument_list|(
name|scratch
argument_list|,
name|scratch1
argument_list|,
name|bytes
argument_list|,
name|indices
argument_list|)
expr_stmt|;
name|int
name|uniqueCount
init|=
literal|1
decl_stmt|;
name|BytesRefBuilder
name|previous
init|=
name|scratch
decl_stmt|;
name|BytesRefBuilder
name|current
init|=
name|scratch1
decl_stmt|;
name|bytes
operator|.
name|get
argument_list|(
name|previous
argument_list|,
name|indices
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|numValues
condition|;
operator|++
name|i
control|)
block|{
name|bytes
operator|.
name|get
argument_list|(
name|current
argument_list|,
name|indices
index|[
name|i
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|previous
operator|.
name|get
argument_list|()
operator|.
name|equals
argument_list|(
name|current
operator|.
name|get
argument_list|()
argument_list|)
condition|)
block|{
name|indices
index|[
name|uniqueCount
operator|++
index|]
operator|=
name|indices
index|[
name|i
index|]
expr_stmt|;
block|}
name|BytesRefBuilder
name|tmp
init|=
name|previous
decl_stmt|;
name|previous
operator|=
name|current
expr_stmt|;
name|current
operator|=
name|tmp
expr_stmt|;
block|}
return|return
name|uniqueCount
return|;
block|}
comment|/**      * Combines multiple iterators into a single iterator.      */
DECL|method|concat
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Iterator
argument_list|<
name|T
argument_list|>
name|concat
parameter_list|(
name|Iterator
argument_list|<
name|?
extends|extends
name|T
argument_list|>
modifier|...
name|iterators
parameter_list|)
block|{
return|return
name|Iterators
operator|.
expr|<
name|T
operator|>
name|concat
argument_list|(
name|iterators
argument_list|)
return|;
block|}
DECL|method|newArrayList
specifier|public
specifier|static
parameter_list|<
name|E
parameter_list|>
name|ArrayList
argument_list|<
name|E
argument_list|>
name|newArrayList
parameter_list|(
name|E
modifier|...
name|elements
parameter_list|)
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|elements
argument_list|)
return|;
block|}
block|}
end_enum

end_unit
