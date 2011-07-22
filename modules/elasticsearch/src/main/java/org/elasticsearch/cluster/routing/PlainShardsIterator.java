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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
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
DECL|field|origIndex
specifier|private
specifier|final
name|int
name|origIndex
decl_stmt|;
DECL|field|index
specifier|private
specifier|volatile
name|int
name|index
decl_stmt|;
DECL|field|counter
specifier|private
specifier|volatile
name|int
name|counter
init|=
literal|0
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
name|this
operator|.
name|index
operator|=
name|Math
operator|.
name|abs
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|this
operator|.
name|origIndex
operator|=
name|this
operator|.
name|index
expr_stmt|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|ShardRouting
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|this
return|;
block|}
DECL|method|reset
annotation|@
name|Override
specifier|public
name|ShardsIterator
name|reset
parameter_list|()
block|{
name|counter
operator|=
literal|0
expr_stmt|;
name|index
operator|=
name|origIndex
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|hasNext
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|counter
operator|<
name|size
return|;
block|}
DECL|method|next
annotation|@
name|Override
specifier|public
name|ShardRouting
name|next
parameter_list|()
throws|throws
name|NoSuchElementException
block|{
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|(
literal|"No shard found"
argument_list|)
throw|;
block|}
name|counter
operator|++
expr_stmt|;
return|return
name|shardModulo
argument_list|(
name|index
operator|++
argument_list|)
return|;
block|}
DECL|method|remove
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
DECL|method|size
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
DECL|method|sizeActive
annotation|@
name|Override
specifier|public
name|int
name|sizeActive
parameter_list|()
block|{
name|int
name|shardsActive
init|=
literal|0
decl_stmt|;
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
name|active
argument_list|()
condition|)
block|{
name|shardsActive
operator|++
expr_stmt|;
block|}
block|}
return|return
name|shardsActive
return|;
block|}
DECL|method|hasNextActive
annotation|@
name|Override
specifier|public
name|boolean
name|hasNextActive
parameter_list|()
block|{
name|int
name|counter
init|=
name|this
operator|.
name|counter
decl_stmt|;
name|int
name|index
init|=
name|this
operator|.
name|index
decl_stmt|;
while|while
condition|(
name|counter
operator|++
operator|<
name|size
condition|)
block|{
name|ShardRouting
name|shardRouting
init|=
name|shardModulo
argument_list|(
name|index
operator|++
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|.
name|active
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|nextActive
annotation|@
name|Override
specifier|public
name|ShardRouting
name|nextActive
parameter_list|()
throws|throws
name|NoSuchElementException
block|{
name|ShardRouting
name|shardRouting
init|=
name|nextActiveOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|(
literal|"No active shard found"
argument_list|)
throw|;
block|}
return|return
name|shardRouting
return|;
block|}
DECL|method|nextActiveOrNull
annotation|@
name|Override
specifier|public
name|ShardRouting
name|nextActiveOrNull
parameter_list|()
block|{
name|int
name|counter
init|=
name|this
operator|.
name|counter
decl_stmt|;
name|int
name|index
init|=
name|this
operator|.
name|index
decl_stmt|;
while|while
condition|(
name|counter
operator|++
operator|<
name|size
condition|)
block|{
name|ShardRouting
name|shardRouting
init|=
name|shardModulo
argument_list|(
name|index
operator|++
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|.
name|active
argument_list|()
condition|)
block|{
name|this
operator|.
name|counter
operator|=
name|counter
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
return|return
name|shardRouting
return|;
block|}
block|}
name|this
operator|.
name|counter
operator|=
name|counter
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
return|return
literal|null
return|;
block|}
DECL|method|sizeAssigned
annotation|@
name|Override
specifier|public
name|int
name|sizeAssigned
parameter_list|()
block|{
name|int
name|shardsAssigned
init|=
literal|0
decl_stmt|;
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
name|assignedToNode
argument_list|()
condition|)
block|{
name|shardsAssigned
operator|++
expr_stmt|;
block|}
block|}
return|return
name|shardsAssigned
return|;
block|}
DECL|method|hasNextAssigned
annotation|@
name|Override
specifier|public
name|boolean
name|hasNextAssigned
parameter_list|()
block|{
name|int
name|counter
init|=
name|this
operator|.
name|counter
decl_stmt|;
name|int
name|index
init|=
name|this
operator|.
name|index
decl_stmt|;
while|while
condition|(
name|counter
operator|++
operator|<
name|size
condition|)
block|{
name|ShardRouting
name|shardRouting
init|=
name|shardModulo
argument_list|(
name|index
operator|++
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|.
name|assignedToNode
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|nextAssigned
annotation|@
name|Override
specifier|public
name|ShardRouting
name|nextAssigned
parameter_list|()
throws|throws
name|NoSuchElementException
block|{
name|ShardRouting
name|shardRouting
init|=
name|nextAssignedOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|(
literal|"No assigned shard found"
argument_list|)
throw|;
block|}
return|return
name|shardRouting
return|;
block|}
DECL|method|nextAssignedOrNull
annotation|@
name|Override
specifier|public
name|ShardRouting
name|nextAssignedOrNull
parameter_list|()
block|{
name|int
name|counter
init|=
name|this
operator|.
name|counter
decl_stmt|;
name|int
name|index
init|=
name|this
operator|.
name|index
decl_stmt|;
while|while
condition|(
name|counter
operator|++
operator|<
name|size
condition|)
block|{
name|ShardRouting
name|shardRouting
init|=
name|shardModulo
argument_list|(
name|index
operator|++
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|.
name|assignedToNode
argument_list|()
condition|)
block|{
name|this
operator|.
name|counter
operator|=
name|counter
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
return|return
name|shardRouting
return|;
block|}
block|}
name|this
operator|.
name|counter
operator|=
name|counter
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
return|return
literal|null
return|;
block|}
DECL|method|shardModulo
specifier|final
name|ShardRouting
name|shardModulo
parameter_list|(
name|int
name|counter
parameter_list|)
block|{
return|return
name|shards
operator|.
name|get
argument_list|(
operator|(
name|counter
operator|%
name|size
operator|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

