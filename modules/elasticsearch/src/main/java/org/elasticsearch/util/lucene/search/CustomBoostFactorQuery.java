begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.lucene.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|ToStringUtils
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
name|Set
import|;
end_import

begin_comment
comment|/**  * A query that wraps another query and applies the provided boost values to it. Simply  * applied the boost factor to the score of the wrapped query.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|CustomBoostFactorQuery
specifier|public
class|class
name|CustomBoostFactorQuery
extends|extends
name|Query
block|{
DECL|field|subQuery
specifier|private
name|Query
name|subQuery
decl_stmt|;
DECL|field|boostFactor
specifier|private
name|float
name|boostFactor
decl_stmt|;
DECL|method|CustomBoostFactorQuery
specifier|public
name|CustomBoostFactorQuery
parameter_list|(
name|Query
name|subQuery
parameter_list|,
name|float
name|boostFactor
parameter_list|)
block|{
name|this
operator|.
name|subQuery
operator|=
name|subQuery
expr_stmt|;
name|this
operator|.
name|boostFactor
operator|=
name|boostFactor
expr_stmt|;
block|}
DECL|method|getSubQuery
specifier|public
name|Query
name|getSubQuery
parameter_list|()
block|{
return|return
name|subQuery
return|;
block|}
DECL|method|getBoostFactor
specifier|public
name|float
name|getBoostFactor
parameter_list|()
block|{
return|return
name|boostFactor
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
name|newQ
init|=
name|subQuery
operator|.
name|rewrite
argument_list|(
name|reader
argument_list|)
decl_stmt|;
if|if
condition|(
name|newQ
operator|==
name|subQuery
condition|)
return|return
name|this
return|;
name|CustomBoostFactorQuery
name|bq
init|=
operator|(
name|CustomBoostFactorQuery
operator|)
name|this
operator|.
name|clone
argument_list|()
decl_stmt|;
name|bq
operator|.
name|subQuery
operator|=
name|newQ
expr_stmt|;
return|return
name|bq
return|;
block|}
annotation|@
name|Override
DECL|method|extractTerms
specifier|public
name|void
name|extractTerms
parameter_list|(
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
parameter_list|)
block|{
name|subQuery
operator|.
name|extractTerms
argument_list|(
name|terms
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createWeight
specifier|public
name|Weight
name|createWeight
parameter_list|(
name|Searcher
name|searcher
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|CustomBoostFactorWeight
argument_list|(
name|searcher
argument_list|)
return|;
block|}
DECL|class|CustomBoostFactorWeight
specifier|private
class|class
name|CustomBoostFactorWeight
extends|extends
name|Weight
block|{
DECL|field|searcher
name|Searcher
name|searcher
decl_stmt|;
DECL|field|subQueryWeight
name|Weight
name|subQueryWeight
decl_stmt|;
DECL|method|CustomBoostFactorWeight
specifier|public
name|CustomBoostFactorWeight
parameter_list|(
name|Searcher
name|searcher
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|searcher
operator|=
name|searcher
expr_stmt|;
name|this
operator|.
name|subQueryWeight
operator|=
name|subQuery
operator|.
name|weight
argument_list|(
name|searcher
argument_list|)
expr_stmt|;
block|}
DECL|method|getQuery
specifier|public
name|Query
name|getQuery
parameter_list|()
block|{
return|return
name|CustomBoostFactorQuery
operator|.
name|this
return|;
block|}
DECL|method|getValue
specifier|public
name|float
name|getValue
parameter_list|()
block|{
return|return
name|getBoost
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|sumOfSquaredWeights
specifier|public
name|float
name|sumOfSquaredWeights
parameter_list|()
throws|throws
name|IOException
block|{
name|float
name|sum
init|=
name|subQueryWeight
operator|.
name|sumOfSquaredWeights
argument_list|()
decl_stmt|;
name|sum
operator|*=
name|getBoost
argument_list|()
operator|*
name|getBoost
argument_list|()
expr_stmt|;
return|return
name|sum
return|;
block|}
annotation|@
name|Override
DECL|method|normalize
specifier|public
name|void
name|normalize
parameter_list|(
name|float
name|norm
parameter_list|)
block|{
name|norm
operator|*=
name|getBoost
argument_list|()
expr_stmt|;
name|subQueryWeight
operator|.
name|normalize
argument_list|(
name|norm
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|scorer
specifier|public
name|Scorer
name|scorer
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|boolean
name|scoreDocsInOrder
parameter_list|,
name|boolean
name|topScorer
parameter_list|)
throws|throws
name|IOException
block|{
name|Scorer
name|subQueryScorer
init|=
name|subQueryWeight
operator|.
name|scorer
argument_list|(
name|reader
argument_list|,
name|scoreDocsInOrder
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|subQueryScorer
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|CustomBoostFactorScorer
argument_list|(
name|getSimilarity
argument_list|(
name|searcher
argument_list|)
argument_list|,
name|reader
argument_list|,
name|this
argument_list|,
name|subQueryScorer
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|explain
specifier|public
name|Explanation
name|explain
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|Explanation
name|subQueryExpl
init|=
name|subQueryWeight
operator|.
name|explain
argument_list|(
name|reader
argument_list|,
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|subQueryExpl
operator|.
name|isMatch
argument_list|()
condition|)
block|{
return|return
name|subQueryExpl
return|;
block|}
name|float
name|sc
init|=
name|subQueryExpl
operator|.
name|getValue
argument_list|()
operator|*
name|boostFactor
decl_stmt|;
name|Explanation
name|res
init|=
operator|new
name|ComplexExplanation
argument_list|(
literal|true
argument_list|,
name|sc
argument_list|,
name|CustomBoostFactorQuery
operator|.
name|this
operator|.
name|toString
argument_list|()
operator|+
literal|", product of:"
argument_list|)
decl_stmt|;
name|res
operator|.
name|addDetail
argument_list|(
name|subQueryExpl
argument_list|)
expr_stmt|;
name|res
operator|.
name|addDetail
argument_list|(
operator|new
name|Explanation
argument_list|(
name|boostFactor
argument_list|,
literal|"boostFactor"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|res
return|;
block|}
block|}
DECL|class|CustomBoostFactorScorer
specifier|private
class|class
name|CustomBoostFactorScorer
extends|extends
name|Scorer
block|{
DECL|field|weight
specifier|private
specifier|final
name|CustomBoostFactorWeight
name|weight
decl_stmt|;
DECL|field|subQueryWeight
specifier|private
specifier|final
name|float
name|subQueryWeight
decl_stmt|;
DECL|field|scorer
specifier|private
specifier|final
name|Scorer
name|scorer
decl_stmt|;
DECL|field|reader
specifier|private
specifier|final
name|IndexReader
name|reader
decl_stmt|;
DECL|method|CustomBoostFactorScorer
specifier|private
name|CustomBoostFactorScorer
parameter_list|(
name|Similarity
name|similarity
parameter_list|,
name|IndexReader
name|reader
parameter_list|,
name|CustomBoostFactorWeight
name|w
parameter_list|,
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|similarity
argument_list|)
expr_stmt|;
name|this
operator|.
name|weight
operator|=
name|w
expr_stmt|;
name|this
operator|.
name|subQueryWeight
operator|=
name|w
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|this
operator|.
name|scorer
operator|=
name|scorer
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|docID
specifier|public
name|int
name|docID
parameter_list|()
block|{
return|return
name|scorer
operator|.
name|docID
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|advance
specifier|public
name|int
name|advance
parameter_list|(
name|int
name|target
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|scorer
operator|.
name|advance
argument_list|(
name|target
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|nextDoc
specifier|public
name|int
name|nextDoc
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|scorer
operator|.
name|nextDoc
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|score
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
name|float
name|score
init|=
name|subQueryWeight
operator|*
name|scorer
operator|.
name|score
argument_list|()
operator|*
name|boostFactor
decl_stmt|;
comment|// Current Lucene priority queues can't handle NaN and -Infinity, so
comment|// map to -Float.MAX_VALUE. This conditional handles both -infinity
comment|// and NaN since comparisons with NaN are always false.
return|return
name|score
operator|>
name|Float
operator|.
name|NEGATIVE_INFINITY
condition|?
name|score
else|:
operator|-
name|Float
operator|.
name|MAX_VALUE
return|;
block|}
DECL|method|explain
specifier|public
name|Explanation
name|explain
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|Explanation
name|subQueryExpl
init|=
name|weight
operator|.
name|subQueryWeight
operator|.
name|explain
argument_list|(
name|reader
argument_list|,
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|subQueryExpl
operator|.
name|isMatch
argument_list|()
condition|)
block|{
return|return
name|subQueryExpl
return|;
block|}
name|float
name|sc
init|=
name|subQueryExpl
operator|.
name|getValue
argument_list|()
operator|*
name|boostFactor
decl_stmt|;
name|Explanation
name|res
init|=
operator|new
name|ComplexExplanation
argument_list|(
literal|true
argument_list|,
name|sc
argument_list|,
name|CustomBoostFactorQuery
operator|.
name|this
operator|.
name|toString
argument_list|()
operator|+
literal|", product of:"
argument_list|)
decl_stmt|;
name|res
operator|.
name|addDetail
argument_list|(
name|subQueryExpl
argument_list|)
expr_stmt|;
name|res
operator|.
name|addDetail
argument_list|(
operator|new
name|Explanation
argument_list|(
name|boostFactor
argument_list|,
literal|"boostFactor"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|res
return|;
block|}
block|}
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"CustomBoostFactor("
argument_list|)
operator|.
name|append
argument_list|(
name|subQuery
operator|.
name|toString
argument_list|(
name|field
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|','
argument_list|)
operator|.
name|append
argument_list|(
name|boostFactor
argument_list|)
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|ToStringUtils
operator|.
name|boost
argument_list|(
name|getBoost
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|CustomBoostFactorQuery
name|other
init|=
operator|(
name|CustomBoostFactorQuery
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|getBoost
argument_list|()
operator|==
name|other
operator|.
name|getBoost
argument_list|()
operator|&&
name|this
operator|.
name|subQuery
operator|.
name|equals
argument_list|(
name|other
operator|.
name|subQuery
argument_list|)
operator|&&
name|this
operator|.
name|boostFactor
operator|==
name|other
operator|.
name|boostFactor
return|;
block|}
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|h
init|=
name|subQuery
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|h
operator|^=
operator|(
name|h
operator|<<
literal|17
operator|)
operator||
operator|(
name|h
operator|>>>
literal|16
operator|)
expr_stmt|;
name|h
operator|+=
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|boostFactor
argument_list|)
expr_stmt|;
name|h
operator|^=
operator|(
name|h
operator|<<
literal|8
operator|)
operator||
operator|(
name|h
operator|>>>
literal|25
operator|)
expr_stmt|;
name|h
operator|+=
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|getBoost
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|h
return|;
block|}
block|}
end_class

end_unit

