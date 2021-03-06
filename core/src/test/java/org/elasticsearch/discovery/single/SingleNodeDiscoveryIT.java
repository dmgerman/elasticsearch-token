begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.single
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|single
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
name|IOUtils
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
name|service
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
name|unit
operator|.
name|TimeValue
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
name|PingContextProvider
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
name|UnicastHostsProvider
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
name|UnicastZenPing
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
name|ZenPing
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
name|InternalTestCluster
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
name|NodeConfigurationSource
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
name|threadpool
operator|.
name|TestThreadPool
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
name|MockTcpTransportPlugin
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
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Stack
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
name|CompletableFuture
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
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
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
name|not
import|;
end_import

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
literal|1
argument_list|,
name|numClientNodes
operator|=
literal|0
argument_list|,
name|supportsDedicatedMasters
operator|=
literal|false
argument_list|,
name|autoMinMasterNodes
operator|=
literal|false
argument_list|)
DECL|class|SingleNodeDiscoveryIT
specifier|public
class|class
name|SingleNodeDiscoveryIT
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
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"single-node"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
literal|"0"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|testDoesNotRespondToZenPings
specifier|public
name|void
name|testDoesNotRespondToZenPings
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.name"
argument_list|,
name|internalCluster
argument_list|()
operator|.
name|getClusterName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|Version
name|version
init|=
name|Version
operator|.
name|CURRENT
decl_stmt|;
specifier|final
name|Stack
argument_list|<
name|Closeable
argument_list|>
name|closeables
init|=
operator|new
name|Stack
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|TestThreadPool
name|threadPool
init|=
operator|new
name|TestThreadPool
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|MockTransportService
name|pingTransport
init|=
name|MockTransportService
operator|.
name|createNewService
argument_list|(
name|settings
argument_list|,
name|version
argument_list|,
name|threadPool
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|pingTransport
operator|.
name|start
argument_list|()
expr_stmt|;
name|closeables
operator|.
name|push
argument_list|(
name|pingTransport
argument_list|)
expr_stmt|;
specifier|final
name|TransportService
name|nodeTransport
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// try to ping the single node directly
specifier|final
name|UnicastHostsProvider
name|provider
init|=
parameter_list|()
lambda|->
name|Collections
operator|.
name|singletonList
argument_list|(
name|nodeTransport
operator|.
name|getLocalNode
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|DiscoveryNodes
name|nodes
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|add
argument_list|(
name|nodeTransport
operator|.
name|getLocalNode
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|pingTransport
operator|.
name|getLocalNode
argument_list|()
argument_list|)
operator|.
name|localNodeId
argument_list|(
name|pingTransport
operator|.
name|getLocalNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|ClusterName
name|clusterName
init|=
operator|new
name|ClusterName
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|getClusterName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|ClusterState
name|state
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterName
argument_list|)
operator|.
name|nodes
argument_list|(
name|nodes
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|UnicastZenPing
name|unicastZenPing
init|=
operator|new
name|UnicastZenPing
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|pingTransport
argument_list|,
name|provider
argument_list|,
parameter_list|()
lambda|->
name|state
argument_list|)
block|{                     @
name|Override
specifier|protected
name|void
name|finishPingingRound
argument_list|(
name|PingingRound
name|pingingRound
argument_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
block|;
name|super
operator|.
name|finishPingingRound
argument_list|(
name|pingingRound
argument_list|)
block|;                     }
block|}
empty_stmt|;
name|unicastZenPing
operator|.
name|start
argument_list|()
expr_stmt|;
name|closeables
operator|.
name|push
argument_list|(
name|unicastZenPing
argument_list|)
expr_stmt|;
specifier|final
name|CompletableFuture
argument_list|<
name|ZenPing
operator|.
name|PingCollection
argument_list|>
name|responses
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|unicastZenPing
operator|.
name|ping
argument_list|(
name|responses
operator|::
name|complete
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|responses
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|responses
operator|.
name|get
argument_list|()
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
block|}
finally|finally
block|{
while|while
condition|(
operator|!
name|closeables
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|IOUtils
operator|.
name|closeWhileHandlingException
argument_list|(
name|closeables
operator|.
name|pop
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSingleNodesDoNotDiscoverEachOther
specifier|public
name|void
name|testSingleNodesDoNotDiscoverEachOther
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|TransportService
name|service
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|int
name|port
init|=
name|service
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
operator|.
name|getPort
argument_list|()
decl_stmt|;
specifier|final
name|NodeConfigurationSource
name|configurationSource
init|=
operator|new
name|NodeConfigurationSource
argument_list|()
block|{
annotation|@
name|Override
specifier|public
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
literal|"discovery.type"
argument_list|,
literal|"single-node"
argument_list|)
operator|.
name|put
argument_list|(
literal|"http.enabled"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.type"
argument_list|,
literal|"mock-socket-network"
argument_list|)
comment|/*                          * We align the port ranges of the two as then with zen discovery these two                          * nodes would find each other.                          */
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
name|port
operator|+
literal|"-"
operator|+
operator|(
name|port
operator|+
literal|5
operator|-
literal|1
operator|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
decl_stmt|;
try|try
init|(
name|InternalTestCluster
name|other
init|=
operator|new
name|InternalTestCluster
argument_list|(
name|randomLong
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
name|internalCluster
argument_list|()
operator|.
name|getClusterName
argument_list|()
argument_list|,
name|configurationSource
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|"other"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|MockTcpTransportPlugin
operator|.
name|class
argument_list|)
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|)
init|)
block|{
name|other
operator|.
name|beforeTest
argument_list|(
name|random
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
specifier|final
name|ClusterState
name|first
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
operator|.
name|state
argument_list|()
decl_stmt|;
specifier|final
name|ClusterState
name|second
init|=
name|other
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
operator|.
name|state
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|first
operator|.
name|nodes
argument_list|()
operator|.
name|getSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|second
operator|.
name|nodes
argument_list|()
operator|.
name|getSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|first
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNodeId
argument_list|()
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|second
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNodeId
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|first
operator|.
name|metaData
argument_list|()
operator|.
name|clusterUUID
argument_list|()
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|second
operator|.
name|metaData
argument_list|()
operator|.
name|clusterUUID
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

