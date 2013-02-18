begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.open
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
name|open
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
name|master
operator|.
name|MasterNodeOperationRequestBuilder
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|OpenIndexRequestBuilder
specifier|public
class|class
name|OpenIndexRequestBuilder
extends|extends
name|MasterNodeOperationRequestBuilder
argument_list|<
name|OpenIndexRequest
argument_list|,
name|OpenIndexResponse
argument_list|,
name|OpenIndexRequestBuilder
argument_list|>
block|{
DECL|method|OpenIndexRequestBuilder
specifier|public
name|OpenIndexRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalIndicesAdminClient
operator|)
name|indicesClient
argument_list|,
operator|new
name|OpenIndexRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|OpenIndexRequestBuilder
specifier|public
name|OpenIndexRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|,
name|String
name|index
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalIndicesAdminClient
operator|)
name|indicesClient
argument_list|,
operator|new
name|OpenIndexRequest
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|setIndex
specifier|public
name|OpenIndexRequestBuilder
name|setIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|request
operator|.
name|setIndex
argument_list|(
name|index
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Timeout to wait for the operation to be acknowledged by current cluster nodes. Defaults      * to<tt>10s</tt>.      */
DECL|method|setTimeout
specifier|public
name|OpenIndexRequestBuilder
name|setTimeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|setTimeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Timeout to wait for the index deletion to be acknowledged by current cluster nodes. Defaults      * to<tt>10s</tt>.      */
DECL|method|setTimeout
specifier|public
name|OpenIndexRequestBuilder
name|setTimeout
parameter_list|(
name|String
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|setTimeout
argument_list|(
name|timeout
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
name|OpenIndexResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|(
operator|(
name|IndicesAdminClient
operator|)
name|client
operator|)
operator|.
name|open
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

