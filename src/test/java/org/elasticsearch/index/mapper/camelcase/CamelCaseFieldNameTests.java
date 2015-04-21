begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.camelcase
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|camelcase
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
name|index
operator|.
name|IndexService
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
name|DocumentMapper
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
name|ParsedDocument
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
name|ElasticsearchSingleNodeTest
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
name|nullValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|CamelCaseFieldNameTests
specifier|public
class|class
name|CamelCaseFieldNameTests
extends|extends
name|ElasticsearchSingleNodeTest
block|{
annotation|@
name|Test
DECL|method|testCamelCaseFieldNameStaysAsIs
specifier|public
name|void
name|testCamelCaseFieldNameStaysAsIs
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
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
literal|"type"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|IndexService
name|index
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|mapping
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|index
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
name|ParsedDocument
name|doc
init|=
name|documentMapper
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
literal|"1"
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
literal|"thisIsCamelCase"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|doc
operator|.
name|dynamicMappingsUpdate
argument_list|()
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|doc
operator|.
name|dynamicMappingsUpdate
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"thisIsCamelCase"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"this_is_camel_case"
argument_list|)
argument_list|)
expr_stmt|;
name|documentMapper
operator|.
name|refreshSource
argument_list|()
expr_stmt|;
name|documentMapper
operator|=
name|index
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|parse
argument_list|(
name|documentMapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"thisIsCamelCase"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"this_is_camel_case"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

