begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.termvector
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|termvector
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
name|analysis
operator|.
name|standard
operator|.
name|StandardAnalyzer
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
name|*
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
name|*
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
name|IndexWriterConfig
operator|.
name|OpenMode
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
name|search
operator|.
name|IndexSearcher
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
name|search
operator|.
name|ScoreDoc
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
name|search
operator|.
name|TermQuery
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
name|search
operator|.
name|TopDocs
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
name|store
operator|.
name|Directory
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
name|termvector
operator|.
name|TermVectorRequest
operator|.
name|Flag
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
name|io
operator|.
name|Streams
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
name|io
operator|.
name|stream
operator|.
name|InputStreamStreamInput
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
name|io
operator|.
name|stream
operator|.
name|OutputStreamStreamOutput
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
name|core
operator|.
name|AbstractFieldMapper
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
name|TypeParsers
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
name|AllFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|termvector
operator|.
name|RestTermVectorAction
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
name|ElasticsearchLuceneTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
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
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|EnumSet
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
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

begin_class
DECL|class|TermVectorUnitTests
specifier|public
class|class
name|TermVectorUnitTests
extends|extends
name|ElasticsearchLuceneTestCase
block|{
annotation|@
name|Test
DECL|method|streamResponse
specifier|public
name|void
name|streamResponse
parameter_list|()
throws|throws
name|Exception
block|{
name|TermVectorResponse
name|outResponse
init|=
operator|new
name|TermVectorResponse
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
decl_stmt|;
name|outResponse
operator|.
name|setExists
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|writeStandardTermVector
argument_list|(
name|outResponse
argument_list|)
expr_stmt|;
comment|// write
name|ByteArrayOutputStream
name|outBuffer
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|OutputStreamStreamOutput
name|out
init|=
operator|new
name|OutputStreamStreamOutput
argument_list|(
name|outBuffer
argument_list|)
decl_stmt|;
name|outResponse
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
comment|// read
name|ByteArrayInputStream
name|esInBuffer
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|outBuffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|InputStreamStreamInput
name|esBuffer
init|=
operator|new
name|InputStreamStreamInput
argument_list|(
name|esInBuffer
argument_list|)
decl_stmt|;
name|TermVectorResponse
name|inResponse
init|=
operator|new
name|TermVectorResponse
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
decl_stmt|;
name|inResponse
operator|.
name|readFrom
argument_list|(
name|esBuffer
argument_list|)
expr_stmt|;
comment|// see if correct
name|checkIfStandardTermVector
argument_list|(
name|inResponse
argument_list|)
expr_stmt|;
name|outResponse
operator|=
operator|new
name|TermVectorResponse
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
expr_stmt|;
name|writeEmptyTermVector
argument_list|(
name|outResponse
argument_list|)
expr_stmt|;
comment|// write
name|outBuffer
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|out
operator|=
operator|new
name|OutputStreamStreamOutput
argument_list|(
name|outBuffer
argument_list|)
expr_stmt|;
name|outResponse
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
comment|// read
name|esInBuffer
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|outBuffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|esBuffer
operator|=
operator|new
name|InputStreamStreamInput
argument_list|(
name|esInBuffer
argument_list|)
expr_stmt|;
name|inResponse
operator|=
operator|new
name|TermVectorResponse
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
expr_stmt|;
name|inResponse
operator|.
name|readFrom
argument_list|(
name|esBuffer
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|inResponse
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|writeEmptyTermVector
specifier|private
name|void
name|writeEmptyTermVector
parameter_list|(
name|TermVectorResponse
name|outResponse
parameter_list|)
throws|throws
name|IOException
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriterConfig
name|conf
init|=
operator|new
name|IndexWriterConfig
argument_list|(
operator|new
name|StandardAnalyzer
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setOpenMode
argument_list|(
name|OpenMode
operator|.
name|CREATE
argument_list|)
expr_stmt|;
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|FieldType
name|type
init|=
operator|new
name|FieldType
argument_list|(
name|TextField
operator|.
name|TYPE_STORED
argument_list|)
decl_stmt|;
name|type
operator|.
name|setStoreTermVectorOffsets
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|type
operator|.
name|setStoreTermVectorPayloads
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|type
operator|.
name|setStoreTermVectorPositions
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|type
operator|.
name|setStoreTermVectors
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|type
operator|.
name|freeze
argument_list|()
expr_stmt|;
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
literal|"id"
argument_list|,
literal|"abc"
argument_list|,
name|StringField
operator|.
name|TYPE_STORED
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|updateDocument
argument_list|(
operator|new
name|Term
argument_list|(
literal|"id"
argument_list|,
literal|"abc"
argument_list|)
argument_list|,
name|d
argument_list|)
expr_stmt|;
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|DirectoryReader
name|dr
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|dir
argument_list|)
decl_stmt|;
name|IndexSearcher
name|s
init|=
operator|new
name|IndexSearcher
argument_list|(
name|dr
argument_list|)
decl_stmt|;
name|TopDocs
name|search
init|=
name|s
operator|.
name|search
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"id"
argument_list|,
literal|"abc"
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|ScoreDoc
index|[]
name|scoreDocs
init|=
name|search
operator|.
name|scoreDocs
decl_stmt|;
name|int
name|doc
init|=
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
decl_stmt|;
name|Fields
name|fields
init|=
name|dr
operator|.
name|getTermVectors
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|EnumSet
argument_list|<
name|Flag
argument_list|>
name|flags
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|Flag
operator|.
name|Positions
argument_list|,
name|Flag
operator|.
name|Offsets
argument_list|)
decl_stmt|;
name|outResponse
operator|.
name|setFields
argument_list|(
name|fields
argument_list|,
literal|null
argument_list|,
name|flags
argument_list|,
name|fields
argument_list|)
expr_stmt|;
name|outResponse
operator|.
name|setExists
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|dr
operator|.
name|close
argument_list|()
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|writeStandardTermVector
specifier|private
name|void
name|writeStandardTermVector
parameter_list|(
name|TermVectorResponse
name|outResponse
parameter_list|)
throws|throws
name|IOException
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriterConfig
name|conf
init|=
operator|new
name|IndexWriterConfig
argument_list|(
operator|new
name|StandardAnalyzer
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setOpenMode
argument_list|(
name|OpenMode
operator|.
name|CREATE
argument_list|)
expr_stmt|;
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|FieldType
name|type
init|=
operator|new
name|FieldType
argument_list|(
name|TextField
operator|.
name|TYPE_STORED
argument_list|)
decl_stmt|;
name|type
operator|.
name|setStoreTermVectorOffsets
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|type
operator|.
name|setStoreTermVectorPayloads
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|type
operator|.
name|setStoreTermVectorPositions
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|type
operator|.
name|setStoreTermVectors
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|type
operator|.
name|freeze
argument_list|()
expr_stmt|;
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
literal|"id"
argument_list|,
literal|"abc"
argument_list|,
name|StringField
operator|.
name|TYPE_STORED
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
literal|"title"
argument_list|,
literal|"the1 quick brown fox jumps over  the1 lazy dog"
argument_list|,
name|type
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
literal|"desc"
argument_list|,
literal|"the1 quick brown fox jumps over  the1 lazy dog"
argument_list|,
name|type
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|updateDocument
argument_list|(
operator|new
name|Term
argument_list|(
literal|"id"
argument_list|,
literal|"abc"
argument_list|)
argument_list|,
name|d
argument_list|)
expr_stmt|;
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|DirectoryReader
name|dr
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|dir
argument_list|)
decl_stmt|;
name|IndexSearcher
name|s
init|=
operator|new
name|IndexSearcher
argument_list|(
name|dr
argument_list|)
decl_stmt|;
name|TopDocs
name|search
init|=
name|s
operator|.
name|search
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"id"
argument_list|,
literal|"abc"
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|ScoreDoc
index|[]
name|scoreDocs
init|=
name|search
operator|.
name|scoreDocs
decl_stmt|;
name|int
name|doc
init|=
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
decl_stmt|;
name|Fields
name|termVectors
init|=
name|dr
operator|.
name|getTermVectors
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|EnumSet
argument_list|<
name|Flag
argument_list|>
name|flags
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|Flag
operator|.
name|Positions
argument_list|,
name|Flag
operator|.
name|Offsets
argument_list|)
decl_stmt|;
name|outResponse
operator|.
name|setFields
argument_list|(
name|termVectors
argument_list|,
literal|null
argument_list|,
name|flags
argument_list|,
name|termVectors
argument_list|)
expr_stmt|;
name|dr
operator|.
name|close
argument_list|()
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|checkIfStandardTermVector
specifier|private
name|void
name|checkIfStandardTermVector
parameter_list|(
name|TermVectorResponse
name|inResponse
parameter_list|)
throws|throws
name|IOException
block|{
name|Fields
name|fields
init|=
name|inResponse
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|fields
operator|.
name|terms
argument_list|(
literal|"title"
argument_list|)
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fields
operator|.
name|terms
argument_list|(
literal|"desc"
argument_list|)
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
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
block|}
annotation|@
name|Test
DECL|method|testRestRequestParsing
specifier|public
name|void
name|testRestRequestParsing
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesReference
name|inputBytes
init|=
operator|new
name|BytesArray
argument_list|(
literal|" {\"fields\" : [\"a\",  \"b\",\"c\"], \"offsets\":false, \"positions\":false, \"payloads\":true}"
argument_list|)
decl_stmt|;
name|TermVectorRequest
name|tvr
init|=
operator|new
name|TermVectorRequest
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|XContentParser
name|parser
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
name|inputBytes
argument_list|)
decl_stmt|;
name|TermVectorRequest
operator|.
name|parseRequest
argument_list|(
name|tvr
argument_list|,
name|parser
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|fields
init|=
name|tvr
operator|.
name|selectedFields
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|fields
operator|.
name|contains
argument_list|(
literal|"a"
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
name|fields
operator|.
name|contains
argument_list|(
literal|"b"
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
name|fields
operator|.
name|contains
argument_list|(
literal|"c"
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
name|tvr
operator|.
name|offsets
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
name|tvr
operator|.
name|positions
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
name|tvr
operator|.
name|payloads
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|additionalFields
init|=
literal|"b,c  ,d, e  "
decl_stmt|;
name|RestTermVectorAction
operator|.
name|addFieldStringsFromParameter
argument_list|(
name|tvr
argument_list|,
name|additionalFields
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tvr
operator|.
name|selectedFields
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fields
operator|.
name|contains
argument_list|(
literal|"d"
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
name|fields
operator|.
name|contains
argument_list|(
literal|"e"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|additionalFields
operator|=
literal|""
expr_stmt|;
name|RestTermVectorAction
operator|.
name|addFieldStringsFromParameter
argument_list|(
name|tvr
argument_list|,
name|additionalFields
argument_list|)
expr_stmt|;
name|inputBytes
operator|=
operator|new
name|BytesArray
argument_list|(
literal|" {\"offsets\":false, \"positions\":false, \"payloads\":true}"
argument_list|)
expr_stmt|;
name|tvr
operator|=
operator|new
name|TermVectorRequest
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|parser
operator|=
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
name|inputBytes
argument_list|)
expr_stmt|;
name|TermVectorRequest
operator|.
name|parseRequest
argument_list|(
name|tvr
argument_list|,
name|parser
argument_list|)
expr_stmt|;
name|additionalFields
operator|=
literal|""
expr_stmt|;
name|RestTermVectorAction
operator|.
name|addFieldStringsFromParameter
argument_list|(
name|tvr
argument_list|,
name|additionalFields
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tvr
operator|.
name|selectedFields
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|additionalFields
operator|=
literal|"b,c  ,d, e  "
expr_stmt|;
name|RestTermVectorAction
operator|.
name|addFieldStringsFromParameter
argument_list|(
name|tvr
argument_list|,
name|additionalFields
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tvr
operator|.
name|selectedFields
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testRequestParsingThrowsException
specifier|public
name|void
name|testRequestParsingThrowsException
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesReference
name|inputBytes
init|=
operator|new
name|BytesArray
argument_list|(
literal|" {\"fields\" : \"a,  b,c   \", \"offsets\":false, \"positions\":false, \"payloads\":true, \"meaningless_term\":2}"
argument_list|)
decl_stmt|;
name|TermVectorRequest
name|tvr
init|=
operator|new
name|TermVectorRequest
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|boolean
name|threwException
init|=
literal|false
decl_stmt|;
try|try
block|{
name|XContentParser
name|parser
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
name|inputBytes
argument_list|)
decl_stmt|;
name|TermVectorRequest
operator|.
name|parseRequest
argument_list|(
name|tvr
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|threwException
operator|=
literal|true
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|threwException
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
DECL|method|streamRequest
specifier|public
name|void
name|streamRequest
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|TermVectorRequest
name|request
init|=
operator|new
name|TermVectorRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
decl_stmt|;
name|request
operator|.
name|offsets
argument_list|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|request
operator|.
name|fieldStatistics
argument_list|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|request
operator|.
name|payloads
argument_list|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|request
operator|.
name|positions
argument_list|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|request
operator|.
name|termStatistics
argument_list|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|parent
init|=
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
literal|"someParent"
else|:
literal|null
decl_stmt|;
name|request
operator|.
name|parent
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|String
name|pref
init|=
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
literal|"somePreference"
else|:
literal|null
decl_stmt|;
name|request
operator|.
name|preference
argument_list|(
name|pref
argument_list|)
expr_stmt|;
comment|// write
name|ByteArrayOutputStream
name|outBuffer
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|OutputStreamStreamOutput
name|out
init|=
operator|new
name|OutputStreamStreamOutput
argument_list|(
name|outBuffer
argument_list|)
decl_stmt|;
name|request
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
comment|// read
name|ByteArrayInputStream
name|esInBuffer
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|outBuffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|InputStreamStreamInput
name|esBuffer
init|=
operator|new
name|InputStreamStreamInput
argument_list|(
name|esInBuffer
argument_list|)
decl_stmt|;
name|TermVectorRequest
name|req2
init|=
operator|new
name|TermVectorRequest
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|req2
operator|.
name|readFrom
argument_list|(
name|esBuffer
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|offsets
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|req2
operator|.
name|offsets
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|fieldStatistics
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|req2
operator|.
name|fieldStatistics
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|payloads
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|req2
operator|.
name|payloads
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|positions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|req2
operator|.
name|positions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|termStatistics
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|req2
operator|.
name|termStatistics
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|preference
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|pref
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|parent
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testFieldTypeToTermVectorString
specifier|public
name|void
name|testFieldTypeToTermVectorString
parameter_list|()
throws|throws
name|Exception
block|{
name|FieldType
name|ft
init|=
operator|new
name|FieldType
argument_list|()
decl_stmt|;
name|ft
operator|.
name|setStoreTermVectorOffsets
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ft
operator|.
name|setStoreTermVectorPayloads
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ft
operator|.
name|setStoreTermVectors
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ft
operator|.
name|setStoreTermVectorPositions
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|String
name|ftOpts
init|=
name|AbstractFieldMapper
operator|.
name|termVectorOptionsToString
argument_list|(
name|ft
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"with_positions_payloads"
argument_list|,
name|equalTo
argument_list|(
name|ftOpts
argument_list|)
argument_list|)
expr_stmt|;
name|AllFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|AllFieldMapper
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|boolean
name|exceptiontrown
init|=
literal|false
decl_stmt|;
try|try
block|{
name|TypeParsers
operator|.
name|parseTermVector
argument_list|(
literal|""
argument_list|,
name|ftOpts
argument_list|,
name|builder
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MapperParsingException
name|e
parameter_list|)
block|{
name|exceptiontrown
operator|=
literal|true
expr_stmt|;
block|}
name|assertThat
argument_list|(
literal|"TypeParsers.parseTermVector should accept string with_positions_payloads but does not."
argument_list|,
name|exceptiontrown
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
DECL|method|testTermVectorStringGenerationWithoutPositions
specifier|public
name|void
name|testTermVectorStringGenerationWithoutPositions
parameter_list|()
throws|throws
name|Exception
block|{
name|FieldType
name|ft
init|=
operator|new
name|FieldType
argument_list|()
decl_stmt|;
name|ft
operator|.
name|setStoreTermVectorOffsets
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ft
operator|.
name|setStoreTermVectorPayloads
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ft
operator|.
name|setStoreTermVectors
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ft
operator|.
name|setStoreTermVectorPositions
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|String
name|ftOpts
init|=
name|AbstractFieldMapper
operator|.
name|termVectorOptionsToString
argument_list|(
name|ft
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|ftOpts
argument_list|,
name|equalTo
argument_list|(
literal|"with_offsets"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMultiParser
specifier|public
name|void
name|testMultiParser
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|data
init|=
name|Streams
operator|.
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/action/termvector/multiRequest1.json"
argument_list|)
decl_stmt|;
name|BytesReference
name|bytes
init|=
operator|new
name|BytesArray
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|MultiTermVectorsRequest
name|request
init|=
operator|new
name|MultiTermVectorsRequest
argument_list|()
decl_stmt|;
name|request
operator|.
name|add
argument_list|(
operator|new
name|TermVectorRequest
argument_list|()
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|checkParsedParameters
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|data
operator|=
name|Streams
operator|.
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/action/termvector/multiRequest2.json"
argument_list|)
expr_stmt|;
name|bytes
operator|=
operator|new
name|BytesArray
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|request
operator|=
operator|new
name|MultiTermVectorsRequest
argument_list|()
expr_stmt|;
name|request
operator|.
name|add
argument_list|(
operator|new
name|TermVectorRequest
argument_list|()
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|checkParsedParameters
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
DECL|method|checkParsedParameters
name|void
name|checkParsedParameters
parameter_list|(
name|MultiTermVectorsRequest
name|request
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|ids
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|ids
operator|.
name|add
argument_list|(
literal|"1"
argument_list|)
expr_stmt|;
name|ids
operator|.
name|add
argument_list|(
literal|"2"
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|fields
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
literal|"b"
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
literal|"c"
argument_list|)
expr_stmt|;
for|for
control|(
name|TermVectorRequest
name|singleRequest
range|:
name|request
operator|.
name|requests
control|)
block|{
name|assertThat
argument_list|(
name|singleRequest
operator|.
name|index
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"testidx"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|singleRequest
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|singleRequest
operator|.
name|payloads
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
name|singleRequest
operator|.
name|positions
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
name|singleRequest
operator|.
name|offsets
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
name|singleRequest
operator|.
name|termStatistics
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
name|singleRequest
operator|.
name|fieldStatistics
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
name|singleRequest
operator|.
name|id
argument_list|()
argument_list|,
name|Matchers
operator|.
name|anyOf
argument_list|(
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|singleRequest
operator|.
name|selectedFields
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|fields
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

