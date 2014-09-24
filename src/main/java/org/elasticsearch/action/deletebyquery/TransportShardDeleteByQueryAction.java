begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.deletebyquery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|deletebyquery
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Filter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalStateException
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
name|cache
operator|.
name|recycler
operator|.
name|PageCacheRecycler
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
name|BigArrays
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
name|query
operator|.
name|ParsedQuery
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
name|IndicesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|DefaultSearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|ShardSearchLocalRequest
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
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportShardDeleteByQueryAction
specifier|public
class|class
name|TransportShardDeleteByQueryAction
extends|extends
name|TransportShardReplicationOperationAction
argument_list|<
name|ShardDeleteByQueryRequest
argument_list|,
name|ShardDeleteByQueryRequest
argument_list|,
name|ShardDeleteByQueryResponse
argument_list|>
block|{
DECL|field|DELETE_BY_QUERY_API
specifier|public
specifier|final
specifier|static
name|String
name|DELETE_BY_QUERY_API
init|=
literal|"delete_by_query"
decl_stmt|;
DECL|field|ACTION_NAME
specifier|private
specifier|static
specifier|final
name|String
name|ACTION_NAME
init|=
name|DeleteByQueryAction
operator|.
name|NAME
operator|+
literal|"[s]"
decl_stmt|;
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|pageCacheRecycler
specifier|private
specifier|final
name|PageCacheRecycler
name|pageCacheRecycler
decl_stmt|;
DECL|field|bigArrays
specifier|private
specifier|final
name|BigArrays
name|bigArrays
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportShardDeleteByQueryAction
specifier|public
name|TransportShardDeleteByQueryAction
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
name|ScriptService
name|scriptService
parameter_list|,
name|PageCacheRecycler
name|pageCacheRecycler
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|ACTION_NAME
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
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|pageCacheRecycler
operator|=
name|pageCacheRecycler
expr_stmt|;
name|this
operator|.
name|bigArrays
operator|=
name|bigArrays
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
DECL|method|newRequestInstance
specifier|protected
name|ShardDeleteByQueryRequest
name|newRequestInstance
parameter_list|()
block|{
return|return
operator|new
name|ShardDeleteByQueryRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newReplicaRequestInstance
specifier|protected
name|ShardDeleteByQueryRequest
name|newReplicaRequestInstance
parameter_list|()
block|{
return|return
operator|new
name|ShardDeleteByQueryRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponseInstance
specifier|protected
name|ShardDeleteByQueryResponse
name|newResponseInstance
parameter_list|()
block|{
return|return
operator|new
name|ShardDeleteByQueryResponse
argument_list|()
return|;
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
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperationOnPrimary
specifier|protected
name|PrimaryResponse
argument_list|<
name|ShardDeleteByQueryResponse
argument_list|,
name|ShardDeleteByQueryRequest
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
name|ShardDeleteByQueryRequest
name|request
init|=
name|shardRequest
operator|.
name|request
decl_stmt|;
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
name|SearchContext
operator|.
name|setCurrent
argument_list|(
operator|new
name|DefaultSearchContext
argument_list|(
literal|0
argument_list|,
operator|new
name|ShardSearchLocalRequest
argument_list|(
name|request
operator|.
name|types
argument_list|()
argument_list|,
name|request
operator|.
name|nowInMillis
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
name|indexShard
operator|.
name|acquireSearcher
argument_list|(
name|DELETE_BY_QUERY_API
argument_list|)
argument_list|,
name|indexService
argument_list|,
name|indexShard
argument_list|,
name|scriptService
argument_list|,
name|pageCacheRecycler
argument_list|,
name|bigArrays
argument_list|,
name|threadPool
operator|.
name|estimatedTimeInMillisCounter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|Engine
operator|.
name|DeleteByQuery
name|deleteByQuery
init|=
name|indexShard
operator|.
name|prepareDeleteByQuery
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|,
name|request
operator|.
name|filteringAliases
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
name|types
argument_list|()
argument_list|)
decl_stmt|;
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|parsedQuery
argument_list|(
operator|new
name|ParsedQuery
argument_list|(
name|deleteByQuery
operator|.
name|query
argument_list|()
argument_list|,
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|Filter
operator|>
name|of
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|deleteByQuery
argument_list|(
name|deleteByQuery
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
init|(
name|SearchContext
name|searchContext
init|=
name|SearchContext
operator|.
name|current
argument_list|()
init|)
block|{
name|SearchContext
operator|.
name|removeCurrent
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|PrimaryResponse
argument_list|<>
argument_list|(
name|shardRequest
operator|.
name|request
argument_list|,
operator|new
name|ShardDeleteByQueryResponse
argument_list|()
argument_list|,
literal|null
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
name|ShardDeleteByQueryRequest
name|request
init|=
name|shardRequest
operator|.
name|request
decl_stmt|;
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
name|SearchContext
operator|.
name|setCurrent
argument_list|(
operator|new
name|DefaultSearchContext
argument_list|(
literal|0
argument_list|,
operator|new
name|ShardSearchLocalRequest
argument_list|(
name|request
operator|.
name|types
argument_list|()
argument_list|,
name|request
operator|.
name|nowInMillis
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
name|indexShard
operator|.
name|acquireSearcher
argument_list|(
name|DELETE_BY_QUERY_API
argument_list|,
name|IndexShard
operator|.
name|Mode
operator|.
name|WRITE
argument_list|)
argument_list|,
name|indexService
argument_list|,
name|indexShard
argument_list|,
name|scriptService
argument_list|,
name|pageCacheRecycler
argument_list|,
name|bigArrays
argument_list|,
name|threadPool
operator|.
name|estimatedTimeInMillisCounter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|Engine
operator|.
name|DeleteByQuery
name|deleteByQuery
init|=
name|indexShard
operator|.
name|prepareDeleteByQuery
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|,
name|request
operator|.
name|filteringAliases
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
name|types
argument_list|()
argument_list|)
decl_stmt|;
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|parsedQuery
argument_list|(
operator|new
name|ParsedQuery
argument_list|(
name|deleteByQuery
operator|.
name|query
argument_list|()
argument_list|,
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|Filter
operator|>
name|of
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|deleteByQuery
argument_list|(
name|deleteByQuery
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
init|(
name|SearchContext
name|searchContext
init|=
name|SearchContext
operator|.
name|current
argument_list|()
init|)
block|{
name|SearchContext
operator|.
name|removeCurrent
argument_list|()
expr_stmt|;
block|}
block|}
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
name|GroupShardsIterator
name|group
init|=
name|clusterService
operator|.
name|operationRouting
argument_list|()
operator|.
name|deleteByQueryShards
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
name|routing
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ShardIterator
name|shardIt
range|:
name|group
control|)
block|{
if|if
condition|(
name|shardIt
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
operator|==
name|request
operator|.
name|request
argument_list|()
operator|.
name|shardId
argument_list|()
condition|)
block|{
return|return
name|shardIt
return|;
block|}
block|}
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
literal|"No shards iterator found for shard ["
operator|+
name|request
operator|.
name|request
argument_list|()
operator|.
name|shardId
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

