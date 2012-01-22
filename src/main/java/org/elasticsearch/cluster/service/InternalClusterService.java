begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.service
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|service
package|;
end_package

begin_import
import|import
name|jsr166y
operator|.
name|LinkedTransferQueue
import|;
end_import

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
name|ElasticSearchIllegalStateException
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
name|block
operator|.
name|ClusterBlock
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
name|block
operator|.
name|ClusterBlocks
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
name|metadata
operator|.
name|MetaData
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
name|node
operator|.
name|DiscoveryNodes
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
name|RoutingTable
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
name|operation
operator|.
name|OperationRouting
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
name|discovery
operator|.
name|Discovery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|DiscoveryService
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
name|settings
operator|.
name|NodeSettingsService
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
name|Iterator
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
name|Queue
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
name|CopyOnWriteArrayList
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
name|ExecutorService
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
name|TimeUnit
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
operator|.
name|newSingleThreadExecutor
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
operator|.
name|Builder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
operator|.
name|newClusterStateBuilder
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
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
operator|.
name|daemonThreadFactory
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|InternalClusterService
specifier|public
class|class
name|InternalClusterService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|ClusterService
argument_list|>
implements|implements
name|ClusterService
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|discoveryService
specifier|private
specifier|final
name|DiscoveryService
name|discoveryService
decl_stmt|;
DECL|field|operationRouting
specifier|private
specifier|final
name|OperationRouting
name|operationRouting
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|nodeSettingsService
specifier|private
specifier|final
name|NodeSettingsService
name|nodeSettingsService
decl_stmt|;
DECL|field|reconnectInterval
specifier|private
specifier|final
name|TimeValue
name|reconnectInterval
decl_stmt|;
DECL|field|updateTasksExecutor
specifier|private
specifier|volatile
name|ExecutorService
name|updateTasksExecutor
decl_stmt|;
DECL|field|priorityClusterStateListeners
specifier|private
specifier|final
name|List
argument_list|<
name|ClusterStateListener
argument_list|>
name|priorityClusterStateListeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|ClusterStateListener
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|clusterStateListeners
specifier|private
specifier|final
name|List
argument_list|<
name|ClusterStateListener
argument_list|>
name|clusterStateListeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|ClusterStateListener
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|lastClusterStateListeners
specifier|private
specifier|final
name|List
argument_list|<
name|ClusterStateListener
argument_list|>
name|lastClusterStateListeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|ClusterStateListener
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|onGoingTimeouts
specifier|private
specifier|final
name|Queue
argument_list|<
name|NotifyTimeout
argument_list|>
name|onGoingTimeouts
init|=
operator|new
name|LinkedTransferQueue
argument_list|<
name|NotifyTimeout
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|clusterState
specifier|private
specifier|volatile
name|ClusterState
name|clusterState
init|=
name|newClusterStateBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|field|initialBlocks
specifier|private
specifier|final
name|ClusterBlocks
operator|.
name|Builder
name|initialBlocks
init|=
name|ClusterBlocks
operator|.
name|builder
argument_list|()
operator|.
name|addGlobalBlock
argument_list|(
name|Discovery
operator|.
name|NO_MASTER_BLOCK
argument_list|)
decl_stmt|;
DECL|field|reconnectToNodes
specifier|private
specifier|volatile
name|ScheduledFuture
name|reconnectToNodes
decl_stmt|;
annotation|@
name|Inject
DECL|method|InternalClusterService
specifier|public
name|InternalClusterService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|DiscoveryService
name|discoveryService
parameter_list|,
name|OperationRouting
name|operationRouting
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|operationRouting
operator|=
name|operationRouting
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|discoveryService
operator|=
name|discoveryService
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|nodeSettingsService
operator|=
name|nodeSettingsService
expr_stmt|;
name|this
operator|.
name|nodeSettingsService
operator|.
name|setClusterService
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|reconnectInterval
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"reconnect_interval"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|settingsService
specifier|public
name|NodeSettingsService
name|settingsService
parameter_list|()
block|{
return|return
name|this
operator|.
name|nodeSettingsService
return|;
block|}
DECL|method|addInitialStateBlock
specifier|public
name|void
name|addInitialStateBlock
parameter_list|(
name|ClusterBlock
name|block
parameter_list|)
throws|throws
name|ElasticSearchIllegalStateException
block|{
if|if
condition|(
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"can't set initial block when started"
argument_list|)
throw|;
block|}
name|initialBlocks
operator|.
name|addGlobalBlock
argument_list|(
name|block
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
name|ElasticSearchException
block|{
name|this
operator|.
name|clusterState
operator|=
name|newClusterStateBuilder
argument_list|()
operator|.
name|blocks
argument_list|(
name|initialBlocks
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|this
operator|.
name|updateTasksExecutor
operator|=
name|newSingleThreadExecutor
argument_list|(
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"clusterService#updateTask"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|reconnectToNodes
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|reconnectInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|CACHED
argument_list|,
operator|new
name|ReconnectToNodes
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|this
operator|.
name|reconnectToNodes
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|NotifyTimeout
name|onGoingTimeout
range|:
name|onGoingTimeouts
control|)
block|{
name|onGoingTimeout
operator|.
name|cancel
argument_list|()
expr_stmt|;
name|onGoingTimeout
operator|.
name|listener
operator|.
name|onClose
argument_list|()
expr_stmt|;
block|}
name|updateTasksExecutor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
name|updateTasksExecutor
operator|.
name|awaitTermination
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
annotation|@
name|Override
DECL|method|localNode
specifier|public
name|DiscoveryNode
name|localNode
parameter_list|()
block|{
return|return
name|discoveryService
operator|.
name|localNode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|operationRouting
specifier|public
name|OperationRouting
name|operationRouting
parameter_list|()
block|{
return|return
name|operationRouting
return|;
block|}
DECL|method|state
specifier|public
name|ClusterState
name|state
parameter_list|()
block|{
return|return
name|this
operator|.
name|clusterState
return|;
block|}
DECL|method|addPriority
specifier|public
name|void
name|addPriority
parameter_list|(
name|ClusterStateListener
name|listener
parameter_list|)
block|{
name|priorityClusterStateListeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|addLast
specifier|public
name|void
name|addLast
parameter_list|(
name|ClusterStateListener
name|listener
parameter_list|)
block|{
name|lastClusterStateListeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|ClusterStateListener
name|listener
parameter_list|)
block|{
name|clusterStateListeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|remove
specifier|public
name|void
name|remove
parameter_list|(
name|ClusterStateListener
name|listener
parameter_list|)
block|{
name|clusterStateListeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
expr_stmt|;
name|priorityClusterStateListeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
expr_stmt|;
name|lastClusterStateListeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|NotifyTimeout
argument_list|>
name|it
init|=
name|onGoingTimeouts
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|NotifyTimeout
name|timeout
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|timeout
operator|.
name|listener
operator|.
name|equals
argument_list|(
name|listener
argument_list|)
condition|)
block|{
name|timeout
operator|.
name|cancel
argument_list|()
expr_stmt|;
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|TimeValue
name|timeout
parameter_list|,
specifier|final
name|TimeoutClusterStateListener
name|listener
parameter_list|)
block|{
if|if
condition|(
name|lifecycle
operator|.
name|stoppedOrClosed
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onClose
argument_list|()
expr_stmt|;
return|return;
block|}
name|NotifyTimeout
name|notifyTimeout
init|=
operator|new
name|NotifyTimeout
argument_list|(
name|listener
argument_list|,
name|timeout
argument_list|)
decl_stmt|;
name|notifyTimeout
operator|.
name|future
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|timeout
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|CACHED
argument_list|,
name|notifyTimeout
argument_list|)
expr_stmt|;
name|onGoingTimeouts
operator|.
name|add
argument_list|(
name|notifyTimeout
argument_list|)
expr_stmt|;
name|clusterStateListeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
comment|// call the post added notification on the same event thread
name|updateTasksExecutor
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|listener
operator|.
name|postAdded
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|submitStateUpdateTask
specifier|public
name|void
name|submitStateUpdateTask
parameter_list|(
specifier|final
name|String
name|source
parameter_list|,
specifier|final
name|ClusterStateUpdateTask
name|updateTask
parameter_list|)
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
return|return;
block|}
name|updateTasksExecutor
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"processing [{}]: ignoring, cluster_service not started"
argument_list|,
name|source
argument_list|)
expr_stmt|;
return|return;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"processing [{}]: execute"
argument_list|,
name|source
argument_list|)
expr_stmt|;
name|ClusterState
name|previousClusterState
init|=
name|clusterState
decl_stmt|;
name|ClusterState
name|newClusterState
decl_stmt|;
try|try
block|{
name|newClusterState
operator|=
name|updateTask
operator|.
name|execute
argument_list|(
name|previousClusterState
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"failed to execute cluster state update, state:\nversion ["
argument_list|)
operator|.
name|append
argument_list|(
name|previousClusterState
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"], source ["
argument_list|)
operator|.
name|append
argument_list|(
name|source
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|previousClusterState
operator|.
name|nodes
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|previousClusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|previousClusterState
operator|.
name|readOnlyRoutingNodes
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|warn
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|previousClusterState
operator|==
name|newClusterState
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"processing [{}]: no change in cluster_state"
argument_list|,
name|source
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
if|if
condition|(
name|newClusterState
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeMaster
argument_list|()
condition|)
block|{
comment|// only the master controls the version numbers
name|Builder
name|builder
init|=
name|ClusterState
operator|.
name|builder
argument_list|()
operator|.
name|state
argument_list|(
name|newClusterState
argument_list|)
operator|.
name|version
argument_list|(
name|newClusterState
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|previousClusterState
operator|.
name|routingTable
argument_list|()
operator|!=
name|newClusterState
operator|.
name|routingTable
argument_list|()
condition|)
block|{
name|builder
operator|.
name|routingTable
argument_list|(
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|routingTable
argument_list|(
name|newClusterState
operator|.
name|routingTable
argument_list|()
argument_list|)
operator|.
name|version
argument_list|(
name|newClusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|previousClusterState
operator|.
name|metaData
argument_list|()
operator|!=
name|newClusterState
operator|.
name|metaData
argument_list|()
condition|)
block|{
name|builder
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|metaData
argument_list|(
name|newClusterState
operator|.
name|metaData
argument_list|()
argument_list|)
operator|.
name|version
argument_list|(
name|newClusterState
operator|.
name|metaData
argument_list|()
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|newClusterState
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|previousClusterState
operator|.
name|blocks
argument_list|()
operator|.
name|hasGlobalBlock
argument_list|(
name|Discovery
operator|.
name|NO_MASTER_BLOCK
argument_list|)
operator|&&
operator|!
name|newClusterState
operator|.
name|blocks
argument_list|()
operator|.
name|hasGlobalBlock
argument_list|(
name|Discovery
operator|.
name|NO_MASTER_BLOCK
argument_list|)
condition|)
block|{
comment|// force an update, its a fresh update from the master as we transition from a start of not having a master to having one
comment|// have a fresh instances of routing and metadata to remove the chance that version might be the same
name|Builder
name|builder
init|=
name|ClusterState
operator|.
name|builder
argument_list|()
operator|.
name|state
argument_list|(
name|newClusterState
argument_list|)
decl_stmt|;
name|builder
operator|.
name|routingTable
argument_list|(
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|routingTable
argument_list|(
name|newClusterState
operator|.
name|routingTable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|metaData
argument_list|(
name|newClusterState
operator|.
name|metaData
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|newClusterState
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"got first state from fresh master [{}]"
argument_list|,
name|newClusterState
operator|.
name|nodes
argument_list|()
operator|.
name|masterNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|newClusterState
operator|.
name|version
argument_list|()
operator|<
name|previousClusterState
operator|.
name|version
argument_list|()
condition|)
block|{
comment|// we got this cluster state from the master, filter out based on versions (don't call listeners)
name|logger
operator|.
name|debug
argument_list|(
literal|"got old cluster state ["
operator|+
name|newClusterState
operator|.
name|version
argument_list|()
operator|+
literal|"<"
operator|+
name|previousClusterState
operator|.
name|version
argument_list|()
operator|+
literal|"] from source ["
operator|+
name|source
operator|+
literal|"], ignoring"
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"cluster state updated:\nversion ["
argument_list|)
operator|.
name|append
argument_list|(
name|newClusterState
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"], source ["
argument_list|)
operator|.
name|append
argument_list|(
name|source
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|newClusterState
operator|.
name|nodes
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|newClusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|newClusterState
operator|.
name|readOnlyRoutingNodes
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"cluster state updated, version [{}], source [{}]"
argument_list|,
name|newClusterState
operator|.
name|version
argument_list|()
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
name|ClusterChangedEvent
name|clusterChangedEvent
init|=
operator|new
name|ClusterChangedEvent
argument_list|(
name|source
argument_list|,
name|newClusterState
argument_list|,
name|previousClusterState
argument_list|)
decl_stmt|;
comment|// new cluster state, notify all listeners
specifier|final
name|DiscoveryNodes
operator|.
name|Delta
name|nodesDelta
init|=
name|clusterChangedEvent
operator|.
name|nodesDelta
argument_list|()
decl_stmt|;
if|if
condition|(
name|nodesDelta
operator|.
name|hasChanges
argument_list|()
operator|&&
name|logger
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|String
name|summary
init|=
name|nodesDelta
operator|.
name|shortSummary
argument_list|()
decl_stmt|;
if|if
condition|(
name|summary
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"{}, reason: {}"
argument_list|,
name|summary
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
block|}
comment|// TODO, do this in parallel (and wait)
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|nodesDelta
operator|.
name|addedNodes
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|nodeRequiresConnection
argument_list|(
name|node
argument_list|)
condition|)
block|{
continue|continue;
block|}
try|try
block|{
name|transportService
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// the fault detection will detect it as failed as well
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to connect to node ["
operator|+
name|node
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// if we are the master, publish the new state to all nodes
comment|// we publish here before we send a notification to all the listeners, since if it fails
comment|// we don't want to notify
if|if
condition|(
name|newClusterState
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeMaster
argument_list|()
condition|)
block|{
name|discoveryService
operator|.
name|publish
argument_list|(
name|newClusterState
argument_list|)
expr_stmt|;
block|}
comment|// update the current cluster state
name|clusterState
operator|=
name|newClusterState
expr_stmt|;
for|for
control|(
name|ClusterStateListener
name|listener
range|:
name|priorityClusterStateListeners
control|)
block|{
name|listener
operator|.
name|clusterChanged
argument_list|(
name|clusterChangedEvent
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ClusterStateListener
name|listener
range|:
name|clusterStateListeners
control|)
block|{
name|listener
operator|.
name|clusterChanged
argument_list|(
name|clusterChangedEvent
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ClusterStateListener
name|listener
range|:
name|lastClusterStateListeners
control|)
block|{
name|listener
operator|.
name|clusterChanged
argument_list|(
name|clusterChangedEvent
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|nodesDelta
operator|.
name|removedNodes
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|cached
argument_list|()
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|nodesDelta
operator|.
name|removedNodes
argument_list|()
control|)
block|{
name|transportService
operator|.
name|disconnectFromNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|updateTask
operator|instanceof
name|ProcessedClusterStateUpdateTask
condition|)
block|{
operator|(
operator|(
name|ProcessedClusterStateUpdateTask
operator|)
name|updateTask
operator|)
operator|.
name|clusterStateProcessed
argument_list|(
name|newClusterState
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"processing [{}]: done applying updated cluster_state"
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"failed to apply updated cluster state:\nversion ["
argument_list|)
operator|.
name|append
argument_list|(
name|newClusterState
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"], source ["
argument_list|)
operator|.
name|append
argument_list|(
name|source
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|newClusterState
operator|.
name|nodes
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|newClusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|newClusterState
operator|.
name|readOnlyRoutingNodes
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|warn
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
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
DECL|class|NotifyTimeout
class|class
name|NotifyTimeout
implements|implements
name|Runnable
block|{
DECL|field|listener
specifier|final
name|TimeoutClusterStateListener
name|listener
decl_stmt|;
DECL|field|timeout
specifier|final
name|TimeValue
name|timeout
decl_stmt|;
DECL|field|future
name|ScheduledFuture
name|future
decl_stmt|;
DECL|method|NotifyTimeout
name|NotifyTimeout
parameter_list|(
name|TimeoutClusterStateListener
name|listener
parameter_list|,
name|TimeValue
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
block|}
DECL|method|cancel
specifier|public
name|void
name|cancel
parameter_list|()
block|{
name|future
operator|.
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|future
operator|.
name|isCancelled
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|lifecycle
operator|.
name|stoppedOrClosed
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onClose
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onTimeout
argument_list|(
name|this
operator|.
name|timeout
argument_list|)
expr_stmt|;
block|}
comment|// note, we rely on the listener to remove itself in case of timeout if needed
block|}
block|}
DECL|class|ReconnectToNodes
specifier|private
class|class
name|ReconnectToNodes
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
comment|// master node will check against all nodes if its alive with certain discoveries implementations,
comment|// but we can't rely on that, so we check on it as well
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|clusterState
operator|.
name|nodes
argument_list|()
control|)
block|{
if|if
condition|(
name|lifecycle
operator|.
name|stoppedOrClosed
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|nodeRequiresConnection
argument_list|(
name|node
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|nodeExists
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
comment|// we double check existence of node since connectToNode might take time...
if|if
condition|(
operator|!
name|transportService
operator|.
name|nodeConnected
argument_list|(
name|node
argument_list|)
condition|)
block|{
try|try
block|{
name|transportService
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|lifecycle
operator|.
name|stoppedOrClosed
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|nodeExists
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
comment|// double check here as well, maybe its gone?
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to reconnect to node {}"
argument_list|,
name|e
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
name|reconnectToNodes
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|reconnectInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|CACHED
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|nodeRequiresConnection
specifier|private
name|boolean
name|nodeRequiresConnection
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
return|return
name|localNode
argument_list|()
operator|.
name|shouldConnectTo
argument_list|(
name|node
argument_list|)
return|;
block|}
block|}
end_class

end_unit

