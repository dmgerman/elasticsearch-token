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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Parser for constant_score query  */
end_comment

begin_class
DECL|class|ConstantScoreQueryParser
specifier|public
class|class
name|ConstantScoreQueryParser
extends|extends
name|BaseQueryParser
argument_list|<
name|ConstantScoreQueryBuilder
argument_list|>
block|{
DECL|field|INNER_QUERY_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|INNER_QUERY_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"filter"
argument_list|,
literal|"query"
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
name|ConstantScoreQueryBuilder
operator|.
name|NAME
block|,
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|ConstantScoreQueryBuilder
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
name|ConstantScoreQueryBuilder
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
name|QueryBuilder
name|query
init|=
literal|null
decl_stmt|;
name|boolean
name|queryFound
init|=
literal|false
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
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
name|INNER_QUERY_FIELD
argument_list|)
condition|)
block|{
name|query
operator|=
name|parseContext
operator|.
name|parseInnerFilterToQueryBuilder
argument_list|()
expr_stmt|;
name|queryFound
operator|=
literal|true
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
literal|"[constant_score] query does not support ["
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
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[constant_score] query does not support ["
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
operator|!
name|queryFound
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[constant_score] requires a 'filter' element"
argument_list|)
throw|;
block|}
name|ConstantScoreQueryBuilder
name|constantScoreBuilder
init|=
operator|new
name|ConstantScoreQueryBuilder
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|constantScoreBuilder
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
name|constantScoreBuilder
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
expr_stmt|;
return|return
name|constantScoreBuilder
return|;
block|}
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|ConstantScoreQueryBuilder
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|ConstantScoreQueryBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

