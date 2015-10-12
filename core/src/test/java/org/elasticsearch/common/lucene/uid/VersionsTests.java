begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.uid
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|uid
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
name|Analyzer
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
name|analysis
operator|.
name|TokenStream
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
name|analysis
operator|.
name|core
operator|.
name|KeywordAnalyzer
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|CharTermAttribute
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|PayloadAttribute
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
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Field
operator|.
name|Store
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
name|FieldType
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
name|NumericDocValuesField
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
name|StringField
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
name|DirectoryReader
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
name|IndexWriter
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
name|LeafReader
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
name|NumericDocValues
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
name|SlowCompositeReaderWrapper
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
name|Term
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
name|Numbers
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
name|lucene
operator|.
name|Lucene
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
name|internal
operator|.
name|VersionFieldMapper
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
name|shard
operator|.
name|ElasticsearchMergePolicy
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
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|notNullValue
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
name|nullValue
import|;
end_import

begin_class
DECL|class|VersionsTests
specifier|public
class|class
name|VersionsTests
extends|extends
name|ESTestCase
block|{
DECL|method|reopen
specifier|public
specifier|static
name|DirectoryReader
name|reopen
parameter_list|(
name|DirectoryReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|reopen
argument_list|(
name|reader
argument_list|,
literal|true
argument_list|)
return|;
block|}
DECL|method|reopen
specifier|public
specifier|static
name|DirectoryReader
name|reopen
parameter_list|(
name|DirectoryReader
name|reader
parameter_list|,
name|boolean
name|newReaderExpected
parameter_list|)
throws|throws
name|IOException
block|{
name|DirectoryReader
name|newReader
init|=
name|DirectoryReader
operator|.
name|openIfChanged
argument_list|(
name|reader
argument_list|)
decl_stmt|;
if|if
condition|(
name|newReader
operator|!=
literal|null
condition|)
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|assertFalse
argument_list|(
name|newReaderExpected
argument_list|)
expr_stmt|;
block|}
return|return
name|newReader
return|;
block|}
annotation|@
name|Test
DECL|method|testVersions
specifier|public
name|void
name|testVersions
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
argument_list|)
decl_stmt|;
name|DirectoryReader
name|directoryReader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Versions
operator|.
name|NOT_FOUND
argument_list|)
argument_list|)
expr_stmt|;
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|directoryReader
operator|=
name|reopen
argument_list|(
name|directoryReader
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Versions
operator|.
name|NOT_SET
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadDocIdAndVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
operator|.
name|version
argument_list|,
name|equalTo
argument_list|(
name|Versions
operator|.
name|NOT_SET
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|,
literal|1
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
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|doc
argument_list|)
expr_stmt|;
name|directoryReader
operator|=
name|reopen
argument_list|(
name|directoryReader
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadDocIdAndVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
operator|.
name|version
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|Field
name|uid
init|=
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
decl_stmt|;
name|Field
name|version
init|=
operator|new
name|NumericDocValuesField
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|uid
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|writer
operator|.
name|updateDocument
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|doc
argument_list|)
expr_stmt|;
name|directoryReader
operator|=
name|reopen
argument_list|(
name|directoryReader
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadDocIdAndVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
operator|.
name|version
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
comment|// test reuse of uid field
name|doc
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|version
operator|.
name|setLongValue
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|uid
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|writer
operator|.
name|updateDocument
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|doc
argument_list|)
expr_stmt|;
name|directoryReader
operator|=
name|reopen
argument_list|(
name|directoryReader
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadDocIdAndVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
operator|.
name|version
argument_list|,
name|equalTo
argument_list|(
literal|3l
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|deleteDocuments
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|directoryReader
operator|=
name|reopen
argument_list|(
name|directoryReader
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Versions
operator|.
name|NOT_FOUND
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadDocIdAndVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|directoryReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|writer
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
annotation|@
name|Test
DECL|method|testNestedDocuments
specifier|public
name|void
name|testNestedDocuments
parameter_list|()
throws|throws
name|IOException
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Document
argument_list|>
name|docs
init|=
operator|new
name|ArrayList
argument_list|<>
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
literal|4
condition|;
operator|++
name|i
control|)
block|{
comment|// Nested
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|NESTED_FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|add
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
comment|// Root
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|NumericDocValuesField
name|version
init|=
operator|new
name|NumericDocValuesField
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|,
literal|5L
argument_list|)
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|docs
operator|.
name|add
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|writer
operator|.
name|updateDocuments
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|docs
argument_list|)
expr_stmt|;
name|DirectoryReader
name|directoryReader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|5l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadDocIdAndVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
operator|.
name|version
argument_list|,
name|equalTo
argument_list|(
literal|5l
argument_list|)
argument_list|)
expr_stmt|;
name|version
operator|.
name|setLongValue
argument_list|(
literal|6L
argument_list|)
expr_stmt|;
name|writer
operator|.
name|updateDocuments
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|docs
argument_list|)
expr_stmt|;
name|version
operator|.
name|setLongValue
argument_list|(
literal|7L
argument_list|)
expr_stmt|;
name|writer
operator|.
name|updateDocuments
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|docs
argument_list|)
expr_stmt|;
name|directoryReader
operator|=
name|reopen
argument_list|(
name|directoryReader
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|7l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadDocIdAndVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
operator|.
name|version
argument_list|,
name|equalTo
argument_list|(
literal|7l
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|deleteDocuments
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|directoryReader
operator|=
name|reopen
argument_list|(
name|directoryReader
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Versions
operator|.
name|NOT_FOUND
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadDocIdAndVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|directoryReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|writer
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
annotation|@
name|Test
DECL|method|testBackwardCompatibility
specifier|public
name|void
name|testBackwardCompatibility
parameter_list|()
throws|throws
name|IOException
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
argument_list|)
decl_stmt|;
name|DirectoryReader
name|directoryReader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Versions
operator|.
name|NOT_FOUND
argument_list|)
argument_list|)
expr_stmt|;
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|UidField
name|uidAndVersion
init|=
operator|new
name|UidField
argument_list|(
literal|"1"
argument_list|,
literal|1L
argument_list|)
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|uidAndVersion
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|uidAndVersion
operator|.
name|uid
operator|=
literal|"2"
expr_stmt|;
name|uidAndVersion
operator|.
name|version
operator|=
literal|2
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|directoryReader
operator|=
name|reopen
argument_list|(
name|directoryReader
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"2"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Versions
operator|.
name|loadVersion
argument_list|(
name|directoryReader
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"3"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Versions
operator|.
name|NOT_FOUND
argument_list|)
argument_list|)
expr_stmt|;
name|directoryReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|writer
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
comment|// This is how versions used to be encoded
DECL|class|UidField
specifier|private
specifier|static
class|class
name|UidField
extends|extends
name|Field
block|{
DECL|field|FIELD_TYPE
specifier|private
specifier|static
specifier|final
name|FieldType
name|FIELD_TYPE
init|=
operator|new
name|FieldType
argument_list|()
decl_stmt|;
static|static
block|{
name|FIELD_TYPE
operator|.
name|setTokenized
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS_AND_FREQS_AND_POSITIONS
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setStored
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
DECL|field|uid
name|String
name|uid
decl_stmt|;
DECL|field|version
name|long
name|version
decl_stmt|;
DECL|method|UidField
name|UidField
parameter_list|(
name|String
name|uid
parameter_list|,
name|long
name|version
parameter_list|)
block|{
name|super
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|uid
argument_list|,
name|FIELD_TYPE
argument_list|)
expr_stmt|;
name|this
operator|.
name|uid
operator|=
name|uid
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|tokenStream
specifier|public
name|TokenStream
name|tokenStream
parameter_list|(
name|Analyzer
name|analyzer
parameter_list|,
name|TokenStream
name|reuse
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TokenStream
argument_list|()
block|{
name|boolean
name|finished
init|=
literal|true
decl_stmt|;
specifier|final
name|CharTermAttribute
name|term
init|=
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|PayloadAttribute
name|payload
init|=
name|addAttribute
argument_list|(
name|PayloadAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|incrementToken
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|finished
condition|)
block|{
return|return
literal|false
return|;
block|}
name|term
operator|.
name|setEmpty
argument_list|()
operator|.
name|append
argument_list|(
name|uid
argument_list|)
expr_stmt|;
name|payload
operator|.
name|setPayload
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|Numbers
operator|.
name|longToBytes
argument_list|(
name|version
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|finished
operator|=
literal|true
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|finished
operator|=
literal|false
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
annotation|@
name|Test
DECL|method|testMergingOldIndices
specifier|public
name|void
name|testMergingOldIndices
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|IndexWriterConfig
name|iwConf
init|=
operator|new
name|IndexWriterConfig
argument_list|(
operator|new
name|KeywordAnalyzer
argument_list|()
argument_list|)
decl_stmt|;
name|iwConf
operator|.
name|setMergePolicy
argument_list|(
operator|new
name|ElasticsearchMergePolicy
argument_list|(
name|iwConf
operator|.
name|getMergePolicy
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
specifier|final
name|IndexWriter
name|iw
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|iwConf
argument_list|)
decl_stmt|;
comment|// 1st segment, no _version
name|Document
name|document
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
comment|// Add a dummy field (enough to trigger #3237)
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|StringField
name|uid
init|=
operator|new
name|StringField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"1"
argument_list|,
name|Store
operator|.
name|YES
argument_list|)
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
name|uid
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|uid
operator|.
name|setStringValue
argument_list|(
literal|"2"
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|iw
operator|.
name|commit
argument_list|()
expr_stmt|;
comment|// 2nd segment, old layout
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|UidField
name|uidAndVersion
init|=
operator|new
name|UidField
argument_list|(
literal|"3"
argument_list|,
literal|3L
argument_list|)
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
name|uidAndVersion
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|uidAndVersion
operator|.
name|uid
operator|=
literal|"4"
expr_stmt|;
name|uidAndVersion
operator|.
name|version
operator|=
literal|4L
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|iw
operator|.
name|commit
argument_list|()
expr_stmt|;
comment|// 3rd segment new layout
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|uid
operator|.
name|setStringValue
argument_list|(
literal|"5"
argument_list|)
expr_stmt|;
name|Field
name|version
init|=
operator|new
name|NumericDocValuesField
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|,
literal|5L
argument_list|)
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
name|uid
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|uid
operator|.
name|setStringValue
argument_list|(
literal|"6"
argument_list|)
expr_stmt|;
name|version
operator|.
name|setLongValue
argument_list|(
literal|6L
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|iw
operator|.
name|commit
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|expectedVersions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|expectedVersions
operator|.
name|put
argument_list|(
literal|"1"
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|expectedVersions
operator|.
name|put
argument_list|(
literal|"2"
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|expectedVersions
operator|.
name|put
argument_list|(
literal|"3"
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|expectedVersions
operator|.
name|put
argument_list|(
literal|"4"
argument_list|,
literal|4L
argument_list|)
expr_stmt|;
name|expectedVersions
operator|.
name|put
argument_list|(
literal|"5"
argument_list|,
literal|5L
argument_list|)
expr_stmt|;
name|expectedVersions
operator|.
name|put
argument_list|(
literal|"6"
argument_list|,
literal|6L
argument_list|)
expr_stmt|;
comment|// Force merge and check versions
name|iw
operator|.
name|forceMerge
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|LeafReader
name|ir
init|=
name|SlowCompositeReaderWrapper
operator|.
name|wrap
argument_list|(
name|DirectoryReader
operator|.
name|open
argument_list|(
name|iw
operator|.
name|getDirectory
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|NumericDocValues
name|versions
init|=
name|ir
operator|.
name|getNumericDocValues
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|versions
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ir
operator|.
name|maxDoc
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|String
name|uidValue
init|=
name|ir
operator|.
name|document
argument_list|(
name|i
argument_list|)
operator|.
name|get
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
specifier|final
name|long
name|expectedVersion
init|=
name|expectedVersions
operator|.
name|get
argument_list|(
name|uidValue
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|versions
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|expectedVersion
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|iw
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|IndexWriter
operator|.
name|isLocked
argument_list|(
name|iw
operator|.
name|getDirectory
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|ir
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
block|}
end_class

end_unit

