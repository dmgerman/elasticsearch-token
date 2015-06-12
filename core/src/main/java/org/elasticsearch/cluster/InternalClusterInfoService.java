begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|LatchedActionListener
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
name|stats
operator|.
name|NodeStats
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
name|stats
operator|.
name|NodesStatsRequest
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
name|stats
operator|.
name|NodesStatsResponse
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
name|stats
operator|.
name|TransportNodesStatsAction
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
name|indices
operator|.
name|stats
operator|.
name|IndicesStatsRequest
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
name|indices
operator|.
name|stats
operator|.
name|IndicesStatsResponse
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
name|indices
operator|.
name|stats
operator|.
name|ShardStats
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
name|indices
operator|.
name|stats
operator|.
name|TransportIndicesStatsAction
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
name|ClusterBlockException
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
name|ShardRouting
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
name|decider
operator|.
name|DiskThresholdDecider
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
name|AbstractComponent
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
name|EsRejectedExecutionException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|fs
operator|.
name|FsStats
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
name|ReceiveTimeoutTransportException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|CountDownLatch
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

begin_comment
comment|/**  * InternalClusterInfoService provides the ClusterInfoService interface,  * routinely updated on a timer. The timer can be dynamically changed by  * setting the<code>cluster.info.update.interval</code> setting (defaulting  * to 30 seconds). The InternalClusterInfoService only runs on the master node.  * Listens for changes in the number of data nodes and immediately submits a  * ClusterInfoUpdateJob if a node has been added.  *  * Every time the timer runs, gathers information about the disk usage and  * shard sizes across the cluster.  */
end_comment

