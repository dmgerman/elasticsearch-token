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
name|common
operator|.
name|bytes
operator|.
name|BytesReference
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
name|inject
operator|.
name|Inject
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|Lucene
operator|.
name|EarlyTerminatingCollector
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
name|index
operator|.
name|query
operator|.
name|ParsedQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|CompiledScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ExecutableScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
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
name|SuggestContextParser
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
name|NoisyChannelSpellChecker
operator|.
name|Result
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
DECL|class|PhraseSuggester
specifier|public
specifier|final
class|class
name|PhraseSuggester
extends|extends
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
DECL|field|SUGGESTION_TEMPLATE_VAR_NAME
specifier|private
specifier|static
specifier|final
name|String
name|SUGGESTION_TEMPLATE_VAR_NAME
init|=
literal|"suggestion"
decl_stmt|;
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
annotation|@
name|Inject
DECL|method|PhraseSuggester
specifier|public
name|PhraseSuggester
parameter_list|(
name|ScriptService
name|scriptService
parameter_list|)
block|{
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
block|}
comment|/*      * More Ideas:      *   - add ability to find whitespace problems -> we can build a poor mans decompounder with our index based on a automaton?      *   - add ability to build different error models maybe based on a confusion matrix?         *   - try to combine a token with its subsequent token to find / detect word splits (optional)      *      - for this to work we need some way to defined the position length of a candidate      *   - phonetic filters could be interesting here too for candidate selection      */
annotation|@
name|Override
DECL|method|innerExecute
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
name|innerExecute
parameter_list|(
name|String
name|name
parameter_list|,
name|PhraseSuggestionContext
name|suggestion
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|,
name|CharsRefBuilder
name|spare
parameter_list|)
throws|throws
name|IOException
block|{
name|double
name|realWordErrorLikelihood
init|=
name|suggestion
operator|.
name|realworldErrorLikelyhood
argument_list|()
decl_stmt|;
specifier|final
name|PhraseSuggestion
name|response
init|=
operator|new
name|PhraseSuggestion
argument_list|(
name|name
argument_list|,
name|suggestion
operator|.
name|getSize
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|IndexReader
name|indexReader
init|=
name|searcher
operator|.
name|getIndexReader
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
specifier|final
name|int
name|numGenerators
init|=
name|generators
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|CandidateGenerator
argument_list|>
name|gens
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|generators
operator|.
name|size
argument_list|()
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
name|numGenerators
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
name|Terms
name|terms
init|=
name|MultiFields
operator|.
name|getTerms
argument_list|(
name|indexReader
argument_list|,
name|generator
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
name|gens
operator|.
name|add
argument_list|(
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
name|size
argument_list|()
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
argument_list|,
name|terms
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|String
name|suggestField
init|=
name|suggestion
operator|.
name|getField
argument_list|()
decl_stmt|;
specifier|final
name|Terms
name|suggestTerms
init|=
name|MultiFields
operator|.
name|getTerms
argument_list|(
name|indexReader
argument_list|,
name|suggestField
argument_list|)
decl_stmt|;
if|if
condition|(
name|gens
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|&&
name|suggestTerms
operator|!=
literal|null
condition|)
block|{
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
argument_list|,
name|suggestion
operator|.
name|getTokenLimit
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
name|suggestTerms
argument_list|,
name|suggestField
argument_list|,
name|realWordErrorLikelihood
argument_list|,
name|separator
argument_list|)
decl_stmt|;
name|Result
name|checkerResult
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
name|suggestion
operator|.
name|getShardSize
argument_list|()
argument_list|,
name|gens
operator|.
name|toArray
argument_list|(
operator|new
name|CandidateGenerator
index|[
name|gens
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
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
name|wordScorer
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
name|PhraseSuggestion
operator|.
name|Entry
name|resultEntry
init|=
name|buildResultEntry
argument_list|(
name|suggestion
argument_list|,
name|spare
argument_list|,
name|checkerResult
operator|.
name|cutoffScore
argument_list|)
decl_stmt|;
name|response
operator|.
name|addTerm
argument_list|(
name|resultEntry
argument_list|)
expr_stmt|;
specifier|final
name|BytesRefBuilder
name|byteSpare
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
specifier|final
name|EarlyTerminatingCollector
name|collector
init|=
name|Lucene
operator|.
name|createExistsCollector
argument_list|()
decl_stmt|;
specifier|final
name|CompiledScript
name|collateScript
init|=
name|suggestion
operator|.
name|getCollateQueryScript
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|collatePrune
init|=
operator|(
name|collateScript
operator|!=
literal|null
operator|)
operator|&&
name|suggestion
operator|.
name|collatePrune
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
name|checkerResult
operator|.
name|corrections
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Correction
name|correction
init|=
name|checkerResult
operator|.
name|corrections
index|[
name|i
index|]
decl_stmt|;
name|spare
operator|.
name|copyUTF8Bytes
argument_list|(
name|correction
operator|.
name|join
argument_list|(
name|SEPARATOR
argument_list|,
name|byteSpare
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|boolean
name|collateMatch
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|collateScript
operator|!=
literal|null
condition|)
block|{
comment|// Checks if the template query collateScript yields any documents
comment|// from the index for a correction, collateMatch is updated
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
name|suggestion
operator|.
name|getCollateScriptParams
argument_list|()
decl_stmt|;
name|vars
operator|.
name|put
argument_list|(
name|SUGGESTION_TEMPLATE_VAR_NAME
argument_list|,
name|spare
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|ExecutableScript
name|executable
init|=
name|scriptService
operator|.
name|executable
argument_list|(
name|collateScript
argument_list|,
name|vars
argument_list|)
decl_stmt|;
specifier|final
name|BytesReference
name|querySource
init|=
operator|(
name|BytesReference
operator|)
name|executable
operator|.
name|run
argument_list|()
decl_stmt|;
specifier|final
name|ParsedQuery
name|parsedQuery
init|=
name|suggestion
operator|.
name|getQueryParserService
argument_list|()
operator|.
name|parse
argument_list|(
name|querySource
argument_list|)
decl_stmt|;
name|collateMatch
operator|=
name|Lucene
operator|.
name|exists
argument_list|(
name|searcher
argument_list|,
name|parsedQuery
operator|.
name|query
argument_list|()
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|collateMatch
operator|&&
operator|!
name|collatePrune
condition|)
block|{
continue|continue;
block|}
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
name|Text
name|highlighted
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|suggestion
operator|.
name|getPreTag
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|spare
operator|.
name|copyUTF8Bytes
argument_list|(
name|correction
operator|.
name|join
argument_list|(
name|SEPARATOR
argument_list|,
name|byteSpare
argument_list|,
name|suggestion
operator|.
name|getPreTag
argument_list|()
argument_list|,
name|suggestion
operator|.
name|getPostTag
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|highlighted
operator|=
operator|new
name|StringText
argument_list|(
name|spare
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|collatePrune
condition|)
block|{
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
name|highlighted
argument_list|,
call|(
name|float
call|)
argument_list|(
name|correction
operator|.
name|score
argument_list|)
argument_list|,
name|collateMatch
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|highlighted
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
block|}
block|}
else|else
block|{
name|response
operator|.
name|addTerm
argument_list|(
name|buildResultEntry
argument_list|(
name|suggestion
argument_list|,
name|spare
argument_list|,
name|Double
operator|.
name|MIN_VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|response
return|;
block|}
DECL|method|buildResultEntry
specifier|private
name|PhraseSuggestion
operator|.
name|Entry
name|buildResultEntry
parameter_list|(
name|PhraseSuggestionContext
name|suggestion
parameter_list|,
name|CharsRefBuilder
name|spare
parameter_list|,
name|double
name|cutoffScore
parameter_list|)
block|{
name|spare
operator|.
name|copyUTF8Bytes
argument_list|(
name|suggestion
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|PhraseSuggestion
operator|.
name|Entry
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
argument_list|()
argument_list|,
name|cutoffScore
argument_list|)
return|;
block|}
DECL|method|scriptService
name|ScriptService
name|scriptService
parameter_list|()
block|{
return|return
name|scriptService
return|;
block|}
annotation|@
name|Override
DECL|method|names
specifier|public
name|String
index|[]
name|names
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
literal|"phrase"
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|getContextParser
specifier|public
name|SuggestContextParser
name|getContextParser
parameter_list|()
block|{
return|return
operator|new
name|PhraseSuggestParser
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
end_class

end_unit
