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
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
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
name|bulk
operator|.
name|BulkRequest
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
name|bulk
operator|.
name|BulkShardRequest
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
name|ActionFilters
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
name|WriteRequest
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
name|WriteResponse
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
name|action
operator|.
name|shard
operator|.
name|ShardStateAction
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
name|block
operator|.
name|ClusterBlockLevel
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
name|IndexNameExpressionResolver
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
name|service
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
name|translog
operator|.
name|Translog
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
name|translog
operator|.
name|Translog
operator|.
name|Location
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
name|TransportResponse
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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

begin_comment
comment|/**  * Base class for transport actions that modify data in some shard like index, delete, and shardBulk.  * Allows performing async actions (e.g. refresh) after performing write operations on primary and replica shards  */
end_comment

begin_class
DECL|class|TransportWriteAction
specifier|public
specifier|abstract
class|class
name|TransportWriteAction
parameter_list|<
name|Request
extends|extends
name|ReplicatedWriteRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|,
name|ReplicaRequest
extends|extends
name|ReplicatedWriteRequest
parameter_list|<
name|ReplicaRequest
parameter_list|>
parameter_list|,
name|Response
extends|extends
name|ReplicationResponse
operator|&
name|WriteResponse
parameter_list|>
extends|extends
name|TransportReplicationAction
argument_list|<
name|Request
argument_list|,
name|ReplicaRequest
argument_list|,
name|Response
argument_list|>
block|{
DECL|method|TransportWriteAction
specifier|protected
name|TransportWriteAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|actionName
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ShardStateAction
name|shardStateAction
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|Supplier
argument_list|<
name|Request
argument_list|>
name|request
parameter_list|,
name|Supplier
argument_list|<
name|ReplicaRequest
argument_list|>
name|replicaRequest
parameter_list|,
name|String
name|executor
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|actionName
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|indicesService
argument_list|,
name|threadPool
argument_list|,
name|shardStateAction
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|request
argument_list|,
name|replicaRequest
argument_list|,
name|executor
argument_list|)
expr_stmt|;
block|}
comment|/**      * Called on the primary with a reference to the primary {@linkplain IndexShard} to modify.      *      * @return the result of the operation on primary, including current translog location and operation response and failure      * async refresh is performed on the<code>primary</code> shard according to the<code>Request</code> refresh policy      */
annotation|@
name|Override
DECL|method|shardOperationOnPrimary
specifier|protected
specifier|abstract
name|WritePrimaryResult
argument_list|<
name|ReplicaRequest
argument_list|,
name|Response
argument_list|>
name|shardOperationOnPrimary
parameter_list|(
name|Request
name|request
parameter_list|,
name|IndexShard
name|primary
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**      * Called once per replica with a reference to the replica {@linkplain IndexShard} to modify.      *      * @return the result of the operation on replica, including current translog location and operation response and failure      * async refresh is performed on the<code>replica</code> shard according to the<code>ReplicaRequest</code> refresh policy      */
annotation|@
name|Override
DECL|method|shardOperationOnReplica
specifier|protected
specifier|abstract
name|WriteReplicaResult
argument_list|<
name|ReplicaRequest
argument_list|>
name|shardOperationOnReplica
parameter_list|(
name|ReplicaRequest
name|request
parameter_list|,
name|IndexShard
name|replica
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**      * Result of taking the action on the primary.      */
DECL|class|WritePrimaryResult
specifier|protected
specifier|static
class|class
name|WritePrimaryResult
parameter_list|<
name|ReplicaRequest
extends|extends
name|ReplicatedWriteRequest
parameter_list|<
name|ReplicaRequest
parameter_list|>
parameter_list|,
name|Response
extends|extends
name|ReplicationResponse
operator|&
name|WriteResponse
parameter_list|>
extends|extends
name|PrimaryResult
argument_list|<
name|ReplicaRequest
argument_list|,
name|Response
argument_list|>
implements|implements
name|RespondingWriteResult
block|{
DECL|field|finishedAsyncActions
name|boolean
name|finishedAsyncActions
decl_stmt|;
DECL|field|location
specifier|public
specifier|final
name|Location
name|location
decl_stmt|;
DECL|field|listener
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
init|=
literal|null
decl_stmt|;
DECL|method|WritePrimaryResult
specifier|public
name|WritePrimaryResult
parameter_list|(
name|ReplicaRequest
name|request
parameter_list|,
annotation|@
name|Nullable
name|Response
name|finalResponse
parameter_list|,
annotation|@
name|Nullable
name|Location
name|location
parameter_list|,
annotation|@
name|Nullable
name|Exception
name|operationFailure
parameter_list|,
name|IndexShard
name|primary
parameter_list|,
name|Logger
name|logger
parameter_list|)
block|{
name|super
argument_list|(
name|request
argument_list|,
name|finalResponse
argument_list|,
name|operationFailure
argument_list|)
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
assert|assert
name|location
operator|==
literal|null
operator|||
name|operationFailure
operator|==
literal|null
operator|:
literal|"expected either failure to be null or translog location to be null, "
operator|+
literal|"but found: ["
operator|+
name|location
operator|+
literal|"] translog location and ["
operator|+
name|operationFailure
operator|+
literal|"] failure"
assert|;
if|if
condition|(
name|operationFailure
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|finishedAsyncActions
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|/*                  * We call this before replication because this might wait for a refresh and that can take a while.                  * This way we wait for the refresh in parallel on the primary and on the replica.                  */
operator|new
name|AsyncAfterWriteAction
argument_list|(
name|primary
argument_list|,
name|request
argument_list|,
name|location
argument_list|,
name|this
argument_list|,
name|logger
argument_list|)
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|respond
specifier|public
specifier|synchronized
name|void
name|respond
parameter_list|(
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|respondIfPossible
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**          * Respond if the refresh has occurred and the listener is ready. Always called while synchronized on {@code this}.          */
DECL|method|respondIfPossible
specifier|protected
name|void
name|respondIfPossible
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
if|if
condition|(
name|finishedAsyncActions
operator|&&
name|listener
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|ex
operator|==
literal|null
condition|)
block|{
name|super
operator|.
name|respond
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|onFailure
specifier|public
specifier|synchronized
name|void
name|onFailure
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
name|finishedAsyncActions
operator|=
literal|true
expr_stmt|;
name|respondIfPossible
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onSuccess
specifier|public
specifier|synchronized
name|void
name|onSuccess
parameter_list|(
name|boolean
name|forcedRefresh
parameter_list|)
block|{
name|finalResponseIfSuccessful
operator|.
name|setForcedRefresh
argument_list|(
name|forcedRefresh
argument_list|)
expr_stmt|;
name|finishedAsyncActions
operator|=
literal|true
expr_stmt|;
name|respondIfPossible
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Result of taking the action on the replica.      */
DECL|class|WriteReplicaResult
specifier|protected
specifier|static
class|class
name|WriteReplicaResult
parameter_list|<
name|ReplicaRequest
extends|extends
name|ReplicatedWriteRequest
parameter_list|<
name|ReplicaRequest
parameter_list|>
parameter_list|>
extends|extends
name|ReplicaResult
implements|implements
name|RespondingWriteResult
block|{
DECL|field|location
specifier|public
specifier|final
name|Location
name|location
decl_stmt|;
DECL|field|finishedAsyncActions
name|boolean
name|finishedAsyncActions
decl_stmt|;
DECL|field|listener
specifier|private
name|ActionListener
argument_list|<
name|TransportResponse
operator|.
name|Empty
argument_list|>
name|listener
decl_stmt|;
DECL|method|WriteReplicaResult
specifier|public
name|WriteReplicaResult
parameter_list|(
name|ReplicaRequest
name|request
parameter_list|,
annotation|@
name|Nullable
name|Location
name|location
parameter_list|,
annotation|@
name|Nullable
name|Exception
name|operationFailure
parameter_list|,
name|IndexShard
name|replica
parameter_list|,
name|Logger
name|logger
parameter_list|)
block|{
name|super
argument_list|(
name|operationFailure
argument_list|)
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
if|if
condition|(
name|operationFailure
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|finishedAsyncActions
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
operator|new
name|AsyncAfterWriteAction
argument_list|(
name|replica
argument_list|,
name|request
argument_list|,
name|location
argument_list|,
name|this
argument_list|,
name|logger
argument_list|)
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|respond
specifier|public
name|void
name|respond
parameter_list|(
name|ActionListener
argument_list|<
name|TransportResponse
operator|.
name|Empty
argument_list|>
name|listener
parameter_list|)
block|{
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|respondIfPossible
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**          * Respond if the refresh has occurred and the listener is ready. Always called while synchronized on {@code this}.          */
DECL|method|respondIfPossible
specifier|protected
name|void
name|respondIfPossible
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
if|if
condition|(
name|finishedAsyncActions
operator|&&
name|listener
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|ex
operator|==
literal|null
condition|)
block|{
name|super
operator|.
name|respond
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|finishedAsyncActions
operator|=
literal|true
expr_stmt|;
name|respondIfPossible
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onSuccess
specifier|public
specifier|synchronized
name|void
name|onSuccess
parameter_list|(
name|boolean
name|forcedRefresh
parameter_list|)
block|{
name|finishedAsyncActions
operator|=
literal|true
expr_stmt|;
name|respondIfPossible
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|globalBlockLevel
specifier|protected
name|ClusterBlockLevel
name|globalBlockLevel
parameter_list|()
block|{
return|return
name|ClusterBlockLevel
operator|.
name|WRITE
return|;
block|}
annotation|@
name|Override
DECL|method|indexBlockLevel
specifier|protected
name|ClusterBlockLevel
name|indexBlockLevel
parameter_list|()
block|{
return|return
name|ClusterBlockLevel
operator|.
name|WRITE
return|;
block|}
comment|/**      * callback used by {@link AsyncAfterWriteAction} to notify that all post      * process actions have been executed      */
DECL|interface|RespondingWriteResult
interface|interface
name|RespondingWriteResult
block|{
comment|/**          * Called on successful processing of all post write actions          * @param forcedRefresh<code>true</code> iff this write has caused a refresh          */
DECL|method|onSuccess
name|void
name|onSuccess
parameter_list|(
name|boolean
name|forcedRefresh
parameter_list|)
function_decl|;
comment|/**          * Called on failure if a post action failed.          */
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|Exception
name|ex
parameter_list|)
function_decl|;
block|}
comment|/**      * This class encapsulates post write actions like async waits for      * translog syncs or waiting for a refresh to happen making the write operation      * visible.      */
DECL|class|AsyncAfterWriteAction
specifier|static
specifier|final
class|class
name|AsyncAfterWriteAction
block|{
DECL|field|location
specifier|private
specifier|final
name|Location
name|location
decl_stmt|;
DECL|field|waitUntilRefresh
specifier|private
specifier|final
name|boolean
name|waitUntilRefresh
decl_stmt|;
DECL|field|sync
specifier|private
specifier|final
name|boolean
name|sync
decl_stmt|;
DECL|field|pendingOps
specifier|private
specifier|final
name|AtomicInteger
name|pendingOps
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
decl_stmt|;
DECL|field|refreshed
specifier|private
specifier|final
name|AtomicBoolean
name|refreshed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
DECL|field|syncFailure
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|syncFailure
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
DECL|field|respond
specifier|private
specifier|final
name|RespondingWriteResult
name|respond
decl_stmt|;
DECL|field|indexShard
specifier|private
specifier|final
name|IndexShard
name|indexShard
decl_stmt|;
DECL|field|request
specifier|private
specifier|final
name|WriteRequest
argument_list|<
name|?
argument_list|>
name|request
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|method|AsyncAfterWriteAction
name|AsyncAfterWriteAction
parameter_list|(
specifier|final
name|IndexShard
name|indexShard
parameter_list|,
specifier|final
name|WriteRequest
argument_list|<
name|?
argument_list|>
name|request
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|Translog
operator|.
name|Location
name|location
parameter_list|,
specifier|final
name|RespondingWriteResult
name|respond
parameter_list|,
specifier|final
name|Logger
name|logger
parameter_list|)
block|{
name|this
operator|.
name|indexShard
operator|=
name|indexShard
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|boolean
name|waitUntilRefresh
init|=
literal|false
decl_stmt|;
switch|switch
condition|(
name|request
operator|.
name|getRefreshPolicy
argument_list|()
condition|)
block|{
case|case
name|IMMEDIATE
case|:
name|indexShard
operator|.
name|refresh
argument_list|(
literal|"refresh_flag_index"
argument_list|)
expr_stmt|;
name|refreshed
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
break|break;
case|case
name|WAIT_UNTIL
case|:
if|if
condition|(
name|location
operator|!=
literal|null
condition|)
block|{
name|waitUntilRefresh
operator|=
literal|true
expr_stmt|;
name|pendingOps
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|NONE
case|:
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown refresh policy: "
operator|+
name|request
operator|.
name|getRefreshPolicy
argument_list|()
argument_list|)
throw|;
block|}
name|this
operator|.
name|waitUntilRefresh
operator|=
name|waitUntilRefresh
expr_stmt|;
name|this
operator|.
name|respond
operator|=
name|respond
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
if|if
condition|(
operator|(
name|sync
operator|=
name|indexShard
operator|.
name|getTranslogDurability
argument_list|()
operator|==
name|Translog
operator|.
name|Durability
operator|.
name|REQUEST
operator|&&
name|location
operator|!=
literal|null
operator|)
condition|)
block|{
name|pendingOps
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
assert|assert
name|pendingOps
operator|.
name|get
argument_list|()
operator|>=
literal|0
operator|&&
name|pendingOps
operator|.
name|get
argument_list|()
operator|<=
literal|3
operator|:
literal|"pendingOpts was: "
operator|+
name|pendingOps
operator|.
name|get
argument_list|()
assert|;
block|}
comment|/** calls the response listener if all pending operations have returned otherwise it just decrements the pending opts counter.*/
DECL|method|maybeFinish
specifier|private
name|void
name|maybeFinish
parameter_list|()
block|{
specifier|final
name|int
name|numPending
init|=
name|pendingOps
operator|.
name|decrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|numPending
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|syncFailure
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|respond
operator|.
name|onFailure
argument_list|(
name|syncFailure
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|respond
operator|.
name|onSuccess
argument_list|(
name|refreshed
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
assert|assert
name|numPending
operator|>=
literal|0
operator|&&
name|numPending
operator|<=
literal|2
operator|:
literal|"numPending must either 2, 1 or 0 but was "
operator|+
name|numPending
assert|;
block|}
DECL|method|run
name|void
name|run
parameter_list|()
block|{
comment|// we either respond immediately ie. if we we don't fsync per request or wait for refresh
comment|// OR we got an pass async operations on and wait for them to return to respond.
name|indexShard
operator|.
name|maybeFlush
argument_list|()
expr_stmt|;
name|maybeFinish
argument_list|()
expr_stmt|;
comment|// decrement the pendingOpts by one, if there is nothing else to do we just respond with success.
if|if
condition|(
name|waitUntilRefresh
condition|)
block|{
assert|assert
name|pendingOps
operator|.
name|get
argument_list|()
operator|>
literal|0
assert|;
name|indexShard
operator|.
name|addRefreshListener
argument_list|(
name|location
argument_list|,
name|forcedRefresh
lambda|->
block|{
if|if
condition|(
name|forcedRefresh
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"block_until_refresh request ran out of slots and forced a refresh: [{}]"
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
name|refreshed
operator|.
name|set
argument_list|(
name|forcedRefresh
argument_list|)
expr_stmt|;
name|maybeFinish
argument_list|()
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sync
condition|)
block|{
assert|assert
name|pendingOps
operator|.
name|get
argument_list|()
operator|>
literal|0
assert|;
name|indexShard
operator|.
name|sync
argument_list|(
name|location
argument_list|,
parameter_list|(
name|ex
parameter_list|)
lambda|->
block|{
name|syncFailure
operator|.
name|set
argument_list|(
name|ex
argument_list|)
expr_stmt|;
name|maybeFinish
argument_list|()
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

