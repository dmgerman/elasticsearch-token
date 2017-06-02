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
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|storedscripts
operator|.
name|GetStoredScriptRequest
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
name|admin
operator|.
name|cluster
operator|.
name|storedscripts
operator|.
name|GetStoredScriptResponse
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
name|XContentBuilder
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
name|BytesRestResponse
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
name|RestResponse
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
name|RestStatus
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
name|RestBuilderListener
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
name|StoredScriptSource
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

begin_class
DECL|class|RestGetSearchTemplateAction
specifier|public
class|class
name|RestGetSearchTemplateAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|_ID_PARSE_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|_ID_PARSE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"_id"
argument_list|)
decl_stmt|;
DECL|field|FOUND_PARSE_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|FOUND_PARSE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"found"
argument_list|)
decl_stmt|;
DECL|method|RestGetSearchTemplateAction
specifier|public
name|RestGetSearchTemplateAction
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
literal|"/_search/template/{id}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"get_search_template_action"
return|;
block|}
annotation|@
name|Override
DECL|method|prepareRequest
specifier|public
name|RestChannelConsumer
name|prepareRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|id
init|=
name|request
operator|.
name|param
argument_list|(
literal|"id"
argument_list|)
decl_stmt|;
name|GetStoredScriptRequest
name|getRequest
init|=
operator|new
name|GetStoredScriptRequest
argument_list|(
name|id
argument_list|,
name|Script
operator|.
name|DEFAULT_TEMPLATE_LANG
argument_list|)
decl_stmt|;
return|return
name|channel
lambda|->
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|getStoredScript
argument_list|(
name|getRequest
argument_list|,
operator|new
name|RestBuilderListener
argument_list|<
name|GetStoredScriptResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
block|@Override             public RestResponse buildResponse(GetStoredScriptResponse response
operator|,
name|XContentBuilder
name|builder
block|)
throws|throws
name|Exception
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|_ID_PARSE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|StoredScriptSource
operator|.
name|LANG_PARSE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|Script
operator|.
name|DEFAULT_TEMPLATE_LANG
argument_list|)
expr_stmt|;
name|StoredScriptSource
name|source
init|=
name|response
operator|.
name|getSource
argument_list|()
decl_stmt|;
name|boolean
name|found
init|=
name|source
operator|!=
literal|null
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FOUND_PARSE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|found
argument_list|)
expr_stmt|;
if|if
condition|(
name|found
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|StoredScriptSource
operator|.
name|TEMPLATE_PARSE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|source
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|found
condition|?
name|RestStatus
operator|.
name|OK
else|:
name|RestStatus
operator|.
name|NOT_FOUND
argument_list|,
name|builder
argument_list|)
return|;
block|}
block|}
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

unit|} }
end_unit

