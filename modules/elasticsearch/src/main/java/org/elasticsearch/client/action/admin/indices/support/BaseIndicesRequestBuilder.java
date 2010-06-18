begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.action.admin.indices.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|support
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
name|ActionRequest
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
name|ActionResponse
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
name|ListenableActionFuture
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
name|PlainListenableActionFuture
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
name|action
operator|.
name|RequestBuilder
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
name|internal
operator|.
name|InternalIndicesAdminClient
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|BaseIndicesRequestBuilder
specifier|public
specifier|abstract
class|class
name|BaseIndicesRequestBuilder
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
implements|implements
name|RequestBuilder
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|field|client
specifier|protected
specifier|final
name|InternalIndicesAdminClient
name|client
decl_stmt|;
DECL|field|request
specifier|protected
specifier|final
name|Request
name|request
decl_stmt|;
DECL|method|BaseIndicesRequestBuilder
specifier|protected
name|BaseIndicesRequestBuilder
parameter_list|(
name|InternalIndicesAdminClient
name|client
parameter_list|,
name|Request
name|request
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
block|}
DECL|method|execute
annotation|@
name|Override
specifier|public
name|ListenableActionFuture
argument_list|<
name|Response
argument_list|>
name|execute
parameter_list|()
block|{
name|PlainListenableActionFuture
argument_list|<
name|Response
argument_list|>
name|future
init|=
operator|new
name|PlainListenableActionFuture
argument_list|<
name|Response
argument_list|>
argument_list|(
name|request
operator|.
name|listenerThreaded
argument_list|()
argument_list|,
name|client
operator|.
name|threadPool
argument_list|()
argument_list|)
decl_stmt|;
name|execute
argument_list|(
name|future
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
DECL|method|execute
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|(
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|doExecute
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|doExecute
specifier|protected
specifier|abstract
name|void
name|doExecute
parameter_list|(
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
function_decl|;
block|}
end_class

end_unit

