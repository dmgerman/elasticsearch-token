begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.nodes
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|nodes
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
name|action
operator|.
name|FailedNodeException
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
name|support
operator|.
name|ActionFilters
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
name|support
operator|.
name|PlainActionFuture
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
name|support
operator|.
name|broadcast
operator|.
name|node
operator|.
name|TransportBroadcastByNodeActionTests
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|StreamOutput
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
name|LocalTransportAddress
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
name|transport
operator|.
name|CapturingTransport
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
name|TransportService
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
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|HashMap
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReferenceArray
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
name|Supplier
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
name|ClusterServiceUtils
operator|.
name|createClusterService
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
name|ClusterServiceUtils
operator|.
name|setState
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_class
DECL|class|TransportNodesActionTests
specifier|public
class|class
name|TransportNodesActionTests
extends|extends
name|ESTestCase
block|{
DECL|field|THREAD_POOL
specifier|private
specifier|static
name|ThreadPool
name|THREAD_POOL
decl_stmt|;
DECL|field|clusterService
specifier|private
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|transport
specifier|private
name|CapturingTransport
name|transport
decl_stmt|;
DECL|field|transportService
specifier|private
name|TransportService
name|transportService
decl_stmt|;
DECL|method|testRequestIsSentToEachNode
specifier|public
name|void
name|testRequestIsSentToEachNode
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportNodesAction
name|action
init|=
name|getTestTransportNodesAction
argument_list|()
decl_stmt|;
name|TestNodesRequest
name|request
init|=
operator|new
name|TestNodesRequest
argument_list|()
decl_stmt|;
name|PlainActionFuture
argument_list|<
name|TestNodesResponse
argument_list|>
name|listener
init|=
operator|new
name|PlainActionFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|action
operator|.
expr|new
name|AsyncAction
argument_list|(
literal|null
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|CapturingTransport
operator|.
name|CapturedRequest
argument_list|>
argument_list|>
name|capturedRequests
init|=
name|transport
operator|.
name|getCapturedRequestsByTargetNodeAndClear
argument_list|()
decl_stmt|;
name|int
name|numNodes
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|getNodes
argument_list|()
operator|.
name|getSize
argument_list|()
decl_stmt|;
comment|// check a request was sent to the right number of nodes
name|assertEquals
argument_list|(
name|numNodes
argument_list|,
name|capturedRequests
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testNodesSelectors
specifier|public
name|void
name|testNodesSelectors
parameter_list|()
block|{
name|TransportNodesAction
name|action
init|=
name|getTestTransportNodesAction
argument_list|()
decl_stmt|;
name|int
name|numSelectors
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|nodeSelectors
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
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
name|numSelectors
condition|;
name|i
operator|++
control|)
block|{
name|nodeSelectors
operator|.
name|add
argument_list|(
name|randomFrom
argument_list|(
name|NodeSelector
operator|.
name|values
argument_list|()
argument_list|)
operator|.
name|selector
argument_list|)
expr_stmt|;
block|}
name|int
name|numNodeIds
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|String
index|[]
name|nodeIds
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|getNodes
argument_list|()
operator|.
name|keys
argument_list|()
operator|.
name|toArray
argument_list|(
name|String
operator|.
name|class
argument_list|)
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
name|numNodeIds
condition|;
name|i
operator|++
control|)
block|{
name|String
name|nodeId
init|=
name|randomFrom
argument_list|(
name|nodeIds
argument_list|)
decl_stmt|;
name|nodeSelectors
operator|.
name|add
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|finalNodesIds
init|=
name|nodeSelectors
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|nodeSelectors
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|TestNodesRequest
name|request
init|=
operator|new
name|TestNodesRequest
argument_list|(
name|finalNodesIds
argument_list|)
decl_stmt|;
name|action
operator|.
expr|new
name|AsyncAction
argument_list|(
literal|null
argument_list|,
name|request
argument_list|,
operator|new
name|PlainActionFuture
argument_list|<>
argument_list|()
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|CapturingTransport
operator|.
name|CapturedRequest
argument_list|>
argument_list|>
name|capturedRequests
init|=
name|transport
operator|.
name|getCapturedRequestsByTargetNodeAndClear
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|resolveNodes
argument_list|(
name|finalNodesIds
argument_list|)
operator|.
name|length
argument_list|,
name|capturedRequests
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testNewResponseNullArray
specifier|public
name|void
name|testNewResponseNullArray
parameter_list|()
block|{
name|TransportNodesAction
name|action
init|=
name|getTestTransportNodesAction
argument_list|()
decl_stmt|;
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|action
operator|.
name|newResponse
argument_list|(
operator|new
name|TestNodesRequest
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNewResponse
specifier|public
name|void
name|testNewResponse
parameter_list|()
block|{
name|TestTransportNodesAction
name|action
init|=
name|getTestTransportNodesAction
argument_list|()
decl_stmt|;
name|TestNodesRequest
name|request
init|=
operator|new
name|TestNodesRequest
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TestNodeResponse
argument_list|>
name|expectedNodeResponses
init|=
name|mockList
argument_list|(
name|TestNodeResponse
operator|.
name|class
argument_list|,
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|expectedNodeResponses
operator|.
name|add
argument_list|(
operator|new
name|TestNodeResponse
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|BaseNodeResponse
argument_list|>
name|nodeResponses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|expectedNodeResponses
argument_list|)
decl_stmt|;
comment|// This should be ignored:
name|nodeResponses
operator|.
name|add
argument_list|(
operator|new
name|OtherNodeResponse
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failures
init|=
name|mockList
argument_list|(
name|FailedNodeException
operator|.
name|class
argument_list|,
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|allResponses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|expectedNodeResponses
argument_list|)
decl_stmt|;
name|allResponses
operator|.
name|addAll
argument_list|(
name|failures
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|allResponses
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
name|AtomicReferenceArray
argument_list|<
name|?
argument_list|>
name|atomicArray
init|=
operator|new
name|AtomicReferenceArray
argument_list|<>
argument_list|(
name|allResponses
operator|.
name|toArray
argument_list|()
argument_list|)
decl_stmt|;
name|TestNodesResponse
name|response
init|=
name|action
operator|.
name|newResponse
argument_list|(
name|request
argument_list|,
name|atomicArray
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|request
argument_list|,
name|response
operator|.
name|request
argument_list|)
expr_stmt|;
comment|// note: I shuffled the overall list, so it's not possible to guarantee that it's in the right order
name|assertTrue
argument_list|(
name|expectedNodeResponses
operator|.
name|containsAll
argument_list|(
name|response
operator|.
name|getNodes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|failures
operator|.
name|containsAll
argument_list|(
name|response
operator|.
name|failures
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCustomResolving
specifier|public
name|void
name|testCustomResolving
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportNodesAction
name|action
init|=
name|getDataNodesOnlyTransportNodesAction
argument_list|(
name|transportService
argument_list|)
decl_stmt|;
name|TestNodesRequest
name|request
init|=
operator|new
name|TestNodesRequest
argument_list|(
name|randomBoolean
argument_list|()
condition|?
literal|null
else|:
name|generateRandomStringArray
argument_list|(
literal|10
argument_list|,
literal|5
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|PlainActionFuture
argument_list|<
name|TestNodesResponse
argument_list|>
name|listener
init|=
operator|new
name|PlainActionFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|action
operator|.
expr|new
name|AsyncAction
argument_list|(
literal|null
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|CapturingTransport
operator|.
name|CapturedRequest
argument_list|>
argument_list|>
name|capturedRequests
init|=
name|transport
operator|.
name|getCapturedRequestsByTargetNodeAndClear
argument_list|()
decl_stmt|;
comment|// check requests were only sent to data nodes
for|for
control|(
name|String
name|nodeTarget
range|:
name|capturedRequests
operator|.
name|keySet
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|nodeTarget
argument_list|)
operator|.
name|isDataNode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|capturedRequests
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|mockList
specifier|private
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|mockList
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|List
argument_list|<
name|T
argument_list|>
name|failures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
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
name|size
condition|;
operator|++
name|i
control|)
block|{
name|failures
operator|.
name|add
argument_list|(
name|mock
argument_list|(
name|clazz
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|failures
return|;
block|}
DECL|enum|NodeSelector
specifier|private
enum|enum
name|NodeSelector
block|{
DECL|enum constant|LOCAL
DECL|enum constant|ELECTED_MASTER
DECL|enum constant|MASTER_ELIGIBLE
DECL|enum constant|DATA
DECL|enum constant|CUSTOM_ATTRIBUTE
name|LOCAL
argument_list|(
literal|"_local"
argument_list|)
block|,
name|ELECTED_MASTER
argument_list|(
literal|"_master"
argument_list|)
block|,
name|MASTER_ELIGIBLE
argument_list|(
literal|"master:true"
argument_list|)
block|,
name|DATA
argument_list|(
literal|"data:true"
argument_list|)
block|,
name|CUSTOM_ATTRIBUTE
argument_list|(
literal|"attr:value"
argument_list|)
block|;
DECL|field|selector
specifier|private
specifier|final
name|String
name|selector
decl_stmt|;
DECL|method|NodeSelector
name|NodeSelector
parameter_list|(
name|String
name|selector
parameter_list|)
block|{
name|this
operator|.
name|selector
operator|=
name|selector
expr_stmt|;
block|}
block|}
annotation|@
name|BeforeClass
DECL|method|startThreadPool
specifier|public
specifier|static
name|void
name|startThreadPool
parameter_list|()
block|{
name|THREAD_POOL
operator|=
operator|new
name|TestThreadPool
argument_list|(
name|TransportBroadcastByNodeActionTests
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|destroyThreadPool
specifier|public
specifier|static
name|void
name|destroyThreadPool
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
comment|// since static must set to null to be eligible for collection
name|THREAD_POOL
operator|=
literal|null
expr_stmt|;
block|}
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
name|transport
operator|=
operator|new
name|CapturingTransport
argument_list|()
expr_stmt|;
name|clusterService
operator|=
name|createClusterService
argument_list|(
name|THREAD_POOL
argument_list|)
expr_stmt|;
name|transportService
operator|=
operator|new
name|TransportService
argument_list|(
name|clusterService
operator|.
name|getSettings
argument_list|()
argument_list|,
name|transport
argument_list|,
name|THREAD_POOL
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
name|int
name|numNodes
init|=
name|randomIntBetween
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|DiscoveryNodes
operator|.
name|Builder
name|discoBuilder
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoveryNodes
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
literal|0
init|;
name|i
operator|<
name|numNodes
condition|;
name|i
operator|++
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
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
if|if
condition|(
name|frequently
argument_list|()
condition|)
block|{
name|attributes
operator|.
name|put
argument_list|(
literal|"custom"
argument_list|,
name|randomBoolean
argument_list|()
condition|?
literal|"match"
else|:
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|DiscoveryNode
name|node
init|=
name|newNode
argument_list|(
name|i
argument_list|,
name|attributes
argument_list|,
name|roles
argument_list|)
decl_stmt|;
name|discoBuilder
operator|=
name|discoBuilder
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|discoveryNodes
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
name|discoBuilder
operator|.
name|localNodeId
argument_list|(
name|randomFrom
argument_list|(
name|discoveryNodes
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|discoBuilder
operator|.
name|masterNodeId
argument_list|(
name|randomFrom
argument_list|(
name|discoveryNodes
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|ClusterState
operator|.
name|Builder
name|stateBuilder
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|getClusterName
argument_list|()
argument_list|)
decl_stmt|;
name|stateBuilder
operator|.
name|nodes
argument_list|(
name|discoBuilder
argument_list|)
expr_stmt|;
name|ClusterState
name|clusterState
init|=
name|stateBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
block|}
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
name|clusterService
operator|.
name|close
argument_list|()
expr_stmt|;
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|getTestTransportNodesAction
specifier|public
name|TestTransportNodesAction
name|getTestTransportNodesAction
parameter_list|()
block|{
return|return
operator|new
name|TestTransportNodesAction
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|THREAD_POOL
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
operator|new
name|ActionFilters
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|)
argument_list|,
name|TestNodesRequest
operator|::
operator|new
argument_list|,
name|TestNodeRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|)
return|;
block|}
DECL|method|getDataNodesOnlyTransportNodesAction
specifier|public
name|DataNodesOnlyTransportNodesAction
name|getDataNodesOnlyTransportNodesAction
parameter_list|(
name|TransportService
name|transportService
parameter_list|)
block|{
return|return
operator|new
name|DataNodesOnlyTransportNodesAction
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|THREAD_POOL
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
operator|new
name|ActionFilters
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|)
argument_list|,
name|TestNodesRequest
operator|::
operator|new
argument_list|,
name|TestNodeRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|)
return|;
block|}
DECL|method|newNode
specifier|private
specifier|static
name|DiscoveryNode
name|newNode
parameter_list|(
name|int
name|nodeId
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
parameter_list|,
name|Set
argument_list|<
name|DiscoveryNode
operator|.
name|Role
argument_list|>
name|roles
parameter_list|)
block|{
name|String
name|node
init|=
literal|"node_"
operator|+
name|nodeId
decl_stmt|;
return|return
operator|new
name|DiscoveryNode
argument_list|(
name|node
argument_list|,
name|node
argument_list|,
name|LocalTransportAddress
operator|.
name|buildUnique
argument_list|()
argument_list|,
name|attributes
argument_list|,
name|roles
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
DECL|class|TestTransportNodesAction
specifier|private
specifier|static
class|class
name|TestTransportNodesAction
extends|extends
name|TransportNodesAction
argument_list|<
name|TestNodesRequest
argument_list|,
name|TestNodesResponse
argument_list|,
name|TestNodeRequest
argument_list|,
name|TestNodeResponse
argument_list|>
block|{
DECL|method|TestTransportNodesAction
name|TestTransportNodesAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|Supplier
argument_list|<
name|TestNodesRequest
argument_list|>
name|request
parameter_list|,
name|Supplier
argument_list|<
name|TestNodeRequest
argument_list|>
name|nodeRequest
parameter_list|,
name|String
name|nodeExecutor
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
literal|"indices:admin/test"
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
literal|null
argument_list|,
name|request
argument_list|,
name|nodeRequest
argument_list|,
name|nodeExecutor
argument_list|,
name|TestNodeResponse
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|TestNodesResponse
name|newResponse
parameter_list|(
name|TestNodesRequest
name|request
parameter_list|,
name|List
argument_list|<
name|TestNodeResponse
argument_list|>
name|responses
parameter_list|,
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failures
parameter_list|)
block|{
return|return
operator|new
name|TestNodesResponse
argument_list|(
name|clusterService
operator|.
name|getClusterName
argument_list|()
argument_list|,
name|request
argument_list|,
name|responses
argument_list|,
name|failures
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newNodeRequest
specifier|protected
name|TestNodeRequest
name|newNodeRequest
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|TestNodesRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|TestNodeRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newNodeResponse
specifier|protected
name|TestNodeResponse
name|newNodeResponse
parameter_list|()
block|{
return|return
operator|new
name|TestNodeResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|nodeOperation
specifier|protected
name|TestNodeResponse
name|nodeOperation
parameter_list|(
name|TestNodeRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|TestNodeResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|accumulateExceptions
specifier|protected
name|boolean
name|accumulateExceptions
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
DECL|class|DataNodesOnlyTransportNodesAction
specifier|private
specifier|static
class|class
name|DataNodesOnlyTransportNodesAction
extends|extends
name|TestTransportNodesAction
block|{
DECL|method|DataNodesOnlyTransportNodesAction
name|DataNodesOnlyTransportNodesAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|Supplier
argument_list|<
name|TestNodesRequest
argument_list|>
name|request
parameter_list|,
name|Supplier
argument_list|<
name|TestNodeRequest
argument_list|>
name|nodeRequest
parameter_list|,
name|String
name|nodeExecutor
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|request
argument_list|,
name|nodeRequest
argument_list|,
name|nodeExecutor
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|resolveRequest
specifier|protected
name|void
name|resolveRequest
parameter_list|(
name|TestNodesRequest
name|request
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|request
operator|.
name|setConcreteNodes
argument_list|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
name|DiscoveryNode
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TestNodesRequest
specifier|private
specifier|static
class|class
name|TestNodesRequest
extends|extends
name|BaseNodesRequest
argument_list|<
name|TestNodesRequest
argument_list|>
block|{
DECL|method|TestNodesRequest
name|TestNodesRequest
parameter_list|(
name|String
modifier|...
name|nodesIds
parameter_list|)
block|{
name|super
argument_list|(
name|nodesIds
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TestNodesResponse
specifier|private
specifier|static
class|class
name|TestNodesResponse
extends|extends
name|BaseNodesResponse
argument_list|<
name|TestNodeResponse
argument_list|>
block|{
DECL|field|request
specifier|private
specifier|final
name|TestNodesRequest
name|request
decl_stmt|;
DECL|method|TestNodesResponse
name|TestNodesResponse
parameter_list|(
name|ClusterName
name|clusterName
parameter_list|,
name|TestNodesRequest
name|request
parameter_list|,
name|List
argument_list|<
name|TestNodeResponse
argument_list|>
name|nodeResponses
parameter_list|,
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failures
parameter_list|)
block|{
name|super
argument_list|(
name|clusterName
argument_list|,
name|nodeResponses
argument_list|,
name|failures
argument_list|)
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readNodesFrom
specifier|protected
name|List
argument_list|<
name|TestNodeResponse
argument_list|>
name|readNodesFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|readStreamableList
argument_list|(
name|TestNodeResponse
operator|::
operator|new
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeNodesTo
specifier|protected
name|void
name|writeNodesTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|,
name|List
argument_list|<
name|TestNodeResponse
argument_list|>
name|nodes
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeStreamableList
argument_list|(
name|nodes
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TestNodeRequest
specifier|private
specifier|static
class|class
name|TestNodeRequest
extends|extends
name|BaseNodeRequest
block|{ }
DECL|class|TestNodeResponse
specifier|private
specifier|static
class|class
name|TestNodeResponse
extends|extends
name|BaseNodeResponse
block|{ }
DECL|class|OtherNodeResponse
specifier|private
specifier|static
class|class
name|OtherNodeResponse
extends|extends
name|BaseNodeResponse
block|{ }
block|}
end_class

end_unit

