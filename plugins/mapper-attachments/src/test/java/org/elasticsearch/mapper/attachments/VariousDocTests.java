begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.mapper.attachments
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tika
operator|.
name|io
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tika
operator|.
name|metadata
operator|.
name|Metadata
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
name|index
operator|.
name|MapperTestUtils
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
name|ParseContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|io
operator|.
name|InputStream
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
name|mapper
operator|.
name|attachments
operator|.
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|AUTHOR
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|CONTENT_LENGTH
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|CONTENT_TYPE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|DATE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|KEYWORDS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|LANGUAGE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|NAME
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|AttachmentMapper
operator|.
name|FieldNames
operator|.
name|TITLE
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
name|isEmptyOrNullString
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
name|not
import|;
end_import

begin_comment
comment|/**  * Test for different documents  */
end_comment

begin_class
DECL|class|VariousDocTests
specifier|public
class|class
name|VariousDocTests
extends|extends
name|AttachmentUnitTestCase
block|{
DECL|field|docMapper
specifier|protected
name|DocumentMapper
name|docMapper
decl_stmt|;
annotation|@
name|Before
DECL|method|createMapper
specifier|public
name|void
name|createMapper
parameter_list|()
throws|throws
name|IOException
block|{
name|DocumentMapperParser
name|mapperParser
init|=
name|MapperTestUtils
operator|.
name|newMapperService
argument_list|(
name|createTempDir
argument_list|()
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
name|getIndicesModuleWithRegisteredAttachmentMapper
argument_list|()
argument_list|)
operator|.
name|documentMapperParser
argument_list|()
decl_stmt|;
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/unit/various-doc/test-mapping.json"
argument_list|)
decl_stmt|;
name|docMapper
operator|=
name|mapperParser
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
expr_stmt|;
block|}
comment|/**      * Test for https://github.com/elastic/elasticsearch-mapper-attachments/issues/104      */
DECL|method|testWordDocxDocument104
specifier|public
name|void
name|testWordDocxDocument104
parameter_list|()
throws|throws
name|Exception
block|{
name|assertParseable
argument_list|(
literal|"issue-104.docx"
argument_list|)
expr_stmt|;
name|testMapper
argument_list|(
literal|"issue-104.docx"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test for encrypted PDF      */
DECL|method|testEncryptedPDFDocument
specifier|public
name|void
name|testEncryptedPDFDocument
parameter_list|()
throws|throws
name|Exception
block|{
name|assertException
argument_list|(
literal|"encrypted.pdf"
argument_list|,
literal|"is encrypted"
argument_list|)
expr_stmt|;
name|testMapper
argument_list|(
literal|"encrypted.pdf"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test for HTML      */
DECL|method|testHtmlDocument
specifier|public
name|void
name|testHtmlDocument
parameter_list|()
throws|throws
name|Exception
block|{
name|assertParseable
argument_list|(
literal|"htmlWithEmptyDateMeta.html"
argument_list|)
expr_stmt|;
name|testMapper
argument_list|(
literal|"htmlWithEmptyDateMeta.html"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test for XHTML      */
DECL|method|testXHtmlDocument
specifier|public
name|void
name|testXHtmlDocument
parameter_list|()
throws|throws
name|Exception
block|{
name|assertParseable
argument_list|(
literal|"testXHTML.html"
argument_list|)
expr_stmt|;
name|testMapper
argument_list|(
literal|"testXHTML.html"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test for TXT      */
DECL|method|testTxtDocument
specifier|public
name|void
name|testTxtDocument
parameter_list|()
throws|throws
name|Exception
block|{
name|assertParseable
argument_list|(
literal|"text-in-english.txt"
argument_list|)
expr_stmt|;
name|testMapper
argument_list|(
literal|"text-in-english.txt"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test for .epub      */
DECL|method|testEpubDocument
specifier|public
name|void
name|testEpubDocument
parameter_list|()
throws|throws
name|Exception
block|{
name|assertParseable
argument_list|(
literal|"testEPUB.epub"
argument_list|)
expr_stmt|;
name|testMapper
argument_list|(
literal|"testEPUB.epub"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test for ASCIIDOC      * Not yet supported by Tika: https://github.com/elastic/elasticsearch-mapper-attachments/issues/29      */
DECL|method|testAsciidocDocument
specifier|public
name|void
name|testAsciidocDocument
parameter_list|()
throws|throws
name|Exception
block|{
name|assertParseable
argument_list|(
literal|"asciidoc.asciidoc"
argument_list|)
expr_stmt|;
name|testMapper
argument_list|(
literal|"asciidoc.asciidoc"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|assertException
name|void
name|assertException
parameter_list|(
name|String
name|filename
parameter_list|,
name|String
name|expectedMessage
parameter_list|)
throws|throws
name|Exception
block|{
try|try
init|(
name|InputStream
name|is
init|=
name|VariousDocTests
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/sample-files/"
operator|+
name|filename
argument_list|)
init|)
block|{
name|byte
name|bytes
index|[]
init|=
name|IOUtils
operator|.
name|toByteArray
argument_list|(
name|is
argument_list|)
decl_stmt|;
name|TikaImpl
operator|.
name|parse
argument_list|(
name|bytes
argument_list|,
operator|new
name|Metadata
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getMessage
argument_list|()
operator|!=
literal|null
operator|&&
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
name|expectedMessage
argument_list|)
condition|)
block|{
comment|// ok
block|}
else|else
block|{
comment|// unexpected
throw|throw
name|e
throw|;
block|}
block|}
block|}
DECL|method|assertParseable
specifier|protected
name|void
name|assertParseable
parameter_list|(
name|String
name|filename
parameter_list|)
throws|throws
name|Exception
block|{
try|try
init|(
name|InputStream
name|is
init|=
name|VariousDocTests
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/sample-files/"
operator|+
name|filename
argument_list|)
init|)
block|{
name|byte
name|bytes
index|[]
init|=
name|IOUtils
operator|.
name|toByteArray
argument_list|(
name|is
argument_list|)
decl_stmt|;
name|String
name|parsedContent
init|=
name|TikaImpl
operator|.
name|parse
argument_list|(
name|bytes
argument_list|,
operator|new
name|Metadata
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedContent
argument_list|,
name|not
argument_list|(
name|isEmptyOrNullString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"extracted content: {}"
argument_list|,
name|parsedContent
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testMapper
specifier|protected
name|void
name|testMapper
parameter_list|(
name|String
name|filename
parameter_list|,
name|boolean
name|errorExpected
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|html
init|=
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/sample-files/"
operator|+
name|filename
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
name|startObject
argument_list|(
literal|"file"
argument_list|)
operator|.
name|field
argument_list|(
literal|"_name"
argument_list|,
name|filename
argument_list|)
operator|.
name|field
argument_list|(
literal|"_content"
argument_list|,
name|html
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
name|ParseContext
operator|.
name|Document
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
literal|"person"
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
if|if
condition|(
operator|!
name|errorExpected
condition|)
block|{
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
name|getMapper
argument_list|(
literal|"file.content"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|,
name|not
argument_list|(
name|isEmptyOrNullString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"-> extracted content: {}"
argument_list|,
name|doc
operator|.
name|get
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"file"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"-> extracted metadata:"
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|AUTHOR
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|CONTENT_LENGTH
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|DATE
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|KEYWORDS
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|LANGUAGE
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|NAME
argument_list|)
expr_stmt|;
name|printMetadataContent
argument_list|(
name|doc
argument_list|,
name|TITLE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|printMetadataContent
specifier|private
name|void
name|printMetadataContent
parameter_list|(
name|ParseContext
operator|.
name|Document
name|doc
parameter_list|,
name|String
name|field
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"- [{}]: [{}]"
argument_list|,
name|field
argument_list|,
name|doc
operator|.
name|get
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"file."
operator|+
name|field
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
