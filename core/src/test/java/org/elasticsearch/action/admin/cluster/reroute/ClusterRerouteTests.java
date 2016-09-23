begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.reroute
package|package
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
name|reroute
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
name|ActionListener
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
name|ESAllocationTestCase
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
name|EmptyClusterInfoService
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
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|FailedShard
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
name|allocator
operator|.
name|BalancedShardsAllocator
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
name|command
operator|.
name|AllocateEmptyPrimaryAllocationCommand
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
name|decider
operator|.
name|AllocationDeciders
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
name|decider
operator|.
name|MaxRetryAllocationDecider
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
name|NamedWriteableAwareStreamInput
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
name|network
operator|.
name|NetworkModule
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
name|test
operator|.
name|gateway
operator|.
name|NoopGatewayAllocator
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
name|AtomicReference
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRoutingState
operator|.
name|INITIALIZING
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRoutingState
operator|.
name|UNASSIGNED
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
DECL|class|ClusterRerouteTests
specifier|public
class|class
name|ClusterRerouteTests
extends|extends
name|ESAllocationTestCase
block|{
DECL|method|testSerializeRequest
specifier|public
name|void
name|testSerializeRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|ClusterRerouteRequest
name|req
init|=
operator|new
name|ClusterRerouteRequest
argument_list|()
decl_stmt|;
name|req
operator|.
name|setRetryFailed
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|req
operator|.
name|dryRun
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|req
operator|.
name|explain
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|req
operator|.
name|add
argument_list|(
operator|new
name|AllocateEmptyPrimaryAllocationCommand
argument_list|(
literal|"foo"
argument_list|,
literal|1
argument_list|,
literal|"bar"
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|req
operator|.
name|timeout
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|req
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|BytesReference
name|bytes
init|=
name|out
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|NamedWriteableRegistry
name|namedWriteableRegistry
init|=
operator|new
name|NamedWriteableRegistry
argument_list|(
name|NetworkModule
operator|.
name|getNamedWriteables
argument_list|()
argument_list|)
decl_stmt|;
name|StreamInput
name|wrap
init|=
operator|new
name|NamedWriteableAwareStreamInput
argument_list|(
name|bytes
operator|.
name|streamInput
argument_list|()
argument_list|,
name|namedWriteableRegistry
argument_list|)
decl_stmt|;
name|ClusterRerouteRequest
name|deserializedReq
init|=
operator|new
name|ClusterRerouteRequest
argument_list|()
decl_stmt|;
name|deserializedReq
operator|.
name|readFrom
argument_list|(
name|wrap
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|req
operator|.
name|isRetryFailed
argument_list|()
argument_list|,
name|deserializedReq
operator|.
name|isRetryFailed
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|req
operator|.
name|dryRun
argument_list|()
argument_list|,
name|deserializedReq
operator|.
name|dryRun
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|req
operator|.
name|explain
argument_list|()
argument_list|,
name|deserializedReq
operator|.
name|explain
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|req
operator|.
name|timeout
argument_list|()
argument_list|,
name|deserializedReq
operator|.
name|timeout
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|deserializedReq
operator|.
name|getCommands
argument_list|()
operator|.
name|commands
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// allocation commands have their own tests
name|assertEquals
argument_list|(
name|req
operator|.
name|getCommands
argument_list|()
operator|.
name|commands
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|deserializedReq
operator|.
name|getCommands
argument_list|()
operator|.
name|commands
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testClusterStateUpdateTask
specifier|public
name|void
name|testClusterStateUpdateTask
parameter_list|()
block|{
name|AllocationService
name|allocationService
init|=
operator|new
name|AllocationService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|AllocationDeciders
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
operator|new
name|MaxRetryAllocationDecider
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|NoopGatewayAllocator
operator|.
name|INSTANCE
argument_list|,
operator|new
name|BalancedShardsAllocator
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|EmptyClusterInfoService
operator|.
name|INSTANCE
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|createInitialClusterState
argument_list|(
name|allocationService
argument_list|)
decl_stmt|;
name|ClusterRerouteRequest
name|req
init|=
operator|new
name|ClusterRerouteRequest
argument_list|()
decl_stmt|;
name|req
operator|.
name|dryRun
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|AtomicReference
argument_list|<
name|ClusterRerouteResponse
argument_list|>
name|responseRef
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|ActionListener
argument_list|<
name|ClusterRerouteResponse
argument_list|>
name|responseActionListener
init|=
operator|new
name|ActionListener
argument_list|<
name|ClusterRerouteResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ClusterRerouteResponse
name|clusterRerouteResponse
parameter_list|)
block|{
name|responseRef
operator|.
name|set
argument_list|(
name|clusterRerouteResponse
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{              }
block|}
decl_stmt|;
name|TransportClusterRerouteAction
operator|.
name|ClusterRerouteResponseAckedClusterStateUpdateTask
name|task
init|=
operator|new
name|TransportClusterRerouteAction
operator|.
name|ClusterRerouteResponseAckedClusterStateUpdateTask
argument_list|(
name|logger
argument_list|,
name|allocationService
argument_list|,
name|req
argument_list|,
name|responseActionListener
argument_list|)
decl_stmt|;
name|ClusterState
name|execute
init|=
name|task
operator|.
name|execute
argument_list|(
name|clusterState
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|execute
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
comment|// dry-run
name|task
operator|.
name|onAllNodesAcked
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|responseRef
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
argument_list|,
name|execute
argument_list|)
expr_stmt|;
name|req
operator|.
name|dryRun
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// now we allocate
specifier|final
name|int
name|retries
init|=
name|MaxRetryAllocationDecider
operator|.
name|SETTING_ALLOCATION_MAX_RETRY
operator|.
name|get
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
comment|// now fail it N-1 times
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|retries
condition|;
name|i
operator|++
control|)
block|{
name|ClusterState
name|newState
init|=
name|task
operator|.
name|execute
argument_list|(
name|clusterState
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|newState
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
comment|// dry-run=false
name|clusterState
operator|=
name|newState
expr_stmt|;
name|RoutingTable
name|routingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|INITIALIZING
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getNumFailedAllocations
argument_list|()
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FailedShard
argument_list|>
name|failedShards
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|FailedShard
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"boom"
operator|+
name|i
argument_list|,
operator|new
name|UnsupportedOperationException
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|newState
operator|=
name|allocationService
operator|.
name|applyFailedShards
argument_list|(
name|clusterState
argument_list|,
name|failedShards
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newState
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|clusterState
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|newState
expr_stmt|;
name|routingTable
operator|=
name|clusterState
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
name|retries
operator|-
literal|1
condition|)
block|{
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|UNASSIGNED
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|INITIALIZING
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getNumFailedAllocations
argument_list|()
argument_list|,
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// without retry_failed we won't allocate that shard
name|ClusterState
name|newState
init|=
name|task
operator|.
name|execute
argument_list|(
name|clusterState
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|newState
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
comment|// dry-run=false
name|task
operator|.
name|onAllNodesAcked
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|responseRef
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
argument_list|,
name|newState
argument_list|)
expr_stmt|;
name|RoutingTable
name|routingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|UNASSIGNED
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getNumFailedAllocations
argument_list|()
argument_list|,
name|retries
argument_list|)
expr_stmt|;
name|req
operator|.
name|setRetryFailed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// now we manually retry and get the shard back into initializing
name|newState
operator|=
name|task
operator|.
name|execute
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|newState
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
comment|// dry-run=false
name|clusterState
operator|=
name|newState
expr_stmt|;
name|routingTable
operator|=
name|clusterState
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|INITIALIZING
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|unassignedInfo
argument_list|()
operator|.
name|getNumFailedAllocations
argument_list|()
argument_list|,
name|retries
argument_list|)
expr_stmt|;
block|}
DECL|method|createInitialClusterState
specifier|private
name|ClusterState
name|createInitialClusterState
parameter_list|(
name|AllocationService
name|service
parameter_list|)
block|{
name|MetaData
operator|.
name|Builder
name|metaBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
decl_stmt|;
name|metaBuilder
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"idx"
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
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|MetaData
name|metaData
init|=
name|metaBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
operator|.
name|Builder
name|routingTableBuilder
init|=
name|RoutingTable
operator|.
name|builder
argument_list|()
decl_stmt|;
name|routingTableBuilder
operator|.
name|addAsNew
argument_list|(
name|metaData
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
argument_list|)
expr_stmt|;
name|RoutingTable
name|routingTable
init|=
name|routingTableBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
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
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
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
name|newNode
argument_list|(
literal|"node1"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|newNode
argument_list|(
literal|"node2"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|RoutingTable
name|prevRoutingTable
init|=
name|routingTable
decl_stmt|;
name|routingTable
operator|=
name|service
operator|.
name|reroute
argument_list|(
name|clusterState
argument_list|,
literal|"reroute"
argument_list|)
operator|.
name|routingTable
argument_list|()
expr_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTable
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|prevRoutingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|prevRoutingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|UNASSIGNED
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|routingTable
operator|.
name|index
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|INITIALIZING
argument_list|)
expr_stmt|;
return|return
name|clusterState
return|;
block|}
block|}
end_class

end_unit

