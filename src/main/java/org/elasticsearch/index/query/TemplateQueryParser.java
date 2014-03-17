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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Query
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
name|bytes
operator|.
name|BytesReference
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
name|script
operator|.
name|ExecutableScript
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

begin_comment
comment|/**  * In the simplest case, parse template string and variables from the request, compile the template and  * execute the template against the given variables.  * */
end_comment

begin_class
DECL|class|TemplateQueryParser
specifier|public
class|class
name|TemplateQueryParser
implements|implements
name|QueryParser
block|{
comment|/** Name to reference this type of query. */
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"template"
decl_stmt|;
comment|/** Name of query parameter containing the template string. */
DECL|field|QUERY
specifier|public
specifier|static
specifier|final
name|String
name|QUERY
init|=
literal|"query"
decl_stmt|;
comment|/** Name of query parameter containing the template parameters. */
DECL|field|PARAMS
specifier|public
specifier|static
specifier|final
name|String
name|PARAMS
init|=
literal|"params"
decl_stmt|;
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TemplateQueryParser
specifier|public
name|TemplateQueryParser
parameter_list|(
name|ScriptService
name|scriptService
parameter_list|)
block|{
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
block|}
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
name|NAME
block|}
return|;
block|}
annotation|@
name|Override
annotation|@
name|Nullable
DECL|method|parse
specifier|public
name|Query
name|parse
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|TemplateContext
name|templateContext
init|=
name|parse
argument_list|(
name|parser
argument_list|,
name|QUERY
argument_list|,
name|PARAMS
argument_list|)
decl_stmt|;
name|ExecutableScript
name|executable
init|=
name|this
operator|.
name|scriptService
operator|.
name|executable
argument_list|(
literal|"mustache"
argument_list|,
name|templateContext
operator|.
name|template
argument_list|()
argument_list|,
name|templateContext
operator|.
name|params
argument_list|()
argument_list|)
decl_stmt|;
name|BytesReference
name|querySource
init|=
operator|(
name|BytesReference
operator|)
name|executable
operator|.
name|run
argument_list|()
decl_stmt|;
name|XContentParser
name|qSourceParser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|querySource
argument_list|)
operator|.
name|createParser
argument_list|(
name|querySource
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
name|parseContext
operator|.
name|indexQueryParser
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|qSourceParser
argument_list|)
expr_stmt|;
name|Query
name|result
init|=
name|context
operator|.
name|parseInnerQuery
argument_list|()
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
finally|finally
block|{
name|qSourceParser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|parse
specifier|public
specifier|static
name|TemplateContext
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|String
name|templateFieldname
parameter_list|,
name|String
name|paramsFieldname
parameter_list|)
throws|throws
name|IOException
block|{
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
name|templateNameOrTemplateContent
init|=
literal|null
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
name|templateFieldname
operator|.
name|equals
argument_list|(
name|currentFieldName
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
operator|&&
operator|!
name|parser
operator|.
name|hasTextCharacters
argument_list|()
condition|)
block|{
name|XContentBuilder
name|builder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|parser
operator|.
name|contentType
argument_list|()
operator|.
name|xContent
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|templateNameOrTemplateContent
operator|=
name|builder
operator|.
name|string
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|templateNameOrTemplateContent
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
name|paramsFieldname
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
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
block|}
return|return
operator|new
name|TemplateContext
argument_list|(
name|templateNameOrTemplateContent
argument_list|,
name|params
argument_list|)
return|;
block|}
DECL|class|TemplateContext
specifier|public
specifier|static
class|class
name|TemplateContext
block|{
DECL|field|params
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
decl_stmt|;
DECL|field|template
specifier|private
name|String
name|template
decl_stmt|;
DECL|method|TemplateContext
specifier|public
name|TemplateContext
parameter_list|(
name|String
name|templateName
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
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
name|this
operator|.
name|template
operator|=
name|templateName
expr_stmt|;
block|}
DECL|method|params
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|()
block|{
return|return
name|params
return|;
block|}
DECL|method|template
specifier|public
name|String
name|template
parameter_list|()
block|{
return|return
name|template
return|;
block|}
block|}
block|}
end_class

end_unit

