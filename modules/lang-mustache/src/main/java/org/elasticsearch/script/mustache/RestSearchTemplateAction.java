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
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|node
operator|.
name|NodeClient
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
name|settings
operator|.
name|Settings
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
name|ObjectParser
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
name|rest
operator|.
name|BaseRestHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestController
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|RestStatusToXContentListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|search
operator|.
name|RestSearchAction
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
name|Set
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|GET
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|POST
import|;
end_import

begin_class
DECL|class|RestSearchTemplateAction
specifier|public
class|class
name|RestSearchTemplateAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|RESPONSE_PARAMS
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|RESPONSE_PARAMS
init|=
name|Collections
operator|.
name|singleton
argument_list|(
name|RestSearchAction
operator|.
name|TYPED_KEYS_PARAM
argument_list|)
decl_stmt|;
DECL|field|PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|SearchTemplateRequest
argument_list|,
name|Void
argument_list|>
name|PARSER
decl_stmt|;
static|static
block|{
name|PARSER
operator|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"search_template"
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|parser
parameter_list|,
name|request
parameter_list|,
name|s
parameter_list|)
lambda|->
name|request
operator|.
name|setScriptParams
argument_list|(
name|parser
operator|.
name|map
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"params"
argument_list|)
argument_list|,
name|ObjectParser
operator|.
name|ValueType
operator|.
name|OBJECT
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareString
argument_list|(
parameter_list|(
name|request
parameter_list|,
name|s
parameter_list|)
lambda|->
block|{
name|request
operator|.
name|setScriptType
argument_list|(
name|ScriptType
operator|.
name|STORED
argument_list|)
expr_stmt|;
name|request
operator|.
name|setScript
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareBoolean
argument_list|(
name|SearchTemplateRequest
operator|::
name|setExplain
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"explain"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareBoolean
argument_list|(
name|SearchTemplateRequest
operator|::
name|setProfile
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"profile"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|parser
parameter_list|,
name|request
parameter_list|,
name|value
parameter_list|)
lambda|->
block|{
name|request
operator|.
name|setScriptType
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|)
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
comment|//convert the template to json which is the only supported XContentType (see CustomMustacheFactory#createEncoder)
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
init|)
block|{
name|request
operator|.
name|setScript
argument_list|(
name|builder
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
operator|.
name|string
argument_list|()
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
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Could not parse inline template"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|request
operator|.
name|setScript
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"inline"
argument_list|,
literal|"template"
argument_list|)
argument_list|,
name|ObjectParser
operator|.
name|ValueType
operator|.
name|OBJECT_OR_STRING
argument_list|)
expr_stmt|;
block|}
DECL|method|RestSearchTemplateAction
specifier|public
name|RestSearchTemplateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_search/template"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/_search/template"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/_search/template"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/_search/template"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/_search/template"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/_search/template"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|prepareRequest
specifier|public
name|RestChannelConsumer
name|prepareRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|request
operator|.
name|hasContentOrSourceParam
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"request body is required"
argument_list|)
throw|;
block|}
comment|// Creates the search request with all required params
name|SearchRequest
name|searchRequest
init|=
operator|new
name|SearchRequest
argument_list|()
decl_stmt|;
name|RestSearchAction
operator|.
name|parseSearchRequest
argument_list|(
name|searchRequest
argument_list|,
name|request
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Creates the search template request
name|SearchTemplateRequest
name|searchTemplateRequest
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|request
operator|.
name|contentOrSourceParamParser
argument_list|()
init|)
block|{
name|searchTemplateRequest
operator|=
name|PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
operator|new
name|SearchTemplateRequest
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|searchTemplateRequest
operator|.
name|setRequest
argument_list|(
name|searchRequest
argument_list|)
expr_stmt|;
return|return
name|channel
lambda|->
name|client
operator|.
name|execute
argument_list|(
name|SearchTemplateAction
operator|.
name|INSTANCE
argument_list|,
name|searchTemplateRequest
argument_list|,
operator|new
name|RestStatusToXContentListener
argument_list|<>
argument_list|(
name|channel
argument_list|)
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|SearchTemplateRequest
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
operator|new
name|SearchTemplateRequest
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|responseParams
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|responseParams
parameter_list|()
block|{
return|return
name|RESPONSE_PARAMS
return|;
block|}
block|}
end_class

end_unit

