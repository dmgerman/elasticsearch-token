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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * {@link ImmutableShardRouting} immutably encapsulates information about shard  * routings like id, state, version, etc.  */
end_comment

begin_class
DECL|class|ImmutableShardRouting
specifier|public
class|class
name|ImmutableShardRouting
implements|implements
name|Streamable
implements|,
name|Serializable
implements|,
name|ShardRouting
block|{
DECL|field|index
specifier|protected
name|String
name|index
decl_stmt|;
DECL|field|shardId
specifier|protected
name|int
name|shardId
decl_stmt|;
DECL|field|currentNodeId
specifier|protected
name|String
name|currentNodeId
decl_stmt|;
DECL|field|relocatingNodeId
specifier|protected
name|String
name|relocatingNodeId
decl_stmt|;
DECL|field|primary
specifier|protected
name|boolean
name|primary
decl_stmt|;
DECL|field|state
specifier|protected
name|ShardRoutingState
name|state
decl_stmt|;
DECL|field|version
specifier|protected
name|long
name|version
decl_stmt|;
DECL|field|shardIdentifier
specifier|private
specifier|transient
name|ShardId
name|shardIdentifier
decl_stmt|;
DECL|field|asList
specifier|private
specifier|final
specifier|transient
name|ImmutableList
argument_list|<
name|ShardRouting
argument_list|>
name|asList
decl_stmt|;
DECL|method|ImmutableShardRouting
name|ImmutableShardRouting
parameter_list|()
block|{
name|this
operator|.
name|asList
operator|=
name|ImmutableList
operator|.
name|of
argument_list|(
operator|(
name|ShardRouting
operator|)
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|ImmutableShardRouting
specifier|public
name|ImmutableShardRouting
parameter_list|(
name|ShardRouting
name|copy
parameter_list|)
block|{
name|this
argument_list|(
name|copy
operator|.
name|index
argument_list|()
argument_list|,
name|copy
operator|.
name|id
argument_list|()
argument_list|,
name|copy
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|copy
operator|.
name|primary
argument_list|()
argument_list|,
name|copy
operator|.
name|state
argument_list|()
argument_list|,
name|copy
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|relocatingNodeId
operator|=
name|copy
operator|.
name|relocatingNodeId
argument_list|()
expr_stmt|;
block|}
DECL|method|ImmutableShardRouting
specifier|public
name|ImmutableShardRouting
parameter_list|(
name|ShardRouting
name|copy
parameter_list|,
name|long
name|version
parameter_list|)
block|{
name|this
argument_list|(
name|copy
operator|.
name|index
argument_list|()
argument_list|,
name|copy
operator|.
name|id
argument_list|()
argument_list|,
name|copy
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|copy
operator|.
name|primary
argument_list|()
argument_list|,
name|copy
operator|.
name|state
argument_list|()
argument_list|,
name|copy
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|relocatingNodeId
operator|=
name|copy
operator|.
name|relocatingNodeId
argument_list|()
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
DECL|method|ImmutableShardRouting
specifier|public
name|ImmutableShardRouting
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
name|this
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
name|this
operator|.
name|relocatingNodeId
operator|=
name|relocatingNodeId
expr_stmt|;
block|}
DECL|method|ImmutableShardRouting
specifier|public
name|ImmutableShardRouting
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
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|currentNodeId
operator|=
name|currentNodeId
expr_stmt|;
name|this
operator|.
name|primary
operator|=
name|primary
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|asList
operator|=
name|ImmutableList
operator|.
name|of
argument_list|(
operator|(
name|ShardRouting
operator|)
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
return|;
block|}
annotation|@
name|Override
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|index
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|id
specifier|public
name|int
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardId
return|;
block|}
annotation|@
name|Override
DECL|method|getId
specifier|public
name|int
name|getId
parameter_list|()
block|{
return|return
name|id
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|version
specifier|public
name|long
name|version
parameter_list|()
block|{
return|return
name|this
operator|.
name|version
return|;
block|}
annotation|@
name|Override
DECL|method|unassigned
specifier|public
name|boolean
name|unassigned
parameter_list|()
block|{
return|return
name|state
operator|==
name|ShardRoutingState
operator|.
name|UNASSIGNED
return|;
block|}
annotation|@
name|Override
DECL|method|initializing
specifier|public
name|boolean
name|initializing
parameter_list|()
block|{
return|return
name|state
operator|==
name|ShardRoutingState
operator|.
name|INITIALIZING
return|;
block|}
annotation|@
name|Override
DECL|method|active
specifier|public
name|boolean
name|active
parameter_list|()
block|{
return|return
name|started
argument_list|()
operator|||
name|relocating
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|started
specifier|public
name|boolean
name|started
parameter_list|()
block|{
return|return
name|state
operator|==
name|ShardRoutingState
operator|.
name|STARTED
return|;
block|}
annotation|@
name|Override
DECL|method|relocating
specifier|public
name|boolean
name|relocating
parameter_list|()
block|{
return|return
name|state
operator|==
name|ShardRoutingState
operator|.
name|RELOCATING
return|;
block|}
annotation|@
name|Override
DECL|method|assignedToNode
specifier|public
name|boolean
name|assignedToNode
parameter_list|()
block|{
return|return
name|currentNodeId
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|currentNodeId
specifier|public
name|String
name|currentNodeId
parameter_list|()
block|{
return|return
name|this
operator|.
name|currentNodeId
return|;
block|}
annotation|@
name|Override
DECL|method|relocatingNodeId
specifier|public
name|String
name|relocatingNodeId
parameter_list|()
block|{
return|return
name|this
operator|.
name|relocatingNodeId
return|;
block|}
annotation|@
name|Override
DECL|method|primary
specifier|public
name|boolean
name|primary
parameter_list|()
block|{
return|return
name|this
operator|.
name|primary
return|;
block|}
annotation|@
name|Override
DECL|method|state
specifier|public
name|ShardRoutingState
name|state
parameter_list|()
block|{
return|return
name|this
operator|.
name|state
return|;
block|}
annotation|@
name|Override
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
if|if
condition|(
name|shardIdentifier
operator|!=
literal|null
condition|)
block|{
return|return
name|shardIdentifier
return|;
block|}
name|shardIdentifier
operator|=
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
return|return
name|shardIdentifier
return|;
block|}
annotation|@
name|Override
DECL|method|shardsIt
specifier|public
name|ShardIterator
name|shardsIt
parameter_list|()
block|{
return|return
operator|new
name|PlainShardIterator
argument_list|(
name|shardId
argument_list|()
argument_list|,
name|asList
argument_list|)
return|;
block|}
comment|/**      * Reads a {@link ImmutableShardRouting} instance of a shard from an {@link InputStream}      *       * @param in {@link InputStream} to read the entry from      * @return {@link ImmutableShardRouting} instances read from the given {@link InputStream}      *       * @throws IOException if some exception occurs during the read operations      */
DECL|method|readShardRoutingEntry
specifier|public
specifier|static
name|ImmutableShardRouting
name|readShardRoutingEntry
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ImmutableShardRouting
name|entry
init|=
operator|new
name|ImmutableShardRouting
argument_list|()
decl_stmt|;
name|entry
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|entry
return|;
block|}
comment|/**      * Reads a routingentry from an inputstream with given<code>index</code> and      *<code>shardId</code>.      *       * @param in inputstream to read the entry from      * @param index shards index      * @param id id of the shard      * @return Shard routing entry read      *       * @throws IOException if some exception occurs during the read operations      */
DECL|method|readShardRoutingEntry
specifier|public
specifier|static
name|ImmutableShardRouting
name|readShardRoutingEntry
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
throws|throws
name|IOException
block|{
name|ImmutableShardRouting
name|entry
init|=
operator|new
name|ImmutableShardRouting
argument_list|()
decl_stmt|;
name|entry
operator|.
name|readFrom
argument_list|(
name|in
argument_list|,
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
return|return
name|entry
return|;
block|}
comment|/**      * Read information from an inputstream with given<code>index</code> and      *<code>shardId</code>.      *       * @param in inputstream to read the entry from      * @param index shards index      * @param id id of the shard      *       * @throws IOException if some exception occurs during the read operations      */
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|readFromThin
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFromThin
specifier|public
name|void
name|readFromThin
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|version
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|currentNodeId
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|relocatingNodeId
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
name|primary
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|state
operator|=
name|ShardRoutingState
operator|.
name|fromValue
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|readFrom
argument_list|(
name|in
argument_list|,
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|in
operator|.
name|readVInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Writes shard information to {@link StreamOutput} without writing index name and shard id      * @param out {@link StreamOutput} to write shard information to      * @throws IOException if something happens during write      */
DECL|method|writeToThin
specifier|public
name|void
name|writeToThin
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|version
argument_list|)
expr_stmt|;
if|if
condition|(
name|currentNodeId
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|currentNodeId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|relocatingNodeId
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|relocatingNodeId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|primary
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|state
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
name|writeToThin
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
comment|// we check on instanceof so we also handle the MutableShardRouting case as well
if|if
condition|(
name|o
operator|==
literal|null
operator|||
operator|!
operator|(
name|o
operator|instanceof
name|ImmutableShardRouting
operator|)
condition|)
return|return
literal|false
return|;
name|ImmutableShardRouting
name|that
init|=
operator|(
name|ImmutableShardRouting
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|primary
operator|!=
name|that
operator|.
name|primary
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|shardId
operator|!=
name|that
operator|.
name|shardId
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|currentNodeId
operator|!=
literal|null
condition|?
operator|!
name|currentNodeId
operator|.
name|equals
argument_list|(
name|that
operator|.
name|currentNodeId
argument_list|)
else|:
name|that
operator|.
name|currentNodeId
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|index
operator|!=
literal|null
condition|?
operator|!
name|index
operator|.
name|equals
argument_list|(
name|that
operator|.
name|index
argument_list|)
else|:
name|that
operator|.
name|index
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|relocatingNodeId
operator|!=
literal|null
condition|?
operator|!
name|relocatingNodeId
operator|.
name|equals
argument_list|(
name|that
operator|.
name|relocatingNodeId
argument_list|)
else|:
name|that
operator|.
name|relocatingNodeId
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|state
operator|!=
name|that
operator|.
name|state
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|index
operator|!=
literal|null
condition|?
name|index
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|shardId
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|currentNodeId
operator|!=
literal|null
condition|?
name|currentNodeId
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|relocatingNodeId
operator|!=
literal|null
condition|?
name|relocatingNodeId
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|primary
condition|?
literal|1
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|state
operator|!=
literal|null
condition|?
name|state
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|shortSummary
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|shortSummary
specifier|public
name|String
name|shortSummary
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|index
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|shardId
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", node["
argument_list|)
operator|.
name|append
argument_list|(
name|currentNodeId
argument_list|)
operator|.
name|append
argument_list|(
literal|"], "
argument_list|)
expr_stmt|;
if|if
condition|(
name|relocatingNodeId
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"relocating ["
argument_list|)
operator|.
name|append
argument_list|(
name|relocatingNodeId
argument_list|)
operator|.
name|append
argument_list|(
literal|"], "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|primary
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"[P]"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"[R]"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|", s["
argument_list|)
operator|.
name|append
argument_list|(
name|state
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"state"
argument_list|,
name|state
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"primary"
argument_list|,
name|primary
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"node"
argument_list|,
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"relocating_node"
argument_list|,
name|relocatingNodeId
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"shard"
argument_list|,
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

