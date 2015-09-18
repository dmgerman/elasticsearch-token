begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.refresh
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
name|refresh
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
name|ReplicationRequest
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
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportShardRefreshAction
specifier|public
class|class
name|TransportShardRefreshAction
extends|extends
name|TransportReplicationAction
argument_list|<
name|ReplicationRequest
argument_list|,
name|ReplicationRequest
argument_list|,
name|ActionWriteResponse
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
name|RefreshAction
operator|.
name|NAME
operator|+
literal|"[s]"
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportShardRefreshAction
specifier|public
name|TransportShardRefreshAction
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
name|indexNameExpressionResolver
argument_list|,
name|ReplicationRequest
operator|::
operator|new
argument_list|,
name|ReplicationRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|REFRESH
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newResponseInstance
specifier|protected
name|ActionWriteResponse
name|newResponseInstance
parameter_list|()
block|{
return|return
operator|new
name|ActionWriteResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperationOnPrimary
specifier|protected
name|Tuple
argument_list|<
name|ActionWriteResponse
argument_list|,
name|ReplicationRequest
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
name|IndexShard
name|indexShard
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
name|indexShard
operator|.
name|refresh
argument_list|(
literal|"api"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"{} refresh request executed on primary"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|Tuple
argument_list|<>
argument_list|(
operator|new
name|ActionWriteResponse
argument_list|()
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
name|ReplicationRequest
name|request
parameter_list|)
block|{
name|IndexShard
name|indexShard
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
operator|.
name|shardSafe
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|refresh
argument_list|(
literal|"api"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"{} refresh request executed on replica"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
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
literal|false
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
name|clusterState
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|indicesRouting
argument_list|()
operator|.
name|get
argument_list|(
name|request
operator|.
name|concreteIndex
argument_list|()
argument_list|)
operator|.
name|getShards
argument_list|()
operator|.
name|get
argument_list|(
name|request
operator|.
name|request
argument_list|()
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|shardsIt
argument_list|()
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
name|METADATA_WRITE
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
name|InternalRequest
name|request
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
name|METADATA_WRITE
argument_list|,
operator|new
name|String
index|[]
block|{
name|request
operator|.
name|concreteIndex
argument_list|()
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|shouldExecuteReplication
specifier|protected
name|boolean
name|shouldExecuteReplication
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit
