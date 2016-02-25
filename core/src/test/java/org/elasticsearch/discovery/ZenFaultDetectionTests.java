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
name|discovery
operator|.
name|zen
operator|.
name|fd
operator|.
name|MasterFaultDetection
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
name|NodesFaultDetection
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
name|cluster
operator|.
name|NoopClusterService
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
name|local
operator|.
name|LocalTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|concurrent
operator|.
name|TimeUnit
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
DECL|class|ZenFaultDetectionTests
specifier|public
class|class
name|ZenFaultDetectionTests
extends|extends
name|ESTestCase
block|{
DECL|field|threadPool
specifier|protected
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|version0
specifier|protected
specifier|static
specifier|final
name|Version
name|version0
init|=
name|Version
operator|.
name|fromId
argument_list|(
comment|/*0*/
literal|99
argument_list|)
decl_stmt|;
DECL|field|nodeA
specifier|protected
name|DiscoveryNode
name|nodeA
decl_stmt|;
DECL|field|serviceA
specifier|protected
name|MockTransportService
name|serviceA
decl_stmt|;
DECL|field|version1
specifier|protected
specifier|static
specifier|final
name|Version
name|version1
init|=
name|Version
operator|.
name|fromId
argument_list|(
literal|199
argument_list|)
decl_stmt|;
DECL|field|nodeB
specifier|protected
name|DiscoveryNode
name|nodeB
decl_stmt|;
DECL|field|serviceB
specifier|protected
name|MockTransportService
name|serviceB
decl_stmt|;
annotation|@
name|Override
annotation|@
name|Before
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|threadPool
operator|=
operator|new
name|ThreadPool
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|serviceA
operator|=
name|build
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"TS_A"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|version0
argument_list|)
expr_stmt|;
name|nodeA
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"TS_A"
argument_list|,
literal|"TS_A"
argument_list|,
name|serviceA
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
name|version0
argument_list|)
expr_stmt|;
name|serviceB
operator|=
name|build
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"TS_B"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|version1
argument_list|)
expr_stmt|;
name|nodeB
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"TS_B"
argument_list|,
literal|"TS_B"
argument_list|,
name|serviceB
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
name|version1
argument_list|)
expr_stmt|;
comment|// wait till all nodes are properly connected and the event has been sent, so tests in this class
comment|// will not get this callback called on the connections done in this setup
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|TransportConnectionListener
name|waitForConnection
init|=
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
name|latch
operator|.
name|countDown
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
block|{
name|fail
argument_list|(
literal|"disconnect should not be called "
operator|+
name|node
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|serviceA
operator|.
name|addConnectionListener
argument_list|(
name|waitForConnection
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|addConnectionListener
argument_list|(
name|waitForConnection
argument_list|)
expr_stmt|;
name|serviceA
operator|.
name|connectToNode
argument_list|(
name|nodeB
argument_list|)
expr_stmt|;
name|serviceA
operator|.
name|connectToNode
argument_list|(
name|nodeA
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|connectToNode
argument_list|(
name|nodeA
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|connectToNode
argument_list|(
name|nodeB
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"failed to wait for all nodes to connect"
argument_list|,
name|latch
operator|.
name|await
argument_list|(
literal|5
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|serviceA
operator|.
name|removeConnectionListener
argument_list|(
name|waitForConnection
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|removeConnectionListener
argument_list|(
name|waitForConnection
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|After
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|serviceA
operator|.
name|close
argument_list|()
expr_stmt|;
name|serviceB
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
DECL|method|build
specifier|protected
name|MockTransportService
name|build
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
name|NamedWriteableRegistry
name|namedWriteableRegistry
init|=
operator|new
name|NamedWriteableRegistry
argument_list|()
decl_stmt|;
name|MockTransportService
name|transportService
init|=
operator|new
name|MockTransportService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|LocalTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|version
argument_list|,
name|namedWriteableRegistry
argument_list|)
argument_list|,
name|threadPool
argument_list|,
name|namedWriteableRegistry
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
return|return
name|transportService
return|;
block|}
DECL|method|buildNodesForA
specifier|private
name|DiscoveryNodes
name|buildNodesForA
parameter_list|(
name|boolean
name|master
parameter_list|)
block|{
name|DiscoveryNodes
operator|.
name|Builder
name|builder
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|nodeA
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|nodeB
argument_list|)
expr_stmt|;
name|builder
operator|.
name|localNodeId
argument_list|(
name|nodeA
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|masterNodeId
argument_list|(
name|master
condition|?
name|nodeA
operator|.
name|id
argument_list|()
else|:
name|nodeB
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|buildNodesForB
specifier|private
name|DiscoveryNodes
name|buildNodesForB
parameter_list|(
name|boolean
name|master
parameter_list|)
block|{
name|DiscoveryNodes
operator|.
name|Builder
name|builder
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|nodeA
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|nodeB
argument_list|)
expr_stmt|;
name|builder
operator|.
name|localNodeId
argument_list|(
name|nodeB
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|masterNodeId
argument_list|(
name|master
condition|?
name|nodeB
operator|.
name|id
argument_list|()
else|:
name|nodeA
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|testNodesFaultDetectionConnectOnDisconnect
specifier|public
name|void
name|testNodesFaultDetectionConnectOnDisconnect
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|Settings
operator|.
name|Builder
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|boolean
name|shouldRetry
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
comment|// make sure we don't ping again after the initial ping
name|settings
operator|.
name|put
argument_list|(
name|FaultDetection
operator|.
name|CONNECT_ON_NETWORK_DISCONNECT_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|shouldRetry
argument_list|)
operator|.
name|put
argument_list|(
name|FaultDetection
operator|.
name|PING_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"5m"
argument_list|)
expr_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|nodes
argument_list|(
name|buildNodesForA
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NodesFaultDetection
name|nodesFDA
init|=
operator|new
name|NodesFaultDetection
argument_list|(
name|settings
operator|.
name|build
argument_list|()
argument_list|,
name|threadPool
argument_list|,
name|serviceA
argument_list|,
name|clusterState
operator|.
name|getClusterName
argument_list|()
argument_list|)
decl_stmt|;
name|nodesFDA
operator|.
name|setLocalNode
argument_list|(
name|nodeA
argument_list|)
expr_stmt|;
name|NodesFaultDetection
name|nodesFDB
init|=
operator|new
name|NodesFaultDetection
argument_list|(
name|settings
operator|.
name|build
argument_list|()
argument_list|,
name|threadPool
argument_list|,
name|serviceB
argument_list|,
name|clusterState
operator|.
name|getClusterName
argument_list|()
argument_list|)
decl_stmt|;
name|nodesFDB
operator|.
name|setLocalNode
argument_list|(
name|nodeB
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|pingSent
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|nodesFDB
operator|.
name|addListener
argument_list|(
operator|new
name|NodesFaultDetection
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onPingReceived
parameter_list|(
name|NodesFaultDetection
operator|.
name|PingRequest
name|pingRequest
parameter_list|)
block|{
name|pingSent
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|nodesFDA
operator|.
name|updateNodesAndPing
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
comment|// wait for the first ping to go out, so we will really respond to a disconnect event rather then
comment|// the ping failing
name|pingSent
operator|.
name|await
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
specifier|final
name|String
index|[]
name|failureReason
init|=
operator|new
name|String
index|[
literal|1
index|]
decl_stmt|;
specifier|final
name|DiscoveryNode
index|[]
name|failureNode
init|=
operator|new
name|DiscoveryNode
index|[
literal|1
index|]
decl_stmt|;
specifier|final
name|CountDownLatch
name|notified
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|nodesFDA
operator|.
name|addListener
argument_list|(
operator|new
name|NodesFaultDetection
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onNodeFailure
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
name|failureNode
index|[
literal|0
index|]
operator|=
name|node
expr_stmt|;
name|failureReason
index|[
literal|0
index|]
operator|=
name|reason
expr_stmt|;
name|notified
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// will raise a disconnect on A
name|serviceB
operator|.
name|stop
argument_list|()
expr_stmt|;
name|notified
operator|.
name|await
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|nodeB
argument_list|,
name|failureNode
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Matcher
argument_list|<
name|String
argument_list|>
name|matcher
init|=
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"verified"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|shouldRetry
condition|)
block|{
name|matcher
operator|=
name|Matchers
operator|.
name|not
argument_list|(
name|matcher
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|failureReason
index|[
literal|0
index|]
argument_list|,
name|matcher
argument_list|)
expr_stmt|;
block|}
DECL|method|testMasterFaultDetectionConnectOnDisconnect
specifier|public
name|void
name|testMasterFaultDetectionConnectOnDisconnect
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|Settings
operator|.
name|Builder
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|boolean
name|shouldRetry
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
comment|// make sure we don't ping
name|settings
operator|.
name|put
argument_list|(
name|FaultDetection
operator|.
name|CONNECT_ON_NETWORK_DISCONNECT_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|shouldRetry
argument_list|)
operator|.
name|put
argument_list|(
name|FaultDetection
operator|.
name|PING_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"5m"
argument_list|)
expr_stmt|;
name|ClusterName
name|clusterName
init|=
operator|new
name|ClusterName
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
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
name|buildNodesForA
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|MasterFaultDetection
name|masterFD
init|=
operator|new
name|MasterFaultDetection
argument_list|(
name|settings
operator|.
name|build
argument_list|()
argument_list|,
name|threadPool
argument_list|,
name|serviceA
argument_list|,
name|clusterName
argument_list|,
operator|new
name|NoopClusterService
argument_list|(
name|state
argument_list|)
argument_list|)
decl_stmt|;
name|masterFD
operator|.
name|start
argument_list|(
name|nodeB
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
specifier|final
name|String
index|[]
name|failureReason
init|=
operator|new
name|String
index|[
literal|1
index|]
decl_stmt|;
specifier|final
name|DiscoveryNode
index|[]
name|failureNode
init|=
operator|new
name|DiscoveryNode
index|[
literal|1
index|]
decl_stmt|;
specifier|final
name|CountDownLatch
name|notified
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|masterFD
operator|.
name|addListener
argument_list|(
operator|new
name|MasterFaultDetection
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onMasterFailure
parameter_list|(
name|DiscoveryNode
name|masterNode
parameter_list|,
name|Throwable
name|cause
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
name|failureNode
index|[
literal|0
index|]
operator|=
name|masterNode
expr_stmt|;
name|failureReason
index|[
literal|0
index|]
operator|=
name|reason
expr_stmt|;
name|notified
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// will raise a disconnect on A
name|serviceB
operator|.
name|stop
argument_list|()
expr_stmt|;
name|notified
operator|.
name|await
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|nodeB
argument_list|,
name|failureNode
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Matcher
argument_list|<
name|String
argument_list|>
name|matcher
init|=
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"verified"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|shouldRetry
condition|)
block|{
name|matcher
operator|=
name|Matchers
operator|.
name|not
argument_list|(
name|matcher
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|failureReason
index|[
literal|0
index|]
argument_list|,
name|matcher
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

