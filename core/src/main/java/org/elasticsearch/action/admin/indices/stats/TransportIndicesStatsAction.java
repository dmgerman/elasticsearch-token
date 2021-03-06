begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.stats
package|package
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
name|stats
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
name|broadcast
operator|.
name|node
operator|.
name|TransportBroadcastByNodeAction
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
name|ShardRouting
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
name|ShardsIterator
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|ShardNotFoundException
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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

begin_class
DECL|class|TransportIndicesStatsAction
specifier|public
class|class
name|TransportIndicesStatsAction
extends|extends
name|TransportBroadcastByNodeAction
argument_list|<
name|IndicesStatsRequest
argument_list|,
name|IndicesStatsResponse
argument_list|,
name|ShardStats
argument_list|>
block|{
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportIndicesStatsAction
specifier|public
name|TransportIndicesStatsAction
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
name|IndicesService
name|indicesService
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
name|IndicesStatsAction
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
name|IndicesStatsRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|MANAGEMENT
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
block|}
comment|/**      * Status goes across *all* shards.      */
annotation|@
name|Override
DECL|method|shards
specifier|protected
name|ShardsIterator
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|IndicesStatsRequest
name|request
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|)
block|{
return|return
name|clusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|allShards
argument_list|(
name|concreteIndices
argument_list|)
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
name|IndicesStatsRequest
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
name|METADATA_READ
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
name|IndicesStatsRequest
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
name|METADATA_READ
argument_list|,
name|concreteIndices
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readShardResult
specifier|protected
name|ShardStats
name|readShardResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ShardStats
operator|.
name|readShardStats
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|IndicesStatsResponse
name|newResponse
parameter_list|(
name|IndicesStatsRequest
name|request
parameter_list|,
name|int
name|totalShards
parameter_list|,
name|int
name|successfulShards
parameter_list|,
name|int
name|failedShards
parameter_list|,
name|List
argument_list|<
name|ShardStats
argument_list|>
name|responses
parameter_list|,
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|shardFailures
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
return|return
operator|new
name|IndicesStatsResponse
argument_list|(
name|responses
operator|.
name|toArray
argument_list|(
operator|new
name|ShardStats
index|[
name|responses
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|totalShards
argument_list|,
name|successfulShards
argument_list|,
name|failedShards
argument_list|,
name|shardFailures
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readRequestFrom
specifier|protected
name|IndicesStatsRequest
name|readRequestFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|IndicesStatsRequest
name|request
init|=
operator|new
name|IndicesStatsRequest
argument_list|()
decl_stmt|;
name|request
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|request
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperation
specifier|protected
name|ShardStats
name|shardOperation
parameter_list|(
name|IndicesStatsRequest
name|request
parameter_list|,
name|ShardRouting
name|shardRouting
parameter_list|)
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
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
name|getShard
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
comment|// if we don't have the routing entry yet, we need it stats wise, we treat it as if the shard is not ready yet
if|if
condition|(
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ShardNotFoundException
argument_list|(
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
throw|;
block|}
name|CommonStatsFlags
name|flags
init|=
operator|new
name|CommonStatsFlags
argument_list|()
operator|.
name|clear
argument_list|()
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|docs
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Docs
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|store
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Store
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|indexing
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Indexing
argument_list|)
expr_stmt|;
name|flags
operator|.
name|types
argument_list|(
name|request
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|get
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Get
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|search
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Search
argument_list|)
expr_stmt|;
name|flags
operator|.
name|groups
argument_list|(
name|request
operator|.
name|groups
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|merge
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Merge
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
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Refresh
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|flush
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Flush
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|warmer
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Warmer
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|queryCache
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|QueryCache
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|fieldData
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|FieldData
argument_list|)
expr_stmt|;
name|flags
operator|.
name|fieldDataFields
argument_list|(
name|request
operator|.
name|fieldDataFields
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|segments
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Segments
argument_list|)
expr_stmt|;
name|flags
operator|.
name|includeSegmentFileSizes
argument_list|(
name|request
operator|.
name|includeSegmentFileSizes
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|completion
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Completion
argument_list|)
expr_stmt|;
name|flags
operator|.
name|completionDataFields
argument_list|(
name|request
operator|.
name|completionFields
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|translog
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Translog
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|suggest
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Suggest
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|requestCache
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|RequestCache
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|recovery
argument_list|()
condition|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|Recovery
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ShardStats
argument_list|(
name|indexShard
operator|.
name|routingEntry
argument_list|()
argument_list|,
name|indexShard
operator|.
name|shardPath
argument_list|()
argument_list|,
operator|new
name|CommonStats
argument_list|(
name|indicesService
operator|.
name|getIndicesQueryCache
argument_list|()
argument_list|,
name|indexShard
argument_list|,
name|flags
argument_list|)
argument_list|,
name|indexShard
operator|.
name|commitStats
argument_list|()
argument_list|,
name|indexShard
operator|.
name|seqNoStats
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

