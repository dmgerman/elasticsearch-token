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
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
operator|.
name|ScriptField
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
name|ScriptParameterParser
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
name|ScriptParameterParser
operator|.
name|ScriptParameterValue
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
comment|/**  * Parser for script query  */
end_comment

begin_class
DECL|class|ScriptQueryParser
specifier|public
class|class
name|ScriptQueryParser
extends|extends
name|BaseQueryParser
argument_list|<
name|ScriptQueryBuilder
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|ScriptQueryParser
specifier|public
name|ScriptQueryParser
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
name|ScriptQueryBuilder
operator|.
name|NAME
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|ScriptQueryBuilder
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
name|ScriptParameterParser
name|scriptParameterParser
init|=
operator|new
name|ScriptParameterParser
argument_list|()
decl_stmt|;
comment|// also, when caching, since its isCacheable is false, will result in loading all bit set...
name|Script
name|script
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
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
name|ScriptField
operator|.
name|SCRIPT
argument_list|)
condition|)
block|{
name|script
operator|=
name|Script
operator|.
name|parse
argument_list|(
name|parser
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
literal|"params"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
comment|// TODO remove in 3.0 (here to support old script APIs)
name|params
operator|=
name|parser
operator|.
name|map
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
literal|"[script] query does not support ["
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
elseif|else
if|if
condition|(
operator|!
name|scriptParameterParser
operator|.
name|token
argument_list|(
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|,
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[script] query does not support ["
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
name|script
operator|==
literal|null
condition|)
block|{
comment|// Didn't find anything using the new API so try using the old one instead
name|ScriptParameterValue
name|scriptValue
init|=
name|scriptParameterParser
operator|.
name|getDefaultScriptParameterValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|scriptValue
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
name|params
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|script
operator|=
operator|new
name|Script
argument_list|(
name|scriptValue
operator|.
name|script
argument_list|()
argument_list|,
name|scriptValue
operator|.
name|scriptType
argument_list|()
argument_list|,
name|scriptParameterParser
operator|.
name|lang
argument_list|()
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|params
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
literal|"script params must be specified inside script object in a [script] filter"
argument_list|)
throw|;
block|}
if|if
condition|(
name|script
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
literal|"script must be provided with a [script] filter"
argument_list|)
throw|;
block|}
return|return
operator|new
name|ScriptQueryBuilder
argument_list|(
name|script
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
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|ScriptQueryBuilder
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|ScriptQueryBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

