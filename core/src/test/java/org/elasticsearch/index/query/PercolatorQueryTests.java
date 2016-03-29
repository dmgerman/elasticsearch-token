begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|core
operator|.
name|WhitespaceAnalyzer
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
name|NoMergePolicy
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
name|index
operator|.
name|memory
operator|.
name|MemoryIndex
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
name|queries
operator|.
name|BlendedTermQuery
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
name|queries
operator|.
name|CommonTermsQuery
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
name|BooleanClause
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
name|BooleanQuery
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
name|PhraseQuery
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
name|PrefixQuery
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
name|search
operator|.
name|WildcardQuery
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
name|percolator
operator|.
name|ExtractQueryTermsService
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
name|percolator
operator|.
name|PercolatorFieldMapper
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
name|junit
operator|.
name|After
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

begin_class
DECL|class|PercolatorQueryTests
specifier|public
class|class
name|PercolatorQueryTests
extends|extends
name|ESTestCase
block|{
DECL|field|EXTRACTED_TERMS_FIELD_NAME
specifier|public
specifier|final
specifier|static
name|String
name|EXTRACTED_TERMS_FIELD_NAME
init|=
literal|"extracted_terms"
decl_stmt|;
DECL|field|UNKNOWN_QUERY_FIELD_NAME
specifier|public
specifier|final
specifier|static
name|String
name|UNKNOWN_QUERY_FIELD_NAME
init|=
literal|"unknown_query"
decl_stmt|;
DECL|field|EXTRACTED_TERMS_FIELD_TYPE
specifier|public
specifier|static
name|FieldType
name|EXTRACTED_TERMS_FIELD_TYPE
init|=
operator|new
name|FieldType
argument_list|()
decl_stmt|;
static|static
block|{
name|EXTRACTED_TERMS_FIELD_TYPE
operator|.
name|setTokenized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|EXTRACTED_TERMS_FIELD_TYPE
operator|.
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS
argument_list|)
expr_stmt|;
name|EXTRACTED_TERMS_FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
DECL|field|directory
specifier|private
name|Directory
name|directory
decl_stmt|;
DECL|field|indexWriter
specifier|private
name|IndexWriter
name|indexWriter
decl_stmt|;
DECL|field|queries
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Query
argument_list|>
name|queries
decl_stmt|;
DECL|field|queryRegistry
specifier|private
name|PercolatorQuery
operator|.
name|QueryRegistry
name|queryRegistry
decl_stmt|;
DECL|field|directoryReader
specifier|private
name|DirectoryReader
name|directoryReader
decl_stmt|;
annotation|@
name|Before
DECL|method|init
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|Exception
block|{
name|directory
operator|=
name|newDirectory
argument_list|()
expr_stmt|;
name|queries
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|queryRegistry
operator|=
name|ctx
lambda|->
name|docId
lambda|->
block|{
block|try
block|{
name|String
name|val
init|=
name|ctx
operator|.
name|reader
argument_list|()
operator|.
name|document
argument_list|(
name|docId
argument_list|)
operator|.
name|get
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
return|return
name|queries
operator|.
name|get
argument_list|(
name|Uid
operator|.
name|createUid
argument_list|(
name|val
argument_list|)
operator|.
name|id
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
empty_stmt|;
name|IndexWriterConfig
name|config
init|=
operator|new
name|IndexWriterConfig
argument_list|(
operator|new
name|WhitespaceAnalyzer
argument_list|()
argument_list|)
decl_stmt|;
name|config
operator|.
name|setMergePolicy
parameter_list|(
name|NoMergePolicy
operator|.
name|INSTANCE
parameter_list|)
constructor_decl|;
name|indexWriter
operator|=
operator|new
name|IndexWriter
argument_list|(
name|directory
argument_list|,
name|config
argument_list|)
expr_stmt|;
block|}
end_class

begin_function
annotation|@
name|After
DECL|method|destroy
specifier|public
name|void
name|destroy
parameter_list|()
throws|throws
name|Exception
block|{
name|directoryReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|directory
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
DECL|method|testVariousQueries
specifier|public
name|void
name|testVariousQueries
parameter_list|()
throws|throws
name|Exception
block|{
name|addPercolatorQuery
argument_list|(
literal|"1"
argument_list|,
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"brown"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|addPercolatorQuery
argument_list|(
literal|"2"
argument_list|,
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"monkey"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|addPercolatorQuery
argument_list|(
literal|"3"
argument_list|,
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"fox"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|BooleanQuery
operator|.
name|Builder
name|bq1
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|bq1
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"fox"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|bq1
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"monkey"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|addPercolatorQuery
argument_list|(
literal|"4"
argument_list|,
name|bq1
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|BooleanQuery
operator|.
name|Builder
name|bq2
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|bq2
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"fox"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
name|bq2
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"monkey"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
name|addPercolatorQuery
argument_list|(
literal|"5"
argument_list|,
name|bq2
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|BooleanQuery
operator|.
name|Builder
name|bq3
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|bq3
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"fox"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
name|bq3
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"apes"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|)
expr_stmt|;
name|addPercolatorQuery
argument_list|(
literal|"6"
argument_list|,
name|bq3
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|BooleanQuery
operator|.
name|Builder
name|bq4
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|bq4
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"fox"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|)
expr_stmt|;
name|bq4
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"apes"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
name|addPercolatorQuery
argument_list|(
literal|"7"
argument_list|,
name|bq4
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|PhraseQuery
operator|.
name|Builder
name|pq1
init|=
operator|new
name|PhraseQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|pq1
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"lazy"
argument_list|)
argument_list|)
expr_stmt|;
name|pq1
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"dog"
argument_list|)
argument_list|)
expr_stmt|;
name|addPercolatorQuery
argument_list|(
literal|"8"
argument_list|,
name|pq1
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|directoryReader
operator|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|directory
argument_list|)
expr_stmt|;
name|IndexSearcher
name|shardSearcher
init|=
name|newSearcher
argument_list|(
name|directoryReader
argument_list|)
decl_stmt|;
name|MemoryIndex
name|memoryIndex
init|=
operator|new
name|MemoryIndex
argument_list|()
decl_stmt|;
name|memoryIndex
operator|.
name|addField
argument_list|(
literal|"field"
argument_list|,
literal|"the quick brown fox jumps over the lazy dog"
argument_list|,
operator|new
name|WhitespaceAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|IndexSearcher
name|percolateSearcher
init|=
name|memoryIndex
operator|.
name|createSearcher
argument_list|()
decl_stmt|;
name|PercolatorQuery
operator|.
name|Builder
name|builder
init|=
operator|new
name|PercolatorQuery
operator|.
name|Builder
argument_list|(
literal|"docType"
argument_list|,
name|queryRegistry
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{}"
argument_list|)
argument_list|,
name|percolateSearcher
argument_list|,
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|extractQueryTermsQuery
argument_list|(
name|EXTRACTED_TERMS_FIELD_NAME
argument_list|,
name|UNKNOWN_QUERY_FIELD_NAME
argument_list|)
expr_stmt|;
name|TopDocs
name|topDocs
init|=
name|shardSearcher
operator|.
name|search
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
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
name|Explanation
name|explanation
init|=
name|shardSearcher
operator|.
name|explain
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|isMatch
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|score
argument_list|)
argument_list|)
expr_stmt|;
name|explanation
operator|=
name|shardSearcher
operator|.
name|explain
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|isMatch
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
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
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|explanation
operator|=
name|shardSearcher
operator|.
name|explain
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|isMatch
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|score
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|explanation
operator|=
name|shardSearcher
operator|.
name|explain
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|isMatch
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|.
name|score
argument_list|)
argument_list|)
expr_stmt|;
name|explanation
operator|=
name|shardSearcher
operator|.
name|explain
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|isMatch
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|3
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|explanation
operator|=
name|shardSearcher
operator|.
name|explain
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|isMatch
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|3
index|]
operator|.
name|score
argument_list|)
argument_list|)
expr_stmt|;
name|explanation
operator|=
name|shardSearcher
operator|.
name|explain
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|isMatch
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|4
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|explanation
operator|=
name|shardSearcher
operator|.
name|explain
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|isMatch
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|explanation
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|4
index|]
operator|.
name|score
argument_list|)
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
DECL|method|testDuel
specifier|public
name|void
name|testDuel
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numQueries
init|=
name|scaledRandomIntBetween
argument_list|(
literal|32
argument_list|,
literal|256
argument_list|)
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
name|numQueries
condition|;
name|i
operator|++
control|)
block|{
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Query
name|query
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|=
operator|new
name|PrefixQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
name|id
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|=
operator|new
name|WildcardQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
name|id
operator|+
literal|"*"
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|=
operator|new
name|CustomQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
name|id
operator|+
literal|"*"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|query
operator|=
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
name|id
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|addPercolatorQuery
argument_list|(
name|id
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|directoryReader
operator|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|directory
argument_list|)
expr_stmt|;
name|IndexSearcher
name|shardSearcher
init|=
name|newSearcher
argument_list|(
name|directoryReader
argument_list|)
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
name|numQueries
condition|;
name|i
operator|++
control|)
block|{
name|MemoryIndex
name|memoryIndex
init|=
operator|new
name|MemoryIndex
argument_list|()
decl_stmt|;
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|memoryIndex
operator|.
name|addField
argument_list|(
literal|"field"
argument_list|,
name|id
argument_list|,
operator|new
name|WhitespaceAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|duelRun
argument_list|(
name|memoryIndex
argument_list|,
name|shardSearcher
argument_list|)
expr_stmt|;
block|}
block|}
end_function

