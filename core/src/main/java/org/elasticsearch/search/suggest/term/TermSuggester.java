begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.term
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|term
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
name|spell
operator|.
name|DirectSpellChecker
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
name|spell
operator|.
name|SuggestWord
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
name|BytesRefBuilder
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
name|CharsRefBuilder
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|text
operator|.
name|Text
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
name|query
operator|.
name|QueryParseContext
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
name|SuggestUtils
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
name|Suggester
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
name|SuggestionBuilder
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
name|SuggestionSearchContext
operator|.
name|SuggestionContext
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
name|phrase
operator|.
name|DirectCandidateGenerator
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
name|List
import|;
end_import

begin_class
DECL|class|TermSuggester
specifier|public
specifier|final
class|class
name|TermSuggester
extends|extends
name|Suggester
argument_list|<
name|TermSuggestionContext
argument_list|>
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|TermSuggester
name|INSTANCE
init|=
operator|new
name|TermSuggester
argument_list|()
decl_stmt|;
DECL|method|TermSuggester
specifier|private
name|TermSuggester
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|innerExecute
specifier|public
name|TermSuggestion
name|innerExecute
parameter_list|(
name|String
name|name
parameter_list|,
name|TermSuggestionContext
name|suggestion
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|,
name|CharsRefBuilder
name|spare
parameter_list|)
throws|throws
name|IOException
block|{
name|DirectSpellChecker
name|directSpellChecker
init|=
name|suggestion
operator|.
name|getDirectSpellCheckerSettings
argument_list|()
operator|.
name|getDirectSpellChecker
argument_list|()
decl_stmt|;
specifier|final
name|IndexReader
name|indexReader
init|=
name|searcher
operator|.
name|getIndexReader
argument_list|()
decl_stmt|;
name|TermSuggestion
name|response
init|=
operator|new
name|TermSuggestion
argument_list|(
name|name
argument_list|,
name|suggestion
operator|.
name|getSize
argument_list|()
argument_list|,
name|suggestion
operator|.
name|getDirectSpellCheckerSettings
argument_list|()
operator|.
name|sort
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Token
argument_list|>
name|tokens
init|=
name|queryTerms
argument_list|(
name|suggestion
argument_list|,
name|spare
argument_list|)
decl_stmt|;
for|for
control|(
name|Token
name|token
range|:
name|tokens
control|)
block|{
comment|// TODO: Extend DirectSpellChecker in 4.1, to get the raw suggested words as BytesRef
name|SuggestWord
index|[]
name|suggestedWords
init|=
name|directSpellChecker
operator|.
name|suggestSimilar
argument_list|(
name|token
operator|.
name|term
argument_list|,
name|suggestion
operator|.
name|getShardSize
argument_list|()
argument_list|,
name|indexReader
argument_list|,
name|suggestion
operator|.
name|getDirectSpellCheckerSettings
argument_list|()
operator|.
name|suggestMode
argument_list|()
argument_list|)
decl_stmt|;
name|Text
name|key
init|=
operator|new
name|Text
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|token
operator|.
name|term
operator|.
name|bytes
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|TermSuggestion
operator|.
name|Entry
name|resultEntry
init|=
operator|new
name|TermSuggestion
operator|.
name|Entry
argument_list|(
name|key
argument_list|,
name|token
operator|.
name|startOffset
argument_list|,
name|token
operator|.
name|endOffset
operator|-
name|token
operator|.
name|startOffset
argument_list|)
decl_stmt|;
for|for
control|(
name|SuggestWord
name|suggestWord
range|:
name|suggestedWords
control|)
block|{
name|Text
name|word
init|=
operator|new
name|Text
argument_list|(
name|suggestWord
operator|.
name|string
argument_list|)
decl_stmt|;
name|resultEntry
operator|.
name|addOption
argument_list|(
operator|new
name|TermSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|(
name|word
argument_list|,
name|suggestWord
operator|.
name|freq
argument_list|,
name|suggestWord
operator|.
name|score
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|response
operator|.
name|addTerm
argument_list|(
name|resultEntry
argument_list|)
expr_stmt|;
block|}
return|return
name|response
return|;
block|}
DECL|method|queryTerms
specifier|private
name|List
argument_list|<
name|Token
argument_list|>
name|queryTerms
parameter_list|(
name|SuggestionContext
name|suggestion
parameter_list|,
name|CharsRefBuilder
name|spare
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|List
argument_list|<
name|Token
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|String
name|field
init|=
name|suggestion
operator|.
name|getField
argument_list|()
decl_stmt|;
name|DirectCandidateGenerator
operator|.
name|analyze
argument_list|(
name|suggestion
operator|.
name|getAnalyzer
argument_list|()
argument_list|,
name|suggestion
operator|.
name|getText
argument_list|()
argument_list|,
name|field
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
block|{
name|Term
name|term
init|=
operator|new
name|Term
argument_list|(
name|field
argument_list|,
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|fillBytesRef
argument_list|(
operator|new
name|BytesRefBuilder
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|result
operator|.
name|add
argument_list|(
operator|new
name|Token
argument_list|(
name|term
argument_list|,
name|offsetAttr
operator|.
name|startOffset
argument_list|()
argument_list|,
name|offsetAttr
operator|.
name|endOffset
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|spare
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|innerFromXContent
specifier|public
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
name|innerFromXContent
parameter_list|(
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|TermSuggestionBuilder
operator|.
name|innerFromXContent
argument_list|(
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
name|read
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TermSuggestionBuilder
argument_list|(
name|in
argument_list|)
return|;
block|}
DECL|class|Token
specifier|private
specifier|static
class|class
name|Token
block|{
DECL|field|term
specifier|public
specifier|final
name|Term
name|term
decl_stmt|;
DECL|field|startOffset
specifier|public
specifier|final
name|int
name|startOffset
decl_stmt|;
DECL|field|endOffset
specifier|public
specifier|final
name|int
name|endOffset
decl_stmt|;
DECL|method|Token
specifier|private
name|Token
parameter_list|(
name|Term
name|term
parameter_list|,
name|int
name|startOffset
parameter_list|,
name|int
name|endOffset
parameter_list|)
block|{
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
name|this
operator|.
name|startOffset
operator|=
name|startOffset
expr_stmt|;
name|this
operator|.
name|endOffset
operator|=
name|endOffset
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

