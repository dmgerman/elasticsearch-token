begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.all
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|all
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
name|StoredField
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
name|IndexReader
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
name|search
operator|.
name|Explanation
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
name|MatchAllDocsQuery
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
name|Query
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
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|RAMDirectory
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
name|test
operator|.
name|ESTestCase
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
DECL|class|SimpleAllTests
specifier|public
class|class
name|SimpleAllTests
extends|extends
name|ESTestCase
block|{
DECL|method|getAllFieldType
specifier|private
name|FieldType
name|getAllFieldType
parameter_list|()
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
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS_AND_FREQS_AND_POSITIONS
argument_list|)
expr_stmt|;
name|ft
operator|.
name|setTokenized
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ft
operator|.
name|freeze
argument_list|()
expr_stmt|;
return|return
name|ft
return|;
block|}
DECL|method|assertExplanationScore
specifier|private
name|void
name|assertExplanationScore
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|Query
name|query
parameter_list|,
name|ScoreDoc
name|scoreDoc
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Explanation
name|expl
init|=
name|searcher
operator|.
name|explain
argument_list|(
name|query
argument_list|,
name|scoreDoc
operator|.
name|doc
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|scoreDoc
operator|.
name|score
argument_list|,
name|expl
operator|.
name|getValue
argument_list|()
argument_list|,
literal|0.00001f
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleAllNoBoost
specifier|public
name|void
name|testSimpleAllNoBoost
parameter_list|()
throws|throws
name|Exception
block|{
name|FieldType
name|allFt
init|=
name|getAllFieldType
argument_list|()
decl_stmt|;
name|Directory
name|dir
init|=
operator|new
name|RAMDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|indexWriter
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
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"something"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"else"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
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
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"else"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"something"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|IndexReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|indexWriter
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"else"
argument_list|)
argument_list|)
decl_stmt|;
name|TopDocs
name|docs
init|=
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanationScore
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanationScore
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|query
operator|=
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"something"
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|=
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanationScore
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanationScore
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|testSimpleAllWithBoost
specifier|public
name|void
name|testSimpleAllWithBoost
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
operator|new
name|RAMDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|indexWriter
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
name|FieldType
name|allFt
init|=
name|getAllFieldType
argument_list|()
decl_stmt|;
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
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"something"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"else"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
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
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"else"
argument_list|,
literal|2.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"something"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|IndexReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|indexWriter
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
comment|// this one is boosted. so the second doc is more relevant
name|Query
name|query
init|=
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"else"
argument_list|)
argument_list|)
decl_stmt|;
name|TopDocs
name|docs
init|=
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanationScore
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanationScore
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|query
operator|=
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"something"
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|=
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanationScore
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanationScore
argument_list|(
name|searcher
argument_list|,
name|query
argument_list|,
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|testTermMissingFromOneSegment
specifier|public
name|void
name|testTermMissingFromOneSegment
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
operator|new
name|RAMDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|indexWriter
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
name|FieldType
name|allFt
init|=
name|getAllFieldType
argument_list|()
decl_stmt|;
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
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"something"
argument_list|,
literal|2.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
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
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"else"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|IndexReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|indexWriter
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|reader
operator|.
name|leaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
comment|// "something" only appears in the first segment:
name|Query
name|query
init|=
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"something"
argument_list|)
argument_list|)
decl_stmt|;
name|TopDocs
name|docs
init|=
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|docs
operator|.
name|totalHits
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|testMultipleTokensAllNoBoost
specifier|public
name|void
name|testMultipleTokensAllNoBoost
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
operator|new
name|RAMDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|indexWriter
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
name|FieldType
name|allFt
init|=
name|getAllFieldType
argument_list|()
decl_stmt|;
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
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"something moo"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"else koo"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
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
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"else koo"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"something moo"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|IndexReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|indexWriter
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
name|TopDocs
name|docs
init|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"else"
argument_list|)
argument_list|)
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"koo"
argument_list|)
argument_list|)
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"something"
argument_list|)
argument_list|)
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"moo"
argument_list|)
argument_list|)
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|testMultipleTokensAllWithBoost
specifier|public
name|void
name|testMultipleTokensAllWithBoost
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
operator|new
name|RAMDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|indexWriter
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
name|FieldType
name|allFt
init|=
name|getAllFieldType
argument_list|()
decl_stmt|;
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
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"something moo"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"else koo"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
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
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"else koo"
argument_list|,
literal|2.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|"something moo"
argument_list|,
literal|1.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|IndexReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|indexWriter
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
name|TopDocs
name|docs
init|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"else"
argument_list|)
argument_list|)
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"koo"
argument_list|)
argument_list|)
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"something"
argument_list|)
argument_list|)
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"_all"
argument_list|,
literal|"moo"
argument_list|)
argument_list|)
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|testNoTokens
specifier|public
name|void
name|testNoTokens
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
operator|new
name|RAMDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|indexWriter
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
name|KEYWORD_ANALYZER
argument_list|)
argument_list|)
decl_stmt|;
name|FieldType
name|allFt
init|=
name|getAllFieldType
argument_list|()
decl_stmt|;
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
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|StoredField
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|AllField
argument_list|(
literal|"_all"
argument_list|,
literal|""
argument_list|,
literal|2.0f
argument_list|,
name|allFt
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|IndexReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|indexWriter
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
name|TopDocs
name|docs
init|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

