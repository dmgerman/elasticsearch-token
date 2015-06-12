begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation.decider
package|package
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
name|routing
operator|.
name|MutableShardRouting
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
name|RoutingNode
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
name|ShardRoutingState
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
name|settings
operator|.
name|NodeSettingsService
import|;
end_import

begin_comment
comment|/**  * {@link ThrottlingAllocationDecider} controls the recovery process per node in  * the cluster. It exposes two settings via the cluster update API that allow  * changes in real-time:  *<p/>  *<ul>  *<li><tt>cluster.routing.allocation.node_initial_primaries_recoveries</tt> -  * restricts the number of initial primary shard recovery operations on a single  * node. The default is<tt>4</tt></li>  *<p/>  *<li><tt>cluster.routing.allocation.node_concurrent_recoveries</tt> -  * restricts the number of concurrent recovery operations on a single node. The  * default is<tt>2</tt></li>  *</ul>  *<p/>  * If one of the above thresholds is exceeded per node this allocation decider  * will return {@link Decision#THROTTLE} as a hit to upstream logic to throttle  * the allocation process to prevent overloading nodes due to too many concurrent recovery  * processes.  */
end_comment

begin_class
DECL|class|ThrottlingAllocationDecider
specifier|public
class|class
name|ThrottlingAllocationDecider
extends|extends
name|AllocationDecider
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"throttling"
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES
init|=
literal|"cluster.routing.allocation.node_initial_primaries_recoveries"
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES
init|=
literal|"cluster.routing.allocation.node_concurrent_recoveries"
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_CONCURRENT_RECOVERIES
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_ROUTING_ALLOCATION_CONCURRENT_RECOVERIES
init|=
literal|"cluster.routing.allocation.concurrent_recoveries"
decl_stmt|;
DECL|field|DEFAULT_CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES
init|=
literal|2
decl_stmt|;
DECL|field|DEFAULT_CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES
init|=
literal|4
decl_stmt|;
DECL|field|primariesInitialRecoveries
specifier|private
specifier|volatile
name|int
name|primariesInitialRecoveries
decl_stmt|;
DECL|field|concurrentRecoveries
specifier|private
specifier|volatile
name|int
name|concurrentRecoveries
decl_stmt|;
annotation|@
name|Inject
DECL|method|ThrottlingAllocationDecider
specifier|public
name|ThrottlingAllocationDecider
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|primariesInitialRecoveries
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES
argument_list|,
name|DEFAULT_CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES
argument_list|)
expr_stmt|;
name|this
operator|.
name|concurrentRecoveries
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_CONCURRENT_RECOVERIES
argument_list|,
name|settings
operator|.
name|getAsInt
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES
argument_list|,
name|DEFAULT_CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using node_concurrent_recoveries [{}], node_initial_primaries_recoveries [{}]"
argument_list|,
name|concurrentRecoveries
argument_list|,
name|primariesInitialRecoveries
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
block|}
annotation|@
name|Override
DECL|method|canAllocate
specifier|public
name|Decision
name|canAllocate
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
assert|assert
name|shardRouting
operator|.
name|unassigned
argument_list|()
operator|||
name|shardRouting
operator|.
name|active
argument_list|()
assert|;
if|if
condition|(
name|shardRouting
operator|.
name|unassigned
argument_list|()
condition|)
block|{
comment|// primary is unassigned, means we are going to do recovery from gateway
comment|// count *just the primary* currently doing recovery on the node and check against concurrent_recoveries
name|int
name|primariesInRecovery
init|=
literal|0
decl_stmt|;
for|for
control|(
name|MutableShardRouting
name|shard
range|:
name|node
control|)
block|{
comment|// when a primary shard is INITIALIZING, it can be because of *initial recovery* or *relocation from another node*
comment|// we only count initial recoveries here, so we need to make sure that relocating node is null
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|ShardRoutingState
operator|.
name|INITIALIZING
operator|&&
name|shard
operator|.
name|primary
argument_list|()
operator|&&
name|shard
operator|.
name|relocatingNodeId
argument_list|()
operator|==
literal|null
condition|)
block|{
name|primariesInRecovery
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|primariesInRecovery
operator|>=
name|primariesInitialRecoveries
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|THROTTLE
argument_list|,
name|NAME
argument_list|,
literal|"too many primaries currently recovering [%d], limit: [%d]"
argument_list|,
name|primariesInRecovery
argument_list|,
name|primariesInitialRecoveries
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"below primary recovery limit of [%d]"
argument_list|,
name|primariesInitialRecoveries
argument_list|)
return|;
block|}
block|}
block|}
comment|// either primary or replica doing recovery (from peer shard)
comment|// count the number of recoveries on the node, its for both target (INITIALIZING) and source (RELOCATING)
return|return
name|canAllocate
argument_list|(
name|node
argument_list|,
name|allocation
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|canAllocate
specifier|public
name|Decision
name|canAllocate
parameter_list|(
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
name|int
name|currentRecoveries
init|=
literal|0
decl_stmt|;
for|for
control|(
name|MutableShardRouting
name|shard
range|:
name|node
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|ShardRoutingState
operator|.
name|INITIALIZING
operator|||
name|shard
operator|.
name|state
argument_list|()
operator|==
name|ShardRoutingState
operator|.
name|RELOCATING
condition|)
block|{
name|currentRecoveries
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|currentRecoveries
operator|>=
name|concurrentRecoveries
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|THROTTLE
argument_list|,
name|NAME
argument_list|,
literal|"too many shards currently recovering [%d], limit: [%d]"
argument_list|,
name|currentRecoveries
argument_list|,
name|concurrentRecoveries
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"below shard recovery limit of [%d]"
argument_list|,
name|concurrentRecoveries
argument_list|)
return|;
block|}
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
name|int
name|primariesInitialRecoveries
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES
argument_list|,
name|ThrottlingAllocationDecider
operator|.
name|this
operator|.
name|primariesInitialRecoveries
argument_list|)
decl_stmt|;
if|if
condition|(
name|primariesInitialRecoveries
operator|!=
name|ThrottlingAllocationDecider
operator|.
name|this
operator|.
name|primariesInitialRecoveries
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [cluster.routing.allocation.node_initial_primaries_recoveries] from [{}] to [{}]"
argument_list|,
name|ThrottlingAllocationDecider
operator|.
name|this
operator|.
name|primariesInitialRecoveries
argument_list|,
name|primariesInitialRecoveries
argument_list|)
expr_stmt|;
name|ThrottlingAllocationDecider
operator|.
name|this
operator|.
name|primariesInitialRecoveries
operator|=
name|primariesInitialRecoveries
expr_stmt|;
block|}
name|int
name|concurrentRecoveries
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES
argument_list|,
name|ThrottlingAllocationDecider
operator|.
name|this
operator|.
name|concurrentRecoveries
argument_list|)
decl_stmt|;
if|if
condition|(
name|concurrentRecoveries
operator|!=
name|ThrottlingAllocationDecider
operator|.
name|this
operator|.
name|concurrentRecoveries
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [cluster.routing.allocation.node_concurrent_recoveries] from [{}] to [{}]"
argument_list|,
name|ThrottlingAllocationDecider
operator|.
name|this
operator|.
name|concurrentRecoveries
argument_list|,
name|concurrentRecoveries
argument_list|)
expr_stmt|;
name|ThrottlingAllocationDecider
operator|.
name|this
operator|.
name|concurrentRecoveries
operator|=
name|concurrentRecoveries
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit
