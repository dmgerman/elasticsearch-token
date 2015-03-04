begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.recovery
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
name|recovery
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|BroadcastShardOperationRequest
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
name|TransportBroadcastOperationAction
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
name|indices
operator|.
name|recovery
operator|.
name|RecoveryState
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
name|HashMap
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReferenceArray
import|;
end_import

begin_comment
comment|/**  * Transport action for shard recovery operation. This transport action does not actually  * perform shard recovery, it only reports on recoveries (both active and complete).  */
end_comment

begin_class
DECL|class|TransportRecoveryAction
specifier|public
class|class
name|TransportRecoveryAction
extends|extends
name|TransportBroadcastOperationAction
argument_list|<
name|RecoveryRequest
argument_list|,
name|RecoveryResponse
argument_list|,
name|TransportRecoveryAction
operator|.
name|ShardRecoveryRequest
argument_list|,
name|ShardRecoveryResponse
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
DECL|method|TransportRecoveryAction
specifier|public
name|TransportRecoveryAction
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
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|RecoveryAction
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
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
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
name|MANAGEMENT
return|;
block|}
annotation|@
name|Override
DECL|method|newRequest
specifier|protected
name|RecoveryRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|RecoveryRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|RecoveryResponse
name|newResponse
parameter_list|(
name|RecoveryRequest
name|request
parameter_list|,
name|AtomicReferenceArray
name|shardsResponses
parameter_list|,
name|ClusterState
name|clusterState
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
name|ShardOperationFailedException
argument_list|>
name|shardFailures
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
argument_list|>
name|shardResponses
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
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
name|ShardRecoveryResponse
name|recoveryResponse
init|=
operator|(
name|ShardRecoveryResponse
operator|)
name|shardResponse
decl_stmt|;
name|successfulShards
operator|++
expr_stmt|;
if|if
condition|(
name|recoveryResponse
operator|.
name|recoveryState
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// recovery not yet started
continue|continue;
block|}
name|String
name|indexName
init|=
name|recoveryResponse
operator|.
name|getIndex
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|responses
init|=
name|shardResponses
operator|.
name|get
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
if|if
condition|(
name|responses
operator|==
literal|null
condition|)
block|{
name|responses
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|shardResponses
operator|.
name|put
argument_list|(
name|indexName
argument_list|,
name|responses
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|activeOnly
argument_list|()
condition|)
block|{
if|if
condition|(
name|recoveryResponse
operator|.
name|recoveryState
argument_list|()
operator|.
name|getStage
argument_list|()
operator|!=
name|RecoveryState
operator|.
name|Stage
operator|.
name|DONE
condition|)
block|{
name|responses
operator|.
name|add
argument_list|(
name|recoveryResponse
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|responses
operator|.
name|add
argument_list|(
name|recoveryResponse
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|RecoveryResponse
name|response
init|=
operator|new
name|RecoveryResponse
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
name|request
operator|.
name|detailed
argument_list|()
argument_list|,
name|shardResponses
argument_list|,
name|shardFailures
argument_list|)
decl_stmt|;
return|return
name|response
return|;
block|}
annotation|@
name|Override
DECL|method|newShardRequest
specifier|protected
name|ShardRecoveryRequest
name|newShardRequest
parameter_list|()
block|{
return|return
operator|new
name|ShardRecoveryRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newShardRequest
specifier|protected
name|ShardRecoveryRequest
name|newShardRequest
parameter_list|(
name|int
name|numShards
parameter_list|,
name|ShardRouting
name|shard
parameter_list|,
name|RecoveryRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|ShardRecoveryRequest
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newShardResponse
specifier|protected
name|ShardRecoveryResponse
name|newShardResponse
parameter_list|()
block|{
return|return
operator|new
name|ShardRecoveryResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperation
specifier|protected
name|ShardRecoveryResponse
name|shardOperation
parameter_list|(
name|ShardRecoveryRequest
name|request
parameter_list|)
throws|throws
name|ElasticsearchException
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
name|shardSafe
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
name|ShardRecoveryResponse
name|shardRecoveryResponse
init|=
operator|new
name|ShardRecoveryResponse
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
name|RecoveryState
name|state
init|=
name|indexShard
operator|.
name|recoveryState
argument_list|()
decl_stmt|;
name|shardRecoveryResponse
operator|.
name|recoveryState
argument_list|(
name|state
argument_list|)
expr_stmt|;
return|return
name|shardRecoveryResponse
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
name|state
parameter_list|,
name|RecoveryRequest
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
name|routingTable
argument_list|()
operator|.
name|allAssignedShardsGrouped
argument_list|(
name|concreteIndices
argument_list|,
literal|true
argument_list|,
literal|true
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
name|RecoveryRequest
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
name|RecoveryRequest
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
DECL|class|ShardRecoveryRequest
specifier|static
class|class
name|ShardRecoveryRequest
extends|extends
name|BroadcastShardOperationRequest
block|{
DECL|method|ShardRecoveryRequest
name|ShardRecoveryRequest
parameter_list|()
block|{         }
DECL|method|ShardRecoveryRequest
name|ShardRecoveryRequest
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|RecoveryRequest
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

