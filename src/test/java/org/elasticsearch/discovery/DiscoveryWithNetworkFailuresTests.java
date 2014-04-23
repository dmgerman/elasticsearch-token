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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LuceneTestCase
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
name|ClusterHealthResponse
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
name|common
operator|.
name|Priority
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
name|Arrays
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
name|test
operator|.
name|ElasticsearchIntegrationTest
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
name|ElasticsearchIntegrationTest
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
comment|/**  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|DiscoveryWithNetworkFailuresTests
specifier|public
class|class
name|DiscoveryWithNetworkFailuresTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
annotation|@
name|LuceneTestCase
operator|.
name|AwaitsFix
argument_list|(
name|bugUrl
operator|=
literal|"https://github.com/elasticsearch/elasticsearch/issues/2488"
argument_list|)
DECL|method|failWithMinimumMasterNodesConfigured
specifier|public
name|void
name|failWithMinimumMasterNodesConfigured
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.zen.fd.ping_timeout"
argument_list|,
literal|"1s"
argument_list|)
comment|//<-- for hitting simulated network failures quickly
operator|.
name|put
argument_list|(
name|TransportModule
operator|.
name|TRANSPORT_SERVICE_TYPE_KEY
argument_list|,
name|MockTransportService
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|cluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
literal|3
argument_list|,
name|settings
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// Wait until a green status has been reaches and 3 nodes are part of the cluster
name|List
argument_list|<
name|String
argument_list|>
name|nodesList
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|nodes
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|3
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|ClusterHealthResponse
name|clusterHealthResponse
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForNodes
argument_list|(
literal|"3"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clusterHealthResponse
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// Figure out what is the elected master node
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
name|nodesList
control|)
block|{
name|ClusterState
name|state
init|=
name|cluster
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
literal|3
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
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|masterDiscoNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
assert|assert
name|masterDiscoNode
operator|!=
literal|null
assert|;
name|logger
operator|.
name|info
argument_list|(
literal|"---> legit elected master node="
operator|+
name|masterDiscoNode
argument_list|)
expr_stmt|;
specifier|final
name|Client
name|masterClient
init|=
name|cluster
argument_list|()
operator|.
name|masterClient
argument_list|()
decl_stmt|;
comment|// Everything is stable now, it is now time to simulate evil...
comment|// Pick a node that isn't the elected master.
name|String
name|unluckyNode
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|node
range|:
name|nodesList
control|)
block|{
if|if
condition|(
operator|!
name|node
operator|.
name|equals
argument_list|(
name|masterDiscoNode
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|unluckyNode
operator|=
name|node
expr_stmt|;
block|}
block|}
assert|assert
name|unluckyNode
operator|!=
literal|null
assert|;
comment|// Simulate a network issue between the unlucky node and elected master node in both directions.
name|addFailToSendNoConnectRule
argument_list|(
name|masterDiscoNode
operator|.
name|getName
argument_list|()
argument_list|,
name|unluckyNode
argument_list|)
expr_stmt|;
name|addFailToSendNoConnectRule
argument_list|(
name|unluckyNode
argument_list|,
name|masterDiscoNode
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Wait until elected master has removed that the unlucky node...
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
return|return
name|masterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|2
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// The unlucky node must report *no* master node, since it can't connect to master and in fact it should
comment|// continuously ping until network failures have been resolved.
name|Client
name|isolatedNodeClient
init|=
name|cluster
argument_list|()
operator|.
name|client
argument_list|(
name|unluckyNode
argument_list|)
decl_stmt|;
name|ClusterState
name|localClusterState
init|=
name|isolatedNodeClient
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
decl_stmt|;
name|DiscoveryNodes
name|localDiscoveryNodes
init|=
name|localClusterState
operator|.
name|nodes
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|localDiscoveryNodes
operator|.
name|masterNode
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// stop simulating network failures, from this point on the unlucky node is able to rejoin
comment|// We also need to do this even if assertions fail, since otherwise the test framework can't work properly
name|clearNoConnectRule
argument_list|(
name|masterDiscoNode
operator|.
name|getName
argument_list|()
argument_list|,
name|unluckyNode
argument_list|)
expr_stmt|;
name|clearNoConnectRule
argument_list|(
name|unluckyNode
argument_list|,
name|masterDiscoNode
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Wait until the master node sees all 3 nodes again.
name|clusterHealthResponse
operator|=
name|masterClient
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForNodes
argument_list|(
literal|"3"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealthResponse
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|node
range|:
name|nodesList
control|)
block|{
name|ClusterState
name|state
init|=
name|cluster
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
literal|3
argument_list|)
argument_list|)
expr_stmt|;
comment|// The elected master shouldn't have changed, since the unlucky node never could have elected himself as
comment|// master since m_m_n of 2 could never be satisfied.
name|assertThat
argument_list|(
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|masterDiscoNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|addFailToSendNoConnectRule
specifier|private
name|void
name|addFailToSendNoConnectRule
parameter_list|(
name|String
name|fromNode
parameter_list|,
name|String
name|toNode
parameter_list|)
block|{
name|TransportService
name|mockTransportService
init|=
name|cluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|fromNode
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
name|cluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Discovery
operator|.
name|class
argument_list|,
name|toNode
argument_list|)
operator|.
name|localNode
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|clearNoConnectRule
specifier|private
name|void
name|clearNoConnectRule
parameter_list|(
name|String
name|fromNode
parameter_list|,
name|String
name|toNode
parameter_list|)
block|{
name|TransportService
name|mockTransportService
init|=
name|cluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|fromNode
argument_list|)
decl_stmt|;
operator|(
operator|(
name|MockTransportService
operator|)
name|mockTransportService
operator|)
operator|.
name|clearRule
argument_list|(
name|cluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Discovery
operator|.
name|class
argument_list|,
name|toNode
argument_list|)
operator|.
name|localNode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

