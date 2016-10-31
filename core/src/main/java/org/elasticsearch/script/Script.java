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
name|ElasticsearchParseException
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
name|Nullable
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
name|bytes
operator|.
name|BytesArray
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|ToXContent
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
name|XContentBuilder
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
name|XContentFactory
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
name|common
operator|.
name|xcontent
operator|.
name|XContentType
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
name|QueryParseContext
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
name|Map
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

begin_comment
comment|/**  * Script holds all the parameters necessary to compile or find in cache and then execute a script.  */
end_comment

begin_class
DECL|class|Script
specifier|public
specifier|final
class|class
name|Script
implements|implements
name|ToXContent
implements|,
name|Writeable
block|{
DECL|field|DEFAULT_SCRIPT_LANG
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_SCRIPT_LANG
init|=
literal|"painless"
decl_stmt|;
DECL|field|script
specifier|private
name|String
name|script
decl_stmt|;
DECL|field|type
specifier|private
name|ScriptType
name|type
decl_stmt|;
DECL|field|lang
annotation|@
name|Nullable
specifier|private
name|String
name|lang
decl_stmt|;
DECL|field|params
annotation|@
name|Nullable
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
decl_stmt|;
DECL|field|contentType
annotation|@
name|Nullable
specifier|private
name|XContentType
name|contentType
decl_stmt|;
comment|/**      * Constructor for simple inline script. The script will have no lang or params set.      *      * @param script The inline script to execute.      */
DECL|method|Script
specifier|public
name|Script
parameter_list|(
name|String
name|script
parameter_list|)
block|{
name|this
argument_list|(
name|script
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|Script
specifier|public
name|Script
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
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|params
parameter_list|)
block|{
name|this
argument_list|(
name|script
argument_list|,
name|type
argument_list|,
name|lang
argument_list|,
name|params
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructor for Script.      *      * @param script        The cache key of the script to be compiled/executed. For inline scripts this is the actual      *                      script source code. For indexed scripts this is the id used in the request. For on file      *                      scripts this is the file name.      * @param type          The type of script -- dynamic, stored, or file.      * @param lang          The language of the script to be compiled/executed.      * @param params        The map of parameters the script will be executed with.      * @param contentType   The {@link XContentType} of the script. Only relevant for inline scripts that have not been      *                      defined as a plain string, but as json or yaml content. This class needs this information      *                      when serializing the script back to xcontent.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|Script
specifier|public
name|Script
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
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|params
parameter_list|,
annotation|@
name|Nullable
name|XContentType
name|contentType
parameter_list|)
block|{
if|if
condition|(
name|contentType
operator|!=
literal|null
operator|&&
name|type
operator|!=
name|ScriptType
operator|.
name|INLINE
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The parameter contentType only makes sense for inline scripts"
argument_list|)
throw|;
block|}
name|this
operator|.
name|script
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|script
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|lang
operator|=
name|lang
operator|==
literal|null
condition|?
name|DEFAULT_SCRIPT_LANG
else|:
name|lang
expr_stmt|;
name|this
operator|.
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
name|params
expr_stmt|;
name|this
operator|.
name|contentType
operator|=
name|contentType
expr_stmt|;
block|}
DECL|method|Script
specifier|public
name|Script
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|script
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|type
operator|=
name|ScriptType
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|lang
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|params
operator|=
name|in
operator|.
name|readMap
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|contentType
operator|=
name|XContentType
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|script
argument_list|)
expr_stmt|;
name|boolean
name|hasType
init|=
name|type
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasType
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasType
condition|)
block|{
name|type
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|lang
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeMap
argument_list|(
name|params
argument_list|)
expr_stmt|;
name|boolean
name|hasContentType
init|=
name|contentType
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasContentType
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasContentType
condition|)
block|{
name|XContentType
operator|.
name|writeTo
argument_list|(
name|contentType
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Method for getting the script.      * @return The cache key of the script to be compiled/executed.  For dynamic scripts this is the actual      *         script source code.  For indexed scripts this is the id used in the request.  For on disk scripts      *         this is the file name.      */
DECL|method|getScript
specifier|public
name|String
name|getScript
parameter_list|()
block|{
return|return
name|script
return|;
block|}
comment|/**      * Method for getting the type.      *      * @return The type of script -- inline, stored, or file.      */
DECL|method|getType
specifier|public
name|ScriptType
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * Method for getting language.      *      * @return The language of the script to be compiled/executed.      */
DECL|method|getLang
specifier|public
name|String
name|getLang
parameter_list|()
block|{
return|return
name|lang
return|;
block|}
comment|/**      * Method for getting the parameters.      *      * @return The map of parameters the script will be executed with.      */
DECL|method|getParams
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getParams
parameter_list|()
block|{
return|return
name|params
return|;
block|}
comment|/**      * @return The content type of the script if it is an inline script and the script has been defined as json      *         or yaml content instead of a plain string.      */
DECL|method|getContentType
specifier|public
name|XContentType
name|getContentType
parameter_list|()
block|{
return|return
name|contentType
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|builderParams
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
return|return
name|builder
operator|.
name|value
argument_list|(
name|script
argument_list|)
return|;
block|}
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|type
operator|==
name|ScriptType
operator|.
name|INLINE
operator|&&
name|contentType
operator|!=
literal|null
operator|&&
name|builder
operator|.
name|contentType
argument_list|()
operator|==
name|contentType
condition|)
block|{
name|builder
operator|.
name|rawField
argument_list|(
name|type
operator|.
name|getParseField
argument_list|()
operator|.
name|getPreferredName
argument_list|()
argument_list|,
operator|new
name|BytesArray
argument_list|(
name|script
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
name|type
operator|.
name|getParseField
argument_list|()
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|script
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lang
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|ScriptField
operator|.
name|LANG
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|lang
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|params
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|ScriptField
operator|.
name|PARAMS
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|Script
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
return|return
name|parse
argument_list|(
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|Script
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
block|{
try|try
block|{
return|return
name|parse
argument_list|(
name|parser
argument_list|,
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|,
name|context
operator|.
name|getDefaultScriptLanguage
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
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
literal|"Error parsing ["
operator|+
name|ScriptField
operator|.
name|SCRIPT
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|"] field"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|parse
specifier|public
specifier|static
name|Script
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|,
annotation|@
name|Nullable
name|String
name|lang
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
operator|new
name|Script
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|lang
argument_list|,
literal|null
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
name|ElasticsearchParseException
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
name|XContentType
name|contentType
init|=
literal|null
decl_stmt|;
name|String
name|cfn
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
name|cfn
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
name|cfn
argument_list|,
name|ScriptType
operator|.
name|INLINE
operator|.
name|getParseField
argument_list|()
argument_list|)
condition|)
block|{
name|type
operator|=
name|ScriptType
operator|.
name|INLINE
expr_stmt|;
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
name|START_OBJECT
condition|)
block|{
name|contentType
operator|=
name|parser
operator|.
name|contentType
argument_list|()
expr_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|script
operator|=
name|builder
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
operator|.
name|bytes
argument_list|()
operator|.
name|utf8ToString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|script
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|cfn
argument_list|,
name|ScriptType
operator|.
name|FILE
operator|.
name|getParseField
argument_list|()
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
name|ElasticsearchParseException
argument_list|(
literal|"expected a string value for field [{}], but found [{}]"
argument_list|,
name|cfn
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
name|cfn
argument_list|,
name|ScriptType
operator|.
name|STORED
operator|.
name|getParseField
argument_list|()
argument_list|)
condition|)
block|{
name|type
operator|=
name|ScriptType
operator|.
name|STORED
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
name|ElasticsearchParseException
argument_list|(
literal|"expected a string value for field [{}], but found [{}]"
argument_list|,
name|cfn
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
name|cfn
argument_list|,
name|ScriptField
operator|.
name|LANG
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
name|ElasticsearchParseException
argument_list|(
literal|"expected a string value for field [{}], but found [{}]"
argument_list|,
name|cfn
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
name|cfn
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
name|ElasticsearchParseException
argument_list|(
literal|"expected an object for field [{}], but found [{}]"
argument_list|,
name|cfn
argument_list|,
name|token
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"unexpected field [{}]"
argument_list|,
name|cfn
argument_list|)
throw|;
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
name|ElasticsearchParseException
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
name|STORED
operator|.
name|getParseField
argument_list|()
operator|.
name|getPreferredName
argument_list|()
argument_list|)
throw|;
block|}
return|return
operator|new
name|Script
argument_list|(
name|script
argument_list|,
name|type
argument_list|,
name|lang
argument_list|,
name|params
argument_list|,
name|contentType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|lang
argument_list|,
name|params
argument_list|,
name|script
argument_list|,
name|type
argument_list|,
name|contentType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|Script
name|other
init|=
operator|(
name|Script
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|lang
argument_list|,
name|other
operator|.
name|lang
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|params
argument_list|,
name|other
operator|.
name|params
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|script
argument_list|,
name|other
operator|.
name|script
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|type
argument_list|,
name|other
operator|.
name|type
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|contentType
argument_list|,
name|other
operator|.
name|contentType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"[script: "
operator|+
name|script
operator|+
literal|", type: "
operator|+
name|type
operator|.
name|getParseField
argument_list|()
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|", lang: "
operator|+
name|lang
operator|+
literal|", params: "
operator|+
name|params
operator|+
literal|"]"
return|;
block|}
DECL|interface|ScriptField
specifier|public
interface|interface
name|ScriptField
block|{
DECL|field|SCRIPT
name|ParseField
name|SCRIPT
init|=
operator|new
name|ParseField
argument_list|(
literal|"script"
argument_list|)
decl_stmt|;
DECL|field|LANG
name|ParseField
name|LANG
init|=
operator|new
name|ParseField
argument_list|(
literal|"lang"
argument_list|)
decl_stmt|;
DECL|field|PARAMS
name|ParseField
name|PARAMS
init|=
operator|new
name|ParseField
argument_list|(
literal|"params"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

