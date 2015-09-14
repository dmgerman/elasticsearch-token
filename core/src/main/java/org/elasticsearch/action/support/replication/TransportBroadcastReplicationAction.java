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
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|IntObjectCursor
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
name|ActionWriteResponse
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
name|HandledTransportAction
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
name|action
operator|.
name|support
operator|.
name|broadcast
operator|.
name|BroadcastRequest
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
name|BroadcastResponse
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
name|util
operator|.
name|concurrent
operator|.
name|CountDown
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
name|Arrays
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
name|CopyOnWriteArrayList
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
comment|/**  * Base class for requests that should be executed on all shards of an index or several indices.  * This action sends shard requests to all primary shards of the indices and they are then replicated like write requests  */
end_comment

begin_class
DECL|class|TransportBroadcastReplicationAction
specifier|public
specifier|abstract
class|class
name|TransportBroadcastReplicationAction
parameter_list|<
name|Request
extends|extends
name|BroadcastRequest
parameter_list|,
name|Response
extends|extends
name|BroadcastResponse
parameter_list|,
name|ShardRequest
extends|extends
name|ReplicationRequest
parameter_list|,
name|ShardResponse
extends|extends
name|ActionWriteResponse
parameter_list|>
extends|extends
name|HandledTransportAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|field|replicatedBroadcastShardAction
specifier|private
specifier|final
name|TransportReplicationAction
name|replicatedBroadcastShardAction
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|method|TransportBroadcastReplicationAction
specifier|public
name|TransportBroadcastReplicationAction
parameter_list|(
name|String
name|name
parameter_list|,
name|Supplier
argument_list|<
name|Request
argument_list|>
name|request
parameter_list|,
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
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|TransportReplicationAction
name|replicatedBroadcastShardAction
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|name
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicatedBroadcastShardAction
operator|=
name|replicatedBroadcastShardAction
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
specifier|final
name|Request
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ShardId
argument_list|>
name|shards
init|=
name|shards
argument_list|(
name|request
argument_list|,
name|clusterState
argument_list|)
decl_stmt|;
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|ShardResponse
argument_list|>
name|shardsResponses
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|shards
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|finishAndNotifyListener
argument_list|(
name|listener
argument_list|,
name|shardsResponses
argument_list|)
expr_stmt|;
block|}
specifier|final
name|CountDown
name|responsesCountDown
init|=
operator|new
name|CountDown
argument_list|(
name|shards
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|ShardId
name|shardId
range|:
name|shards
control|)
block|{
name|ActionListener
argument_list|<
name|ShardResponse
argument_list|>
name|shardActionListener
init|=
operator|new
name|ActionListener
argument_list|<
name|ShardResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ShardResponse
name|shardResponse
parameter_list|)
block|{
name|shardsResponses
operator|.
name|add
argument_list|(
name|shardResponse
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: got response from {}"
argument_list|,
name|actionName
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
if|if
condition|(
name|responsesCountDown
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|finishAndNotifyListener
argument_list|(
name|listener
argument_list|,
name|shardsResponses
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: got failure from {}"
argument_list|,
name|actionName
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|int
name|totalNumCopies
init|=
name|clusterState
operator|.
name|getMetaData
argument_list|()
operator|.
name|index
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getNumberOfReplicas
argument_list|()
operator|+
literal|1
decl_stmt|;
name|ShardResponse
name|shardResponse
init|=
name|newShardResponse
argument_list|()
decl_stmt|;
name|ActionWriteResponse
operator|.
name|ShardInfo
operator|.
name|Failure
index|[]
name|failures
decl_stmt|;
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
name|failures
operator|=
operator|new
name|ActionWriteResponse
operator|.
name|ShardInfo
operator|.
name|Failure
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
name|ActionWriteResponse
operator|.
name|ShardInfo
operator|.
name|Failure
name|failure
init|=
operator|new
name|ActionWriteResponse
operator|.
name|ShardInfo
operator|.
name|Failure
argument_list|(
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
argument_list|,
literal|null
argument_list|,
name|e
argument_list|,
name|ExceptionsHelper
operator|.
name|status
argument_list|(
name|e
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|failures
operator|=
operator|new
name|ActionWriteResponse
operator|.
name|ShardInfo
operator|.
name|Failure
index|[
name|totalNumCopies
index|]
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|failures
argument_list|,
name|failure
argument_list|)
expr_stmt|;
block|}
name|shardResponse
operator|.
name|setShardInfo
argument_list|(
operator|new
name|ActionWriteResponse
operator|.
name|ShardInfo
argument_list|(
name|totalNumCopies
argument_list|,
literal|0
argument_list|,
name|failures
argument_list|)
argument_list|)
expr_stmt|;
name|shardsResponses
operator|.
name|add
argument_list|(
name|shardResponse
argument_list|)
expr_stmt|;
if|if
condition|(
name|responsesCountDown
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|finishAndNotifyListener
argument_list|(
name|listener
argument_list|,
name|shardsResponses
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|shardExecute
argument_list|(
name|request
argument_list|,
name|shardId
argument_list|,
name|shardActionListener
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|shardExecute
specifier|protected
name|void
name|shardExecute
parameter_list|(
name|Request
name|request
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|ActionListener
argument_list|<
name|ShardResponse
argument_list|>
name|shardActionListener
parameter_list|)
block|{
name|replicatedBroadcastShardAction
operator|.
name|execute
argument_list|(
name|newShardRequest
argument_list|(
name|request
argument_list|,
name|shardId
argument_list|)
argument_list|,
name|shardActionListener
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return all shard ids the request should run on      */
DECL|method|shards
specifier|protected
name|List
argument_list|<
name|ShardId
argument_list|>
name|shards
parameter_list|(
name|Request
name|request
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|List
argument_list|<
name|ShardId
argument_list|>
name|shardIds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
name|indexNameExpressionResolver
operator|.
name|concreteIndices
argument_list|(
name|clusterState
argument_list|,
name|request
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|index
range|:
name|concreteIndices
control|)
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|getIndices
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexMetaData
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|IntObjectCursor
argument_list|<
name|IndexShardRoutingTable
argument_list|>
name|shardRouting
range|:
name|clusterState
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|indicesRouting
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getShards
argument_list|()
control|)
block|{
name|shardIds
operator|.
name|add
argument_list|(
name|shardRouting
operator|.
name|value
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|shardIds
return|;
block|}
DECL|method|newShardResponse
specifier|protected
specifier|abstract
name|ShardResponse
name|newShardResponse
parameter_list|()
function_decl|;
DECL|method|newShardRequest
specifier|protected
specifier|abstract
name|ShardRequest
name|newShardRequest
parameter_list|(
name|Request
name|request
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
function_decl|;
DECL|method|finishAndNotifyListener
specifier|private
name|void
name|finishAndNotifyListener
parameter_list|(
name|ActionListener
name|listener
parameter_list|,
name|CopyOnWriteArrayList
argument_list|<
name|ShardResponse
argument_list|>
name|shardsResponses
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: got all shard responses"
argument_list|,
name|actionName
argument_list|)
expr_stmt|;
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
name|int
name|totalNumCopies
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
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ActionWriteResponse
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
comment|// non active shard, ignore
block|}
else|else
block|{
name|failedShards
operator|+=
name|shardResponse
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getFailed
argument_list|()
expr_stmt|;
name|successfulShards
operator|+=
name|shardResponse
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getSuccessful
argument_list|()
expr_stmt|;
name|totalNumCopies
operator|+=
name|shardResponse
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getTotal
argument_list|()
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
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|ActionWriteResponse
operator|.
name|ShardInfo
operator|.
name|Failure
name|failure
range|:
name|shardResponse
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getFailures
argument_list|()
control|)
block|{
name|shardFailures
operator|.
name|add
argument_list|(
operator|new
name|DefaultShardOperationFailedException
argument_list|(
operator|new
name|BroadcastShardOperationFailedException
argument_list|(
operator|new
name|ShardId
argument_list|(
name|failure
operator|.
name|index
argument_list|()
argument_list|,
name|failure
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|,
name|failure
operator|.
name|getCause
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|listener
operator|.
name|onResponse
argument_list|(
name|newResponse
argument_list|(
name|successfulShards
argument_list|,
name|failedShards
argument_list|,
name|totalNumCopies
argument_list|,
name|shardFailures
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|newResponse
specifier|protected
specifier|abstract
name|BroadcastResponse
name|newResponse
parameter_list|(
name|int
name|successfulShards
parameter_list|,
name|int
name|failedShards
parameter_list|,
name|int
name|totalNumCopies
parameter_list|,
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|shardFailures
parameter_list|)
function_decl|;
block|}
end_class

end_unit