begin_function
DECL|method|testDuelSpecificQueries
specifier|public
name|void
name|testDuelSpecificQueries
parameter_list|()
throws|throws
name|Exception
block|{
name|CommonTermsQuery
name|commonTermsQuery
init|=
operator|new
name|CommonTermsQuery
argument_list|(
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|,
literal|128
argument_list|)
decl_stmt|;
name|commonTermsQuery
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"quick"
argument_list|)
argument_list|)
expr_stmt|;
name|commonTermsQuery
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"brown"
argument_list|)
argument_list|)
expr_stmt|;
name|commonTermsQuery
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"fox"
argument_list|)
argument_list|)
expr_stmt|;
name|addPercolatorQuery
argument_list|(
literal|"_id1"
argument_list|,
name|commonTermsQuery
argument_list|)
expr_stmt|;
name|BlendedTermQuery
name|blendedTermQuery
init|=
name|BlendedTermQuery
operator|.
name|booleanBlendedQuery
argument_list|(
operator|new
name|Term
index|[]
block|{
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"quick"
argument_list|)
block|,
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"brown"
argument_list|)
block|,
operator|new
name|Term
argument_list|(
literal|"field"
argument_list|,
literal|"fox"
argument_list|)
block|}
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|addPercolatorQuery
argument_list|(
literal|"_id2"
argument_list|,
name|blendedTermQuery
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|directoryReader
operator|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|directory
argument_list|)
expr_stmt|;
name|IndexSearcher
name|shardSearcher
init|=
name|newSearcher
argument_list|(
name|directoryReader
argument_list|)
decl_stmt|;
name|MemoryIndex
name|memoryIndex
init|=
operator|new
name|MemoryIndex
argument_list|()
decl_stmt|;
name|memoryIndex
operator|.
name|addField
argument_list|(
literal|"field"
argument_list|,
literal|"the quick brown fox jumps over the lazy dog"
argument_list|,
operator|new
name|WhitespaceAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|duelRun
argument_list|(
name|memoryIndex
argument_list|,
name|shardSearcher
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
DECL|method|addPercolatorQuery
name|void
name|addPercolatorQuery
parameter_list|(
name|String
name|id
parameter_list|,
name|Query
name|query
parameter_list|,
name|String
modifier|...
name|extraFields
parameter_list|)
throws|throws
name|IOException
block|{
name|queries
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|query
argument_list|)
expr_stmt|;
name|ParseContext
operator|.
name|Document
name|document
init|=
operator|new
name|ParseContext
operator|.
name|Document
argument_list|()
decl_stmt|;
name|ExtractQueryTermsService
operator|.
name|extractQueryTerms
argument_list|(
name|query
argument_list|,
name|document
argument_list|,
name|EXTRACTED_TERMS_FIELD_NAME
argument_list|,
name|UNKNOWN_QUERY_FIELD_NAME
argument_list|,
name|EXTRACTED_TERMS_FIELD_TYPE
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StoredField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|PercolatorFieldMapper
operator|.
name|TYPE_NAME
argument_list|,
name|id
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
assert|assert
name|extraFields
operator|.
name|length
operator|%
literal|2
operator|==
literal|0
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|extraFields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
name|extraFields
index|[
name|i
index|]
argument_list|,
name|extraFields
index|[
operator|++
name|i
index|]
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
DECL|method|duelRun
specifier|private
name|void
name|duelRun
parameter_list|(
name|MemoryIndex
name|memoryIndex
parameter_list|,
name|IndexSearcher
name|shardSearcher
parameter_list|)
throws|throws
name|IOException
block|{
name|IndexSearcher
name|percolateSearcher
init|=
name|memoryIndex
operator|.
name|createSearcher
argument_list|()
decl_stmt|;
name|PercolatorQuery
operator|.
name|Builder
name|builder1
init|=
operator|new
name|PercolatorQuery
operator|.
name|Builder
argument_list|(
literal|"docType"
argument_list|,
name|queryRegistry
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{}"
argument_list|)
argument_list|,
name|percolateSearcher
argument_list|,
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|)
decl_stmt|;
comment|// enables the optimization that prevents queries from being evaluated that don't match
name|builder1
operator|.
name|extractQueryTermsQuery
argument_list|(
name|EXTRACTED_TERMS_FIELD_NAME
argument_list|,
name|UNKNOWN_QUERY_FIELD_NAME
argument_list|)
expr_stmt|;
name|TopDocs
name|topDocs1
init|=
name|shardSearcher
operator|.
name|search
argument_list|(
name|builder1
operator|.
name|build
argument_list|()
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|PercolatorQuery
operator|.
name|Builder
name|builder2
init|=
operator|new
name|PercolatorQuery
operator|.
name|Builder
argument_list|(
literal|"docType"
argument_list|,
name|queryRegistry
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{}"
argument_list|)
argument_list|,
name|percolateSearcher
argument_list|,
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|)
decl_stmt|;
name|TopDocs
name|topDocs2
init|=
name|shardSearcher
operator|.
name|search
argument_list|(
name|builder2
operator|.
name|build
argument_list|()
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topDocs1
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
name|topDocs2
operator|.
name|totalHits
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs1
operator|.
name|scoreDocs
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|topDocs2
operator|.
name|scoreDocs
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|topDocs1
operator|.
name|scoreDocs
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|topDocs1
operator|.
name|scoreDocs
index|[
name|j
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
name|topDocs2
operator|.
name|scoreDocs
index|[
name|j
index|]
operator|.
name|doc
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs1
operator|.
name|scoreDocs
index|[
name|j
index|]
operator|.
name|score
argument_list|,
name|equalTo
argument_list|(
name|topDocs2
operator|.
name|scoreDocs
index|[
name|j
index|]
operator|.
name|score
argument_list|)
argument_list|)
expr_stmt|;
name|Explanation
name|explain1
init|=
name|shardSearcher
operator|.
name|explain
argument_list|(
name|builder1
operator|.
name|build
argument_list|()
argument_list|,
name|topDocs1
operator|.
name|scoreDocs
index|[
name|j
index|]
operator|.
name|doc
argument_list|)
decl_stmt|;
name|Explanation
name|explain2
init|=
name|shardSearcher
operator|.
name|explain
argument_list|(
name|builder2
operator|.
name|build
argument_list|()
argument_list|,
name|topDocs2
operator|.
name|scoreDocs
index|[
name|j
index|]
operator|.
name|doc
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|explain1
operator|.
name|toHtml
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|explain2
operator|.
name|toHtml
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_function

begin_class
DECL|class|CustomQuery
specifier|private
specifier|final
specifier|static
class|class
name|CustomQuery
extends|extends
name|Query
block|{
DECL|field|term
specifier|private
specifier|final
name|Term
name|term
decl_stmt|;
DECL|method|CustomQuery
specifier|private
name|CustomQuery
parameter_list|(
name|Term
name|term
parameter_list|)
block|{
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|rewrite
specifier|public
name|Query
name|rewrite
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TermQuery
argument_list|(
name|term
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
literal|"custom{"
operator|+
name|field
operator|+
literal|"}"
return|;
block|}
block|}
end_class

unit|}
end_unit

