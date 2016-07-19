begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|recovery
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|TestShardRouting
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
name|transport
operator|.
name|LocalTransportAddress
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
name|replication
operator|.
name|ESIndexLevelReplicationTestCase
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
name|indices
operator|.
name|IndicesService
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
name|RecoveriesCollection
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
name|RecoveryFailedException
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
name|RecoveryState
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
name|RecoveryTargetService
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
name|ESSingleNodeTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|TestThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|TimeUnit
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
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptySet
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
name|lessThan
import|;
end_import

begin_class
DECL|class|RecoveriesCollectionTests
specifier|public
class|class
name|RecoveriesCollectionTests
extends|extends
name|ESIndexLevelReplicationTestCase
block|{
DECL|field|listener
specifier|static
specifier|final
name|RecoveryTargetService
operator|.
name|RecoveryListener
name|listener
init|=
operator|new
name|RecoveryTargetService
operator|.
name|RecoveryListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onRecoveryDone
parameter_list|(
name|RecoveryState
name|state
parameter_list|)
block|{          }
annotation|@
name|Override
specifier|public
name|void
name|onRecoveryFailure
parameter_list|(
name|RecoveryState
name|state
parameter_list|,
name|RecoveryFailedException
name|e
parameter_list|,
name|boolean
name|sendShardFailure
parameter_list|)
block|{          }
block|}
decl_stmt|;
DECL|method|testLastAccessTimeUpdate
specifier|public
name|void
name|testLastAccessTimeUpdate
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
specifier|final
name|RecoveriesCollection
name|collection
init|=
operator|new
name|RecoveriesCollection
argument_list|(
name|logger
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
specifier|final
name|long
name|recoveryId
init|=
name|startRecovery
argument_list|(
name|collection
argument_list|,
name|shards
operator|.
name|getPrimaryNode
argument_list|()
argument_list|,
name|shards
operator|.
name|addReplica
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|RecoveriesCollection
operator|.
name|RecoveryRef
name|status
init|=
name|collection
operator|.
name|getRecovery
argument_list|(
name|recoveryId
argument_list|)
init|)
block|{
specifier|final
name|long
name|lastSeenTime
init|=
name|status
operator|.
name|status
argument_list|()
operator|.
name|lastAccessTime
argument_list|()
decl_stmt|;
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
init|(
name|RecoveriesCollection
operator|.
name|RecoveryRef
name|currentStatus
init|=
name|collection
operator|.
name|getRecovery
argument_list|(
name|recoveryId
argument_list|)
init|)
block|{
name|assertThat
argument_list|(
literal|"access time failed to update"
argument_list|,
name|lastSeenTime
argument_list|,
name|lessThan
argument_list|(
name|currentStatus
operator|.
name|status
argument_list|()
operator|.
name|lastAccessTime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|collection
operator|.
name|cancelRecovery
argument_list|(
name|recoveryId
argument_list|,
literal|"life"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testRecoveryTimeout
specifier|public
name|void
name|testRecoveryTimeout
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
specifier|final
name|RecoveriesCollection
name|collection
init|=
operator|new
name|RecoveriesCollection
argument_list|(
name|logger
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|failed
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|long
name|recoveryId
init|=
name|startRecovery
argument_list|(
name|collection
argument_list|,
name|shards
operator|.
name|getPrimaryNode
argument_list|()
argument_list|,
name|shards
operator|.
name|addReplica
argument_list|()
argument_list|,
operator|new
name|RecoveryTargetService
operator|.
name|RecoveryListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onRecoveryDone
parameter_list|(
name|RecoveryState
name|state
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onRecoveryFailure
parameter_list|(
name|RecoveryState
name|state
parameter_list|,
name|RecoveryFailedException
name|e
parameter_list|,
name|boolean
name|sendShardFailure
parameter_list|)
block|{
name|failed
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|100
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|latch
operator|.
name|await
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"recovery failed to timeout"
argument_list|,
name|failed
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|collection
operator|.
name|cancelRecovery
argument_list|(
name|recoveryId
argument_list|,
literal|"meh"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testRecoveryCancellation
specifier|public
name|void
name|testRecoveryCancellation
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
specifier|final
name|RecoveriesCollection
name|collection
init|=
operator|new
name|RecoveriesCollection
argument_list|(
name|logger
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
specifier|final
name|long
name|recoveryId
init|=
name|startRecovery
argument_list|(
name|collection
argument_list|,
name|shards
operator|.
name|getPrimaryNode
argument_list|()
argument_list|,
name|shards
operator|.
name|addReplica
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|long
name|recoveryId2
init|=
name|startRecovery
argument_list|(
name|collection
argument_list|,
name|shards
operator|.
name|getPrimaryNode
argument_list|()
argument_list|,
name|shards
operator|.
name|addReplica
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|RecoveriesCollection
operator|.
name|RecoveryRef
name|recoveryRef
init|=
name|collection
operator|.
name|getRecovery
argument_list|(
name|recoveryId
argument_list|)
init|)
block|{
name|ShardId
name|shardId
init|=
name|recoveryRef
operator|.
name|status
argument_list|()
operator|.
name|shardId
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"failed to cancel recoveries"
argument_list|,
name|collection
operator|.
name|cancelRecoveriesForShard
argument_list|(
name|shardId
argument_list|,
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"all recoveries should be cancelled"
argument_list|,
name|collection
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|collection
operator|.
name|cancelRecovery
argument_list|(
name|recoveryId
argument_list|,
literal|"meh"
argument_list|)
expr_stmt|;
name|collection
operator|.
name|cancelRecovery
argument_list|(
name|recoveryId2
argument_list|,
literal|"meh"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testResetRecovery
specifier|public
name|void
name|testResetRecovery
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
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|15
argument_list|)
decl_stmt|;
name|shards
operator|.
name|indexDocs
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
specifier|final
name|RecoveriesCollection
name|collection
init|=
operator|new
name|RecoveriesCollection
argument_list|(
name|logger
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
name|IndexShard
name|shard
init|=
name|shards
operator|.
name|addReplica
argument_list|()
decl_stmt|;
specifier|final
name|long
name|recoveryId
init|=
name|startRecovery
argument_list|(
name|collection
argument_list|,
name|shards
operator|.
name|getPrimaryNode
argument_list|()
argument_list|,
name|shard
argument_list|)
decl_stmt|;
try|try
init|(
name|RecoveriesCollection
operator|.
name|RecoveryRef
name|recovery
init|=
name|collection
operator|.
name|getRecovery
argument_list|(
name|recoveryId
argument_list|)
init|)
block|{
specifier|final
name|int
name|currentAsTarget
init|=
name|shard
operator|.
name|recoveryStats
argument_list|()
operator|.
name|currentAsTarget
argument_list|()
decl_stmt|;
specifier|final
name|int
name|referencesToStore
init|=
name|recovery
operator|.
name|status
argument_list|()
operator|.
name|store
argument_list|()
operator|.
name|refCount
argument_list|()
decl_stmt|;
name|String
name|tempFileName
init|=
name|recovery
operator|.
name|status
argument_list|()
operator|.
name|getTempNameForFile
argument_list|(
literal|"foobar"
argument_list|)
decl_stmt|;
name|collection
operator|.
name|resetRecovery
argument_list|(
name|recoveryId
argument_list|,
name|recovery
operator|.
name|status
argument_list|()
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|RecoveriesCollection
operator|.
name|RecoveryRef
name|resetRecovery
init|=
name|collection
operator|.
name|getRecovery
argument_list|(
name|recoveryId
argument_list|)
init|)
block|{
name|assertNotSame
argument_list|(
name|recovery
operator|.
name|status
argument_list|()
argument_list|,
name|resetRecovery
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|recovery
operator|.
name|status
argument_list|()
operator|.
name|CancellableThreads
argument_list|()
argument_list|,
name|resetRecovery
operator|.
name|status
argument_list|()
operator|.
name|CancellableThreads
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|recovery
operator|.
name|status
argument_list|()
operator|.
name|indexShard
argument_list|()
argument_list|,
name|resetRecovery
operator|.
name|status
argument_list|()
operator|.
name|indexShard
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|recovery
operator|.
name|status
argument_list|()
operator|.
name|store
argument_list|()
argument_list|,
name|resetRecovery
operator|.
name|status
argument_list|()
operator|.
name|store
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|referencesToStore
operator|+
literal|1
argument_list|,
name|resetRecovery
operator|.
name|status
argument_list|()
operator|.
name|store
argument_list|()
operator|.
name|refCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|currentAsTarget
operator|+
literal|1
argument_list|,
name|shard
operator|.
name|recoveryStats
argument_list|()
operator|.
name|currentAsTarget
argument_list|()
argument_list|)
expr_stmt|;
comment|// we blink for a short moment...
name|recovery
operator|.
name|close
argument_list|()
expr_stmt|;
name|expectThrows
argument_list|(
name|ElasticsearchException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|recovery
operator|.
name|status
argument_list|()
operator|.
name|store
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|referencesToStore
argument_list|,
name|resetRecovery
operator|.
name|status
argument_list|()
operator|.
name|store
argument_list|()
operator|.
name|refCount
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|resetTempFileName
init|=
name|resetRecovery
operator|.
name|status
argument_list|()
operator|.
name|getTempNameForFile
argument_list|(
literal|"foobar"
argument_list|)
decl_stmt|;
name|assertNotEquals
argument_list|(
name|tempFileName
argument_list|,
name|resetTempFileName
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|currentAsTarget
argument_list|,
name|shard
operator|.
name|recoveryStats
argument_list|()
operator|.
name|currentAsTarget
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|RecoveriesCollection
operator|.
name|RecoveryRef
name|resetRecovery
init|=
name|collection
operator|.
name|getRecovery
argument_list|(
name|recoveryId
argument_list|)
init|)
block|{
name|shards
operator|.
name|recoverReplica
argument_list|(
name|shard
argument_list|,
parameter_list|(
name|s
parameter_list|,
name|n
parameter_list|)
lambda|->
block|{
name|assertSame
argument_list|(
name|s
argument_list|,
name|resetRecovery
operator|.
name|status
argument_list|()
operator|.
name|indexShard
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|resetRecovery
operator|.
name|status
argument_list|()
return|;
block|}
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|shards
operator|.
name|assertAllEqual
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"recovery is done"
argument_list|,
name|collection
operator|.
name|getRecovery
argument_list|(
name|recoveryId
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|startRecovery
name|long
name|startRecovery
parameter_list|(
name|RecoveriesCollection
name|collection
parameter_list|,
name|DiscoveryNode
name|sourceNode
parameter_list|,
name|IndexShard
name|shard
parameter_list|)
block|{
return|return
name|startRecovery
argument_list|(
name|collection
argument_list|,
name|sourceNode
argument_list|,
name|shard
argument_list|,
name|listener
argument_list|,
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|60
argument_list|)
argument_list|)
return|;
block|}
DECL|method|startRecovery
name|long
name|startRecovery
parameter_list|(
name|RecoveriesCollection
name|collection
parameter_list|,
name|DiscoveryNode
name|sourceNode
parameter_list|,
name|IndexShard
name|indexShard
parameter_list|,
name|RecoveryTargetService
operator|.
name|RecoveryListener
name|listener
parameter_list|,
name|TimeValue
name|timeValue
parameter_list|)
block|{
specifier|final
name|DiscoveryNode
name|rNode
init|=
name|getDiscoveryNode
argument_list|(
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|markAsRecovering
argument_list|(
literal|"remote"
argument_list|,
operator|new
name|RecoveryState
argument_list|(
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|,
literal|false
argument_list|,
name|RecoveryState
operator|.
name|Type
operator|.
name|REPLICA
argument_list|,
name|sourceNode
argument_list|,
name|rNode
argument_list|)
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|prepareForIndexRecovery
argument_list|()
expr_stmt|;
return|return
name|collection
operator|.
name|startRecovery
argument_list|(
name|indexShard
argument_list|,
name|sourceNode
argument_list|,
name|listener
argument_list|,
name|timeValue
argument_list|)
return|;
block|}
block|}
end_class

end_unit

