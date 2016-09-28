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
comment|/**  * Similar to the {@link ClusterRebalanceAllocationDecider} this  * {@link AllocationDecider} controls the number of currently in-progress  * re-balance (relocation) operations and restricts node allocations if the  * configured threshold is reached. The default number of concurrent rebalance  * operations is set to<tt>2</tt>  *<p>  * Re-balance operations can be controlled in real-time via the cluster update API using  *<tt>cluster.routing.allocation.cluster_concurrent_rebalance</tt>. Iff this  * setting is set to<tt>-1</tt> the number of concurrent re-balance operations  * are unlimited.  */
end_comment

begin_class
DECL|class|ConcurrentRebalanceAllocationDecider
specifier|public
class|class
name|ConcurrentRebalanceAllocationDecider
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
literal|"concurrent_rebalance"
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE_SETTING
init|=
name|Setting
operator|.
name|intSetting
argument_list|(
literal|"cluster.routing.allocation.cluster_concurrent_rebalance"
argument_list|,
literal|2
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
DECL|field|clusterConcurrentRebalance
specifier|private
specifier|volatile
name|int
name|clusterConcurrentRebalance
decl_stmt|;
DECL|method|ConcurrentRebalanceAllocationDecider
specifier|public
name|ConcurrentRebalanceAllocationDecider
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
name|clusterConcurrentRebalance
operator|=
name|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE_SETTING
operator|.
name|get
argument_list|(
name|settings
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
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_CLUSTER_CONCURRENT_REBALANCE_SETTING
argument_list|,
name|this
operator|::
name|setClusterConcurrentRebalance
argument_list|)
expr_stmt|;
block|}
DECL|method|setClusterConcurrentRebalance
specifier|private
name|void
name|setClusterConcurrentRebalance
parameter_list|(
name|int
name|concurrentRebalance
parameter_list|)
block|{
name|clusterConcurrentRebalance
operator|=
name|concurrentRebalance
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
literal|"unlimited concurrent rebalances are allowed"
argument_list|)
return|;
block|}
name|int
name|relocatingShards
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|getRelocatingShardCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|relocatingShards
operator|>=
name|clusterConcurrentRebalance
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
literal|"too many shards are concurrently rebalancing [%d], limit: [%d]"
argument_list|,
name|relocatingShards
argument_list|,
name|clusterConcurrentRebalance
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
literal|"below threshold [%d] for concurrent rebalances, current rebalance shard count [%d]"
argument_list|,
name|clusterConcurrentRebalance
argument_list|,
name|relocatingShards
argument_list|)
return|;
block|}
block|}
end_class

end_unit

