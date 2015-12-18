begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.queries
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queries
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
name|MockAnalyzer
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
name|TextField
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
name|MultiReader
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
name|DisjunctionMaxQuery
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
name|QueryUtils
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
name|search
operator|.
name|similarities
operator|.
name|BM25Similarity
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
name|similarities
operator|.
name|DefaultSimilarity
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
name|similarities
operator|.
name|Similarity
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
name|TestUtil
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
name|Collections
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
name|List
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
name|containsInAnyOrder
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
comment|/**  */
end_comment

begin_class
DECL|class|BlendedTermQueryTests
specifier|public
class|class
name|BlendedTermQueryTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBooleanQuery
specifier|public
name|void
name|testBooleanQuery
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
name|w
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|newIndexWriterConfig
argument_list|(
operator|new
name|MockAnalyzer
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
index|[]
name|firstNames
init|=
operator|new
name|String
index|[]
block|{
literal|"simon"
block|,
literal|"paul"
block|}
decl_stmt|;
name|String
index|[]
name|surNames
init|=
operator|new
name|String
index|[]
block|{
literal|"willnauer"
block|,
literal|"simon"
block|}
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
name|surNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
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
name|TextField
argument_list|(
literal|"id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"firstname"
argument_list|,
name|firstNames
index|[
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
name|d
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"surname"
argument_list|,
name|surNames
index|[
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
name|w
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
name|int
name|iters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|25
argument_list|,
literal|100
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|iters
condition|;
name|j
operator|++
control|)
block|{
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
name|TextField
argument_list|(
literal|"id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|firstNames
operator|.
name|length
operator|+
name|j
argument_list|)
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"firstname"
argument_list|,
name|rarely
argument_list|()
condition|?
literal|"some_other_name"
else|:
literal|"simon the sorcerer"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure length-norm is the tie-breaker
name|d
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"surname"
argument_list|,
literal|"bogus"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|w
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|commit
argument_list|()
expr_stmt|;
name|DirectoryReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|w
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
name|setSimilarity
argument_list|(
name|newSearcher
argument_list|(
name|reader
argument_list|)
argument_list|)
decl_stmt|;
block|{
name|Term
index|[]
name|terms
init|=
operator|new
name|Term
index|[]
block|{
operator|new
name|Term
argument_list|(
literal|"firstname"
argument_list|,
literal|"simon"
argument_list|)
block|,
operator|new
name|Term
argument_list|(
literal|"surname"
argument_list|,
literal|"simon"
argument_list|)
block|}
decl_stmt|;
name|BlendedTermQuery
name|query
init|=
name|BlendedTermQuery
operator|.
name|booleanBlendedQuery
argument_list|(
name|terms
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|TopDocs
name|search
init|=
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|3
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
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|scoreDocs
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
literal|0
argument_list|)
argument_list|,
name|reader
operator|.
name|document
argument_list|(
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|)
operator|.
name|getField
argument_list|(
literal|"id"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|{
name|BooleanQuery
operator|.
name|Builder
name|query
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|query
operator|.
name|setDisableCoord
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"firstname"
argument_list|,
literal|"simon"
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
name|query
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"surname"
argument_list|,
literal|"simon"
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
name|TopDocs
name|search
init|=
name|searcher
operator|.
name|search
argument_list|(
name|query
operator|.
name|build
argument_list|()
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
name|assertEquals
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
literal|1
argument_list|)
argument_list|,
name|reader
operator|.
name|document
argument_list|(
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|)
operator|.
name|getField
argument_list|(
literal|"id"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|w
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
DECL|method|testDismaxQuery
specifier|public
name|void
name|testDismaxQuery
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
name|w
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|newIndexWriterConfig
argument_list|(
operator|new
name|MockAnalyzer
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
index|[]
name|username
init|=
operator|new
name|String
index|[]
block|{
literal|"foo fighters"
block|,
literal|"some cool fan"
block|,
literal|"cover band"
block|}
decl_stmt|;
name|String
index|[]
name|song
init|=
operator|new
name|String
index|[]
block|{
literal|"generator"
block|,
literal|"foo fighers - generator"
block|,
literal|"foo fighters generator"
block|}
decl_stmt|;
specifier|final
name|boolean
name|omitNorms
init|=
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
name|FieldType
name|ft
init|=
operator|new
name|FieldType
argument_list|(
name|TextField
operator|.
name|TYPE_NOT_STORED
argument_list|)
decl_stmt|;
name|ft
operator|.
name|setIndexOptions
argument_list|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
name|IndexOptions
operator|.
name|DOCS
else|:
name|IndexOptions
operator|.
name|DOCS_AND_FREQS
argument_list|)
expr_stmt|;
name|ft
operator|.
name|setOmitNorms
argument_list|(
name|omitNorms
argument_list|)
expr_stmt|;
name|ft
operator|.
name|freeze
argument_list|()
expr_stmt|;
name|FieldType
name|ft1
init|=
operator|new
name|FieldType
argument_list|(
name|TextField
operator|.
name|TYPE_NOT_STORED
argument_list|)
decl_stmt|;
name|ft1
operator|.
name|setIndexOptions
argument_list|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
name|IndexOptions
operator|.
name|DOCS
else|:
name|IndexOptions
operator|.
name|DOCS_AND_FREQS
argument_list|)
expr_stmt|;
name|ft1
operator|.
name|setOmitNorms
argument_list|(
name|omitNorms
argument_list|)
expr_stmt|;
name|ft1
operator|.
name|freeze
argument_list|()
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
name|username
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
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
name|TextField
argument_list|(
literal|"id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
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
literal|"username"
argument_list|,
name|username
index|[
name|i
index|]
argument_list|,
name|ft
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
literal|"song"
argument_list|,
name|song
index|[
name|i
index|]
argument_list|,
name|ft
argument_list|)
argument_list|)
expr_stmt|;
name|w
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
name|int
name|iters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|25
argument_list|,
literal|100
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|iters
condition|;
name|j
operator|++
control|)
block|{
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
name|TextField
argument_list|(
literal|"id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|username
operator|.
name|length
operator|+
name|j
argument_list|)
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
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
literal|"username"
argument_list|,
literal|"foo fighters"
argument_list|,
name|ft1
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
literal|"song"
argument_list|,
literal|"some bogus text to bump up IDF"
argument_list|,
name|ft1
argument_list|)
argument_list|)
expr_stmt|;
name|w
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|commit
argument_list|()
expr_stmt|;
name|DirectoryReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|w
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
name|setSimilarity
argument_list|(
name|newSearcher
argument_list|(
name|reader
argument_list|)
argument_list|)
decl_stmt|;
block|{
name|String
index|[]
name|fields
init|=
operator|new
name|String
index|[]
block|{
literal|"username"
block|,
literal|"song"
block|}
decl_stmt|;
name|BooleanQuery
operator|.
name|Builder
name|query
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|query
operator|.
name|setDisableCoord
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
name|BlendedTermQuery
operator|.
name|dismaxBlendedQuery
argument_list|(
name|toTerms
argument_list|(
name|fields
argument_list|,
literal|"foo"
argument_list|)
argument_list|,
literal|0.1f
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
name|BlendedTermQuery
operator|.
name|dismaxBlendedQuery
argument_list|(
name|toTerms
argument_list|(
name|fields
argument_list|,
literal|"fighters"
argument_list|)
argument_list|,
literal|0.1f
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
name|BlendedTermQuery
operator|.
name|dismaxBlendedQuery
argument_list|(
name|toTerms
argument_list|(
name|fields
argument_list|,
literal|"generator"
argument_list|)
argument_list|,
literal|0.1f
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|TopDocs
name|search
init|=
name|searcher
operator|.
name|search
argument_list|(
name|query
operator|.
name|build
argument_list|()
argument_list|,
literal|10
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
name|assertEquals
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
literal|0
argument_list|)
argument_list|,
name|reader
operator|.
name|document
argument_list|(
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|)
operator|.
name|getField
argument_list|(
literal|"id"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|{
name|BooleanQuery
operator|.
name|Builder
name|query
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|query
operator|.
name|setDisableCoord
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|DisjunctionMaxQuery
name|uname
init|=
operator|new
name|DisjunctionMaxQuery
argument_list|(
literal|0.0f
argument_list|)
decl_stmt|;
name|uname
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"username"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|uname
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"song"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|DisjunctionMaxQuery
name|s
init|=
operator|new
name|DisjunctionMaxQuery
argument_list|(
literal|0.0f
argument_list|)
decl_stmt|;
name|s
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"username"
argument_list|,
literal|"fighers"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"song"
argument_list|,
literal|"fighers"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|DisjunctionMaxQuery
name|gen
init|=
operator|new
name|DisjunctionMaxQuery
argument_list|(
literal|0f
argument_list|)
decl_stmt|;
name|gen
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"username"
argument_list|,
literal|"generator"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|gen
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"song"
argument_list|,
literal|"generator"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
name|uname
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
name|s
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
name|gen
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|TopDocs
name|search
init|=
name|searcher
operator|.
name|search
argument_list|(
name|query
operator|.
name|build
argument_list|()
argument_list|,
literal|4
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
name|assertEquals
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
literal|1
argument_list|)
argument_list|,
name|reader
operator|.
name|document
argument_list|(
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|)
operator|.
name|getField
argument_list|(
literal|"id"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|w
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
DECL|method|testBasics
specifier|public
name|void
name|testBasics
parameter_list|()
block|{
specifier|final
name|int
name|iters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|5
argument_list|,
literal|25
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|iters
condition|;
name|j
operator|++
control|)
block|{
name|String
index|[]
name|fields
init|=
operator|new
name|String
index|[
literal|1
operator|+
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
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
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|fields
index|[
name|i
index|]
operator|=
name|TestUtil
operator|.
name|randomRealisticUnicodeString
argument_list|(
name|random
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
name|String
name|term
init|=
name|TestUtil
operator|.
name|randomRealisticUnicodeString
argument_list|(
name|random
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Term
index|[]
name|terms
init|=
name|toTerms
argument_list|(
name|fields
argument_list|,
name|term
argument_list|)
decl_stmt|;
name|boolean
name|disableCoord
init|=
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
name|boolean
name|useBoolean
init|=
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
name|float
name|tieBreaker
init|=
name|random
argument_list|()
operator|.
name|nextFloat
argument_list|()
decl_stmt|;
name|BlendedTermQuery
name|query
init|=
name|useBoolean
condition|?
name|BlendedTermQuery
operator|.
name|booleanBlendedQuery
argument_list|(
name|terms
argument_list|,
name|disableCoord
argument_list|)
else|:
name|BlendedTermQuery
operator|.
name|dismaxBlendedQuery
argument_list|(
name|terms
argument_list|,
name|tieBreaker
argument_list|)
decl_stmt|;
name|QueryUtils
operator|.
name|check
argument_list|(
name|query
argument_list|)
expr_stmt|;
name|terms
operator|=
name|toTerms
argument_list|(
name|fields
argument_list|,
name|term
argument_list|)
expr_stmt|;
name|BlendedTermQuery
name|query2
init|=
name|useBoolean
condition|?
name|BlendedTermQuery
operator|.
name|booleanBlendedQuery
argument_list|(
name|terms
argument_list|,
name|disableCoord
argument_list|)
else|:
name|BlendedTermQuery
operator|.
name|dismaxBlendedQuery
argument_list|(
name|terms
argument_list|,
name|tieBreaker
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
name|query2
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|toTerms
specifier|public
name|Term
index|[]
name|toTerms
parameter_list|(
name|String
index|[]
name|fields
parameter_list|,
name|String
name|term
parameter_list|)
block|{
name|Term
index|[]
name|terms
init|=
operator|new
name|Term
index|[
name|fields
operator|.
name|length
index|]
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldsList
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|fields
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|fieldsList
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
name|fields
operator|=
name|fieldsList
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
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
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|terms
index|[
name|i
index|]
operator|=
operator|new
name|Term
argument_list|(
name|fields
index|[
name|i
index|]
argument_list|,
name|term
argument_list|)
expr_stmt|;
block|}
return|return
name|terms
return|;
block|}
DECL|method|setSimilarity
specifier|public
name|IndexSearcher
name|setSimilarity
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|)
block|{
name|Similarity
name|similarity
init|=
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
operator|new
name|BM25Similarity
argument_list|()
else|:
operator|new
name|DefaultSimilarity
argument_list|()
decl_stmt|;
name|searcher
operator|.
name|setSimilarity
argument_list|(
name|similarity
argument_list|)
expr_stmt|;
return|return
name|searcher
return|;
block|}
DECL|method|testExtractTerms
specifier|public
name|void
name|testExtractTerms
parameter_list|()
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|num
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
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
name|num
condition|;
name|i
operator|++
control|)
block|{
name|terms
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
name|TestUtil
operator|.
name|randomRealisticUnicodeString
argument_list|(
name|random
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|TestUtil
operator|.
name|randomRealisticUnicodeString
argument_list|(
name|random
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|BlendedTermQuery
name|blendedTermQuery
init|=
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
name|BlendedTermQuery
operator|.
name|dismaxBlendedQuery
argument_list|(
name|terms
operator|.
name|toArray
argument_list|(
operator|new
name|Term
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|random
argument_list|()
operator|.
name|nextFloat
argument_list|()
argument_list|)
else|:
name|BlendedTermQuery
operator|.
name|booleanBlendedQuery
argument_list|(
name|terms
operator|.
name|toArray
argument_list|(
operator|new
name|Term
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Term
argument_list|>
name|extracted
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
operator|new
name|MultiReader
argument_list|()
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|createNormalizedWeight
argument_list|(
name|blendedTermQuery
argument_list|,
literal|false
argument_list|)
operator|.
name|extractTerms
argument_list|(
name|extracted
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|extracted
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|terms
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|extracted
argument_list|,
name|containsInAnyOrder
argument_list|(
name|terms
operator|.
name|toArray
argument_list|(
operator|new
name|Term
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

