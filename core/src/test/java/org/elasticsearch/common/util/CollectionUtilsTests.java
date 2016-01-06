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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRefArray
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
name|BytesRefBuilder
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
name|Counter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|CollectionUtils
operator|.
name|eagerPartition
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_class
DECL|class|CollectionUtilsTests
specifier|public
class|class
name|CollectionUtilsTests
extends|extends
name|ESTestCase
block|{
DECL|method|testRotateEmpty
specifier|public
name|void
name|testRotateEmpty
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|CollectionUtils
operator|.
name|rotate
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
name|randomInt
argument_list|()
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRotate
specifier|public
name|void
name|testRotate
parameter_list|()
block|{
specifier|final
name|int
name|iters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|iters
condition|;
operator|++
name|k
control|)
block|{
specifier|final
name|int
name|size
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
specifier|final
name|int
name|distance
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
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
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
operator|++
name|i
control|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|Object
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|rotated
init|=
name|CollectionUtils
operator|.
name|rotate
argument_list|(
name|list
argument_list|,
name|distance
argument_list|)
decl_stmt|;
comment|// check content is the same
name|assertEquals
argument_list|(
name|rotated
operator|.
name|size
argument_list|()
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rotated
operator|.
name|size
argument_list|()
argument_list|,
name|list
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|rotated
argument_list|)
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|list
argument_list|)
argument_list|)
expr_stmt|;
comment|// check stability
for|for
control|(
name|int
name|j
init|=
name|randomInt
argument_list|(
literal|4
argument_list|)
init|;
name|j
operator|>=
literal|0
condition|;
operator|--
name|j
control|)
block|{
name|assertEquals
argument_list|(
name|rotated
argument_list|,
name|CollectionUtils
operator|.
name|rotate
argument_list|(
name|list
argument_list|,
name|distance
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// reverse
if|if
condition|(
name|distance
operator|!=
name|Integer
operator|.
name|MIN_VALUE
condition|)
block|{
name|assertEquals
argument_list|(
name|list
argument_list|,
name|CollectionUtils
operator|.
name|rotate
argument_list|(
name|CollectionUtils
operator|.
name|rotate
argument_list|(
name|list
argument_list|,
name|distance
argument_list|)
argument_list|,
operator|-
name|distance
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testSortAndDedupByteRefArray
specifier|public
name|void
name|testSortAndDedupByteRefArray
parameter_list|()
block|{
name|SortedSet
argument_list|<
name|BytesRef
argument_list|>
name|set
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numValues
init|=
name|scaledRandomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|BytesRef
argument_list|>
name|tmpList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|BytesRefArray
name|array
init|=
operator|new
name|BytesRefArray
argument_list|(
name|Counter
operator|.
name|newCounter
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numValues
condition|;
name|i
operator|++
control|)
block|{
name|String
name|s
init|=
name|randomRealisticUnicodeOfCodepointLengthBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|set
operator|.
name|add
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
name|tmpList
operator|.
name|add
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
name|array
operator|.
name|append
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|Collections
operator|.
name|shuffle
argument_list|(
name|tmpList
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|BytesRef
name|ref
range|:
name|tmpList
control|)
block|{
name|array
operator|.
name|append
argument_list|(
name|ref
argument_list|)
expr_stmt|;
block|}
block|}
name|int
index|[]
name|indices
init|=
operator|new
name|int
index|[
name|array
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indices
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
name|int
name|numUnique
init|=
name|CollectionUtils
operator|.
name|sortAndDedup
argument_list|(
name|array
argument_list|,
name|indices
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|numUnique
argument_list|,
name|equalTo
argument_list|(
name|set
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|BytesRef
argument_list|>
name|iterator
init|=
name|set
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|BytesRefBuilder
name|spare
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numUnique
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|iterator
operator|.
name|hasNext
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|array
operator|.
name|get
argument_list|(
name|spare
argument_list|,
name|indices
index|[
name|i
index|]
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|iterator
operator|.
name|next
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSortByteRefArray
specifier|public
name|void
name|testSortByteRefArray
parameter_list|()
block|{
name|List
argument_list|<
name|BytesRef
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numValues
init|=
name|scaledRandomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|BytesRefArray
name|array
init|=
operator|new
name|BytesRefArray
argument_list|(
name|Counter
operator|.
name|newCounter
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numValues
condition|;
name|i
operator|++
control|)
block|{
name|String
name|s
init|=
name|randomRealisticUnicodeOfCodepointLengthBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|values
operator|.
name|add
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
name|array
operator|.
name|append
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|Collections
operator|.
name|shuffle
argument_list|(
name|values
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
index|[]
name|indices
init|=
operator|new
name|int
index|[
name|array
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indices
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
name|CollectionUtils
operator|.
name|sort
argument_list|(
name|array
argument_list|,
name|indices
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|values
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|BytesRef
argument_list|>
name|iterator
init|=
name|values
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|BytesRefBuilder
name|spare
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|values
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|iterator
operator|.
name|hasNext
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|array
operator|.
name|get
argument_list|(
name|spare
argument_list|,
name|indices
index|[
name|i
index|]
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|iterator
operator|.
name|next
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testEmptyPartition
specifier|public
name|void
name|testEmptyPartition
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
name|eagerPartition
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimplePartition
specifier|public
name|void
name|testSimplePartition
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|3
argument_list|,
literal|4
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|,
name|eagerPartition
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|)
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingletonPartition
specifier|public
name|void
name|testSingletonPartition
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|2
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|3
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|4
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|,
name|eagerPartition
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|)
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testOversizedPartition
specifier|public
name|void
name|testOversizedPartition
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|)
argument_list|)
argument_list|,
name|eagerPartition
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|)
argument_list|,
literal|15
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPerfectPartition
specifier|public
name|void
name|testPerfectPartition
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|,
literal|6
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|7
argument_list|,
literal|8
argument_list|,
literal|9
argument_list|,
literal|10
argument_list|,
literal|11
argument_list|,
literal|12
argument_list|)
argument_list|)
argument_list|,
name|eagerPartition
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|,
literal|6
argument_list|,
literal|7
argument_list|,
literal|8
argument_list|,
literal|9
argument_list|,
literal|10
argument_list|,
literal|11
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|6
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

