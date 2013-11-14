begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
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
name|codecs
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
name|FieldInfo
operator|.
name|DocValuesType
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
name|FieldInfo
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
name|search
operator|.
name|suggest
operator|.
name|InputIterator
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
name|suggest
operator|.
name|Lookup
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
name|suggest
operator|.
name|Lookup
operator|.
name|LookupResult
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
name|suggest
operator|.
name|analyzing
operator|.
name|AnalyzingSuggester
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
name|suggest
operator|.
name|analyzing
operator|.
name|XAnalyzingSuggester
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
name|IOContext
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
name|IndexInput
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
name|IndexOutput
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LineFileDocs
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
name|NamedAnalyzer
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
name|codec
operator|.
name|postingsformat
operator|.
name|ElasticSearch090PostingsFormat
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
name|codec
operator|.
name|postingsformat
operator|.
name|PostingsFormatProvider
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
name|codec
operator|.
name|postingsformat
operator|.
name|PreBuiltPostingsFormatProvider
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
name|FieldMapper
operator|.
name|Names
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
name|CompletionFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|AnalyzingCompletionLookupProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|Completion090PostingsFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|Completion090PostingsFormat
operator|.
name|LookupFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|CompletionSuggestionContext
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
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
DECL|class|CompletionPostingsFormatTest
specifier|public
class|class
name|CompletionPostingsFormatTest
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testCompletionPostingsFormat
specifier|public
name|void
name|testCompletionPostingsFormat
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalyzingCompletionLookupProvider
name|provider
init|=
operator|new
name|AnalyzingCompletionLookupProvider
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|RAMDirectory
name|dir
init|=
operator|new
name|RAMDirectory
argument_list|()
decl_stmt|;
name|IndexOutput
name|output
init|=
name|dir
operator|.
name|createOutput
argument_list|(
literal|"foo.txt"
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
decl_stmt|;
name|FieldsConsumer
name|consumer
init|=
name|provider
operator|.
name|consumer
argument_list|(
name|output
argument_list|)
decl_stmt|;
name|FieldInfo
name|fieldInfo
init|=
operator|new
name|FieldInfo
argument_list|(
literal|"foo"
argument_list|,
literal|true
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|IndexOptions
operator|.
name|DOCS_AND_FREQS_AND_POSITIONS
argument_list|,
name|DocValuesType
operator|.
name|SORTED
argument_list|,
name|DocValuesType
operator|.
name|BINARY
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|TermsConsumer
name|addField
init|=
name|consumer
operator|.
name|addField
argument_list|(
name|fieldInfo
argument_list|)
decl_stmt|;
name|PostingsConsumer
name|postingsConsumer
init|=
name|addField
operator|.
name|startTerm
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"foofightersgenerator"
argument_list|)
argument_list|)
decl_stmt|;
name|postingsConsumer
operator|.
name|startDoc
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|postingsConsumer
operator|.
name|addPosition
argument_list|(
literal|256
operator|-
literal|2
argument_list|,
name|provider
operator|.
name|buildPayload
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"Generator - Foo Fighters"
argument_list|)
argument_list|,
literal|9
argument_list|,
operator|new
name|BytesRef
argument_list|(
literal|"id:10"
argument_list|)
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|postingsConsumer
operator|.
name|finishDoc
argument_list|()
expr_stmt|;
name|addField
operator|.
name|finishTerm
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"foofightersgenerator"
argument_list|)
argument_list|,
operator|new
name|TermStats
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|addField
operator|.
name|startTerm
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"generator"
argument_list|)
argument_list|)
expr_stmt|;
name|postingsConsumer
operator|.
name|startDoc
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|postingsConsumer
operator|.
name|addPosition
argument_list|(
literal|256
operator|-
literal|1
argument_list|,
name|provider
operator|.
name|buildPayload
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"Generator - Foo Fighters"
argument_list|)
argument_list|,
literal|9
argument_list|,
operator|new
name|BytesRef
argument_list|(
literal|"id:10"
argument_list|)
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|postingsConsumer
operator|.
name|finishDoc
argument_list|()
expr_stmt|;
name|addField
operator|.
name|finishTerm
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"generator"
argument_list|)
argument_list|,
operator|new
name|TermStats
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|addField
operator|.
name|finish
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|close
argument_list|()
expr_stmt|;
name|output
operator|.
name|close
argument_list|()
expr_stmt|;
name|IndexInput
name|input
init|=
name|dir
operator|.
name|openInput
argument_list|(
literal|"foo.txt"
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
decl_stmt|;
name|LookupFactory
name|load
init|=
name|provider
operator|.
name|load
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|PostingsFormatProvider
name|format
init|=
operator|new
name|PreBuiltPostingsFormatProvider
argument_list|(
operator|new
name|ElasticSearch090PostingsFormat
argument_list|()
argument_list|)
decl_stmt|;
name|NamedAnalyzer
name|analyzer
init|=
operator|new
name|NamedAnalyzer
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|(
name|TEST_VERSION_CURRENT
argument_list|)
argument_list|)
decl_stmt|;
name|Lookup
name|lookup
init|=
name|load
operator|.
name|getLookup
argument_list|(
operator|new
name|CompletionFieldMapper
argument_list|(
operator|new
name|Names
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|analyzer
argument_list|,
name|analyzer
argument_list|,
name|format
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
operator|new
name|CompletionSuggestionContext
argument_list|(
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|LookupResult
argument_list|>
name|result
init|=
name|lookup
operator|.
name|lookup
argument_list|(
literal|"ge"
argument_list|,
literal|false
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|key
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Generator - Foo Fighters"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|payload
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"id:10"
argument_list|)
argument_list|)
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDuellCompletions
specifier|public
name|void
name|testDuellCompletions
parameter_list|()
throws|throws
name|IOException
throws|,
name|NoSuchFieldException
throws|,
name|SecurityException
throws|,
name|IllegalArgumentException
throws|,
name|IllegalAccessException
block|{
specifier|final
name|boolean
name|preserveSeparators
init|=
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|preservePositionIncrements
init|=
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|usePayloads
init|=
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
specifier|final
name|int
name|options
init|=
name|preserveSeparators
condition|?
name|AnalyzingSuggester
operator|.
name|PRESERVE_SEP
else|:
literal|0
decl_stmt|;
name|XAnalyzingSuggester
name|reference
init|=
operator|new
name|XAnalyzingSuggester
argument_list|(
operator|new
name|StandardAnalyzer
argument_list|(
name|TEST_VERSION_CURRENT
argument_list|)
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|(
name|TEST_VERSION_CURRENT
argument_list|)
argument_list|,
name|options
argument_list|,
literal|256
argument_list|,
operator|-
literal|1
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|reference
operator|.
name|setPreservePositionIncrements
argument_list|(
name|preservePositionIncrements
argument_list|)
expr_stmt|;
name|LineFileDocs
name|docs
init|=
operator|new
name|LineFileDocs
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|num
init|=
name|atLeast
argument_list|(
literal|150
argument_list|)
decl_stmt|;
specifier|final
name|String
index|[]
name|titles
init|=
operator|new
name|String
index|[
name|num
index|]
decl_stmt|;
specifier|final
name|long
index|[]
name|weights
init|=
operator|new
name|long
index|[
name|num
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
name|titles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Document
name|nextDoc
init|=
name|docs
operator|.
name|nextDoc
argument_list|()
decl_stmt|;
name|IndexableField
name|field
init|=
name|nextDoc
operator|.
name|getField
argument_list|(
literal|"title"
argument_list|)
decl_stmt|;
name|titles
index|[
name|i
index|]
operator|=
name|field
operator|.
name|stringValue
argument_list|()
expr_stmt|;
name|weights
index|[
name|i
index|]
operator|=
name|between
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
expr_stmt|;
block|}
name|docs
operator|.
name|close
argument_list|()
expr_stmt|;
specifier|final
name|InputIterator
name|primaryIter
init|=
operator|new
name|InputIterator
argument_list|()
block|{
name|int
name|index
init|=
literal|0
decl_stmt|;
name|long
name|currentWeight
init|=
operator|-
literal|1
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Comparator
argument_list|<
name|BytesRef
argument_list|>
name|getComparator
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|BytesRef
name|next
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|index
operator|<
name|titles
operator|.
name|length
condition|)
block|{
name|currentWeight
operator|=
name|weights
index|[
name|index
index|]
expr_stmt|;
return|return
operator|new
name|BytesRef
argument_list|(
name|titles
index|[
name|index
operator|++
index|]
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|weight
parameter_list|()
block|{
return|return
name|currentWeight
return|;
block|}
annotation|@
name|Override
specifier|public
name|BytesRef
name|payload
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasPayloads
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
decl_stmt|;
name|InputIterator
name|iter
decl_stmt|;
if|if
condition|(
name|usePayloads
condition|)
block|{
name|iter
operator|=
operator|new
name|InputIterator
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|weight
parameter_list|()
block|{
return|return
name|primaryIter
operator|.
name|weight
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Comparator
argument_list|<
name|BytesRef
argument_list|>
name|getComparator
parameter_list|()
block|{
return|return
name|primaryIter
operator|.
name|getComparator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|BytesRef
name|next
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|primaryIter
operator|.
name|next
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|BytesRef
name|payload
parameter_list|()
block|{
return|return
operator|new
name|BytesRef
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|weight
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasPayloads
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
expr_stmt|;
block|}
else|else
block|{
name|iter
operator|=
name|primaryIter
expr_stmt|;
block|}
name|reference
operator|.
name|build
argument_list|(
name|iter
argument_list|)
expr_stmt|;
name|PostingsFormatProvider
name|provider
init|=
operator|new
name|PreBuiltPostingsFormatProvider
argument_list|(
operator|new
name|ElasticSearch090PostingsFormat
argument_list|()
argument_list|)
decl_stmt|;
name|NamedAnalyzer
name|namedAnalzyer
init|=
operator|new
name|NamedAnalyzer
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|(
name|TEST_VERSION_CURRENT
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|CompletionFieldMapper
name|mapper
init|=
operator|new
name|CompletionFieldMapper
argument_list|(
operator|new
name|Names
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|namedAnalzyer
argument_list|,
name|namedAnalzyer
argument_list|,
name|provider
argument_list|,
literal|null
argument_list|,
name|usePayloads
argument_list|,
name|preserveSeparators
argument_list|,
name|preservePositionIncrements
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|Lookup
name|buildAnalyzingLookup
init|=
name|buildAnalyzingLookup
argument_list|(
name|mapper
argument_list|,
name|titles
argument_list|,
name|titles
argument_list|,
name|weights
argument_list|)
decl_stmt|;
name|Field
name|field
init|=
name|buildAnalyzingLookup
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredField
argument_list|(
literal|"maxAnalyzedPathsForOneInput"
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Field
name|refField
init|=
name|reference
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredField
argument_list|(
literal|"maxAnalyzedPathsForOneInput"
argument_list|)
decl_stmt|;
name|refField
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|refField
operator|.
name|get
argument_list|(
name|reference
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|field
operator|.
name|get
argument_list|(
name|buildAnalyzingLookup
argument_list|)
argument_list|)
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
name|titles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|res
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
specifier|final
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|SuggestUtils
operator|.
name|analyze
argument_list|(
name|namedAnalzyer
operator|.
name|tokenStream
argument_list|(
literal|"foo"
argument_list|,
name|titles
index|[
name|i
index|]
argument_list|)
argument_list|,
operator|new
name|SuggestUtils
operator|.
name|TokenConsumer
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|nextToken
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|builder
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|this
operator|.
name|charTermAttr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|String
name|firstTerm
init|=
name|builder
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|prefix
init|=
name|firstTerm
operator|.
name|isEmpty
argument_list|()
condition|?
literal|""
else|:
name|firstTerm
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|between
argument_list|(
literal|1
argument_list|,
name|firstTerm
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|LookupResult
argument_list|>
name|refLookup
init|=
name|reference
operator|.
name|lookup
argument_list|(
name|prefix
argument_list|,
literal|false
argument_list|,
name|res
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|LookupResult
argument_list|>
name|lookup
init|=
name|buildAnalyzingLookup
operator|.
name|lookup
argument_list|(
name|prefix
argument_list|,
literal|false
argument_list|,
name|res
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|refLookup
operator|.
name|toString
argument_list|()
argument_list|,
name|lookup
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|refLookup
operator|.
name|size
argument_list|()
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
name|refLookup
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|lookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|key
argument_list|,
name|equalTo
argument_list|(
name|refLookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"prefix: "
operator|+
name|prefix
operator|+
literal|" "
operator|+
name|j
operator|+
literal|" -- missmatch cost: "
operator|+
name|lookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|key
operator|+
literal|" - "
operator|+
name|lookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|value
operator|+
literal|" | "
operator|+
name|refLookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|key
operator|+
literal|" - "
operator|+
name|refLookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|value
argument_list|,
name|lookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|value
argument_list|,
name|equalTo
argument_list|(
name|refLookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|lookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|payload
argument_list|,
name|equalTo
argument_list|(
name|refLookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|payload
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|usePayloads
condition|)
block|{
name|assertThat
argument_list|(
name|lookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|payload
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|lookup
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|buildAnalyzingLookup
specifier|public
name|Lookup
name|buildAnalyzingLookup
parameter_list|(
specifier|final
name|CompletionFieldMapper
name|mapper
parameter_list|,
name|String
index|[]
name|terms
parameter_list|,
name|String
index|[]
name|surfaces
parameter_list|,
name|long
index|[]
name|weights
parameter_list|)
throws|throws
name|IOException
block|{
name|RAMDirectory
name|dir
init|=
operator|new
name|RAMDirectory
argument_list|()
decl_stmt|;
name|FilterCodec
name|filterCodec
init|=
operator|new
name|FilterCodec
argument_list|(
literal|"filtered"
argument_list|,
name|Codec
operator|.
name|getDefault
argument_list|()
argument_list|)
block|{
specifier|public
name|PostingsFormat
name|postingsFormat
parameter_list|()
block|{
return|return
name|mapper
operator|.
name|postingsFormatProvider
argument_list|()
operator|.
name|get
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|IndexWriterConfig
name|indexWriterConfig
init|=
operator|new
name|IndexWriterConfig
argument_list|(
name|TEST_VERSION_CURRENT
argument_list|,
name|mapper
operator|.
name|indexAnalyzer
argument_list|()
argument_list|)
decl_stmt|;
name|indexWriterConfig
operator|.
name|setCodec
argument_list|(
name|filterCodec
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
name|indexWriterConfig
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
name|weights
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|BytesRef
name|payload
init|=
name|mapper
operator|.
name|buildPayload
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|surfaces
index|[
name|i
index|]
argument_list|)
argument_list|,
name|weights
index|[
name|i
index|]
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|weights
index|[
name|i
index|]
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|mapper
operator|.
name|getCompletionField
argument_list|(
name|terms
index|[
name|i
index|]
argument_list|,
name|payload
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
block|}
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|writer
operator|.
name|forceMerge
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|writer
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
name|writer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|reader
operator|.
name|leaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|reader
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|reader
argument_list|()
operator|.
name|numDocs
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|weights
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|AtomicReaderContext
name|atomicReaderContext
init|=
name|reader
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Terms
name|luceneTerms
init|=
name|atomicReaderContext
operator|.
name|reader
argument_list|()
operator|.
name|terms
argument_list|(
name|mapper
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|Lookup
name|lookup
init|=
operator|(
operator|(
name|Completion090PostingsFormat
operator|.
name|CompletionTerms
operator|)
name|luceneTerms
operator|)
operator|.
name|getLookup
argument_list|(
name|mapper
argument_list|,
operator|new
name|CompletionSuggestionContext
argument_list|(
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|reader
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
return|return
name|lookup
return|;
block|}
annotation|@
name|Test
DECL|method|testNoDocs
specifier|public
name|void
name|testNoDocs
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalyzingCompletionLookupProvider
name|provider
init|=
operator|new
name|AnalyzingCompletionLookupProvider
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|RAMDirectory
name|dir
init|=
operator|new
name|RAMDirectory
argument_list|()
decl_stmt|;
name|IndexOutput
name|output
init|=
name|dir
operator|.
name|createOutput
argument_list|(
literal|"foo.txt"
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
decl_stmt|;
name|FieldsConsumer
name|consumer
init|=
name|provider
operator|.
name|consumer
argument_list|(
name|output
argument_list|)
decl_stmt|;
name|FieldInfo
name|fieldInfo
init|=
operator|new
name|FieldInfo
argument_list|(
literal|"foo"
argument_list|,
literal|true
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|IndexOptions
operator|.
name|DOCS_AND_FREQS_AND_POSITIONS
argument_list|,
name|DocValuesType
operator|.
name|SORTED
argument_list|,
name|DocValuesType
operator|.
name|BINARY
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|TermsConsumer
name|addField
init|=
name|consumer
operator|.
name|addField
argument_list|(
name|fieldInfo
argument_list|)
decl_stmt|;
name|addField
operator|.
name|finish
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|close
argument_list|()
expr_stmt|;
name|output
operator|.
name|close
argument_list|()
expr_stmt|;
name|IndexInput
name|input
init|=
name|dir
operator|.
name|openInput
argument_list|(
literal|"foo.txt"
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
decl_stmt|;
name|LookupFactory
name|load
init|=
name|provider
operator|.
name|load
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|PostingsFormatProvider
name|format
init|=
operator|new
name|PreBuiltPostingsFormatProvider
argument_list|(
operator|new
name|ElasticSearch090PostingsFormat
argument_list|()
argument_list|)
decl_stmt|;
name|NamedAnalyzer
name|analyzer
init|=
operator|new
name|NamedAnalyzer
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|StandardAnalyzer
argument_list|(
name|TEST_VERSION_CURRENT
argument_list|)
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|load
operator|.
name|getLookup
argument_list|(
operator|new
name|CompletionFieldMapper
argument_list|(
operator|new
name|Names
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|analyzer
argument_list|,
name|analyzer
argument_list|,
name|format
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
operator|new
name|CompletionSuggestionContext
argument_list|(
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// TODO ADD more unittests
block|}
end_class

end_unit

