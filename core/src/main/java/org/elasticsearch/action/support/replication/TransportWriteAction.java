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
name|function
operator|.
name|Supplier
import|;
end_import

begin_comment
comment|/**  * Base class for transport actions that modify data in some shard like index, delete, and shardBulk.  */
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
name|Request
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
name|request
argument_list|,
name|executor
argument_list|)
expr_stmt|;
block|}
comment|/**      * Called on the primary with a reference to the {@linkplain IndexShard} to modify.      */
DECL|method|onPrimaryShard
specifier|protected
specifier|abstract
name|WriteResult
argument_list|<
name|Response
argument_list|>
name|onPrimaryShard
parameter_list|(
name|Request
name|request
parameter_list|,
name|IndexShard
name|indexShard
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**      * Called once per replica with a reference to the {@linkplain IndexShard} to modify.      *      * @return the translog location of the {@linkplain IndexShard} after the write was completed or null if no write occurred      */
DECL|method|onReplicaShard
specifier|protected
specifier|abstract
name|Translog
operator|.
name|Location
name|onReplicaShard
parameter_list|(
name|Request
name|request
parameter_list|,
name|IndexShard
name|indexShard
parameter_list|)
function_decl|;
annotation|@
name|Override
DECL|method|shardOperationOnPrimary
specifier|protected
specifier|final
name|WritePrimaryResult
name|shardOperationOnPrimary
parameter_list|(
name|Request
name|request
parameter_list|)
throws|throws
name|Exception
block|{
name|IndexShard
name|indexShard
init|=
name|indexShard
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|WriteResult
argument_list|<
name|Response
argument_list|>
name|result
init|=
name|onPrimaryShard
argument_list|(
name|request
argument_list|,
name|indexShard
argument_list|)
decl_stmt|;
return|return
operator|new
name|WritePrimaryResult
argument_list|(
name|request
argument_list|,
name|result
operator|.
name|getResponse
argument_list|()
argument_list|,
name|result
operator|.
name|getLocation
argument_list|()
argument_list|,
name|indexShard
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperationOnReplica
specifier|protected
specifier|final
name|WriteReplicaResult
name|shardOperationOnReplica
parameter_list|(
name|Request
name|request
parameter_list|)
block|{
name|IndexShard
name|indexShard
init|=
name|indexShard
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|Translog
operator|.
name|Location
name|location
init|=
name|onReplicaShard
argument_list|(
name|request
argument_list|,
name|indexShard
argument_list|)
decl_stmt|;
return|return
operator|new
name|WriteReplicaResult
argument_list|(
name|indexShard
argument_list|,
name|request
argument_list|,
name|location
argument_list|)
return|;
block|}
comment|/**      * Fetch the IndexShard for the request. Protected so it can be mocked in tests.      */
DECL|method|indexShard
specifier|protected
name|IndexShard
name|indexShard
parameter_list|(
name|Request
name|request
parameter_list|)
block|{
specifier|final
name|ShardId
name|shardId
init|=
name|request
operator|.
name|shardId
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|indexService
operator|.
name|getShard
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Simple result from a write action. Write actions have static method to return these so they can integrate with bulk.      */
DECL|class|WriteResult
specifier|public
specifier|static
class|class
name|WriteResult
parameter_list|<
name|Response
extends|extends
name|ReplicationResponse
parameter_list|>
block|{
DECL|field|response
specifier|private
specifier|final
name|Response
name|response
decl_stmt|;
DECL|field|location
specifier|private
specifier|final
name|Translog
operator|.
name|Location
name|location
decl_stmt|;
DECL|method|WriteResult
specifier|public
name|WriteResult
parameter_list|(
name|Response
name|response
parameter_list|,
annotation|@
name|Nullable
name|Location
name|location
parameter_list|)
block|{
name|this
operator|.
name|response
operator|=
name|response
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
DECL|method|getResponse
specifier|public
name|Response
name|getResponse
parameter_list|()
block|{
return|return
name|response
return|;
block|}
DECL|method|getLocation
specifier|public
name|Translog
operator|.
name|Location
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
block|}
comment|/**      * Result of taking the action on the primary.      */
DECL|class|WritePrimaryResult
class|class
name|WritePrimaryResult
extends|extends
name|PrimaryResult
implements|implements
name|RespondingWriteResult
block|{
DECL|field|finishedAsyncActions
name|boolean
name|finishedAsyncActions
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
name|Request
name|request
parameter_list|,
name|Response
name|finalResponse
parameter_list|,
annotation|@
name|Nullable
name|Translog
operator|.
name|Location
name|location
parameter_list|,
name|IndexShard
name|indexShard
parameter_list|)
block|{
name|super
argument_list|(
name|request
argument_list|,
name|finalResponse
argument_list|)
expr_stmt|;
comment|/*              * We call this before replication because this might wait for a refresh and that can take a while. This way we wait for the              * refresh in parallel on the primary and on the replica.              */
name|postWriteActions
argument_list|(
name|indexShard
argument_list|,
name|request
argument_list|,
name|location
argument_list|,
name|this
argument_list|,
name|logger
argument_list|)
expr_stmt|;
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
argument_list|()
expr_stmt|;
block|}
comment|/**          * Respond if the refresh has occurred and the listener is ready. Always called while synchronized on {@code this}.          */
DECL|method|respondIfPossible
specifier|protected
name|void
name|respondIfPossible
parameter_list|()
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
name|super
operator|.
name|respond
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|respondAfterAsyncAction
specifier|public
specifier|synchronized
name|void
name|respondAfterAsyncAction
parameter_list|(
name|boolean
name|forcedRefresh
parameter_list|)
block|{
name|finalResponse
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
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Result of taking the action on the replica.      */
DECL|class|WriteReplicaResult
class|class
name|WriteReplicaResult
extends|extends
name|ReplicaResult
implements|implements
name|RespondingWriteResult
block|{
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
name|IndexShard
name|indexShard
parameter_list|,
name|ReplicatedWriteRequest
argument_list|<
name|?
argument_list|>
name|request
parameter_list|,
name|Translog
operator|.
name|Location
name|location
parameter_list|)
block|{
name|postWriteActions
argument_list|(
name|indexShard
argument_list|,
name|request
argument_list|,
name|location
argument_list|,
name|this
argument_list|,
name|logger
argument_list|)
expr_stmt|;
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
argument_list|()
expr_stmt|;
block|}
comment|/**          * Respond if the refresh has occurred and the listener is ready. Always called while synchronized on {@code this}.          */
DECL|method|respondIfPossible
specifier|protected
name|void
name|respondIfPossible
parameter_list|()
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
name|super
operator|.
name|respond
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|respondAfterAsyncAction
specifier|public
specifier|synchronized
name|void
name|respondAfterAsyncAction
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
argument_list|()
expr_stmt|;
block|}
block|}
DECL|interface|RespondingWriteResult
specifier|private
interface|interface
name|RespondingWriteResult
block|{
DECL|method|respondAfterAsyncAction
name|void
name|respondAfterAsyncAction
parameter_list|(
name|boolean
name|forcedRefresh
parameter_list|)
function_decl|;
block|}
DECL|method|postWriteActions
specifier|static
name|void
name|postWriteActions
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
name|ESLogger
name|logger
parameter_list|)
block|{
name|boolean
name|pendingOps
init|=
literal|false
decl_stmt|;
name|boolean
name|immediateRefresh
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
name|immediateRefresh
operator|=
literal|true
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
name|pendingOps
operator|=
literal|true
expr_stmt|;
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
name|respond
operator|.
name|respondAfterAsyncAction
argument_list|(
name|forcedRefresh
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|NONE
case|:
break|break;
block|}
name|boolean
name|fsyncTranslog
init|=
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
decl_stmt|;
if|if
condition|(
name|fsyncTranslog
condition|)
block|{
name|indexShard
operator|.
name|sync
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
name|indexShard
operator|.
name|maybeFlush
argument_list|()
expr_stmt|;
if|if
condition|(
name|pendingOps
operator|==
literal|false
condition|)
block|{
name|respond
operator|.
name|respondAfterAsyncAction
argument_list|(
name|immediateRefresh
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

