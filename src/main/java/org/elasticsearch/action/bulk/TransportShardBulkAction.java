begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bulk
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
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
name|RoutingMissingException
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
name|delete
operator|.
name|DeleteRequest
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
name|delete
operator|.
name|DeleteResponse
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
name|index
operator|.
name|IndexRequest
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
name|index
operator|.
name|IndexResponse
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
name|replication
operator|.
name|TransportShardReplicationOperationAction
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
name|action
operator|.
name|index
operator|.
name|MappingUpdatedAction
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
name|MappingMetaData
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
name|Strings
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
name|mapper
operator|.
name|DocumentMapper
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
name|mapper
operator|.
name|MapperService
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
name|mapper
operator|.
name|SourceToParse
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
name|percolator
operator|.
name|PercolatorExecutor
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
name|index
operator|.
name|shard
operator|.
name|service
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
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportRequestOptions
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

begin_comment
comment|/**  * Performs the index operation.  */
end_comment

begin_class
DECL|class|TransportShardBulkAction
specifier|public
class|class
name|TransportShardBulkAction
extends|extends
name|TransportShardReplicationOperationAction
argument_list|<
name|BulkShardRequest
argument_list|,
name|BulkShardRequest
argument_list|,
name|BulkShardResponse
argument_list|>
block|{
DECL|field|mappingUpdatedAction
specifier|private
specifier|final
name|MappingUpdatedAction
name|mappingUpdatedAction
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportShardBulkAction
specifier|public
name|TransportShardBulkAction
parameter_list|(
name|Settings
name|settings
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
name|MappingUpdatedAction
name|mappingUpdatedAction
parameter_list|)
block|{
name|super
argument_list|(
name|settings
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
argument_list|)
expr_stmt|;
name|this
operator|.
name|mappingUpdatedAction
operator|=
name|mappingUpdatedAction
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
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
name|INDEX
return|;
block|}
annotation|@
name|Override
DECL|method|checkWriteConsistency
specifier|protected
name|boolean
name|checkWriteConsistency
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|transportOptions
specifier|protected
name|TransportRequestOptions
name|transportOptions
parameter_list|()
block|{
comment|// low type since we don't want the large bulk requests to cause high latency on typical requests
return|return
name|TransportRequestOptions
operator|.
name|options
argument_list|()
operator|.
name|withCompress
argument_list|(
literal|true
argument_list|)
operator|.
name|withLowType
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newRequestInstance
specifier|protected
name|BulkShardRequest
name|newRequestInstance
parameter_list|()
block|{
return|return
operator|new
name|BulkShardRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newReplicaRequestInstance
specifier|protected
name|BulkShardRequest
name|newReplicaRequestInstance
parameter_list|()
block|{
return|return
operator|new
name|BulkShardRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponseInstance
specifier|protected
name|BulkShardResponse
name|newResponseInstance
parameter_list|()
block|{
return|return
operator|new
name|BulkShardResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|transportAction
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|TransportActions
operator|.
name|BULK
operator|+
literal|"/shard"
return|;
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
name|BulkShardRequest
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
name|BulkShardRequest
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
annotation|@
name|Override
DECL|method|shards
specifier|protected
name|ShardIterator
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|BulkShardRequest
name|request
parameter_list|)
block|{
return|return
name|clusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|shard
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
operator|.
name|shardsIt
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperationOnPrimary
specifier|protected
name|PrimaryResponse
argument_list|<
name|BulkShardResponse
argument_list|,
name|BulkShardRequest
argument_list|>
name|shardOperationOnPrimary
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|PrimaryOperationRequest
name|shardRequest
parameter_list|)
block|{
specifier|final
name|BulkShardRequest
name|request
init|=
name|shardRequest
operator|.
name|request
decl_stmt|;
name|IndexShard
name|indexShard
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|shardRequest
operator|.
name|request
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|shardSafe
argument_list|(
name|shardRequest
operator|.
name|shardId
argument_list|)
decl_stmt|;
name|Engine
operator|.
name|IndexingOperation
index|[]
name|ops
init|=
literal|null
decl_stmt|;
name|BulkItemResponse
index|[]
name|responses
init|=
operator|new
name|BulkItemResponse
index|[
name|request
operator|.
name|items
argument_list|()
operator|.
name|length
index|]
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
name|request
operator|.
name|items
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|BulkItemRequest
name|item
init|=
name|request
operator|.
name|items
argument_list|()
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|item
operator|.
name|request
argument_list|()
operator|instanceof
name|IndexRequest
condition|)
block|{
name|IndexRequest
name|indexRequest
init|=
operator|(
name|IndexRequest
operator|)
name|item
operator|.
name|request
argument_list|()
decl_stmt|;
try|try
block|{
comment|// validate, if routing is required, that we got routing
name|MappingMetaData
name|mappingMd
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|mapping
argument_list|(
name|indexRequest
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|mappingMd
operator|!=
literal|null
operator|&&
name|mappingMd
operator|.
name|routing
argument_list|()
operator|.
name|required
argument_list|()
condition|)
block|{
if|if
condition|(
name|indexRequest
operator|.
name|routing
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RoutingMissingException
argument_list|(
name|indexRequest
operator|.
name|index
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|type
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|id
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|SourceToParse
name|sourceToParse
init|=
name|SourceToParse
operator|.
name|source
argument_list|(
name|indexRequest
operator|.
name|underlyingSource
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|underlyingSourceOffset
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|underlyingSourceLength
argument_list|()
argument_list|)
operator|.
name|type
argument_list|(
name|indexRequest
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|id
argument_list|(
name|indexRequest
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|routing
argument_list|(
name|indexRequest
operator|.
name|routing
argument_list|()
argument_list|)
operator|.
name|parent
argument_list|(
name|indexRequest
operator|.
name|parent
argument_list|()
argument_list|)
operator|.
name|timestamp
argument_list|(
name|indexRequest
operator|.
name|timestamp
argument_list|()
argument_list|)
operator|.
name|ttl
argument_list|(
name|indexRequest
operator|.
name|ttl
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|version
decl_stmt|;
name|Engine
operator|.
name|IndexingOperation
name|op
decl_stmt|;
if|if
condition|(
name|indexRequest
operator|.
name|opType
argument_list|()
operator|==
name|IndexRequest
operator|.
name|OpType
operator|.
name|INDEX
condition|)
block|{
name|Engine
operator|.
name|Index
name|index
init|=
name|indexShard
operator|.
name|prepareIndex
argument_list|(
name|sourceToParse
argument_list|)
operator|.
name|version
argument_list|(
name|indexRequest
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|versionType
argument_list|(
name|indexRequest
operator|.
name|versionType
argument_list|()
argument_list|)
operator|.
name|origin
argument_list|(
name|Engine
operator|.
name|Operation
operator|.
name|Origin
operator|.
name|PRIMARY
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|index
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|version
operator|=
name|index
operator|.
name|version
argument_list|()
expr_stmt|;
name|op
operator|=
name|index
expr_stmt|;
block|}
else|else
block|{
name|Engine
operator|.
name|Create
name|create
init|=
name|indexShard
operator|.
name|prepareCreate
argument_list|(
name|sourceToParse
argument_list|)
operator|.
name|version
argument_list|(
name|indexRequest
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|versionType
argument_list|(
name|indexRequest
operator|.
name|versionType
argument_list|()
argument_list|)
operator|.
name|origin
argument_list|(
name|Engine
operator|.
name|Operation
operator|.
name|Origin
operator|.
name|PRIMARY
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|create
argument_list|(
name|create
argument_list|)
expr_stmt|;
name|version
operator|=
name|create
operator|.
name|version
argument_list|()
expr_stmt|;
name|op
operator|=
name|create
expr_stmt|;
block|}
comment|// update the version on request so it will happen on the replicas
name|indexRequest
operator|.
name|version
argument_list|(
name|version
argument_list|)
expr_stmt|;
comment|// update mapping on master if needed, we won't update changes to the same type, since once its changed, it won't have mappers added
if|if
condition|(
name|op
operator|.
name|parsedDoc
argument_list|()
operator|.
name|mappersAdded
argument_list|()
condition|)
block|{
name|updateMappingOnMaster
argument_list|(
name|indexRequest
argument_list|)
expr_stmt|;
block|}
comment|// if we are going to percolate, then we need to keep this op for the postPrimary operation
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|indexRequest
operator|.
name|percolate
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|ops
operator|==
literal|null
condition|)
block|{
name|ops
operator|=
operator|new
name|Engine
operator|.
name|IndexingOperation
index|[
name|request
operator|.
name|items
argument_list|()
operator|.
name|length
index|]
expr_stmt|;
block|}
name|ops
index|[
name|i
index|]
operator|=
name|op
expr_stmt|;
block|}
comment|// add the response
name|responses
index|[
name|i
index|]
operator|=
operator|new
name|BulkItemResponse
argument_list|(
name|item
operator|.
name|id
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|opType
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|,
operator|new
name|IndexResponse
argument_list|(
name|indexRequest
operator|.
name|index
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|type
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|id
argument_list|()
argument_list|,
name|version
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// rethrow the failure if we are going to retry on primary and let parent failure to handle it
if|if
condition|(
name|retryPrimaryException
argument_list|(
name|e
argument_list|)
condition|)
block|{
throw|throw
operator|(
name|ElasticSearchException
operator|)
name|e
throw|;
block|}
if|if
condition|(
name|e
operator|instanceof
name|ElasticSearchException
operator|&&
operator|(
operator|(
name|ElasticSearchException
operator|)
name|e
operator|)
operator|.
name|status
argument_list|()
operator|==
name|RestStatus
operator|.
name|CONFLICT
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] failed to bulk item (index) {}"
argument_list|,
name|e
argument_list|,
name|shardRequest
operator|.
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|shardRequest
operator|.
name|shardId
argument_list|,
name|indexRequest
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}] failed to bulk item (index) {}"
argument_list|,
name|e
argument_list|,
name|shardRequest
operator|.
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|shardRequest
operator|.
name|shardId
argument_list|,
name|indexRequest
argument_list|)
expr_stmt|;
block|}
name|responses
index|[
name|i
index|]
operator|=
operator|new
name|BulkItemResponse
argument_list|(
name|item
operator|.
name|id
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|opType
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|,
operator|new
name|BulkItemResponse
operator|.
name|Failure
argument_list|(
name|indexRequest
operator|.
name|index
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|type
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|id
argument_list|()
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// nullify the request so it won't execute on the replicas
name|request
operator|.
name|items
argument_list|()
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|item
operator|.
name|request
argument_list|()
operator|instanceof
name|DeleteRequest
condition|)
block|{
name|DeleteRequest
name|deleteRequest
init|=
operator|(
name|DeleteRequest
operator|)
name|item
operator|.
name|request
argument_list|()
decl_stmt|;
try|try
block|{
name|Engine
operator|.
name|Delete
name|delete
init|=
name|indexShard
operator|.
name|prepareDelete
argument_list|(
name|deleteRequest
operator|.
name|type
argument_list|()
argument_list|,
name|deleteRequest
operator|.
name|id
argument_list|()
argument_list|,
name|deleteRequest
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|versionType
argument_list|(
name|deleteRequest
operator|.
name|versionType
argument_list|()
argument_list|)
operator|.
name|origin
argument_list|(
name|Engine
operator|.
name|Operation
operator|.
name|Origin
operator|.
name|PRIMARY
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
comment|// update the request with teh version so it will go to the replicas
name|deleteRequest
operator|.
name|version
argument_list|(
name|delete
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
comment|// add the response
name|responses
index|[
name|i
index|]
operator|=
operator|new
name|BulkItemResponse
argument_list|(
name|item
operator|.
name|id
argument_list|()
argument_list|,
literal|"delete"
argument_list|,
operator|new
name|DeleteResponse
argument_list|(
name|deleteRequest
operator|.
name|index
argument_list|()
argument_list|,
name|deleteRequest
operator|.
name|type
argument_list|()
argument_list|,
name|deleteRequest
operator|.
name|id
argument_list|()
argument_list|,
name|delete
operator|.
name|version
argument_list|()
argument_list|,
name|delete
operator|.
name|notFound
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// rethrow the failure if we are going to retry on primary and let parent failure to handle it
if|if
condition|(
name|retryPrimaryException
argument_list|(
name|e
argument_list|)
condition|)
block|{
throw|throw
operator|(
name|ElasticSearchException
operator|)
name|e
throw|;
block|}
if|if
condition|(
name|e
operator|instanceof
name|ElasticSearchException
operator|&&
operator|(
operator|(
name|ElasticSearchException
operator|)
name|e
operator|)
operator|.
name|status
argument_list|()
operator|==
name|RestStatus
operator|.
name|CONFLICT
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] failed to bulk item (delete) {}"
argument_list|,
name|e
argument_list|,
name|shardRequest
operator|.
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|shardRequest
operator|.
name|shardId
argument_list|,
name|deleteRequest
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}] failed to bulk item (delete) {}"
argument_list|,
name|e
argument_list|,
name|shardRequest
operator|.
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|shardRequest
operator|.
name|shardId
argument_list|,
name|deleteRequest
argument_list|)
expr_stmt|;
block|}
name|responses
index|[
name|i
index|]
operator|=
operator|new
name|BulkItemResponse
argument_list|(
name|item
operator|.
name|id
argument_list|()
argument_list|,
literal|"delete"
argument_list|,
operator|new
name|BulkItemResponse
operator|.
name|Failure
argument_list|(
name|deleteRequest
operator|.
name|index
argument_list|()
argument_list|,
name|deleteRequest
operator|.
name|type
argument_list|()
argument_list|,
name|deleteRequest
operator|.
name|id
argument_list|()
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// nullify the request so it won't execute on the replicas
name|request
operator|.
name|items
argument_list|()
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|request
operator|.
name|refresh
argument_list|()
condition|)
block|{
try|try
block|{
name|indexShard
operator|.
name|refresh
argument_list|(
operator|new
name|Engine
operator|.
name|Refresh
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
name|BulkShardResponse
name|response
init|=
operator|new
name|BulkShardResponse
argument_list|(
operator|new
name|ShardId
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|,
name|responses
argument_list|)
decl_stmt|;
return|return
operator|new
name|PrimaryResponse
argument_list|<
name|BulkShardResponse
argument_list|,
name|BulkShardRequest
argument_list|>
argument_list|(
name|shardRequest
operator|.
name|request
argument_list|,
name|response
argument_list|,
name|ops
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|postPrimaryOperation
specifier|protected
name|void
name|postPrimaryOperation
parameter_list|(
name|BulkShardRequest
name|request
parameter_list|,
name|PrimaryResponse
argument_list|<
name|BulkShardResponse
argument_list|,
name|BulkShardRequest
argument_list|>
name|response
parameter_list|)
block|{
name|IndexService
name|indexService
init|=
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
name|Engine
operator|.
name|IndexingOperation
index|[]
name|ops
init|=
operator|(
name|Engine
operator|.
name|IndexingOperation
index|[]
operator|)
name|response
operator|.
name|payload
argument_list|()
decl_stmt|;
if|if
condition|(
name|ops
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ops
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|BulkItemRequest
name|itemRequest
init|=
name|request
operator|.
name|items
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|BulkItemResponse
name|itemResponse
init|=
name|response
operator|.
name|response
argument_list|()
operator|.
name|responses
argument_list|()
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|itemResponse
operator|.
name|failed
argument_list|()
condition|)
block|{
comment|// failure, continue
continue|continue;
block|}
name|Engine
operator|.
name|IndexingOperation
name|op
init|=
name|ops
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|op
operator|==
literal|null
condition|)
block|{
continue|continue;
comment|// failed / no matches requested
block|}
if|if
condition|(
name|itemRequest
operator|.
name|request
argument_list|()
operator|instanceof
name|IndexRequest
condition|)
block|{
name|IndexRequest
name|indexRequest
init|=
operator|(
name|IndexRequest
operator|)
name|itemRequest
operator|.
name|request
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Strings
operator|.
name|hasLength
argument_list|(
name|indexRequest
operator|.
name|percolate
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
try|try
block|{
name|PercolatorExecutor
operator|.
name|Response
name|percolate
init|=
name|indexService
operator|.
name|percolateService
argument_list|()
operator|.
name|percolate
argument_list|(
operator|new
name|PercolatorExecutor
operator|.
name|DocAndSourceQueryRequest
argument_list|(
name|op
operator|.
name|parsedDoc
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|percolate
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
operator|(
operator|(
name|IndexResponse
operator|)
name|itemResponse
operator|.
name|response
argument_list|()
operator|)
operator|.
name|matches
argument_list|(
name|percolate
operator|.
name|matches
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
name|warn
argument_list|(
literal|"failed to percolate [{}]"
argument_list|,
name|e
argument_list|,
name|itemRequest
operator|.
name|request
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|shardOperationOnReplica
specifier|protected
name|void
name|shardOperationOnReplica
parameter_list|(
name|ReplicaOperationRequest
name|shardRequest
parameter_list|)
block|{
name|IndexShard
name|indexShard
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|shardRequest
operator|.
name|request
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|shardSafe
argument_list|(
name|shardRequest
operator|.
name|shardId
argument_list|)
decl_stmt|;
specifier|final
name|BulkShardRequest
name|request
init|=
name|shardRequest
operator|.
name|request
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
name|request
operator|.
name|items
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|BulkItemRequest
name|item
init|=
name|request
operator|.
name|items
argument_list|()
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|item
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|item
operator|.
name|request
argument_list|()
operator|instanceof
name|IndexRequest
condition|)
block|{
name|IndexRequest
name|indexRequest
init|=
operator|(
name|IndexRequest
operator|)
name|item
operator|.
name|request
argument_list|()
decl_stmt|;
try|try
block|{
name|SourceToParse
name|sourceToParse
init|=
name|SourceToParse
operator|.
name|source
argument_list|(
name|indexRequest
operator|.
name|underlyingSource
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|underlyingSourceOffset
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|underlyingSourceLength
argument_list|()
argument_list|)
operator|.
name|type
argument_list|(
name|indexRequest
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|id
argument_list|(
name|indexRequest
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|routing
argument_list|(
name|indexRequest
operator|.
name|routing
argument_list|()
argument_list|)
operator|.
name|parent
argument_list|(
name|indexRequest
operator|.
name|parent
argument_list|()
argument_list|)
operator|.
name|timestamp
argument_list|(
name|indexRequest
operator|.
name|timestamp
argument_list|()
argument_list|)
operator|.
name|ttl
argument_list|(
name|indexRequest
operator|.
name|ttl
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRequest
operator|.
name|opType
argument_list|()
operator|==
name|IndexRequest
operator|.
name|OpType
operator|.
name|INDEX
condition|)
block|{
name|Engine
operator|.
name|Index
name|index
init|=
name|indexShard
operator|.
name|prepareIndex
argument_list|(
name|sourceToParse
argument_list|)
operator|.
name|version
argument_list|(
name|indexRequest
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|origin
argument_list|(
name|Engine
operator|.
name|Operation
operator|.
name|Origin
operator|.
name|REPLICA
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|index
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Engine
operator|.
name|Create
name|create
init|=
name|indexShard
operator|.
name|prepareCreate
argument_list|(
name|sourceToParse
argument_list|)
operator|.
name|version
argument_list|(
name|indexRequest
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|origin
argument_list|(
name|Engine
operator|.
name|Operation
operator|.
name|Origin
operator|.
name|REPLICA
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|create
argument_list|(
name|create
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore, we are on backup
block|}
block|}
elseif|else
if|if
condition|(
name|item
operator|.
name|request
argument_list|()
operator|instanceof
name|DeleteRequest
condition|)
block|{
name|DeleteRequest
name|deleteRequest
init|=
operator|(
name|DeleteRequest
operator|)
name|item
operator|.
name|request
argument_list|()
decl_stmt|;
try|try
block|{
name|Engine
operator|.
name|Delete
name|delete
init|=
name|indexShard
operator|.
name|prepareDelete
argument_list|(
name|deleteRequest
operator|.
name|type
argument_list|()
argument_list|,
name|deleteRequest
operator|.
name|id
argument_list|()
argument_list|,
name|deleteRequest
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|origin
argument_list|(
name|Engine
operator|.
name|Operation
operator|.
name|Origin
operator|.
name|REPLICA
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore, we are on backup
block|}
block|}
block|}
if|if
condition|(
name|request
operator|.
name|refresh
argument_list|()
condition|)
block|{
try|try
block|{
name|indexShard
operator|.
name|refresh
argument_list|(
operator|new
name|Engine
operator|.
name|Refresh
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
DECL|method|updateMappingOnMaster
specifier|private
name|void
name|updateMappingOnMaster
parameter_list|(
specifier|final
name|IndexRequest
name|request
parameter_list|)
block|{
try|try
block|{
name|MapperService
name|mapperService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|mapperService
argument_list|()
decl_stmt|;
specifier|final
name|DocumentMapper
name|documentMapper
init|=
name|mapperService
operator|.
name|documentMapper
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|documentMapper
operator|==
literal|null
condition|)
block|{
comment|// should not happen
return|return;
block|}
name|documentMapper
operator|.
name|refreshSource
argument_list|()
expr_stmt|;
name|mappingUpdatedAction
operator|.
name|execute
argument_list|(
operator|new
name|MappingUpdatedAction
operator|.
name|MappingUpdatedRequest
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|type
argument_list|()
argument_list|,
name|documentMapper
operator|.
name|mappingSource
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|MappingUpdatedAction
operator|.
name|MappingUpdatedResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|MappingUpdatedAction
operator|.
name|MappingUpdatedResponse
name|mappingUpdatedResponse
parameter_list|)
block|{
comment|// all is well
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
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to update master on updated mapping for index ["
operator|+
name|request
operator|.
name|index
argument_list|()
operator|+
literal|"], type ["
operator|+
name|request
operator|.
name|type
argument_list|()
operator|+
literal|"] and source ["
operator|+
name|documentMapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|string
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
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
name|warn
argument_list|(
literal|"Failed to update master on updated mapping for index ["
operator|+
name|request
operator|.
name|index
argument_list|()
operator|+
literal|"], type ["
operator|+
name|request
operator|.
name|type
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

