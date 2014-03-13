begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
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
name|util
operator|.
name|concurrent
operator|.
name|AtomicArray
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|concurrent
operator|.
name|CountDownLatch
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
annotation|@
name|LuceneTestCase
operator|.
name|Slow
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
name|numNodes
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
annotation|@
name|Test
annotation|@
name|TestLogging
argument_list|(
literal|"discovery.zen:TRACE"
argument_list|)
comment|// The bug zen unicast ping override bug, may rarely manifest itself, it is very timing dependant.
comment|// Without the fix in UnicastZenPing, this test fails roughly 1 out of 10 runs from the command line.
DECL|method|testMasterElectionNotMissed
specifier|public
name|void
name|testMasterElectionNotMissed
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
comment|// Failure only manifests if multicast ping is disabled!
operator|.
name|put
argument_list|(
literal|"discovery.zen.ping.multicast.ping.enabled"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.zen.minimum_master_nodes"
argument_list|,
literal|2
argument_list|)
comment|// Can't use this, b/c at the moment all node will only ping localhost:9300
comment|//                .put("discovery.zen.ping.unicast.hosts", "localhost")
operator|.
name|put
argument_list|(
literal|"discovery.zen.ping.unicast.hosts"
argument_list|,
literal|"localhost:15300,localhost:15301,localhost:15302"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
literal|"15300-15400"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|3
argument_list|)
decl_stmt|;
specifier|final
name|AtomicArray
argument_list|<
name|String
argument_list|>
name|nodes
init|=
operator|new
name|AtomicArray
argument_list|<
name|String
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|Runnable
name|r1
init|=
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> start first node"
argument_list|)
expr_stmt|;
name|nodes
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
operator|new
name|Thread
argument_list|(
name|r1
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|sleep
argument_list|(
name|between
argument_list|(
literal|500
argument_list|,
literal|3000
argument_list|)
argument_list|)
expr_stmt|;
name|Runnable
name|r2
init|=
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> start second node"
argument_list|)
expr_stmt|;
name|nodes
operator|.
name|set
argument_list|(
literal|1
argument_list|,
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
operator|new
name|Thread
argument_list|(
name|r2
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|sleep
argument_list|(
name|between
argument_list|(
literal|500
argument_list|,
literal|3000
argument_list|)
argument_list|)
expr_stmt|;
name|Runnable
name|r3
init|=
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> start third node"
argument_list|)
expr_stmt|;
name|nodes
operator|.
name|set
argument_list|(
literal|2
argument_list|,
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
operator|new
name|Thread
argument_list|(
name|r3
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clusterHealthResponse
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
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
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|3
index|]
argument_list|)
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

