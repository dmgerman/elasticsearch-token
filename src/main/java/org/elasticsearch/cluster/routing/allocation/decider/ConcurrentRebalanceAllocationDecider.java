begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Similar to the {@link ClusterRebalanceAllocationDecider} this  * {@link AllocationDecider} controls the number of currently in-progress  * re-balance (relocation) operations and restricts node allocations if the  * configured threashold is reached. The default number of concurrent rebalance  * operations is set to<tt>2</tt>  *<p/>  * Re-balance operations can be controlled in real-time via the cluster update API using  *<tt>cluster.routing.allocation.cluster_concurrent_rebalance</tt>. Iff this  * setting is set to<tt>-1</tt> the number of concurrent re-balance operations  * are unlimited.  */
end_comment

begin_class
DECL|class|ConcurrentRebalanceAllocationDecider
specifier|public
class|class
name|ConcurrentRebalanceAllocationDecider
extends|extends
name|AllocationDecider
block|{
DECL|field|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE
init|=
literal|"cluster.routing.allocation.cluster_concurrent_rebalance"
decl_stmt|;
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
name|clusterConcurrentRebalance
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE
argument_list|,
name|ConcurrentRebalanceAllocationDecider
operator|.
name|this
operator|.
name|clusterConcurrentRebalance
argument_list|)
decl_stmt|;
if|if
condition|(
name|clusterConcurrentRebalance
operator|!=
name|ConcurrentRebalanceAllocationDecider
operator|.
name|this
operator|.
name|clusterConcurrentRebalance
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [cluster.routing.allocation.cluster_concurrent_rebalance] from [{}], to [{}]"
argument_list|,
name|ConcurrentRebalanceAllocationDecider
operator|.
name|this
operator|.
name|clusterConcurrentRebalance
argument_list|,
name|clusterConcurrentRebalance
argument_list|)
expr_stmt|;
name|ConcurrentRebalanceAllocationDecider
operator|.
name|this
operator|.
name|clusterConcurrentRebalance
operator|=
name|clusterConcurrentRebalance
expr_stmt|;
block|}
block|}
block|}
DECL|field|clusterConcurrentRebalance
specifier|private
specifier|volatile
name|int
name|clusterConcurrentRebalance
decl_stmt|;
annotation|@
name|Inject
DECL|method|ConcurrentRebalanceAllocationDecider
specifier|public
name|ConcurrentRebalanceAllocationDecider
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
name|clusterConcurrentRebalance
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using [cluster_concurrent_rebalance] with [{}]"
argument_list|,
name|clusterConcurrentRebalance
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
DECL|method|canRebalance
specifier|public
name|Decision
name|canRebalance
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
if|if
condition|(
name|clusterConcurrentRebalance
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|Decision
operator|.
name|YES
return|;
block|}
name|int
name|rebalance
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RoutingNode
name|node
range|:
name|allocation
operator|.
name|routingNodes
argument_list|()
control|)
block|{
name|List
argument_list|<
name|MutableShardRouting
argument_list|>
name|shards
init|=
name|node
operator|.
name|shards
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
name|shards
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|shards
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|state
argument_list|()
operator|==
name|ShardRoutingState
operator|.
name|RELOCATING
condition|)
block|{
name|rebalance
operator|++
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|rebalance
operator|>=
name|clusterConcurrentRebalance
condition|)
block|{
return|return
name|Decision
operator|.
name|NO
return|;
block|}
return|return
name|Decision
operator|.
name|YES
return|;
block|}
block|}
end_class

end_unit

