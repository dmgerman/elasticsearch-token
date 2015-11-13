begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.externalvalues
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|externalvalues
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
name|util
operator|.
name|GeoUtils
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
name|MapperService
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
name|VersionUtils
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
comment|/**  */
end_comment

begin_class
DECL|class|SimpleExternalMappingTests
specifier|public
class|class
name|SimpleExternalMappingTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testExternalValues
specifier|public
name|void
name|testExternalValues
parameter_list|()
throws|throws
name|Exception
block|{
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_1_0_0
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|MapperService
name|mapperService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
operator|.
name|mapperService
argument_list|()
decl_stmt|;
name|mapperService
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|putRootTypeParser
argument_list|(
name|ExternalMetadataMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|ExternalMetadataMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|putTypeParser
argument_list|(
name|ExternalMapperPlugin
operator|.
name|EXTERNAL
argument_list|,
operator|new
name|ExternalMapper
operator|.
name|TypeParser
argument_list|(
name|ExternalMapperPlugin
operator|.
name|EXTERNAL
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|mapperService
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|parse
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
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
name|ExternalMetadataMapper
operator|.
name|CONTENT_TYPE
argument_list|)
operator|.
name|endObject
argument_list|()
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
literal|"external"
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
argument_list|)
decl_stmt|;
name|ParsedDocument
name|doc
init|=
name|documentMapper
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
literal|"1234"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.bool"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.bool"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"T"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.point"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|version
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.point"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42.0,51.0"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.point"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
name|GeoUtils
operator|.
name|mortonHash
argument_list|(
literal|51.0
argument_list|,
literal|42.0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.shape"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
name|ExternalMetadataMapper
operator|.
name|FIELD_NAME
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
name|ExternalMetadataMapper
operator|.
name|FIELD_VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExternalValuesWithMultifield
specifier|public
name|void
name|testExternalValuesWithMultifield
parameter_list|()
throws|throws
name|Exception
block|{
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_1_0_0
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|MapperService
name|mapperService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
operator|.
name|mapperService
argument_list|()
decl_stmt|;
name|mapperService
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|putTypeParser
argument_list|(
name|ExternalMapperPlugin
operator|.
name|EXTERNAL
argument_list|,
operator|new
name|ExternalMapper
operator|.
name|TypeParser
argument_list|(
name|ExternalMapperPlugin
operator|.
name|EXTERNAL
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|mapperService
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|parse
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
name|ExternalMapperPlugin
operator|.
name|EXTERNAL
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
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
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|"yes"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"raw"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|"yes"
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
argument_list|)
decl_stmt|;
name|ParsedDocument
name|doc
init|=
name|documentMapper
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
literal|"1234"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.bool"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.bool"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"T"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.point"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|version
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.point"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42.0,51.0"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.point"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
name|GeoUtils
operator|.
name|mortonHash
argument_list|(
literal|51.0
argument_list|,
literal|42.0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.shape"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field.raw"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field.raw"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExternalValuesWithMultifieldTwoLevels
specifier|public
name|void
name|testExternalValuesWithMultifieldTwoLevels
parameter_list|()
throws|throws
name|Exception
block|{
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_1_0_0
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|MapperService
name|mapperService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
operator|.
name|mapperService
argument_list|()
decl_stmt|;
name|mapperService
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|putTypeParser
argument_list|(
name|ExternalMapperPlugin
operator|.
name|EXTERNAL
argument_list|,
operator|new
name|ExternalMapper
operator|.
name|TypeParser
argument_list|(
name|ExternalMapperPlugin
operator|.
name|EXTERNAL
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|putTypeParser
argument_list|(
name|ExternalMapperPlugin
operator|.
name|EXTERNAL_BIS
argument_list|,
operator|new
name|ExternalMapper
operator|.
name|TypeParser
argument_list|(
name|ExternalMapperPlugin
operator|.
name|EXTERNAL_BIS
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|mapperService
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|parse
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
name|ExternalMapperPlugin
operator|.
name|EXTERNAL
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
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
literal|"string"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"generated"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|ExternalMapperPlugin
operator|.
name|EXTERNAL_BIS
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"raw"
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
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"raw"
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
argument_list|)
decl_stmt|;
name|ParsedDocument
name|doc
init|=
name|documentMapper
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
literal|"1234"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.bool"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.bool"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"T"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.point"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|version
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.point"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42.0,51.0"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.point"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
name|GeoUtils
operator|.
name|mortonHash
argument_list|(
literal|51.0
argument_list|,
literal|42.0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.shape"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field.generated.generated"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field.generated.generated"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field.raw"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.field.raw"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.raw"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field.raw"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

