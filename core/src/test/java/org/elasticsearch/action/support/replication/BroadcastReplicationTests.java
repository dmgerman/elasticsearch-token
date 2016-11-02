begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.replication
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|replication
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
name|util
operator|.
name|IOUtils
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
name|NoShardAvailableActionException
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
name|ShardOperationFailedException
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
name|UnavailableShardsException
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
name|FlushRequest
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
name|flush
operator|.
name|TransportFlushAction
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
name|ActionFilters
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
name|broadcast
operator|.
name|BroadcastRequest
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
name|broadcast
operator|.
name|BroadcastResponse
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
name|IndexNameExpressionResolver
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
name|ShardRoutingState
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
name|collect
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
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|NamedWriteableRegistry
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
name|network
operator|.
name|NetworkService
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
name|util
operator|.
name|BigArrays
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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
name|breaker
operator|.
name|CircuitBreakerService
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
name|breaker
operator|.
name|NoneCircuitBreakerService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|Task
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
name|MockTcpTransport
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
name|Date
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
name|Set
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
name|Future
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
import|import static
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
operator|.
name|state
import|;
end_import

begin_import
import|import static
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
operator|.
name|stateWithAssignedPrimariesAndOneReplica
import|;
end_import

begin_import
import|import static
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
operator|.
name|stateWithNoShard
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
name|lessThanOrEqualTo
import|;
end_import

