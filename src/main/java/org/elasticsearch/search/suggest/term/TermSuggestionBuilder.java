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
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
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
name|SuggestBuilder
operator|.
name|SuggestionBuilder
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

begin_comment
comment|/**  * Defines the actual suggest command. Each command uses the global options  * unless defined in the suggestion itself. All options are the same as the  * global options, but are only applicable for this suggestion.  */
end_comment

begin_class
DECL|class|TermSuggestionBuilder
specifier|public
class|class
name|TermSuggestionBuilder
extends|extends
name|SuggestionBuilder
argument_list|<
name|TermSuggestionBuilder
argument_list|>
block|{
DECL|field|suggestMode
specifier|private
name|String
name|suggestMode
decl_stmt|;
DECL|field|accuracy
specifier|private
name|Float
name|accuracy
decl_stmt|;
DECL|field|sort
specifier|private
name|String
name|sort
decl_stmt|;
DECL|field|stringDistance
specifier|private
name|String
name|stringDistance
decl_stmt|;
DECL|field|maxEdits
specifier|private
name|Integer
name|maxEdits
decl_stmt|;
DECL|field|maxInspections
specifier|private
name|Integer
name|maxInspections
decl_stmt|;
DECL|field|maxTermFreq
specifier|private
name|Float
name|maxTermFreq
decl_stmt|;
DECL|field|prefixLength
specifier|private
name|Integer
name|prefixLength
decl_stmt|;
DECL|field|minWordLength
specifier|private
name|Integer
name|minWordLength
decl_stmt|;
DECL|field|minDocFreq
specifier|private
name|Float
name|minDocFreq
decl_stmt|;
comment|/**      * @param name      *            The name of this suggestion. This is a required parameter.      */
DECL|method|TermSuggestionBuilder
specifier|public
name|TermSuggestionBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
literal|"term"
argument_list|)
expr_stmt|;
block|}
comment|/**      * The global suggest mode controls what suggested terms are included or      * controls for what suggest text tokens, terms should be suggested for.      * Three possible values can be specified:      *<ol>      *<li><code>missing</code> - Only suggest terms in the suggest text that      * aren't in the index. This is the default.      *<li><code>popular</code> - Only suggest terms that occur in more docs      * then the original suggest text term.      *<li><code>always</code> - Suggest any matching suggest terms based on      * tokens in the suggest text.      *</ol>      */
DECL|method|suggestMode
specifier|public
name|TermSuggestionBuilder
name|suggestMode
parameter_list|(
name|String
name|suggestMode
parameter_list|)
block|{
name|this
operator|.
name|suggestMode
operator|=
name|suggestMode
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * s how similar the suggested terms at least need to be compared to the      * original suggest text tokens. A value between 0 and 1 can be specified.      * This value will be compared to the string distance result of each      * candidate spelling correction.      *<p/>      * Default is<tt>0.5</tt>      */
DECL|method|setAccuracy
specifier|public
name|TermSuggestionBuilder
name|setAccuracy
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
return|return
name|this
return|;
block|}
comment|/**      * Sets how to sort the suggest terms per suggest text token. Two possible      * values:      *<ol>      *<li><code>score</code> - Sort should first be based on score, then      * document frequency and then the term itself.      *<li><code>frequency</code> - Sort should first be based on document      * frequency, then scotr and then the term itself.      *</ol>      *<p/>      * What the score is depends on the suggester being used.      */
DECL|method|sort
specifier|public
name|TermSuggestionBuilder
name|sort
parameter_list|(
name|String
name|sort
parameter_list|)
block|{
name|this
operator|.
name|sort
operator|=
name|sort
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets what string distance implementation to use for comparing how similar      * suggested terms are. Four possible values can be specified:      *<ol>      *<li><code>internal</code> - This is the default and is based on      *<code>damerau_levenshtein</code>, but highly optimized for comparing      * string distance for terms inside the index.      *<li><code>damerau_levenshtein</code> - String distance algorithm based on      * Damerau-Levenshtein algorithm.      *<li><code>levenstein</code> - String distance algorithm based on      * Levenstein edit distance algorithm.      *<li><code>jarowinkler</code> - String distance algorithm based on      * Jaro-Winkler algorithm.      *<li><code>ngram</code> - String distance algorithm based on character      * n-grams.      *</ol>      */
DECL|method|stringDistance
specifier|public
name|TermSuggestionBuilder
name|stringDistance
parameter_list|(
name|String
name|stringDistance
parameter_list|)
block|{
name|this
operator|.
name|stringDistance
operator|=
name|stringDistance
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the maximum edit distance candidate suggestions can have in order to      * be considered as a suggestion. Can only be a value between 1 and 2. Any      * other value result in an bad request error being thrown. Defaults to      *<tt>2</tt>.      */
DECL|method|maxEdits
specifier|public
name|TermSuggestionBuilder
name|maxEdits
parameter_list|(
name|Integer
name|maxEdits
parameter_list|)
block|{
name|this
operator|.
name|maxEdits
operator|=
name|maxEdits
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A factor that is used to multiply with the size in order to inspect more      * candidate suggestions. Can improve accuracy at the cost of performance.      * Defaults to<tt>5</tt>.      */
DECL|method|maxInspections
specifier|public
name|TermSuggestionBuilder
name|maxInspections
parameter_list|(
name|Integer
name|maxInspections
parameter_list|)
block|{
name|this
operator|.
name|maxInspections
operator|=
name|maxInspections
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets a maximum threshold in number of documents a suggest text token can      * exist in order to be corrected. Can be a relative percentage number (e.g      * 0.4) or an absolute number to represent document frequencies. If an value      * higher than 1 is specified then fractional can not be specified. Defaults      * to<tt>0.01</tt>.      *<p/>      * This can be used to exclude high frequency terms from being suggested.      * High frequency terms are usually spelled correctly on top of this this      * also improves the suggest performance.      */
DECL|method|maxTermFreq
specifier|public
name|TermSuggestionBuilder
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
return|return
name|this
return|;
block|}
comment|/**      * Sets the number of minimal prefix characters that must match in order be      * a candidate suggestion. Defaults to 1. Increasing this number improves      * suggest performance. Usually misspellings don't occur in the beginning of      * terms.      */
DECL|method|prefixLength
specifier|public
name|TermSuggestionBuilder
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
return|return
name|this
return|;
block|}
comment|/**      * The minimum length a suggest text term must have in order to be      * corrected. Defaults to<tt>4</tt>.      */
DECL|method|minWordLength
specifier|public
name|TermSuggestionBuilder
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
return|return
name|this
return|;
block|}
comment|/**      * Sets a minimal threshold in number of documents a suggested term should      * appear in. This can be specified as an absolute number or as a relative      * percentage of number of documents. This can improve quality by only      * suggesting high frequency terms. Defaults to 0f and is not enabled. If a      * value higher than 1 is specified then the number cannot be fractional.      */
DECL|method|minDocFreq
specifier|public
name|TermSuggestionBuilder
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
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|innerToXContent
specifier|public
name|XContentBuilder
name|innerToXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|suggestMode
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"suggest_mode"
argument_list|,
name|suggestMode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|accuracy
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"accuracy"
argument_list|,
name|accuracy
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sort
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"sort"
argument_list|,
name|sort
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|stringDistance
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"string_distance"
argument_list|,
name|stringDistance
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|maxEdits
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"max_edits"
argument_list|,
name|maxEdits
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|maxInspections
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"max_inspections"
argument_list|,
name|maxInspections
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|maxTermFreq
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"max_term_freq"
argument_list|,
name|maxTermFreq
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|prefixLength
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"prefix_length"
argument_list|,
name|prefixLength
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minWordLength
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"min_word_length"
argument_list|,
name|minWordLength
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minDocFreq
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"min_doc_freq"
argument_list|,
name|minDocFreq
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

