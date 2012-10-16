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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ShardsLimitAllocationDecider
specifier|public
class|class
name|ShardsLimitAllocationDecider
extends|extends
name|AllocationDecider
block|{
DECL|field|INDEX_TOTAL_SHARDS_PER_NODE
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_TOTAL_SHARDS_PER_NODE
init|=
literal|"index.routing.allocation.total_shards_per_node"
decl_stmt|;
static|static
block|{
name|IndexMetaData
operator|.
name|addDynamicSettings
argument_list|(
name|INDEX_TOTAL_SHARDS_PER_NODE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|ShardsLimitAllocationDecider
specifier|public
name|ShardsLimitAllocationDecider
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
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
name|int
name|totalShardsPerNode
init|=
name|indexMd
operator|.
name|settings
argument_list|()
operator|.
name|getAsInt
argument_list|(
name|INDEX_TOTAL_SHARDS_PER_NODE
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|totalShardsPerNode
operator|<=
literal|0
condition|)
block|{
return|return
name|Decision
operator|.
name|YES
return|;
block|}
name|int
name|nodeCount
init|=
literal|0
decl_stmt|;
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
name|MutableShardRouting
name|nodeShard
init|=
name|shards
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
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
continue|continue;
block|}
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
name|nodeCount
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|nodeCount
operator|>=
name|totalShardsPerNode
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
name|int
name|totalShardsPerNode
init|=
name|indexMd
operator|.
name|settings
argument_list|()
operator|.
name|getAsInt
argument_list|(
name|INDEX_TOTAL_SHARDS_PER_NODE
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|totalShardsPerNode
operator|<=
literal|0
condition|)
block|{
return|return
name|Decision
operator|.
name|YES
return|;
block|}
name|int
name|nodeCount
init|=
literal|0
decl_stmt|;
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
name|MutableShardRouting
name|nodeShard
init|=
name|shards
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
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
continue|continue;
block|}
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
name|nodeCount
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|nodeCount
operator|>
name|totalShardsPerNode
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

