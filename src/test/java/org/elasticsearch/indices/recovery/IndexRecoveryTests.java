begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|LifecycleScope
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
name|cluster
operator|.
name|repositories
operator|.
name|put
operator|.
name|PutRepositoryResponse
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
name|cluster
operator|.
name|snapshots
operator|.
name|create
operator|.
name|CreateSnapshotResponse
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
name|cluster
operator|.
name|snapshots
operator|.
name|restore
operator|.
name|RestoreSnapshotResponse
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
name|recovery
operator|.
name|RecoveryResponse
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
name|recovery
operator|.
name|ShardRecoveryResponse
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
name|IndicesStatsResponse
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
name|ImmutableSettings
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
name|snapshots
operator|.
name|SnapshotState
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
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
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
name|ArrayList
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
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
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
name|greaterThan
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ElasticsearchIntegrationTest
operator|.
name|Scope
operator|.
name|TEST
argument_list|,
name|numNodes
operator|=
literal|0
argument_list|)
DECL|class|IndexRecoveryTests
specifier|public
class|class
name|IndexRecoveryTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|INDEX_NAME
specifier|private
specifier|static
specifier|final
name|String
name|INDEX_NAME
init|=
literal|"test-idx-1"
decl_stmt|;
DECL|field|REPO_NAME
specifier|private
specifier|static
specifier|final
name|String
name|REPO_NAME
init|=
literal|"test-repo-1"
decl_stmt|;
DECL|field|SNAP_NAME
specifier|private
specifier|static
specifier|final
name|String
name|SNAP_NAME
init|=
literal|"test-snap-1"
decl_stmt|;
DECL|field|DOC_COUNT
specifier|private
specifier|static
specifier|final
name|int
name|DOC_COUNT
init|=
literal|100
decl_stmt|;
DECL|field|SHARD_COUNT
specifier|private
specifier|static
specifier|final
name|int
name|SHARD_COUNT
init|=
literal|1
decl_stmt|;
DECL|field|REPLICA_COUNT
specifier|private
specifier|static
specifier|final
name|int
name|REPLICA_COUNT
init|=
literal|0
decl_stmt|;
annotation|@
name|Test
DECL|method|gatewayRecoveryTest
specifier|public
name|void
name|gatewayRecoveryTest
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start nodes"
argument_list|)
expr_stmt|;
name|String
name|node
init|=
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
argument_list|)
decl_stmt|;
name|createAndPopulateIndex
argument_list|(
name|INDEX_NAME
argument_list|,
literal|1
argument_list|,
name|SHARD_COUNT
argument_list|,
name|REPLICA_COUNT
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> restarting cluster"
argument_list|)
expr_stmt|;
name|cluster
argument_list|()
operator|.
name|fullRestart
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> request recoveries"
argument_list|)
expr_stmt|;
name|RecoveryResponse
name|response
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
name|prepareRecoveries
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|shardResponses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|SHARD_COUNT
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|shardResponses
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
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
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|shardResponses
init|=
name|response
operator|.
name|shardResponses
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|shardResponses
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
name|ShardRecoveryResponse
name|shardResponse
init|=
name|shardResponses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RecoveryState
name|state
init|=
name|shardResponse
operator|.
name|recoveryState
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RecoveryState
operator|.
name|Type
operator|.
name|GATEWAY
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|getStage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RecoveryState
operator|.
name|Stage
operator|.
name|DONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|node
argument_list|,
name|equalTo
argument_list|(
name|state
operator|.
name|getSourceNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|node
argument_list|,
name|equalTo
argument_list|(
name|state
operator|.
name|getTargetNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|state
operator|.
name|getRestoreSource
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|gatewayRecoveryTestActiveOnly
specifier|public
name|void
name|gatewayRecoveryTestActiveOnly
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start nodes"
argument_list|)
expr_stmt|;
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
argument_list|)
expr_stmt|;
name|createAndPopulateIndex
argument_list|(
name|INDEX_NAME
argument_list|,
literal|1
argument_list|,
name|SHARD_COUNT
argument_list|,
name|REPLICA_COUNT
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> restarting cluster"
argument_list|)
expr_stmt|;
name|cluster
argument_list|()
operator|.
name|fullRestart
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> request recoveries"
argument_list|)
expr_stmt|;
name|RecoveryResponse
name|response
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
name|prepareRecoveries
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setActiveOnly
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|shardResponses
init|=
name|response
operator|.
name|shardResponses
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|shardResponses
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
comment|// Should not expect any responses back
block|}
annotation|@
name|Test
DECL|method|replicaRecoveryTest
specifier|public
name|void
name|replicaRecoveryTest
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start node A"
argument_list|)
expr_stmt|;
name|String
name|nodeA
init|=
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> create index on node: {}"
argument_list|,
name|nodeA
argument_list|)
expr_stmt|;
name|createAndPopulateIndex
argument_list|(
name|INDEX_NAME
argument_list|,
literal|1
argument_list|,
name|SHARD_COUNT
argument_list|,
name|REPLICA_COUNT
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start node B"
argument_list|)
expr_stmt|;
name|String
name|nodeB
init|=
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
argument_list|)
decl_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// force a shard recovery from nodeA to nodeB
name|logger
operator|.
name|info
argument_list|(
literal|"--> bump replica count"
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
name|INDEX_NAME
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"number_of_replicas"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> request recoveries"
argument_list|)
expr_stmt|;
name|RecoveryResponse
name|response
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
name|prepareRecoveries
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
comment|// we should now have two total shards, one primary and one replica
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|shardResponses
init|=
name|response
operator|.
name|shardResponses
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|shardResponses
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
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|nodeAResponses
init|=
name|findRecoveriesForTargetNode
argument_list|(
name|nodeA
argument_list|,
name|shardResponses
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nodeAResponses
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
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|nodeBResponses
init|=
name|findRecoveriesForTargetNode
argument_list|(
name|nodeB
argument_list|,
name|shardResponses
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nodeBResponses
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
comment|// validate node A recovery
name|ShardRecoveryResponse
name|nodeAShardResponse
init|=
name|nodeAResponses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nodeAShardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getShardId
argument_list|()
operator|.
name|id
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
name|nodeAShardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getSourceNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|nodeA
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeAShardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getTargetNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|nodeA
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeAShardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RecoveryState
operator|.
name|Type
operator|.
name|GATEWAY
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeAShardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getStage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RecoveryState
operator|.
name|Stage
operator|.
name|DONE
argument_list|)
argument_list|)
expr_stmt|;
comment|// validate node B recovery
name|ShardRecoveryResponse
name|nodeBShardResponse
init|=
name|nodeBResponses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nodeBShardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getShardId
argument_list|()
operator|.
name|id
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
name|nodeBShardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getSourceNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|nodeA
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeBShardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getTargetNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|nodeB
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeBShardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RecoveryState
operator|.
name|Type
operator|.
name|REPLICA
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeBShardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getStage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RecoveryState
operator|.
name|Stage
operator|.
name|DONE
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|rerouteRecoveryTest
specifier|public
name|void
name|rerouteRecoveryTest
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start node A"
argument_list|)
expr_stmt|;
name|String
name|nodeA
init|=
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> create index on node: {}"
argument_list|,
name|nodeA
argument_list|)
expr_stmt|;
name|createAndPopulateIndex
argument_list|(
name|INDEX_NAME
argument_list|,
literal|1
argument_list|,
name|SHARD_COUNT
argument_list|,
name|REPLICA_COUNT
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start node B"
argument_list|)
expr_stmt|;
name|String
name|nodeB
init|=
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
argument_list|)
decl_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> move shard from: {} to: {}"
argument_list|,
name|nodeA
argument_list|,
name|nodeB
argument_list|)
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
name|INDEX_NAME
argument_list|,
literal|0
argument_list|)
argument_list|,
name|nodeA
argument_list|,
name|nodeB
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> request recoveries"
argument_list|)
expr_stmt|;
name|RecoveryResponse
name|response
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
name|prepareRecoveries
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|shardResponses
init|=
name|response
operator|.
name|shardResponses
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|shardResponses
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
name|ShardRecoveryResponse
name|shardResponse
init|=
name|shardResponses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RecoveryState
name|state
init|=
name|shardResponse
operator|.
name|recoveryState
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RecoveryState
operator|.
name|Type
operator|.
name|RELOCATION
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|getStage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RecoveryState
operator|.
name|Stage
operator|.
name|DONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeA
argument_list|,
name|equalTo
argument_list|(
name|state
operator|.
name|getSourceNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeB
argument_list|,
name|equalTo
argument_list|(
name|state
operator|.
name|getTargetNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|state
operator|.
name|getRestoreSource
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|snapshotRecoveryTest
specifier|public
name|void
name|snapshotRecoveryTest
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start node A"
argument_list|)
expr_stmt|;
name|String
name|nodeA
init|=
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> create repository"
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
name|REPO_NAME
argument_list|)
operator|.
name|setType
argument_list|(
literal|"fs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|newTempDir
argument_list|(
name|LifecycleScope
operator|.
name|SUITE
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"compress"
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> create index on node: {}"
argument_list|,
name|nodeA
argument_list|)
expr_stmt|;
name|createAndPopulateIndex
argument_list|(
name|INDEX_NAME
argument_list|,
literal|1
argument_list|,
name|SHARD_COUNT
argument_list|,
name|REPLICA_COUNT
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> snapshot"
argument_list|)
expr_stmt|;
name|CreateSnapshotResponse
name|createSnapshotResponse
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
name|prepareCreateSnapshot
argument_list|(
name|REPO_NAME
argument_list|,
name|SNAP_NAME
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|createSnapshotResponse
operator|.
name|getSnapshotInfo
argument_list|()
operator|.
name|successfulShards
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|createSnapshotResponse
operator|.
name|getSnapshotInfo
argument_list|()
operator|.
name|successfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|createSnapshotResponse
operator|.
name|getSnapshotInfo
argument_list|()
operator|.
name|totalShards
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareGetSnapshots
argument_list|(
name|REPO_NAME
argument_list|)
operator|.
name|setSnapshots
argument_list|(
name|SNAP_NAME
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getSnapshots
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|SnapshotState
operator|.
name|SUCCESS
argument_list|)
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
name|prepareClose
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> restore"
argument_list|)
expr_stmt|;
name|RestoreSnapshotResponse
name|restoreSnapshotResponse
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
name|prepareRestoreSnapshot
argument_list|(
name|REPO_NAME
argument_list|,
name|SNAP_NAME
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|int
name|totalShards
init|=
name|restoreSnapshotResponse
operator|.
name|getRestoreInfo
argument_list|()
operator|.
name|totalShards
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|totalShards
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> request recoveries"
argument_list|)
expr_stmt|;
name|RecoveryResponse
name|response
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
name|prepareRecoveries
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
argument_list|>
name|shardRecoveryResponse
range|:
name|response
operator|.
name|shardResponses
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|shardRecoveryResponse
operator|.
name|getKey
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|INDEX_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|shardRecoveryResponses
init|=
name|shardRecoveryResponse
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|shardRecoveryResponses
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|totalShards
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardRecoveryResponse
name|shardResponse
range|:
name|shardRecoveryResponses
control|)
block|{
name|assertThat
argument_list|(
name|shardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RecoveryState
operator|.
name|Type
operator|.
name|SNAPSHOT
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getStage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RecoveryState
operator|.
name|Stage
operator|.
name|DONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|shardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getRestoreSource
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getTargetNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|nodeA
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|findRecoveriesForTargetNode
specifier|private
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|findRecoveriesForTargetNode
parameter_list|(
name|String
name|nodeName
parameter_list|,
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|responses
parameter_list|)
block|{
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|nodeResponses
init|=
operator|new
name|ArrayList
argument_list|<
name|ShardRecoveryResponse
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardRecoveryResponse
name|response
range|:
name|responses
control|)
block|{
if|if
condition|(
name|response
operator|.
name|recoveryState
argument_list|()
operator|.
name|getTargetNode
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|nodeName
argument_list|)
condition|)
block|{
name|nodeResponses
operator|.
name|add
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|nodeResponses
return|;
block|}
DECL|method|createAndPopulateIndex
specifier|private
name|IndicesStatsResponse
name|createAndPopulateIndex
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|nodeCount
parameter_list|,
name|int
name|shardCount
parameter_list|,
name|int
name|replicaCount
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> creating test index: {}"
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|name
argument_list|,
name|nodeCount
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"number_of_shards"
argument_list|,
name|shardCount
argument_list|)
operator|.
name|put
argument_list|(
literal|"number_of_replicas"
argument_list|,
name|replicaCount
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexing sample data"
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
name|DOC_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|index
argument_list|(
name|INDEX_NAME
argument_list|,
literal|"x"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
literal|"foo-"
operator|+
name|i
argument_list|,
literal|"bar-"
operator|+
name|i
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
argument_list|(
name|INDEX_NAME
argument_list|)
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
name|DOC_COUNT
argument_list|)
argument_list|)
expr_stmt|;
return|return
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
name|INDEX_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
return|;
block|}
block|}
end_class

end_unit

