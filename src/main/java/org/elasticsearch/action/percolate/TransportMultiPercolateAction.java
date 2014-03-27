begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.percolate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|percolate
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
name|IntArrayList
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
name|get
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
name|AtomicArray
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
name|DocumentMissingException
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
name|IndexMissingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
operator|.
name|PercolatorService
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
name|BaseTransportRequestHandler
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
name|TransportChannel
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
comment|/**  */
end_comment

begin_class
DECL|class|TransportMultiPercolateAction
specifier|public
class|class
name|TransportMultiPercolateAction
extends|extends
name|TransportAction
argument_list|<
name|MultiPercolateRequest
argument_list|,
name|MultiPercolateResponse
argument_list|>
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|percolatorService
specifier|private
specifier|final
name|PercolatorService
name|percolatorService
decl_stmt|;
DECL|field|multiGetAction
specifier|private
specifier|final
name|TransportMultiGetAction
name|multiGetAction
decl_stmt|;
DECL|field|shardMultiPercolateAction
specifier|private
specifier|final
name|TransportShardMultiPercolateAction
name|shardMultiPercolateAction
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportMultiPercolateAction
specifier|public
name|TransportMultiPercolateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportShardMultiPercolateAction
name|shardMultiPercolateAction
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|PercolatorService
name|percolatorService
parameter_list|,
name|TransportMultiGetAction
name|multiGetAction
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardMultiPercolateAction
operator|=
name|shardMultiPercolateAction
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|percolatorService
operator|=
name|percolatorService
expr_stmt|;
name|this
operator|.
name|multiGetAction
operator|=
name|multiGetAction
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|MultiPercolateAction
operator|.
name|NAME
argument_list|,
operator|new
name|TransportHandler
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
specifier|final
name|MultiPercolateRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|MultiPercolateResponse
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
name|clusterState
operator|.
name|blocks
argument_list|()
operator|.
name|globalBlockedRaiseException
argument_list|(
name|ClusterBlockLevel
operator|.
name|READ
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|percolateRequests
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// Can have a mixture of percolate requests. (normal percolate requests& percolate existing doc),
comment|// so we need to keep track for what percolate request we had a get request
specifier|final
name|IntArrayList
name|getRequestSlots
init|=
operator|new
name|IntArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|GetRequest
argument_list|>
name|existingDocsRequests
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|slot
init|=
literal|0
init|;
name|slot
operator|<
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|slot
operator|++
control|)
block|{
name|PercolateRequest
name|percolateRequest
init|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
name|slot
argument_list|)
decl_stmt|;
name|percolateRequest
operator|.
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|percolateRequests
operator|.
name|add
argument_list|(
name|percolateRequest
argument_list|)
expr_stmt|;
if|if
condition|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|existingDocsRequests
operator|.
name|add
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|)
expr_stmt|;
name|getRequestSlots
operator|.
name|add
argument_list|(
name|slot
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|existingDocsRequests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
specifier|final
name|MultiGetRequest
name|multiGetRequest
init|=
operator|new
name|MultiGetRequest
argument_list|()
decl_stmt|;
for|for
control|(
name|GetRequest
name|getRequest
range|:
name|existingDocsRequests
control|)
block|{
name|multiGetRequest
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
name|getRequest
operator|.
name|index
argument_list|()
argument_list|,
name|getRequest
operator|.
name|type
argument_list|()
argument_list|,
name|getRequest
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|routing
argument_list|(
name|getRequest
operator|.
name|routing
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|multiGetAction
operator|.
name|execute
argument_list|(
name|multiGetRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|MultiGetResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|MultiGetResponse
name|multiGetItemResponses
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|multiGetItemResponses
operator|.
name|getResponses
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|MultiGetItemResponse
name|itemResponse
init|=
name|multiGetItemResponses
operator|.
name|getResponses
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|int
name|slot
init|=
name|getRequestSlots
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|itemResponse
operator|.
name|isFailed
argument_list|()
condition|)
block|{
name|GetResponse
name|getResponse
init|=
name|itemResponse
operator|.
name|getResponse
argument_list|()
decl_stmt|;
if|if
condition|(
name|getResponse
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|PercolateRequest
name|originalRequest
init|=
operator|(
name|PercolateRequest
operator|)
name|percolateRequests
operator|.
name|get
argument_list|(
name|slot
argument_list|)
decl_stmt|;
name|percolateRequests
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|PercolateRequest
argument_list|(
name|originalRequest
argument_list|,
name|getResponse
operator|.
name|getSourceAsBytesRef
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"mpercolate existing doc, item[{}] doesn't exist"
argument_list|,
name|slot
argument_list|)
expr_stmt|;
name|percolateRequests
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|DocumentMissingException
argument_list|(
literal|null
argument_list|,
name|getResponse
operator|.
name|getType
argument_list|()
argument_list|,
name|getResponse
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"mpercolate existing doc, item[{}] failure {}"
argument_list|,
name|slot
argument_list|,
name|itemResponse
operator|.
name|getFailure
argument_list|()
argument_list|)
expr_stmt|;
name|percolateRequests
operator|.
name|set
argument_list|(
name|slot
argument_list|,
name|itemResponse
operator|.
name|getFailure
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
operator|new
name|ASyncAction
argument_list|(
name|percolateRequests
argument_list|,
name|listener
argument_list|,
name|clusterState
argument_list|)
operator|.
name|run
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
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|new
name|ASyncAction
argument_list|(
name|percolateRequests
argument_list|,
name|listener
argument_list|,
name|clusterState
argument_list|)
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|ASyncAction
specifier|private
class|class
name|ASyncAction
block|{
DECL|field|finalListener
specifier|final
name|ActionListener
argument_list|<
name|MultiPercolateResponse
argument_list|>
name|finalListener
decl_stmt|;
DECL|field|requestsByShard
specifier|final
name|Map
argument_list|<
name|ShardId
argument_list|,
name|TransportShardMultiPercolateAction
operator|.
name|Request
argument_list|>
name|requestsByShard
decl_stmt|;
DECL|field|percolateRequests
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|percolateRequests
decl_stmt|;
DECL|field|shardToSlots
specifier|final
name|Map
argument_list|<
name|ShardId
argument_list|,
name|IntArrayList
argument_list|>
name|shardToSlots
decl_stmt|;
DECL|field|expectedOperations
specifier|final
name|AtomicInteger
name|expectedOperations
decl_stmt|;
DECL|field|reducedResponses
specifier|final
name|AtomicArray
argument_list|<
name|Object
argument_list|>
name|reducedResponses
decl_stmt|;
DECL|field|expectedOperationsPerItem
specifier|final
name|AtomicReferenceArray
argument_list|<
name|AtomicInteger
argument_list|>
name|expectedOperationsPerItem
decl_stmt|;
DECL|field|responsesByItemAndShard
specifier|final
name|AtomicReferenceArray
argument_list|<
name|AtomicReferenceArray
argument_list|>
name|responsesByItemAndShard
decl_stmt|;
DECL|method|ASyncAction
name|ASyncAction
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|percolateRequests
parameter_list|,
name|ActionListener
argument_list|<
name|MultiPercolateResponse
argument_list|>
name|finalListener
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|this
operator|.
name|finalListener
operator|=
name|finalListener
expr_stmt|;
name|this
operator|.
name|percolateRequests
operator|=
name|percolateRequests
expr_stmt|;
name|responsesByItemAndShard
operator|=
operator|new
name|AtomicReferenceArray
argument_list|<>
argument_list|(
name|percolateRequests
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|expectedOperationsPerItem
operator|=
operator|new
name|AtomicReferenceArray
argument_list|<>
argument_list|(
name|percolateRequests
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|reducedResponses
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|percolateRequests
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Resolving concrete indices and routing and grouping the requests by shard
name|requestsByShard
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
comment|// Keep track what slots belong to what shard, in case a request to a shard fails on all copies
name|shardToSlots
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|int
name|expectedResults
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|slot
init|=
literal|0
init|;
name|slot
operator|<
name|percolateRequests
operator|.
name|size
argument_list|()
condition|;
name|slot
operator|++
control|)
block|{
name|Object
name|element
init|=
name|percolateRequests
operator|.
name|get
argument_list|(
name|slot
argument_list|)
decl_stmt|;
assert|assert
name|element
operator|!=
literal|null
assert|;
if|if
condition|(
name|element
operator|instanceof
name|PercolateRequest
condition|)
block|{
name|PercolateRequest
name|percolateRequest
init|=
operator|(
name|PercolateRequest
operator|)
name|element
decl_stmt|;
name|String
index|[]
name|concreteIndices
decl_stmt|;
try|try
block|{
name|concreteIndices
operator|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|concreteIndices
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
argument_list|,
name|percolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{
name|reducedResponses
operator|.
name|set
argument_list|(
name|slot
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|responsesByItemAndShard
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|AtomicReferenceArray
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|expectedOperationsPerItem
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|routing
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|resolveSearchRouting
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|percolateRequest
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
comment|// TODO: I only need shardIds, ShardIterator(ShardRouting) is only needed in TransportShardMultiPercolateAction
name|GroupShardsIterator
name|shards
init|=
name|clusterService
operator|.
name|operationRouting
argument_list|()
operator|.
name|searchShards
argument_list|(
name|clusterState
argument_list|,
name|percolateRequest
operator|.
name|indices
argument_list|()
argument_list|,
name|concreteIndices
argument_list|,
name|routing
argument_list|,
name|percolateRequest
operator|.
name|preference
argument_list|()
argument_list|)
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
name|reducedResponses
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|UnavailableShardsException
argument_list|(
literal|null
argument_list|,
literal|"No shards available"
argument_list|)
argument_list|)
expr_stmt|;
name|responsesByItemAndShard
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|AtomicReferenceArray
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|expectedOperationsPerItem
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|responsesByItemAndShard
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|AtomicReferenceArray
argument_list|(
name|shards
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|expectedOperationsPerItem
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|AtomicInteger
argument_list|(
name|shards
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardIterator
name|shard
range|:
name|shards
control|)
block|{
name|ShardId
name|shardId
init|=
name|shard
operator|.
name|shardId
argument_list|()
decl_stmt|;
name|TransportShardMultiPercolateAction
operator|.
name|Request
name|requests
init|=
name|requestsByShard
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|requests
operator|==
literal|null
condition|)
block|{
name|requestsByShard
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|requests
operator|=
operator|new
name|TransportShardMultiPercolateAction
operator|.
name|Request
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|percolateRequest
operator|.
name|preference
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"Adding shard[{}] percolate request for item[{}]"
argument_list|,
name|shardId
argument_list|,
name|slot
argument_list|)
expr_stmt|;
name|requests
operator|.
name|add
argument_list|(
operator|new
name|TransportShardMultiPercolateAction
operator|.
name|Request
operator|.
name|Item
argument_list|(
name|slot
argument_list|,
operator|new
name|PercolateShardRequest
argument_list|(
name|shardId
argument_list|,
name|percolateRequest
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|IntArrayList
name|items
init|=
name|shardToSlots
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|items
operator|==
literal|null
condition|)
block|{
name|shardToSlots
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|items
operator|=
operator|new
name|IntArrayList
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|items
operator|.
name|add
argument_list|(
name|slot
argument_list|)
expr_stmt|;
block|}
name|expectedResults
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|element
operator|instanceof
name|Throwable
operator|||
name|element
operator|instanceof
name|MultiGetResponse
operator|.
name|Failure
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"item[{}] won't be executed, reason: {}"
argument_list|,
name|slot
argument_list|,
name|element
argument_list|)
expr_stmt|;
name|reducedResponses
operator|.
name|set
argument_list|(
name|slot
argument_list|,
name|element
argument_list|)
expr_stmt|;
name|responsesByItemAndShard
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|AtomicReferenceArray
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|expectedOperationsPerItem
operator|.
name|set
argument_list|(
name|slot
argument_list|,
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|expectedOperations
operator|=
operator|new
name|AtomicInteger
argument_list|(
name|expectedResults
argument_list|)
expr_stmt|;
block|}
DECL|method|run
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|expectedOperations
operator|.
name|get
argument_list|()
operator|==
literal|0
condition|)
block|{
name|finish
argument_list|()
expr_stmt|;
return|return;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"mpercolate executing for shards {}"
argument_list|,
name|requestsByShard
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|TransportShardMultiPercolateAction
operator|.
name|Request
argument_list|>
name|entry
range|:
name|requestsByShard
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|ShardId
name|shardId
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|TransportShardMultiPercolateAction
operator|.
name|Request
name|shardRequest
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|shardMultiPercolateAction
operator|.
name|execute
argument_list|(
name|shardRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|TransportShardMultiPercolateAction
operator|.
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
name|TransportShardMultiPercolateAction
operator|.
name|Response
name|response
parameter_list|)
block|{
name|onShardResponse
argument_list|(
name|shardId
argument_list|,
name|response
argument_list|)
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
name|onShardFailure
argument_list|(
name|shardId
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|onShardResponse
name|void
name|onShardResponse
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|TransportShardMultiPercolateAction
operator|.
name|Response
name|response
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} Percolate shard response"
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
name|TransportShardMultiPercolateAction
operator|.
name|Response
operator|.
name|Item
name|item
range|:
name|response
operator|.
name|items
argument_list|()
control|)
block|{
name|AtomicReferenceArray
name|shardResults
init|=
name|responsesByItemAndShard
operator|.
name|get
argument_list|(
name|item
operator|.
name|slot
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardResults
operator|==
literal|null
condition|)
block|{
assert|assert
literal|false
operator|:
literal|"shardResults can't be null"
assert|;
continue|continue;
block|}
if|if
condition|(
name|item
operator|.
name|failed
argument_list|()
condition|)
block|{
name|shardResults
operator|.
name|set
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
operator|new
name|BroadcastShardOperationFailedException
argument_list|(
name|shardId
argument_list|,
name|item
operator|.
name|error
argument_list|()
operator|.
name|string
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shardResults
operator|.
name|set
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|item
operator|.
name|response
argument_list|()
argument_list|)
expr_stmt|;
block|}
assert|assert
name|expectedOperationsPerItem
operator|.
name|get
argument_list|(
name|item
operator|.
name|slot
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
operator|>=
literal|1
operator|:
literal|"slot["
operator|+
name|item
operator|.
name|slot
argument_list|()
operator|+
literal|"] can't be lower than one"
assert|;
if|if
condition|(
name|expectedOperationsPerItem
operator|.
name|get
argument_list|(
name|item
operator|.
name|slot
argument_list|()
argument_list|)
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Failure won't bubble up, since we fail the whole request now via the catch clause below,
comment|// so expectedOperationsPerItem will not be decremented twice.
name|reduce
argument_list|(
name|item
operator|.
name|slot
argument_list|()
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
name|logger
operator|.
name|error
argument_list|(
literal|"{} Percolate original reduce error"
argument_list|,
name|e
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|finalListener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|onShardFailure
name|void
name|onShardFailure
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} Shard multi percolate failure"
argument_list|,
name|e
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
try|try
block|{
name|IntArrayList
name|slots
init|=
name|shardToSlots
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
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
name|slots
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|slot
init|=
name|slots
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|AtomicReferenceArray
name|shardResults
init|=
name|responsesByItemAndShard
operator|.
name|get
argument_list|(
name|slot
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardResults
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|shardResults
operator|.
name|set
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
operator|new
name|BroadcastShardOperationFailedException
argument_list|(
name|shardId
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
assert|assert
name|expectedOperationsPerItem
operator|.
name|get
argument_list|(
name|slot
argument_list|)
operator|.
name|get
argument_list|()
operator|>=
literal|1
operator|:
literal|"slot["
operator|+
name|slot
operator|+
literal|"] can't be lower than one. Caused by: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
assert|;
if|if
condition|(
name|expectedOperationsPerItem
operator|.
name|get
argument_list|(
name|slot
argument_list|)
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|reduce
argument_list|(
name|slot
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"{} Percolate original reduce error, original error {}"
argument_list|,
name|t
argument_list|,
name|shardId
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|finalListener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|reduce
name|void
name|reduce
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
name|AtomicReferenceArray
name|shardResponses
init|=
name|responsesByItemAndShard
operator|.
name|get
argument_list|(
name|slot
argument_list|)
decl_stmt|;
name|PercolateResponse
name|reducedResponse
init|=
name|TransportPercolateAction
operator|.
name|reduce
argument_list|(
operator|(
name|PercolateRequest
operator|)
name|percolateRequests
operator|.
name|get
argument_list|(
name|slot
argument_list|)
argument_list|,
name|shardResponses
argument_list|,
name|percolatorService
argument_list|)
decl_stmt|;
name|reducedResponses
operator|.
name|set
argument_list|(
name|slot
argument_list|,
name|reducedResponse
argument_list|)
expr_stmt|;
assert|assert
name|expectedOperations
operator|.
name|get
argument_list|()
operator|>=
literal|1
operator|:
literal|"slot["
operator|+
name|slot
operator|+
literal|"] expected options should be>= 1 but is "
operator|+
name|expectedOperations
operator|.
name|get
argument_list|()
assert|;
if|if
condition|(
name|expectedOperations
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
name|void
name|finish
parameter_list|()
block|{
name|MultiPercolateResponse
operator|.
name|Item
index|[]
name|finalResponse
init|=
operator|new
name|MultiPercolateResponse
operator|.
name|Item
index|[
name|reducedResponses
operator|.
name|length
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|slot
init|=
literal|0
init|;
name|slot
operator|<
name|reducedResponses
operator|.
name|length
argument_list|()
condition|;
name|slot
operator|++
control|)
block|{
name|Object
name|element
init|=
name|reducedResponses
operator|.
name|get
argument_list|(
name|slot
argument_list|)
decl_stmt|;
assert|assert
name|element
operator|!=
literal|null
operator|:
literal|"Element["
operator|+
name|slot
operator|+
literal|"] shouldn't be null"
assert|;
if|if
condition|(
name|element
operator|instanceof
name|PercolateResponse
condition|)
block|{
name|finalResponse
index|[
name|slot
index|]
operator|=
operator|new
name|MultiPercolateResponse
operator|.
name|Item
argument_list|(
operator|(
name|PercolateResponse
operator|)
name|element
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|element
operator|instanceof
name|Throwable
condition|)
block|{
name|finalResponse
index|[
name|slot
index|]
operator|=
operator|new
name|MultiPercolateResponse
operator|.
name|Item
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
operator|(
name|Throwable
operator|)
name|element
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|element
operator|instanceof
name|MultiGetResponse
operator|.
name|Failure
condition|)
block|{
name|finalResponse
index|[
name|slot
index|]
operator|=
operator|new
name|MultiPercolateResponse
operator|.
name|Item
argument_list|(
operator|(
operator|(
name|MultiGetResponse
operator|.
name|Failure
operator|)
name|element
operator|)
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|finalListener
operator|.
name|onResponse
argument_list|(
operator|new
name|MultiPercolateResponse
argument_list|(
name|finalResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TransportHandler
class|class
name|TransportHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|MultiPercolateRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|newInstance
specifier|public
name|MultiPercolateRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|MultiPercolateRequest
argument_list|()
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
name|MultiPercolateRequest
name|request
parameter_list|,
specifier|final
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
comment|// no need to use threaded listener, since we just send a response
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
name|MultiPercolateResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|MultiPercolateResponse
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
literal|"Failed to send error response for action [mpercolate] and request ["
operator|+
name|request
operator|+
literal|"]"
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
block|}
block|}
end_class

end_unit

