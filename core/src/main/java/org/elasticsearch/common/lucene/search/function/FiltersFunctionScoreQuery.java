begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.search.function
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
operator|.
name|function
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
name|LeafReaderContext
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
name|Explanation
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
name|FilterScorer
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
name|Scorer
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
name|Weight
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
name|Bits
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|Writeable
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
name|lucene
operator|.
name|Lucene
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Locale
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
comment|/**  * A query that allows for a pluggable boost function / filter. If it matches  * the filter, it will be boosted by the formula.  */
end_comment

begin_class
DECL|class|FiltersFunctionScoreQuery
specifier|public
class|class
name|FiltersFunctionScoreQuery
extends|extends
name|Query
block|{
DECL|class|FilterFunction
specifier|public
specifier|static
class|class
name|FilterFunction
block|{
DECL|field|filter
specifier|public
specifier|final
name|Query
name|filter
decl_stmt|;
DECL|field|function
specifier|public
specifier|final
name|ScoreFunction
name|function
decl_stmt|;
DECL|method|FilterFunction
specifier|public
name|FilterFunction
parameter_list|(
name|Query
name|filter
parameter_list|,
name|ScoreFunction
name|function
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
name|this
operator|.
name|function
operator|=
name|function
expr_stmt|;
block|}
annotation|@
name|Override
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
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|FilterFunction
name|that
init|=
operator|(
name|FilterFunction
operator|)
name|o
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|filter
argument_list|,
name|that
operator|.
name|filter
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|function
argument_list|,
name|that
operator|.
name|function
argument_list|)
return|;
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
name|super
operator|.
name|hashCode
argument_list|()
argument_list|,
name|filter
argument_list|,
name|function
argument_list|)
return|;
block|}
block|}
DECL|enum|ScoreMode
specifier|public
enum|enum
name|ScoreMode
implements|implements
name|Writeable
block|{
DECL|enum constant|FIRST
DECL|enum constant|AVG
DECL|enum constant|MAX
DECL|enum constant|SUM
DECL|enum constant|MIN
DECL|enum constant|MULTIPLY
name|FIRST
block|,
name|AVG
block|,
name|MAX
block|,
name|SUM
block|,
name|MIN
block|,
name|MULTIPLY
block|;
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|this
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|readFromStream
specifier|public
specifier|static
name|ScoreMode
name|readFromStream
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|ordinal
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|ordinal
operator|<
literal|0
operator|||
name|ordinal
operator|>=
name|values
argument_list|()
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown ScoreMode ordinal ["
operator|+
name|ordinal
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|values
argument_list|()
index|[
name|ordinal
index|]
return|;
block|}
DECL|method|fromString
specifier|public
specifier|static
name|ScoreMode
name|fromString
parameter_list|(
name|String
name|scoreMode
parameter_list|)
block|{
return|return
name|valueOf
argument_list|(
name|scoreMode
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
return|;
block|}
block|}
DECL|field|subQuery
specifier|final
name|Query
name|subQuery
decl_stmt|;
DECL|field|filterFunctions
specifier|final
name|FilterFunction
index|[]
name|filterFunctions
decl_stmt|;
DECL|field|scoreMode
specifier|final
name|ScoreMode
name|scoreMode
decl_stmt|;
DECL|field|maxBoost
specifier|final
name|float
name|maxBoost
decl_stmt|;
DECL|field|minScore
specifier|private
specifier|final
name|Float
name|minScore
decl_stmt|;
DECL|field|combineFunction
specifier|protected
specifier|final
name|CombineFunction
name|combineFunction
decl_stmt|;
DECL|method|FiltersFunctionScoreQuery
specifier|public
name|FiltersFunctionScoreQuery
parameter_list|(
name|Query
name|subQuery
parameter_list|,
name|ScoreMode
name|scoreMode
parameter_list|,
name|FilterFunction
index|[]
name|filterFunctions
parameter_list|,
name|float
name|maxBoost
parameter_list|,
name|Float
name|minScore
parameter_list|,
name|CombineFunction
name|combineFunction
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
name|scoreMode
operator|=
name|scoreMode
expr_stmt|;
name|this
operator|.
name|filterFunctions
operator|=
name|filterFunctions
expr_stmt|;
name|this
operator|.
name|maxBoost
operator|=
name|maxBoost
expr_stmt|;
name|this
operator|.
name|combineFunction
operator|=
name|combineFunction
expr_stmt|;
name|this
operator|.
name|minScore
operator|=
name|minScore
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
DECL|method|getFilterFunctions
specifier|public
name|FilterFunction
index|[]
name|getFilterFunctions
parameter_list|()
block|{
return|return
name|filterFunctions
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
return|return
operator|new
name|FiltersFunctionScoreQuery
argument_list|(
name|newQ
argument_list|,
name|scoreMode
argument_list|,
name|filterFunctions
argument_list|,
name|maxBoost
argument_list|,
name|minScore
argument_list|,
name|combineFunction
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createWeight
specifier|public
name|Weight
name|createWeight
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|boolean
name|needsScores
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|needsScores
operator|==
literal|false
operator|&&
name|minScore
operator|==
literal|null
condition|)
block|{
return|return
name|subQuery
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|,
name|needsScores
argument_list|)
return|;
block|}
name|boolean
name|subQueryNeedsScores
init|=
name|combineFunction
operator|!=
name|CombineFunction
operator|.
name|REPLACE
decl_stmt|;
name|Weight
index|[]
name|filterWeights
init|=
operator|new
name|Weight
index|[
name|filterFunctions
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
name|filterFunctions
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|subQueryNeedsScores
operator||=
name|filterFunctions
index|[
name|i
index|]
operator|.
name|function
operator|.
name|needsScores
argument_list|()
expr_stmt|;
name|filterWeights
index|[
name|i
index|]
operator|=
name|searcher
operator|.
name|createNormalizedWeight
argument_list|(
name|filterFunctions
index|[
name|i
index|]
operator|.
name|filter
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|Weight
name|subQueryWeight
init|=
name|subQuery
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|,
name|subQueryNeedsScores
argument_list|)
decl_stmt|;
return|return
operator|new
name|CustomBoostFactorWeight
argument_list|(
name|this
argument_list|,
name|subQueryWeight
argument_list|,
name|filterWeights
argument_list|,
name|subQueryNeedsScores
argument_list|)
return|;
block|}
DECL|class|CustomBoostFactorWeight
class|class
name|CustomBoostFactorWeight
extends|extends
name|Weight
block|{
DECL|field|subQueryWeight
specifier|final
name|Weight
name|subQueryWeight
decl_stmt|;
DECL|field|filterWeights
specifier|final
name|Weight
index|[]
name|filterWeights
decl_stmt|;
DECL|field|needsScores
specifier|final
name|boolean
name|needsScores
decl_stmt|;
DECL|method|CustomBoostFactorWeight
specifier|public
name|CustomBoostFactorWeight
parameter_list|(
name|Query
name|parent
parameter_list|,
name|Weight
name|subQueryWeight
parameter_list|,
name|Weight
index|[]
name|filterWeights
parameter_list|,
name|boolean
name|needsScores
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|this
operator|.
name|subQueryWeight
operator|=
name|subQueryWeight
expr_stmt|;
name|this
operator|.
name|filterWeights
operator|=
name|filterWeights
expr_stmt|;
name|this
operator|.
name|needsScores
operator|=
name|needsScores
expr_stmt|;
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
name|subQueryWeight
operator|.
name|extractTerms
argument_list|(
name|terms
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getValueForNormalization
specifier|public
name|float
name|getValueForNormalization
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|subQueryWeight
operator|.
name|getValueForNormalization
argument_list|()
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
parameter_list|,
name|float
name|boost
parameter_list|)
block|{
name|subQueryWeight
operator|.
name|normalize
argument_list|(
name|norm
argument_list|,
name|boost
argument_list|)
expr_stmt|;
block|}
DECL|method|functionScorer
specifier|private
name|FiltersFunctionFactorScorer
name|functionScorer
parameter_list|(
name|LeafReaderContext
name|context
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
name|context
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
specifier|final
name|LeafScoreFunction
index|[]
name|functions
init|=
operator|new
name|LeafScoreFunction
index|[
name|filterFunctions
operator|.
name|length
index|]
decl_stmt|;
specifier|final
name|Bits
index|[]
name|docSets
init|=
operator|new
name|Bits
index|[
name|filterFunctions
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
name|filterFunctions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|FilterFunction
name|filterFunction
init|=
name|filterFunctions
index|[
name|i
index|]
decl_stmt|;
name|functions
index|[
name|i
index|]
operator|=
name|filterFunction
operator|.
name|function
operator|.
name|getLeafScoreFunction
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|Scorer
name|filterScorer
init|=
name|filterWeights
index|[
name|i
index|]
operator|.
name|scorer
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|docSets
index|[
name|i
index|]
operator|=
name|Lucene
operator|.
name|asSequentialAccessBits
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|filterScorer
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|FiltersFunctionFactorScorer
argument_list|(
name|this
argument_list|,
name|subQueryScorer
argument_list|,
name|scoreMode
argument_list|,
name|filterFunctions
argument_list|,
name|maxBoost
argument_list|,
name|functions
argument_list|,
name|docSets
argument_list|,
name|combineFunction
argument_list|,
name|needsScores
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|scorer
specifier|public
name|Scorer
name|scorer
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|Scorer
name|scorer
init|=
name|functionScorer
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|scorer
operator|!=
literal|null
operator|&&
name|minScore
operator|!=
literal|null
condition|)
block|{
name|scorer
operator|=
operator|new
name|MinScoreScorer
argument_list|(
name|this
argument_list|,
name|scorer
argument_list|,
name|minScore
argument_list|)
expr_stmt|;
block|}
return|return
name|scorer
return|;
block|}
annotation|@
name|Override
DECL|method|explain
specifier|public
name|Explanation
name|explain
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|,
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|Explanation
name|expl
init|=
name|subQueryWeight
operator|.
name|explain
argument_list|(
name|context
argument_list|,
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|expl
operator|.
name|isMatch
argument_list|()
condition|)
block|{
return|return
name|expl
return|;
block|}
comment|// First: Gather explanations for all filters
name|List
argument_list|<
name|Explanation
argument_list|>
name|filterExplanations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|filterFunctions
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|Bits
name|docSet
init|=
name|Lucene
operator|.
name|asSequentialAccessBits
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|filterWeights
index|[
name|i
index|]
operator|.
name|scorer
argument_list|(
name|context
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|docSet
operator|.
name|get
argument_list|(
name|doc
argument_list|)
condition|)
block|{
name|FilterFunction
name|filterFunction
init|=
name|filterFunctions
index|[
name|i
index|]
decl_stmt|;
name|Explanation
name|functionExplanation
init|=
name|filterFunction
operator|.
name|function
operator|.
name|getLeafScoreFunction
argument_list|(
name|context
argument_list|)
operator|.
name|explainScore
argument_list|(
name|doc
argument_list|,
name|expl
argument_list|)
decl_stmt|;
name|double
name|factor
init|=
name|functionExplanation
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|float
name|sc
init|=
name|CombineFunction
operator|.
name|toFloat
argument_list|(
name|factor
argument_list|)
decl_stmt|;
name|Explanation
name|filterExplanation
init|=
name|Explanation
operator|.
name|match
argument_list|(
name|sc
argument_list|,
literal|"function score, product of:"
argument_list|,
name|Explanation
operator|.
name|match
argument_list|(
literal|1.0f
argument_list|,
literal|"match filter: "
operator|+
name|filterFunction
operator|.
name|filter
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|functionExplanation
argument_list|)
decl_stmt|;
name|filterExplanations
operator|.
name|add
argument_list|(
name|filterExplanation
argument_list|)
expr_stmt|;
block|}
block|}
name|FiltersFunctionFactorScorer
name|scorer
init|=
name|functionScorer
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|int
name|actualDoc
init|=
name|scorer
operator|.
name|iterator
argument_list|()
operator|.
name|advance
argument_list|(
name|doc
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|actualDoc
operator|==
name|doc
operator|)
assert|;
name|double
name|score
init|=
name|scorer
operator|.
name|computeScore
argument_list|(
name|doc
argument_list|,
name|expl
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|Explanation
name|factorExplanation
decl_stmt|;
if|if
condition|(
name|filterExplanations
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|factorExplanation
operator|=
name|Explanation
operator|.
name|match
argument_list|(
name|CombineFunction
operator|.
name|toFloat
argument_list|(
name|score
argument_list|)
argument_list|,
literal|"function score, score mode ["
operator|+
name|scoreMode
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|+
literal|"]"
argument_list|,
name|filterExplanations
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// it is a little weird to add a match although no function matches but that is the way function_score behaves right now
name|factorExplanation
operator|=
name|Explanation
operator|.
name|match
argument_list|(
literal|1.0f
argument_list|,
literal|"No function matched"
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|expl
operator|=
name|combineFunction
operator|.
name|explain
argument_list|(
name|expl
argument_list|,
name|factorExplanation
argument_list|,
name|maxBoost
argument_list|)
expr_stmt|;
if|if
condition|(
name|minScore
operator|!=
literal|null
operator|&&
name|minScore
operator|>
name|expl
operator|.
name|getValue
argument_list|()
condition|)
block|{
name|expl
operator|=
name|Explanation
operator|.
name|noMatch
argument_list|(
literal|"Score value is too low, expected at least "
operator|+
name|minScore
operator|+
literal|" but got "
operator|+
name|expl
operator|.
name|getValue
argument_list|()
argument_list|,
name|expl
argument_list|)
expr_stmt|;
block|}
return|return
name|expl
return|;
block|}
block|}
DECL|class|FiltersFunctionFactorScorer
specifier|static
class|class
name|FiltersFunctionFactorScorer
extends|extends
name|FilterScorer
block|{
DECL|field|filterFunctions
specifier|private
specifier|final
name|FilterFunction
index|[]
name|filterFunctions
decl_stmt|;
DECL|field|scoreMode
specifier|private
specifier|final
name|ScoreMode
name|scoreMode
decl_stmt|;
DECL|field|functions
specifier|private
specifier|final
name|LeafScoreFunction
index|[]
name|functions
decl_stmt|;
DECL|field|docSets
specifier|private
specifier|final
name|Bits
index|[]
name|docSets
decl_stmt|;
DECL|field|scoreCombiner
specifier|private
specifier|final
name|CombineFunction
name|scoreCombiner
decl_stmt|;
DECL|field|maxBoost
specifier|private
specifier|final
name|float
name|maxBoost
decl_stmt|;
DECL|field|needsScores
specifier|private
specifier|final
name|boolean
name|needsScores
decl_stmt|;
DECL|method|FiltersFunctionFactorScorer
specifier|private
name|FiltersFunctionFactorScorer
parameter_list|(
name|CustomBoostFactorWeight
name|w
parameter_list|,
name|Scorer
name|scorer
parameter_list|,
name|ScoreMode
name|scoreMode
parameter_list|,
name|FilterFunction
index|[]
name|filterFunctions
parameter_list|,
name|float
name|maxBoost
parameter_list|,
name|LeafScoreFunction
index|[]
name|functions
parameter_list|,
name|Bits
index|[]
name|docSets
parameter_list|,
name|CombineFunction
name|scoreCombiner
parameter_list|,
name|boolean
name|needsScores
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|scorer
argument_list|,
name|w
argument_list|)
expr_stmt|;
name|this
operator|.
name|scoreMode
operator|=
name|scoreMode
expr_stmt|;
name|this
operator|.
name|filterFunctions
operator|=
name|filterFunctions
expr_stmt|;
name|this
operator|.
name|functions
operator|=
name|functions
expr_stmt|;
name|this
operator|.
name|docSets
operator|=
name|docSets
expr_stmt|;
name|this
operator|.
name|scoreCombiner
operator|=
name|scoreCombiner
expr_stmt|;
name|this
operator|.
name|maxBoost
operator|=
name|maxBoost
expr_stmt|;
name|this
operator|.
name|needsScores
operator|=
name|needsScores
expr_stmt|;
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
name|int
name|docId
init|=
name|docID
argument_list|()
decl_stmt|;
comment|// Even if the weight is created with needsScores=false, it might
comment|// be costly to call score(), so we explicitly check if scores
comment|// are needed
name|float
name|subQueryScore
init|=
name|needsScores
condition|?
name|super
operator|.
name|score
argument_list|()
else|:
literal|0f
decl_stmt|;
name|double
name|factor
init|=
name|computeScore
argument_list|(
name|docId
argument_list|,
name|subQueryScore
argument_list|)
decl_stmt|;
return|return
name|scoreCombiner
operator|.
name|combine
argument_list|(
name|subQueryScore
argument_list|,
name|factor
argument_list|,
name|maxBoost
argument_list|)
return|;
block|}
DECL|method|computeScore
specifier|protected
name|double
name|computeScore
parameter_list|(
name|int
name|docId
parameter_list|,
name|float
name|subQueryScore
parameter_list|)
block|{
name|double
name|factor
init|=
literal|1d
decl_stmt|;
switch|switch
condition|(
name|scoreMode
condition|)
block|{
case|case
name|FIRST
case|:
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|filterFunctions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|docSets
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|docId
argument_list|)
condition|)
block|{
name|factor
operator|=
name|functions
index|[
name|i
index|]
operator|.
name|score
argument_list|(
name|docId
argument_list|,
name|subQueryScore
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
break|break;
case|case
name|MAX
case|:
name|double
name|maxFactor
init|=
name|Double
operator|.
name|NEGATIVE_INFINITY
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
name|filterFunctions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|docSets
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|docId
argument_list|)
condition|)
block|{
name|maxFactor
operator|=
name|Math
operator|.
name|max
argument_list|(
name|functions
index|[
name|i
index|]
operator|.
name|score
argument_list|(
name|docId
argument_list|,
name|subQueryScore
argument_list|)
argument_list|,
name|maxFactor
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|maxFactor
operator|!=
name|Float
operator|.
name|NEGATIVE_INFINITY
condition|)
block|{
name|factor
operator|=
name|maxFactor
expr_stmt|;
block|}
break|break;
case|case
name|MIN
case|:
name|double
name|minFactor
init|=
name|Double
operator|.
name|POSITIVE_INFINITY
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
name|filterFunctions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|docSets
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|docId
argument_list|)
condition|)
block|{
name|minFactor
operator|=
name|Math
operator|.
name|min
argument_list|(
name|functions
index|[
name|i
index|]
operator|.
name|score
argument_list|(
name|docId
argument_list|,
name|subQueryScore
argument_list|)
argument_list|,
name|minFactor
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|minFactor
operator|!=
name|Float
operator|.
name|POSITIVE_INFINITY
condition|)
block|{
name|factor
operator|=
name|minFactor
expr_stmt|;
block|}
break|break;
case|case
name|MULTIPLY
case|:
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|filterFunctions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|docSets
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|docId
argument_list|)
condition|)
block|{
name|factor
operator|*=
name|functions
index|[
name|i
index|]
operator|.
name|score
argument_list|(
name|docId
argument_list|,
name|subQueryScore
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
default|default:
comment|// Avg / Total
name|double
name|totalFactor
init|=
literal|0.0f
decl_stmt|;
name|double
name|weightSum
init|=
literal|0
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
name|filterFunctions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|docSets
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|docId
argument_list|)
condition|)
block|{
name|totalFactor
operator|+=
name|functions
index|[
name|i
index|]
operator|.
name|score
argument_list|(
name|docId
argument_list|,
name|subQueryScore
argument_list|)
expr_stmt|;
if|if
condition|(
name|filterFunctions
index|[
name|i
index|]
operator|.
name|function
operator|instanceof
name|WeightFactorFunction
condition|)
block|{
name|weightSum
operator|+=
operator|(
operator|(
name|WeightFactorFunction
operator|)
name|filterFunctions
index|[
name|i
index|]
operator|.
name|function
operator|)
operator|.
name|getWeight
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|weightSum
operator|+=
literal|1.0
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|weightSum
operator|!=
literal|0
condition|)
block|{
name|factor
operator|=
name|totalFactor
expr_stmt|;
if|if
condition|(
name|scoreMode
operator|==
name|ScoreMode
operator|.
name|AVG
condition|)
block|{
name|factor
operator|/=
name|weightSum
expr_stmt|;
block|}
block|}
break|break;
block|}
return|return
name|factor
return|;
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
literal|"function score ("
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
literal|", functions: ["
argument_list|)
expr_stmt|;
for|for
control|(
name|FilterFunction
name|filterFunction
range|:
name|filterFunctions
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"{filter("
argument_list|)
operator|.
name|append
argument_list|(
name|filterFunction
operator|.
name|filter
argument_list|)
operator|.
name|append
argument_list|(
literal|"), function ["
argument_list|)
operator|.
name|append
argument_list|(
name|filterFunction
operator|.
name|function
argument_list|)
operator|.
name|append
argument_list|(
literal|"]}"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"])"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
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
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|sameClassAs
argument_list|(
name|o
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return
literal|false
return|;
block|}
name|FiltersFunctionScoreQuery
name|other
init|=
operator|(
name|FiltersFunctionScoreQuery
operator|)
name|o
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|subQuery
argument_list|,
name|other
operator|.
name|subQuery
argument_list|)
operator|&&
name|this
operator|.
name|maxBoost
operator|==
name|other
operator|.
name|maxBoost
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|combineFunction
argument_list|,
name|other
operator|.
name|combineFunction
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|minScore
argument_list|,
name|other
operator|.
name|minScore
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|scoreMode
argument_list|,
name|other
operator|.
name|scoreMode
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|this
operator|.
name|filterFunctions
argument_list|,
name|other
operator|.
name|filterFunctions
argument_list|)
return|;
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
name|subQuery
argument_list|,
name|maxBoost
argument_list|,
name|combineFunction
argument_list|,
name|minScore
argument_list|,
name|scoreMode
argument_list|,
name|Arrays
operator|.
name|hashCode
argument_list|(
name|filterFunctions
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

