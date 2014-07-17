begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|store
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|StoreRateLimiting
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
name|IndexShardRoutingTable
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
name|collect
operator|.
name|Tuple
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
name|component
operator|.
name|AbstractComponent
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
name|io
operator|.
name|FileSystemUtils
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|ImmutableSettings
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
name|ByteSizeUnit
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
name|env
operator|.
name|NodeEnvironment
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
name|service
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
name|index
operator|.
name|shard
operator|.
name|service
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|ArrayList
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|IndicesStore
specifier|public
class|class
name|IndicesStore
extends|extends
name|AbstractComponent
implements|implements
name|ClusterStateListener
block|{
DECL|field|INDICES_STORE_THROTTLE_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|INDICES_STORE_THROTTLE_TYPE
init|=
literal|"indices.store.throttle.type"
decl_stmt|;
DECL|field|INDICES_STORE_THROTTLE_MAX_BYTES_PER_SEC
specifier|public
specifier|static
specifier|final
name|String
name|INDICES_STORE_THROTTLE_MAX_BYTES_PER_SEC
init|=
literal|"indices.store.throttle.max_bytes_per_sec"
decl_stmt|;
DECL|field|ACTION_SHARD_EXISTS
specifier|private
specifier|static
specifier|final
name|String
name|ACTION_SHARD_EXISTS
init|=
literal|"index/shard/exists"
decl_stmt|;
DECL|field|ACTIVE_STATES
specifier|private
specifier|static
specifier|final
name|EnumSet
argument_list|<
name|IndexShardState
argument_list|>
name|ACTIVE_STATES
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|IndexShardState
operator|.
name|STARTED
argument_list|,
name|IndexShardState
operator|.
name|RELOCATED
argument_list|)
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
name|rateLimitingType
init|=
name|settings
operator|.
name|get
argument_list|(
name|INDICES_STORE_THROTTLE_TYPE
argument_list|,
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimitingType
argument_list|)
decl_stmt|;
comment|// try and parse the type
name|StoreRateLimiting
operator|.
name|Type
operator|.
name|fromString
argument_list|(
name|rateLimitingType
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|rateLimitingType
operator|.
name|equals
argument_list|(
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimitingType
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating indices.store.throttle.type from [{}] to [{}]"
argument_list|,
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimitingType
argument_list|,
name|rateLimitingType
argument_list|)
expr_stmt|;
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimitingType
operator|=
name|rateLimitingType
expr_stmt|;
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimiting
operator|.
name|setType
argument_list|(
name|rateLimitingType
argument_list|)
expr_stmt|;
block|}
name|ByteSizeValue
name|rateLimitingThrottle
init|=
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|INDICES_STORE_THROTTLE_MAX_BYTES_PER_SEC
argument_list|,
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimitingThrottle
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|rateLimitingThrottle
operator|.
name|equals
argument_list|(
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimitingThrottle
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating indices.store.throttle.max_bytes_per_sec from [{}] to [{}], note, type is [{}]"
argument_list|,
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimitingThrottle
argument_list|,
name|rateLimitingThrottle
argument_list|,
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimitingType
argument_list|)
expr_stmt|;
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimitingThrottle
operator|=
name|rateLimitingThrottle
expr_stmt|;
name|IndicesStore
operator|.
name|this
operator|.
name|rateLimiting
operator|.
name|setMaxRate
argument_list|(
name|rateLimitingThrottle
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|field|nodeEnv
specifier|private
specifier|final
name|NodeEnvironment
name|nodeEnv
decl_stmt|;
DECL|field|nodeSettingsService
specifier|private
specifier|final
name|NodeSettingsService
name|nodeSettingsService
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|rateLimitingType
specifier|private
specifier|volatile
name|String
name|rateLimitingType
decl_stmt|;
DECL|field|rateLimitingThrottle
specifier|private
specifier|volatile
name|ByteSizeValue
name|rateLimitingThrottle
decl_stmt|;
DECL|field|rateLimiting
specifier|private
specifier|final
name|StoreRateLimiting
name|rateLimiting
init|=
operator|new
name|StoreRateLimiting
argument_list|()
decl_stmt|;
DECL|field|applySettings
specifier|private
specifier|final
name|ApplySettings
name|applySettings
init|=
operator|new
name|ApplySettings
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndicesStore
specifier|public
name|IndicesStore
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NodeEnvironment
name|nodeEnv
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodeEnv
operator|=
name|nodeEnv
expr_stmt|;
name|this
operator|.
name|nodeSettingsService
operator|=
name|nodeSettingsService
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|ACTION_SHARD_EXISTS
argument_list|,
operator|new
name|ShardActiveRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
comment|// we limit with 20MB / sec by default with a default type set to merge sice 0.90.1
name|this
operator|.
name|rateLimitingType
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"throttle.type"
argument_list|,
name|StoreRateLimiting
operator|.
name|Type
operator|.
name|MERGE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|rateLimiting
operator|.
name|setType
argument_list|(
name|rateLimitingType
argument_list|)
expr_stmt|;
name|this
operator|.
name|rateLimitingThrottle
operator|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"throttle.max_bytes_per_sec"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|20
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
expr_stmt|;
name|rateLimiting
operator|.
name|setMaxRate
argument_list|(
name|rateLimitingThrottle
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using indices.store.throttle.type [{}], with index.store.throttle.max_bytes_per_sec [{}]"
argument_list|,
name|rateLimitingType
argument_list|,
name|rateLimitingThrottle
argument_list|)
expr_stmt|;
name|nodeSettingsService
operator|.
name|addListener
argument_list|(
name|applySettings
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|addLast
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|IndicesStore
name|IndicesStore
parameter_list|()
block|{
name|super
argument_list|(
name|ImmutableSettings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|nodeEnv
operator|=
literal|null
expr_stmt|;
name|nodeSettingsService
operator|=
literal|null
expr_stmt|;
name|indicesService
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|rateLimiting
specifier|public
name|StoreRateLimiting
name|rateLimiting
parameter_list|()
block|{
return|return
name|this
operator|.
name|rateLimiting
return|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|nodeSettingsService
operator|.
name|removeListener
argument_list|(
name|applySettings
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clusterChanged
specifier|public
name|void
name|clusterChanged
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
if|if
condition|(
operator|!
name|event
operator|.
name|routingTableChanged
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|blocks
argument_list|()
operator|.
name|disableStatePersistence
argument_list|()
condition|)
block|{
return|return;
block|}
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|event
operator|.
name|state
argument_list|()
operator|.
name|routingTable
argument_list|()
control|)
block|{
comment|// Note, closed indices will not have any routing information, so won't be deleted
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
if|if
condition|(
name|shardCanBeDeleted
argument_list|(
name|event
operator|.
name|state
argument_list|()
argument_list|,
name|indexShardRoutingTable
argument_list|)
condition|)
block|{
name|ShardId
name|shardId
init|=
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|nodeEnv
operator|.
name|hasNodeFile
argument_list|()
condition|)
block|{
name|File
index|[]
name|shardLocations
init|=
name|nodeEnv
operator|.
name|shardLocations
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|FileSystemUtils
operator|.
name|exists
argument_list|(
name|shardLocations
argument_list|)
condition|)
block|{
name|deleteShardIfExistElseWhere
argument_list|(
name|event
operator|.
name|state
argument_list|()
argument_list|,
name|indexShardRoutingTable
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|indexService
operator|.
name|hasShard
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|indexService
operator|.
name|store
argument_list|()
operator|.
name|canDeleteUnallocated
argument_list|(
name|shardId
argument_list|)
condition|)
block|{
name|deleteShardIfExistElseWhere
argument_list|(
name|event
operator|.
name|state
argument_list|()
argument_list|,
name|indexShardRoutingTable
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
block|}
DECL|method|shardCanBeDeleted
name|boolean
name|shardCanBeDeleted
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|IndexShardRoutingTable
name|indexShardRoutingTable
parameter_list|)
block|{
comment|// a shard can be deleted if all its copies are active, and its not allocated on this node
if|if
condition|(
name|indexShardRoutingTable
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// should not really happen, there should always be at least 1 (primary) shard in a
comment|// shard replication group, in any case, protected from deleting something by mistake
return|return
literal|false
return|;
block|}
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
comment|// be conservative here, check on started, not even active
if|if
condition|(
operator|!
name|shardRouting
operator|.
name|started
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// if the allocated or relocation node id doesn't exists in the cluster state  it may be a stale node,
comment|// make sure we don't do anything with this until the routing table has properly been rerouted to reflect
comment|// the fact that the node does not exists
name|DiscoveryNode
name|node
init|=
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// If all nodes have been upgraded to>= 1.3.0 at some point we get back here and have the chance to
comment|// run this api. (when cluster state is then updated)
if|if
condition|(
name|node
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_1_3_0
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Skip deleting deleting shard instance [{}], a node holding a shard instance is< 1.3.0"
argument_list|,
name|shardRouting
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|shardRouting
operator|.
name|relocatingNodeId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|node
operator|=
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|node
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_1_3_0
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Skip deleting deleting shard instance [{}], a node holding a shard instance is< 1.3.0"
argument_list|,
name|shardRouting
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
comment|// check if shard is active on the current node or is getting relocated to the our node
name|String
name|localNodeId
init|=
name|state
operator|.
name|getNodes
argument_list|()
operator|.
name|localNode
argument_list|()
operator|.
name|id
argument_list|()
decl_stmt|;
if|if
condition|(
name|localNodeId
operator|.
name|equals
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|||
name|localNodeId
operator|.
name|equals
argument_list|(
name|shardRouting
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
DECL|method|deleteShardIfExistElseWhere
specifier|private
name|void
name|deleteShardIfExistElseWhere
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|IndexShardRoutingTable
name|indexShardRoutingTable
parameter_list|)
block|{
name|List
argument_list|<
name|Tuple
argument_list|<
name|DiscoveryNode
argument_list|,
name|ShardActiveRequest
argument_list|>
argument_list|>
name|requests
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|indexShardRoutingTable
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|indexUUID
init|=
name|state
operator|.
name|getMetaData
argument_list|()
operator|.
name|index
argument_list|(
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|)
operator|.
name|getUUID
argument_list|()
decl_stmt|;
name|ClusterName
name|clusterName
init|=
name|state
operator|.
name|getClusterName
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
comment|// Node can't be null, because otherwise shardCanBeDeleted() would have returned false
name|DiscoveryNode
name|currentNode
init|=
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
name|currentNode
operator|!=
literal|null
assert|;
name|requests
operator|.
name|add
argument_list|(
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|currentNode
argument_list|,
operator|new
name|ShardActiveRequest
argument_list|(
name|clusterName
argument_list|,
name|indexUUID
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|shardRouting
operator|.
name|relocatingNodeId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|DiscoveryNode
name|relocatingNode
init|=
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
name|relocatingNode
operator|!=
literal|null
assert|;
name|requests
operator|.
name|add
argument_list|(
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|relocatingNode
argument_list|,
operator|new
name|ShardActiveRequest
argument_list|(
name|clusterName
argument_list|,
name|indexUUID
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|ShardActiveResponseHandler
name|responseHandler
init|=
operator|new
name|ShardActiveResponseHandler
argument_list|(
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
argument_list|,
name|state
argument_list|,
name|requests
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Tuple
argument_list|<
name|DiscoveryNode
argument_list|,
name|ShardActiveRequest
argument_list|>
name|request
range|:
name|requests
control|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|request
operator|.
name|v1
argument_list|()
argument_list|,
name|ACTION_SHARD_EXISTS
argument_list|,
name|request
operator|.
name|v2
argument_list|()
argument_list|,
name|responseHandler
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ShardActiveResponseHandler
specifier|private
class|class
name|ShardActiveResponseHandler
implements|implements
name|TransportResponseHandler
argument_list|<
name|ShardActiveResponse
argument_list|>
block|{
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|expectedActiveCopies
specifier|private
specifier|final
name|int
name|expectedActiveCopies
decl_stmt|;
DECL|field|clusterState
specifier|private
specifier|final
name|ClusterState
name|clusterState
decl_stmt|;
DECL|field|awaitingResponses
specifier|private
specifier|final
name|AtomicInteger
name|awaitingResponses
decl_stmt|;
DECL|field|activeCopies
specifier|private
specifier|final
name|AtomicInteger
name|activeCopies
decl_stmt|;
DECL|method|ShardActiveResponseHandler
specifier|public
name|ShardActiveResponseHandler
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|,
name|int
name|expectedActiveCopies
parameter_list|)
block|{
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|expectedActiveCopies
operator|=
name|expectedActiveCopies
expr_stmt|;
name|this
operator|.
name|clusterState
operator|=
name|clusterState
expr_stmt|;
name|this
operator|.
name|awaitingResponses
operator|=
operator|new
name|AtomicInteger
argument_list|(
name|expectedActiveCopies
argument_list|)
expr_stmt|;
name|this
operator|.
name|activeCopies
operator|=
operator|new
name|AtomicInteger
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newInstance
specifier|public
name|ShardActiveResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|ShardActiveResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|handleResponse
specifier|public
name|void
name|handleResponse
parameter_list|(
name|ShardActiveResponse
name|response
parameter_list|)
block|{
if|if
condition|(
name|response
operator|.
name|shardActive
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}] exists on node [{}]"
argument_list|,
name|shardId
argument_list|,
name|response
operator|.
name|node
argument_list|)
expr_stmt|;
name|activeCopies
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|awaitingResponses
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|allNodesResponded
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|handleException
specifier|public
name|void
name|handleException
parameter_list|(
name|TransportException
name|exp
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"shards active request failed for {}"
argument_list|,
name|exp
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
if|if
condition|(
name|awaitingResponses
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|allNodesResponded
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|executor
specifier|public
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
DECL|method|allNodesResponded
specifier|private
name|void
name|allNodesResponded
parameter_list|()
block|{
if|if
condition|(
name|activeCopies
operator|.
name|get
argument_list|()
operator|!=
name|expectedActiveCopies
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"not deleting shard {}, expected {} active copies, but only {} found active copies"
argument_list|,
name|shardId
argument_list|,
name|expectedActiveCopies
argument_list|,
name|activeCopies
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|ClusterState
name|latestClusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
name|clusterState
operator|.
name|getVersion
argument_list|()
operator|!=
name|latestClusterState
operator|.
name|getVersion
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"not deleting shard {}, the latest cluster state version[{}] is not equal to cluster state before shard active api call [{}]"
argument_list|,
name|shardId
argument_list|,
name|latestClusterState
operator|.
name|getVersion
argument_list|()
argument_list|,
name|clusterState
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"indices_store"
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
if|if
condition|(
name|clusterState
operator|.
name|getVersion
argument_list|()
operator|!=
name|currentState
operator|.
name|getVersion
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"not deleting shard {}, the update task state version[{}] is not equal to cluster state before shard active api call [{}]"
argument_list|,
name|shardId
argument_list|,
name|currentState
operator|.
name|getVersion
argument_list|()
argument_list|,
name|clusterState
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|currentState
return|;
block|}
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
comment|// not physical allocation of the index, delete it from the file system if applicable
if|if
condition|(
name|nodeEnv
operator|.
name|hasNodeFile
argument_list|()
condition|)
block|{
name|File
index|[]
name|shardLocations
init|=
name|nodeEnv
operator|.
name|shardLocations
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|FileSystemUtils
operator|.
name|exists
argument_list|(
name|shardLocations
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}] deleting shard that is no longer used"
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
name|shardLocations
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|indexService
operator|.
name|hasShard
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|indexService
operator|.
name|store
argument_list|()
operator|.
name|canDeleteUnallocated
argument_list|(
name|shardId
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} deleting shard that is no longer used"
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
try|try
block|{
name|indexService
operator|.
name|store
argument_list|()
operator|.
name|deleteUnallocated
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} failed to delete unallocated shard, ignoring"
argument_list|,
name|e
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// this state is weird, should we log?
comment|// basically, it means that the shard is not allocated on this node using the routing
comment|// but its still physically exists on an IndexService
comment|// Note, this listener should run after IndicesClusterStateService...
block|}
block|}
return|return
name|currentState
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
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"{} unexpected error during deletion of unallocated shard"
argument_list|,
name|t
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ShardActiveRequestHandler
specifier|private
class|class
name|ShardActiveRequestHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|ShardActiveRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|newInstance
specifier|public
name|ShardActiveRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|ShardActiveRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|executor
specifier|public
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ShardActiveRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|ShardActiveResponse
argument_list|(
name|shardActive
argument_list|(
name|request
argument_list|)
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|shardActive
specifier|private
name|boolean
name|shardActive
parameter_list|(
name|ShardActiveRequest
name|request
parameter_list|)
block|{
name|ClusterName
name|thisClusterName
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|getClusterName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|thisClusterName
operator|.
name|equals
argument_list|(
name|request
operator|.
name|clusterName
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"shard exists request meant for cluster[{}], but this is cluster[{}], ignoring request"
argument_list|,
name|request
operator|.
name|clusterName
argument_list|,
name|thisClusterName
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|ShardId
name|shardId
init|=
name|request
operator|.
name|shardId
decl_stmt|;
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|!=
literal|null
operator|&&
name|indexService
operator|.
name|indexUUID
argument_list|()
operator|.
name|equals
argument_list|(
name|request
operator|.
name|indexUUID
argument_list|)
condition|)
block|{
name|IndexShard
name|indexShard
init|=
name|indexService
operator|.
name|shard
argument_list|(
name|shardId
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexShard
operator|!=
literal|null
condition|)
block|{
return|return
name|ACTIVE_STATES
operator|.
name|contains
argument_list|(
name|indexShard
operator|.
name|state
argument_list|()
argument_list|)
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
DECL|class|ShardActiveRequest
specifier|private
specifier|static
class|class
name|ShardActiveRequest
extends|extends
name|TransportRequest
block|{
DECL|field|clusterName
specifier|private
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|indexUUID
specifier|private
name|String
name|indexUUID
decl_stmt|;
DECL|field|shardId
specifier|private
name|ShardId
name|shardId
decl_stmt|;
DECL|method|ShardActiveRequest
name|ShardActiveRequest
parameter_list|()
block|{         }
DECL|method|ShardActiveRequest
name|ShardActiveRequest
parameter_list|(
name|ClusterName
name|clusterName
parameter_list|,
name|String
name|indexUUID
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
block|{
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|indexUUID
operator|=
name|indexUUID
expr_stmt|;
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|clusterName
operator|=
name|ClusterName
operator|.
name|readClusterName
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|indexUUID
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|shardId
operator|=
name|ShardId
operator|.
name|readShardId
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|clusterName
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|indexUUID
argument_list|)
expr_stmt|;
name|shardId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ShardActiveResponse
specifier|private
specifier|static
class|class
name|ShardActiveResponse
extends|extends
name|TransportResponse
block|{
DECL|field|shardActive
specifier|private
name|boolean
name|shardActive
decl_stmt|;
DECL|field|node
specifier|private
name|DiscoveryNode
name|node
decl_stmt|;
DECL|method|ShardActiveResponse
name|ShardActiveResponse
parameter_list|()
block|{         }
DECL|method|ShardActiveResponse
name|ShardActiveResponse
parameter_list|(
name|boolean
name|shardActive
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|this
operator|.
name|shardActive
operator|=
name|shardActive
expr_stmt|;
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|shardActive
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|node
operator|=
name|DiscoveryNode
operator|.
name|readNode
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|shardActive
argument_list|)
expr_stmt|;
name|node
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

