begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|xcontent
operator|.
name|NamedXContentRegistry
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
name|common
operator|.
name|xcontent
operator|.
name|XContentHelper
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
name|XContentParser
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
name|XContentType
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
name|json
operator|.
name|JsonXContent
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
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Predicate
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|XContentTestUtils
operator|.
name|insertRandomFields
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
name|hasItem
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
name|instanceOf
import|;
end_import

begin_empty_stmt
empty_stmt|;
end_empty_stmt

begin_class
DECL|class|XContentTestUtilsTests
specifier|public
class|class
name|XContentTestUtilsTests
extends|extends
name|ESTestCase
block|{
DECL|method|testGetInsertPaths
specifier|public
name|void
name|testGetInsertPaths
parameter_list|()
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"list1"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|value
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"inner1"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"inner1field1"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"inn.er2"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"inner2field1"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|NamedXContentRegistry
operator|.
name|EMPTY
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
name|builder
operator|.
name|contentType
argument_list|()
argument_list|)
init|)
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|insertPaths
init|=
name|XContentTestUtils
operator|.
name|getInsertPaths
argument_list|(
name|parser
argument_list|,
operator|new
name|Stack
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|insertPaths
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|insertPaths
argument_list|,
name|hasItem
argument_list|(
name|equalTo
argument_list|(
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|insertPaths
argument_list|,
name|hasItem
argument_list|(
name|equalTo
argument_list|(
literal|"list1.2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|insertPaths
argument_list|,
name|hasItem
argument_list|(
name|equalTo
argument_list|(
literal|"list1.4"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|insertPaths
argument_list|,
name|hasItem
argument_list|(
name|equalTo
argument_list|(
literal|"inner1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|insertPaths
argument_list|,
name|hasItem
argument_list|(
name|equalTo
argument_list|(
literal|"inner1.inn\\.er2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|testInsertIntoXContent
specifier|public
name|void
name|testInsertIntoXContent
parameter_list|()
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|=
name|XContentTestUtils
operator|.
name|insertIntoXContent
argument_list|(
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|""
argument_list|)
argument_list|,
parameter_list|()
lambda|->
literal|"inn.er1"
argument_list|,
parameter_list|()
lambda|->
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|=
name|XContentTestUtils
operator|.
name|insertIntoXContent
argument_list|(
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|""
argument_list|)
argument_list|,
parameter_list|()
lambda|->
literal|"field1"
argument_list|,
parameter_list|()
lambda|->
literal|"value1"
argument_list|)
expr_stmt|;
name|builder
operator|=
name|XContentTestUtils
operator|.
name|insertIntoXContent
argument_list|(
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"inn\\.er1"
argument_list|)
argument_list|,
parameter_list|()
lambda|->
literal|"inner2"
argument_list|,
parameter_list|()
lambda|->
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|=
name|XContentTestUtils
operator|.
name|insertIntoXContent
argument_list|(
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"inn\\.er1"
argument_list|)
argument_list|,
parameter_list|()
lambda|->
literal|"field2"
argument_list|,
parameter_list|()
lambda|->
literal|"value2"
argument_list|)
expr_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|NamedXContentRegistry
operator|.
name|EMPTY
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
name|builder
operator|.
name|contentType
argument_list|()
argument_list|)
init|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|parser
operator|.
name|map
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"value1"
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"inn.er1"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|innerMap
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|map
operator|.
name|get
argument_list|(
literal|"inn.er1"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|innerMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"value2"
argument_list|,
name|innerMap
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|innerMap
operator|.
name|get
argument_list|(
literal|"inner2"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|innerMap
operator|.
name|get
argument_list|(
literal|"inner2"
argument_list|)
operator|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|testInsertRandomXContent
specifier|public
name|void
name|testInsertRandomXContent
parameter_list|()
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"bar"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"foo1"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"foo2"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"buzz"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"foo3"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"foo4"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"foo5"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|resultMap
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
argument_list|,
name|insertRandomFields
argument_list|(
name|builder
operator|.
name|contentType
argument_list|()
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
literal|null
argument_list|,
name|random
argument_list|()
argument_list|)
argument_list|)
init|)
block|{
name|resultMap
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|resultMap
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|resultMap
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
operator|)
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|foo1
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|resultMap
operator|.
name|get
argument_list|(
literal|"foo1"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|foo1
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|foo1
operator|.
name|get
argument_list|(
literal|"foo2"
argument_list|)
operator|)
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|foo4List
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|resultMap
operator|.
name|get
argument_list|(
literal|"foo4"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|foo4List
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|foo4List
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Predicate
argument_list|<
name|String
argument_list|>
name|pathsToExclude
init|=
name|path
lambda|->
name|path
operator|.
name|endsWith
argument_list|(
literal|"foo1"
argument_list|)
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
argument_list|,
name|insertRandomFields
argument_list|(
name|builder
operator|.
name|contentType
argument_list|()
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
name|pathsToExclude
argument_list|,
name|random
argument_list|()
argument_list|)
argument_list|)
init|)
block|{
name|resultMap
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|resultMap
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|resultMap
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
operator|)
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|foo1
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|resultMap
operator|.
name|get
argument_list|(
literal|"foo1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|foo1
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|foo1
operator|.
name|get
argument_list|(
literal|"foo2"
argument_list|)
operator|)
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|foo4List
operator|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|resultMap
operator|.
name|get
argument_list|(
literal|"foo4"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|foo4List
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|foo4List
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|pathsToExclude
operator|=
name|path
lambda|->
name|path
operator|.
name|contains
argument_list|(
literal|"foo1"
argument_list|)
expr_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
argument_list|,
name|insertRandomFields
argument_list|(
name|builder
operator|.
name|contentType
argument_list|()
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
name|pathsToExclude
argument_list|,
name|random
argument_list|()
argument_list|)
argument_list|)
init|)
block|{
name|resultMap
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|resultMap
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|resultMap
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
operator|)
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|foo1
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|resultMap
operator|.
name|get
argument_list|(
literal|"foo1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|foo1
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|foo1
operator|.
name|get
argument_list|(
literal|"foo2"
argument_list|)
operator|)
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|foo4List
operator|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|resultMap
operator|.
name|get
argument_list|(
literal|"foo4"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|foo4List
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|foo4List
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

