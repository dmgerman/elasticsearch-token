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
name|settings
operator|.
name|Settings
import|;
end_import

begin_comment
comment|/**  * {@link AllocationDecider} is an abstract base class that allows to make  * dynamic cluster- or index-wide shard allocation decisions on a per-node  * basis.  */
end_comment

begin_class
DECL|class|AllocationDecider
specifier|public
specifier|abstract
class|class
name|AllocationDecider
extends|extends
name|AbstractComponent
block|{
comment|/**      * Initializes a new {@link AllocationDecider}      * @param settings {@link Settings} used by this {@link AllocationDecider}      */
DECL|method|AllocationDecider
specifier|protected
name|AllocationDecider
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
comment|/**      * Returns a {@link Decision} whether the given shard routing can be      * re-balanced to the given allocation. The default is      * {@link Decision#ALWAYS}.      */
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
return|return
name|Decision
operator|.
name|ALWAYS
return|;
block|}
comment|/**      * Returns a {@link Decision} whether the given shard routing can be      * allocated on the given node. The default is {@link Decision#ALWAYS}.      */
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
name|Decision
operator|.
name|ALWAYS
return|;
block|}
comment|/**      * Returns a {@link Decision} whether the given shard routing can be remain      * on the given node. The default is {@link Decision#ALWAYS}.      */
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
return|return
name|Decision
operator|.
name|ALWAYS
return|;
block|}
comment|/**      * Returns a {@link Decision} whether the given shard routing can be allocated at all at this state of the      * {@link RoutingAllocation}. The default is {@link Decision#ALWAYS}.      */
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
return|return
name|Decision
operator|.
name|ALWAYS
return|;
block|}
comment|/**      * Returns a {@link Decision} whether the given node can allow any allocation at all at this state of the      * {@link RoutingAllocation}. The default is {@link Decision#ALWAYS}.      */
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
return|return
name|Decision
operator|.
name|ALWAYS
return|;
block|}
block|}
end_class

end_unit

