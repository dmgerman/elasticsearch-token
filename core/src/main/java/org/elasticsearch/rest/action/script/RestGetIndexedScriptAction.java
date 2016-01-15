begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
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
name|action
operator|.
name|indexedscripts
operator|.
name|get
operator|.
name|GetIndexedScriptRequest
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
name|indexedscripts
operator|.
name|get
operator|.
name|GetIndexedScriptResponse
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
name|Client
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
name|XContentBuilderString
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
name|VersionType
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
name|RestChannel
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
name|support
operator|.
name|RestBuilderListener
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestGetIndexedScriptAction
specifier|public
class|class
name|RestGetIndexedScriptAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestGetIndexedScriptAction
specifier|public
name|RestGetIndexedScriptAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|this
argument_list|(
name|settings
argument_list|,
name|controller
argument_list|,
literal|true
argument_list|,
name|client
argument_list|)
expr_stmt|;
block|}
DECL|method|RestGetIndexedScriptAction
specifier|protected
name|RestGetIndexedScriptAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|boolean
name|registerDefaultHandlers
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|controller
argument_list|,
name|client
argument_list|)
expr_stmt|;
if|if
condition|(
name|registerDefaultHandlers
condition|)
block|{
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_scripts/{lang}/{id}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getScriptFieldName
specifier|protected
name|XContentBuilderString
name|getScriptFieldName
parameter_list|()
block|{
return|return
name|Fields
operator|.
name|SCRIPT
return|;
block|}
DECL|method|getScriptLang
specifier|protected
name|String
name|getScriptLang
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
return|return
name|request
operator|.
name|param
argument_list|(
literal|"lang"
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
specifier|final
name|GetIndexedScriptRequest
name|getRequest
init|=
operator|new
name|GetIndexedScriptRequest
argument_list|(
name|getScriptLang
argument_list|(
name|request
argument_list|)
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"id"
argument_list|)
argument_list|)
decl_stmt|;
name|getRequest
operator|.
name|version
argument_list|(
name|request
operator|.
name|paramAsLong
argument_list|(
literal|"version"
argument_list|,
name|getRequest
operator|.
name|version
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|versionType
argument_list|(
name|VersionType
operator|.
name|fromString
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"version_type"
argument_list|)
argument_list|,
name|getRequest
operator|.
name|versionType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|getIndexedScript
argument_list|(
name|getRequest
argument_list|,
operator|new
name|RestBuilderListener
argument_list|<
name|GetIndexedScriptResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|RestResponse
name|buildResponse
parameter_list|(
name|GetIndexedScriptResponse
name|response
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|)
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
name|Fields
operator|.
name|LANG
argument_list|,
name|response
operator|.
name|getScriptLang
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_ID
argument_list|,
name|response
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|FOUND
argument_list|,
name|response
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|RestStatus
name|status
init|=
name|RestStatus
operator|.
name|NOT_FOUND
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_VERSION
argument_list|,
name|response
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|getScriptFieldName
argument_list|()
argument_list|,
name|response
operator|.
name|getScript
argument_list|()
argument_list|)
expr_stmt|;
name|status
operator|=
name|RestStatus
operator|.
name|OK
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
name|status
argument_list|,
name|builder
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|Fields
specifier|private
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|SCRIPT
specifier|private
specifier|static
specifier|final
name|XContentBuilderString
name|SCRIPT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"script"
argument_list|)
decl_stmt|;
DECL|field|LANG
specifier|private
specifier|static
specifier|final
name|XContentBuilderString
name|LANG
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"lang"
argument_list|)
decl_stmt|;
DECL|field|_ID
specifier|private
specifier|static
specifier|final
name|XContentBuilderString
name|_ID
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_id"
argument_list|)
decl_stmt|;
DECL|field|_VERSION
specifier|private
specifier|static
specifier|final
name|XContentBuilderString
name|_VERSION
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_version"
argument_list|)
decl_stmt|;
DECL|field|FOUND
specifier|private
specifier|static
specifier|final
name|XContentBuilderString
name|FOUND
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"found"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

