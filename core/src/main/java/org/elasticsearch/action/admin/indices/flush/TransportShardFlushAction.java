begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.flush
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
name|flush
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
name|ReplicationResponse
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
DECL|class|TransportShardFlushAction
specifier|public
class|class
name|TransportShardFlushAction
extends|extends
name|TransportReplicationAction
argument_list|<
name|ShardFlushRequest
argument_list|,
name|ShardFlushRequest
argument_list|,
name|ReplicationResponse
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
name|FlushAction
operator|.
name|NAME
operator|+
literal|"[s]"
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportShardFlushAction
specifier|public
name|TransportShardFlushAction
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
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|ShardFlushRequest
operator|::
operator|new
argument_list|,
name|ShardFlushRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|FLUSH
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newResponseInstance
specifier|protected
name|ReplicationResponse
name|newResponseInstance
parameter_list|()
block|{
return|return
operator|new
name|ReplicationResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperationOnPrimary
specifier|protected
name|PrimaryResult
name|shardOperationOnPrimary
parameter_list|(
name|ShardFlushRequest
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
name|shardId
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|)
operator|.
name|getShard
argument_list|(
name|shardRequest
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|flush
argument_list|(
name|shardRequest
operator|.
name|getRequest
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"{} flush request executed on primary"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|PrimaryResult
argument_list|(
name|shardRequest
argument_list|,
operator|new
name|ReplicationResponse
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperationOnReplica
specifier|protected
name|ReplicaResult
name|shardOperationOnReplica
parameter_list|(
name|ShardFlushRequest
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
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|)
operator|.
name|getShard
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|flush
argument_list|(
name|request
operator|.
name|getRequest
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"{} flush request executed on replica"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|ReplicaResult
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|globalBlockLevel
specifier|protected
name|ClusterBlockLevel
name|globalBlockLevel
parameter_list|()
block|{
return|return
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
return|;
block|}
annotation|@
name|Override
DECL|method|indexBlockLevel
specifier|protected
name|ClusterBlockLevel
name|indexBlockLevel
parameter_list|()
block|{
return|return
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
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

