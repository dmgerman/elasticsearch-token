begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|inject
operator|.
name|Inject
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
name|engine
operator|.
name|Engine
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
name|engine
operator|.
name|FlushNotAllowedEngineException
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
name|service
operator|.
name|IndexService
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
name|IllegalIndexShardStateException
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
name|IndexShardState
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
name|service
operator|.
name|IndexShard
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
name|service
operator|.
name|InternalIndexShard
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
name|translog
operator|.
name|Translog
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
name|SizeUnit
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
name|Tuple
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
name|component
operator|.
name|AbstractComponent
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
name|ArrayList
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
name|Comparator
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|IndicesMemoryCleaner
specifier|public
class|class
name|IndicesMemoryCleaner
extends|extends
name|AbstractComponent
block|{
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|method|IndicesMemoryCleaner
annotation|@
name|Inject
specifier|public
name|IndicesMemoryCleaner
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
block|}
DECL|method|cleanTranslog
specifier|public
name|TranslogCleanResult
name|cleanTranslog
parameter_list|(
name|int
name|translogNumberOfOperationsThreshold
parameter_list|)
block|{
name|int
name|totalShards
init|=
literal|0
decl_stmt|;
name|int
name|cleanedShards
init|=
literal|0
decl_stmt|;
name|long
name|cleaned
init|=
literal|0
decl_stmt|;
for|for
control|(
name|IndexService
name|indexService
range|:
name|indicesService
control|)
block|{
for|for
control|(
name|IndexShard
name|indexShard
range|:
name|indexService
control|)
block|{
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|!=
name|IndexShardState
operator|.
name|STARTED
condition|)
block|{
continue|continue;
block|}
name|totalShards
operator|++
expr_stmt|;
name|Translog
name|translog
init|=
operator|(
operator|(
name|InternalIndexShard
operator|)
name|indexShard
operator|)
operator|.
name|translog
argument_list|()
decl_stmt|;
if|if
condition|(
name|translog
operator|.
name|size
argument_list|()
operator|>
name|translogNumberOfOperationsThreshold
condition|)
block|{
name|cleanedShards
operator|++
expr_stmt|;
name|cleaned
operator|=
name|indexShard
operator|.
name|estimateFlushableMemorySize
argument_list|()
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|indexShard
operator|.
name|flush
argument_list|(
operator|new
name|Engine
operator|.
name|Flush
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|TranslogCleanResult
argument_list|(
name|totalShards
argument_list|,
name|cleanedShards
argument_list|,
operator|new
name|SizeValue
argument_list|(
name|cleaned
argument_list|,
name|SizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Checks if memory needs to be cleaned and cleans it. Returns the amount of memory cleaned.      */
DECL|method|cleanMemory
specifier|public
name|MemoryCleanResult
name|cleanMemory
parameter_list|(
name|long
name|memoryToClean
parameter_list|,
name|SizeValue
name|minimumFlushableSizeToClean
parameter_list|)
block|{
name|int
name|totalShards
init|=
literal|0
decl_stmt|;
name|long
name|estimatedFlushableSize
init|=
literal|0
decl_stmt|;
name|ArrayList
argument_list|<
name|Tuple
argument_list|<
name|SizeValue
argument_list|,
name|IndexShard
argument_list|>
argument_list|>
name|shards
init|=
operator|new
name|ArrayList
argument_list|<
name|Tuple
argument_list|<
name|SizeValue
argument_list|,
name|IndexShard
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexService
name|indexService
range|:
name|indicesService
control|)
block|{
for|for
control|(
name|IndexShard
name|indexShard
range|:
name|indexService
control|)
block|{
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|!=
name|IndexShardState
operator|.
name|STARTED
condition|)
block|{
continue|continue;
block|}
name|totalShards
operator|++
expr_stmt|;
name|SizeValue
name|estimatedSize
init|=
name|indexShard
operator|.
name|estimateFlushableMemorySize
argument_list|()
decl_stmt|;
name|estimatedFlushableSize
operator|+=
name|estimatedSize
operator|.
name|bytes
argument_list|()
expr_stmt|;
if|if
condition|(
name|estimatedSize
operator|!=
literal|null
condition|)
block|{
name|shards
operator|.
name|add
argument_list|(
operator|new
name|Tuple
argument_list|<
name|SizeValue
argument_list|,
name|IndexShard
argument_list|>
argument_list|(
name|estimatedSize
argument_list|,
name|indexShard
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|shards
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Tuple
argument_list|<
name|SizeValue
argument_list|,
name|IndexShard
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Tuple
argument_list|<
name|SizeValue
argument_list|,
name|IndexShard
argument_list|>
name|o1
parameter_list|,
name|Tuple
argument_list|<
name|SizeValue
argument_list|,
name|IndexShard
argument_list|>
name|o2
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|o1
operator|.
name|v1
argument_list|()
operator|.
name|bytes
argument_list|()
operator|-
name|o2
operator|.
name|v1
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|int
name|cleanedShards
init|=
literal|0
decl_stmt|;
name|long
name|cleaned
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Tuple
argument_list|<
name|SizeValue
argument_list|,
name|IndexShard
argument_list|>
name|tuple
range|:
name|shards
control|)
block|{
if|if
condition|(
name|tuple
operator|.
name|v1
argument_list|()
operator|.
name|bytes
argument_list|()
operator|<
name|minimumFlushableSizeToClean
operator|.
name|bytes
argument_list|()
condition|)
block|{
comment|// we passed the minimum threshold, don't flush
break|break;
block|}
try|try
block|{
name|tuple
operator|.
name|v2
argument_list|()
operator|.
name|flush
argument_list|(
operator|new
name|Engine
operator|.
name|Flush
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FlushNotAllowedEngineException
name|e
parameter_list|)
block|{
comment|// ignore this one, its temporal
block|}
catch|catch
parameter_list|(
name|IllegalIndexShardStateException
name|e
parameter_list|)
block|{
comment|// ignore this one as well
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
name|tuple
operator|.
name|v2
argument_list|()
operator|.
name|shardId
argument_list|()
operator|+
literal|": Failed to flush in order to clean memory"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|cleanedShards
operator|++
expr_stmt|;
name|cleaned
operator|+=
name|tuple
operator|.
name|v1
argument_list|()
operator|.
name|bytes
argument_list|()
expr_stmt|;
if|if
condition|(
name|cleaned
operator|>
name|memoryToClean
condition|)
block|{
break|break;
block|}
block|}
return|return
operator|new
name|MemoryCleanResult
argument_list|(
name|totalShards
argument_list|,
name|cleanedShards
argument_list|,
operator|new
name|SizeValue
argument_list|(
name|estimatedFlushableSize
argument_list|)
argument_list|,
operator|new
name|SizeValue
argument_list|(
name|cleaned
argument_list|)
argument_list|)
return|;
block|}
DECL|class|TranslogCleanResult
specifier|public
specifier|static
class|class
name|TranslogCleanResult
block|{
DECL|field|totalShards
specifier|private
specifier|final
name|int
name|totalShards
decl_stmt|;
DECL|field|cleanedShards
specifier|private
specifier|final
name|int
name|cleanedShards
decl_stmt|;
DECL|field|cleaned
specifier|private
specifier|final
name|SizeValue
name|cleaned
decl_stmt|;
DECL|method|TranslogCleanResult
specifier|public
name|TranslogCleanResult
parameter_list|(
name|int
name|totalShards
parameter_list|,
name|int
name|cleanedShards
parameter_list|,
name|SizeValue
name|cleaned
parameter_list|)
block|{
name|this
operator|.
name|totalShards
operator|=
name|totalShards
expr_stmt|;
name|this
operator|.
name|cleanedShards
operator|=
name|cleanedShards
expr_stmt|;
name|this
operator|.
name|cleaned
operator|=
name|cleaned
expr_stmt|;
block|}
DECL|method|totalShards
specifier|public
name|int
name|totalShards
parameter_list|()
block|{
return|return
name|totalShards
return|;
block|}
DECL|method|cleanedShards
specifier|public
name|int
name|cleanedShards
parameter_list|()
block|{
return|return
name|cleanedShards
return|;
block|}
DECL|method|cleaned
specifier|public
name|SizeValue
name|cleaned
parameter_list|()
block|{
return|return
name|cleaned
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"cleaned["
operator|+
name|cleaned
operator|+
literal|"], cleaned_shards["
operator|+
name|cleanedShards
operator|+
literal|"], total_shards["
operator|+
name|totalShards
operator|+
literal|"]"
return|;
block|}
block|}
DECL|class|MemoryCleanResult
specifier|public
specifier|static
class|class
name|MemoryCleanResult
block|{
DECL|field|totalShards
specifier|private
specifier|final
name|int
name|totalShards
decl_stmt|;
DECL|field|cleanedShards
specifier|private
specifier|final
name|int
name|cleanedShards
decl_stmt|;
DECL|field|estimatedFlushableSize
specifier|private
specifier|final
name|SizeValue
name|estimatedFlushableSize
decl_stmt|;
DECL|field|cleaned
specifier|private
specifier|final
name|SizeValue
name|cleaned
decl_stmt|;
DECL|method|MemoryCleanResult
specifier|public
name|MemoryCleanResult
parameter_list|(
name|int
name|totalShards
parameter_list|,
name|int
name|cleanedShards
parameter_list|,
name|SizeValue
name|estimatedFlushableSize
parameter_list|,
name|SizeValue
name|cleaned
parameter_list|)
block|{
name|this
operator|.
name|totalShards
operator|=
name|totalShards
expr_stmt|;
name|this
operator|.
name|cleanedShards
operator|=
name|cleanedShards
expr_stmt|;
name|this
operator|.
name|estimatedFlushableSize
operator|=
name|estimatedFlushableSize
expr_stmt|;
name|this
operator|.
name|cleaned
operator|=
name|cleaned
expr_stmt|;
block|}
DECL|method|totalShards
specifier|public
name|int
name|totalShards
parameter_list|()
block|{
return|return
name|totalShards
return|;
block|}
DECL|method|cleanedShards
specifier|public
name|int
name|cleanedShards
parameter_list|()
block|{
return|return
name|cleanedShards
return|;
block|}
DECL|method|estimatedFlushableSize
specifier|public
name|SizeValue
name|estimatedFlushableSize
parameter_list|()
block|{
return|return
name|estimatedFlushableSize
return|;
block|}
DECL|method|cleaned
specifier|public
name|SizeValue
name|cleaned
parameter_list|()
block|{
return|return
name|cleaned
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"cleaned["
operator|+
name|cleaned
operator|+
literal|"], estimated_flushable_size["
operator|+
name|estimatedFlushableSize
operator|+
literal|"], cleaned_shards["
operator|+
name|cleanedShards
operator|+
literal|"], total_shards["
operator|+
name|totalShards
operator|+
literal|"]"
return|;
block|}
block|}
block|}
end_class

end_unit

