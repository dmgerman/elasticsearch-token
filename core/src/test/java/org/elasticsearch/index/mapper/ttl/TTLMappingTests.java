begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.ttl
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|ttl
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
name|IndexOptions
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
name|index
operator|.
name|mapper
operator|.
name|SourceToParse
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
name|internal
operator|.
name|TTLFieldMapper
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
name|containsString
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

begin_class
DECL|class|TTLMappingTests
specifier|public
class|class
name|TTLMappingTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testSimpleDisabled
specifier|public
name|void
name|testSimpleDisabled
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
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
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
name|BytesReference
name|source
init|=
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
decl_stmt|;
name|ParsedDocument
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
argument_list|(
name|source
argument_list|)
operator|.
name|type
argument_list|(
literal|"type"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|ttl
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
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
literal|"_ttl"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEnabled
specifier|public
name|void
name|testEnabled
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
literal|"_ttl"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
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
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
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
name|BytesReference
name|source
init|=
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
decl_stmt|;
name|ParsedDocument
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
argument_list|(
name|source
argument_list|)
operator|.
name|type
argument_list|(
literal|"type"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|ttl
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
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
literal|"_ttl"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
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
literal|"_ttl"
argument_list|)
operator|.
name|tokenStream
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|indexAnalyzer
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testDefaultValues
specifier|public
name|void
name|testDefaultValues
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
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
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
name|assertThat
argument_list|(
name|docMapper
operator|.
name|TTLFieldMapper
argument_list|()
operator|.
name|enabled
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|TTLFieldMapper
operator|.
name|Defaults
operator|.
name|ENABLED_STATE
operator|.
name|enabled
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|TTLFieldMapper
argument_list|()
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|TTLFieldMapper
operator|.
name|Defaults
operator|.
name|TTL_FIELD_TYPE
operator|.
name|stored
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|TTLFieldMapper
argument_list|()
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|TTLFieldMapper
operator|.
name|Defaults
operator|.
name|TTL_FIELD_TYPE
operator|.
name|indexOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatEnablingTTLFieldOnMergeWorks
specifier|public
name|void
name|testThatEnablingTTLFieldOnMergeWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mappingWithoutTtl
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
name|field
argument_list|(
literal|"field"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
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
name|String
name|mappingWithTtl
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
literal|"_ttl"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|"yes"
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
name|field
argument_list|(
literal|"field"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
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
name|MapperService
name|mapperService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
decl_stmt|;
name|DocumentMapper
name|mapperWithoutTtl
init|=
name|mapperService
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mappingWithoutTtl
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|DocumentMapper
name|mapperWithTtl
init|=
name|mapperService
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mappingWithTtl
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mapperWithoutTtl
operator|.
name|TTLFieldMapper
argument_list|()
operator|.
name|enabled
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mapperWithTtl
operator|.
name|TTLFieldMapper
argument_list|()
operator|.
name|enabled
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatChangingTTLKeepsMapperEnabled
specifier|public
name|void
name|testThatChangingTTLKeepsMapperEnabled
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mappingWithTtl
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
literal|"_ttl"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|"yes"
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
name|field
argument_list|(
literal|"field"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
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
name|String
name|updatedMapping
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
literal|"_ttl"
argument_list|)
operator|.
name|field
argument_list|(
literal|"default"
argument_list|,
literal|"1w"
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
name|field
argument_list|(
literal|"field"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
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
name|MapperService
name|mapperService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
decl_stmt|;
name|DocumentMapper
name|initialMapper
init|=
name|mapperService
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mappingWithTtl
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|DocumentMapper
name|updatedMapper
init|=
name|mapperService
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|updatedMapping
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|initialMapper
operator|.
name|TTLFieldMapper
argument_list|()
operator|.
name|enabled
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|updatedMapper
operator|.
name|TTLFieldMapper
argument_list|()
operator|.
name|enabled
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatDisablingTTLReportsConflict
specifier|public
name|void
name|testThatDisablingTTLReportsConflict
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mappingWithTtl
init|=
name|getMappingWithTtlEnabled
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|String
name|mappingWithTtlDisabled
init|=
name|getMappingWithTtlDisabled
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|MapperService
name|mapperService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
decl_stmt|;
name|DocumentMapper
name|initialMapper
init|=
name|mapperService
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mappingWithTtl
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
name|mapperService
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mappingWithTtlDisabled
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// expected
block|}
name|assertThat
argument_list|(
name|initialMapper
operator|.
name|TTLFieldMapper
argument_list|()
operator|.
name|enabled
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatDisablingTTLReportsConflictOnCluster
specifier|public
name|void
name|testThatDisablingTTLReportsConflictOnCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mappingWithTtl
init|=
name|getMappingWithTtlEnabled
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|String
name|mappingWithTtlDisabled
init|=
name|getMappingWithTtlDisabled
argument_list|()
operator|.
name|string
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
literal|"testindex"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|mappingWithTtl
argument_list|)
argument_list|)
expr_stmt|;
name|GetMappingsResponse
name|mappingsBeforeUpdateResponse
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
literal|"testindex"
argument_list|)
operator|.
name|addTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
try|try
block|{
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
literal|"testindex"
argument_list|)
operator|.
name|setSource
argument_list|(
name|mappingWithTtlDisabled
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"_ttl cannot be disabled once it was enabled."
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|GetMappingsResponse
name|mappingsAfterUpdateResponse
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
literal|"testindex"
argument_list|)
operator|.
name|addTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|mappingsBeforeUpdateResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"testindex"
argument_list|)
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|source
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|mappingsAfterUpdateResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"testindex"
argument_list|)
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|source
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatEnablingTTLAfterFirstDisablingWorks
specifier|public
name|void
name|testThatEnablingTTLAfterFirstDisablingWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mappingWithTtl
init|=
name|getMappingWithTtlEnabled
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|String
name|withTtlDisabled
init|=
name|getMappingWithTtlDisabled
argument_list|()
operator|.
name|string
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
literal|"testindex"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|withTtlDisabled
argument_list|)
argument_list|)
expr_stmt|;
name|GetMappingsResponse
name|mappingsAfterUpdateResponse
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
literal|"testindex"
argument_list|)
operator|.
name|addTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|mappingsAfterUpdateResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"testindex"
argument_list|)
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"{enabled=false}"
argument_list|)
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
literal|"testindex"
argument_list|)
operator|.
name|setSource
argument_list|(
name|mappingWithTtl
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|mappingsAfterUpdateResponse
operator|=
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
literal|"testindex"
argument_list|)
operator|.
name|addTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|mappingsAfterUpdateResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"testindex"
argument_list|)
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"_ttl"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"{enabled=true}"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoConflictIfNothingSetAndDisabledLater
specifier|public
name|void
name|testNoConflictIfNothingSetAndDisabledLater
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"testindex"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
literal|"type"
argument_list|)
decl_stmt|;
name|XContentBuilder
name|mappingWithTtlDisabled
init|=
name|getMappingWithTtlDisabled
argument_list|(
literal|"7d"
argument_list|)
decl_stmt|;
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mappingWithTtlDisabled
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoConflictIfNothingSetAndEnabledLater
specifier|public
name|void
name|testNoConflictIfNothingSetAndEnabledLater
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"testindex"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
literal|"type"
argument_list|)
decl_stmt|;
name|XContentBuilder
name|mappingWithTtlEnabled
init|=
name|getMappingWithTtlEnabled
argument_list|(
literal|"7d"
argument_list|)
decl_stmt|;
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mappingWithTtlEnabled
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testMergeWithOnlyDefaultSet
specifier|public
name|void
name|testMergeWithOnlyDefaultSet
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|mappingWithTtlEnabled
init|=
name|getMappingWithTtlEnabled
argument_list|(
literal|"7d"
argument_list|)
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"testindex"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
literal|"type"
argument_list|,
name|mappingWithTtlEnabled
argument_list|)
decl_stmt|;
name|XContentBuilder
name|mappingWithOnlyDefaultSet
init|=
name|getMappingWithOnlyTtlDefaultSet
argument_list|(
literal|"6m"
argument_list|)
decl_stmt|;
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mappingWithOnlyDefaultSet
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|CompressedXContent
name|mappingAfterMerge
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
literal|"type"
argument_list|)
operator|.
name|mappingSource
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|mappingAfterMerge
argument_list|,
name|equalTo
argument_list|(
operator|new
name|CompressedXContent
argument_list|(
literal|"{\"type\":{\"_ttl\":{\"enabled\":true,\"default\":360000},\"properties\":{\"field\":{\"type\":\"text\"}}}}"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMergeWithOnlyDefaultSetTtlDisabled
specifier|public
name|void
name|testMergeWithOnlyDefaultSetTtlDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|mappingWithTtlEnabled
init|=
name|getMappingWithTtlDisabled
argument_list|(
literal|"7d"
argument_list|)
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"testindex"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
literal|"type"
argument_list|,
name|mappingWithTtlEnabled
argument_list|)
decl_stmt|;
name|CompressedXContent
name|mappingAfterCreation
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
literal|"type"
argument_list|)
operator|.
name|mappingSource
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|mappingAfterCreation
argument_list|,
name|equalTo
argument_list|(
operator|new
name|CompressedXContent
argument_list|(
literal|"{\"type\":{\"_ttl\":{\"enabled\":false},\"properties\":{\"field\":{\"type\":\"text\"}}}}"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|XContentBuilder
name|mappingWithOnlyDefaultSet
init|=
name|getMappingWithOnlyTtlDefaultSet
argument_list|(
literal|"6m"
argument_list|)
decl_stmt|;
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
literal|"type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mappingWithOnlyDefaultSet
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|CompressedXContent
name|mappingAfterMerge
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
literal|"type"
argument_list|)
operator|.
name|mappingSource
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|mappingAfterMerge
argument_list|,
name|equalTo
argument_list|(
operator|new
name|CompressedXContent
argument_list|(
literal|"{\"type\":{\"_ttl\":{\"enabled\":false},\"properties\":{\"field\":{\"type\":\"text\"}}}}"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIncludeInObjectNotAllowed
specifier|public
name|void
name|testIncludeInObjectNotAllowed
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
literal|"_ttl"
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
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
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
try|try
block|{
name|docMapper
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
literal|"_ttl"
argument_list|,
literal|"2d"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected failure to parse metadata field"
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
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Field [_ttl] is a metadata field and cannot be added inside a document"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getMappingWithTtlEnabled
specifier|private
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
name|getMappingWithTtlEnabled
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getMappingWithTtlEnabled
argument_list|(
literal|null
argument_list|)
return|;
block|}
DECL|method|getMappingWithTtlDisabled
specifier|private
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
name|getMappingWithTtlDisabled
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getMappingWithTtlDisabled
argument_list|(
literal|null
argument_list|)
return|;
block|}
DECL|method|getMappingWithTtlEnabled
specifier|private
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
name|getMappingWithTtlEnabled
parameter_list|(
name|String
name|defaultValue
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentBuilder
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
literal|"_ttl"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|defaultValue
operator|!=
literal|null
condition|)
block|{
name|mapping
operator|.
name|field
argument_list|(
literal|"default"
argument_list|,
name|defaultValue
argument_list|)
expr_stmt|;
block|}
return|return
name|mapping
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
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
return|;
block|}
DECL|method|getMappingWithTtlDisabled
specifier|private
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
name|getMappingWithTtlDisabled
parameter_list|(
name|String
name|defaultValue
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentBuilder
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
literal|"_ttl"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|defaultValue
operator|!=
literal|null
condition|)
block|{
name|mapping
operator|.
name|field
argument_list|(
literal|"default"
argument_list|,
name|defaultValue
argument_list|)
expr_stmt|;
block|}
return|return
name|mapping
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
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
return|;
block|}
DECL|method|getMappingWithOnlyTtlDefaultSet
specifier|private
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
name|getMappingWithOnlyTtlDefaultSet
parameter_list|(
name|String
name|defaultValue
parameter_list|)
throws|throws
name|IOException
block|{
return|return
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
literal|"_ttl"
argument_list|)
operator|.
name|field
argument_list|(
literal|"default"
argument_list|,
name|defaultValue
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
name|field
argument_list|(
literal|"field"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
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
return|;
block|}
block|}
end_class

end_unit

