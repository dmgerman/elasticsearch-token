begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.shards
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
name|shards
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
name|master
operator|.
name|TransportMasterNodeReadOperationAction
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
operator|.
name|newHashSet
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|TransportClusterSearchShardsAction
specifier|public
class|class
name|TransportClusterSearchShardsAction
extends|extends
name|TransportMasterNodeReadOperationAction
argument_list|<
name|ClusterSearchShardsRequest
argument_list|,
name|ClusterSearchShardsResponse
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|TransportClusterSearchShardsAction
specifier|public
name|TransportClusterSearchShardsAction
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
name|ThreadPool
name|threadPool
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|ClusterSearchShardsAction
operator|.
name|NAME
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|,
name|actionFilters
argument_list|,
name|ClusterSearchShardsRequest
operator|.
name|class
argument_list|)
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
comment|// all in memory work here...
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|ClusterSearchShardsRequest
name|request
parameter_list|,
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
name|indicesBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_READ
argument_list|,
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
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|ClusterSearchShardsResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|ClusterSearchShardsResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|masterOperation
specifier|protected
name|void
name|masterOperation
parameter_list|(
specifier|final
name|ClusterSearchShardsRequest
name|request
parameter_list|,
specifier|final
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|ClusterSearchShardsResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|ClusterState
name|clusterState
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
name|clusterState
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
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|routingMap
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|resolveSearchRouting
argument_list|(
name|request
operator|.
name|routing
argument_list|()
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|nodeIds
init|=
name|newHashSet
argument_list|()
decl_stmt|;
name|GroupShardsIterator
name|groupShardsIterator
init|=
name|clusterService
operator|.
name|operationRouting
argument_list|()
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
name|concreteIndices
argument_list|,
name|routingMap
argument_list|,
name|request
operator|.
name|preference
argument_list|()
argument_list|)
decl_stmt|;
name|ShardRouting
name|shard
decl_stmt|;
name|ClusterSearchShardsGroup
index|[]
name|groupResponses
init|=
operator|new
name|ClusterSearchShardsGroup
index|[
name|groupShardsIterator
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|currentGroup
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ShardIterator
name|shardIt
range|:
name|groupShardsIterator
control|)
block|{
name|String
name|index
init|=
name|shardIt
operator|.
name|shardId
argument_list|()
operator|.
name|getIndex
argument_list|()
decl_stmt|;
name|int
name|shardId
init|=
name|shardIt
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
decl_stmt|;
name|ShardRouting
index|[]
name|shardRoutings
init|=
operator|new
name|ShardRouting
index|[
name|shardIt
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|currentShard
init|=
literal|0
decl_stmt|;
name|shardIt
operator|.
name|reset
argument_list|()
expr_stmt|;
while|while
condition|(
operator|(
name|shard
operator|=
name|shardIt
operator|.
name|nextOrNull
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|shardRoutings
index|[
name|currentShard
operator|++
index|]
operator|=
name|shard
expr_stmt|;
name|nodeIds
operator|.
name|add
argument_list|(
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|groupResponses
index|[
name|currentGroup
operator|++
index|]
operator|=
operator|new
name|ClusterSearchShardsGroup
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|,
name|shardRoutings
argument_list|)
expr_stmt|;
block|}
name|DiscoveryNode
index|[]
name|nodes
init|=
operator|new
name|DiscoveryNode
index|[
name|nodeIds
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|currentNode
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|nodeId
range|:
name|nodeIds
control|)
block|{
name|nodes
index|[
name|currentNode
operator|++
index|]
operator|=
name|clusterState
operator|.
name|getNodes
argument_list|()
operator|.
name|get
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
block|}
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClusterSearchShardsResponse
argument_list|(
name|groupResponses
argument_list|,
name|nodes
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

