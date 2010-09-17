begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indexer.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indexer
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
name|get
operator|.
name|GetResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|Maps
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
name|AbstractLifecycleComponent
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
name|compress
operator|.
name|CompressedString
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
name|common
operator|.
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indexer
operator|.
name|IndexerName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indexer
operator|.
name|cluster
operator|.
name|IndexerClusterService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indexer
operator|.
name|cluster
operator|.
name|IndexerClusterState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indexer
operator|.
name|cluster
operator|.
name|IndexerClusterStateUpdateTask
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indexer
operator|.
name|cluster
operator|.
name|IndexerNodeHelper
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|IndexersRouter
specifier|public
class|class
name|IndexersRouter
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|IndexersRouter
argument_list|>
implements|implements
name|ClusterStateListener
block|{
DECL|field|indexerIndexName
specifier|private
specifier|final
name|String
name|indexerIndexName
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|indexerClusterService
specifier|private
specifier|final
name|IndexerClusterService
name|indexerClusterService
decl_stmt|;
DECL|method|IndexersRouter
annotation|@
name|Inject
specifier|public
name|IndexersRouter
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndexerClusterService
name|indexerClusterService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexerIndexName
operator|=
name|settings
operator|.
name|get
argument_list|(
literal|"indexer.index_name"
argument_list|,
literal|"indexer"
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexerClusterService
operator|=
name|indexerClusterService
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|clusterService
operator|.
name|add
argument_list|(
name|this
argument_list|)
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
block|{     }
DECL|method|doStop
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
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
name|event
operator|.
name|localNodeMaster
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|event
operator|.
name|nodesChanged
argument_list|()
operator|||
name|event
operator|.
name|metaDataChanged
argument_list|()
operator|||
name|event
operator|.
name|blocksChanged
argument_list|()
condition|)
block|{
name|indexerClusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"reroute_indexers_node_changed"
argument_list|,
operator|new
name|IndexerClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|IndexerClusterState
name|execute
parameter_list|(
name|IndexerClusterState
name|currentState
parameter_list|)
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
name|indexerIndexName
argument_list|)
condition|)
block|{
comment|// if there are routings, publish an empty one (so it will be deleted on nodes), otherwise, return the same state
if|if
condition|(
operator|!
name|currentState
operator|.
name|routing
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|IndexerClusterState
operator|.
name|builder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|routing
argument_list|(
name|IndexersRouting
operator|.
name|builder
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
return|return
name|currentState
return|;
block|}
name|IndexersRouting
operator|.
name|Builder
name|routingBuilder
init|=
name|IndexersRouting
operator|.
name|builder
argument_list|()
operator|.
name|routing
argument_list|(
name|currentState
operator|.
name|routing
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|dirty
init|=
literal|false
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|indexerIndexName
argument_list|)
decl_stmt|;
comment|// go over and create new indexer routing (with no node) for new types (indexers names)
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|CompressedString
argument_list|>
name|entry
range|:
name|indexMetaData
operator|.
name|mappings
argument_list|()
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
comment|// mapping type is the name of the indexer
if|if
condition|(
operator|!
name|currentState
operator|.
name|routing
argument_list|()
operator|.
name|hasIndexerByName
argument_list|(
name|mappingType
argument_list|)
condition|)
block|{
comment|// no indexer, we need to add it to the routing with no node allocation
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
name|indexerIndexName
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|GetResponse
name|getResponse
init|=
name|client
operator|.
name|prepareGet
argument_list|(
name|indexerIndexName
argument_list|,
name|mappingType
argument_list|,
literal|"_meta"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|getResponse
operator|.
name|exists
argument_list|()
condition|)
block|{
name|String
name|indexerType
init|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|getResponse
operator|.
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexerType
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"no indexer type provided for [{}], ignoring..."
argument_list|,
name|indexerIndexName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|routingBuilder
operator|.
name|put
argument_list|(
operator|new
name|IndexerRouting
argument_list|(
operator|new
name|IndexerName
argument_list|(
name|indexerType
argument_list|,
name|mappingType
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|dirty
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|ClusterBlockException
name|e
parameter_list|)
block|{
comment|// ignore, we will get it next time
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
literal|"failed to get/parse _meta for [{}]"
argument_list|,
name|e
argument_list|,
name|mappingType
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// now, remove routings that were deleted
for|for
control|(
name|IndexerRouting
name|routing
range|:
name|currentState
operator|.
name|routing
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|indexMetaData
operator|.
name|mappings
argument_list|()
operator|.
name|containsKey
argument_list|(
name|routing
operator|.
name|indexerName
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
name|routingBuilder
operator|.
name|remove
argument_list|(
name|routing
argument_list|)
expr_stmt|;
name|dirty
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|// build a list from nodes to indexers
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|List
argument_list|<
name|IndexerRouting
argument_list|>
argument_list|>
name|nodesToIndexers
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
control|)
block|{
if|if
condition|(
name|IndexerNodeHelper
operator|.
name|isIndexerNode
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|nodesToIndexers
operator|.
name|put
argument_list|(
name|node
argument_list|,
name|Lists
operator|.
expr|<
name|IndexerRouting
operator|>
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|IndexerRouting
argument_list|>
name|unassigned
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexerRouting
name|routing
range|:
name|routingBuilder
operator|.
name|build
argument_list|()
control|)
block|{
if|if
condition|(
name|routing
operator|.
name|node
argument_list|()
operator|==
literal|null
condition|)
block|{
name|unassigned
operator|.
name|add
argument_list|(
name|routing
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|IndexerRouting
argument_list|>
name|l
init|=
name|nodesToIndexers
operator|.
name|get
argument_list|(
name|routing
operator|.
name|node
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|l
operator|==
literal|null
condition|)
block|{
name|l
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
name|nodesToIndexers
operator|.
name|put
argument_list|(
name|routing
operator|.
name|node
argument_list|()
argument_list|,
name|l
argument_list|)
expr_stmt|;
block|}
name|l
operator|.
name|add
argument_list|(
name|routing
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Iterator
argument_list|<
name|IndexerRouting
argument_list|>
name|it
init|=
name|unassigned
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|IndexerRouting
name|routing
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|DiscoveryNode
name|smallest
init|=
literal|null
decl_stmt|;
name|int
name|smallestSize
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|DiscoveryNode
argument_list|,
name|List
argument_list|<
name|IndexerRouting
argument_list|>
argument_list|>
name|entry
range|:
name|nodesToIndexers
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|IndexerNodeHelper
operator|.
name|isIndexerNode
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|routing
operator|.
name|indexerName
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
operator|<
name|smallestSize
condition|)
block|{
name|smallestSize
operator|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|smallest
operator|=
name|entry
operator|.
name|getKey
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|smallest
operator|!=
literal|null
condition|)
block|{
name|dirty
operator|=
literal|true
expr_stmt|;
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
name|routing
operator|.
name|node
argument_list|(
name|smallest
argument_list|)
expr_stmt|;
name|nodesToIndexers
operator|.
name|get
argument_list|(
name|smallest
argument_list|)
operator|.
name|add
argument_list|(
name|routing
argument_list|)
expr_stmt|;
block|}
block|}
comment|// add relocation logic...
if|if
condition|(
name|dirty
condition|)
block|{
return|return
name|IndexerClusterState
operator|.
name|builder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|routing
argument_list|(
name|routingBuilder
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
return|return
name|currentState
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

