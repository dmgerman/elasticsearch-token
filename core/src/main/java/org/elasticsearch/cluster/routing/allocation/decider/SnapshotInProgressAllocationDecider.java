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
name|cluster
operator|.
name|SnapshotsInProgress
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
name|Setting
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
name|Setting
operator|.
name|Property
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

begin_comment
comment|/**  * This {@link org.elasticsearch.cluster.routing.allocation.decider.AllocationDecider} prevents shards that  * are currently been snapshotted to be moved to other nodes.  */
end_comment

begin_class
DECL|class|SnapshotInProgressAllocationDecider
specifier|public
class|class
name|SnapshotInProgressAllocationDecider
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
literal|"snapshot_in_progress"
decl_stmt|;
comment|/**      * Disables relocation of shards that are currently being snapshotted.      */
DECL|field|CLUSTER_ROUTING_ALLOCATION_SNAPSHOT_RELOCATION_ENABLED_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|CLUSTER_ROUTING_ALLOCATION_SNAPSHOT_RELOCATION_ENABLED_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"cluster.routing.allocation.snapshot.relocation_enabled"
argument_list|,
literal|false
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|enableRelocation
specifier|private
specifier|volatile
name|boolean
name|enableRelocation
init|=
literal|false
decl_stmt|;
comment|/**      * Creates a new {@link org.elasticsearch.cluster.routing.allocation.decider.SnapshotInProgressAllocationDecider} instance      */
DECL|method|SnapshotInProgressAllocationDecider
specifier|public
name|SnapshotInProgressAllocationDecider
parameter_list|()
block|{
name|this
argument_list|(
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new {@link org.elasticsearch.cluster.routing.allocation.decider.SnapshotInProgressAllocationDecider} instance from      * given settings      *      * @param settings {@link org.elasticsearch.common.settings.Settings} to use      */
DECL|method|SnapshotInProgressAllocationDecider
specifier|public
name|SnapshotInProgressAllocationDecider
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
name|ClusterSettings
argument_list|(
name|settings
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|SnapshotInProgressAllocationDecider
specifier|public
name|SnapshotInProgressAllocationDecider
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|enableRelocation
operator|=
name|CLUSTER_ROUTING_ALLOCATION_SNAPSHOT_RELOCATION_ENABLED_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|CLUSTER_ROUTING_ALLOCATION_SNAPSHOT_RELOCATION_ENABLED_SETTING
argument_list|,
name|this
operator|::
name|setEnableRelocation
argument_list|)
expr_stmt|;
block|}
DECL|method|setEnableRelocation
specifier|private
name|void
name|setEnableRelocation
parameter_list|(
name|boolean
name|enableRelocation
parameter_list|)
block|{
name|this
operator|.
name|enableRelocation
operator|=
name|enableRelocation
expr_stmt|;
block|}
comment|/**      * Returns a {@link Decision} whether the given shard routing can be      * re-balanced to the given allocation. The default is      * {@link Decision#ALWAYS}.      */
annotation|@
name|Override
DECL|method|canRebalance
specifier|public
name|Decision
name|canRebalance
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
return|return
name|canMove
argument_list|(
name|shardRouting
argument_list|,
name|allocation
argument_list|)
return|;
block|}
comment|/**      * Returns a {@link Decision} whether the given shard routing can be      * allocated on the given node. The default is {@link Decision#ALWAYS}.      */
annotation|@
name|Override
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
return|return
name|canMove
argument_list|(
name|shardRouting
argument_list|,
name|allocation
argument_list|)
return|;
block|}
DECL|method|canMove
specifier|private
name|Decision
name|canMove
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
if|if
condition|(
operator|!
name|enableRelocation
operator|&&
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
comment|// Only primary shards are snapshotted
name|SnapshotsInProgress
name|snapshotsInProgress
init|=
name|allocation
operator|.
name|custom
argument_list|(
name|SnapshotsInProgress
operator|.
name|TYPE
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshotsInProgress
operator|==
literal|null
condition|)
block|{
comment|// Snapshots are not running
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
literal|"no snapshots are currently running"
argument_list|)
return|;
block|}
for|for
control|(
name|SnapshotsInProgress
operator|.
name|Entry
name|snapshot
range|:
name|snapshotsInProgress
operator|.
name|entries
argument_list|()
control|)
block|{
name|SnapshotsInProgress
operator|.
name|ShardSnapshotStatus
name|shardSnapshotStatus
init|=
name|snapshot
operator|.
name|shards
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardSnapshotStatus
operator|!=
literal|null
operator|&&
operator|!
name|shardSnapshotStatus
operator|.
name|state
argument_list|()
operator|.
name|completed
argument_list|()
operator|&&
name|shardSnapshotStatus
operator|.
name|nodeId
argument_list|()
operator|!=
literal|null
operator|&&
name|shardSnapshotStatus
operator|.
name|nodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
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
literal|"Preventing snapshotted shard [{}] to be moved from node [{}]"
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|shardSnapshotStatus
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
literal|"snapshot for shard [%s] is currently running on node [%s]"
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|shardSnapshotStatus
operator|.
name|nodeId
argument_list|()
argument_list|)
return|;
block|}
block|}
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
literal|"the shard is not primary or relocation is disabled"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

