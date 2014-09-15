begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bwcompat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bwcompat
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
name|node
operator|.
name|stats
operator|.
name|NodesStatsRequestBuilder
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
name|stats
operator|.
name|NodesStatsResponse
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
name|transport
operator|.
name|TransportClient
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
name|test
operator|.
name|ElasticsearchBackwardsCompatIntegrationTest
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_class
annotation|@
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ElasticsearchIntegrationTest
operator|.
name|Scope
operator|.
name|SUITE
argument_list|,
name|numClientNodes
operator|=
literal|0
argument_list|)
DECL|class|NodesStatsBasicBackwardsCompat
specifier|public
class|class
name|NodesStatsBasicBackwardsCompat
extends|extends
name|ElasticsearchBackwardsCompatIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testNodeStatsSetIndices
specifier|public
name|void
name|testNodeStatsSetIndices
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"client.transport.ignore_cluster_name"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// We explicitly connect to each node with a custom TransportClient
for|for
control|(
name|NodeInfo
name|n
range|:
name|nodesInfo
operator|.
name|getNodes
argument_list|()
control|)
block|{
name|TransportClient
name|tc
init|=
operator|new
name|TransportClient
argument_list|(
name|settings
argument_list|)
operator|.
name|addTransportAddress
argument_list|(
name|n
operator|.
name|getNode
argument_list|()
operator|.
name|address
argument_list|()
argument_list|)
decl_stmt|;
comment|// Just verify that the NS can be sent and serialized/deserialized between nodes with basic indices
name|NodesStatsResponse
name|ns
init|=
name|tc
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesStats
argument_list|()
operator|.
name|setIndices
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
block|}
block|}
annotation|@
name|Test
DECL|method|testNodeStatsSetRandom
specifier|public
name|void
name|testNodeStatsSetRandom
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"client.transport.ignore_cluster_name"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// We explicitly connect to each node with a custom TransportClient
for|for
control|(
name|NodeInfo
name|n
range|:
name|nodesInfo
operator|.
name|getNodes
argument_list|()
control|)
block|{
name|TransportClient
name|tc
init|=
operator|new
name|TransportClient
argument_list|(
name|settings
argument_list|)
operator|.
name|addTransportAddress
argument_list|(
name|n
operator|.
name|getNode
argument_list|()
operator|.
name|address
argument_list|()
argument_list|)
decl_stmt|;
comment|// randomize the combination of flags set
comment|// Uses reflection to find methods in an attempt to future-proof this test against newly added flags
name|NodesStatsRequestBuilder
name|nsBuilder
init|=
name|tc
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesStats
argument_list|()
decl_stmt|;
name|Class
name|c
init|=
name|nsBuilder
operator|.
name|getClass
argument_list|()
decl_stmt|;
for|for
control|(
name|Method
name|method
range|:
name|c
operator|.
name|getDeclaredMethods
argument_list|()
control|)
block|{
if|if
condition|(
name|method
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"set"
argument_list|)
condition|)
block|{
if|if
condition|(
name|method
operator|.
name|getParameterTypes
argument_list|()
operator|.
name|length
operator|==
literal|1
operator|&&
name|method
operator|.
name|getParameterTypes
argument_list|()
index|[
literal|0
index|]
operator|==
name|boolean
operator|.
name|class
condition|)
block|{
name|method
operator|.
name|invoke
argument_list|(
name|nsBuilder
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
operator|(
name|method
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"all"
argument_list|)
operator|||
name|method
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"clear"
argument_list|)
operator|)
operator|&&
name|randomBoolean
argument_list|()
condition|)
block|{
name|method
operator|.
name|invoke
argument_list|(
name|nsBuilder
argument_list|)
expr_stmt|;
block|}
block|}
name|NodesStatsResponse
name|ns
init|=
name|nsBuilder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
block|}
block|}
block|}
end_class

end_unit