begin_class
DECL|class|BroadcastReplicationTests
specifier|public
class|class
name|BroadcastReplicationTests
extends|extends
name|ESTestCase
block|{
DECL|field|threadPool
specifier|private
specifier|static
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|circuitBreakerService
specifier|private
specifier|static
name|CircuitBreakerService
name|circuitBreakerService
decl_stmt|;
DECL|field|clusterService
specifier|private
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|transportService
specifier|private
name|TransportService
name|transportService
decl_stmt|;
DECL|field|broadcastReplicationAction
specifier|private
name|TestBroadcastReplicationAction
name|broadcastReplicationAction
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|beforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
block|{
name|threadPool
operator|=
operator|new
name|TestThreadPool
argument_list|(
literal|"BroadcastReplicationTests"
argument_list|)
expr_stmt|;
name|circuitBreakerService
operator|=
operator|new
name|NoneCircuitBreakerService
argument_list|()
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
name|MockTcpTransport
name|transport
init|=
operator|new
name|MockTcpTransport
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|threadPool
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|,
name|circuitBreakerService
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
argument_list|,
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|clusterService
operator|=
name|createClusterService
argument_list|(
name|threadPool
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
name|threadPool
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
name|broadcastReplicationAction
operator|=
operator|new
name|TestBroadcastReplicationAction
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
operator|new
name|ActionFilters
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|)
argument_list|,
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
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
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|clusterService
argument_list|,
name|transportService
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|afterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
block|{
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|threadPool
argument_list|,
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|threadPool
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testNotStartedPrimary
specifier|public
name|void
name|testNotStartedPrimary
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|IOException
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
name|state
argument_list|(
name|index
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
condition|?
name|ShardRoutingState
operator|.
name|INITIALIZING
else|:
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|,
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"--> using initial state:\n{}"
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|Future
argument_list|<
name|BroadcastResponse
argument_list|>
name|response
init|=
operator|(
name|broadcastReplicationAction
operator|.
name|execute
argument_list|(
operator|new
name|DummyBroadcastRequest
argument_list|()
operator|.
name|indices
argument_list|(
name|index
argument_list|)
argument_list|)
operator|)
decl_stmt|;
for|for
control|(
name|Tuple
argument_list|<
name|ShardId
argument_list|,
name|ActionListener
argument_list|<
name|ReplicationResponse
argument_list|>
argument_list|>
name|shardRequests
range|:
name|broadcastReplicationAction
operator|.
name|capturedShardRequests
control|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|shardRequests
operator|.
name|v2
argument_list|()
operator|.
name|onFailure
argument_list|(
operator|new
name|NoShardAvailableActionException
argument_list|(
name|shardRequests
operator|.
name|v1
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shardRequests
operator|.
name|v2
argument_list|()
operator|.
name|onFailure
argument_list|(
operator|new
name|UnavailableShardsException
argument_list|(
name|shardRequests
operator|.
name|v1
argument_list|()
argument_list|,
literal|"test exception"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|response
operator|.
name|get
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"total shards: {}, "
argument_list|,
name|response
operator|.
name|get
argument_list|()
operator|.
name|getTotalShards
argument_list|()
argument_list|)
expr_stmt|;
comment|// we expect no failures here because UnavailableShardsException does not count as failed
name|assertBroadcastResponse
argument_list|(
literal|2
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|response
operator|.
name|get
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testStartedPrimary
specifier|public
name|void
name|testStartedPrimary
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|IOException
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
name|state
argument_list|(
name|index
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"--> using initial state:\n{}"
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|Future
argument_list|<
name|BroadcastResponse
argument_list|>
name|response
init|=
operator|(
name|broadcastReplicationAction
operator|.
name|execute
argument_list|(
operator|new
name|DummyBroadcastRequest
argument_list|()
operator|.
name|indices
argument_list|(
name|index
argument_list|)
argument_list|)
operator|)
decl_stmt|;
for|for
control|(
name|Tuple
argument_list|<
name|ShardId
argument_list|,
name|ActionListener
argument_list|<
name|ReplicationResponse
argument_list|>
argument_list|>
name|shardRequests
range|:
name|broadcastReplicationAction
operator|.
name|capturedShardRequests
control|)
block|{
name|ReplicationResponse
name|replicationResponse
init|=
operator|new
name|ReplicationResponse
argument_list|()
decl_stmt|;
name|replicationResponse
operator|.
name|setShardInfo
argument_list|(
operator|new
name|ReplicationResponse
operator|.
name|ShardInfo
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|shardRequests
operator|.
name|v2
argument_list|()
operator|.
name|onResponse
argument_list|(
name|replicationResponse
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"total shards: {}, "
argument_list|,
name|response
operator|.
name|get
argument_list|()
operator|.
name|getTotalShards
argument_list|()
argument_list|)
expr_stmt|;
name|assertBroadcastResponse
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
name|response
operator|.
name|get
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testResultCombine
specifier|public
name|void
name|testResultCombine
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|IOException
block|{
specifier|final
name|String
name|index
init|=
literal|"test"
decl_stmt|;
name|int
name|numShards
init|=
literal|1
operator|+
name|randomInt
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|stateWithAssignedPrimariesAndOneReplica
argument_list|(
name|index
argument_list|,
name|numShards
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"--> using initial state:\n{}"
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|Future
argument_list|<
name|BroadcastResponse
argument_list|>
name|response
init|=
operator|(
name|broadcastReplicationAction
operator|.
name|execute
argument_list|(
operator|new
name|DummyBroadcastRequest
argument_list|()
operator|.
name|indices
argument_list|(
name|index
argument_list|)
argument_list|)
operator|)
decl_stmt|;
name|int
name|succeeded
init|=
literal|0
decl_stmt|;
name|int
name|failed
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Tuple
argument_list|<
name|ShardId
argument_list|,
name|ActionListener
argument_list|<
name|ReplicationResponse
argument_list|>
argument_list|>
name|shardRequests
range|:
name|broadcastReplicationAction
operator|.
name|capturedShardRequests
control|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|ReplicationResponse
operator|.
name|ShardInfo
operator|.
name|Failure
index|[]
name|failures
init|=
operator|new
name|ReplicationResponse
operator|.
name|ShardInfo
operator|.
name|Failure
index|[
literal|0
index|]
decl_stmt|;
name|int
name|shardsSucceeded
init|=
name|randomInt
argument_list|(
literal|1
argument_list|)
operator|+
literal|1
decl_stmt|;
name|succeeded
operator|+=
name|shardsSucceeded
expr_stmt|;
name|ReplicationResponse
name|replicationResponse
init|=
operator|new
name|ReplicationResponse
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardsSucceeded
operator|==
literal|1
operator|&&
name|randomBoolean
argument_list|()
condition|)
block|{
comment|//sometimes add failure (no failure means shard unavailable)
name|failures
operator|=
operator|new
name|ReplicationResponse
operator|.
name|ShardInfo
operator|.
name|Failure
index|[
literal|1
index|]
expr_stmt|;
name|failures
index|[
literal|0
index|]
operator|=
operator|new
name|ReplicationResponse
operator|.
name|ShardInfo
operator|.
name|Failure
argument_list|(
name|shardRequests
operator|.
name|v1
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|Exception
argument_list|(
literal|"pretend shard failed"
argument_list|)
argument_list|,
name|RestStatus
operator|.
name|GATEWAY_TIMEOUT
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|failed
operator|++
expr_stmt|;
block|}
name|replicationResponse
operator|.
name|setShardInfo
argument_list|(
operator|new
name|ReplicationResponse
operator|.
name|ShardInfo
argument_list|(
literal|2
argument_list|,
name|shardsSucceeded
argument_list|,
name|failures
argument_list|)
argument_list|)
expr_stmt|;
name|shardRequests
operator|.
name|v2
argument_list|()
operator|.
name|onResponse
argument_list|(
name|replicationResponse
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// sometimes fail
name|failed
operator|+=
literal|2
expr_stmt|;
comment|// just add a general exception and see if failed shards will be incremented by 2
name|shardRequests
operator|.
name|v2
argument_list|()
operator|.
name|onFailure
argument_list|(
operator|new
name|Exception
argument_list|(
literal|"pretend shard failed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertBroadcastResponse
argument_list|(
literal|2
operator|*
name|numShards
argument_list|,
name|succeeded
argument_list|,
name|failed
argument_list|,
name|response
operator|.
name|get
argument_list|()
argument_list|,
name|Exception
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoShards
specifier|public
name|void
name|testNoShards
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|IOException
block|{
name|setState
argument_list|(
name|clusterService
argument_list|,
name|stateWithNoShard
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"--> using initial state:\n{}"
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|BroadcastResponse
name|response
init|=
name|executeAndAssertImmediateResponse
argument_list|(
name|broadcastReplicationAction
argument_list|,
operator|new
name|DummyBroadcastRequest
argument_list|()
argument_list|)
decl_stmt|;
name|assertBroadcastResponse
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|response
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testShardsList
specifier|public
name|void
name|testShardsList
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
specifier|final
name|String
name|index
init|=
literal|"test"
decl_stmt|;
specifier|final
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
literal|"_na_"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|state
argument_list|(
name|index
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
condition|?
name|ShardRoutingState
operator|.
name|INITIALIZING
else|:
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|,
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"--> using initial state:\n{}"
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ShardId
argument_list|>
name|shards
init|=
name|broadcastReplicationAction
operator|.
name|shards
argument_list|(
operator|new
name|DummyBroadcastRequest
argument_list|()
operator|.
name|indices
argument_list|(
name|shardId
operator|.
name|getIndexName
argument_list|()
argument_list|)
argument_list|,
name|clusterState
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|shards
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shards
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|shardId
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|TestBroadcastReplicationAction
specifier|private
class|class
name|TestBroadcastReplicationAction
extends|extends
name|TransportBroadcastReplicationAction
argument_list|<
name|DummyBroadcastRequest
argument_list|,
name|BroadcastResponse
argument_list|,
name|BasicReplicationRequest
argument_list|,
name|ReplicationResponse
argument_list|>
block|{
DECL|field|capturedShardRequests
specifier|protected
specifier|final
name|Set
argument_list|<
name|Tuple
argument_list|<
name|ShardId
argument_list|,
name|ActionListener
argument_list|<
name|ReplicationResponse
argument_list|>
argument_list|>
argument_list|>
name|capturedShardRequests
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentSet
argument_list|()
decl_stmt|;
DECL|method|TestBroadcastReplicationAction
specifier|public
name|TestBroadcastReplicationAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|TransportReplicationAction
name|replicatedBroadcastShardAction
parameter_list|)
block|{
name|super
argument_list|(
literal|"test-broadcast-replication-action"
argument_list|,
name|DummyBroadcastRequest
operator|::
operator|new
argument_list|,
name|settings
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|replicatedBroadcastShardAction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newShardResponse
specifier|protected
name|ReplicationResponse
name|newShardResponse
parameter_list|()
block|{
return|return
operator|new
name|ReplicationResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newShardRequest
specifier|protected
name|BasicReplicationRequest
name|newShardRequest
parameter_list|(
name|DummyBroadcastRequest
name|request
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
block|{
return|return
operator|new
name|BasicReplicationRequest
argument_list|()
operator|.
name|setShardId
argument_list|(
name|shardId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|BroadcastResponse
name|newResponse
parameter_list|(
name|int
name|successfulShards
parameter_list|,
name|int
name|failedShards
parameter_list|,
name|int
name|totalNumCopies
parameter_list|,
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|shardFailures
parameter_list|)
block|{
return|return
operator|new
name|BroadcastResponse
argument_list|(
name|totalNumCopies
argument_list|,
name|successfulShards
argument_list|,
name|failedShards
argument_list|,
name|shardFailures
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|shardExecute
specifier|protected
name|void
name|shardExecute
parameter_list|(
name|Task
name|task
parameter_list|,
name|DummyBroadcastRequest
name|request
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|ActionListener
argument_list|<
name|ReplicationResponse
argument_list|>
name|shardActionListener
parameter_list|)
block|{
name|capturedShardRequests
operator|.
name|add
argument_list|(
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|shardId
argument_list|,
name|shardActionListener
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertImmediateResponse
specifier|public
name|FlushResponse
name|assertImmediateResponse
parameter_list|(
name|String
name|index
parameter_list|,
name|TransportFlushAction
name|flushAction
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|Date
name|beginDate
init|=
operator|new
name|Date
argument_list|()
decl_stmt|;
name|FlushResponse
name|flushResponse
init|=
name|flushAction
operator|.
name|execute
argument_list|(
operator|new
name|FlushRequest
argument_list|(
name|index
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|Date
name|endDate
init|=
operator|new
name|Date
argument_list|()
decl_stmt|;
name|long
name|maxTime
init|=
literal|500
decl_stmt|;
name|assertThat
argument_list|(
literal|"this should not take longer than "
operator|+
name|maxTime
operator|+
literal|" ms. The request hangs somewhere"
argument_list|,
name|endDate
operator|.
name|getTime
argument_list|()
operator|-
name|beginDate
operator|.
name|getTime
argument_list|()
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|maxTime
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|flushResponse
return|;
block|}
DECL|method|executeAndAssertImmediateResponse
specifier|public
name|BroadcastResponse
name|executeAndAssertImmediateResponse
parameter_list|(
name|TransportBroadcastReplicationAction
name|broadcastAction
parameter_list|,
name|DummyBroadcastRequest
name|request
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
return|return
operator|(
name|BroadcastResponse
operator|)
name|broadcastAction
operator|.
name|execute
argument_list|(
name|request
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|"5s"
argument_list|)
return|;
block|}
DECL|method|assertBroadcastResponse
specifier|private
name|void
name|assertBroadcastResponse
parameter_list|(
name|int
name|total
parameter_list|,
name|int
name|successful
parameter_list|,
name|int
name|failed
parameter_list|,
name|BroadcastResponse
name|response
parameter_list|,
name|Class
name|exceptionClass
parameter_list|)
block|{
name|assertThat
argument_list|(
name|response
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|successful
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getTotalShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|total
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|failed
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|failed
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|response
operator|.
name|getShardFailures
argument_list|()
index|[
literal|0
index|]
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|exceptionClass
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|DummyBroadcastRequest
specifier|public
specifier|static
class|class
name|DummyBroadcastRequest
extends|extends
name|BroadcastRequest
argument_list|<
name|DummyBroadcastRequest
argument_list|>
block|{      }
block|}
end_class

end_unit

