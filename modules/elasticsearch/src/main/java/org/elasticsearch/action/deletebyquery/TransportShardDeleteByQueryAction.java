begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
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
comment|/**  * @author kimchy (Shay Banon)  */
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
name|ShardDeleteByQueryResponse
argument_list|>
block|{
DECL|method|TransportShardDeleteByQueryAction
annotation|@
name|Inject
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
block|}
DECL|method|checkWriteConsistency
annotation|@
name|Override
specifier|protected
name|boolean
name|checkWriteConsistency
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|executor
annotation|@
name|Override
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
DECL|method|newRequestInstance
annotation|@
name|Override
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
DECL|method|newResponseInstance
annotation|@
name|Override
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
DECL|method|transportAction
annotation|@
name|Override
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
literal|"indices/index/shard/deleteByQuery"
return|;
block|}
DECL|method|checkBlock
annotation|@
name|Override
specifier|protected
name|void
name|checkBlock
parameter_list|(
name|ShardDeleteByQueryRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indexBlockedRaiseException
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
expr_stmt|;
block|}
DECL|method|shardOperationOnPrimary
annotation|@
name|Override
specifier|protected
name|PrimaryResponse
argument_list|<
name|ShardDeleteByQueryResponse
argument_list|>
name|shardOperationOnPrimary
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|ShardOperationRequest
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
name|indexShard
argument_list|(
name|shardRequest
argument_list|)
operator|.
name|deleteByQuery
argument_list|(
name|request
operator|.
name|querySource
argument_list|()
argument_list|,
name|request
operator|.
name|filteringAliases
argument_list|()
argument_list|,
name|request
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|PrimaryResponse
argument_list|<
name|ShardDeleteByQueryResponse
argument_list|>
argument_list|(
operator|new
name|ShardDeleteByQueryResponse
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|shardOperationOnReplica
annotation|@
name|Override
specifier|protected
name|void
name|shardOperationOnReplica
parameter_list|(
name|ShardOperationRequest
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
name|indexShard
argument_list|(
name|shardRequest
argument_list|)
operator|.
name|deleteByQuery
argument_list|(
name|request
operator|.
name|querySource
argument_list|()
argument_list|,
name|request
operator|.
name|filteringAliases
argument_list|()
argument_list|,
name|request
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|shards
annotation|@
name|Override
specifier|protected
name|ShardIterator
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|ShardDeleteByQueryRequest
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
name|index
argument_list|()
argument_list|,
name|request
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
name|ElasticSearchIllegalStateException
argument_list|(
literal|"No shards iterator found for shard ["
operator|+
name|request
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

