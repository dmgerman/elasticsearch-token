begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|util
operator|.
name|Throwables
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|ClusterChangedEvent
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
name|ClusterStateTaskListener
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
name|ClusterStateUpdateTask
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
name|NodeConnectionsService
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
name|block
operator|.
name|ClusterBlocks
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
name|ClusterApplier
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
name|ClusterApplierService
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
name|cluster
operator|.
name|service
operator|.
name|MasterService
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
name|ClusterSettings
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
name|Discovery
operator|.
name|AckListener
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
name|EnumSet
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
name|BiConsumer
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|fail
import|;
end_import

begin_class
DECL|class|ClusterServiceUtils
specifier|public
class|class
name|ClusterServiceUtils
block|{
DECL|method|createMasterService
specifier|public
specifier|static
name|MasterService
name|createMasterService
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterState
name|initialClusterState
parameter_list|)
block|{
name|MasterService
name|masterService
init|=
operator|new
name|MasterService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|ClusterState
argument_list|>
name|clusterStateRef
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
name|initialClusterState
argument_list|)
decl_stmt|;
name|masterService
operator|.
name|setClusterStatePublisher
argument_list|(
parameter_list|(
name|event
parameter_list|,
name|ackListener
parameter_list|)
lambda|->
name|clusterStateRef
operator|.
name|set
argument_list|(
name|event
operator|.
name|state
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|masterService
operator|.
name|setClusterStateSupplier
argument_list|(
name|clusterStateRef
operator|::
name|get
argument_list|)
expr_stmt|;
name|masterService
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|masterService
return|;
block|}
DECL|method|createMasterService
specifier|public
specifier|static
name|MasterService
name|createMasterService
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|,
name|DiscoveryNode
name|localNode
parameter_list|)
block|{
name|ClusterState
name|initialClusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
name|ClusterServiceUtils
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|add
argument_list|(
name|localNode
argument_list|)
operator|.
name|localNodeId
argument_list|(
name|localNode
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|masterNodeId
argument_list|(
name|localNode
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|blocks
argument_list|(
name|ClusterBlocks
operator|.
name|EMPTY_CLUSTER_BLOCK
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|createMasterService
argument_list|(
name|threadPool
argument_list|,
name|initialClusterState
argument_list|)
return|;
block|}
DECL|method|setState
specifier|public
specifier|static
name|void
name|setState
parameter_list|(
name|ClusterApplierService
name|executor
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|exception
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|executor
operator|.
name|onNewClusterState
argument_list|(
literal|"test setting state"
argument_list|,
parameter_list|()
lambda|->
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|version
argument_list|(
name|clusterState
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|ClusterStateTaskListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|String
name|source
parameter_list|,
name|ClusterState
name|oldState
parameter_list|,
name|ClusterState
name|newState
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
expr|@
name|Override
specifier|public
name|void
name|onFailure
argument_list|(
name|String
name|source
argument_list|,
name|Exception
name|e
argument_list|)
block|{
name|exception
operator|.
name|set
argument_list|(
name|e
argument_list|)
block|;
name|latch
operator|.
name|countDown
argument_list|()
block|;                 }
block|}
block|)
class|;
end_class

begin_try
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
if|if
condition|(
name|exception
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Throwables
operator|.
name|rethrow
argument_list|(
name|exception
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"unexpected exception"
argument_list|,
name|e
argument_list|)
throw|;
block|}
end_try

begin_function
unit|}      public
DECL|method|setState
specifier|static
name|void
name|setState
parameter_list|(
name|MasterService
name|executor
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|executor
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"test setting state"
argument_list|,
operator|new
name|ClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
throws|throws
name|Exception
block|{
comment|// make sure we increment versions as listener may depend on it for change
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|String
name|source
parameter_list|,
name|ClusterState
name|oldState
parameter_list|,
name|ClusterState
name|newState
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
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"unexpected exception"
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"unexpected interruption"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
end_function

begin_function
DECL|method|createClusterService
specifier|public
specifier|static
name|ClusterService
name|createClusterService
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|DiscoveryNode
name|discoveryNode
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node"
argument_list|,
name|ESTestCase
operator|.
name|buildNewFakeTransportAddress
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|EnumSet
operator|.
name|allOf
argument_list|(
name|DiscoveryNode
operator|.
name|Role
operator|.
name|class
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
return|return
name|createClusterService
argument_list|(
name|threadPool
argument_list|,
name|discoveryNode
argument_list|)
return|;
block|}
end_function

begin_function
DECL|method|createClusterService
specifier|public
specifier|static
name|ClusterService
name|createClusterService
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|,
name|DiscoveryNode
name|localNode
parameter_list|)
block|{
return|return
name|createClusterService
argument_list|(
name|threadPool
argument_list|,
name|localNode
argument_list|,
operator|new
name|ClusterSettings
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
argument_list|)
return|;
block|}
end_function

begin_function
DECL|method|createClusterService
specifier|public
specifier|static
name|ClusterService
name|createClusterService
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|,
name|DiscoveryNode
name|localNode
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|)
block|{
name|ClusterService
name|clusterService
init|=
operator|new
name|ClusterService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.name"
argument_list|,
literal|"ClusterServiceTests"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|clusterSettings
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
name|clusterService
operator|.
name|setNodeConnectionsService
argument_list|(
operator|new
name|NodeConnectionsService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|connectToNodes
parameter_list|(
name|DiscoveryNodes
name|discoveryNodes
parameter_list|)
block|{
comment|// skip
block|}
annotation|@
name|Override
specifier|public
name|void
name|disconnectFromNodesExcept
parameter_list|(
name|DiscoveryNodes
name|nodesToKeep
parameter_list|)
block|{
comment|// skip
block|}
block|}
argument_list|)
expr_stmt|;
name|ClusterState
name|initialClusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
name|ClusterServiceUtils
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|add
argument_list|(
name|localNode
argument_list|)
operator|.
name|localNodeId
argument_list|(
name|localNode
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|masterNodeId
argument_list|(
name|localNode
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|blocks
argument_list|(
name|ClusterBlocks
operator|.
name|EMPTY_CLUSTER_BLOCK
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|clusterService
operator|.
name|getClusterApplierService
argument_list|()
operator|.
name|setInitialState
argument_list|(
name|initialClusterState
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|getMasterService
argument_list|()
operator|.
name|setClusterStatePublisher
argument_list|(
name|createClusterStatePublisher
argument_list|(
name|clusterService
operator|.
name|getClusterApplierService
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|getMasterService
argument_list|()
operator|.
name|setClusterStateSupplier
argument_list|(
name|clusterService
operator|.
name|getClusterApplierService
argument_list|()
operator|::
name|state
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|clusterService
return|;
block|}
end_function

begin_function
DECL|method|createClusterStatePublisher
specifier|public
specifier|static
name|BiConsumer
argument_list|<
name|ClusterChangedEvent
argument_list|,
name|AckListener
argument_list|>
name|createClusterStatePublisher
parameter_list|(
name|ClusterApplier
name|clusterApplier
parameter_list|)
block|{
return|return
parameter_list|(
name|event
parameter_list|,
name|ackListener
parameter_list|)
lambda|->
block|{
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|ex
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|clusterApplier
operator|.
name|onNewClusterState
argument_list|(
literal|"mock_publish_to_self["
operator|+
name|event
operator|.
name|source
argument_list|()
operator|+
literal|"]"
argument_list|,
parameter_list|()
lambda|->
name|event
operator|.
name|state
argument_list|()
argument_list|,
operator|new
name|ClusterStateTaskListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|String
name|source
parameter_list|,
name|ClusterState
name|oldState
parameter_list|,
name|ClusterState
name|newState
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
expr|@
name|Override
specifier|public
name|void
name|onFailure
argument_list|(
name|String
name|source
argument_list|,
name|Exception
name|e
argument_list|)
block|{
name|ex
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|)
function|;
end_function

begin_try
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Throwables
operator|.
name|rethrow
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
end_try

begin_if
if|if
condition|(
name|ex
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Throwables
operator|.
name|rethrow
argument_list|(
name|ex
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_if

begin_function
unit|};     }      public
DECL|method|createClusterService
specifier|static
name|ClusterService
name|createClusterService
parameter_list|(
name|ClusterState
name|initialState
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|ClusterService
name|clusterService
init|=
name|createClusterService
argument_list|(
name|threadPool
argument_list|)
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|initialState
argument_list|)
expr_stmt|;
return|return
name|clusterService
return|;
block|}
end_function

begin_function
DECL|method|setState
specifier|public
specifier|static
name|void
name|setState
parameter_list|(
name|ClusterService
name|clusterService
parameter_list|,
name|ClusterState
operator|.
name|Builder
name|clusterStateBuilder
parameter_list|)
block|{
name|setState
argument_list|(
name|clusterService
argument_list|,
name|clusterStateBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_function

begin_comment
comment|/**      * Sets the state on the cluster applier service      */
end_comment

begin_function
DECL|method|setState
specifier|public
specifier|static
name|void
name|setState
parameter_list|(
name|ClusterService
name|clusterService
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|setState
argument_list|(
name|clusterService
operator|.
name|getClusterApplierService
argument_list|()
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
block|}
end_function

unit|}
end_unit

