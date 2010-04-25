begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.create
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
name|create
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
name|action
operator|.
name|TransportActions
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
name|metadata
operator|.
name|MetaDataService
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

begin_comment
comment|/**  * Create index action.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportCreateIndexAction
specifier|public
class|class
name|TransportCreateIndexAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|CreateIndexRequest
argument_list|,
name|CreateIndexResponse
argument_list|>
block|{
DECL|field|metaDataService
specifier|private
specifier|final
name|MetaDataService
name|metaDataService
decl_stmt|;
DECL|method|TransportCreateIndexAction
annotation|@
name|Inject
specifier|public
name|TransportCreateIndexAction
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
name|MetaDataService
name|metaDataService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaDataService
operator|=
name|metaDataService
expr_stmt|;
block|}
DECL|method|transportAction
annotation|@
name|Override
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|TransportActions
operator|.
name|Admin
operator|.
name|Indices
operator|.
name|CREATE
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|CreateIndexRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|CreateIndexRequest
argument_list|()
return|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|CreateIndexResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|CreateIndexResponse
argument_list|()
return|;
block|}
DECL|method|masterOperation
annotation|@
name|Override
specifier|protected
name|CreateIndexResponse
name|masterOperation
parameter_list|(
name|CreateIndexRequest
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
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
name|MetaDataService
operator|.
name|CreateIndexResult
name|createIndexResult
init|=
name|metaDataService
operator|.
name|createIndex
argument_list|(
name|cause
argument_list|,
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|settings
argument_list|()
argument_list|,
name|request
operator|.
name|mappings
argument_list|()
argument_list|,
name|request
operator|.
name|timeout
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|CreateIndexResponse
argument_list|(
name|createIndexResult
operator|.
name|acknowledged
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

