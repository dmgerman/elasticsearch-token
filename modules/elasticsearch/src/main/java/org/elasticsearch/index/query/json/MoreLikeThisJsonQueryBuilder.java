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
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|MoreLikeThisJsonQueryBuilder
specifier|public
class|class
name|MoreLikeThisJsonQueryBuilder
extends|extends
name|BaseJsonQueryBuilder
block|{
DECL|field|fields
specifier|private
specifier|final
name|String
index|[]
name|fields
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
DECL|field|minTermFrequency
specifier|private
name|int
name|minTermFrequency
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
name|Boolean
name|boostTerms
init|=
literal|null
decl_stmt|;
DECL|field|boostTermsFactor
specifier|private
name|float
name|boostTermsFactor
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|MoreLikeThisJsonQueryBuilder
specifier|public
name|MoreLikeThisJsonQueryBuilder
parameter_list|(
name|String
modifier|...
name|fields
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
block|}
DECL|method|likeText
specifier|public
name|MoreLikeThisJsonQueryBuilder
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
DECL|method|percentTermsToMatch
specifier|public
name|MoreLikeThisJsonQueryBuilder
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
DECL|method|minTermFrequency
specifier|public
name|MoreLikeThisJsonQueryBuilder
name|minTermFrequency
parameter_list|(
name|int
name|minTermFrequency
parameter_list|)
block|{
name|this
operator|.
name|minTermFrequency
operator|=
name|minTermFrequency
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|maxQueryTerms
specifier|public
name|MoreLikeThisJsonQueryBuilder
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
DECL|method|stopWords
specifier|public
name|MoreLikeThisJsonQueryBuilder
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
DECL|method|minDocFreq
specifier|public
name|MoreLikeThisJsonQueryBuilder
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
DECL|method|maxDocFreq
specifier|public
name|MoreLikeThisJsonQueryBuilder
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
DECL|method|minWordLen
specifier|public
name|MoreLikeThisJsonQueryBuilder
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
DECL|method|maxWordLen
specifier|public
name|MoreLikeThisJsonQueryBuilder
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
DECL|method|boostTerms
specifier|public
name|MoreLikeThisJsonQueryBuilder
name|boostTerms
parameter_list|(
name|boolean
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
DECL|method|boostTermsFactor
specifier|public
name|MoreLikeThisJsonQueryBuilder
name|boostTermsFactor
parameter_list|(
name|float
name|boostTermsFactor
parameter_list|)
block|{
name|this
operator|.
name|boostTermsFactor
operator|=
name|boostTermsFactor
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
name|MoreLikeThisJsonQueryParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|fields
operator|==
literal|null
operator|||
name|fields
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|QueryBuilderException
argument_list|(
literal|"moreLikeThis requires 'fields' to be provided"
argument_list|)
throw|;
block|}
name|builder
operator|.
name|startArray
argument_list|(
literal|"fields"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|builder
operator|.
name|string
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
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
literal|"moreLikeThis requires 'likeText' to be provided"
argument_list|)
throw|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"likeText"
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
literal|"percentTermsToMatch"
argument_list|,
name|percentTermsToMatch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minTermFrequency
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"minTermFrequency"
argument_list|,
name|minTermFrequency
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
literal|"maxQueryTerms"
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
literal|"stopWords"
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
name|string
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
literal|"minDocFreq"
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
literal|"maxDocFreq"
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
literal|"minWordLen"
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
literal|"maxWordLen"
argument_list|,
name|maxWordLen
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boostTerms
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boostTerms"
argument_list|,
name|boostTerms
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boostTermsFactor
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boostTermsFactor"
argument_list|,
name|boostTermsFactor
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

