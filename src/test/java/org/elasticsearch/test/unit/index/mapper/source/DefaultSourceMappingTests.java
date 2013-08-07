begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.unit.index.mapper.source
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
operator|.
name|index
operator|.
name|mapper
operator|.
name|source
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
name|IndexableField
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
name|compress
operator|.
name|CompressorFactory
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
name|XContentType
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
name|test
operator|.
name|integration
operator|.
name|ElasticsearchTestCase
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
name|unit
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperTestUtils
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|DefaultSourceMappingTests
specifier|public
class|class
name|DefaultSourceMappingTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testNoFormat
specifier|public
name|void
name|testNoFormat
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
literal|"_source"
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
name|documentMapper
init|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
name|mapping
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
name|assertThat
argument_list|(
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|doc
operator|.
name|source
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
expr_stmt|;
name|documentMapper
operator|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
name|mapping
argument_list|)
expr_stmt|;
name|doc
operator|=
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
name|smileBuilder
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
expr_stmt|;
name|assertThat
argument_list|(
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|doc
operator|.
name|source
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|XContentType
operator|.
name|SMILE
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testJsonFormat
specifier|public
name|void
name|testJsonFormat
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
literal|"_source"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"json"
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
name|documentMapper
init|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
name|mapping
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
name|assertThat
argument_list|(
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|doc
operator|.
name|source
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
expr_stmt|;
name|documentMapper
operator|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
name|mapping
argument_list|)
expr_stmt|;
name|doc
operator|=
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
name|smileBuilder
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
expr_stmt|;
name|assertThat
argument_list|(
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|doc
operator|.
name|source
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testJsonFormatCompressed
specifier|public
name|void
name|testJsonFormatCompressed
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
literal|"_source"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"json"
argument_list|)
operator|.
name|field
argument_list|(
literal|"compress"
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
name|documentMapper
init|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
name|mapping
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
name|assertThat
argument_list|(
name|CompressorFactory
operator|.
name|isCompressed
argument_list|(
name|doc
operator|.
name|source
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|uncompressed
init|=
name|CompressorFactory
operator|.
name|uncompressIfNeeded
argument_list|(
name|doc
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|toBytes
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|uncompressed
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
expr_stmt|;
name|documentMapper
operator|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
name|mapping
argument_list|)
expr_stmt|;
name|doc
operator|=
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
name|smileBuilder
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
expr_stmt|;
name|assertThat
argument_list|(
name|CompressorFactory
operator|.
name|isCompressed
argument_list|(
name|doc
operator|.
name|source
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|uncompressed
operator|=
name|CompressorFactory
operator|.
name|uncompressIfNeeded
argument_list|(
name|doc
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|toBytes
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|uncompressed
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIncludeExclude
specifier|public
name|void
name|testIncludeExclude
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
literal|"_source"
argument_list|)
operator|.
name|field
argument_list|(
literal|"includes"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"path1*"
block|}
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
name|documentMapper
init|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
name|mapping
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
name|startObject
argument_list|(
literal|"path1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"path2"
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
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|IndexableField
name|sourceField
init|=
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"_source"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|sourceField
operator|.
name|binaryValue
argument_list|()
argument_list|)
argument_list|)
operator|.
name|mapAndClose
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|sourceAsMap
operator|.
name|containsKey
argument_list|(
literal|"path1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sourceAsMap
operator|.
name|containsKey
argument_list|(
literal|"path2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDefaultMappingAndNoMapping
specifier|public
name|void
name|testDefaultMappingAndNoMapping
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|defaultMapping
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
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
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
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|mapper
init|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
literal|"my_type"
argument_list|,
literal|null
argument_list|,
name|defaultMapping
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my_type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|sourceMapper
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
try|try
block|{
name|mapper
operator|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|defaultMapping
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my_type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|sourceMapper
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
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|MapperParsingException
name|e
parameter_list|)
block|{
comment|// all is well
block|}
try|try
block|{
name|mapper
operator|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
literal|null
argument_list|,
literal|"{}"
argument_list|,
name|defaultMapping
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my_type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|sourceMapper
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
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|MapperParsingException
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
name|equalTo
argument_list|(
literal|"malformed mapping no root object found"
argument_list|)
argument_list|)
expr_stmt|;
comment|// all is well
block|}
block|}
annotation|@
name|Test
DECL|method|testDefaultMappingAndWithMappingOverride
specifier|public
name|void
name|testDefaultMappingAndWithMappingOverride
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|defaultMapping
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
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
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
name|string
argument_list|()
decl_stmt|;
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
literal|"_source"
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
name|mapper
init|=
name|MapperTestUtils
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
literal|"my_type"
argument_list|,
name|mapping
argument_list|,
name|defaultMapping
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my_type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|sourceMapper
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
annotation|@
name|Test
DECL|method|testDefaultMappingAndNoMappingWithMapperService
specifier|public
name|void
name|testDefaultMappingAndNoMappingWithMapperService
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|defaultMapping
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
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
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
name|string
argument_list|()
decl_stmt|;
name|MapperService
name|mapperService
init|=
name|MapperTestUtils
operator|.
name|newMapperService
argument_list|()
decl_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|,
name|defaultMapping
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|DocumentMapper
name|mapper
init|=
name|mapperService
operator|.
name|documentMapperWithAutoCreate
argument_list|(
literal|"my_type"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my_type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|sourceMapper
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
block|}
annotation|@
name|Test
DECL|method|testDefaultMappingAndWithMappingOverrideWithMapperService
specifier|public
name|void
name|testDefaultMappingAndWithMappingOverrideWithMapperService
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|defaultMapping
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
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
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
name|string
argument_list|()
decl_stmt|;
name|MapperService
name|mapperService
init|=
name|MapperTestUtils
operator|.
name|newMapperService
argument_list|()
decl_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|,
name|defaultMapping
argument_list|,
literal|true
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
literal|"_source"
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
name|mapperService
operator|.
name|merge
argument_list|(
literal|"my_type"
argument_list|,
name|mapping
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|DocumentMapper
name|mapper
init|=
name|mapperService
operator|.
name|documentMapper
argument_list|(
literal|"my_type"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my_type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mapper
operator|.
name|sourceMapper
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
block|}
end_class

end_unit

