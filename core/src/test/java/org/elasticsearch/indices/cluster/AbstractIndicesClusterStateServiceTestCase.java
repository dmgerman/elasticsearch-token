begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cluster
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
name|common
operator|.
name|Nullable
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
name|common
operator|.
name|util
operator|.
name|Callback
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
name|IndexService
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
name|IndexSettings
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
name|NodeServicesProvider
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
name|IndexEventListener
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
name|IndexShard
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
name|IndexShardState
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
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cluster
operator|.
name|IndicesClusterStateService
operator|.
name|AllocatedIndex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cluster
operator|.
name|IndicesClusterStateService
operator|.
name|AllocatedIndices
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cluster
operator|.
name|IndicesClusterStateService
operator|.
name|Shard
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
operator|.
name|PeerRecoveryTargetService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
operator|.
name|RecoveryState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|RepositoriesService
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
name|junit
operator|.
name|Before
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|MapBuilder
operator|.
name|newMapBuilder
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
name|empty
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
name|greaterThanOrEqualTo
import|;
end_import

begin_comment
comment|/**  * Abstract base class for tests against {@link IndicesClusterStateService}  */
end_comment

begin_class
DECL|class|AbstractIndicesClusterStateServiceTestCase
specifier|public
specifier|abstract
class|class
name|AbstractIndicesClusterStateServiceTestCase
extends|extends
name|ESTestCase
block|{
DECL|field|enableRandomFailures
specifier|private
name|boolean
name|enableRandomFailures
decl_stmt|;
annotation|@
name|Before
DECL|method|injectRandomFailures
specifier|public
name|void
name|injectRandomFailures
parameter_list|()
block|{
name|enableRandomFailures
operator|=
name|randomBoolean
argument_list|()
expr_stmt|;
block|}
DECL|method|failRandomly
specifier|protected
name|void
name|failRandomly
parameter_list|()
block|{
if|if
condition|(
name|enableRandomFailures
operator|&&
name|rarely
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"dummy test failure"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Checks if cluster state matches internal state of IndicesClusterStateService instance      *      * @param state cluster state used for matching      */
DECL|method|assertClusterStateMatchesNodeState
specifier|public
name|void
name|assertClusterStateMatchesNodeState
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|IndicesClusterStateService
name|indicesClusterStateService
parameter_list|)
block|{
name|AllocatedIndices
argument_list|<
name|?
extends|extends
name|Shard
argument_list|,
name|?
extends|extends
name|AllocatedIndex
argument_list|<
name|?
extends|extends
name|Shard
argument_list|>
argument_list|>
name|indicesService
init|=
name|indicesClusterStateService
operator|.
name|indicesService
decl_stmt|;
name|ConcurrentMap
argument_list|<
name|ShardId
argument_list|,
name|ShardRouting
argument_list|>
name|failedShardsCache
init|=
name|indicesClusterStateService
operator|.
name|failedShardsCache
decl_stmt|;
name|RoutingNode
name|localRoutingNode
init|=
name|state
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|state
operator|.
name|getNodes
argument_list|()
operator|.
name|getLocalNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|localRoutingNode
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|enableRandomFailures
operator|==
literal|false
condition|)
block|{
name|assertThat
argument_list|(
literal|"failed shard cache should be empty"
argument_list|,
name|failedShardsCache
operator|.
name|values
argument_list|()
argument_list|,
name|empty
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// check that all shards in local routing nodes have been allocated
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|localRoutingNode
control|)
block|{
name|Index
name|index
init|=
name|shardRouting
operator|.
name|index
argument_list|()
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|getIndexSafe
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|Shard
name|shard
init|=
name|indicesService
operator|.
name|getShardOrNull
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
name|ShardRouting
name|failedShard
init|=
name|failedShardsCache
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
name|enableRandomFailures
condition|)
block|{
if|if
condition|(
name|shard
operator|==
literal|null
operator|&&
name|failedShard
operator|==
literal|null
condition|)
block|{
name|fail
argument_list|(
literal|"Shard with id "
operator|+
name|shardRouting
operator|+
literal|" expected but missing in indicesService and failedShardsCache"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|failedShard
operator|!=
literal|null
operator|&&
name|failedShard
operator|.
name|isSameAllocation
argument_list|(
name|shardRouting
argument_list|)
operator|==
literal|false
condition|)
block|{
name|fail
argument_list|(
literal|"Shard cache has not been properly cleaned for "
operator|+
name|failedShard
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|shard
operator|==
literal|null
condition|)
block|{
name|fail
argument_list|(
literal|"Shard with id "
operator|+
name|shardRouting
operator|+
literal|" expected but missing in indicesService"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|AllocatedIndex
argument_list|<
name|?
extends|extends
name|Shard
argument_list|>
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Index "
operator|+
name|index
operator|+
literal|" expected but missing in indicesService"
argument_list|,
name|indexService
operator|!=
literal|null
argument_list|)
expr_stmt|;
comment|// index metadata has been updated
name|assertThat
argument_list|(
name|indexService
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|getIndexMetaData
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|indexMetaData
argument_list|)
argument_list|)
expr_stmt|;
comment|// shard has been created
if|if
condition|(
name|enableRandomFailures
operator|==
literal|false
operator|||
name|failedShard
operator|==
literal|null
condition|)
block|{
name|assertTrue
argument_list|(
literal|"Shard with id "
operator|+
name|shardRouting
operator|+
literal|" expected but missing in indexService"
argument_list|,
name|shard
operator|!=
literal|null
argument_list|)
expr_stmt|;
comment|// shard has latest shard routing
name|assertThat
argument_list|(
name|shard
operator|.
name|routingEntry
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|shardRouting
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// all other shards / indices have been cleaned up
for|for
control|(
name|AllocatedIndex
argument_list|<
name|?
extends|extends
name|Shard
argument_list|>
name|indexService
range|:
name|indicesService
control|)
block|{
name|assertTrue
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|getIndexSafe
argument_list|(
name|indexService
operator|.
name|index
argument_list|()
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|boolean
name|shardsFound
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Shard
name|shard
range|:
name|indexService
control|)
block|{
name|shardsFound
operator|=
literal|true
expr_stmt|;
name|ShardRouting
name|persistedShardRouting
init|=
name|shard
operator|.
name|routingEntry
argument_list|()
decl_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|localRoutingNode
operator|.
name|getByShardId
argument_list|(
name|persistedShardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|==
literal|null
condition|)
block|{
name|fail
argument_list|(
literal|"Shard with id "
operator|+
name|persistedShardRouting
operator|+
literal|" locally exists but missing in routing table"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shardRouting
operator|.
name|equals
argument_list|(
name|persistedShardRouting
argument_list|)
operator|==
literal|false
condition|)
block|{
name|fail
argument_list|(
literal|"Local shard "
operator|+
name|persistedShardRouting
operator|+
literal|" has stale routing"
operator|+
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|shardsFound
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|enableRandomFailures
condition|)
block|{
comment|// check if we have shards of that index in failedShardsCache
comment|// if yes, we might not have cleaned the index as failedShardsCache can be populated by another thread
name|assertFalse
argument_list|(
name|failedShardsCache
operator|.
name|keySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|noneMatch
argument_list|(
name|shardId
lambda|->
name|shardId
operator|.
name|getIndex
argument_list|()
operator|.
name|equals
argument_list|(
name|indexService
operator|.
name|index
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
literal|"index service for index "
operator|+
name|indexService
operator|.
name|index
argument_list|()
operator|+
literal|" has no shards"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * Mock for {@link IndicesService}      */
DECL|class|MockIndicesService
specifier|protected
class|class
name|MockIndicesService
implements|implements
name|AllocatedIndices
argument_list|<
name|MockIndexShard
argument_list|,
name|MockIndexService
argument_list|>
block|{
DECL|field|indices
specifier|private
specifier|volatile
name|Map
argument_list|<
name|String
argument_list|,
name|MockIndexService
argument_list|>
name|indices
init|=
name|emptyMap
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|createIndex
specifier|public
specifier|synchronized
name|MockIndexService
name|createIndex
parameter_list|(
name|NodeServicesProvider
name|nodeServicesProvider
parameter_list|,
name|IndexMetaData
name|indexMetaData
parameter_list|,
name|List
argument_list|<
name|IndexEventListener
argument_list|>
name|buildInIndexListener
parameter_list|)
throws|throws
name|IOException
block|{
name|MockIndexService
name|indexService
init|=
operator|new
name|MockIndexService
argument_list|(
operator|new
name|IndexSettings
argument_list|(
name|indexMetaData
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
decl_stmt|;
name|indices
operator|=
name|newMapBuilder
argument_list|(
name|indices
argument_list|)
operator|.
name|put
argument_list|(
name|indexMetaData
operator|.
name|getIndexUUID
argument_list|()
argument_list|,
name|indexService
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
return|return
name|indexService
return|;
block|}
annotation|@
name|Override
DECL|method|verifyIndexIsDeleted
specifier|public
name|IndexMetaData
name|verifyIndexIsDeleted
parameter_list|(
name|Index
name|index
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|deleteUnassignedIndex
specifier|public
name|void
name|deleteUnassignedIndex
parameter_list|(
name|String
name|reason
parameter_list|,
name|IndexMetaData
name|metaData
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{          }
annotation|@
name|Override
DECL|method|deleteIndex
specifier|public
specifier|synchronized
name|void
name|deleteIndex
parameter_list|(
name|Index
name|index
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|hasIndex
argument_list|(
name|index
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|MockIndexService
argument_list|>
name|newIndices
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|indices
argument_list|)
decl_stmt|;
name|newIndices
operator|.
name|remove
argument_list|(
name|index
operator|.
name|getUUID
argument_list|()
argument_list|)
expr_stmt|;
name|indices
operator|=
name|unmodifiableMap
argument_list|(
name|newIndices
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|removeIndex
specifier|public
specifier|synchronized
name|void
name|removeIndex
parameter_list|(
name|Index
name|index
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|hasIndex
argument_list|(
name|index
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|MockIndexService
argument_list|>
name|newIndices
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|indices
argument_list|)
decl_stmt|;
name|newIndices
operator|.
name|remove
argument_list|(
name|index
operator|.
name|getUUID
argument_list|()
argument_list|)
expr_stmt|;
name|indices
operator|=
name|unmodifiableMap
argument_list|(
name|newIndices
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Nullable
DECL|method|indexService
specifier|public
name|MockIndexService
name|indexService
parameter_list|(
name|Index
name|index
parameter_list|)
block|{
return|return
name|indices
operator|.
name|get
argument_list|(
name|index
operator|.
name|getUUID
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createShard
specifier|public
name|MockIndexShard
name|createShard
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RecoveryState
name|recoveryState
parameter_list|,
name|PeerRecoveryTargetService
name|recoveryTargetService
parameter_list|,
name|PeerRecoveryTargetService
operator|.
name|RecoveryListener
name|recoveryListener
parameter_list|,
name|RepositoriesService
name|repositoriesService
parameter_list|,
name|NodeServicesProvider
name|nodeServicesProvider
parameter_list|,
name|Callback
argument_list|<
name|IndexShard
operator|.
name|ShardFailure
argument_list|>
name|onShardFailure
parameter_list|)
throws|throws
name|IOException
block|{
name|failRandomly
argument_list|()
expr_stmt|;
name|MockIndexService
name|indexService
init|=
name|indexService
argument_list|(
name|recoveryState
operator|.
name|getShardId
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
name|MockIndexShard
name|indexShard
init|=
name|indexService
operator|.
name|createShard
argument_list|(
name|shardRouting
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|recoveryState
operator|=
name|recoveryState
expr_stmt|;
return|return
name|indexShard
return|;
block|}
annotation|@
name|Override
DECL|method|processPendingDeletes
specifier|public
name|void
name|processPendingDeletes
parameter_list|(
name|Index
name|index
parameter_list|,
name|IndexSettings
name|indexSettings
parameter_list|,
name|TimeValue
name|timeValue
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{          }
DECL|method|hasIndex
specifier|private
name|boolean
name|hasIndex
parameter_list|(
name|Index
name|index
parameter_list|)
block|{
return|return
name|indices
operator|.
name|containsKey
argument_list|(
name|index
operator|.
name|getUUID
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|MockIndexService
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|indices
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
comment|/**      * Mock for {@link IndexService}      */
DECL|class|MockIndexService
specifier|protected
class|class
name|MockIndexService
implements|implements
name|AllocatedIndex
argument_list|<
name|MockIndexShard
argument_list|>
block|{
DECL|field|shards
specifier|private
specifier|volatile
name|Map
argument_list|<
name|Integer
argument_list|,
name|MockIndexShard
argument_list|>
name|shards
init|=
name|emptyMap
argument_list|()
decl_stmt|;
DECL|field|indexSettings
specifier|private
specifier|final
name|IndexSettings
name|indexSettings
decl_stmt|;
DECL|method|MockIndexService
specifier|public
name|MockIndexService
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|)
block|{
name|this
operator|.
name|indexSettings
operator|=
name|indexSettings
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getIndexSettings
specifier|public
name|IndexSettings
name|getIndexSettings
parameter_list|()
block|{
return|return
name|indexSettings
return|;
block|}
annotation|@
name|Override
DECL|method|updateMapping
specifier|public
name|boolean
name|updateMapping
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
throws|throws
name|IOException
block|{
name|failRandomly
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|updateMetaData
specifier|public
name|void
name|updateMetaData
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
name|indexSettings
operator|.
name|updateIndexMetaData
argument_list|(
name|indexMetaData
argument_list|)
expr_stmt|;
for|for
control|(
name|MockIndexShard
name|shard
range|:
name|shards
operator|.
name|values
argument_list|()
control|)
block|{
name|shard
operator|.
name|updateTerm
argument_list|(
name|indexMetaData
operator|.
name|primaryTerm
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getShardOrNull
specifier|public
name|MockIndexShard
name|getShardOrNull
parameter_list|(
name|int
name|shardId
parameter_list|)
block|{
return|return
name|shards
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
return|;
block|}
DECL|method|createShard
specifier|public
specifier|synchronized
name|MockIndexShard
name|createShard
parameter_list|(
name|ShardRouting
name|routing
parameter_list|)
throws|throws
name|IOException
block|{
name|failRandomly
argument_list|()
expr_stmt|;
name|MockIndexShard
name|shard
init|=
operator|new
name|MockIndexShard
argument_list|(
name|routing
argument_list|,
name|indexSettings
operator|.
name|getIndexMetaData
argument_list|()
operator|.
name|primaryTerm
argument_list|(
name|routing
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|shards
operator|=
name|newMapBuilder
argument_list|(
name|shards
argument_list|)
operator|.
name|put
argument_list|(
name|routing
operator|.
name|id
argument_list|()
argument_list|,
name|shard
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
return|return
name|shard
return|;
block|}
annotation|@
name|Override
DECL|method|removeShard
specifier|public
specifier|synchronized
name|void
name|removeShard
parameter_list|(
name|int
name|shardId
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|shards
operator|.
name|containsKey
argument_list|(
name|shardId
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return;
block|}
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|MockIndexShard
argument_list|>
name|newShards
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|shards
argument_list|)
decl_stmt|;
name|MockIndexShard
name|indexShard
init|=
name|newShards
operator|.
name|remove
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
assert|assert
name|indexShard
operator|!=
literal|null
assert|;
name|shards
operator|=
name|unmodifiableMap
argument_list|(
name|newShards
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|MockIndexShard
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|shards
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|index
specifier|public
name|Index
name|index
parameter_list|()
block|{
return|return
name|indexSettings
operator|.
name|getIndex
argument_list|()
return|;
block|}
block|}
comment|/**      * Mock for {@link IndexShard}      */
DECL|class|MockIndexShard
specifier|protected
class|class
name|MockIndexShard
implements|implements
name|IndicesClusterStateService
operator|.
name|Shard
block|{
DECL|field|shardRouting
specifier|private
specifier|volatile
name|ShardRouting
name|shardRouting
decl_stmt|;
DECL|field|recoveryState
specifier|private
specifier|volatile
name|RecoveryState
name|recoveryState
decl_stmt|;
DECL|field|term
specifier|private
specifier|volatile
name|long
name|term
decl_stmt|;
DECL|method|MockIndexShard
specifier|public
name|MockIndexShard
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|long
name|term
parameter_list|)
block|{
name|this
operator|.
name|shardRouting
operator|=
name|shardRouting
expr_stmt|;
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|shardRouting
operator|.
name|shardId
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|recoveryState
specifier|public
name|RecoveryState
name|recoveryState
parameter_list|()
block|{
return|return
name|recoveryState
return|;
block|}
annotation|@
name|Override
DECL|method|routingEntry
specifier|public
name|ShardRouting
name|routingEntry
parameter_list|()
block|{
return|return
name|shardRouting
return|;
block|}
annotation|@
name|Override
DECL|method|state
specifier|public
name|IndexShardState
name|state
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|updateRoutingEntry
specifier|public
name|void
name|updateRoutingEntry
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|)
throws|throws
name|IOException
block|{
name|failRandomly
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|this
operator|.
name|shardId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"current: "
operator|+
name|this
operator|.
name|shardRouting
operator|+
literal|", got: "
operator|+
name|shardRouting
argument_list|,
name|this
operator|.
name|shardRouting
operator|.
name|isSameAllocation
argument_list|(
name|shardRouting
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|shardRouting
operator|.
name|active
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
literal|"and active shard must stay active, current: "
operator|+
name|this
operator|.
name|shardRouting
operator|+
literal|", got: "
operator|+
name|shardRouting
argument_list|,
name|shardRouting
operator|.
name|active
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|shardRouting
operator|=
name|shardRouting
expr_stmt|;
block|}
DECL|method|updateTerm
specifier|public
name|void
name|updateTerm
parameter_list|(
name|long
name|newTerm
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"term can only be incremented: "
operator|+
name|shardRouting
argument_list|,
name|newTerm
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|term
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
operator|&&
name|shardRouting
operator|.
name|active
argument_list|()
condition|)
block|{
name|assertThat
argument_list|(
literal|"term can not be changed on an active primary shard: "
operator|+
name|shardRouting
argument_list|,
name|newTerm
argument_list|,
name|equalTo
argument_list|(
name|term
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|term
operator|=
name|newTerm
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

