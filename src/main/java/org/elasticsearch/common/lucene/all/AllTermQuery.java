begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|index
operator|.
name|AtomicReaderContext
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
name|DocsAndPositionsEnum
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
name|ComplexExplanation
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
name|spans
operator|.
name|SpanScorer
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
name|spans
operator|.
name|SpanTermQuery
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
name|spans
operator|.
name|SpanWeight
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
name|spans
operator|.
name|TermSpans
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import static
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
operator|.
name|decodeFloat
import|;
end_import

begin_comment
comment|/**  * A term query that takes all payload boost values into account.  *  *  */
end_comment

begin_class
DECL|class|AllTermQuery
specifier|public
class|class
name|AllTermQuery
extends|extends
name|SpanTermQuery
block|{
DECL|method|AllTermQuery
specifier|public
name|AllTermQuery
parameter_list|(
name|Term
name|term
parameter_list|)
block|{
name|super
argument_list|(
name|term
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
name|IndexSearcher
name|searcher
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|AllTermWeight
argument_list|(
name|this
argument_list|,
name|searcher
argument_list|)
return|;
block|}
DECL|class|AllTermWeight
specifier|protected
class|class
name|AllTermWeight
extends|extends
name|SpanWeight
block|{
DECL|method|AllTermWeight
specifier|public
name|AllTermWeight
parameter_list|(
name|AllTermQuery
name|query
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|query
argument_list|,
name|searcher
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|scorer
specifier|public
name|AllTermSpanScorer
name|scorer
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|,
name|boolean
name|scoreDocsInOrder
parameter_list|,
name|boolean
name|topScorer
parameter_list|,
name|Bits
name|acceptDocs
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|stats
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|SimScorer
name|sloppySimScorer
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
return|return
operator|new
name|AllTermSpanScorer
argument_list|(
operator|(
name|TermSpans
operator|)
name|query
operator|.
name|getSpans
argument_list|(
name|context
argument_list|,
name|acceptDocs
argument_list|,
name|termContexts
argument_list|)
argument_list|,
name|this
argument_list|,
name|sloppySimScorer
argument_list|)
return|;
block|}
DECL|class|AllTermSpanScorer
specifier|protected
class|class
name|AllTermSpanScorer
extends|extends
name|SpanScorer
block|{
DECL|field|positions
specifier|protected
name|DocsAndPositionsEnum
name|positions
decl_stmt|;
DECL|field|payloadScore
specifier|protected
name|float
name|payloadScore
decl_stmt|;
DECL|field|payloadsSeen
specifier|protected
name|int
name|payloadsSeen
decl_stmt|;
DECL|method|AllTermSpanScorer
specifier|public
name|AllTermSpanScorer
parameter_list|(
name|TermSpans
name|spans
parameter_list|,
name|Weight
name|weight
parameter_list|,
name|Similarity
operator|.
name|SimScorer
name|docScorer
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|spans
argument_list|,
name|weight
argument_list|,
name|docScorer
argument_list|)
expr_stmt|;
name|positions
operator|=
name|spans
operator|.
name|getPostings
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setFreqCurrentDoc
specifier|protected
name|boolean
name|setFreqCurrentDoc
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|more
condition|)
block|{
return|return
literal|false
return|;
block|}
name|doc
operator|=
name|spans
operator|.
name|doc
argument_list|()
expr_stmt|;
name|freq
operator|=
literal|0.0f
expr_stmt|;
name|numMatches
operator|=
literal|0
expr_stmt|;
name|payloadScore
operator|=
literal|0
expr_stmt|;
name|payloadsSeen
operator|=
literal|0
expr_stmt|;
do|do
block|{
name|int
name|matchLength
init|=
name|spans
operator|.
name|end
argument_list|()
operator|-
name|spans
operator|.
name|start
argument_list|()
decl_stmt|;
name|freq
operator|+=
name|docScorer
operator|.
name|computeSlopFactor
argument_list|(
name|matchLength
argument_list|)
expr_stmt|;
name|numMatches
operator|++
expr_stmt|;
name|processPayload
argument_list|()
expr_stmt|;
name|more
operator|=
name|spans
operator|.
name|next
argument_list|()
expr_stmt|;
comment|// this moves positions to the next match
block|}
do|while
condition|(
name|more
operator|&&
operator|(
name|doc
operator|==
name|spans
operator|.
name|doc
argument_list|()
operator|)
condition|)
do|;
return|return
literal|true
return|;
block|}
DECL|method|processPayload
specifier|protected
name|void
name|processPayload
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|BytesRef
name|payload
decl_stmt|;
if|if
condition|(
operator|(
name|payload
operator|=
name|positions
operator|.
name|getPayload
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|payloadScore
operator|+=
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
name|payloadsSeen
operator|++
expr_stmt|;
block|}
else|else
block|{
comment|// zero out the payload?
block|}
block|}
comment|/**              * @return {@link #getSpanScore()} * {@link #getPayloadScore()}              * @throws IOException              */
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
name|getSpanScore
argument_list|()
operator|*
name|getPayloadScore
argument_list|()
return|;
block|}
comment|/**              * Returns the SpanScorer score only.              *<p/>              * Should not be overridden without good cause!              *              * @return the score for just the Span part w/o the payload              * @throws IOException              * @see #score()              */
DECL|method|getSpanScore
specifier|protected
name|float
name|getSpanScore
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|score
argument_list|()
return|;
block|}
comment|/**              * The score for the payload              */
DECL|method|getPayloadScore
specifier|protected
name|float
name|getPayloadScore
parameter_list|()
block|{
return|return
name|payloadsSeen
operator|>
literal|0
condition|?
operator|(
name|payloadScore
operator|/
name|payloadsSeen
operator|)
else|:
literal|1
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|explain
specifier|public
name|Explanation
name|explain
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|,
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|AllTermSpanScorer
name|scorer
init|=
name|scorer
argument_list|(
name|context
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getLiveDocs
argument_list|()
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
name|freq
init|=
name|scorer
operator|.
name|sloppyFreq
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
name|ComplexExplanation
name|inner
init|=
operator|new
name|ComplexExplanation
argument_list|()
decl_stmt|;
name|inner
operator|.
name|setDescription
argument_list|(
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
literal|"], result of:"
argument_list|)
expr_stmt|;
name|Explanation
name|scoreExplanation
init|=
name|docScorer
operator|.
name|explain
argument_list|(
name|doc
argument_list|,
operator|new
name|Explanation
argument_list|(
name|freq
argument_list|,
literal|"phraseFreq="
operator|+
name|freq
argument_list|)
argument_list|)
decl_stmt|;
name|inner
operator|.
name|addDetail
argument_list|(
name|scoreExplanation
argument_list|)
expr_stmt|;
name|inner
operator|.
name|setValue
argument_list|(
name|scoreExplanation
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|inner
operator|.
name|setMatch
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ComplexExplanation
name|result
init|=
operator|new
name|ComplexExplanation
argument_list|()
decl_stmt|;
name|result
operator|.
name|addDetail
argument_list|(
name|inner
argument_list|)
expr_stmt|;
name|Explanation
name|payloadBoost
init|=
operator|new
name|Explanation
argument_list|()
decl_stmt|;
name|result
operator|.
name|addDetail
argument_list|(
name|payloadBoost
argument_list|)
expr_stmt|;
specifier|final
name|float
name|payloadScore
init|=
name|scorer
operator|.
name|getPayloadScore
argument_list|()
decl_stmt|;
name|payloadBoost
operator|.
name|setValue
argument_list|(
name|payloadScore
argument_list|)
expr_stmt|;
comment|// GSI: I suppose we could toString the payload, but I don't think that
comment|// would be a good idea
name|payloadBoost
operator|.
name|setDescription
argument_list|(
literal|"allPayload(...)"
argument_list|)
expr_stmt|;
name|result
operator|.
name|setValue
argument_list|(
name|inner
operator|.
name|getValue
argument_list|()
operator|*
name|payloadScore
argument_list|)
expr_stmt|;
name|result
operator|.
name|setDescription
argument_list|(
literal|"btq, product of:"
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
return|return
operator|new
name|ComplexExplanation
argument_list|(
literal|false
argument_list|,
literal|0.0f
argument_list|,
literal|"no matching term"
argument_list|)
return|;
block|}
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
name|super
operator|.
name|hashCode
argument_list|()
operator|+
literal|1
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
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

