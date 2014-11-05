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
name|analysis
operator|.
name|payloads
operator|.
name|PayloadHelper
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
name|search
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
name|ElasticsearchTestCase
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
DECL|class|SimpleAllTests
specifier|public
class|class
name|SimpleAllTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testBoostOnEagerTokenizer
specifier|public
name|void
name|testBoostOnEagerTokenizer
parameter_list|()
throws|throws
name|Exception
block|{
name|AllEntries
name|allEntries
init|=
operator|new
name|AllEntries
argument_list|()
decl_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"all"
argument_list|,
literal|2.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field2"
argument_list|,
literal|"your"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"boosts"
argument_list|,
literal|0.5f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// whitespace analyzer's tokenizer reads characters eagerly on the contrary to the standard tokenizer
specifier|final
name|TokenStream
name|ts
init|=
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"any"
argument_list|,
name|allEntries
argument_list|,
operator|new
name|WhitespaceAnalyzer
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|CharTermAttribute
name|termAtt
init|=
name|ts
operator|.
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|PayloadAttribute
name|payloadAtt
init|=
name|ts
operator|.
name|addAttribute
argument_list|(
name|PayloadAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|ts
operator|.
name|reset
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
literal|3
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|ts
operator|.
name|incrementToken
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|term
decl_stmt|;
specifier|final
name|float
name|boost
decl_stmt|;
switch|switch
condition|(
name|i
condition|)
block|{
case|case
literal|0
case|:
name|term
operator|=
literal|"all"
expr_stmt|;
name|boost
operator|=
literal|2
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|term
operator|=
literal|"your"
expr_stmt|;
name|boost
operator|=
literal|1
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|term
operator|=
literal|"boosts"
expr_stmt|;
name|boost
operator|=
literal|0.5f
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
name|assertEquals
argument_list|(
name|term
argument_list|,
name|termAtt
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|BytesRef
name|payload
init|=
name|payloadAtt
operator|.
name|getPayload
argument_list|()
decl_stmt|;
if|if
condition|(
name|payload
operator|==
literal|null
operator|||
name|payload
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|boost
argument_list|,
literal|1f
argument_list|,
literal|0.001f
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|payload
operator|.
name|length
argument_list|)
expr_stmt|;
specifier|final
name|float
name|b
init|=
name|PayloadHelper
operator|.
name|decodeFloat
argument_list|(
name|payload
operator|.
name|bytes
argument_list|,
name|payload
operator|.
name|offset
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|boost
argument_list|,
name|b
argument_list|,
literal|0.001f
argument_list|)
expr_stmt|;
block|}
block|}
name|assertFalse
argument_list|(
name|ts
operator|.
name|incrementToken
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testAllEntriesRead
specifier|public
name|void
name|testAllEntriesRead
parameter_list|()
throws|throws
name|Exception
block|{
name|AllEntries
name|allEntries
init|=
operator|new
name|AllEntries
argument_list|()
decl_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"something"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field2"
argument_list|,
literal|"else"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|30
condition|;
name|i
operator|++
control|)
block|{
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|char
index|[]
name|data
init|=
operator|new
name|char
index|[
name|i
index|]
decl_stmt|;
name|String
name|value
init|=
name|slurpToString
argument_list|(
name|allEntries
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"failed for "
operator|+
name|i
argument_list|,
name|value
argument_list|,
name|equalTo
argument_list|(
literal|"something else"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|slurpToString
specifier|private
name|String
name|slurpToString
parameter_list|(
name|AllEntries
name|allEntries
parameter_list|,
name|char
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|read
init|=
name|allEntries
operator|.
name|read
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|read
operator|==
operator|-
literal|1
condition|)
block|{
break|break;
block|}
name|sb
operator|.
name|append
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|read
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
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
annotation|@
name|Test
DECL|method|testSimpleAllNoBoost
specifier|public
name|void
name|testSimpleAllNoBoost
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
name|AllEntries
name|allEntries
init|=
operator|new
name|AllEntries
argument_list|()
decl_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"something"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field2"
argument_list|,
literal|"else"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"_all"
argument_list|,
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"_all"
argument_list|,
name|allEntries
argument_list|,
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
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
name|allEntries
operator|=
operator|new
name|AllEntries
argument_list|()
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"else"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field2"
argument_list|,
literal|"something"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"_all"
argument_list|,
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"_all"
argument_list|,
name|allEntries
argument_list|,
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
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
argument_list|,
literal|true
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
annotation|@
name|Test
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
name|AllEntries
name|allEntries
init|=
operator|new
name|AllEntries
argument_list|()
decl_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"something"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field2"
argument_list|,
literal|"else"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"_all"
argument_list|,
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"_all"
argument_list|,
name|allEntries
argument_list|,
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
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
name|allEntries
operator|=
operator|new
name|AllEntries
argument_list|()
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"else"
argument_list|,
literal|2.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field2"
argument_list|,
literal|"something"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"_all"
argument_list|,
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"_all"
argument_list|,
name|allEntries
argument_list|,
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
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
argument_list|,
literal|true
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
annotation|@
name|Test
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
name|AllEntries
name|allEntries
init|=
operator|new
name|AllEntries
argument_list|()
decl_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"something moo"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field2"
argument_list|,
literal|"else koo"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"_all"
argument_list|,
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"_all"
argument_list|,
name|allEntries
argument_list|,
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
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
name|allEntries
operator|=
operator|new
name|AllEntries
argument_list|()
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"else koo"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field2"
argument_list|,
literal|"something moo"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"_all"
argument_list|,
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"_all"
argument_list|,
name|allEntries
argument_list|,
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
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
argument_list|,
literal|true
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
annotation|@
name|Test
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
name|AllEntries
name|allEntries
init|=
operator|new
name|AllEntries
argument_list|()
decl_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"something moo"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field2"
argument_list|,
literal|"else koo"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"_all"
argument_list|,
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"_all"
argument_list|,
name|allEntries
argument_list|,
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
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
name|allEntries
operator|=
operator|new
name|AllEntries
argument_list|()
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
literal|"else koo"
argument_list|,
literal|2.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field2"
argument_list|,
literal|"something moo"
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"_all"
argument_list|,
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"_all"
argument_list|,
name|allEntries
argument_list|,
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
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
argument_list|,
literal|true
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
annotation|@
name|Test
DECL|method|testNoTokensWithKeywordAnalyzer
specifier|public
name|void
name|testNoTokensWithKeywordAnalyzer
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
name|AllEntries
name|allEntries
init|=
operator|new
name|AllEntries
argument_list|()
decl_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"_all"
argument_list|,
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"_all"
argument_list|,
name|allEntries
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|)
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
argument_list|,
literal|true
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

