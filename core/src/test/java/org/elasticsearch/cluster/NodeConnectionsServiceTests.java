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
name|component
operator|.
name|Lifecycle
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
name|component
operator|.
name|LifecycleListener
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
name|BoundTransportAddress
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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
name|ConnectTransportException
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
name|Transport
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
name|TransportException
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
name|TransportRequest
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
name|TransportRequestOptions
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
name|TransportServiceAdapter
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
name|AfterClass
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
name|ArrayList
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
DECL|class|NodeConnectionsServiceTests
specifier|public
class|class
name|NodeConnectionsServiceTests
extends|extends
name|ESTestCase
block|{
DECL|field|THREAD_POOL
specifier|private
specifier|static
name|ThreadPool
name|THREAD_POOL
decl_stmt|;
DECL|field|transport
specifier|private
name|MockTransport
name|transport
decl_stmt|;
DECL|field|transportService
specifier|private
name|TransportService
name|transportService
decl_stmt|;
DECL|method|generateNodes
specifier|private
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|generateNodes
parameter_list|()
block|{
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|randomIntBetween
argument_list|(
literal|20
argument_list|,
literal|50
argument_list|)
init|;
name|i
operator|>
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|Set
argument_list|<
name|DiscoveryNode
operator|.
name|Role
argument_list|>
name|roles
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|randomSubsetOf
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|DiscoveryNode
operator|.
name|Role
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|nodes
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"node_"
operator|+
name|i
argument_list|,
literal|""
operator|+
name|i
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|roles
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|nodes
return|;
block|}
DECL|method|clusterStateFromNodes
specifier|private
name|ClusterState
name|clusterStateFromNodes
parameter_list|(
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
parameter_list|)
block|{
specifier|final
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
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|nodes
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
return|return
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
name|builder
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|testConnectAndDisconnect
specifier|public
name|void
name|testConnectAndDisconnect
parameter_list|()
block|{
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|generateNodes
argument_list|()
decl_stmt|;
name|NodeConnectionsService
name|service
init|=
operator|new
name|NodeConnectionsService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|THREAD_POOL
argument_list|,
name|transportService
argument_list|)
decl_stmt|;
name|ClusterState
name|current
init|=
name|clusterStateFromNodes
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterChangedEvent
name|event
init|=
operator|new
name|ClusterChangedEvent
argument_list|(
literal|"test"
argument_list|,
name|clusterStateFromNodes
argument_list|(
name|randomSubsetOf
argument_list|(
name|nodes
argument_list|)
argument_list|)
argument_list|,
name|current
argument_list|)
decl_stmt|;
name|service
operator|.
name|connectToAddedNodes
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|assertConnected
argument_list|(
name|event
operator|.
name|nodesDelta
argument_list|()
operator|.
name|addedNodes
argument_list|()
argument_list|)
expr_stmt|;
name|service
operator|.
name|disconnectFromRemovedNodes
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|assertConnectedExactlyToNodes
argument_list|(
name|event
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|current
operator|=
name|event
operator|.
name|state
argument_list|()
expr_stmt|;
name|event
operator|=
operator|new
name|ClusterChangedEvent
argument_list|(
literal|"test"
argument_list|,
name|clusterStateFromNodes
argument_list|(
name|randomSubsetOf
argument_list|(
name|nodes
argument_list|)
argument_list|)
argument_list|,
name|current
argument_list|)
expr_stmt|;
name|service
operator|.
name|connectToAddedNodes
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|assertConnected
argument_list|(
name|event
operator|.
name|nodesDelta
argument_list|()
operator|.
name|addedNodes
argument_list|()
argument_list|)
expr_stmt|;
name|service
operator|.
name|disconnectFromRemovedNodes
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|assertConnectedExactlyToNodes
argument_list|(
name|event
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testReconnect
specifier|public
name|void
name|testReconnect
parameter_list|()
block|{
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|generateNodes
argument_list|()
decl_stmt|;
name|NodeConnectionsService
name|service
init|=
operator|new
name|NodeConnectionsService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|THREAD_POOL
argument_list|,
name|transportService
argument_list|)
decl_stmt|;
name|ClusterState
name|current
init|=
name|clusterStateFromNodes
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterChangedEvent
name|event
init|=
operator|new
name|ClusterChangedEvent
argument_list|(
literal|"test"
argument_list|,
name|clusterStateFromNodes
argument_list|(
name|randomSubsetOf
argument_list|(
name|nodes
argument_list|)
argument_list|)
argument_list|,
name|current
argument_list|)
decl_stmt|;
name|transport
operator|.
name|randomConnectionExceptions
operator|=
literal|true
expr_stmt|;
name|service
operator|.
name|connectToAddedNodes
argument_list|(
name|event
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
comment|// simulate disconnects
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|randomSubsetOf
argument_list|(
name|nodes
argument_list|)
control|)
block|{
name|transport
operator|.
name|disconnectFromNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
name|service
operator|.
expr|new
name|ConnectionChecker
argument_list|()
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
comment|// disable exceptions so things can be restored
name|transport
operator|.
name|randomConnectionExceptions
operator|=
literal|false
expr_stmt|;
name|service
operator|.
expr|new
name|ConnectionChecker
argument_list|()
operator|.
name|run
argument_list|()
expr_stmt|;
name|assertConnectedExactlyToNodes
argument_list|(
name|event
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|assertConnectedExactlyToNodes
specifier|private
name|void
name|assertConnectedExactlyToNodes
parameter_list|(
name|ClusterState
name|state
parameter_list|)
block|{
name|assertConnected
argument_list|(
name|state
operator|.
name|nodes
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|transport
operator|.
name|connectedNodes
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|getSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertConnected
specifier|private
name|void
name|assertConnected
parameter_list|(
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
parameter_list|)
block|{
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|nodes
control|)
block|{
name|assertTrue
argument_list|(
literal|"not connected to "
operator|+
name|node
argument_list|,
name|transport
operator|.
name|connectedNodes
operator|.
name|contains
argument_list|(
name|node
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertNotConnected
specifier|private
name|void
name|assertNotConnected
parameter_list|(
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
parameter_list|)
block|{
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|nodes
control|)
block|{
name|assertFalse
argument_list|(
literal|"still connected to "
operator|+
name|node
argument_list|,
name|transport
operator|.
name|connectedNodes
operator|.
name|contains
argument_list|(
name|node
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
name|this
operator|.
name|transport
operator|=
operator|new
name|MockTransport
argument_list|()
expr_stmt|;
name|transportService
operator|=
operator|new
name|TransportService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|transport
argument_list|,
name|THREAD_POOL
argument_list|,
name|TransportService
operator|.
name|NOOP_TRANSPORT_INTERCEPTOR
argument_list|)
expr_stmt|;
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
name|transportService
operator|.
name|stop
argument_list|()
expr_stmt|;
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|stopThreadPool
specifier|public
specifier|static
name|void
name|stopThreadPool
parameter_list|()
block|{
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|THREAD_POOL
argument_list|,
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|THREAD_POOL
operator|=
literal|null
expr_stmt|;
block|}
DECL|class|MockTransport
specifier|final
class|class
name|MockTransport
implements|implements
name|Transport
block|{
DECL|field|connectedNodes
name|Set
argument_list|<
name|DiscoveryNode
argument_list|>
name|connectedNodes
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentSet
argument_list|()
decl_stmt|;
DECL|field|randomConnectionExceptions
specifier|volatile
name|boolean
name|randomConnectionExceptions
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
DECL|method|transportServiceAdapter
specifier|public
name|void
name|transportServiceAdapter
parameter_list|(
name|TransportServiceAdapter
name|service
parameter_list|)
block|{          }
annotation|@
name|Override
DECL|method|boundAddress
specifier|public
name|BoundTransportAddress
name|boundAddress
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|profileBoundAddresses
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|BoundTransportAddress
argument_list|>
name|profileBoundAddresses
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|addressesFromString
specifier|public
name|TransportAddress
index|[]
name|addressesFromString
parameter_list|(
name|String
name|address
parameter_list|,
name|int
name|perAddressLimit
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|new
name|TransportAddress
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
DECL|method|addressSupported
specifier|public
name|boolean
name|addressSupported
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|TransportAddress
argument_list|>
name|address
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|nodeConnected
specifier|public
name|boolean
name|nodeConnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
return|return
name|connectedNodes
operator|.
name|contains
argument_list|(
name|node
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|connectToNode
specifier|public
name|void
name|connectToNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
if|if
condition|(
name|connectedNodes
operator|.
name|contains
argument_list|(
name|node
argument_list|)
operator|==
literal|false
operator|&&
name|randomConnectionExceptions
operator|&&
name|randomBoolean
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"simulated"
argument_list|)
throw|;
block|}
name|connectedNodes
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|connectToNodeLight
specifier|public
name|void
name|connectToNodeLight
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{          }
annotation|@
name|Override
DECL|method|disconnectFromNode
specifier|public
name|void
name|disconnectFromNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|connectedNodes
operator|.
name|remove
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|sendRequest
specifier|public
name|void
name|sendRequest
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|long
name|requestId
parameter_list|,
name|String
name|action
parameter_list|,
name|TransportRequest
name|request
parameter_list|,
name|TransportRequestOptions
name|options
parameter_list|)
throws|throws
name|IOException
throws|,
name|TransportException
block|{          }
annotation|@
name|Override
DECL|method|serverOpen
specifier|public
name|long
name|serverOpen
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|getLocalAddresses
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getLocalAddresses
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|lifecycleState
specifier|public
name|Lifecycle
operator|.
name|State
name|lifecycleState
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|addLifecycleListener
specifier|public
name|void
name|addLifecycleListener
parameter_list|(
name|LifecycleListener
name|listener
parameter_list|)
block|{          }
annotation|@
name|Override
DECL|method|removeLifecycleListener
specifier|public
name|void
name|removeLifecycleListener
parameter_list|(
name|LifecycleListener
name|listener
parameter_list|)
block|{          }
annotation|@
name|Override
DECL|method|start
specifier|public
name|void
name|start
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|stop
specifier|public
name|void
name|stop
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{}
block|}
block|}
end_class

end_unit

