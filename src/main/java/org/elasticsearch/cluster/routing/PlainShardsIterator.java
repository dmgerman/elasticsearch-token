begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * A simple {@link ShardsIterator} that iterates a list or sub-list of  * {@link ShardRouting shard routings}.  */
end_comment

begin_class
DECL|class|PlainShardsIterator
specifier|public
class|class
name|PlainShardsIterator
implements|implements
name|ShardsIterator
block|{
DECL|field|shards
specifier|private
specifier|final
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
decl_stmt|;
DECL|field|size
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
DECL|field|index
specifier|private
specifier|final
name|int
name|index
decl_stmt|;
DECL|field|limit
specifier|private
specifier|final
name|int
name|limit
decl_stmt|;
DECL|field|counter
specifier|private
specifier|volatile
name|int
name|counter
decl_stmt|;
DECL|method|PlainShardsIterator
specifier|public
name|PlainShardsIterator
parameter_list|(
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
parameter_list|)
block|{
name|this
argument_list|(
name|shards
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
DECL|method|PlainShardsIterator
specifier|public
name|PlainShardsIterator
parameter_list|(
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|this
operator|.
name|shards
operator|=
name|shards
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|shards
operator|.
name|size
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|index
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|index
operator|=
name|Math
operator|.
name|abs
argument_list|(
name|index
operator|%
name|size
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|counter
operator|=
name|this
operator|.
name|index
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|this
operator|.
name|index
operator|+
name|size
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|reset
specifier|public
name|ShardsIterator
name|reset
parameter_list|()
block|{
name|this
operator|.
name|counter
operator|=
name|this
operator|.
name|index
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|remaining
specifier|public
name|int
name|remaining
parameter_list|()
block|{
return|return
name|limit
operator|-
name|counter
return|;
block|}
annotation|@
name|Override
DECL|method|firstOrNull
specifier|public
name|ShardRouting
name|firstOrNull
parameter_list|()
block|{
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|shards
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|nextOrNull
specifier|public
name|ShardRouting
name|nextOrNull
parameter_list|()
block|{
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|counter
init|=
operator|(
name|this
operator|.
name|counter
operator|)
decl_stmt|;
if|if
condition|(
name|counter
operator|>=
name|size
condition|)
block|{
if|if
condition|(
name|counter
operator|>=
name|limit
condition|)
block|{
return|return
literal|null
return|;
block|}
name|this
operator|.
name|counter
operator|=
name|counter
operator|+
literal|1
expr_stmt|;
return|return
name|shards
operator|.
name|get
argument_list|(
name|counter
operator|-
name|size
argument_list|)
return|;
block|}
else|else
block|{
name|this
operator|.
name|counter
operator|=
name|counter
operator|+
literal|1
expr_stmt|;
return|return
name|shards
operator|.
name|get
argument_list|(
name|counter
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
annotation|@
name|Override
DECL|method|sizeActive
specifier|public
name|int
name|sizeActive
parameter_list|()
block|{
name|int
name|count
init|=
literal|0
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
if|if
condition|(
name|shards
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|active
argument_list|()
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
annotation|@
name|Override
DECL|method|assignedReplicasIncludingRelocating
specifier|public
name|int
name|assignedReplicasIncludingRelocating
parameter_list|()
block|{
name|int
name|count
init|=
literal|0
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
name|ShardRouting
name|shard
init|=
name|shards
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|shard
operator|.
name|unassigned
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// if the shard is primary and relocating, add one to the counter since we perform it on the replica as well
comment|// (and we already did it on the primary)
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
condition|)
block|{
if|if
condition|(
name|shard
operator|.
name|relocating
argument_list|()
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|count
operator|++
expr_stmt|;
comment|// if we are relocating the replica, we want to perform the index operation on both the relocating
comment|// shard and the target shard. This means that we won't loose index operations between end of recovery
comment|// and reassignment of the shard by the master node
if|if
condition|(
name|shard
operator|.
name|relocating
argument_list|()
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
annotation|@
name|Override
DECL|method|asUnordered
specifier|public
name|Iterable
argument_list|<
name|ShardRouting
argument_list|>
name|asUnordered
parameter_list|()
block|{
return|return
name|shards
return|;
block|}
block|}
end_class

end_unit

