begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|support
operator|.
name|replication
operator|.
name|TransportIndexReplicationOperationAction
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
DECL|class|TransportIndexDeleteByQueryAction
specifier|public
class|class
name|TransportIndexDeleteByQueryAction
extends|extends
name|TransportIndexReplicationOperationAction
argument_list|<
name|IndexDeleteByQueryRequest
argument_list|,
name|IndexDeleteByQueryResponse
argument_list|,
name|ShardDeleteByQueryRequest
argument_list|,
name|ShardDeleteByQueryRequest
argument_list|,
name|ShardDeleteByQueryResponse
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|TransportIndexDeleteByQueryAction
specifier|public
name|TransportIndexDeleteByQueryAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportShardDeleteByQueryAction
name|shardDeleteByQueryAction
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
name|threadPool
argument_list|,
name|shardDeleteByQueryAction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newRequestInstance
specifier|protected
name|IndexDeleteByQueryRequest
name|newRequestInstance
parameter_list|()
block|{
return|return
operator|new
name|IndexDeleteByQueryRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponseInstance
specifier|protected
name|IndexDeleteByQueryResponse
name|newResponseInstance
parameter_list|(
name|IndexDeleteByQueryRequest
name|request
parameter_list|,
name|AtomicReferenceArray
name|shardsResponses
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
if|if
condition|(
name|shardsResponses
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
literal|null
condition|)
block|{
name|failedShards
operator|++
expr_stmt|;
block|}
else|else
block|{
name|successfulShards
operator|++
expr_stmt|;
block|}
block|}
return|return
operator|new
name|IndexDeleteByQueryResponse
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|successfulShards
argument_list|,
name|failedShards
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|accumulateExceptions
specifier|protected
name|boolean
name|accumulateExceptions
parameter_list|()
block|{
return|return
literal|false
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
name|DELETE_BY_QUERY
operator|+
literal|"/index"
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
name|IndexDeleteByQueryRequest
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
name|IndexDeleteByQueryRequest
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
name|GroupShardsIterator
name|shards
parameter_list|(
name|IndexDeleteByQueryRequest
name|request
parameter_list|)
block|{
return|return
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
return|;
block|}
annotation|@
name|Override
DECL|method|newShardRequestInstance
specifier|protected
name|ShardDeleteByQueryRequest
name|newShardRequestInstance
parameter_list|(
name|IndexDeleteByQueryRequest
name|request
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
return|return
operator|new
name|ShardDeleteByQueryRequest
argument_list|(
name|request
argument_list|,
name|shardId
argument_list|)
return|;
block|}
block|}
end_class

end_unit

