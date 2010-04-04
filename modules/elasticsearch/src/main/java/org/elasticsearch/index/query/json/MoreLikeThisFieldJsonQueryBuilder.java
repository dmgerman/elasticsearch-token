begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|json
package|;
end_package

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
name|QueryBuilderException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|json
operator|.
name|JsonBuilder
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
comment|/**  * A more like this query that runs against a specific field.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|MoreLikeThisFieldJsonQueryBuilder
specifier|public
class|class
name|MoreLikeThisFieldJsonQueryBuilder
extends|extends
name|BaseJsonQueryBuilder
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|likeText
specifier|private
name|String
name|likeText
decl_stmt|;
DECL|field|percentTermsToMatch
specifier|private
name|float
name|percentTermsToMatch
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|minTermFreq
specifier|private
name|int
name|minTermFreq
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|maxQueryTerms
specifier|private
name|int
name|maxQueryTerms
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|stopWords
specifier|private
name|String
index|[]
name|stopWords
init|=
literal|null
decl_stmt|;
DECL|field|minDocFreq
specifier|private
name|int
name|minDocFreq
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|maxDocFreq
specifier|private
name|int
name|maxDocFreq
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|minWordLen
specifier|private
name|int
name|minWordLen
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|maxWordLen
specifier|private
name|int
name|maxWordLen
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|boostTerms
specifier|private
name|float
name|boostTerms
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|boost
specifier|private
name|float
name|boost
init|=
operator|-
literal|1
decl_stmt|;
comment|/**      * A more like this query that runs against a specific field.      *      * @param name The field name to run the query against      */
DECL|method|MoreLikeThisFieldJsonQueryBuilder
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**      * The text to use in order to find documents that are "like" this.      */
DECL|method|likeText
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|likeText
parameter_list|(
name|String
name|likeText
parameter_list|)
block|{
name|this
operator|.
name|likeText
operator|=
name|likeText
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The percentage of terms to match. Defaults to<tt>0.3</tt>.      */
DECL|method|percentTermsToMatch
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|percentTermsToMatch
parameter_list|(
name|float
name|percentTermsToMatch
parameter_list|)
block|{
name|this
operator|.
name|percentTermsToMatch
operator|=
name|percentTermsToMatch
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The frequency below which terms will be ignored in the source doc. The default      * frequency is<tt>2</tt>.      */
DECL|method|minTermFreq
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|minTermFreq
parameter_list|(
name|int
name|minTermFreqy
parameter_list|)
block|{
name|this
operator|.
name|minTermFreq
operator|=
name|minTermFreqy
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the maximum number of query terms that will be included in any generated query.      * Defaults to<tt>25</tt>.      */
DECL|method|maxQueryTerms
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|maxQueryTerms
parameter_list|(
name|int
name|maxQueryTerms
parameter_list|)
block|{
name|this
operator|.
name|maxQueryTerms
operator|=
name|maxQueryTerms
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the set of stopwords.      *      *<p>Any word in this set is considered "uninteresting" and ignored. Even if your Analyzer allows stopwords, you      * might want to tell the MoreLikeThis code to ignore them, as for the purposes of document similarity it seems      * reasonable to assume that "a stop word is never interesting".      */
DECL|method|stopWords
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|stopWords
parameter_list|(
name|String
modifier|...
name|stopWords
parameter_list|)
block|{
name|this
operator|.
name|stopWords
operator|=
name|stopWords
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the frequency at which words will be ignored which do not occur in at least this      * many docs. Defaults to<tt>5</tt>.      */
DECL|method|minDocFreq
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|minDocFreq
parameter_list|(
name|int
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
comment|/**      * Set the maximum frequency in which words may still appear. Words that appear      * in more than this many docs will be ignored. Defaults to unbounded.      */
DECL|method|maxDocFreq
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|maxDocFreq
parameter_list|(
name|int
name|maxDocFreq
parameter_list|)
block|{
name|this
operator|.
name|maxDocFreq
operator|=
name|maxDocFreq
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the minimum word length below which words will be ignored. Defaults      * to<tt>0</tt>.      */
DECL|method|minWordLen
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|minWordLen
parameter_list|(
name|int
name|minWordLen
parameter_list|)
block|{
name|this
operator|.
name|minWordLen
operator|=
name|minWordLen
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the maximum word length above which words will be ignored. Defaults to      * unbounded (<tt>0</tt>).      */
DECL|method|maxWordLen
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|maxWordLen
parameter_list|(
name|int
name|maxWordLen
parameter_list|)
block|{
name|this
operator|.
name|maxWordLen
operator|=
name|maxWordLen
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the boost factor to use when boosting terms. Defaults to<tt>1</tt>.      */
DECL|method|boostTerms
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|boostTerms
parameter_list|(
name|float
name|boostTerms
parameter_list|)
block|{
name|this
operator|.
name|boostTerms
operator|=
name|boostTerms
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|boost
specifier|public
name|MoreLikeThisFieldJsonQueryBuilder
name|boost
parameter_list|(
name|float
name|boost
parameter_list|)
block|{
name|this
operator|.
name|boost
operator|=
name|boost
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|doJson
annotation|@
name|Override
specifier|protected
name|void
name|doJson
parameter_list|(
name|JsonBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|MoreLikeThisFieldJsonQueryParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|likeText
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryBuilderException
argument_list|(
literal|"moreLikeThisField requires 'like_text' to be provided"
argument_list|)
throw|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"like_text"
argument_list|,
name|likeText
argument_list|)
expr_stmt|;
if|if
condition|(
name|percentTermsToMatch
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"percent_terms_to_match"
argument_list|,
name|percentTermsToMatch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minTermFreq
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"min_term_freq"
argument_list|,
name|minTermFreq
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|maxQueryTerms
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"max_query_terms"
argument_list|,
name|maxQueryTerms
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|stopWords
operator|!=
literal|null
operator|&&
name|stopWords
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"stop_words"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|stopWord
range|:
name|stopWords
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|stopWord
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|minDocFreq
operator|!=
operator|-
literal|1
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
if|if
condition|(
name|maxDocFreq
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"max_doc_freq"
argument_list|,
name|maxDocFreq
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minWordLen
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"min_word_len"
argument_list|,
name|minWordLen
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|maxWordLen
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"max_word_len"
argument_list|,
name|maxWordLen
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boostTerms
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boost_terms"
argument_list|,
name|boostTerms
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boost
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
name|boost
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

