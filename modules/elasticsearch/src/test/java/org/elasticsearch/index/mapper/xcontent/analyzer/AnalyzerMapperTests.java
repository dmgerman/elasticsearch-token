begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.xcontent.analyzer
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|xcontent
operator|.
name|analyzer
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
name|analysis
operator|.
name|FieldNameAnalyzer
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
name|analysis
operator|.
name|NamedAnalyzer
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
name|xcontent
operator|.
name|MapperTests
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
name|xcontent
operator|.
name|XContentDocumentMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AnalyzerMapperTests
specifier|public
class|class
name|AnalyzerMapperTests
block|{
DECL|method|testLatLonValues
annotation|@
name|Test
specifier|public
name|void
name|testLatLonValues
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
literal|"_analyzer"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"field_analyzer"
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
literal|"field_analyzer"
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
literal|"field1"
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
literal|"field2"
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
literal|"analyzer"
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
name|XContentDocumentMapper
name|documentMapper
init|=
name|MapperTests
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
literal|"field_analyzer"
argument_list|,
literal|"whitespace"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
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
name|copiedBytes
argument_list|()
argument_list|)
decl_stmt|;
name|FieldNameAnalyzer
name|analyzer
init|=
operator|(
name|FieldNameAnalyzer
operator|)
name|doc
operator|.
name|analyzer
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NamedAnalyzer
operator|)
name|analyzer
operator|.
name|defaultAnalyzer
argument_list|()
operator|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"whitespace"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NamedAnalyzer
operator|)
name|analyzer
operator|.
name|analyzers
argument_list|()
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NamedAnalyzer
operator|)
name|analyzer
operator|.
name|analyzers
argument_list|()
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
operator|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"simple"
argument_list|)
argument_list|)
expr_stmt|;
comment|// check that it serializes and de-serializes correctly
name|XContentDocumentMapper
name|reparsedMapper
init|=
name|MapperTests
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
name|documentMapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|string
argument_list|()
argument_list|)
decl_stmt|;
name|doc
operator|=
name|reparsedMapper
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
literal|"field_analyzer"
argument_list|,
literal|"whitespace"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
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
name|copiedBytes
argument_list|()
argument_list|)
expr_stmt|;
name|analyzer
operator|=
operator|(
name|FieldNameAnalyzer
operator|)
name|doc
operator|.
name|analyzer
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NamedAnalyzer
operator|)
name|analyzer
operator|.
name|defaultAnalyzer
argument_list|()
operator|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"whitespace"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NamedAnalyzer
operator|)
name|analyzer
operator|.
name|analyzers
argument_list|()
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NamedAnalyzer
operator|)
name|analyzer
operator|.
name|analyzers
argument_list|()
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
operator|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"simple"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

