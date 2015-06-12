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
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|newArrayList
import|;
end_import

begin_comment
comment|/**  * Parser for the {@link BoolQueryBuilder}  */
end_comment

begin_class
DECL|class|BoolQueryParser
specifier|public
class|class
name|BoolQueryParser
extends|extends
name|BaseQueryParser
block|{
annotation|@
name|Inject
DECL|method|BoolQueryParser
specifier|public
name|BoolQueryParser
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|BooleanQuery
operator|.
name|setMaxClauseCount
argument_list|(
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"index.query.bool.max_clause_count"
argument_list|,
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"indices.query.bool.max_clause_count"
argument_list|,
name|BooleanQuery
operator|.
name|getMaxClauseCount
argument_list|()
argument_list|)
argument_list|)
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
name|BoolQueryBuilder
operator|.
name|NAME
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|QueryBuilder
name|fromXContent
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
name|boolean
name|disableCoord
init|=
name|BoolQueryBuilder
operator|.
name|DISABLE_COORD_DEFAULT
decl_stmt|;
name|boolean
name|adjustPureNegative
init|=
name|BoolQueryBuilder
operator|.
name|ADJUST_PURE_NEGATIVE_DEFAULT
decl_stmt|;
name|float
name|boost
init|=
literal|1.0f
decl_stmt|;
name|String
name|minimumShouldMatch
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|mustClauses
init|=
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|mustNotClauses
init|=
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|shouldClauses
init|=
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|filterClauses
init|=
name|newArrayList
argument_list|()
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
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
name|QueryBuilder
name|query
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
name|parseContext
operator|.
name|isDeprecatedSetting
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
comment|// skip
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
name|START_OBJECT
condition|)
block|{
switch|switch
condition|(
name|currentFieldName
condition|)
block|{
case|case
literal|"must"
case|:
name|query
operator|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
expr_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|mustClauses
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|"should"
case|:
name|query
operator|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
expr_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|shouldClauses
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
if|if
condition|(
name|parseContext
operator|.
name|isFilter
argument_list|()
operator|&&
name|minimumShouldMatch
operator|==
literal|null
condition|)
block|{
name|minimumShouldMatch
operator|=
literal|"1"
expr_stmt|;
block|}
block|}
break|break;
case|case
literal|"filter"
case|:
name|query
operator|=
name|parseContext
operator|.
name|parseInnerFilterToQueryBuilder
argument_list|()
expr_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|filterClauses
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|"must_not"
case|:
case|case
literal|"mustNot"
case|:
name|query
operator|=
name|parseContext
operator|.
name|parseInnerFilterToQueryBuilder
argument_list|()
expr_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|mustNotClauses
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[bool] query does not support ["
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
switch|switch
condition|(
name|currentFieldName
condition|)
block|{
case|case
literal|"must"
case|:
name|query
operator|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
expr_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|mustClauses
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|"should"
case|:
name|query
operator|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
expr_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|shouldClauses
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
if|if
condition|(
name|parseContext
operator|.
name|isFilter
argument_list|()
operator|&&
name|minimumShouldMatch
operator|==
literal|null
condition|)
block|{
name|minimumShouldMatch
operator|=
literal|"1"
expr_stmt|;
block|}
block|}
break|break;
case|case
literal|"filter"
case|:
name|query
operator|=
name|parseContext
operator|.
name|parseInnerFilterToQueryBuilder
argument_list|()
expr_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|filterClauses
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|"must_not"
case|:
case|case
literal|"mustNot"
case|:
name|query
operator|=
name|parseContext
operator|.
name|parseInnerFilterToQueryBuilder
argument_list|()
expr_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|mustNotClauses
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"bool query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
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
literal|"disable_coord"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"disableCoord"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|disableCoord
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
literal|"minimum_number_should_match"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"minimumNumberShouldMatch"
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
literal|"adjust_pure_negative"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"adjustPureNegative"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|adjustPureNegative
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
argument_list|,
literal|"[bool] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
name|BoolQueryBuilder
name|boolQuery
init|=
operator|new
name|BoolQueryBuilder
argument_list|()
decl_stmt|;
name|boolQuery
operator|.
name|must
argument_list|(
name|mustClauses
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|mustNot
argument_list|(
name|mustNotClauses
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|should
argument_list|(
name|shouldClauses
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|filter
argument_list|(
name|filterClauses
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|disableCoord
argument_list|(
name|disableCoord
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|adjustPureNegative
argument_list|(
name|adjustPureNegative
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|minimumNumberShouldMatch
argument_list|(
name|minimumShouldMatch
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
expr_stmt|;
name|boolQuery
operator|.
name|validate
argument_list|()
expr_stmt|;
return|return
name|boolQuery
return|;
block|}
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|BoolQueryBuilder
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|BoolQueryBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit
