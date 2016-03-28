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
name|ExceptionsHelper
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
name|node
operator|.
name|stats
operator|.
name|NodesStatsResponse
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
name|indices
operator|.
name|recovery
operator|.
name|RecoveryResponse
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
name|ClusterStateListener
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
name|bytes
operator|.
name|BytesReference
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
name|LocalTransportAddress
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
name|xcontent
operator|.
name|ToXContent
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
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
name|DiscoveryStats
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
name|membership
operator|.
name|MembershipAction
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
name|publish
operator|.
name|PublishClusterStateAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
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
name|TestCustomMetaData
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
name|BytesTransportRequest
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
name|EmptyTransportResponseHandler
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
name|TransportResponse
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
name|hamcrest
operator|.
name|Matchers
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
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
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
name|atomic
operator|.
name|AtomicReference
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
name|containsString
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
name|not
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
name|notNullValue
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
name|nullValue
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
name|sameInstance
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
literal|0
argument_list|,
name|numClientNodes
operator|=
literal|0
argument_list|)
annotation|@
name|ESIntegTestCase
operator|.
name|SuppressLocalMode
annotation|@
name|TestLogging
argument_list|(
literal|"_root:DEBUG"
argument_list|)
DECL|class|ZenDiscoveryIT
specifier|public
class|class
name|ZenDiscoveryIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testNoShardRelocationsOccurWhenElectedMasterNodeFails
specifier|public
name|void
name|testNoShardRelocationsOccurWhenElectedMasterNodeFails
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|defaultSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|FaultDetection
operator|.
name|PING_TIMEOUT_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"1s"
argument_list|)
operator|.
name|put
argument_list|(
name|FaultDetection
operator|.
name|PING_RETRIES_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|masterNodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|defaultSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
literal|2
argument_list|,
name|masterNodeSettings
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|Settings
name|dateNodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|defaultSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
literal|2
argument_list|,
name|dateNodeSettings
argument_list|)
operator|.
name|get
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
literal|"4"
argument_list|)
operator|.
name|setWaitForRelocatingShards
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clusterHealthResponse
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureSearchable
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|RecoveryResponse
name|r
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRecoveries
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|numRecoveriesBeforeNewMaster
init|=
name|r
operator|.
name|shardRecoveryStates
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|String
name|oldMaster
init|=
name|internalCluster
argument_list|()
operator|.
name|getMasterName
argument_list|()
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|stopCurrentMasterNode
argument_list|()
expr_stmt|;
name|assertBusy
argument_list|(
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
name|String
name|current
init|=
name|internalCluster
argument_list|()
operator|.
name|getMasterName
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|current
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|current
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|oldMaster
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|ensureSearchable
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|r
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRecoveries
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|int
name|numRecoveriesAfterNewMaster
init|=
name|r
operator|.
name|shardRecoveryStates
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|numRecoveriesAfterNewMaster
argument_list|,
name|equalTo
argument_list|(
name|numRecoveriesBeforeNewMaster
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNodeFailuresAreProcessedOnce
specifier|public
name|void
name|testNodeFailuresAreProcessedOnce
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
throws|,
name|IOException
block|{
name|Settings
name|defaultSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|FaultDetection
operator|.
name|PING_TIMEOUT_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"1s"
argument_list|)
operator|.
name|put
argument_list|(
name|FaultDetection
operator|.
name|PING_RETRIES_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|masterNodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|defaultSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
name|master
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|masterNodeSettings
argument_list|)
decl_stmt|;
name|Settings
name|dateNodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|defaultSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
literal|2
argument_list|,
name|dateNodeSettings
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
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
name|setWaitForNodes
argument_list|(
literal|"3"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ClusterService
name|clusterService
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|,
name|master
argument_list|)
decl_stmt|;
specifier|final
name|ArrayList
argument_list|<
name|ClusterState
argument_list|>
name|statesFound
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|nodesStopped
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
operator|new
name|ClusterStateListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|clusterChanged
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
name|statesFound
operator|.
name|add
argument_list|(
name|event
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// block until both nodes have stopped to accumulate node failures
name|nodesStopped
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
comment|//meh
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|stopRandomNonMasterNode
argument_list|()
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|stopRandomNonMasterNode
argument_list|()
expr_stmt|;
name|nodesStopped
operator|.
name|countDown
argument_list|()
expr_stmt|;
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
name|get
argument_list|()
expr_stmt|;
comment|// wait for all to be processed
name|assertThat
argument_list|(
name|statesFound
argument_list|,
name|Matchers
operator|.
name|hasSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNodeRejectsClusterStateWithWrongMasterNode
specifier|public
name|void
name|testNodeRejectsClusterStateWithWrongMasterNode
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|nodeNames
init|=
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
literal|2
argument_list|,
name|settings
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
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
name|setWaitForNodes
argument_list|(
literal|"2"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|nonMasterNodes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|nodeNames
argument_list|)
decl_stmt|;
name|nonMasterNodes
operator|.
name|remove
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|getMasterName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|noneMasterNode
init|=
name|nonMasterNodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ClusterState
name|state
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
name|DiscoveryNode
name|node
init|=
literal|null
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|discoveryNode
range|:
name|state
operator|.
name|nodes
argument_list|()
control|)
block|{
if|if
condition|(
name|discoveryNode
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|noneMasterNode
argument_list|)
condition|)
block|{
name|node
operator|=
name|discoveryNode
expr_stmt|;
block|}
block|}
assert|assert
name|node
operator|!=
literal|null
assert|;
name|DiscoveryNodes
operator|.
name|Builder
name|nodes
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|(
name|state
operator|.
name|nodes
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"abc"
argument_list|,
operator|new
name|LocalTransportAddress
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|masterNodeId
argument_list|(
literal|"abc"
argument_list|)
decl_stmt|;
name|ClusterState
operator|.
name|Builder
name|builder
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|state
argument_list|)
decl_stmt|;
name|builder
operator|.
name|nodes
argument_list|(
name|nodes
argument_list|)
expr_stmt|;
name|BytesReference
name|bytes
init|=
name|PublishClusterStateAction
operator|.
name|serializeFullClusterState
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|node
operator|.
name|version
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
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|reference
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|noneMasterNode
argument_list|)
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|PublishClusterStateAction
operator|.
name|SEND_ACTION_NAME
argument_list|,
operator|new
name|BytesTransportRequest
argument_list|(
name|bytes
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
operator|new
name|EmptyTransportResponseHandler
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|TransportResponse
operator|.
name|Empty
name|response
parameter_list|)
block|{
name|super
operator|.
name|handleResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
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
name|handleException
parameter_list|(
name|TransportException
name|exp
parameter_list|)
block|{
name|super
operator|.
name|handleException
argument_list|(
name|exp
argument_list|)
expr_stmt|;
name|reference
operator|.
name|set
argument_list|(
name|exp
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|reference
operator|.
name|get
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|reference
operator|.
name|get
argument_list|()
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"cluster state from a different master than the current one, rejecting"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHandleNodeJoin_incompatibleClusterState
specifier|public
name|void
name|testHandleNodeJoin_incompatibleClusterState
parameter_list|()
throws|throws
name|UnknownHostException
block|{
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|)
comment|//<-- To override the local setting if set externally
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
name|masterOnlyNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startMasterOnlyNode
argument_list|(
name|nodeSettings
argument_list|)
decl_stmt|;
name|String
name|node1
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|nodeSettings
argument_list|)
decl_stmt|;
name|ZenDiscovery
name|zenDiscovery
init|=
operator|(
name|ZenDiscovery
operator|)
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Discovery
operator|.
name|class
argument_list|,
name|masterOnlyNode
argument_list|)
decl_stmt|;
name|ClusterService
name|clusterService
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|,
name|node1
argument_list|)
decl_stmt|;
specifier|final
name|ClusterState
name|state
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
argument_list|)
decl_stmt|;
name|mdBuilder
operator|.
name|putCustom
argument_list|(
name|CustomMetaData
operator|.
name|TYPE
argument_list|,
operator|new
name|CustomMetaData
argument_list|(
literal|"data"
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterState
name|stateWithCustomMetaData
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|state
argument_list|)
operator|.
name|metaData
argument_list|(
name|mdBuilder
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|IllegalStateException
argument_list|>
name|holder
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|DiscoveryNode
name|node
init|=
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
decl_stmt|;
name|zenDiscovery
operator|.
name|handleJoinRequest
argument_list|(
name|node
argument_list|,
name|stateWithCustomMetaData
argument_list|,
operator|new
name|MembershipAction
operator|.
name|JoinCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|()
block|{             }
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|holder
operator|.
name|set
argument_list|(
operator|(
name|IllegalStateException
operator|)
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|holder
operator|.
name|get
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|holder
operator|.
name|get
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"failure when sending a validation request to node"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|CustomMetaData
specifier|public
specifier|static
class|class
name|CustomMetaData
extends|extends
name|TestCustomMetaData
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"custom_md"
decl_stmt|;
DECL|method|CustomMetaData
name|CustomMetaData
parameter_list|(
name|String
name|data
parameter_list|)
block|{
name|super
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newTestCustomMetaData
specifier|protected
name|TestCustomMetaData
name|newTestCustomMetaData
parameter_list|(
name|String
name|data
parameter_list|)
block|{
return|return
operator|new
name|CustomMetaData
argument_list|(
name|data
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|context
specifier|public
name|EnumSet
argument_list|<
name|MetaData
operator|.
name|XContentContext
argument_list|>
name|context
parameter_list|()
block|{
return|return
name|EnumSet
operator|.
name|of
argument_list|(
name|MetaData
operator|.
name|XContentContext
operator|.
name|GATEWAY
argument_list|,
name|MetaData
operator|.
name|XContentContext
operator|.
name|SNAPSHOT
argument_list|)
return|;
block|}
block|}
DECL|method|testHandleNodeJoin_incompatibleMinVersion
specifier|public
name|void
name|testHandleNodeJoin_incompatibleMinVersion
parameter_list|()
throws|throws
name|UnknownHostException
block|{
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|)
comment|//<-- To override the local setting if set externally
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
name|nodeName
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|nodeSettings
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
decl_stmt|;
name|ZenDiscovery
name|zenDiscovery
init|=
operator|(
name|ZenDiscovery
operator|)
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Discovery
operator|.
name|class
argument_list|,
name|nodeName
argument_list|)
decl_stmt|;
name|ClusterService
name|clusterService
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|,
name|nodeName
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"_node_id"
argument_list|,
operator|new
name|InetSocketTransportAddress
argument_list|(
name|InetAddress
operator|.
name|getByName
argument_list|(
literal|"0.0.0.0"
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|Version
operator|.
name|V_2_0_0
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|IllegalStateException
argument_list|>
name|holder
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|zenDiscovery
operator|.
name|handleJoinRequest
argument_list|(
name|node
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|,
operator|new
name|MembershipAction
operator|.
name|JoinCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|()
block|{             }
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|holder
operator|.
name|set
argument_list|(
operator|(
name|IllegalStateException
operator|)
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|holder
operator|.
name|get
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|holder
operator|.
name|get
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Can't handle join request from a node with a version [2.0.0] that is lower than the minimum compatible version ["
operator|+
name|Version
operator|.
name|V_5_0_0_alpha1
operator|.
name|minimumCompatibilityVersion
argument_list|()
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testJoinElectedMaster_incompatibleMinVersion
specifier|public
name|void
name|testJoinElectedMaster_incompatibleMinVersion
parameter_list|()
block|{
name|ElectMasterService
name|electMasterService
init|=
operator|new
name|ElectMasterService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"_node_id"
argument_list|,
operator|new
name|LocalTransportAddress
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|electMasterService
operator|.
name|electMaster
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|node
argument_list|)
argument_list|)
argument_list|,
name|sameInstance
argument_list|(
name|node
argument_list|)
argument_list|)
expr_stmt|;
name|node
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"_node_id"
argument_list|,
operator|new
name|LocalTransportAddress
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|Version
operator|.
name|V_2_0_0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Can't join master because version 2.0.0 is lower than the minimum compatable version 5.0.0 can support"
argument_list|,
name|electMasterService
operator|.
name|electMaster
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|node
argument_list|)
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testDiscoveryStats
specifier|public
name|void
name|testDiscoveryStats
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|expectedStatsJsonResponse
init|=
literal|"{\n"
operator|+
literal|"  \"discovery\" : {\n"
operator|+
literal|"    \"cluster_state_queue\" : {\n"
operator|+
literal|"      \"total\" : 0,\n"
operator|+
literal|"      \"pending\" : 0,\n"
operator|+
literal|"      \"committed\" : 0\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|)
comment|//<-- To override the local setting if set externally
operator|.
name|build
argument_list|()
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|nodeSettings
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> request node discovery stats"
argument_list|)
expr_stmt|;
name|NodesStatsResponse
name|statsResponse
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
name|prepareNodesStats
argument_list|()
operator|.
name|clear
argument_list|()
operator|.
name|setDiscovery
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|statsResponse
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|DiscoveryStats
name|stats
init|=
name|statsResponse
operator|.
name|getNodes
argument_list|()
index|[
literal|0
index|]
operator|.
name|getDiscoveryStats
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getQueueStats
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getQueueStats
argument_list|()
operator|.
name|getTotal
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getQueueStats
argument_list|()
operator|.
name|getCommitted
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getQueueStats
argument_list|()
operator|.
name|getPending
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|stats
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedStatsJsonResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

