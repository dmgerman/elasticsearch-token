begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.zen.ping.unicast
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|ping
operator|.
name|unicast
package|;
end_package

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
name|io
operator|.
name|stream
operator|.
name|NamedWriteableRegistry
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
name|network
operator|.
name|NetworkAddress
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
name|network
operator|.
name|NetworkService
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
name|transport
operator|.
name|InetSocketTransportAddress
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
name|transport
operator|.
name|TransportAddress
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
name|common
operator|.
name|util
operator|.
name|BigArrays
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
name|elect
operator|.
name|ElectMasterService
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
name|ping
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
name|ping
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
name|indices
operator|.
name|breaker
operator|.
name|NoneCircuitBreakerService
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
name|ESTestCase
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
name|VersionUtils
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
name|threadpool
operator|.
name|ThreadPool
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
name|TransportConnectionListener
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
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportSettings
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
name|netty
operator|.
name|NettyTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|util
operator|.
name|internal
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
name|ConcurrentMap
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptySet
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

begin_class
DECL|class|UnicastZenPingIT
specifier|public
class|class
name|UnicastZenPingIT
extends|extends
name|ESTestCase
block|{
DECL|method|testSimplePings
specifier|public
name|void
name|testSimplePings
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|EMPTY
decl_stmt|;
name|int
name|startPort
init|=
literal|11000
operator|+
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|int
name|endPort
init|=
name|startPort
operator|+
literal|10
decl_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
name|TransportSettings
operator|.
name|PORT
operator|.
name|getKey
argument_list|()
argument_list|,
name|startPort
operator|+
literal|"-"
operator|+
name|endPort
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|ThreadPool
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
name|ClusterName
name|test
init|=
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|ClusterName
name|mismatch
init|=
operator|new
name|ClusterName
argument_list|(
literal|"mismatch"
argument_list|)
decl_stmt|;
name|NetworkService
name|networkService
init|=
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|ElectMasterService
name|electMasterService
init|=
operator|new
name|ElectMasterService
argument_list|(
name|settings
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|NetworkHandle
name|handleA
init|=
name|startServices
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|networkService
argument_list|,
literal|"UZP_A"
argument_list|,
name|test
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|NetworkHandle
name|handleB
init|=
name|startServices
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|networkService
argument_list|,
literal|"UZP_B"
argument_list|,
name|test
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|NetworkHandle
name|handleC
init|=
name|startServices
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|networkService
argument_list|,
literal|"UZP_C"
argument_list|,
operator|new
name|ClusterName
argument_list|(
literal|"mismatch"
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
comment|// just fake that no versions are compatible with this node
name|Version
name|previousVersion
init|=
name|VersionUtils
operator|.
name|getPreviousVersion
argument_list|(
name|Version
operator|.
name|CURRENT
operator|.
name|minimumCompatibilityVersion
argument_list|()
argument_list|)
decl_stmt|;
name|Version
name|versionD
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|previousVersion
operator|.
name|minimumCompatibilityVersion
argument_list|()
argument_list|,
name|previousVersion
argument_list|)
decl_stmt|;
name|NetworkHandle
name|handleD
init|=
name|startServices
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|networkService
argument_list|,
literal|"UZP_D"
argument_list|,
name|test
argument_list|,
name|versionD
argument_list|)
decl_stmt|;
name|Settings
name|hostsSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
literal|"discovery.zen.ping.unicast.hosts"
argument_list|,
name|NetworkAddress
operator|.
name|format
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|handleA
operator|.
name|address
operator|.
name|address
argument_list|()
operator|.
name|getAddress
argument_list|()
argument_list|,
name|handleA
operator|.
name|address
operator|.
name|address
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|NetworkAddress
operator|.
name|format
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|handleB
operator|.
name|address
operator|.
name|address
argument_list|()
operator|.
name|getAddress
argument_list|()
argument_list|,
name|handleB
operator|.
name|address
operator|.
name|address
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|NetworkAddress
operator|.
name|format
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|handleC
operator|.
name|address
operator|.
name|address
argument_list|()
operator|.
name|getAddress
argument_list|()
argument_list|,
name|handleC
operator|.
name|address
operator|.
name|address
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|NetworkAddress
operator|.
name|format
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|handleD
operator|.
name|address
operator|.
name|address
argument_list|()
operator|.
name|getAddress
argument_list|()
argument_list|,
name|handleD
operator|.
name|address
operator|.
name|address
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|UnicastZenPing
name|zenPingA
init|=
operator|new
name|UnicastZenPing
argument_list|(
name|hostsSettings
argument_list|,
name|threadPool
argument_list|,
name|handleA
operator|.
name|transportService
argument_list|,
name|test
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|electMasterService
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|zenPingA
operator|.
name|setPingContextProvider
argument_list|(
operator|new
name|PingContextProvider
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DiscoveryNodes
name|nodes
parameter_list|()
block|{
return|return
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|handleA
operator|.
name|node
argument_list|)
operator|.
name|localNodeId
argument_list|(
literal|"UZP_A"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|nodeHasJoinedClusterOnce
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|zenPingA
operator|.
name|start
argument_list|()
expr_stmt|;
name|UnicastZenPing
name|zenPingB
init|=
operator|new
name|UnicastZenPing
argument_list|(
name|hostsSettings
argument_list|,
name|threadPool
argument_list|,
name|handleB
operator|.
name|transportService
argument_list|,
name|test
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|electMasterService
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|zenPingB
operator|.
name|setPingContextProvider
argument_list|(
operator|new
name|PingContextProvider
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DiscoveryNodes
name|nodes
parameter_list|()
block|{
return|return
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|handleB
operator|.
name|node
argument_list|)
operator|.
name|localNodeId
argument_list|(
literal|"UZP_B"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|nodeHasJoinedClusterOnce
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|zenPingB
operator|.
name|start
argument_list|()
expr_stmt|;
name|UnicastZenPing
name|zenPingC
init|=
operator|new
name|UnicastZenPing
argument_list|(
name|hostsSettings
argument_list|,
name|threadPool
argument_list|,
name|handleC
operator|.
name|transportService
argument_list|,
name|mismatch
argument_list|,
name|versionD
argument_list|,
name|electMasterService
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|zenPingC
operator|.
name|setPingContextProvider
argument_list|(
operator|new
name|PingContextProvider
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DiscoveryNodes
name|nodes
parameter_list|()
block|{
return|return
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|handleC
operator|.
name|node
argument_list|)
operator|.
name|localNodeId
argument_list|(
literal|"UZP_C"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|nodeHasJoinedClusterOnce
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|zenPingC
operator|.
name|start
argument_list|()
expr_stmt|;
name|UnicastZenPing
name|zenPingD
init|=
operator|new
name|UnicastZenPing
argument_list|(
name|hostsSettings
argument_list|,
name|threadPool
argument_list|,
name|handleD
operator|.
name|transportService
argument_list|,
name|mismatch
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|electMasterService
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|zenPingD
operator|.
name|setPingContextProvider
argument_list|(
operator|new
name|PingContextProvider
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DiscoveryNodes
name|nodes
parameter_list|()
block|{
return|return
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|handleD
operator|.
name|node
argument_list|)
operator|.
name|localNodeId
argument_list|(
literal|"UZP_D"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|nodeHasJoinedClusterOnce
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|zenPingD
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"ping from UZP_A"
argument_list|)
expr_stmt|;
name|ZenPing
operator|.
name|PingResponse
index|[]
name|pingResponses
init|=
name|zenPingA
operator|.
name|pingAndWait
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|pingResponses
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pingResponses
index|[
literal|0
index|]
operator|.
name|node
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"UZP_B"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|pingResponses
index|[
literal|0
index|]
operator|.
name|hasJoinedOnce
argument_list|()
argument_list|)
expr_stmt|;
name|assertCounters
argument_list|(
name|handleA
argument_list|,
name|handleA
argument_list|,
name|handleB
argument_list|,
name|handleC
argument_list|,
name|handleD
argument_list|)
expr_stmt|;
comment|// ping again, this time from B,
name|logger
operator|.
name|info
argument_list|(
literal|"ping from UZP_B"
argument_list|)
expr_stmt|;
name|pingResponses
operator|=
name|zenPingB
operator|.
name|pingAndWait
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pingResponses
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pingResponses
index|[
literal|0
index|]
operator|.
name|node
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"UZP_A"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|pingResponses
index|[
literal|0
index|]
operator|.
name|hasJoinedOnce
argument_list|()
argument_list|)
expr_stmt|;
name|assertCounters
argument_list|(
name|handleB
argument_list|,
name|handleA
argument_list|,
name|handleB
argument_list|,
name|handleC
argument_list|,
name|handleD
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"ping from UZP_C"
argument_list|)
expr_stmt|;
name|pingResponses
operator|=
name|zenPingC
operator|.
name|pingAndWait
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pingResponses
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertCounters
argument_list|(
name|handleC
argument_list|,
name|handleA
argument_list|,
name|handleB
argument_list|,
name|handleC
argument_list|,
name|handleD
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"ping from UZP_D"
argument_list|)
expr_stmt|;
name|pingResponses
operator|=
name|zenPingD
operator|.
name|pingAndWait
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pingResponses
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertCounters
argument_list|(
name|handleD
argument_list|,
name|handleA
argument_list|,
name|handleB
argument_list|,
name|handleC
argument_list|,
name|handleD
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|zenPingA
operator|.
name|close
argument_list|()
expr_stmt|;
name|zenPingB
operator|.
name|close
argument_list|()
expr_stmt|;
name|zenPingC
operator|.
name|close
argument_list|()
expr_stmt|;
name|zenPingD
operator|.
name|close
argument_list|()
expr_stmt|;
name|handleA
operator|.
name|transportService
operator|.
name|close
argument_list|()
expr_stmt|;
name|handleB
operator|.
name|transportService
operator|.
name|close
argument_list|()
expr_stmt|;
name|handleC
operator|.
name|transportService
operator|.
name|close
argument_list|()
expr_stmt|;
name|handleD
operator|.
name|transportService
operator|.
name|close
argument_list|()
expr_stmt|;
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
block|}
comment|// assert that we tried to ping each of the configured nodes at least once
DECL|method|assertCounters
specifier|private
name|void
name|assertCounters
parameter_list|(
name|NetworkHandle
name|that
parameter_list|,
name|NetworkHandle
modifier|...
name|handles
parameter_list|)
block|{
for|for
control|(
name|NetworkHandle
name|handle
range|:
name|handles
control|)
block|{
if|if
condition|(
name|handle
operator|!=
name|that
condition|)
block|{
name|assertThat
argument_list|(
name|that
operator|.
name|counters
operator|.
name|get
argument_list|(
name|handle
operator|.
name|address
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|startServices
specifier|private
name|NetworkHandle
name|startServices
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|NetworkService
name|networkService
parameter_list|,
name|String
name|nodeId
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
name|NettyTransport
name|transport
init|=
operator|new
name|NettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|networkService
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|,
name|version
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|,
operator|new
name|NoneCircuitBreakerService
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|TransportService
name|transportService
init|=
operator|new
name|TransportService
argument_list|(
name|transport
argument_list|,
name|threadPool
argument_list|,
name|clusterName
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|start
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|acceptIncomingRequests
argument_list|()
expr_stmt|;
name|ConcurrentMap
argument_list|<
name|TransportAddress
argument_list|,
name|AtomicInteger
argument_list|>
name|counters
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|transportService
operator|.
name|addConnectionListener
argument_list|(
operator|new
name|TransportConnectionListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onNodeConnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|counters
operator|.
name|computeIfAbsent
argument_list|(
name|node
operator|.
name|getAddress
argument_list|()
argument_list|,
name|k
lambda|->
operator|new
name|AtomicInteger
argument_list|()
argument_list|)
expr_stmt|;
name|counters
operator|.
name|get
argument_list|(
name|node
operator|.
name|getAddress
argument_list|()
argument_list|)
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onNodeDisconnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{             }
block|}
argument_list|)
expr_stmt|;
specifier|final
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
name|nodeId
argument_list|,
name|transportService
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|version
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|setLocalNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
return|return
operator|new
name|NetworkHandle
argument_list|(
operator|(
name|InetSocketTransportAddress
operator|)
name|transport
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|,
name|transportService
argument_list|,
name|node
argument_list|,
name|counters
argument_list|)
return|;
block|}
DECL|class|NetworkHandle
specifier|private
specifier|static
class|class
name|NetworkHandle
block|{
DECL|field|address
specifier|public
specifier|final
name|InetSocketTransportAddress
name|address
decl_stmt|;
DECL|field|transportService
specifier|public
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|node
specifier|public
specifier|final
name|DiscoveryNode
name|node
decl_stmt|;
DECL|field|counters
specifier|public
specifier|final
name|ConcurrentMap
argument_list|<
name|TransportAddress
argument_list|,
name|AtomicInteger
argument_list|>
name|counters
decl_stmt|;
DECL|method|NetworkHandle
specifier|public
name|NetworkHandle
parameter_list|(
name|InetSocketTransportAddress
name|address
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|DiscoveryNode
name|discoveryNode
parameter_list|,
name|ConcurrentMap
argument_list|<
name|TransportAddress
argument_list|,
name|AtomicInteger
argument_list|>
name|counters
parameter_list|)
block|{
name|this
operator|.
name|address
operator|=
name|address
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|node
operator|=
name|discoveryNode
expr_stmt|;
name|this
operator|.
name|counters
operator|=
name|counters
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

