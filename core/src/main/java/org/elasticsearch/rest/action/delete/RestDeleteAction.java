begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.delete
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|delete
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
name|delete
operator|.
name|DeleteRequest
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
name|ActiveShardCount
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
name|action
operator|.
name|support
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
name|support
operator|.
name|RestStatusToXContentListener
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
name|DELETE
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestDeleteAction
specifier|public
class|class
name|RestDeleteAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestDeleteAction
specifier|public
name|RestDeleteAction
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
name|DELETE
argument_list|,
literal|"/{index}/{type}/{id}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
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
specifier|final
name|NodeClient
name|client
parameter_list|)
block|{
name|DeleteRequest
name|deleteRequest
init|=
operator|new
name|DeleteRequest
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
name|deleteRequest
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
name|deleteRequest
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
comment|// order is important, set it after routing, so it will set the routing
name|deleteRequest
operator|.
name|timeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"timeout"
argument_list|,
name|DeleteRequest
operator|.
name|DEFAULT_TIMEOUT
argument_list|)
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|setRefreshPolicy
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"refresh"
argument_list|)
argument_list|)
expr_stmt|;
name|deleteRequest
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
name|deleteRequest
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
name|deleteRequest
operator|.
name|versionType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|waitForActiveShards
init|=
name|request
operator|.
name|param
argument_list|(
literal|"wait_for_active_shards"
argument_list|)
decl_stmt|;
if|if
condition|(
name|waitForActiveShards
operator|!=
literal|null
condition|)
block|{
name|deleteRequest
operator|.
name|waitForActiveShards
argument_list|(
name|ActiveShardCount
operator|.
name|parseString
argument_list|(
name|waitForActiveShards
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|delete
argument_list|(
name|deleteRequest
argument_list|,
operator|new
name|RestStatusToXContentListener
argument_list|<>
argument_list|(
name|channel
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

