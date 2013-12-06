begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectFloatOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|queryparser
operator|.
name|classic
operator|.
name|MapperQueryParser
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
name|queryparser
operator|.
name|classic
operator|.
name|QueryParserSettings
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
name|BooleanQuery
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
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
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
name|search
operator|.
name|Queries
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
name|regex
operator|.
name|Regex
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
name|settings
operator|.
name|Settings
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
name|xcontent
operator|.
name|XContentParser
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
name|analysis
operator|.
name|NamedAnalyzer
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
name|support
operator|.
name|QueryParsers
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
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
operator|.
name|Queries
operator|.
name|fixNegativeQueryIfNeeded
import|;
end_import

begin_import
import|import static
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
name|Queries
operator|.
name|optimizeQuery
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|QueryStringQueryParser
specifier|public
class|class
name|QueryStringQueryParser
implements|implements
name|QueryParser
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"query_string"
decl_stmt|;
DECL|field|defaultAnalyzeWildcard
specifier|private
specifier|final
name|boolean
name|defaultAnalyzeWildcard
decl_stmt|;
DECL|field|defaultAllowLeadingWildcard
specifier|private
specifier|final
name|boolean
name|defaultAllowLeadingWildcard
decl_stmt|;
annotation|@
name|Inject
DECL|method|QueryStringQueryParser
specifier|public
name|QueryStringQueryParser
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|defaultAnalyzeWildcard
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"indices.query.query_string.analyze_wildcard"
argument_list|,
name|QueryParserSettings
operator|.
name|DEFAULT_ANALYZE_WILDCARD
argument_list|)
expr_stmt|;
name|this
operator|.
name|defaultAllowLeadingWildcard
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"indices.query.query_string.allowLeadingWildcard"
argument_list|,
name|QueryParserSettings
operator|.
name|DEFAULT_ALLOW_LEADING_WILDCARD
argument_list|)
expr_stmt|;
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
name|NAME
block|,
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|NAME
argument_list|)
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Query
name|parse
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|QueryParsingException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|QueryParserSettings
name|qpSettings
init|=
operator|new
name|QueryParserSettings
argument_list|()
decl_stmt|;
name|qpSettings
operator|.
name|defaultField
argument_list|(
name|parseContext
operator|.
name|defaultField
argument_list|()
argument_list|)
expr_stmt|;
name|qpSettings
operator|.
name|lenient
argument_list|(
name|parseContext
operator|.
name|queryStringLenient
argument_list|()
argument_list|)
expr_stmt|;
name|qpSettings
operator|.
name|analyzeWildcard
argument_list|(
name|defaultAnalyzeWildcard
argument_list|)
expr_stmt|;
name|qpSettings
operator|.
name|allowLeadingWildcard
argument_list|(
name|defaultAllowLeadingWildcard
argument_list|)
expr_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
literal|"fields"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|String
name|fField
init|=
literal|null
decl_stmt|;
name|float
name|fBoost
init|=
operator|-
literal|1
decl_stmt|;
name|char
index|[]
name|text
init|=
name|parser
operator|.
name|textCharacters
argument_list|()
decl_stmt|;
name|int
name|end
init|=
name|parser
operator|.
name|textOffset
argument_list|()
operator|+
name|parser
operator|.
name|textLength
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|parser
operator|.
name|textOffset
argument_list|()
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|text
index|[
name|i
index|]
operator|==
literal|'^'
condition|)
block|{
name|int
name|relativeLocation
init|=
name|i
operator|-
name|parser
operator|.
name|textOffset
argument_list|()
decl_stmt|;
name|fField
operator|=
operator|new
name|String
argument_list|(
name|text
argument_list|,
name|parser
operator|.
name|textOffset
argument_list|()
argument_list|,
name|relativeLocation
argument_list|)
expr_stmt|;
name|fBoost
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
operator|new
name|String
argument_list|(
name|text
argument_list|,
name|i
operator|+
literal|1
argument_list|,
name|parser
operator|.
name|textLength
argument_list|()
operator|-
name|relativeLocation
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|fField
operator|==
literal|null
condition|)
block|{
name|fField
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|qpSettings
operator|.
name|fields
argument_list|()
operator|==
literal|null
condition|)
block|{
name|qpSettings
operator|.
name|fields
argument_list|(
name|Lists
operator|.
expr|<
name|String
operator|>
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Regex
operator|.
name|isSimpleMatchPattern
argument_list|(
name|fField
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|field
range|:
name|parseContext
operator|.
name|mapperService
argument_list|()
operator|.
name|simpleMatchToIndexNames
argument_list|(
name|fField
argument_list|)
control|)
block|{
name|qpSettings
operator|.
name|fields
argument_list|()
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
if|if
condition|(
name|fBoost
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|qpSettings
operator|.
name|boosts
argument_list|()
operator|==
literal|null
condition|)
block|{
name|qpSettings
operator|.
name|boosts
argument_list|(
operator|new
name|ObjectFloatOpenHashMap
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|qpSettings
operator|.
name|boosts
argument_list|()
operator|.
name|put
argument_list|(
name|field
argument_list|,
name|fBoost
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|qpSettings
operator|.
name|fields
argument_list|()
operator|.
name|add
argument_list|(
name|fField
argument_list|)
expr_stmt|;
if|if
condition|(
name|fBoost
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|qpSettings
operator|.
name|boosts
argument_list|()
operator|==
literal|null
condition|)
block|{
name|qpSettings
operator|.
name|boosts
argument_list|(
operator|new
name|ObjectFloatOpenHashMap
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|qpSettings
operator|.
name|boosts
argument_list|()
operator|.
name|put
argument_list|(
name|fField
argument_list|,
name|fBoost
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[query_string] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"query"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|queryString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"default_field"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"defaultField"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|defaultField
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"default_operator"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"defaultOperator"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|String
name|op
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"or"
operator|.
name|equalsIgnoreCase
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|defaultOperator
argument_list|(
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queryparser
operator|.
name|classic
operator|.
name|QueryParser
operator|.
name|Operator
operator|.
name|OR
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"and"
operator|.
name|equalsIgnoreCase
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|defaultOperator
argument_list|(
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queryparser
operator|.
name|classic
operator|.
name|QueryParser
operator|.
name|Operator
operator|.
name|AND
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"Query default operator ["
operator|+
name|op
operator|+
literal|"] is not allowed"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"analyzer"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|NamedAnalyzer
name|analyzer
init|=
name|parseContext
operator|.
name|analysisService
argument_list|()
operator|.
name|analyzer
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[query_string] analyzer ["
operator|+
name|parser
operator|.
name|text
argument_list|()
operator|+
literal|"] not found"
argument_list|)
throw|;
block|}
name|qpSettings
operator|.
name|forcedAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"quote_analyzer"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"quoteAnalyzer"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|NamedAnalyzer
name|analyzer
init|=
name|parseContext
operator|.
name|analysisService
argument_list|()
operator|.
name|analyzer
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[query_string] quote_analyzer ["
operator|+
name|parser
operator|.
name|text
argument_list|()
operator|+
literal|"] not found"
argument_list|)
throw|;
block|}
name|qpSettings
operator|.
name|forcedQuoteAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"allow_leading_wildcard"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"allowLeadingWildcard"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|allowLeadingWildcard
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"auto_generate_phrase_queries"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"autoGeneratePhraseQueries"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|autoGeneratePhraseQueries
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"lowercase_expanded_terms"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"lowercaseExpandedTerms"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|lowercaseExpandedTerms
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"enable_position_increments"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"enablePositionIncrements"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|enablePositionIncrements
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"escape"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|escape
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"use_dis_max"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"useDisMax"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|useDisMax
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"fuzzy_prefix_length"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"fuzzyPrefixLength"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|fuzzyPrefixLength
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"fuzzy_max_expansions"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"fuzzyMaxExpansions"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|fuzzyMaxExpansions
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"fuzzy_rewrite"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"fuzzyRewrite"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|fuzzyRewriteMethod
argument_list|(
name|QueryParsers
operator|.
name|parseRewriteMethod
argument_list|(
name|parser
operator|.
name|textOrNull
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"phrase_slop"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"phraseSlop"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|phraseSlop
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"fuzzy_min_sim"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"fuzzyMinSim"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|fuzzyMinSim
argument_list|(
name|parser
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"boost"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|boost
argument_list|(
name|parser
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"tie_breaker"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"tieBreaker"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|tieBreaker
argument_list|(
name|parser
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"analyze_wildcard"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"analyzeWildcard"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|analyzeWildcard
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"rewrite"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|rewriteMethod
argument_list|(
name|QueryParsers
operator|.
name|parseRewriteMethod
argument_list|(
name|parser
operator|.
name|textOrNull
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"minimum_should_match"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"minimumShouldMatch"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|minimumShouldMatch
argument_list|(
name|parser
operator|.
name|textOrNull
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"quote_field_suffix"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"quoteFieldSuffix"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|quoteFieldSuffix
argument_list|(
name|parser
operator|.
name|textOrNull
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"lenient"
operator|.
name|equalsIgnoreCase
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|qpSettings
operator|.
name|lenient
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_name"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|queryName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[query_string] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|qpSettings
operator|.
name|queryString
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"query_string must be provided with a [query]"
argument_list|)
throw|;
block|}
name|qpSettings
operator|.
name|defaultAnalyzer
argument_list|(
name|parseContext
operator|.
name|mapperService
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
name|qpSettings
operator|.
name|defaultQuoteAnalyzer
argument_list|(
name|parseContext
operator|.
name|mapperService
argument_list|()
operator|.
name|searchQuoteAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|qpSettings
operator|.
name|escape
argument_list|()
condition|)
block|{
name|qpSettings
operator|.
name|queryString
argument_list|(
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queryparser
operator|.
name|classic
operator|.
name|QueryParser
operator|.
name|escape
argument_list|(
name|qpSettings
operator|.
name|queryString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|qpSettings
operator|.
name|queryTypes
argument_list|(
name|parseContext
operator|.
name|queryTypes
argument_list|()
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|parseContext
operator|.
name|indexCache
argument_list|()
operator|.
name|queryParserCache
argument_list|()
operator|.
name|get
argument_list|(
name|qpSettings
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|queryName
operator|!=
literal|null
condition|)
block|{
name|parseContext
operator|.
name|addNamedQuery
argument_list|(
name|queryName
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
name|MapperQueryParser
name|queryParser
init|=
name|parseContext
operator|.
name|queryParser
argument_list|(
name|qpSettings
argument_list|)
decl_stmt|;
try|try
block|{
name|query
operator|=
name|queryParser
operator|.
name|parse
argument_list|(
name|qpSettings
operator|.
name|queryString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|qpSettings
operator|.
name|boost
argument_list|()
operator|!=
name|QueryParserSettings
operator|.
name|DEFAULT_BOOST
condition|)
block|{
name|query
operator|.
name|setBoost
argument_list|(
name|query
operator|.
name|getBoost
argument_list|()
operator|*
name|qpSettings
operator|.
name|boost
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|query
operator|=
name|optimizeQuery
argument_list|(
name|fixNegativeQueryIfNeeded
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|query
operator|instanceof
name|BooleanQuery
condition|)
block|{
name|Queries
operator|.
name|applyMinimumShouldMatch
argument_list|(
operator|(
name|BooleanQuery
operator|)
name|query
argument_list|,
name|qpSettings
operator|.
name|minimumShouldMatch
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|parseContext
operator|.
name|indexCache
argument_list|()
operator|.
name|queryParserCache
argument_list|()
operator|.
name|put
argument_list|(
name|qpSettings
argument_list|,
name|query
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryName
operator|!=
literal|null
condition|)
block|{
name|parseContext
operator|.
name|addNamedQuery
argument_list|(
name|queryName
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queryparser
operator|.
name|classic
operator|.
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"Failed to parse query ["
operator|+
name|qpSettings
operator|.
name|queryString
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

