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
name|java
operator|.
name|util
operator|.
name|List
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
name|text
operator|.
name|StringText
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
name|text
operator|.
name|Text
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
name|internal
operator|.
name|SearchContext
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
name|Suggest
operator|.
name|Suggestion
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
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
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
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
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
name|Suggester
import|;
end_import

begin_class
DECL|class|PhraseSuggester
specifier|final
class|class
name|PhraseSuggester
implements|implements
name|Suggester
argument_list|<
name|PhraseSuggestionContext
argument_list|>
block|{
DECL|field|SEPARATOR
specifier|private
specifier|final
name|BytesRef
name|SEPARATOR
init|=
operator|new
name|BytesRef
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
comment|/*      * More Ideas:      *   - add ability to find whitespace problems -> we can build a poor mans decompounder with our index based on a automaton?      *   - add ability to build different error models maybe based on a confusion matrix?         *   - try to combine a token with its subsequent token to find / detect word splits (optional)      *      - for this to work we need some way to defined the position length of a candidate      *   - phonetic filters could be interesting here too for candidate selection      */
annotation|@
name|Override
DECL|method|execute
specifier|public
name|Suggestion
argument_list|<
name|?
extends|extends
name|Entry
argument_list|<
name|?
extends|extends
name|Option
argument_list|>
argument_list|>
name|execute
parameter_list|(
name|String
name|name
parameter_list|,
name|PhraseSuggestionContext
name|suggestion
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|CharsRef
name|spare
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|IndexReader
name|indexReader
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
decl_stmt|;
name|double
name|realWordErrorLikelihood
init|=
name|suggestion
operator|.
name|realworldErrorLikelyhood
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|PhraseSuggestionContext
operator|.
name|DirectCandidateGenerator
argument_list|>
name|generators
init|=
name|suggestion
operator|.
name|generators
argument_list|()
decl_stmt|;
name|CandidateGenerator
index|[]
name|gens
init|=
operator|new
name|CandidateGenerator
index|[
name|generators
operator|.
name|size
argument_list|()
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
name|gens
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|PhraseSuggestionContext
operator|.
name|DirectCandidateGenerator
name|generator
init|=
name|generators
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|DirectSpellChecker
name|directSpellChecker
init|=
name|SuggestUtils
operator|.
name|getDirectSpellChecker
argument_list|(
name|generator
argument_list|)
decl_stmt|;
name|gens
index|[
name|i
index|]
operator|=
operator|new
name|DirectCandidateGenerator
argument_list|(
name|directSpellChecker
argument_list|,
name|generator
operator|.
name|field
argument_list|()
argument_list|,
name|generator
operator|.
name|suggestMode
argument_list|()
argument_list|,
name|indexReader
argument_list|,
name|realWordErrorLikelihood
argument_list|,
name|generator
operator|.
name|preFilter
argument_list|()
argument_list|,
name|generator
operator|.
name|postFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|NoisyChannelSpellChecker
name|checker
init|=
operator|new
name|NoisyChannelSpellChecker
argument_list|(
name|realWordErrorLikelihood
argument_list|,
name|suggestion
operator|.
name|getRequireUnigram
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|BytesRef
name|separator
init|=
name|suggestion
operator|.
name|separator
argument_list|()
decl_stmt|;
name|TokenStream
name|stream
init|=
name|checker
operator|.
name|tokenStream
argument_list|(
name|suggestion
operator|.
name|getAnalyzer
argument_list|()
argument_list|,
name|suggestion
operator|.
name|getText
argument_list|()
argument_list|,
name|spare
argument_list|,
name|suggestion
operator|.
name|getField
argument_list|()
argument_list|)
decl_stmt|;
name|WordScorer
name|wordScorer
init|=
name|suggestion
operator|.
name|model
argument_list|()
operator|.
name|newScorer
argument_list|(
name|indexReader
argument_list|,
name|suggestion
operator|.
name|getField
argument_list|()
argument_list|,
name|realWordErrorLikelihood
argument_list|,
name|separator
argument_list|)
decl_stmt|;
name|Correction
index|[]
name|corrections
init|=
name|checker
operator|.
name|getCorrections
argument_list|(
name|stream
argument_list|,
operator|new
name|MultiCandidateGeneratorWrapper
argument_list|(
name|gens
argument_list|)
argument_list|,
name|suggestion
operator|.
name|getShardSize
argument_list|()
argument_list|,
name|suggestion
operator|.
name|maxErrors
argument_list|()
argument_list|,
name|suggestion
operator|.
name|getShardSize
argument_list|()
argument_list|,
name|indexReader
argument_list|,
name|wordScorer
argument_list|,
name|separator
argument_list|,
name|suggestion
operator|.
name|confidence
argument_list|()
argument_list|,
name|suggestion
operator|.
name|gramSize
argument_list|()
argument_list|)
decl_stmt|;
name|UnicodeUtil
operator|.
name|UTF8toUTF16
argument_list|(
name|suggestion
operator|.
name|getText
argument_list|()
argument_list|,
name|spare
argument_list|)
expr_stmt|;
name|Suggestion
operator|.
name|Entry
argument_list|<
name|Option
argument_list|>
name|resultEntry
init|=
operator|new
name|Suggestion
operator|.
name|Entry
argument_list|<
name|Option
argument_list|>
argument_list|(
operator|new
name|StringText
argument_list|(
name|spare
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
literal|0
argument_list|,
name|spare
operator|.
name|length
argument_list|)
decl_stmt|;
name|BytesRef
name|byteSpare
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
for|for
control|(
name|Correction
name|correction
range|:
name|corrections
control|)
block|{
name|UnicodeUtil
operator|.
name|UTF8toUTF16
argument_list|(
name|correction
operator|.
name|join
argument_list|(
name|SEPARATOR
argument_list|,
name|byteSpare
argument_list|)
argument_list|,
name|spare
argument_list|)
expr_stmt|;
name|Text
name|phrase
init|=
operator|new
name|StringText
argument_list|(
name|spare
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|resultEntry
operator|.
name|addOption
argument_list|(
operator|new
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|(
name|phrase
argument_list|,
call|(
name|float
call|)
argument_list|(
name|correction
operator|.
name|score
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Suggestion
argument_list|<
name|Entry
argument_list|<
name|Option
argument_list|>
argument_list|>
name|response
init|=
operator|new
name|Suggestion
argument_list|<
name|Entry
argument_list|<
name|Option
argument_list|>
argument_list|>
argument_list|(
name|name
argument_list|,
name|suggestion
operator|.
name|getSize
argument_list|()
argument_list|)
decl_stmt|;
name|response
operator|.
name|addTerm
argument_list|(
name|resultEntry
argument_list|)
expr_stmt|;
return|return
name|response
return|;
block|}
block|}
end_class

end_unit

