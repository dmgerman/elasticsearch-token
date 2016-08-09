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
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectCursor
import|;
end_import

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
name|ClusterName
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
name|ClusterStateTaskExecutor
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
name|metadata
operator|.
name|MetaData
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
name|GroupShardsIterator
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
name|RoutingNodes
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
name|ShardIterator
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
name|routing
operator|.
name|allocation
operator|.
name|FailedRerouteAllocation
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
name|RoutingAllocation
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
name|decider
operator|.
name|ClusterRebalanceAllocationDecider
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
name|UUIDs
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
name|Index
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
name|ESAllocationTestCase
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
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
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
name|CoreMatchers
operator|.
name|not
import|;
end_import

begin_class
DECL|class|ShardFailedClusterStateTaskExecutorTests
specifier|public
class|class
name|ShardFailedClusterStateTaskExecutorTests
extends|extends
name|ESAllocationTestCase
block|{
DECL|field|INDEX
specifier|private
specifier|static
specifier|final
name|String
name|INDEX
init|=
literal|"INDEX"
decl_stmt|;
DECL|field|allocationService
specifier|private
name|AllocationService
name|allocationService
decl_stmt|;
DECL|field|numberOfReplicas
specifier|private
name|int
name|numberOfReplicas
decl_stmt|;
DECL|field|metaData
specifier|private
name|MetaData
name|metaData
decl_stmt|;
DECL|field|routingTable
specifier|private
name|RoutingTable
name|routingTable
decl_stmt|;
DECL|field|clusterState
specifier|private
name|ClusterState
name|clusterState
decl_stmt|;
DECL|field|executor
specifier|private
name|ShardStateAction
operator|.
name|ShardFailedClusterStateTaskExecutor
name|executor
decl_stmt|;
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
name|allocationService
operator|=
name|createAllocationService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.node_concurrent_recoveries"
argument_list|,
literal|8
argument_list|)
operator|.
name|put
argument_list|(
name|ClusterRebalanceAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_ALLOW_REBALANCE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"always"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|numberOfReplicas
operator|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|16
argument_list|)
expr_stmt|;
name|metaData
operator|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|INDEX
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
name|numberOfReplicas
argument_list|)
operator|.
name|primaryTerm
argument_list|(
literal|0
argument_list|,
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|routingTable
operator|=
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|addAsNew
argument_list|(
name|metaData
operator|.
name|index
argument_list|(
name|INDEX
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|ClusterName
operator|.
name|CLUSTER_NAME_SETTING
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|executor
operator|=
operator|new
name|ShardStateAction
operator|.
name|ShardFailedClusterStateTaskExecutor
argument_list|(
name|allocationService
argument_list|,
literal|null
argument_list|,
name|logger
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmptyTaskListProducesSameClusterState
specifier|public
name|void
name|testEmptyTaskListProducesSameClusterState
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|tasks
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
name|ClusterStateTaskExecutor
operator|.
name|BatchResult
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|result
init|=
name|executor
operator|.
name|execute
argument_list|(
name|clusterState
argument_list|,
name|tasks
argument_list|)
decl_stmt|;
name|assertTasksSuccessful
argument_list|(
name|tasks
argument_list|,
name|result
argument_list|,
name|clusterState
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testDuplicateFailuresAreOkay
specifier|public
name|void
name|testDuplicateFailuresAreOkay
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|reason
init|=
literal|"test duplicate failures are okay"
decl_stmt|;
name|ClusterState
name|currentState
init|=
name|createClusterStateWithStartedShards
argument_list|(
name|reason
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|tasks
init|=
name|createExistingShards
argument_list|(
name|currentState
argument_list|,
name|reason
argument_list|)
decl_stmt|;
name|ClusterStateTaskExecutor
operator|.
name|BatchResult
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|result
init|=
name|executor
operator|.
name|execute
argument_list|(
name|currentState
argument_list|,
name|tasks
argument_list|)
decl_stmt|;
name|assertTasksSuccessful
argument_list|(
name|tasks
argument_list|,
name|result
argument_list|,
name|clusterState
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|testNonExistentShardsAreMarkedAsSuccessful
specifier|public
name|void
name|testNonExistentShardsAreMarkedAsSuccessful
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|reason
init|=
literal|"test non existent shards are marked as successful"
decl_stmt|;
name|ClusterState
name|currentState
init|=
name|createClusterStateWithStartedShards
argument_list|(
name|reason
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|tasks
init|=
name|createNonExistentShards
argument_list|(
name|currentState
argument_list|,
name|reason
argument_list|)
decl_stmt|;
name|ClusterStateTaskExecutor
operator|.
name|BatchResult
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|result
init|=
name|executor
operator|.
name|execute
argument_list|(
name|clusterState
argument_list|,
name|tasks
argument_list|)
decl_stmt|;
name|assertTasksSuccessful
argument_list|(
name|tasks
argument_list|,
name|result
argument_list|,
name|clusterState
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testTriviallySuccessfulTasksBatchedWithFailingTasks
specifier|public
name|void
name|testTriviallySuccessfulTasksBatchedWithFailingTasks
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|reason
init|=
literal|"test trivially successful tasks batched with failing tasks"
decl_stmt|;
name|ClusterState
name|currentState
init|=
name|createClusterStateWithStartedShards
argument_list|(
name|reason
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|failingTasks
init|=
name|createExistingShards
argument_list|(
name|currentState
argument_list|,
name|reason
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|nonExistentTasks
init|=
name|createNonExistentShards
argument_list|(
name|currentState
argument_list|,
name|reason
argument_list|)
decl_stmt|;
name|ShardStateAction
operator|.
name|ShardFailedClusterStateTaskExecutor
name|failingExecutor
init|=
operator|new
name|ShardStateAction
operator|.
name|ShardFailedClusterStateTaskExecutor
argument_list|(
name|allocationService
argument_list|,
literal|null
argument_list|,
name|logger
argument_list|)
block|{
annotation|@
name|Override
name|RoutingAllocation
operator|.
name|Result
name|applyFailedShards
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|List
argument_list|<
name|FailedRerouteAllocation
operator|.
name|FailedShard
argument_list|>
name|failedShards
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"simulated applyFailedShards failure"
argument_list|)
throw|;
block|}
block|}
decl_stmt|;
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|tasks
operator|.
name|addAll
argument_list|(
name|failingTasks
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|addAll
argument_list|(
name|nonExistentTasks
argument_list|)
expr_stmt|;
name|ClusterStateTaskExecutor
operator|.
name|BatchResult
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|result
init|=
name|failingExecutor
operator|.
name|execute
argument_list|(
name|currentState
argument_list|,
name|tasks
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|,
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
argument_list|>
name|taskResultMap
init|=
name|failingTasks
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|task
lambda|->
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
operator|.
name|failure
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"simulated applyFailedShards failure"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|taskResultMap
operator|.
name|putAll
argument_list|(
name|nonExistentTasks
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|task
lambda|->
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
operator|.
name|success
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTaskResults
argument_list|(
name|taskResultMap
argument_list|,
name|result
argument_list|,
name|currentState
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testIllegalShardFailureRequests
specifier|public
name|void
name|testIllegalShardFailureRequests
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|reason
init|=
literal|"test illegal shard failure requests"
decl_stmt|;
name|ClusterState
name|currentState
init|=
name|createClusterStateWithStartedShards
argument_list|(
name|reason
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|failingTasks
init|=
name|createExistingShards
argument_list|(
name|currentState
argument_list|,
name|reason
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardStateAction
operator|.
name|ShardEntry
name|failingTask
range|:
name|failingTasks
control|)
block|{
name|long
name|primaryTerm
init|=
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|failingTask
operator|.
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|)
operator|.
name|primaryTerm
argument_list|(
name|failingTask
operator|.
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|tasks
operator|.
name|add
argument_list|(
operator|new
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|(
name|failingTask
operator|.
name|shardId
argument_list|,
name|failingTask
operator|.
name|allocationId
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
operator|(
name|int
operator|)
name|primaryTerm
operator|-
literal|1
argument_list|)
argument_list|,
name|failingTask
operator|.
name|message
argument_list|,
name|failingTask
operator|.
name|failure
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|,
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
argument_list|>
name|taskResultMap
init|=
name|tasks
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|task
lambda|->
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
operator|.
name|failure
argument_list|(
operator|new
name|ShardStateAction
operator|.
name|NoLongerPrimaryShardException
argument_list|(
name|task
operator|.
name|shardId
argument_list|,
literal|"primary term ["
operator|+
name|task
operator|.
name|primaryTerm
operator|+
literal|"] did not match current primary term ["
operator|+
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|task
operator|.
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|)
operator|.
name|primaryTerm
argument_list|(
name|task
operator|.
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
operator|+
literal|"]"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|ClusterStateTaskExecutor
operator|.
name|BatchResult
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|result
init|=
name|executor
operator|.
name|execute
argument_list|(
name|currentState
argument_list|,
name|tasks
argument_list|)
decl_stmt|;
name|assertTaskResults
argument_list|(
name|taskResultMap
argument_list|,
name|result
argument_list|,
name|currentState
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|createClusterStateWithStartedShards
specifier|private
name|ClusterState
name|createClusterStateWithStartedShards
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|int
name|numberOfNodes
init|=
literal|1
operator|+
name|numberOfReplicas
decl_stmt|;
name|DiscoveryNodes
operator|.
name|Builder
name|nodes
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
decl_stmt|;
name|IntStream
operator|.
name|rangeClosed
argument_list|(
literal|1
argument_list|,
name|numberOfNodes
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|node
lambda|->
name|newNode
argument_list|(
literal|"node"
operator|+
name|node
argument_list|)
argument_list|)
operator|.
name|forEach
argument_list|(
name|nodes
operator|::
name|add
argument_list|)
expr_stmt|;
name|ClusterState
name|stateAfterAddingNode
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|nodes
argument_list|(
name|nodes
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
name|afterReroute
init|=
name|allocationService
operator|.
name|reroute
argument_list|(
name|stateAfterAddingNode
argument_list|,
name|reason
argument_list|)
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|ClusterState
name|stateAfterReroute
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|stateAfterAddingNode
argument_list|)
operator|.
name|routingTable
argument_list|(
name|afterReroute
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingNodes
name|routingNodes
init|=
name|stateAfterReroute
operator|.
name|getRoutingNodes
argument_list|()
decl_stmt|;
name|RoutingTable
name|afterStart
init|=
name|allocationService
operator|.
name|applyStartedShards
argument_list|(
name|stateAfterReroute
argument_list|,
name|routingNodes
operator|.
name|shardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
argument_list|)
operator|.
name|routingTable
argument_list|()
decl_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|stateAfterReroute
argument_list|)
operator|.
name|routingTable
argument_list|(
name|afterStart
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|createExistingShards
specifier|private
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|createExistingShards
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|GroupShardsIterator
name|shardGroups
init|=
name|currentState
operator|.
name|routingTable
argument_list|()
operator|.
name|allAssignedShardsGrouped
argument_list|(
operator|new
name|String
index|[]
block|{
name|INDEX
block|}
argument_list|,
literal|true
argument_list|)
decl_stmt|;
for|for
control|(
name|ShardIterator
name|shardIt
range|:
name|shardGroups
control|)
block|{
for|for
control|(
name|ShardRouting
name|shard
range|:
name|shardIt
operator|.
name|asUnordered
argument_list|()
control|)
block|{
name|shards
operator|.
name|add
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|failures
init|=
name|randomSubsetOf
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1
operator|+
name|shards
operator|.
name|size
argument_list|()
operator|/
literal|4
argument_list|)
argument_list|,
name|shards
operator|.
name|toArray
argument_list|(
operator|new
name|ShardRouting
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|indexUUID
init|=
name|metaData
operator|.
name|index
argument_list|(
name|INDEX
argument_list|)
operator|.
name|getIndexUUID
argument_list|()
decl_stmt|;
name|int
name|numberOfTasks
init|=
name|randomIntBetween
argument_list|(
name|failures
operator|.
name|size
argument_list|()
argument_list|,
literal|2
operator|*
name|failures
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shardsToFail
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numberOfTasks
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
name|numberOfTasks
condition|;
name|i
operator|++
control|)
block|{
name|shardsToFail
operator|.
name|add
argument_list|(
name|randomFrom
argument_list|(
name|failures
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|toTasks
argument_list|(
name|currentState
argument_list|,
name|shardsToFail
argument_list|,
name|indexUUID
argument_list|,
name|reason
argument_list|)
return|;
block|}
DECL|method|createNonExistentShards
specifier|private
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|createNonExistentShards
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
comment|// add shards from a non-existent index
name|String
name|nonExistentIndexUUID
init|=
literal|"non-existent"
decl_stmt|;
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"non-existent"
argument_list|,
name|nonExistentIndexUUID
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|nodeIds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ObjectCursor
argument_list|<
name|String
argument_list|>
name|nodeId
range|:
name|currentState
operator|.
name|nodes
argument_list|()
operator|.
name|getNodes
argument_list|()
operator|.
name|keys
argument_list|()
control|)
block|{
name|nodeIds
operator|.
name|add
argument_list|(
name|nodeId
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|nonExistentShards
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|nonExistentShards
operator|.
name|add
argument_list|(
name|nonExistentShardRouting
argument_list|(
name|index
argument_list|,
name|nodeIds
argument_list|,
literal|true
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
name|numberOfReplicas
condition|;
name|i
operator|++
control|)
block|{
name|nonExistentShards
operator|.
name|add
argument_list|(
name|nonExistentShardRouting
argument_list|(
name|index
argument_list|,
name|nodeIds
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|existingShards
init|=
name|createExistingShards
argument_list|(
name|currentState
argument_list|,
name|reason
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|shardsWithMismatchedAllocationIds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardStateAction
operator|.
name|ShardEntry
name|existingShard
range|:
name|existingShards
control|)
block|{
name|shardsWithMismatchedAllocationIds
operator|.
name|add
argument_list|(
operator|new
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|(
name|existingShard
operator|.
name|shardId
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|,
literal|0L
argument_list|,
name|existingShard
operator|.
name|message
argument_list|,
name|existingShard
operator|.
name|failure
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|nonExistentShards
operator|.
name|forEach
argument_list|(
name|shard
lambda|->
name|tasks
operator|.
name|add
argument_list|(
operator|new
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|shard
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
literal|0L
argument_list|,
name|reason
argument_list|,
operator|new
name|CorruptIndexException
argument_list|(
literal|"simulated"
argument_list|,
name|nonExistentIndexUUID
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|addAll
argument_list|(
name|shardsWithMismatchedAllocationIds
argument_list|)
expr_stmt|;
return|return
name|tasks
return|;
block|}
DECL|method|nonExistentShardRouting
specifier|private
name|ShardRouting
name|nonExistentShardRouting
parameter_list|(
name|Index
name|index
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|nodeIds
parameter_list|,
name|boolean
name|primary
parameter_list|)
block|{
return|return
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
literal|0
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|nodeIds
argument_list|)
argument_list|,
name|primary
argument_list|,
name|randomFrom
argument_list|(
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|,
name|ShardRoutingState
operator|.
name|RELOCATING
argument_list|,
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
argument_list|)
return|;
block|}
DECL|method|assertTasksSuccessful
specifier|private
specifier|static
name|void
name|assertTasksSuccessful
parameter_list|(
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|tasks
parameter_list|,
name|ClusterStateTaskExecutor
operator|.
name|BatchResult
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|result
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|,
name|boolean
name|clusterStateChanged
parameter_list|)
block|{
name|Map
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|,
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
argument_list|>
name|taskResultMap
init|=
name|tasks
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|task
lambda|->
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
operator|.
name|success
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertTaskResults
argument_list|(
name|taskResultMap
argument_list|,
name|result
argument_list|,
name|clusterState
argument_list|,
name|clusterStateChanged
argument_list|)
expr_stmt|;
block|}
DECL|method|assertTaskResults
specifier|private
specifier|static
name|void
name|assertTaskResults
parameter_list|(
name|Map
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|,
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
argument_list|>
name|taskResultMap
parameter_list|,
name|ClusterStateTaskExecutor
operator|.
name|BatchResult
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|result
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|,
name|boolean
name|clusterStateChanged
parameter_list|)
block|{
comment|// there should be as many task results as tasks
name|assertEquals
argument_list|(
name|taskResultMap
operator|.
name|size
argument_list|()
argument_list|,
name|result
operator|.
name|executionResults
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|,
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
argument_list|>
name|entry
range|:
name|taskResultMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// every task should have a corresponding task result
name|assertTrue
argument_list|(
name|result
operator|.
name|executionResults
operator|.
name|containsKey
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// the task results are as expected
name|assertEquals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|isSuccess
argument_list|()
argument_list|,
name|result
operator|.
name|executionResults
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|isSuccess
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
init|=
name|clusterState
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|allShards
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|,
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
argument_list|>
name|entry
range|:
name|taskResultMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|isSuccess
argument_list|()
condition|)
block|{
comment|// the shard was successfully failed and so should not be in the routing table
for|for
control|(
name|ShardRouting
name|shard
range|:
name|shards
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|assignedToNode
argument_list|()
condition|)
block|{
name|assertFalse
argument_list|(
literal|"entry key "
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|", shard routing "
operator|+
name|shard
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getShardId
argument_list|()
operator|.
name|equals
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
operator|&&
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getAllocationId
argument_list|()
operator|.
name|equals
argument_list|(
name|shard
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// check we saw the expected failure
name|ClusterStateTaskExecutor
operator|.
name|TaskResult
name|actualResult
init|=
name|result
operator|.
name|executionResults
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|actualResult
operator|.
name|getFailure
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getFailure
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualResult
operator|.
name|getFailure
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getFailure
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|clusterStateChanged
condition|)
block|{
name|assertNotSame
argument_list|(
name|clusterState
argument_list|,
name|result
operator|.
name|resultingState
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertSame
argument_list|(
name|clusterState
argument_list|,
name|result
operator|.
name|resultingState
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|toTasks
specifier|private
specifier|static
name|List
argument_list|<
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|>
name|toTasks
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
parameter_list|,
name|String
name|indexUUID
parameter_list|,
name|String
name|message
parameter_list|)
block|{
return|return
name|shards
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|shard
lambda|->
operator|new
name|ShardStateAction
operator|.
name|ShardEntry
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|shard
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
condition|?
literal|0L
else|:
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|getIndexSafe
argument_list|(
name|shard
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|primaryTerm
argument_list|(
name|shard
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
name|message
argument_list|,
operator|new
name|CorruptIndexException
argument_list|(
literal|"simulated"
argument_list|,
name|indexUUID
argument_list|)
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

