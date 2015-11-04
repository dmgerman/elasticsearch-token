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
name|LongObjectHashMap
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
name|ESSingleNodeTestCase
import|;
end_import

begin_class
DECL|class|LongObjectHashMapTests
specifier|public
class|class
name|LongObjectHashMapTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testDuel
specifier|public
name|void
name|testDuel
parameter_list|()
block|{
specifier|final
name|LongObjectHashMap
argument_list|<
name|Object
argument_list|>
name|map1
init|=
operator|new
name|LongObjectHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|LongObjectPagedHashMap
argument_list|<
name|Object
argument_list|>
name|map2
init|=
operator|new
name|LongObjectPagedHashMap
argument_list|<>
argument_list|(
name|randomInt
argument_list|(
literal|42
argument_list|)
argument_list|,
literal|0.6f
operator|+
name|randomFloat
argument_list|()
operator|*
literal|0.39f
argument_list|,
name|BigArraysTests
operator|.
name|randombigArrays
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|maxKey
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
specifier|final
name|int
name|iters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10000
argument_list|,
literal|100000
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
name|boolean
name|put
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
specifier|final
name|int
name|iters2
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|iters2
condition|;
operator|++
name|j
control|)
block|{
specifier|final
name|long
name|key
init|=
name|randomInt
argument_list|(
name|maxKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|put
condition|)
block|{
specifier|final
name|Object
name|value
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
name|assertSame
argument_list|(
name|map1
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|,
name|map2
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertSame
argument_list|(
name|map1
operator|.
name|remove
argument_list|(
name|key
argument_list|)
argument_list|,
name|map2
operator|.
name|remove
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|map1
operator|.
name|size
argument_list|()
argument_list|,
name|map2
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|maxKey
condition|;
operator|++
name|i
control|)
block|{
name|assertSame
argument_list|(
name|map1
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|map2
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|LongObjectHashMap
argument_list|<
name|Object
argument_list|>
name|copy
init|=
operator|new
name|LongObjectHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|LongObjectPagedHashMap
operator|.
name|Cursor
argument_list|<
name|Object
argument_list|>
name|cursor
range|:
name|map2
control|)
block|{
name|copy
operator|.
name|put
argument_list|(
name|cursor
operator|.
name|key
argument_list|,
name|cursor
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
name|map2
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|map1
argument_list|,
name|copy
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

