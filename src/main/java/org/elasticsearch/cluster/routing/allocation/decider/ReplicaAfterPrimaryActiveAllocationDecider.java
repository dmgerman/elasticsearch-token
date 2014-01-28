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

begin_comment
comment|/**  * An allocation strategy that only allows for a replica to be allocated when the primary is active.  */
end_comment

begin_class
DECL|class|ReplicaAfterPrimaryActiveAllocationDecider
specifier|public
class|class
name|ReplicaAfterPrimaryActiveAllocationDecider
extends|extends
name|AllocationDecider
block|{
annotation|@
name|Inject
DECL|method|ReplicaAfterPrimaryActiveAllocationDecider
specifier|public
name|ReplicaAfterPrimaryActiveAllocationDecider
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
return|return
name|canAllocate
argument_list|(
name|shardRouting
argument_list|,
name|allocation
argument_list|)
return|;
block|}
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
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
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
literal|"shard is primary"
argument_list|)
return|;
block|}
name|MutableShardRouting
name|primary
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|activePrimary
argument_list|(
name|shardRouting
argument_list|)
decl_stmt|;
if|if
condition|(
name|primary
operator|==
literal|null
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
literal|"primary shard is not yet active"
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
literal|"primary is already active"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

