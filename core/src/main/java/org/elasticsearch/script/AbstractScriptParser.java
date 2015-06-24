begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
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
name|Script
operator|.
name|ScriptParseException
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
name|ScriptService
operator|.
name|ScriptType
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_class
DECL|class|AbstractScriptParser
specifier|public
specifier|abstract
class|class
name|AbstractScriptParser
parameter_list|<
name|S
extends|extends
name|Script
parameter_list|>
block|{
DECL|method|parseInlineScript
specifier|protected
specifier|abstract
name|String
name|parseInlineScript
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|createScript
specifier|protected
specifier|abstract
name|S
name|createScript
parameter_list|(
name|String
name|script
parameter_list|,
name|ScriptType
name|type
parameter_list|,
name|String
name|lang
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
function_decl|;
DECL|method|createSimpleScript
specifier|protected
specifier|abstract
name|S
name|createSimpleScript
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Deprecated
DECL|method|getAdditionalScriptParameters
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptType
argument_list|>
name|getAdditionalScriptParameters
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
DECL|method|parse
specifier|public
name|S
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
throws|throws
name|IOException
block|{
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
comment|// If the parser hasn't yet been pushed to the first token, do it now
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
return|return
name|createSimpleScript
argument_list|(
name|parser
argument_list|)
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
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|ScriptParseException
argument_list|(
literal|"expected a string value or an object, but found [{}] instead"
argument_list|,
name|token
argument_list|)
throw|;
block|}
name|String
name|script
init|=
literal|null
decl_stmt|;
name|ScriptType
name|type
init|=
literal|null
decl_stmt|;
name|String
name|lang
init|=
name|getDefaultScriptLang
argument_list|()
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
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ScriptType
operator|.
name|INLINE
operator|.
name|getParseField
argument_list|()
argument_list|)
operator|||
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ScriptService
operator|.
name|SCRIPT_INLINE
argument_list|)
condition|)
block|{
name|type
operator|=
name|ScriptType
operator|.
name|INLINE
expr_stmt|;
name|script
operator|=
name|parseInlineScript
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ScriptType
operator|.
name|FILE
operator|.
name|getParseField
argument_list|()
argument_list|)
operator|||
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ScriptService
operator|.
name|SCRIPT_FILE
argument_list|)
condition|)
block|{
name|type
operator|=
name|ScriptType
operator|.
name|FILE
expr_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
name|script
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
name|ScriptParseException
argument_list|(
literal|"expected a string value for field [{}], but found [{}]"
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ScriptType
operator|.
name|INDEXED
operator|.
name|getParseField
argument_list|()
argument_list|)
operator|||
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ScriptService
operator|.
name|SCRIPT_ID
argument_list|)
condition|)
block|{
name|type
operator|=
name|ScriptType
operator|.
name|INDEXED
expr_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
name|script
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
name|ScriptParseException
argument_list|(
literal|"expected a string value for field [{}], but found [{}]"
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ScriptField
operator|.
name|LANG
argument_list|)
operator|||
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ScriptService
operator|.
name|SCRIPT_LANG
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
name|VALUE_STRING
condition|)
block|{
name|lang
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
name|ScriptParseException
argument_list|(
literal|"expected a string value for field [{}], but found [{}]"
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ScriptField
operator|.
name|PARAMS
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
name|START_OBJECT
condition|)
block|{
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
name|ScriptParseException
argument_list|(
literal|"expected an object for field [{}], but found [{}]"
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// TODO remove this in 3.0
name|ScriptType
name|paramScriptType
init|=
name|getAdditionalScriptParameters
argument_list|()
operator|.
name|get
argument_list|(
name|currentFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|paramScriptType
operator|!=
literal|null
condition|)
block|{
name|script
operator|=
name|parseInlineScript
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|type
operator|=
name|paramScriptType
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ScriptParseException
argument_list|(
literal|"unexpected field [{}]"
argument_list|,
name|currentFieldName
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
throw|throw
operator|new
name|ScriptParseException
argument_list|(
literal|"expected one of [{}], [{}] or [{}] fields, but found none"
argument_list|,
name|ScriptType
operator|.
name|INLINE
operator|.
name|getParseField
argument_list|()
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|ScriptType
operator|.
name|FILE
operator|.
name|getParseField
argument_list|()
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|ScriptType
operator|.
name|INDEXED
operator|.
name|getParseField
argument_list|()
operator|.
name|getPreferredName
argument_list|()
argument_list|)
throw|;
block|}
assert|assert
name|type
operator|!=
literal|null
operator|:
literal|"if script is not null, type should definitely not be null"
assert|;
return|return
name|createScript
argument_list|(
name|script
argument_list|,
name|type
argument_list|,
name|lang
argument_list|,
name|params
argument_list|)
return|;
block|}
comment|/**      * @return the default script language for this parser or<code>null</code>      *         to use the default set in the ScriptService      */
DECL|method|getDefaultScriptLang
specifier|protected
name|String
name|getDefaultScriptLang
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|method|parse
specifier|public
name|S
name|parse
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|,
name|boolean
name|removeMatchedEntries
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
block|{
name|String
name|script
init|=
literal|null
decl_stmt|;
name|ScriptType
name|type
init|=
literal|null
decl_stmt|;
name|String
name|lang
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
for|for
control|(
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|itr
init|=
name|config
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|itr
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|parameterName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Object
name|parameterValue
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|parameterName
argument_list|,
name|ScriptField
operator|.
name|LANG
argument_list|)
operator|||
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|parameterName
argument_list|,
name|ScriptService
operator|.
name|SCRIPT_LANG
argument_list|)
condition|)
block|{
if|if
condition|(
name|parameterValue
operator|instanceof
name|String
operator|||
name|parameterValue
operator|==
literal|null
condition|)
block|{
name|lang
operator|=
operator|(
name|String
operator|)
name|parameterValue
expr_stmt|;
if|if
condition|(
name|removeMatchedEntries
condition|)
block|{
name|itr
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ScriptParseException
argument_list|(
literal|"Value must be of type String: ["
operator|+
name|parameterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|parameterName
argument_list|,
name|ScriptField
operator|.
name|PARAMS
argument_list|)
condition|)
block|{
if|if
condition|(
name|parameterValue
operator|instanceof
name|Map
operator|||
name|parameterValue
operator|==
literal|null
condition|)
block|{
name|params
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|parameterValue
expr_stmt|;
if|if
condition|(
name|removeMatchedEntries
condition|)
block|{
name|itr
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ScriptParseException
argument_list|(
literal|"Value must be of type String: ["
operator|+
name|parameterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|parameterName
argument_list|,
name|ScriptType
operator|.
name|INLINE
operator|.
name|getParseField
argument_list|()
argument_list|)
operator|||
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|parameterName
argument_list|,
name|ScriptService
operator|.
name|SCRIPT_INLINE
argument_list|)
condition|)
block|{
if|if
condition|(
name|parameterValue
operator|instanceof
name|String
operator|||
name|parameterValue
operator|==
literal|null
condition|)
block|{
name|script
operator|=
operator|(
name|String
operator|)
name|parameterValue
expr_stmt|;
name|type
operator|=
name|ScriptType
operator|.
name|INLINE
expr_stmt|;
if|if
condition|(
name|removeMatchedEntries
condition|)
block|{
name|itr
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ScriptParseException
argument_list|(
literal|"Value must be of type String: ["
operator|+
name|parameterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|parameterName
argument_list|,
name|ScriptType
operator|.
name|FILE
operator|.
name|getParseField
argument_list|()
argument_list|)
operator|||
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|parameterName
argument_list|,
name|ScriptService
operator|.
name|SCRIPT_FILE
argument_list|)
condition|)
block|{
if|if
condition|(
name|parameterValue
operator|instanceof
name|String
operator|||
name|parameterValue
operator|==
literal|null
condition|)
block|{
name|script
operator|=
operator|(
name|String
operator|)
name|parameterValue
expr_stmt|;
name|type
operator|=
name|ScriptType
operator|.
name|FILE
expr_stmt|;
if|if
condition|(
name|removeMatchedEntries
condition|)
block|{
name|itr
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ScriptParseException
argument_list|(
literal|"Value must be of type String: ["
operator|+
name|parameterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|parameterName
argument_list|,
name|ScriptType
operator|.
name|INDEXED
operator|.
name|getParseField
argument_list|()
argument_list|)
operator|||
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|parameterName
argument_list|,
name|ScriptService
operator|.
name|SCRIPT_ID
argument_list|)
condition|)
block|{
if|if
condition|(
name|parameterValue
operator|instanceof
name|String
operator|||
name|parameterValue
operator|==
literal|null
condition|)
block|{
name|script
operator|=
operator|(
name|String
operator|)
name|parameterValue
expr_stmt|;
name|type
operator|=
name|ScriptType
operator|.
name|INDEXED
expr_stmt|;
if|if
condition|(
name|removeMatchedEntries
condition|)
block|{
name|itr
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ScriptParseException
argument_list|(
literal|"Value must be of type String: ["
operator|+
name|parameterName
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
throw|throw
operator|new
name|ScriptParseException
argument_list|(
literal|"expected one of [{}], [{}] or [{}] fields, but found none"
argument_list|,
name|ScriptType
operator|.
name|INLINE
operator|.
name|getParseField
argument_list|()
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|ScriptType
operator|.
name|FILE
operator|.
name|getParseField
argument_list|()
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|ScriptType
operator|.
name|INDEXED
operator|.
name|getParseField
argument_list|()
operator|.
name|getPreferredName
argument_list|()
argument_list|)
throw|;
block|}
assert|assert
name|type
operator|!=
literal|null
operator|:
literal|"if script is not null, type should definitely not be null"
assert|;
return|return
name|createScript
argument_list|(
name|script
argument_list|,
name|type
argument_list|,
name|lang
argument_list|,
name|params
argument_list|)
return|;
block|}
block|}
end_class

end_unit

