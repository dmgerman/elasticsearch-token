begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.action.delete
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
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
name|delete
operator|.
name|DeleteResponse
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
name|replication
operator|.
name|ReplicationType
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
name|support
operator|.
name|BaseRequestBuilder
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
name|InternalClient
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_comment
comment|/**  * A delete document action request builder.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|DeleteRequestBuilder
specifier|public
class|class
name|DeleteRequestBuilder
extends|extends
name|BaseRequestBuilder
argument_list|<
name|DeleteRequest
argument_list|,
name|DeleteResponse
argument_list|>
block|{
DECL|method|DeleteRequestBuilder
specifier|public
name|DeleteRequestBuilder
parameter_list|(
name|InternalClient
name|client
parameter_list|,
annotation|@
name|Nullable
name|String
name|index
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
operator|new
name|DeleteRequest
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the index the delete will happen on.      */
DECL|method|setIndex
specifier|public
name|DeleteRequestBuilder
name|setIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|request
operator|.
name|index
argument_list|(
name|index
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the type of the document to delete.      */
DECL|method|setType
specifier|public
name|DeleteRequestBuilder
name|setType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|request
operator|.
name|type
argument_list|(
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the id of the document to delete.      */
DECL|method|setId
specifier|public
name|DeleteRequestBuilder
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|request
operator|.
name|id
argument_list|(
name|id
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|setListenerThreaded
specifier|public
name|DeleteRequestBuilder
name|setListenerThreaded
parameter_list|(
name|boolean
name|threadedListener
parameter_list|)
block|{
name|request
operator|.
name|listenerThreaded
argument_list|(
name|threadedListener
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Controls if the operation will be executed on a separate thread when executed locally. Defaults      * to<tt>true</tt> when running in embedded mode.      */
DECL|method|setOperationThreaded
specifier|public
name|DeleteRequestBuilder
name|setOperationThreaded
parameter_list|(
name|boolean
name|threadedOperation
parameter_list|)
block|{
name|request
operator|.
name|operationThreaded
argument_list|(
name|threadedOperation
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the replication type for this operation.      */
DECL|method|setReplicationType
specifier|public
name|DeleteRequestBuilder
name|setReplicationType
parameter_list|(
name|ReplicationType
name|replicationType
parameter_list|)
block|{
name|request
operator|.
name|replicationType
argument_list|(
name|replicationType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|doExecute
annotation|@
name|Override
specifier|protected
name|void
name|doExecute
parameter_list|(
name|ActionListener
argument_list|<
name|DeleteResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|client
operator|.
name|delete
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

