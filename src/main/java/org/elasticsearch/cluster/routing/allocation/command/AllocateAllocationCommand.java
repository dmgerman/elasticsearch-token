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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_comment
comment|/**  * Allocates an unassigned shard to a specific node. Note, primary allocation will "force"  * allocation which might mean one will loose data if using local gateway..., use with care  * with the<tt>allowPrimary</tt> flag.  */
end_comment

begin_class
DECL|class|AllocateAllocationCommand
specifier|public
class|class
name|AllocateAllocationCommand
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
literal|"allocate"
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
name|AllocateAllocationCommand
argument_list|>
block|{
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|AllocateAllocationCommand
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
name|AllocateAllocationCommand
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
name|readBoolean
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
name|AllocateAllocationCommand
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
name|node
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|command
operator|.
name|allowPrimary
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|AllocateAllocationCommand
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
name|nodeId
init|=
literal|null
decl_stmt|;
name|boolean
name|allowPrimary
init|=
literal|false
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
literal|"node"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|nodeId
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
literal|"allow_primary"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"allowPrimary"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|allowPrimary
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"[allocate] command does not support field ["
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
literal|"[allocate] command does not support complex json tokens ["
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
literal|"[allocate] command missing the index parameter"
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
literal|"[allocate] command missing the shard parameter"
argument_list|)
throw|;
block|}
if|if
condition|(
name|nodeId
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"[allocate] command missing the node parameter"
argument_list|)
throw|;
block|}
return|return
operator|new
name|AllocateAllocationCommand
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
argument_list|,
name|nodeId
argument_list|,
name|allowPrimary
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
name|AllocateAllocationCommand
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
literal|"node"
argument_list|,
name|command
operator|.
name|node
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"allow_primary"
argument_list|,
name|command
operator|.
name|allowPrimary
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
DECL|field|node
specifier|private
specifier|final
name|String
name|node
decl_stmt|;
DECL|field|allowPrimary
specifier|private
specifier|final
name|boolean
name|allowPrimary
decl_stmt|;
DECL|method|AllocateAllocationCommand
specifier|public
name|AllocateAllocationCommand
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|node
parameter_list|,
name|boolean
name|allowPrimary
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
name|node
operator|=
name|node
expr_stmt|;
name|this
operator|.
name|allowPrimary
operator|=
name|allowPrimary
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
DECL|method|node
specifier|public
name|String
name|node
parameter_list|()
block|{
return|return
name|this
operator|.
name|node
return|;
block|}
DECL|method|allowPrimary
specifier|public
name|boolean
name|allowPrimary
parameter_list|()
block|{
return|return
name|this
operator|.
name|allowPrimary
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
name|discoNode
init|=
name|allocation
operator|.
name|nodes
argument_list|()
operator|.
name|resolveNode
argument_list|(
name|node
argument_list|)
decl_stmt|;
name|MutableShardRouting
name|shardRouting
init|=
literal|null
decl_stmt|;
for|for
control|(
name|MutableShardRouting
name|routing
range|:
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|unassigned
argument_list|()
control|)
block|{
if|if
condition|(
name|routing
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
comment|// prefer primaries first to allocate
if|if
condition|(
name|shardRouting
operator|==
literal|null
operator|||
name|routing
operator|.
name|primary
argument_list|()
condition|)
block|{
name|shardRouting
operator|=
name|routing
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|shardRouting
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"[allocate] failed to find "
operator|+
name|shardId
operator|+
literal|" on the list of unassigned shards"
argument_list|)
throw|;
block|}
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
operator|&&
operator|!
name|allowPrimary
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"[allocate] trying to allocate a primary shard "
operator|+
name|shardId
operator|+
literal|"], which is disabled"
argument_list|)
throw|;
block|}
name|RoutingNode
name|routingNode
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|discoNode
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
name|routingNode
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
literal|"[allocate] allocation of "
operator|+
name|shardId
operator|+
literal|" on node "
operator|+
name|discoNode
operator|+
literal|" is not allowed, reason: "
operator|+
name|decision
argument_list|)
throw|;
block|}
comment|// go over and remove it from the unassigned
for|for
control|(
name|Iterator
argument_list|<
name|MutableShardRouting
argument_list|>
name|it
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|unassigned
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
if|if
condition|(
name|it
operator|.
name|next
argument_list|()
operator|!=
name|shardRouting
condition|)
block|{
continue|continue;
block|}
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
name|routingNode
operator|.
name|add
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
end_class

end_unit

