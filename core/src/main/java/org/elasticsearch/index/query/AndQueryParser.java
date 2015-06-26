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
name|ArrayList
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

begin_class
annotation|@
name|Deprecated
DECL|class|AndQueryParser
specifier|public
class|class
name|AndQueryParser
extends|extends
name|BaseQueryParser
block|{
annotation|@
name|Inject
DECL|method|AndQueryParser
specifier|public
name|AndQueryParser
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
name|AndQueryBuilder
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
specifier|final
name|ArrayList
argument_list|<
name|QueryBuilder
argument_list|>
name|queries
init|=
name|newArrayList
argument_list|()
decl_stmt|;
name|boolean
name|queriesFound
init|=
literal|false
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
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
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
name|queriesFound
operator|=
literal|true
expr_stmt|;
name|QueryBuilder
name|filter
init|=
name|parseContext
operator|.
name|parseInnerFilterToQueryBuilder
argument_list|()
decl_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
block|}
else|else
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
name|START_ARRAY
condition|)
block|{
if|if
condition|(
literal|"filters"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|queriesFound
operator|=
literal|true
expr_stmt|;
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
name|QueryBuilder
name|filter
init|=
name|parseContext
operator|.
name|parseInnerFilterToQueryBuilder
argument_list|()
decl_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|queriesFound
operator|=
literal|true
expr_stmt|;
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
name|QueryBuilder
name|filter
init|=
name|parseContext
operator|.
name|parseInnerFilterToQueryBuilder
argument_list|()
decl_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|filter
argument_list|)
expr_stmt|;
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
literal|"[and] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
if|if
condition|(
operator|!
name|queriesFound
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[and] query requires 'filters' to be set on it'"
argument_list|)
throw|;
block|}
name|AndQueryBuilder
name|andQuery
init|=
operator|new
name|AndQueryBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|QueryBuilder
name|query
range|:
name|queries
control|)
block|{
name|andQuery
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
name|andQuery
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
expr_stmt|;
name|andQuery
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
return|return
name|andQuery
return|;
block|}
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|AndQueryBuilder
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|AndQueryBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

