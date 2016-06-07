begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.replication
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|replication
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|action
operator|.
name|UnavailableShardsException
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
name|WriteConsistencyLevel
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
name|TransportActions
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
name|logging
operator|.
name|ESLogger
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
name|VersionConflictEngineException
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
name|rest
operator|.
name|RestStatus
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
name|Locale
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
name|AtomicBoolean
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_class
DECL|class|ReplicationOperation
specifier|public
class|class
name|ReplicationOperation
parameter_list|<
name|Request
extends|extends
name|ReplicationRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|,
name|ReplicaRequest
extends|extends
name|ReplicationRequest
parameter_list|<
name|ReplicaRequest
parameter_list|>
parameter_list|,
name|PrimaryResultT
extends|extends
name|ReplicationOperation
operator|.
name|PrimaryResult
parameter_list|<
name|ReplicaRequest
parameter_list|>
parameter_list|>
block|{
DECL|field|logger
specifier|final
specifier|private
name|ESLogger
name|logger
decl_stmt|;
DECL|field|request
specifier|final
specifier|private
name|Request
name|request
decl_stmt|;
DECL|field|clusterStateSupplier
specifier|final
specifier|private
name|Supplier
argument_list|<
name|ClusterState
argument_list|>
name|clusterStateSupplier
decl_stmt|;
DECL|field|opType
specifier|final
specifier|private
name|String
name|opType
decl_stmt|;
DECL|field|totalShards
specifier|final
specifier|private
name|AtomicInteger
name|totalShards
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
comment|/**      * The number of pending sub-operations in this operation. This is incremented when the following operations start and decremented when      * they complete:      *<ul>      *<li>The operation on the primary</li>      *<li>The operation on each replica</li>      *<li>Coordination of the operation as a whole. This prevents the operation from terminating early if we haven't started any replica      * operations and the primary finishes.</li>      *</ul>      */
DECL|field|pendingShards
specifier|final
specifier|private
name|AtomicInteger
name|pendingShards
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|successfulShards
specifier|final
specifier|private
name|AtomicInteger
name|successfulShards
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|executeOnReplicas
specifier|final
specifier|private
name|boolean
name|executeOnReplicas
decl_stmt|;
DECL|field|checkWriteConsistency
specifier|final
specifier|private
name|boolean
name|checkWriteConsistency
decl_stmt|;
DECL|field|primary
specifier|final
specifier|private
name|Primary
argument_list|<
name|Request
argument_list|,
name|ReplicaRequest
argument_list|,
name|PrimaryResultT
argument_list|>
name|primary
decl_stmt|;
DECL|field|replicasProxy
specifier|final
specifier|private
name|Replicas
argument_list|<
name|ReplicaRequest
argument_list|>
name|replicasProxy
decl_stmt|;
DECL|field|finished
specifier|final
specifier|private
name|AtomicBoolean
name|finished
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|field|resultListener
specifier|final
specifier|protected
name|ActionListener
argument_list|<
name|PrimaryResultT
argument_list|>
name|resultListener
decl_stmt|;
DECL|field|primaryResult
specifier|private
specifier|volatile
name|PrimaryResultT
name|primaryResult
init|=
literal|null
decl_stmt|;
DECL|field|shardReplicaFailures
specifier|private
specifier|final
name|List
argument_list|<
name|ReplicationResponse
operator|.
name|ShardInfo
operator|.
name|Failure
argument_list|>
name|shardReplicaFailures
init|=
name|Collections
operator|.
name|synchronizedList
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
DECL|method|ReplicationOperation
name|ReplicationOperation
parameter_list|(
name|Request
name|request
parameter_list|,
name|Primary
argument_list|<
name|Request
argument_list|,
name|ReplicaRequest
argument_list|,
name|PrimaryResultT
argument_list|>
name|primary
parameter_list|,
name|ActionListener
argument_list|<
name|PrimaryResultT
argument_list|>
name|listener
parameter_list|,
name|boolean
name|executeOnReplicas
parameter_list|,
name|boolean
name|checkWriteConsistency
parameter_list|,
name|Replicas
argument_list|<
name|ReplicaRequest
argument_list|>
name|replicas
parameter_list|,
name|Supplier
argument_list|<
name|ClusterState
argument_list|>
name|clusterStateSupplier
parameter_list|,
name|ESLogger
name|logger
parameter_list|,
name|String
name|opType
parameter_list|)
block|{
name|this
operator|.
name|checkWriteConsistency
operator|=
name|checkWriteConsistency
expr_stmt|;
name|this
operator|.
name|executeOnReplicas
operator|=
name|executeOnReplicas
expr_stmt|;
name|this
operator|.
name|replicasProxy
operator|=
name|replicas
expr_stmt|;
name|this
operator|.
name|primary
operator|=
name|primary
expr_stmt|;
name|this
operator|.
name|resultListener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|clusterStateSupplier
operator|=
name|clusterStateSupplier
expr_stmt|;
name|this
operator|.
name|opType
operator|=
name|opType
expr_stmt|;
block|}
DECL|method|execute
name|void
name|execute
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|writeConsistencyFailure
init|=
name|checkWriteConsistency
condition|?
name|checkWriteConsistency
argument_list|()
else|:
literal|null
decl_stmt|;
specifier|final
name|ShardRouting
name|primaryRouting
init|=
name|primary
operator|.
name|routingEntry
argument_list|()
decl_stmt|;
specifier|final
name|ShardId
name|primaryId
init|=
name|primaryRouting
operator|.
name|shardId
argument_list|()
decl_stmt|;
if|if
condition|(
name|writeConsistencyFailure
operator|!=
literal|null
condition|)
block|{
name|finishAsFailed
argument_list|(
operator|new
name|UnavailableShardsException
argument_list|(
name|primaryId
argument_list|,
literal|"{} Timeout: [{}], request: [{}]"
argument_list|,
name|writeConsistencyFailure
argument_list|,
name|request
operator|.
name|timeout
argument_list|()
argument_list|,
name|request
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|totalShards
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|pendingShards
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
comment|// increase by 1 until we finish all primary coordination
name|primaryResult
operator|=
name|primary
operator|.
name|perform
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|primary
operator|.
name|updateLocalCheckpointForShard
argument_list|(
name|primaryRouting
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|primary
operator|.
name|localCheckpoint
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|ReplicaRequest
name|replicaRequest
init|=
name|primaryResult
operator|.
name|replicaRequest
argument_list|()
decl_stmt|;
assert|assert
name|replicaRequest
operator|.
name|primaryTerm
argument_list|()
operator|>
literal|0
operator|:
literal|"replicaRequest doesn't have a primary term"
assert|;
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
literal|"[{}] op [{}] completed on primary for request [{}]"
argument_list|,
name|primaryId
argument_list|,
name|opType
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
comment|// we have to get a new state after successfully indexing into the primary in order to honour recovery semantics.
comment|// we have to make sure that every operation indexed into the primary after recovery start will also be replicated
comment|// to the recovery target. If we use an old cluster state, we may miss a relocation that has started since then.
comment|// If the index gets deleted after primary operation, we skip replication
specifier|final
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
init|=
name|getShards
argument_list|(
name|primaryId
argument_list|,
name|clusterStateSupplier
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|String
name|localNodeId
init|=
name|primary
operator|.
name|routingEntry
argument_list|()
operator|.
name|currentNodeId
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|ShardRouting
name|shard
range|:
name|shards
control|)
block|{
if|if
condition|(
name|executeOnReplicas
operator|==
literal|false
operator|||
name|shard
operator|.
name|unassigned
argument_list|()
condition|)
block|{
if|if
condition|(
name|shard
operator|.
name|primary
argument_list|()
operator|==
literal|false
condition|)
block|{
name|totalShards
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
continue|continue;
block|}
if|if
condition|(
name|shard
operator|.
name|currentNodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|localNodeId
argument_list|)
operator|==
literal|false
condition|)
block|{
name|performOnReplica
argument_list|(
name|shard
argument_list|,
name|replicaRequest
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shard
operator|.
name|relocating
argument_list|()
operator|&&
name|shard
operator|.
name|relocatingNodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|localNodeId
argument_list|)
operator|==
literal|false
condition|)
block|{
name|performOnReplica
argument_list|(
name|shard
operator|.
name|buildTargetRelocatingShard
argument_list|()
argument_list|,
name|replicaRequest
argument_list|)
expr_stmt|;
block|}
block|}
name|successfulShards
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
comment|// mark primary as successful
name|decPendingAndFinishIfNeeded
argument_list|()
expr_stmt|;
block|}
DECL|method|performOnReplica
specifier|private
name|void
name|performOnReplica
parameter_list|(
specifier|final
name|ShardRouting
name|shard
parameter_list|,
specifier|final
name|ReplicaRequest
name|replicaRequest
parameter_list|)
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
literal|"[{}] sending op [{}] to replica {} for request [{}]"
argument_list|,
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|opType
argument_list|,
name|shard
argument_list|,
name|replicaRequest
argument_list|)
expr_stmt|;
block|}
name|totalShards
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|pendingShards
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|replicasProxy
operator|.
name|performOn
argument_list|(
name|shard
argument_list|,
name|replicaRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|ReplicaResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ReplicaResponse
name|response
parameter_list|)
block|{
name|successfulShards
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|primary
operator|.
name|updateLocalCheckpointForShard
argument_list|(
name|response
operator|.
name|allocationId
argument_list|()
argument_list|,
name|response
operator|.
name|localCheckpoint
argument_list|()
argument_list|)
expr_stmt|;
name|decPendingAndFinishIfNeeded
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|replicaException
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}] failure while performing [{}] on replica {}, request [{}]"
argument_list|,
name|replicaException
argument_list|,
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|opType
argument_list|,
name|shard
argument_list|,
name|replicaRequest
argument_list|)
expr_stmt|;
if|if
condition|(
name|ignoreReplicaException
argument_list|(
name|replicaException
argument_list|)
condition|)
block|{
name|decPendingAndFinishIfNeeded
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|RestStatus
name|restStatus
init|=
name|ExceptionsHelper
operator|.
name|status
argument_list|(
name|replicaException
argument_list|)
decl_stmt|;
name|shardReplicaFailures
operator|.
name|add
argument_list|(
operator|new
name|ReplicationResponse
operator|.
name|ShardInfo
operator|.
name|Failure
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|replicaException
argument_list|,
name|restStatus
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|message
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"failed to perform %s on replica %s"
argument_list|,
name|opType
argument_list|,
name|shard
argument_list|)
decl_stmt|;
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] {}"
argument_list|,
name|replicaException
argument_list|,
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|message
argument_list|)
expr_stmt|;
name|replicasProxy
operator|.
name|failShard
argument_list|(
name|shard
argument_list|,
name|primary
operator|.
name|routingEntry
argument_list|()
argument_list|,
name|message
argument_list|,
name|replicaException
argument_list|,
name|ReplicationOperation
operator|.
name|this
operator|::
name|decPendingAndFinishIfNeeded
argument_list|,
name|ReplicationOperation
operator|.
name|this
operator|::
name|onPrimaryDemoted
argument_list|,
name|throwable
lambda|->
name|decPendingAndFinishIfNeeded
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|onPrimaryDemoted
specifier|private
name|void
name|onPrimaryDemoted
parameter_list|(
name|Throwable
name|demotionFailure
parameter_list|)
block|{
name|String
name|primaryFail
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"primary shard [%s] was demoted while failing replica shard"
argument_list|,
name|primary
operator|.
name|routingEntry
argument_list|()
argument_list|)
decl_stmt|;
comment|// we are no longer the primary, fail ourselves and start over
name|primary
operator|.
name|failShard
argument_list|(
name|primaryFail
argument_list|,
name|demotionFailure
argument_list|)
expr_stmt|;
name|finishAsFailed
argument_list|(
operator|new
name|RetryOnPrimaryException
argument_list|(
name|primary
operator|.
name|routingEntry
argument_list|()
operator|.
name|shardId
argument_list|()
argument_list|,
name|primaryFail
argument_list|,
name|demotionFailure
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * checks whether we can perform a write based on the write consistency setting      * returns **null* if OK to proceed, or a string describing the reason to stop      */
DECL|method|checkWriteConsistency
name|String
name|checkWriteConsistency
parameter_list|()
block|{
assert|assert
name|request
operator|.
name|consistencyLevel
argument_list|()
operator|!=
name|WriteConsistencyLevel
operator|.
name|DEFAULT
operator|:
literal|"consistency level should be set"
assert|;
specifier|final
name|ShardId
name|shardId
init|=
name|primary
operator|.
name|routingEntry
argument_list|()
operator|.
name|shardId
argument_list|()
decl_stmt|;
specifier|final
name|ClusterState
name|state
init|=
name|clusterStateSupplier
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|WriteConsistencyLevel
name|consistencyLevel
init|=
name|request
operator|.
name|consistencyLevel
argument_list|()
decl_stmt|;
specifier|final
name|int
name|sizeActive
decl_stmt|;
specifier|final
name|int
name|requiredNumber
decl_stmt|;
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|state
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|index
argument_list|(
name|shardId
operator|.
name|getIndexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|!=
literal|null
condition|)
block|{
name|IndexShardRoutingTable
name|shardRoutingTable
init|=
name|indexRoutingTable
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
name|shardRoutingTable
operator|!=
literal|null
condition|)
block|{
name|sizeActive
operator|=
name|shardRoutingTable
operator|.
name|activeShards
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
if|if
condition|(
name|consistencyLevel
operator|==
name|WriteConsistencyLevel
operator|.
name|QUORUM
operator|&&
name|shardRoutingTable
operator|.
name|getSize
argument_list|()
operator|>
literal|2
condition|)
block|{
comment|// only for more than 2 in the number of shardIt it makes sense, otherwise its 1 shard with 1 replica,
comment|// quorum is 1 (which is what it is initialized to)
name|requiredNumber
operator|=
operator|(
name|shardRoutingTable
operator|.
name|getSize
argument_list|()
operator|/
literal|2
operator|)
operator|+
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|consistencyLevel
operator|==
name|WriteConsistencyLevel
operator|.
name|ALL
condition|)
block|{
name|requiredNumber
operator|=
name|shardRoutingTable
operator|.
name|getSize
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|requiredNumber
operator|=
literal|1
expr_stmt|;
block|}
block|}
else|else
block|{
name|sizeActive
operator|=
literal|0
expr_stmt|;
name|requiredNumber
operator|=
literal|1
expr_stmt|;
block|}
block|}
else|else
block|{
name|sizeActive
operator|=
literal|0
expr_stmt|;
name|requiredNumber
operator|=
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|sizeActive
operator|<
name|requiredNumber
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}] not enough active copies to meet write consistency of [{}] (have {}, needed {}), scheduling a retry."
operator|+
literal|" op [{}], request [{}]"
argument_list|,
name|shardId
argument_list|,
name|consistencyLevel
argument_list|,
name|sizeActive
argument_list|,
name|requiredNumber
argument_list|,
name|opType
argument_list|,
name|request
argument_list|)
expr_stmt|;
return|return
literal|"Not enough active copies to meet write consistency of ["
operator|+
name|consistencyLevel
operator|+
literal|"] (have "
operator|+
name|sizeActive
operator|+
literal|", needed "
operator|+
name|requiredNumber
operator|+
literal|")."
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|method|getShards
specifier|protected
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|getShards
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
comment|// can be null if the index is deleted / closed on us..
specifier|final
name|IndexShardRoutingTable
name|shardRoutingTable
init|=
name|state
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|shardRoutingTableOrNull
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shards
init|=
name|shardRoutingTable
operator|==
literal|null
condition|?
name|Collections
operator|.
name|emptyList
argument_list|()
else|:
name|shardRoutingTable
operator|.
name|shards
argument_list|()
decl_stmt|;
return|return
name|shards
return|;
block|}
DECL|method|decPendingAndFinishIfNeeded
specifier|private
name|void
name|decPendingAndFinishIfNeeded
parameter_list|()
block|{
assert|assert
name|pendingShards
operator|.
name|get
argument_list|()
operator|>
literal|0
assert|;
if|if
condition|(
name|pendingShards
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|finish
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|finish
specifier|private
name|void
name|finish
parameter_list|()
block|{
if|if
condition|(
name|finished
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
specifier|final
name|ReplicationResponse
operator|.
name|ShardInfo
operator|.
name|Failure
index|[]
name|failuresArray
decl_stmt|;
if|if
condition|(
name|shardReplicaFailures
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|failuresArray
operator|=
name|ReplicationResponse
operator|.
name|EMPTY
expr_stmt|;
block|}
else|else
block|{
name|failuresArray
operator|=
operator|new
name|ReplicationResponse
operator|.
name|ShardInfo
operator|.
name|Failure
index|[
name|shardReplicaFailures
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|shardReplicaFailures
operator|.
name|toArray
argument_list|(
name|failuresArray
argument_list|)
expr_stmt|;
block|}
name|primaryResult
operator|.
name|setShardInfo
argument_list|(
operator|new
name|ReplicationResponse
operator|.
name|ShardInfo
argument_list|(
name|totalShards
operator|.
name|get
argument_list|()
argument_list|,
name|successfulShards
operator|.
name|get
argument_list|()
argument_list|,
name|failuresArray
argument_list|)
argument_list|)
expr_stmt|;
name|resultListener
operator|.
name|onResponse
argument_list|(
name|primaryResult
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|finishAsFailed
specifier|private
name|void
name|finishAsFailed
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
block|{
if|if
condition|(
name|finished
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|resultListener
operator|.
name|onFailure
argument_list|(
name|throwable
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Should an exception be ignored when the operation is performed on the replica.      */
DECL|method|ignoreReplicaException
specifier|public
specifier|static
name|boolean
name|ignoreReplicaException
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|TransportActions
operator|.
name|isShardNotAvailableException
argument_list|(
name|e
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// on version conflict or document missing, it means
comment|// that a new change has crept into the replica, and it's fine
if|if
condition|(
name|isConflictException
argument_list|(
name|e
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
DECL|method|isConflictException
specifier|public
specifier|static
name|boolean
name|isConflictException
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|e
argument_list|)
decl_stmt|;
comment|// on version conflict or document missing, it means
comment|// that a new change has crept into the replica, and it's fine
if|if
condition|(
name|cause
operator|instanceof
name|VersionConflictEngineException
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
DECL|interface|Primary
interface|interface
name|Primary
parameter_list|<
name|Request
extends|extends
name|ReplicationRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|,
name|ReplicaRequest
extends|extends
name|ReplicationRequest
parameter_list|<
name|ReplicaRequest
parameter_list|>
parameter_list|,
name|PrimaryResultT
extends|extends
name|PrimaryResult
parameter_list|<
name|ReplicaRequest
parameter_list|>
parameter_list|>
block|{
comment|/**          * routing entry for this primary          */
DECL|method|routingEntry
name|ShardRouting
name|routingEntry
parameter_list|()
function_decl|;
comment|/**          * fail the primary, typically due to the fact that the operation has learned the primary has been demoted by the master          */
DECL|method|failShard
name|void
name|failShard
parameter_list|(
name|String
name|message
parameter_list|,
name|Throwable
name|throwable
parameter_list|)
function_decl|;
comment|/**          * Performs the given request on this primary. Yes, this returns as soon as it can with the request for the replicas and calls a          * listener when the primary request is completed. Yes, the primary request might complete before the method returns. Yes, it might          * also complete after. Deal with it.          *          * @param request the request to perform          * @return the request to send to the repicas          */
DECL|method|perform
name|PrimaryResultT
name|perform
parameter_list|(
name|Request
name|request
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**          * Notifies the primary of a local checkpoint for the given allocation.          *          * Note: The primary will use this information to advance the global checkpoint if possible.          *          * @param allocationId allocation ID of the shard corresponding to the supplied local checkpoint          * @param checkpoint the *local* checkpoint for the shard          */
DECL|method|updateLocalCheckpointForShard
name|void
name|updateLocalCheckpointForShard
parameter_list|(
name|String
name|allocationId
parameter_list|,
name|long
name|checkpoint
parameter_list|)
function_decl|;
comment|/** returns the local checkpoint of the primary shard */
DECL|method|localCheckpoint
name|long
name|localCheckpoint
parameter_list|()
function_decl|;
block|}
DECL|interface|Replicas
interface|interface
name|Replicas
parameter_list|<
name|ReplicaRequest
extends|extends
name|ReplicationRequest
parameter_list|<
name|ReplicaRequest
parameter_list|>
parameter_list|>
block|{
comment|/**          * performs the the given request on the specified replica          *          * @param replica        {@link ShardRouting} of the shard this request should be executed on          * @param replicaRequest operation to peform          * @param listener       a callback to call once the operation has been complicated, either successfully or with an error.          */
DECL|method|performOn
name|void
name|performOn
parameter_list|(
name|ShardRouting
name|replica
parameter_list|,
name|ReplicaRequest
name|replicaRequest
parameter_list|,
name|ActionListener
argument_list|<
name|ReplicaResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**          * Fail the specified shard, removing it from the current set of active shards          *          * @param replica          shard to fail          * @param primary          the primary shard that requested the failure          * @param message          a (short) description of the reason          * @param throwable        the original exception which caused the ReplicationOperation to request the shard to be failed          * @param onSuccess        a callback to call when the shard has been successfully removed from the active set.          * @param onPrimaryDemoted a callback to call when the shard can not be failed because the current primary has been demoted          *                         by the master.          * @param onIgnoredFailure a callback to call when failing a shard has failed, but it that failure can be safely ignored and the          *                         replication operation can finish processing          *                         Note: this callback should be used in extreme situations, typically node shutdown.          */
DECL|method|failShard
name|void
name|failShard
parameter_list|(
name|ShardRouting
name|replica
parameter_list|,
name|ShardRouting
name|primary
parameter_list|,
name|String
name|message
parameter_list|,
name|Throwable
name|throwable
parameter_list|,
name|Runnable
name|onSuccess
parameter_list|,
name|Consumer
argument_list|<
name|Throwable
argument_list|>
name|onPrimaryDemoted
parameter_list|,
name|Consumer
argument_list|<
name|Throwable
argument_list|>
name|onIgnoredFailure
parameter_list|)
function_decl|;
block|}
comment|/**      * An interface to encapsulate the metadata needed from replica shards when they respond to operations performed on them      */
DECL|interface|ReplicaResponse
interface|interface
name|ReplicaResponse
block|{
comment|/** the local check point for the shard. see {@link org.elasticsearch.index.seqno.SequenceNumbersService#getLocalCheckpoint()} */
DECL|method|localCheckpoint
name|long
name|localCheckpoint
parameter_list|()
function_decl|;
comment|/** the allocation id of the replica shard */
DECL|method|allocationId
name|String
name|allocationId
parameter_list|()
function_decl|;
block|}
DECL|class|RetryOnPrimaryException
specifier|public
specifier|static
class|class
name|RetryOnPrimaryException
extends|extends
name|ElasticsearchException
block|{
DECL|method|RetryOnPrimaryException
specifier|public
name|RetryOnPrimaryException
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|msg
parameter_list|)
block|{
name|this
argument_list|(
name|shardId
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|RetryOnPrimaryException
specifier|public
name|RetryOnPrimaryException
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|msg
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|setShard
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
block|}
DECL|method|RetryOnPrimaryException
specifier|public
name|RetryOnPrimaryException
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
DECL|interface|PrimaryResult
interface|interface
name|PrimaryResult
parameter_list|<
name|R
extends|extends
name|ReplicationRequest
parameter_list|<
name|R
parameter_list|>
parameter_list|>
block|{
DECL|method|replicaRequest
name|R
name|replicaRequest
parameter_list|()
function_decl|;
DECL|method|setShardInfo
name|void
name|setShardInfo
parameter_list|(
name|ReplicationResponse
operator|.
name|ShardInfo
name|shardInfo
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

