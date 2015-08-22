begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.nodesinfo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|nodesinfo
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
name|health
operator|.
name|ClusterHealthResponse
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
name|cluster
operator|.
name|ClusterService
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
name|ESIntegTestCase
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
name|List
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|nodesInfoRequest
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
name|*
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
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|SimpleNodesInfoIT
specifier|public
class|class
name|SimpleNodesInfoIT
extends|extends
name|ESIntegTestCase
block|{
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|SITE_PLUGIN
specifier|static
specifier|final
name|String
name|SITE_PLUGIN
init|=
literal|"dummy"
decl_stmt|;
DECL|field|SITE_PLUGIN_DESCRIPTION
specifier|static
specifier|final
name|String
name|SITE_PLUGIN_DESCRIPTION
init|=
literal|"This is a description for a dummy test site plugin."
decl_stmt|;
DECL|field|SITE_PLUGIN_VERSION
specifier|static
specifier|final
name|String
name|SITE_PLUGIN_VERSION
init|=
literal|"0.0.7-BOND-SITE"
decl_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNodesInfos
specifier|public
name|void
name|testNodesInfos
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|nodesIds
init|=
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
literal|2
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|String
name|node_1
init|=
name|nodesIds
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|String
name|node_2
init|=
name|nodesIds
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ClusterHealthResponse
name|clusterHealth
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
name|prepareHealth
argument_list|()
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"2"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> done cluster_health, status "
operator|+
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|server1NodeId
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|,
name|node_1
argument_list|)
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
decl_stmt|;
name|String
name|server2NodeId
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|,
name|node_2
argument_list|)
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> started nodes: "
operator|+
name|server1NodeId
operator|+
literal|" and "
operator|+
name|server2NodeId
argument_list|)
expr_stmt|;
name|NodesInfoResponse
name|response
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
name|assertThat
argument_list|(
name|response
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodesMap
argument_list|()
operator|.
name|get
argument_list|(
name|server1NodeId
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodesMap
argument_list|()
operator|.
name|get
argument_list|(
name|server2NodeId
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|nodesInfo
argument_list|(
name|nodesInfoRequest
argument_list|()
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodesMap
argument_list|()
operator|.
name|get
argument_list|(
name|server1NodeId
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodesMap
argument_list|()
operator|.
name|get
argument_list|(
name|server2NodeId
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|nodesInfo
argument_list|(
name|nodesInfoRequest
argument_list|(
name|server1NodeId
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodesMap
argument_list|()
operator|.
name|get
argument_list|(
name|server1NodeId
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|nodesInfo
argument_list|(
name|nodesInfoRequest
argument_list|(
name|server1NodeId
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodesMap
argument_list|()
operator|.
name|get
argument_list|(
name|server1NodeId
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|nodesInfo
argument_list|(
name|nodesInfoRequest
argument_list|(
name|server2NodeId
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodesMap
argument_list|()
operator|.
name|get
argument_list|(
name|server2NodeId
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|nodesInfo
argument_list|(
name|nodesInfoRequest
argument_list|(
name|server2NodeId
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNodesMap
argument_list|()
operator|.
name|get
argument_list|(
name|server2NodeId
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

