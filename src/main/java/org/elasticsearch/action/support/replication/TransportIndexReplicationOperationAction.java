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
name|ActionResponse
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
name|ActionWriteResponse
operator|.
name|ShardInfo
operator|.
name|Failure
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
name|rest
operator|.
name|RestStatus
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
comment|/**  * Internal transport action that executes on multiple shards, doesn't register any transport handler as it is always executed locally.  * It relies on a shard sub-action that gets sent over the transport and executed on each of the shard.  * The index provided with the request is expected to be a concrete index, properly resolved by the callers (parent actions).  */
end_comment

begin_class
DECL|class|TransportIndexReplicationOperationAction
specifier|public
specifier|abstract
class|class
name|TransportIndexReplicationOperationAction
parameter_list|<
name|Request
extends|extends
name|IndexReplicationOperationRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|,
name|ShardRequest
extends|extends
name|ShardReplicationOperationRequest
parameter_list|,
name|ShardResponse
extends|extends
name|ActionWriteResponse
parameter_list|>
extends|extends
name|TransportAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|field|clusterService
specifier|protected
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|shardAction
specifier|protected
specifier|final
name|TransportShardReplicationOperationAction
argument_list|<
name|ShardRequest
argument_list|,
name|ShardRequest
argument_list|,
name|ShardResponse
argument_list|>
name|shardAction
decl_stmt|;
DECL|method|TransportIndexReplicationOperationAction
specifier|protected
name|TransportIndexReplicationOperationAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|actionName
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportShardReplicationOperationAction
argument_list|<
name|ShardRequest
argument_list|,
name|ShardRequest
argument_list|,
name|ShardResponse
argument_list|>
name|shardAction
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
name|shardAction
operator|=
name|shardAction
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
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
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
name|blockException
operator|=
name|checkRequestBlock
argument_list|(
name|clusterState
argument_list|,
name|request
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
specifier|final
name|GroupShardsIterator
name|groups
decl_stmt|;
try|try
block|{
name|groups
operator|=
name|shards
argument_list|(
name|request
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
return|return;
block|}
specifier|final
name|AtomicInteger
name|indexCounter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|failureCounter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|completionCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|groups
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReferenceArray
argument_list|<
name|ShardActionResult
argument_list|>
name|shardsResponses
init|=
operator|new
name|AtomicReferenceArray
argument_list|<>
argument_list|(
name|groups
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|ShardIterator
name|shardIt
range|:
name|groups
control|)
block|{
specifier|final
name|ShardRequest
name|shardRequest
init|=
name|newShardRequestInstance
argument_list|(
name|request
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|shardRequest
operator|.
name|operationThreaded
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// no need for threaded listener, we will fork when its done based on the index request
name|shardRequest
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|shardAction
operator|.
name|execute
argument_list|(
name|shardRequest
argument_list|,
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
name|result
parameter_list|)
block|{
name|shardsResponses
operator|.
name|set
argument_list|(
name|indexCounter
operator|.
name|getAndIncrement
argument_list|()
argument_list|,
operator|new
name|ShardActionResult
argument_list|(
name|result
argument_list|)
argument_list|)
expr_stmt|;
name|returnIfNeeded
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
name|e
parameter_list|)
block|{
name|failureCounter
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
name|int
name|index
init|=
name|indexCounter
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
comment|// this is a failure for an entire shard group, constructs shard info accordingly
specifier|final
name|RestStatus
name|status
init|=
name|ExceptionsHelper
operator|.
name|status
argument_list|(
name|e
argument_list|)
decl_stmt|;
name|Failure
name|failure
init|=
operator|new
name|Failure
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|shardIt
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
literal|null
argument_list|,
name|e
argument_list|,
name|status
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|shardsResponses
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|ShardActionResult
argument_list|(
operator|new
name|ActionWriteResponse
operator|.
name|ShardInfo
argument_list|(
name|shardIt
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|,
name|failure
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|returnIfNeeded
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|returnIfNeeded
parameter_list|()
block|{
if|if
condition|(
name|completionCounter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|List
argument_list|<
name|ShardResponse
argument_list|>
name|responses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Failure
argument_list|>
name|failureList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|total
init|=
literal|0
decl_stmt|;
name|int
name|successful
init|=
literal|0
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
name|ShardActionResult
name|shardActionResult
init|=
name|shardsResponses
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
specifier|final
name|ActionWriteResponse
operator|.
name|ShardInfo
name|sf
decl_stmt|;
if|if
condition|(
name|shardActionResult
operator|.
name|isFailure
argument_list|()
condition|)
block|{
assert|assert
name|shardActionResult
operator|.
name|shardInfoOnFailure
operator|!=
literal|null
assert|;
name|sf
operator|=
name|shardActionResult
operator|.
name|shardInfoOnFailure
expr_stmt|;
block|}
else|else
block|{
name|responses
operator|.
name|add
argument_list|(
name|shardActionResult
operator|.
name|shardResponse
argument_list|)
expr_stmt|;
name|sf
operator|=
name|shardActionResult
operator|.
name|shardResponse
operator|.
name|getShardInfo
argument_list|()
expr_stmt|;
block|}
name|total
operator|+=
name|sf
operator|.
name|getTotal
argument_list|()
expr_stmt|;
name|successful
operator|+=
name|sf
operator|.
name|getSuccessful
argument_list|()
expr_stmt|;
name|failureList
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|sf
operator|.
name|getFailures
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
assert|assert
name|failureList
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|||
name|numShardGroupFailures
argument_list|(
name|failureList
argument_list|)
operator|==
name|failureCounter
operator|.
name|get
argument_list|()
assert|;
specifier|final
name|Failure
index|[]
name|failures
decl_stmt|;
if|if
condition|(
name|failureList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|failures
operator|=
name|ActionWriteResponse
operator|.
name|EMPTY
expr_stmt|;
block|}
else|else
block|{
name|failures
operator|=
name|failureList
operator|.
name|toArray
argument_list|(
operator|new
name|Failure
index|[
name|failureList
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
name|listener
operator|.
name|onResponse
argument_list|(
name|newResponseInstance
argument_list|(
name|request
argument_list|,
name|responses
argument_list|,
operator|new
name|ActionWriteResponse
operator|.
name|ShardInfo
argument_list|(
name|total
argument_list|,
name|successful
argument_list|,
name|failures
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|int
name|numShardGroupFailures
parameter_list|(
name|List
argument_list|<
name|Failure
argument_list|>
name|failures
parameter_list|)
block|{
name|int
name|numShardGroupFailures
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Failure
name|failure
range|:
name|failures
control|)
block|{
if|if
condition|(
name|failure
operator|.
name|primary
argument_list|()
condition|)
block|{
name|numShardGroupFailures
operator|++
expr_stmt|;
block|}
block|}
return|return
name|numShardGroupFailures
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|newResponseInstance
specifier|protected
specifier|abstract
name|Response
name|newResponseInstance
parameter_list|(
name|Request
name|request
parameter_list|,
name|List
argument_list|<
name|ShardResponse
argument_list|>
name|shardResponses
parameter_list|,
name|ActionWriteResponse
operator|.
name|ShardInfo
name|shardInfo
parameter_list|)
function_decl|;
DECL|method|shards
specifier|protected
specifier|abstract
name|GroupShardsIterator
name|shards
parameter_list|(
name|Request
name|request
parameter_list|)
throws|throws
name|ElasticsearchException
function_decl|;
DECL|method|newShardRequestInstance
specifier|protected
specifier|abstract
name|ShardRequest
name|newShardRequestInstance
parameter_list|(
name|Request
name|request
parameter_list|,
name|int
name|shardId
parameter_list|)
function_decl|;
DECL|method|checkGlobalBlock
specifier|protected
name|ClusterBlockException
name|checkGlobalBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|Request
name|request
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|globalBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|WRITE
argument_list|)
return|;
block|}
DECL|method|checkRequestBlock
specifier|protected
name|ClusterBlockException
name|checkRequestBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|Request
name|request
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indexBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|WRITE
argument_list|,
name|request
operator|.
name|index
argument_list|()
argument_list|)
return|;
block|}
DECL|class|ShardActionResult
specifier|private
class|class
name|ShardActionResult
block|{
DECL|field|shardResponse
specifier|private
specifier|final
name|ShardResponse
name|shardResponse
decl_stmt|;
DECL|field|shardInfoOnFailure
specifier|private
specifier|final
name|ActionWriteResponse
operator|.
name|ShardInfo
name|shardInfoOnFailure
decl_stmt|;
DECL|method|ShardActionResult
specifier|private
name|ShardActionResult
parameter_list|(
name|ShardResponse
name|shardResponse
parameter_list|)
block|{
assert|assert
name|shardResponse
operator|!=
literal|null
assert|;
name|this
operator|.
name|shardResponse
operator|=
name|shardResponse
expr_stmt|;
name|this
operator|.
name|shardInfoOnFailure
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|ShardActionResult
specifier|private
name|ShardActionResult
parameter_list|(
name|ActionWriteResponse
operator|.
name|ShardInfo
name|shardInfoOnFailure
parameter_list|)
block|{
assert|assert
name|shardInfoOnFailure
operator|!=
literal|null
assert|;
name|this
operator|.
name|shardInfoOnFailure
operator|=
name|shardInfoOnFailure
expr_stmt|;
name|this
operator|.
name|shardResponse
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|isFailure
name|boolean
name|isFailure
parameter_list|()
block|{
return|return
name|shardInfoOnFailure
operator|!=
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

