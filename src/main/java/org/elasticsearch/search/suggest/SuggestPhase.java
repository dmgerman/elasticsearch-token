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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|tokenattributes
operator|.
name|OffsetAttribute
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
name|TermToBytesRefAttribute
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
name|search
operator|.
name|spell
operator|.
name|SuggestWordFrequencyComparator
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
name|SuggestWordQueue
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
name|CharsRef
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
name|UnicodeUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|component
operator|.
name|AbstractComponent
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
name|inject
operator|.
name|Inject
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
name|FastCharArrayReader
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
name|common
operator|.
name|text
operator|.
name|BytesText
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
name|StringText
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
name|search
operator|.
name|SearchParseElement
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
name|SearchPhase
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
name|internal
operator|.
name|SearchContext
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
name|Comparator
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
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|Suggest
operator|.
name|Suggestion
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|SuggestPhase
specifier|public
class|class
name|SuggestPhase
extends|extends
name|AbstractComponent
implements|implements
name|SearchPhase
block|{
annotation|@
name|Inject
DECL|method|SuggestPhase
specifier|public
name|SuggestPhase
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parseElements
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|SearchParseElement
argument_list|>
name|parseElements
parameter_list|()
block|{
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|SearchParseElement
argument_list|>
name|parseElements
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|parseElements
operator|.
name|put
argument_list|(
literal|"suggest"
argument_list|,
operator|new
name|SuggestParseElement
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|parseElements
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|preProcess
specifier|public
name|void
name|preProcess
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|SearchContext
name|context
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|SuggestionSearchContext
name|suggest
init|=
name|context
operator|.
name|suggest
argument_list|()
decl_stmt|;
if|if
condition|(
name|suggest
operator|==
literal|null
condition|)
block|{
return|return;
block|}
try|try
block|{
name|CharsRef
name|spare
init|=
operator|new
name|CharsRef
argument_list|()
decl_stmt|;
comment|// Maybe add CharsRef to CacheRecycler?
name|List
argument_list|<
name|Suggestion
argument_list|>
name|suggestions
init|=
operator|new
name|ArrayList
argument_list|<
name|Suggestion
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|SuggestionSearchContext
operator|.
name|Suggestion
argument_list|>
name|entry
range|:
name|suggest
operator|.
name|suggestions
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|SuggestionSearchContext
operator|.
name|Suggestion
name|suggestion
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"fuzzy"
operator|.
name|equals
argument_list|(
name|suggestion
operator|.
name|suggester
argument_list|()
argument_list|)
condition|)
block|{
name|suggestions
operator|.
name|add
argument_list|(
name|executeDirectSpellChecker
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|suggestion
argument_list|,
name|context
argument_list|,
name|spare
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Unsupported suggester["
operator|+
name|suggestion
operator|.
name|suggester
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|suggest
argument_list|(
operator|new
name|Suggest
argument_list|(
name|suggestions
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"I/O exception during suggest phase"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|executeDirectSpellChecker
specifier|private
name|Suggestion
name|executeDirectSpellChecker
parameter_list|(
name|String
name|name
parameter_list|,
name|SuggestionSearchContext
operator|.
name|Suggestion
name|suggestion
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|CharsRef
name|spare
parameter_list|)
throws|throws
name|IOException
block|{
name|DirectSpellChecker
name|directSpellChecker
init|=
operator|new
name|DirectSpellChecker
argument_list|()
decl_stmt|;
name|directSpellChecker
operator|.
name|setAccuracy
argument_list|(
name|suggestion
operator|.
name|accuracy
argument_list|()
argument_list|)
expr_stmt|;
name|Comparator
argument_list|<
name|SuggestWord
argument_list|>
name|comparator
decl_stmt|;
switch|switch
condition|(
name|suggestion
operator|.
name|sort
argument_list|()
condition|)
block|{
case|case
name|SCORE
case|:
name|comparator
operator|=
name|SuggestWordQueue
operator|.
name|DEFAULT_COMPARATOR
expr_stmt|;
break|break;
case|case
name|FREQUENCY
case|:
name|comparator
operator|=
name|LUCENE_FREQUENCY
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Illegal suggest sort: "
operator|+
name|suggestion
operator|.
name|sort
argument_list|()
argument_list|)
throw|;
block|}
name|directSpellChecker
operator|.
name|setComparator
argument_list|(
name|comparator
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setDistance
argument_list|(
name|suggestion
operator|.
name|stringDistance
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setLowerCaseTerms
argument_list|(
name|suggestion
operator|.
name|lowerCaseTerms
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setMaxEdits
argument_list|(
name|suggestion
operator|.
name|maxEdits
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setMaxInspections
argument_list|(
name|suggestion
operator|.
name|factor
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setMaxQueryFrequency
argument_list|(
name|suggestion
operator|.
name|maxTermFreq
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setMinPrefix
argument_list|(
name|suggestion
operator|.
name|prefixLength
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setMinQueryLength
argument_list|(
name|suggestion
operator|.
name|minWordLength
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setThresholdFrequency
argument_list|(
name|suggestion
operator|.
name|minDocFreq
argument_list|()
argument_list|)
expr_stmt|;
name|Suggestion
name|response
init|=
operator|new
name|Suggestion
argument_list|(
name|name
argument_list|,
name|suggestion
operator|.
name|size
argument_list|()
argument_list|,
name|suggestion
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
name|IndexReader
name|indexReader
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
decl_stmt|;
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
name|shardSize
argument_list|()
argument_list|,
name|indexReader
argument_list|,
name|suggestion
operator|.
name|suggestMode
argument_list|()
argument_list|)
decl_stmt|;
name|Text
name|key
init|=
operator|new
name|BytesText
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
name|Suggestion
operator|.
name|Term
name|resultTerm
init|=
operator|new
name|Suggestion
operator|.
name|Term
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
name|StringText
argument_list|(
name|suggestWord
operator|.
name|string
argument_list|)
decl_stmt|;
name|resultTerm
operator|.
name|addSuggested
argument_list|(
operator|new
name|Suggestion
operator|.
name|Term
operator|.
name|SuggestedTerm
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
name|resultTerm
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
name|SuggestionSearchContext
operator|.
name|Suggestion
name|suggestion
parameter_list|,
name|CharsRef
name|spare
parameter_list|)
throws|throws
name|IOException
block|{
name|UnicodeUtil
operator|.
name|UTF8toUTF16
argument_list|(
name|suggestion
operator|.
name|text
argument_list|()
argument_list|,
name|spare
argument_list|)
expr_stmt|;
name|TokenStream
name|ts
init|=
name|suggestion
operator|.
name|analyzer
argument_list|()
operator|.
name|tokenStream
argument_list|(
name|suggestion
operator|.
name|field
argument_list|()
argument_list|,
operator|new
name|FastCharArrayReader
argument_list|(
name|spare
operator|.
name|chars
argument_list|,
name|spare
operator|.
name|offset
argument_list|,
name|spare
operator|.
name|length
argument_list|)
argument_list|)
decl_stmt|;
name|ts
operator|.
name|reset
argument_list|()
expr_stmt|;
name|TermToBytesRefAttribute
name|termAtt
init|=
name|ts
operator|.
name|addAttribute
argument_list|(
name|TermToBytesRefAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|OffsetAttribute
name|offsetAtt
init|=
name|ts
operator|.
name|addAttribute
argument_list|(
name|OffsetAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|BytesRef
name|termRef
init|=
name|termAtt
operator|.
name|getBytesRef
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Token
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Token
argument_list|>
argument_list|(
literal|5
argument_list|)
decl_stmt|;
while|while
condition|(
name|ts
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|termAtt
operator|.
name|fillBytesRef
argument_list|()
expr_stmt|;
name|Term
name|term
init|=
operator|new
name|Term
argument_list|(
name|suggestion
operator|.
name|field
argument_list|()
argument_list|,
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|termRef
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
name|offsetAtt
operator|.
name|startOffset
argument_list|()
argument_list|,
name|offsetAtt
operator|.
name|endOffset
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
DECL|field|LUCENE_FREQUENCY
specifier|private
specifier|static
name|Comparator
argument_list|<
name|SuggestWord
argument_list|>
name|LUCENE_FREQUENCY
init|=
operator|new
name|SuggestWordFrequencyComparator
argument_list|()
decl_stmt|;
DECL|field|SCORE
specifier|public
specifier|static
name|Comparator
argument_list|<
name|Suggestion
operator|.
name|Term
operator|.
name|SuggestedTerm
argument_list|>
name|SCORE
init|=
operator|new
name|Score
argument_list|()
decl_stmt|;
DECL|field|FREQUENCY
specifier|public
specifier|static
name|Comparator
argument_list|<
name|Suggestion
operator|.
name|Term
operator|.
name|SuggestedTerm
argument_list|>
name|FREQUENCY
init|=
operator|new
name|Frequency
argument_list|()
decl_stmt|;
comment|// Same behaviour as comparators in suggest module, but for SuggestedWord
comment|// Highest score first, then highest freq first, then lowest term first
DECL|class|Score
specifier|public
specifier|static
class|class
name|Score
implements|implements
name|Comparator
argument_list|<
name|Suggestion
operator|.
name|Term
operator|.
name|SuggestedTerm
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|Suggestion
operator|.
name|Term
operator|.
name|SuggestedTerm
name|first
parameter_list|,
name|Suggestion
operator|.
name|Term
operator|.
name|SuggestedTerm
name|second
parameter_list|)
block|{
comment|// first criteria: the distance
name|int
name|cmp
init|=
name|Float
operator|.
name|compare
argument_list|(
name|second
operator|.
name|getScore
argument_list|()
argument_list|,
name|first
operator|.
name|getScore
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
comment|// second criteria (if first criteria is equal): the popularity
name|cmp
operator|=
name|second
operator|.
name|getFrequency
argument_list|()
operator|-
name|first
operator|.
name|getFrequency
argument_list|()
expr_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
comment|// third criteria: term text
return|return
name|first
operator|.
name|getTerm
argument_list|()
operator|.
name|compareTo
argument_list|(
name|second
operator|.
name|getTerm
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|// Same behaviour as comparators in suggest module, but for SuggestedWord
comment|// Highest freq first, then highest score first, then lowest term first
DECL|class|Frequency
specifier|public
specifier|static
class|class
name|Frequency
implements|implements
name|Comparator
argument_list|<
name|Suggestion
operator|.
name|Term
operator|.
name|SuggestedTerm
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|Suggestion
operator|.
name|Term
operator|.
name|SuggestedTerm
name|first
parameter_list|,
name|Suggestion
operator|.
name|Term
operator|.
name|SuggestedTerm
name|second
parameter_list|)
block|{
comment|// first criteria: the popularity
name|int
name|cmp
init|=
name|second
operator|.
name|getFrequency
argument_list|()
operator|-
name|first
operator|.
name|getFrequency
argument_list|()
decl_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
comment|// second criteria (if first criteria is equal): the distance
name|cmp
operator|=
name|Float
operator|.
name|compare
argument_list|(
name|second
operator|.
name|getScore
argument_list|()
argument_list|,
name|first
operator|.
name|getScore
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
comment|// third criteria: term text
return|return
name|first
operator|.
name|getTerm
argument_list|()
operator|.
name|compareTo
argument_list|(
name|second
operator|.
name|getTerm
argument_list|()
argument_list|)
return|;
block|}
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

