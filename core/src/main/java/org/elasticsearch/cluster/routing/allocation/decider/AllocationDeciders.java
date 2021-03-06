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
name|Settings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|routing
operator|.
name|allocation
operator|.
name|RoutingAllocation
operator|.
name|DebugMode
operator|.
name|EXCLUDE_YES_DECISIONS
import|;
end_import

begin_comment
comment|/**  * A composite {@link AllocationDecider} combining the "decision" of multiple  * {@link AllocationDecider} implementations into a single allocation decision.  */
end_comment

begin_class
DECL|class|AllocationDeciders
specifier|public
class|class
name|AllocationDeciders
extends|extends
name|AllocationDecider
block|{
DECL|field|allocations
specifier|private
specifier|final
name|Collection
argument_list|<
name|AllocationDecider
argument_list|>
name|allocations
decl_stmt|;
DECL|method|AllocationDeciders
specifier|public
name|AllocationDeciders
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Collection
argument_list|<
name|AllocationDecider
argument_list|>
name|allocations
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|allocations
operator|=
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|allocations
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
name|Decision
operator|.
name|Multi
name|ret
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
for|for
control|(
name|AllocationDecider
name|allocationDecider
range|:
name|allocations
control|)
block|{
name|Decision
name|decision
init|=
name|allocationDecider
operator|.
name|canRebalance
argument_list|(
name|shardRouting
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
comment|// short track if a NO is returned.
if|if
condition|(
name|decision
operator|==
name|Decision
operator|.
name|NO
condition|)
block|{
if|if
condition|(
operator|!
name|allocation
operator|.
name|debugDecision
argument_list|()
condition|)
block|{
return|return
name|decision
return|;
block|}
else|else
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|decision
operator|!=
name|Decision
operator|.
name|ALWAYS
operator|&&
operator|(
name|allocation
operator|.
name|getDebugMode
argument_list|()
operator|!=
name|EXCLUDE_YES_DECISIONS
operator|||
name|decision
operator|.
name|type
argument_list|()
operator|!=
name|Decision
operator|.
name|Type
operator|.
name|YES
operator|)
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
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
name|allocation
operator|.
name|shouldIgnoreShardForNode
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|Decision
operator|.
name|NO
return|;
block|}
name|Decision
operator|.
name|Multi
name|ret
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
for|for
control|(
name|AllocationDecider
name|allocationDecider
range|:
name|allocations
control|)
block|{
name|Decision
name|decision
init|=
name|allocationDecider
operator|.
name|canAllocate
argument_list|(
name|shardRouting
argument_list|,
name|node
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
comment|// short track if a NO is returned.
if|if
condition|(
name|decision
operator|==
name|Decision
operator|.
name|NO
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
literal|"Can not allocate [{}] on node [{}] due to [{}]"
argument_list|,
name|shardRouting
argument_list|,
name|node
operator|.
name|node
argument_list|()
argument_list|,
name|allocationDecider
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// short circuit only if debugging is not enabled
if|if
condition|(
operator|!
name|allocation
operator|.
name|debugDecision
argument_list|()
condition|)
block|{
return|return
name|decision
return|;
block|}
else|else
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|decision
operator|!=
name|Decision
operator|.
name|ALWAYS
operator|&&
operator|(
name|allocation
operator|.
name|getDebugMode
argument_list|()
operator|!=
name|EXCLUDE_YES_DECISIONS
operator|||
name|decision
operator|.
name|type
argument_list|()
operator|!=
name|Decision
operator|.
name|Type
operator|.
name|YES
operator|)
condition|)
block|{
comment|// the assumption is that a decider that returns the static instance Decision#ALWAYS
comment|// does not really implements canAllocate
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
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
if|if
condition|(
name|allocation
operator|.
name|shouldIgnoreShardForNode
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
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
literal|"Shard [{}] should be ignored for node [{}]"
argument_list|,
name|shardRouting
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|Decision
operator|.
name|NO
return|;
block|}
name|Decision
operator|.
name|Multi
name|ret
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
for|for
control|(
name|AllocationDecider
name|allocationDecider
range|:
name|allocations
control|)
block|{
name|Decision
name|decision
init|=
name|allocationDecider
operator|.
name|canRemain
argument_list|(
name|shardRouting
argument_list|,
name|node
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
comment|// short track if a NO is returned.
if|if
condition|(
name|decision
operator|==
name|Decision
operator|.
name|NO
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
literal|"Shard [{}] can not remain on node [{}] due to [{}]"
argument_list|,
name|shardRouting
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|,
name|allocationDecider
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|allocation
operator|.
name|debugDecision
argument_list|()
condition|)
block|{
return|return
name|decision
return|;
block|}
else|else
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|decision
operator|!=
name|Decision
operator|.
name|ALWAYS
operator|&&
operator|(
name|allocation
operator|.
name|getDebugMode
argument_list|()
operator|!=
name|EXCLUDE_YES_DECISIONS
operator|||
name|decision
operator|.
name|type
argument_list|()
operator|!=
name|Decision
operator|.
name|Type
operator|.
name|YES
operator|)
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
DECL|method|canAllocate
specifier|public
name|Decision
name|canAllocate
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|,
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
name|Decision
operator|.
name|Multi
name|ret
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
for|for
control|(
name|AllocationDecider
name|allocationDecider
range|:
name|allocations
control|)
block|{
name|Decision
name|decision
init|=
name|allocationDecider
operator|.
name|canAllocate
argument_list|(
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
comment|// short track if a NO is returned.
if|if
condition|(
name|decision
operator|==
name|Decision
operator|.
name|NO
condition|)
block|{
if|if
condition|(
operator|!
name|allocation
operator|.
name|debugDecision
argument_list|()
condition|)
block|{
return|return
name|decision
return|;
block|}
else|else
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|decision
operator|!=
name|Decision
operator|.
name|ALWAYS
operator|&&
operator|(
name|allocation
operator|.
name|getDebugMode
argument_list|()
operator|!=
name|EXCLUDE_YES_DECISIONS
operator|||
name|decision
operator|.
name|type
argument_list|()
operator|!=
name|Decision
operator|.
name|Type
operator|.
name|YES
operator|)
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
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
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
name|Decision
operator|.
name|Multi
name|ret
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
for|for
control|(
name|AllocationDecider
name|allocationDecider
range|:
name|allocations
control|)
block|{
name|Decision
name|decision
init|=
name|allocationDecider
operator|.
name|canAllocate
argument_list|(
name|shardRouting
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
comment|// short track if a NO is returned.
if|if
condition|(
name|decision
operator|==
name|Decision
operator|.
name|NO
condition|)
block|{
if|if
condition|(
operator|!
name|allocation
operator|.
name|debugDecision
argument_list|()
condition|)
block|{
return|return
name|decision
return|;
block|}
else|else
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|decision
operator|!=
name|Decision
operator|.
name|ALWAYS
operator|&&
operator|(
name|allocation
operator|.
name|getDebugMode
argument_list|()
operator|!=
name|EXCLUDE_YES_DECISIONS
operator|||
name|decision
operator|.
name|type
argument_list|()
operator|!=
name|Decision
operator|.
name|Type
operator|.
name|YES
operator|)
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
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
name|Decision
operator|.
name|Multi
name|ret
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
for|for
control|(
name|AllocationDecider
name|allocationDecider
range|:
name|allocations
control|)
block|{
name|Decision
name|decision
init|=
name|allocationDecider
operator|.
name|canAllocate
argument_list|(
name|node
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
comment|// short track if a NO is returned.
if|if
condition|(
name|decision
operator|==
name|Decision
operator|.
name|NO
condition|)
block|{
if|if
condition|(
operator|!
name|allocation
operator|.
name|debugDecision
argument_list|()
condition|)
block|{
return|return
name|decision
return|;
block|}
else|else
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|decision
operator|!=
name|Decision
operator|.
name|ALWAYS
operator|&&
operator|(
name|allocation
operator|.
name|getDebugMode
argument_list|()
operator|!=
name|EXCLUDE_YES_DECISIONS
operator|||
name|decision
operator|.
name|type
argument_list|()
operator|!=
name|Decision
operator|.
name|Type
operator|.
name|YES
operator|)
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
DECL|method|canRebalance
specifier|public
name|Decision
name|canRebalance
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
name|Decision
operator|.
name|Multi
name|ret
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
for|for
control|(
name|AllocationDecider
name|allocationDecider
range|:
name|allocations
control|)
block|{
name|Decision
name|decision
init|=
name|allocationDecider
operator|.
name|canRebalance
argument_list|(
name|allocation
argument_list|)
decl_stmt|;
comment|// short track if a NO is returned.
if|if
condition|(
name|decision
operator|==
name|Decision
operator|.
name|NO
condition|)
block|{
if|if
condition|(
operator|!
name|allocation
operator|.
name|debugDecision
argument_list|()
condition|)
block|{
return|return
name|decision
return|;
block|}
else|else
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|decision
operator|!=
name|Decision
operator|.
name|ALWAYS
operator|&&
operator|(
name|allocation
operator|.
name|getDebugMode
argument_list|()
operator|!=
name|EXCLUDE_YES_DECISIONS
operator|||
name|decision
operator|.
name|type
argument_list|()
operator|!=
name|Decision
operator|.
name|Type
operator|.
name|YES
operator|)
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
DECL|method|canForceAllocatePrimary
specifier|public
name|Decision
name|canForceAllocatePrimary
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
assert|assert
name|shardRouting
operator|.
name|primary
argument_list|()
operator|:
literal|"must not call canForceAllocatePrimary on a non-primary shard routing "
operator|+
name|shardRouting
assert|;
if|if
condition|(
name|allocation
operator|.
name|shouldIgnoreShardForNode
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|Decision
operator|.
name|NO
return|;
block|}
name|Decision
operator|.
name|Multi
name|ret
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
for|for
control|(
name|AllocationDecider
name|decider
range|:
name|allocations
control|)
block|{
name|Decision
name|decision
init|=
name|decider
operator|.
name|canForceAllocatePrimary
argument_list|(
name|shardRouting
argument_list|,
name|node
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
comment|// short track if a NO is returned.
if|if
condition|(
name|decision
operator|==
name|Decision
operator|.
name|NO
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
literal|"Shard [{}] can not be forcefully allocated to node [{}] due to [{}]."
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|,
name|decider
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|allocation
operator|.
name|debugDecision
argument_list|()
condition|)
block|{
return|return
name|decision
return|;
block|}
else|else
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|decision
operator|!=
name|Decision
operator|.
name|ALWAYS
operator|&&
operator|(
name|allocation
operator|.
name|getDebugMode
argument_list|()
operator|!=
name|EXCLUDE_YES_DECISIONS
operator|||
name|decision
operator|.
name|type
argument_list|()
operator|!=
name|Decision
operator|.
name|Type
operator|.
name|YES
operator|)
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

