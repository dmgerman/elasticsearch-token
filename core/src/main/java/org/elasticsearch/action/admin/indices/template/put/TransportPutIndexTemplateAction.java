begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.template.put
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
name|template
operator|.
name|put
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|MetaDataIndexTemplateService
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
name|IndexScopedSettings
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

begin_comment
comment|/**  * Put index template action.  */
end_comment

begin_class
DECL|class|TransportPutIndexTemplateAction
specifier|public
class|class
name|TransportPutIndexTemplateAction
extends|extends
name|TransportMasterNodeAction
argument_list|<
name|PutIndexTemplateRequest
argument_list|,
name|PutIndexTemplateResponse
argument_list|>
block|{
DECL|field|indexTemplateService
specifier|private
specifier|final
name|MetaDataIndexTemplateService
name|indexTemplateService
decl_stmt|;
DECL|field|indexScopedSettings
specifier|private
specifier|final
name|IndexScopedSettings
name|indexScopedSettings
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportPutIndexTemplateAction
specifier|public
name|TransportPutIndexTemplateAction
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
name|MetaDataIndexTemplateService
name|indexTemplateService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|IndexScopedSettings
name|indexScopedSettings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|PutIndexTemplateAction
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
name|PutIndexTemplateRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexTemplateService
operator|=
name|indexTemplateService
expr_stmt|;
name|this
operator|.
name|indexScopedSettings
operator|=
name|indexScopedSettings
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
name|PutIndexTemplateResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|PutIndexTemplateResponse
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
name|PutIndexTemplateRequest
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
name|indexBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|,
literal|""
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
name|PutIndexTemplateRequest
name|request
parameter_list|,
specifier|final
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|PutIndexTemplateResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|String
name|cause
init|=
name|request
operator|.
name|cause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|cause
operator|=
literal|"api"
expr_stmt|;
block|}
specifier|final
name|Settings
operator|.
name|Builder
name|templateSettingsBuilder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|templateSettingsBuilder
operator|.
name|put
argument_list|(
name|request
operator|.
name|settings
argument_list|()
argument_list|)
operator|.
name|normalizePrefix
argument_list|(
name|IndexMetaData
operator|.
name|INDEX_SETTING_PREFIX
argument_list|)
expr_stmt|;
name|indexScopedSettings
operator|.
name|validate
argument_list|(
name|templateSettingsBuilder
argument_list|)
expr_stmt|;
name|indexTemplateService
operator|.
name|putTemplate
argument_list|(
operator|new
name|MetaDataIndexTemplateService
operator|.
name|PutRequest
argument_list|(
name|cause
argument_list|,
name|request
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|patterns
argument_list|(
name|request
operator|.
name|patterns
argument_list|()
argument_list|)
operator|.
name|order
argument_list|(
name|request
operator|.
name|order
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|templateSettingsBuilder
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|mappings
argument_list|(
name|request
operator|.
name|mappings
argument_list|()
argument_list|)
operator|.
name|aliases
argument_list|(
name|request
operator|.
name|aliases
argument_list|()
argument_list|)
operator|.
name|customs
argument_list|(
name|request
operator|.
name|customs
argument_list|()
argument_list|)
operator|.
name|create
argument_list|(
name|request
operator|.
name|create
argument_list|()
argument_list|)
operator|.
name|masterTimeout
argument_list|(
name|request
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
operator|.
name|version
argument_list|(
name|request
operator|.
name|version
argument_list|()
argument_list|)
argument_list|,
operator|new
name|MetaDataIndexTemplateService
operator|.
name|PutListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|MetaDataIndexTemplateService
operator|.
name|PutResponse
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|PutIndexTemplateResponse
argument_list|(
name|response
operator|.
name|acknowledged
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
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"failed to put template [{}]"
argument_list|,
name|request
operator|.
name|name
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|e
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

