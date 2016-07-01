begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|recovery
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
name|indices
operator|.
name|recovery
operator|.
name|RecoveryRequest
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
name|indices
operator|.
name|recovery
operator|.
name|RecoveryResponse
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
name|IndicesOptions
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

begin_comment
comment|/**  * REST handler to report on index recoveries.  */
end_comment

begin_class
DECL|class|RestRecoveryAction
specifier|public
class|class
name|RestRecoveryAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestRecoveryAction
specifier|public
name|RestRecoveryAction
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
literal|"/_recovery"
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
literal|"/{index}/_recovery"
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
specifier|final
name|RecoveryRequest
name|recoveryRequest
init|=
operator|new
name|RecoveryRequest
argument_list|(
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|recoveryRequest
operator|.
name|detailed
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"detailed"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|recoveryRequest
operator|.
name|activeOnly
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"active_only"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|recoveryRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|fromRequest
argument_list|(
name|request
argument_list|,
name|recoveryRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|recoveries
argument_list|(
name|recoveryRequest
argument_list|,
operator|new
name|RestBuilderListener
argument_list|<
name|RecoveryResponse
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
name|RecoveryResponse
name|response
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|Exception
block|{
name|response
operator|.
name|detailed
argument_list|(
name|recoveryRequest
operator|.
name|detailed
argument_list|()
argument_list|)
expr_stmt|;
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
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

