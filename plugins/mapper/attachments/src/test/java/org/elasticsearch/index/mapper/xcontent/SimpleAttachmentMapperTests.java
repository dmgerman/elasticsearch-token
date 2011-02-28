begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.xcontent
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
name|document
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
name|Index
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
name|AnalysisService
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
name|BeforeClass
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
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|Streams
operator|.
name|*
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
name|*
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
annotation|@
name|Test
DECL|class|SimpleAttachmentMapperTests
specifier|public
class|class
name|SimpleAttachmentMapperTests
block|{
DECL|field|mapperParser
specifier|private
name|XContentDocumentMapperParser
name|mapperParser
decl_stmt|;
DECL|method|setupMapperParser
annotation|@
name|BeforeClass
specifier|public
name|void
name|setupMapperParser
parameter_list|()
block|{
name|mapperParser
operator|=
operator|new
name|XContentDocumentMapperParser
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|,
operator|new
name|AnalysisService
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mapperParser
operator|.
name|putTypeParser
argument_list|(
name|AttachmentMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|AttachmentMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleMappings
annotation|@
name|Test
specifier|public
name|void
name|testSimpleMappings
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/xcontent/test-mapping.json"
argument_list|)
decl_stmt|;
name|XContentDocumentMapper
name|docMapper
init|=
name|mapperParser
operator|.
name|parse
argument_list|(
name|mapping
argument_list|)
decl_stmt|;
name|byte
index|[]
name|html
init|=
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/xcontent/testXHTML.html"
argument_list|)
decl_stmt|;
name|byte
index|[]
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
literal|"_id"
argument_list|,
literal|1
argument_list|)
operator|.
name|field
argument_list|(
literal|"file"
argument_list|,
name|html
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|copiedBytes
argument_list|()
decl_stmt|;
name|Document
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
name|json
argument_list|)
operator|.
name|doc
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|get
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
literal|"file.title"
argument_list|)
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"XHTML test document"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|get
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
literal|"file"
argument_list|)
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"This document tests the ability of Apache Tika to extract content"
argument_list|)
argument_list|)
expr_stmt|;
comment|// re-parse it
name|String
name|builtMapping
init|=
name|docMapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|docMapper
operator|=
name|mapperParser
operator|.
name|parse
argument_list|(
name|builtMapping
argument_list|)
expr_stmt|;
name|json
operator|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"_id"
argument_list|,
literal|1
argument_list|)
operator|.
name|field
argument_list|(
literal|"file"
argument_list|,
name|html
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|copiedBytes
argument_list|()
expr_stmt|;
name|doc
operator|=
name|docMapper
operator|.
name|parse
argument_list|(
name|json
argument_list|)
operator|.
name|doc
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|get
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
literal|"file.title"
argument_list|)
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"XHTML test document"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doc
operator|.
name|get
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
literal|"file"
argument_list|)
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"This document tests the ability of Apache Tika to extract content"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

