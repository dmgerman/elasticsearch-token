begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|MultiFields
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
name|search
operator|.
name|spell
operator|.
name|SuggestWord
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
name|BytesRefBuilder
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
name|CharsRefBuilder
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
name|Collections
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
name|Set
import|;
end_import

begin_comment
comment|//TODO public for tests
end_comment

begin_class
DECL|class|DirectCandidateGenerator
specifier|public
specifier|final
class|class
name|DirectCandidateGenerator
extends|extends
name|CandidateGenerator
block|{
DECL|field|spellchecker
specifier|private
specifier|final
name|DirectSpellChecker
name|spellchecker
decl_stmt|;
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|field|suggestMode
specifier|private
specifier|final
name|SuggestMode
name|suggestMode
decl_stmt|;
DECL|field|termsEnum
specifier|private
specifier|final
name|TermsEnum
name|termsEnum
decl_stmt|;
DECL|field|reader
specifier|private
specifier|final
name|IndexReader
name|reader
decl_stmt|;
DECL|field|dictSize
specifier|private
specifier|final
name|long
name|dictSize
decl_stmt|;
DECL|field|logBase
specifier|private
specifier|final
name|double
name|logBase
init|=
literal|5
decl_stmt|;
DECL|field|frequencyPlateau
specifier|private
specifier|final
name|long
name|frequencyPlateau
decl_stmt|;
DECL|field|preFilter
specifier|private
specifier|final
name|Analyzer
name|preFilter
decl_stmt|;
DECL|field|postFilter
specifier|private
specifier|final
name|Analyzer
name|postFilter
decl_stmt|;
DECL|field|nonErrorLikelihood
specifier|private
specifier|final
name|double
name|nonErrorLikelihood
decl_stmt|;
DECL|field|useTotalTermFrequency
specifier|private
specifier|final
name|boolean
name|useTotalTermFrequency
decl_stmt|;
DECL|field|spare
specifier|private
specifier|final
name|CharsRefBuilder
name|spare
init|=
operator|new
name|CharsRefBuilder
argument_list|()
decl_stmt|;
DECL|field|byteSpare
specifier|private
specifier|final
name|BytesRefBuilder
name|byteSpare
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
DECL|field|numCandidates
specifier|private
specifier|final
name|int
name|numCandidates
decl_stmt|;
DECL|method|DirectCandidateGenerator
specifier|public
name|DirectCandidateGenerator
parameter_list|(
name|DirectSpellChecker
name|spellchecker
parameter_list|,
name|String
name|field
parameter_list|,
name|SuggestMode
name|suggestMode
parameter_list|,
name|IndexReader
name|reader
parameter_list|,
name|double
name|nonErrorLikelihood
parameter_list|,
name|int
name|numCandidates
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|spellchecker
argument_list|,
name|field
argument_list|,
name|suggestMode
argument_list|,
name|reader
argument_list|,
name|nonErrorLikelihood
argument_list|,
name|numCandidates
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|MultiFields
operator|.
name|getTerms
argument_list|(
name|reader
argument_list|,
name|field
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|DirectCandidateGenerator
specifier|public
name|DirectCandidateGenerator
parameter_list|(
name|DirectSpellChecker
name|spellchecker
parameter_list|,
name|String
name|field
parameter_list|,
name|SuggestMode
name|suggestMode
parameter_list|,
name|IndexReader
name|reader
parameter_list|,
name|double
name|nonErrorLikelihood
parameter_list|,
name|int
name|numCandidates
parameter_list|,
name|Analyzer
name|preFilter
parameter_list|,
name|Analyzer
name|postFilter
parameter_list|,
name|Terms
name|terms
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|terms
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"generator field ["
operator|+
name|field
operator|+
literal|"] doesn't exist"
argument_list|)
throw|;
block|}
name|this
operator|.
name|spellchecker
operator|=
name|spellchecker
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|numCandidates
operator|=
name|numCandidates
expr_stmt|;
name|this
operator|.
name|suggestMode
operator|=
name|suggestMode
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
specifier|final
name|long
name|dictSize
init|=
name|terms
operator|.
name|getSumTotalTermFreq
argument_list|()
decl_stmt|;
name|this
operator|.
name|useTotalTermFrequency
operator|=
name|dictSize
operator|!=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|dictSize
operator|=
name|dictSize
operator|==
operator|-
literal|1
condition|?
name|reader
operator|.
name|maxDoc
argument_list|()
else|:
name|dictSize
expr_stmt|;
name|this
operator|.
name|preFilter
operator|=
name|preFilter
expr_stmt|;
name|this
operator|.
name|postFilter
operator|=
name|postFilter
expr_stmt|;
name|this
operator|.
name|nonErrorLikelihood
operator|=
name|nonErrorLikelihood
expr_stmt|;
name|float
name|thresholdFrequency
init|=
name|spellchecker
operator|.
name|getThresholdFrequency
argument_list|()
decl_stmt|;
name|this
operator|.
name|frequencyPlateau
operator|=
name|thresholdFrequency
operator|>=
literal|1.0f
condition|?
operator|(
name|int
operator|)
name|thresholdFrequency
else|:
call|(
name|int
call|)
argument_list|(
name|dictSize
operator|*
name|thresholdFrequency
argument_list|)
expr_stmt|;
name|termsEnum
operator|=
name|terms
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
comment|/* (non-Javadoc)      * @see org.elasticsearch.search.suggest.phrase.CandidateGenerator#isKnownWord(org.apache.lucene.util.BytesRef)      */
annotation|@
name|Override
DECL|method|isKnownWord
specifier|public
name|boolean
name|isKnownWord
parameter_list|(
name|BytesRef
name|term
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|frequency
argument_list|(
name|term
argument_list|)
operator|>
literal|0
return|;
block|}
comment|/* (non-Javadoc)      * @see org.elasticsearch.search.suggest.phrase.CandidateGenerator#frequency(org.apache.lucene.util.BytesRef)      */
annotation|@
name|Override
DECL|method|frequency
specifier|public
name|long
name|frequency
parameter_list|(
name|BytesRef
name|term
parameter_list|)
throws|throws
name|IOException
block|{
name|term
operator|=
name|preFilter
argument_list|(
name|term
argument_list|,
name|spare
argument_list|,
name|byteSpare
argument_list|)
expr_stmt|;
return|return
name|internalFrequency
argument_list|(
name|term
argument_list|)
return|;
block|}
DECL|method|internalFrequency
specifier|public
name|long
name|internalFrequency
parameter_list|(
name|BytesRef
name|term
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|termsEnum
operator|.
name|seekExact
argument_list|(
name|term
argument_list|)
condition|)
block|{
return|return
name|useTotalTermFrequency
condition|?
name|termsEnum
operator|.
name|totalTermFreq
argument_list|()
else|:
name|termsEnum
operator|.
name|docFreq
argument_list|()
return|;
block|}
return|return
literal|0
return|;
block|}
DECL|method|getField
specifier|public
name|String
name|getField
parameter_list|()
block|{
return|return
name|field
return|;
block|}
comment|/* (non-Javadoc)      * @see org.elasticsearch.search.suggest.phrase.CandidateGenerator#drawCandidates(org.elasticsearch.search.suggest.phrase.DirectCandidateGenerator.CandidateSet, int)      */
annotation|@
name|Override
DECL|method|drawCandidates
specifier|public
name|CandidateSet
name|drawCandidates
parameter_list|(
name|CandidateSet
name|set
parameter_list|)
throws|throws
name|IOException
block|{
name|Candidate
name|original
init|=
name|set
operator|.
name|originalTerm
decl_stmt|;
name|BytesRef
name|term
init|=
name|preFilter
argument_list|(
name|original
operator|.
name|term
argument_list|,
name|spare
argument_list|,
name|byteSpare
argument_list|)
decl_stmt|;
specifier|final
name|long
name|frequency
init|=
name|original
operator|.
name|frequency
decl_stmt|;
name|spellchecker
operator|.
name|setThresholdFrequency
argument_list|(
name|this
operator|.
name|suggestMode
operator|==
name|SuggestMode
operator|.
name|SUGGEST_ALWAYS
condition|?
literal|0
else|:
name|thresholdFrequency
argument_list|(
name|frequency
argument_list|,
name|dictSize
argument_list|)
argument_list|)
expr_stmt|;
name|SuggestWord
index|[]
name|suggestSimilar
init|=
name|spellchecker
operator|.
name|suggestSimilar
argument_list|(
operator|new
name|Term
argument_list|(
name|field
argument_list|,
name|term
argument_list|)
argument_list|,
name|numCandidates
argument_list|,
name|reader
argument_list|,
name|this
operator|.
name|suggestMode
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Candidate
argument_list|>
name|candidates
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|suggestSimilar
operator|.
name|length
argument_list|)
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
name|suggestSimilar
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|SuggestWord
name|suggestWord
init|=
name|suggestSimilar
index|[
name|i
index|]
decl_stmt|;
name|BytesRef
name|candidate
init|=
operator|new
name|BytesRef
argument_list|(
name|suggestWord
operator|.
name|string
argument_list|)
decl_stmt|;
name|postFilter
argument_list|(
operator|new
name|Candidate
argument_list|(
name|candidate
argument_list|,
name|internalFrequency
argument_list|(
name|candidate
argument_list|)
argument_list|,
name|suggestWord
operator|.
name|score
argument_list|,
name|score
argument_list|(
name|suggestWord
operator|.
name|freq
argument_list|,
name|suggestWord
operator|.
name|score
argument_list|,
name|dictSize
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|,
name|spare
argument_list|,
name|byteSpare
argument_list|,
name|candidates
argument_list|)
expr_stmt|;
block|}
name|set
operator|.
name|addCandidates
argument_list|(
name|candidates
argument_list|)
expr_stmt|;
return|return
name|set
return|;
block|}
DECL|method|preFilter
specifier|protected
name|BytesRef
name|preFilter
parameter_list|(
specifier|final
name|BytesRef
name|term
parameter_list|,
specifier|final
name|CharsRefBuilder
name|spare
parameter_list|,
specifier|final
name|BytesRefBuilder
name|byteSpare
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|preFilter
operator|==
literal|null
condition|)
block|{
return|return
name|term
return|;
block|}
specifier|final
name|BytesRefBuilder
name|result
init|=
name|byteSpare
decl_stmt|;
name|SuggestUtils
operator|.
name|analyze
argument_list|(
name|preFilter
argument_list|,
name|term
argument_list|,
name|field
argument_list|,
operator|new
name|SuggestUtils
operator|.
name|TokenConsumer
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|nextToken
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|fillBytesRef
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|spare
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|get
argument_list|()
return|;
block|}
DECL|method|postFilter
specifier|protected
name|void
name|postFilter
parameter_list|(
specifier|final
name|Candidate
name|candidate
parameter_list|,
specifier|final
name|CharsRefBuilder
name|spare
parameter_list|,
name|BytesRefBuilder
name|byteSpare
parameter_list|,
specifier|final
name|List
argument_list|<
name|Candidate
argument_list|>
name|candidates
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|postFilter
operator|==
literal|null
condition|)
block|{
name|candidates
operator|.
name|add
argument_list|(
name|candidate
argument_list|)
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|BytesRefBuilder
name|result
init|=
name|byteSpare
decl_stmt|;
name|SuggestUtils
operator|.
name|analyze
argument_list|(
name|postFilter
argument_list|,
name|candidate
operator|.
name|term
argument_list|,
name|field
argument_list|,
operator|new
name|SuggestUtils
operator|.
name|TokenConsumer
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|nextToken
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|fillBytesRef
argument_list|(
name|result
argument_list|)
expr_stmt|;
if|if
condition|(
name|posIncAttr
operator|.
name|getPositionIncrement
argument_list|()
operator|>
literal|0
operator|&&
name|result
operator|.
name|get
argument_list|()
operator|.
name|bytesEquals
argument_list|(
name|candidate
operator|.
name|term
argument_list|)
condition|)
block|{
name|BytesRef
name|term
init|=
name|result
operator|.
name|toBytesRef
argument_list|()
decl_stmt|;
comment|// We should not use frequency(term) here because it will analyze the term again
comment|// If preFilter and postFilter are the same analyzer it would fail.
name|long
name|freq
init|=
name|internalFrequency
argument_list|(
name|term
argument_list|)
decl_stmt|;
name|candidates
operator|.
name|add
argument_list|(
operator|new
name|Candidate
argument_list|(
name|result
operator|.
name|toBytesRef
argument_list|()
argument_list|,
name|freq
argument_list|,
name|candidate
operator|.
name|stringDistance
argument_list|,
name|score
argument_list|(
name|candidate
operator|.
name|frequency
argument_list|,
name|candidate
operator|.
name|stringDistance
argument_list|,
name|dictSize
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|candidates
operator|.
name|add
argument_list|(
operator|new
name|Candidate
argument_list|(
name|result
operator|.
name|toBytesRef
argument_list|()
argument_list|,
name|candidate
operator|.
name|frequency
argument_list|,
name|nonErrorLikelihood
argument_list|,
name|score
argument_list|(
name|candidate
operator|.
name|frequency
argument_list|,
name|candidate
operator|.
name|stringDistance
argument_list|,
name|dictSize
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|,
name|spare
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|score
specifier|private
name|double
name|score
parameter_list|(
name|long
name|frequency
parameter_list|,
name|double
name|errorScore
parameter_list|,
name|long
name|dictionarySize
parameter_list|)
block|{
return|return
name|errorScore
operator|*
operator|(
operator|(
operator|(
name|double
operator|)
name|frequency
operator|+
literal|1
operator|)
operator|/
operator|(
operator|(
name|double
operator|)
name|dictionarySize
operator|+
literal|1
operator|)
operator|)
return|;
block|}
DECL|method|thresholdFrequency
specifier|protected
name|long
name|thresholdFrequency
parameter_list|(
name|long
name|termFrequency
parameter_list|,
name|long
name|dictionarySize
parameter_list|)
block|{
if|if
condition|(
name|termFrequency
operator|>
literal|0
condition|)
block|{
return|return
operator|(
name|long
operator|)
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|Math
operator|.
name|round
argument_list|(
name|termFrequency
operator|*
operator|(
name|Math
operator|.
name|log10
argument_list|(
name|termFrequency
operator|-
name|frequencyPlateau
argument_list|)
operator|*
operator|(
literal|1.0
operator|/
name|Math
operator|.
name|log10
argument_list|(
name|logBase
argument_list|)
operator|)
operator|)
operator|+
literal|1
argument_list|)
argument_list|)
return|;
block|}
return|return
literal|0
return|;
block|}
DECL|class|CandidateSet
specifier|public
specifier|static
class|class
name|CandidateSet
block|{
DECL|field|candidates
specifier|public
name|Candidate
index|[]
name|candidates
decl_stmt|;
DECL|field|originalTerm
specifier|public
specifier|final
name|Candidate
name|originalTerm
decl_stmt|;
DECL|method|CandidateSet
specifier|public
name|CandidateSet
parameter_list|(
name|Candidate
index|[]
name|candidates
parameter_list|,
name|Candidate
name|originalTerm
parameter_list|)
block|{
name|this
operator|.
name|candidates
operator|=
name|candidates
expr_stmt|;
name|this
operator|.
name|originalTerm
operator|=
name|originalTerm
expr_stmt|;
block|}
DECL|method|addCandidates
specifier|public
name|void
name|addCandidates
parameter_list|(
name|List
argument_list|<
name|Candidate
argument_list|>
name|candidates
parameter_list|)
block|{
comment|// Merge new candidates into existing ones,
comment|// deduping:
specifier|final
name|Set
argument_list|<
name|Candidate
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|candidates
argument_list|)
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
name|this
operator|.
name|candidates
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|set
operator|.
name|add
argument_list|(
name|this
operator|.
name|candidates
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|candidates
operator|=
name|set
operator|.
name|toArray
argument_list|(
operator|new
name|Candidate
index|[
name|set
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
comment|// Sort strongest to weakest:
name|Arrays
operator|.
name|sort
argument_list|(
name|this
operator|.
name|candidates
argument_list|,
name|Collections
operator|.
name|reverseOrder
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|addOneCandidate
specifier|public
name|void
name|addOneCandidate
parameter_list|(
name|Candidate
name|candidate
parameter_list|)
block|{
name|Candidate
index|[]
name|candidates
init|=
operator|new
name|Candidate
index|[
name|this
operator|.
name|candidates
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|this
operator|.
name|candidates
argument_list|,
literal|0
argument_list|,
name|candidates
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|candidates
operator|.
name|length
argument_list|)
expr_stmt|;
name|candidates
index|[
name|candidates
operator|.
name|length
operator|-
literal|1
index|]
operator|=
name|candidate
expr_stmt|;
name|this
operator|.
name|candidates
operator|=
name|candidates
expr_stmt|;
block|}
block|}
DECL|class|Candidate
specifier|public
specifier|static
class|class
name|Candidate
implements|implements
name|Comparable
argument_list|<
name|Candidate
argument_list|>
block|{
DECL|field|EMPTY
specifier|public
specifier|static
specifier|final
name|Candidate
index|[]
name|EMPTY
init|=
operator|new
name|Candidate
index|[
literal|0
index|]
decl_stmt|;
DECL|field|term
specifier|public
specifier|final
name|BytesRef
name|term
decl_stmt|;
DECL|field|stringDistance
specifier|public
specifier|final
name|double
name|stringDistance
decl_stmt|;
DECL|field|frequency
specifier|public
specifier|final
name|long
name|frequency
decl_stmt|;
DECL|field|score
specifier|public
specifier|final
name|double
name|score
decl_stmt|;
DECL|field|userInput
specifier|public
specifier|final
name|boolean
name|userInput
decl_stmt|;
DECL|method|Candidate
specifier|public
name|Candidate
parameter_list|(
name|BytesRef
name|term
parameter_list|,
name|long
name|frequency
parameter_list|,
name|double
name|stringDistance
parameter_list|,
name|double
name|score
parameter_list|,
name|boolean
name|userInput
parameter_list|)
block|{
name|this
operator|.
name|frequency
operator|=
name|frequency
expr_stmt|;
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
name|this
operator|.
name|stringDistance
operator|=
name|stringDistance
expr_stmt|;
name|this
operator|.
name|score
operator|=
name|score
expr_stmt|;
name|this
operator|.
name|userInput
operator|=
name|userInput
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Candidate [term="
operator|+
name|term
operator|.
name|utf8ToString
argument_list|()
operator|+
literal|", stringDistance="
operator|+
name|stringDistance
operator|+
literal|", score="
operator|+
name|score
operator|+
literal|", frequency="
operator|+
name|frequency
operator|+
operator|(
name|userInput
condition|?
literal|", userInput"
else|:
literal|""
operator|)
operator|+
literal|"]"
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
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|term
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|term
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|result
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
name|obj
operator|==
literal|null
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
name|Candidate
name|other
init|=
operator|(
name|Candidate
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|term
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|term
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
name|term
operator|.
name|equals
argument_list|(
name|other
operator|.
name|term
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
comment|/** Lower scores sort first; if scores are equal, then later (zzz) terms sort first */
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|Candidate
name|other
parameter_list|)
block|{
if|if
condition|(
name|score
operator|==
name|other
operator|.
name|score
condition|)
block|{
comment|// Later (zzz) terms sort before earlier (aaa) terms:
return|return
name|other
operator|.
name|term
operator|.
name|compareTo
argument_list|(
name|term
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Double
operator|.
name|compare
argument_list|(
name|score
argument_list|,
name|other
operator|.
name|score
argument_list|)
return|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|createCandidate
specifier|public
name|Candidate
name|createCandidate
parameter_list|(
name|BytesRef
name|term
parameter_list|,
name|long
name|frequency
parameter_list|,
name|double
name|channelScore
parameter_list|,
name|boolean
name|userInput
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Candidate
argument_list|(
name|term
argument_list|,
name|frequency
argument_list|,
name|channelScore
argument_list|,
name|score
argument_list|(
name|frequency
argument_list|,
name|channelScore
argument_list|,
name|dictSize
argument_list|)
argument_list|,
name|userInput
argument_list|)
return|;
block|}
block|}
end_class

end_unit

