begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.zen
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
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
name|ClusterStateTaskExecutor
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
name|routing
operator|.
name|allocation
operator|.
name|AllocationService
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
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicReference
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
name|Consumer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|StreamSupport
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|eq
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

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
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
name|verifyNoMoreInteractions
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
name|when
import|;
end_import

begin_class
DECL|class|NodeRemovalClusterStateTaskExecutorTests
specifier|public
class|class
name|NodeRemovalClusterStateTaskExecutorTests
extends|extends
name|ESTestCase
block|{
DECL|method|testRemovingNonExistentNodes
specifier|public
name|void
name|testRemovingNonExistentNodes
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
name|executor
init|=
operator|new
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|logger
argument_list|)
decl_stmt|;
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
specifier|final
name|int
name|nodes
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|16
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
name|nodes
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|node
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
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
name|builder
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|DiscoveryNodes
operator|.
name|Builder
name|removeBuilder
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|nodes
init|;
name|i
operator|<
name|nodes
operator|+
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|16
argument_list|)
condition|;
name|i
operator|++
control|)
block|{
name|removeBuilder
operator|.
name|add
argument_list|(
name|node
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|List
argument_list|<
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
argument_list|>
name|tasks
init|=
name|StreamSupport
operator|.
name|stream
argument_list|(
name|removeBuilder
operator|.
name|build
argument_list|()
operator|.
name|spliterator
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|map
argument_list|(
name|node
lambda|->
operator|new
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
argument_list|(
name|node
argument_list|,
name|randomBoolean
argument_list|()
condition|?
literal|"left"
else|:
literal|"failed"
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|ClusterStateTaskExecutor
operator|.
name|ClusterTasksResult
argument_list|<
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
argument_list|>
name|result
init|=
name|executor
operator|.
name|execute
argument_list|(
name|clusterState
argument_list|,
name|tasks
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|resultingState
argument_list|,
name|equalTo
argument_list|(
name|clusterState
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNotEnoughMasterNodesAfterRemove
specifier|public
name|void
name|testNotEnoughMasterNodesAfterRemove
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ElectMasterService
name|electMasterService
init|=
name|mock
argument_list|(
name|ElectMasterService
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|electMasterService
operator|.
name|hasEnoughMasterNodes
argument_list|(
name|any
argument_list|(
name|Iterable
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
specifier|final
name|AllocationService
name|allocationService
init|=
name|mock
argument_list|(
name|AllocationService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|AtomicBoolean
name|rejoinCalled
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
specifier|final
name|Consumer
argument_list|<
name|String
argument_list|>
name|submitRejoin
init|=
name|source
lambda|->
name|rejoinCalled
operator|.
name|set
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|ClusterState
argument_list|>
name|remainingNodesClusterState
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
name|executor
init|=
operator|new
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
argument_list|(
name|allocationService
argument_list|,
name|electMasterService
argument_list|,
name|submitRejoin
argument_list|,
name|logger
argument_list|)
block|{
annotation|@
name|Override
name|ClusterState
name|remainingNodesClusterState
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|DiscoveryNodes
operator|.
name|Builder
name|remainingNodesBuilder
parameter_list|)
block|{
name|remainingNodesClusterState
operator|.
name|set
argument_list|(
name|super
operator|.
name|remainingNodesClusterState
argument_list|(
name|currentState
argument_list|,
name|remainingNodesBuilder
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|remainingNodesClusterState
operator|.
name|get
argument_list|()
return|;
block|}
block|}
decl_stmt|;
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
specifier|final
name|int
name|nodes
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|16
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// to ensure there is at least one removal
name|boolean
name|first
init|=
literal|true
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
name|nodes
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|DiscoveryNode
name|node
init|=
name|node
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
if|if
condition|(
name|first
operator|||
name|randomBoolean
argument_list|()
condition|)
block|{
name|tasks
operator|.
name|add
argument_list|(
operator|new
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
argument_list|(
name|node
argument_list|,
name|randomBoolean
argument_list|()
condition|?
literal|"left"
else|:
literal|"failed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
block|}
specifier|final
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
name|builder
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|ClusterStateTaskExecutor
operator|.
name|ClusterTasksResult
argument_list|<
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
argument_list|>
name|result
init|=
name|executor
operator|.
name|execute
argument_list|(
name|clusterState
argument_list|,
name|tasks
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|electMasterService
argument_list|)
operator|.
name|hasEnoughMasterNodes
argument_list|(
name|eq
argument_list|(
name|remainingNodesClusterState
operator|.
name|get
argument_list|()
operator|.
name|nodes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|verifyNoMoreInteractions
argument_list|(
name|electMasterService
argument_list|)
expr_stmt|;
comment|// ensure that we did not reroute
name|verifyNoMoreInteractions
argument_list|(
name|allocationService
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rejoinCalled
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|resultingState
argument_list|,
name|equalTo
argument_list|(
name|clusterState
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
specifier|final
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
name|task
range|:
name|tasks
control|)
block|{
name|assertNotNull
argument_list|(
name|result
operator|.
name|resultingState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|task
operator|.
name|node
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRerouteAfterRemovingNodes
specifier|public
name|void
name|testRerouteAfterRemovingNodes
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ElectMasterService
name|electMasterService
init|=
name|mock
argument_list|(
name|ElectMasterService
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|electMasterService
operator|.
name|hasEnoughMasterNodes
argument_list|(
name|any
argument_list|(
name|Iterable
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|AllocationService
name|allocationService
init|=
name|mock
argument_list|(
name|AllocationService
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|allocationService
operator|.
name|deassociateDeadNodes
argument_list|(
name|any
argument_list|(
name|ClusterState
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|true
argument_list|)
argument_list|,
name|any
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
name|im
lambda|->
name|im
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
specifier|final
name|Consumer
argument_list|<
name|String
argument_list|>
name|submitRejoin
init|=
name|source
lambda|->
name|fail
argument_list|(
literal|"rejoin should not be invoked"
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|ClusterState
argument_list|>
name|remainingNodesClusterState
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
name|executor
init|=
operator|new
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
argument_list|(
name|allocationService
argument_list|,
name|electMasterService
argument_list|,
name|submitRejoin
argument_list|,
name|logger
argument_list|)
block|{
annotation|@
name|Override
name|ClusterState
name|remainingNodesClusterState
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|DiscoveryNodes
operator|.
name|Builder
name|remainingNodesBuilder
parameter_list|)
block|{
name|remainingNodesClusterState
operator|.
name|set
argument_list|(
name|super
operator|.
name|remainingNodesClusterState
argument_list|(
name|currentState
argument_list|,
name|remainingNodesBuilder
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|remainingNodesClusterState
operator|.
name|get
argument_list|()
return|;
block|}
block|}
decl_stmt|;
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
specifier|final
name|int
name|nodes
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|16
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// to ensure that there is at least one removal
name|boolean
name|first
init|=
literal|true
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
name|nodes
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|DiscoveryNode
name|node
init|=
name|node
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
if|if
condition|(
name|first
operator|||
name|randomBoolean
argument_list|()
condition|)
block|{
name|tasks
operator|.
name|add
argument_list|(
operator|new
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
argument_list|(
name|node
argument_list|,
name|randomBoolean
argument_list|()
condition|?
literal|"left"
else|:
literal|"failed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
block|}
specifier|final
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
name|builder
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|ClusterStateTaskExecutor
operator|.
name|ClusterTasksResult
argument_list|<
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
argument_list|>
name|result
init|=
name|executor
operator|.
name|execute
argument_list|(
name|clusterState
argument_list|,
name|tasks
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|electMasterService
argument_list|)
operator|.
name|hasEnoughMasterNodes
argument_list|(
name|eq
argument_list|(
name|remainingNodesClusterState
operator|.
name|get
argument_list|()
operator|.
name|nodes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|verifyNoMoreInteractions
argument_list|(
name|electMasterService
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|allocationService
argument_list|)
operator|.
name|deassociateDeadNodes
argument_list|(
name|eq
argument_list|(
name|remainingNodesClusterState
operator|.
name|get
argument_list|()
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|true
argument_list|)
argument_list|,
name|any
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
specifier|final
name|ZenDiscovery
operator|.
name|NodeRemovalClusterStateTaskExecutor
operator|.
name|Task
name|task
range|:
name|tasks
control|)
block|{
name|assertNull
argument_list|(
name|result
operator|.
name|resultingState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|task
operator|.
name|node
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|node
specifier|private
name|DiscoveryNode
name|node
parameter_list|(
specifier|final
name|int
name|id
parameter_list|)
block|{
return|return
operator|new
name|DiscoveryNode
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|id
argument_list|)
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
block|}
end_class

end_unit

