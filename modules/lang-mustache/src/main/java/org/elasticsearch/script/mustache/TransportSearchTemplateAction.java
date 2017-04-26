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
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|ActionListener
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
name|action
operator|.
name|search
operator|.
name|SearchResponse
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
name|TransportSearchAction
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
name|support
operator|.
name|ActionFilters
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
name|support
operator|.
name|HandledTransportAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|NamedXContentRegistry
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
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|CompiledScript
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
name|ScriptService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|builder
operator|.
name|SearchSourceBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|template
operator|.
name|CompiledTemplate
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptContext
operator|.
name|Standard
operator|.
name|SEARCH
import|;
end_import

begin_class
DECL|class|TransportSearchTemplateAction
specifier|public
class|class
name|TransportSearchTemplateAction
extends|extends
name|HandledTransportAction
argument_list|<
name|SearchTemplateRequest
argument_list|,
name|SearchTemplateResponse
argument_list|>
block|{
DECL|field|TEMPLATE_LANG
specifier|private
specifier|static
specifier|final
name|String
name|TEMPLATE_LANG
init|=
name|MustacheScriptEngineService
operator|.
name|NAME
decl_stmt|;
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|searchAction
specifier|private
specifier|final
name|TransportSearchAction
name|searchAction
decl_stmt|;
DECL|field|xContentRegistry
specifier|private
specifier|final
name|NamedXContentRegistry
name|xContentRegistry
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportSearchTemplateAction
specifier|public
name|TransportSearchTemplateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|resolver
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|TransportSearchAction
name|searchAction
parameter_list|,
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|SearchTemplateAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|resolver
argument_list|,
name|SearchTemplateRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|searchAction
operator|=
name|searchAction
expr_stmt|;
name|this
operator|.
name|xContentRegistry
operator|=
name|xContentRegistry
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|SearchTemplateRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|SearchTemplateResponse
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|SearchTemplateResponse
name|response
init|=
operator|new
name|SearchTemplateResponse
argument_list|()
decl_stmt|;
try|try
block|{
name|Script
name|script
init|=
operator|new
name|Script
argument_list|(
name|request
operator|.
name|getScriptType
argument_list|()
argument_list|,
name|TEMPLATE_LANG
argument_list|,
name|request
operator|.
name|getScript
argument_list|()
argument_list|,
name|request
operator|.
name|getScriptParams
argument_list|()
operator|==
literal|null
condition|?
name|Collections
operator|.
name|emptyMap
argument_list|()
else|:
name|request
operator|.
name|getScriptParams
argument_list|()
argument_list|)
decl_stmt|;
name|CompiledTemplate
name|compiledScript
init|=
name|scriptService
operator|.
name|compileTemplate
argument_list|(
name|script
argument_list|,
name|SEARCH
argument_list|)
decl_stmt|;
name|BytesReference
name|source
init|=
name|compiledScript
operator|.
name|run
argument_list|(
name|script
operator|.
name|getParams
argument_list|()
argument_list|)
decl_stmt|;
name|response
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|isSimulate
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Executes the search
name|SearchRequest
name|searchRequest
init|=
name|request
operator|.
name|getRequest
argument_list|()
decl_stmt|;
comment|//we can assume the template is always json as we convert it before compiling it
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|xContentRegistry
argument_list|,
name|source
argument_list|)
init|)
block|{
name|SearchSourceBuilder
name|builder
init|=
name|SearchSourceBuilder
operator|.
name|searchSource
argument_list|()
decl_stmt|;
name|builder
operator|.
name|parseXContent
argument_list|(
operator|new
name|QueryParseContext
argument_list|(
name|parser
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|explain
argument_list|(
name|request
operator|.
name|isExplain
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|profile
argument_list|(
name|request
operator|.
name|isProfile
argument_list|()
argument_list|)
expr_stmt|;
name|searchRequest
operator|.
name|source
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|searchAction
operator|.
name|execute
argument_list|(
name|searchRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|)
block|{
try|try
block|{
name|response
operator|.
name|setResponse
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|t
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|t
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|t
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

