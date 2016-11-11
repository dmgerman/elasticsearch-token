begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|ClusterChangedEvent
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
name|ClusterStateUpdateTask
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
name|service
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
name|component
operator|.
name|AbstractLifecycleComponent
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * A {@link RoutingService} listens to clusters state. When this service  * receives a {@link ClusterChangedEvent} the cluster state will be verified and  * the routing tables might be updated.  *<p>  * Note: The {@link RoutingService} is responsible for cluster wide operations  * that include modifications to the cluster state. Such an operation can only  * be performed on the clusters master node. Unless the local node this service  * is running on is the clusters master node this service will not perform any  * actions.  *</p>  */
end_comment

begin_class
DECL|class|RoutingService
specifier|public
class|class
name|RoutingService
extends|extends
name|AbstractLifecycleComponent
block|{
DECL|field|CLUSTER_UPDATE_TASK_SOURCE
specifier|private
specifier|static
specifier|final
name|String
name|CLUSTER_UPDATE_TASK_SOURCE
init|=
literal|"cluster_reroute"
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|allocationService
specifier|private
specifier|final
name|AllocationService
name|allocationService
decl_stmt|;
DECL|field|rerouting
specifier|private
name|AtomicBoolean
name|rerouting
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|RoutingService
specifier|public
name|RoutingService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|AllocationService
name|allocationService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
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
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{     }
comment|/**      * Initiates a reroute.      */
DECL|method|reroute
specifier|public
specifier|final
name|void
name|reroute
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|performReroute
argument_list|(
name|reason
argument_list|)
expr_stmt|;
block|}
comment|// visible for testing
DECL|method|performReroute
specifier|protected
name|void
name|performReroute
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|lifecycle
operator|.
name|stopped
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|rerouting
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"already has pending reroute, ignoring {}"
argument_list|,
name|reason
argument_list|)
expr_stmt|;
return|return;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"rerouting {}"
argument_list|,
name|reason
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
name|CLUSTER_UPDATE_TASK_SOURCE
operator|+
literal|"("
operator|+
name|reason
operator|+
literal|")"
argument_list|,
operator|new
name|ClusterStateUpdateTask
argument_list|(
name|Priority
operator|.
name|HIGH
argument_list|)
block|{
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
name|rerouting
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|allocationService
operator|.
name|reroute
argument_list|(
name|currentState
argument_list|,
name|reason
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onNoLongerMaster
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|rerouting
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// no biggie
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
name|Exception
name|e
parameter_list|)
block|{
name|rerouting
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ClusterState
name|state
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|error
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"unexpected failure during [{}], current state:\n{}"
argument_list|,
name|source
argument_list|,
name|state
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|error
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"unexpected failure during [{}], current state version [{}]"
argument_list|,
name|source
argument_list|,
name|state
operator|.
name|version
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|rerouting
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ClusterState
name|state
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|logger
operator|.
name|warn
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"failed to reroute routing table, current state:\n{}"
argument_list|,
name|state
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

