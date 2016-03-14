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
name|metadata
operator|.
name|IndexMetaData
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
comment|/**  * This {@link AllocationDecider} limits the number of shards per node on a per  * index or node-wide basis. The allocator prevents a single node to hold more  * than<tt>index.routing.allocation.total_shards_per_node</tt> per index and  *<tt>cluster.routing.allocation.total_shards_per_node</tt> globally during the allocation  * process. The limits of this decider can be changed in real-time via a the  * index settings API.  *<p>  * If<tt>index.routing.allocation.total_shards_per_node</tt> is reset to a negative value shards  * per index are unlimited per node. Shards currently in the  * {@link ShardRoutingState#RELOCATING relocating} state are ignored by this  * {@link AllocationDecider} until the shard changed its state to either  * {@link ShardRoutingState#STARTED started},  * {@link ShardRoutingState#INITIALIZING inializing} or  * {@link ShardRoutingState#UNASSIGNED unassigned}  *<p>  * Note: Reducing the number of shards per node via the index update API can  * trigger relocation and significant additional load on the clusters nodes.  *</p>  */
end_comment

begin_class
DECL|class|ShardsLimitAllocationDecider
specifier|public
class|class
name|ShardsLimitAllocationDecider
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
literal|"shards_limit"
decl_stmt|;
DECL|field|clusterShardLimit
specifier|private
specifier|volatile
name|int
name|clusterShardLimit
decl_stmt|;
comment|/**      * Controls the maximum number of shards per index on a single Elasticsearch      * node. Negative values are interpreted as unlimited.      */
DECL|field|INDEX_TOTAL_SHARDS_PER_NODE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|INDEX_TOTAL_SHARDS_PER_NODE_SETTING
init|=
name|Setting
operator|.
name|intSetting
argument_list|(
literal|"index.routing.allocation.total_shards_per_node"
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|IndexScope
argument_list|)
decl_stmt|;
comment|/**      * Controls the maximum number of shards per node on a global level.      * Negative values are interpreted as unlimited.      */
DECL|field|CLUSTER_TOTAL_SHARDS_PER_NODE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|CLUSTER_TOTAL_SHARDS_PER_NODE_SETTING
init|=
name|Setting
operator|.
name|intSetting
argument_list|(
literal|"cluster.routing.allocation.total_shards_per_node"
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
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
annotation|@
name|Inject
DECL|method|ShardsLimitAllocationDecider
specifier|public
name|ShardsLimitAllocationDecider
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
name|clusterShardLimit
operator|=
name|CLUSTER_TOTAL_SHARDS_PER_NODE_SETTING
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
name|CLUSTER_TOTAL_SHARDS_PER_NODE_SETTING
argument_list|,
name|this
operator|::
name|setClusterShardLimit
argument_list|)
expr_stmt|;
block|}
DECL|method|setClusterShardLimit
specifier|private
name|void
name|setClusterShardLimit
parameter_list|(
name|int
name|clusterShardLimit
parameter_list|)
block|{
name|this
operator|.
name|clusterShardLimit
operator|=
name|clusterShardLimit
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
name|IndexMetaData
name|indexMd
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|indexShardLimit
init|=
name|INDEX_TOTAL_SHARDS_PER_NODE_SETTING
operator|.
name|get
argument_list|(
name|indexMd
operator|.
name|getSettings
argument_list|()
argument_list|,
name|settings
argument_list|)
decl_stmt|;
comment|// Capture the limit here in case it changes during this method's
comment|// execution
specifier|final
name|int
name|clusterShardLimit
init|=
name|this
operator|.
name|clusterShardLimit
decl_stmt|;
if|if
condition|(
name|indexShardLimit
operator|<=
literal|0
operator|&&
name|clusterShardLimit
operator|<=
literal|0
condition|)
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
literal|"total shard limit disabled: [index: %d, cluster: %d]<= 0"
argument_list|,
name|indexShardLimit
argument_list|,
name|clusterShardLimit
argument_list|)
return|;
block|}
name|int
name|indexShardCount
init|=
literal|0
decl_stmt|;
name|int
name|nodeShardCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ShardRouting
name|nodeShard
range|:
name|node
control|)
block|{
comment|// don't count relocating shards...
if|if
condition|(
name|nodeShard
operator|.
name|relocating
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|nodeShardCount
operator|++
expr_stmt|;
if|if
condition|(
name|nodeShard
operator|.
name|index
argument_list|()
operator|.
name|equals
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
name|indexShardCount
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|clusterShardLimit
operator|>
literal|0
operator|&&
name|nodeShardCount
operator|>=
name|clusterShardLimit
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"too many shards for this node [%d], limit: [%d]"
argument_list|,
name|nodeShardCount
argument_list|,
name|clusterShardLimit
argument_list|)
return|;
block|}
if|if
condition|(
name|indexShardLimit
operator|>
literal|0
operator|&&
name|indexShardCount
operator|>=
name|indexShardLimit
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"too many shards for this index [%s] on node [%d], limit: [%d]"
argument_list|,
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|,
name|indexShardCount
argument_list|,
name|indexShardLimit
argument_list|)
return|;
block|}
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
literal|"shard count under index limit [%d] and node limit [%d] of total shards per node"
argument_list|,
name|indexShardLimit
argument_list|,
name|clusterShardLimit
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|canRemain
specifier|public
name|Decision
name|canRemain
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
name|IndexMetaData
name|indexMd
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|indexShardLimit
init|=
name|INDEX_TOTAL_SHARDS_PER_NODE_SETTING
operator|.
name|get
argument_list|(
name|indexMd
operator|.
name|getSettings
argument_list|()
argument_list|,
name|settings
argument_list|)
decl_stmt|;
comment|// Capture the limit here in case it changes during this method's
comment|// execution
specifier|final
name|int
name|clusterShardLimit
init|=
name|this
operator|.
name|clusterShardLimit
decl_stmt|;
if|if
condition|(
name|indexShardLimit
operator|<=
literal|0
operator|&&
name|clusterShardLimit
operator|<=
literal|0
condition|)
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
literal|"total shard limit disabled: [index: %d, cluster: %d]<= 0"
argument_list|,
name|indexShardLimit
argument_list|,
name|clusterShardLimit
argument_list|)
return|;
block|}
name|int
name|indexShardCount
init|=
literal|0
decl_stmt|;
name|int
name|nodeShardCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ShardRouting
name|nodeShard
range|:
name|node
control|)
block|{
comment|// don't count relocating shards...
if|if
condition|(
name|nodeShard
operator|.
name|relocating
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|nodeShardCount
operator|++
expr_stmt|;
if|if
condition|(
name|nodeShard
operator|.
name|index
argument_list|()
operator|.
name|equals
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
name|indexShardCount
operator|++
expr_stmt|;
block|}
block|}
comment|// Subtle difference between the `canAllocate` and `canRemain` is that
comment|// this checks> while canAllocate checks>=
if|if
condition|(
name|clusterShardLimit
operator|>
literal|0
operator|&&
name|nodeShardCount
operator|>
name|clusterShardLimit
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"too many shards for this node [%d], limit: [%d]"
argument_list|,
name|nodeShardCount
argument_list|,
name|clusterShardLimit
argument_list|)
return|;
block|}
if|if
condition|(
name|indexShardLimit
operator|>
literal|0
operator|&&
name|indexShardCount
operator|>
name|indexShardLimit
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"too many shards for this index [%s] on node [%d], limit: [%d]"
argument_list|,
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|,
name|indexShardCount
argument_list|,
name|indexShardLimit
argument_list|)
return|;
block|}
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
literal|"shard count under index limit [%d] and node limit [%d] of total shards per node"
argument_list|,
name|indexShardLimit
argument_list|,
name|clusterShardLimit
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
comment|// Only checks the node-level limit, not the index-level
comment|// Capture the limit here in case it changes during this method's
comment|// execution
specifier|final
name|int
name|clusterShardLimit
init|=
name|this
operator|.
name|clusterShardLimit
decl_stmt|;
if|if
condition|(
name|clusterShardLimit
operator|<=
literal|0
condition|)
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
literal|"total shard limit disabled: [cluster: %d]<= 0"
argument_list|,
name|clusterShardLimit
argument_list|)
return|;
block|}
name|int
name|nodeShardCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ShardRouting
name|nodeShard
range|:
name|node
control|)
block|{
comment|// don't count relocating shards...
if|if
condition|(
name|nodeShard
operator|.
name|relocating
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|nodeShardCount
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|clusterShardLimit
operator|>=
literal|0
operator|&&
name|nodeShardCount
operator|>=
name|clusterShardLimit
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"too many shards for this node [%d], limit: [%d]"
argument_list|,
name|nodeShardCount
argument_list|,
name|clusterShardLimit
argument_list|)
return|;
block|}
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
literal|"shard count under node limit [%d] of total shards per node"
argument_list|,
name|clusterShardLimit
argument_list|)
return|;
block|}
block|}
end_class

end_unit

