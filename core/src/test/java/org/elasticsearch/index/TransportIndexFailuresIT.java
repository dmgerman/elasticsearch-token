begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
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
name|base
operator|.
name|Predicate
import|;
end_import

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
name|ImmutableSet
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
name|health
operator|.
name|ClusterHealthStatus
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
name|index
operator|.
name|IndexAction
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
name|index
operator|.
name|IndexResponse
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
name|RoutingNodes
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
name|Strings
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
name|discovery
operator|.
name|DiscoverySettings
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
name|zen
operator|.
name|fd
operator|.
name|FaultDetection
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|ESIntegTestCase
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
name|MockTransportService
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
name|TransportModule
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
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|*
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
comment|/**  * Test failure when index replication actions fail mid-flight  */
end_comment

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|,
name|transportClientRatio
operator|=
literal|0
argument_list|)
annotation|@
name|ESIntegTestCase
operator|.
name|SuppressLocalMode
DECL|class|TransportIndexFailuresIT
specifier|public
class|class
name|TransportIndexFailuresIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|nodeSettings
specifier|private
specifier|static
specifier|final
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|)
comment|//<-- To override the local setting if set externally
operator|.
name|put
argument_list|(
name|FaultDetection
operator|.
name|SETTING_PING_TIMEOUT
argument_list|,
literal|"1s"
argument_list|)
comment|//<-- for hitting simulated network failures quickly
operator|.
name|put
argument_list|(
name|FaultDetection
operator|.
name|SETTING_PING_RETRIES
argument_list|,
literal|"1"
argument_list|)
comment|//<-- for hitting simulated network failures quickly
operator|.
name|put
argument_list|(
name|DiscoverySettings
operator|.
name|PUBLISH_TIMEOUT
argument_list|,
literal|"1s"
argument_list|)
comment|//<-- for hitting simulated network failures quickly
operator|.
name|put
argument_list|(
literal|"discovery.zen.minimum_master_nodes"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|MockTransportService
operator|.
name|TestPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|numberOfShards
specifier|protected
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
annotation|@
name|Override
DECL|method|numberOfReplicas
specifier|protected
name|int
name|numberOfReplicas
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
annotation|@
name|Test
DECL|method|testNetworkPartitionDuringReplicaIndexOp
specifier|public
name|void
name|testNetworkPartitionDuringReplicaIndexOp
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|INDEX
init|=
literal|"testidx"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
literal|2
argument_list|,
name|nodeSettings
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// Create index test with 1 shard, 1 replica and ensure it is green
name|createIndex
argument_list|(
name|INDEX
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
name|INDEX
argument_list|)
expr_stmt|;
comment|// Disable allocation so the replica cannot be reallocated when it fails
name|Settings
name|s
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.enable"
argument_list|,
literal|"none"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|s
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// Determine which node holds the primary shard
name|ClusterState
name|state
init|=
name|getNodeClusterState
argument_list|(
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|IndexShardRoutingTable
name|shard
init|=
name|state
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|index
argument_list|(
name|INDEX
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|primaryNode
decl_stmt|;
name|String
name|replicaNode
decl_stmt|;
if|if
condition|(
name|shard
operator|.
name|getShards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|primary
argument_list|()
condition|)
block|{
name|primaryNode
operator|=
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|replicaNode
operator|=
name|nodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|primaryNode
operator|=
name|nodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|replicaNode
operator|=
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> primary shard is on {}"
argument_list|,
name|primaryNode
argument_list|)
expr_stmt|;
comment|// Index a document to make sure everything works well
name|IndexResponse
name|resp
init|=
name|internalCluster
argument_list|()
operator|.
name|client
argument_list|(
name|primaryNode
argument_list|)
operator|.
name|prepareIndex
argument_list|(
name|INDEX
argument_list|,
literal|"doc"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"document exists on primary node"
argument_list|,
name|internalCluster
argument_list|()
operator|.
name|client
argument_list|(
name|primaryNode
argument_list|)
operator|.
name|prepareGet
argument_list|(
name|INDEX
argument_list|,
literal|"doc"
argument_list|,
name|resp
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|setPreference
argument_list|(
literal|"_only_local"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isExists
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
literal|"document exists on replica node"
argument_list|,
name|internalCluster
argument_list|()
operator|.
name|client
argument_list|(
name|replicaNode
argument_list|)
operator|.
name|prepareGet
argument_list|(
name|INDEX
argument_list|,
literal|"doc"
argument_list|,
name|resp
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|setPreference
argument_list|(
literal|"_only_local"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// Disrupt the network so indexing requests fail to replicate
name|logger
operator|.
name|info
argument_list|(
literal|"--> preventing index/replica operations"
argument_list|)
expr_stmt|;
name|TransportService
name|mockTransportService
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|primaryNode
argument_list|)
decl_stmt|;
operator|(
operator|(
name|MockTransportService
operator|)
name|mockTransportService
operator|)
operator|.
name|addFailToSendNoConnectRule
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Discovery
operator|.
name|class
argument_list|,
name|replicaNode
argument_list|)
operator|.
name|localNode
argument_list|()
argument_list|,
name|ImmutableSet
operator|.
name|of
argument_list|(
name|IndexAction
operator|.
name|NAME
operator|+
literal|"[r]"
argument_list|)
argument_list|)
expr_stmt|;
name|mockTransportService
operator|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|replicaNode
argument_list|)
expr_stmt|;
operator|(
operator|(
name|MockTransportService
operator|)
name|mockTransportService
operator|)
operator|.
name|addFailToSendNoConnectRule
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Discovery
operator|.
name|class
argument_list|,
name|primaryNode
argument_list|)
operator|.
name|localNode
argument_list|()
argument_list|,
name|ImmutableSet
operator|.
name|of
argument_list|(
name|IndexAction
operator|.
name|NAME
operator|+
literal|"[r]"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexing into primary"
argument_list|)
expr_stmt|;
comment|// the replica shard should now be marked as failed because the replication operation will fail
name|resp
operator|=
name|internalCluster
argument_list|()
operator|.
name|client
argument_list|(
name|primaryNode
argument_list|)
operator|.
name|prepareIndex
argument_list|(
name|INDEX
argument_list|,
literal|"doc"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// wait until the cluster reaches an exact yellow state, meaning replica has failed
name|assertBusy
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
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
name|prepareHealth
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getStatus
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|YELLOW
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"document should still be indexed and available"
argument_list|,
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
name|INDEX
argument_list|,
literal|"doc"
argument_list|,
name|resp
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|state
operator|=
name|getNodeClusterState
argument_list|(
name|randomFrom
argument_list|(
name|nodes
operator|.
name|toArray
argument_list|(
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|RoutingNodes
name|rn
init|=
name|state
operator|.
name|getRoutingNodes
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> counts: total: {}, unassigned: {}, initializing: {}, relocating: {}, started: {}"
argument_list|,
name|rn
operator|.
name|shards
argument_list|(
operator|new
name|Predicate
argument_list|<
name|ShardRouting
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|ShardRouting
name|input
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|rn
operator|.
name|shardsWithState
argument_list|(
name|UNASSIGNED
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|rn
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|rn
operator|.
name|shardsWithState
argument_list|(
name|RELOCATING
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|rn
operator|.
name|shardsWithState
argument_list|(
name|STARTED
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> unassigned: {}, initializing: {}, relocating: {}, started: {}"
argument_list|,
name|rn
operator|.
name|shardsWithState
argument_list|(
name|UNASSIGNED
argument_list|)
argument_list|,
name|rn
operator|.
name|shardsWithState
argument_list|(
name|INITIALIZING
argument_list|)
argument_list|,
name|rn
operator|.
name|shardsWithState
argument_list|(
name|RELOCATING
argument_list|)
argument_list|,
name|rn
operator|.
name|shardsWithState
argument_list|(
name|STARTED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"only a single shard is now active (replica should be failed and not reallocated)"
argument_list|,
name|rn
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
block|}
DECL|method|getNodeClusterState
specifier|private
name|ClusterState
name|getNodeClusterState
parameter_list|(
name|String
name|node
parameter_list|)
block|{
return|return
name|internalCluster
argument_list|()
operator|.
name|client
argument_list|(
name|node
argument_list|)
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
name|setLocal
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
return|;
block|}
block|}
end_class

end_unit

