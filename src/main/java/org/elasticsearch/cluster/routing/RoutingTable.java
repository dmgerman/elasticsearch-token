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
name|*
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
name|ClusterState
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
name|metadata
operator|.
name|MetaData
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
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndexMissingException
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
name|ArrayList
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|newArrayList
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|newHashMap
import|;
end_import

begin_comment
comment|/**  * Represents a global cluster-wide routing table for all indices including the  * version of the current routing state.   * @see IndexRoutingTable  */
end_comment

begin_class
DECL|class|RoutingTable
specifier|public
class|class
name|RoutingTable
implements|implements
name|Iterable
argument_list|<
name|IndexRoutingTable
argument_list|>
block|{
DECL|field|EMPTY_ROUTING_TABLE
specifier|public
specifier|static
specifier|final
name|RoutingTable
name|EMPTY_ROUTING_TABLE
init|=
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|field|version
specifier|private
specifier|final
name|long
name|version
decl_stmt|;
comment|// index to IndexRoutingTable map
DECL|field|indicesRouting
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|IndexRoutingTable
argument_list|>
name|indicesRouting
decl_stmt|;
DECL|method|RoutingTable
name|RoutingTable
parameter_list|(
name|long
name|version
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|IndexRoutingTable
argument_list|>
name|indicesRouting
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|indicesRouting
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|indicesRouting
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the version of the {@link RoutingTable}.      * @return version of the {@link RoutingTable}      */
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
DECL|method|iterator
specifier|public
name|UnmodifiableIterator
argument_list|<
name|IndexRoutingTable
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|indicesRouting
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|hasIndex
specifier|public
name|boolean
name|hasIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
return|return
name|indicesRouting
operator|.
name|containsKey
argument_list|(
name|index
argument_list|)
return|;
block|}
DECL|method|index
specifier|public
name|IndexRoutingTable
name|index
parameter_list|(
name|String
name|index
parameter_list|)
block|{
return|return
name|indicesRouting
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
DECL|method|indicesRouting
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|IndexRoutingTable
argument_list|>
name|indicesRouting
parameter_list|()
block|{
return|return
name|indicesRouting
return|;
block|}
DECL|method|getIndicesRouting
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|IndexRoutingTable
argument_list|>
name|getIndicesRouting
parameter_list|()
block|{
return|return
name|indicesRouting
argument_list|()
return|;
block|}
DECL|method|routingNodes
specifier|public
name|RoutingNodes
name|routingNodes
parameter_list|(
name|ClusterState
name|state
parameter_list|)
block|{
return|return
operator|new
name|RoutingNodes
argument_list|(
name|state
argument_list|)
return|;
block|}
DECL|method|validateRaiseException
specifier|public
name|RoutingTable
name|validateRaiseException
parameter_list|(
name|MetaData
name|metaData
parameter_list|)
throws|throws
name|RoutingValidationException
block|{
name|RoutingTableValidation
name|validation
init|=
name|validate
argument_list|(
name|metaData
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|validation
operator|.
name|valid
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RoutingValidationException
argument_list|(
name|validation
argument_list|)
throw|;
block|}
return|return
name|this
return|;
block|}
DECL|method|validate
specifier|public
name|RoutingTableValidation
name|validate
parameter_list|(
name|MetaData
name|metaData
parameter_list|)
block|{
name|RoutingTableValidation
name|validation
init|=
operator|new
name|RoutingTableValidation
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|this
control|)
block|{
name|indexRoutingTable
operator|.
name|validate
argument_list|(
name|validation
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
return|return
name|validation
return|;
block|}
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
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|this
control|)
block|{
name|shards
operator|.
name|addAll
argument_list|(
name|indexRoutingTable
operator|.
name|shardsWithState
argument_list|(
name|states
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|shards
return|;
block|}
comment|/**      * All the shards (replicas) for the provided indices.      *      * @param indices The indices to return all the shards (replicas), can be<tt>null</tt> or empty array to indicate all indices      * @return All the shards matching the specific index      * @throws IndexMissingException If an index passed does not exists      */
DECL|method|allShards
specifier|public
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|allShards
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
throws|throws
name|IndexMissingException
block|{
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|indices
operator|==
literal|null
operator|||
name|indices
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|indices
operator|=
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexMissingException
argument_list|(
operator|new
name|Index
argument_list|(
name|index
argument_list|)
argument_list|)
throw|;
block|}
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
name|shards
operator|.
name|add
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|shards
return|;
block|}
comment|/**      * All the shards (primary + replicas) for the provided indices grouped (each group is a single element, consisting      * of the shard). This is handy for components that expect to get group iterators, but still want in some      * cases to iterate over all the shards (and not just one shard in replication group).      *      * @param indices The indices to return all the shards (replicas), can be<tt>null</tt> or empty array to indicate all indices      * @return All the shards grouped into a single shard element group each      * @throws IndexMissingException If an index passed does not exists      * @see IndexRoutingTable#groupByAllIt()      */
DECL|method|allShardsGrouped
specifier|public
name|GroupShardsIterator
name|allShardsGrouped
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
throws|throws
name|IndexMissingException
block|{
comment|// use list here since we need to maintain identity across shards
name|ArrayList
argument_list|<
name|ShardIterator
argument_list|>
name|set
init|=
operator|new
name|ArrayList
argument_list|<
name|ShardIterator
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|indices
operator|==
literal|null
operator|||
name|indices
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|indices
operator|=
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|==
literal|null
condition|)
block|{
continue|continue;
comment|// we simply ignore indices that don't exists (make sense for operations that use it currently)
comment|//                throw new IndexMissingException(new Index(index));
block|}
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
name|set
operator|.
name|add
argument_list|(
name|shardRouting
operator|.
name|shardsIt
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|GroupShardsIterator
argument_list|(
name|set
argument_list|)
return|;
block|}
DECL|method|allActiveShardsGrouped
specifier|public
name|GroupShardsIterator
name|allActiveShardsGrouped
parameter_list|(
name|String
index|[]
name|indices
parameter_list|,
name|boolean
name|includeEmpty
parameter_list|)
throws|throws
name|IndexMissingException
block|{
comment|// use list here since we need to maintain identity across shards
name|ArrayList
argument_list|<
name|ShardIterator
argument_list|>
name|set
init|=
operator|new
name|ArrayList
argument_list|<
name|ShardIterator
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|indices
operator|==
literal|null
operator|||
name|indices
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|indices
operator|=
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|==
literal|null
condition|)
block|{
continue|continue;
comment|// we simply ignore indices that don't exists (make sense for operations that use it currently)
comment|//                throw new IndexMissingException(new Index(index));
block|}
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|active
argument_list|()
condition|)
block|{
name|set
operator|.
name|add
argument_list|(
name|shardRouting
operator|.
name|shardsIt
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|includeEmpty
condition|)
block|{
comment|// we need this for counting properly, just make it an empty one
name|set
operator|.
name|add
argument_list|(
operator|new
name|PlainShardIterator
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|ImmutableList
operator|.
expr|<
name|ShardRouting
operator|>
name|of
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
operator|new
name|GroupShardsIterator
argument_list|(
name|set
argument_list|)
return|;
block|}
DECL|method|allAssignedShardsGrouped
specifier|public
name|GroupShardsIterator
name|allAssignedShardsGrouped
parameter_list|(
name|String
index|[]
name|indices
parameter_list|,
name|boolean
name|includeEmpty
parameter_list|)
throws|throws
name|IndexMissingException
block|{
comment|// use list here since we need to maintain identity across shards
name|ArrayList
argument_list|<
name|ShardIterator
argument_list|>
name|set
init|=
operator|new
name|ArrayList
argument_list|<
name|ShardIterator
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|indices
operator|==
literal|null
operator|||
name|indices
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|indices
operator|=
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|==
literal|null
condition|)
block|{
continue|continue;
comment|// we simply ignore indices that don't exists (make sense for operations that use it currently)
comment|//                throw new IndexMissingException(new Index(index));
block|}
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|assignedToNode
argument_list|()
condition|)
block|{
name|set
operator|.
name|add
argument_list|(
name|shardRouting
operator|.
name|shardsIt
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|includeEmpty
condition|)
block|{
comment|// we need this for counting properly, just make it an empty one
name|set
operator|.
name|add
argument_list|(
operator|new
name|PlainShardIterator
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|ImmutableList
operator|.
expr|<
name|ShardRouting
operator|>
name|of
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
operator|new
name|GroupShardsIterator
argument_list|(
name|set
argument_list|)
return|;
block|}
comment|/**      * All the *active* primary shards for the provided indices grouped (each group is a single element, consisting      * of the primary shard). This is handy for components that expect to get group iterators, but still want in some      * cases to iterate over all primary shards (and not just one shard in replication group).      *      * @param indices The indices to return all the shards (replicas), can be<tt>null</tt> or empty array to indicate all indices      * @return All the primary shards grouped into a single shard element group each      * @throws IndexMissingException If an index passed does not exists      * @see IndexRoutingTable#groupByAllIt()      */
DECL|method|activePrimaryShardsGrouped
specifier|public
name|GroupShardsIterator
name|activePrimaryShardsGrouped
parameter_list|(
name|String
index|[]
name|indices
parameter_list|,
name|boolean
name|includeEmpty
parameter_list|)
throws|throws
name|IndexMissingException
block|{
comment|// use list here since we need to maintain identity across shards
name|ArrayList
argument_list|<
name|ShardIterator
argument_list|>
name|set
init|=
operator|new
name|ArrayList
argument_list|<
name|ShardIterator
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|indices
operator|==
literal|null
operator|||
name|indices
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|indices
operator|=
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexMissingException
argument_list|(
operator|new
name|Index
argument_list|(
name|index
argument_list|)
argument_list|)
throw|;
block|}
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
name|ShardRouting
name|primary
init|=
name|indexShardRoutingTable
operator|.
name|primaryShard
argument_list|()
decl_stmt|;
if|if
condition|(
name|primary
operator|.
name|active
argument_list|()
condition|)
block|{
name|set
operator|.
name|add
argument_list|(
name|primary
operator|.
name|shardsIt
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|includeEmpty
condition|)
block|{
comment|// we need this for counting properly, just make it an empty one
name|set
operator|.
name|add
argument_list|(
operator|new
name|PlainShardIterator
argument_list|(
name|primary
operator|.
name|shardId
argument_list|()
argument_list|,
name|ImmutableList
operator|.
expr|<
name|ShardRouting
operator|>
name|of
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|GroupShardsIterator
argument_list|(
name|set
argument_list|)
return|;
block|}
DECL|method|builder
specifier|public
specifier|static
name|Builder
name|builder
parameter_list|()
block|{
return|return
operator|new
name|Builder
argument_list|()
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|version
specifier|private
name|long
name|version
decl_stmt|;
DECL|field|indicesRouting
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|IndexRoutingTable
argument_list|>
name|indicesRouting
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|method|routingTable
specifier|public
name|Builder
name|routingTable
parameter_list|(
name|RoutingTable
name|routingTable
parameter_list|)
block|{
name|version
operator|=
name|routingTable
operator|.
name|version
expr_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|routingTable
control|)
block|{
name|indicesRouting
operator|.
name|put
argument_list|(
name|indexRoutingTable
operator|.
name|index
argument_list|()
argument_list|,
name|indexRoutingTable
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|updateNodes
specifier|public
name|Builder
name|updateNodes
parameter_list|(
name|RoutingNodes
name|routingNodes
parameter_list|)
block|{
comment|// this is being called without pre initializing the routing table, so we must copy over the version as well
name|this
operator|.
name|version
operator|=
name|routingNodes
operator|.
name|routingTable
argument_list|()
operator|.
name|version
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|IndexRoutingTable
operator|.
name|Builder
argument_list|>
name|indexRoutingTableBuilders
init|=
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|routingNodes
control|)
block|{
for|for
control|(
name|MutableShardRouting
name|shardRoutingEntry
range|:
name|routingNode
control|)
block|{
comment|// every relocating shard has a double entry, ignore the target one.
if|if
condition|(
name|shardRoutingEntry
operator|.
name|state
argument_list|()
operator|==
name|ShardRoutingState
operator|.
name|INITIALIZING
operator|&&
name|shardRoutingEntry
operator|.
name|relocatingNodeId
argument_list|()
operator|!=
literal|null
condition|)
continue|continue;
name|String
name|index
init|=
name|shardRoutingEntry
operator|.
name|index
argument_list|()
decl_stmt|;
name|IndexRoutingTable
operator|.
name|Builder
name|indexBuilder
init|=
name|indexRoutingTableBuilders
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexBuilder
operator|==
literal|null
condition|)
block|{
name|indexBuilder
operator|=
operator|new
name|IndexRoutingTable
operator|.
name|Builder
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|indexRoutingTableBuilders
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|indexBuilder
argument_list|)
expr_stmt|;
block|}
name|IndexShardRoutingTable
name|refData
init|=
name|routingNodes
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
name|shardRoutingEntry
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|shard
argument_list|(
name|shardRoutingEntry
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|indexBuilder
operator|.
name|addShard
argument_list|(
name|refData
argument_list|,
name|shardRoutingEntry
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|MutableShardRouting
name|shardRoutingEntry
range|:
name|Iterables
operator|.
name|concat
argument_list|(
name|routingNodes
operator|.
name|unassigned
argument_list|()
argument_list|,
name|routingNodes
operator|.
name|ignoredUnassigned
argument_list|()
argument_list|)
control|)
block|{
name|String
name|index
init|=
name|shardRoutingEntry
operator|.
name|index
argument_list|()
decl_stmt|;
name|IndexRoutingTable
operator|.
name|Builder
name|indexBuilder
init|=
name|indexRoutingTableBuilders
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexBuilder
operator|==
literal|null
condition|)
block|{
name|indexBuilder
operator|=
operator|new
name|IndexRoutingTable
operator|.
name|Builder
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|indexRoutingTableBuilders
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|indexBuilder
argument_list|)
expr_stmt|;
block|}
name|IndexShardRoutingTable
name|refData
init|=
name|routingNodes
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
name|shardRoutingEntry
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|shard
argument_list|(
name|shardRoutingEntry
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|indexBuilder
operator|.
name|addShard
argument_list|(
name|refData
argument_list|,
name|shardRoutingEntry
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|IndexRoutingTable
operator|.
name|Builder
name|indexBuilder
range|:
name|indexRoutingTableBuilders
operator|.
name|values
argument_list|()
control|)
block|{
name|add
argument_list|(
name|indexBuilder
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|updateNumberOfReplicas
specifier|public
name|Builder
name|updateNumberOfReplicas
parameter_list|(
name|int
name|numberOfReplicas
parameter_list|,
name|String
modifier|...
name|indices
parameter_list|)
throws|throws
name|IndexMissingException
block|{
if|if
condition|(
name|indices
operator|==
literal|null
operator|||
name|indices
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|indices
operator|=
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|indicesRouting
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|indicesRouting
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|==
literal|null
condition|)
block|{
comment|// ignore index missing failure, its closed...
continue|continue;
block|}
name|int
name|currentNumberOfReplicas
init|=
name|indexRoutingTable
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|size
argument_list|()
operator|-
literal|1
decl_stmt|;
comment|// remove the required primary
name|IndexRoutingTable
operator|.
name|Builder
name|builder
init|=
operator|new
name|IndexRoutingTable
operator|.
name|Builder
argument_list|(
name|index
argument_list|)
decl_stmt|;
comment|// re-add all the shards
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
name|builder
operator|.
name|addIndexShard
argument_list|(
name|indexShardRoutingTable
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|currentNumberOfReplicas
operator|<
name|numberOfReplicas
condition|)
block|{
comment|// now, add "empty" ones
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
operator|(
name|numberOfReplicas
operator|-
name|currentNumberOfReplicas
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|addReplica
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|currentNumberOfReplicas
operator|>
name|numberOfReplicas
condition|)
block|{
name|int
name|delta
init|=
name|currentNumberOfReplicas
operator|-
name|numberOfReplicas
decl_stmt|;
if|if
condition|(
name|delta
operator|<=
literal|0
condition|)
block|{
comment|// ignore, can't remove below the current one...
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|delta
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|removeReplica
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|indicesRouting
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|addAsNew
specifier|public
name|Builder
name|addAsNew
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
if|if
condition|(
name|indexMetaData
operator|.
name|state
argument_list|()
operator|==
name|IndexMetaData
operator|.
name|State
operator|.
name|OPEN
condition|)
block|{
name|IndexRoutingTable
operator|.
name|Builder
name|indexRoutingBuilder
init|=
operator|new
name|IndexRoutingTable
operator|.
name|Builder
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|initializeAsNew
argument_list|(
name|indexMetaData
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|indexRoutingBuilder
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|addAsRecovery
specifier|public
name|Builder
name|addAsRecovery
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
if|if
condition|(
name|indexMetaData
operator|.
name|state
argument_list|()
operator|==
name|IndexMetaData
operator|.
name|State
operator|.
name|OPEN
condition|)
block|{
name|IndexRoutingTable
operator|.
name|Builder
name|indexRoutingBuilder
init|=
operator|new
name|IndexRoutingTable
operator|.
name|Builder
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|initializeAsRecovery
argument_list|(
name|indexMetaData
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|indexRoutingBuilder
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|add
specifier|public
name|Builder
name|add
parameter_list|(
name|IndexRoutingTable
name|indexRoutingTable
parameter_list|)
block|{
name|indexRoutingTable
operator|.
name|validate
argument_list|()
expr_stmt|;
name|indicesRouting
operator|.
name|put
argument_list|(
name|indexRoutingTable
operator|.
name|index
argument_list|()
argument_list|,
name|indexRoutingTable
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|add
specifier|public
name|Builder
name|add
parameter_list|(
name|IndexRoutingTable
operator|.
name|Builder
name|indexRoutingTableBuilder
parameter_list|)
block|{
name|add
argument_list|(
name|indexRoutingTableBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|remove
specifier|public
name|Builder
name|remove
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|indicesRouting
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|version
specifier|public
name|Builder
name|version
parameter_list|(
name|long
name|version
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|RoutingTable
name|build
parameter_list|()
block|{
comment|// normalize the versions right before we build it...
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|indicesRouting
operator|.
name|values
argument_list|()
control|)
block|{
name|indicesRouting
operator|.
name|put
argument_list|(
name|indexRoutingTable
operator|.
name|index
argument_list|()
argument_list|,
name|indexRoutingTable
operator|.
name|normalizeVersions
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|RoutingTable
argument_list|(
name|version
argument_list|,
name|indicesRouting
argument_list|)
return|;
block|}
DECL|method|readFrom
specifier|public
specifier|static
name|RoutingTable
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|version
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|IndexRoutingTable
name|index
init|=
name|IndexRoutingTable
operator|.
name|Builder
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|writeTo
specifier|public
specifier|static
name|void
name|writeTo
parameter_list|(
name|RoutingTable
name|table
parameter_list|,
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
name|table
operator|.
name|version
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|table
operator|.
name|indicesRouting
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexRoutingTable
name|index
range|:
name|table
operator|.
name|indicesRouting
operator|.
name|values
argument_list|()
control|)
block|{
name|IndexRoutingTable
operator|.
name|Builder
operator|.
name|writeTo
argument_list|(
name|index
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
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
argument_list|(
literal|"routing_table:\n"
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|IndexRoutingTable
argument_list|>
name|entry
range|:
name|indicesRouting
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|prettyPrint
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
block|}
end_class

end_unit

