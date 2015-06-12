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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|ClusterInfo
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
name|ClusterInfoService
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
name|DiskUsage
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
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|Map
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
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
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
operator|new
name|ClusterInfoService
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterInfo
name|getClusterInfo
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|usages
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|shardSizes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
return|return
operator|new
name|ClusterInfo
argument_list|(
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|usages
argument_list|)
argument_list|,
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|shardSizes
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addListener
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
comment|// noop
block|}
block|}
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
block|}
end_class

end_unit
