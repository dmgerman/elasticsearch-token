begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|index
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|get
operator|.
name|GetMappingsResponse
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|PutMappingResponse
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
name|Locale
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
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
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
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|IndexTypeMapperIntegrationTests
specifier|public
class|class
name|IndexTypeMapperIntegrationTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
comment|// issue 5053
DECL|method|testThatUpdatingMappingShouldNotRemoveSizeMappingConfiguration
specifier|public
name|void
name|testThatUpdatingMappingShouldNotRemoveSizeMappingConfiguration
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|index
init|=
literal|"foo"
decl_stmt|;
name|String
name|type
init|=
literal|"mytype"
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"_index"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
name|index
argument_list|)
operator|.
name|addMapping
argument_list|(
name|type
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
comment|// check mapping again
name|assertIndexMappingEnabled
argument_list|(
name|index
argument_list|,
name|type
argument_list|)
expr_stmt|;
comment|// update some field in the mapping
name|XContentBuilder
name|updateMappingBuilder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"otherField"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|PutMappingResponse
name|putMappingResponse
init|=
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
name|index
argument_list|)
operator|.
name|setType
argument_list|(
name|type
argument_list|)
operator|.
name|setSource
argument_list|(
name|updateMappingBuilder
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|putMappingResponse
argument_list|)
expr_stmt|;
comment|// make sure timestamp field is still in mapping
name|assertIndexMappingEnabled
argument_list|(
name|index
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
DECL|method|assertIndexMappingEnabled
specifier|private
name|void
name|assertIndexMappingEnabled
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|errMsg
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"Expected index field mapping to be enabled for %s/%s"
argument_list|,
name|index
argument_list|,
name|type
argument_list|)
decl_stmt|;
name|GetMappingsResponse
name|getMappingsResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareGetMappings
argument_list|(
name|index
argument_list|)
operator|.
name|addTypes
argument_list|(
name|type
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mappingSource
init|=
name|getMappingsResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|(
name|type
argument_list|)
operator|.
name|getSourceAsMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|errMsg
argument_list|,
name|mappingSource
argument_list|,
name|hasKey
argument_list|(
literal|"_index"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|ttlAsString
init|=
name|mappingSource
operator|.
name|get
argument_list|(
literal|"_index"
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ttlAsString
argument_list|,
name|is
argument_list|(
name|notNullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|errMsg
argument_list|,
name|ttlAsString
argument_list|,
name|is
argument_list|(
literal|"{enabled=true}"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

