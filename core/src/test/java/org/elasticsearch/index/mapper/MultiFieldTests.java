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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRef
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
name|XContentHelper
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
name|support
operator|.
name|XContentMapValues
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
name|DateFieldMapper
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
name|KeywordFieldMapper
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
name|RootObjectMapper
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
name|TextFieldMapper
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
name|TokenCountFieldMapper
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
name|StreamsUtils
operator|.
name|copyToBytesFromClasspath
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
name|StreamsUtils
operator|.
name|copyToStringFromClasspath
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
name|notNullValue
import|;
end_import

begin_class
DECL|class|MultiFieldTests
specifier|public
class|class
name|MultiFieldTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testMultiFieldMultiFields
specifier|public
name|void
name|testMultiFieldMultiFields
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/multifield/test-multi-fields.json"
argument_list|)
decl_stmt|;
name|testMultiField
argument_list|(
name|mapping
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiField
specifier|private
name|void
name|testMultiField
parameter_list|(
name|String
name|mapping
parameter_list|)
throws|throws
name|Exception
block|{
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
literal|"person"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
argument_list|)
argument_list|)
decl_stmt|;
name|BytesReference
name|json
init|=
operator|new
name|BytesArray
argument_list|(
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/multifield/test-data.json"
argument_list|)
argument_list|)
decl_stmt|;
name|Document
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
literal|"person"
argument_list|,
literal|"1"
argument_list|,
name|json
argument_list|)
operator|.
name|rootDoc
argument_list|()
decl_stmt|;
name|IndexableField
name|f
init|=
name|doc
operator|.
name|getField
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"some name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
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
name|f
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
argument_list|)
expr_stmt|;
name|f
operator|=
name|doc
operator|.
name|getField
argument_list|(
literal|"name.indexed"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"name.indexed"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"some name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|f
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
argument_list|)
expr_stmt|;
name|f
operator|=
name|doc
operator|.
name|getField
argument_list|(
literal|"name.not_indexed"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"name.not_indexed"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"some name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
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
name|assertEquals
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|f
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
argument_list|)
expr_stmt|;
name|f
operator|=
name|doc
operator|.
name|getField
argument_list|(
literal|"object1.multi1"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"object1.multi1"
argument_list|)
argument_list|)
expr_stmt|;
name|f
operator|=
name|doc
operator|.
name|getField
argument_list|(
literal|"object1.multi1.string"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"object1.multi1.string"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|binaryValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"2010-01-01"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|TextFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name"
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name"
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
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|tokenized
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.indexed"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.indexed"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|TextFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.indexed"
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.indexed"
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
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.indexed"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|tokenized
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.not_indexed"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.not_indexed"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|TextFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.not_indexed"
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.not_indexed"
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
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.not_indexed"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|tokenized
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test1"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|TextFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test1"
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test1"
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
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|tokenized
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|eagerGlobalOrdinals
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test2"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test2"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|TokenCountFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test2"
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test2"
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
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test2"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|tokenized
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
operator|(
operator|(
name|TokenCountFieldMapper
operator|)
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test2"
argument_list|)
operator|)
operator|.
name|analyzer
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"simple"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|TokenCountFieldMapper
operator|)
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"name.test2"
argument_list|)
operator|)
operator|.
name|analyzer
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"simple"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"object1.multi1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"object1.multi1"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|DateFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"object1.multi1.string"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"object1.multi1.string"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|KeywordFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"object1.multi1.string"
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
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"object1.multi1.string"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|tokenized
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testBuildThenParse
specifier|public
name|void
name|testBuildThenParse
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
name|DocumentMapper
name|builderDocMapper
init|=
operator|new
name|DocumentMapper
operator|.
name|Builder
argument_list|(
operator|new
name|RootObjectMapper
operator|.
name|Builder
argument_list|(
literal|"person"
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|TextFieldMapper
operator|.
name|Builder
argument_list|(
literal|"name"
argument_list|)
operator|.
name|store
argument_list|(
literal|true
argument_list|)
operator|.
name|addMultiField
argument_list|(
operator|new
name|TextFieldMapper
operator|.
name|Builder
argument_list|(
literal|"indexed"
argument_list|)
operator|.
name|index
argument_list|(
literal|true
argument_list|)
operator|.
name|tokenized
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|addMultiField
argument_list|(
operator|new
name|TextFieldMapper
operator|.
name|Builder
argument_list|(
literal|"not_indexed"
argument_list|)
operator|.
name|index
argument_list|(
literal|false
argument_list|)
operator|.
name|store
argument_list|(
literal|true
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|indexService
operator|.
name|mapperService
argument_list|()
argument_list|)
operator|.
name|build
argument_list|(
name|indexService
operator|.
name|mapperService
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|builtMapping
init|=
name|builderDocMapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
comment|//        System.out.println(builtMapping);
comment|// reparse it
name|DocumentMapper
name|docMapper
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|parse
argument_list|(
literal|"person"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|builtMapping
argument_list|)
argument_list|)
decl_stmt|;
name|BytesReference
name|json
init|=
operator|new
name|BytesArray
argument_list|(
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/multifield/test-data.json"
argument_list|)
argument_list|)
decl_stmt|;
name|Document
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
literal|"person"
argument_list|,
literal|"1"
argument_list|,
name|json
argument_list|)
operator|.
name|rootDoc
argument_list|()
decl_stmt|;
name|IndexableField
name|f
init|=
name|doc
operator|.
name|getField
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"some name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
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
name|f
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
argument_list|)
expr_stmt|;
name|f
operator|=
name|doc
operator|.
name|getField
argument_list|(
literal|"name.indexed"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"name.indexed"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"some name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
operator|.
name|tokenized
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
name|f
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|f
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
argument_list|)
expr_stmt|;
name|f
operator|=
name|doc
operator|.
name|getField
argument_list|(
literal|"name.not_indexed"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"name.not_indexed"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
operator|.
name|stringValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"some name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|f
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
name|assertEquals
argument_list|(
name|IndexOptions
operator|.
name|NONE
argument_list|,
name|f
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// The underlying order of the fields in multi fields in the mapping source should always be consistent, if not this
comment|// can to unnecessary re-syncing of the mappings between the local instance and cluster state
DECL|method|testMultiFieldsInConsistentOrder
specifier|public
name|void
name|testMultiFieldsInConsistentOrder
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|multiFieldNames
init|=
operator|new
name|String
index|[
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
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
name|multiFieldNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|multiFieldNames
index|[
name|i
index|]
operator|=
name|randomAsciiOfLength
argument_list|(
literal|4
argument_list|)
expr_stmt|;
block|}
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
literal|"my_field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|multiFieldName
range|:
name|multiFieldNames
control|)
block|{
name|builder
operator|=
name|builder
operator|.
name|startObject
argument_list|(
name|multiFieldName
argument_list|)
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
expr_stmt|;
block|}
name|builder
operator|=
name|builder
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
expr_stmt|;
name|String
name|mapping
init|=
name|builder
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
name|Arrays
operator|.
name|sort
argument_list|(
name|multiFieldNames
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
init|=
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|docMapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|compressedReference
argument_list|()
argument_list|,
literal|true
argument_list|,
name|builder
operator|.
name|contentType
argument_list|()
argument_list|)
operator|.
name|v2
argument_list|()
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|multiFields
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"type.properties.my_field.fields"
argument_list|,
name|sourceAsMap
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|multiFields
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|multiFieldNames
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
comment|// underlying map is LinkedHashMap, so this ok:
for|for
control|(
name|String
name|field
range|:
name|multiFields
operator|.
name|keySet
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|field
argument_list|,
name|equalTo
argument_list|(
name|multiFieldNames
index|[
name|i
operator|++
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testObjectFieldNotAllowed
specifier|public
name|void
name|testObjectFieldNotAllowed
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
literal|"my_field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"multi"
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
specifier|final
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
literal|"expected mapping parse failure"
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
literal|"cannot be used in multi field"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testNestedFieldNotAllowed
specifier|public
name|void
name|testNestedFieldNotAllowed
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
literal|"my_field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"multi"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"nested"
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
specifier|final
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
literal|"expected mapping parse failure"
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
literal|"cannot be used in multi field"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testMultiFieldWithDot
specifier|public
name|void
name|testMultiFieldWithDot
parameter_list|()
throws|throws
name|IOException
block|{
name|XContentBuilder
name|mapping
init|=
name|jsonBuilder
argument_list|()
decl_stmt|;
name|mapping
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"my_type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"city"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"raw.foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
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
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
expr_stmt|;
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
try|try
block|{
name|mapperService
operator|.
name|documentMapperParser
argument_list|()
operator|.
name|parse
argument_list|(
literal|"my_type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|mapping
operator|.
name|string
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"this should throw an exception because one field contains a dot"
argument_list|)
expr_stmt|;
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
literal|"Field name [raw.foo] which is a multi field of [city] cannot contain '.'"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

