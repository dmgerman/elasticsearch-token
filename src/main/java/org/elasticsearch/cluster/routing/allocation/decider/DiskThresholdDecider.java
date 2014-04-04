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
name|ElasticsearchParseException
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
name|DiskUsage
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
name|RoutingNode
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
name|ShardRouting
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
name|inject
operator|.
name|Inject
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
name|common
operator|.
name|unit
operator|.
name|RatioValue
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
name|elasticsearch
operator|.
name|cluster
operator|.
name|InternalClusterInfoService
operator|.
name|shardIdentifierFromRouting
import|;
end_import

begin_comment
comment|/**  * The {@link DiskThresholdDecider} checks that the node a shard is potentially  * being allocated to has enough disk space.  *  * It has three configurable settings, all of which can be changed dynamically:  *  *<code>cluster.routing.allocation.disk.watermark.low</code> is the low disk  * watermark. New shards will not allocated to a node with usage higher than this,  * although this watermark may be passed by allocating a shard. It defaults to  * 0.70 (70.0%).  *  *<code>cluster.routing.allocation.disk.watermark.high</code> is the high disk  * watermark. If a node has usage higher than this, shards are not allowed to  * remain on the node. In addition, if allocating a shard to a node causes the  * node to pass this watermark, it will not be allowed. It defaults to  * 0.85 (85.0%).  *  * Both watermark settings are expressed in terms of used disk percentage, or  * exact byte values for free space (like "500mb")  *  *<code>cluster.routing.allocation.disk.threshold_enabled</code> is used to  * enable or disable this decider. It defaults to false (disabled).  */
end_comment

