begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation.allocator
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
name|allocator
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
name|allocation
operator|.
name|FailedRerouteAllocation
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
name|routing
operator|.
name|allocation
operator|.
name|StartedRerouteAllocation
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
name|ImmutableSettings
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
name|gateway
operator|.
name|none
operator|.
name|NoneGatewayAllocator
import|;
end_import

begin_comment
comment|/**  * The {@link ShardsAllocator} class offers methods for allocating shard within a cluster.  * These methods include moving shards and re-balancing the cluster. It also allows management  * of shards by their state.   */
end_comment

begin_class
DECL|class|ShardsAllocators
specifier|public
class|class
name|ShardsAllocators
extends|extends
name|AbstractComponent
implements|implements
name|ShardsAllocator
block|{
DECL|field|gatewayAllocator
specifier|private
specifier|final
name|GatewayAllocator
name|gatewayAllocator
decl_stmt|;
DECL|field|allocator
specifier|private
specifier|final
name|ShardsAllocator
name|allocator
decl_stmt|;
DECL|method|ShardsAllocators
specifier|public
name|ShardsAllocators
parameter_list|()
block|{
name|this
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
DECL|method|ShardsAllocators
specifier|public
name|ShardsAllocators
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
argument_list|(
name|settings
argument_list|,
operator|new
name|NoneGatewayAllocator
argument_list|()
argument_list|,
operator|new
name|EvenShardsCountAllocator
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|ShardsAllocators
specifier|public
name|ShardsAllocators
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|GatewayAllocator
name|gatewayAllocator
parameter_list|,
name|ShardsAllocator
name|allocator
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|gatewayAllocator
operator|=
name|gatewayAllocator
expr_stmt|;
name|this
operator|.
name|allocator
operator|=
name|allocator
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|applyStartedShards
specifier|public
name|void
name|applyStartedShards
parameter_list|(
name|StartedRerouteAllocation
name|allocation
parameter_list|)
block|{
name|gatewayAllocator
operator|.
name|applyStartedShards
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
name|allocator
operator|.
name|applyStartedShards
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|applyFailedShards
specifier|public
name|void
name|applyFailedShards
parameter_list|(
name|FailedRerouteAllocation
name|allocation
parameter_list|)
block|{
name|gatewayAllocator
operator|.
name|applyFailedShards
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
name|allocator
operator|.
name|applyFailedShards
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|allocateUnassigned
specifier|public
name|boolean
name|allocateUnassigned
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
name|changed
operator||=
name|gatewayAllocator
operator|.
name|allocateUnassigned
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
name|changed
operator||=
name|allocator
operator|.
name|allocateUnassigned
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
return|return
name|changed
return|;
block|}
annotation|@
name|Override
DECL|method|rebalance
specifier|public
name|boolean
name|rebalance
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
return|return
name|allocator
operator|.
name|rebalance
argument_list|(
name|allocation
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|move
specifier|public
name|boolean
name|move
parameter_list|(
name|MutableShardRouting
name|shardRouting
parameter_list|,
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
return|return
name|allocator
operator|.
name|move
argument_list|(
name|shardRouting
argument_list|,
name|node
argument_list|,
name|allocation
argument_list|)
return|;
block|}
block|}
end_class

end_unit

