begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.gce
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|gce
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
name|Lists
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
name|cloud
operator|.
name|gce
operator|.
name|GceComputeService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|gce
operator|.
name|GceComputeService
operator|.
name|Fields
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
name|gce
operator|.
name|mock
operator|.
name|*
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
name|PluginsService
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
name|Ignore
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
name|io
operator|.
name|IOException
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
name|notNullValue
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
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|,
name|numClientNodes
operator|=
literal|0
argument_list|,
name|transportClientRatio
operator|=
literal|0.0
argument_list|)
DECL|class|GceComputeEngineTest
specifier|public
class|class
name|GceComputeEngineTest
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|method|getPort
specifier|public
specifier|static
name|int
name|getPort
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
try|try
block|{
return|return
name|PropertiesHelper
operator|.
name|getAsInt
argument_list|(
literal|"plugin.port"
argument_list|)
operator|+
name|nodeOrdinal
operator|*
literal|10
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{         }
return|return
operator|-
literal|1
return|;
block|}
DECL|method|checkNumberOfNodes
specifier|protected
name|void
name|checkNumberOfNodes
parameter_list|(
name|int
name|expected
parameter_list|)
block|{
name|NodesInfoResponse
name|nodeInfos
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
name|assertNotNull
argument_list|(
name|nodeInfos
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|nodeInfos
operator|.
name|getNodes
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|nodeInfos
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|settingsBuilder
specifier|protected
name|Settings
name|settingsBuilder
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|GceComputeService
argument_list|>
name|mock
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|Settings
operator|.
name|Builder
name|builder
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
literal|"gce"
argument_list|)
operator|.
name|put
argument_list|(
literal|"cloud.gce.api.impl"
argument_list|,
name|mock
argument_list|)
comment|// We need the network to make the mock working
operator|.
name|put
argument_list|(
literal|"node.mode"
argument_list|,
literal|"network"
argument_list|)
comment|// Make the tests run faster
operator|.
name|put
argument_list|(
literal|"discovery.zen.join.timeout"
argument_list|,
literal|"100ms"
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.zen.ping.timeout"
argument_list|,
literal|"10ms"
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.initial_state_timeout"
argument_list|,
literal|"300ms"
argument_list|)
comment|// We use a specific port for each node
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
name|getPort
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
comment|// We disable http
operator|.
name|put
argument_list|(
literal|"http.enabled"
argument_list|,
literal|false
argument_list|)
comment|// We force plugin loading
operator|.
name|put
argument_list|(
literal|"plugins."
operator|+
name|PluginsService
operator|.
name|LOAD_PLUGIN_FROM_CLASSPATH
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|settings
argument_list|)
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
decl_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|startNode
specifier|protected
name|void
name|startNode
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|GceComputeService
argument_list|>
name|mock
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start node #{}, mock [{}], settings [{}]"
argument_list|,
name|nodeOrdinal
argument_list|,
name|mock
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|settings
operator|.
name|getAsMap
argument_list|()
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|(
name|nodeOrdinal
argument_list|,
name|mock
argument_list|,
name|settings
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
name|prepareState
argument_list|()
operator|.
name|setMasterNodeTimeout
argument_list|(
literal|"1s"
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNodeId
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Ignore
DECL|method|nodes_with_different_tags_and_no_tag_set
specifier|public
name|void
name|nodes_with_different_tags_and_no_tag_set
parameter_list|()
block|{
name|startNode
argument_list|(
literal|1
argument_list|,
name|GceComputeServiceTwoNodesDifferentTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|2
argument_list|,
name|GceComputeServiceTwoNodesDifferentTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
comment|// We expect having 2 nodes as part of the cluster, let's test that
name|checkNumberOfNodes
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**      * We need to ignore this test from elasticsearch version 1.2.1 as      * expected nodes running is 2 and this test will create 2 clusters with one node each.      * @see org.elasticsearch.test.ElasticsearchIntegrationTest#ensureClusterSizeConsistency()      * TODO Reactivate when it will be possible to set the number of running nodes      */
annotation|@
name|Test
annotation|@
name|Ignore
DECL|method|nodes_with_different_tags_and_one_tag_set
specifier|public
name|void
name|nodes_with_different_tags_and_one_tag_set
parameter_list|()
block|{
name|startNode
argument_list|(
literal|1
argument_list|,
name|GceComputeServiceTwoNodesDifferentTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TAGS
argument_list|,
literal|"elasticsearch"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|2
argument_list|,
name|GceComputeServiceTwoNodesDifferentTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TAGS
argument_list|,
literal|"elasticsearch"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// We expect having 1 nodes as part of the cluster, let's test that
name|checkNumberOfNodes
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**      * We need to ignore this test from elasticsearch version 1.2.1 as      * expected nodes running is 2 and this test will create 2 clusters with one node each.      * @see org.elasticsearch.test.ElasticsearchIntegrationTest#ensureClusterSizeConsistency()      * TODO Reactivate when it will be possible to set the number of running nodes      */
annotation|@
name|Test
annotation|@
name|Ignore
DECL|method|nodes_with_different_tags_and_two_tag_set
specifier|public
name|void
name|nodes_with_different_tags_and_two_tag_set
parameter_list|()
block|{
name|startNode
argument_list|(
literal|1
argument_list|,
name|GceComputeServiceTwoNodesDifferentTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TAGS
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"elasticsearch"
argument_list|,
literal|"dev"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|2
argument_list|,
name|GceComputeServiceTwoNodesDifferentTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TAGS
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"elasticsearch"
argument_list|,
literal|"dev"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// We expect having 1 nodes as part of the cluster, let's test that
name|checkNumberOfNodes
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Ignore
DECL|method|nodes_with_same_tags_and_no_tag_set
specifier|public
name|void
name|nodes_with_same_tags_and_no_tag_set
parameter_list|()
block|{
name|startNode
argument_list|(
literal|1
argument_list|,
name|GceComputeServiceTwoNodesSameTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|2
argument_list|,
name|GceComputeServiceTwoNodesSameTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
comment|// We expect having 2 nodes as part of the cluster, let's test that
name|checkNumberOfNodes
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Ignore
DECL|method|nodes_with_same_tags_and_one_tag_set
specifier|public
name|void
name|nodes_with_same_tags_and_one_tag_set
parameter_list|()
block|{
name|startNode
argument_list|(
literal|1
argument_list|,
name|GceComputeServiceTwoNodesSameTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TAGS
argument_list|,
literal|"elasticsearch"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|2
argument_list|,
name|GceComputeServiceTwoNodesSameTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TAGS
argument_list|,
literal|"elasticsearch"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// We expect having 2 nodes as part of the cluster, let's test that
name|checkNumberOfNodes
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Ignore
DECL|method|nodes_with_same_tags_and_two_tags_set
specifier|public
name|void
name|nodes_with_same_tags_and_two_tags_set
parameter_list|()
block|{
name|startNode
argument_list|(
literal|1
argument_list|,
name|GceComputeServiceTwoNodesSameTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TAGS
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"elasticsearch"
argument_list|,
literal|"dev"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|2
argument_list|,
name|GceComputeServiceTwoNodesSameTagsMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TAGS
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"elasticsearch"
argument_list|,
literal|"dev"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// We expect having 2 nodes as part of the cluster, let's test that
name|checkNumberOfNodes
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Ignore
DECL|method|multiple_zones_and_two_nodes_in_same_zone
specifier|public
name|void
name|multiple_zones_and_two_nodes_in_same_zone
parameter_list|()
block|{
name|startNode
argument_list|(
literal|1
argument_list|,
name|GceComputeServiceTwoNodesOneZoneMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|ZONE
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"us-central1-a"
argument_list|,
literal|"us-central1-b"
argument_list|,
literal|"us-central1-f"
argument_list|,
literal|"europe-west1-a"
argument_list|,
literal|"europe-west1-b"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|2
argument_list|,
name|GceComputeServiceTwoNodesOneZoneMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|ZONE
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"us-central1-a"
argument_list|,
literal|"us-central1-b"
argument_list|,
literal|"us-central1-f"
argument_list|,
literal|"europe-west1-a"
argument_list|,
literal|"europe-west1-b"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// We expect having 2 nodes as part of the cluster, let's test that
name|checkNumberOfNodes
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Ignore
DECL|method|multiple_zones_and_two_nodes_in_different_zones
specifier|public
name|void
name|multiple_zones_and_two_nodes_in_different_zones
parameter_list|()
block|{
name|startNode
argument_list|(
literal|1
argument_list|,
name|GceComputeServiceTwoNodesTwoZonesMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|ZONE
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"us-central1-a"
argument_list|,
literal|"us-central1-b"
argument_list|,
literal|"us-central1-f"
argument_list|,
literal|"europe-west1-a"
argument_list|,
literal|"europe-west1-b"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|2
argument_list|,
name|GceComputeServiceTwoNodesTwoZonesMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|ZONE
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"us-central1-a"
argument_list|,
literal|"us-central1-b"
argument_list|,
literal|"us-central1-f"
argument_list|,
literal|"europe-west1-a"
argument_list|,
literal|"europe-west1-b"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// We expect having 2 nodes as part of the cluster, let's test that
name|checkNumberOfNodes
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**      * For issue https://github.com/elastic/elasticsearch-cloud-gce/issues/43      */
annotation|@
name|Test
annotation|@
name|Ignore
DECL|method|zero_node_43
specifier|public
name|void
name|zero_node_43
parameter_list|()
block|{
name|startNode
argument_list|(
literal|1
argument_list|,
name|GceComputeServiceZeroNodeMock
operator|.
name|class
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|ZONE
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"us-central1-a"
argument_list|,
literal|"us-central1-b"
argument_list|,
literal|"us-central1-f"
argument_list|,
literal|"europe-west1-a"
argument_list|,
literal|"europe-west1-b"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

