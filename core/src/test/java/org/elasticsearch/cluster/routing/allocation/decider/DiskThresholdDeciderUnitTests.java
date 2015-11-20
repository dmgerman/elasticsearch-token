begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation.decider
package|package
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
name|*
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
name|MockInternalClusterInfoService
operator|.
name|DevNullClusterInfo
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
name|*
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
name|RoutingAllocation
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
name|collect
operator|.
name|ImmutableOpenMap
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
name|DummyTransportAddress
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
name|unit
operator|.
name|ByteSizeValue
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
name|settings
operator|.
name|NodeSettingsService
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
name|Arrays
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

begin_comment
comment|/**  * Unit tests for the DiskThresholdDecider  */
end_comment

begin_class
DECL|class|DiskThresholdDeciderUnitTests
specifier|public
class|class
name|DiskThresholdDeciderUnitTests
extends|extends
name|ESTestCase
block|{
DECL|method|testDynamicSettings
specifier|public
name|void
name|testDynamicSettings
parameter_list|()
block|{
name|NodeSettingsService
name|nss
init|=
operator|new
name|NodeSettingsService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|ClusterInfoService
name|cis
init|=
name|EmptyClusterInfoService
operator|.
name|INSTANCE
decl_stmt|;
name|DiskThresholdDecider
name|decider
init|=
operator|new
name|DiskThresholdDecider
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|nss
argument_list|,
name|cis
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|decider
operator|.
name|getFreeBytesThresholdHigh
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"0b"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|decider
operator|.
name|getFreeDiskThresholdHigh
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10.0d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|decider
operator|.
name|getFreeBytesThresholdLow
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"0b"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|decider
operator|.
name|getFreeDiskThresholdLow
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|15.0d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|decider
operator|.
name|getUsedDiskThresholdLow
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|85.0d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|decider
operator|.
name|getRerouteInterval
argument_list|()
operator|.
name|seconds
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|60L
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|decider
operator|.
name|isEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|decider
operator|.
name|isIncludeRelocations
argument_list|()
argument_list|)
expr_stmt|;
name|DiskThresholdDecider
operator|.
name|ApplySettings
name|applySettings
init|=
name|decider
operator|.
name|newApplySettings
argument_list|()
decl_stmt|;
name|Settings
name|newSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_INCLUDE_RELOCATIONS
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_HIGH_DISK_WATERMARK
argument_list|,
literal|"70%"
argument_list|)
operator|.
name|put
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_LOW_DISK_WATERMARK
argument_list|,
literal|"500mb"
argument_list|)
operator|.
name|put
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_REROUTE_INTERVAL
argument_list|,
literal|"30s"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|applySettings
operator|.
name|onRefreshSettings
argument_list|(
name|newSettings
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"high threshold bytes should be unset"
argument_list|,
name|decider
operator|.
name|getFreeBytesThresholdHigh
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"0b"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"high threshold percentage should be changed"
argument_list|,
name|decider
operator|.
name|getFreeDiskThresholdHigh
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|30.0d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"low threshold bytes should be set to 500mb"
argument_list|,
name|decider
operator|.
name|getFreeBytesThresholdLow
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"500mb"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"low threshold bytes should be unset"
argument_list|,
name|decider
operator|.
name|getFreeDiskThresholdLow
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.0d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"reroute interval should be changed to 30 seconds"
argument_list|,
name|decider
operator|.
name|getRerouteInterval
argument_list|()
operator|.
name|seconds
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|30L
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"disk threshold decider should now be disabled"
argument_list|,
name|decider
operator|.
name|isEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"relocations should now be disabled"
argument_list|,
name|decider
operator|.
name|isIncludeRelocations
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testCanAllocateUsesMaxAvailableSpace
specifier|public
name|void
name|testCanAllocateUsesMaxAvailableSpace
parameter_list|()
block|{
name|NodeSettingsService
name|nss
init|=
operator|new
name|NodeSettingsService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|ClusterInfoService
name|cis
init|=
name|EmptyClusterInfoService
operator|.
name|INSTANCE
decl_stmt|;
name|DiskThresholdDecider
name|decider
init|=
operator|new
name|DiskThresholdDecider
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|nss
argument_list|,
name|cis
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ShardRouting
name|test_0
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node_0
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node_0"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node_1
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node_1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test"
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
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|addAsNew
argument_list|(
name|metaData
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
argument_list|)
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
name|DEFAULT
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
name|put
argument_list|(
name|node_0
argument_list|)
operator|.
name|put
argument_list|(
name|node_1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// actual test -- after all that bloat :)
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|leastAvailableUsages
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|leastAvailableUsages
operator|.
name|put
argument_list|(
literal|"node_0"
argument_list|,
operator|new
name|DiskUsage
argument_list|(
literal|"node_0"
argument_list|,
literal|"node_0"
argument_list|,
literal|"_na_"
argument_list|,
literal|100
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// all full
name|leastAvailableUsages
operator|.
name|put
argument_list|(
literal|"node_1"
argument_list|,
operator|new
name|DiskUsage
argument_list|(
literal|"node_1"
argument_list|,
literal|"node_1"
argument_list|,
literal|"_na_"
argument_list|,
literal|100
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// all full
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|mostAvailableUsage
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|mostAvailableUsage
operator|.
name|put
argument_list|(
literal|"node_0"
argument_list|,
operator|new
name|DiskUsage
argument_list|(
literal|"node_0"
argument_list|,
literal|"node_0"
argument_list|,
literal|"_na_"
argument_list|,
literal|100
argument_list|,
name|randomIntBetween
argument_list|(
literal|20
argument_list|,
literal|100
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// 20 - 99 percent since after allocation there must be at least 10% left and shard is 10byte
name|mostAvailableUsage
operator|.
name|put
argument_list|(
literal|"node_1"
argument_list|,
operator|new
name|DiskUsage
argument_list|(
literal|"node_1"
argument_list|,
literal|"node_1"
argument_list|,
literal|"_na_"
argument_list|,
literal|100
argument_list|,
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// this is weird and smells like a bug! it should be up to 20%?
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|shardSizes
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|shardSizes
operator|.
name|put
argument_list|(
literal|"[test][0][p]"
argument_list|,
literal|10L
argument_list|)
expr_stmt|;
comment|// 10 bytes
specifier|final
name|ClusterInfo
name|clusterInfo
init|=
operator|new
name|ClusterInfo
argument_list|(
name|leastAvailableUsages
operator|.
name|build
argument_list|()
argument_list|,
name|mostAvailableUsage
operator|.
name|build
argument_list|()
argument_list|,
name|shardSizes
operator|.
name|build
argument_list|()
argument_list|,
name|ImmutableOpenMap
operator|.
name|of
argument_list|()
argument_list|)
decl_stmt|;
name|RoutingAllocation
name|allocation
init|=
operator|new
name|RoutingAllocation
argument_list|(
operator|new
name|AllocationDeciders
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|AllocationDecider
index|[]
block|{
name|decider
block|}
argument_list|)
argument_list|,
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|,
name|clusterInfo
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|mostAvailableUsage
operator|.
name|toString
argument_list|()
argument_list|,
name|Decision
operator|.
name|YES
argument_list|,
name|decider
operator|.
name|canAllocate
argument_list|(
name|test_0
argument_list|,
operator|new
name|RoutingNode
argument_list|(
literal|"node_0"
argument_list|,
name|node_0
argument_list|)
argument_list|,
name|allocation
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mostAvailableUsage
operator|.
name|toString
argument_list|()
argument_list|,
name|Decision
operator|.
name|NO
argument_list|,
name|decider
operator|.
name|canAllocate
argument_list|(
name|test_0
argument_list|,
operator|new
name|RoutingNode
argument_list|(
literal|"node_1"
argument_list|,
name|node_1
argument_list|)
argument_list|,
name|allocation
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCanRemainUsesLeastAvailableSpace
specifier|public
name|void
name|testCanRemainUsesLeastAvailableSpace
parameter_list|()
block|{
name|NodeSettingsService
name|nss
init|=
operator|new
name|NodeSettingsService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|ClusterInfoService
name|cis
init|=
name|EmptyClusterInfoService
operator|.
name|INSTANCE
decl_stmt|;
name|DiskThresholdDecider
name|decider
init|=
operator|new
name|DiskThresholdDecider
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|nss
argument_list|,
name|cis
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|ShardRouting
argument_list|,
name|String
argument_list|>
name|shardRoutingMap
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|DiscoveryNode
name|node_0
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node_0"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node_1
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node_1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|ShardRouting
name|test_0
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|ShardRoutingHelper
operator|.
name|initialize
argument_list|(
name|test_0
argument_list|,
name|node_0
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|test_0
argument_list|)
expr_stmt|;
name|shardRoutingMap
operator|.
name|put
argument_list|(
name|test_0
argument_list|,
literal|"/node0/least"
argument_list|)
expr_stmt|;
name|ShardRouting
name|test_1
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|ShardRoutingHelper
operator|.
name|initialize
argument_list|(
name|test_1
argument_list|,
name|node_1
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|test_1
argument_list|)
expr_stmt|;
name|shardRoutingMap
operator|.
name|put
argument_list|(
name|test_1
argument_list|,
literal|"/node1/least"
argument_list|)
expr_stmt|;
name|ShardRouting
name|test_2
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
literal|"test"
argument_list|,
literal|2
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|ShardRoutingHelper
operator|.
name|initialize
argument_list|(
name|test_2
argument_list|,
name|node_1
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|test_2
argument_list|)
expr_stmt|;
name|shardRoutingMap
operator|.
name|put
argument_list|(
name|test_2
argument_list|,
literal|"/node1/most"
argument_list|)
expr_stmt|;
name|ShardRouting
name|test_3
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
literal|"test"
argument_list|,
literal|3
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|ShardRoutingHelper
operator|.
name|initialize
argument_list|(
name|test_3
argument_list|,
name|node_1
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|test_3
argument_list|)
expr_stmt|;
comment|// Intentionally not in the shardRoutingMap. We want to test what happens when we don't know where it is.
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test"
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
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|addAsNew
argument_list|(
name|metaData
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
argument_list|)
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
name|DEFAULT
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> adding two nodes"
argument_list|)
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
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|node_0
argument_list|)
operator|.
name|put
argument_list|(
name|node_1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// actual test -- after all that bloat :)
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|leastAvailableUsages
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|leastAvailableUsages
operator|.
name|put
argument_list|(
literal|"node_0"
argument_list|,
operator|new
name|DiskUsage
argument_list|(
literal|"node_0"
argument_list|,
literal|"node_0"
argument_list|,
literal|"/node0/least"
argument_list|,
literal|100
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
comment|// 90% used
name|leastAvailableUsages
operator|.
name|put
argument_list|(
literal|"node_1"
argument_list|,
operator|new
name|DiskUsage
argument_list|(
literal|"node_1"
argument_list|,
literal|"node_1"
argument_list|,
literal|"/node1/least"
argument_list|,
literal|100
argument_list|,
literal|9
argument_list|)
argument_list|)
expr_stmt|;
comment|// 91% used
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|mostAvailableUsage
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|mostAvailableUsage
operator|.
name|put
argument_list|(
literal|"node_0"
argument_list|,
operator|new
name|DiskUsage
argument_list|(
literal|"node_0"
argument_list|,
literal|"node_0"
argument_list|,
literal|"/node0/most"
argument_list|,
literal|100
argument_list|,
literal|90
argument_list|)
argument_list|)
expr_stmt|;
comment|// 10% used
name|mostAvailableUsage
operator|.
name|put
argument_list|(
literal|"node_1"
argument_list|,
operator|new
name|DiskUsage
argument_list|(
literal|"node_1"
argument_list|,
literal|"node_1"
argument_list|,
literal|"/node1/most"
argument_list|,
literal|100
argument_list|,
literal|90
argument_list|)
argument_list|)
expr_stmt|;
comment|// 10% used
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|shardSizes
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|shardSizes
operator|.
name|put
argument_list|(
literal|"[test][0][p]"
argument_list|,
literal|10L
argument_list|)
expr_stmt|;
comment|// 10 bytes
name|shardSizes
operator|.
name|put
argument_list|(
literal|"[test][1][p]"
argument_list|,
literal|10L
argument_list|)
expr_stmt|;
name|shardSizes
operator|.
name|put
argument_list|(
literal|"[test][2][p]"
argument_list|,
literal|10L
argument_list|)
expr_stmt|;
specifier|final
name|ClusterInfo
name|clusterInfo
init|=
operator|new
name|ClusterInfo
argument_list|(
name|leastAvailableUsages
operator|.
name|build
argument_list|()
argument_list|,
name|mostAvailableUsage
operator|.
name|build
argument_list|()
argument_list|,
name|shardSizes
operator|.
name|build
argument_list|()
argument_list|,
name|shardRoutingMap
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|RoutingAllocation
name|allocation
init|=
operator|new
name|RoutingAllocation
argument_list|(
operator|new
name|AllocationDeciders
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|AllocationDecider
index|[]
block|{
name|decider
block|}
argument_list|)
argument_list|,
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|,
name|clusterInfo
argument_list|,
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|decider
operator|.
name|canRemain
argument_list|(
name|test_0
argument_list|,
operator|new
name|RoutingNode
argument_list|(
literal|"node_0"
argument_list|,
name|node_0
argument_list|)
argument_list|,
name|allocation
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|decider
operator|.
name|canRemain
argument_list|(
name|test_1
argument_list|,
operator|new
name|RoutingNode
argument_list|(
literal|"node_1"
argument_list|,
name|node_1
argument_list|)
argument_list|,
name|allocation
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|decider
operator|.
name|canRemain
argument_list|(
name|test_0
argument_list|,
operator|new
name|RoutingNode
argument_list|(
literal|"node_1"
argument_list|,
name|node_1
argument_list|)
argument_list|,
name|allocation
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"not allocated on this node"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// not allocated on that node
block|}
try|try
block|{
name|decider
operator|.
name|canRemain
argument_list|(
name|test_1
argument_list|,
operator|new
name|RoutingNode
argument_list|(
literal|"node_0"
argument_list|,
name|node_0
argument_list|)
argument_list|,
name|allocation
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"not allocated on this node"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// not allocated on that node
block|}
name|assertEquals
argument_list|(
literal|"can stay since allocated on a different path with enough space"
argument_list|,
name|Decision
operator|.
name|YES
argument_list|,
name|decider
operator|.
name|canRemain
argument_list|(
name|test_2
argument_list|,
operator|new
name|RoutingNode
argument_list|(
literal|"node_1"
argument_list|,
name|node_1
argument_list|)
argument_list|,
name|allocation
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"can stay since we don't have information about this shard"
argument_list|,
name|Decision
operator|.
name|YES
argument_list|,
name|decider
operator|.
name|canRemain
argument_list|(
name|test_2
argument_list|,
operator|new
name|RoutingNode
argument_list|(
literal|"node_1"
argument_list|,
name|node_1
argument_list|)
argument_list|,
name|allocation
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testShardSizeAndRelocatingSize
specifier|public
name|void
name|testShardSizeAndRelocatingSize
parameter_list|()
block|{
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|shardSizes
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|shardSizes
operator|.
name|put
argument_list|(
literal|"[test][0][r]"
argument_list|,
literal|10L
argument_list|)
expr_stmt|;
name|shardSizes
operator|.
name|put
argument_list|(
literal|"[test][1][r]"
argument_list|,
literal|100L
argument_list|)
expr_stmt|;
name|shardSizes
operator|.
name|put
argument_list|(
literal|"[test][2][r]"
argument_list|,
literal|1000L
argument_list|)
expr_stmt|;
name|shardSizes
operator|.
name|put
argument_list|(
literal|"[other][0][p]"
argument_list|,
literal|10000L
argument_list|)
expr_stmt|;
name|ClusterInfo
name|info
init|=
operator|new
name|DevNullClusterInfo
argument_list|(
name|ImmutableOpenMap
operator|.
name|of
argument_list|()
argument_list|,
name|ImmutableOpenMap
operator|.
name|of
argument_list|()
argument_list|,
name|shardSizes
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|ShardRouting
name|test_0
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|ShardRoutingHelper
operator|.
name|initialize
argument_list|(
name|test_0
argument_list|,
literal|"node1"
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|test_0
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|relocate
argument_list|(
name|test_0
argument_list|,
literal|"node2"
argument_list|)
expr_stmt|;
name|ShardRouting
name|test_1
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|ShardRoutingHelper
operator|.
name|initialize
argument_list|(
name|test_1
argument_list|,
literal|"node2"
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|test_1
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|relocate
argument_list|(
name|test_1
argument_list|,
literal|"node1"
argument_list|)
expr_stmt|;
name|ShardRouting
name|test_2
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
literal|"test"
argument_list|,
literal|2
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|ShardRoutingHelper
operator|.
name|initialize
argument_list|(
name|test_2
argument_list|,
literal|"node1"
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|test_2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000l
argument_list|,
name|DiskThresholdDecider
operator|.
name|getShardSize
argument_list|(
name|test_2
argument_list|,
name|info
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100l
argument_list|,
name|DiskThresholdDecider
operator|.
name|getShardSize
argument_list|(
name|test_1
argument_list|,
name|info
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10l
argument_list|,
name|DiskThresholdDecider
operator|.
name|getShardSize
argument_list|(
name|test_0
argument_list|,
name|info
argument_list|)
argument_list|)
expr_stmt|;
name|RoutingNode
name|node
init|=
operator|new
name|RoutingNode
argument_list|(
literal|"node1"
argument_list|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"node1"
argument_list|,
name|LocalTransportAddress
operator|.
name|PROTO
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|test_0
argument_list|,
name|test_1
operator|.
name|buildTargetRelocatingShard
argument_list|()
argument_list|,
name|test_2
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|100l
argument_list|,
name|DiskThresholdDecider
operator|.
name|sizeOfRelocatingShards
argument_list|(
name|node
argument_list|,
name|info
argument_list|,
literal|false
argument_list|,
literal|"/dev/null"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|90l
argument_list|,
name|DiskThresholdDecider
operator|.
name|sizeOfRelocatingShards
argument_list|(
name|node
argument_list|,
name|info
argument_list|,
literal|true
argument_list|,
literal|"/dev/null"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0l
argument_list|,
name|DiskThresholdDecider
operator|.
name|sizeOfRelocatingShards
argument_list|(
name|node
argument_list|,
name|info
argument_list|,
literal|true
argument_list|,
literal|"/dev/some/other/dev"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0l
argument_list|,
name|DiskThresholdDecider
operator|.
name|sizeOfRelocatingShards
argument_list|(
name|node
argument_list|,
name|info
argument_list|,
literal|true
argument_list|,
literal|"/dev/some/other/dev"
argument_list|)
argument_list|)
expr_stmt|;
name|ShardRouting
name|test_3
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
literal|"test"
argument_list|,
literal|3
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|ShardRoutingHelper
operator|.
name|initialize
argument_list|(
name|test_3
argument_list|,
literal|"node1"
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|test_3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0l
argument_list|,
name|DiskThresholdDecider
operator|.
name|getShardSize
argument_list|(
name|test_3
argument_list|,
name|info
argument_list|)
argument_list|)
expr_stmt|;
name|ShardRouting
name|other_0
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
literal|"other"
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|ShardRoutingHelper
operator|.
name|initialize
argument_list|(
name|other_0
argument_list|,
literal|"node2"
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|other_0
argument_list|)
expr_stmt|;
name|ShardRoutingHelper
operator|.
name|relocate
argument_list|(
name|other_0
argument_list|,
literal|"node1"
argument_list|)
expr_stmt|;
name|node
operator|=
operator|new
name|RoutingNode
argument_list|(
literal|"node1"
argument_list|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"node1"
argument_list|,
name|LocalTransportAddress
operator|.
name|PROTO
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|test_0
argument_list|,
name|test_1
operator|.
name|buildTargetRelocatingShard
argument_list|()
argument_list|,
name|test_2
argument_list|,
name|other_0
operator|.
name|buildTargetRelocatingShard
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|other_0
operator|.
name|primary
argument_list|()
condition|)
block|{
name|assertEquals
argument_list|(
literal|10100l
argument_list|,
name|DiskThresholdDecider
operator|.
name|sizeOfRelocatingShards
argument_list|(
name|node
argument_list|,
name|info
argument_list|,
literal|false
argument_list|,
literal|"/dev/null"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10090l
argument_list|,
name|DiskThresholdDecider
operator|.
name|sizeOfRelocatingShards
argument_list|(
name|node
argument_list|,
name|info
argument_list|,
literal|true
argument_list|,
literal|"/dev/null"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|100l
argument_list|,
name|DiskThresholdDecider
operator|.
name|sizeOfRelocatingShards
argument_list|(
name|node
argument_list|,
name|info
argument_list|,
literal|false
argument_list|,
literal|"/dev/null"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|90l
argument_list|,
name|DiskThresholdDecider
operator|.
name|sizeOfRelocatingShards
argument_list|(
name|node
argument_list|,
name|info
argument_list|,
literal|true
argument_list|,
literal|"/dev/null"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

