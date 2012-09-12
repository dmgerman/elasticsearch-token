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
comment|/**  * A pluggable logic allowing to control if allocation of a shard is allowed on a specific node.  */
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
DECL|enum|Decision
specifier|public
specifier|static
enum|enum
name|Decision
block|{
DECL|enum constant|YES
name|YES
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|allocate
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|allowed
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
block|,
DECL|enum constant|NO
name|NO
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|allocate
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|allowed
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|,
DECL|enum constant|THROTTLE
name|THROTTLE
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|allocate
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|allowed
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
block|;
comment|/**          * It can be allocated *now* on a node. Note, it might be {@link #allowed()} to be allocated          * on a node, yet, allocate will be<tt>false</tt> since its being throttled for example.          */
DECL|method|allocate
specifier|public
specifier|abstract
name|boolean
name|allocate
parameter_list|()
function_decl|;
comment|/**          * Is allocation allowed on a node. Note, this does not mean that we should allocate *now*,          * though, in extreme cases, we might "force" allocation.          */
DECL|method|allowed
specifier|public
specifier|abstract
name|boolean
name|allowed
parameter_list|()
function_decl|;
block|}
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
DECL|method|canRebalance
specifier|public
name|boolean
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
literal|true
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
name|YES
return|;
block|}
comment|/**      * Can the provided shard routing remain on the node?      */
DECL|method|canRemain
specifier|public
name|boolean
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
literal|true
return|;
block|}
block|}
end_class

end_unit

