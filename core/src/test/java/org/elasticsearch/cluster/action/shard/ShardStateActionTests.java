begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.action.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|action
operator|.
name|shard
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|CorruptIndexException
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
name|support
operator|.
name|replication
operator|.
name|ClusterStateCreationUtils
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
name|ClusterStateObserver
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
name|NotMasterException
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
name|DiscoveryNodes
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
name|IndexRoutingTable
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
name|RoutingService
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
name|RoutingTable
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
name|ShardsIterator
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
name|AllocationService
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
name|service
operator|.
name|ClusterService
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
name|discovery
operator|.
name|Discovery
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|transport
operator|.
name|CapturingTransport
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
name|elasticsearch
operator|.
name|transport
operator|.
name|NodeDisconnectedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|NodeNotConnectedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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
name|AfterClass
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
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|LongConsumer
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ClusterServiceUtils
operator|.
name|createClusterService
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ClusterServiceUtils
operator|.
name|setState
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
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
name|CoreMatchers
operator|.
name|instanceOf
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
name|greaterThanOrEqualTo
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
name|is
import|;
end_import

begin_class
DECL|class|ShardStateActionTests
specifier|public
class|class
name|ShardStateActionTests
extends|extends
name|ESTestCase
block|{
DECL|field|THREAD_POOL
specifier|private
specifier|static
name|ThreadPool
name|THREAD_POOL
decl_stmt|;
DECL|field|shardStateAction
specifier|private
name|TestShardStateAction
name|shardStateAction
decl_stmt|;
DECL|field|transport
specifier|private
name|CapturingTransport
name|transport
decl_stmt|;
DECL|field|transportService
specifier|private
name|TransportService
name|transportService
decl_stmt|;
DECL|field|clusterService
specifier|private
name|ClusterService
name|clusterService
decl_stmt|;
DECL|class|TestShardStateAction
specifier|private
specifier|static
class|class
name|TestShardStateAction
extends|extends
name|ShardStateAction
block|{
DECL|method|TestShardStateAction
specifier|public
name|TestShardStateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|AllocationService
name|allocationService
parameter_list|,
name|RoutingService
name|routingService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|allocationService
argument_list|,
name|routingService
argument_list|,
name|THREAD_POOL
argument_list|)
expr_stmt|;
block|}
DECL|field|onBeforeWaitForNewMasterAndRetry
specifier|private
name|Runnable
name|onBeforeWaitForNewMasterAndRetry
decl_stmt|;
DECL|method|setOnBeforeWaitForNewMasterAndRetry
specifier|public
name|void
name|setOnBeforeWaitForNewMasterAndRetry
parameter_list|(
name|Runnable
name|onBeforeWaitForNewMasterAndRetry
parameter_list|)
block|{
name|this
operator|.
name|onBeforeWaitForNewMasterAndRetry
operator|=
name|onBeforeWaitForNewMasterAndRetry
expr_stmt|;
block|}
DECL|field|onAfterWaitForNewMasterAndRetry
specifier|private
name|Runnable
name|onAfterWaitForNewMasterAndRetry
decl_stmt|;
DECL|method|setOnAfterWaitForNewMasterAndRetry
specifier|public
name|void
name|setOnAfterWaitForNewMasterAndRetry
parameter_list|(
name|Runnable
name|onAfterWaitForNewMasterAndRetry
parameter_list|)
block|{
name|this
operator|.
name|onAfterWaitForNewMasterAndRetry
operator|=
name|onAfterWaitForNewMasterAndRetry
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|waitForNewMasterAndRetry
specifier|protected
name|void
name|waitForNewMasterAndRetry
parameter_list|(
name|String
name|actionName
parameter_list|,
name|ClusterStateObserver
name|observer
parameter_list|,
name|ShardEntry
name|shardEntry
parameter_list|,
name|Listener
name|listener
parameter_list|)
block|{
name|onBeforeWaitForNewMasterAndRetry
operator|.
name|run
argument_list|()
expr_stmt|;
name|super
operator|.
name|waitForNewMasterAndRetry
argument_list|(
name|actionName
argument_list|,
name|observer
argument_list|,
name|shardEntry
argument_list|,
name|listener
argument_list|)
expr_stmt|;
name|onAfterWaitForNewMasterAndRetry
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|BeforeClass
DECL|method|startThreadPool
specifier|public
specifier|static
name|void
name|startThreadPool
parameter_list|()
block|{
name|THREAD_POOL
operator|=
operator|new
name|TestThreadPool
argument_list|(
literal|"ShardStateActionTest"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Before
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|this
operator|.
name|transport
operator|=
operator|new
name|CapturingTransport
argument_list|()
expr_stmt|;
name|clusterService
operator|=
name|createClusterService
argument_list|(
name|THREAD_POOL
argument_list|)
expr_stmt|;
name|transportService
operator|=
operator|new
name|TransportService
argument_list|(
name|clusterService
operator|.
name|getSettings
argument_list|()
argument_list|,
name|transport
argument_list|,
name|THREAD_POOL
argument_list|,
name|TransportService
operator|.
name|NOOP_TRANSPORT_INTERCEPTOR
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|start
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|acceptIncomingRequests
argument_list|()
expr_stmt|;
name|shardStateAction
operator|=
operator|new
name|TestShardStateAction
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|setOnBeforeWaitForNewMasterAndRetry
argument_list|(
parameter_list|()
lambda|->
block|{         }
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|setOnAfterWaitForNewMasterAndRetry
argument_list|(
parameter_list|()
lambda|->
block|{         }
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|After
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|clusterService
operator|.
name|close
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|close
argument_list|()
expr_stmt|;
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|stopThreadPool
specifier|public
specifier|static
name|void
name|stopThreadPool
parameter_list|()
block|{
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|THREAD_POOL
argument_list|,
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|THREAD_POOL
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testSuccess
specifier|public
name|void
name|testSuccess
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|String
name|index
init|=
literal|"test"
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|ClusterStateCreationUtils
operator|.
name|stateWithActivePrimary
argument_list|(
name|index
argument_list|,
literal|true
argument_list|,
name|randomInt
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|AtomicBoolean
name|success
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|getRandomShardRouting
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|shardStateAction
operator|.
name|localShardFailed
argument_list|(
name|shardRouting
argument_list|,
literal|"test"
argument_list|,
name|getSimulatedFailure
argument_list|()
argument_list|,
operator|new
name|ShardStateAction
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|()
block|{
name|success
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
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|success
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
block|}
argument_list|)
expr_stmt|;
name|CapturingTransport
operator|.
name|CapturedRequest
index|[]
name|capturedRequests
init|=
name|transport
operator|.
name|getCapturedRequestsAndClear
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|capturedRequests
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// the request is a shard failed request
name|assertThat
argument_list|(
name|capturedRequests
index|[
literal|0
index|]
operator|.
name|request
argument_list|,
name|is
argument_list|(
name|instanceOf
argument_list|(
name|ShardStateAction
operator|.
name|ShardEntry
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ShardStateAction
operator|.
name|ShardEntry
name|shardEntry
init|=
operator|(
name|ShardStateAction
operator|.
name|ShardEntry
operator|)
name|capturedRequests
index|[
literal|0
index|]
operator|.
name|request
decl_stmt|;
comment|// for the right shard
name|assertEquals
argument_list|(
name|shardEntry
operator|.
name|shardId
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardEntry
operator|.
name|allocationId
argument_list|,
name|shardRouting
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
comment|// sent to the master
name|assertEquals
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|capturedRequests
index|[
literal|0
index|]
operator|.
name|node
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|transport
operator|.
name|handleResponse
argument_list|(
name|capturedRequests
index|[
literal|0
index|]
operator|.
name|requestId
argument_list|,
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|success
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoMaster
specifier|public
name|void
name|testNoMaster
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|String
name|index
init|=
literal|"test"
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|ClusterStateCreationUtils
operator|.
name|stateWithActivePrimary
argument_list|(
name|index
argument_list|,
literal|true
argument_list|,
name|randomInt
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|DiscoveryNodes
operator|.
name|Builder
name|noMasterBuilder
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
argument_list|)
decl_stmt|;
name|noMasterBuilder
operator|.
name|masterNodeId
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|nodes
argument_list|(
name|noMasterBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|AtomicInteger
name|retries
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|AtomicBoolean
name|success
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|setUpMasterRetryVerification
argument_list|(
literal|1
argument_list|,
name|retries
argument_list|,
name|latch
argument_list|,
name|requestId
lambda|->
block|{         }
argument_list|)
expr_stmt|;
name|ShardRouting
name|failedShard
init|=
name|getRandomShardRouting
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|shardStateAction
operator|.
name|localShardFailed
argument_list|(
name|failedShard
argument_list|,
literal|"test"
argument_list|,
name|getSimulatedFailure
argument_list|()
argument_list|,
operator|new
name|ShardStateAction
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|()
block|{
name|success
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
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|success
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
block|}
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|retries
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|success
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testMasterChannelException
specifier|public
name|void
name|testMasterChannelException
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|String
name|index
init|=
literal|"test"
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|ClusterStateCreationUtils
operator|.
name|stateWithActivePrimary
argument_list|(
name|index
argument_list|,
literal|true
argument_list|,
name|randomInt
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|AtomicInteger
name|retries
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|AtomicBoolean
name|success
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|throwable
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|LongConsumer
name|retryLoop
init|=
name|requestId
lambda|->
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|transport
operator|.
name|handleRemoteError
argument_list|(
name|requestId
argument_list|,
name|randomFrom
argument_list|(
operator|new
name|NotMasterException
argument_list|(
literal|"simulated"
argument_list|)
argument_list|,
operator|new
name|Discovery
operator|.
name|FailedToCommitClusterStateException
argument_list|(
literal|"simulated"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|transport
operator|.
name|handleLocalError
argument_list|(
name|requestId
argument_list|,
operator|new
name|NodeNotConnectedException
argument_list|(
literal|null
argument_list|,
literal|"simulated"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|transport
operator|.
name|handleError
argument_list|(
name|requestId
argument_list|,
operator|new
name|NodeDisconnectedException
argument_list|(
literal|null
argument_list|,
name|ShardStateAction
operator|.
name|SHARD_FAILED_ACTION_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
specifier|final
name|int
name|numberOfRetries
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|256
argument_list|)
decl_stmt|;
name|setUpMasterRetryVerification
argument_list|(
name|numberOfRetries
argument_list|,
name|retries
argument_list|,
name|latch
argument_list|,
name|retryLoop
argument_list|)
expr_stmt|;
name|ShardRouting
name|failedShard
init|=
name|getRandomShardRouting
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|shardStateAction
operator|.
name|localShardFailed
argument_list|(
name|failedShard
argument_list|,
literal|"test"
argument_list|,
name|getSimulatedFailure
argument_list|()
argument_list|,
operator|new
name|ShardStateAction
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|()
block|{
name|success
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
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|success
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|throwable
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|CapturingTransport
operator|.
name|CapturedRequest
index|[]
name|capturedRequests
init|=
name|transport
operator|.
name|getCapturedRequestsAndClear
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|capturedRequests
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|success
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|retries
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|retryLoop
operator|.
name|accept
argument_list|(
name|capturedRequests
index|[
literal|0
index|]
operator|.
name|requestId
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|throwable
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|retries
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numberOfRetries
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|success
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnhandledFailure
specifier|public
name|void
name|testUnhandledFailure
parameter_list|()
block|{
specifier|final
name|String
name|index
init|=
literal|"test"
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|ClusterStateCreationUtils
operator|.
name|stateWithActivePrimary
argument_list|(
name|index
argument_list|,
literal|true
argument_list|,
name|randomInt
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|AtomicBoolean
name|failure
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|ShardRouting
name|failedShard
init|=
name|getRandomShardRouting
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|shardStateAction
operator|.
name|localShardFailed
argument_list|(
name|failedShard
argument_list|,
literal|"test"
argument_list|,
name|getSimulatedFailure
argument_list|()
argument_list|,
operator|new
name|ShardStateAction
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|()
block|{
name|failure
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|failure
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|CapturingTransport
operator|.
name|CapturedRequest
index|[]
name|capturedRequests
init|=
name|transport
operator|.
name|getCapturedRequestsAndClear
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|capturedRequests
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|failure
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|transport
operator|.
name|handleRemoteError
argument_list|(
name|capturedRequests
index|[
literal|0
index|]
operator|.
name|requestId
argument_list|,
operator|new
name|TransportException
argument_list|(
literal|"simulated"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|failure
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testShardNotFound
specifier|public
name|void
name|testShardNotFound
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|String
name|index
init|=
literal|"test"
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|ClusterStateCreationUtils
operator|.
name|stateWithActivePrimary
argument_list|(
name|index
argument_list|,
literal|true
argument_list|,
name|randomInt
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|AtomicBoolean
name|success
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ShardRouting
name|failedShard
init|=
name|getRandomShardRouting
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
name|RoutingTable
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|getRoutingTable
argument_list|()
argument_list|)
operator|.
name|remove
argument_list|(
name|index
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|localShardFailed
argument_list|(
name|failedShard
argument_list|,
literal|"test"
argument_list|,
name|getSimulatedFailure
argument_list|()
argument_list|,
operator|new
name|ShardStateAction
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|()
block|{
name|success
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
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|success
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
block|}
argument_list|)
expr_stmt|;
name|CapturingTransport
operator|.
name|CapturedRequest
index|[]
name|capturedRequests
init|=
name|transport
operator|.
name|getCapturedRequestsAndClear
argument_list|()
decl_stmt|;
name|transport
operator|.
name|handleResponse
argument_list|(
name|capturedRequests
index|[
literal|0
index|]
operator|.
name|requestId
argument_list|,
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|success
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoLongerPrimaryShardException
specifier|public
name|void
name|testNoLongerPrimaryShardException
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|String
name|index
init|=
literal|"test"
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|ClusterStateCreationUtils
operator|.
name|stateWithActivePrimary
argument_list|(
name|index
argument_list|,
literal|true
argument_list|,
name|randomInt
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ShardRouting
name|failedShard
init|=
name|getRandomShardRouting
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|failure
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|long
name|primaryTerm
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|index
argument_list|)
operator|.
name|primaryTerm
argument_list|(
name|failedShard
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|primaryTerm
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|remoteShardFailed
argument_list|(
name|failedShard
operator|.
name|shardId
argument_list|()
argument_list|,
name|failedShard
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|primaryTerm
operator|+
literal|1
argument_list|,
literal|"test"
argument_list|,
name|getSimulatedFailure
argument_list|()
argument_list|,
operator|new
name|ShardStateAction
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|()
block|{
name|failure
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
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
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|failure
operator|.
name|set
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
name|ShardStateAction
operator|.
name|NoLongerPrimaryShardException
name|catastrophicError
init|=
operator|new
name|ShardStateAction
operator|.
name|NoLongerPrimaryShardException
argument_list|(
name|failedShard
operator|.
name|shardId
argument_list|()
argument_list|,
literal|"dummy failure"
argument_list|)
decl_stmt|;
name|CapturingTransport
operator|.
name|CapturedRequest
index|[]
name|capturedRequests
init|=
name|transport
operator|.
name|getCapturedRequestsAndClear
argument_list|()
decl_stmt|;
name|transport
operator|.
name|handleRemoteError
argument_list|(
name|capturedRequests
index|[
literal|0
index|]
operator|.
name|requestId
argument_list|,
name|catastrophicError
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|failure
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|failure
operator|.
name|get
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|ShardStateAction
operator|.
name|NoLongerPrimaryShardException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|failure
operator|.
name|get
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|catastrophicError
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getRandomShardRouting
specifier|private
name|ShardRouting
name|getRandomShardRouting
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|ShardsIterator
name|shardsIterator
init|=
name|indexRoutingTable
operator|.
name|randomAllActiveShardsIt
argument_list|()
decl_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|shardsIterator
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
assert|assert
name|shardRouting
operator|!=
literal|null
assert|;
return|return
name|shardRouting
return|;
block|}
DECL|method|setUpMasterRetryVerification
specifier|private
name|void
name|setUpMasterRetryVerification
parameter_list|(
name|int
name|numberOfRetries
parameter_list|,
name|AtomicInteger
name|retries
parameter_list|,
name|CountDownLatch
name|latch
parameter_list|,
name|LongConsumer
name|retryLoop
parameter_list|)
block|{
name|shardStateAction
operator|.
name|setOnBeforeWaitForNewMasterAndRetry
argument_list|(
parameter_list|()
lambda|->
block|{
name|DiscoveryNodes
operator|.
name|Builder
name|masterBuilder
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
argument_list|)
decl_stmt|;
name|masterBuilder
operator|.
name|masterNodeId
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNodes
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|value
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|nodes
argument_list|(
name|masterBuilder
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|setOnAfterWaitForNewMasterAndRetry
argument_list|(
parameter_list|()
lambda|->
name|verifyRetry
argument_list|(
name|numberOfRetries
argument_list|,
name|retries
argument_list|,
name|latch
argument_list|,
name|retryLoop
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|verifyRetry
specifier|private
name|void
name|verifyRetry
parameter_list|(
name|int
name|numberOfRetries
parameter_list|,
name|AtomicInteger
name|retries
parameter_list|,
name|CountDownLatch
name|latch
parameter_list|,
name|LongConsumer
name|retryLoop
parameter_list|)
block|{
comment|// assert a retry request was sent
specifier|final
name|CapturingTransport
operator|.
name|CapturedRequest
index|[]
name|capturedRequests
init|=
name|transport
operator|.
name|getCapturedRequestsAndClear
argument_list|()
decl_stmt|;
if|if
condition|(
name|capturedRequests
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|retries
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|retries
operator|.
name|get
argument_list|()
operator|==
name|numberOfRetries
condition|)
block|{
comment|// finish the request
name|transport
operator|.
name|handleResponse
argument_list|(
name|capturedRequests
index|[
literal|0
index|]
operator|.
name|requestId
argument_list|,
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|retryLoop
operator|.
name|accept
argument_list|(
name|capturedRequests
index|[
literal|0
index|]
operator|.
name|requestId
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// there failed to be a retry request
comment|// release the driver thread to fail the test
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|getSimulatedFailure
specifier|private
name|Exception
name|getSimulatedFailure
parameter_list|()
block|{
return|return
operator|new
name|CorruptIndexException
argument_list|(
literal|"simulated"
argument_list|,
operator|(
name|String
operator|)
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

