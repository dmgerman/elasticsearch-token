begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.node.info
package|package
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
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|TransportActions
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
name|nodes
operator|.
name|NodeOperationRequest
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
name|nodes
operator|.
name|TransportNodesOperationAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterService
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
name|node
operator|.
name|service
operator|.
name|NodeService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReferenceArray
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportNodesInfoAction
specifier|public
class|class
name|TransportNodesInfoAction
extends|extends
name|TransportNodesOperationAction
argument_list|<
name|NodesInfoRequest
argument_list|,
name|NodesInfoResponse
argument_list|,
name|TransportNodesInfoAction
operator|.
name|NodeInfoRequest
argument_list|,
name|NodeInfo
argument_list|>
block|{
DECL|field|nodeService
specifier|private
specifier|final
name|NodeService
name|nodeService
decl_stmt|;
DECL|method|TransportNodesInfoAction
annotation|@
name|Inject
specifier|public
name|TransportNodesInfoAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|NodeService
name|nodeService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|clusterName
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodeService
operator|=
name|nodeService
expr_stmt|;
block|}
DECL|method|executor
annotation|@
name|Override
specifier|protected
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|MANAGEMENT
return|;
block|}
DECL|method|transportAction
annotation|@
name|Override
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|TransportActions
operator|.
name|Admin
operator|.
name|Cluster
operator|.
name|Node
operator|.
name|INFO
return|;
block|}
DECL|method|transportNodeAction
annotation|@
name|Override
specifier|protected
name|String
name|transportNodeAction
parameter_list|()
block|{
return|return
literal|"/cluster/nodes/info/node"
return|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|NodesInfoResponse
name|newResponse
parameter_list|(
name|NodesInfoRequest
name|nodesInfoRequest
parameter_list|,
name|AtomicReferenceArray
name|responses
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|NodeInfo
argument_list|>
name|nodesInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|NodeInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|responses
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|resp
init|=
name|responses
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|resp
operator|instanceof
name|NodeInfo
condition|)
block|{
name|nodesInfos
operator|.
name|add
argument_list|(
operator|(
name|NodeInfo
operator|)
name|resp
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|NodesInfoResponse
argument_list|(
name|clusterName
argument_list|,
name|nodesInfos
operator|.
name|toArray
argument_list|(
operator|new
name|NodeInfo
index|[
name|nodesInfos
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|NodesInfoRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|NodesInfoRequest
argument_list|()
return|;
block|}
DECL|method|newNodeRequest
annotation|@
name|Override
specifier|protected
name|NodeInfoRequest
name|newNodeRequest
parameter_list|()
block|{
return|return
operator|new
name|NodeInfoRequest
argument_list|()
return|;
block|}
DECL|method|newNodeRequest
annotation|@
name|Override
specifier|protected
name|NodeInfoRequest
name|newNodeRequest
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|NodesInfoRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|NodeInfoRequest
argument_list|(
name|nodeId
argument_list|)
return|;
block|}
DECL|method|newNodeResponse
annotation|@
name|Override
specifier|protected
name|NodeInfo
name|newNodeResponse
parameter_list|()
block|{
return|return
operator|new
name|NodeInfo
argument_list|()
return|;
block|}
DECL|method|nodeOperation
annotation|@
name|Override
specifier|protected
name|NodeInfo
name|nodeOperation
parameter_list|(
name|NodeInfoRequest
name|nodeInfoRequest
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
return|return
name|nodeService
operator|.
name|info
argument_list|()
return|;
block|}
DECL|method|accumulateExceptions
annotation|@
name|Override
specifier|protected
name|boolean
name|accumulateExceptions
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|class|NodeInfoRequest
specifier|protected
specifier|static
class|class
name|NodeInfoRequest
extends|extends
name|NodeOperationRequest
block|{
DECL|method|NodeInfoRequest
specifier|private
name|NodeInfoRequest
parameter_list|()
block|{         }
DECL|method|NodeInfoRequest
specifier|private
name|NodeInfoRequest
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
name|super
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

