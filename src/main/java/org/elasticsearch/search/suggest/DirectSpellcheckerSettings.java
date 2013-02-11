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
name|util
operator|.
name|automaton
operator|.
name|LevenshteinAutomata
import|;
end_import

begin_class
DECL|class|DirectSpellcheckerSettings
specifier|public
class|class
name|DirectSpellcheckerSettings
block|{
DECL|field|suggestMode
specifier|private
name|SuggestMode
name|suggestMode
init|=
name|SuggestMode
operator|.
name|SUGGEST_WHEN_NOT_IN_INDEX
decl_stmt|;
DECL|field|accuracy
specifier|private
name|float
name|accuracy
init|=
literal|0.5f
decl_stmt|;
DECL|field|sort
specifier|private
name|Suggest
operator|.
name|Suggestion
operator|.
name|Sort
name|sort
init|=
name|Suggest
operator|.
name|Suggestion
operator|.
name|Sort
operator|.
name|SCORE
decl_stmt|;
DECL|field|stringDistance
specifier|private
name|StringDistance
name|stringDistance
init|=
name|DirectSpellChecker
operator|.
name|INTERNAL_LEVENSHTEIN
decl_stmt|;
DECL|field|maxEdits
specifier|private
name|int
name|maxEdits
init|=
name|LevenshteinAutomata
operator|.
name|MAXIMUM_SUPPORTED_DISTANCE
decl_stmt|;
DECL|field|maxInspections
specifier|private
name|int
name|maxInspections
init|=
literal|5
decl_stmt|;
DECL|field|maxTermFreq
specifier|private
name|float
name|maxTermFreq
init|=
literal|0.01f
decl_stmt|;
DECL|field|prefixLength
specifier|private
name|int
name|prefixLength
init|=
literal|1
decl_stmt|;
DECL|field|minWordLength
specifier|private
name|int
name|minWordLength
init|=
literal|4
decl_stmt|;
DECL|field|minDocFreq
specifier|private
name|float
name|minDocFreq
init|=
literal|0f
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
name|Suggest
operator|.
name|Suggestion
operator|.
name|Sort
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
name|Suggest
operator|.
name|Suggestion
operator|.
name|Sort
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
DECL|method|minQueryLength
specifier|public
name|void
name|minQueryLength
parameter_list|(
name|int
name|minQueryLength
parameter_list|)
block|{
name|this
operator|.
name|minWordLength
operator|=
name|minQueryLength
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
block|}
end_class

end_unit

