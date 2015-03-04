begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
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
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
operator|.
name|Scope
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
name|discovery
operator|.
name|ClusterDiscoveryConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|ZenUnicastDiscoveryTests
specifier|public
class|class
name|ZenUnicastDiscoveryTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|discoveryConfig
specifier|private
name|ClusterDiscoveryConfiguration
name|discoveryConfig
decl_stmt|;
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
name|discoveryConfig
operator|.
name|node
argument_list|(
name|nodeOrdinal
argument_list|)
return|;
block|}
annotation|@
name|Before
DECL|method|clearConfig
specifier|public
name|void
name|clearConfig
parameter_list|()
block|{
name|discoveryConfig
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNormalClusterForming
specifier|public
name|void
name|testNormalClusterForming
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
name|int
name|currentNumNodes
init|=
name|randomIntBetween
argument_list|(
literal|3
argument_list|,
literal|5
argument_list|)
decl_stmt|;
comment|// use explicit unicast hosts so we can start those first
name|int
index|[]
name|unicastHostOrdinals
init|=
operator|new
name|int
index|[
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|currentNumNodes
argument_list|)
index|]
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
name|unicastHostOrdinals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|unicastHostOrdinals
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
name|discoveryConfig
operator|=
operator|new
name|ClusterDiscoveryConfiguration
operator|.
name|UnicastZen
argument_list|(
name|currentNumNodes
argument_list|,
name|unicastHostOrdinals
argument_list|)
expr_stmt|;
comment|// start the unicast hosts
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
name|unicastHostOrdinals
operator|.
name|length
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// start the rest of the cluster
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
name|currentNumNodes
operator|-
name|unicastHostOrdinals
operator|.
name|length
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
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
name|setWaitForNodes
argument_list|(
literal|""
operator|+
name|currentNumNodes
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isTimedOut
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"cluster forming timed out, cluster state:\n{}"
argument_list|,
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
name|get
argument_list|()
operator|.
name|getState
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"timed out waiting for cluster to form with ["
operator|+
name|currentNumNodes
operator|+
literal|"] nodes"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
comment|// Without the 'include temporalResponses responses to nodesToConnect' improvement in UnicastZenPing#sendPings this
comment|// test fails, because 2 nodes elect themselves as master and the health request times out b/c waiting_for_nodes=N
comment|// can't be satisfied.
DECL|method|testMinimumMasterNodes
specifier|public
name|void
name|testMinimumMasterNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|currentNumNodes
init|=
name|randomIntBetween
argument_list|(
literal|3
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|int
name|currentNumOfUnicastHosts
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|currentNumNodes
argument_list|)
decl_stmt|;
specifier|final
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
literal|"discovery.zen.minimum_master_nodes"
argument_list|,
name|currentNumNodes
operator|/
literal|2
operator|+
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|discoveryConfig
operator|=
operator|new
name|ClusterDiscoveryConfiguration
operator|.
name|UnicastZen
argument_list|(
name|currentNumNodes
argument_list|,
name|currentNumOfUnicastHosts
argument_list|,
name|settings
argument_list|)
expr_stmt|;
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
name|currentNumNodes
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|DiscoveryNode
name|masterDiscoNode
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|node
range|:
name|nodes
control|)
block|{
name|ClusterState
name|state
init|=
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|currentNumNodes
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|masterDiscoNode
operator|==
literal|null
condition|)
block|{
name|masterDiscoNode
operator|=
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|masterDiscoNode
operator|.
name|equals
argument_list|(
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
argument_list|)
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
end_class

end_unit

