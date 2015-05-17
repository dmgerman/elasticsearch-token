begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.seal
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
name|seal
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
name|HandledTransportAction
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
name|node
operator|.
name|DiscoveryNode
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|concurrent
operator|.
name|ConcurrentCollections
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
name|concurrent
operator|.
name|CountDown
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
name|IndicesLifecycle
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
name|SyncedFlushService
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
name|*
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|TransportSealIndicesAction
specifier|public
class|class
name|TransportSealIndicesAction
extends|extends
name|HandledTransportAction
argument_list|<
name|SealIndicesRequest
argument_list|,
name|SealIndicesResponse
argument_list|>
block|{
DECL|field|syncedFlushService
specifier|final
specifier|private
name|SyncedFlushService
name|syncedFlushService
decl_stmt|;
DECL|field|clusterService
specifier|final
specifier|private
name|ClusterService
name|clusterService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportSealIndicesAction
specifier|public
name|TransportSealIndicesAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|SyncedFlushService
name|syncedFlushService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|SealIndicesAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|SealIndicesRequest
operator|.
name|class
argument_list|)
expr_stmt|;
name|this
operator|.
name|syncedFlushService
operator|=
name|syncedFlushService
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
name|SealIndicesRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|SealIndicesResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|ClusterState
name|state
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|concreteIndices
argument_list|(
name|request
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
name|GroupShardsIterator
name|primaries
init|=
name|state
operator|.
name|routingTable
argument_list|()
operator|.
name|activePrimaryShardsGrouped
argument_list|(
name|concreteIndices
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|SyncedFlushService
operator|.
name|SyncedFlushResult
argument_list|>
name|results
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentSet
argument_list|()
decl_stmt|;
specifier|final
name|CountDown
name|countDown
init|=
operator|new
name|CountDown
argument_list|(
name|primaries
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|ShardIterator
name|shard
range|:
name|primaries
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|results
operator|.
name|add
argument_list|(
operator|new
name|SyncedFlushService
operator|.
name|SyncedFlushResult
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
literal|"no active primary available"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|countDown
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|SealIndicesResponse
argument_list|(
name|results
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
specifier|final
name|ShardId
name|shardId
init|=
name|shard
operator|.
name|shardId
argument_list|()
decl_stmt|;
name|syncedFlushService
operator|.
name|attemptSyncedFlush
argument_list|(
name|shardId
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|SyncedFlushService
operator|.
name|SyncedFlushResult
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|SyncedFlushService
operator|.
name|SyncedFlushResult
name|syncedFlushResult
parameter_list|)
block|{
name|results
operator|.
name|add
argument_list|(
name|syncedFlushResult
argument_list|)
expr_stmt|;
if|if
condition|(
name|countDown
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|SealIndicesResponse
argument_list|(
name|results
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|logger
operator|.
name|debug
argument_list|(
literal|"{} unexpected error while executing synced flush"
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|results
operator|.
name|add
argument_list|(
operator|new
name|SyncedFlushService
operator|.
name|SyncedFlushResult
argument_list|(
name|shardId
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|countDown
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|SealIndicesResponse
argument_list|(
name|results
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

