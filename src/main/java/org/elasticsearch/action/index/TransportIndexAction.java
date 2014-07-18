begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
package|;
end_package

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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|CreateIndexRequest
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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|CreateIndexResponse
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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|TransportCreateIndexAction
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
name|AutoCreateIndex
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
name|metadata
operator|.
name|MetaData
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
name|IndexAlreadyExistsException
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

begin_comment
comment|/**  * Performs the index operation.  *<p/>  *<p>Allows for the following settings:  *<ul>  *<li><b>autoCreateIndex</b>: When set to<tt>true</tt>, will automatically create an index if one does not exists.  * Defaults to<tt>true</tt>.  *<li><b>allowIdGeneration</b>: If the id is set not, should it be generated. Defaults to<tt>true</tt>.  *</ul>  */
end_comment

begin_class
DECL|class|TransportIndexAction
specifier|public
class|class
name|TransportIndexAction
extends|extends
name|TransportShardReplicationOperationAction
argument_list|<
name|IndexRequest
argument_list|,
name|IndexRequest
argument_list|,
name|IndexResponse
argument_list|>
block|{
DECL|field|autoCreateIndex
specifier|private
specifier|final
name|AutoCreateIndex
name|autoCreateIndex
decl_stmt|;
DECL|field|allowIdGeneration
specifier|private
specifier|final
name|boolean
name|allowIdGeneration
decl_stmt|;
DECL|field|createIndexAction
specifier|private
specifier|final
name|TransportCreateIndexAction
name|createIndexAction
decl_stmt|;
DECL|field|mappingUpdatedAction
specifier|private
specifier|final
name|MappingUpdatedAction
name|mappingUpdatedAction
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportIndexAction
specifier|public
name|TransportIndexAction
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
name|TransportCreateIndexAction
name|createIndexAction
parameter_list|,
name|MappingUpdatedAction
name|mappingUpdatedAction
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|IndexAction
operator|.
name|NAME
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
argument_list|)
expr_stmt|;
name|this
operator|.
name|createIndexAction
operator|=
name|createIndexAction
expr_stmt|;
name|this
operator|.
name|mappingUpdatedAction
operator|=
name|mappingUpdatedAction
expr_stmt|;
name|this
operator|.
name|autoCreateIndex
operator|=
operator|new
name|AutoCreateIndex
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|allowIdGeneration
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"action.allow_id_generation"
argument_list|,
literal|true
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
name|IndexRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|IndexResponse
argument_list|>
name|listener
parameter_list|)
block|{
comment|// if we don't have a master, we don't have metadata, that's fine, let it find a master using create index API
if|if
condition|(
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
condition|)
block|{
name|request
operator|.
name|beforeLocalFork
argument_list|()
expr_stmt|;
comment|// we fork on another thread...
name|createIndexAction
operator|.
name|execute
argument_list|(
operator|new
name|CreateIndexRequest
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|cause
argument_list|(
literal|"auto(index api)"
argument_list|)
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|timeout
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|CreateIndexResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|CreateIndexResponse
name|result
parameter_list|)
block|{
name|innerExecute
argument_list|(
name|request
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
if|if
condition|(
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|e
argument_list|)
operator|instanceof
name|IndexAlreadyExistsException
condition|)
block|{
comment|// we have the index, do it
try|try
block|{
name|innerExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e1
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
else|else
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
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|innerExecute
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
DECL|method|resolveRequest
specifier|protected
name|boolean
name|resolveRequest
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|IndexRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|IndexResponse
argument_list|>
name|indexResponseActionListener
parameter_list|)
block|{
name|MetaData
name|metaData
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
decl_stmt|;
name|String
name|aliasOrIndex
init|=
name|request
operator|.
name|index
argument_list|()
decl_stmt|;
name|request
operator|.
name|index
argument_list|(
name|metaData
operator|.
name|concreteSingleIndex
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|MappingMetaData
name|mappingMd
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|metaData
operator|.
name|hasIndex
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
name|mappingMd
operator|=
name|metaData
operator|.
name|index
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|mappingOrDefault
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|request
operator|.
name|process
argument_list|(
name|metaData
argument_list|,
name|aliasOrIndex
argument_list|,
name|mappingMd
argument_list|,
name|allowIdGeneration
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
DECL|method|innerExecute
specifier|private
name|void
name|innerExecute
parameter_list|(
specifier|final
name|IndexRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|IndexResponse
argument_list|>
name|listener
parameter_list|)
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
DECL|method|newRequestInstance
specifier|protected
name|IndexRequest
name|newRequestInstance
parameter_list|()
block|{
return|return
operator|new
name|IndexRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newReplicaRequestInstance
specifier|protected
name|IndexRequest
name|newReplicaRequestInstance
parameter_list|()
block|{
return|return
operator|new
name|IndexRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponseInstance
specifier|protected
name|IndexResponse
name|newResponseInstance
parameter_list|()
block|{
return|return
operator|new
name|IndexResponse
argument_list|()
return|;
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
DECL|method|checkGlobalBlock
specifier|protected
name|ClusterBlockException
name|checkGlobalBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|IndexRequest
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
name|IndexRequest
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
name|IndexRequest
name|request
parameter_list|)
block|{
return|return
name|clusterService
operator|.
name|operationRouting
argument_list|()
operator|.
name|indexShards
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|,
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
name|request
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|routing
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperationOnPrimary
specifier|protected
name|PrimaryResponse
argument_list|<
name|IndexResponse
argument_list|,
name|IndexRequest
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
name|IndexRequest
name|request
init|=
name|shardRequest
operator|.
name|request
decl_stmt|;
comment|// validate, if routing is required, that we got routing
name|IndexMetaData
name|indexMetaData
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
decl_stmt|;
name|MappingMetaData
name|mappingMd
init|=
name|indexMetaData
operator|.
name|mappingOrDefault
argument_list|(
name|request
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
name|request
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
name|request
operator|.
name|id
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|IndexService
name|indexService
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
decl_stmt|;
name|IndexShard
name|indexShard
init|=
name|indexService
operator|.
name|shardSafe
argument_list|(
name|shardRequest
operator|.
name|shardId
argument_list|)
decl_stmt|;
name|SourceToParse
name|sourceToParse
init|=
name|SourceToParse
operator|.
name|source
argument_list|(
name|SourceToParse
operator|.
name|Origin
operator|.
name|PRIMARY
argument_list|,
name|request
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|type
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|id
argument_list|(
name|request
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|routing
argument_list|(
name|request
operator|.
name|routing
argument_list|()
argument_list|)
operator|.
name|parent
argument_list|(
name|request
operator|.
name|parent
argument_list|()
argument_list|)
operator|.
name|timestamp
argument_list|(
name|request
operator|.
name|timestamp
argument_list|()
argument_list|)
operator|.
name|ttl
argument_list|(
name|request
operator|.
name|ttl
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|version
decl_stmt|;
name|boolean
name|created
decl_stmt|;
name|Engine
operator|.
name|IndexingOperation
name|op
decl_stmt|;
if|if
condition|(
name|request
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
argument_list|,
name|request
operator|.
name|version
argument_list|()
argument_list|,
name|request
operator|.
name|versionType
argument_list|()
argument_list|,
name|Engine
operator|.
name|Operation
operator|.
name|Origin
operator|.
name|PRIMARY
argument_list|,
name|request
operator|.
name|canHaveDuplicates
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|.
name|parsedDoc
argument_list|()
operator|.
name|mappingsModified
argument_list|()
condition|)
block|{
name|mappingUpdatedAction
operator|.
name|updateMappingOnMaster
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|index
operator|.
name|docMapper
argument_list|()
argument_list|,
name|indexService
operator|.
name|indexUUID
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|created
operator|=
name|index
operator|.
name|created
argument_list|()
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
argument_list|,
name|request
operator|.
name|version
argument_list|()
argument_list|,
name|request
operator|.
name|versionType
argument_list|()
argument_list|,
name|Engine
operator|.
name|Operation
operator|.
name|Origin
operator|.
name|PRIMARY
argument_list|,
name|request
operator|.
name|canHaveDuplicates
argument_list|()
argument_list|,
name|request
operator|.
name|autoGeneratedId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|create
operator|.
name|parsedDoc
argument_list|()
operator|.
name|mappingsModified
argument_list|()
condition|)
block|{
name|mappingUpdatedAction
operator|.
name|updateMappingOnMaster
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|create
operator|.
name|docMapper
argument_list|()
argument_list|,
name|indexService
operator|.
name|indexUUID
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|created
operator|=
literal|true
expr_stmt|;
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
literal|"refresh_flag_index"
argument_list|)
operator|.
name|force
argument_list|(
literal|false
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
comment|// ignore
block|}
block|}
comment|// update the version on the request, so it will be used for the replicas
name|request
operator|.
name|version
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|request
operator|.
name|versionType
argument_list|(
name|request
operator|.
name|versionType
argument_list|()
operator|.
name|versionTypeForReplicationAndRecovery
argument_list|()
argument_list|)
expr_stmt|;
assert|assert
name|request
operator|.
name|versionType
argument_list|()
operator|.
name|validateVersionForWrites
argument_list|(
name|request
operator|.
name|version
argument_list|()
argument_list|)
assert|;
name|IndexResponse
name|response
init|=
operator|new
name|IndexResponse
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
name|request
operator|.
name|id
argument_list|()
argument_list|,
name|version
argument_list|,
name|created
argument_list|)
decl_stmt|;
return|return
operator|new
name|PrimaryResponse
argument_list|<>
argument_list|(
name|shardRequest
operator|.
name|request
argument_list|,
name|response
argument_list|,
name|op
argument_list|)
return|;
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
name|IndexRequest
name|request
init|=
name|shardRequest
operator|.
name|request
decl_stmt|;
name|SourceToParse
name|sourceToParse
init|=
name|SourceToParse
operator|.
name|source
argument_list|(
name|SourceToParse
operator|.
name|Origin
operator|.
name|REPLICA
argument_list|,
name|request
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|type
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|id
argument_list|(
name|request
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|routing
argument_list|(
name|request
operator|.
name|routing
argument_list|()
argument_list|)
operator|.
name|parent
argument_list|(
name|request
operator|.
name|parent
argument_list|()
argument_list|)
operator|.
name|timestamp
argument_list|(
name|request
operator|.
name|timestamp
argument_list|()
argument_list|)
operator|.
name|ttl
argument_list|(
name|request
operator|.
name|ttl
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
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
argument_list|,
name|request
operator|.
name|version
argument_list|()
argument_list|,
name|request
operator|.
name|versionType
argument_list|()
argument_list|,
name|Engine
operator|.
name|Operation
operator|.
name|Origin
operator|.
name|REPLICA
argument_list|,
name|request
operator|.
name|canHaveDuplicates
argument_list|()
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
argument_list|,
name|request
operator|.
name|version
argument_list|()
argument_list|,
name|request
operator|.
name|versionType
argument_list|()
argument_list|,
name|Engine
operator|.
name|Operation
operator|.
name|Origin
operator|.
name|REPLICA
argument_list|,
name|request
operator|.
name|canHaveDuplicates
argument_list|()
argument_list|,
name|request
operator|.
name|autoGeneratedId
argument_list|()
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
literal|"refresh_flag_index"
argument_list|)
operator|.
name|force
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
block|}
end_class

end_unit

