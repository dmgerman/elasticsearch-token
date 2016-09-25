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
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
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
name|ESAllocationTestCase
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
name|Settings
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
name|ShardRoutingState
operator|.
name|UNASSIGNED
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
name|nullValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PrimaryElectionRoutingTests
specifier|public
class|class
name|PrimaryElectionRoutingTests
extends|extends
name|ESAllocationTestCase
block|{
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|PrimaryElectionRoutingTests
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|testBackupElectionToPrimaryWhenPrimaryCanBeAllocatedToAnotherNode
specifier|public
name|void
name|testBackupElectionToPrimaryWhenPrimaryCanBeAllocatedToAnotherNode
parameter_list|()
block|{
name|AllocationService
name|strategy
init|=
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
argument_list|(
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
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
name|add
argument_list|(
name|newNode
argument_list|(
literal|"node1"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|strategy
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
literal|"reroute"
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
name|add
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
name|clusterState
operator|=
name|strategy
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
literal|"reroute"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Start the primary shard (on node1)"
argument_list|)
expr_stmt|;
name|RoutingNodes
name|routingNodes
init|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
decl_stmt|;
name|clusterState
operator|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|routingNodes
operator|.
name|node
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Start the backup shard (on node2)"
argument_list|)
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|strategy
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|routingNodes
operator|.
name|node
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Adding third node and reroute and kill first node"
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
name|add
argument_list|(
name|newNode
argument_list|(
literal|"node3"
argument_list|)
argument_list|)
operator|.
name|remove
argument_list|(
literal|"node1"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|RoutingTable
name|prevRoutingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|clusterState
operator|=
name|strategy
operator|.
name|deassociateDeadNodes
argument_list|(
name|clusterState
argument_list|,
literal|true
argument_list|,
literal|"reroute"
argument_list|)
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
expr_stmt|;
name|routingTable
operator|=
name|clusterState
operator|.
name|routingTable
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
literal|1
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
argument_list|,
name|nullValue
argument_list|()
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
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodes
operator|.
name|node
argument_list|(
literal|"node3"
argument_list|)
operator|.
name|numberOfShardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify where the primary is
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
name|currentNodeId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"node2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|primaryTerm
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2L
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
name|currentNodeId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"node3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemovingInitializingReplicasIfPrimariesFails
specifier|public
name|void
name|testRemovingInitializingReplicasIfPrimariesFails
parameter_list|()
block|{
name|AllocationService
name|allocation
init|=
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
argument_list|(
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
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
name|add
argument_list|(
name|newNode
argument_list|(
literal|"node1"
argument_list|)
argument_list|)
operator|.
name|add
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
name|clusterState
operator|=
name|allocation
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
literal|"reroute"
argument_list|)
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
name|getRoutingNodes
argument_list|()
decl_stmt|;
name|clusterState
operator|=
name|allocation
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
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodes
operator|.
name|shardsWithState
argument_list|(
name|STARTED
argument_list|)
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
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
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
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|primaryTerm
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|primaryTerm
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
comment|// now, fail one node, while the replica is initializing, and it also holds a primary
name|logger
operator|.
name|info
argument_list|(
literal|"--> fail node with primary"
argument_list|)
expr_stmt|;
name|String
name|nodeIdToFail
init|=
name|clusterState
operator|.
name|routingTable
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
name|primaryShard
argument_list|()
operator|.
name|currentNodeId
argument_list|()
decl_stmt|;
name|String
name|nodeIdRemaining
init|=
name|nodeIdToFail
operator|.
name|equals
argument_list|(
literal|"node1"
argument_list|)
condition|?
literal|"node2"
else|:
literal|"node1"
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
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|add
argument_list|(
name|newNode
argument_list|(
name|nodeIdRemaining
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|allocation
operator|.
name|deassociateDeadNodes
argument_list|(
name|clusterState
argument_list|,
literal|true
argument_list|,
literal|"reroute"
argument_list|)
expr_stmt|;
name|routingNodes
operator|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|routingNodes
operator|.
name|shardsWithState
argument_list|(
name|STARTED
argument_list|)
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
name|routingNodes
operator|.
name|shardsWithState
argument_list|(
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
name|routingNodes
operator|.
name|shardsWithState
argument_list|(
name|UNASSIGNED
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
comment|// 2 replicas and one primary
name|assertThat
argument_list|(
name|routingNodes
operator|.
name|node
argument_list|(
name|nodeIdRemaining
argument_list|)
operator|.
name|shardsWithState
argument_list|(
name|STARTED
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|primary
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
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|primaryTerm
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

