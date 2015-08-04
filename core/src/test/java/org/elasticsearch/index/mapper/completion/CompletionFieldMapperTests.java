begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.completion
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|completion
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
name|core
operator|.
name|CompletionFieldMapper
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

begin_class
DECL|class|CompletionFieldMapperTests
specifier|public
class|class
name|CompletionFieldMapperTests
extends|extends
name|ESSingleNodeTestCase
block|{
annotation|@
name|Test
DECL|method|testDefaultConfiguration
specifier|public
name|void
name|testDefaultConfiguration
parameter_list|()
throws|throws
name|IOException
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
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"completion"
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
name|defaultMapper
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
name|FieldMapper
name|fieldMapper
init|=
name|defaultMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"completion"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldMapper
argument_list|,
name|instanceOf
argument_list|(
name|CompletionFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|CompletionFieldMapper
name|completionFieldMapper
init|=
operator|(
name|CompletionFieldMapper
operator|)
name|fieldMapper
decl_stmt|;
name|assertThat
argument_list|(
name|completionFieldMapper
operator|.
name|isStoringPayloads
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testThatSerializationIncludesAllElements
specifier|public
name|void
name|testThatSerializationIncludesAllElements
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
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"simple"
argument_list|)
operator|.
name|field
argument_list|(
literal|"search_analyzer"
argument_list|,
literal|"standard"
argument_list|)
operator|.
name|field
argument_list|(
literal|"payloads"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"preserve_separators"
argument_list|,
literal|false
argument_list|)
operator|.
name|field
argument_list|(
literal|"preserve_position_increments"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"max_input_length"
argument_list|,
literal|14
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
name|defaultMapper
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
name|FieldMapper
name|fieldMapper
init|=
name|defaultMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"completion"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldMapper
argument_list|,
name|instanceOf
argument_list|(
name|CompletionFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|CompletionFieldMapper
name|completionFieldMapper
init|=
operator|(
name|CompletionFieldMapper
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
name|completionFieldMapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
literal|null
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
name|configMap
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
literal|"completion"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|configMap
operator|.
name|get
argument_list|(
literal|"analyzer"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"simple"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|configMap
operator|.
name|get
argument_list|(
literal|"search_analyzer"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"standard"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Boolean
operator|.
name|valueOf
argument_list|(
name|configMap
operator|.
name|get
argument_list|(
literal|"payloads"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Boolean
operator|.
name|valueOf
argument_list|(
name|configMap
operator|.
name|get
argument_list|(
literal|"preserve_separators"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Boolean
operator|.
name|valueOf
argument_list|(
name|configMap
operator|.
name|get
argument_list|(
literal|"preserve_position_increments"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|configMap
operator|.
name|get
argument_list|(
literal|"max_input_length"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
literal|14
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testThatSerializationCombinesToOneAnalyzerFieldIfBothAreEqual
specifier|public
name|void
name|testThatSerializationCombinesToOneAnalyzerFieldIfBothAreEqual
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
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"simple"
argument_list|)
operator|.
name|field
argument_list|(
literal|"search_analyzer"
argument_list|,
literal|"simple"
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
name|defaultMapper
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
name|FieldMapper
name|fieldMapper
init|=
name|defaultMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"completion"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldMapper
argument_list|,
name|instanceOf
argument_list|(
name|CompletionFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|CompletionFieldMapper
name|completionFieldMapper
init|=
operator|(
name|CompletionFieldMapper
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
name|completionFieldMapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
literal|null
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
name|configMap
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
literal|"completion"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|configMap
operator|.
name|get
argument_list|(
literal|"analyzer"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"simple"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

