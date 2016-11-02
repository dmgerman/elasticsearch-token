begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.queryparser.classic
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queryparser
operator|.
name|classic
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
name|search
operator|.
name|MultiTermQuery
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
name|unit
operator|.
name|Fuzziness
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Encapsulates settings that affect query_string parsing via {@link MapperQueryParser}  */
end_comment

begin_class
DECL|class|QueryParserSettings
specifier|public
class|class
name|QueryParserSettings
block|{
DECL|field|queryString
specifier|private
specifier|final
name|String
name|queryString
decl_stmt|;
DECL|field|defaultField
specifier|private
name|String
name|defaultField
decl_stmt|;
DECL|field|fieldsAndWeights
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|fieldsAndWeights
decl_stmt|;
DECL|field|defaultOperator
specifier|private
name|QueryParser
operator|.
name|Operator
name|defaultOperator
decl_stmt|;
DECL|field|analyzer
specifier|private
name|Analyzer
name|analyzer
decl_stmt|;
DECL|field|forceAnalyzer
specifier|private
name|boolean
name|forceAnalyzer
decl_stmt|;
DECL|field|quoteAnalyzer
specifier|private
name|Analyzer
name|quoteAnalyzer
decl_stmt|;
DECL|field|forceQuoteAnalyzer
specifier|private
name|boolean
name|forceQuoteAnalyzer
decl_stmt|;
DECL|field|quoteFieldSuffix
specifier|private
name|String
name|quoteFieldSuffix
decl_stmt|;
DECL|field|autoGeneratePhraseQueries
specifier|private
name|boolean
name|autoGeneratePhraseQueries
decl_stmt|;
DECL|field|allowLeadingWildcard
specifier|private
name|boolean
name|allowLeadingWildcard
decl_stmt|;
DECL|field|analyzeWildcard
specifier|private
name|boolean
name|analyzeWildcard
decl_stmt|;
DECL|field|lowercaseExpandedTerms
specifier|private
name|boolean
name|lowercaseExpandedTerms
decl_stmt|;
DECL|field|enablePositionIncrements
specifier|private
name|boolean
name|enablePositionIncrements
decl_stmt|;
DECL|field|locale
specifier|private
name|Locale
name|locale
decl_stmt|;
DECL|field|fuzziness
specifier|private
name|Fuzziness
name|fuzziness
decl_stmt|;
DECL|field|fuzzyPrefixLength
specifier|private
name|int
name|fuzzyPrefixLength
decl_stmt|;
DECL|field|fuzzyMaxExpansions
specifier|private
name|int
name|fuzzyMaxExpansions
decl_stmt|;
DECL|field|fuzzyRewriteMethod
specifier|private
name|MultiTermQuery
operator|.
name|RewriteMethod
name|fuzzyRewriteMethod
decl_stmt|;
DECL|field|phraseSlop
specifier|private
name|int
name|phraseSlop
decl_stmt|;
DECL|field|useDisMax
specifier|private
name|boolean
name|useDisMax
decl_stmt|;
DECL|field|tieBreaker
specifier|private
name|float
name|tieBreaker
decl_stmt|;
DECL|field|rewriteMethod
specifier|private
name|MultiTermQuery
operator|.
name|RewriteMethod
name|rewriteMethod
decl_stmt|;
DECL|field|lenient
specifier|private
name|boolean
name|lenient
decl_stmt|;
DECL|field|timeZone
specifier|private
name|DateTimeZone
name|timeZone
decl_stmt|;
comment|/** To limit effort spent determinizing regexp queries. */
DECL|field|maxDeterminizedStates
specifier|private
name|int
name|maxDeterminizedStates
decl_stmt|;
DECL|field|splitOnWhitespace
specifier|private
name|boolean
name|splitOnWhitespace
decl_stmt|;
DECL|method|QueryParserSettings
specifier|public
name|QueryParserSettings
parameter_list|(
name|String
name|queryString
parameter_list|)
block|{
name|this
operator|.
name|queryString
operator|=
name|queryString
expr_stmt|;
block|}
DECL|method|queryString
specifier|public
name|String
name|queryString
parameter_list|()
block|{
return|return
name|queryString
return|;
block|}
DECL|method|defaultField
specifier|public
name|String
name|defaultField
parameter_list|()
block|{
return|return
name|defaultField
return|;
block|}
DECL|method|defaultField
specifier|public
name|void
name|defaultField
parameter_list|(
name|String
name|defaultField
parameter_list|)
block|{
name|this
operator|.
name|defaultField
operator|=
name|defaultField
expr_stmt|;
block|}
DECL|method|fieldsAndWeights
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|fieldsAndWeights
parameter_list|()
block|{
return|return
name|fieldsAndWeights
return|;
block|}
DECL|method|fieldsAndWeights
specifier|public
name|void
name|fieldsAndWeights
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|fieldsAndWeights
parameter_list|)
block|{
name|this
operator|.
name|fieldsAndWeights
operator|=
name|fieldsAndWeights
expr_stmt|;
block|}
DECL|method|defaultOperator
specifier|public
name|QueryParser
operator|.
name|Operator
name|defaultOperator
parameter_list|()
block|{
return|return
name|defaultOperator
return|;
block|}
DECL|method|defaultOperator
specifier|public
name|void
name|defaultOperator
parameter_list|(
name|QueryParser
operator|.
name|Operator
name|defaultOperator
parameter_list|)
block|{
name|this
operator|.
name|defaultOperator
operator|=
name|defaultOperator
expr_stmt|;
block|}
DECL|method|autoGeneratePhraseQueries
specifier|public
name|boolean
name|autoGeneratePhraseQueries
parameter_list|()
block|{
return|return
name|autoGeneratePhraseQueries
return|;
block|}
DECL|method|autoGeneratePhraseQueries
specifier|public
name|void
name|autoGeneratePhraseQueries
parameter_list|(
name|boolean
name|autoGeneratePhraseQueries
parameter_list|)
block|{
name|this
operator|.
name|autoGeneratePhraseQueries
operator|=
name|autoGeneratePhraseQueries
expr_stmt|;
block|}
DECL|method|maxDeterminizedStates
specifier|public
name|int
name|maxDeterminizedStates
parameter_list|()
block|{
return|return
name|maxDeterminizedStates
return|;
block|}
DECL|method|maxDeterminizedStates
specifier|public
name|void
name|maxDeterminizedStates
parameter_list|(
name|int
name|maxDeterminizedStates
parameter_list|)
block|{
name|this
operator|.
name|maxDeterminizedStates
operator|=
name|maxDeterminizedStates
expr_stmt|;
block|}
DECL|method|allowLeadingWildcard
specifier|public
name|boolean
name|allowLeadingWildcard
parameter_list|()
block|{
return|return
name|allowLeadingWildcard
return|;
block|}
DECL|method|allowLeadingWildcard
specifier|public
name|void
name|allowLeadingWildcard
parameter_list|(
name|boolean
name|allowLeadingWildcard
parameter_list|)
block|{
name|this
operator|.
name|allowLeadingWildcard
operator|=
name|allowLeadingWildcard
expr_stmt|;
block|}
DECL|method|lowercaseExpandedTerms
specifier|public
name|boolean
name|lowercaseExpandedTerms
parameter_list|()
block|{
return|return
name|lowercaseExpandedTerms
return|;
block|}
DECL|method|lowercaseExpandedTerms
specifier|public
name|void
name|lowercaseExpandedTerms
parameter_list|(
name|boolean
name|lowercaseExpandedTerms
parameter_list|)
block|{
name|this
operator|.
name|lowercaseExpandedTerms
operator|=
name|lowercaseExpandedTerms
expr_stmt|;
block|}
DECL|method|enablePositionIncrements
specifier|public
name|boolean
name|enablePositionIncrements
parameter_list|()
block|{
return|return
name|enablePositionIncrements
return|;
block|}
DECL|method|enablePositionIncrements
specifier|public
name|void
name|enablePositionIncrements
parameter_list|(
name|boolean
name|enablePositionIncrements
parameter_list|)
block|{
name|this
operator|.
name|enablePositionIncrements
operator|=
name|enablePositionIncrements
expr_stmt|;
block|}
DECL|method|phraseSlop
specifier|public
name|int
name|phraseSlop
parameter_list|()
block|{
return|return
name|phraseSlop
return|;
block|}
DECL|method|phraseSlop
specifier|public
name|void
name|phraseSlop
parameter_list|(
name|int
name|phraseSlop
parameter_list|)
block|{
name|this
operator|.
name|phraseSlop
operator|=
name|phraseSlop
expr_stmt|;
block|}
DECL|method|fuzzyPrefixLength
specifier|public
name|int
name|fuzzyPrefixLength
parameter_list|()
block|{
return|return
name|fuzzyPrefixLength
return|;
block|}
DECL|method|fuzzyPrefixLength
specifier|public
name|void
name|fuzzyPrefixLength
parameter_list|(
name|int
name|fuzzyPrefixLength
parameter_list|)
block|{
name|this
operator|.
name|fuzzyPrefixLength
operator|=
name|fuzzyPrefixLength
expr_stmt|;
block|}
DECL|method|fuzzyMaxExpansions
specifier|public
name|int
name|fuzzyMaxExpansions
parameter_list|()
block|{
return|return
name|fuzzyMaxExpansions
return|;
block|}
DECL|method|fuzzyMaxExpansions
specifier|public
name|void
name|fuzzyMaxExpansions
parameter_list|(
name|int
name|fuzzyMaxExpansions
parameter_list|)
block|{
name|this
operator|.
name|fuzzyMaxExpansions
operator|=
name|fuzzyMaxExpansions
expr_stmt|;
block|}
DECL|method|fuzzyRewriteMethod
specifier|public
name|MultiTermQuery
operator|.
name|RewriteMethod
name|fuzzyRewriteMethod
parameter_list|()
block|{
return|return
name|fuzzyRewriteMethod
return|;
block|}
DECL|method|fuzzyRewriteMethod
specifier|public
name|void
name|fuzzyRewriteMethod
parameter_list|(
name|MultiTermQuery
operator|.
name|RewriteMethod
name|fuzzyRewriteMethod
parameter_list|)
block|{
name|this
operator|.
name|fuzzyRewriteMethod
operator|=
name|fuzzyRewriteMethod
expr_stmt|;
block|}
DECL|method|defaultAnalyzer
specifier|public
name|void
name|defaultAnalyzer
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
name|this
operator|.
name|forceAnalyzer
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|forceAnalyzer
specifier|public
name|void
name|forceAnalyzer
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
name|this
operator|.
name|forceAnalyzer
operator|=
literal|true
expr_stmt|;
block|}
DECL|method|analyzer
specifier|public
name|Analyzer
name|analyzer
parameter_list|()
block|{
return|return
name|analyzer
return|;
block|}
DECL|method|forceAnalyzer
specifier|public
name|boolean
name|forceAnalyzer
parameter_list|()
block|{
return|return
name|forceAnalyzer
return|;
block|}
DECL|method|defaultQuoteAnalyzer
specifier|public
name|void
name|defaultQuoteAnalyzer
parameter_list|(
name|Analyzer
name|quoteAnalyzer
parameter_list|)
block|{
name|this
operator|.
name|quoteAnalyzer
operator|=
name|quoteAnalyzer
expr_stmt|;
name|this
operator|.
name|forceQuoteAnalyzer
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|forceQuoteAnalyzer
specifier|public
name|void
name|forceQuoteAnalyzer
parameter_list|(
name|Analyzer
name|quoteAnalyzer
parameter_list|)
block|{
name|this
operator|.
name|quoteAnalyzer
operator|=
name|quoteAnalyzer
expr_stmt|;
name|this
operator|.
name|forceQuoteAnalyzer
operator|=
literal|true
expr_stmt|;
block|}
DECL|method|quoteAnalyzer
specifier|public
name|Analyzer
name|quoteAnalyzer
parameter_list|()
block|{
return|return
name|quoteAnalyzer
return|;
block|}
DECL|method|forceQuoteAnalyzer
specifier|public
name|boolean
name|forceQuoteAnalyzer
parameter_list|()
block|{
return|return
name|forceQuoteAnalyzer
return|;
block|}
DECL|method|analyzeWildcard
specifier|public
name|boolean
name|analyzeWildcard
parameter_list|()
block|{
return|return
name|this
operator|.
name|analyzeWildcard
return|;
block|}
DECL|method|analyzeWildcard
specifier|public
name|void
name|analyzeWildcard
parameter_list|(
name|boolean
name|analyzeWildcard
parameter_list|)
block|{
name|this
operator|.
name|analyzeWildcard
operator|=
name|analyzeWildcard
expr_stmt|;
block|}
DECL|method|rewriteMethod
specifier|public
name|MultiTermQuery
operator|.
name|RewriteMethod
name|rewriteMethod
parameter_list|()
block|{
return|return
name|this
operator|.
name|rewriteMethod
return|;
block|}
DECL|method|rewriteMethod
specifier|public
name|void
name|rewriteMethod
parameter_list|(
name|MultiTermQuery
operator|.
name|RewriteMethod
name|rewriteMethod
parameter_list|)
block|{
name|this
operator|.
name|rewriteMethod
operator|=
name|rewriteMethod
expr_stmt|;
block|}
DECL|method|quoteFieldSuffix
specifier|public
name|void
name|quoteFieldSuffix
parameter_list|(
name|String
name|quoteFieldSuffix
parameter_list|)
block|{
name|this
operator|.
name|quoteFieldSuffix
operator|=
name|quoteFieldSuffix
expr_stmt|;
block|}
DECL|method|quoteFieldSuffix
specifier|public
name|String
name|quoteFieldSuffix
parameter_list|()
block|{
return|return
name|this
operator|.
name|quoteFieldSuffix
return|;
block|}
DECL|method|lenient
specifier|public
name|void
name|lenient
parameter_list|(
name|boolean
name|lenient
parameter_list|)
block|{
name|this
operator|.
name|lenient
operator|=
name|lenient
expr_stmt|;
block|}
DECL|method|lenient
specifier|public
name|boolean
name|lenient
parameter_list|()
block|{
return|return
name|this
operator|.
name|lenient
return|;
block|}
DECL|method|tieBreaker
specifier|public
name|float
name|tieBreaker
parameter_list|()
block|{
return|return
name|tieBreaker
return|;
block|}
DECL|method|tieBreaker
specifier|public
name|void
name|tieBreaker
parameter_list|(
name|float
name|tieBreaker
parameter_list|)
block|{
name|this
operator|.
name|tieBreaker
operator|=
name|tieBreaker
expr_stmt|;
block|}
DECL|method|useDisMax
specifier|public
name|boolean
name|useDisMax
parameter_list|()
block|{
return|return
name|useDisMax
return|;
block|}
DECL|method|useDisMax
specifier|public
name|void
name|useDisMax
parameter_list|(
name|boolean
name|useDisMax
parameter_list|)
block|{
name|this
operator|.
name|useDisMax
operator|=
name|useDisMax
expr_stmt|;
block|}
DECL|method|locale
specifier|public
name|void
name|locale
parameter_list|(
name|Locale
name|locale
parameter_list|)
block|{
name|this
operator|.
name|locale
operator|=
name|locale
expr_stmt|;
block|}
DECL|method|locale
specifier|public
name|Locale
name|locale
parameter_list|()
block|{
return|return
name|this
operator|.
name|locale
return|;
block|}
DECL|method|timeZone
specifier|public
name|void
name|timeZone
parameter_list|(
name|DateTimeZone
name|timeZone
parameter_list|)
block|{
name|this
operator|.
name|timeZone
operator|=
name|timeZone
expr_stmt|;
block|}
DECL|method|timeZone
specifier|public
name|DateTimeZone
name|timeZone
parameter_list|()
block|{
return|return
name|this
operator|.
name|timeZone
return|;
block|}
DECL|method|fuzziness
specifier|public
name|void
name|fuzziness
parameter_list|(
name|Fuzziness
name|fuzziness
parameter_list|)
block|{
name|this
operator|.
name|fuzziness
operator|=
name|fuzziness
expr_stmt|;
block|}
DECL|method|fuzziness
specifier|public
name|Fuzziness
name|fuzziness
parameter_list|()
block|{
return|return
name|fuzziness
return|;
block|}
DECL|method|splitOnWhitespace
specifier|public
name|void
name|splitOnWhitespace
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|this
operator|.
name|splitOnWhitespace
operator|=
name|value
expr_stmt|;
block|}
DECL|method|splitOnWhitespace
specifier|public
name|boolean
name|splitOnWhitespace
parameter_list|()
block|{
return|return
name|splitOnWhitespace
return|;
block|}
block|}
end_class

end_unit

