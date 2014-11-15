begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.broadcast
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|broadcast
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
name|NoShardAvailableActionException
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
name|TransportAction
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
name|block
operator|.
name|ClusterBlockException
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
name|node
operator|.
name|DiscoveryNodes
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
name|AtomicReferenceArray
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportBroadcastOperationAction
specifier|public
specifier|abstract
class|class
name|TransportBroadcastOperationAction
parameter_list|<
name|Request
extends|extends
name|BroadcastOperationRequest
parameter_list|,
name|Response
extends|extends
name|BroadcastOperationResponse
parameter_list|,
name|ShardRequest
extends|extends
name|BroadcastShardOperationRequest
parameter_list|,
name|ShardResponse
extends|extends
name|BroadcastShardOperationResponse
parameter_list|>
extends|extends
name|TransportAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|field|threadPool
specifier|protected
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|clusterService
specifier|protected
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|transportService
specifier|protected
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|transportShardAction
specifier|final
name|String
name|transportShardAction
decl_stmt|;
DECL|field|executor
specifier|final
name|String
name|executor
decl_stmt|;
DECL|method|TransportBroadcastOperationAction
specifier|protected
name|TransportBroadcastOperationAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|actionName
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
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|actionName
argument_list|,
name|threadPool
argument_list|,
name|actionFilters
argument_list|)
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
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|transportShardAction
operator|=
name|actionName
operator|+
literal|"[s]"
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|actionName
argument_list|,
operator|new
name|TransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|transportShardAction
argument_list|,
operator|new
name|ShardTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
operator|new
name|AsyncBroadcastAction
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|method|executor
specifier|protected
specifier|abstract
name|String
name|executor
parameter_list|()
function_decl|;
DECL|method|newRequest
specifier|protected
specifier|abstract
name|Request
name|newRequest
parameter_list|()
function_decl|;
DECL|method|newResponse
specifier|protected
specifier|abstract
name|Response
name|newResponse
parameter_list|(
name|Request
name|request
parameter_list|,
name|AtomicReferenceArray
name|shardsResponses
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
function_decl|;
DECL|method|newShardRequest
specifier|protected
specifier|abstract
name|ShardRequest
name|newShardRequest
parameter_list|()
function_decl|;
DECL|method|newShardRequest
specifier|protected
specifier|abstract
name|ShardRequest
name|newShardRequest
parameter_list|(
name|int
name|numShards
parameter_list|,
name|ShardRouting
name|shard
parameter_list|,
name|Request
name|request
parameter_list|)
function_decl|;
DECL|method|newShardResponse
specifier|protected
specifier|abstract
name|ShardResponse
name|newShardResponse
parameter_list|()
function_decl|;
DECL|method|shardOperation
specifier|protected
specifier|abstract
name|ShardResponse
name|shardOperation
parameter_list|(
name|ShardRequest
name|request
parameter_list|)
throws|throws
name|ElasticsearchException
function_decl|;
comment|/**      * Determines the shards this operation will be executed on. The operation is executed once per shard iterator, typically      * on the first shard in it. If the operation fails, it will be retried on the next shard in the iterator.      */
DECL|method|shards
specifier|protected
specifier|abstract
name|GroupShardsIterator
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|Request
name|request
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|)
function_decl|;
DECL|method|checkGlobalBlock
specifier|protected
specifier|abstract
name|ClusterBlockException
name|checkGlobalBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|Request
name|request
parameter_list|)
function_decl|;
DECL|method|checkRequestBlock
specifier|protected
specifier|abstract
name|ClusterBlockException
name|checkRequestBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|Request
name|request
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|)
function_decl|;
DECL|class|AsyncBroadcastAction
specifier|protected
class|class
name|AsyncBroadcastAction
block|{
DECL|field|request
specifier|private
specifier|final
name|Request
name|request
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
decl_stmt|;
DECL|field|clusterState
specifier|private
specifier|final
name|ClusterState
name|clusterState
decl_stmt|;
DECL|field|nodes
specifier|private
specifier|final
name|DiscoveryNodes
name|nodes
decl_stmt|;
DECL|field|shardsIts
specifier|private
specifier|final
name|GroupShardsIterator
name|shardsIts
decl_stmt|;
DECL|field|expectedOps
specifier|private
specifier|final
name|int
name|expectedOps
decl_stmt|;
DECL|field|counterOps
specifier|private
specifier|final
name|AtomicInteger
name|counterOps
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|shardsResponses
specifier|private
specifier|final
name|AtomicReferenceArray
name|shardsResponses
decl_stmt|;
DECL|method|AsyncBroadcastAction
specifier|protected
name|AsyncBroadcastAction
parameter_list|(
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|clusterState
operator|=
name|clusterService
operator|.
name|state
argument_list|()
expr_stmt|;
name|ClusterBlockException
name|blockException
init|=
name|checkGlobalBlock
argument_list|(
name|clusterState
argument_list|,
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockException
operator|!=
literal|null
condition|)
block|{
throw|throw
name|blockException
throw|;
block|}
comment|// update to concrete indices
name|String
index|[]
name|concreteIndices
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|concreteIndices
argument_list|(
name|request
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
name|blockException
operator|=
name|checkRequestBlock
argument_list|(
name|clusterState
argument_list|,
name|request
argument_list|,
name|concreteIndices
argument_list|)
expr_stmt|;
if|if
condition|(
name|blockException
operator|!=
literal|null
condition|)
block|{
throw|throw
name|blockException
throw|;
block|}
name|nodes
operator|=
name|clusterState
operator|.
name|nodes
argument_list|()
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"resolving shards based on cluster state version [{}]"
argument_list|,
name|clusterState
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
name|shardsIts
operator|=
name|shards
argument_list|(
name|clusterState
argument_list|,
name|request
argument_list|,
name|concreteIndices
argument_list|)
expr_stmt|;
name|expectedOps
operator|=
name|shardsIts
operator|.
name|size
argument_list|()
expr_stmt|;
name|shardsResponses
operator|=
operator|new
name|AtomicReferenceArray
argument_list|<
name|Object
argument_list|>
argument_list|(
name|expectedOps
argument_list|)
expr_stmt|;
block|}
DECL|method|start
specifier|public
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
name|shardsIts
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// no shards
try|try
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|newResponse
argument_list|(
name|request
argument_list|,
operator|new
name|AtomicReferenceArray
argument_list|(
literal|0
argument_list|)
argument_list|,
name|clusterState
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|request
operator|.
name|beforeStart
argument_list|()
expr_stmt|;
comment|// count the local operations, and perform the non local ones
name|int
name|shardIndex
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
specifier|final
name|ShardIterator
name|shardIt
range|:
name|shardsIts
control|)
block|{
name|shardIndex
operator|++
expr_stmt|;
specifier|final
name|ShardRouting
name|shard
init|=
name|shardIt
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|performOperation
argument_list|(
name|shardIt
argument_list|,
name|shard
argument_list|,
name|shardIndex
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// really, no shards active in this group
name|onOperation
argument_list|(
literal|null
argument_list|,
name|shardIt
argument_list|,
name|shardIndex
argument_list|,
operator|new
name|NoShardAvailableActionException
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|performOperation
specifier|protected
name|void
name|performOperation
parameter_list|(
specifier|final
name|ShardIterator
name|shardIt
parameter_list|,
specifier|final
name|ShardRouting
name|shard
parameter_list|,
specifier|final
name|int
name|shardIndex
parameter_list|)
block|{
if|if
condition|(
name|shard
operator|==
literal|null
condition|)
block|{
comment|// no more active shards... (we should not really get here, just safety)
name|onOperation
argument_list|(
literal|null
argument_list|,
name|shardIt
argument_list|,
name|shardIndex
argument_list|,
operator|new
name|NoShardAvailableActionException
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
specifier|final
name|ShardRequest
name|shardRequest
init|=
name|newShardRequest
argument_list|(
name|shardIt
operator|.
name|size
argument_list|()
argument_list|,
name|shard
argument_list|,
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|shard
operator|.
name|currentNodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|nodes
operator|.
name|localNodeId
argument_list|()
argument_list|)
condition|)
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|executor
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|onOperation
argument_list|(
name|shard
argument_list|,
name|shardIndex
argument_list|,
name|shardOperation
argument_list|(
name|shardRequest
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|onOperation
argument_list|(
name|shard
argument_list|,
name|shardIt
argument_list|,
name|shardIndex
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|shard
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
comment|// no node connected, act as failure
name|onOperation
argument_list|(
name|shard
argument_list|,
name|shardIt
argument_list|,
name|shardIndex
argument_list|,
operator|new
name|NoShardAvailableActionException
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|transportShardAction
argument_list|,
name|shardRequest
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|ShardResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ShardResponse
name|newInstance
parameter_list|()
block|{
return|return
name|newShardResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
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
specifier|public
name|void
name|handleResponse
parameter_list|(
name|ShardResponse
name|response
parameter_list|)
block|{
name|onOperation
argument_list|(
name|shard
argument_list|,
name|shardIndex
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|TransportException
name|e
parameter_list|)
block|{
name|onOperation
argument_list|(
name|shard
argument_list|,
name|shardIt
argument_list|,
name|shardIndex
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|onOperation
argument_list|(
name|shard
argument_list|,
name|shardIt
argument_list|,
name|shardIndex
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|onOperation
specifier|protected
name|void
name|onOperation
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
name|int
name|shardIndex
parameter_list|,
name|ShardResponse
name|response
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"received response for {}"
argument_list|,
name|shard
argument_list|)
expr_stmt|;
name|shardsResponses
operator|.
name|set
argument_list|(
name|shardIndex
argument_list|,
name|response
argument_list|)
expr_stmt|;
if|if
condition|(
name|expectedOps
operator|==
name|counterOps
operator|.
name|incrementAndGet
argument_list|()
condition|)
block|{
name|finishHim
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|onOperation
name|void
name|onOperation
parameter_list|(
annotation|@
name|Nullable
name|ShardRouting
name|shard
parameter_list|,
specifier|final
name|ShardIterator
name|shardIt
parameter_list|,
name|int
name|shardIndex
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
comment|// we set the shard failure always, even if its the first in the replication group, and the next one
comment|// will work (it will just override it...)
name|setFailure
argument_list|(
name|shardIt
argument_list|,
name|shardIndex
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|ShardRouting
name|nextShard
init|=
name|shardIt
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|nextShard
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|t
operator|!=
literal|null
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
if|if
condition|(
operator|!
name|TransportActions
operator|.
name|isShardNotAvailableException
argument_list|(
name|t
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: failed to execute [{}]"
argument_list|,
name|t
argument_list|,
name|shard
operator|!=
literal|null
condition|?
name|shard
operator|.
name|shortSummary
argument_list|()
else|:
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|performOperation
argument_list|(
name|shardIt
argument_list|,
name|nextShard
argument_list|,
name|shardIndex
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|TransportActions
operator|.
name|isShardNotAvailableException
argument_list|(
name|t
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{}: failed to execute [{}]"
argument_list|,
name|t
argument_list|,
name|shard
operator|!=
literal|null
condition|?
name|shard
operator|.
name|shortSummary
argument_list|()
else|:
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|expectedOps
operator|==
name|counterOps
operator|.
name|incrementAndGet
argument_list|()
condition|)
block|{
name|finishHim
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|finishHim
specifier|protected
name|void
name|finishHim
parameter_list|()
block|{
try|try
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|newResponse
argument_list|(
name|request
argument_list|,
name|shardsResponses
argument_list|,
name|clusterState
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|setFailure
name|void
name|setFailure
parameter_list|(
name|ShardIterator
name|shardIt
parameter_list|,
name|int
name|shardIndex
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
comment|// we don't aggregate shard failures on non active shards (but do keep the header counts right)
if|if
condition|(
name|TransportActions
operator|.
name|isShardNotAvailableException
argument_list|(
name|t
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
operator|(
name|t
operator|instanceof
name|BroadcastShardOperationFailedException
operator|)
condition|)
block|{
name|t
operator|=
operator|new
name|BroadcastShardOperationFailedException
argument_list|(
name|shardIt
operator|.
name|shardId
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
name|Object
name|response
init|=
name|shardsResponses
operator|.
name|get
argument_list|(
name|shardIndex
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|==
literal|null
condition|)
block|{
comment|// just override it and return
name|shardsResponses
operator|.
name|set
argument_list|(
name|shardIndex
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
name|response
operator|instanceof
name|Throwable
operator|)
condition|)
block|{
comment|// we should never really get here...
return|return;
block|}
comment|// the failure is already present, try and not override it with an exception that is less meaningless
comment|// for example, getting illegal shard state
if|if
condition|(
name|TransportActions
operator|.
name|isReadOverrideException
argument_list|(
name|t
argument_list|)
condition|)
block|{
name|shardsResponses
operator|.
name|set
argument_list|(
name|shardIndex
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|TransportHandler
class|class
name|TransportHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|Request
argument_list|>
block|{
annotation|@
name|Override
DECL|method|newInstance
specifier|public
name|Request
name|newInstance
parameter_list|()
block|{
return|return
name|newRequest
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
name|Request
name|request
parameter_list|,
specifier|final
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
comment|// we just send back a response, no need to fork a listener
name|request
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|execute
argument_list|(
name|request
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|Response
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|Response
name|response
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|onFailure
argument_list|(
name|e
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
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to send response"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ShardTransportHandler
class|class
name|ShardTransportHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|ShardRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|newInstance
specifier|public
name|ShardRequest
name|newInstance
parameter_list|()
block|{
return|return
name|newShardRequest
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
name|executor
return|;
block|}
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
specifier|final
name|ShardRequest
name|request
parameter_list|,
specifier|final
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
name|shardOperation
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

