begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.document
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|document
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
name|get
operator|.
name|GetRequest
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
name|get
operator|.
name|GetResponse
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
name|Strings
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
name|action
operator|.
name|RestActions
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
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|FetchSourceContext
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|NOT_FOUND
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
name|RestStatus
operator|.
name|OK
import|;
end_import

begin_class
DECL|class|RestGetAction
specifier|public
class|class
name|RestGetAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestGetAction
specifier|public
name|RestGetAction
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
literal|"/{index}/{type}/{id}"
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
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"type"
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
name|operationThreaded
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|refresh
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"refresh"
argument_list|,
name|getRequest
operator|.
name|refresh
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|routing
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
comment|// order is important, set it after routing, so it will set the routing
name|getRequest
operator|.
name|parent
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"parent"
argument_list|)
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|preference
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"preference"
argument_list|)
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|realtime
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"realtime"
argument_list|,
name|getRequest
operator|.
name|realtime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|param
argument_list|(
literal|"fields"
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The parameter [fields] is no longer supported, "
operator|+
literal|"please use [stored_fields] to retrieve stored fields or or [_source] to load the field from _source"
argument_list|)
throw|;
block|}
name|String
name|sField
init|=
name|request
operator|.
name|param
argument_list|(
literal|"stored_fields"
argument_list|)
decl_stmt|;
if|if
condition|(
name|sField
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|sFields
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|sField
argument_list|)
decl_stmt|;
if|if
condition|(
name|sFields
operator|!=
literal|null
condition|)
block|{
name|getRequest
operator|.
name|storedFields
argument_list|(
name|sFields
argument_list|)
expr_stmt|;
block|}
block|}
name|getRequest
operator|.
name|version
argument_list|(
name|RestActions
operator|.
name|parseVersion
argument_list|(
name|request
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
name|getRequest
operator|.
name|fetchSourceContext
argument_list|(
name|FetchSourceContext
operator|.
name|parseFromRestRequest
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|channel
lambda|->
name|client
operator|.
name|get
argument_list|(
name|getRequest
argument_list|,
operator|new
name|RestBuilderListener
argument_list|<
name|GetResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
block|@Override             public RestResponse buildResponse(GetResponse response
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
name|response
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|response
operator|.
name|isExists
argument_list|()
condition|)
block|{
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|NOT_FOUND
argument_list|,
name|builder
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|OK
argument_list|,
name|builder
argument_list|)
return|;
block|}
block|}
block|}
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

unit|} }
end_unit

