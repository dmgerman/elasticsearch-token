begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|IndexShardRoutingTable
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
name|test
operator|.
name|ElasticsearchTestCase
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
name|cluster
operator|.
name|routing
operator|.
name|ShardRoutingState
operator|.
name|STARTED
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
name|allocation
operator|.
name|RoutingAllocationTests
operator|.
name|newNode
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ElectReplicaAsPrimaryDuringRelocationTests
specifier|public
class|class
name|ElectReplicaAsPrimaryDuringRelocationTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|ElectReplicaAsPrimaryDuringRelocationTests
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
DECL|method|testElectReplicaAsPrimaryDuringRelocation
specifier|public
name|void
name|testElectReplicaAsPrimaryDuringRelocation
parameter_list|()
block|{
name|AllocationService
name|strategy
init|=
operator|new
name|AllocationService
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.concurrent_recoveries"
argument_list|,
literal|10
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Building initial routing table"
argument_list|)
expr_stmt|;
name|MetaData
name|metaData
init|=
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
literal|"test"
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|2
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
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
literal|"test"
argument_list|)
argument_list|)
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
name|build
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Adding two nodes and performing rerouting"
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
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node1"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node2"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|RoutingTable
name|prevRoutingTable
init|=
name|routingTable
decl_stmt|;
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
name|logger
operator|.
name|info
argument_list|(
literal|"Start the primary shards"
argument_list|)
expr_stmt|;
name|RoutingNodes
name|routingNodes
init|=
name|clusterState
operator|.
name|routingNodes
argument_list|()
decl_stmt|;
name|prevRoutingTable
operator|=
name|routingTable
expr_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|routingNodes
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
name|logger
operator|.
name|info
argument_list|(
literal|"Start the replica shards"
argument_list|)
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|routingNodes
argument_list|()
expr_stmt|;
name|prevRoutingTable
operator|=
name|routingTable
expr_stmt|;
name|routingTable
operator|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|routingNodes
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
name|routingNodes
operator|=
name|clusterState
operator|.
name|routingNodes
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|prevRoutingTable
operator|!=
name|routingTable
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodes
operator|.
name|node
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodes
operator|.
name|node
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|numberOfShardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Start another node and perform rerouting"
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
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|newNode
argument_list|(
literal|"node3"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|prevRoutingTable
operator|=
name|routingTable
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
name|logger
operator|.
name|info
argument_list|(
literal|"find the replica shard that gets relocated"
argument_list|)
expr_stmt|;
name|IndexShardRoutingTable
name|indexShardRoutingTable
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|routingTable
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
name|replicaShards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|relocating
argument_list|()
condition|)
block|{
name|indexShardRoutingTable
operator|=
name|routingTable
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
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
literal|1
argument_list|)
operator|.
name|replicaShards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|relocating
argument_list|()
condition|)
block|{
name|indexShardRoutingTable
operator|=
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// we might have primary relocating, and the test is only for replicas, so only test in the case of replica allocation
if|if
condition|(
name|indexShardRoutingTable
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"kill the node [{}] of the primary shard for the relocating replica"
argument_list|,
name|indexShardRoutingTable
operator|.
name|primaryShard
argument_list|()
operator|.
name|currentNodeId
argument_list|()
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
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|)
operator|.
name|remove
argument_list|(
name|indexShardRoutingTable
operator|.
name|primaryShard
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|prevRoutingTable
operator|=
name|routingTable
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
name|logger
operator|.
name|info
argument_list|(
literal|"make sure all the primary shards are active"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
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
name|primaryShard
argument_list|()
operator|.
name|active
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|shard
argument_list|(
literal|1
argument_list|)
operator|.
name|primaryShard
argument_list|()
operator|.
name|active
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

