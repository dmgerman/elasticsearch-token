begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
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
name|LongLongMap
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
name|LongLongOpenHashMap
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
name|cursors
operator|.
name|LongLongCursor
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
name|util
operator|.
name|BigArraysTests
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
name|ElasticsearchTestCase
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

begin_class
DECL|class|LongHashTests
specifier|public
class|class
name|LongHashTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|method|testDuell
specifier|public
name|void
name|testDuell
parameter_list|()
block|{
specifier|final
name|Long
index|[]
name|values
init|=
operator|new
name|Long
index|[
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100000
argument_list|)
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
name|values
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|values
index|[
name|i
index|]
operator|=
name|randomLong
argument_list|()
expr_stmt|;
block|}
specifier|final
name|LongLongMap
name|valueToId
init|=
operator|new
name|LongLongOpenHashMap
argument_list|()
decl_stmt|;
specifier|final
name|long
index|[]
name|idToValue
init|=
operator|new
name|long
index|[
name|values
operator|.
name|length
index|]
decl_stmt|;
comment|// Test high load factors to make sure that collision resolution works fine
specifier|final
name|float
name|maxLoadFactor
init|=
literal|0.6f
operator|+
name|randomFloat
argument_list|()
operator|*
literal|0.39f
decl_stmt|;
specifier|final
name|LongHash
name|longHash
init|=
operator|new
name|LongHash
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
argument_list|,
name|maxLoadFactor
argument_list|,
name|BigArraysTests
operator|.
name|randomCacheRecycler
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|iters
init|=
name|randomInt
argument_list|(
literal|1000000
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
name|iters
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|Long
name|value
init|=
name|randomFrom
argument_list|(
name|values
argument_list|)
decl_stmt|;
if|if
condition|(
name|valueToId
operator|.
name|containsKey
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
operator|-
literal|1
operator|-
name|valueToId
operator|.
name|get
argument_list|(
name|value
argument_list|)
argument_list|,
name|longHash
operator|.
name|add
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|valueToId
operator|.
name|size
argument_list|()
argument_list|,
name|longHash
operator|.
name|add
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|idToValue
index|[
name|valueToId
operator|.
name|size
argument_list|()
index|]
operator|=
name|value
expr_stmt|;
name|valueToId
operator|.
name|put
argument_list|(
name|value
argument_list|,
name|valueToId
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|valueToId
operator|.
name|size
argument_list|()
argument_list|,
name|longHash
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|LongLongCursor
argument_list|>
name|iterator
init|=
name|valueToId
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
specifier|final
name|LongLongCursor
name|next
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|next
operator|.
name|value
argument_list|,
name|longHash
operator|.
name|find
argument_list|(
name|next
operator|.
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|longHash
operator|.
name|capacity
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|long
name|id
init|=
name|longHash
operator|.
name|id
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|id
operator|>=
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|idToValue
index|[
operator|(
name|int
operator|)
name|id
index|]
argument_list|,
name|longHash
operator|.
name|key
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|longHash
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

