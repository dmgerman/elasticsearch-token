begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.close
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
name|close
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
name|DestructiveOperations
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
name|MetaDataIndexStateService
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
name|ClusterSettings
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
name|Setting
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
name|Setting
operator|.
name|Property
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
name|tasks
operator|.
name|Task
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
comment|/**  * Close index action  */
end_comment

begin_class
DECL|class|TransportCloseIndexAction
specifier|public
class|class
name|TransportCloseIndexAction
extends|extends
name|TransportMasterNodeAction
argument_list|<
name|CloseIndexRequest
argument_list|,
name|CloseIndexResponse
argument_list|>
block|{
DECL|field|indexStateService
specifier|private
specifier|final
name|MetaDataIndexStateService
name|indexStateService
decl_stmt|;
DECL|field|destructiveOperations
specifier|private
specifier|final
name|DestructiveOperations
name|destructiveOperations
decl_stmt|;
DECL|field|closeIndexEnabled
specifier|private
specifier|volatile
name|boolean
name|closeIndexEnabled
decl_stmt|;
DECL|field|CLUSTER_INDICES_CLOSE_ENABLE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|CLUSTER_INDICES_CLOSE_ENABLE_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"cluster.indices.close.enable"
argument_list|,
literal|true
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportCloseIndexAction
specifier|public
name|TransportCloseIndexAction
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
name|MetaDataIndexStateService
name|indexStateService
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|DestructiveOperations
name|destructiveOperations
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|CloseIndexAction
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
name|CloseIndexRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexStateService
operator|=
name|indexStateService
expr_stmt|;
name|this
operator|.
name|destructiveOperations
operator|=
name|destructiveOperations
expr_stmt|;
name|this
operator|.
name|closeIndexEnabled
operator|=
name|CLUSTER_INDICES_CLOSE_ENABLE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|CLUSTER_INDICES_CLOSE_ENABLE_SETTING
argument_list|,
name|this
operator|::
name|setCloseIndexEnabled
argument_list|)
expr_stmt|;
block|}
DECL|method|setCloseIndexEnabled
specifier|private
name|void
name|setCloseIndexEnabled
parameter_list|(
name|boolean
name|closeIndexEnabled
parameter_list|)
block|{
name|this
operator|.
name|closeIndexEnabled
operator|=
name|closeIndexEnabled
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
comment|// no need to use a thread pool, we go async right away
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
name|CloseIndexResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|CloseIndexResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|Task
name|task
parameter_list|,
name|CloseIndexRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|CloseIndexResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|destructiveOperations
operator|.
name|failDestructive
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|closeIndexEnabled
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"closing indices is disabled - set ["
operator|+
name|CLUSTER_INDICES_CLOSE_ENABLE_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|": true] to enable it. NOTE: closed indices still consume a significant amount of diskspace"
argument_list|)
throw|;
block|}
name|super
operator|.
name|doExecute
argument_list|(
name|task
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|CloseIndexRequest
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
DECL|method|masterOperation
specifier|protected
name|void
name|masterOperation
parameter_list|(
specifier|final
name|CloseIndexRequest
name|request
parameter_list|,
specifier|final
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|CloseIndexResponse
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
name|CloseIndexClusterStateUpdateRequest
name|updateRequest
init|=
operator|new
name|CloseIndexClusterStateUpdateRequest
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
name|indices
argument_list|(
name|concreteIndices
argument_list|)
decl_stmt|;
name|indexStateService
operator|.
name|closeIndex
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
name|CloseIndexResponse
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
literal|"failed to close indices [{}]"
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

