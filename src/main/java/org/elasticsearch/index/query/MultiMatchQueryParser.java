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
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|BooleanClause
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
name|query
operator|.
name|support
operator|.
name|QueryParsers
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
name|search
operator|.
name|MatchQuery
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
name|search
operator|.
name|MultiMatchQuery
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Same ad {@link MatchQueryParser} but has support for multiple fields.  */
end_comment

begin_class
DECL|class|MultiMatchQueryParser
specifier|public
class|class
name|MultiMatchQueryParser
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
literal|"multi_match"
decl_stmt|;
annotation|@
name|Inject
DECL|method|MultiMatchQueryParser
specifier|public
name|MultiMatchQueryParser
parameter_list|()
block|{     }
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
literal|"multiMatch"
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
name|text
init|=
literal|null
decl_stmt|;
name|float
name|boost
init|=
literal|1.0f
decl_stmt|;
name|MatchQuery
operator|.
name|Type
name|type
init|=
name|MatchQuery
operator|.
name|Type
operator|.
name|BOOLEAN
decl_stmt|;
name|MultiMatchQuery
name|multiMatchQuery
init|=
operator|new
name|MultiMatchQuery
argument_list|(
name|parseContext
argument_list|)
decl_stmt|;
name|String
name|minimumShouldMatch
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|fieldNameWithBoosts
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
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
name|Float
name|fBoost
init|=
literal|null
decl_stmt|;
name|char
index|[]
name|fieldText
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
name|fieldText
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
name|fieldText
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
name|fieldText
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
name|fieldNameWithBoosts
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
else|else
block|{
name|fieldNameWithBoosts
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
name|text
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"type"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|String
name|tStr
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"boolean"
operator|.
name|equals
argument_list|(
name|tStr
argument_list|)
condition|)
block|{
name|type
operator|=
name|MatchQuery
operator|.
name|Type
operator|.
name|BOOLEAN
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"phrase"
operator|.
name|equals
argument_list|(
name|tStr
argument_list|)
condition|)
block|{
name|type
operator|=
name|MatchQuery
operator|.
name|Type
operator|.
name|PHRASE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"phrase_prefix"
operator|.
name|equals
argument_list|(
name|tStr
argument_list|)
operator|||
literal|"phrasePrefix"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|type
operator|=
name|MatchQuery
operator|.
name|Type
operator|.
name|PHRASE_PREFIX
expr_stmt|;
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
name|String
name|analyzer
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
name|parseContext
operator|.
name|analysisService
argument_list|()
operator|.
name|analyzer
argument_list|(
name|analyzer
argument_list|)
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
literal|"[match] analyzer ["
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
name|multiMatchQuery
operator|.
name|setAnalyzer
argument_list|(
name|analyzer
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
name|boost
operator|=
name|parser
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"slop"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
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
name|multiMatchQuery
operator|.
name|setPhraseSlop
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
literal|"fuzziness"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|multiMatchQuery
operator|.
name|setFuzziness
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
literal|"prefix_length"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"prefixLength"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|multiMatchQuery
operator|.
name|setFuzzyPrefixLength
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
literal|"max_expansions"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"maxExpansions"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|multiMatchQuery
operator|.
name|setMaxExpansions
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
literal|"operator"
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
name|multiMatchQuery
operator|.
name|setOccur
argument_list|(
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
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
name|multiMatchQuery
operator|.
name|setOccur
argument_list|(
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
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
literal|"text query requires operator to be either 'and' or 'or', not ["
operator|+
name|op
operator|+
literal|"]"
argument_list|)
throw|;
block|}
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
name|minimumShouldMatch
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
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
name|multiMatchQuery
operator|.
name|setRewriteMethod
argument_list|(
name|QueryParsers
operator|.
name|parseRewriteMethod
argument_list|(
name|parser
operator|.
name|textOrNull
argument_list|()
argument_list|,
literal|null
argument_list|)
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
name|multiMatchQuery
operator|.
name|setFuzzyRewriteMethod
argument_list|(
name|QueryParsers
operator|.
name|parseRewriteMethod
argument_list|(
name|parser
operator|.
name|textOrNull
argument_list|()
argument_list|,
literal|null
argument_list|)
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
name|multiMatchQuery
operator|.
name|setUseDisMax
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
name|multiMatchQuery
operator|.
name|setTieBreaker
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
literal|"lenient"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|multiMatchQuery
operator|.
name|setLenient
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
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
literal|"[match] query does not support ["
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
name|text
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
literal|"No text specified for match_all query"
argument_list|)
throw|;
block|}
if|if
condition|(
name|fieldNameWithBoosts
operator|.
name|isEmpty
argument_list|()
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
literal|"No fields specified for match_all query"
argument_list|)
throw|;
block|}
name|Query
name|query
init|=
name|multiMatchQuery
operator|.
name|parse
argument_list|(
name|type
argument_list|,
name|fieldNameWithBoosts
argument_list|,
name|text
argument_list|)
decl_stmt|;
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
name|minimumShouldMatch
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
return|return
name|query
return|;
block|}
block|}
end_class

end_unit

