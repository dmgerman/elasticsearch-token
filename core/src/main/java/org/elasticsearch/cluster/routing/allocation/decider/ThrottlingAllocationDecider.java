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
name|ClusterSettings
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
name|Setting
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
name|Setting
operator|.
name|Property
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

begin_comment
comment|/**  * {@link ThrottlingAllocationDecider} controls the recovery process per node in  * the cluster. It exposes two settings via the cluster update API that allow  * changes in real-time:  *<ul>  *<li><tt>cluster.routing.allocation.node_initial_primaries_recoveries</tt> -  * restricts the number of initial primary shard recovery operations on a single  * node. The default is<tt>4</tt></li>  *<li><tt>cluster.routing.allocation.node_concurrent_recoveries</tt> -  * restricts the number of total concurrent shards initializing on a single node. The  * default is<tt>2</tt></li>  *</ul>  *<p>  * If one of the above thresholds is exceeded per node this allocation decider  * will return {@link Decision#THROTTLE} as a hit to upstream logic to throttle  * the allocation process to prevent overloading nodes due to too many concurrent recovery  * processes.  */
end_comment

begin_class
DECL|class|ThrottlingAllocationDecider
specifier|public
class|class
name|ThrottlingAllocationDecider
extends|extends
name|AllocationDecider
block|{
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
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"throttling"
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cluster.routing.allocation.node_concurrent_recoveries"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|DEFAULT_CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES
argument_list|)
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Setting
operator|.
name|parseInt
argument_list|(
name|s
argument_list|,
literal|0
argument_list|,
literal|"cluster.routing.allocation.node_concurrent_recoveries"
argument_list|)
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES_SETTING
init|=
name|Setting
operator|.
name|intSetting
argument_list|(
literal|"cluster.routing.allocation.node_initial_primaries_recoveries"
argument_list|,
name|DEFAULT_CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES
argument_list|,
literal|0
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_INCOMING_RECOVERIES_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_INCOMING_RECOVERIES_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cluster.routing.allocation.node_concurrent_incoming_recoveries"
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES_SETTING
operator|.
name|getRaw
argument_list|(
name|s
argument_list|)
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Setting
operator|.
name|parseInt
argument_list|(
name|s
argument_list|,
literal|0
argument_list|,
literal|"cluster.routing.allocation.node_concurrent_incoming_recoveries"
argument_list|)
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_OUTGOING_RECOVERIES_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_OUTGOING_RECOVERIES_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cluster.routing.allocation.node_concurrent_outgoing_recoveries"
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES_SETTING
operator|.
name|getRaw
argument_list|(
name|s
argument_list|)
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Setting
operator|.
name|parseInt
argument_list|(
name|s
argument_list|,
literal|0
argument_list|,
literal|"cluster.routing.allocation.node_concurrent_outgoing_recoveries"
argument_list|)
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|primariesInitialRecoveries
specifier|private
specifier|volatile
name|int
name|primariesInitialRecoveries
decl_stmt|;
DECL|field|concurrentIncomingRecoveries
specifier|private
specifier|volatile
name|int
name|concurrentIncomingRecoveries
decl_stmt|;
DECL|field|concurrentOutgoingRecoveries
specifier|private
specifier|volatile
name|int
name|concurrentOutgoingRecoveries
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
name|ClusterSettings
name|clusterSettings
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
name|CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|concurrentIncomingRecoveries
operator|=
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_INCOMING_RECOVERIES_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|concurrentOutgoingRecoveries
operator|=
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_OUTGOING_RECOVERIES_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_NODE_INITIAL_PRIMARIES_RECOVERIES_SETTING
argument_list|,
name|this
operator|::
name|setPrimariesInitialRecoveries
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_INCOMING_RECOVERIES_SETTING
argument_list|,
name|this
operator|::
name|setConcurrentIncomingRecoverries
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_OUTGOING_RECOVERIES_SETTING
argument_list|,
name|this
operator|::
name|setConcurrentOutgoingRecoverries
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using node_concurrent_outgoing_recoveries [{}], node_concurrent_incoming_recoveries [{}], "
operator|+
literal|"node_initial_primaries_recoveries [{}]"
argument_list|,
name|concurrentOutgoingRecoveries
argument_list|,
name|concurrentIncomingRecoveries
argument_list|,
name|primariesInitialRecoveries
argument_list|)
expr_stmt|;
block|}
DECL|method|setConcurrentIncomingRecoverries
specifier|private
name|void
name|setConcurrentIncomingRecoverries
parameter_list|(
name|int
name|concurrentIncomingRecoveries
parameter_list|)
block|{
name|this
operator|.
name|concurrentIncomingRecoveries
operator|=
name|concurrentIncomingRecoveries
expr_stmt|;
block|}
DECL|method|setConcurrentOutgoingRecoverries
specifier|private
name|void
name|setConcurrentOutgoingRecoverries
parameter_list|(
name|int
name|concurrentOutgoingRecoveries
parameter_list|)
block|{
name|this
operator|.
name|concurrentOutgoingRecoveries
operator|=
name|concurrentOutgoingRecoveries
expr_stmt|;
block|}
DECL|method|setPrimariesInitialRecoveries
specifier|private
name|void
name|setPrimariesInitialRecoveries
parameter_list|(
name|int
name|primariesInitialRecoveries
parameter_list|)
block|{
name|this
operator|.
name|primariesInitialRecoveries
operator|=
name|primariesInitialRecoveries
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
name|ShardRouting
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
name|initializing
argument_list|()
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
literal|"too many primaries are currently recovering [%d], limit: [%d]"
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
comment|// TODO should we allow shards not allocated post API to always allocate?
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
name|currentOutRecoveries
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|getOutgoingRecoveries
argument_list|(
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|currentInRecoveries
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|getIncomingRecoveries
argument_list|(
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentOutRecoveries
operator|>=
name|concurrentOutgoingRecoveries
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
literal|"too many outgoing shards are currently recovering [%d], limit: [%d]"
argument_list|,
name|currentOutRecoveries
argument_list|,
name|concurrentOutgoingRecoveries
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|currentInRecoveries
operator|>=
name|concurrentIncomingRecoveries
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
literal|"too many incoming shards are currently recovering [%d], limit: [%d]"
argument_list|,
name|currentInRecoveries
argument_list|,
name|concurrentIncomingRecoveries
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
literal|"below shard recovery limit of outgoing: [%d< %d] incoming: [%d< %d]"
argument_list|,
name|currentOutRecoveries
argument_list|,
name|concurrentOutgoingRecoveries
argument_list|,
name|currentInRecoveries
argument_list|,
name|concurrentIncomingRecoveries
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

