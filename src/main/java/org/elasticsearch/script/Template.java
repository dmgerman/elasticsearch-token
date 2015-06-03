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
name|script
operator|.
name|ScriptService
operator|.
name|ScriptType
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
name|mustache
operator|.
name|MustacheScriptEngineService
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
name|Map
import|;
end_import

begin_class
DECL|class|Template
specifier|public
class|class
name|Template
extends|extends
name|Script
block|{
DECL|field|contentType
specifier|private
name|XContentType
name|contentType
decl_stmt|;
DECL|method|Template
specifier|public
name|Template
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**      * Constructor for simple inline template. The template will have no lang,      * content type or params set.      *       * @param template      *            The inline template.      */
DECL|method|Template
specifier|public
name|Template
parameter_list|(
name|String
name|template
parameter_list|)
block|{
name|super
argument_list|(
name|template
argument_list|,
name|MustacheScriptEngineService
operator|.
name|NAME
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructor for Template.      *       * @param template      *            The cache key of the template to be compiled/executed. For      *            inline templates this is the actual templates source code. For      *            indexed templates this is the id used in the request. For on      *            file templates this is the file name.      * @param type      *            The type of template -- dynamic, indexed, or file.      * @param lang      *            The language of the template to be compiled/executed.      * @param xContentType      *            The {@link XContentType} of the template.      * @param params      *            The map of parameters the template will be executed with.      */
DECL|method|Template
specifier|public
name|Template
parameter_list|(
name|String
name|template
parameter_list|,
name|ScriptType
name|type
parameter_list|,
annotation|@
name|Nullable
name|String
name|lang
parameter_list|,
annotation|@
name|Nullable
name|XContentType
name|xContentType
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
name|super
argument_list|(
name|template
argument_list|,
name|type
argument_list|,
name|lang
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|this
operator|.
name|contentType
operator|=
name|xContentType
expr_stmt|;
block|}
comment|/**      * Method for getting the {@link XContentType} of the template.      *       * @return The {@link XContentType} of the template.      */
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
DECL|method|doReadFrom
specifier|protected
name|void
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|this
operator|.
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
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
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
annotation|@
name|Override
DECL|method|scriptFieldToXContent
specifier|protected
name|XContentBuilder
name|scriptFieldToXContent
parameter_list|(
name|String
name|template
parameter_list|,
name|ScriptType
name|type
parameter_list|,
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
name|template
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
name|template
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|readTemplate
specifier|public
specifier|static
name|Template
name|readTemplate
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Template
name|template
init|=
operator|new
name|Template
argument_list|()
decl_stmt|;
name|template
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|template
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|parse
specifier|public
specifier|static
name|Script
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
parameter_list|)
block|{
return|return
operator|new
name|TemplateParser
argument_list|(
name|Collections
operator|.
name|EMPTY_MAP
argument_list|,
name|MustacheScriptEngineService
operator|.
name|NAME
argument_list|)
operator|.
name|parse
argument_list|(
name|config
argument_list|,
name|removeMatchedEntries
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|parse
specifier|public
specifier|static
name|Template
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TemplateParser
argument_list|(
name|Collections
operator|.
name|EMPTY_MAP
argument_list|,
name|MustacheScriptEngineService
operator|.
name|NAME
argument_list|)
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
return|;
block|}
annotation|@
name|Deprecated
DECL|method|parse
specifier|public
specifier|static
name|Template
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptType
argument_list|>
name|additionalTemplateFieldNames
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TemplateParser
argument_list|(
name|additionalTemplateFieldNames
argument_list|,
name|MustacheScriptEngineService
operator|.
name|NAME
argument_list|)
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
return|;
block|}
annotation|@
name|Deprecated
DECL|method|parse
specifier|public
specifier|static
name|Template
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptType
argument_list|>
name|additionalTemplateFieldNames
parameter_list|,
name|String
name|defaultLang
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TemplateParser
argument_list|(
name|additionalTemplateFieldNames
argument_list|,
name|defaultLang
argument_list|)
operator|.
name|parse
argument_list|(
name|parser
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
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
name|super
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|contentType
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|contentType
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|result
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
operator|!
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
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
name|Template
name|other
init|=
operator|(
name|Template
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|contentType
operator|!=
name|other
operator|.
name|contentType
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
DECL|class|TemplateParser
specifier|private
specifier|static
class|class
name|TemplateParser
extends|extends
name|AbstractScriptParser
argument_list|<
name|Template
argument_list|>
block|{
DECL|field|contentType
specifier|private
name|XContentType
name|contentType
init|=
literal|null
decl_stmt|;
DECL|field|additionalTemplateFieldNames
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptType
argument_list|>
name|additionalTemplateFieldNames
decl_stmt|;
DECL|field|defaultLang
specifier|private
name|String
name|defaultLang
decl_stmt|;
DECL|method|TemplateParser
specifier|public
name|TemplateParser
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptType
argument_list|>
name|additionalTemplateFieldNames
parameter_list|,
name|String
name|defaultLang
parameter_list|)
block|{
name|this
operator|.
name|additionalTemplateFieldNames
operator|=
name|additionalTemplateFieldNames
expr_stmt|;
name|this
operator|.
name|defaultLang
operator|=
name|defaultLang
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createSimpleScript
specifier|protected
name|Template
name|createSimpleScript
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Template
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|parser
operator|.
name|objectText
argument_list|()
argument_list|)
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|MustacheScriptEngineService
operator|.
name|NAME
argument_list|,
name|contentType
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createScript
specifier|protected
name|Template
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
block|{
return|return
operator|new
name|Template
argument_list|(
name|script
argument_list|,
name|type
argument_list|,
name|lang
argument_list|,
name|contentType
argument_list|,
name|params
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|parseInlineScript
specifier|protected
name|String
name|parseInlineScript
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
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
return|return
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
name|toUtf8
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|parser
operator|.
name|text
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
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
name|additionalTemplateFieldNames
return|;
block|}
annotation|@
name|Override
DECL|method|getDefaultScriptLang
specifier|protected
name|String
name|getDefaultScriptLang
parameter_list|()
block|{
return|return
name|defaultLang
return|;
block|}
block|}
block|}
end_class

end_unit

