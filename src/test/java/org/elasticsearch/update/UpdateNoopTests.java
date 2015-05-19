begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.update
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|update
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|update
operator|.
name|UpdateResponse
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
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
name|ElasticsearchIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|notNullValue
import|;
end_import

begin_comment
comment|/**  * Tests for noop updates.  */
end_comment

begin_class
DECL|class|UpdateNoopTests
specifier|public
class|class
name|UpdateNoopTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|singleField
specifier|public
name|void
name|singleField
parameter_list|()
throws|throws
name|Exception
block|{
name|updateAndCheckSource
argument_list|(
literal|1
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|1
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|2
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|"bir"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|2
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|"bir"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|3
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|4
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|4
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|5
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|totalNoopUpdates
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|twoFields
specifier|public
name|void
name|twoFields
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Use random keys so we get random iteration order.
name|String
name|key1
init|=
literal|1
operator|+
name|randomAsciiOfLength
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|String
name|key2
init|=
literal|2
operator|+
name|randomAsciiOfLength
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|String
name|key3
init|=
literal|3
operator|+
name|randomAsciiOfLength
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|updateAndCheckSource
argument_list|(
literal|1
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|,
name|key2
argument_list|,
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|1
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|,
name|key2
argument_list|,
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|2
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|,
name|key2
argument_list|,
literal|"bir"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|2
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|,
name|key2
argument_list|,
literal|"bir"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|3
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|,
name|key2
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|4
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|,
name|key2
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|4
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|,
name|key2
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|5
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|,
name|key2
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|6
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|null
argument_list|,
name|key2
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|6
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|null
argument_list|,
name|key2
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|7
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|null
argument_list|,
name|key2
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|7
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|null
argument_list|,
name|key2
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|8
argument_list|,
name|fields
argument_list|(
name|key1
argument_list|,
literal|null
argument_list|,
name|key2
argument_list|,
literal|null
argument_list|,
name|key3
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|totalNoopUpdates
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|arrayField
specifier|public
name|void
name|arrayField
parameter_list|()
throws|throws
name|Exception
block|{
name|updateAndCheckSource
argument_list|(
literal|1
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|2
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"baz"
block|,
literal|"bort"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|2
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"baz"
block|,
literal|"bort"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|3
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|"bir"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|3
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
literal|"bir"
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|4
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"baz"
block|,
literal|"bort"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|4
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"baz"
block|,
literal|"bort"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|5
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bir"
block|,
literal|"bort"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|5
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bir"
block|,
literal|"bort"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|6
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bir"
block|,
literal|"for"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|6
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bir"
block|,
literal|"for"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|7
argument_list|,
name|fields
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bir"
block|,
literal|"for"
block|,
literal|"far"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|totalNoopUpdates
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|map
specifier|public
name|void
name|map
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Use random keys so we get variable iteration order.
name|String
name|key1
init|=
literal|1
operator|+
name|randomAsciiOfLength
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|String
name|key2
init|=
literal|2
operator|+
name|randomAsciiOfLength
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|String
name|key3
init|=
literal|3
operator|+
name|randomAsciiOfLength
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|updateAndCheckSource
argument_list|(
literal|1
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
name|key2
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|1
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
name|key2
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|2
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
name|key2
argument_list|,
literal|"bir"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|2
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
name|key2
argument_list|,
literal|"bir"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|3
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
name|key2
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|4
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
name|key2
argument_list|,
operator|(
name|Object
operator|)
literal|null
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|4
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
name|key2
argument_list|,
operator|(
name|Object
operator|)
literal|null
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|5
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
name|key1
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
name|key2
argument_list|,
operator|(
name|Object
operator|)
literal|null
argument_list|)
operator|.
name|field
argument_list|(
name|key3
argument_list|,
operator|(
name|Object
operator|)
literal|null
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|totalNoopUpdates
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|mapAndField
specifier|public
name|void
name|mapAndField
parameter_list|()
throws|throws
name|Exception
block|{
name|updateAndCheckSource
argument_list|(
literal|1
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"f"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"m"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf2"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|1
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"f"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"m"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf2"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|2
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"f"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"m"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf2"
argument_list|,
literal|"bir"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|2
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"f"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"m"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf2"
argument_list|,
literal|"bir"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|3
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"f"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"m"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf2"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|4
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"f"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"m"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf2"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|4
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"f"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"m"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf2"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|5
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"f"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"m"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf2"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|updateAndCheckSource
argument_list|(
literal|6
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"f"
argument_list|,
literal|"bop"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"m"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf2"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|totalNoopUpdates
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Totally empty requests are noop if and only if detect noops is true.      */
annotation|@
name|Test
DECL|method|totallyEmpty
specifier|public
name|void
name|totallyEmpty
parameter_list|()
throws|throws
name|Exception
block|{
name|updateAndCheckSource
argument_list|(
literal|1
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"f"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"m"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mf2"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|update
argument_list|(
literal|true
argument_list|,
literal|1
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|update
argument_list|(
literal|false
argument_list|,
literal|2
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|fields
specifier|private
name|XContentBuilder
name|fields
parameter_list|(
name|Object
modifier|...
name|fields
parameter_list|)
throws|throws
name|IOException
block|{
name|assertEquals
argument_list|(
literal|"Fields must field1, value1, field2, value2, etc"
argument_list|,
literal|0
argument_list|,
name|fields
operator|.
name|length
operator|%
literal|2
argument_list|)
expr_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
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
name|fields
operator|.
name|length
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
operator|(
name|String
operator|)
name|fields
index|[
name|i
index|]
argument_list|,
name|fields
index|[
name|i
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|updateAndCheckSource
specifier|private
name|void
name|updateAndCheckSource
parameter_list|(
name|long
name|expectedVersion
parameter_list|,
name|XContentBuilder
name|xContentBuilder
parameter_list|)
block|{
name|UpdateResponse
name|updateResponse
init|=
name|update
argument_list|(
literal|true
argument_list|,
name|expectedVersion
argument_list|,
name|xContentBuilder
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|updateResponse
operator|.
name|getGetResult
argument_list|()
operator|.
name|sourceRef
argument_list|()
operator|.
name|toUtf8
argument_list|()
argument_list|,
name|xContentBuilder
operator|.
name|bytes
argument_list|()
operator|.
name|toUtf8
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|update
specifier|private
name|UpdateResponse
name|update
parameter_list|(
name|boolean
name|detectNoop
parameter_list|,
name|long
name|expectedVersion
parameter_list|,
name|XContentBuilder
name|xContentBuilder
parameter_list|)
block|{
name|UpdateResponse
name|updateResponse
init|=
name|client
argument_list|()
operator|.
name|prepareUpdate
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setDoc
argument_list|(
name|xContentBuilder
operator|.
name|bytes
argument_list|()
operator|.
name|toUtf8
argument_list|()
argument_list|)
operator|.
name|setDocAsUpsert
argument_list|(
literal|true
argument_list|)
operator|.
name|setDetectNoop
argument_list|(
name|detectNoop
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|updateResponse
operator|.
name|getGetResult
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedVersion
argument_list|,
name|updateResponse
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|updateResponse
return|;
block|}
DECL|method|totalNoopUpdates
specifier|private
name|long
name|totalNoopUpdates
parameter_list|()
block|{
return|return
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setIndexing
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getTotal
argument_list|()
operator|.
name|getIndexing
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getNoopUpdateCount
argument_list|()
return|;
block|}
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

