begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
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
name|index
operator|.
name|Fields
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
name|index
operator|.
name|Terms
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
name|TermsEnum
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
name|similarities
operator|.
name|ClassicSimilarity
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
name|similarities
operator|.
name|Similarity
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
name|similarities
operator|.
name|TFIDFSimilarity
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
name|io
operator|.
name|FastStringReader
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
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MoreLikeThisQuery
specifier|public
class|class
name|MoreLikeThisQuery
extends|extends
name|Query
block|{
DECL|field|DEFAULT_MINIMUM_SHOULD_MATCH
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_MINIMUM_SHOULD_MATCH
init|=
literal|"30%"
decl_stmt|;
DECL|field|similarity
specifier|private
name|TFIDFSimilarity
name|similarity
decl_stmt|;
DECL|field|likeText
specifier|private
name|String
index|[]
name|likeText
decl_stmt|;
DECL|field|likeFields
specifier|private
name|Fields
index|[]
name|likeFields
decl_stmt|;
DECL|field|unlikeText
specifier|private
name|String
index|[]
name|unlikeText
decl_stmt|;
DECL|field|unlikeFields
specifier|private
name|Fields
index|[]
name|unlikeFields
decl_stmt|;
DECL|field|moreLikeFields
specifier|private
name|String
index|[]
name|moreLikeFields
decl_stmt|;
DECL|field|analyzer
specifier|private
name|Analyzer
name|analyzer
decl_stmt|;
DECL|field|minimumShouldMatch
specifier|private
name|String
name|minimumShouldMatch
init|=
name|DEFAULT_MINIMUM_SHOULD_MATCH
decl_stmt|;
DECL|field|minTermFrequency
specifier|private
name|int
name|minTermFrequency
init|=
name|XMoreLikeThis
operator|.
name|DEFAULT_MIN_TERM_FREQ
decl_stmt|;
DECL|field|maxQueryTerms
specifier|private
name|int
name|maxQueryTerms
init|=
name|XMoreLikeThis
operator|.
name|DEFAULT_MAX_QUERY_TERMS
decl_stmt|;
DECL|field|stopWords
specifier|private
name|Set
argument_list|<
name|?
argument_list|>
name|stopWords
init|=
name|XMoreLikeThis
operator|.
name|DEFAULT_STOP_WORDS
decl_stmt|;
DECL|field|minDocFreq
specifier|private
name|int
name|minDocFreq
init|=
name|XMoreLikeThis
operator|.
name|DEFAULT_MIN_DOC_FREQ
decl_stmt|;
DECL|field|maxDocFreq
specifier|private
name|int
name|maxDocFreq
init|=
name|XMoreLikeThis
operator|.
name|DEFAULT_MAX_DOC_FREQ
decl_stmt|;
DECL|field|minWordLen
specifier|private
name|int
name|minWordLen
init|=
name|XMoreLikeThis
operator|.
name|DEFAULT_MIN_WORD_LENGTH
decl_stmt|;
DECL|field|maxWordLen
specifier|private
name|int
name|maxWordLen
init|=
name|XMoreLikeThis
operator|.
name|DEFAULT_MAX_WORD_LENGTH
decl_stmt|;
DECL|field|boostTerms
specifier|private
name|boolean
name|boostTerms
init|=
name|XMoreLikeThis
operator|.
name|DEFAULT_BOOST
decl_stmt|;
DECL|field|boostTermsFactor
specifier|private
name|float
name|boostTermsFactor
init|=
literal|1
decl_stmt|;
DECL|method|MoreLikeThisQuery
specifier|public
name|MoreLikeThisQuery
parameter_list|()
block|{      }
DECL|method|MoreLikeThisQuery
specifier|public
name|MoreLikeThisQuery
parameter_list|(
name|String
name|likeText
parameter_list|,
name|String
index|[]
name|moreLikeFields
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|)
block|{
name|this
operator|.
name|likeText
operator|=
operator|new
name|String
index|[]
block|{
name|likeText
block|}
expr_stmt|;
name|this
operator|.
name|moreLikeFields
operator|=
name|moreLikeFields
expr_stmt|;
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|classHash
argument_list|()
argument_list|,
name|boostTerms
argument_list|,
name|boostTermsFactor
argument_list|,
name|Arrays
operator|.
name|hashCode
argument_list|(
name|likeText
argument_list|)
argument_list|,
name|maxDocFreq
argument_list|,
name|maxQueryTerms
argument_list|,
name|maxWordLen
argument_list|,
name|minDocFreq
argument_list|,
name|minTermFrequency
argument_list|,
name|minWordLen
argument_list|,
name|Arrays
operator|.
name|hashCode
argument_list|(
name|moreLikeFields
argument_list|)
argument_list|,
name|minimumShouldMatch
argument_list|,
name|stopWords
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|sameClassAs
argument_list|(
name|obj
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return
literal|false
return|;
block|}
name|MoreLikeThisQuery
name|other
init|=
operator|(
name|MoreLikeThisQuery
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|analyzer
operator|.
name|equals
argument_list|(
name|other
operator|.
name|analyzer
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|boostTerms
operator|!=
name|other
operator|.
name|boostTerms
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|boostTermsFactor
operator|!=
name|other
operator|.
name|boostTermsFactor
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
operator|(
name|Arrays
operator|.
name|equals
argument_list|(
name|likeText
argument_list|,
name|other
operator|.
name|likeText
argument_list|)
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|maxDocFreq
operator|!=
name|other
operator|.
name|maxDocFreq
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|maxQueryTerms
operator|!=
name|other
operator|.
name|maxQueryTerms
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|maxWordLen
operator|!=
name|other
operator|.
name|maxWordLen
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|minDocFreq
operator|!=
name|other
operator|.
name|minDocFreq
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|minTermFrequency
operator|!=
name|other
operator|.
name|minTermFrequency
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|minWordLen
operator|!=
name|other
operator|.
name|minWordLen
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|moreLikeFields
argument_list|,
name|other
operator|.
name|moreLikeFields
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|minimumShouldMatch
operator|.
name|equals
argument_list|(
name|other
operator|.
name|minimumShouldMatch
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|similarity
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|similarity
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|similarity
operator|.
name|equals
argument_list|(
name|other
operator|.
name|similarity
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|stopWords
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|stopWords
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|stopWords
operator|.
name|equals
argument_list|(
name|other
operator|.
name|stopWords
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|rewrite
specifier|public
name|Query
name|rewrite
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|Query
name|rewritten
init|=
name|super
operator|.
name|rewrite
argument_list|(
name|reader
argument_list|)
decl_stmt|;
if|if
condition|(
name|rewritten
operator|!=
name|this
condition|)
block|{
return|return
name|rewritten
return|;
block|}
name|XMoreLikeThis
name|mlt
init|=
operator|new
name|XMoreLikeThis
argument_list|(
name|reader
argument_list|,
name|similarity
operator|==
literal|null
condition|?
operator|new
name|ClassicSimilarity
argument_list|()
else|:
name|similarity
argument_list|)
decl_stmt|;
name|mlt
operator|.
name|setFieldNames
argument_list|(
name|moreLikeFields
argument_list|)
expr_stmt|;
name|mlt
operator|.
name|setAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
name|mlt
operator|.
name|setMinTermFreq
argument_list|(
name|minTermFrequency
argument_list|)
expr_stmt|;
name|mlt
operator|.
name|setMinDocFreq
argument_list|(
name|minDocFreq
argument_list|)
expr_stmt|;
name|mlt
operator|.
name|setMaxDocFreq
argument_list|(
name|maxDocFreq
argument_list|)
expr_stmt|;
name|mlt
operator|.
name|setMaxQueryTerms
argument_list|(
name|maxQueryTerms
argument_list|)
expr_stmt|;
name|mlt
operator|.
name|setMinWordLen
argument_list|(
name|minWordLen
argument_list|)
expr_stmt|;
name|mlt
operator|.
name|setMaxWordLen
argument_list|(
name|maxWordLen
argument_list|)
expr_stmt|;
name|mlt
operator|.
name|setStopWords
argument_list|(
name|stopWords
argument_list|)
expr_stmt|;
name|mlt
operator|.
name|setBoost
argument_list|(
name|boostTerms
argument_list|)
expr_stmt|;
name|mlt
operator|.
name|setBoostFactor
argument_list|(
name|boostTermsFactor
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|unlikeText
operator|!=
literal|null
operator|||
name|this
operator|.
name|unlikeFields
operator|!=
literal|null
condition|)
block|{
name|handleUnlike
argument_list|(
name|mlt
argument_list|,
name|this
operator|.
name|unlikeText
argument_list|,
name|this
operator|.
name|unlikeFields
argument_list|)
expr_stmt|;
block|}
return|return
name|createQuery
argument_list|(
name|mlt
argument_list|)
return|;
block|}
DECL|method|createQuery
specifier|private
name|Query
name|createQuery
parameter_list|(
name|XMoreLikeThis
name|mlt
parameter_list|)
throws|throws
name|IOException
block|{
name|BooleanQuery
operator|.
name|Builder
name|bqBuilder
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|likeFields
operator|!=
literal|null
condition|)
block|{
name|Query
name|mltQuery
init|=
name|mlt
operator|.
name|like
argument_list|(
name|this
operator|.
name|likeFields
argument_list|)
decl_stmt|;
name|mltQuery
operator|=
name|Queries
operator|.
name|applyMinimumShouldMatch
argument_list|(
operator|(
name|BooleanQuery
operator|)
name|mltQuery
argument_list|,
name|minimumShouldMatch
argument_list|)
expr_stmt|;
name|bqBuilder
operator|.
name|add
argument_list|(
name|mltQuery
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|likeText
operator|!=
literal|null
condition|)
block|{
name|Reader
index|[]
name|readers
init|=
operator|new
name|Reader
index|[
name|likeText
operator|.
name|length
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
name|readers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|readers
index|[
name|i
index|]
operator|=
operator|new
name|FastStringReader
argument_list|(
name|likeText
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|//LUCENE 4 UPGRADE this mapps the 3.6 behavior (only use the first field)
name|Query
name|mltQuery
init|=
name|mlt
operator|.
name|like
argument_list|(
name|moreLikeFields
index|[
literal|0
index|]
argument_list|,
name|readers
argument_list|)
decl_stmt|;
name|mltQuery
operator|=
name|Queries
operator|.
name|applyMinimumShouldMatch
argument_list|(
operator|(
name|BooleanQuery
operator|)
name|mltQuery
argument_list|,
name|minimumShouldMatch
argument_list|)
expr_stmt|;
name|bqBuilder
operator|.
name|add
argument_list|(
name|mltQuery
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
return|return
name|bqBuilder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|handleUnlike
specifier|private
name|void
name|handleUnlike
parameter_list|(
name|XMoreLikeThis
name|mlt
parameter_list|,
name|String
index|[]
name|unlikeText
parameter_list|,
name|Fields
index|[]
name|unlikeFields
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|Term
argument_list|>
name|skipTerms
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|// handle like text
if|if
condition|(
name|unlikeText
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|text
range|:
name|unlikeText
control|)
block|{
comment|// only use the first field to be consistent
name|String
name|fieldName
init|=
name|moreLikeFields
index|[
literal|0
index|]
decl_stmt|;
try|try
init|(
name|TokenStream
name|ts
init|=
name|analyzer
operator|.
name|tokenStream
argument_list|(
name|fieldName
argument_list|,
name|text
argument_list|)
init|)
block|{
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
name|ts
operator|.
name|reset
argument_list|()
expr_stmt|;
while|while
condition|(
name|ts
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|skipTerms
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
name|fieldName
argument_list|,
name|termAtt
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ts
operator|.
name|end
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|// handle like fields
if|if
condition|(
name|unlikeFields
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Fields
name|fields
range|:
name|unlikeFields
control|)
block|{
for|for
control|(
name|String
name|fieldName
range|:
name|fields
control|)
block|{
name|Terms
name|terms
init|=
name|fields
operator|.
name|terms
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
specifier|final
name|TermsEnum
name|termsEnum
init|=
name|terms
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|BytesRef
name|text
decl_stmt|;
while|while
condition|(
operator|(
name|text
operator|=
name|termsEnum
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|skipTerms
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
name|fieldName
argument_list|,
name|text
operator|.
name|utf8ToString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
operator|!
name|skipTerms
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mlt
operator|.
name|setSkipTerms
argument_list|(
name|skipTerms
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
literal|"like:"
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|likeText
argument_list|)
return|;
block|}
DECL|method|getLikeText
specifier|public
name|String
name|getLikeText
parameter_list|()
block|{
return|return
operator|(
name|likeText
operator|==
literal|null
condition|?
literal|null
else|:
name|likeText
index|[
literal|0
index|]
operator|)
return|;
block|}
DECL|method|getLikeTexts
specifier|public
name|String
index|[]
name|getLikeTexts
parameter_list|()
block|{
return|return
name|likeText
return|;
block|}
DECL|method|setLikeText
specifier|public
name|void
name|setLikeText
parameter_list|(
name|String
name|likeText
parameter_list|)
block|{
name|setLikeText
argument_list|(
operator|new
name|String
index|[]
block|{
name|likeText
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|setLikeText
specifier|public
name|void
name|setLikeText
parameter_list|(
name|String
modifier|...
name|likeText
parameter_list|)
block|{
name|this
operator|.
name|likeText
operator|=
name|likeText
expr_stmt|;
block|}
DECL|method|getLikeFields
specifier|public
name|Fields
index|[]
name|getLikeFields
parameter_list|()
block|{
return|return
name|likeFields
return|;
block|}
DECL|method|setLikeText
specifier|public
name|void
name|setLikeText
parameter_list|(
name|Fields
modifier|...
name|likeFields
parameter_list|)
block|{
name|this
operator|.
name|likeFields
operator|=
name|likeFields
expr_stmt|;
block|}
DECL|method|setLikeText
specifier|public
name|void
name|setLikeText
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|likeText
parameter_list|)
block|{
name|setLikeText
argument_list|(
name|likeText
operator|.
name|toArray
argument_list|(
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|setUnlikeText
specifier|public
name|void
name|setUnlikeText
parameter_list|(
name|Fields
modifier|...
name|unlikeFields
parameter_list|)
block|{
name|this
operator|.
name|unlikeFields
operator|=
name|unlikeFields
expr_stmt|;
block|}
DECL|method|setUnlikeText
specifier|public
name|void
name|setUnlikeText
parameter_list|(
name|String
index|[]
name|unlikeText
parameter_list|)
block|{
name|this
operator|.
name|unlikeText
operator|=
name|unlikeText
expr_stmt|;
block|}
DECL|method|getMoreLikeFields
specifier|public
name|String
index|[]
name|getMoreLikeFields
parameter_list|()
block|{
return|return
name|moreLikeFields
return|;
block|}
DECL|method|setMoreLikeFields
specifier|public
name|void
name|setMoreLikeFields
parameter_list|(
name|String
index|[]
name|moreLikeFields
parameter_list|)
block|{
name|this
operator|.
name|moreLikeFields
operator|=
name|moreLikeFields
expr_stmt|;
block|}
DECL|method|getSimilarity
specifier|public
name|Similarity
name|getSimilarity
parameter_list|()
block|{
return|return
name|similarity
return|;
block|}
DECL|method|setSimilarity
specifier|public
name|void
name|setSimilarity
parameter_list|(
name|Similarity
name|similarity
parameter_list|)
block|{
if|if
condition|(
name|similarity
operator|==
literal|null
operator|||
name|similarity
operator|instanceof
name|TFIDFSimilarity
condition|)
block|{
comment|//LUCENE 4 UPGRADE we need TFIDF similarity here so I only set it if it is an instance of it
name|this
operator|.
name|similarity
operator|=
operator|(
name|TFIDFSimilarity
operator|)
name|similarity
expr_stmt|;
block|}
block|}
DECL|method|getAnalyzer
specifier|public
name|Analyzer
name|getAnalyzer
parameter_list|()
block|{
return|return
name|analyzer
return|;
block|}
DECL|method|setAnalyzer
specifier|public
name|void
name|setAnalyzer
parameter_list|(
name|Analyzer
name|analyzer
parameter_list|)
block|{
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
block|}
comment|/**      * Number of terms that must match the generated query expressed in the      * common syntax for minimum should match.      *      * @see    org.elasticsearch.common.lucene.search.Queries#calculateMinShouldMatch(int, String)      */
DECL|method|getMinimumShouldMatch
specifier|public
name|String
name|getMinimumShouldMatch
parameter_list|()
block|{
return|return
name|minimumShouldMatch
return|;
block|}
comment|/**      * Number of terms that must match the generated query expressed in the      * common syntax for minimum should match. Defaults to<tt>30%</tt>.      *      * @see    org.elasticsearch.common.lucene.search.Queries#calculateMinShouldMatch(int, String)      */
DECL|method|setMinimumShouldMatch
specifier|public
name|void
name|setMinimumShouldMatch
parameter_list|(
name|String
name|minimumShouldMatch
parameter_list|)
block|{
name|this
operator|.
name|minimumShouldMatch
operator|=
name|minimumShouldMatch
expr_stmt|;
block|}
DECL|method|getMinTermFrequency
specifier|public
name|int
name|getMinTermFrequency
parameter_list|()
block|{
return|return
name|minTermFrequency
return|;
block|}
DECL|method|setMinTermFrequency
specifier|public
name|void
name|setMinTermFrequency
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
block|}
DECL|method|getMaxQueryTerms
specifier|public
name|int
name|getMaxQueryTerms
parameter_list|()
block|{
return|return
name|maxQueryTerms
return|;
block|}
DECL|method|setMaxQueryTerms
specifier|public
name|void
name|setMaxQueryTerms
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
block|}
DECL|method|getStopWords
specifier|public
name|Set
argument_list|<
name|?
argument_list|>
name|getStopWords
parameter_list|()
block|{
return|return
name|stopWords
return|;
block|}
DECL|method|setStopWords
specifier|public
name|void
name|setStopWords
parameter_list|(
name|Set
argument_list|<
name|?
argument_list|>
name|stopWords
parameter_list|)
block|{
name|this
operator|.
name|stopWords
operator|=
name|stopWords
expr_stmt|;
block|}
DECL|method|getMinDocFreq
specifier|public
name|int
name|getMinDocFreq
parameter_list|()
block|{
return|return
name|minDocFreq
return|;
block|}
DECL|method|setMinDocFreq
specifier|public
name|void
name|setMinDocFreq
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
block|}
DECL|method|getMaxDocFreq
specifier|public
name|int
name|getMaxDocFreq
parameter_list|()
block|{
return|return
name|maxDocFreq
return|;
block|}
DECL|method|setMaxDocFreq
specifier|public
name|void
name|setMaxDocFreq
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
block|}
DECL|method|getMinWordLen
specifier|public
name|int
name|getMinWordLen
parameter_list|()
block|{
return|return
name|minWordLen
return|;
block|}
DECL|method|setMinWordLen
specifier|public
name|void
name|setMinWordLen
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
block|}
DECL|method|getMaxWordLen
specifier|public
name|int
name|getMaxWordLen
parameter_list|()
block|{
return|return
name|maxWordLen
return|;
block|}
DECL|method|setMaxWordLen
specifier|public
name|void
name|setMaxWordLen
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
block|}
DECL|method|isBoostTerms
specifier|public
name|boolean
name|isBoostTerms
parameter_list|()
block|{
return|return
name|boostTerms
return|;
block|}
DECL|method|setBoostTerms
specifier|public
name|void
name|setBoostTerms
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
block|}
DECL|method|getBoostTermsFactor
specifier|public
name|float
name|getBoostTermsFactor
parameter_list|()
block|{
return|return
name|boostTermsFactor
return|;
block|}
DECL|method|setBoostTermsFactor
specifier|public
name|void
name|setBoostTermsFactor
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
block|}
block|}
end_class

end_unit

