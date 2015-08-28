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
name|get
operator|.
name|GetRequest
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
name|GetResponse
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
name|TransportGetAction
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
name|TransportBroadcastAction
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
name|bytes
operator|.
name|BytesReference
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
name|percolator
operator|.
name|PercolateException
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
name|List
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
name|Set
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
DECL|class|TransportPercolateAction
specifier|public
class|class
name|TransportPercolateAction
extends|extends
name|TransportBroadcastAction
argument_list|<
name|PercolateRequest
argument_list|,
name|PercolateResponse
argument_list|,
name|PercolateShardRequest
argument_list|,
name|PercolateShardResponse
argument_list|>
block|{
DECL|field|percolatorService
specifier|private
specifier|final
name|PercolatorService
name|percolatorService
decl_stmt|;
DECL|field|getAction
specifier|private
specifier|final
name|TransportGetAction
name|getAction
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportPercolateAction
specifier|public
name|TransportPercolateAction
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
name|PercolatorService
name|percolatorService
parameter_list|,
name|TransportGetAction
name|getAction
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|PercolateAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|PercolateRequest
operator|.
name|class
argument_list|,
name|PercolateShardRequest
operator|.
name|class
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|PERCOLATE
argument_list|)
expr_stmt|;
name|this
operator|.
name|percolatorService
operator|=
name|percolatorService
expr_stmt|;
name|this
operator|.
name|getAction
operator|=
name|getAction
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
name|PercolateRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|PercolateResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|request
operator|.
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|getRequest
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|//create a new get request to make sure it has the same headers and context as the original percolate request
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
name|request
operator|.
name|getRequest
argument_list|()
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|getAction
operator|.
name|execute
argument_list|(
name|getRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|GetResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|GetResponse
name|getResponse
parameter_list|)
block|{
if|if
condition|(
operator|!
name|getResponse
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|onFailure
argument_list|(
operator|new
name|DocumentMissingException
argument_list|(
literal|null
argument_list|,
name|request
operator|.
name|getRequest
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|request
operator|.
name|getRequest
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|BytesReference
name|docSource
init|=
name|getResponse
operator|.
name|getSourceAsBytesRef
argument_list|()
decl_stmt|;
name|TransportPercolateAction
operator|.
name|super
operator|.
name|doExecute
argument_list|(
operator|new
name|PercolateRequest
argument_list|(
name|request
argument_list|,
name|docSource
argument_list|)
argument_list|,
name|listener
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
name|super
operator|.
name|doExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|checkGlobalBlock
specifier|protected
name|ClusterBlockException
name|checkGlobalBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|PercolateRequest
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
name|READ
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|checkRequestBlock
specifier|protected
name|ClusterBlockException
name|checkRequestBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|PercolateRequest
name|request
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indicesBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|READ
argument_list|,
name|concreteIndices
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|PercolateResponse
name|newResponse
parameter_list|(
name|PercolateRequest
name|request
parameter_list|,
name|AtomicReferenceArray
name|shardsResponses
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
return|return
name|reduce
argument_list|(
name|request
argument_list|,
name|shardsResponses
argument_list|,
name|percolatorService
argument_list|)
return|;
block|}
DECL|method|reduce
specifier|public
specifier|static
name|PercolateResponse
name|reduce
parameter_list|(
name|PercolateRequest
name|request
parameter_list|,
name|AtomicReferenceArray
name|shardsResponses
parameter_list|,
name|PercolatorService
name|percolatorService
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
name|PercolateShardResponse
argument_list|>
name|shardResults
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|shardFailures
init|=
literal|null
decl_stmt|;
name|byte
name|percolatorTypeId
init|=
literal|0x00
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
operator|new
name|ArrayList
argument_list|<>
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
name|PercolateShardResponse
name|percolateShardResponse
init|=
operator|(
name|PercolateShardResponse
operator|)
name|shardResponse
decl_stmt|;
name|successfulShards
operator|++
expr_stmt|;
if|if
condition|(
operator|!
name|percolateShardResponse
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|shardResults
operator|==
literal|null
condition|)
block|{
name|percolatorTypeId
operator|=
name|percolateShardResponse
operator|.
name|percolatorTypeId
argument_list|()
expr_stmt|;
name|shardResults
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|shardResults
operator|.
name|add
argument_list|(
name|percolateShardResponse
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|shardResults
operator|==
literal|null
condition|)
block|{
name|long
name|tookInMillis
init|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|request
operator|.
name|startTime
argument_list|)
decl_stmt|;
name|PercolateResponse
operator|.
name|Match
index|[]
name|matches
init|=
name|request
operator|.
name|onlyCount
argument_list|()
condition|?
literal|null
else|:
name|PercolateResponse
operator|.
name|EMPTY
decl_stmt|;
return|return
operator|new
name|PercolateResponse
argument_list|(
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
argument_list|,
name|tookInMillis
argument_list|,
name|matches
argument_list|)
return|;
block|}
else|else
block|{
name|PercolatorService
operator|.
name|ReduceResult
name|result
init|=
name|percolatorService
operator|.
name|reduce
argument_list|(
name|percolatorTypeId
argument_list|,
name|shardResults
argument_list|)
decl_stmt|;
name|long
name|tookInMillis
init|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|request
operator|.
name|startTime
argument_list|)
decl_stmt|;
return|return
operator|new
name|PercolateResponse
argument_list|(
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
argument_list|,
name|result
operator|.
name|matches
argument_list|()
argument_list|,
name|result
operator|.
name|count
argument_list|()
argument_list|,
name|tookInMillis
argument_list|,
name|result
operator|.
name|reducedAggregations
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|newShardRequest
specifier|protected
name|PercolateShardRequest
name|newShardRequest
parameter_list|(
name|int
name|numShards
parameter_list|,
name|ShardRouting
name|shard
parameter_list|,
name|PercolateRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|PercolateShardRequest
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|numShards
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newShardResponse
specifier|protected
name|PercolateShardResponse
name|newShardResponse
parameter_list|()
block|{
return|return
operator|new
name|PercolateShardResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|shards
specifier|protected
name|GroupShardsIterator
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|PercolateRequest
name|request
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|routingMap
init|=
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|clusterState
argument_list|,
name|request
operator|.
name|routing
argument_list|()
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|clusterService
operator|.
name|operationRouting
argument_list|()
operator|.
name|searchShards
argument_list|(
name|clusterState
argument_list|,
name|concreteIndices
argument_list|,
name|routingMap
argument_list|,
name|request
operator|.
name|preference
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperation
specifier|protected
name|PercolateShardResponse
name|shardOperation
parameter_list|(
name|PercolateShardRequest
name|request
parameter_list|)
block|{
try|try
block|{
return|return
name|percolatorService
operator|.
name|percolate
argument_list|(
name|request
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} failed to percolate"
argument_list|,
name|e
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|PercolateException
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
literal|"failed to percolate"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

