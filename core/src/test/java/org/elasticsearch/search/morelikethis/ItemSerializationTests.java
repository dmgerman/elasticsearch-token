begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.morelikethis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|morelikethis
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomPicks
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
operator|.
name|MultiGetRequest
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
name|bytes
operator|.
name|BytesArray
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
name|ToXContent
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
name|index
operator|.
name|VersionType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|MoreLikeThisQueryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|MoreLikeThisQueryBuilder
operator|.
name|Item
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|source
operator|.
name|FetchSourceContext
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
name|Random
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
name|StreamsUtils
operator|.
name|copyToStringFromClasspath
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
DECL|class|ItemSerializationTests
specifier|public
class|class
name|ItemSerializationTests
extends|extends
name|ESTestCase
block|{
DECL|method|generateRandomItem
specifier|private
name|Item
name|generateRandomItem
parameter_list|(
name|int
name|arraySize
parameter_list|,
name|int
name|stringSize
parameter_list|)
block|{
name|String
name|index
init|=
name|randomAsciiOfLength
argument_list|(
name|stringSize
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|randomAsciiOfLength
argument_list|(
name|stringSize
argument_list|)
decl_stmt|;
name|String
name|id
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|randomInt
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|routing
init|=
name|randomBoolean
argument_list|()
condition|?
name|randomAsciiOfLength
argument_list|(
name|stringSize
argument_list|)
else|:
literal|null
decl_stmt|;
name|String
index|[]
name|fields
init|=
name|generateRandomStringArray
argument_list|(
name|arraySize
argument_list|,
name|stringSize
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|long
name|version
init|=
name|Math
operator|.
name|abs
argument_list|(
name|randomLong
argument_list|()
argument_list|)
decl_stmt|;
name|VersionType
name|versionType
init|=
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
operator|new
name|Random
argument_list|()
argument_list|,
name|VersionType
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|FetchSourceContext
name|fetchSourceContext
decl_stmt|;
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|fetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|fetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
name|generateRandomStringArray
argument_list|(
name|arraySize
argument_list|,
name|stringSize
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|fetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
name|generateRandomStringArray
argument_list|(
name|arraySize
argument_list|,
name|stringSize
argument_list|,
literal|true
argument_list|)
argument_list|,
name|generateRandomStringArray
argument_list|(
name|arraySize
argument_list|,
name|stringSize
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
name|fetchSourceContext
operator|=
literal|null
expr_stmt|;
break|break;
block|}
return|return
operator|(
name|Item
operator|)
operator|new
name|Item
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|)
operator|.
name|routing
argument_list|(
name|routing
argument_list|)
operator|.
name|fields
argument_list|(
name|fields
argument_list|)
operator|.
name|version
argument_list|(
name|version
argument_list|)
operator|.
name|versionType
argument_list|(
name|versionType
argument_list|)
operator|.
name|fetchSourceContext
argument_list|(
name|fetchSourceContext
argument_list|)
return|;
block|}
DECL|method|ItemToJSON
specifier|private
name|String
name|ItemToJSON
parameter_list|(
name|Item
name|item
parameter_list|)
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
name|builder
operator|.
name|startArray
argument_list|(
literal|"docs"
argument_list|)
expr_stmt|;
name|item
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|XContentHelper
operator|.
name|convertToJson
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
literal|false
argument_list|)
return|;
block|}
DECL|method|JSONtoItem
specifier|private
name|MultiGetRequest
operator|.
name|Item
name|JSONtoItem
parameter_list|(
name|String
name|json
parameter_list|)
throws|throws
name|Exception
block|{
name|MultiGetRequest
name|request
init|=
operator|new
name|MultiGetRequest
argument_list|()
operator|.
name|add
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|BytesArray
argument_list|(
name|json
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
return|return
name|request
operator|.
name|getItems
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|testItemSerialization
specifier|public
name|void
name|testItemSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numOfTrials
init|=
literal|100
decl_stmt|;
name|int
name|maxArraySize
init|=
literal|7
decl_stmt|;
name|int
name|maxStringSize
init|=
literal|8
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
name|numOfTrials
condition|;
name|i
operator|++
control|)
block|{
name|Item
name|item1
init|=
name|generateRandomItem
argument_list|(
name|maxArraySize
argument_list|,
name|maxStringSize
argument_list|)
decl_stmt|;
name|String
name|json
init|=
name|ItemToJSON
argument_list|(
name|item1
argument_list|)
decl_stmt|;
name|MultiGetRequest
operator|.
name|Item
name|item2
init|=
name|JSONtoItem
argument_list|(
name|json
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|item1
argument_list|,
name|item2
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testItemsFromJSON
specifier|private
name|List
argument_list|<
name|MultiGetRequest
operator|.
name|Item
argument_list|>
name|testItemsFromJSON
parameter_list|(
name|String
name|json
parameter_list|)
throws|throws
name|Exception
block|{
name|MultiGetRequest
name|request
init|=
operator|new
name|MultiGetRequest
argument_list|()
decl_stmt|;
name|request
operator|.
name|add
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|BytesArray
argument_list|(
name|json
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|MultiGetRequest
operator|.
name|Item
argument_list|>
name|items
init|=
name|request
operator|.
name|getItems
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|items
operator|.
name|size
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
for|for
control|(
name|MultiGetRequest
operator|.
name|Item
name|item
range|:
name|items
control|)
block|{
name|assertThat
argument_list|(
name|item
operator|.
name|index
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|type
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
name|FetchSourceContext
name|fetchSource
init|=
name|item
operator|.
name|fetchSourceContext
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|item
operator|.
name|id
argument_list|()
condition|)
block|{
case|case
literal|"1"
case|:
name|assertThat
argument_list|(
name|fetchSource
operator|.
name|fetchSource
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"2"
case|:
name|assertThat
argument_list|(
name|fetchSource
operator|.
name|fetchSource
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
name|fetchSource
operator|.
name|includes
argument_list|()
argument_list|,
name|is
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"field3"
block|,
literal|"field4"
block|}
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"3"
case|:
name|assertThat
argument_list|(
name|fetchSource
operator|.
name|fetchSource
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
name|fetchSource
operator|.
name|includes
argument_list|()
argument_list|,
name|is
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"user"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fetchSource
operator|.
name|excludes
argument_list|()
argument_list|,
name|is
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"user.location"
block|}
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
name|fail
argument_list|(
literal|"item with id: "
operator|+
name|item
operator|.
name|id
argument_list|()
operator|+
literal|" is not 1, 2 or 3"
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
return|return
name|items
return|;
block|}
annotation|@
name|Test
DECL|method|testSimpleItemSerializationFromFile
specifier|public
name|void
name|testSimpleItemSerializationFromFile
parameter_list|()
throws|throws
name|Exception
block|{
comment|// test items from JSON
name|List
argument_list|<
name|MultiGetRequest
operator|.
name|Item
argument_list|>
name|itemsFromJSON
init|=
name|testItemsFromJSON
argument_list|(
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/search/morelikethis/items.json"
argument_list|)
argument_list|)
decl_stmt|;
comment|// create builder from items
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
name|builder
operator|.
name|startArray
argument_list|(
literal|"docs"
argument_list|)
expr_stmt|;
for|for
control|(
name|MultiGetRequest
operator|.
name|Item
name|item
range|:
name|itemsFromJSON
control|)
block|{
name|MoreLikeThisQueryBuilder
operator|.
name|Item
name|itemForBuilder
init|=
operator|(
name|MoreLikeThisQueryBuilder
operator|.
name|Item
operator|)
operator|new
name|MoreLikeThisQueryBuilder
operator|.
name|Item
argument_list|(
name|item
operator|.
name|index
argument_list|()
argument_list|,
name|item
operator|.
name|type
argument_list|()
argument_list|,
name|item
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|fetchSourceContext
argument_list|(
name|item
operator|.
name|fetchSourceContext
argument_list|()
argument_list|)
operator|.
name|fields
argument_list|(
name|item
operator|.
name|fields
argument_list|()
argument_list|)
decl_stmt|;
name|itemForBuilder
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
comment|// verify generated JSON lead to the same items
name|String
name|json
init|=
name|XContentHelper
operator|.
name|convertToJson
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|testItemsFromJSON
argument_list|(
name|json
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

