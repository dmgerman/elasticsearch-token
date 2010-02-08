begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
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
name|ActionFuture
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
name|node
operator|.
name|info
operator|.
name|NodesInfoRequest
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
name|node
operator|.
name|info
operator|.
name|NodesInfoResponse
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
name|broadcast
operator|.
name|BroadcastPingRequest
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
name|broadcast
operator|.
name|BroadcastPingResponse
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
name|admin
operator|.
name|cluster
operator|.
name|ping
operator|.
name|single
operator|.
name|SinglePingRequest
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
name|single
operator|.
name|SinglePingResponse
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
name|state
operator|.
name|ClusterStateRequest
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
name|state
operator|.
name|ClusterStateResponse
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_interface
DECL|interface|ClusterAdminClient
specifier|public
interface|interface
name|ClusterAdminClient
block|{
DECL|method|state
name|ActionFuture
argument_list|<
name|ClusterStateResponse
argument_list|>
name|state
parameter_list|(
name|ClusterStateRequest
name|request
parameter_list|)
function_decl|;
DECL|method|state
name|ActionFuture
argument_list|<
name|ClusterStateResponse
argument_list|>
name|state
parameter_list|(
name|ClusterStateRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|ClusterStateResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|execState
name|void
name|execState
parameter_list|(
name|ClusterStateRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|ClusterStateResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|ping
name|ActionFuture
argument_list|<
name|SinglePingResponse
argument_list|>
name|ping
parameter_list|(
name|SinglePingRequest
name|request
parameter_list|)
function_decl|;
DECL|method|ping
name|ActionFuture
argument_list|<
name|SinglePingResponse
argument_list|>
name|ping
parameter_list|(
name|SinglePingRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|SinglePingResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|execPing
name|void
name|execPing
parameter_list|(
name|SinglePingRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|SinglePingResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|ping
name|ActionFuture
argument_list|<
name|BroadcastPingResponse
argument_list|>
name|ping
parameter_list|(
name|BroadcastPingRequest
name|request
parameter_list|)
function_decl|;
DECL|method|ping
name|ActionFuture
argument_list|<
name|BroadcastPingResponse
argument_list|>
name|ping
parameter_list|(
name|BroadcastPingRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|BroadcastPingResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|execPing
name|void
name|execPing
parameter_list|(
name|BroadcastPingRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|BroadcastPingResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|ping
name|ActionFuture
argument_list|<
name|ReplicationPingResponse
argument_list|>
name|ping
parameter_list|(
name|ReplicationPingRequest
name|request
parameter_list|)
function_decl|;
DECL|method|ping
name|ActionFuture
argument_list|<
name|ReplicationPingResponse
argument_list|>
name|ping
parameter_list|(
name|ReplicationPingRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|ReplicationPingResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|execPing
name|void
name|execPing
parameter_list|(
name|ReplicationPingRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|ReplicationPingResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|nodesInfo
name|ActionFuture
argument_list|<
name|NodesInfoResponse
argument_list|>
name|nodesInfo
parameter_list|(
name|NodesInfoRequest
name|request
parameter_list|)
function_decl|;
DECL|method|nodesInfo
name|ActionFuture
argument_list|<
name|NodesInfoResponse
argument_list|>
name|nodesInfo
parameter_list|(
name|NodesInfoRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|NodesInfoResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|method|execNodesInfo
name|void
name|execNodesInfo
parameter_list|(
name|NodesInfoRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|NodesInfoResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

