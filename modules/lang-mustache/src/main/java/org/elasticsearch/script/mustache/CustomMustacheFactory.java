begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.mustache
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|mustache
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|io
operator|.
name|JsonStringEncoder
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|Code
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|DefaultMustacheFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|DefaultMustacheVisitor
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|Mustache
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|MustacheException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|MustacheVisitor
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|TemplateContext
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|codes
operator|.
name|DefaultMustache
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|codes
operator|.
name|IterableCode
import|;
end_import

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|codes
operator|.
name|WriteCode
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
name|XContentType
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
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Writer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLEncoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|HashMap
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|StringJoiner
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_class
DECL|class|CustomMustacheFactory
specifier|public
class|class
name|CustomMustacheFactory
extends|extends
name|DefaultMustacheFactory
block|{
DECL|field|CONTENT_TYPE_PARAM
specifier|static
specifier|final
name|String
name|CONTENT_TYPE_PARAM
init|=
literal|"content_type"
decl_stmt|;
DECL|field|JSON_MIME_TYPE_WITH_CHARSET
specifier|static
specifier|final
name|String
name|JSON_MIME_TYPE_WITH_CHARSET
init|=
literal|"application/json; charset=UTF-8"
decl_stmt|;
DECL|field|JSON_MIME_TYPE
specifier|static
specifier|final
name|String
name|JSON_MIME_TYPE
init|=
literal|"application/json"
decl_stmt|;
DECL|field|PLAIN_TEXT_MIME_TYPE
specifier|static
specifier|final
name|String
name|PLAIN_TEXT_MIME_TYPE
init|=
literal|"text/plain"
decl_stmt|;
DECL|field|X_WWW_FORM_URLENCODED_MIME_TYPE
specifier|static
specifier|final
name|String
name|X_WWW_FORM_URLENCODED_MIME_TYPE
init|=
literal|"application/x-www-form-urlencoded"
decl_stmt|;
DECL|field|DEFAULT_MIME_TYPE
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_MIME_TYPE
init|=
name|JSON_MIME_TYPE
decl_stmt|;
DECL|field|ENCODERS
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|Encoder
argument_list|>
argument_list|>
name|ENCODERS
decl_stmt|;
static|static
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|Encoder
argument_list|>
argument_list|>
name|encoders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|encoders
operator|.
name|put
argument_list|(
name|JSON_MIME_TYPE_WITH_CHARSET
argument_list|,
name|JsonEscapeEncoder
operator|::
operator|new
argument_list|)
expr_stmt|;
name|encoders
operator|.
name|put
argument_list|(
name|JSON_MIME_TYPE
argument_list|,
name|JsonEscapeEncoder
operator|::
operator|new
argument_list|)
expr_stmt|;
name|encoders
operator|.
name|put
argument_list|(
name|PLAIN_TEXT_MIME_TYPE
argument_list|,
name|DefaultEncoder
operator|::
operator|new
argument_list|)
expr_stmt|;
name|encoders
operator|.
name|put
argument_list|(
name|X_WWW_FORM_URLENCODED_MIME_TYPE
argument_list|,
name|UrlEncoder
operator|::
operator|new
argument_list|)
expr_stmt|;
name|ENCODERS
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|encoders
argument_list|)
expr_stmt|;
block|}
DECL|field|encoder
specifier|private
specifier|final
name|Encoder
name|encoder
decl_stmt|;
DECL|method|CustomMustacheFactory
specifier|public
name|CustomMustacheFactory
parameter_list|(
name|String
name|mimeType
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|setObjectHandler
argument_list|(
operator|new
name|CustomReflectionObjectHandler
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|encoder
operator|=
name|createEncoder
argument_list|(
name|mimeType
argument_list|)
expr_stmt|;
block|}
DECL|method|CustomMustacheFactory
specifier|public
name|CustomMustacheFactory
parameter_list|()
block|{
name|this
argument_list|(
name|DEFAULT_MIME_TYPE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|encode
specifier|public
name|void
name|encode
parameter_list|(
name|String
name|value
parameter_list|,
name|Writer
name|writer
parameter_list|)
block|{
try|try
block|{
name|encoder
operator|.
name|encode
argument_list|(
name|value
argument_list|,
name|writer
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MustacheException
argument_list|(
literal|"Unable to encode value"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|createEncoder
specifier|static
name|Encoder
name|createEncoder
parameter_list|(
name|String
name|mimeType
parameter_list|)
block|{
name|Supplier
argument_list|<
name|Encoder
argument_list|>
name|supplier
init|=
name|ENCODERS
operator|.
name|get
argument_list|(
name|mimeType
argument_list|)
decl_stmt|;
if|if
condition|(
name|supplier
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No encoder found for MIME type ["
operator|+
name|mimeType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|supplier
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|createMustacheVisitor
specifier|public
name|MustacheVisitor
name|createMustacheVisitor
parameter_list|()
block|{
return|return
operator|new
name|CustomMustacheVisitor
argument_list|(
name|this
argument_list|)
return|;
block|}
DECL|class|CustomMustacheVisitor
class|class
name|CustomMustacheVisitor
extends|extends
name|DefaultMustacheVisitor
block|{
DECL|method|CustomMustacheVisitor
name|CustomMustacheVisitor
parameter_list|(
name|DefaultMustacheFactory
name|df
parameter_list|)
block|{
name|super
argument_list|(
name|df
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterable
specifier|public
name|void
name|iterable
parameter_list|(
name|TemplateContext
name|templateContext
parameter_list|,
name|String
name|variable
parameter_list|,
name|Mustache
name|mustache
parameter_list|)
block|{
if|if
condition|(
name|ToJsonCode
operator|.
name|match
argument_list|(
name|variable
argument_list|)
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|ToJsonCode
argument_list|(
name|templateContext
argument_list|,
name|df
argument_list|,
name|mustache
argument_list|,
name|variable
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|JoinerCode
operator|.
name|match
argument_list|(
name|variable
argument_list|)
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|JoinerCode
argument_list|(
name|templateContext
argument_list|,
name|df
argument_list|,
name|mustache
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|CustomJoinerCode
operator|.
name|match
argument_list|(
name|variable
argument_list|)
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|CustomJoinerCode
argument_list|(
name|templateContext
argument_list|,
name|df
argument_list|,
name|mustache
argument_list|,
name|variable
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|UrlEncoderCode
operator|.
name|match
argument_list|(
name|variable
argument_list|)
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|UrlEncoderCode
argument_list|(
name|templateContext
argument_list|,
name|df
argument_list|,
name|mustache
argument_list|,
name|variable
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|IterableCode
argument_list|(
name|templateContext
argument_list|,
name|df
argument_list|,
name|mustache
argument_list|,
name|variable
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Base class for custom Mustache functions      */
DECL|class|CustomCode
specifier|abstract
specifier|static
class|class
name|CustomCode
extends|extends
name|IterableCode
block|{
DECL|field|code
specifier|private
specifier|final
name|String
name|code
decl_stmt|;
DECL|method|CustomCode
name|CustomCode
parameter_list|(
name|TemplateContext
name|tc
parameter_list|,
name|DefaultMustacheFactory
name|df
parameter_list|,
name|Mustache
name|mustache
parameter_list|,
name|String
name|code
parameter_list|)
block|{
name|super
argument_list|(
name|tc
argument_list|,
name|df
argument_list|,
name|mustache
argument_list|,
name|extractVariableName
argument_list|(
name|code
argument_list|,
name|mustache
argument_list|,
name|tc
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|code
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|code
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|Writer
name|execute
parameter_list|(
name|Writer
name|writer
parameter_list|,
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|scopes
parameter_list|)
block|{
name|Object
name|resolved
init|=
name|get
argument_list|(
name|scopes
argument_list|)
decl_stmt|;
name|writer
operator|=
name|handle
argument_list|(
name|writer
argument_list|,
name|createFunction
argument_list|(
name|resolved
argument_list|)
argument_list|,
name|scopes
argument_list|)
expr_stmt|;
name|appendText
argument_list|(
name|writer
argument_list|)
expr_stmt|;
return|return
name|writer
return|;
block|}
annotation|@
name|Override
DECL|method|tag
specifier|protected
name|void
name|tag
parameter_list|(
name|Writer
name|writer
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|write
argument_list|(
name|tc
operator|.
name|startChars
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|code
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|tc
operator|.
name|endChars
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|createFunction
specifier|protected
specifier|abstract
name|Function
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|createFunction
parameter_list|(
name|Object
name|resolved
parameter_list|)
function_decl|;
comment|/**          * At compile time, this function extracts the name of the variable:          * {{#toJson}}variable_name{{/toJson}}          */
DECL|method|extractVariableName
specifier|protected
specifier|static
name|String
name|extractVariableName
parameter_list|(
name|String
name|fn
parameter_list|,
name|Mustache
name|mustache
parameter_list|,
name|TemplateContext
name|tc
parameter_list|)
block|{
name|Code
index|[]
name|codes
init|=
name|mustache
operator|.
name|getCodes
argument_list|()
decl_stmt|;
if|if
condition|(
name|codes
operator|==
literal|null
operator|||
name|codes
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|MustacheException
argument_list|(
literal|"Mustache function ["
operator|+
name|fn
operator|+
literal|"] must contain one and only one identifier"
argument_list|)
throw|;
block|}
try|try
init|(
name|StringWriter
name|capture
init|=
operator|new
name|StringWriter
argument_list|()
init|)
block|{
comment|// Variable name is in plain text and has type WriteCode
if|if
condition|(
name|codes
index|[
literal|0
index|]
operator|instanceof
name|WriteCode
condition|)
block|{
name|codes
index|[
literal|0
index|]
operator|.
name|execute
argument_list|(
name|capture
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|capture
operator|.
name|toString
argument_list|()
return|;
block|}
else|else
block|{
name|codes
index|[
literal|0
index|]
operator|.
name|identity
argument_list|(
name|capture
argument_list|)
expr_stmt|;
return|return
name|capture
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MustacheException
argument_list|(
literal|"Exception while parsing mustache function ["
operator|+
name|fn
operator|+
literal|"] at line "
operator|+
name|tc
operator|.
name|line
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**      * This function renders {@link Iterable} and {@link Map} as their JSON representation      */
DECL|class|ToJsonCode
specifier|static
class|class
name|ToJsonCode
extends|extends
name|CustomCode
block|{
DECL|field|CODE
specifier|private
specifier|static
specifier|final
name|String
name|CODE
init|=
literal|"toJson"
decl_stmt|;
DECL|method|ToJsonCode
name|ToJsonCode
parameter_list|(
name|TemplateContext
name|tc
parameter_list|,
name|DefaultMustacheFactory
name|df
parameter_list|,
name|Mustache
name|mustache
parameter_list|,
name|String
name|variable
parameter_list|)
block|{
name|super
argument_list|(
name|tc
argument_list|,
name|df
argument_list|,
name|mustache
argument_list|,
name|CODE
argument_list|)
expr_stmt|;
if|if
condition|(
name|CODE
operator|.
name|equalsIgnoreCase
argument_list|(
name|variable
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|MustacheException
argument_list|(
literal|"Mismatch function code ["
operator|+
name|CODE
operator|+
literal|"] cannot be applied to ["
operator|+
name|variable
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|createFunction
specifier|protected
name|Function
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|createFunction
parameter_list|(
name|Object
name|resolved
parameter_list|)
block|{
return|return
name|s
lambda|->
block|{
if|if
condition|(
name|resolved
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
argument_list|)
init|)
block|{
if|if
condition|(
name|resolved
operator|==
literal|null
condition|)
block|{
name|builder
operator|.
name|nullValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|resolved
operator|instanceof
name|Iterable
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|()
expr_stmt|;
for|for
control|(
name|Object
name|o
range|:
operator|(
name|Iterable
operator|)
name|resolved
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|o
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|resolved
operator|instanceof
name|Map
condition|)
block|{
name|builder
operator|.
name|map
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
operator|)
name|resolved
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Do not handle as JSON
return|return
name|oh
operator|.
name|stringify
argument_list|(
name|resolved
argument_list|)
return|;
block|}
return|return
name|builder
operator|.
name|string
argument_list|()
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
name|MustacheException
argument_list|(
literal|"Failed to convert object to JSON"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|;
block|}
DECL|method|match
specifier|static
name|boolean
name|match
parameter_list|(
name|String
name|variable
parameter_list|)
block|{
return|return
name|CODE
operator|.
name|equalsIgnoreCase
argument_list|(
name|variable
argument_list|)
return|;
block|}
block|}
comment|/**      * This function concatenates the values of an {@link Iterable} using a given delimiter      */
DECL|class|JoinerCode
specifier|static
class|class
name|JoinerCode
extends|extends
name|CustomCode
block|{
DECL|field|CODE
specifier|protected
specifier|static
specifier|final
name|String
name|CODE
init|=
literal|"join"
decl_stmt|;
DECL|field|DEFAULT_DELIMITER
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_DELIMITER
init|=
literal|","
decl_stmt|;
DECL|field|delimiter
specifier|private
specifier|final
name|String
name|delimiter
decl_stmt|;
DECL|method|JoinerCode
name|JoinerCode
parameter_list|(
name|TemplateContext
name|tc
parameter_list|,
name|DefaultMustacheFactory
name|df
parameter_list|,
name|Mustache
name|mustache
parameter_list|,
name|String
name|delimiter
parameter_list|)
block|{
name|super
argument_list|(
name|tc
argument_list|,
name|df
argument_list|,
name|mustache
argument_list|,
name|CODE
argument_list|)
expr_stmt|;
name|this
operator|.
name|delimiter
operator|=
name|delimiter
expr_stmt|;
block|}
DECL|method|JoinerCode
name|JoinerCode
parameter_list|(
name|TemplateContext
name|tc
parameter_list|,
name|DefaultMustacheFactory
name|df
parameter_list|,
name|Mustache
name|mustache
parameter_list|)
block|{
name|this
argument_list|(
name|tc
argument_list|,
name|df
argument_list|,
name|mustache
argument_list|,
name|DEFAULT_DELIMITER
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createFunction
specifier|protected
name|Function
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|createFunction
parameter_list|(
name|Object
name|resolved
parameter_list|)
block|{
return|return
name|s
lambda|->
block|{
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|resolved
operator|instanceof
name|Iterable
condition|)
block|{
name|StringJoiner
name|joiner
init|=
operator|new
name|StringJoiner
argument_list|(
name|delimiter
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|o
range|:
operator|(
name|Iterable
operator|)
name|resolved
control|)
block|{
name|joiner
operator|.
name|add
argument_list|(
name|oh
operator|.
name|stringify
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|joiner
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
name|s
return|;
block|}
return|;
block|}
DECL|method|match
specifier|static
name|boolean
name|match
parameter_list|(
name|String
name|variable
parameter_list|)
block|{
return|return
name|CODE
operator|.
name|equalsIgnoreCase
argument_list|(
name|variable
argument_list|)
return|;
block|}
block|}
DECL|class|CustomJoinerCode
specifier|static
class|class
name|CustomJoinerCode
extends|extends
name|JoinerCode
block|{
DECL|field|PATTERN
specifier|private
specifier|static
specifier|final
name|Pattern
name|PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^(?:"
operator|+
name|CODE
operator|+
literal|" delimiter='(.*)')$"
argument_list|)
decl_stmt|;
DECL|method|CustomJoinerCode
name|CustomJoinerCode
parameter_list|(
name|TemplateContext
name|tc
parameter_list|,
name|DefaultMustacheFactory
name|df
parameter_list|,
name|Mustache
name|mustache
parameter_list|,
name|String
name|variable
parameter_list|)
block|{
name|super
argument_list|(
name|tc
argument_list|,
name|df
argument_list|,
name|mustache
argument_list|,
name|extractDelimiter
argument_list|(
name|variable
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|extractDelimiter
specifier|private
specifier|static
name|String
name|extractDelimiter
parameter_list|(
name|String
name|variable
parameter_list|)
block|{
name|Matcher
name|matcher
init|=
name|PATTERN
operator|.
name|matcher
argument_list|(
name|variable
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|find
argument_list|()
condition|)
block|{
return|return
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
return|;
block|}
throw|throw
operator|new
name|MustacheException
argument_list|(
literal|"Failed to extract delimiter for join function"
argument_list|)
throw|;
block|}
DECL|method|match
specifier|static
name|boolean
name|match
parameter_list|(
name|String
name|variable
parameter_list|)
block|{
return|return
name|PATTERN
operator|.
name|matcher
argument_list|(
name|variable
argument_list|)
operator|.
name|matches
argument_list|()
return|;
block|}
block|}
comment|/**      * This function encodes a string using the {@link URLEncoder#encode(String, String)} method      * with the UTF-8 charset.      */
DECL|class|UrlEncoderCode
specifier|static
class|class
name|UrlEncoderCode
extends|extends
name|DefaultMustache
block|{
DECL|field|CODE
specifier|private
specifier|static
specifier|final
name|String
name|CODE
init|=
literal|"url"
decl_stmt|;
DECL|field|encoder
specifier|private
specifier|final
name|Encoder
name|encoder
decl_stmt|;
DECL|method|UrlEncoderCode
name|UrlEncoderCode
parameter_list|(
name|TemplateContext
name|tc
parameter_list|,
name|DefaultMustacheFactory
name|df
parameter_list|,
name|Mustache
name|mustache
parameter_list|,
name|String
name|variable
parameter_list|)
block|{
name|super
argument_list|(
name|tc
argument_list|,
name|df
argument_list|,
name|mustache
operator|.
name|getCodes
argument_list|()
argument_list|,
name|variable
argument_list|)
expr_stmt|;
name|this
operator|.
name|encoder
operator|=
operator|new
name|UrlEncoder
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|Writer
name|run
parameter_list|(
name|Writer
name|writer
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|scopes
parameter_list|)
block|{
if|if
condition|(
name|getCodes
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Code
name|code
range|:
name|getCodes
argument_list|()
control|)
block|{
try|try
init|(
name|StringWriter
name|capture
init|=
operator|new
name|StringWriter
argument_list|()
init|)
block|{
name|code
operator|.
name|execute
argument_list|(
name|capture
argument_list|,
name|scopes
argument_list|)
expr_stmt|;
name|String
name|s
init|=
name|capture
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|s
operator|!=
literal|null
condition|)
block|{
name|encoder
operator|.
name|encode
argument_list|(
name|s
argument_list|,
name|writer
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MustacheException
argument_list|(
literal|"Exception while parsing mustache function at line "
operator|+
name|tc
operator|.
name|line
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
return|return
name|writer
return|;
block|}
DECL|method|match
specifier|static
name|boolean
name|match
parameter_list|(
name|String
name|variable
parameter_list|)
block|{
return|return
name|CODE
operator|.
name|equalsIgnoreCase
argument_list|(
name|variable
argument_list|)
return|;
block|}
block|}
annotation|@
name|FunctionalInterface
DECL|interface|Encoder
interface|interface
name|Encoder
block|{
comment|/**          * Encodes the {@code s} string and writes it to the {@code writer} {@link Writer}.          *          * @param s      The string to encode          * @param writer The {@link Writer} to which the encoded string will be written to          */
DECL|method|encode
name|void
name|encode
parameter_list|(
name|String
name|s
parameter_list|,
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**      * Encoder that simply writes the string to the writer without encoding.      */
DECL|class|DefaultEncoder
specifier|static
class|class
name|DefaultEncoder
implements|implements
name|Encoder
block|{
annotation|@
name|Override
DECL|method|encode
specifier|public
name|void
name|encode
parameter_list|(
name|String
name|s
parameter_list|,
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|write
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Encoder that escapes JSON string values/fields.      */
DECL|class|JsonEscapeEncoder
specifier|static
class|class
name|JsonEscapeEncoder
implements|implements
name|Encoder
block|{
annotation|@
name|Override
DECL|method|encode
specifier|public
name|void
name|encode
parameter_list|(
name|String
name|s
parameter_list|,
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|write
argument_list|(
name|JsonStringEncoder
operator|.
name|getInstance
argument_list|()
operator|.
name|quoteAsString
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Encoder that escapes strings using HTML form encoding      */
DECL|class|UrlEncoder
specifier|static
class|class
name|UrlEncoder
implements|implements
name|Encoder
block|{
annotation|@
name|Override
DECL|method|encode
specifier|public
name|void
name|encode
parameter_list|(
name|String
name|s
parameter_list|,
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|write
argument_list|(
name|URLEncoder
operator|.
name|encode
argument_list|(
name|s
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

