begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionListener
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
name|admin
operator|.
name|indices
operator|.
name|flush
operator|.
name|FlushResponse
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|IndexStats
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|ShardStats
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
name|admin
operator|.
name|indices
operator|.
name|seal
operator|.
name|SealIndicesResponse
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
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|command
operator|.
name|MoveAllocationCommand
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
name|ElasticsearchIntegrationTest
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|Arrays
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
name|concurrent
operator|.
name|CopyOnWriteArrayList
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
name|ExecutionException
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|Thread
operator|.
name|sleep
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
name|emptyIterable
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
DECL|class|FlushTest
specifier|public
class|class
name|FlushTest
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testWaitIfOngoing
specifier|public
name|void
name|testWaitIfOngoing
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numIters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|30
argument_list|)
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
name|numIters
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|10
condition|;
name|j
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|10
argument_list|)
decl_stmt|;
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|Throwable
argument_list|>
name|errors
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|10
condition|;
name|j
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setWaitIfOngoing
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|FlushResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|FlushResponse
name|flushResponse
parameter_list|)
block|{
try|try
block|{
comment|// dont' use assertAllSuccesssful it uses a randomized context that belongs to a different thread
name|assertThat
argument_list|(
literal|"Unexpected ShardFailures: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|flushResponse
operator|.
name|getShardFailures
argument_list|()
argument_list|)
argument_list|,
name|flushResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
name|onFailure
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|errors
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|errors
argument_list|,
name|emptyIterable
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|TestLogging
argument_list|(
literal|"indices:TRACE"
argument_list|)
DECL|method|testSyncedFlush
specifier|public
name|void
name|testSyncedFlush
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
throws|,
name|IOException
block|{
name|internalCluster
argument_list|()
operator|.
name|ensureAtLeastNumDataNodes
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|IndexStats
name|indexStats
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|assertNull
argument_list|(
name|shardStats
operator|.
name|getCommitStats
argument_list|()
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Engine
operator|.
name|SYNC_COMMIT_ID
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SyncedFlushService
operator|.
name|SyncedFlushResult
name|result
init|=
name|SyncedFlushUtil
operator|.
name|attemptSyncedFlush
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|SyncedFlushService
operator|.
name|class
argument_list|)
argument_list|,
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|success
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|totalShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|indexStats
operator|.
name|getShards
argument_list|()
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|successfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|indexStats
operator|.
name|getShards
argument_list|()
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|indexStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|String
name|syncId
init|=
name|result
operator|.
name|syncId
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexStats
operator|.
name|getShards
argument_list|()
control|)
block|{
specifier|final
name|String
name|shardSyncId
init|=
name|shardStats
operator|.
name|getCommitStats
argument_list|()
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Engine
operator|.
name|SYNC_COMMIT_ID
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|shardSyncId
argument_list|,
name|equalTo
argument_list|(
name|syncId
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// now, start new node and relocate a shard there and see if sync id still there
name|String
name|newNodeName
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|()
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|clusterState
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|currentNodeName
init|=
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|resolveNode
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|name
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|currentNodeName
operator|.
name|equals
argument_list|(
name|newNodeName
argument_list|)
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareReroute
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|MoveAllocationCommand
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
argument_list|,
name|currentNodeName
argument_list|,
name|newNodeName
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForRelocatingShards
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|indexStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|assertNotNull
argument_list|(
name|shardStats
operator|.
name|getCommitStats
argument_list|()
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Engine
operator|.
name|SYNC_COMMIT_ID
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|indexStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|assertNotNull
argument_list|(
name|shardStats
operator|.
name|getCommitStats
argument_list|()
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Engine
operator|.
name|SYNC_COMMIT_ID
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
name|internalCluster
argument_list|()
operator|.
name|numDataNodes
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|indexStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|assertNotNull
argument_list|(
name|shardStats
operator|.
name|getCommitStats
argument_list|()
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Engine
operator|.
name|SYNC_COMMIT_ID
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|TestLogging
argument_list|(
literal|"indices:TRACE"
argument_list|)
DECL|method|testSyncedFlushWithApi
specifier|public
name|void
name|testSyncedFlushWithApi
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
throws|,
name|IOException
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|IndexStats
name|indexStats
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|assertNull
argument_list|(
name|shardStats
operator|.
name|getCommitStats
argument_list|()
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Engine
operator|.
name|SYNC_COMMIT_ID
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> trying sync flush"
argument_list|)
expr_stmt|;
name|SealIndicesResponse
name|sealIndicesResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareSealIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> sync flush done"
argument_list|)
expr_stmt|;
name|indexStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|assertNotNull
argument_list|(
name|shardStats
operator|.
name|getCommitStats
argument_list|()
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Engine
operator|.
name|SYNC_COMMIT_ID
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|TestLogging
argument_list|(
literal|"indices:TRACE"
argument_list|)
DECL|method|testSyncedFlushWithApiAndConcurrentIndexing
specifier|public
name|void
name|testSyncedFlushWithApiAndConcurrentIndexing
parameter_list|()
throws|throws
name|Exception
block|{
name|internalCluster
argument_list|()
operator|.
name|ensureAtLeastNumDataNodes
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.translog.disable_flush"
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
operator|-
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
name|internalCluster
argument_list|()
operator|.
name|numDataNodes
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
specifier|final
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|numDocs
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Thread
name|indexingThread
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
while|while
condition|(
name|stop
operator|.
name|get
argument_list|()
operator|==
literal|false
condition|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|()
operator|.
name|setIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|numDocs
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|indexingThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|IndexStats
name|indexStats
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|assertNull
argument_list|(
name|shardStats
operator|.
name|getCommitStats
argument_list|()
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Engine
operator|.
name|SYNC_COMMIT_ID
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> trying sync flush"
argument_list|)
expr_stmt|;
name|SealIndicesResponse
name|sealIndicesResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareSealIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> sync flush done"
argument_list|)
expr_stmt|;
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|indexingThread
operator|.
name|join
argument_list|()
expr_stmt|;
name|indexStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|assertFlushResponseEqualsShardStats
argument_list|(
name|shardStats
argument_list|,
name|sealIndicesResponse
argument_list|)
expr_stmt|;
block|}
name|refresh
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocs
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"indexed {} docs"
argument_list|,
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|logClusterState
argument_list|()
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|fullRestart
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocs
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertFlushResponseEqualsShardStats
specifier|private
name|void
name|assertFlushResponseEqualsShardStats
parameter_list|(
name|ShardStats
name|shardStats
parameter_list|,
name|SealIndicesResponse
name|sealIndicesResponse
parameter_list|)
block|{
for|for
control|(
name|SyncedFlushService
operator|.
name|SyncedFlushResult
name|shardResult
range|:
name|sealIndicesResponse
operator|.
name|results
argument_list|()
control|)
block|{
if|if
condition|(
name|shardStats
operator|.
name|getShardRouting
argument_list|()
operator|.
name|getId
argument_list|()
operator|==
name|shardResult
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardRouting
argument_list|,
name|SyncedFlushService
operator|.
name|SyncedFlushResponse
argument_list|>
name|singleResponse
range|:
name|shardResult
operator|.
name|shardResponses
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|singleResponse
operator|.
name|getKey
argument_list|()
operator|.
name|currentNodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|shardStats
operator|.
name|getShardRouting
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|singleResponse
operator|.
name|getValue
argument_list|()
operator|.
name|success
argument_list|()
condition|)
block|{
name|assertNotNull
argument_list|(
name|shardStats
operator|.
name|getCommitStats
argument_list|()
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Engine
operator|.
name|SYNC_COMMIT_ID
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"sync flushed {} on node {}"
argument_list|,
name|singleResponse
operator|.
name|getKey
argument_list|()
operator|.
name|shardId
argument_list|()
argument_list|,
name|singleResponse
operator|.
name|getKey
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNull
argument_list|(
name|shardStats
operator|.
name|getCommitStats
argument_list|()
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Engine
operator|.
name|SYNC_COMMIT_ID
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"sync flush failed for {} on node {}"
argument_list|,
name|singleResponse
operator|.
name|getKey
argument_list|()
operator|.
name|shardId
argument_list|()
argument_list|,
name|singleResponse
operator|.
name|getKey
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

