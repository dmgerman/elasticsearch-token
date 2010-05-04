begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.ping.broadcast
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|ping
operator|.
name|broadcast
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
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|util
operator|.
name|settings
operator|.
name|Settings
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReferenceArray
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|TransportBroadcastPingAction
specifier|public
class|class
name|TransportBroadcastPingAction
extends|extends
name|TransportBroadcastOperationAction
argument_list|<
name|BroadcastPingRequest
argument_list|,
name|BroadcastPingResponse
argument_list|,
name|BroadcastShardPingRequest
argument_list|,
name|BroadcastShardPingResponse
argument_list|>
block|{
DECL|method|TransportBroadcastPingAction
annotation|@
name|Inject
specifier|public
name|TransportBroadcastPingAction
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
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|indicesService
argument_list|)
expr_stmt|;
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
name|TransportActions
operator|.
name|Admin
operator|.
name|Cluster
operator|.
name|Ping
operator|.
name|BROADCAST
return|;
block|}
DECL|method|transportShardAction
annotation|@
name|Override
specifier|protected
name|String
name|transportShardAction
parameter_list|()
block|{
return|return
literal|"/cluster/ping/broadcast/shard"
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|BroadcastPingRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|BroadcastPingRequest
argument_list|()
return|;
block|}
DECL|method|shards
annotation|@
name|Override
specifier|protected
name|GroupShardsIterator
name|shards
parameter_list|(
name|BroadcastPingRequest
name|request
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
return|return
name|indicesService
operator|.
name|searchShards
argument_list|(
name|clusterState
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|,
name|request
operator|.
name|queryHint
argument_list|()
argument_list|)
return|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|BroadcastPingResponse
name|newResponse
parameter_list|(
name|BroadcastPingRequest
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
name|failedShards
operator|++
expr_stmt|;
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
name|newArrayList
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
name|successfulShards
operator|++
expr_stmt|;
block|}
block|}
return|return
operator|new
name|BroadcastPingResponse
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
name|shardFailures
argument_list|)
return|;
block|}
DECL|method|newShardRequest
annotation|@
name|Override
specifier|protected
name|BroadcastShardPingRequest
name|newShardRequest
parameter_list|()
block|{
return|return
operator|new
name|BroadcastShardPingRequest
argument_list|()
return|;
block|}
DECL|method|newShardRequest
annotation|@
name|Override
specifier|protected
name|BroadcastShardPingRequest
name|newShardRequest
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
name|BroadcastPingRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|BroadcastShardPingRequest
argument_list|(
name|shard
operator|.
name|index
argument_list|()
argument_list|,
name|shard
operator|.
name|id
argument_list|()
argument_list|)
return|;
block|}
DECL|method|newShardResponse
annotation|@
name|Override
specifier|protected
name|BroadcastShardPingResponse
name|newShardResponse
parameter_list|()
block|{
return|return
operator|new
name|BroadcastShardPingResponse
argument_list|()
return|;
block|}
DECL|method|shardOperation
annotation|@
name|Override
specifier|protected
name|BroadcastShardPingResponse
name|shardOperation
parameter_list|(
name|BroadcastShardPingRequest
name|broadcastShardPingRequest
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
return|return
operator|new
name|BroadcastShardPingResponse
argument_list|()
return|;
block|}
block|}
end_class

end_unit

