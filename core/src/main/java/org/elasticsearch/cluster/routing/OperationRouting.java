begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
package|;
end_package

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
name|allocation
operator|.
name|decider
operator|.
name|AwarenessAllocationDecider
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
name|Strings
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
name|IndexNotFoundException
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
name|index
operator|.
name|shard
operator|.
name|ShardNotFoundException
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_class
DECL|class|OperationRouting
specifier|public
class|class
name|OperationRouting
extends|extends
name|AbstractComponent
block|{
DECL|field|awarenessAllocationDecider
specifier|private
specifier|final
name|AwarenessAllocationDecider
name|awarenessAllocationDecider
decl_stmt|;
annotation|@
name|Inject
DECL|method|OperationRouting
specifier|public
name|OperationRouting
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|AwarenessAllocationDecider
name|awarenessAllocationDecider
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|awarenessAllocationDecider
operator|=
name|awarenessAllocationDecider
expr_stmt|;
block|}
DECL|method|indexShards
specifier|public
name|ShardIterator
name|indexShards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
name|index
parameter_list|,
name|String
name|id
parameter_list|,
annotation|@
name|Nullable
name|String
name|routing
parameter_list|)
block|{
return|return
name|shards
argument_list|(
name|clusterState
argument_list|,
name|index
argument_list|,
name|id
argument_list|,
name|routing
argument_list|)
operator|.
name|shardsIt
argument_list|()
return|;
block|}
DECL|method|getShards
specifier|public
name|ShardIterator
name|getShards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
name|index
parameter_list|,
name|String
name|id
parameter_list|,
annotation|@
name|Nullable
name|String
name|routing
parameter_list|,
annotation|@
name|Nullable
name|String
name|preference
parameter_list|)
block|{
return|return
name|preferenceActiveShardIterator
argument_list|(
name|shards
argument_list|(
name|clusterState
argument_list|,
name|index
argument_list|,
name|id
argument_list|,
name|routing
argument_list|)
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|getLocalNodeId
argument_list|()
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|,
name|preference
argument_list|)
return|;
block|}
DECL|method|getShards
specifier|public
name|ShardIterator
name|getShards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
annotation|@
name|Nullable
name|String
name|preference
parameter_list|)
block|{
specifier|final
name|IndexShardRoutingTable
name|indexShard
init|=
name|clusterState
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|shardRoutingTable
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
decl_stmt|;
return|return
name|preferenceActiveShardIterator
argument_list|(
name|indexShard
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|getLocalNodeId
argument_list|()
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|,
name|preference
argument_list|)
return|;
block|}
DECL|method|searchShardsCount
specifier|public
name|int
name|searchShardsCount
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|routing
parameter_list|)
block|{
specifier|final
name|Set
argument_list|<
name|IndexShardRoutingTable
argument_list|>
name|shards
init|=
name|computeTargetedShards
argument_list|(
name|clusterState
argument_list|,
name|concreteIndices
argument_list|,
name|routing
argument_list|)
decl_stmt|;
return|return
name|shards
operator|.
name|size
argument_list|()
return|;
block|}
DECL|method|searchShards
specifier|public
name|GroupShardsIterator
name|searchShards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|routing
parameter_list|,
annotation|@
name|Nullable
name|String
name|preference
parameter_list|)
block|{
specifier|final
name|Set
argument_list|<
name|IndexShardRoutingTable
argument_list|>
name|shards
init|=
name|computeTargetedShards
argument_list|(
name|clusterState
argument_list|,
name|concreteIndices
argument_list|,
name|routing
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|ShardIterator
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|shards
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|IndexShardRoutingTable
name|shard
range|:
name|shards
control|)
block|{
name|ShardIterator
name|iterator
init|=
name|preferenceActiveShardIterator
argument_list|(
name|shard
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|getLocalNodeId
argument_list|()
argument_list|,
name|clusterState
operator|.
name|nodes
argument_list|()
argument_list|,
name|preference
argument_list|)
decl_stmt|;
if|if
condition|(
name|iterator
operator|!=
literal|null
condition|)
block|{
name|set
operator|.
name|add
argument_list|(
name|iterator
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|GroupShardsIterator
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|set
argument_list|)
argument_list|)
return|;
block|}
DECL|field|EMPTY_ROUTING
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|EMPTY_ROUTING
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
DECL|method|computeTargetedShards
specifier|private
name|Set
argument_list|<
name|IndexShardRoutingTable
argument_list|>
name|computeTargetedShards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|routing
parameter_list|)
block|{
name|routing
operator|=
name|routing
operator|==
literal|null
condition|?
name|EMPTY_ROUTING
else|:
name|routing
expr_stmt|;
comment|// just use an empty map
specifier|final
name|Set
argument_list|<
name|IndexShardRoutingTable
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|// we use set here and not list since we might get duplicates
for|for
control|(
name|String
name|index
range|:
name|concreteIndices
control|)
block|{
specifier|final
name|IndexRoutingTable
name|indexRouting
init|=
name|indexRoutingTable
argument_list|(
name|clusterState
argument_list|,
name|index
argument_list|)
decl_stmt|;
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
name|indexMetaData
argument_list|(
name|clusterState
argument_list|,
name|index
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|effectiveRouting
init|=
name|routing
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|effectiveRouting
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|r
range|:
name|effectiveRouting
control|)
block|{
name|int
name|shardId
init|=
name|generateShardId
argument_list|(
name|indexMetaData
argument_list|,
literal|null
argument_list|,
name|r
argument_list|)
decl_stmt|;
name|IndexShardRoutingTable
name|indexShard
init|=
name|indexRouting
operator|.
name|shard
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexShard
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ShardNotFoundException
argument_list|(
operator|new
name|ShardId
argument_list|(
name|indexRouting
operator|.
name|getIndex
argument_list|()
argument_list|,
name|shardId
argument_list|)
argument_list|)
throw|;
block|}
comment|// we might get duplicates, but that's ok, they will override one another
name|set
operator|.
name|add
argument_list|(
name|indexShard
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|IndexShardRoutingTable
name|indexShard
range|:
name|indexRouting
control|)
block|{
name|set
operator|.
name|add
argument_list|(
name|indexShard
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|set
return|;
block|}
DECL|method|preferenceActiveShardIterator
specifier|private
name|ShardIterator
name|preferenceActiveShardIterator
parameter_list|(
name|IndexShardRoutingTable
name|indexShard
parameter_list|,
name|String
name|localNodeId
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|,
annotation|@
name|Nullable
name|String
name|preference
parameter_list|)
block|{
if|if
condition|(
name|preference
operator|==
literal|null
operator|||
name|preference
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
index|[]
name|awarenessAttributes
init|=
name|awarenessAllocationDecider
operator|.
name|awarenessAttributes
argument_list|()
decl_stmt|;
if|if
condition|(
name|awarenessAttributes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|indexShard
operator|.
name|activeInitializingShardsRandomIt
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|indexShard
operator|.
name|preferAttributesActiveInitializingShardsIt
argument_list|(
name|awarenessAttributes
argument_list|,
name|nodes
argument_list|)
return|;
block|}
block|}
if|if
condition|(
name|preference
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'_'
condition|)
block|{
name|Preference
name|preferenceType
init|=
name|Preference
operator|.
name|parse
argument_list|(
name|preference
argument_list|)
decl_stmt|;
if|if
condition|(
name|preferenceType
operator|==
name|Preference
operator|.
name|SHARDS
condition|)
block|{
comment|// starts with _shards, so execute on specific ones
name|int
name|index
init|=
name|preference
operator|.
name|indexOf
argument_list|(
literal|';'
argument_list|)
decl_stmt|;
name|String
name|shards
decl_stmt|;
if|if
condition|(
name|index
operator|==
operator|-
literal|1
condition|)
block|{
name|shards
operator|=
name|preference
operator|.
name|substring
argument_list|(
name|Preference
operator|.
name|SHARDS
operator|.
name|type
argument_list|()
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shards
operator|=
name|preference
operator|.
name|substring
argument_list|(
name|Preference
operator|.
name|SHARDS
operator|.
name|type
argument_list|()
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|ids
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|shards
argument_list|)
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|id
range|:
name|ids
control|)
block|{
if|if
condition|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|id
argument_list|)
operator|==
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// no more preference
if|if
condition|(
name|index
operator|==
operator|-
literal|1
operator|||
name|index
operator|==
name|preference
operator|.
name|length
argument_list|()
operator|-
literal|1
condition|)
block|{
name|String
index|[]
name|awarenessAttributes
init|=
name|awarenessAllocationDecider
operator|.
name|awarenessAttributes
argument_list|()
decl_stmt|;
if|if
condition|(
name|awarenessAttributes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|indexShard
operator|.
name|activeInitializingShardsRandomIt
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|indexShard
operator|.
name|preferAttributesActiveInitializingShardsIt
argument_list|(
name|awarenessAttributes
argument_list|,
name|nodes
argument_list|)
return|;
block|}
block|}
else|else
block|{
comment|// update the preference and continue
name|preference
operator|=
name|preference
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
name|preferenceType
operator|=
name|Preference
operator|.
name|parse
argument_list|(
name|preference
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|preferenceType
condition|)
block|{
case|case
name|PREFER_NODES
case|:
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|nodesIds
init|=
name|Arrays
operator|.
name|stream
argument_list|(
name|preference
operator|.
name|substring
argument_list|(
name|Preference
operator|.
name|PREFER_NODES
operator|.
name|type
argument_list|()
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|indexShard
operator|.
name|preferNodeActiveInitializingShardsIt
argument_list|(
name|nodesIds
argument_list|)
return|;
case|case
name|LOCAL
case|:
return|return
name|indexShard
operator|.
name|preferNodeActiveInitializingShardsIt
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
name|localNodeId
argument_list|)
argument_list|)
return|;
case|case
name|PRIMARY
case|:
return|return
name|indexShard
operator|.
name|primaryActiveInitializingShardIt
argument_list|()
return|;
case|case
name|REPLICA
case|:
return|return
name|indexShard
operator|.
name|replicaActiveInitializingShardIt
argument_list|()
return|;
case|case
name|PRIMARY_FIRST
case|:
return|return
name|indexShard
operator|.
name|primaryFirstActiveInitializingShardsIt
argument_list|()
return|;
case|case
name|REPLICA_FIRST
case|:
return|return
name|indexShard
operator|.
name|replicaFirstActiveInitializingShardsIt
argument_list|()
return|;
case|case
name|ONLY_LOCAL
case|:
return|return
name|indexShard
operator|.
name|onlyNodeActiveInitializingShardsIt
argument_list|(
name|localNodeId
argument_list|)
return|;
case|case
name|ONLY_NODE
case|:
name|String
name|nodeId
init|=
name|preference
operator|.
name|substring
argument_list|(
name|Preference
operator|.
name|ONLY_NODE
operator|.
name|type
argument_list|()
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|ensureNodeIdExists
argument_list|(
name|nodes
argument_list|,
name|nodeId
argument_list|)
expr_stmt|;
return|return
name|indexShard
operator|.
name|onlyNodeActiveInitializingShardsIt
argument_list|(
name|nodeId
argument_list|)
return|;
case|case
name|ONLY_NODES
case|:
name|String
name|nodeAttributes
init|=
name|preference
operator|.
name|substring
argument_list|(
name|Preference
operator|.
name|ONLY_NODES
operator|.
name|type
argument_list|()
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
return|return
name|indexShard
operator|.
name|onlyNodeSelectorActiveInitializingShardsIt
argument_list|(
name|nodeAttributes
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|,
name|nodes
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown preference ["
operator|+
name|preferenceType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
comment|// if not, then use it as the index
name|String
index|[]
name|awarenessAttributes
init|=
name|awarenessAllocationDecider
operator|.
name|awarenessAttributes
argument_list|()
decl_stmt|;
if|if
condition|(
name|awarenessAttributes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|indexShard
operator|.
name|activeInitializingShardsIt
argument_list|(
name|Murmur3HashFunction
operator|.
name|hash
argument_list|(
name|preference
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|indexShard
operator|.
name|preferAttributesActiveInitializingShardsIt
argument_list|(
name|awarenessAttributes
argument_list|,
name|nodes
argument_list|,
name|Murmur3HashFunction
operator|.
name|hash
argument_list|(
name|preference
argument_list|)
argument_list|)
return|;
block|}
block|}
DECL|method|indexRoutingTable
specifier|protected
name|IndexRoutingTable
name|indexRoutingTable
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
name|index
parameter_list|)
block|{
name|IndexRoutingTable
name|indexRouting
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRouting
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexNotFoundException
argument_list|(
name|index
argument_list|)
throw|;
block|}
return|return
name|indexRouting
return|;
block|}
DECL|method|indexMetaData
specifier|protected
name|IndexMetaData
name|indexMetaData
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
name|index
parameter_list|)
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexMetaData
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexNotFoundException
argument_list|(
name|index
argument_list|)
throw|;
block|}
return|return
name|indexMetaData
return|;
block|}
DECL|method|shards
specifier|protected
name|IndexShardRoutingTable
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
name|index
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|routing
parameter_list|)
block|{
name|int
name|shardId
init|=
name|generateShardId
argument_list|(
name|indexMetaData
argument_list|(
name|clusterState
argument_list|,
name|index
argument_list|)
argument_list|,
name|id
argument_list|,
name|routing
argument_list|)
decl_stmt|;
return|return
name|clusterState
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|shardRoutingTable
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
return|;
block|}
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
name|index
parameter_list|,
name|String
name|id
parameter_list|,
annotation|@
name|Nullable
name|String
name|routing
parameter_list|)
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|indexMetaData
argument_list|(
name|clusterState
argument_list|,
name|index
argument_list|)
decl_stmt|;
return|return
operator|new
name|ShardId
argument_list|(
name|indexMetaData
operator|.
name|getIndex
argument_list|()
argument_list|,
name|generateShardId
argument_list|(
name|indexMetaData
argument_list|,
name|id
argument_list|,
name|routing
argument_list|)
argument_list|)
return|;
block|}
DECL|method|generateShardId
specifier|static
name|int
name|generateShardId
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|,
name|String
name|id
parameter_list|,
annotation|@
name|Nullable
name|String
name|routing
parameter_list|)
block|{
specifier|final
name|int
name|hash
decl_stmt|;
if|if
condition|(
name|routing
operator|==
literal|null
condition|)
block|{
name|hash
operator|=
name|Murmur3HashFunction
operator|.
name|hash
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hash
operator|=
name|Murmur3HashFunction
operator|.
name|hash
argument_list|(
name|routing
argument_list|)
expr_stmt|;
block|}
comment|// we don't use IMD#getNumberOfShards since the index might have been shrunk such that we need to use the size
comment|// of original index to hash documents
return|return
name|Math
operator|.
name|floorMod
argument_list|(
name|hash
argument_list|,
name|indexMetaData
operator|.
name|getRoutingNumShards
argument_list|()
argument_list|)
operator|/
name|indexMetaData
operator|.
name|getRoutingFactor
argument_list|()
return|;
block|}
DECL|method|ensureNodeIdExists
specifier|private
name|void
name|ensureNodeIdExists
parameter_list|(
name|DiscoveryNodes
name|nodes
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
if|if
condition|(
operator|!
name|nodes
operator|.
name|getDataNodes
argument_list|()
operator|.
name|keys
argument_list|()
operator|.
name|contains
argument_list|(
name|nodeId
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No data node with id["
operator|+
name|nodeId
operator|+
literal|"] found"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

