begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.gateway.snapshot
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
name|gateway
operator|.
name|snapshot
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
name|support
operator|.
name|replication
operator|.
name|IndicesReplicationOperationRequest
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
comment|/**  * Gateway snapshot allows to explicitly perform a snapshot through the gateway of one or more indices (backup them).  * By default, each index gateway periodically snapshot changes, though it can be disabled and be controlled completely  * through this API. Best created using {@link org.elasticsearch.client.Requests#gatewaySnapshotRequest(String...)}.  *  * @author kimchy (shay.banon)  * @see org.elasticsearch.client.Requests#gatewaySnapshotRequest(String...)  * @see org.elasticsearch.client.IndicesAdminClient#gatewaySnapshot(GatewaySnapshotRequest)  * @see GatewaySnapshotResponse  */
end_comment

begin_class
DECL|class|GatewaySnapshotRequest
specifier|public
class|class
name|GatewaySnapshotRequest
extends|extends
name|IndicesReplicationOperationRequest
block|{
DECL|method|GatewaySnapshotRequest
name|GatewaySnapshotRequest
parameter_list|()
block|{      }
comment|/**      * Constructs a new gateway snapshot against one or more indices. No indices means the gateway snapshot      * will be executed against all indices.      */
DECL|method|GatewaySnapshotRequest
specifier|public
name|GatewaySnapshotRequest
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
block|}
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|listenerThreaded
annotation|@
name|Override
specifier|public
name|GatewaySnapshotRequest
name|listenerThreaded
parameter_list|(
name|boolean
name|threadedListener
parameter_list|)
block|{
name|super
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
DECL|method|timeout
specifier|public
name|GatewaySnapshotRequest
name|timeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|timeout
specifier|public
name|GatewaySnapshotRequest
name|timeout
parameter_list|(
name|String
name|timeout
parameter_list|)
block|{
return|return
name|timeout
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|timeout
argument_list|,
literal|null
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

