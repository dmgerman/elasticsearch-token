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
name|NamedXContentRegistry
operator|.
name|UnknownNamedObjectException
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
name|XContentLocation
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
name|script
operator|.
name|Script
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
name|Objects
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
specifier|final
name|XContentParser
name|parser
decl_stmt|;
DECL|field|defaultScriptLanguage
specifier|private
specifier|final
name|String
name|defaultScriptLanguage
decl_stmt|;
DECL|method|QueryParseContext
specifier|public
name|QueryParseContext
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
block|{
name|this
argument_list|(
name|Script
operator|.
name|DEFAULT_SCRIPT_LANG
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
comment|//TODO this constructor can be removed from master branch
DECL|method|QueryParseContext
specifier|public
name|QueryParseContext
parameter_list|(
name|String
name|defaultScriptLanguage
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
block|{
name|this
operator|.
name|parser
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|parser
argument_list|,
literal|"parser cannot be null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|defaultScriptLanguage
operator|=
name|defaultScriptLanguage
expr_stmt|;
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
name|CACHE
operator|.
name|match
argument_list|(
name|setting
argument_list|)
operator|||
name|CACHE_KEY
operator|.
name|match
argument_list|(
name|setting
argument_list|)
return|;
block|}
comment|/**      * Parses a top level query including the query element that wraps it      */
DECL|method|parseTopLevelQueryBuilder
specifier|public
name|QueryBuilder
name|parseTopLevelQueryBuilder
parameter_list|()
block|{
try|try
block|{
name|QueryBuilder
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
name|Exception
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
name|parseInnerQueryBuilder
parameter_list|()
throws|throws
name|IOException
block|{
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
if|if
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
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
if|if
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
comment|// we encountered '{}' for a query clause, it used to be supported, deprecated in 5.0 and removed in 6.0
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"query malformed, empty clause found at ["
operator|+
name|parser
operator|.
name|getTokenLocation
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
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
if|if
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
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
literal|"["
operator|+
name|queryName
operator|+
literal|"] query malformed, no start_object after query name"
argument_list|)
throw|;
block|}
name|QueryBuilder
name|result
decl_stmt|;
try|try
block|{
name|result
operator|=
name|parser
operator|.
name|namedObject
argument_list|(
name|QueryBuilder
operator|.
name|class
argument_list|,
name|queryName
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownNamedObjectException
name|e
parameter_list|)
block|{
comment|// Preserve the error message from 5.0 until we have a compellingly better message so we don't break BWC.
comment|// This intentionally doesn't include the causing exception because that'd change the "root_cause" of any unknown query errors
throw|throw
operator|new
name|ParsingException
argument_list|(
operator|new
name|XContentLocation
argument_list|(
name|e
operator|.
name|getLineNumber
argument_list|()
argument_list|,
name|e
operator|.
name|getColumnNumber
argument_list|()
argument_list|)
argument_list|,
literal|"no [query] registered for ["
operator|+
name|e
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|//end_object of the specific query (e.g. match, multi_match etc.) element
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
name|END_OBJECT
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
literal|"["
operator|+
name|queryName
operator|+
literal|"] malformed query, expected [END_OBJECT] but found ["
operator|+
name|parser
operator|.
name|currentToken
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|//end_object of the query object
if|if
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
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
literal|"["
operator|+
name|queryName
operator|+
literal|"] malformed query, expected [END_OBJECT] but found ["
operator|+
name|parser
operator|.
name|currentToken
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
comment|/**      * Returns the default scripting language, that should be used if scripts don't specify the script language      * explicitly.      */
DECL|method|getDefaultScriptLanguage
specifier|public
name|String
name|getDefaultScriptLanguage
parameter_list|()
block|{
return|return
name|defaultScriptLanguage
return|;
block|}
block|}
end_class

end_unit

