begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|StringDistance
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
name|SuggestMode
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
name|automaton
operator|.
name|LevenshteinAutomata
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

begin_class
DECL|class|DirectSpellcheckerSettings
specifier|public
class|class
name|DirectSpellcheckerSettings
block|{
comment|// NB: If this changes, make sure to change the default in TermBuilderSuggester
DECL|field|DEFAULT_SUGGEST_MODE
specifier|public
specifier|static
name|SuggestMode
name|DEFAULT_SUGGEST_MODE
init|=
name|SuggestMode
operator|.
name|SUGGEST_WHEN_NOT_IN_INDEX
decl_stmt|;
DECL|field|DEFAULT_ACCURACY
specifier|public
specifier|static
name|float
name|DEFAULT_ACCURACY
init|=
literal|0.5f
decl_stmt|;
DECL|field|DEFAULT_SORT
specifier|public
specifier|static
name|SortBy
name|DEFAULT_SORT
init|=
name|SortBy
operator|.
name|SCORE
decl_stmt|;
comment|// NB: If this changes, make sure to change the default in TermBuilderSuggester
DECL|field|DEFAULT_STRING_DISTANCE
specifier|public
specifier|static
name|StringDistance
name|DEFAULT_STRING_DISTANCE
init|=
name|DirectSpellChecker
operator|.
name|INTERNAL_LEVENSHTEIN
decl_stmt|;
DECL|field|DEFAULT_MAX_EDITS
specifier|public
specifier|static
name|int
name|DEFAULT_MAX_EDITS
init|=
name|LevenshteinAutomata
operator|.
name|MAXIMUM_SUPPORTED_DISTANCE
decl_stmt|;
DECL|field|DEFAULT_MAX_INSPECTIONS
specifier|public
specifier|static
name|int
name|DEFAULT_MAX_INSPECTIONS
init|=
literal|5
decl_stmt|;
DECL|field|DEFAULT_MAX_TERM_FREQ
specifier|public
specifier|static
name|float
name|DEFAULT_MAX_TERM_FREQ
init|=
literal|0.01f
decl_stmt|;
DECL|field|DEFAULT_PREFIX_LENGTH
specifier|public
specifier|static
name|int
name|DEFAULT_PREFIX_LENGTH
init|=
literal|1
decl_stmt|;
DECL|field|DEFAULT_MIN_WORD_LENGTH
specifier|public
specifier|static
name|int
name|DEFAULT_MIN_WORD_LENGTH
init|=
literal|4
decl_stmt|;
DECL|field|DEFAULT_MIN_DOC_FREQ
specifier|public
specifier|static
name|float
name|DEFAULT_MIN_DOC_FREQ
init|=
literal|0f
decl_stmt|;
DECL|field|suggestMode
specifier|private
name|SuggestMode
name|suggestMode
init|=
name|DEFAULT_SUGGEST_MODE
decl_stmt|;
DECL|field|accuracy
specifier|private
name|float
name|accuracy
init|=
name|DEFAULT_ACCURACY
decl_stmt|;
DECL|field|sort
specifier|private
name|SortBy
name|sort
init|=
name|DEFAULT_SORT
decl_stmt|;
DECL|field|stringDistance
specifier|private
name|StringDistance
name|stringDistance
init|=
name|DEFAULT_STRING_DISTANCE
decl_stmt|;
DECL|field|maxEdits
specifier|private
name|int
name|maxEdits
init|=
name|DEFAULT_MAX_EDITS
decl_stmt|;
DECL|field|maxInspections
specifier|private
name|int
name|maxInspections
init|=
name|DEFAULT_MAX_INSPECTIONS
decl_stmt|;
DECL|field|maxTermFreq
specifier|private
name|float
name|maxTermFreq
init|=
name|DEFAULT_MAX_TERM_FREQ
decl_stmt|;
DECL|field|prefixLength
specifier|private
name|int
name|prefixLength
init|=
name|DEFAULT_PREFIX_LENGTH
decl_stmt|;
DECL|field|minWordLength
specifier|private
name|int
name|minWordLength
init|=
name|DEFAULT_MIN_WORD_LENGTH
decl_stmt|;
DECL|field|minDocFreq
specifier|private
name|float
name|minDocFreq
init|=
name|DEFAULT_MIN_DOC_FREQ
decl_stmt|;
DECL|field|LUCENE_FREQUENCY
specifier|private
specifier|static
specifier|final
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
DECL|field|SCORE_COMPARATOR
specifier|private
specifier|static
specifier|final
name|Comparator
argument_list|<
name|SuggestWord
argument_list|>
name|SCORE_COMPARATOR
init|=
name|SuggestWordQueue
operator|.
name|DEFAULT_COMPARATOR
decl_stmt|;
DECL|method|suggestMode
specifier|public
name|SuggestMode
name|suggestMode
parameter_list|()
block|{
return|return
name|suggestMode
return|;
block|}
DECL|method|suggestMode
specifier|public
name|void
name|suggestMode
parameter_list|(
name|SuggestMode
name|suggestMode
parameter_list|)
block|{
name|this
operator|.
name|suggestMode
operator|=
name|suggestMode
expr_stmt|;
block|}
DECL|method|accuracy
specifier|public
name|float
name|accuracy
parameter_list|()
block|{
return|return
name|accuracy
return|;
block|}
DECL|method|accuracy
specifier|public
name|void
name|accuracy
parameter_list|(
name|float
name|accuracy
parameter_list|)
block|{
name|this
operator|.
name|accuracy
operator|=
name|accuracy
expr_stmt|;
block|}
DECL|method|sort
specifier|public
name|SortBy
name|sort
parameter_list|()
block|{
return|return
name|sort
return|;
block|}
DECL|method|sort
specifier|public
name|void
name|sort
parameter_list|(
name|SortBy
name|sort
parameter_list|)
block|{
name|this
operator|.
name|sort
operator|=
name|sort
expr_stmt|;
block|}
DECL|method|stringDistance
specifier|public
name|StringDistance
name|stringDistance
parameter_list|()
block|{
return|return
name|stringDistance
return|;
block|}
DECL|method|stringDistance
specifier|public
name|void
name|stringDistance
parameter_list|(
name|StringDistance
name|distance
parameter_list|)
block|{
name|this
operator|.
name|stringDistance
operator|=
name|distance
expr_stmt|;
block|}
DECL|method|maxEdits
specifier|public
name|int
name|maxEdits
parameter_list|()
block|{
return|return
name|maxEdits
return|;
block|}
DECL|method|maxEdits
specifier|public
name|void
name|maxEdits
parameter_list|(
name|int
name|maxEdits
parameter_list|)
block|{
name|this
operator|.
name|maxEdits
operator|=
name|maxEdits
expr_stmt|;
block|}
DECL|method|maxInspections
specifier|public
name|int
name|maxInspections
parameter_list|()
block|{
return|return
name|maxInspections
return|;
block|}
DECL|method|maxInspections
specifier|public
name|void
name|maxInspections
parameter_list|(
name|int
name|maxInspections
parameter_list|)
block|{
name|this
operator|.
name|maxInspections
operator|=
name|maxInspections
expr_stmt|;
block|}
DECL|method|maxTermFreq
specifier|public
name|float
name|maxTermFreq
parameter_list|()
block|{
return|return
name|maxTermFreq
return|;
block|}
DECL|method|maxTermFreq
specifier|public
name|void
name|maxTermFreq
parameter_list|(
name|float
name|maxTermFreq
parameter_list|)
block|{
name|this
operator|.
name|maxTermFreq
operator|=
name|maxTermFreq
expr_stmt|;
block|}
DECL|method|prefixLength
specifier|public
name|int
name|prefixLength
parameter_list|()
block|{
return|return
name|prefixLength
return|;
block|}
DECL|method|prefixLength
specifier|public
name|void
name|prefixLength
parameter_list|(
name|int
name|prefixLength
parameter_list|)
block|{
name|this
operator|.
name|prefixLength
operator|=
name|prefixLength
expr_stmt|;
block|}
DECL|method|minWordLength
specifier|public
name|int
name|minWordLength
parameter_list|()
block|{
return|return
name|minWordLength
return|;
block|}
DECL|method|minWordLength
specifier|public
name|void
name|minWordLength
parameter_list|(
name|int
name|minWordLength
parameter_list|)
block|{
name|this
operator|.
name|minWordLength
operator|=
name|minWordLength
expr_stmt|;
block|}
DECL|method|minDocFreq
specifier|public
name|float
name|minDocFreq
parameter_list|()
block|{
return|return
name|minDocFreq
return|;
block|}
DECL|method|minDocFreq
specifier|public
name|void
name|minDocFreq
parameter_list|(
name|float
name|minDocFreq
parameter_list|)
block|{
name|this
operator|.
name|minDocFreq
operator|=
name|minDocFreq
expr_stmt|;
block|}
DECL|method|createDirectSpellChecker
specifier|public
name|DirectSpellChecker
name|createDirectSpellChecker
parameter_list|()
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
name|sort
argument_list|()
condition|)
block|{
case|case
name|SCORE
case|:
name|comparator
operator|=
name|SCORE_COMPARATOR
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
name|IllegalArgumentException
argument_list|(
literal|"Illegal suggest sort: "
operator|+
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
name|stringDistance
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setMaxEdits
argument_list|(
name|maxEdits
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setMaxInspections
argument_list|(
name|maxInspections
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setMaxQueryFrequency
argument_list|(
name|maxTermFreq
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setMinPrefix
argument_list|(
name|prefixLength
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setMinQueryLength
argument_list|(
name|minWordLength
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setThresholdFrequency
argument_list|(
name|minDocFreq
argument_list|()
argument_list|)
expr_stmt|;
name|directSpellChecker
operator|.
name|setLowerCaseTerms
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|directSpellChecker
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
literal|"suggestMode="
operator|+
name|suggestMode
operator|+
literal|",sort="
operator|+
name|sort
operator|+
literal|",stringDistance="
operator|+
name|stringDistance
operator|+
literal|",accuracy="
operator|+
name|accuracy
operator|+
literal|",maxEdits="
operator|+
name|maxEdits
operator|+
literal|",maxInspections="
operator|+
name|maxInspections
operator|+
literal|",maxTermFreq="
operator|+
name|maxTermFreq
operator|+
literal|",prefixLength="
operator|+
name|prefixLength
operator|+
literal|",minWordLength="
operator|+
name|minWordLength
operator|+
literal|",minDocFreq="
operator|+
name|minDocFreq
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

