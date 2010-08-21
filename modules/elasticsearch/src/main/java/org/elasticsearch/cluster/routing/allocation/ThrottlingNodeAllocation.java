begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation
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
name|*
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ThrottlingNodeAllocation
specifier|public
class|class
name|ThrottlingNodeAllocation
extends|extends
name|AbstractComponent
implements|implements
name|NodeAllocation
block|{
DECL|field|concurrentRecoveries
specifier|private
specifier|final
name|int
name|concurrentRecoveries
decl_stmt|;
DECL|method|ThrottlingNodeAllocation
annotation|@
name|Inject
specifier|public
name|ThrottlingNodeAllocation
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
name|this
operator|.
name|concurrentRecoveries
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"concurrent_recoveries"
argument_list|,
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|allocate
annotation|@
name|Override
specifier|public
name|boolean
name|allocate
parameter_list|(
name|RoutingNodes
name|routingNodes
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|canAllocate
annotation|@
name|Override
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
name|RoutingNodes
name|routingNodes
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
name|boolean
name|primaryUnassigned
init|=
literal|false
decl_stmt|;
for|for
control|(
name|MutableShardRouting
name|shard
range|:
name|routingNodes
operator|.
name|unassigned
argument_list|()
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
condition|)
block|{
name|primaryUnassigned
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
name|primaryUnassigned
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
name|concurrentRecoveries
condition|)
block|{
return|return
name|Decision
operator|.
name|THROTTLE
return|;
block|}
else|else
block|{
return|return
name|Decision
operator|.
name|YES
return|;
block|}
block|}
block|}
comment|// either primary or replica doing recovery (from peer shard)
comment|// count the number of recoveries on the node, its for both target (INITIALIZING) and source (RELOCATING)
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
name|Decision
operator|.
name|THROTTLE
return|;
block|}
else|else
block|{
return|return
name|Decision
operator|.
name|YES
return|;
block|}
block|}
block|}
end_class

end_unit

