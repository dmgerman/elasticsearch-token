begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ClusterService
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
name|concurrent
operator|.
name|ConcurrentCollections
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
name|threadpool
operator|.
name|ThreadPool
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
name|ScheduledFuture
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
DECL|field|nodeEnv
specifier|private
specifier|final
name|NodeEnvironment
name|nodeEnv
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
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|danglingTimeout
specifier|private
specifier|final
name|TimeValue
name|danglingTimeout
decl_stmt|;
DECL|field|danglingIndices
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|DanglingIndex
argument_list|>
name|danglingIndices
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|danglingMutex
specifier|private
specifier|final
name|Object
name|danglingMutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|class|DanglingIndex
specifier|static
class|class
name|DanglingIndex
block|{
DECL|field|index
specifier|public
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|future
specifier|public
specifier|final
name|ScheduledFuture
name|future
decl_stmt|;
DECL|method|DanglingIndex
name|DanglingIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|ScheduledFuture
name|future
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|future
operator|=
name|future
expr_stmt|;
block|}
block|}
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
name|IndicesService
name|indicesService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
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
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|danglingTimeout
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"dangling_timeout"
argument_list|,
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|2
argument_list|)
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
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
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
comment|// when all shards are started within a shard replication group, delete an unallocated shard on this node
name|RoutingTable
name|routingTable
init|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|routingTable
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|routingTable
control|)
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|indexRoutingTable
operator|.
name|index
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
comment|// we handle this later...
continue|continue;
block|}
comment|// if the store is not persistent, don't bother trying to check if it can be deleted
if|if
condition|(
operator|!
name|indexService
operator|.
name|store
argument_list|()
operator|.
name|persistent
argument_list|()
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
comment|// if it has been created on this node, we don't want to delete it
if|if
condition|(
name|indexService
operator|.
name|hasShard
argument_list|(
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|indexService
operator|.
name|store
argument_list|()
operator|.
name|canDeleteUnallocated
argument_list|(
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// only delete an unallocated shard if all (other shards) are started
if|if
condition|(
name|indexShardRoutingTable
operator|.
name|countWithState
argument_list|(
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
operator|==
name|indexShardRoutingTable
operator|.
name|size
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
literal|"[{}][{}] deleting unallocated shard"
argument_list|,
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|indexService
operator|.
name|store
argument_list|()
operator|.
name|deleteUnallocated
argument_list|(
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
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
literal|"[{}][{}] failed to delete unallocated shard, ignoring"
argument_list|,
name|e
argument_list|,
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// do the reverse, and delete dangling indices / shards that might remain on that node
comment|// this can happen when deleting a closed index, or when a node joins and it has deleted indices / shards
if|if
condition|(
name|nodeEnv
operator|.
name|hasNodeFile
argument_list|()
condition|)
block|{
comment|// delete unused shards for existing indices
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|routingTable
control|)
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|indexRoutingTable
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|!=
literal|null
condition|)
block|{
comment|// allocated, ignore this
continue|continue;
block|}
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
name|boolean
name|shardCanBeDeleted
init|=
literal|true
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
comment|// don't delete a shard that not all instances are active
if|if
condition|(
operator|!
name|shardRouting
operator|.
name|active
argument_list|()
condition|)
block|{
name|shardCanBeDeleted
operator|=
literal|false
expr_stmt|;
break|break;
block|}
name|String
name|localNodeId
init|=
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|id
argument_list|()
decl_stmt|;
comment|// check if shard is active on the current node or is getting relocated to the our node
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
comment|// shard will be used locally - keep it
name|shardCanBeDeleted
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|shardCanBeDeleted
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
for|for
control|(
name|File
name|shardLocation
range|:
name|nodeEnv
operator|.
name|shardLocations
argument_list|(
name|shardId
argument_list|)
control|)
block|{
if|if
condition|(
name|shardLocation
operator|.
name|exists
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}] deleting shard that is no longer used"
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
name|shardLocation
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|danglingTimeout
operator|.
name|millis
argument_list|()
operator|>=
literal|0
condition|)
block|{
synchronized|synchronized
init|(
name|danglingMutex
init|)
block|{
for|for
control|(
name|String
name|danglingIndex
range|:
name|danglingIndices
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|hasIndex
argument_list|(
name|danglingIndex
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] no longer dangling (created), removing"
argument_list|,
name|danglingIndex
argument_list|)
expr_stmt|;
name|DanglingIndex
name|removed
init|=
name|danglingIndices
operator|.
name|remove
argument_list|(
name|danglingIndex
argument_list|)
decl_stmt|;
name|removed
operator|.
name|future
operator|.
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
comment|// delete indices that are no longer part of the metadata
try|try
block|{
for|for
control|(
name|String
name|indexName
range|:
name|nodeEnv
operator|.
name|findAllIndices
argument_list|()
control|)
block|{
comment|// if we have the index on the metadata, don't delete it
if|if
condition|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|hasIndex
argument_list|(
name|indexName
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|danglingIndices
operator|.
name|containsKey
argument_list|(
name|indexName
argument_list|)
condition|)
block|{
comment|// already dangling, continue
continue|continue;
block|}
if|if
condition|(
name|danglingTimeout
operator|.
name|millis
argument_list|()
operator|==
literal|0
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] dangling index, exists on local file system, but not in cluster metadata, timeout set to 0, deleting now"
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
name|nodeEnv
operator|.
name|indexLocations
argument_list|(
operator|new
name|Index
argument_list|(
name|indexName
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] dangling index, exists on local file system, but not in cluster metadata, scheduling to delete in [{}]"
argument_list|,
name|indexName
argument_list|,
name|danglingTimeout
argument_list|)
expr_stmt|;
name|danglingIndices
operator|.
name|put
argument_list|(
name|indexName
argument_list|,
operator|new
name|DanglingIndex
argument_list|(
name|indexName
argument_list|,
name|threadPool
operator|.
name|schedule
argument_list|(
name|danglingTimeout
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|RemoveDanglingIndex
argument_list|(
name|indexName
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to find dangling indices"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|class|RemoveDanglingIndex
class|class
name|RemoveDanglingIndex
implements|implements
name|Runnable
block|{
DECL|field|index
specifier|private
specifier|final
name|String
name|index
decl_stmt|;
DECL|method|RemoveDanglingIndex
name|RemoveDanglingIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
synchronized|synchronized
init|(
name|danglingMutex
init|)
block|{
name|DanglingIndex
name|remove
init|=
name|danglingIndices
operator|.
name|remove
argument_list|(
name|index
argument_list|)
decl_stmt|;
comment|// no longer there...
if|if
condition|(
name|remove
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] deleting dangling index"
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
name|nodeEnv
operator|.
name|indexLocations
argument_list|(
operator|new
name|Index
argument_list|(
name|index
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

