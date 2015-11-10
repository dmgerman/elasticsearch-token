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
name|ParseFieldMatcher
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
name|query
operator|.
name|IndicesQueriesRegistry
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

begin_class
DECL|class|QueryParseContext
specifier|public
class|class
name|QueryParseContext
block|{
DECL|field|CACHE
specifier|private
specifier|static
specifier|final
name|ParseField
name|CACHE
init|=
operator|new
name|ParseField
argument_list|(
literal|"_cache"
argument_list|)
operator|.
name|withAllDeprecated
argument_list|(
literal|"Elasticsearch makes its own caching decisions"
argument_list|)
decl_stmt|;
DECL|field|CACHE_KEY
specifier|private
specifier|static
specifier|final
name|ParseField
name|CACHE_KEY
init|=
operator|new
name|ParseField
argument_list|(
literal|"_cache_key"
argument_list|)
operator|.
name|withAllDeprecated
argument_list|(
literal|"Filters are always used as cache keys"
argument_list|)
decl_stmt|;
DECL|field|parser
specifier|private
name|XContentParser
name|parser
decl_stmt|;
DECL|field|parseFieldMatcher
specifier|private
name|ParseFieldMatcher
name|parseFieldMatcher
init|=
name|ParseFieldMatcher
operator|.
name|EMPTY
decl_stmt|;
DECL|field|indicesQueriesRegistry
specifier|private
name|IndicesQueriesRegistry
name|indicesQueriesRegistry
decl_stmt|;
DECL|method|QueryParseContext
specifier|public
name|QueryParseContext
parameter_list|(
name|IndicesQueriesRegistry
name|registry
parameter_list|)
block|{
name|this
operator|.
name|indicesQueriesRegistry
operator|=
name|registry
expr_stmt|;
block|}
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|(
name|XContentParser
name|jp
parameter_list|)
block|{
name|this
operator|.
name|parseFieldMatcher
operator|=
name|ParseFieldMatcher
operator|.
name|EMPTY
expr_stmt|;
name|this
operator|.
name|parser
operator|=
name|jp
expr_stmt|;
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|parser
operator|.
name|setParseFieldMatcher
argument_list|(
name|parseFieldMatcher
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|parser
specifier|public
name|XContentParser
name|parser
parameter_list|()
block|{
return|return
name|this
operator|.
name|parser
return|;
block|}
DECL|method|parseFieldMatcher
specifier|public
name|void
name|parseFieldMatcher
parameter_list|(
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
block|{
if|if
condition|(
name|parseFieldMatcher
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"parseFieldMatcher must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|parseFieldMatcher
operator|=
name|parseFieldMatcher
expr_stmt|;
block|}
DECL|method|isDeprecatedSetting
specifier|public
name|boolean
name|isDeprecatedSetting
parameter_list|(
name|String
name|setting
parameter_list|)
block|{
return|return
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|setting
argument_list|,
name|CACHE
argument_list|)
operator|||
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|setting
argument_list|,
name|CACHE_KEY
argument_list|)
return|;
block|}
comment|/**      * Parses a top level query including the query element that wraps it      */
DECL|method|parseTopLevelQueryBuilder
specifier|public
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|parseTopLevelQueryBuilder
parameter_list|()
block|{
try|try
block|{
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|queryBuilder
init|=
literal|null
decl_stmt|;
for|for
control|(
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
init|;
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
control|)
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
name|String
name|fieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"query"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|queryBuilder
operator|=
name|parseInnerQueryBuilder
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
literal|"request does not support ["
operator|+
name|parser
operator|.
name|currentName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|queryBuilder
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
literal|"Required query is missing"
argument_list|)
throw|;
block|}
return|return
name|queryBuilder
return|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|==
literal|null
condition|?
literal|null
else|:
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Failed to parse"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Parses a query excluding the query element that wraps it      */
DECL|method|parseInnerQueryBuilder
specifier|public
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|parseInnerQueryBuilder
parameter_list|()
throws|throws
name|IOException
block|{
comment|// move to START object
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
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
literal|"[_na] query malformed, must start with start_object"
argument_list|)
throw|;
block|}
block|}
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
comment|// empty query
return|return
name|EmptyQueryBuilder
operator|.
name|PROTOTYPE
return|;
block|}
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
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
literal|"[_na] query malformed, no field after start_object"
argument_list|)
throw|;
block|}
name|String
name|queryName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
comment|// move to the next START_OBJECT
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
operator|&&
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
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
literal|"[_na] query malformed, no field after start_object"
argument_list|)
throw|;
block|}
name|QueryParser
name|queryParser
init|=
name|queryParser
argument_list|(
name|queryName
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryParser
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
literal|"No query registered for ["
operator|+
name|queryName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|QueryBuilder
name|result
init|=
name|queryParser
operator|.
name|fromXContent
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
operator|||
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
comment|// if we are at END_OBJECT, move to the next one...
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
DECL|method|parseFieldMatcher
specifier|public
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|()
block|{
return|return
name|parseFieldMatcher
return|;
block|}
DECL|method|parser
specifier|public
name|void
name|parser
parameter_list|(
name|XContentParser
name|innerParser
parameter_list|)
block|{
name|this
operator|.
name|parser
operator|=
name|innerParser
expr_stmt|;
block|}
comment|/**      * Get the query parser for a specific type of query registered under its name      * @param name the name of the parser to retrieve      * @return the query parser      */
DECL|method|queryParser
specifier|private
name|QueryParser
name|queryParser
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|indicesQueriesRegistry
operator|.
name|queryParsers
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
end_class

end_unit