begin_class
DECL|class|DiskThresholdDecider
specifier|public
class|class
name|DiskThresholdDecider
extends|extends
name|AllocationDecider
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"disk_threshold"
decl_stmt|;
DECL|field|freeDiskThresholdLow
specifier|private
specifier|volatile
name|Double
name|freeDiskThresholdLow
decl_stmt|;
DECL|field|freeDiskThresholdHigh
specifier|private
specifier|volatile
name|Double
name|freeDiskThresholdHigh
decl_stmt|;
DECL|field|freeBytesThresholdLow
specifier|private
specifier|volatile
name|ByteSizeValue
name|freeBytesThresholdLow
decl_stmt|;
DECL|field|freeBytesThresholdHigh
specifier|private
specifier|volatile
name|ByteSizeValue
name|freeBytesThresholdHigh
decl_stmt|;
DECL|field|enabled
specifier|private
specifier|volatile
name|boolean
name|enabled
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED
init|=
literal|"cluster.routing.allocation.disk.threshold_enabled"
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_LOW_DISK_WATERMARK
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_ROUTING_ALLOCATION_LOW_DISK_WATERMARK
init|=
literal|"cluster.routing.allocation.disk.watermark.low"
decl_stmt|;
DECL|field|CLUSTER_ROUTING_ALLOCATION_HIGH_DISK_WATERMARK
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_ROUTING_ALLOCATION_HIGH_DISK_WATERMARK
init|=
literal|"cluster.routing.allocation.disk.watermark.high"
decl_stmt|;
DECL|class|ApplySettings
class|class
name|ApplySettings
implements|implements
name|NodeSettingsService
operator|.
name|Listener
block|{
annotation|@
name|Override
DECL|method|onRefreshSettings
specifier|public
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|String
name|newLowWatermark
init|=
name|settings
operator|.
name|get
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_LOW_DISK_WATERMARK
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|String
name|newHighWatermark
init|=
name|settings
operator|.
name|get
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_HIGH_DISK_WATERMARK
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Boolean
name|newEnableSetting
init|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|newEnableSetting
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [{}] from [{}] to [{}]"
argument_list|,
name|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED
argument_list|,
name|DiskThresholdDecider
operator|.
name|this
operator|.
name|enabled
argument_list|,
name|newEnableSetting
argument_list|)
expr_stmt|;
name|DiskThresholdDecider
operator|.
name|this
operator|.
name|enabled
operator|=
name|newEnableSetting
expr_stmt|;
block|}
if|if
condition|(
name|newLowWatermark
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|validWatermarkSetting
argument_list|(
name|newLowWatermark
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Unable to parse low watermark: ["
operator|+
name|newLowWatermark
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"updating [{}] to [{}]"
argument_list|,
name|CLUSTER_ROUTING_ALLOCATION_LOW_DISK_WATERMARK
argument_list|,
name|newLowWatermark
argument_list|)
expr_stmt|;
name|DiskThresholdDecider
operator|.
name|this
operator|.
name|freeDiskThresholdLow
operator|=
literal|100.0
operator|-
name|thresholdPercentageFromWatermark
argument_list|(
name|newLowWatermark
argument_list|)
expr_stmt|;
name|DiskThresholdDecider
operator|.
name|this
operator|.
name|freeBytesThresholdLow
operator|=
name|thresholdBytesFromWatermark
argument_list|(
name|newLowWatermark
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|newHighWatermark
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|validWatermarkSetting
argument_list|(
name|newHighWatermark
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Unable to parse high watermark: ["
operator|+
name|newHighWatermark
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"updating [{}] to [{}]"
argument_list|,
name|CLUSTER_ROUTING_ALLOCATION_HIGH_DISK_WATERMARK
argument_list|,
name|newHighWatermark
argument_list|)
expr_stmt|;
name|DiskThresholdDecider
operator|.
name|this
operator|.
name|freeDiskThresholdHigh
operator|=
literal|100.0
operator|-
name|thresholdPercentageFromWatermark
argument_list|(
name|newHighWatermark
argument_list|)
expr_stmt|;
name|DiskThresholdDecider
operator|.
name|this
operator|.
name|freeBytesThresholdHigh
operator|=
name|thresholdBytesFromWatermark
argument_list|(
name|newHighWatermark
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|DiskThresholdDecider
specifier|public
name|DiskThresholdDecider
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
argument_list|(
name|settings
argument_list|,
operator|new
name|NodeSettingsService
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|DiskThresholdDecider
specifier|public
name|DiskThresholdDecider
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|String
name|lowWatermark
init|=
name|settings
operator|.
name|get
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_LOW_DISK_WATERMARK
argument_list|,
literal|"70%"
argument_list|)
decl_stmt|;
name|String
name|highWatermark
init|=
name|settings
operator|.
name|get
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_HIGH_DISK_WATERMARK
argument_list|,
literal|"85%"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|validWatermarkSetting
argument_list|(
name|lowWatermark
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Unable to parse low watermark: ["
operator|+
name|lowWatermark
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|validWatermarkSetting
argument_list|(
name|highWatermark
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Unable to parse high watermark: ["
operator|+
name|highWatermark
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|// Watermark is expressed in terms of used data, but we need "free" data watermark
name|this
operator|.
name|freeDiskThresholdLow
operator|=
literal|100.0
operator|-
name|thresholdPercentageFromWatermark
argument_list|(
name|lowWatermark
argument_list|)
expr_stmt|;
name|this
operator|.
name|freeDiskThresholdHigh
operator|=
literal|100.0
operator|-
name|thresholdPercentageFromWatermark
argument_list|(
name|highWatermark
argument_list|)
expr_stmt|;
name|this
operator|.
name|freeBytesThresholdLow
operator|=
name|thresholdBytesFromWatermark
argument_list|(
name|lowWatermark
argument_list|)
expr_stmt|;
name|this
operator|.
name|freeBytesThresholdHigh
operator|=
name|thresholdBytesFromWatermark
argument_list|(
name|highWatermark
argument_list|)
expr_stmt|;
name|this
operator|.
name|enabled
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|nodeSettingsService
operator|.
name|addListener
argument_list|(
operator|new
name|ApplySettings
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|canAllocate
specifier|public
name|Decision
name|canAllocate
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"disk threshold decider disabled"
argument_list|)
return|;
block|}
comment|// Allow allocation regardless if only a single node is available
if|if
condition|(
name|allocation
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"only a single node is present"
argument_list|)
return|;
block|}
name|ClusterInfo
name|clusterInfo
init|=
name|allocation
operator|.
name|clusterInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|clusterInfo
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Cluster info unavailable for disk threshold decider, allowing allocation."
argument_list|)
expr_stmt|;
block|}
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"cluster info unavailable"
argument_list|)
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|usages
init|=
name|clusterInfo
operator|.
name|getNodeDiskUsages
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
name|clusterInfo
operator|.
name|getShardSizes
argument_list|()
decl_stmt|;
if|if
condition|(
name|usages
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Unable to determine disk usages for disk-aware allocation, allowing allocation"
argument_list|)
expr_stmt|;
block|}
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"disk usages unavailable"
argument_list|)
return|;
block|}
name|DiskUsage
name|usage
init|=
name|usages
operator|.
name|get
argument_list|(
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|usage
operator|==
literal|null
condition|)
block|{
comment|// If there is no usage, and we have other nodes in the cluster,
comment|// use the average usage for all nodes as the usage for this node
name|usage
operator|=
name|averageUsage
argument_list|(
name|node
argument_list|,
name|usages
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Unable to determine disk usage for [{}], defaulting to average across nodes [{} total] [{} free] [{}% free]"
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|,
name|usage
operator|.
name|getTotalBytes
argument_list|()
argument_list|,
name|usage
operator|.
name|getFreeBytes
argument_list|()
argument_list|,
name|usage
operator|.
name|getFreeDiskAsPercentage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// First, check that the node currently over the low watermark
name|double
name|freeDiskPercentage
init|=
name|usage
operator|.
name|getFreeDiskAsPercentage
argument_list|()
decl_stmt|;
name|long
name|freeBytes
init|=
name|usage
operator|.
name|getFreeBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Node [{}] has {}% free disk"
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|,
name|freeDiskPercentage
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|freeBytes
operator|<
name|freeBytesThresholdLow
operator|.
name|bytes
argument_list|()
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Less than the required {} free bytes threshold ({} bytes free) on node {}, preventing allocation"
argument_list|,
name|freeBytesThresholdLow
argument_list|,
name|freeBytes
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"less than required [%s] free on node, free: [%s]"
argument_list|,
name|freeBytesThresholdLow
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|freeBytes
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|freeDiskPercentage
operator|<
name|freeDiskThresholdLow
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Less than the required {}% free disk threshold ({}% free) on node [{}], preventing allocation"
argument_list|,
name|freeDiskThresholdLow
argument_list|,
name|freeDiskPercentage
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"less than required [%d%%] free disk on node, free: [%d%%]"
argument_list|,
name|freeDiskThresholdLow
argument_list|,
name|freeDiskThresholdLow
argument_list|)
return|;
block|}
comment|// Secondly, check that allocating the shard to this node doesn't put it above the high watermark
name|Long
name|shardSize
init|=
name|shardSizes
operator|.
name|get
argument_list|(
name|shardIdentifierFromRouting
argument_list|(
name|shardRouting
argument_list|)
argument_list|)
decl_stmt|;
name|shardSize
operator|=
name|shardSize
operator|==
literal|null
condition|?
literal|0
else|:
name|shardSize
expr_stmt|;
name|double
name|freeSpaceAfterShard
init|=
name|this
operator|.
name|freeDiskPercentageAfterShardAssigned
argument_list|(
name|usage
argument_list|,
name|shardSize
argument_list|)
decl_stmt|;
name|long
name|freeBytesAfterShard
init|=
name|freeBytes
operator|-
name|shardSize
decl_stmt|;
if|if
condition|(
name|freeBytesAfterShard
operator|<
name|freeBytesThresholdHigh
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"After allocating, node [{}] would have less than the required {} free bytes threshold ({} bytes free), preventing allocation"
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|,
name|freeBytesThresholdHigh
argument_list|,
name|freeBytesAfterShard
argument_list|)
expr_stmt|;
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"after allocation less than required [%s] free on node, free: [%s]"
argument_list|,
name|freeBytesThresholdLow
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|freeBytesAfterShard
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|freeSpaceAfterShard
operator|<
name|freeDiskThresholdHigh
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"After allocating, node [{}] would have less than the required {}% free disk threshold ({}% free), preventing allocation"
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|,
name|freeDiskThresholdHigh
argument_list|,
name|freeSpaceAfterShard
argument_list|)
expr_stmt|;
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"after allocation less than required [%d%%] free disk on node, free: [%d%%]"
argument_list|,
name|freeDiskThresholdLow
argument_list|,
name|freeSpaceAfterShard
argument_list|)
return|;
block|}
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"enough disk for shard on node, free: [%s]"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|freeBytes
argument_list|)
argument_list|)
return|;
block|}
DECL|method|canRemain
specifier|public
name|Decision
name|canRemain
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"disk threshold decider disabled"
argument_list|)
return|;
block|}
comment|// Allow allocation regardless if only a single node is available
if|if
condition|(
name|allocation
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"only a single node is present"
argument_list|)
return|;
block|}
name|ClusterInfo
name|clusterInfo
init|=
name|allocation
operator|.
name|clusterInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|clusterInfo
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Cluster info unavailable for disk threshold decider, allowing allocation."
argument_list|)
expr_stmt|;
block|}
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"cluster info unavailable"
argument_list|)
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|usages
init|=
name|clusterInfo
operator|.
name|getNodeDiskUsages
argument_list|()
decl_stmt|;
if|if
condition|(
name|usages
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Unable to determine disk usages for disk-aware allocation, allowing allocation"
argument_list|)
expr_stmt|;
block|}
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"disk usages unavailable"
argument_list|)
return|;
block|}
name|DiskUsage
name|usage
init|=
name|usages
operator|.
name|get
argument_list|(
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|usage
operator|==
literal|null
condition|)
block|{
comment|// If there is no usage, and we have other nodes in the cluster,
comment|// use the average usage for all nodes as the usage for this node
name|usage
operator|=
name|averageUsage
argument_list|(
name|node
argument_list|,
name|usages
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Unable to determine disk usage for {}, defaulting to average across nodes [{} total] [{} free] [{}% free]"
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|,
name|usage
operator|.
name|getTotalBytes
argument_list|()
argument_list|,
name|usage
operator|.
name|getFreeBytes
argument_list|()
argument_list|,
name|usage
operator|.
name|getFreeDiskAsPercentage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// If this node is already above the high threshold, the shard cannot remain (get it off!)
name|double
name|freeDiskPercentage
init|=
name|usage
operator|.
name|getFreeDiskAsPercentage
argument_list|()
decl_stmt|;
name|long
name|freeBytes
init|=
name|usage
operator|.
name|getFreeBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Node [{}] has {}% free disk ({} bytes)"
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|,
name|freeDiskPercentage
argument_list|,
name|freeBytes
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|freeBytes
operator|<
name|freeBytesThresholdHigh
operator|.
name|bytes
argument_list|()
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Less than the required {} free bytes threshold ({} bytes free) on node {}, shard cannot remain"
argument_list|,
name|freeBytesThresholdHigh
argument_list|,
name|freeBytes
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"after allocation less than required [%s] free on node, free: [%s]"
argument_list|,
name|freeBytesThresholdHigh
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|freeBytes
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|freeDiskPercentage
operator|<
name|freeDiskThresholdHigh
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Less than the required {}% free disk threshold ({}% free) on node {}, shard cannot remain"
argument_list|,
name|freeDiskThresholdHigh
argument_list|,
name|freeDiskPercentage
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"after allocation less than required [%d%%] free disk on node, free: [%d%%]"
argument_list|,
name|freeDiskThresholdHigh
argument_list|,
name|freeDiskPercentage
argument_list|)
return|;
block|}
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"enough disk for shard to remain on node, free: [%s]"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|freeBytes
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns a {@link DiskUsage} for the {@link RoutingNode} using the      * average usage of other nodes in the disk usage map.      * @param node Node to return an averaged DiskUsage object for      * @param usages Map of nodeId to DiskUsage for all known nodes      * @return DiskUsage representing given node using the average disk usage      */
DECL|method|averageUsage
specifier|public
name|DiskUsage
name|averageUsage
parameter_list|(
name|RoutingNode
name|node
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|DiskUsage
argument_list|>
name|usages
parameter_list|)
block|{
name|long
name|totalBytes
init|=
literal|0
decl_stmt|;
name|long
name|freeBytes
init|=
literal|0
decl_stmt|;
for|for
control|(
name|DiskUsage
name|du
range|:
name|usages
operator|.
name|values
argument_list|()
control|)
block|{
name|totalBytes
operator|+=
name|du
operator|.
name|getTotalBytes
argument_list|()
expr_stmt|;
name|freeBytes
operator|+=
name|du
operator|.
name|getFreeBytes
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|DiskUsage
argument_list|(
name|node
operator|.
name|nodeId
argument_list|()
argument_list|,
name|totalBytes
operator|/
name|usages
operator|.
name|size
argument_list|()
argument_list|,
name|freeBytes
operator|/
name|usages
operator|.
name|size
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Given the DiskUsage for a node and the size of the shard, return the      * percentage of free disk if the shard were to be allocated to the node.      * @param usage A DiskUsage for the node to have space computed for      * @param shardSize Size in bytes of the shard      * @return Percentage of free space after the shard is assigned to the node      */
DECL|method|freeDiskPercentageAfterShardAssigned
specifier|public
name|double
name|freeDiskPercentageAfterShardAssigned
parameter_list|(
name|DiskUsage
name|usage
parameter_list|,
name|Long
name|shardSize
parameter_list|)
block|{
name|shardSize
operator|=
operator|(
name|shardSize
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|shardSize
expr_stmt|;
return|return
literal|100.0
operator|-
operator|(
operator|(
call|(
name|double
call|)
argument_list|(
name|usage
operator|.
name|getUsedBytes
argument_list|()
operator|+
name|shardSize
argument_list|)
operator|/
name|usage
operator|.
name|getTotalBytes
argument_list|()
operator|)
operator|*
literal|100.0
operator|)
return|;
block|}
comment|/**      * Attempts to parse the watermark into a percentage, returning 100.0% if      * it cannot be parsed.      */
DECL|method|thresholdPercentageFromWatermark
specifier|public
name|double
name|thresholdPercentageFromWatermark
parameter_list|(
name|String
name|watermark
parameter_list|)
block|{
try|try
block|{
return|return
name|RatioValue
operator|.
name|parseRatioValue
argument_list|(
name|watermark
argument_list|)
operator|.
name|getAsPercent
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|ex
parameter_list|)
block|{
return|return
literal|100.0
return|;
block|}
block|}
comment|/**      * Attempts to parse the watermark into a {@link ByteSizeValue}, returning      * a ByteSizeValue of 0 bytes if the value cannot be parsed.      */
DECL|method|thresholdBytesFromWatermark
specifier|public
name|ByteSizeValue
name|thresholdBytesFromWatermark
parameter_list|(
name|String
name|watermark
parameter_list|)
block|{
try|try
block|{
return|return
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
name|watermark
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|ex
parameter_list|)
block|{
return|return
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"0b"
argument_list|)
return|;
block|}
block|}
comment|/**      * Checks if a watermark string is a valid percentage or byte size value,      * returning true if valid, false if invalid.      */
DECL|method|validWatermarkSetting
specifier|public
name|boolean
name|validWatermarkSetting
parameter_list|(
name|String
name|watermark
parameter_list|)
block|{
try|try
block|{
name|RatioValue
operator|.
name|parseRatioValue
argument_list|(
name|watermark
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
try|try
block|{
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
name|watermark
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|ex
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

