begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.replication
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|replication
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|DocWriteResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
operator|.
name|IndexRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
operator|.
name|IndexResponse
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
name|ShardRouting
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
name|XContentHelper
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
name|InternalEngine
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
name|InternalEngineTests
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
name|SegmentsStats
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
name|seqno
operator|.
name|SeqNoStats
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
name|seqno
operator|.
name|SequenceNumbersService
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
name|IndexShardTests
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
name|store
operator|.
name|Store
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
name|recovery
operator|.
name|RecoveryTarget
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matcher
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
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
name|anyOf
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

begin_class
DECL|class|IndexLevelReplicationTests
specifier|public
class|class
name|IndexLevelReplicationTests
extends|extends
name|ESIndexLevelReplicationTestCase
block|{
DECL|method|testSimpleReplication
specifier|public
name|void
name|testSimpleReplication
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|ReplicationGroup
name|shards
init|=
name|createGroup
argument_list|(
name|randomInt
argument_list|(
literal|2
argument_list|)
argument_list|)
init|)
block|{
name|shards
operator|.
name|startAll
argument_list|()
expr_stmt|;
specifier|final
name|int
name|docCount
init|=
name|randomInt
argument_list|(
literal|50
argument_list|)
decl_stmt|;
name|shards
operator|.
name|indexDocs
argument_list|(
name|docCount
argument_list|)
expr_stmt|;
name|shards
operator|.
name|assertAllEqual
argument_list|(
name|docCount
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSimpleAppendOnlyReplication
specifier|public
name|void
name|testSimpleAppendOnlyReplication
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|ReplicationGroup
name|shards
init|=
name|createGroup
argument_list|(
name|randomInt
argument_list|(
literal|2
argument_list|)
argument_list|)
init|)
block|{
name|shards
operator|.
name|startAll
argument_list|()
expr_stmt|;
specifier|final
name|int
name|docCount
init|=
name|randomInt
argument_list|(
literal|50
argument_list|)
decl_stmt|;
name|shards
operator|.
name|appendDocs
argument_list|(
name|docCount
argument_list|)
expr_stmt|;
name|shards
operator|.
name|assertAllEqual
argument_list|(
name|docCount
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testAppendWhileRecovering
specifier|public
name|void
name|testAppendWhileRecovering
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|ReplicationGroup
name|shards
init|=
name|createGroup
argument_list|(
literal|0
argument_list|)
init|)
block|{
name|shards
operator|.
name|startAll
argument_list|()
expr_stmt|;
name|IndexShard
name|replica
init|=
name|shards
operator|.
name|addReplica
argument_list|()
decl_stmt|;
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|100
argument_list|,
literal|200
argument_list|)
decl_stmt|;
name|shards
operator|.
name|appendDocs
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// just append one to the translog so we can assert below
name|Thread
name|thread
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|shards
operator|.
name|appendDocs
argument_list|(
name|numDocs
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
name|Future
argument_list|<
name|Void
argument_list|>
name|future
init|=
name|shards
operator|.
name|asyncRecoverReplica
argument_list|(
name|replica
argument_list|,
parameter_list|(
name|indexShard
parameter_list|,
name|node
parameter_list|)
lambda|->
operator|new
name|RecoveryTarget
argument_list|(
name|indexShard
argument_list|,
name|node
argument_list|,
name|recoveryListener
argument_list|,
name|version
lambda|->
block|{}
argument_list|)
block|{
block|@Override                 public void cleanFiles(int totalTranslogOps
argument_list|,
name|Store
operator|.
name|MetadataSnapshot
name|sourceMetaData
argument_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|cleanFiles
argument_list|(
name|totalTranslogOps
argument_list|,
name|sourceMetaData
argument_list|)
decl_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|)
class|;
end_class

begin_expr_stmt
name|future
operator|.
name|get
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|thread
operator|.
name|join
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|shards
operator|.
name|assertAllEqual
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
end_expr_stmt

begin_decl_stmt
name|Engine
name|engine
init|=
name|IndexShardTests
operator|.
name|getEngineFromShard
argument_list|(
name|replica
argument_list|)
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|assertEquals
argument_list|(
literal|"expected at no version lookups "
argument_list|,
name|InternalEngineTests
operator|.
name|getNumVersionLookups
argument_list|(
operator|(
name|InternalEngine
operator|)
name|engine
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
end_expr_stmt

begin_for
for|for
control|(
name|IndexShard
name|shard
range|:
name|shards
control|)
block|{
name|engine
operator|=
name|IndexShardTests
operator|.
name|getEngineFromShard
argument_list|(
name|shard
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|InternalEngineTests
operator|.
name|getNumIndexVersionsLookups
argument_list|(
operator|(
name|InternalEngine
operator|)
name|engine
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|InternalEngineTests
operator|.
name|getNumVersionLookups
argument_list|(
operator|(
name|InternalEngine
operator|)
name|engine
argument_list|)
argument_list|)
expr_stmt|;
block|}
end_for

begin_function
unit|}     }
DECL|method|testInheritMaxValidAutoIDTimestampOnRecovery
specifier|public
name|void
name|testInheritMaxValidAutoIDTimestampOnRecovery
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|ReplicationGroup
name|shards
init|=
name|createGroup
argument_list|(
literal|0
argument_list|)
init|)
block|{
name|shards
operator|.
name|startAll
argument_list|()
expr_stmt|;
specifier|final
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
name|index
operator|.
name|getName
argument_list|()
argument_list|,
literal|"type"
argument_list|)
operator|.
name|source
argument_list|(
literal|"{}"
argument_list|)
decl_stmt|;
name|indexRequest
operator|.
name|onRetry
argument_list|()
expr_stmt|;
comment|// force an update of the timestamp
specifier|final
name|IndexResponse
name|response
init|=
name|shards
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|DocWriteResponse
operator|.
name|Result
operator|.
name|CREATED
argument_list|,
name|response
operator|.
name|getResult
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
comment|// lets check if that also happens if no translog record is replicated
name|shards
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
name|IndexShard
name|replica
init|=
name|shards
operator|.
name|addReplica
argument_list|()
decl_stmt|;
name|shards
operator|.
name|recoverReplica
argument_list|(
name|replica
argument_list|)
expr_stmt|;
name|SegmentsStats
name|segmentsStats
init|=
name|replica
operator|.
name|segmentStats
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|SegmentsStats
name|primarySegmentStats
init|=
name|shards
operator|.
name|getPrimary
argument_list|()
operator|.
name|segmentStats
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|assertNotEquals
argument_list|(
name|IndexRequest
operator|.
name|UNSET_AUTO_GENERATED_TIMESTAMP
argument_list|,
name|primarySegmentStats
operator|.
name|getMaxUnsafeAutoIdTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|primarySegmentStats
operator|.
name|getMaxUnsafeAutoIdTimestamp
argument_list|()
argument_list|,
name|segmentsStats
operator|.
name|getMaxUnsafeAutoIdTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|segmentsStats
operator|.
name|getMaxUnsafeAutoIdTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_function

begin_function
DECL|method|testCheckpointsAdvance
specifier|public
name|void
name|testCheckpointsAdvance
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|ReplicationGroup
name|shards
init|=
name|createGroup
argument_list|(
name|randomInt
argument_list|(
literal|3
argument_list|)
argument_list|)
init|)
block|{
name|shards
operator|.
name|startPrimary
argument_list|()
expr_stmt|;
name|int
name|numDocs
init|=
literal|0
decl_stmt|;
name|int
name|startedShards
decl_stmt|;
do|do
block|{
name|numDocs
operator|+=
name|shards
operator|.
name|indexDocs
argument_list|(
name|randomInt
argument_list|(
literal|20
argument_list|)
argument_list|)
expr_stmt|;
name|startedShards
operator|=
name|shards
operator|.
name|startReplicas
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|startedShards
operator|>
literal|0
condition|)
do|;
if|if
condition|(
name|numDocs
operator|==
literal|0
operator|||
name|randomBoolean
argument_list|()
condition|)
block|{
comment|// in the case we have no indexing, we simulate the background global checkpoint sync
name|shards
operator|.
name|getPrimary
argument_list|()
operator|.
name|updateGlobalCheckpointOnPrimary
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|IndexShard
name|shard
range|:
name|shards
control|)
block|{
specifier|final
name|SeqNoStats
name|shardStats
init|=
name|shard
operator|.
name|seqNoStats
argument_list|()
decl_stmt|;
specifier|final
name|ShardRouting
name|shardRouting
init|=
name|shard
operator|.
name|routingEntry
argument_list|()
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"seq_no stats for {}: {}"
argument_list|,
name|shardRouting
argument_list|,
name|XContentHelper
operator|.
name|toString
argument_list|(
name|shardStats
argument_list|,
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"pretty"
argument_list|,
literal|"false"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardRouting
operator|+
literal|" local checkpoint mismatch"
argument_list|,
name|shardStats
operator|.
name|getLocalCheckpoint
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numDocs
operator|-
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Matcher
argument_list|<
name|Long
argument_list|>
name|globalCheckpointMatcher
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
name|globalCheckpointMatcher
operator|=
name|equalTo
argument_list|(
name|numDocs
operator|-
literal|1L
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// nocommit: removed once fixed
name|globalCheckpointMatcher
operator|=
name|anyOf
argument_list|(
name|equalTo
argument_list|(
name|SequenceNumbersService
operator|.
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|numDocs
operator|-
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|shardRouting
operator|+
literal|" global checkpoint mismatch"
argument_list|,
name|shardStats
operator|.
name|getGlobalCheckpoint
argument_list|()
argument_list|,
name|globalCheckpointMatcher
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardRouting
operator|+
literal|" max seq no mismatch"
argument_list|,
name|shardStats
operator|.
name|getMaxSeqNo
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numDocs
operator|-
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_function

unit|}
end_unit
