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
name|cluster
operator|.
name|*
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|FutureUtils
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueSeconds
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
argument_list|<
name|RoutingService
argument_list|>
implements|implements
name|ClusterStateListener
block|{
DECL|field|CLUSTER_UPDATE_TASK_SOURCE
specifier|private
specifier|static
specifier|final
name|String
name|CLUSTER_UPDATE_TASK_SOURCE
init|=
literal|"routing-table-updater"
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
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
DECL|field|schedule
specifier|private
specifier|final
name|TimeValue
name|schedule
decl_stmt|;
DECL|field|routingTableDirty
specifier|private
specifier|volatile
name|boolean
name|routingTableDirty
init|=
literal|false
decl_stmt|;
DECL|field|scheduledRoutingTableFuture
specifier|private
specifier|volatile
name|Future
name|scheduledRoutingTableFuture
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
name|ThreadPool
name|threadPool
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
name|threadPool
operator|=
name|threadPool
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
name|this
operator|.
name|schedule
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"schedule"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|addFirst
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticsearchException
block|{     }
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticsearchException
block|{     }
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|scheduledRoutingTableFuture
argument_list|)
expr_stmt|;
name|scheduledRoutingTableFuture
operator|=
literal|null
expr_stmt|;
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
comment|/** make sure that a reroute will be done by the next scheduled check */
DECL|method|scheduleReroute
specifier|public
name|void
name|scheduleReroute
parameter_list|()
block|{
name|routingTableDirty
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clusterChanged
specifier|public
name|void
name|clusterChanged
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
if|if
condition|(
name|event
operator|.
name|source
argument_list|()
operator|.
name|equals
argument_list|(
name|CLUSTER_UPDATE_TASK_SOURCE
argument_list|)
condition|)
block|{
comment|// that's us, ignore this event
return|return;
block|}
if|if
condition|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeMaster
argument_list|()
condition|)
block|{
comment|// we are master, schedule the routing table updater
if|if
condition|(
name|scheduledRoutingTableFuture
operator|==
literal|null
condition|)
block|{
comment|// a new master (us), make sure we reroute shards
name|routingTableDirty
operator|=
literal|true
expr_stmt|;
name|scheduledRoutingTableFuture
operator|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
operator|new
name|RoutingTableUpdater
argument_list|()
argument_list|,
name|schedule
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|event
operator|.
name|nodesRemoved
argument_list|()
condition|)
block|{
comment|// if nodes were removed, we don't want to wait for the scheduled task
comment|// since we want to get primary election as fast as possible
name|routingTableDirty
operator|=
literal|true
expr_stmt|;
name|reroute
argument_list|()
expr_stmt|;
comment|// Commented out since we make sure to reroute whenever shards changes state or metadata changes state
comment|//            } else if (event.routingTableChanged()) {
comment|//                routingTableDirty = true;
comment|//                reroute();
block|}
else|else
block|{
if|if
condition|(
name|event
operator|.
name|nodesAdded
argument_list|()
condition|)
block|{
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|event
operator|.
name|nodesDelta
argument_list|()
operator|.
name|addedNodes
argument_list|()
control|)
block|{
if|if
condition|(
name|node
operator|.
name|dataNode
argument_list|()
condition|)
block|{
name|routingTableDirty
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
block|}
else|else
block|{
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|scheduledRoutingTableFuture
argument_list|)
expr_stmt|;
name|scheduledRoutingTableFuture
operator|=
literal|null
expr_stmt|;
block|}
block|}
DECL|method|reroute
specifier|private
name|void
name|reroute
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
operator|!
name|routingTableDirty
condition|)
block|{
return|return;
block|}
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
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
name|CLUSTER_UPDATE_TASK_SOURCE
argument_list|,
name|Priority
operator|.
name|HIGH
argument_list|,
operator|new
name|ClusterStateUpdateTask
argument_list|()
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
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|routingResult
operator|.
name|changed
argument_list|()
condition|)
block|{
comment|// no state changed
return|return
name|currentState
return|;
block|}
return|return
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
name|Throwable
name|t
parameter_list|)
block|{
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
literal|"unexpected failure during [{}], current state:\n{}"
argument_list|,
name|t
argument_list|,
name|source
argument_list|,
name|state
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"unexpected failure during [{}], current state version [{}]"
argument_list|,
name|t
argument_list|,
name|source
argument_list|,
name|state
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|routingTableDirty
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
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
literal|"Failed to reroute routing table, current state:\n{}"
argument_list|,
name|e
argument_list|,
name|state
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|RoutingTableUpdater
specifier|private
class|class
name|RoutingTableUpdater
implements|implements
name|Runnable
block|{
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|reroute
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

