begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|ParsingException
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
name|search
operator|.
name|MatchQuery
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
name|HashMap
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
comment|/**  * Same as {@link MatchQueryParser} but has support for multiple fields.  */
end_comment

begin_class
DECL|class|MultiMatchQueryParser
specifier|public
class|class
name|MultiMatchQueryParser
implements|implements
name|QueryParser
argument_list|<
name|MultiMatchQueryBuilder
argument_list|>
block|{
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
name|MultiMatchQueryBuilder
operator|.
name|NAME
block|,
literal|"multiMatch"
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|MultiMatchQueryBuilder
name|fromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|Object
name|value
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|fieldsBoosts
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|MultiMatchQueryBuilder
operator|.
name|Type
name|type
init|=
name|MultiMatchQueryBuilder
operator|.
name|DEFAULT_TYPE
decl_stmt|;
name|String
name|analyzer
init|=
literal|null
decl_stmt|;
name|int
name|slop
init|=
name|MultiMatchQueryBuilder
operator|.
name|DEFAULT_PHRASE_SLOP
decl_stmt|;
name|Fuzziness
name|fuzziness
init|=
literal|null
decl_stmt|;
name|int
name|prefixLength
init|=
name|MultiMatchQueryBuilder
operator|.
name|DEFAULT_PREFIX_LENGTH
decl_stmt|;
name|int
name|maxExpansions
init|=
name|MultiMatchQueryBuilder
operator|.
name|DEFAULT_MAX_EXPANSIONS
decl_stmt|;
name|Operator
name|operator
init|=
name|MultiMatchQueryBuilder
operator|.
name|DEFAULT_OPERATOR
decl_stmt|;
name|String
name|minimumShouldMatch
init|=
literal|null
decl_stmt|;
name|String
name|fuzzyRewrite
init|=
literal|null
decl_stmt|;
name|Boolean
name|useDisMax
init|=
literal|null
decl_stmt|;
name|Float
name|tieBreaker
init|=
literal|null
decl_stmt|;
name|Float
name|cutoffFrequency
init|=
literal|null
decl_stmt|;
name|boolean
name|lenient
init|=
name|MultiMatchQueryBuilder
operator|.
name|DEFAULT_LENIENCY
decl_stmt|;
name|MatchQuery
operator|.
name|ZeroTermsQuery
name|zeroTermsQuery
init|=
name|MultiMatchQueryBuilder
operator|.
name|DEFAULT_ZERO_TERMS_QUERY
decl_stmt|;
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
decl_stmt|;
name|String
name|queryName
init|=
literal|null
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
literal|"fields"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
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
name|START_ARRAY
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
name|parseFieldAndBoost
argument_list|(
name|parser
argument_list|,
name|fieldsBoosts
argument_list|)
expr_stmt|;
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
name|parseFieldAndBoost
argument_list|(
name|parser
argument_list|,
name|fieldsBoosts
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|MultiMatchQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support ["
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
name|value
operator|=
name|parser
operator|.
name|objectText
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
name|type
operator|=
name|MultiMatchQueryBuilder
operator|.
name|Type
operator|.
name|parse
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|,
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
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
name|analyzer
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
name|slop
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Fuzziness
operator|.
name|FIELD
argument_list|)
condition|)
block|{
name|fuzziness
operator|=
name|Fuzziness
operator|.
name|parse
argument_list|(
name|parser
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
name|prefixLength
operator|=
name|parser
operator|.
name|intValue
argument_list|()
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
name|maxExpansions
operator|=
name|parser
operator|.
name|intValue
argument_list|()
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
name|operator
operator|=
name|Operator
operator|.
name|fromString
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
name|fuzzyRewrite
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
name|useDisMax
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
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
name|tieBreaker
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
literal|"cutoff_frequency"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|cutoffFrequency
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
literal|"lenient"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|lenient
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"zero_terms_query"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|String
name|zeroTermsDocs
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"none"
operator|.
name|equalsIgnoreCase
argument_list|(
name|zeroTermsDocs
argument_list|)
condition|)
block|{
name|zeroTermsQuery
operator|=
name|MatchQuery
operator|.
name|ZeroTermsQuery
operator|.
name|NONE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"all"
operator|.
name|equalsIgnoreCase
argument_list|(
name|zeroTermsDocs
argument_list|)
condition|)
block|{
name|zeroTermsQuery
operator|=
name|MatchQuery
operator|.
name|ZeroTermsQuery
operator|.
name|ALL
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unsupported zero_terms_docs value ["
operator|+
name|zeroTermsDocs
operator|+
literal|"]"
argument_list|)
throw|;
block|}
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
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|MultiMatchQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|MultiMatchQueryBuilder
operator|.
name|NAME
operator|+
literal|"] unknown token ["
operator|+
name|token
operator|+
literal|"] after ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"No text specified for multi_match query"
argument_list|)
throw|;
block|}
if|if
condition|(
name|fieldsBoosts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"No fields specified for multi_match query"
argument_list|)
throw|;
block|}
return|return
operator|new
name|MultiMatchQueryBuilder
argument_list|(
name|value
argument_list|)
operator|.
name|fields
argument_list|(
name|fieldsBoosts
argument_list|)
operator|.
name|type
argument_list|(
name|type
argument_list|)
operator|.
name|analyzer
argument_list|(
name|analyzer
argument_list|)
operator|.
name|cutoffFrequency
argument_list|(
name|cutoffFrequency
argument_list|)
operator|.
name|fuzziness
argument_list|(
name|fuzziness
argument_list|)
operator|.
name|fuzzyRewrite
argument_list|(
name|fuzzyRewrite
argument_list|)
operator|.
name|useDisMax
argument_list|(
name|useDisMax
argument_list|)
operator|.
name|lenient
argument_list|(
name|lenient
argument_list|)
operator|.
name|maxExpansions
argument_list|(
name|maxExpansions
argument_list|)
operator|.
name|minimumShouldMatch
argument_list|(
name|minimumShouldMatch
argument_list|)
operator|.
name|operator
argument_list|(
name|operator
argument_list|)
operator|.
name|prefixLength
argument_list|(
name|prefixLength
argument_list|)
operator|.
name|slop
argument_list|(
name|slop
argument_list|)
operator|.
name|tieBreaker
argument_list|(
name|tieBreaker
argument_list|)
operator|.
name|zeroTermsQuery
argument_list|(
name|zeroTermsQuery
argument_list|)
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
return|;
block|}
DECL|method|parseFieldAndBoost
specifier|private
name|void
name|parseFieldAndBoost
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|fieldsBoosts
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|fField
init|=
literal|null
decl_stmt|;
name|Float
name|fBoost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
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
name|fieldsBoosts
operator|.
name|put
argument_list|(
name|fField
argument_list|,
name|fBoost
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|MultiMatchQueryBuilder
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|MultiMatchQueryBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

