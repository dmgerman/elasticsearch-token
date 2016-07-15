begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation.command
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
name|command
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
name|DiscoveryNode
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
name|RoutingNodes
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
name|UnassignedInfo
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
name|RerouteExplanation
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
name|decider
operator|.
name|Decision
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
name|ParseField
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
name|ParseFieldMatcher
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
name|ParseFieldMatcherSupplier
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|xcontent
operator|.
name|ObjectParser
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
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|IndexNotFoundException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Allocates an unassigned empty primary shard to a specific node. Use with extreme care as this will result in data loss.  * Allocation deciders are ignored.  */
end_comment

begin_class
DECL|class|AllocateEmptyPrimaryAllocationCommand
specifier|public
class|class
name|AllocateEmptyPrimaryAllocationCommand
extends|extends
name|BasePrimaryAllocationCommand
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"allocate_empty_primary"
decl_stmt|;
DECL|field|COMMAND_NAME_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|COMMAND_NAME_FIELD
init|=
operator|new
name|ParseField
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
DECL|field|EMPTY_PRIMARY_PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|Builder
argument_list|,
name|ParseFieldMatcherSupplier
argument_list|>
name|EMPTY_PRIMARY_PARSER
init|=
name|BasePrimaryAllocationCommand
operator|.
name|createAllocatePrimaryParser
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
comment|/**      * Creates a new {@link AllocateEmptyPrimaryAllocationCommand}      *      * @param shardId        {@link ShardId} of the shard to assign      * @param node           node id of the node to assign the shard to      * @param acceptDataLoss whether the user agrees to data loss      */
DECL|method|AllocateEmptyPrimaryAllocationCommand
specifier|public
name|AllocateEmptyPrimaryAllocationCommand
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|String
name|node
parameter_list|,
name|boolean
name|acceptDataLoss
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|,
name|node
argument_list|,
name|acceptDataLoss
argument_list|)
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|AllocateEmptyPrimaryAllocationCommand
specifier|public
name|AllocateEmptyPrimaryAllocationCommand
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|AllocateEmptyPrimaryAllocationCommand
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Builder
argument_list|()
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|BasePrimaryAllocationCommand
operator|.
name|Builder
argument_list|<
name|AllocateEmptyPrimaryAllocationCommand
argument_list|>
block|{
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Builder
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|EMPTY_PRIMARY_PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|this
argument_list|,
parameter_list|()
lambda|->
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|AllocateEmptyPrimaryAllocationCommand
name|build
parameter_list|()
block|{
name|validate
argument_list|()
expr_stmt|;
return|return
operator|new
name|AllocateEmptyPrimaryAllocationCommand
argument_list|(
name|index
argument_list|,
name|shard
argument_list|,
name|node
argument_list|,
name|acceptDataLoss
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|RerouteExplanation
name|execute
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|,
name|boolean
name|explain
parameter_list|)
block|{
specifier|final
name|DiscoveryNode
name|discoNode
decl_stmt|;
try|try
block|{
name|discoNode
operator|=
name|allocation
operator|.
name|nodes
argument_list|()
operator|.
name|resolveNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
return|return
name|explainOrThrowRejectedCommand
argument_list|(
name|explain
argument_list|,
name|allocation
argument_list|,
name|e
argument_list|)
return|;
block|}
specifier|final
name|RoutingNodes
name|routingNodes
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
decl_stmt|;
name|RoutingNode
name|routingNode
init|=
name|routingNodes
operator|.
name|node
argument_list|(
name|discoNode
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|routingNode
operator|==
literal|null
condition|)
block|{
return|return
name|explainOrThrowMissingRoutingNode
argument_list|(
name|allocation
argument_list|,
name|explain
argument_list|,
name|discoNode
argument_list|)
return|;
block|}
specifier|final
name|ShardRouting
name|shardRouting
decl_stmt|;
try|try
block|{
name|shardRouting
operator|=
name|allocation
operator|.
name|routingTable
argument_list|()
operator|.
name|shardRoutingTable
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
operator|.
name|primaryShard
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexNotFoundException
decl||
name|ShardNotFoundException
name|e
parameter_list|)
block|{
return|return
name|explainOrThrowRejectedCommand
argument_list|(
name|explain
argument_list|,
name|allocation
argument_list|,
name|e
argument_list|)
return|;
block|}
if|if
condition|(
name|shardRouting
operator|.
name|unassigned
argument_list|()
operator|==
literal|false
condition|)
block|{
return|return
name|explainOrThrowRejectedCommand
argument_list|(
name|explain
argument_list|,
name|allocation
argument_list|,
literal|"primary ["
operator|+
name|index
operator|+
literal|"]["
operator|+
name|shardId
operator|+
literal|"] is already assigned"
argument_list|)
return|;
block|}
if|if
condition|(
name|shardRouting
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getReason
argument_list|()
operator|!=
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
operator|&&
name|acceptDataLoss
operator|==
literal|false
condition|)
block|{
return|return
name|explainOrThrowRejectedCommand
argument_list|(
name|explain
argument_list|,
name|allocation
argument_list|,
literal|"allocating an empty primary for ["
operator|+
name|index
operator|+
literal|"]["
operator|+
name|shardId
operator|+
literal|"] can result in data loss. Please confirm by setting the accept_data_loss parameter to true"
argument_list|)
return|;
block|}
name|UnassignedInfo
name|unassignedInfoToUpdate
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getReason
argument_list|()
operator|!=
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
condition|)
block|{
comment|// we need to move the unassigned info back to treat it as if it was index creation
name|unassignedInfoToUpdate
operator|=
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"force empty allocation from previous reason "
operator|+
name|shardRouting
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getReason
argument_list|()
operator|+
literal|", "
operator|+
name|shardRouting
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getFailure
argument_list|()
argument_list|,
literal|0
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|false
argument_list|,
name|shardRouting
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getLastAllocationStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|initializeUnassignedShard
argument_list|(
name|allocation
argument_list|,
name|routingNodes
argument_list|,
name|routingNode
argument_list|,
name|shardRouting
argument_list|,
name|unassignedInfoToUpdate
argument_list|)
expr_stmt|;
return|return
operator|new
name|RerouteExplanation
argument_list|(
name|this
argument_list|,
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|name
argument_list|()
operator|+
literal|" (allocation command)"
argument_list|,
literal|"ignore deciders"
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

