begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.update
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
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
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
operator|.
name|ScriptType
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
name|util
operator|.
name|Map
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
name|notNullValue
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
name|nullValue
import|;
end_import

begin_class
DECL|class|UpdateRequestTests
specifier|public
class|class
name|UpdateRequestTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testUpdateRequest
specifier|public
name|void
name|testUpdateRequest
parameter_list|()
throws|throws
name|Exception
block|{
name|UpdateRequest
name|request
init|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
decl_stmt|;
comment|// simple script
name|request
operator|.
name|source
argument_list|(
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
literal|"script"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|Script
name|script
init|=
name|request
operator|.
name|script
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|script
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getLang
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
name|script
operator|.
name|getParams
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|params
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// script with params
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"script"
argument_list|)
operator|.
name|field
argument_list|(
literal|"inline"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"params"
argument_list|)
operator|.
name|field
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|script
operator|=
name|request
operator|.
name|script
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|script
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getLang
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|params
operator|=
name|script
operator|.
name|getParams
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|params
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"script"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"params"
argument_list|)
operator|.
name|field
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"inline"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|script
operator|=
name|request
operator|.
name|script
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|script
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getLang
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|params
operator|=
name|script
operator|.
name|getParams
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|params
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
comment|// script with params and upsert
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"script"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"params"
argument_list|)
operator|.
name|field
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"inline"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"upsert"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"compound"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|script
operator|=
name|request
operator|.
name|script
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|script
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getLang
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|params
operator|=
name|script
operator|.
name|getParams
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|params
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|upsertDoc
init|=
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|request
operator|.
name|upsertRequest
argument_list|()
operator|.
name|source
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|v2
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|upsertDoc
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|upsertDoc
operator|.
name|get
argument_list|(
literal|"compound"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"upsert"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"compound"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"script"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"params"
argument_list|)
operator|.
name|field
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"inline"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|script
operator|=
name|request
operator|.
name|script
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|script
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getScript
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|script
operator|.
name|getLang
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|params
operator|=
name|script
operator|.
name|getParams
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|params
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|upsertDoc
operator|=
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|request
operator|.
name|upsertRequest
argument_list|()
operator|.
name|source
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|v2
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|upsertDoc
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|upsertDoc
operator|.
name|get
argument_list|(
literal|"compound"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
comment|// script with doc
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"compound"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|doc
init|=
name|request
operator|.
name|doc
argument_list|()
operator|.
name|sourceAsMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|doc
operator|.
name|get
argument_list|(
literal|"compound"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/*      * TODO Remove in 2.0      */
annotation|@
name|Test
DECL|method|testUpdateRequestOldAPI
specifier|public
name|void
name|testUpdateRequestOldAPI
parameter_list|()
throws|throws
name|Exception
block|{
name|UpdateRequest
name|request
init|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
decl_stmt|;
comment|// simple script
name|request
operator|.
name|source
argument_list|(
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
literal|"script"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
comment|// script with params
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"script"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"params"
argument_list|)
operator|.
name|field
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptParams
argument_list|()
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"params"
argument_list|)
operator|.
name|field
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"script"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptParams
argument_list|()
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
comment|// script with params and upsert
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"params"
argument_list|)
operator|.
name|field
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"script"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"upsert"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"compound"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptParams
argument_list|()
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|upsertDoc
init|=
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|request
operator|.
name|upsertRequest
argument_list|()
operator|.
name|source
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|v2
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|upsertDoc
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|upsertDoc
operator|.
name|get
argument_list|(
literal|"compound"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"upsert"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"compound"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"params"
argument_list|)
operator|.
name|field
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"script"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptParams
argument_list|()
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|upsertDoc
operator|=
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|request
operator|.
name|upsertRequest
argument_list|()
operator|.
name|source
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|v2
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|upsertDoc
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|upsertDoc
operator|.
name|get
argument_list|(
literal|"compound"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"params"
argument_list|)
operator|.
name|field
argument_list|(
literal|"param1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"upsert"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"compound"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"script"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"script1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|scriptParams
argument_list|()
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|upsertDoc
operator|=
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|request
operator|.
name|upsertRequest
argument_list|()
operator|.
name|source
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|v2
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|upsertDoc
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|upsertDoc
operator|.
name|get
argument_list|(
literal|"compound"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
comment|// script with doc
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|request
operator|.
name|source
argument_list|(
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
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"compound"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|doc
init|=
name|request
operator|.
name|doc
argument_list|()
operator|.
name|sourceAsMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|doc
operator|.
name|get
argument_list|(
literal|"compound"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

