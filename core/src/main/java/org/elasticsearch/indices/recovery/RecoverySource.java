begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
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
name|ExceptionsHelper
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
name|routing
operator|.
name|RoutingNode
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
name|Nullable
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
name|component
operator|.
name|AbstractComponent
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
name|IndexEventListener
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
name|TransportChannel
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
name|TransportRequestHandler
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
name|HashSet
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
name|Set
import|;
end_import

begin_comment
comment|/**  * The source recovery accepts recovery requests from other peer shards and start the recovery process from this  * source shard to the target shard.  */
end_comment

begin_class
DECL|class|RecoverySource
specifier|public
class|class
name|RecoverySource
extends|extends
name|AbstractComponent
implements|implements
name|IndexEventListener
block|{
DECL|class|Actions
specifier|public
specifier|static
class|class
name|Actions
block|{
DECL|field|START_RECOVERY
specifier|public
specifier|static
specifier|final
name|String
name|START_RECOVERY
init|=
literal|"internal:index/shard/recovery/start_recovery"
decl_stmt|;
block|}
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|recoverySettings
specifier|private
specifier|final
name|RecoverySettings
name|recoverySettings
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|ongoingRecoveries
specifier|private
specifier|final
name|OngoingRecoveres
name|ongoingRecoveries
init|=
operator|new
name|OngoingRecoveres
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|RecoverySource
specifier|public
name|RecoverySource
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|RecoverySettings
name|recoverySettings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|recoverySettings
operator|=
name|recoverySettings
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|Actions
operator|.
name|START_RECOVERY
argument_list|,
name|StartRecoveryRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|,
operator|new
name|StartRecoveryTransportRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|beforeIndexShardClosed
specifier|public
name|void
name|beforeIndexShardClosed
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|Nullable
name|IndexShard
name|indexShard
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
if|if
condition|(
name|indexShard
operator|!=
literal|null
condition|)
block|{
name|ongoingRecoveries
operator|.
name|cancel
argument_list|(
name|indexShard
argument_list|,
literal|"shard is closed"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|recover
specifier|private
name|RecoveryResponse
name|recover
parameter_list|(
specifier|final
name|StartRecoveryRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
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
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|IndexShard
name|shard
init|=
name|indexService
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
comment|// starting recovery from that our (the source) shard state is marking the shard to be in recovery mode as well, otherwise
comment|// the index operations will not be routed to it properly
name|RoutingNode
name|node
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|request
operator|.
name|targetNode
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"delaying recovery of {} as source node {} is unknown"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|DelayRecoveryException
argument_list|(
literal|"source node does not have the node ["
operator|+
name|request
operator|.
name|targetNode
argument_list|()
operator|+
literal|"] in its state yet.."
argument_list|)
throw|;
block|}
name|ShardRouting
name|targetShardRouting
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|node
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|equals
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
condition|)
block|{
name|targetShardRouting
operator|=
name|shardRouting
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|targetShardRouting
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"delaying recovery of {} as it is not listed as assigned to target node {}"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|DelayRecoveryException
argument_list|(
literal|"source node does not have the shard listed in its state as allocated on the node"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|targetShardRouting
operator|.
name|initializing
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"delaying recovery of {} as it is not listed as initializing on the target node {}. known shards state is [{}]"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|targetShardRouting
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|DelayRecoveryException
argument_list|(
literal|"source node has the state of the target shard to be ["
operator|+
name|targetShardRouting
operator|.
name|state
argument_list|()
operator|+
literal|"], expecting to be [initializing]"
argument_list|)
throw|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] starting recovery to {}, mark_as_relocated {}"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|request
operator|.
name|markAsRelocated
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|RecoverySourceHandler
name|handler
decl_stmt|;
if|if
condition|(
name|shard
operator|.
name|indexSettings
argument_list|()
operator|.
name|isOnSharedFilesystem
argument_list|()
condition|)
block|{
name|handler
operator|=
operator|new
name|SharedFSRecoverySourceHandler
argument_list|(
name|shard
argument_list|,
name|request
argument_list|,
name|recoverySettings
argument_list|,
name|transportService
argument_list|,
name|logger
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|handler
operator|=
operator|new
name|RecoverySourceHandler
argument_list|(
name|shard
argument_list|,
name|request
argument_list|,
name|recoverySettings
argument_list|,
name|transportService
argument_list|,
name|logger
argument_list|)
expr_stmt|;
block|}
name|ongoingRecoveries
operator|.
name|add
argument_list|(
name|shard
argument_list|,
name|handler
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|handler
operator|.
name|recoverToTarget
argument_list|()
return|;
block|}
finally|finally
block|{
name|ongoingRecoveries
operator|.
name|remove
argument_list|(
name|shard
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|StartRecoveryTransportRequestHandler
class|class
name|StartRecoveryTransportRequestHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|StartRecoveryRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
specifier|final
name|StartRecoveryRequest
name|request
parameter_list|,
specifier|final
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|RecoveryResponse
name|response
init|=
name|recover
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|OngoingRecoveres
specifier|private
specifier|static
specifier|final
class|class
name|OngoingRecoveres
block|{
DECL|field|ongoingRecoveries
specifier|private
specifier|final
name|Map
argument_list|<
name|IndexShard
argument_list|,
name|Set
argument_list|<
name|RecoverySourceHandler
argument_list|>
argument_list|>
name|ongoingRecoveries
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|add
specifier|synchronized
name|void
name|add
parameter_list|(
name|IndexShard
name|shard
parameter_list|,
name|RecoverySourceHandler
name|handler
parameter_list|)
block|{
name|Set
argument_list|<
name|RecoverySourceHandler
argument_list|>
name|shardRecoveryHandlers
init|=
name|ongoingRecoveries
operator|.
name|get
argument_list|(
name|shard
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardRecoveryHandlers
operator|==
literal|null
condition|)
block|{
name|shardRecoveryHandlers
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|ongoingRecoveries
operator|.
name|put
argument_list|(
name|shard
argument_list|,
name|shardRecoveryHandlers
argument_list|)
expr_stmt|;
block|}
assert|assert
name|shardRecoveryHandlers
operator|.
name|contains
argument_list|(
name|handler
argument_list|)
operator|==
literal|false
operator|:
literal|"Handler was already registered ["
operator|+
name|handler
operator|+
literal|"]"
assert|;
name|shardRecoveryHandlers
operator|.
name|add
argument_list|(
name|handler
argument_list|)
expr_stmt|;
name|shard
operator|.
name|recoveryStats
argument_list|()
operator|.
name|incCurrentAsSource
argument_list|()
expr_stmt|;
block|}
DECL|method|remove
specifier|synchronized
name|void
name|remove
parameter_list|(
name|IndexShard
name|shard
parameter_list|,
name|RecoverySourceHandler
name|handler
parameter_list|)
block|{
specifier|final
name|Set
argument_list|<
name|RecoverySourceHandler
argument_list|>
name|shardRecoveryHandlers
init|=
name|ongoingRecoveries
operator|.
name|get
argument_list|(
name|shard
argument_list|)
decl_stmt|;
assert|assert
name|shardRecoveryHandlers
operator|!=
literal|null
operator|:
literal|"Shard was not registered ["
operator|+
name|shard
operator|+
literal|"]"
assert|;
name|boolean
name|remove
init|=
name|shardRecoveryHandlers
operator|.
name|remove
argument_list|(
name|handler
argument_list|)
decl_stmt|;
assert|assert
name|remove
operator|:
literal|"Handler was not registered ["
operator|+
name|handler
operator|+
literal|"]"
assert|;
if|if
condition|(
name|remove
condition|)
block|{
name|shard
operator|.
name|recoveryStats
argument_list|()
operator|.
name|decCurrentAsSource
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|shardRecoveryHandlers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ongoingRecoveries
operator|.
name|remove
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|cancel
specifier|synchronized
name|void
name|cancel
parameter_list|(
name|IndexShard
name|shard
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
specifier|final
name|Set
argument_list|<
name|RecoverySourceHandler
argument_list|>
name|shardRecoveryHandlers
init|=
name|ongoingRecoveries
operator|.
name|get
argument_list|(
name|shard
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardRecoveryHandlers
operator|!=
literal|null
condition|)
block|{
specifier|final
name|List
argument_list|<
name|Exception
argument_list|>
name|failures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|RecoverySourceHandler
name|handlers
range|:
name|shardRecoveryHandlers
control|)
block|{
try|try
block|{
name|handlers
operator|.
name|cancel
argument_list|(
name|reason
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|failures
operator|.
name|add
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|shard
operator|.
name|recoveryStats
argument_list|()
operator|.
name|decCurrentAsSource
argument_list|()
expr_stmt|;
block|}
block|}
name|ExceptionsHelper
operator|.
name|maybeThrowRuntimeAndSuppress
argument_list|(
name|failures
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

