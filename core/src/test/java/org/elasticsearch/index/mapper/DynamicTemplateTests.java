begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|json
operator|.
name|JsonXContent
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
name|mapper
operator|.
name|DynamicTemplate
operator|.
name|XContentFieldType
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
name|Map
import|;
end_import

begin_class
DECL|class|DynamicTemplateTests
specifier|public
class|class
name|DynamicTemplateTests
extends|extends
name|ESTestCase
block|{
DECL|method|testParseUnknownParam
specifier|public
name|void
name|testParseUnknownParam
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|templateDef
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"match_mapping_type"
argument_list|,
literal|"string"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"mapping"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"store"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"random_param"
argument_list|,
literal|"random_value"
argument_list|)
expr_stmt|;
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|DynamicTemplate
operator|.
name|parse
argument_list|(
literal|"my_template"
argument_list|,
name|templateDef
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Illegal dynamic template parameter: [random_param]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseUnknownMatchType
specifier|public
name|void
name|testParseUnknownMatchType
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|templateDef
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"match_mapping_type"
argument_list|,
literal|"short"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"mapping"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"store"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// if a wrong match type is specified, we ignore the template
name|assertNull
argument_list|(
name|DynamicTemplate
operator|.
name|parse
argument_list|(
literal|"my_template"
argument_list|,
name|templateDef
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha5
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|templateDef2
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|templateDef2
operator|.
name|put
argument_list|(
literal|"match_mapping_type"
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
name|templateDef2
operator|.
name|put
argument_list|(
literal|"mapping"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"store"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// if a wrong match type is specified, we ignore the template
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|DynamicTemplate
operator|.
name|parse
argument_list|(
literal|"my_template"
argument_list|,
name|templateDef2
argument_list|,
name|Version
operator|.
name|V_6_0_0_alpha1_UNRELEASED
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"No xcontent type matched on [text], possible values are [object, string, long, double, boolean, date, binary]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testMatchAllTemplate
specifier|public
name|void
name|testMatchAllTemplate
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|templateDef
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"match_mapping_type"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"mapping"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"store"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|DynamicTemplate
name|template
init|=
name|DynamicTemplate
operator|.
name|parse
argument_list|(
literal|"my_template"
argument_list|,
name|templateDef
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha5
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|template
operator|.
name|match
argument_list|(
literal|"a.b"
argument_list|,
literal|"b"
argument_list|,
name|randomFrom
argument_list|(
name|XContentFieldType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMatchTypeTemplate
specifier|public
name|void
name|testMatchTypeTemplate
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|templateDef
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"match_mapping_type"
argument_list|,
literal|"string"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"mapping"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"store"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|DynamicTemplate
name|template
init|=
name|DynamicTemplate
operator|.
name|parse
argument_list|(
literal|"my_template"
argument_list|,
name|templateDef
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha5
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|template
operator|.
name|match
argument_list|(
literal|"a.b"
argument_list|,
literal|"b"
argument_list|,
name|XContentFieldType
operator|.
name|STRING
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|template
operator|.
name|match
argument_list|(
literal|"a.b"
argument_list|,
literal|"b"
argument_list|,
name|XContentFieldType
operator|.
name|BOOLEAN
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
comment|// type-based template
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|templateDef
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"match_mapping_type"
argument_list|,
literal|"string"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"mapping"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"store"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|DynamicTemplate
name|template
init|=
name|DynamicTemplate
operator|.
name|parse
argument_list|(
literal|"my_template"
argument_list|,
name|templateDef
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
decl_stmt|;
name|template
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
name|assertEquals
argument_list|(
literal|"{\"match_mapping_type\":\"string\",\"mapping\":{\"store\":true}}"
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
comment|// name-based template
name|templateDef
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"match"
argument_list|,
literal|"*name"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"unmatch"
argument_list|,
literal|"first_name"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"mapping"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"store"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|template
operator|=
name|DynamicTemplate
operator|.
name|parse
argument_list|(
literal|"my_template"
argument_list|,
name|templateDef
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
expr_stmt|;
name|builder
operator|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
expr_stmt|;
name|template
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
name|assertEquals
argument_list|(
literal|"{\"match\":\"*name\",\"unmatch\":\"first_name\",\"mapping\":{\"store\":true}}"
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
comment|// path-based template
name|templateDef
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"path_match"
argument_list|,
literal|"*name"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"path_unmatch"
argument_list|,
literal|"first_name"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"mapping"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"store"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|template
operator|=
name|DynamicTemplate
operator|.
name|parse
argument_list|(
literal|"my_template"
argument_list|,
name|templateDef
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
expr_stmt|;
name|builder
operator|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
expr_stmt|;
name|template
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
name|assertEquals
argument_list|(
literal|"{\"path_match\":\"*name\",\"path_unmatch\":\"first_name\",\"mapping\":{\"store\":true}}"
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
comment|// regex matching
name|templateDef
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"match"
argument_list|,
literal|"^a$"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"match_pattern"
argument_list|,
literal|"regex"
argument_list|)
expr_stmt|;
name|templateDef
operator|.
name|put
argument_list|(
literal|"mapping"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"store"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|template
operator|=
name|DynamicTemplate
operator|.
name|parse
argument_list|(
literal|"my_template"
argument_list|,
name|templateDef
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
expr_stmt|;
name|builder
operator|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
expr_stmt|;
name|template
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
name|assertEquals
argument_list|(
literal|"{\"match\":\"^a$\",\"match_pattern\":\"regex\",\"mapping\":{\"store\":true}}"
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

