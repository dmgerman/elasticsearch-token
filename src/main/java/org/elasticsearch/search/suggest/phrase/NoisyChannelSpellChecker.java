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
name|shingle
operator|.
name|ShingleFilter
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
name|synonym
operator|.
name|SynonymFilter
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
name|TypeAttribute
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|CharsRef
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
name|UnicodeUtil
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
name|FastCharArrayReader
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
name|CandidateSet
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
name|List
import|;
end_import

begin_comment
comment|//TODO public for tests
end_comment

begin_class
DECL|class|NoisyChannelSpellChecker
specifier|public
specifier|final
class|class
name|NoisyChannelSpellChecker
block|{
DECL|field|REAL_WORD_LIKELYHOOD
specifier|public
specifier|static
specifier|final
name|double
name|REAL_WORD_LIKELYHOOD
init|=
literal|0.95d
decl_stmt|;
DECL|field|DEFAULT_TOKEN_LIMIT
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_TOKEN_LIMIT
init|=
literal|10
decl_stmt|;
DECL|field|realWordLikelihood
specifier|private
specifier|final
name|double
name|realWordLikelihood
decl_stmt|;
DECL|field|requireUnigram
specifier|private
specifier|final
name|boolean
name|requireUnigram
decl_stmt|;
DECL|field|tokenLimit
specifier|private
specifier|final
name|int
name|tokenLimit
decl_stmt|;
DECL|method|NoisyChannelSpellChecker
specifier|public
name|NoisyChannelSpellChecker
parameter_list|()
block|{
name|this
argument_list|(
name|REAL_WORD_LIKELYHOOD
argument_list|)
expr_stmt|;
block|}
DECL|method|NoisyChannelSpellChecker
specifier|public
name|NoisyChannelSpellChecker
parameter_list|(
name|double
name|nonErrorLikelihood
parameter_list|)
block|{
name|this
argument_list|(
name|nonErrorLikelihood
argument_list|,
literal|true
argument_list|,
name|DEFAULT_TOKEN_LIMIT
argument_list|)
expr_stmt|;
block|}
DECL|method|NoisyChannelSpellChecker
specifier|public
name|NoisyChannelSpellChecker
parameter_list|(
name|double
name|nonErrorLikelihood
parameter_list|,
name|boolean
name|requireUnigram
parameter_list|,
name|int
name|tokenLimit
parameter_list|)
block|{
name|this
operator|.
name|realWordLikelihood
operator|=
name|nonErrorLikelihood
expr_stmt|;
name|this
operator|.
name|requireUnigram
operator|=
name|requireUnigram
expr_stmt|;
name|this
operator|.
name|tokenLimit
operator|=
name|tokenLimit
expr_stmt|;
block|}
DECL|method|getCorrections
specifier|public
name|Result
name|getCorrections
parameter_list|(
name|TokenStream
name|stream
parameter_list|,
specifier|final
name|CandidateGenerator
name|generator
parameter_list|,
name|float
name|maxErrors
parameter_list|,
name|int
name|numCorrections
parameter_list|,
name|IndexReader
name|reader
parameter_list|,
name|WordScorer
name|wordScorer
parameter_list|,
name|BytesRef
name|separator
parameter_list|,
name|float
name|confidence
parameter_list|,
name|int
name|gramSize
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|List
argument_list|<
name|CandidateSet
argument_list|>
name|candidateSetsList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|SuggestUtils
operator|.
name|analyze
argument_list|(
name|stream
argument_list|,
operator|new
name|SuggestUtils
operator|.
name|TokenConsumer
argument_list|()
block|{
name|CandidateSet
name|currentSet
init|=
literal|null
decl_stmt|;
specifier|private
name|TypeAttribute
name|typeAttribute
decl_stmt|;
specifier|private
specifier|final
name|BytesRef
name|termsRef
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|anyUnigram
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|anyTokens
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|(
name|TokenStream
name|stream
parameter_list|)
block|{
name|super
operator|.
name|reset
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|typeAttribute
operator|=
name|stream
operator|.
name|addAttribute
argument_list|(
name|TypeAttribute
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nextToken
parameter_list|()
throws|throws
name|IOException
block|{
name|anyTokens
operator|=
literal|true
expr_stmt|;
name|BytesRef
name|term
init|=
name|fillBytesRef
argument_list|(
name|termsRef
argument_list|)
decl_stmt|;
if|if
condition|(
name|requireUnigram
operator|&&
name|typeAttribute
operator|.
name|type
argument_list|()
operator|==
name|ShingleFilter
operator|.
name|DEFAULT_TOKEN_TYPE
condition|)
block|{
return|return;
block|}
name|anyUnigram
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|posIncAttr
operator|.
name|getPositionIncrement
argument_list|()
operator|==
literal|0
operator|&&
name|typeAttribute
operator|.
name|type
argument_list|()
operator|==
name|SynonymFilter
operator|.
name|TYPE_SYNONYM
condition|)
block|{
assert|assert
name|currentSet
operator|!=
literal|null
assert|;
name|long
name|freq
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|(
name|freq
operator|=
name|generator
operator|.
name|frequency
argument_list|(
name|term
argument_list|)
operator|)
operator|>
literal|0
condition|)
block|{
name|currentSet
operator|.
name|addOneCandidate
argument_list|(
name|generator
operator|.
name|createCandidate
argument_list|(
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|term
argument_list|)
argument_list|,
name|freq
argument_list|,
name|realWordLikelihood
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|currentSet
operator|!=
literal|null
condition|)
block|{
name|candidateSetsList
operator|.
name|add
argument_list|(
name|currentSet
argument_list|)
expr_stmt|;
block|}
name|currentSet
operator|=
operator|new
name|CandidateSet
argument_list|(
name|Candidate
operator|.
name|EMPTY
argument_list|,
name|generator
operator|.
name|createCandidate
argument_list|(
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|term
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|end
parameter_list|()
block|{
if|if
condition|(
name|currentSet
operator|!=
literal|null
condition|)
block|{
name|candidateSetsList
operator|.
name|add
argument_list|(
name|currentSet
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|requireUnigram
operator|&&
operator|!
name|anyUnigram
operator|&&
name|anyTokens
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"At least one unigram is required but all tokens were ngrams"
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
name|candidateSetsList
operator|.
name|isEmpty
argument_list|()
operator|||
name|candidateSetsList
operator|.
name|size
argument_list|()
operator|>=
name|tokenLimit
condition|)
block|{
return|return
name|Result
operator|.
name|EMPTY
return|;
block|}
for|for
control|(
name|CandidateSet
name|candidateSet
range|:
name|candidateSetsList
control|)
block|{
name|generator
operator|.
name|drawCandidates
argument_list|(
name|candidateSet
argument_list|)
expr_stmt|;
block|}
name|double
name|cutoffScore
init|=
name|Double
operator|.
name|MIN_VALUE
decl_stmt|;
name|CandidateScorer
name|scorer
init|=
operator|new
name|CandidateScorer
argument_list|(
name|wordScorer
argument_list|,
name|numCorrections
argument_list|,
name|gramSize
argument_list|)
decl_stmt|;
name|CandidateSet
index|[]
name|candidateSets
init|=
name|candidateSetsList
operator|.
name|toArray
argument_list|(
operator|new
name|CandidateSet
index|[
name|candidateSetsList
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|confidence
operator|>
literal|0.0
condition|)
block|{
name|Candidate
index|[]
name|candidates
init|=
operator|new
name|Candidate
index|[
name|candidateSets
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
name|candidates
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|candidates
index|[
name|i
index|]
operator|=
name|candidateSets
index|[
name|i
index|]
operator|.
name|originalTerm
expr_stmt|;
block|}
name|double
name|inputPhraseScore
init|=
name|scorer
operator|.
name|score
argument_list|(
name|candidates
argument_list|,
name|candidateSets
argument_list|)
decl_stmt|;
name|cutoffScore
operator|=
name|inputPhraseScore
operator|*
name|confidence
expr_stmt|;
block|}
name|Correction
index|[]
name|findBestCandiates
init|=
name|scorer
operator|.
name|findBestCandiates
argument_list|(
name|candidateSets
argument_list|,
name|maxErrors
argument_list|,
name|cutoffScore
argument_list|)
decl_stmt|;
return|return
operator|new
name|Result
argument_list|(
name|findBestCandiates
argument_list|,
name|cutoffScore
argument_list|)
return|;
block|}
DECL|method|getCorrections
specifier|public
name|Result
name|getCorrections
parameter_list|(
name|Analyzer
name|analyzer
parameter_list|,
name|BytesRef
name|query
parameter_list|,
name|CandidateGenerator
name|generator
parameter_list|,
name|float
name|maxErrors
parameter_list|,
name|int
name|numCorrections
parameter_list|,
name|IndexReader
name|reader
parameter_list|,
name|String
name|analysisField
parameter_list|,
name|WordScorer
name|scorer
parameter_list|,
name|float
name|confidence
parameter_list|,
name|int
name|gramSize
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getCorrections
argument_list|(
name|tokenStream
argument_list|(
name|analyzer
argument_list|,
name|query
argument_list|,
operator|new
name|CharsRef
argument_list|()
argument_list|,
name|analysisField
argument_list|)
argument_list|,
name|generator
argument_list|,
name|maxErrors
argument_list|,
name|numCorrections
argument_list|,
name|reader
argument_list|,
name|scorer
argument_list|,
operator|new
name|BytesRef
argument_list|(
literal|" "
argument_list|)
argument_list|,
name|confidence
argument_list|,
name|gramSize
argument_list|)
return|;
block|}
DECL|method|tokenStream
specifier|public
name|TokenStream
name|tokenStream
parameter_list|(
name|Analyzer
name|analyzer
parameter_list|,
name|BytesRef
name|query
parameter_list|,
name|CharsRef
name|spare
parameter_list|,
name|String
name|field
parameter_list|)
throws|throws
name|IOException
block|{
name|UnicodeUtil
operator|.
name|UTF8toUTF16
argument_list|(
name|query
argument_list|,
name|spare
argument_list|)
expr_stmt|;
return|return
name|analyzer
operator|.
name|tokenStream
argument_list|(
name|field
argument_list|,
operator|new
name|FastCharArrayReader
argument_list|(
name|spare
operator|.
name|chars
argument_list|,
name|spare
operator|.
name|offset
argument_list|,
name|spare
operator|.
name|length
argument_list|)
argument_list|)
return|;
block|}
DECL|class|Result
specifier|public
specifier|static
class|class
name|Result
block|{
DECL|field|EMPTY
specifier|public
specifier|static
specifier|final
name|Result
name|EMPTY
init|=
operator|new
name|Result
argument_list|(
name|Correction
operator|.
name|EMPTY
argument_list|,
name|Double
operator|.
name|MIN_VALUE
argument_list|)
decl_stmt|;
DECL|field|corrections
specifier|public
specifier|final
name|Correction
index|[]
name|corrections
decl_stmt|;
DECL|field|cutoffScore
specifier|public
specifier|final
name|double
name|cutoffScore
decl_stmt|;
DECL|method|Result
specifier|public
name|Result
parameter_list|(
name|Correction
index|[]
name|corrections
parameter_list|,
name|double
name|cutoffScore
parameter_list|)
block|{
name|this
operator|.
name|corrections
operator|=
name|corrections
expr_stmt|;
name|this
operator|.
name|cutoffScore
operator|=
name|cutoffScore
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

