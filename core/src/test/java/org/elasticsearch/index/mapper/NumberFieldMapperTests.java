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
name|HashSet
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

begin_class
DECL|class|NumberFieldMapperTests
specifier|public
class|class
name|NumberFieldMapperTests
extends|extends
name|AbstractNumericFieldMapperTestCase
block|{
annotation|@
name|Override
DECL|method|setTypeList
specifier|protected
name|void
name|setTypeList
parameter_list|()
block|{
name|TYPES
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"byte"
argument_list|,
literal|"short"
argument_list|,
literal|"integer"
argument_list|,
literal|"long"
argument_list|,
literal|"float"
argument_list|,
literal|"double"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doTestDefaults
specifier|public
name|void
name|doTestDefaults
parameter_list|(
name|String
name|type
parameter_list|)
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
name|type
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
name|assertEquals
argument_list|(
name|mapping
argument_list|,
name|mapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ParsedDocument
name|doc
init|=
name|mapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
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
literal|123
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
name|IndexableField
name|pointField
init|=
name|fields
index|[
literal|0
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|pointField
operator|.
name|fieldType
argument_list|()
operator|.
name|pointDimensionCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|pointField
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|123
argument_list|,
name|pointField
operator|.
name|numericValue
argument_list|()
operator|.
name|doubleValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
name|IndexableField
name|dvField
init|=
name|fields
index|[
literal|1
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|DocValuesType
operator|.
name|SORTED_NUMERIC
argument_list|,
name|dvField
operator|.
name|fieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|dvField
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doTestNotIndexed
specifier|public
name|void
name|doTestNotIndexed
parameter_list|(
name|String
name|type
parameter_list|)
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
name|type
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
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
name|assertEquals
argument_list|(
name|mapping
argument_list|,
name|mapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ParsedDocument
name|doc
init|=
name|mapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
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
literal|123
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
name|IndexableField
name|dvField
init|=
name|fields
index|[
literal|0
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|DocValuesType
operator|.
name|SORTED_NUMERIC
argument_list|,
name|dvField
operator|.
name|fieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doTestNoDocValues
specifier|public
name|void
name|doTestNoDocValues
parameter_list|(
name|String
name|type
parameter_list|)
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
name|type
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
name|assertEquals
argument_list|(
name|mapping
argument_list|,
name|mapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ParsedDocument
name|doc
init|=
name|mapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
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
literal|123
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
name|IndexableField
name|pointField
init|=
name|fields
index|[
literal|0
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|pointField
operator|.
name|fieldType
argument_list|()
operator|.
name|pointDimensionCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|123
argument_list|,
name|pointField
operator|.
name|numericValue
argument_list|()
operator|.
name|doubleValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doTestStore
specifier|public
name|void
name|doTestStore
parameter_list|(
name|String
name|type
parameter_list|)
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
name|type
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
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
name|assertEquals
argument_list|(
name|mapping
argument_list|,
name|mapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ParsedDocument
name|doc
init|=
name|mapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
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
literal|123
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
name|IndexableField
name|pointField
init|=
name|fields
index|[
literal|0
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|pointField
operator|.
name|fieldType
argument_list|()
operator|.
name|pointDimensionCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|123
argument_list|,
name|pointField
operator|.
name|numericValue
argument_list|()
operator|.
name|doubleValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
name|IndexableField
name|dvField
init|=
name|fields
index|[
literal|1
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|DocValuesType
operator|.
name|SORTED_NUMERIC
argument_list|,
name|dvField
operator|.
name|fieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|)
expr_stmt|;
name|IndexableField
name|storedField
init|=
name|fields
index|[
literal|2
index|]
decl_stmt|;
name|assertTrue
argument_list|(
name|storedField
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|123
argument_list|,
name|storedField
operator|.
name|numericValue
argument_list|()
operator|.
name|doubleValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doTestCoerce
specifier|public
name|void
name|doTestCoerce
parameter_list|(
name|String
name|type
parameter_list|)
throws|throws
name|IOException
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
name|type
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
name|assertEquals
argument_list|(
name|mapping
argument_list|,
name|mapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ParsedDocument
name|doc
init|=
name|mapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
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
literal|"123"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
name|IndexableField
name|pointField
init|=
name|fields
index|[
literal|0
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|pointField
operator|.
name|fieldType
argument_list|()
operator|.
name|pointDimensionCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|123
argument_list|,
name|pointField
operator|.
name|numericValue
argument_list|()
operator|.
name|doubleValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
name|IndexableField
name|dvField
init|=
name|fields
index|[
literal|1
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|DocValuesType
operator|.
name|SORTED_NUMERIC
argument_list|,
name|dvField
operator|.
name|fieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|)
expr_stmt|;
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
name|type
argument_list|)
operator|.
name|field
argument_list|(
literal|"coerce"
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
expr_stmt|;
name|DocumentMapper
name|mapper2
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
name|assertEquals
argument_list|(
name|mapping
argument_list|,
name|mapper2
operator|.
name|mappingSource
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ThrowingRunnable
name|runnable
init|=
parameter_list|()
lambda|->
name|mapper2
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
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
literal|"123"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|MapperParsingException
name|e
init|=
name|expectThrows
argument_list|(
name|MapperParsingException
operator|.
name|class
argument_list|,
name|runnable
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"passed as String"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIgnoreMalformed
specifier|public
name|void
name|testIgnoreMalformed
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|String
name|type
range|:
name|TYPES
control|)
block|{
name|doTestIgnoreMalformed
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|doTestIgnoreMalformed
specifier|private
name|void
name|doTestIgnoreMalformed
parameter_list|(
name|String
name|type
parameter_list|)
throws|throws
name|IOException
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
name|type
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
name|assertEquals
argument_list|(
name|mapping
argument_list|,
name|mapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ThrowingRunnable
name|runnable
init|=
parameter_list|()
lambda|->
name|mapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
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
literal|"a"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|MapperParsingException
name|e
init|=
name|expectThrows
argument_list|(
name|MapperParsingException
operator|.
name|class
argument_list|,
name|runnable
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"For input string: \"a\""
argument_list|)
argument_list|)
expr_stmt|;
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
name|type
argument_list|)
operator|.
name|field
argument_list|(
literal|"ignore_malformed"
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
name|DocumentMapper
name|mapper2
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
name|doc
init|=
name|mapper2
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
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
literal|"a"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|testRejectNorms
specifier|public
name|void
name|testRejectNorms
parameter_list|()
throws|throws
name|IOException
block|{
comment|// not supported as of 5.0
for|for
control|(
name|String
name|type
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|"byte"
argument_list|,
literal|"short"
argument_list|,
literal|"integer"
argument_list|,
literal|"long"
argument_list|,
literal|"float"
argument_list|,
literal|"double"
argument_list|)
control|)
block|{
name|DocumentMapperParser
name|parser
init|=
name|createIndex
argument_list|(
literal|"index-"
operator|+
name|type
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
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
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|type
argument_list|)
operator|.
name|field
argument_list|(
literal|"norms"
argument_list|,
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
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
name|MapperParsingException
name|e
init|=
name|expectThrows
argument_list|(
name|MapperParsingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
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
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Mapping definition for [foo] has unsupported parameters:  [norms"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doTestNullValue
specifier|protected
name|void
name|doTestNullValue
parameter_list|(
name|String
name|type
parameter_list|)
throws|throws
name|IOException
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
name|type
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
name|assertEquals
argument_list|(
name|mapping
argument_list|,
name|mapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ParsedDocument
name|doc
init|=
name|mapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
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
name|nullField
argument_list|(
literal|"field"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|IndexableField
index|[
literal|0
index|]
argument_list|,
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
literal|"field"
argument_list|)
argument_list|)
expr_stmt|;
name|Object
name|missing
decl_stmt|;
if|if
condition|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"float"
argument_list|,
literal|"double"
argument_list|)
operator|.
name|contains
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|missing
operator|=
literal|123d
expr_stmt|;
block|}
else|else
block|{
name|missing
operator|=
literal|123L
expr_stmt|;
block|}
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
name|type
argument_list|)
operator|.
name|field
argument_list|(
literal|"null_value"
argument_list|,
name|missing
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
name|mapper
operator|=
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
name|assertEquals
argument_list|(
name|mapping
argument_list|,
name|mapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|doc
operator|=
name|mapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
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
name|nullField
argument_list|(
literal|"field"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
expr_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
name|IndexableField
name|pointField
init|=
name|fields
index|[
literal|0
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|pointField
operator|.
name|fieldType
argument_list|()
operator|.
name|pointDimensionCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|pointField
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|123
argument_list|,
name|pointField
operator|.
name|numericValue
argument_list|()
operator|.
name|doubleValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
name|IndexableField
name|dvField
init|=
name|fields
index|[
literal|1
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|DocValuesType
operator|.
name|SORTED_NUMERIC
argument_list|,
name|dvField
operator|.
name|fieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|dvField
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmptyName
specifier|public
name|void
name|testEmptyName
parameter_list|()
throws|throws
name|IOException
block|{
comment|// after version 5
for|for
control|(
name|String
name|type
range|:
name|TYPES
control|)
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
literal|""
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|type
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
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"name cannot be empty string"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

