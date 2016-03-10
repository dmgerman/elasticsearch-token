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
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|List
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
name|internal
operator|.
name|UidFieldMapper
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
name|object
operator|.
name|ObjectMapper
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

begin_comment
comment|// TODO: make this a real unit test
end_comment

begin_class
DECL|class|DocumentParserTests
specifier|public
class|class
name|DocumentParserTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testTypeDisabled
specifier|public
name|void
name|testTypeDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapperParser
name|mapperParser
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
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|mapper
init|=
name|mapperParser
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
name|bytes
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
literal|"foo"
argument_list|)
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
name|endObject
argument_list|()
operator|.
name|bytes
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
name|bytes
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFieldDisabled
specifier|public
name|void
name|testFieldDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapperParser
name|mapperParser
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
literal|"enabled"
argument_list|,
literal|false
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"bar"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
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
name|mapperParser
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
name|bytes
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
literal|"foo"
argument_list|,
literal|"1234"
argument_list|)
operator|.
name|field
argument_list|(
literal|"bar"
argument_list|,
literal|10
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
name|bytes
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|createDummyMapping
name|DocumentMapper
name|createDummyMapping
parameter_list|(
name|MapperService
name|mapperService
parameter_list|)
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
literal|"a"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"b"
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
literal|"c"
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
name|defaultMapper
init|=
name|mapperService
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
return|return
name|defaultMapper
return|;
block|}
comment|// creates an object mapper, which is about 100x harder than it should be....
DECL|method|createObjectMapper
name|ObjectMapper
name|createObjectMapper
parameter_list|(
name|MapperService
name|mapperService
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|Exception
block|{
name|String
index|[]
name|nameParts
init|=
name|name
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
name|ContentPath
name|path
init|=
operator|new
name|ContentPath
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
name|nameParts
operator|.
name|length
operator|-
literal|1
condition|;
operator|++
name|i
control|)
block|{
name|path
operator|.
name|add
argument_list|(
name|nameParts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|ParseContext
name|context
init|=
operator|new
name|ParseContext
operator|.
name|InternalParseContext
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|mapperService
operator|.
name|documentMapperParser
argument_list|()
argument_list|,
name|mapperService
operator|.
name|documentMapper
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|Mapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|ObjectMapper
operator|.
name|Builder
argument_list|(
name|nameParts
index|[
name|nameParts
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
operator|.
name|enabled
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|Mapper
operator|.
name|BuilderContext
name|builderContext
init|=
operator|new
name|Mapper
operator|.
name|BuilderContext
argument_list|(
name|context
operator|.
name|indexSettings
argument_list|()
argument_list|,
name|context
operator|.
name|path
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|(
name|ObjectMapper
operator|)
name|builder
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
return|;
block|}
DECL|method|testEmptyMappingUpdate
specifier|public
name|void
name|testEmptyMappingUpdate
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapper
name|docMapper
init|=
name|createDummyMapping
argument_list|(
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|DocumentParser
operator|.
name|createDynamicUpdate
argument_list|(
name|docMapper
operator|.
name|mapping
argument_list|()
argument_list|,
name|docMapper
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleMappingUpdate
specifier|public
name|void
name|testSingleMappingUpdate
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapper
name|docMapper
init|=
name|createDummyMapping
argument_list|(
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Mapper
argument_list|>
name|updates
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|MockFieldMapper
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|Mapping
name|mapping
init|=
name|DocumentParser
operator|.
name|createDynamicUpdate
argument_list|(
name|docMapper
operator|.
name|mapping
argument_list|()
argument_list|,
name|docMapper
argument_list|,
name|updates
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|mapping
operator|.
name|root
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSubfieldMappingUpdate
specifier|public
name|void
name|testSubfieldMappingUpdate
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapper
name|docMapper
init|=
name|createDummyMapping
argument_list|(
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Mapper
argument_list|>
name|updates
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|MockFieldMapper
argument_list|(
literal|"a.foo"
argument_list|)
argument_list|)
decl_stmt|;
name|Mapping
name|mapping
init|=
name|DocumentParser
operator|.
name|createDynamicUpdate
argument_list|(
name|docMapper
operator|.
name|mapping
argument_list|()
argument_list|,
name|docMapper
argument_list|,
name|updates
argument_list|)
decl_stmt|;
name|Mapper
name|aMapper
init|=
name|mapping
operator|.
name|root
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|aMapper
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aMapper
operator|instanceof
name|ObjectMapper
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
operator|(
operator|(
name|ObjectMapper
operator|)
name|aMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
operator|(
operator|(
name|ObjectMapper
operator|)
name|aMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultipleSubfieldMappingUpdate
specifier|public
name|void
name|testMultipleSubfieldMappingUpdate
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapper
name|docMapper
init|=
name|createDummyMapping
argument_list|(
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Mapper
argument_list|>
name|updates
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|updates
operator|.
name|add
argument_list|(
operator|new
name|MockFieldMapper
argument_list|(
literal|"a.foo"
argument_list|)
argument_list|)
expr_stmt|;
name|updates
operator|.
name|add
argument_list|(
operator|new
name|MockFieldMapper
argument_list|(
literal|"a.bar"
argument_list|)
argument_list|)
expr_stmt|;
name|Mapping
name|mapping
init|=
name|DocumentParser
operator|.
name|createDynamicUpdate
argument_list|(
name|docMapper
operator|.
name|mapping
argument_list|()
argument_list|,
name|docMapper
argument_list|,
name|updates
argument_list|)
decl_stmt|;
name|Mapper
name|aMapper
init|=
name|mapping
operator|.
name|root
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|aMapper
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aMapper
operator|instanceof
name|ObjectMapper
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
operator|(
operator|(
name|ObjectMapper
operator|)
name|aMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
operator|(
operator|(
name|ObjectMapper
operator|)
name|aMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
operator|(
operator|(
name|ObjectMapper
operator|)
name|aMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDeepSubfieldMappingUpdate
specifier|public
name|void
name|testDeepSubfieldMappingUpdate
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapper
name|docMapper
init|=
name|createDummyMapping
argument_list|(
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Mapper
argument_list|>
name|updates
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|MockFieldMapper
argument_list|(
literal|"a.b.foo"
argument_list|)
argument_list|)
decl_stmt|;
name|Mapping
name|mapping
init|=
name|DocumentParser
operator|.
name|createDynamicUpdate
argument_list|(
name|docMapper
operator|.
name|mapping
argument_list|()
argument_list|,
name|docMapper
argument_list|,
name|updates
argument_list|)
decl_stmt|;
name|Mapper
name|aMapper
init|=
name|mapping
operator|.
name|root
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|aMapper
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|aMapper
operator|instanceof
name|ObjectMapper
argument_list|)
expr_stmt|;
name|Mapper
name|bMapper
init|=
operator|(
operator|(
name|ObjectMapper
operator|)
name|aMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|bMapper
operator|instanceof
name|ObjectMapper
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
operator|(
operator|(
name|ObjectMapper
operator|)
name|bMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
operator|(
operator|(
name|ObjectMapper
operator|)
name|bMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"c"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testObjectMappingUpdate
specifier|public
name|void
name|testObjectMappingUpdate
parameter_list|()
throws|throws
name|Exception
block|{
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
name|docMapper
init|=
name|createDummyMapping
argument_list|(
name|mapperService
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Mapper
argument_list|>
name|updates
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|updates
operator|.
name|add
argument_list|(
name|createObjectMapper
argument_list|(
name|mapperService
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|updates
operator|.
name|add
argument_list|(
name|createObjectMapper
argument_list|(
name|mapperService
argument_list|,
literal|"foo.bar"
argument_list|)
argument_list|)
expr_stmt|;
name|updates
operator|.
name|add
argument_list|(
operator|new
name|MockFieldMapper
argument_list|(
literal|"foo.bar.baz"
argument_list|)
argument_list|)
expr_stmt|;
name|updates
operator|.
name|add
argument_list|(
operator|new
name|MockFieldMapper
argument_list|(
literal|"foo.field"
argument_list|)
argument_list|)
expr_stmt|;
name|Mapping
name|mapping
init|=
name|DocumentParser
operator|.
name|createDynamicUpdate
argument_list|(
name|docMapper
operator|.
name|mapping
argument_list|()
argument_list|,
name|docMapper
argument_list|,
name|updates
argument_list|)
decl_stmt|;
name|Mapper
name|fooMapper
init|=
name|mapping
operator|.
name|root
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|fooMapper
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fooMapper
operator|instanceof
name|ObjectMapper
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
operator|(
operator|(
name|ObjectMapper
operator|)
name|fooMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"field"
argument_list|)
argument_list|)
expr_stmt|;
name|Mapper
name|barMapper
init|=
operator|(
operator|(
name|ObjectMapper
operator|)
name|fooMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"bar"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|barMapper
operator|instanceof
name|ObjectMapper
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
operator|(
operator|(
name|ObjectMapper
operator|)
name|barMapper
operator|)
operator|.
name|getMapper
argument_list|(
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

