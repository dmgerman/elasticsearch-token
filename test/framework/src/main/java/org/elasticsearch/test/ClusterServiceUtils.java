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
name|HashSet
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
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
name|clusterService
operator|.
name|setLocalNode
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"node"
argument_list|,
name|LocalTransportAddress
operator|.
name|buildUnique
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
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
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
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
name|connectToAddedNodes
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
comment|// skip
block|}
annotation|@
name|Override
specifier|public
name|void
name|disconnectFromRemovedNodes
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
comment|// skip
block|}
block|}
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|setClusterStatePublisher
argument_list|(
parameter_list|(
name|event
parameter_list|,
name|ackListener
parameter_list|)
lambda|->
block|{         }
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|start
argument_list|()
expr_stmt|;
specifier|final
name|DiscoveryNodes
operator|.
name|Builder
name|nodes
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
argument_list|)
decl_stmt|;
name|nodes
operator|.
name|masterNodeId
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|nodes
argument_list|(
name|nodes
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|clusterService
return|;
block|}
DECL|method|createClusterService
specifier|public
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
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|clusterService
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
name|version
argument_list|(
name|currentState
operator|.
name|version
argument_list|()
operator|+
literal|1
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
name|runOnlyOnMaster
parameter_list|()
block|{
return|return
literal|false
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
block|}
end_class

end_unit

