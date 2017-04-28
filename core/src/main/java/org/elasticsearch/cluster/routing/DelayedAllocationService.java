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
name|ClusterStateListener
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
name|AbstractRunnable
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
name|ScheduledFuture
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
name|AtomicReference
import|;
end_import

begin_comment
comment|/**  * The {@link DelayedAllocationService} listens to cluster state changes and checks  * if there are unassigned shards with delayed allocation (unassigned shards that have  * the delay marker). These are shards that have become unassigned due to a node leaving  * and which were assigned the delay marker based on the index delay setting  * {@link UnassignedInfo#INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING}  * (see {@link AllocationService#deassociateDeadNodes(RoutingAllocation)}).  * This class is responsible for choosing the next (closest) delay expiration of a  * delayed shard to schedule a reroute to remove the delay marker.  * The actual removal of the delay marker happens in  * {@link AllocationService#removeDelayMarkers(RoutingAllocation)}, triggering yet  * another cluster change event.  */
end_comment

begin_class
DECL|class|DelayedAllocationService
specifier|public
class|class
name|DelayedAllocationService
extends|extends
name|AbstractLifecycleComponent
implements|implements
name|ClusterStateListener
block|{
DECL|field|CLUSTER_UPDATE_TASK_SOURCE
specifier|static
specifier|final
name|String
name|CLUSTER_UPDATE_TASK_SOURCE
init|=
literal|"delayed_allocation_reroute"
decl_stmt|;
DECL|field|threadPool
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
DECL|field|delayedRerouteTask
name|AtomicReference
argument_list|<
name|DelayedRerouteTask
argument_list|>
name|delayedRerouteTask
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
comment|// package private to access from tests
comment|/**      * represents a delayed scheduling of the reroute action that can be cancelled.      */
DECL|class|DelayedRerouteTask
class|class
name|DelayedRerouteTask
extends|extends
name|ClusterStateUpdateTask
block|{
DECL|field|nextDelay
specifier|final
name|TimeValue
name|nextDelay
decl_stmt|;
comment|// delay until submitting the reroute command
DECL|field|baseTimestampNanos
specifier|final
name|long
name|baseTimestampNanos
decl_stmt|;
comment|// timestamp (in nanos) upon which delay was calculated
DECL|field|future
specifier|volatile
name|ScheduledFuture
name|future
decl_stmt|;
DECL|field|cancelScheduling
specifier|final
name|AtomicBoolean
name|cancelScheduling
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|method|DelayedRerouteTask
name|DelayedRerouteTask
parameter_list|(
name|TimeValue
name|nextDelay
parameter_list|,
name|long
name|baseTimestampNanos
parameter_list|)
block|{
name|this
operator|.
name|nextDelay
operator|=
name|nextDelay
expr_stmt|;
name|this
operator|.
name|baseTimestampNanos
operator|=
name|baseTimestampNanos
expr_stmt|;
block|}
DECL|method|scheduledTimeToRunInNanos
specifier|public
name|long
name|scheduledTimeToRunInNanos
parameter_list|()
block|{
return|return
name|baseTimestampNanos
operator|+
name|nextDelay
operator|.
name|nanos
argument_list|()
return|;
block|}
DECL|method|cancelScheduling
specifier|public
name|void
name|cancelScheduling
parameter_list|()
block|{
name|cancelScheduling
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|future
argument_list|)
expr_stmt|;
name|removeIfSameTask
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|schedule
specifier|public
name|void
name|schedule
parameter_list|()
block|{
name|future
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|nextDelay
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|AbstractRunnable
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|void
name|doRun
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|cancelScheduling
operator|.
name|get
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
name|DelayedRerouteTask
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to submit schedule/execute reroute post unassigned shard"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|removeIfSameTask
argument_list|(
name|DelayedRerouteTask
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
throws|throws
name|Exception
block|{
name|removeIfSameTask
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
name|allocationService
operator|.
name|reroute
argument_list|(
name|currentState
argument_list|,
literal|"assign delayed unassigned shards"
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|clusterStateProcessed
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
block|{
if|if
condition|(
name|oldState
operator|==
name|newState
condition|)
block|{
comment|// no state changed, check when we should remove the delay flag from the shards the next time.
comment|// if cluster state changed, we can leave the scheduling of the next delay up to the clusterChangedEvent
comment|// this should not be needed, but we want to be extra safe here
name|scheduleIfNeeded
argument_list|(
name|currentNanoTime
argument_list|()
argument_list|,
name|newState
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|onFailure
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
name|removeIfSameTask
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to schedule/execute reroute post unassigned shard"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Inject
DECL|method|DelayedAllocationService
specifier|public
name|DelayedAllocationService
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
name|clusterService
operator|.
name|addListener
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
block|{
name|clusterService
operator|.
name|removeListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|removeTaskAndCancel
argument_list|()
expr_stmt|;
block|}
comment|/** override this to control time based decisions during delayed allocation */
DECL|method|currentNanoTime
specifier|protected
name|long
name|currentNanoTime
parameter_list|()
block|{
return|return
name|System
operator|.
name|nanoTime
argument_list|()
return|;
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
name|long
name|currentNanoTime
init|=
name|currentNanoTime
argument_list|()
decl_stmt|;
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
name|isLocalNodeElectedMaster
argument_list|()
condition|)
block|{
name|scheduleIfNeeded
argument_list|(
name|currentNanoTime
argument_list|,
name|event
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|removeTaskAndCancel
specifier|private
name|void
name|removeTaskAndCancel
parameter_list|()
block|{
name|DelayedRerouteTask
name|existingTask
init|=
name|delayedRerouteTask
operator|.
name|getAndSet
argument_list|(
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingTask
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"cancelling existing delayed reroute task"
argument_list|)
expr_stmt|;
name|existingTask
operator|.
name|cancelScheduling
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|removeIfSameTask
specifier|private
name|void
name|removeIfSameTask
parameter_list|(
name|DelayedRerouteTask
name|expectedTask
parameter_list|)
block|{
name|delayedRerouteTask
operator|.
name|compareAndSet
argument_list|(
name|expectedTask
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * Figure out if an existing scheduled reroute is good enough or whether we need to cancel and reschedule.      */
DECL|method|scheduleIfNeeded
specifier|private
specifier|synchronized
name|void
name|scheduleIfNeeded
parameter_list|(
name|long
name|currentNanoTime
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
name|assertClusterOrMasterStateThread
argument_list|()
expr_stmt|;
name|long
name|nextDelayNanos
init|=
name|UnassignedInfo
operator|.
name|findNextDelayedAllocation
argument_list|(
name|currentNanoTime
argument_list|,
name|state
argument_list|)
decl_stmt|;
if|if
condition|(
name|nextDelayNanos
operator|<
literal|0
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"no need to schedule reroute - no delayed unassigned shards"
argument_list|)
expr_stmt|;
name|removeTaskAndCancel
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|TimeValue
name|nextDelay
init|=
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|nextDelayNanos
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|earlierRerouteNeeded
decl_stmt|;
name|DelayedRerouteTask
name|existingTask
init|=
name|delayedRerouteTask
operator|.
name|get
argument_list|()
decl_stmt|;
name|DelayedRerouteTask
name|newTask
init|=
operator|new
name|DelayedRerouteTask
argument_list|(
name|nextDelay
argument_list|,
name|currentNanoTime
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingTask
operator|==
literal|null
condition|)
block|{
name|earlierRerouteNeeded
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|newTask
operator|.
name|scheduledTimeToRunInNanos
argument_list|()
operator|<
name|existingTask
operator|.
name|scheduledTimeToRunInNanos
argument_list|()
condition|)
block|{
comment|// we need an earlier delayed reroute
name|logger
operator|.
name|trace
argument_list|(
literal|"cancelling existing delayed reroute task as delayed reroute has to happen [{}] earlier"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|existingTask
operator|.
name|scheduledTimeToRunInNanos
argument_list|()
operator|-
name|newTask
operator|.
name|scheduledTimeToRunInNanos
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|existingTask
operator|.
name|cancelScheduling
argument_list|()
expr_stmt|;
name|earlierRerouteNeeded
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|earlierRerouteNeeded
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|earlierRerouteNeeded
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"scheduling reroute for delayed shards in [{}] ({} delayed shards)"
argument_list|,
name|nextDelay
argument_list|,
name|UnassignedInfo
operator|.
name|getNumberOfDelayedUnassigned
argument_list|(
name|state
argument_list|)
argument_list|)
expr_stmt|;
name|DelayedRerouteTask
name|currentTask
init|=
name|delayedRerouteTask
operator|.
name|getAndSet
argument_list|(
name|newTask
argument_list|)
decl_stmt|;
assert|assert
name|existingTask
operator|==
name|currentTask
operator|||
name|currentTask
operator|==
literal|null
assert|;
name|newTask
operator|.
name|schedule
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"no need to reschedule delayed reroute - currently scheduled delayed reroute in [{}] is enough"
argument_list|,
name|nextDelay
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// protected so that it can be overridden (and disabled) by unit tests
DECL|method|assertClusterOrMasterStateThread
specifier|protected
name|void
name|assertClusterOrMasterStateThread
parameter_list|()
block|{
assert|assert
name|ClusterService
operator|.
name|assertClusterOrMasterStateThread
argument_list|()
assert|;
block|}
block|}
end_class

end_unit

