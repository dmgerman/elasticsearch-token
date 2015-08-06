begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.ack
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ack
package|;
end_package

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
name|node
operator|.
name|info
operator|.
name|NodeInfo
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
name|node
operator|.
name|info
operator|.
name|NodesInfoResponse
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
name|settings
operator|.
name|ClusterUpdateSettingsResponse
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
name|close
operator|.
name|CloseIndexResponse
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
name|open
operator|.
name|OpenIndexResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|allocation
operator|.
name|decider
operator|.
name|ThrottlingAllocationDecider
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
name|DiscoverySettings
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
name|common
operator|.
name|settings
operator|.
name|Settings
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
name|ESIntegTestCase
operator|.
name|ClusterScope
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
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
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

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|TEST
argument_list|,
name|minNumDataNodes
operator|=
literal|2
argument_list|)
DECL|class|AckClusterUpdateSettingsIT
specifier|public
class|class
name|AckClusterUpdateSettingsIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
comment|//make sure that enough concurrent reroutes can happen at the same time
comment|//we have a minimum of 2 nodes, and a maximum of 10 shards, thus 5 should be enough
operator|.
name|put
argument_list|(
name|ThrottlingAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_RECOVERIES
argument_list|,
literal|5
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|minimumNumberOfShards
specifier|protected
name|int
name|minimumNumberOfShards
parameter_list|()
block|{
return|return
name|cluster
argument_list|()
operator|.
name|numDataNodes
argument_list|()
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
literal|0
return|;
block|}
DECL|method|removePublishTimeout
specifier|private
name|void
name|removePublishTimeout
parameter_list|()
block|{
comment|//to test that the acknowledgement mechanism is working we better disable the wait for publish
comment|//otherwise the operation is most likely acknowledged even if it doesn't support ack
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DiscoverySettings
operator|.
name|PUBLISH_TIMEOUT
argument_list|,
literal|"0"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testClusterUpdateSettingsAcknowledgement
specifier|public
name|void
name|testClusterUpdateSettingsAcknowledgement
parameter_list|()
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// now that the cluster is stable, remove timeout
name|removePublishTimeout
argument_list|()
expr_stmt|;
name|NodesInfoResponse
name|nodesInfo
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
name|prepareNodesInfo
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|String
name|excludedNodeId
init|=
literal|null
decl_stmt|;
for|for
control|(
name|NodeInfo
name|nodeInfo
range|:
name|nodesInfo
control|)
block|{
if|if
condition|(
name|nodeInfo
operator|.
name|getNode
argument_list|()
operator|.
name|isDataNode
argument_list|()
condition|)
block|{
name|excludedNodeId
operator|=
name|nodeInfo
operator|.
name|getNode
argument_list|()
operator|.
name|id
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
name|assertNotNull
argument_list|(
name|excludedNodeId
argument_list|)
expr_stmt|;
name|ClusterUpdateSettingsResponse
name|clusterUpdateSettingsResponse
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.exclude._id"
argument_list|,
name|excludedNodeId
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|clusterUpdateSettingsResponse
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterUpdateSettingsResponse
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|get
argument_list|(
literal|"cluster.routing.allocation.exclude._id"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|excludedNodeId
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Client
name|client
range|:
name|clients
argument_list|()
control|)
block|{
name|ClusterState
name|clusterState
init|=
name|getLocalClusterState
argument_list|(
name|client
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|transientSettings
argument_list|()
operator|.
name|get
argument_list|(
literal|"cluster.routing.allocation.exclude._id"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|excludedNodeId
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|clusterState
operator|.
name|routingTable
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
assert|assert
name|clusterState
operator|.
name|nodes
argument_list|()
operator|!=
literal|null
assert|;
if|if
condition|(
name|shardRouting
operator|.
name|unassigned
argument_list|()
operator|==
literal|false
operator|&&
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|excludedNodeId
argument_list|)
condition|)
block|{
comment|//if the shard is still there it must be relocating and all nodes need to know, since the request was acknowledged
comment|//reroute happens as part of the update settings and we made sure no throttling comes into the picture via settings
name|assertThat
argument_list|(
name|shardRouting
operator|.
name|relocating
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
block|}
block|}
block|}
annotation|@
name|Test
DECL|method|testClusterUpdateSettingsNoAcknowledgement
specifier|public
name|void
name|testClusterUpdateSettingsNoAcknowledgement
parameter_list|()
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"number_of_shards"
argument_list|,
name|between
argument_list|(
name|cluster
argument_list|()
operator|.
name|numDataNodes
argument_list|()
argument_list|,
name|DEFAULT_MAX_NUM_SHARDS
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"number_of_replicas"
argument_list|,
literal|0
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// now that the cluster is stable, remove timeout
name|removePublishTimeout
argument_list|()
expr_stmt|;
name|NodesInfoResponse
name|nodesInfo
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
name|prepareNodesInfo
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|String
name|excludedNodeId
init|=
literal|null
decl_stmt|;
for|for
control|(
name|NodeInfo
name|nodeInfo
range|:
name|nodesInfo
control|)
block|{
if|if
condition|(
name|nodeInfo
operator|.
name|getNode
argument_list|()
operator|.
name|isDataNode
argument_list|()
condition|)
block|{
name|excludedNodeId
operator|=
name|nodeInfo
operator|.
name|getNode
argument_list|()
operator|.
name|id
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
name|assertNotNull
argument_list|(
name|excludedNodeId
argument_list|)
expr_stmt|;
name|ClusterUpdateSettingsResponse
name|clusterUpdateSettingsResponse
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"0s"
argument_list|)
operator|.
name|setTransientSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.allocation.exclude._id"
argument_list|,
name|excludedNodeId
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clusterUpdateSettingsResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterUpdateSettingsResponse
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|get
argument_list|(
literal|"cluster.routing.allocation.exclude._id"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|excludedNodeId
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getLocalClusterState
specifier|private
specifier|static
name|ClusterState
name|getLocalClusterState
parameter_list|(
name|Client
name|client
parameter_list|)
block|{
return|return
name|client
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
annotation|@
name|Test
DECL|method|testOpenIndexNoAcknowledgement
specifier|public
name|void
name|testOpenIndexNoAcknowledgement
parameter_list|()
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|removePublishTimeout
argument_list|()
expr_stmt|;
name|CloseIndexResponse
name|closeIndexResponse
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
name|prepareClose
argument_list|(
literal|"test"
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
name|closeIndexResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|OpenIndexResponse
name|openIndexResponse
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
name|prepareOpen
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTimeout
argument_list|(
literal|"0s"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|openIndexResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
comment|// make sure that recovery from disk has completed, so that check index doesn't fail.
block|}
block|}
end_class

end_unit

