begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.health
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|health
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
name|admin
operator|.
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthRequest
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
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|health
operator|.
name|TransportClusterHealthAction
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
name|IndicesOptions
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
name|metadata
operator|.
name|IndexMetaData
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
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|metadata
operator|.
name|MetaData
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
name|IndexRoutingTable
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
name|RoutingTable
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
name|RoutingTableGenerator
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
name|BytesStreamOutput
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
name|gateway
operator|.
name|NoopGatewayAllocator
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|allOf
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThanOrEqualTo
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
name|is
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
name|lessThanOrEqualTo
import|;
end_import

begin_class
DECL|class|ClusterStateHealthTests
specifier|public
class|class
name|ClusterStateHealthTests
extends|extends
name|ESTestCase
block|{
DECL|field|indexNameExpressionResolver
specifier|private
specifier|final
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
init|=
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|static
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|clusterService
specifier|private
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|transportService
specifier|private
name|TransportService
name|transportService
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|beforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
block|{
name|threadPool
operator|=
operator|new
name|TestThreadPool
argument_list|(
literal|"ClusterStateHealthTests"
argument_list|)
expr_stmt|;
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
name|clusterService
operator|=
name|createClusterService
argument_list|(
name|threadPool
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
operator|new
name|CapturingTransport
argument_list|()
argument_list|,
name|threadPool
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
name|transportService
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|afterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
block|{
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|threadPool
argument_list|,
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|threadPool
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testClusterHealthWaitsForClusterStateApplication
specifier|public
name|void
name|testClusterHealthWaitsForClusterStateApplication
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
specifier|final
name|CountDownLatch
name|applyLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|listenerCalled
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|clusterService
operator|.
name|add
argument_list|(
name|event
lambda|->
block|{
name|listenerCalled
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|applyLatch
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
name|logger
operator|.
name|debug
argument_list|(
literal|"interrupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"test"
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
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
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
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"unexpected failure"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for listener to be called and cluster state being blocked"
argument_list|)
expr_stmt|;
name|listenerCalled
operator|.
name|await
argument_list|()
expr_stmt|;
name|TransportClusterHealthAction
name|action
init|=
operator|new
name|TransportClusterHealthAction
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|,
operator|new
name|ActionFilters
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|)
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|NoopGatewayAllocator
operator|.
name|INSTANCE
argument_list|)
decl_stmt|;
name|PlainActionFuture
argument_list|<
name|ClusterHealthResponse
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
name|execute
argument_list|(
operator|new
name|ClusterHealthRequest
argument_list|()
argument_list|,
name|listener
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|listener
operator|.
name|isDone
argument_list|()
argument_list|)
expr_stmt|;
name|applyLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|listener
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
DECL|method|testClusterHealth
specifier|public
name|void
name|testClusterHealth
parameter_list|()
throws|throws
name|IOException
block|{
name|RoutingTableGenerator
name|routingTableGenerator
init|=
operator|new
name|RoutingTableGenerator
argument_list|()
decl_stmt|;
name|RoutingTableGenerator
operator|.
name|ShardCounter
name|counter
init|=
operator|new
name|RoutingTableGenerator
operator|.
name|ShardCounter
argument_list|()
decl_stmt|;
name|RoutingTable
operator|.
name|Builder
name|routingTable
init|=
name|RoutingTable
operator|.
name|builder
argument_list|()
decl_stmt|;
name|MetaData
operator|.
name|Builder
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|randomInt
argument_list|(
literal|4
argument_list|)
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|int
name|numberOfShards
init|=
name|randomInt
argument_list|(
literal|3
argument_list|)
operator|+
literal|1
decl_stmt|;
name|int
name|numberOfReplicas
init|=
name|randomInt
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test_"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
name|numberOfShards
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
name|numberOfReplicas
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|routingTableGenerator
operator|.
name|genIndexRoutingTable
argument_list|(
name|indexMetaData
argument_list|,
name|counter
argument_list|)
decl_stmt|;
name|metaData
operator|.
name|put
argument_list|(
name|indexMetaData
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|routingTable
operator|.
name|add
argument_list|(
name|indexRoutingTable
argument_list|)
expr_stmt|;
block|}
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|ClusterName
operator|.
name|CLUSTER_NAME_SETTING
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
name|indexNameExpressionResolver
operator|.
name|concreteIndexNames
argument_list|(
name|clusterState
argument_list|,
name|IndicesOptions
operator|.
name|strictExpand
argument_list|()
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
decl_stmt|;
name|ClusterStateHealth
name|clusterStateHealth
init|=
operator|new
name|ClusterStateHealth
argument_list|(
name|clusterState
argument_list|,
name|concreteIndices
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"cluster status: {}, expected {}"
argument_list|,
name|clusterStateHealth
operator|.
name|getStatus
argument_list|()
argument_list|,
name|counter
operator|.
name|status
argument_list|()
argument_list|)
expr_stmt|;
name|clusterStateHealth
operator|=
name|maybeSerialize
argument_list|(
name|clusterStateHealth
argument_list|)
expr_stmt|;
name|assertClusterHealth
argument_list|(
name|clusterStateHealth
argument_list|,
name|counter
argument_list|)
expr_stmt|;
block|}
DECL|method|maybeSerialize
name|ClusterStateHealth
name|maybeSerialize
parameter_list|(
name|ClusterStateHealth
name|clusterStateHealth
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|clusterStateHealth
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|StreamInput
name|in
init|=
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
decl_stmt|;
name|clusterStateHealth
operator|=
operator|new
name|ClusterStateHealth
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
return|return
name|clusterStateHealth
return|;
block|}
DECL|method|assertClusterHealth
specifier|private
name|void
name|assertClusterHealth
parameter_list|(
name|ClusterStateHealth
name|clusterStateHealth
parameter_list|,
name|RoutingTableGenerator
operator|.
name|ShardCounter
name|counter
parameter_list|)
block|{
name|assertThat
argument_list|(
name|clusterStateHealth
operator|.
name|getStatus
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|.
name|status
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateHealth
operator|.
name|getActiveShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|.
name|active
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateHealth
operator|.
name|getActivePrimaryShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|.
name|primaryActive
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateHealth
operator|.
name|getInitializingShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|.
name|initializing
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateHealth
operator|.
name|getRelocatingShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|.
name|relocating
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateHealth
operator|.
name|getUnassignedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|.
name|unassigned
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateHealth
operator|.
name|getActiveShardsPercent
argument_list|()
argument_list|,
name|is
argument_list|(
name|allOf
argument_list|(
name|greaterThanOrEqualTo
argument_list|(
literal|0.0
argument_list|)
argument_list|,
name|lessThanOrEqualTo
argument_list|(
literal|100.0
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