begin_class
DECL|class|InternalClusterInfoService
specifier|public
class|class
name|InternalClusterInfoService
extends|extends
name|AbstractComponent
implements|implements
name|ClusterInfoService
implements|,
name|LocalNodeMasterListener
implements|,
name|ClusterStateListener
block|{
DECL|field|INTERNAL_CLUSTER_INFO_UPDATE_INTERVAL
specifier|public
specifier|static
specifier|final
name|String
name|INTERNAL_CLUSTER_INFO_UPDATE_INTERVAL
init|=
literal|"cluster.info.update.interval"
decl_stmt|;
DECL|field|INTERNAL_CLUSTER_INFO_TIMEOUT
specifier|public
specifier|static
specifier|final
name|String
name|INTERNAL_CLUSTER_INFO_TIMEOUT
init|=
literal|"cluster.info.update.timeout"
decl_stmt|;
DECL|field|updateFrequency
specifier|private
specifier|volatile
name|TimeValue
name|updateFrequency
decl_stmt|;
DECL|field|usages
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|usages
decl_stmt|;
DECL|field|shardSizes
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|shardSizes
decl_stmt|;
DECL|field|isMaster
specifier|private
specifier|volatile
name|boolean
name|isMaster
init|=
literal|false
decl_stmt|;
DECL|field|enabled
specifier|private
specifier|volatile
name|boolean
name|enabled
decl_stmt|;
DECL|field|fetchTimeout
specifier|private
specifier|volatile
name|TimeValue
name|fetchTimeout
decl_stmt|;
DECL|field|transportNodesStatsAction
specifier|private
specifier|final
name|TransportNodesStatsAction
name|transportNodesStatsAction
decl_stmt|;
DECL|field|transportIndicesStatsAction
specifier|private
specifier|final
name|TransportIndicesStatsAction
name|transportIndicesStatsAction
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|listeners
specifier|private
specifier|final
name|Set
argument_list|<
name|Listener
argument_list|>
name|listeners
init|=
name|Collections
operator|.
name|synchronizedSet
argument_list|(
operator|new
name|HashSet
argument_list|<
name|Listener
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Inject
DECL|method|InternalClusterInfoService
specifier|public
name|InternalClusterInfoService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|,
name|TransportNodesStatsAction
name|transportNodesStatsAction
parameter_list|,
name|TransportIndicesStatsAction
name|transportIndicesStatsAction
parameter_list|,
name|ClusterService
name|clusterService
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
name|usages
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
name|this
operator|.
name|shardSizes
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
name|this
operator|.
name|transportNodesStatsAction
operator|=
name|transportNodesStatsAction
expr_stmt|;
name|this
operator|.
name|transportIndicesStatsAction
operator|=
name|transportIndicesStatsAction
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|updateFrequency
operator|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INTERNAL_CLUSTER_INFO_UPDATE_INTERVAL
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchTimeout
operator|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INTERNAL_CLUSTER_INFO_TIMEOUT
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|enabled
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|nodeSettingsService
operator|.
name|addListener
argument_list|(
operator|new
name|ApplySettings
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add InternalClusterInfoService to listen for Master changes
name|this
operator|.
name|clusterService
operator|.
name|add
argument_list|(
operator|(
name|LocalNodeMasterListener
operator|)
name|this
argument_list|)
expr_stmt|;
comment|// Add to listen for state changes (when nodes are added)
name|this
operator|.
name|clusterService
operator|.
name|add
argument_list|(
operator|(
name|ClusterStateListener
operator|)
name|this
argument_list|)
expr_stmt|;
block|}
DECL|class|ApplySettings
class|class
name|ApplySettings
implements|implements
name|NodeSettingsService
operator|.
name|Listener
block|{
annotation|@
name|Override
DECL|method|onRefreshSettings
specifier|public
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|TimeValue
name|newUpdateFrequency
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INTERNAL_CLUSTER_INFO_UPDATE_INTERVAL
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// ClusterInfoService is only enabled if the DiskThresholdDecider is enabled
name|Boolean
name|newEnabled
init|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|newUpdateFrequency
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|newUpdateFrequency
operator|.
name|getMillis
argument_list|()
operator|<
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
operator|.
name|getMillis
argument_list|()
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] set too low [{}] (< 10s)"
argument_list|,
name|INTERNAL_CLUSTER_INFO_UPDATE_INTERVAL
argument_list|,
name|newUpdateFrequency
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unable to set "
operator|+
name|INTERNAL_CLUSTER_INFO_UPDATE_INTERVAL
operator|+
literal|" less than 10 seconds"
argument_list|)
throw|;
block|}
else|else
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [{}] from [{}] to [{}]"
argument_list|,
name|INTERNAL_CLUSTER_INFO_UPDATE_INTERVAL
argument_list|,
name|updateFrequency
argument_list|,
name|newUpdateFrequency
argument_list|)
expr_stmt|;
name|InternalClusterInfoService
operator|.
name|this
operator|.
name|updateFrequency
operator|=
name|newUpdateFrequency
expr_stmt|;
block|}
block|}
name|TimeValue
name|newFetchTimeout
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INTERNAL_CLUSTER_INFO_TIMEOUT
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|newFetchTimeout
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating fetch timeout [{}] from [{}] to [{}]"
argument_list|,
name|INTERNAL_CLUSTER_INFO_TIMEOUT
argument_list|,
name|fetchTimeout
argument_list|,
name|newFetchTimeout
argument_list|)
expr_stmt|;
name|InternalClusterInfoService
operator|.
name|this
operator|.
name|fetchTimeout
operator|=
name|newFetchTimeout
expr_stmt|;
block|}
comment|// We don't log about enabling it here, because the DiskThresholdDecider will already be logging about enable/disable
if|if
condition|(
name|newEnabled
operator|!=
literal|null
condition|)
block|{
name|InternalClusterInfoService
operator|.
name|this
operator|.
name|enabled
operator|=
name|newEnabled
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|onMaster
specifier|public
name|void
name|onMaster
parameter_list|()
block|{
name|this
operator|.
name|isMaster
operator|=
literal|true
expr_stmt|;
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
name|trace
argument_list|(
literal|"I have been elected master, scheduling a ClusterInfoUpdateJob"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// Submit a job that will start after DEFAULT_STARTING_INTERVAL, and reschedule itself after running
name|threadPool
operator|.
name|schedule
argument_list|(
name|updateFrequency
argument_list|,
name|executorName
argument_list|()
argument_list|,
operator|new
name|SubmitReschedulingClusterInfoUpdatedJob
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|getNodes
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
comment|// Submit an info update job to be run immediately
name|updateOnce
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|EsRejectedExecutionException
name|ex
parameter_list|)
block|{
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
literal|"Couldn't schedule cluster info update task - node might be shutting down"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// called from tests as well
comment|/**      * will collect a fresh {@link ClusterInfo} from the nodes, without scheduling a future collection      */
DECL|method|updateOnce
name|void
name|updateOnce
parameter_list|()
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|executorName
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|ClusterInfoUpdateJob
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|offMaster
specifier|public
name|void
name|offMaster
parameter_list|()
block|{
name|this
operator|.
name|isMaster
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executorName
specifier|public
name|String
name|executorName
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
operator|!
name|this
operator|.
name|enabled
condition|)
block|{
return|return;
block|}
comment|// Check whether it was a data node that was added
name|boolean
name|dataNodeAdded
init|=
literal|false
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|addedNode
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
name|addedNode
operator|.
name|dataNode
argument_list|()
condition|)
block|{
name|dataNodeAdded
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|this
operator|.
name|isMaster
operator|&&
name|dataNodeAdded
operator|&&
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|getNodes
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
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
literal|"data node was added, retrieving new cluster info"
argument_list|)
expr_stmt|;
block|}
name|updateOnce
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|isMaster
operator|&&
name|event
operator|.
name|nodesRemoved
argument_list|()
condition|)
block|{
for|for
control|(
name|DiscoveryNode
name|removedNode
range|:
name|event
operator|.
name|nodesDelta
argument_list|()
operator|.
name|removedNodes
argument_list|()
control|)
block|{
if|if
condition|(
name|removedNode
operator|.
name|dataNode
argument_list|()
condition|)
block|{
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
name|trace
argument_list|(
literal|"Removing node from cluster info: {}"
argument_list|,
name|removedNode
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|newUsages
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|usages
argument_list|)
decl_stmt|;
name|newUsages
operator|.
name|remove
argument_list|(
name|removedNode
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|usages
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|newUsages
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|getClusterInfo
specifier|public
name|ClusterInfo
name|getClusterInfo
parameter_list|()
block|{
return|return
operator|new
name|ClusterInfo
argument_list|(
name|usages
argument_list|,
name|shardSizes
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|addListener
specifier|public
name|void
name|addListener
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
comment|/**      * Class used to submit {@link ClusterInfoUpdateJob}s on the      * {@link InternalClusterInfoService} threadpool, these jobs will      * reschedule themselves by placing a new instance of this class onto the      * scheduled threadpool.      */
DECL|class|SubmitReschedulingClusterInfoUpdatedJob
specifier|public
class|class
name|SubmitReschedulingClusterInfoUpdatedJob
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
name|trace
argument_list|(
literal|"Submitting new rescheduling cluster info update job"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|executorName
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|ClusterInfoUpdateJob
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EsRejectedExecutionException
name|ex
parameter_list|)
block|{
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
literal|"Couldn't re-schedule cluster info update task - node might be shutting down"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * Retrieve the latest nodes stats, calling the listener when complete      * @return a latch that can be used to wait for the nodes stats to complete if desired      */
DECL|method|updateNodeStats
specifier|protected
name|CountDownLatch
name|updateNodeStats
parameter_list|(
specifier|final
name|ActionListener
argument_list|<
name|NodesStatsResponse
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|NodesStatsRequest
name|nodesStatsRequest
init|=
operator|new
name|NodesStatsRequest
argument_list|(
literal|"data:true"
argument_list|)
decl_stmt|;
name|nodesStatsRequest
operator|.
name|clear
argument_list|()
expr_stmt|;
name|nodesStatsRequest
operator|.
name|fs
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|nodesStatsRequest
operator|.
name|timeout
argument_list|(
name|fetchTimeout
argument_list|)
expr_stmt|;
name|transportNodesStatsAction
operator|.
name|execute
argument_list|(
name|nodesStatsRequest
argument_list|,
operator|new
name|LatchedActionListener
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|latch
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|latch
return|;
block|}
comment|/**      * Retrieve the latest indices stats, calling the listener when complete      * @return a latch that can be used to wait for the indices stats to complete if desired      */
DECL|method|updateIndicesStats
specifier|protected
name|CountDownLatch
name|updateIndicesStats
parameter_list|(
specifier|final
name|ActionListener
argument_list|<
name|IndicesStatsResponse
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|IndicesStatsRequest
name|indicesStatsRequest
init|=
operator|new
name|IndicesStatsRequest
argument_list|()
decl_stmt|;
name|indicesStatsRequest
operator|.
name|clear
argument_list|()
expr_stmt|;
name|indicesStatsRequest
operator|.
name|store
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|transportIndicesStatsAction
operator|.
name|execute
argument_list|(
name|indicesStatsRequest
argument_list|,
operator|new
name|LatchedActionListener
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|latch
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|latch
return|;
block|}
comment|/**      * Runnable class that performs a {@Link NodesStatsRequest} to retrieve      * disk usages for nodes in the cluster and an {@link IndicesStatsRequest}      * to retrieve the sizes of all shards to ensure they can fit on nodes      * during shard balancing.      */
DECL|class|ClusterInfoUpdateJob
specifier|public
class|class
name|ClusterInfoUpdateJob
implements|implements
name|Runnable
block|{
comment|// This boolean is used to signal to the ClusterInfoUpdateJob that it
comment|// needs to reschedule itself to run again at a later time. It can be
comment|// set to false to only run once
DECL|field|reschedule
specifier|private
specifier|final
name|boolean
name|reschedule
decl_stmt|;
DECL|method|ClusterInfoUpdateJob
specifier|public
name|ClusterInfoUpdateJob
parameter_list|(
name|boolean
name|reschedule
parameter_list|)
block|{
name|this
operator|.
name|reschedule
operator|=
name|reschedule
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
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Performing ClusterInfoUpdateJob"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isMaster
operator|&&
name|this
operator|.
name|reschedule
condition|)
block|{
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
name|trace
argument_list|(
literal|"Scheduling next run for updating cluster info in: {}"
argument_list|,
name|updateFrequency
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|threadPool
operator|.
name|schedule
argument_list|(
name|updateFrequency
argument_list|,
name|executorName
argument_list|()
argument_list|,
operator|new
name|SubmitReschedulingClusterInfoUpdatedJob
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EsRejectedExecutionException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Reschedule cluster info service was rejected"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
comment|// Short-circuit if not enabled
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
name|trace
argument_list|(
literal|"Skipping ClusterInfoUpdatedJob since it is disabled"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|CountDownLatch
name|nodeLatch
init|=
name|updateNodeStats
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|NodesStatsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|NodesStatsResponse
name|nodeStatses
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|newUsages
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeStats
name|nodeStats
range|:
name|nodeStatses
operator|.
name|getNodes
argument_list|()
control|)
block|{
if|if
condition|(
name|nodeStats
operator|.
name|getFs
argument_list|()
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Unable to retrieve node FS stats for {}"
argument_list|,
name|nodeStats
operator|.
name|getNode
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|long
name|available
init|=
literal|0
decl_stmt|;
name|long
name|total
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FsStats
operator|.
name|Info
name|info
range|:
name|nodeStats
operator|.
name|getFs
argument_list|()
control|)
block|{
name|available
operator|+=
name|info
operator|.
name|getAvailable
argument_list|()
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|total
operator|+=
name|info
operator|.
name|getTotal
argument_list|()
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
name|String
name|nodeId
init|=
name|nodeStats
operator|.
name|getNode
argument_list|()
operator|.
name|id
argument_list|()
decl_stmt|;
name|String
name|nodeName
init|=
name|nodeStats
operator|.
name|getNode
argument_list|()
operator|.
name|getName
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
name|trace
argument_list|(
literal|"node: [{}], total disk: {}, available disk: {}"
argument_list|,
name|nodeId
argument_list|,
name|total
argument_list|,
name|available
argument_list|)
expr_stmt|;
block|}
name|newUsages
operator|.
name|put
argument_list|(
name|nodeId
argument_list|,
operator|new
name|DiskUsage
argument_list|(
name|nodeId
argument_list|,
name|nodeName
argument_list|,
name|total
argument_list|,
name|available
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|usages
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|newUsages
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|ReceiveTimeoutTransportException
condition|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"NodeStatsAction timed out for ClusterInfoUpdateJob (reason [{}])"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|e
operator|instanceof
name|ClusterBlockException
condition|)
block|{
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
name|trace
argument_list|(
literal|"Failed to execute NodeStatsAction for ClusterInfoUpdateJob"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to execute NodeStatsAction for ClusterInfoUpdateJob"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// we empty the usages list, to be safe - we don't know what's going on.
name|usages
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|CountDownLatch
name|indicesLatch
init|=
name|updateIndicesStats
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|IndicesStatsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|IndicesStatsResponse
name|indicesStatsResponse
parameter_list|)
block|{
name|ShardStats
index|[]
name|stats
init|=
name|indicesStatsResponse
operator|.
name|getShards
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|newShardSizes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardStats
name|s
range|:
name|stats
control|)
block|{
name|long
name|size
init|=
name|s
operator|.
name|getStats
argument_list|()
operator|.
name|getStore
argument_list|()
operator|.
name|sizeInBytes
argument_list|()
decl_stmt|;
name|String
name|sid
init|=
name|shardIdentifierFromRouting
argument_list|(
name|s
operator|.
name|getShardRouting
argument_list|()
argument_list|)
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
name|trace
argument_list|(
literal|"shard: {} size: {}"
argument_list|,
name|sid
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
name|newShardSizes
operator|.
name|put
argument_list|(
name|sid
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
name|shardSizes
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|newShardSizes
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|ReceiveTimeoutTransportException
condition|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"IndicesStatsAction timed out for ClusterInfoUpdateJob (reason [{}])"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|e
operator|instanceof
name|ClusterBlockException
condition|)
block|{
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
name|trace
argument_list|(
literal|"Failed to execute IndicesStatsAction for ClusterInfoUpdateJob"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to execute IndicesStatsAction for ClusterInfoUpdateJob"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// we empty the usages list, to be safe - we don't know what's going on.
name|shardSizes
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|nodeLatch
operator|.
name|await
argument_list|(
name|fetchTimeout
operator|.
name|getMillis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to update node information for ClusterInfoUpdateJob within 15s timeout"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|indicesLatch
operator|.
name|await
argument_list|(
name|fetchTimeout
operator|.
name|getMillis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to update shard information for ClusterInfoUpdateJob within 15s timeout"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Listener
name|l
range|:
name|listeners
control|)
block|{
try|try
block|{
name|l
operator|.
name|onNewInfo
argument_list|(
name|getClusterInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Failed executing ClusterInfoService listener"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * Method that incorporates the ShardId for the shard into a string that      * includes a 'p' or 'r' depending on whether the shard is a primary.      */
DECL|method|shardIdentifierFromRouting
specifier|public
specifier|static
name|String
name|shardIdentifierFromRouting
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|)
block|{
return|return
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"["
operator|+
operator|(
name|shardRouting
operator|.
name|primary
argument_list|()
condition|?
literal|"p"
else|:
literal|"r"
operator|)
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit
