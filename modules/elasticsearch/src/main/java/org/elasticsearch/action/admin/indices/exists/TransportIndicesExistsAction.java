begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.exists
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
name|exists
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

begin_comment
comment|/**  * Indices exists action.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportIndicesExistsAction
specifier|public
class|class
name|TransportIndicesExistsAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|IndicesExistsRequest
argument_list|,
name|IndicesExistsResponse
argument_list|>
block|{
DECL|method|TransportIndicesExistsAction
annotation|@
name|Inject
specifier|public
name|TransportIndicesExistsAction
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
block|}
DECL|method|executor
annotation|@
name|Override
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
name|CACHED
return|;
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
name|EXISTS
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|IndicesExistsRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|IndicesExistsRequest
argument_list|()
return|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|IndicesExistsResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|IndicesExistsResponse
argument_list|()
return|;
block|}
DECL|method|doExecute
annotation|@
name|Override
specifier|protected
name|void
name|doExecute
parameter_list|(
name|IndicesExistsRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|IndicesExistsResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|request
operator|.
name|indices
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|concreteIndices
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|doExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|checkBlock
annotation|@
name|Override
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|IndicesExistsRequest
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
name|METADATA
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|)
return|;
block|}
DECL|method|masterOperation
annotation|@
name|Override
specifier|protected
name|IndicesExistsResponse
name|masterOperation
parameter_list|(
name|IndicesExistsRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|boolean
name|exists
init|=
literal|true
decl_stmt|;
for|for
control|(
name|String
name|index
range|:
name|request
operator|.
name|indices
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|hasConcreteIndex
argument_list|(
name|index
argument_list|)
condition|)
block|{
name|exists
operator|=
literal|false
expr_stmt|;
block|}
block|}
return|return
operator|new
name|IndicesExistsResponse
argument_list|(
name|exists
argument_list|)
return|;
block|}
block|}
end_class

end_unit

