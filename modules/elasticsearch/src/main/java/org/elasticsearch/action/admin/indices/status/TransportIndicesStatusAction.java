begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.status
package|package
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
name|status
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|ShardOperationFailedException
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
name|TransportActions
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
name|support
operator|.
name|DefaultShardOperationFailedException
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
name|support
operator|.
name|broadcast
operator|.
name|BroadcastShardOperationFailedException
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
name|support
operator|.
name|broadcast
operator|.
name|BroadcastShardOperationRequest
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
name|support
operator|.
name|broadcast
operator|.
name|TransportBroadcastOperationAction
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
name|routing
operator|.
name|GroupShardsIterator
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
name|ShardIterator
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
name|Settings
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
name|engine
operator|.
name|Engine
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
name|gateway
operator|.
name|IndexShardGatewayService
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
name|gateway
operator|.
name|SnapshotStatus
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
name|InternalIndexService
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
name|recovery
operator|.
name|RecoveryStatus
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
name|recovery
operator|.
name|RecoveryTarget
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
name|InternalIndexShard
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
name|AtomicReferenceArray
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
name|Lists
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportIndicesStatusAction
specifier|public
class|class
name|TransportIndicesStatusAction
extends|extends
name|TransportBroadcastOperationAction
argument_list|<
name|IndicesStatusRequest
argument_list|,
name|IndicesStatusResponse
argument_list|,
name|TransportIndicesStatusAction
operator|.
name|IndexShardStatusRequest
argument_list|,
name|ShardStatus
argument_list|>
block|{
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|peerRecoveryTarget
specifier|private
specifier|final
name|RecoveryTarget
name|peerRecoveryTarget
decl_stmt|;
DECL|method|TransportIndicesStatusAction
annotation|@
name|Inject
specifier|public
name|TransportIndicesStatusAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|RecoveryTarget
name|peerRecoveryTarget
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|)
expr_stmt|;
name|this
operator|.
name|peerRecoveryTarget
operator|=
name|peerRecoveryTarget
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
block|}
DECL|method|executor
annotation|@
name|Override
specifier|protected
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|CACHED
return|;
block|}
DECL|method|transportAction
annotation|@
name|Override
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|TransportActions
operator|.
name|Admin
operator|.
name|Indices
operator|.
name|STATUS
return|;
block|}
DECL|method|transportShardAction
annotation|@
name|Override
specifier|protected
name|String
name|transportShardAction
parameter_list|()
block|{
return|return
literal|"indices/status/shard"
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|IndicesStatusRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|IndicesStatusRequest
argument_list|()
return|;
block|}
DECL|method|ignoreNonActiveExceptions
annotation|@
name|Override
specifier|protected
name|boolean
name|ignoreNonActiveExceptions
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**      * Status goes across *all* shards.      */
DECL|method|shards
annotation|@
name|Override
specifier|protected
name|GroupShardsIterator
name|shards
parameter_list|(
name|IndicesStatusRequest
name|request
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
return|return
name|clusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|allShardsGrouped
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * We want to go over all assigned nodes (to get recovery status) and not just active ones.      */
DECL|method|nextShardOrNull
annotation|@
name|Override
specifier|protected
name|ShardRouting
name|nextShardOrNull
parameter_list|(
name|ShardIterator
name|shardIt
parameter_list|)
block|{
return|return
name|shardIt
operator|.
name|nextAssignedOrNull
argument_list|()
return|;
block|}
comment|/**      * We want to go over all assigned nodes (to get recovery status) and not just active ones.      */
DECL|method|hasNextShard
annotation|@
name|Override
specifier|protected
name|boolean
name|hasNextShard
parameter_list|(
name|ShardIterator
name|shardIt
parameter_list|)
block|{
return|return
name|shardIt
operator|.
name|hasNextAssigned
argument_list|()
return|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|IndicesStatusResponse
name|newResponse
parameter_list|(
name|IndicesStatusRequest
name|request
parameter_list|,
name|AtomicReferenceArray
name|shardsResponses
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|int
name|successfulShards
init|=
literal|0
decl_stmt|;
name|int
name|failedShards
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|shardFailures
init|=
literal|null
decl_stmt|;
specifier|final
name|List
argument_list|<
name|ShardStatus
argument_list|>
name|shards
init|=
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|shardsResponses
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|shardResponse
init|=
name|shardsResponses
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardResponse
operator|==
literal|null
condition|)
block|{
comment|// simply ignore non active shards
block|}
elseif|else
if|if
condition|(
name|shardResponse
operator|instanceof
name|BroadcastShardOperationFailedException
condition|)
block|{
name|failedShards
operator|++
expr_stmt|;
if|if
condition|(
name|shardFailures
operator|==
literal|null
condition|)
block|{
name|shardFailures
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|shardFailures
operator|.
name|add
argument_list|(
operator|new
name|DefaultShardOperationFailedException
argument_list|(
operator|(
name|BroadcastShardOperationFailedException
operator|)
name|shardResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shards
operator|.
name|add
argument_list|(
operator|(
name|ShardStatus
operator|)
name|shardResponse
argument_list|)
expr_stmt|;
name|successfulShards
operator|++
expr_stmt|;
block|}
block|}
return|return
operator|new
name|IndicesStatusResponse
argument_list|(
name|shards
operator|.
name|toArray
argument_list|(
operator|new
name|ShardStatus
index|[
name|shards
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|clusterState
argument_list|,
name|shardsResponses
operator|.
name|length
argument_list|()
argument_list|,
name|successfulShards
argument_list|,
name|failedShards
argument_list|,
name|shardFailures
argument_list|)
return|;
block|}
DECL|method|newShardRequest
annotation|@
name|Override
specifier|protected
name|IndexShardStatusRequest
name|newShardRequest
parameter_list|()
block|{
return|return
operator|new
name|IndexShardStatusRequest
argument_list|()
return|;
block|}
DECL|method|newShardRequest
annotation|@
name|Override
specifier|protected
name|IndexShardStatusRequest
name|newShardRequest
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
name|IndicesStatusRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|IndexShardStatusRequest
argument_list|(
name|shard
operator|.
name|index
argument_list|()
argument_list|,
name|shard
operator|.
name|id
argument_list|()
argument_list|,
name|request
argument_list|)
return|;
block|}
DECL|method|newShardResponse
annotation|@
name|Override
specifier|protected
name|ShardStatus
name|newShardResponse
parameter_list|()
block|{
return|return
operator|new
name|ShardStatus
argument_list|()
return|;
block|}
DECL|method|shardOperation
annotation|@
name|Override
specifier|protected
name|ShardStatus
name|shardOperation
parameter_list|(
name|IndexShardStatusRequest
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|InternalIndexService
name|indexService
init|=
operator|(
name|InternalIndexService
operator|)
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
name|InternalIndexShard
name|indexShard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indexService
operator|.
name|shardSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
name|ShardStatus
name|shardStatus
init|=
operator|new
name|ShardStatus
argument_list|(
name|indexShard
operator|.
name|routingEntry
argument_list|()
argument_list|)
decl_stmt|;
name|shardStatus
operator|.
name|state
operator|=
name|indexShard
operator|.
name|state
argument_list|()
expr_stmt|;
try|try
block|{
name|shardStatus
operator|.
name|storeSize
operator|=
name|indexShard
operator|.
name|store
argument_list|()
operator|.
name|estimateSize
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// failure to get the store size...
block|}
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|STARTED
condition|)
block|{
comment|//            shardStatus.estimatedFlushableMemorySize = indexShard.estimateFlushableMemorySize();
name|shardStatus
operator|.
name|translogId
operator|=
name|indexShard
operator|.
name|translog
argument_list|()
operator|.
name|currentId
argument_list|()
expr_stmt|;
name|shardStatus
operator|.
name|translogOperations
operator|=
name|indexShard
operator|.
name|translog
argument_list|()
operator|.
name|numberOfOperations
argument_list|()
expr_stmt|;
name|Engine
operator|.
name|Searcher
name|searcher
init|=
name|indexShard
operator|.
name|searcher
argument_list|()
decl_stmt|;
try|try
block|{
name|shardStatus
operator|.
name|docs
operator|=
operator|new
name|DocsStatus
argument_list|()
expr_stmt|;
name|shardStatus
operator|.
name|docs
operator|.
name|numDocs
operator|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|numDocs
argument_list|()
expr_stmt|;
name|shardStatus
operator|.
name|docs
operator|.
name|maxDoc
operator|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
expr_stmt|;
name|shardStatus
operator|.
name|docs
operator|.
name|deletedDocs
operator|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|numDeletedDocs
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|searcher
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
name|shardStatus
operator|.
name|mergeStats
operator|=
name|indexShard
operator|.
name|mergeScheduler
argument_list|()
operator|.
name|stats
argument_list|()
expr_stmt|;
name|shardStatus
operator|.
name|refreshStats
operator|=
name|indexShard
operator|.
name|refreshStats
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|recovery
condition|)
block|{
comment|// check on going recovery (from peer or gateway)
name|RecoveryStatus
name|peerRecoveryStatus
init|=
name|indexShard
operator|.
name|peerRecoveryStatus
argument_list|()
decl_stmt|;
if|if
condition|(
name|peerRecoveryStatus
operator|==
literal|null
condition|)
block|{
name|peerRecoveryStatus
operator|=
name|peerRecoveryTarget
operator|.
name|peerRecoveryStatus
argument_list|(
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|peerRecoveryStatus
operator|!=
literal|null
condition|)
block|{
name|PeerRecoveryStatus
operator|.
name|Stage
name|stage
decl_stmt|;
switch|switch
condition|(
name|peerRecoveryStatus
operator|.
name|stage
argument_list|()
condition|)
block|{
case|case
name|INIT
case|:
name|stage
operator|=
name|PeerRecoveryStatus
operator|.
name|Stage
operator|.
name|INIT
expr_stmt|;
break|break;
case|case
name|INDEX
case|:
name|stage
operator|=
name|PeerRecoveryStatus
operator|.
name|Stage
operator|.
name|INDEX
expr_stmt|;
break|break;
case|case
name|TRANSLOG
case|:
name|stage
operator|=
name|PeerRecoveryStatus
operator|.
name|Stage
operator|.
name|TRANSLOG
expr_stmt|;
break|break;
case|case
name|FINALIZE
case|:
name|stage
operator|=
name|PeerRecoveryStatus
operator|.
name|Stage
operator|.
name|FINALIZE
expr_stmt|;
break|break;
case|case
name|DONE
case|:
name|stage
operator|=
name|PeerRecoveryStatus
operator|.
name|Stage
operator|.
name|DONE
expr_stmt|;
break|break;
default|default:
name|stage
operator|=
name|PeerRecoveryStatus
operator|.
name|Stage
operator|.
name|INIT
expr_stmt|;
block|}
name|shardStatus
operator|.
name|peerRecoveryStatus
operator|=
operator|new
name|PeerRecoveryStatus
argument_list|(
name|stage
argument_list|,
name|peerRecoveryStatus
operator|.
name|startTime
argument_list|()
argument_list|,
name|peerRecoveryStatus
operator|.
name|time
argument_list|()
argument_list|,
name|peerRecoveryStatus
operator|.
name|phase1TotalSize
argument_list|()
argument_list|,
name|peerRecoveryStatus
operator|.
name|phase1ExistingTotalSize
argument_list|()
argument_list|,
name|peerRecoveryStatus
operator|.
name|currentFilesSize
argument_list|()
argument_list|,
name|peerRecoveryStatus
operator|.
name|currentTranslogOperations
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|IndexShardGatewayService
name|gatewayService
init|=
name|indexService
operator|.
name|shardInjector
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
operator|.
name|getInstance
argument_list|(
name|IndexShardGatewayService
operator|.
name|class
argument_list|)
decl_stmt|;
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|gateway
operator|.
name|RecoveryStatus
name|gatewayRecoveryStatus
init|=
name|gatewayService
operator|.
name|recoveryStatus
argument_list|()
decl_stmt|;
if|if
condition|(
name|gatewayRecoveryStatus
operator|!=
literal|null
condition|)
block|{
name|GatewayRecoveryStatus
operator|.
name|Stage
name|stage
decl_stmt|;
switch|switch
condition|(
name|gatewayRecoveryStatus
operator|.
name|stage
argument_list|()
condition|)
block|{
case|case
name|INIT
case|:
name|stage
operator|=
name|GatewayRecoveryStatus
operator|.
name|Stage
operator|.
name|INIT
expr_stmt|;
break|break;
case|case
name|INDEX
case|:
name|stage
operator|=
name|GatewayRecoveryStatus
operator|.
name|Stage
operator|.
name|INDEX
expr_stmt|;
break|break;
case|case
name|TRANSLOG
case|:
name|stage
operator|=
name|GatewayRecoveryStatus
operator|.
name|Stage
operator|.
name|TRANSLOG
expr_stmt|;
break|break;
case|case
name|DONE
case|:
name|stage
operator|=
name|GatewayRecoveryStatus
operator|.
name|Stage
operator|.
name|DONE
expr_stmt|;
break|break;
default|default:
name|stage
operator|=
name|GatewayRecoveryStatus
operator|.
name|Stage
operator|.
name|INIT
expr_stmt|;
block|}
name|shardStatus
operator|.
name|gatewayRecoveryStatus
operator|=
operator|new
name|GatewayRecoveryStatus
argument_list|(
name|stage
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|startTime
argument_list|()
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|time
argument_list|()
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|totalSize
argument_list|()
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|reusedTotalSize
argument_list|()
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|index
argument_list|()
operator|.
name|currentFilesSize
argument_list|()
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|translog
argument_list|()
operator|.
name|currentTranslogOperations
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|request
operator|.
name|snapshot
condition|)
block|{
name|IndexShardGatewayService
name|gatewayService
init|=
name|indexService
operator|.
name|shardInjector
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
operator|.
name|getInstance
argument_list|(
name|IndexShardGatewayService
operator|.
name|class
argument_list|)
decl_stmt|;
name|SnapshotStatus
name|snapshotStatus
init|=
name|gatewayService
operator|.
name|snapshotStatus
argument_list|()
decl_stmt|;
if|if
condition|(
name|snapshotStatus
operator|!=
literal|null
condition|)
block|{
name|GatewaySnapshotStatus
operator|.
name|Stage
name|stage
decl_stmt|;
switch|switch
condition|(
name|snapshotStatus
operator|.
name|stage
argument_list|()
condition|)
block|{
case|case
name|DONE
case|:
name|stage
operator|=
name|GatewaySnapshotStatus
operator|.
name|Stage
operator|.
name|DONE
expr_stmt|;
break|break;
case|case
name|FAILURE
case|:
name|stage
operator|=
name|GatewaySnapshotStatus
operator|.
name|Stage
operator|.
name|FAILURE
expr_stmt|;
break|break;
case|case
name|TRANSLOG
case|:
name|stage
operator|=
name|GatewaySnapshotStatus
operator|.
name|Stage
operator|.
name|TRANSLOG
expr_stmt|;
break|break;
case|case
name|FINALIZE
case|:
name|stage
operator|=
name|GatewaySnapshotStatus
operator|.
name|Stage
operator|.
name|FINALIZE
expr_stmt|;
break|break;
case|case
name|INDEX
case|:
name|stage
operator|=
name|GatewaySnapshotStatus
operator|.
name|Stage
operator|.
name|INDEX
expr_stmt|;
break|break;
default|default:
name|stage
operator|=
name|GatewaySnapshotStatus
operator|.
name|Stage
operator|.
name|NONE
expr_stmt|;
break|break;
block|}
name|shardStatus
operator|.
name|gatewaySnapshotStatus
operator|=
operator|new
name|GatewaySnapshotStatus
argument_list|(
name|stage
argument_list|,
name|snapshotStatus
operator|.
name|startTime
argument_list|()
argument_list|,
name|snapshotStatus
operator|.
name|time
argument_list|()
argument_list|,
name|snapshotStatus
operator|.
name|index
argument_list|()
operator|.
name|totalSize
argument_list|()
argument_list|,
name|snapshotStatus
operator|.
name|translog
argument_list|()
operator|.
name|expectedNumberOfOperations
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|shardStatus
return|;
block|}
DECL|class|IndexShardStatusRequest
specifier|public
specifier|static
class|class
name|IndexShardStatusRequest
extends|extends
name|BroadcastShardOperationRequest
block|{
DECL|field|recovery
name|boolean
name|recovery
decl_stmt|;
DECL|field|snapshot
name|boolean
name|snapshot
decl_stmt|;
DECL|method|IndexShardStatusRequest
name|IndexShardStatusRequest
parameter_list|()
block|{         }
DECL|method|IndexShardStatusRequest
name|IndexShardStatusRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|IndicesStatusRequest
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|recovery
operator|=
name|request
operator|.
name|recovery
argument_list|()
expr_stmt|;
name|snapshot
operator|=
name|request
operator|.
name|snapshot
argument_list|()
expr_stmt|;
block|}
DECL|method|readFrom
annotation|@
name|Override
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
name|recovery
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|snapshot
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
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
name|recovery
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|snapshot
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

