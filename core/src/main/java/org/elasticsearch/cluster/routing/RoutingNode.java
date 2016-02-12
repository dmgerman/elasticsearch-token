begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|java
operator|.
name|util
operator|.
name|ArrayList
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
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * A {@link RoutingNode} represents a cluster node associated with a single {@link DiscoveryNode} including all shards  * that are hosted on that nodes. Each {@link RoutingNode} has a unique node id that can be used to identify the node.  */
end_comment

begin_class
DECL|class|RoutingNode
specifier|public
class|class
name|RoutingNode
implements|implements
name|Iterable
argument_list|<
name|ShardRouting
argument_list|>
block|{
DECL|field|nodeId
specifier|private
specifier|final
name|String
name|nodeId
decl_stmt|;
DECL|field|node
specifier|private
specifier|final
name|DiscoveryNode
name|node
decl_stmt|;
DECL|field|shards
specifier|private
specifier|final
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
decl_stmt|;
DECL|method|RoutingNode
specifier|public
name|RoutingNode
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|this
argument_list|(
name|nodeId
argument_list|,
name|node
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|ShardRouting
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|RoutingNode
specifier|public
name|RoutingNode
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|,
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
parameter_list|)
block|{
name|this
operator|.
name|nodeId
operator|=
name|nodeId
expr_stmt|;
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
name|this
operator|.
name|shards
operator|=
name|shards
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|ShardRouting
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|shards
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|mutableIterator
name|Iterator
argument_list|<
name|ShardRouting
argument_list|>
name|mutableIterator
parameter_list|()
block|{
return|return
name|shards
operator|.
name|iterator
argument_list|()
return|;
block|}
comment|/**      * Returns the nodes {@link DiscoveryNode}.      *      * @return discoveryNode of this node      */
DECL|method|node
specifier|public
name|DiscoveryNode
name|node
parameter_list|()
block|{
return|return
name|this
operator|.
name|node
return|;
block|}
comment|/**      * Get the id of this node      * @return id of the node      */
DECL|method|nodeId
specifier|public
name|String
name|nodeId
parameter_list|()
block|{
return|return
name|this
operator|.
name|nodeId
return|;
block|}
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|shards
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**      * Add a new shard to this node      * @param shard Shard to crate on this Node      */
DECL|method|add
name|void
name|add
parameter_list|(
name|ShardRouting
name|shard
parameter_list|)
block|{
comment|// TODO use Set with ShardIds for faster lookup.
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|shards
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|isSameShard
argument_list|(
name|shard
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Trying to add a shard ["
operator|+
name|shard
operator|.
name|shardId
argument_list|()
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"]["
operator|+
name|shard
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
operator|+
literal|"] to a node ["
operator|+
name|nodeId
operator|+
literal|"] where it already exists"
argument_list|)
throw|;
block|}
block|}
name|shards
operator|.
name|add
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
comment|/**      * Determine the number of shards with a specific state      * @param states set of states which should be counted      * @return number of shards      */
DECL|method|numberOfShardsWithState
specifier|public
name|int
name|numberOfShardsWithState
parameter_list|(
name|ShardRoutingState
modifier|...
name|states
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardEntry
range|:
name|this
control|)
block|{
for|for
control|(
name|ShardRoutingState
name|state
range|:
name|states
control|)
block|{
if|if
condition|(
name|shardEntry
operator|.
name|state
argument_list|()
operator|==
name|state
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
block|}
return|return
name|count
return|;
block|}
comment|/**      * Determine the shards with a specific state      * @param states set of states which should be listed      * @return List of shards      */
DECL|method|shardsWithState
specifier|public
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shardsWithState
parameter_list|(
name|ShardRoutingState
modifier|...
name|states
parameter_list|)
block|{
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardEntry
range|:
name|this
control|)
block|{
for|for
control|(
name|ShardRoutingState
name|state
range|:
name|states
control|)
block|{
if|if
condition|(
name|shardEntry
operator|.
name|state
argument_list|()
operator|==
name|state
condition|)
block|{
name|shards
operator|.
name|add
argument_list|(
name|shardEntry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|shards
return|;
block|}
comment|/**      * Determine the shards of an index with a specific state      * @param index id of the index      * @param states set of states which should be listed      * @return a list of shards      */
DECL|method|shardsWithState
specifier|public
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shardsWithState
parameter_list|(
name|String
name|index
parameter_list|,
name|ShardRoutingState
modifier|...
name|states
parameter_list|)
block|{
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardEntry
range|:
name|this
control|)
block|{
if|if
condition|(
operator|!
name|shardEntry
operator|.
name|getIndexName
argument_list|()
operator|.
name|equals
argument_list|(
name|index
argument_list|)
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|ShardRoutingState
name|state
range|:
name|states
control|)
block|{
if|if
condition|(
name|shardEntry
operator|.
name|state
argument_list|()
operator|==
name|state
condition|)
block|{
name|shards
operator|.
name|add
argument_list|(
name|shardEntry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|shards
return|;
block|}
comment|/**      * The number of shards on this node that will not be eventually relocated.      */
DECL|method|numberOfOwningShards
specifier|public
name|int
name|numberOfOwningShards
parameter_list|()
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardEntry
range|:
name|this
control|)
block|{
if|if
condition|(
name|shardEntry
operator|.
name|state
argument_list|()
operator|!=
name|ShardRoutingState
operator|.
name|RELOCATING
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
return|return
name|count
return|;
block|}
DECL|method|prettyPrint
specifier|public
name|String
name|prettyPrint
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
literal|"-----node_id["
argument_list|)
operator|.
name|append
argument_list|(
name|nodeId
argument_list|)
operator|.
name|append
argument_list|(
literal|"]["
operator|+
operator|(
name|node
operator|==
literal|null
condition|?
literal|"X"
else|:
literal|"V"
operator|)
operator|+
literal|"]\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardRouting
name|entry
range|:
name|shards
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"--------"
argument_list|)
operator|.
name|append
argument_list|(
name|entry
operator|.
name|shortSummary
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|toString
specifier|public
name|String
name|toString
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
literal|"routingNode (["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|node
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"]["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|node
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"]["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|node
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"]["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|node
operator|.
name|getHostAddress
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"], ["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|shards
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" assigned shards])"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|get
specifier|public
name|ShardRouting
name|get
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|shards
operator|.
name|get
argument_list|(
name|i
argument_list|)
return|;
block|}
DECL|method|copyShards
specifier|public
name|Collection
argument_list|<
name|ShardRouting
argument_list|>
name|copyShards
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|shards
argument_list|)
return|;
block|}
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|shards
operator|.
name|isEmpty
argument_list|()
return|;
block|}
block|}
end_class

end_unit

