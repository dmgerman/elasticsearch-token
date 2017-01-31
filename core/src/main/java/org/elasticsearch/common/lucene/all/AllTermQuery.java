begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.all
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|all
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
name|payloads
operator|.
name|PayloadHelper
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
name|PostingsEnum
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
name|TermContext
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
name|TermState
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
name|CollectionStatistics
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
name|DocIdSetIterator
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
name|TermQuery
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
name|TermStatistics
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
name|Similarity
operator|.
name|SimScorer
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
operator|.
name|SimWeight
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
name|SmallFloat
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
comment|/**  * A term query that takes all payload boost values into account.  *<p>  * It is like PayloadTermQuery with AveragePayloadFunction, except  * unlike PayloadTermQuery, it doesn't plug into the similarity to  * determine how the payload should be factored in, it just parses  * the float and multiplies the average with the regular score.  */
end_comment

begin_class
DECL|class|AllTermQuery
specifier|public
specifier|final
class|class
name|AllTermQuery
extends|extends
name|Query
block|{
DECL|field|term
specifier|private
specifier|final
name|Term
name|term
decl_stmt|;
DECL|method|AllTermQuery
specifier|public
name|AllTermQuery
parameter_list|(
name|Term
name|term
parameter_list|)
block|{
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
block|}
DECL|method|getTerm
specifier|public
name|Term
name|getTerm
parameter_list|()
block|{
return|return
name|term
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
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|term
argument_list|,
operator|(
operator|(
name|AllTermQuery
operator|)
name|obj
operator|)
operator|.
name|term
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
literal|31
operator|*
name|classHash
argument_list|()
operator|+
name|term
operator|.
name|hashCode
argument_list|()
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
name|boolean
name|hasPayloads
init|=
literal|false
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|reader
operator|.
name|leaves
argument_list|()
control|)
block|{
specifier|final
name|Terms
name|terms
init|=
name|context
operator|.
name|reader
argument_list|()
operator|.
name|terms
argument_list|(
name|term
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|terms
operator|.
name|hasPayloads
argument_list|()
condition|)
block|{
name|hasPayloads
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
comment|// if the terms does not exist we could return a MatchNoDocsQuery but this would break the unified highlighter
comment|// which rewrites query with an empty reader.
if|if
condition|(
name|hasPayloads
operator|==
literal|false
condition|)
block|{
return|return
operator|new
name|TermQuery
argument_list|(
name|term
argument_list|)
return|;
block|}
return|return
name|this
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
condition|)
block|{
return|return
operator|new
name|TermQuery
argument_list|(
name|term
argument_list|)
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|,
name|needsScores
argument_list|)
return|;
block|}
specifier|final
name|TermContext
name|termStates
init|=
name|TermContext
operator|.
name|build
argument_list|(
name|searcher
operator|.
name|getTopReaderContext
argument_list|()
argument_list|,
name|term
argument_list|)
decl_stmt|;
specifier|final
name|CollectionStatistics
name|collectionStats
init|=
name|searcher
operator|.
name|collectionStatistics
argument_list|(
name|term
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|TermStatistics
name|termStats
init|=
name|searcher
operator|.
name|termStatistics
argument_list|(
name|term
argument_list|,
name|termStates
argument_list|)
decl_stmt|;
specifier|final
name|Similarity
name|similarity
init|=
name|searcher
operator|.
name|getSimilarity
argument_list|(
name|needsScores
argument_list|)
decl_stmt|;
specifier|final
name|SimWeight
name|stats
init|=
name|similarity
operator|.
name|computeWeight
argument_list|(
name|collectionStats
argument_list|,
name|termStats
argument_list|)
decl_stmt|;
return|return
operator|new
name|Weight
argument_list|(
name|this
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|float
name|getValueForNormalization
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|stats
operator|.
name|getValueForNormalization
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|normalize
parameter_list|(
name|float
name|norm
parameter_list|,
name|float
name|topLevelBoost
parameter_list|)
block|{
name|stats
operator|.
name|normalize
argument_list|(
name|norm
argument_list|,
name|topLevelBoost
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|terms
operator|.
name|add
argument_list|(
name|term
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|AllTermScorer
name|scorer
init|=
name|scorer
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|scorer
operator|!=
literal|null
condition|)
block|{
name|int
name|newDoc
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
if|if
condition|(
name|newDoc
operator|==
name|doc
condition|)
block|{
name|float
name|score
init|=
name|scorer
operator|.
name|score
argument_list|()
decl_stmt|;
name|float
name|freq
init|=
name|scorer
operator|.
name|freq
argument_list|()
decl_stmt|;
name|SimScorer
name|docScorer
init|=
name|similarity
operator|.
name|simScorer
argument_list|(
name|stats
argument_list|,
name|context
argument_list|)
decl_stmt|;
name|Explanation
name|freqExplanation
init|=
name|Explanation
operator|.
name|match
argument_list|(
name|freq
argument_list|,
literal|"termFreq="
operator|+
name|freq
argument_list|)
decl_stmt|;
name|Explanation
name|termScoreExplanation
init|=
name|docScorer
operator|.
name|explain
argument_list|(
name|doc
argument_list|,
name|freqExplanation
argument_list|)
decl_stmt|;
name|Explanation
name|payloadBoostExplanation
init|=
name|Explanation
operator|.
name|match
argument_list|(
name|scorer
operator|.
name|payloadBoost
argument_list|()
argument_list|,
literal|"payloadBoost="
operator|+
name|scorer
operator|.
name|payloadBoost
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|Explanation
operator|.
name|match
argument_list|(
name|score
argument_list|,
literal|"weight("
operator|+
name|getQuery
argument_list|()
operator|+
literal|" in "
operator|+
name|doc
operator|+
literal|") ["
operator|+
name|similarity
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"], product of:"
argument_list|,
name|termScoreExplanation
argument_list|,
name|payloadBoostExplanation
argument_list|)
return|;
block|}
block|}
return|return
name|Explanation
operator|.
name|noMatch
argument_list|(
literal|"no matching term"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AllTermScorer
name|scorer
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Terms
name|terms
init|=
name|context
operator|.
name|reader
argument_list|()
operator|.
name|terms
argument_list|(
name|term
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|TermsEnum
name|termsEnum
init|=
name|terms
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|termsEnum
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|TermState
name|state
init|=
name|termStates
operator|.
name|get
argument_list|(
name|context
operator|.
name|ord
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|==
literal|null
condition|)
block|{
comment|// Term does not exist in this segment
return|return
literal|null
return|;
block|}
name|termsEnum
operator|.
name|seekExact
argument_list|(
name|term
operator|.
name|bytes
argument_list|()
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|PostingsEnum
name|docs
init|=
name|termsEnum
operator|.
name|postings
argument_list|(
literal|null
argument_list|,
name|PostingsEnum
operator|.
name|PAYLOADS
argument_list|)
decl_stmt|;
assert|assert
name|docs
operator|!=
literal|null
assert|;
return|return
operator|new
name|AllTermScorer
argument_list|(
name|this
argument_list|,
name|docs
argument_list|,
name|similarity
operator|.
name|simScorer
argument_list|(
name|stats
argument_list|,
name|context
argument_list|)
argument_list|)
return|;
block|}
block|}
return|;
block|}
DECL|class|AllTermScorer
specifier|private
specifier|static
class|class
name|AllTermScorer
extends|extends
name|Scorer
block|{
DECL|field|postings
specifier|final
name|PostingsEnum
name|postings
decl_stmt|;
DECL|field|docScorer
specifier|final
name|Similarity
operator|.
name|SimScorer
name|docScorer
decl_stmt|;
DECL|field|doc
name|int
name|doc
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|payloadBoost
name|float
name|payloadBoost
decl_stmt|;
DECL|method|AllTermScorer
name|AllTermScorer
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|PostingsEnum
name|postings
parameter_list|,
name|Similarity
operator|.
name|SimScorer
name|docScorer
parameter_list|)
block|{
name|super
argument_list|(
name|weight
argument_list|)
expr_stmt|;
name|this
operator|.
name|postings
operator|=
name|postings
expr_stmt|;
name|this
operator|.
name|docScorer
operator|=
name|docScorer
expr_stmt|;
block|}
DECL|method|payloadBoost
name|float
name|payloadBoost
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|doc
operator|!=
name|docID
argument_list|()
condition|)
block|{
specifier|final
name|int
name|freq
init|=
name|postings
operator|.
name|freq
argument_list|()
decl_stmt|;
name|payloadBoost
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|freq
condition|;
operator|++
name|i
control|)
block|{
name|postings
operator|.
name|nextPosition
argument_list|()
expr_stmt|;
specifier|final
name|BytesRef
name|payload
init|=
name|postings
operator|.
name|getPayload
argument_list|()
decl_stmt|;
name|float
name|boost
decl_stmt|;
if|if
condition|(
name|payload
operator|==
literal|null
condition|)
block|{
name|boost
operator|=
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|payload
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|boost
operator|=
name|SmallFloat
operator|.
name|byte315ToFloat
argument_list|(
name|payload
operator|.
name|bytes
index|[
name|payload
operator|.
name|offset
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|payload
operator|.
name|length
operator|==
literal|4
condition|)
block|{
comment|// TODO: for bw compat only, remove this in 6.0
name|boost
operator|=
name|PayloadHelper
operator|.
name|decodeFloat
argument_list|(
name|payload
operator|.
name|bytes
argument_list|,
name|payload
operator|.
name|offset
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Payloads are expected to have a length of 1 or 4 but got: "
operator|+
name|payload
argument_list|)
throw|;
block|}
name|payloadBoost
operator|+=
name|boost
expr_stmt|;
block|}
name|payloadBoost
operator|/=
name|freq
expr_stmt|;
name|doc
operator|=
name|docID
argument_list|()
expr_stmt|;
block|}
return|return
name|payloadBoost
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
return|return
name|payloadBoost
argument_list|()
operator|*
name|docScorer
operator|.
name|score
argument_list|(
name|postings
operator|.
name|docID
argument_list|()
argument_list|,
name|postings
operator|.
name|freq
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|freq
specifier|public
name|int
name|freq
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|postings
operator|.
name|freq
argument_list|()
return|;
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
name|postings
operator|.
name|docID
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|DocIdSetIterator
name|iterator
parameter_list|()
block|{
return|return
name|postings
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
return|return
operator|new
name|TermQuery
argument_list|(
name|term
argument_list|)
operator|.
name|toString
argument_list|(
name|field
argument_list|)
return|;
block|}
block|}
end_class

end_unit

