begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.murmur3
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|murmur3
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|DocValuesType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexableField
import|;
end_import

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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|compress
operator|.
name|CompressedXContent
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
name|settings
operator|.
name|Settings
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
name|DocumentMapperParser
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
name|MapperParsingException
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
name|indices
operator|.
name|mapper
operator|.
name|MapperRegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|InternalSettingsPlugin
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
name|Collection
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

begin_class
DECL|class|Murmur3FieldMapperTests
specifier|public
class|class
name|Murmur3FieldMapperTests
extends|extends
name|ESSingleNodeTestCase
block|{
annotation|@
name|Override
DECL|method|getPlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|getPlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|InternalSettingsPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|field|mapperRegistry
name|MapperRegistry
name|mapperRegistry
decl_stmt|;
DECL|field|indexService
name|IndexService
name|indexService
decl_stmt|;
DECL|field|parser
name|DocumentMapperParser
name|parser
decl_stmt|;
annotation|@
name|Before
DECL|method|before
specifier|public
name|void
name|before
parameter_list|()
block|{
name|indexService
operator|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|mapperRegistry
operator|=
operator|new
name|MapperRegistry
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
name|Murmur3FieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|Murmur3FieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|parser
operator|=
operator|new
name|DocumentMapperParser
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
argument_list|,
name|indexService
operator|.
name|mapperService
argument_list|()
argument_list|,
name|indexService
operator|.
name|analysisService
argument_list|()
argument_list|,
name|indexService
operator|.
name|similarityService
argument_list|()
argument_list|,
name|mapperRegistry
argument_list|,
name|indexService
operator|::
name|newQueryShardContext
argument_list|)
expr_stmt|;
block|}
DECL|method|testDefaults
specifier|public
name|void
name|testDefaults
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
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"murmur3"
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
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|mapper
init|=
name|parser
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|)
decl_stmt|;
name|ParsedDocument
name|parsedDoc
init|=
name|mapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
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
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|parsedDoc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|fields
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|toString
argument_list|(
name|fields
argument_list|)
argument_list|,
literal|1
argument_list|,
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
name|IndexableField
name|field
init|=
name|fields
index|[
literal|0
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|field
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DocValuesType
operator|.
name|SORTED_NUMERIC
argument_list|,
name|field
operator|.
name|fieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testDocValuesSettingNotAllowed
specifier|public
name|void
name|testDocValuesSettingNotAllowed
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
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"murmur3"
argument_list|)
operator|.
name|field
argument_list|(
literal|"doc_values"
argument_list|,
literal|false
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
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
try|try
block|{
name|parser
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected a mapper parsing exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MapperParsingException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Setting [doc_values] cannot be modified"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// even setting to the default is not allowed, the setting is invalid
name|mapping
operator|=
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
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"murmur3"
argument_list|)
operator|.
name|field
argument_list|(
literal|"doc_values"
argument_list|,
literal|true
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
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
expr_stmt|;
try|try
block|{
name|parser
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected a mapper parsing exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MapperParsingException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Setting [doc_values] cannot be modified"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testIndexSettingNotAllowed
specifier|public
name|void
name|testIndexSettingNotAllowed
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
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"murmur3"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
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
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
try|try
block|{
name|parser
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected a mapper parsing exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MapperParsingException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Setting [index] cannot be modified"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// even setting to the default is not allowed, the setting is invalid
name|mapping
operator|=
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
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"murmur3"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"no"
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
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
expr_stmt|;
try|try
block|{
name|parser
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected a mapper parsing exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MapperParsingException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Setting [index] cannot be modified"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testDocValuesSettingBackcompat
specifier|public
name|void
name|testDocValuesSettingBackcompat
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|V_1_4_2
operator|.
name|id
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|indexService
operator|=
name|createIndex
argument_list|(
literal|"test_bwc"
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|parser
operator|=
operator|new
name|DocumentMapperParser
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
argument_list|,
name|indexService
operator|.
name|mapperService
argument_list|()
argument_list|,
name|indexService
operator|.
name|analysisService
argument_list|()
argument_list|,
name|indexService
operator|.
name|similarityService
argument_list|()
argument_list|,
name|mapperRegistry
argument_list|,
name|indexService
operator|::
name|newQueryShardContext
argument_list|)
expr_stmt|;
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
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"murmur3"
argument_list|)
operator|.
name|field
argument_list|(
literal|"doc_values"
argument_list|,
literal|false
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
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|parser
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|)
decl_stmt|;
name|Murmur3FieldMapper
name|mapper
init|=
operator|(
name|Murmur3FieldMapper
operator|)
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndexSettingBackcompat
specifier|public
name|void
name|testIndexSettingBackcompat
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|V_1_4_2
operator|.
name|id
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|indexService
operator|=
name|createIndex
argument_list|(
literal|"test_bwc"
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|parser
operator|=
operator|new
name|DocumentMapperParser
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
argument_list|,
name|indexService
operator|.
name|mapperService
argument_list|()
argument_list|,
name|indexService
operator|.
name|analysisService
argument_list|()
argument_list|,
name|indexService
operator|.
name|similarityService
argument_list|()
argument_list|,
name|mapperRegistry
argument_list|,
name|indexService
operator|::
name|newQueryShardContext
argument_list|)
expr_stmt|;
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
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"murmur3"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
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
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|parser
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|)
decl_stmt|;
name|Murmur3FieldMapper
name|mapper
init|=
operator|(
name|Murmur3FieldMapper
operator|)
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|IndexOptions
operator|.
name|DOCS
argument_list|,
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// TODO: add more tests
block|}
end_class

end_unit

