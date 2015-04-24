begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.alias
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
name|alias
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

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
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|IndicesAliasesRequest
operator|.
name|AliasActions
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
name|TransportMasterNodeOperationAction
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
name|metadata
operator|.
name|AliasAction
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
name|MetaDataIndexAliasesService
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
name|rest
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|delete
operator|.
name|AliasesMissingException
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
name|*
import|;
end_import

begin_comment
comment|/**  * Add/remove aliases action  */
end_comment

begin_class
DECL|class|TransportIndicesAliasesAction
specifier|public
class|class
name|TransportIndicesAliasesAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|IndicesAliasesRequest
argument_list|,
name|IndicesAliasesResponse
argument_list|>
block|{
DECL|field|indexAliasesService
specifier|private
specifier|final
name|MetaDataIndexAliasesService
name|indexAliasesService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportIndicesAliasesAction
specifier|public
name|TransportIndicesAliasesAction
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
name|MetaDataIndexAliasesService
name|indexAliasesService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|IndicesAliasesAction
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
name|IndicesAliasesRequest
operator|.
name|class
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexAliasesService
operator|=
name|indexAliasesService
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
comment|// we go async right away...
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
DECL|method|newResponse
specifier|protected
name|IndicesAliasesResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|IndicesAliasesResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|IndicesAliasesRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|indices
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|AliasActions
name|aliasAction
range|:
name|request
operator|.
name|aliasActions
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|index
range|:
name|aliasAction
operator|.
name|indices
argument_list|()
control|)
block|{
name|indices
operator|.
name|add
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
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
name|METADATA_WRITE
argument_list|,
name|indices
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|indices
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
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
name|IndicesAliasesRequest
name|request
parameter_list|,
specifier|final
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|IndicesAliasesResponse
argument_list|>
name|listener
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
comment|//Expand the indices names
name|List
argument_list|<
name|AliasActions
argument_list|>
name|actions
init|=
name|request
operator|.
name|aliasActions
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|AliasAction
argument_list|>
name|finalActions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|hasOnlyDeletesButNoneCanBeDone
init|=
literal|true
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|AliasActions
name|action
range|:
name|actions
control|)
block|{
comment|//expand indices
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
name|action
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
comment|//collect the aliases
name|Collections
operator|.
name|addAll
argument_list|(
name|aliases
argument_list|,
name|action
operator|.
name|aliases
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|concreteIndices
control|)
block|{
for|for
control|(
name|String
name|alias
range|:
name|action
operator|.
name|concreteAliases
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
argument_list|,
name|index
argument_list|)
control|)
block|{
name|AliasAction
name|finalAction
init|=
operator|new
name|AliasAction
argument_list|(
name|action
operator|.
name|aliasAction
argument_list|()
argument_list|)
decl_stmt|;
name|finalAction
operator|.
name|index
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|finalAction
operator|.
name|alias
argument_list|(
name|alias
argument_list|)
expr_stmt|;
name|finalActions
operator|.
name|add
argument_list|(
name|finalAction
argument_list|)
expr_stmt|;
comment|//if there is only delete requests, none will be added if the types do not map to any existing type
name|hasOnlyDeletesButNoneCanBeDone
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|hasOnlyDeletesButNoneCanBeDone
operator|&&
name|actions
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|AliasesMissingException
argument_list|(
name|aliases
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|aliases
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
throw|;
block|}
name|request
operator|.
name|aliasActions
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|IndicesAliasesClusterStateUpdateRequest
name|updateRequest
init|=
operator|new
name|IndicesAliasesClusterStateUpdateRequest
argument_list|()
operator|.
name|ackTimeout
argument_list|(
name|request
operator|.
name|timeout
argument_list|()
argument_list|)
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
operator|.
name|actions
argument_list|(
name|finalActions
operator|.
name|toArray
argument_list|(
operator|new
name|AliasAction
index|[
name|finalActions
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|indexAliasesService
operator|.
name|indicesAliases
argument_list|(
name|updateRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|ClusterStateUpdateResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ClusterStateUpdateResponse
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|IndicesAliasesResponse
argument_list|(
name|response
operator|.
name|isAcknowledged
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to perform aliases"
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

