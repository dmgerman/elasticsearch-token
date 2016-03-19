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
name|join
operator|.
name|ScoreMode
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
name|ParseField
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
name|QueryInnerHits
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
name|Locale
import|;
end_import

begin_comment
comment|/**  * A query parser for<tt>has_child</tt> queries.  */
end_comment

begin_class
DECL|class|HasChildQueryParser
specifier|public
class|class
name|HasChildQueryParser
implements|implements
name|QueryParser
argument_list|<
name|HasChildQueryBuilder
argument_list|>
block|{
DECL|field|QUERY_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|QUERY_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"query"
argument_list|,
literal|"filter"
argument_list|)
decl_stmt|;
DECL|field|TYPE_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|TYPE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"type"
argument_list|,
literal|"child_type"
argument_list|)
decl_stmt|;
DECL|field|MAX_CHILDREN_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|MAX_CHILDREN_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"max_children"
argument_list|)
decl_stmt|;
DECL|field|MIN_CHILDREN_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|MIN_CHILDREN_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"min_children"
argument_list|)
decl_stmt|;
DECL|field|SCORE_MODE_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|SCORE_MODE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"score_mode"
argument_list|)
decl_stmt|;
DECL|field|INNER_HITS_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|INNER_HITS_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"inner_hits"
argument_list|)
decl_stmt|;
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
name|HasChildQueryBuilder
operator|.
name|NAME
block|,
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|HasChildQueryBuilder
operator|.
name|NAME
argument_list|)
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|HasChildQueryBuilder
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
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
decl_stmt|;
name|String
name|childType
init|=
literal|null
decl_stmt|;
name|ScoreMode
name|scoreMode
init|=
name|HasChildQueryBuilder
operator|.
name|DEFAULT_SCORE_MODE
decl_stmt|;
name|int
name|minChildren
init|=
name|HasChildQueryBuilder
operator|.
name|DEFAULT_MIN_CHILDREN
decl_stmt|;
name|int
name|maxChildren
init|=
name|HasChildQueryBuilder
operator|.
name|DEFAULT_MAX_CHILDREN
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|QueryInnerHits
name|queryInnerHits
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
name|iqb
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
name|QUERY_FIELD
argument_list|)
condition|)
block|{
name|iqb
operator|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
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
name|INNER_HITS_FIELD
argument_list|)
condition|)
block|{
name|queryInnerHits
operator|=
operator|new
name|QueryInnerHits
argument_list|(
name|parser
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
literal|"[has_child] query does not support ["
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
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|TYPE_FIELD
argument_list|)
condition|)
block|{
name|childType
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
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|SCORE_MODE_FIELD
argument_list|)
condition|)
block|{
name|scoreMode
operator|=
name|parseScoreMode
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
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|AbstractQueryBuilder
operator|.
name|BOOST_FIELD
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
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|MIN_CHILDREN_FIELD
argument_list|)
condition|)
block|{
name|minChildren
operator|=
name|parser
operator|.
name|intValue
argument_list|(
literal|true
argument_list|)
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
name|MAX_CHILDREN_FIELD
argument_list|)
condition|)
block|{
name|maxChildren
operator|=
name|parser
operator|.
name|intValue
argument_list|(
literal|true
argument_list|)
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
name|AbstractQueryBuilder
operator|.
name|NAME_FIELD
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
literal|"[has_child] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
name|HasChildQueryBuilder
name|hasChildQueryBuilder
init|=
operator|new
name|HasChildQueryBuilder
argument_list|(
name|childType
argument_list|,
name|iqb
argument_list|,
name|maxChildren
argument_list|,
name|minChildren
argument_list|,
name|scoreMode
argument_list|,
name|queryInnerHits
argument_list|)
decl_stmt|;
name|hasChildQueryBuilder
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
expr_stmt|;
name|hasChildQueryBuilder
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
return|return
name|hasChildQueryBuilder
return|;
block|}
DECL|method|parseScoreMode
specifier|public
specifier|static
name|ScoreMode
name|parseScoreMode
parameter_list|(
name|String
name|scoreModeString
parameter_list|)
block|{
if|if
condition|(
literal|"none"
operator|.
name|equals
argument_list|(
name|scoreModeString
argument_list|)
condition|)
block|{
return|return
name|ScoreMode
operator|.
name|None
return|;
block|}
elseif|else
if|if
condition|(
literal|"min"
operator|.
name|equals
argument_list|(
name|scoreModeString
argument_list|)
condition|)
block|{
return|return
name|ScoreMode
operator|.
name|Min
return|;
block|}
elseif|else
if|if
condition|(
literal|"max"
operator|.
name|equals
argument_list|(
name|scoreModeString
argument_list|)
condition|)
block|{
return|return
name|ScoreMode
operator|.
name|Max
return|;
block|}
elseif|else
if|if
condition|(
literal|"avg"
operator|.
name|equals
argument_list|(
name|scoreModeString
argument_list|)
condition|)
block|{
return|return
name|ScoreMode
operator|.
name|Avg
return|;
block|}
elseif|else
if|if
condition|(
literal|"sum"
operator|.
name|equals
argument_list|(
name|scoreModeString
argument_list|)
condition|)
block|{
return|return
name|ScoreMode
operator|.
name|Total
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No score mode for child query ["
operator|+
name|scoreModeString
operator|+
literal|"] found"
argument_list|)
throw|;
block|}
DECL|method|scoreModeAsString
specifier|public
specifier|static
name|String
name|scoreModeAsString
parameter_list|(
name|ScoreMode
name|scoreMode
parameter_list|)
block|{
if|if
condition|(
name|scoreMode
operator|==
name|ScoreMode
operator|.
name|Total
condition|)
block|{
comment|// Lucene uses 'total' but 'sum' is more consistent with other elasticsearch APIs
return|return
literal|"sum"
return|;
block|}
else|else
block|{
return|return
name|scoreMode
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|HasChildQueryBuilder
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|HasChildQueryBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

