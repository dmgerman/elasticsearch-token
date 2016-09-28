begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.shrink
package|package
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
name|shrink
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
name|IndexWriter
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
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|CreateIndexClusterStateUpdateRequest
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
name|ActiveShardCount
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
name|EmptyClusterInfoService
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
name|block
operator|.
name|ClusterBlocks
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
name|allocator
operator|.
name|BalancedShardsAllocator
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
name|MaxRetryAllocationDecider
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
name|index
operator|.
name|shard
operator|.
name|DocsStats
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
name|gateway
operator|.
name|TestGatewayAllocator
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
name|Collections
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

begin_class
DECL|class|TransportShrinkActionTests
specifier|public
class|class
name|TransportShrinkActionTests
extends|extends
name|ESTestCase
block|{
DECL|method|createClusterState
specifier|private
name|ClusterState
name|createClusterState
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|numShards
parameter_list|,
name|int
name|numReplicas
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|MetaData
operator|.
name|Builder
name|metaBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|name
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
operator|.
name|put
argument_list|(
name|settings
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
name|numReplicas
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|metaBuilder
operator|.
name|put
argument_list|(
name|indexMetaData
argument_list|,
literal|false
argument_list|)
expr_stmt|;
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
name|routingTableBuilder
operator|.
name|addAsNew
argument_list|(
name|metaData
operator|.
name|index
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
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
name|blocks
argument_list|(
name|ClusterBlocks
operator|.
name|builder
argument_list|()
operator|.
name|addBlocks
argument_list|(
name|indexMetaData
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|clusterState
return|;
block|}
DECL|method|testErrorCondition
specifier|public
name|void
name|testErrorCondition
parameter_list|()
block|{
name|ClusterState
name|state
init|=
name|createClusterState
argument_list|(
literal|"source"
argument_list|,
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|42
argument_list|)
argument_list|,
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.blocks.write"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|TransportShrinkAction
operator|.
name|prepareCreateIndexRequest
argument_list|(
operator|new
name|ShrinkRequest
argument_list|(
literal|"target"
argument_list|,
literal|"source"
argument_list|)
argument_list|,
name|state
argument_list|,
parameter_list|(
name|i
parameter_list|)
lambda|->
operator|new
name|DocsStats
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1000
argument_list|)
argument_list|)
argument_list|,
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
operator|.
name|getMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"Can't merge index with more than [2147483519] docs - too many documents in shards "
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|ShrinkRequest
name|req
init|=
operator|new
name|ShrinkRequest
argument_list|(
literal|"target"
argument_list|,
literal|"source"
argument_list|)
decl_stmt|;
name|req
operator|.
name|getShrinkIndexRequest
argument_list|()
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterState
name|clusterState
init|=
name|createClusterState
argument_list|(
literal|"source"
argument_list|,
literal|8
argument_list|,
literal|1
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.blocks.write"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TransportShrinkAction
operator|.
name|prepareCreateIndexRequest
argument_list|(
name|req
argument_list|,
name|clusterState
argument_list|,
operator|(
name|i
operator|)
operator|->
name|i
operator|==
literal|2
operator|||
name|i
operator|==
literal|3
condition|?
operator|new
name|DocsStats
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
operator|/
literal|2
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1000
argument_list|)
argument_list|)
else|:
literal|null
argument_list|,
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
operator|.
name|getMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"Can't merge index with more than [2147483519] docs - too many documents in shards "
argument_list|)
argument_list|)
expr_stmt|;
comment|// create one that won't fail
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|createClusterState
argument_list|(
literal|"source"
argument_list|,
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|0
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.blocks.write"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
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
decl_stmt|;
name|AllocationService
name|service
init|=
operator|new
name|AllocationService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|AllocationDeciders
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
operator|new
name|MaxRetryAllocationDecider
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
argument_list|,
operator|new
name|TestGatewayAllocator
argument_list|()
argument_list|,
operator|new
name|BalancedShardsAllocator
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|EmptyClusterInfoService
operator|.
name|INSTANCE
argument_list|)
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
name|service
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
literal|"reroute"
argument_list|)
operator|.
name|routingTable
argument_list|()
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
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// now we start the shard
name|routingTable
operator|=
name|service
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|routingTable
operator|.
name|index
argument_list|(
literal|"source"
argument_list|)
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
name|TransportShrinkAction
operator|.
name|prepareCreateIndexRequest
argument_list|(
operator|new
name|ShrinkRequest
argument_list|(
literal|"target"
argument_list|,
literal|"source"
argument_list|)
argument_list|,
name|clusterState
argument_list|,
parameter_list|(
name|i
parameter_list|)
lambda|->
operator|new
name|DocsStats
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1000
argument_list|)
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1000
argument_list|)
argument_list|)
argument_list|,
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testShrinkIndexSettings
specifier|public
name|void
name|testShrinkIndexSettings
parameter_list|()
block|{
name|String
name|indexName
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
comment|// create one that won't fail
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|createClusterState
argument_list|(
name|indexName
argument_list|,
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|0
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.blocks.write"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
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
decl_stmt|;
name|AllocationService
name|service
init|=
operator|new
name|AllocationService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|AllocationDeciders
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
operator|new
name|MaxRetryAllocationDecider
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
argument_list|,
operator|new
name|TestGatewayAllocator
argument_list|()
argument_list|,
operator|new
name|BalancedShardsAllocator
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|EmptyClusterInfoService
operator|.
name|INSTANCE
argument_list|)
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
name|service
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
literal|"reroute"
argument_list|)
operator|.
name|routingTable
argument_list|()
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
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// now we start the shard
name|routingTable
operator|=
name|service
operator|.
name|applyStartedShards
argument_list|(
name|clusterState
argument_list|,
name|routingTable
operator|.
name|index
argument_list|(
name|indexName
argument_list|)
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
name|int
name|numSourceShards
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|indexName
argument_list|)
operator|.
name|getNumberOfShards
argument_list|()
decl_stmt|;
name|DocsStats
name|stats
init|=
operator|new
name|DocsStats
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
operator|(
name|IndexWriter
operator|.
name|MAX_DOCS
operator|)
operator|/
name|numSourceShards
argument_list|)
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1000
argument_list|)
argument_list|)
decl_stmt|;
name|ShrinkRequest
name|target
init|=
operator|new
name|ShrinkRequest
argument_list|(
literal|"target"
argument_list|,
name|indexName
argument_list|)
decl_stmt|;
specifier|final
name|ActiveShardCount
name|activeShardCount
init|=
name|randomBoolean
argument_list|()
condition|?
name|ActiveShardCount
operator|.
name|ALL
else|:
name|ActiveShardCount
operator|.
name|ONE
decl_stmt|;
name|target
operator|.
name|setWaitForActiveShards
argument_list|(
name|activeShardCount
argument_list|)
expr_stmt|;
name|CreateIndexClusterStateUpdateRequest
name|request
init|=
name|TransportShrinkAction
operator|.
name|prepareCreateIndexRequest
argument_list|(
name|target
argument_list|,
name|clusterState
argument_list|,
parameter_list|(
name|i
parameter_list|)
lambda|->
name|stats
argument_list|,
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|request
operator|.
name|shrinkFrom
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|indexName
argument_list|,
name|request
operator|.
name|shrinkFrom
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|request
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
literal|"index.number_of_shards"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"shrink_index"
argument_list|,
name|request
operator|.
name|cause
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|waitForActiveShards
argument_list|()
argument_list|,
name|activeShardCount
argument_list|)
expr_stmt|;
block|}
DECL|method|newNode
specifier|private
name|DiscoveryNode
name|newNode
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
return|return
operator|new
name|DiscoveryNode
argument_list|(
name|nodeId
argument_list|,
name|LocalTransportAddress
operator|.
name|buildUnique
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|DiscoveryNode
operator|.
name|Role
operator|.
name|MASTER
argument_list|,
name|DiscoveryNode
operator|.
name|Role
operator|.
name|DATA
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
block|}
end_class

end_unit

