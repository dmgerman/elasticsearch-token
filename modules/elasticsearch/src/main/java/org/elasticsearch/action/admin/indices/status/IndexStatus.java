begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.status
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|status
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
name|Maps
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|SizeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|IndexStatus
specifier|public
class|class
name|IndexStatus
implements|implements
name|Iterable
argument_list|<
name|IndexShardStatus
argument_list|>
block|{
DECL|class|Docs
specifier|public
specifier|static
class|class
name|Docs
block|{
DECL|field|UNKNOWN
specifier|public
specifier|static
specifier|final
name|Docs
name|UNKNOWN
init|=
operator|new
name|Docs
argument_list|()
decl_stmt|;
DECL|field|numDocs
name|int
name|numDocs
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|maxDoc
name|int
name|maxDoc
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|deletedDocs
name|int
name|deletedDocs
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|numDocs
specifier|public
name|int
name|numDocs
parameter_list|()
block|{
return|return
name|numDocs
return|;
block|}
DECL|method|maxDoc
specifier|public
name|int
name|maxDoc
parameter_list|()
block|{
return|return
name|maxDoc
return|;
block|}
DECL|method|deletedDocs
specifier|public
name|int
name|deletedDocs
parameter_list|()
block|{
return|return
name|deletedDocs
return|;
block|}
block|}
DECL|field|index
specifier|private
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|indexShards
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|IndexShardStatus
argument_list|>
name|indexShards
decl_stmt|;
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|method|IndexStatus
name|IndexStatus
parameter_list|(
name|String
name|index
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|ShardStatus
index|[]
name|shards
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
name|settings
operator|=
name|settings
expr_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|ShardStatus
argument_list|>
argument_list|>
name|tmpIndexShards
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardStatus
name|shard
range|:
name|shards
control|)
block|{
name|List
argument_list|<
name|ShardStatus
argument_list|>
name|lst
init|=
name|tmpIndexShards
operator|.
name|get
argument_list|(
name|shard
operator|.
name|shardRouting
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|lst
operator|==
literal|null
condition|)
block|{
name|lst
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
name|tmpIndexShards
operator|.
name|put
argument_list|(
name|shard
operator|.
name|shardRouting
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|lst
argument_list|)
expr_stmt|;
block|}
name|lst
operator|.
name|add
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
name|indexShards
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|ShardStatus
argument_list|>
argument_list|>
name|entry
range|:
name|tmpIndexShards
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|indexShards
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|IndexShardStatus
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|shardRouting
argument_list|()
operator|.
name|shardId
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|ShardStatus
index|[
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
DECL|method|shards
specifier|public
name|Map
argument_list|<
name|Integer
argument_list|,
name|IndexShardStatus
argument_list|>
name|shards
parameter_list|()
block|{
return|return
name|this
operator|.
name|indexShards
return|;
block|}
DECL|method|settings
specifier|public
name|Settings
name|settings
parameter_list|()
block|{
return|return
name|this
operator|.
name|settings
return|;
block|}
DECL|method|storeSize
specifier|public
name|SizeValue
name|storeSize
parameter_list|()
block|{
name|long
name|bytes
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|IndexShardStatus
name|shard
range|:
name|this
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|storeSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|!=
name|SizeValue
operator|.
name|UNKNOWN
operator|.
name|bytes
argument_list|()
condition|)
block|{
if|if
condition|(
name|bytes
operator|==
operator|-
literal|1
condition|)
block|{
name|bytes
operator|=
literal|0
expr_stmt|;
block|}
name|bytes
operator|+=
name|shard
operator|.
name|storeSize
argument_list|()
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|SizeValue
argument_list|(
name|bytes
argument_list|)
return|;
block|}
DECL|method|estimatedFlushableMemorySize
specifier|public
name|SizeValue
name|estimatedFlushableMemorySize
parameter_list|()
block|{
name|long
name|bytes
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|IndexShardStatus
name|shard
range|:
name|this
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|estimatedFlushableMemorySize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|!=
name|SizeValue
operator|.
name|UNKNOWN
operator|.
name|bytes
argument_list|()
condition|)
block|{
if|if
condition|(
name|bytes
operator|==
operator|-
literal|1
condition|)
block|{
name|bytes
operator|=
literal|0
expr_stmt|;
block|}
name|bytes
operator|+=
name|shard
operator|.
name|estimatedFlushableMemorySize
argument_list|()
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|SizeValue
argument_list|(
name|bytes
argument_list|)
return|;
block|}
DECL|method|translogOperations
specifier|public
name|long
name|translogOperations
parameter_list|()
block|{
name|long
name|translogOperations
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|IndexShardStatus
name|shard
range|:
name|this
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|translogOperations
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|translogOperations
operator|==
operator|-
literal|1
condition|)
block|{
name|translogOperations
operator|=
literal|0
expr_stmt|;
block|}
name|translogOperations
operator|+=
name|shard
operator|.
name|translogOperations
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|translogOperations
return|;
block|}
DECL|method|docs
specifier|public
name|Docs
name|docs
parameter_list|()
block|{
name|Docs
name|docs
init|=
operator|new
name|Docs
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexShardStatus
name|shard
range|:
name|this
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|docs
argument_list|()
operator|.
name|numDocs
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|docs
operator|.
name|numDocs
operator|==
operator|-
literal|1
condition|)
block|{
name|docs
operator|.
name|numDocs
operator|=
literal|0
expr_stmt|;
block|}
name|docs
operator|.
name|numDocs
operator|+=
name|shard
operator|.
name|docs
argument_list|()
operator|.
name|numDocs
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|shard
operator|.
name|docs
argument_list|()
operator|.
name|maxDoc
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|docs
operator|.
name|maxDoc
operator|==
operator|-
literal|1
condition|)
block|{
name|docs
operator|.
name|maxDoc
operator|=
literal|0
expr_stmt|;
block|}
name|docs
operator|.
name|maxDoc
operator|+=
name|shard
operator|.
name|docs
argument_list|()
operator|.
name|maxDoc
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|shard
operator|.
name|docs
argument_list|()
operator|.
name|deletedDocs
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|docs
operator|.
name|deletedDocs
operator|==
operator|-
literal|1
condition|)
block|{
name|docs
operator|.
name|deletedDocs
operator|=
literal|0
expr_stmt|;
block|}
name|docs
operator|.
name|deletedDocs
operator|+=
name|shard
operator|.
name|docs
argument_list|()
operator|.
name|deletedDocs
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|docs
return|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|IndexShardStatus
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|indexShards
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
end_class

end_unit

