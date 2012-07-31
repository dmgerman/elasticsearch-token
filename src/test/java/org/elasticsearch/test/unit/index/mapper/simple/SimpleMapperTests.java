begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.unit.index.mapper.simple
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
name|simple
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
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Field
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
name|Uid
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
name|MapperTests
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
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Field
operator|.
name|Store
operator|.
name|YES
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
name|copyToBytesFromClasspath
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
name|copyToStringFromClasspath
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperBuilders
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
name|assertThat
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
name|closeTo
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
DECL|class|SimpleMapperTests
specifier|public
class|class
name|SimpleMapperTests
block|{
annotation|@
name|Test
DECL|method|testSimpleMapper
specifier|public
name|void
name|testSimpleMapper
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapperParser
name|mapperParser
init|=
name|MapperTests
operator|.
name|newParser
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|doc
argument_list|(
literal|"test"
argument_list|,
name|rootObject
argument_list|(
literal|"person"
argument_list|)
operator|.
name|add
argument_list|(
name|object
argument_list|(
literal|"name"
argument_list|)
operator|.
name|add
argument_list|(
name|stringField
argument_list|(
literal|"first"
argument_list|)
operator|.
name|store
argument_list|(
name|YES
argument_list|)
operator|.
name|index
argument_list|(
name|Field
operator|.
name|Index
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|(
name|mapperParser
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
literal|"/org/elasticsearch/test/unit/index/mapper/simple/test1.json"
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
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|doc
operator|.
name|getBoost
argument_list|()
argument_list|,
name|closeTo
argument_list|(
literal|3.7
argument_list|,
literal|0.01
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
name|name
argument_list|(
literal|"first"
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
literal|"shay"
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
name|name
argument_list|(
literal|"first"
argument_list|)
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"name.first"
argument_list|)
argument_list|)
expr_stmt|;
comment|//        System.out.println("Document: " + doc);
comment|//        System.out.println("Json: " + docMapper.sourceMapper().value(doc));
name|doc
operator|=
name|docMapper
operator|.
name|parse
argument_list|(
name|json
argument_list|)
operator|.
name|rootDoc
argument_list|()
expr_stmt|;
comment|//        System.out.println("Document: " + doc);
comment|//        System.out.println("Json: " + docMapper.sourceMapper().value(doc));
block|}
annotation|@
name|Test
DECL|method|testParseToJsonAndParse
specifier|public
name|void
name|testParseToJsonAndParse
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/test/unit/index/mapper/simple/test-mapping.json"
argument_list|)
decl_stmt|;
name|DocumentMapper
name|docMapper
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
comment|//        System.out.println(builtMapping);
comment|// reparse it
name|DocumentMapper
name|builtDocMapper
init|=
name|MapperTests
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
name|builtMapping
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
literal|"/org/elasticsearch/test/unit/index/mapper/simple/test1.json"
argument_list|)
argument_list|)
decl_stmt|;
name|Document
name|doc
init|=
name|builtDocMapper
operator|.
name|parse
argument_list|(
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
name|get
argument_list|(
name|docMapper
operator|.
name|uidMapper
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
name|Uid
operator|.
name|createUid
argument_list|(
literal|"person"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|doc
operator|.
name|getBoost
argument_list|()
argument_list|,
name|closeTo
argument_list|(
literal|3.7
argument_list|,
literal|0.01
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
name|name
argument_list|(
literal|"first"
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
literal|"shay"
argument_list|)
argument_list|)
expr_stmt|;
comment|//        System.out.println("Document: " + doc);
comment|//        System.out.println("Json: " + docMapper.sourceMapper().value(doc));
block|}
annotation|@
name|Test
DECL|method|testSimpleParser
specifier|public
name|void
name|testSimpleParser
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/test/unit/index/mapper/simple/test-mapping.json"
argument_list|)
decl_stmt|;
name|DocumentMapper
name|docMapper
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
name|assertThat
argument_list|(
operator|(
name|String
operator|)
name|docMapper
operator|.
name|meta
argument_list|()
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|BytesReference
name|json
init|=
operator|new
name|BytesArray
argument_list|(
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/test/unit/index/mapper/simple/test1.json"
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
name|get
argument_list|(
name|docMapper
operator|.
name|uidMapper
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
name|Uid
operator|.
name|createUid
argument_list|(
literal|"person"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|doc
operator|.
name|getBoost
argument_list|()
argument_list|,
name|closeTo
argument_list|(
literal|3.7
argument_list|,
literal|0.01
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
name|name
argument_list|(
literal|"first"
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
literal|"shay"
argument_list|)
argument_list|)
expr_stmt|;
comment|//        System.out.println("Document: " + doc);
comment|//        System.out.println("Json: " + docMapper.sourceMapper().value(doc));
block|}
annotation|@
name|Test
DECL|method|testSimpleParserNoTypeNoId
specifier|public
name|void
name|testSimpleParserNoTypeNoId
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/test/unit/index/mapper/simple/test-mapping.json"
argument_list|)
decl_stmt|;
name|DocumentMapper
name|docMapper
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
name|BytesReference
name|json
init|=
operator|new
name|BytesArray
argument_list|(
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/test/unit/index/mapper/simple/test1-notype-noid.json"
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
name|assertThat
argument_list|(
name|doc
operator|.
name|get
argument_list|(
name|docMapper
operator|.
name|uidMapper
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
name|Uid
operator|.
name|createUid
argument_list|(
literal|"person"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|doc
operator|.
name|getBoost
argument_list|()
argument_list|,
name|closeTo
argument_list|(
literal|3.7
argument_list|,
literal|0.01
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
name|name
argument_list|(
literal|"first"
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
literal|"shay"
argument_list|)
argument_list|)
expr_stmt|;
comment|//        System.out.println("Document: " + doc);
comment|//        System.out.println("Json: " + docMapper.sourceMapper().value(doc));
block|}
annotation|@
name|Test
DECL|method|testAttributes
specifier|public
name|void
name|testAttributes
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/test/unit/index/mapper/simple/test-mapping.json"
argument_list|)
decl_stmt|;
name|DocumentMapper
name|docMapper
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
name|assertThat
argument_list|(
operator|(
name|String
operator|)
name|docMapper
operator|.
name|meta
argument_list|()
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
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
name|DocumentMapper
name|builtDocMapper
init|=
name|MapperTests
operator|.
name|newParser
argument_list|()
operator|.
name|parse
argument_list|(
name|builtMapping
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
name|String
operator|)
name|builtDocMapper
operator|.
name|meta
argument_list|()
operator|.
name|get
argument_list|(
literal|"param1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

