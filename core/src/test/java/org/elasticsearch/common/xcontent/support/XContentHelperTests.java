begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|support
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
name|bytes
operator|.
name|BytesReference
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
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
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
name|Arrays
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

begin_class
DECL|class|XContentHelperTests
specifier|public
class|class
name|XContentHelperTests
extends|extends
name|ESTestCase
block|{
DECL|method|getMap
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getMap
parameter_list|(
name|Object
modifier|...
name|keyValues
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|HashMap
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
name|keyValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|map
operator|.
name|put
argument_list|(
operator|(
name|String
operator|)
name|keyValues
index|[
name|i
index|]
argument_list|,
name|keyValues
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
DECL|method|getNamedMap
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getNamedMap
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
modifier|...
name|keyValues
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|getMap
argument_list|(
name|keyValues
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|namedMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|namedMap
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|map
argument_list|)
expr_stmt|;
return|return
name|namedMap
return|;
block|}
DECL|method|getList
name|List
argument_list|<
name|Object
argument_list|>
name|getList
parameter_list|(
name|Object
modifier|...
name|values
parameter_list|)
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|values
argument_list|)
return|;
block|}
DECL|method|testMergingListValuesAreMapsOfOne
specifier|public
name|void
name|testMergingListValuesAreMapsOfOne
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|defaults
init|=
name|getMap
argument_list|(
literal|"test"
argument_list|,
name|getList
argument_list|(
name|getNamedMap
argument_list|(
literal|"name1"
argument_list|,
literal|"t1"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|getNamedMap
argument_list|(
literal|"name2"
argument_list|,
literal|"t2"
argument_list|,
literal|"2"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|content
init|=
name|getMap
argument_list|(
literal|"test"
argument_list|,
name|getList
argument_list|(
name|getNamedMap
argument_list|(
literal|"name2"
argument_list|,
literal|"t3"
argument_list|,
literal|"3"
argument_list|)
argument_list|,
name|getNamedMap
argument_list|(
literal|"name4"
argument_list|,
literal|"t4"
argument_list|,
literal|"4"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|expected
init|=
name|getMap
argument_list|(
literal|"test"
argument_list|,
name|getList
argument_list|(
name|getNamedMap
argument_list|(
literal|"name2"
argument_list|,
literal|"t2"
argument_list|,
literal|"2"
argument_list|,
literal|"t3"
argument_list|,
literal|"3"
argument_list|)
argument_list|,
name|getNamedMap
argument_list|(
literal|"name4"
argument_list|,
literal|"t4"
argument_list|,
literal|"4"
argument_list|)
argument_list|,
name|getNamedMap
argument_list|(
literal|"name1"
argument_list|,
literal|"t1"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|XContentHelper
operator|.
name|mergeDefaults
argument_list|(
name|content
argument_list|,
name|defaults
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|content
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testToXContentWrapInObject
specifier|public
name|void
name|testToXContentWrapInObject
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|wrapInObject
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|XContentType
name|xContentType
init|=
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|ToXContent
name|toXContent
init|=
parameter_list|(
name|builder
parameter_list|,
name|params
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|wrapInObject
operator|==
literal|false
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
if|if
condition|(
name|wrapInObject
operator|==
literal|false
condition|)
block|{
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
decl_stmt|;
name|BytesReference
name|bytes
init|=
name|XContentHelper
operator|.
name|toXContent
argument_list|(
name|toXContent
argument_list|,
name|xContentType
argument_list|,
name|wrapInObject
argument_list|)
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|xContentType
operator|.
name|xContent
argument_list|()
operator|.
name|createParser
argument_list|(
name|NamedXContentRegistry
operator|.
name|EMPTY
argument_list|,
name|bytes
argument_list|)
init|)
block|{
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|.
name|isValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"value"
argument_list|,
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

