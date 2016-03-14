begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.settings.put
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
name|settings
operator|.
name|put
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
name|master
operator|.
name|TransportMasterNodeAction
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
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|MetaDataUpdateSettingsService
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportUpdateSettingsAction
specifier|public
class|class
name|TransportUpdateSettingsAction
extends|extends
name|TransportMasterNodeAction
argument_list|<
name|UpdateSettingsRequest
argument_list|,
name|UpdateSettingsResponse
argument_list|>
block|{
DECL|field|updateSettingsService
specifier|private
specifier|final
name|MetaDataUpdateSettingsService
name|updateSettingsService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportUpdateSettingsAction
specifier|public
name|TransportUpdateSettingsAction
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
name|MetaDataUpdateSettingsService
name|updateSettingsService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|UpdateSettingsAction
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
name|indexNameExpressionResolver
argument_list|,
name|UpdateSettingsRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|updateSettingsService
operator|=
name|updateSettingsService
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
comment|// we go async right away....
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
name|UpdateSettingsRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
comment|// allow for dedicated changes to the metadata blocks, so we don't block those to allow to "re-enable" it
name|ClusterBlockException
name|globalBlock
init|=
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|globalBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalBlock
operator|!=
literal|null
condition|)
block|{
return|return
name|globalBlock
return|;
block|}
if|if
condition|(
name|request
operator|.
name|settings
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|IndexMetaData
operator|.
name|INDEX_BLOCKS_METADATA_SETTING
operator|.
name|exists
argument_list|(
name|request
operator|.
name|settings
argument_list|()
argument_list|)
operator|||
name|IndexMetaData
operator|.
name|INDEX_READ_ONLY_SETTING
operator|.
name|exists
argument_list|(
name|request
operator|.
name|settings
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
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
name|indexNameExpressionResolver
operator|.
name|concreteIndexNames
argument_list|(
name|state
argument_list|,
name|request
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|UpdateSettingsResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|UpdateSettingsResponse
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
name|UpdateSettingsRequest
name|request
parameter_list|,
specifier|final
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|UpdateSettingsResponse
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|Index
index|[]
name|concreteIndices
init|=
name|indexNameExpressionResolver
operator|.
name|concreteIndices
argument_list|(
name|state
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|UpdateSettingsClusterStateUpdateRequest
name|clusterStateUpdateRequest
init|=
operator|new
name|UpdateSettingsClusterStateUpdateRequest
argument_list|()
operator|.
name|indices
argument_list|(
name|concreteIndices
argument_list|)
operator|.
name|settings
argument_list|(
name|request
operator|.
name|settings
argument_list|()
argument_list|)
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
decl_stmt|;
name|updateSettingsService
operator|.
name|updateSettings
argument_list|(
name|clusterStateUpdateRequest
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
name|UpdateSettingsResponse
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
literal|"failed to update settings on indices [{}]"
argument_list|,
name|t
argument_list|,
operator|(
name|Object
operator|)
name|concreteIndices
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

