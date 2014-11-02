begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
package|;
end_package

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
name|ClusterInfoService
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
name|metadata
operator|.
name|MetaData
operator|.
name|Builder
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
name|RoutingNode
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
name|allocation
operator|.
name|allocator
operator|.
name|ShardsAllocators
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
name|AllocationDecider
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
name|AllocationDeciders
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
name|Decision
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
name|SameShardAllocationDecider
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
name|ImmutableSettings
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
name|ElasticsearchAllocationTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRoutingState
operator|.
name|INITIALIZING
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
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
DECL|class|RandomAllocationDeciderTests
specifier|public
class|class
name|RandomAllocationDeciderTests
extends|extends
name|ElasticsearchAllocationTestCase
block|{
comment|/* This test will make random allocation decision on a growing and shrinking      * cluster leading to a random distribution of the shards. After a certain      * amount of iterations the test allows allocation unless the same shard is      * already allocated on a node and balances the cluster to gain optimal      * balance.*/
annotation|@
name|Test
DECL|method|testRandomDecisions
specifier|public
name|void
name|testRandomDecisions
parameter_list|()
block|{
name|RandomAllocationDecider
name|randomAllocationDecider
init|=
operator|new
name|RandomAllocationDecider
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|AllocationService
name|strategy
init|=
operator|new
name|AllocationService
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|AllocationDeciders
argument_list|(
name|ImmutableSettings
operator|.
name|EMPTY
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|SameShardAllocationDecider
argument_list|(
name|ImmutableSettings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|randomAllocationDecider
argument_list|)
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ShardsAllocators
argument_list|()
argument_list|,
name|ClusterInfoService
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|int
name|indices
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|Builder
name|metaBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
decl_stmt|;
name|int
name|maxNumReplicas
init|=
literal|1
decl_stmt|;
name|int
name|totalNumShards
init|=
literal|0
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
name|indices
condition|;
name|i
operator|++
control|)
block|{
name|int
name|replicas
init|=
name|scaledRandomIntBetween
argument_list|(
literal|0
argument_list|,
literal|6
argument_list|)
decl_stmt|;
name|maxNumReplicas
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxNumReplicas
argument_list|,
name|replicas
operator|+
literal|1
argument_list|)
expr_stmt|;
name|int
name|numShards
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|totalNumShards
operator|+=
name|numShards
operator|*
operator|(
name|replicas
operator|+
literal|1
operator|)
expr_stmt|;
name|metaBuilder
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"INDEX_"
operator|+
name|i
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
name|numShards
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
name|replicas
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|MetaData
name|metaData
init|=
name|metaBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
operator|.
name|Builder
name|routingTableBuilder
init|=
name|RoutingTable
operator|.
name|builder
argument_list|()
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
name|indices
condition|;
name|i
operator|++
control|)
block|{
name|routingTableBuilder
operator|.
name|addAsNew
argument_list|(
name|metaData
operator|.
name|index
argument_list|(
literal|"INDEX_"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|RoutingTable
name|routingTable
init|=
name|routingTableBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterName
operator|.
name|DEFAULT
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
decl_stmt|;
name|int
name|numIters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|5
argument_list|,
literal|15
argument_list|)
decl_stmt|;
name|int
name|nodeIdCounter
init|=
literal|0
decl_stmt|;
name|int
name|atMostNodes
init|=
name|scaledRandomIntBetween
argument_list|(
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|maxNumReplicas
argument_list|)
argument_list|,
literal|15
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|frequentNodes
init|=
name|randomBoolean
argument_list|()
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
name|logger
operator|.
name|info
argument_list|(
literal|"Start iteration [{}]"
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|ClusterState
operator|.
name|Builder
name|stateBuilder
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
decl_stmt|;
name|DiscoveryNodes
operator|.
name|Builder
name|newNodesBuilder
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
operator|<=
name|atMostNodes
operator|&&
operator|(
name|nodeIdCounter
operator|==
literal|0
operator|||
operator|(
name|frequentNodes
condition|?
name|frequently
argument_list|()
else|:
name|rarely
argument_list|()
operator|)
operator|)
condition|)
block|{
name|int
name|numNodes
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
literal|3
argument_list|)
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
name|numNodes
condition|;
name|j
operator|++
control|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"adding node [{}]"
argument_list|,
name|nodeIdCounter
argument_list|)
expr_stmt|;
name|newNodesBuilder
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"NODE_"
operator|+
operator|(
name|nodeIdCounter
operator|++
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|nodeIdCounter
operator|>
literal|1
operator|&&
name|rarely
argument_list|()
condition|)
block|{
name|int
name|nodeId
init|=
name|scaledRandomIntBetween
argument_list|(
literal|0
argument_list|,
name|nodeIdCounter
operator|-
literal|2
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"removing node [{}]"
argument_list|,
name|nodeId
argument_list|)
expr_stmt|;
name|newNodesBuilder
operator|.
name|remove
argument_list|(
literal|"NODE_"
operator|+
name|nodeId
argument_list|)
expr_stmt|;
block|}
name|stateBuilder
operator|.
name|nodes
argument_list|(
name|newNodesBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|stateBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
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
if|if
condition|(
name|clusterState
operator|.
name|routingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|routingTable
operator|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|clusterState
operator|.
name|routingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
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
block|}
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Fill up nodes such that every shard can be allocated"
argument_list|)
expr_stmt|;
if|if
condition|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
operator|<
name|maxNumReplicas
condition|)
block|{
name|ClusterState
operator|.
name|Builder
name|stateBuilder
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
decl_stmt|;
name|DiscoveryNodes
operator|.
name|Builder
name|newNodesBuilder
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|)
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
operator|(
name|maxNumReplicas
operator|-
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
operator|)
condition|;
name|j
operator|++
control|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"adding node [{}]"
argument_list|,
name|nodeIdCounter
argument_list|)
expr_stmt|;
name|newNodesBuilder
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"NODE_"
operator|+
operator|(
name|nodeIdCounter
operator|++
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|stateBuilder
operator|.
name|nodes
argument_list|(
name|newNodesBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|stateBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|randomAllocationDecider
operator|.
name|alwaysSayYes
operator|=
literal|true
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"now say YES to everything"
argument_list|)
expr_stmt|;
name|int
name|iterations
init|=
literal|0
decl_stmt|;
do|do
block|{
name|iterations
operator|++
expr_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
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
if|if
condition|(
name|clusterState
operator|.
name|routingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|routingTable
operator|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|clusterState
operator|.
name|routingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
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
block|}
block|}
do|while
condition|(
name|clusterState
operator|.
name|routingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
operator|.
name|size
argument_list|()
operator|!=
literal|0
operator|||
name|clusterState
operator|.
name|routingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|)
operator|.
name|size
argument_list|()
operator|!=
literal|0
operator|&&
name|iterations
operator|<
literal|200
condition|)
do|;
name|logger
operator|.
name|info
argument_list|(
literal|"Done Balancing after [{}] iterations"
argument_list|,
name|iterations
argument_list|)
expr_stmt|;
comment|// we stop after 200 iterations if it didn't stabelize by then something is likely to be wrong
name|assertThat
argument_list|(
literal|"max num iteration exceeded"
argument_list|,
name|iterations
argument_list|,
name|Matchers
operator|.
name|lessThan
argument_list|(
literal|200
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterState
operator|.
name|routingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|)
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
name|assertThat
argument_list|(
name|clusterState
operator|.
name|routingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|)
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
name|int
name|shards
init|=
name|clusterState
operator|.
name|routingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|shards
argument_list|,
name|equalTo
argument_list|(
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numNodes
init|=
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|int
name|upperBound
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|round
argument_list|(
operator|(
operator|(
name|shards
operator|/
name|numNodes
operator|)
operator|*
literal|1.10
operator|)
argument_list|)
decl_stmt|;
specifier|final
name|int
name|lowerBound
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|round
argument_list|(
operator|(
operator|(
name|shards
operator|/
name|numNodes
operator|)
operator|*
literal|0.90
operator|)
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
name|nodeIdCounter
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
literal|"NODE_"
operator|+
name|i
argument_list|)
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|assertThat
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
literal|"NODE_"
operator|+
name|i
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|Matchers
operator|.
name|anyOf
argument_list|(
name|Matchers
operator|.
name|anyOf
argument_list|(
name|equalTo
argument_list|(
operator|(
name|shards
operator|/
name|numNodes
operator|)
operator|+
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|shards
operator|/
name|numNodes
operator|)
operator|-
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|shards
operator|/
name|numNodes
operator|)
argument_list|)
argument_list|)
argument_list|,
name|Matchers
operator|.
name|allOf
argument_list|(
name|Matchers
operator|.
name|greaterThanOrEqualTo
argument_list|(
name|lowerBound
argument_list|)
argument_list|,
name|Matchers
operator|.
name|lessThanOrEqualTo
argument_list|(
name|upperBound
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|RandomAllocationDecider
specifier|private
specifier|static
specifier|final
class|class
name|RandomAllocationDecider
extends|extends
name|AllocationDecider
block|{
DECL|field|random
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
DECL|method|RandomAllocationDecider
specifier|public
name|RandomAllocationDecider
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
name|super
argument_list|(
name|ImmutableSettings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|this
operator|.
name|random
operator|=
name|random
expr_stmt|;
block|}
DECL|field|alwaysSayYes
specifier|public
name|boolean
name|alwaysSayYes
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
DECL|method|canRebalance
specifier|public
name|Decision
name|canRebalance
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
return|return
name|getRandomDecision
argument_list|()
return|;
block|}
DECL|method|getRandomDecision
specifier|private
name|Decision
name|getRandomDecision
parameter_list|()
block|{
if|if
condition|(
name|alwaysSayYes
condition|)
block|{
return|return
name|Decision
operator|.
name|YES
return|;
block|}
switch|switch
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|10
argument_list|)
condition|)
block|{
case|case
literal|9
case|:
case|case
literal|8
case|:
case|case
literal|7
case|:
case|case
literal|6
case|:
case|case
literal|5
case|:
return|return
name|Decision
operator|.
name|NO
return|;
case|case
literal|4
case|:
return|return
name|Decision
operator|.
name|THROTTLE
return|;
case|case
literal|3
case|:
case|case
literal|2
case|:
case|case
literal|1
case|:
return|return
name|Decision
operator|.
name|YES
return|;
default|default:
return|return
name|Decision
operator|.
name|ALWAYS
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|canAllocate
specifier|public
name|Decision
name|canAllocate
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
return|return
name|getRandomDecision
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|canRemain
specifier|public
name|Decision
name|canRemain
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
return|return
name|getRandomDecision
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

