begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.action.admin.cluster.ping.replication
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
name|cluster
operator|.
name|ping
operator|.
name|replication
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
name|admin
operator|.
name|cluster
operator|.
name|ping
operator|.
name|replication
operator|.
name|ReplicationPingRequest
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
name|ping
operator|.
name|replication
operator|.
name|ReplicationPingResponse
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
name|admin
operator|.
name|cluster
operator|.
name|support
operator|.
name|BaseClusterRequestBuilder
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
name|InternalClusterAdminClient
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ReplicationPingRequestBuilder
specifier|public
class|class
name|ReplicationPingRequestBuilder
extends|extends
name|BaseClusterRequestBuilder
argument_list|<
name|ReplicationPingRequest
argument_list|,
name|ReplicationPingResponse
argument_list|>
block|{
DECL|method|ReplicationPingRequestBuilder
specifier|public
name|ReplicationPingRequestBuilder
parameter_list|(
name|InternalClusterAdminClient
name|clusterClient
parameter_list|)
block|{
name|super
argument_list|(
name|clusterClient
argument_list|,
operator|new
name|ReplicationPingRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|setIndices
specifier|public
name|ReplicationPingRequestBuilder
name|setIndices
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|request
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setListenerThreaded
specifier|public
name|ReplicationPingRequestBuilder
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
DECL|method|setReplicationType
specifier|public
name|ReplicationPingRequestBuilder
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
DECL|method|setTimeout
specifier|public
name|ReplicationPingRequestBuilder
name|setTimeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|timeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setTimeout
specifier|public
name|ReplicationPingRequestBuilder
name|setTimeout
parameter_list|(
name|String
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|timeout
argument_list|(
name|timeout
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
name|ReplicationPingResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|client
operator|.
name|ping
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

