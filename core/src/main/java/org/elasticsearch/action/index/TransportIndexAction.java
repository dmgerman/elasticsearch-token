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
name|TransportReplicationAction
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
name|collect
operator|.
name|Tuple
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
name|Mapping
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
name|TransportReplicationAction
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
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
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
name|mappingUpdatedAction
argument_list|,
name|actionFilters
argument_list|,
name|IndexRequest
operator|.
name|class
argument_list|,
name|IndexRequest
operator|.
name|class
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|INDEX
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
name|CreateIndexRequest
name|createIndexRequest
init|=
operator|new
name|CreateIndexRequest
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|createIndexRequest
operator|.
name|index
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|createIndexRequest
operator|.
name|mapping
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|createIndexRequest
operator|.
name|cause
argument_list|(
literal|"auto(index api)"
argument_list|)
expr_stmt|;
name|createIndexRequest
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|timeout
argument_list|()
argument_list|)
expr_stmt|;
name|createIndexAction
operator|.
name|execute
argument_list|(
name|createIndexRequest
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
DECL|method|resolveIndex
specifier|protected
name|boolean
name|resolveIndex
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|resolveRequest
specifier|protected
name|void
name|resolveRequest
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|InternalRequest
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
name|concreteIndex
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
name|concreteIndex
argument_list|()
argument_list|)
operator|.
name|mappingOrDefault
argument_list|(
name|request
operator|.
name|request
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|request
operator|.
name|request
argument_list|()
operator|.
name|process
argument_list|(
name|metaData
argument_list|,
name|mappingMd
argument_list|,
name|allowIdGeneration
argument_list|,
name|request
operator|.
name|concreteIndex
argument_list|()
argument_list|)
expr_stmt|;
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
DECL|method|shards
specifier|protected
name|ShardIterator
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|InternalRequest
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
name|concreteIndex
argument_list|()
argument_list|,
name|request
operator|.
name|request
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|request
operator|.
name|request
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|request
argument_list|()
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
name|Tuple
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
throws|throws
name|Throwable
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
name|shardRequest
operator|.
name|shardId
operator|.
name|getIndex
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
name|shardRequest
operator|.
name|shardId
operator|.
name|getIndex
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
name|shardId
operator|.
name|getIndex
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
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|WriteResult
argument_list|<
name|IndexResponse
argument_list|>
name|result
init|=
name|executeIndexRequestOnPrimary
argument_list|(
literal|null
argument_list|,
name|request
argument_list|,
name|indexShard
argument_list|)
decl_stmt|;
specifier|final
name|IndexResponse
name|response
init|=
name|result
operator|.
name|response
decl_stmt|;
specifier|final
name|Translog
operator|.
name|Location
name|location
init|=
name|result
operator|.
name|location
decl_stmt|;
name|processAfter
argument_list|(
name|request
argument_list|,
name|indexShard
argument_list|,
name|location
argument_list|)
expr_stmt|;
return|return
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|response
argument_list|,
name|shardRequest
operator|.
name|request
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
name|ShardId
name|shardId
parameter_list|,
name|IndexRequest
name|request
parameter_list|)
block|{
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
name|IndexShard
name|indexShard
init|=
name|indexService
operator|.
name|shardSafe
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
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
specifier|final
name|Engine
operator|.
name|IndexingOperation
name|operation
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
name|operation
operator|=
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
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|request
operator|.
name|opType
argument_list|()
operator|==
name|IndexRequest
operator|.
name|OpType
operator|.
name|CREATE
operator|:
name|request
operator|.
name|opType
argument_list|()
assert|;
name|operation
operator|=
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
expr_stmt|;
block|}
name|Mapping
name|update
init|=
name|operation
operator|.
name|parsedDoc
argument_list|()
operator|.
name|dynamicMappingsUpdate
argument_list|()
decl_stmt|;
if|if
condition|(
name|update
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RetryOnReplicaException
argument_list|(
name|shardId
argument_list|,
literal|"Mappings are not available on the replica yet, triggered update: "
operator|+
name|update
argument_list|)
throw|;
block|}
name|operation
operator|.
name|execute
argument_list|(
name|indexShard
argument_list|)
expr_stmt|;
name|processAfter
argument_list|(
name|request
argument_list|,
name|indexShard
argument_list|,
name|operation
operator|.
name|getTranslogLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|processAfter
specifier|private
name|void
name|processAfter
parameter_list|(
name|IndexRequest
name|request
parameter_list|,
name|IndexShard
name|indexShard
parameter_list|,
name|Translog
operator|.
name|Location
name|location
parameter_list|)
block|{
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
literal|"refresh_flag_index"
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
if|if
condition|(
name|indexShard
operator|.
name|getTranslogDurability
argument_list|()
operator|==
name|Translog
operator|.
name|Durabilty
operator|.
name|REQUEST
operator|&&
name|location
operator|!=
literal|null
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
block|}
block|}
end_class

end_unit
