begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.phrase
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|phrase
package|;
end_package

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
name|phrase
operator|.
name|DirectCandidateGenerator
operator|.
name|Candidate
import|;
end_import

begin_class
DECL|class|StupidBackoffScorer
specifier|public
class|class
name|StupidBackoffScorer
extends|extends
name|WordScorer
block|{
DECL|field|FACTORY
specifier|public
specifier|static
specifier|final
name|WordScorerFactory
name|FACTORY
init|=
operator|new
name|WordScorer
operator|.
name|WordScorerFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|WordScorer
name|newScorer
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|String
name|field
parameter_list|,
name|double
name|realWordLikelyhood
parameter_list|,
name|BytesRef
name|separator
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|StupidBackoffScorer
argument_list|(
name|reader
argument_list|,
name|field
argument_list|,
name|realWordLikelyhood
argument_list|,
name|separator
argument_list|,
literal|0.4f
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|discount
specifier|private
specifier|final
name|double
name|discount
decl_stmt|;
DECL|method|StupidBackoffScorer
specifier|public
name|StupidBackoffScorer
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|String
name|field
parameter_list|,
name|double
name|realWordLikelyhood
parameter_list|,
name|BytesRef
name|separator
parameter_list|,
name|double
name|discount
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|reader
argument_list|,
name|field
argument_list|,
name|realWordLikelyhood
argument_list|,
name|separator
argument_list|)
expr_stmt|;
name|this
operator|.
name|discount
operator|=
name|discount
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|scoreBigram
specifier|protected
name|double
name|scoreBigram
parameter_list|(
name|Candidate
name|word
parameter_list|,
name|Candidate
name|w_1
parameter_list|)
throws|throws
name|IOException
block|{
name|SuggestUtils
operator|.
name|join
argument_list|(
name|separator
argument_list|,
name|spare
argument_list|,
name|w_1
operator|.
name|term
argument_list|,
name|word
operator|.
name|term
argument_list|)
expr_stmt|;
specifier|final
name|long
name|count
init|=
name|frequency
argument_list|(
name|spare
argument_list|)
decl_stmt|;
if|if
condition|(
name|count
operator|<
literal|1
condition|)
block|{
return|return
name|discount
operator|*
name|scoreUnigram
argument_list|(
name|word
argument_list|)
return|;
block|}
return|return
name|count
operator|/
operator|(
name|w_1
operator|.
name|frequency
operator|+
literal|0.00000000001d
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|scoreTrigram
specifier|protected
name|double
name|scoreTrigram
parameter_list|(
name|Candidate
name|w
parameter_list|,
name|Candidate
name|w_1
parameter_list|,
name|Candidate
name|w_2
parameter_list|)
throws|throws
name|IOException
block|{
name|SuggestUtils
operator|.
name|join
argument_list|(
name|separator
argument_list|,
name|spare
argument_list|,
name|w_2
operator|.
name|term
argument_list|,
name|w_1
operator|.
name|term
argument_list|,
name|w
operator|.
name|term
argument_list|)
expr_stmt|;
specifier|final
name|long
name|trigramCount
init|=
name|frequency
argument_list|(
name|spare
argument_list|)
decl_stmt|;
if|if
condition|(
name|trigramCount
operator|<
literal|1
condition|)
block|{
name|SuggestUtils
operator|.
name|join
argument_list|(
name|separator
argument_list|,
name|spare
argument_list|,
name|w_1
operator|.
name|term
argument_list|,
name|w
operator|.
name|term
argument_list|)
expr_stmt|;
specifier|final
name|long
name|count
init|=
name|frequency
argument_list|(
name|spare
argument_list|)
decl_stmt|;
if|if
condition|(
name|count
operator|<
literal|1
condition|)
block|{
return|return
name|discount
operator|*
name|scoreUnigram
argument_list|(
name|w
argument_list|)
return|;
block|}
return|return
name|discount
operator|*
operator|(
name|count
operator|/
operator|(
name|w_1
operator|.
name|frequency
operator|+
literal|0.00000000001d
operator|)
operator|)
return|;
block|}
name|SuggestUtils
operator|.
name|join
argument_list|(
name|separator
argument_list|,
name|spare
argument_list|,
name|w_1
operator|.
name|term
argument_list|,
name|w
operator|.
name|term
argument_list|)
expr_stmt|;
specifier|final
name|long
name|bigramCount
init|=
name|frequency
argument_list|(
name|spare
argument_list|)
decl_stmt|;
return|return
name|trigramCount
operator|/
operator|(
name|bigramCount
operator|+
literal|0.00000000001d
operator|)
return|;
block|}
block|}
end_class

end_unit

