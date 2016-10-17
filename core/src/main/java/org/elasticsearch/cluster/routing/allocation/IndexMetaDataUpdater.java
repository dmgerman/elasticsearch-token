begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation
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
name|routing
operator|.
name|RecoverySource
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
name|RoutingChangesObserver
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
name|UnassignedInfo
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
name|util
operator|.
name|set
operator|.
name|Sets
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardId
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
name|Comparator
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
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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

begin_comment
comment|/**  * Observer that tracks changes made to RoutingNodes in order to update the primary terms and in-sync allocation ids in  * {@link IndexMetaData} once the allocation round has completed.  *  * Primary terms are updated on primary initialization or when an active primary fails.  *  * Allocation ids are added for shards that become active and removed for shards that stop being active.  */
end_comment

begin_class
DECL|class|IndexMetaDataUpdater
specifier|public
class|class
name|IndexMetaDataUpdater
extends|extends
name|RoutingChangesObserver
operator|.
name|AbstractRoutingChangesObserver
block|{
DECL|field|shardChanges
specifier|private
specifier|final
name|Map
argument_list|<
name|ShardId
argument_list|,
name|Updates
argument_list|>
name|shardChanges
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|shardInitialized
specifier|public
name|void
name|shardInitialized
parameter_list|(
name|ShardRouting
name|unassignedShard
parameter_list|,
name|ShardRouting
name|initializedShard
parameter_list|)
block|{
assert|assert
name|initializedShard
operator|.
name|isRelocationTarget
argument_list|()
operator|==
literal|false
operator|:
literal|"shardInitialized is not called on relocation target: "
operator|+
name|initializedShard
assert|;
if|if
condition|(
name|initializedShard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|increasePrimaryTerm
argument_list|(
name|initializedShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|Updates
name|updates
init|=
name|changes
argument_list|(
name|initializedShard
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
name|updates
operator|.
name|initializedPrimary
operator|==
literal|null
operator|:
literal|"Primary cannot be initialized more than once in same allocation round: "
operator|+
literal|"(previous: "
operator|+
name|updates
operator|.
name|initializedPrimary
operator|+
literal|", next: "
operator|+
name|initializedShard
operator|+
literal|")"
assert|;
name|updates
operator|.
name|initializedPrimary
operator|=
name|initializedShard
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|shardStarted
specifier|public
name|void
name|shardStarted
parameter_list|(
name|ShardRouting
name|initializingShard
parameter_list|,
name|ShardRouting
name|startedShard
parameter_list|)
block|{
name|addAllocationId
argument_list|(
name|startedShard
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|shardFailed
specifier|public
name|void
name|shardFailed
parameter_list|(
name|ShardRouting
name|failedShard
parameter_list|,
name|UnassignedInfo
name|unassignedInfo
parameter_list|)
block|{
if|if
condition|(
name|failedShard
operator|.
name|active
argument_list|()
operator|&&
name|unassignedInfo
operator|.
name|getReason
argument_list|()
operator|!=
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|NODE_LEFT
condition|)
block|{
name|removeAllocationId
argument_list|(
name|failedShard
argument_list|)
expr_stmt|;
if|if
condition|(
name|failedShard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|Updates
name|updates
init|=
name|changes
argument_list|(
name|failedShard
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|updates
operator|.
name|firstFailedPrimary
operator|==
literal|null
condition|)
block|{
comment|// more than one primary can be failed (because of batching, primary can be failed, replica promoted and then failed...)
name|updates
operator|.
name|firstFailedPrimary
operator|=
name|failedShard
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|failedShard
operator|.
name|active
argument_list|()
operator|&&
name|failedShard
operator|.
name|primary
argument_list|()
condition|)
block|{
name|increasePrimaryTerm
argument_list|(
name|failedShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|relocationCompleted
specifier|public
name|void
name|relocationCompleted
parameter_list|(
name|ShardRouting
name|removedRelocationSource
parameter_list|)
block|{
name|removeAllocationId
argument_list|(
name|removedRelocationSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|startedPrimaryReinitialized
specifier|public
name|void
name|startedPrimaryReinitialized
parameter_list|(
name|ShardRouting
name|startedPrimaryShard
parameter_list|,
name|ShardRouting
name|initializedShard
parameter_list|)
block|{
name|removeAllocationId
argument_list|(
name|startedPrimaryShard
argument_list|)
expr_stmt|;
block|}
comment|/**      * Updates the current {@link MetaData} based on the changes of this RoutingChangesObserver. Specifically      * we update {@link IndexMetaData#getInSyncAllocationIds()} and {@link IndexMetaData#primaryTerm(int)} based on      * the changes made during this allocation.      *      * @param oldMetaData {@link MetaData} object from before the routing nodes was changed.      * @param newRoutingTable {@link RoutingTable} object after routing changes were applied.      * @return adapted {@link MetaData}, potentially the original one if no change was needed.      */
DECL|method|applyChanges
specifier|public
name|MetaData
name|applyChanges
parameter_list|(
name|MetaData
name|oldMetaData
parameter_list|,
name|RoutingTable
name|newRoutingTable
parameter_list|)
block|{
name|Map
argument_list|<
name|Index
argument_list|,
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|Updates
argument_list|>
argument_list|>
argument_list|>
name|changesGroupedByIndex
init|=
name|shardChanges
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|groupingBy
argument_list|(
name|e
lambda|->
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|MetaData
operator|.
name|Builder
name|metaDataBuilder
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Index
argument_list|,
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|Updates
argument_list|>
argument_list|>
argument_list|>
name|indexChanges
range|:
name|changesGroupedByIndex
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Index
name|index
init|=
name|indexChanges
operator|.
name|getKey
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|oldIndexMetaData
init|=
name|oldMetaData
operator|.
name|getIndexSafe
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|IndexMetaData
operator|.
name|Builder
name|indexMetaDataBuilder
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|Updates
argument_list|>
name|shardEntry
range|:
name|indexChanges
operator|.
name|getValue
argument_list|()
control|)
block|{
name|ShardId
name|shardId
init|=
name|shardEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Updates
name|updates
init|=
name|shardEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|indexMetaDataBuilder
operator|=
name|updateInSyncAllocations
argument_list|(
name|newRoutingTable
argument_list|,
name|oldIndexMetaData
argument_list|,
name|indexMetaDataBuilder
argument_list|,
name|shardId
argument_list|,
name|updates
argument_list|)
expr_stmt|;
name|indexMetaDataBuilder
operator|=
name|updatePrimaryTerm
argument_list|(
name|oldIndexMetaData
argument_list|,
name|indexMetaDataBuilder
argument_list|,
name|shardId
argument_list|,
name|updates
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexMetaDataBuilder
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|metaDataBuilder
operator|==
literal|null
condition|)
block|{
name|metaDataBuilder
operator|=
name|MetaData
operator|.
name|builder
argument_list|(
name|oldMetaData
argument_list|)
expr_stmt|;
block|}
name|metaDataBuilder
operator|.
name|put
argument_list|(
name|indexMetaDataBuilder
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|metaDataBuilder
operator|!=
literal|null
condition|)
block|{
return|return
name|metaDataBuilder
operator|.
name|build
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|oldMetaData
return|;
block|}
block|}
comment|/**      * Updates in-sync allocations with routing changes that were made to the routing table.      */
DECL|method|updateInSyncAllocations
specifier|private
name|IndexMetaData
operator|.
name|Builder
name|updateInSyncAllocations
parameter_list|(
name|RoutingTable
name|newRoutingTable
parameter_list|,
name|IndexMetaData
name|oldIndexMetaData
parameter_list|,
name|IndexMetaData
operator|.
name|Builder
name|indexMetaDataBuilder
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|Updates
name|updates
parameter_list|)
block|{
assert|assert
name|Sets
operator|.
name|haveEmptyIntersection
argument_list|(
name|updates
operator|.
name|addedAllocationIds
argument_list|,
name|updates
operator|.
name|removedAllocationIds
argument_list|)
operator|:
literal|"allocation ids cannot be both added and removed in the same allocation round, added ids: "
operator|+
name|updates
operator|.
name|addedAllocationIds
operator|+
literal|", removed ids: "
operator|+
name|updates
operator|.
name|removedAllocationIds
assert|;
name|Set
argument_list|<
name|String
argument_list|>
name|oldInSyncAllocationIds
init|=
name|oldIndexMetaData
operator|.
name|inSyncAllocationIds
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
comment|// check if we have been force-initializing an empty primary or a stale primary
if|if
condition|(
name|updates
operator|.
name|initializedPrimary
operator|!=
literal|null
operator|&&
name|oldInSyncAllocationIds
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
operator|&&
name|oldInSyncAllocationIds
operator|.
name|contains
argument_list|(
name|updates
operator|.
name|initializedPrimary
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// we're not reusing an existing in-sync allocation id to initialize a primary, which means that we're either force-allocating
comment|// an empty or a stale primary (see AllocateEmptyPrimaryAllocationCommand or AllocateStalePrimaryAllocationCommand).
name|RecoverySource
operator|.
name|Type
name|recoverySourceType
init|=
name|updates
operator|.
name|initializedPrimary
operator|.
name|recoverySource
argument_list|()
operator|.
name|getType
argument_list|()
decl_stmt|;
name|boolean
name|emptyPrimary
init|=
name|recoverySourceType
operator|==
name|RecoverySource
operator|.
name|Type
operator|.
name|EMPTY_STORE
decl_stmt|;
assert|assert
name|updates
operator|.
name|addedAllocationIds
operator|.
name|isEmpty
argument_list|()
operator|:
operator|(
name|emptyPrimary
condition|?
literal|"empty"
else|:
literal|"stale"
operator|)
operator|+
literal|" primary is not force-initialized in same allocation round where shards are started"
assert|;
if|if
condition|(
name|indexMetaDataBuilder
operator|==
literal|null
condition|)
block|{
name|indexMetaDataBuilder
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|oldIndexMetaData
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|emptyPrimary
condition|)
block|{
comment|// forcing an empty primary resets the in-sync allocations to the empty set (ShardRouting.allocatedPostIndexCreate)
name|indexMetaDataBuilder
operator|.
name|putInSyncAllocationIds
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// forcing a stale primary resets the in-sync allocations to the singleton set with the stale id
name|indexMetaDataBuilder
operator|.
name|putInSyncAllocationIds
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|updates
operator|.
name|initializedPrimary
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// standard path for updating in-sync ids
name|Set
argument_list|<
name|String
argument_list|>
name|inSyncAllocationIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|oldInSyncAllocationIds
argument_list|)
decl_stmt|;
name|inSyncAllocationIds
operator|.
name|addAll
argument_list|(
name|updates
operator|.
name|addedAllocationIds
argument_list|)
expr_stmt|;
name|inSyncAllocationIds
operator|.
name|removeAll
argument_list|(
name|updates
operator|.
name|removedAllocationIds
argument_list|)
expr_stmt|;
comment|// Prevent set of inSyncAllocationIds to grow unboundedly. This can happen for example if we don't write to a primary
comment|// but repeatedly shut down nodes that have active replicas.
comment|// We use number_of_replicas + 1 (= possible active shard copies) to bound the inSyncAllocationIds set
name|int
name|maxActiveShards
init|=
name|oldIndexMetaData
operator|.
name|getNumberOfReplicas
argument_list|()
operator|+
literal|1
decl_stmt|;
comment|// +1 for the primary
if|if
condition|(
name|inSyncAllocationIds
operator|.
name|size
argument_list|()
operator|>
name|maxActiveShards
condition|)
block|{
comment|// trim entries that have no corresponding shard routing in the cluster state (i.e. trim unavailable copies)
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|assignedShards
init|=
name|newRoutingTable
operator|.
name|shardRoutingTable
argument_list|(
name|shardId
argument_list|)
operator|.
name|assignedShards
argument_list|()
decl_stmt|;
assert|assert
name|assignedShards
operator|.
name|size
argument_list|()
operator|<=
name|maxActiveShards
operator|:
literal|"cannot have more assigned shards "
operator|+
name|assignedShards
operator|+
literal|" than maximum possible active shards "
operator|+
name|maxActiveShards
assert|;
name|Set
argument_list|<
name|String
argument_list|>
name|assignedAllocations
init|=
name|assignedShards
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|s
lambda|->
name|s
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
decl_stmt|;
name|inSyncAllocationIds
operator|=
name|inSyncAllocationIds
operator|.
name|stream
argument_list|()
operator|.
name|sorted
argument_list|(
name|Comparator
operator|.
name|comparing
argument_list|(
name|assignedAllocations
operator|::
name|contains
argument_list|)
operator|.
name|reversed
argument_list|()
argument_list|)
comment|// values with routing entries first
operator|.
name|limit
argument_list|(
name|maxActiveShards
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// only update in-sync allocation ids if there is at least one entry remaining. Assume for example that there only
comment|// ever was a primary active and now it failed. If we were to remove the allocation id from the in-sync set, this would
comment|// create an empty primary on the next allocation (see ShardRouting#allocatedPostIndexCreate)
if|if
condition|(
name|inSyncAllocationIds
operator|.
name|isEmpty
argument_list|()
operator|&&
name|oldInSyncAllocationIds
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
assert|assert
name|updates
operator|.
name|firstFailedPrimary
operator|!=
literal|null
operator|:
literal|"in-sync set became empty but active primary wasn't failed: "
operator|+
name|oldInSyncAllocationIds
assert|;
if|if
condition|(
name|updates
operator|.
name|firstFailedPrimary
operator|!=
literal|null
condition|)
block|{
comment|// add back allocation id of failed primary
name|inSyncAllocationIds
operator|.
name|add
argument_list|(
name|updates
operator|.
name|firstFailedPrimary
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
assert|assert
name|inSyncAllocationIds
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
operator|||
name|oldInSyncAllocationIds
operator|.
name|isEmpty
argument_list|()
operator|:
literal|"in-sync allocations cannot become empty after they have been non-empty: "
operator|+
name|oldInSyncAllocationIds
assert|;
comment|// be extra safe here and only update in-sync set if it is non-empty
if|if
condition|(
name|inSyncAllocationIds
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|indexMetaDataBuilder
operator|==
literal|null
condition|)
block|{
name|indexMetaDataBuilder
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|oldIndexMetaData
argument_list|)
expr_stmt|;
block|}
name|indexMetaDataBuilder
operator|.
name|putInSyncAllocationIds
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|inSyncAllocationIds
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|indexMetaDataBuilder
return|;
block|}
comment|/**      * Removes allocation ids from the in-sync set for shard copies for which there is no routing entries in the routing table.      * This method is called in AllocationService before any changes to the routing table are made.      */
DECL|method|removeStaleIdsWithoutRoutings
specifier|public
specifier|static
name|ClusterState
name|removeStaleIdsWithoutRoutings
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|List
argument_list|<
name|StaleShard
argument_list|>
name|staleShards
parameter_list|)
block|{
name|MetaData
name|oldMetaData
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
decl_stmt|;
name|RoutingTable
name|oldRoutingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|MetaData
operator|.
name|Builder
name|metaDataBuilder
init|=
literal|null
decl_stmt|;
comment|// group staleShards entries by index
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Index
argument_list|,
name|List
argument_list|<
name|StaleShard
argument_list|>
argument_list|>
name|indexEntry
range|:
name|staleShards
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|groupingBy
argument_list|(
name|fs
lambda|->
name|fs
operator|.
name|getShardId
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|)
argument_list|)
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|final
name|IndexMetaData
name|oldIndexMetaData
operator|=
name|oldMetaData
operator|.
name|getIndexSafe
argument_list|(
name|indexEntry
operator|.
name|getKey
argument_list|()
argument_list|)
block|;
name|IndexMetaData
operator|.
name|Builder
name|indexMetaDataBuilder
operator|=
literal|null
block|;
comment|// group staleShards entries by shard id
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|List
argument_list|<
name|StaleShard
argument_list|>
argument_list|>
name|shardEntry
range|:
name|indexEntry
operator|.
name|getValue
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|groupingBy
argument_list|(
name|staleShard
lambda|->
name|staleShard
operator|.
name|getShardId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|shardNumber
operator|=
name|shardEntry
operator|.
name|getKey
argument_list|()
operator|.
name|getId
argument_list|()
block|;
name|Set
argument_list|<
name|String
argument_list|>
name|oldInSyncAllocations
operator|=
name|oldIndexMetaData
operator|.
name|inSyncAllocationIds
argument_list|(
name|shardNumber
argument_list|)
block|;
name|Set
argument_list|<
name|String
argument_list|>
name|idsToRemove
operator|=
name|shardEntry
operator|.
name|getValue
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|e
lambda|->
name|e
operator|.
name|getAllocationId
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
block|;
assert|assert
name|idsToRemove
operator|.
name|stream
argument_list|()
operator|.
name|allMatch
argument_list|(
name|id
lambda|->
name|oldRoutingTable
operator|.
name|getByAllocationId
argument_list|(
name|shardEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|id
argument_list|)
operator|==
literal|null
argument_list|)
operator|:
literal|"removing stale ids: "
operator|+
name|idsToRemove
operator|+
literal|", some of which have still a routing entry: "
operator|+
name|oldRoutingTable
operator|.
name|prettyPrint
argument_list|()
assert|;
name|Set
argument_list|<
name|String
argument_list|>
name|remainingInSyncAllocations
operator|=
name|Sets
operator|.
name|difference
argument_list|(
name|oldInSyncAllocations
argument_list|,
name|idsToRemove
argument_list|)
empty_stmt|;
assert|assert
name|remainingInSyncAllocations
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
operator|:
literal|"Set of in-sync ids cannot become empty for shard "
operator|+
name|shardEntry
operator|.
name|getKey
argument_list|()
operator|+
literal|" (before: "
operator|+
name|oldInSyncAllocations
operator|+
literal|", ids to remove: "
operator|+
name|idsToRemove
operator|+
literal|")"
assert|;
comment|// be extra safe here: if the in-sync set were to become empty, this would create an empty primary on the next allocation
comment|// (see ShardRouting#allocatedPostIndexCreate)
if|if
condition|(
name|remainingInSyncAllocations
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|indexMetaDataBuilder
operator|==
literal|null
condition|)
block|{
name|indexMetaDataBuilder
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|oldIndexMetaData
argument_list|)
expr_stmt|;
block|}
name|indexMetaDataBuilder
operator|.
name|putInSyncAllocationIds
argument_list|(
name|shardNumber
argument_list|,
name|remainingInSyncAllocations
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|indexMetaDataBuilder
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|metaDataBuilder
operator|==
literal|null
condition|)
block|{
name|metaDataBuilder
operator|=
name|MetaData
operator|.
name|builder
argument_list|(
name|oldMetaData
argument_list|)
expr_stmt|;
block|}
name|metaDataBuilder
operator|.
name|put
argument_list|(
name|indexMetaDataBuilder
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_if
if|if
condition|(
name|metaDataBuilder
operator|!=
literal|null
condition|)
block|{
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaDataBuilder
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|clusterState
return|;
block|}
end_if

begin_comment
unit|}
comment|/**      * Increases the primary term if {@link #increasePrimaryTerm} was called for this shard id.      */
end_comment

begin_function
DECL|method|updatePrimaryTerm
unit|private
name|IndexMetaData
operator|.
name|Builder
name|updatePrimaryTerm
parameter_list|(
name|IndexMetaData
name|oldIndexMetaData
parameter_list|,
name|IndexMetaData
operator|.
name|Builder
name|indexMetaDataBuilder
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|Updates
name|updates
parameter_list|)
block|{
if|if
condition|(
name|updates
operator|.
name|increaseTerm
condition|)
block|{
if|if
condition|(
name|indexMetaDataBuilder
operator|==
literal|null
condition|)
block|{
name|indexMetaDataBuilder
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|oldIndexMetaData
argument_list|)
expr_stmt|;
block|}
name|indexMetaDataBuilder
operator|.
name|primaryTerm
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|oldIndexMetaData
operator|.
name|primaryTerm
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|indexMetaDataBuilder
return|;
block|}
end_function

begin_comment
comment|/**      * Helper method that creates update entry for the given shard id if such an entry does not exist yet.      */
end_comment

begin_function
DECL|method|changes
specifier|private
name|Updates
name|changes
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
return|return
name|shardChanges
operator|.
name|computeIfAbsent
argument_list|(
name|shardId
argument_list|,
name|k
lambda|->
operator|new
name|Updates
argument_list|()
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**      * Remove allocation id of this shard from the set of in-sync shard copies      */
end_comment

begin_function
DECL|method|removeAllocationId
specifier|private
name|void
name|removeAllocationId
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|)
block|{
name|changes
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
operator|.
name|removedAllocationIds
operator|.
name|add
argument_list|(
name|shardRouting
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_function

begin_comment
comment|/**      * Add allocation id of this shard to the set of in-sync shard copies      */
end_comment

begin_function
DECL|method|addAllocationId
specifier|private
name|void
name|addAllocationId
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|)
block|{
name|changes
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
operator|.
name|addedAllocationIds
operator|.
name|add
argument_list|(
name|shardRouting
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_function

begin_comment
comment|/**      * Increase primary term for this shard id      */
end_comment

begin_function
DECL|method|increasePrimaryTerm
specifier|private
name|void
name|increasePrimaryTerm
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|changes
argument_list|(
name|shardId
argument_list|)
operator|.
name|increaseTerm
operator|=
literal|true
expr_stmt|;
block|}
end_function

begin_class
DECL|class|Updates
specifier|private
specifier|static
class|class
name|Updates
block|{
DECL|field|increaseTerm
specifier|private
name|boolean
name|increaseTerm
decl_stmt|;
comment|// whether primary term should be increased
DECL|field|addedAllocationIds
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|addedAllocationIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|// allocation ids that should be added to the in-sync set
DECL|field|removedAllocationIds
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|removedAllocationIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|// allocation ids that should be removed from the in-sync set
DECL|field|initializedPrimary
specifier|private
name|ShardRouting
name|initializedPrimary
init|=
literal|null
decl_stmt|;
comment|// primary that was initialized from unassigned
DECL|field|firstFailedPrimary
specifier|private
name|ShardRouting
name|firstFailedPrimary
init|=
literal|null
decl_stmt|;
comment|// first active primary that was failed
block|}
end_class

unit|}
end_unit
