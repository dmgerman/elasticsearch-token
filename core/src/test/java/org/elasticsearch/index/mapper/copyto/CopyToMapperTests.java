begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.copyto
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|copyto
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
name|FieldMapper
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
name|MergeResult
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
name|ParseContext
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
name|ParseContext
operator|.
name|Document
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
name|core
operator|.
name|LongFieldMapper
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
name|core
operator|.
name|StringFieldMapper
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
name|Arrays
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
name|instanceOf
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
name|startsWith
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|CopyToMapperTests
specifier|public
class|class
name|CopyToMapperTests
extends|extends
name|ESSingleNodeTestCase
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
DECL|method|testCopyToFieldsParsing
specifier|public
name|void
name|testCopyToFieldsParsing
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"copy_test"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|array
argument_list|(
literal|"copy_to"
argument_list|,
literal|"another_field"
argument_list|,
literal|"cyclic_test"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"another_field"
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
name|startObject
argument_list|(
literal|"cyclic_test"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|array
argument_list|(
literal|"copy_to"
argument_list|,
literal|"copy_test"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"int_to_str_test"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
argument_list|)
operator|.
name|field
argument_list|(
literal|"doc_values"
argument_list|,
literal|false
argument_list|)
operator|.
name|array
argument_list|(
literal|"copy_to"
argument_list|,
literal|"another_field"
argument_list|,
literal|"new_field"
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
literal|"type1"
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
name|docMapper
init|=
name|index
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
literal|"type1"
argument_list|)
decl_stmt|;
name|FieldMapper
name|fieldMapper
init|=
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"copy_test"
argument_list|)
decl_stmt|;
comment|// Check json serialization
name|StringFieldMapper
name|stringFieldMapper
init|=
operator|(
name|StringFieldMapper
operator|)
name|fieldMapper
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|stringFieldMapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|close
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|serializedMap
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|JsonXContent
operator|.
name|jsonXContent
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
init|)
block|{
name|serializedMap
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|copyTestMap
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|serializedMap
operator|.
name|get
argument_list|(
literal|"copy_test"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|copyTestMap
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"string"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|copyToList
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|copyTestMap
operator|.
name|get
argument_list|(
literal|"copy_to"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|copyToList
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|copyToList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"another_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|copyToList
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cyclic_test"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Check data parsing
name|BytesReference
name|json
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"copy_test"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"cyclic_test"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|field
argument_list|(
literal|"int_to_str_test"
argument_list|,
literal|42
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|ParsedDocument
name|parsedDoc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
name|json
argument_list|)
decl_stmt|;
name|ParseContext
operator|.
name|Document
name|doc
init|=
name|parsedDoc
operator|.
name|rootDoc
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"copy_test"
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"copy_test"
argument_list|)
index|[
literal|0
index|]
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"copy_test"
argument_list|)
index|[
literal|1
index|]
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"another_field"
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"another_field"
argument_list|)
index|[
literal|0
index|]
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"another_field"
argument_list|)
index|[
literal|1
index|]
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"42"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"cyclic_test"
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"cyclic_test"
argument_list|)
index|[
literal|0
index|]
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"cyclic_test"
argument_list|)
index|[
literal|1
index|]
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"int_to_str_test"
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"int_to_str_test"
argument_list|)
index|[
literal|0
index|]
operator|.
name|numericValue
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|42
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"new_field"
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// new field has doc values
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"new_field"
argument_list|)
index|[
literal|0
index|]
operator|.
name|numericValue
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|42
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|parsedDoc
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
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|parsedDoc
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
name|fieldMapper
operator|=
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"new_field"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fieldMapper
argument_list|,
name|instanceOf
argument_list|(
name|LongFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
DECL|method|testCopyToFieldsInnerObjectParsing
specifier|public
name|void
name|testCopyToFieldsInnerObjectParsing
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"copy_test"
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
literal|"copy_to"
argument_list|,
literal|"very.inner.field"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"very"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"object"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"inner"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"object"
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
name|mapping
argument_list|)
decl_stmt|;
name|BytesReference
name|json
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"copy_test"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"bar"
argument_list|)
operator|.
name|field
argument_list|(
literal|"baz"
argument_list|,
literal|"zoo"
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
name|bytes
argument_list|()
decl_stmt|;
name|ParseContext
operator|.
name|Document
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
name|json
argument_list|)
operator|.
name|rootDoc
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"copy_test"
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"copy_test"
argument_list|)
index|[
literal|0
index|]
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"very.inner.field"
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|getFields
argument_list|(
literal|"very.inner.field"
argument_list|)
index|[
literal|0
index|]
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
DECL|method|testCopyToFieldsNonExistingInnerObjectParsing
specifier|public
name|void
name|testCopyToFieldsNonExistingInnerObjectParsing
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"copy_test"
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
literal|"copy_to"
argument_list|,
literal|"very.inner.field"
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
name|mapping
argument_list|)
decl_stmt|;
name|BytesReference
name|json
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"copy_test"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
try|try
block|{
name|docMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
name|json
argument_list|)
operator|.
name|rootDoc
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MapperParsingException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|startsWith
argument_list|(
literal|"attempt to copy value to non-existing object"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testCopyToFieldMerge
specifier|public
name|void
name|testCopyToFieldMerge
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mappingBefore
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"copy_test"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|array
argument_list|(
literal|"copy_to"
argument_list|,
literal|"foo"
argument_list|,
literal|"bar"
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
name|mappingAfter
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"copy_test"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|array
argument_list|(
literal|"copy_to"
argument_list|,
literal|"baz"
argument_list|,
literal|"bar"
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
name|DocumentMapperParser
name|parser
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
decl_stmt|;
name|DocumentMapper
name|docMapperBefore
init|=
name|parser
operator|.
name|parse
argument_list|(
name|mappingBefore
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fields
init|=
name|docMapperBefore
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"copy_test"
argument_list|)
operator|.
name|copyTo
argument_list|()
operator|.
name|copyToFields
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fields
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|DocumentMapper
name|docMapperAfter
init|=
name|parser
operator|.
name|parse
argument_list|(
name|mappingAfter
argument_list|)
decl_stmt|;
name|MergeResult
name|mergeResult
init|=
name|docMapperBefore
operator|.
name|merge
argument_list|(
name|docMapperAfter
operator|.
name|mapping
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|Arrays
operator|.
name|toString
argument_list|(
name|mergeResult
operator|.
name|buildConflicts
argument_list|()
argument_list|)
argument_list|,
name|mergeResult
operator|.
name|hasConflicts
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|docMapperBefore
operator|.
name|merge
argument_list|(
name|docMapperAfter
operator|.
name|mapping
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|fields
operator|=
name|docMapperBefore
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"copy_test"
argument_list|)
operator|.
name|copyTo
argument_list|()
operator|.
name|copyToFields
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fields
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCopyToNestedField
specifier|public
name|void
name|testCopyToNestedField
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|DocumentMapperParser
name|parser
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
decl_stmt|;
for|for
control|(
name|boolean
name|mapped
range|:
operator|new
name|boolean
index|[]
block|{
literal|true
block|,
literal|false
block|}
control|)
block|{
name|XContentBuilder
name|mapping
init|=
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
literal|"target"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"long"
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
name|startObject
argument_list|(
literal|"n1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"nested"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"target"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"long"
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
name|startObject
argument_list|(
literal|"n2"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"nested"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"target"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"long"
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
name|startObject
argument_list|(
literal|"source"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"long"
argument_list|)
operator|.
name|field
argument_list|(
literal|"doc_values"
argument_list|,
literal|false
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"copy_to"
argument_list|)
operator|.
name|value
argument_list|(
literal|"target"
argument_list|)
comment|// should go to the root doc
operator|.
name|value
argument_list|(
literal|"n1.target"
argument_list|)
comment|// should go to the parent doc
operator|.
name|value
argument_list|(
literal|"n1.n2.target"
argument_list|)
comment|// should go to the current doc
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
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
literal|3
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|mapped
condition|)
block|{
name|mapping
operator|=
name|mapping
operator|.
name|startObject
argument_list|(
literal|"target"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"long"
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
expr_stmt|;
block|}
name|mapping
operator|=
name|mapping
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|mapping
operator|=
name|mapping
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|DocumentMapper
name|mapper
init|=
name|parser
operator|.
name|parse
argument_list|(
name|mapping
operator|.
name|string
argument_list|()
argument_list|)
decl_stmt|;
name|XContentBuilder
name|jsonDoc
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startArray
argument_list|(
literal|"n1"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|startArray
argument_list|(
literal|"n2"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"source"
argument_list|,
literal|3
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"source"
argument_list|,
literal|5
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startArray
argument_list|(
literal|"n2"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"source"
argument_list|,
literal|7
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|ParsedDocument
name|doc
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
name|jsonDoc
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|doc
operator|.
name|docs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Document
name|nested
init|=
name|doc
operator|.
name|docs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertFieldValue
argument_list|(
name|nested
argument_list|,
literal|"n1.n2.target"
argument_list|,
literal|7L
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|nested
argument_list|,
literal|"n1.target"
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|nested
argument_list|,
literal|"target"
argument_list|)
expr_stmt|;
name|nested
operator|=
name|doc
operator|.
name|docs
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|nested
argument_list|,
literal|"n1.n2.target"
argument_list|,
literal|5L
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|nested
argument_list|,
literal|"n1.target"
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|nested
argument_list|,
literal|"target"
argument_list|)
expr_stmt|;
name|nested
operator|=
name|doc
operator|.
name|docs
argument_list|()
operator|.
name|get
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|nested
argument_list|,
literal|"n1.n2.target"
argument_list|,
literal|3L
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|nested
argument_list|,
literal|"n1.target"
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|nested
argument_list|,
literal|"target"
argument_list|)
expr_stmt|;
name|Document
name|parent
init|=
name|doc
operator|.
name|docs
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertFieldValue
argument_list|(
name|parent
argument_list|,
literal|"target"
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|parent
argument_list|,
literal|"n1.target"
argument_list|,
literal|7L
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|parent
argument_list|,
literal|"n1.n2.target"
argument_list|)
expr_stmt|;
name|parent
operator|=
name|doc
operator|.
name|docs
argument_list|()
operator|.
name|get
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|parent
argument_list|,
literal|"target"
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|parent
argument_list|,
literal|"n1.target"
argument_list|,
literal|3L
argument_list|,
literal|5L
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|parent
argument_list|,
literal|"n1.n2.target"
argument_list|)
expr_stmt|;
name|Document
name|root
init|=
name|doc
operator|.
name|docs
argument_list|()
operator|.
name|get
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|assertFieldValue
argument_list|(
name|root
argument_list|,
literal|"target"
argument_list|,
literal|3L
argument_list|,
literal|5L
argument_list|,
literal|7L
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|root
argument_list|,
literal|"n1.target"
argument_list|)
expr_stmt|;
name|assertFieldValue
argument_list|(
name|root
argument_list|,
literal|"n1.n2.target"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertFieldValue
specifier|private
name|void
name|assertFieldValue
parameter_list|(
name|Document
name|doc
parameter_list|,
name|String
name|field
parameter_list|,
name|Number
modifier|...
name|expected
parameter_list|)
block|{
name|IndexableField
index|[]
name|values
init|=
name|doc
operator|.
name|getFields
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|==
literal|null
condition|)
block|{
name|values
operator|=
operator|new
name|IndexableField
index|[
literal|0
index|]
expr_stmt|;
block|}
name|Number
index|[]
name|actual
init|=
operator|new
name|Number
index|[
name|values
operator|.
name|length
index|]
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
name|values
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|actual
index|[
name|i
index|]
operator|=
name|values
index|[
name|i
index|]
operator|.
name|numericValue
argument_list|()
expr_stmt|;
block|}
name|assertArrayEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

