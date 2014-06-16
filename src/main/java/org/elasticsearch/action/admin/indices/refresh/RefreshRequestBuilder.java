begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.refresh
package|package
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
name|refresh
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
name|support
operator|.
name|broadcast
operator|.
name|BroadcastOperationRequestBuilder
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
name|IndicesAdminClient
import|;
end_import

begin_comment
comment|/**  * A refresh request making all operations performed since the last refresh available for search. The (near) real-time  * capabilities depends on the index engine used. For example, the internal one requires refresh to be called, but by  * default a refresh is scheduled periodically.  */
end_comment

begin_class
DECL|class|RefreshRequestBuilder
specifier|public
class|class
name|RefreshRequestBuilder
extends|extends
name|BroadcastOperationRequestBuilder
argument_list|<
name|RefreshRequest
argument_list|,
name|RefreshResponse
argument_list|,
name|RefreshRequestBuilder
argument_list|,
name|IndicesAdminClient
argument_list|>
block|{
DECL|method|RefreshRequestBuilder
specifier|public
name|RefreshRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|)
block|{
name|super
argument_list|(
name|indicesClient
argument_list|,
operator|new
name|RefreshRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Forces calling refresh, overriding the check that dirty operations even happened. Defaults      * to true (note, still lightweight if no refresh is needed).      */
DECL|method|setForce
specifier|public
name|RefreshRequestBuilder
name|setForce
parameter_list|(
name|boolean
name|force
parameter_list|)
block|{
name|request
operator|.
name|force
argument_list|(
name|force
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|ActionListener
argument_list|<
name|RefreshResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|client
operator|.
name|refresh
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

