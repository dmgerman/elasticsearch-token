begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.reroute
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
name|reroute
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|support
operator|.
name|master
operator|.
name|TransportMasterNodeOperationAction
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
name|AckedClusterStateUpdateTask
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
name|cluster
operator|.
name|ClusterState
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
name|node
operator|.
name|DiscoveryNode
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
name|routing
operator|.
name|allocation
operator|.
name|AllocationService
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
name|routing
operator|.
name|allocation
operator|.
name|RoutingAllocation
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
name|Nullable
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
name|Priority
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
name|unit
operator|.
name|TimeValue
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|TransportClusterRerouteAction
specifier|public
class|class
name|TransportClusterRerouteAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|ClusterRerouteRequest
argument_list|,
name|ClusterRerouteResponse
argument_list|>
block|{
DECL|field|allocationService
specifier|private
specifier|final
name|AllocationService
name|allocationService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportClusterRerouteAction
specifier|public
name|TransportClusterRerouteAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|AllocationService
name|allocationService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|this
operator|.
name|allocationService
operator|=
name|allocationService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
specifier|protected
name|String
name|executor
parameter_list|()
block|{
comment|// we go async right away
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
annotation|@
name|Override
DECL|method|transportAction
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|ClusterRerouteAction
operator|.
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|newRequest
specifier|protected
name|ClusterRerouteRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|ClusterRerouteRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|ClusterRerouteResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|ClusterRerouteResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|masterOperation
specifier|protected
name|void
name|masterOperation
parameter_list|(
specifier|final
name|ClusterRerouteRequest
name|request
parameter_list|,
specifier|final
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|ClusterRerouteResponse
argument_list|>
name|listener
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"cluster_reroute (api)"
argument_list|,
name|Priority
operator|.
name|URGENT
argument_list|,
operator|new
name|AckedClusterStateUpdateTask
argument_list|()
block|{
specifier|private
specifier|volatile
name|ClusterState
name|clusterStateToSend
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|mustAck
parameter_list|(
name|DiscoveryNode
name|discoveryNode
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onAllNodesAcked
parameter_list|(
annotation|@
name|Nullable
name|Throwable
name|t
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClusterRerouteResponse
argument_list|(
literal|true
argument_list|,
name|clusterStateToSend
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onAckTimeout
parameter_list|()
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClusterRerouteResponse
argument_list|(
literal|false
argument_list|,
name|clusterStateToSend
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TimeValue
name|ackTimeout
parameter_list|()
block|{
return|return
name|request
operator|.
name|timeout
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
name|request
operator|.
name|masterNodeTimeout
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to perform [{}]"
argument_list|,
name|t
argument_list|,
name|source
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
name|RoutingAllocation
operator|.
name|Result
name|routingResult
init|=
name|allocationService
operator|.
name|reroute
argument_list|(
name|currentState
argument_list|,
name|request
operator|.
name|commands
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ClusterState
name|newState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|routingResult
argument_list|(
name|routingResult
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|clusterStateToSend
operator|=
name|newState
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|dryRun
condition|)
block|{
return|return
name|currentState
return|;
block|}
return|return
name|newState
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|String
name|source
parameter_list|,
name|ClusterState
name|oldState
parameter_list|,
name|ClusterState
name|newState
parameter_list|)
block|{              }
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

