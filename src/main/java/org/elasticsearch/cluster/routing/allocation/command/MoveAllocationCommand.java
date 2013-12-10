begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchParseException
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
name|ShardRoutingState
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|ToXContent
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
name|XContentBuilder
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
name|shard
operator|.
name|ShardId
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
comment|/**  * A command that moves a shard from a specific node to another node.<br />  *<b>Note:</b> The shard needs to be in the state  * {@link ShardRoutingState#STARTED} in order to be moved.  */
end_comment

begin_class
DECL|class|MoveAllocationCommand
specifier|public
class|class
name|MoveAllocationCommand
implements|implements
name|AllocationCommand
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"move"
decl_stmt|;
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
implements|implements
name|AllocationCommand
operator|.
name|Factory
argument_list|<
name|MoveAllocationCommand
argument_list|>
block|{
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|MoveAllocationCommand
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|MoveAllocationCommand
argument_list|(
name|ShardId
operator|.
name|readShardId
argument_list|(
name|in
argument_list|)
argument_list|,
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|in
operator|.
name|readString
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|MoveAllocationCommand
name|command
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|command
operator|.
name|shardId
argument_list|()
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|command
operator|.
name|fromNode
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|command
operator|.
name|toNode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|MoveAllocationCommand
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|index
init|=
literal|null
decl_stmt|;
name|int
name|shardId
init|=
operator|-
literal|1
decl_stmt|;
name|String
name|fromNode
init|=
literal|null
decl_stmt|;
name|String
name|toNode
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"index"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|index
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"shard"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|shardId
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"from_node"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"fromNode"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|fromNode
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"to_node"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"toNode"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|toNode
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"[move] command does not support field ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"[move] command does not support complex json tokens ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|index
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"[move] command missing the index parameter"
argument_list|)
throw|;
block|}
if|if
condition|(
name|shardId
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"[move] command missing the shard parameter"
argument_list|)
throw|;
block|}
if|if
condition|(
name|fromNode
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"[move] command missing the from_node parameter"
argument_list|)
throw|;
block|}
if|if
condition|(
name|toNode
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"[move] command missing the to_node parameter"
argument_list|)
throw|;
block|}
return|return
operator|new
name|MoveAllocationCommand
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
argument_list|,
name|fromNode
argument_list|,
name|toNode
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|void
name|toXContent
parameter_list|(
name|MoveAllocationCommand
name|command
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|ToXContent
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
name|command
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"shard"
argument_list|,
name|command
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"from_node"
argument_list|,
name|command
operator|.
name|fromNode
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"to_node"
argument_list|,
name|command
operator|.
name|toNode
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|fromNode
specifier|private
specifier|final
name|String
name|fromNode
decl_stmt|;
DECL|field|toNode
specifier|private
specifier|final
name|String
name|toNode
decl_stmt|;
DECL|method|MoveAllocationCommand
specifier|public
name|MoveAllocationCommand
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|fromNode
parameter_list|,
name|String
name|toNode
parameter_list|)
block|{
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|fromNode
operator|=
name|fromNode
expr_stmt|;
name|this
operator|.
name|toNode
operator|=
name|toNode
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
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardId
return|;
block|}
DECL|method|fromNode
specifier|public
name|String
name|fromNode
parameter_list|()
block|{
return|return
name|this
operator|.
name|fromNode
return|;
block|}
DECL|method|toNode
specifier|public
name|String
name|toNode
parameter_list|()
block|{
return|return
name|this
operator|.
name|toNode
return|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|DiscoveryNode
name|fromDiscoNode
init|=
name|allocation
operator|.
name|nodes
argument_list|()
operator|.
name|resolveNode
argument_list|(
name|fromNode
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|toDiscoNode
init|=
name|allocation
operator|.
name|nodes
argument_list|()
operator|.
name|resolveNode
argument_list|(
name|toNode
argument_list|)
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|MutableShardRouting
name|shardRouting
range|:
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|fromDiscoNode
operator|.
name|id
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|shardId
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|found
operator|=
literal|true
expr_stmt|;
comment|// TODO we can possibly support also relocating cases, where we cancel relocation and move...
if|if
condition|(
operator|!
name|shardRouting
operator|.
name|started
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"[move_allocation] can't move "
operator|+
name|shardId
operator|+
literal|", shard is not started (state = "
operator|+
name|shardRouting
operator|.
name|state
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|RoutingNode
name|toRoutingNode
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|toDiscoNode
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|Decision
name|decision
init|=
name|allocation
operator|.
name|deciders
argument_list|()
operator|.
name|canAllocate
argument_list|(
name|shardRouting
argument_list|,
name|toRoutingNode
argument_list|,
name|allocation
argument_list|)
decl_stmt|;
if|if
condition|(
name|decision
operator|.
name|type
argument_list|()
operator|==
name|Decision
operator|.
name|Type
operator|.
name|NO
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"[move_allocation] can't move "
operator|+
name|shardId
operator|+
literal|", from "
operator|+
name|fromDiscoNode
operator|+
literal|", to "
operator|+
name|toDiscoNode
operator|+
literal|", since its not allowed, reason: "
operator|+
name|decision
argument_list|)
throw|;
block|}
if|if
condition|(
name|decision
operator|.
name|type
argument_list|()
operator|==
name|Decision
operator|.
name|Type
operator|.
name|THROTTLE
condition|)
block|{
comment|// its being throttled, maybe have a flag to take it into account and fail? for now, just do it since the "user" wants it...
block|}
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|assign
argument_list|(
operator|new
name|MutableShardRouting
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|id
argument_list|()
argument_list|,
name|toRoutingNode
operator|.
name|nodeId
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|restoreSource
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|,
name|shardRouting
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|,
name|toRoutingNode
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|relocate
argument_list|(
name|shardRouting
argument_list|,
name|toRoutingNode
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"[move_allocation] can't move "
operator|+
name|shardId
operator|+
literal|", failed to find it on node "
operator|+
name|fromDiscoNode
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

