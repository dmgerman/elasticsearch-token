begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|cluster
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
name|ImmutableMap
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
name|RoutingAllocation
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|test
operator|.
name|ElasticsearchAllocationTestCase
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

begin_class
DECL|class|ClusterAllocationRerouteBenchmark
specifier|public
class|class
name|ClusterAllocationRerouteBenchmark
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|ClusterAllocationRerouteBenchmark
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
specifier|final
name|int
name|numberOfRuns
init|=
literal|1
decl_stmt|;
specifier|final
name|int
name|numIndices
init|=
literal|5
operator|*
literal|365
decl_stmt|;
comment|// five years
specifier|final
name|int
name|numShards
init|=
literal|6
decl_stmt|;
specifier|final
name|int
name|numReplicas
init|=
literal|2
decl_stmt|;
specifier|final
name|int
name|numberOfNodes
init|=
literal|30
decl_stmt|;
specifier|final
name|int
name|numberOfTags
init|=
literal|2
decl_stmt|;
name|AllocationService
name|strategy
init|=
name|ElasticsearchAllocationTestCase
operator|.
name|createAllocationService
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.awareness.attributes"
argument_list|,
literal|"tag"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|Random
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|MetaData
operator|.
name|Builder
name|mb
init|=
name|MetaData
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|numIndices
condition|;
name|i
operator|++
control|)
block|{
name|mb
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test_"
operator|+
name|i
argument_list|)
operator|.
name|numberOfShards
argument_list|(
name|numShards
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
name|numReplicas
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|MetaData
name|metaData
init|=
name|mb
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
operator|.
name|Builder
name|rb
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
literal|1
init|;
name|i
operator|<=
name|numIndices
condition|;
name|i
operator|++
control|)
block|{
name|rb
operator|.
name|addAsNew
argument_list|(
name|metaData
operator|.
name|index
argument_list|(
literal|"test_"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|RoutingTable
name|routingTable
init|=
name|rb
operator|.
name|build
argument_list|()
decl_stmt|;
name|DiscoveryNodes
operator|.
name|Builder
name|nb
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|numberOfNodes
condition|;
name|i
operator|++
control|)
block|{
name|nb
operator|.
name|put
argument_list|(
name|ElasticsearchAllocationTestCase
operator|.
name|newNode
argument_list|(
literal|"node"
operator|+
name|i
argument_list|,
name|numberOfTags
operator|==
literal|0
condition|?
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|of
argument_list|()
operator|:
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"tag"
argument_list|,
literal|"tag_"
operator|+
operator|(
name|i
operator|%
name|numberOfTags
operator|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ClusterState
name|initialClusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|()
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
name|nodes
argument_list|(
name|nb
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
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
name|numberOfRuns
condition|;
name|i
operator|++
control|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] starting... "
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|long
name|runStart
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|initialClusterState
decl_stmt|;
while|while
condition|(
name|clusterState
operator|.
name|readOnlyRoutingNodes
argument_list|()
operator|.
name|hasUnassignedShards
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] remaining unassigned {}"
argument_list|,
name|i
argument_list|,
name|clusterState
operator|.
name|readOnlyRoutingNodes
argument_list|()
operator|.
name|unassigned
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|RoutingAllocation
operator|.
name|Result
name|result
init|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|clusterState
operator|.
name|readOnlyRoutingNodes
argument_list|()
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
decl_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingResult
argument_list|(
name|result
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|result
operator|=
name|strategy
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|)
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
name|routingResult
argument_list|(
name|result
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] took {}"
argument_list|,
name|i
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|runStart
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|took
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"total took {}, AVG {}"
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|took
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|took
operator|/
name|numberOfRuns
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

