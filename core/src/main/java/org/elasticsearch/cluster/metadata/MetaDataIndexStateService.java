begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
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
name|Version
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
name|admin
operator|.
name|indices
operator|.
name|close
operator|.
name|CloseIndexClusterStateUpdateRequest
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
name|admin
operator|.
name|indices
operator|.
name|open
operator|.
name|OpenIndexClusterStateUpdateRequest
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
name|AckedClusterStateUpdateTask
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
name|ack
operator|.
name|ClusterStateUpdateResponse
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
name|ClusterBlock
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
name|block
operator|.
name|ClusterBlocks
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
name|allocation
operator|.
name|AllocationService
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
name|Priority
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
name|Index
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
name|rest
operator|.
name|RestStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|RestoreService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|SnapshotsService
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
name|Set
import|;
end_import

begin_comment
comment|/**  * Service responsible for submitting open/close index requests  */
end_comment

begin_class
DECL|class|MetaDataIndexStateService
specifier|public
class|class
name|MetaDataIndexStateService
extends|extends
name|AbstractComponent
block|{
DECL|field|INDEX_CLOSED_BLOCK
specifier|public
specifier|static
specifier|final
name|ClusterBlock
name|INDEX_CLOSED_BLOCK
init|=
operator|new
name|ClusterBlock
argument_list|(
literal|4
argument_list|,
literal|"index closed"
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|RestStatus
operator|.
name|FORBIDDEN
argument_list|,
name|ClusterBlockLevel
operator|.
name|READ_WRITE
argument_list|)
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|allocationService
specifier|private
specifier|final
name|AllocationService
name|allocationService
decl_stmt|;
DECL|field|metaDataIndexUpgradeService
specifier|private
specifier|final
name|MetaDataIndexUpgradeService
name|metaDataIndexUpgradeService
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
annotation|@
name|Inject
DECL|method|MetaDataIndexStateService
specifier|public
name|MetaDataIndexStateService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|AllocationService
name|allocationService
parameter_list|,
name|MetaDataIndexUpgradeService
name|metaDataIndexUpgradeService
parameter_list|,
name|IndicesService
name|indicesService
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
name|allocationService
operator|=
name|allocationService
expr_stmt|;
name|this
operator|.
name|metaDataIndexUpgradeService
operator|=
name|metaDataIndexUpgradeService
expr_stmt|;
block|}
DECL|method|closeIndex
specifier|public
name|void
name|closeIndex
parameter_list|(
specifier|final
name|CloseIndexClusterStateUpdateRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|ClusterStateUpdateResponse
argument_list|>
name|listener
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|indices
argument_list|()
operator|==
literal|null
operator|||
name|request
operator|.
name|indices
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Index name is required"
argument_list|)
throw|;
block|}
specifier|final
name|String
name|indicesAsString
init|=
name|Arrays
operator|.
name|toString
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"close-indices "
operator|+
name|indicesAsString
argument_list|,
operator|new
name|AckedClusterStateUpdateTask
argument_list|<
name|ClusterStateUpdateResponse
argument_list|>
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|ClusterStateUpdateResponse
name|newResponse
parameter_list|(
name|boolean
name|acknowledged
parameter_list|)
block|{
return|return
operator|new
name|ClusterStateUpdateResponse
argument_list|(
name|acknowledged
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
name|Set
argument_list|<
name|IndexMetaData
argument_list|>
name|indicesToClose
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Index
name|index
range|:
name|request
operator|.
name|indices
argument_list|()
control|)
block|{
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|getIndexSafe
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexMetaData
operator|.
name|getState
argument_list|()
operator|!=
name|IndexMetaData
operator|.
name|State
operator|.
name|CLOSE
condition|)
block|{
name|indicesToClose
operator|.
name|add
argument_list|(
name|indexMetaData
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|indicesToClose
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|currentState
return|;
block|}
comment|// Check if index closing conflicts with any running restores
name|RestoreService
operator|.
name|checkIndexClosing
argument_list|(
name|currentState
argument_list|,
name|indicesToClose
argument_list|)
expr_stmt|;
comment|// Check if index closing conflicts with any running snapshots
name|SnapshotsService
operator|.
name|checkIndexClosing
argument_list|(
name|currentState
argument_list|,
name|indicesToClose
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"closing indices [{}]"
argument_list|,
name|indicesAsString
argument_list|)
expr_stmt|;
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterBlocks
operator|.
name|Builder
name|blocksBuilder
init|=
name|ClusterBlocks
operator|.
name|builder
argument_list|()
operator|.
name|blocks
argument_list|(
name|currentState
operator|.
name|blocks
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|IndexMetaData
name|openIndexMetadata
range|:
name|indicesToClose
control|)
block|{
specifier|final
name|String
name|indexName
init|=
name|openIndexMetadata
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|mdBuilder
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|openIndexMetadata
argument_list|)
operator|.
name|state
argument_list|(
name|IndexMetaData
operator|.
name|State
operator|.
name|CLOSE
argument_list|)
argument_list|)
expr_stmt|;
name|blocksBuilder
operator|.
name|addIndexBlock
argument_list|(
name|indexName
argument_list|,
name|INDEX_CLOSED_BLOCK
argument_list|)
expr_stmt|;
block|}
name|ClusterState
name|updatedState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|mdBuilder
argument_list|)
operator|.
name|blocks
argument_list|(
name|blocksBuilder
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
operator|.
name|Builder
name|rtBuilder
init|=
name|RoutingTable
operator|.
name|builder
argument_list|(
name|currentState
operator|.
name|routingTable
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|IndexMetaData
name|index
range|:
name|indicesToClose
control|)
block|{
name|rtBuilder
operator|.
name|remove
argument_list|(
name|index
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//no explicit wait for other nodes needed as we use AckedClusterStateUpdateTask
return|return
name|allocationService
operator|.
name|reroute
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
name|updatedState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|rtBuilder
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|"indices closed ["
operator|+
name|indicesAsString
operator|+
literal|"]"
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|openIndex
specifier|public
name|void
name|openIndex
parameter_list|(
specifier|final
name|OpenIndexClusterStateUpdateRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|ClusterStateUpdateResponse
argument_list|>
name|listener
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|indices
argument_list|()
operator|==
literal|null
operator|||
name|request
operator|.
name|indices
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Index name is required"
argument_list|)
throw|;
block|}
specifier|final
name|String
name|indicesAsString
init|=
name|Arrays
operator|.
name|toString
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"open-indices "
operator|+
name|indicesAsString
argument_list|,
operator|new
name|AckedClusterStateUpdateTask
argument_list|<
name|ClusterStateUpdateResponse
argument_list|>
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|ClusterStateUpdateResponse
name|newResponse
parameter_list|(
name|boolean
name|acknowledged
parameter_list|)
block|{
return|return
operator|new
name|ClusterStateUpdateResponse
argument_list|(
name|acknowledged
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
name|List
argument_list|<
name|IndexMetaData
argument_list|>
name|indicesToOpen
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Index
name|index
range|:
name|request
operator|.
name|indices
argument_list|()
control|)
block|{
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|getIndexSafe
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexMetaData
operator|.
name|getState
argument_list|()
operator|!=
name|IndexMetaData
operator|.
name|State
operator|.
name|OPEN
condition|)
block|{
name|indicesToOpen
operator|.
name|add
argument_list|(
name|indexMetaData
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|indicesToOpen
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|currentState
return|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"opening indices [{}]"
argument_list|,
name|indicesAsString
argument_list|)
expr_stmt|;
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterBlocks
operator|.
name|Builder
name|blocksBuilder
init|=
name|ClusterBlocks
operator|.
name|builder
argument_list|()
operator|.
name|blocks
argument_list|(
name|currentState
operator|.
name|blocks
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Version
name|minIndexCompatibilityVersion
init|=
name|currentState
operator|.
name|getNodes
argument_list|()
operator|.
name|getMaxNodeVersion
argument_list|()
operator|.
name|minimumIndexCompatibilityVersion
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexMetaData
name|closedMetaData
range|:
name|indicesToOpen
control|)
block|{
specifier|final
name|String
name|indexName
init|=
name|closedMetaData
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|closedMetaData
argument_list|)
operator|.
name|state
argument_list|(
name|IndexMetaData
operator|.
name|State
operator|.
name|OPEN
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// The index might be closed because we couldn't import it due to old incompatible version
comment|// We need to check that this index can be upgraded to the current version
name|indexMetaData
operator|=
name|metaDataIndexUpgradeService
operator|.
name|upgradeIndexMetaData
argument_list|(
name|indexMetaData
argument_list|,
name|minIndexCompatibilityVersion
argument_list|)
expr_stmt|;
try|try
block|{
name|indicesService
operator|.
name|verifyIndexMetadata
argument_list|(
name|indexMetaData
argument_list|,
name|indexMetaData
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Failed to verify index "
operator|+
name|indexMetaData
operator|.
name|getIndex
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|mdBuilder
operator|.
name|put
argument_list|(
name|indexMetaData
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|blocksBuilder
operator|.
name|removeIndexBlock
argument_list|(
name|indexName
argument_list|,
name|INDEX_CLOSED_BLOCK
argument_list|)
expr_stmt|;
block|}
name|ClusterState
name|updatedState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|mdBuilder
argument_list|)
operator|.
name|blocks
argument_list|(
name|blocksBuilder
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingTable
operator|.
name|Builder
name|rtBuilder
init|=
name|RoutingTable
operator|.
name|builder
argument_list|(
name|updatedState
operator|.
name|routingTable
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|IndexMetaData
name|index
range|:
name|indicesToOpen
control|)
block|{
name|rtBuilder
operator|.
name|addAsFromCloseToOpen
argument_list|(
name|updatedState
operator|.
name|metaData
argument_list|()
operator|.
name|getIndexSafe
argument_list|(
name|index
operator|.
name|getIndex
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//no explicit wait for other nodes needed as we use AckedClusterStateUpdateTask
return|return
name|allocationService
operator|.
name|reroute
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
name|updatedState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|rtBuilder
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|"indices opened ["
operator|+
name|indicesAsString
operator|+
literal|"]"
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

