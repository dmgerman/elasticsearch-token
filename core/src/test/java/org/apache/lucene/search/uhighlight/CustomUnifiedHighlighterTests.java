begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.search.uhighlight
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|uhighlight
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
name|RandomIndexWriter
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
name|Sort
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
name|highlight
operator|.
name|DefaultEncoder
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
name|highlight
operator|.
name|Snippet
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
name|Strings
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
name|all
operator|.
name|AllTermQuery
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
name|search
operator|.
name|MultiPhrasePrefixQuery
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
name|text
operator|.
name|BreakIterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|search
operator|.
name|uhighlight
operator|.
name|CustomUnifiedHighlighter
operator|.
name|MULTIVAL_SEP_CHAR
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|CustomUnifiedHighlighterTests
specifier|public
class|class
name|CustomUnifiedHighlighterTests
extends|extends
name|ESTestCase
block|{
DECL|method|assertHighlightOneDoc
specifier|private
name|void
name|assertHighlightOneDoc
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|String
index|[]
name|inputs
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|,
name|Query
name|query
parameter_list|,
name|Locale
name|locale
parameter_list|,
name|BreakIterator
name|breakIterator
parameter_list|,
name|int
name|noMatchSize
parameter_list|,
name|String
index|[]
name|expectedPassages
parameter_list|)
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriterConfig
name|iwc
init|=
name|newIndexWriterConfig
argument_list|(
name|analyzer
argument_list|)
decl_stmt|;
name|iwc
operator|.
name|setMergePolicy
argument_list|(
name|newTieredMergePolicy
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|RandomIndexWriter
name|iw
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|random
argument_list|()
argument_list|,
name|dir
argument_list|,
name|iwc
argument_list|)
decl_stmt|;
name|FieldType
name|ft
init|=
operator|new
name|FieldType
argument_list|(
name|TextField
operator|.
name|TYPE_STORED
argument_list|)
decl_stmt|;
name|ft
operator|.
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS
argument_list|)
expr_stmt|;
name|ft
operator|.
name|freeze
argument_list|()
expr_stmt|;
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|input
range|:
name|inputs
control|)
block|{
name|Field
name|field
init|=
operator|new
name|Field
argument_list|(
name|fieldName
argument_list|,
literal|""
argument_list|,
name|ft
argument_list|)
decl_stmt|;
name|field
operator|.
name|setStringValue
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
name|iw
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|DirectoryReader
name|reader
init|=
name|iw
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
name|newSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
name|iw
operator|.
name|close
argument_list|()
expr_stmt|;
name|TopDocs
name|topDocs
init|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|1
argument_list|,
name|Sort
operator|.
name|INDEXORDER
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
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|rawValue
init|=
name|Strings
operator|.
name|arrayToDelimitedString
argument_list|(
name|inputs
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|MULTIVAL_SEP_CHAR
argument_list|)
argument_list|)
decl_stmt|;
name|CustomUnifiedHighlighter
name|highlighter
init|=
operator|new
name|CustomUnifiedHighlighter
argument_list|(
name|searcher
argument_list|,
name|analyzer
argument_list|,
operator|new
name|CustomPassageFormatter
argument_list|(
literal|"<b>"
argument_list|,
literal|"</b>"
argument_list|,
operator|new
name|DefaultEncoder
argument_list|()
argument_list|)
argument_list|,
name|locale
argument_list|,
name|breakIterator
argument_list|,
name|rawValue
argument_list|,
name|noMatchSize
argument_list|)
decl_stmt|;
name|highlighter
operator|.
name|setFieldMatcher
argument_list|(
parameter_list|(
name|name
parameter_list|)
lambda|->
literal|"text"
operator|.
name|equals
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Snippet
index|[]
name|snippets
init|=
name|highlighter
operator|.
name|highlightField
argument_list|(
literal|"text"
argument_list|,
name|query
argument_list|,
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|expectedPassages
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|snippets
operator|.
name|length
argument_list|,
name|expectedPassages
operator|.
name|length
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
name|snippets
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|snippets
index|[
name|i
index|]
operator|.
name|getText
argument_list|()
argument_list|,
name|expectedPassages
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|reader
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
DECL|method|testSimple
specifier|public
name|void
name|testSimple
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
index|[]
name|inputs
init|=
block|{
literal|"This is a test. Just a test1 highlighting from unified highlighter."
block|,
literal|"This is the second highlighting value to perform highlighting on a longer text that gets scored lower."
block|,
literal|"This is highlighting the third short highlighting value."
block|,
literal|"Just a test4 highlighting from unified highlighter."
block|}
decl_stmt|;
name|String
index|[]
name|expectedPassages
init|=
block|{
literal|"Just a test1<b>highlighting</b> from unified highlighter."
block|,
literal|"This is the second<b>highlighting</b> value to perform<b>highlighting</b> on a"
operator|+
literal|" longer text that gets scored lower."
block|,
literal|"This is<b>highlighting</b> the third short<b>highlighting</b> value."
block|,
literal|"Just a test4<b>highlighting</b> from unified highlighter."
block|}
decl_stmt|;
name|Query
name|query
init|=
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"highlighting"
argument_list|)
argument_list|)
decl_stmt|;
name|assertHighlightOneDoc
argument_list|(
literal|"text"
argument_list|,
name|inputs
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|()
argument_list|,
name|query
argument_list|,
name|Locale
operator|.
name|ROOT
argument_list|,
name|BreakIterator
operator|.
name|getSentenceInstance
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
literal|0
argument_list|,
name|expectedPassages
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoMatchSize
specifier|public
name|void
name|testNoMatchSize
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
index|[]
name|inputs
init|=
block|{
literal|"This is a test. Just a test highlighting from unified. Feel free to ignore."
block|}
decl_stmt|;
name|Query
name|query
init|=
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"body"
argument_list|,
literal|"highlighting"
argument_list|)
argument_list|)
decl_stmt|;
name|assertHighlightOneDoc
argument_list|(
literal|"text"
argument_list|,
name|inputs
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|()
argument_list|,
name|query
argument_list|,
name|Locale
operator|.
name|ROOT
argument_list|,
name|BreakIterator
operator|.
name|getSentenceInstance
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
literal|100
argument_list|,
name|inputs
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiPhrasePrefixQuery
specifier|public
name|void
name|testMultiPhrasePrefixQuery
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
index|[]
name|inputs
init|=
block|{
literal|"The quick brown fox."
block|}
decl_stmt|;
specifier|final
name|String
index|[]
name|outputs
init|=
block|{
literal|"The<b>quick</b><b>brown</b><b>fox</b>."
block|}
decl_stmt|;
name|MultiPhrasePrefixQuery
name|query
init|=
operator|new
name|MultiPhrasePrefixQuery
argument_list|()
decl_stmt|;
name|query
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"quick"
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"brown"
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"fo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertHighlightOneDoc
argument_list|(
literal|"text"
argument_list|,
name|inputs
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|()
argument_list|,
name|query
argument_list|,
name|Locale
operator|.
name|ROOT
argument_list|,
name|BreakIterator
operator|.
name|getSentenceInstance
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
literal|0
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
block|}
DECL|method|testAllTermQuery
specifier|public
name|void
name|testAllTermQuery
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
index|[]
name|inputs
init|=
block|{
literal|"The quick brown fox."
block|}
decl_stmt|;
specifier|final
name|String
index|[]
name|outputs
init|=
block|{
literal|"The quick brown<b>fox</b>."
block|}
decl_stmt|;
name|AllTermQuery
name|query
init|=
operator|new
name|AllTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"fox"
argument_list|)
argument_list|)
decl_stmt|;
name|assertHighlightOneDoc
argument_list|(
literal|"text"
argument_list|,
name|inputs
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|()
argument_list|,
name|query
argument_list|,
name|Locale
operator|.
name|ROOT
argument_list|,
name|BreakIterator
operator|.
name|getSentenceInstance
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
literal|0
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
block|}
DECL|method|testCommonTermsQuery
specifier|public
name|void
name|testCommonTermsQuery
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
index|[]
name|inputs
init|=
block|{
literal|"The quick brown fox."
block|}
decl_stmt|;
specifier|final
name|String
index|[]
name|outputs
init|=
block|{
literal|"The<b>quick</b><b>brown</b><b>fox</b>."
block|}
decl_stmt|;
name|CommonTermsQuery
name|query
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
name|query
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"quick"
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"brown"
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"fox"
argument_list|)
argument_list|)
expr_stmt|;
name|assertHighlightOneDoc
argument_list|(
literal|"text"
argument_list|,
name|inputs
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|()
argument_list|,
name|query
argument_list|,
name|Locale
operator|.
name|ROOT
argument_list|,
name|BreakIterator
operator|.
name|getSentenceInstance
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
literal|0
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
block|}
DECL|method|testSentenceBoundedBreakIterator
specifier|public
name|void
name|testSentenceBoundedBreakIterator
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
index|[]
name|inputs
init|=
block|{
literal|"The quick brown fox in a long sentence with another quick brown fox. "
operator|+
literal|"Another sentence with brown fox."
block|}
decl_stmt|;
specifier|final
name|String
index|[]
name|outputs
init|=
block|{
literal|"The<b>quick</b><b>brown</b>"
block|,
literal|"<b>fox</b> in a long"
block|,
literal|"with another<b>quick</b>"
block|,
literal|"<b>brown</b><b>fox</b>."
block|,
literal|"sentence with<b>brown</b>"
block|,
literal|"<b>fox</b>."
block|,         }
decl_stmt|;
name|BooleanQuery
name|query
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"quick"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"brown"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
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
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertHighlightOneDoc
argument_list|(
literal|"text"
argument_list|,
name|inputs
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|()
argument_list|,
name|query
argument_list|,
name|Locale
operator|.
name|ROOT
argument_list|,
name|BoundedBreakIteratorScanner
operator|.
name|getSentence
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|0
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
block|}
DECL|method|testRepeat
specifier|public
name|void
name|testRepeat
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
index|[]
name|inputs
init|=
block|{
literal|"Fun  fun fun  fun  fun  fun  fun  fun  fun  fun"
block|}
decl_stmt|;
specifier|final
name|String
index|[]
name|outputs
init|=
block|{
literal|"<b>Fun</b><b>fun</b><b>fun</b>"
block|,
literal|"<b>fun</b><b>fun</b>"
block|,
literal|"<b>fun</b><b>fun</b><b>fun</b>"
block|,
literal|"<b>fun</b><b>fun</b>"
block|}
decl_stmt|;
name|Query
name|query
init|=
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"fun"
argument_list|)
argument_list|)
decl_stmt|;
name|assertHighlightOneDoc
argument_list|(
literal|"text"
argument_list|,
name|inputs
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|()
argument_list|,
name|query
argument_list|,
name|Locale
operator|.
name|ROOT
argument_list|,
name|BoundedBreakIteratorScanner
operator|.
name|getSentence
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|0
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|query
operator|=
operator|new
name|PhraseQuery
operator|.
name|Builder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"fun"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text"
argument_list|,
literal|"fun"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertHighlightOneDoc
argument_list|(
literal|"text"
argument_list|,
name|inputs
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|()
argument_list|,
name|query
argument_list|,
name|Locale
operator|.
name|ROOT
argument_list|,
name|BoundedBreakIteratorScanner
operator|.
name|getSentence
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|0
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

