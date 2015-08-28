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
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cache
operator|.
name|query
operator|.
name|terms
operator|.
name|TermsLookup
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

begin_comment
comment|/**  * Parser for terms query and terms lookup.  *  * Filters documents that have fields that match any of the provided terms (not analyzed)  *  * It also supports a terms lookup mechanism which can be used to fetch the term values from  * a document in an index.  */
end_comment

begin_class
DECL|class|TermsQueryParser
specifier|public
class|class
name|TermsQueryParser
extends|extends
name|BaseQueryParser
block|{
DECL|field|MIN_SHOULD_MATCH_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|MIN_SHOULD_MATCH_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"min_match"
argument_list|,
literal|"min_should_match"
argument_list|,
literal|"minimum_should_match"
argument_list|)
operator|.
name|withAllDeprecated
argument_list|(
literal|"Use [bool] query instead"
argument_list|)
decl_stmt|;
DECL|field|DISABLE_COORD_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|DISABLE_COORD_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"disable_coord"
argument_list|)
operator|.
name|withAllDeprecated
argument_list|(
literal|"Use [bool] query instead"
argument_list|)
decl_stmt|;
DECL|field|EXECUTION_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|EXECUTION_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"execution"
argument_list|)
operator|.
name|withAllDeprecated
argument_list|(
literal|"execution is deprecated and has no effect"
argument_list|)
decl_stmt|;
annotation|@
name|Inject
DECL|method|TermsQueryParser
specifier|public
name|TermsQueryParser
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
name|TermsQueryBuilder
operator|.
name|NAME
block|,
literal|"in"
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
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
literal|null
decl_stmt|;
name|String
name|minShouldMatch
init|=
literal|null
decl_stmt|;
name|boolean
name|disableCoord
init|=
name|TermsQueryBuilder
operator|.
name|DEFAULT_DISABLE_COORD
decl_stmt|;
name|TermsLookup
name|termsLookup
init|=
literal|null
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
name|fieldName
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[terms] query does not support multiple fields"
argument_list|)
throw|;
block|}
name|fieldName
operator|=
name|currentFieldName
expr_stmt|;
name|values
operator|=
name|parseValues
argument_list|(
name|parseContext
argument_list|,
name|parser
argument_list|)
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
name|START_OBJECT
condition|)
block|{
name|fieldName
operator|=
name|currentFieldName
expr_stmt|;
name|termsLookup
operator|=
name|parseTermsLookup
argument_list|(
name|parseContext
argument_list|,
name|parser
argument_list|)
expr_stmt|;
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
name|EXECUTION_FIELD
argument_list|)
condition|)
block|{
comment|// ignore
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
name|MIN_SHOULD_MATCH_FIELD
argument_list|)
condition|)
block|{
if|if
condition|(
name|minShouldMatch
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"["
operator|+
name|currentFieldName
operator|+
literal|"] is not allowed in a filter context for the ["
operator|+
name|TermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query"
argument_list|)
throw|;
block|}
name|minShouldMatch
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
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|DISABLE_COORD_FIELD
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
literal|"[terms] query does not support ["
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
name|fieldName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"terms query requires a field name, followed by array of terms or a document lookup specification"
argument_list|)
throw|;
block|}
name|TermsQueryBuilder
name|termsQueryBuilder
decl_stmt|;
if|if
condition|(
name|values
operator|==
literal|null
condition|)
block|{
name|termsQueryBuilder
operator|=
operator|new
name|TermsQueryBuilder
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|termsQueryBuilder
operator|=
operator|new
name|TermsQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
return|return
name|termsQueryBuilder
operator|.
name|disableCoord
argument_list|(
name|disableCoord
argument_list|)
operator|.
name|minimumShouldMatch
argument_list|(
name|minShouldMatch
argument_list|)
operator|.
name|termsLookup
argument_list|(
name|termsLookup
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
DECL|method|parseValues
specifier|private
specifier|static
name|List
argument_list|<
name|Object
argument_list|>
name|parseValues
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|END_ARRAY
condition|)
block|{
name|Object
name|value
init|=
name|parser
operator|.
name|objectBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"No value specified for terms query"
argument_list|)
throw|;
block|}
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|values
return|;
block|}
DECL|method|parseTermsLookup
specifier|private
specifier|static
name|TermsLookup
name|parseTermsLookup
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|TermsLookup
name|termsLookup
init|=
operator|new
name|TermsLookup
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
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"index"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|termsLookup
operator|.
name|index
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
literal|"type"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|termsLookup
operator|.
name|type
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
literal|"id"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|termsLookup
operator|.
name|id
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
literal|"routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|termsLookup
operator|.
name|routing
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
literal|"path"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|termsLookup
operator|.
name|path
argument_list|(
name|parser
operator|.
name|text
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
argument_list|,
literal|"[terms] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"] within lookup element"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|termsLookup
operator|.
name|type
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
argument_list|,
literal|"[terms] query lookup element requires specifying the type"
argument_list|)
throw|;
block|}
if|if
condition|(
name|termsLookup
operator|.
name|id
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
argument_list|,
literal|"[terms] query lookup element requires specifying the id"
argument_list|)
throw|;
block|}
if|if
condition|(
name|termsLookup
operator|.
name|path
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
argument_list|,
literal|"[terms] query lookup element requires specifying the path"
argument_list|)
throw|;
block|}
return|return
name|termsLookup
return|;
block|}
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|TermsQueryBuilder
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|TermsQueryBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

