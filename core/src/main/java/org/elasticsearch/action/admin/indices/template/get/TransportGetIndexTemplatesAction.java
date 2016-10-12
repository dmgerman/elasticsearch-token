begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.template.get
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
name|get
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectObjectCursor
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
name|TransportMasterNodeReadAction
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
name|IndexTemplateMetaData
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
name|regex
operator|.
name|Regex
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
name|List
import|;
end_import

begin_class
DECL|class|TransportGetIndexTemplatesAction
specifier|public
class|class
name|TransportGetIndexTemplatesAction
extends|extends
name|TransportMasterNodeReadAction
argument_list|<
name|GetIndexTemplatesRequest
argument_list|,
name|GetIndexTemplatesResponse
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|TransportGetIndexTemplatesAction
specifier|public
name|TransportGetIndexTemplatesAction
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
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|GetIndexTemplatesAction
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
name|GetIndexTemplatesRequest
operator|::
operator|new
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
name|GetIndexTemplatesRequest
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
name|globalBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_READ
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|GetIndexTemplatesResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|GetIndexTemplatesResponse
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
name|GetIndexTemplatesRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|,
name|ActionListener
argument_list|<
name|GetIndexTemplatesResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|List
argument_list|<
name|IndexTemplateMetaData
argument_list|>
name|results
decl_stmt|;
comment|// If we did not ask for a specific name, then we return all templates
if|if
condition|(
name|request
operator|.
name|names
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|results
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
name|IndexTemplateMetaData
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|results
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|String
name|name
range|:
name|request
operator|.
name|names
argument_list|()
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|isSimpleMatchPattern
argument_list|(
name|name
argument_list|)
condition|)
block|{
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|IndexTemplateMetaData
argument_list|>
name|entry
range|:
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|name
argument_list|,
name|entry
operator|.
name|key
argument_list|)
condition|)
block|{
name|results
operator|.
name|add
argument_list|(
name|entry
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|results
operator|.
name|add
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|GetIndexTemplatesResponse
argument_list|(
name|results
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

