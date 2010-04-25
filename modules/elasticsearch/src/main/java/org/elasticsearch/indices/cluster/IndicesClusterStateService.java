begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this   * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cluster
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
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
name|ElasticSearchException
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
name|ClusterChangedEvent
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
name|ClusterStateListener
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
name|NodeIndexCreatedAction
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
name|NodeIndexDeletedAction
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
name|NodeMappingCreatedAction
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
name|metadata
operator|.
name|IndexMetaData
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
name|node
operator|.
name|DiscoveryNodes
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
name|IndexShardRoutingTable
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
name|RoutingTable
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
name|index
operator|.
name|IndexShardAlreadyExistsException
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
name|IndexShardMissingException
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
name|gateway
operator|.
name|IgnoreGatewayRecoveryException
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
name|gateway
operator|.
name|IndexShardGatewayService
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
name|mapper
operator|.
name|DocumentMapper
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
name|mapper
operator|.
name|MapperService
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
name|IndexShardState
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
name|recovery
operator|.
name|IgnoreRecoveryException
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
name|recovery
operator|.
name|RecoveryAction
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
name|index
operator|.
name|shard
operator|.
name|service
operator|.
name|InternalIndexShard
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
name|util
operator|.
name|component
operator|.
name|AbstractLifecycleComponent
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|Sets
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|IndicesClusterStateService
specifier|public
class|class
name|IndicesClusterStateService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|IndicesClusterStateService
argument_list|>
implements|implements
name|ClusterStateListener
block|{
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|shardStateAction
specifier|private
specifier|final
name|ShardStateAction
name|shardStateAction
decl_stmt|;
DECL|field|nodeIndexCreatedAction
specifier|private
specifier|final
name|NodeIndexCreatedAction
name|nodeIndexCreatedAction
decl_stmt|;
DECL|field|nodeIndexDeletedAction
specifier|private
specifier|final
name|NodeIndexDeletedAction
name|nodeIndexDeletedAction
decl_stmt|;
DECL|field|nodeMappingCreatedAction
specifier|private
specifier|final
name|NodeMappingCreatedAction
name|nodeMappingCreatedAction
decl_stmt|;
DECL|method|IndicesClusterStateService
annotation|@
name|Inject
specifier|public
name|IndicesClusterStateService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ShardStateAction
name|shardStateAction
parameter_list|,
name|NodeIndexCreatedAction
name|nodeIndexCreatedAction
parameter_list|,
name|NodeIndexDeletedAction
name|nodeIndexDeletedAction
parameter_list|,
name|NodeMappingCreatedAction
name|nodeMappingCreatedAction
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
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
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|shardStateAction
operator|=
name|shardStateAction
expr_stmt|;
name|this
operator|.
name|nodeIndexCreatedAction
operator|=
name|nodeIndexCreatedAction
expr_stmt|;
name|this
operator|.
name|nodeIndexDeletedAction
operator|=
name|nodeIndexDeletedAction
expr_stmt|;
name|this
operator|.
name|nodeMappingCreatedAction
operator|=
name|nodeMappingCreatedAction
expr_stmt|;
block|}
DECL|method|doStart
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|clusterService
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|doStop
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|doClose
annotation|@
name|Override
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
DECL|method|clusterChanged
annotation|@
name|Override
specifier|public
name|void
name|clusterChanged
parameter_list|(
specifier|final
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
if|if
condition|(
operator|!
name|indicesService
operator|.
name|changesAllowed
argument_list|()
condition|)
return|return;
name|applyNewIndices
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|applyMappings
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|applyNewShards
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|applyDeletedIndices
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|applyDeletedShards
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
DECL|method|applyDeletedIndices
specifier|private
name|void
name|applyDeletedIndices
parameter_list|(
specifier|final
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
for|for
control|(
specifier|final
name|String
name|index
range|:
name|indicesService
operator|.
name|indices
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|hasIndex
argument_list|(
name|index
argument_list|)
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Index [{}]: Deleting"
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
name|indicesService
operator|.
name|deleteIndex
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|nodeIndexDeletedAction
operator|.
name|nodeIndexDeleted
argument_list|(
name|index
argument_list|,
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|applyDeletedShards
specifier|private
name|void
name|applyDeletedShards
parameter_list|(
specifier|final
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
name|RoutingNode
name|routingNodes
init|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|readOnlyRoutingNodes
argument_list|()
operator|.
name|nodesToShards
argument_list|()
operator|.
name|get
argument_list|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|routingNodes
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
specifier|final
name|String
name|index
range|:
name|indicesService
operator|.
name|indices
argument_list|()
control|)
block|{
if|if
condition|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|hasIndex
argument_list|(
name|index
argument_list|)
condition|)
block|{
comment|// now, go over and delete shards that needs to get deleted
name|Set
argument_list|<
name|Integer
argument_list|>
name|newShardIds
init|=
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|ShardRouting
name|shardRouting
range|:
name|routingNodes
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|index
argument_list|()
operator|.
name|equals
argument_list|(
name|index
argument_list|)
condition|)
block|{
name|newShardIds
operator|.
name|add
argument_list|(
name|shardRouting
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|Integer
name|existingShardId
range|:
name|indexService
operator|.
name|shardIds
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|newShardIds
operator|.
name|contains
argument_list|(
name|existingShardId
argument_list|)
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Index [{}]: Deleting shard [{}]"
argument_list|,
name|index
argument_list|,
name|existingShardId
argument_list|)
expr_stmt|;
block|}
name|indexService
operator|.
name|deleteShard
argument_list|(
name|existingShardId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|applyNewIndices
specifier|private
name|void
name|applyNewIndices
parameter_list|(
specifier|final
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
comment|// first, go over and create and indices that needs to be created
for|for
control|(
specifier|final
name|IndexMetaData
name|indexMetaData
range|:
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|indicesService
operator|.
name|hasIndex
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Index [{}]: Creating"
argument_list|,
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indicesService
operator|.
name|createIndex
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|indexMetaData
operator|.
name|settings
argument_list|()
argument_list|,
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|nodeIndexCreatedAction
operator|.
name|nodeIndexCreated
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|applyMappings
specifier|private
name|void
name|applyMappings
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
comment|// go over and update mappings
for|for
control|(
name|IndexMetaData
name|indexMetaData
range|:
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|indicesService
operator|.
name|hasIndex
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
comment|// we only create / update here
continue|continue;
block|}
name|String
name|index
init|=
name|indexMetaData
operator|.
name|index
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|MapperService
name|mapperService
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
decl_stmt|;
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
init|=
name|indexMetaData
operator|.
name|mappings
argument_list|()
decl_stmt|;
comment|// we don't support removing mappings for now ...
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|mappings
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|mappingType
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|mappingSource
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|mapperService
operator|.
name|hasMapping
argument_list|(
name|mappingType
argument_list|)
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Index ["
operator|+
name|index
operator|+
literal|"] Adding mapping ["
operator|+
name|mappingType
operator|+
literal|"], source ["
operator|+
name|mappingSource
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|mapperService
operator|.
name|add
argument_list|(
name|mappingType
argument_list|,
name|mappingSource
argument_list|)
expr_stmt|;
name|nodeMappingCreatedAction
operator|.
name|nodeMappingCreated
argument_list|(
operator|new
name|NodeMappingCreatedAction
operator|.
name|NodeMappingCreatedResponse
argument_list|(
name|index
argument_list|,
name|mappingType
argument_list|,
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|DocumentMapper
name|existingMapper
init|=
name|mapperService
operator|.
name|documentMapper
argument_list|(
name|mappingType
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|mappingSource
operator|.
name|equals
argument_list|(
name|existingMapper
operator|.
name|mappingSource
argument_list|()
argument_list|)
condition|)
block|{
comment|// mapping changed, update it
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Index ["
operator|+
name|index
operator|+
literal|"] Updating mapping ["
operator|+
name|mappingType
operator|+
literal|"], source ["
operator|+
name|mappingSource
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|mapperService
operator|.
name|add
argument_list|(
name|mappingType
argument_list|,
name|mappingSource
argument_list|)
expr_stmt|;
name|nodeMappingCreatedAction
operator|.
name|nodeMappingCreated
argument_list|(
operator|new
name|NodeMappingCreatedAction
operator|.
name|NodeMappingCreatedResponse
argument_list|(
name|index
argument_list|,
name|mappingType
argument_list|,
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to add mapping ["
operator|+
name|mappingType
operator|+
literal|"], source ["
operator|+
name|mappingSource
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|applyNewShards
specifier|private
name|void
name|applyNewShards
parameter_list|(
specifier|final
name|ClusterChangedEvent
name|event
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|indicesService
operator|.
name|changesAllowed
argument_list|()
condition|)
return|return;
name|RoutingTable
name|routingTable
init|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|routingTable
argument_list|()
decl_stmt|;
name|RoutingNode
name|routingNodes
init|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|readOnlyRoutingNodes
argument_list|()
operator|.
name|nodesToShards
argument_list|()
operator|.
name|get
argument_list|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|routingNodes
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|DiscoveryNodes
name|nodes
init|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|ShardRouting
name|shardRouting
range|:
name|routingNodes
control|)
block|{
specifier|final
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|shardId
init|=
name|shardRouting
operator|.
name|id
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|indexService
operator|.
name|hasShard
argument_list|(
name|shardId
argument_list|)
operator|&&
name|shardRouting
operator|.
name|started
argument_list|()
condition|)
block|{
comment|// the master thinks we are started, but we don't have this shard at all, mark it as failed
name|logger
operator|.
name|warn
argument_list|(
literal|"["
operator|+
name|shardRouting
operator|.
name|index
argument_list|()
operator|+
literal|"]["
operator|+
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
operator|+
literal|"] Master "
operator|+
name|nodes
operator|.
name|masterNode
argument_list|()
operator|+
literal|" marked shard as started, but shard have not been created, mark shard as failed"
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|shardFailed
argument_list|(
name|shardRouting
argument_list|,
literal|"Master "
operator|+
name|nodes
operator|.
name|masterNode
argument_list|()
operator|+
literal|" marked shard as started, but shard have not been created, mark shard as failed"
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|indexService
operator|.
name|hasShard
argument_list|(
name|shardId
argument_list|)
condition|)
block|{
name|InternalIndexShard
name|indexShard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indexService
operator|.
name|shard
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|shardRouting
operator|.
name|equals
argument_list|(
name|indexShard
operator|.
name|routingEntry
argument_list|()
argument_list|)
condition|)
block|{
name|indexShard
operator|.
name|routingEntry
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
name|indexService
operator|.
name|shardInjector
argument_list|(
name|shardId
argument_list|)
operator|.
name|getInstance
argument_list|(
name|IndexShardGatewayService
operator|.
name|class
argument_list|)
operator|.
name|routingStateChanged
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|shardRouting
operator|.
name|initializing
argument_list|()
condition|)
block|{
name|applyInitializingShard
argument_list|(
name|routingTable
argument_list|,
name|nodes
argument_list|,
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|applyInitializingShard
specifier|private
name|void
name|applyInitializingShard
parameter_list|(
specifier|final
name|RoutingTable
name|routingTable
parameter_list|,
specifier|final
name|DiscoveryNodes
name|nodes
parameter_list|,
specifier|final
name|ShardRouting
name|shardRouting
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
specifier|final
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|shardId
init|=
name|shardRouting
operator|.
name|id
argument_list|()
decl_stmt|;
if|if
condition|(
name|indexService
operator|.
name|hasShard
argument_list|(
name|shardId
argument_list|)
condition|)
block|{
name|IndexShard
name|indexShard
init|=
name|indexService
operator|.
name|shardSafe
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|STARTED
condition|)
block|{
comment|// the master thinks we are initializing, but we are already started
comment|// (either master failover, or a cluster event before we managed to tell the master we started), mark us as started
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"["
operator|+
name|shardRouting
operator|.
name|index
argument_list|()
operator|+
literal|"]["
operator|+
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
operator|+
literal|"] Master "
operator|+
name|nodes
operator|.
name|masterNode
argument_list|()
operator|+
literal|" marked shard as initializing, but shard already started, mark shard as started"
argument_list|)
expr_stmt|;
block|}
name|shardStateAction
operator|.
name|shardStarted
argument_list|(
name|shardRouting
argument_list|,
literal|"Master "
operator|+
name|nodes
operator|.
name|masterNode
argument_list|()
operator|+
literal|" marked shard as initializing, but shard already started, mark shard as started"
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
if|if
condition|(
name|indexShard
operator|.
name|ignoreRecoveryAttempt
argument_list|()
condition|)
block|{
return|return;
block|}
block|}
block|}
comment|// if there is no shard, create it
if|if
condition|(
operator|!
name|indexService
operator|.
name|hasShard
argument_list|(
name|shardId
argument_list|)
condition|)
block|{
try|try
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Index [{}]: Creating shard [{}]"
argument_list|,
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
name|InternalIndexShard
name|indexShard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indexService
operator|.
name|createShard
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|indexShard
operator|.
name|routingEntry
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexShardAlreadyExistsException
name|e
parameter_list|)
block|{
comment|// ignore this, the method call can happen several times
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to create shard for index ["
operator|+
name|indexService
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
operator|+
literal|"] and shard id ["
operator|+
name|shardRouting
operator|.
name|id
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
try|try
block|{
name|indexService
operator|.
name|deleteShard
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexShardMissingException
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to delete shard after failed creation for index ["
operator|+
name|indexService
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
operator|+
literal|"] and shard id ["
operator|+
name|shardRouting
operator|.
name|id
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
name|shardStateAction
operator|.
name|shardFailed
argument_list|(
name|shardRouting
argument_list|,
literal|"Failed to create shard, message ["
operator|+
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"]"
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
specifier|final
name|InternalIndexShard
name|indexShard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indexService
operator|.
name|shardSafe
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexShard
operator|.
name|ignoreRecoveryAttempt
argument_list|()
condition|)
block|{
comment|// we are already recovering (we can get to this state since the cluster event can happen several
comment|// times while we recover)
return|return;
block|}
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// recheck here, since the cluster event can be called
if|if
condition|(
name|indexShard
operator|.
name|ignoreRecoveryAttempt
argument_list|()
condition|)
block|{
return|return;
block|}
try|try
block|{
name|RecoveryAction
name|recoveryAction
init|=
name|indexService
operator|.
name|shardInjector
argument_list|(
name|shardId
argument_list|)
operator|.
name|getInstance
argument_list|(
name|RecoveryAction
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
comment|// recovery from primary
name|IndexShardRoutingTable
name|shardRoutingTable
init|=
name|routingTable
operator|.
name|index
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|shard
argument_list|(
name|shardRouting
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ShardRouting
name|entry
range|:
name|shardRoutingTable
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|primary
argument_list|()
operator|&&
name|entry
operator|.
name|started
argument_list|()
condition|)
block|{
comment|// only recover from started primary, if we can't find one, we will do it next round
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|entry
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
comment|// we are recovering a backup from a primary, so no need to mark it as relocated
name|recoveryAction
operator|.
name|startRecovery
argument_list|(
name|nodes
operator|.
name|localNode
argument_list|()
argument_list|,
name|node
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|shardStarted
argument_list|(
name|shardRouting
argument_list|,
literal|"after recovery (backup) from node ["
operator|+
name|node
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IgnoreRecoveryException
name|e
parameter_list|)
block|{
comment|// that's fine, since we might be called concurrently, just ignore this
break|break;
block|}
break|break;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|shardRouting
operator|.
name|relocatingNodeId
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// we are the first primary, recover from the gateway
name|IndexShardGatewayService
name|shardGatewayService
init|=
name|indexService
operator|.
name|shardInjector
argument_list|(
name|shardId
argument_list|)
operator|.
name|getInstance
argument_list|(
name|IndexShardGatewayService
operator|.
name|class
argument_list|)
decl_stmt|;
try|try
block|{
name|shardGatewayService
operator|.
name|recover
argument_list|()
expr_stmt|;
name|shardStateAction
operator|.
name|shardStarted
argument_list|(
name|shardRouting
argument_list|,
literal|"after recovery from gateway"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IgnoreGatewayRecoveryException
name|e
parameter_list|)
block|{
comment|// that's fine, we might be called concurrently, just ignore this, we already recovered
block|}
block|}
else|else
block|{
comment|// relocating primaries, recovery from the relocating shard
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|shardRouting
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
comment|// we mark the primary we are going to recover from as relocated at the end of phase 3
comment|// so operations will start moving to the new primary
name|recoveryAction
operator|.
name|startRecovery
argument_list|(
name|nodes
operator|.
name|localNode
argument_list|()
argument_list|,
name|node
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|shardStateAction
operator|.
name|shardStarted
argument_list|(
name|shardRouting
argument_list|,
literal|"after recovery (primary) from node ["
operator|+
name|node
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IgnoreRecoveryException
name|e
parameter_list|)
block|{
comment|// that's fine, since we might be called concurrently, just ignore this, we are already recovering
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to start shard for index ["
operator|+
name|indexService
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
operator|+
literal|"] and shard id ["
operator|+
name|shardRouting
operator|.
name|id
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexService
operator|.
name|hasShard
argument_list|(
name|shardId
argument_list|)
condition|)
block|{
try|try
block|{
name|indexService
operator|.
name|deleteShard
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to delete shard after failed startup for index ["
operator|+
name|indexService
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
operator|+
literal|"] and shard id ["
operator|+
name|shardRouting
operator|.
name|id
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
name|shardStateAction
operator|.
name|shardFailed
argument_list|(
name|shardRouting
argument_list|,
literal|"Failed to start shard, message ["
operator|+
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to mark shard as failed after a failed start for index ["
operator|+
name|indexService
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
operator|+
literal|"] and shard id ["
operator|+
name|shardRouting
operator|.
name|id
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

