begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.memory
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|memory
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
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
name|unit
operator|.
name|ByteSizeUnit
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
name|unit
operator|.
name|ByteSizeValue
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
name|unit
operator|.
name|TimeValue
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
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|not
import|;
end_import

begin_class
DECL|class|IndexingMemoryControllerTests
specifier|public
class|class
name|IndexingMemoryControllerTests
extends|extends
name|ESTestCase
block|{
DECL|class|MockController
specifier|static
class|class
name|MockController
extends|extends
name|IndexingMemoryController
block|{
DECL|field|INACTIVE
specifier|final
specifier|static
name|ByteSizeValue
name|INACTIVE
init|=
operator|new
name|ByteSizeValue
argument_list|(
operator|-
literal|1
argument_list|)
decl_stmt|;
DECL|field|indexingBuffers
specifier|final
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ByteSizeValue
argument_list|>
name|indexingBuffers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|translogBuffers
specifier|final
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ByteSizeValue
argument_list|>
name|translogBuffers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|lastIndexTimeNanos
specifier|final
name|Map
argument_list|<
name|ShardId
argument_list|,
name|Long
argument_list|>
name|lastIndexTimeNanos
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|activeShards
specifier|final
name|Set
argument_list|<
name|ShardId
argument_list|>
name|activeShards
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|currentTimeSec
name|long
name|currentTimeSec
init|=
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|)
operator|.
name|seconds
argument_list|()
decl_stmt|;
DECL|method|MockController
specifier|public
name|MockController
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|SHARD_INACTIVE_INTERVAL_TIME_SETTING
argument_list|,
literal|"200h"
argument_list|)
comment|// disable it
operator|.
name|put
argument_list|(
name|IndexShard
operator|.
name|INDEX_SHARD_INACTIVE_TIME_SETTING
argument_list|,
literal|"1ms"
argument_list|)
comment|// nearly immediate
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|100
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
comment|// fix jvm mem size to 100mb
block|}
DECL|method|deleteShard
specifier|public
name|void
name|deleteShard
parameter_list|(
name|ShardId
name|id
parameter_list|)
block|{
name|indexingBuffers
operator|.
name|remove
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|translogBuffers
operator|.
name|remove
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
DECL|method|assertBuffers
specifier|public
name|void
name|assertBuffers
parameter_list|(
name|ShardId
name|id
parameter_list|,
name|ByteSizeValue
name|indexing
parameter_list|,
name|ByteSizeValue
name|translog
parameter_list|)
block|{
name|assertThat
argument_list|(
name|indexingBuffers
operator|.
name|get
argument_list|(
name|id
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|indexing
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|translogBuffers
operator|.
name|get
argument_list|(
name|id
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|translog
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertInActive
specifier|public
name|void
name|assertInActive
parameter_list|(
name|ShardId
name|id
parameter_list|)
block|{
name|assertThat
argument_list|(
name|indexingBuffers
operator|.
name|get
argument_list|(
name|id
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|INACTIVE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|translogBuffers
operator|.
name|get
argument_list|(
name|id
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|INACTIVE
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|currentTimeInNanos
specifier|protected
name|long
name|currentTimeInNanos
parameter_list|()
block|{
return|return
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
name|currentTimeSec
argument_list|)
operator|.
name|nanos
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|availableShards
specifier|protected
name|List
argument_list|<
name|ShardId
argument_list|>
name|availableShards
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|indexingBuffers
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|shardAvailable
specifier|protected
name|boolean
name|shardAvailable
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
return|return
name|indexingBuffers
operator|.
name|containsKey
argument_list|(
name|shardId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getShardActive
specifier|protected
name|Boolean
name|getShardActive
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
return|return
name|activeShards
operator|.
name|contains
argument_list|(
name|shardId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|updateShardBuffers
specifier|protected
name|void
name|updateShardBuffers
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|ByteSizeValue
name|shardIndexingBufferSize
parameter_list|,
name|ByteSizeValue
name|shardTranslogBufferSize
parameter_list|)
block|{
name|indexingBuffers
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|shardIndexingBufferSize
argument_list|)
expr_stmt|;
name|translogBuffers
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|shardTranslogBufferSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|checkIdle
specifier|protected
name|Boolean
name|checkIdle
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
specifier|final
name|TimeValue
name|inactiveTime
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|IndexShard
operator|.
name|INDEX_SHARD_INACTIVE_TIME_SETTING
argument_list|,
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|Long
name|ns
init|=
name|lastIndexTimeNanos
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|ns
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|currentTimeInNanos
argument_list|()
operator|-
name|ns
operator|>=
name|inactiveTime
operator|.
name|nanos
argument_list|()
condition|)
block|{
name|indexingBuffers
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|INACTIVE
argument_list|)
expr_stmt|;
name|translogBuffers
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|INACTIVE
argument_list|)
expr_stmt|;
name|activeShards
operator|.
name|remove
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
DECL|method|incrementTimeSec
specifier|public
name|void
name|incrementTimeSec
parameter_list|(
name|int
name|sec
parameter_list|)
block|{
name|currentTimeSec
operator|+=
name|sec
expr_stmt|;
block|}
DECL|method|simulateIndexing
specifier|public
name|void
name|simulateIndexing
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|lastIndexTimeNanos
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|currentTimeInNanos
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexingBuffers
operator|.
name|containsKey
argument_list|(
name|shardId
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// First time we are seeing this shard; start it off with inactive buffers as IndexShard does:
name|indexingBuffers
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|IndexingMemoryController
operator|.
name|INACTIVE_SHARD_INDEXING_BUFFER
argument_list|)
expr_stmt|;
name|translogBuffers
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|IndexingMemoryController
operator|.
name|INACTIVE_SHARD_TRANSLOG_BUFFER
argument_list|)
expr_stmt|;
block|}
name|activeShards
operator|.
name|add
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
name|forceCheck
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|testShardAdditionAndRemoval
specifier|public
name|void
name|testShardAdditionAndRemoval
parameter_list|()
block|{
name|MockController
name|controller
init|=
operator|new
name|MockController
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"10mb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"100kb"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|ShardId
name|shard1
init|=
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard1
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|64
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
comment|// translog is maxed at 64K
comment|// add another shard
specifier|final
name|ShardId
name|shard2
init|=
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard1
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|5
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|50
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard2
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|5
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|50
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
comment|// remove first shard
name|controller
operator|.
name|deleteShard
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|controller
operator|.
name|forceCheck
argument_list|()
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard2
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|64
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
comment|// translog is maxed at 64K
comment|// remove second shard
name|controller
operator|.
name|deleteShard
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|controller
operator|.
name|forceCheck
argument_list|()
expr_stmt|;
comment|// add a new one
specifier|final
name|ShardId
name|shard3
init|=
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard3
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard3
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|64
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
comment|// translog is maxed at 64K
block|}
DECL|method|testActiveInactive
specifier|public
name|void
name|testActiveInactive
parameter_list|()
block|{
name|MockController
name|controller
init|=
operator|new
name|MockController
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"10mb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"100kb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexShard
operator|.
name|INDEX_SHARD_INACTIVE_TIME_SETTING
argument_list|,
literal|"5s"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|ShardId
name|shard1
init|=
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
specifier|final
name|ShardId
name|shard2
init|=
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard1
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|5
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|50
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard2
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|5
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|50
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
comment|// index into both shards, move the clock and see that they are still active
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|controller
operator|.
name|incrementTimeSec
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|controller
operator|.
name|forceCheck
argument_list|()
expr_stmt|;
comment|// both shards now inactive
name|controller
operator|.
name|assertInActive
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertInActive
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
comment|// index into one shard only, see it becomes active
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard1
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|64
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertInActive
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|controller
operator|.
name|incrementTimeSec
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// increment but not enough to become inactive
name|controller
operator|.
name|forceCheck
argument_list|()
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard1
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|64
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertInActive
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|controller
operator|.
name|incrementTimeSec
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// increment some more
name|controller
operator|.
name|forceCheck
argument_list|()
expr_stmt|;
name|controller
operator|.
name|assertInActive
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertInActive
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
comment|// index some and shard becomes immediately active
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertInActive
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard2
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|64
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMinShardBufferSizes
specifier|public
name|void
name|testMinShardBufferSizes
parameter_list|()
block|{
name|MockController
name|controller
init|=
operator|new
name|MockController
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"10mb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"50kb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|MIN_SHARD_INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"6mb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|MIN_SHARD_TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"40kb"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertTwoActiveShards
argument_list|(
name|controller
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|6
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|40
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMaxShardBufferSizes
specifier|public
name|void
name|testMaxShardBufferSizes
parameter_list|()
block|{
name|MockController
name|controller
init|=
operator|new
name|MockController
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"10mb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"50kb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|MAX_SHARD_INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"3mb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|MAX_SHARD_TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"10kb"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertTwoActiveShards
argument_list|(
name|controller
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|3
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRelativeBufferSizes
specifier|public
name|void
name|testRelativeBufferSizes
parameter_list|()
block|{
name|MockController
name|controller
init|=
operator|new
name|MockController
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"50%"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"0.5%"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|controller
operator|.
name|indexingBufferSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|50
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|controller
operator|.
name|translogBufferSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|512
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMinBufferSizes
specifier|public
name|void
name|testMinBufferSizes
parameter_list|()
block|{
name|MockController
name|controller
init|=
operator|new
name|MockController
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"0.001%"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"0.001%"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|MIN_INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"6mb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|MIN_TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"512kb"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|controller
operator|.
name|indexingBufferSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|6
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|controller
operator|.
name|translogBufferSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|512
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMaxBufferSizes
specifier|public
name|void
name|testMaxBufferSizes
parameter_list|()
block|{
name|MockController
name|controller
init|=
operator|new
name|MockController
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"90%"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"90%"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|MAX_INDEX_BUFFER_SIZE_SETTING
argument_list|,
literal|"6mb"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexingMemoryController
operator|.
name|MAX_TRANSLOG_BUFFER_SIZE_SETTING
argument_list|,
literal|"512kb"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|controller
operator|.
name|indexingBufferSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|6
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|controller
operator|.
name|translogBufferSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|512
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertTwoActiveShards
specifier|protected
name|void
name|assertTwoActiveShards
parameter_list|(
name|MockController
name|controller
parameter_list|,
name|ByteSizeValue
name|indexBufferSize
parameter_list|,
name|ByteSizeValue
name|translogBufferSize
parameter_list|)
block|{
specifier|final
name|ShardId
name|shard1
init|=
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard1
argument_list|)
expr_stmt|;
specifier|final
name|ShardId
name|shard2
init|=
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|controller
operator|.
name|simulateIndexing
argument_list|(
name|shard2
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard1
argument_list|,
name|indexBufferSize
argument_list|,
name|translogBufferSize
argument_list|)
expr_stmt|;
name|controller
operator|.
name|assertBuffers
argument_list|(
name|shard2
argument_list|,
name|indexBufferSize
argument_list|,
name|translogBufferSize
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

