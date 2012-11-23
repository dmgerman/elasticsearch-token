begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this   * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
package|;
end_package

begin_comment
comment|/**  * Similar to {@link ImmutableShardRouting} this class keeps metadata of the current shard. But unlike  * {@link ImmutableShardRouting} the information kept in this class can be modified.  * These modifications include changing the primary state, relocating and assigning the shard  * represented by this class  */
end_comment

begin_class
DECL|class|MutableShardRouting
specifier|public
class|class
name|MutableShardRouting
extends|extends
name|ImmutableShardRouting
block|{
DECL|method|MutableShardRouting
specifier|public
name|MutableShardRouting
parameter_list|(
name|ShardRouting
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
expr_stmt|;
block|}
DECL|method|MutableShardRouting
specifier|public
name|MutableShardRouting
parameter_list|(
name|ShardRouting
name|copy
parameter_list|,
name|long
name|version
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
DECL|method|MutableShardRouting
specifier|public
name|MutableShardRouting
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|String
name|currentNodeId
parameter_list|,
name|boolean
name|primary
parameter_list|,
name|ShardRoutingState
name|state
parameter_list|,
name|long
name|version
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|,
name|currentNodeId
argument_list|,
name|primary
argument_list|,
name|state
argument_list|,
name|version
argument_list|)
expr_stmt|;
block|}
DECL|method|MutableShardRouting
specifier|public
name|MutableShardRouting
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|String
name|currentNodeId
parameter_list|,
name|String
name|relocatingNodeId
parameter_list|,
name|boolean
name|primary
parameter_list|,
name|ShardRoutingState
name|state
parameter_list|,
name|long
name|version
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|,
name|currentNodeId
argument_list|,
name|relocatingNodeId
argument_list|,
name|primary
argument_list|,
name|state
argument_list|,
name|version
argument_list|)
expr_stmt|;
block|}
comment|/**      * Assign this shard to a node.      *       * @param nodeId id of the node to assign this shard to      */
DECL|method|assignToNode
specifier|public
name|void
name|assignToNode
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
name|version
operator|++
expr_stmt|;
if|if
condition|(
name|currentNodeId
operator|==
literal|null
condition|)
block|{
assert|assert
name|state
operator|==
name|ShardRoutingState
operator|.
name|UNASSIGNED
assert|;
name|state
operator|=
name|ShardRoutingState
operator|.
name|INITIALIZING
expr_stmt|;
name|currentNodeId
operator|=
name|nodeId
expr_stmt|;
name|relocatingNodeId
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|state
operator|==
name|ShardRoutingState
operator|.
name|STARTED
condition|)
block|{
name|state
operator|=
name|ShardRoutingState
operator|.
name|RELOCATING
expr_stmt|;
name|relocatingNodeId
operator|=
name|nodeId
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|state
operator|==
name|ShardRoutingState
operator|.
name|RELOCATING
condition|)
block|{
assert|assert
name|nodeId
operator|.
name|equals
argument_list|(
name|relocatingNodeId
argument_list|)
assert|;
block|}
block|}
comment|/**      * Relocate the shard to another node.      *       * @param relocatingNodeId id of the node to relocate the shard      */
DECL|method|relocate
specifier|public
name|void
name|relocate
parameter_list|(
name|String
name|relocatingNodeId
parameter_list|)
block|{
name|version
operator|++
expr_stmt|;
assert|assert
name|state
operator|==
name|ShardRoutingState
operator|.
name|STARTED
assert|;
name|state
operator|=
name|ShardRoutingState
operator|.
name|RELOCATING
expr_stmt|;
name|this
operator|.
name|relocatingNodeId
operator|=
name|relocatingNodeId
expr_stmt|;
block|}
comment|/**      * Cancel relocation of a shard. The shards state must be set      * to<code>RELOCATING</code>.      */
DECL|method|cancelRelocation
specifier|public
name|void
name|cancelRelocation
parameter_list|()
block|{
name|version
operator|++
expr_stmt|;
assert|assert
name|state
operator|==
name|ShardRoutingState
operator|.
name|RELOCATING
assert|;
assert|assert
name|assignedToNode
argument_list|()
assert|;
assert|assert
name|relocatingNodeId
operator|!=
literal|null
assert|;
name|state
operator|=
name|ShardRoutingState
operator|.
name|STARTED
expr_stmt|;
name|relocatingNodeId
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Set the shards state to<code>UNASSIGNED</code>.      * //TODO document the state      */
DECL|method|deassignNode
specifier|public
name|void
name|deassignNode
parameter_list|()
block|{
name|version
operator|++
expr_stmt|;
assert|assert
name|state
operator|!=
name|ShardRoutingState
operator|.
name|UNASSIGNED
assert|;
name|state
operator|=
name|ShardRoutingState
operator|.
name|UNASSIGNED
expr_stmt|;
name|this
operator|.
name|currentNodeId
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|relocatingNodeId
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Set the shards state to<code>STARTED</code>. The shards state must be      *<code>INITIALIZING</code> or<code>RELOCATING</code>. Any relocation will be      * canceled.       */
DECL|method|moveToStarted
specifier|public
name|void
name|moveToStarted
parameter_list|()
block|{
name|version
operator|++
expr_stmt|;
assert|assert
name|state
operator|==
name|ShardRoutingState
operator|.
name|INITIALIZING
operator|||
name|state
operator|==
name|ShardRoutingState
operator|.
name|RELOCATING
assert|;
name|relocatingNodeId
operator|=
literal|null
expr_stmt|;
name|state
operator|=
name|ShardRoutingState
operator|.
name|STARTED
expr_stmt|;
block|}
comment|/**      * Make the shard primary unless it's not Primary      * //TODO: doc exception      */
DECL|method|moveToPrimary
specifier|public
name|void
name|moveToPrimary
parameter_list|()
block|{
name|version
operator|++
expr_stmt|;
if|if
condition|(
name|primary
condition|)
block|{
throw|throw
operator|new
name|IllegalShardRoutingStateException
argument_list|(
name|this
argument_list|,
literal|"Already primary, can't move to primary"
argument_list|)
throw|;
block|}
name|primary
operator|=
literal|true
expr_stmt|;
block|}
comment|/**      * Set the primary shard to non-primary      */
DECL|method|moveFromPrimary
specifier|public
name|void
name|moveFromPrimary
parameter_list|()
block|{
name|version
operator|++
expr_stmt|;
if|if
condition|(
operator|!
name|primary
condition|)
block|{
throw|throw
operator|new
name|IllegalShardRoutingStateException
argument_list|(
name|this
argument_list|,
literal|"Not primary, can't move to replica"
argument_list|)
throw|;
block|}
name|primary
operator|=
literal|false
expr_stmt|;
block|}
block|}
end_class

end_unit

