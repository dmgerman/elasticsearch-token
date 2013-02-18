begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.health
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|health
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
name|master
operator|.
name|MasterNodeOperationRequestBuilder
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
name|ClusterAdminClient
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
name|internal
operator|.
name|InternalClusterAdminClient
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
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ClusterHealthRequestBuilder
specifier|public
class|class
name|ClusterHealthRequestBuilder
extends|extends
name|MasterNodeOperationRequestBuilder
argument_list|<
name|ClusterHealthRequest
argument_list|,
name|ClusterHealthResponse
argument_list|,
name|ClusterHealthRequestBuilder
argument_list|>
block|{
DECL|method|ClusterHealthRequestBuilder
specifier|public
name|ClusterHealthRequestBuilder
parameter_list|(
name|ClusterAdminClient
name|clusterClient
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalClusterAdminClient
operator|)
name|clusterClient
argument_list|,
operator|new
name|ClusterHealthRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|setIndices
specifier|public
name|ClusterHealthRequestBuilder
name|setIndices
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|request
operator|.
name|setIndices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setTimeout
specifier|public
name|ClusterHealthRequestBuilder
name|setTimeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|setTimeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setTimeout
specifier|public
name|ClusterHealthRequestBuilder
name|setTimeout
parameter_list|(
name|String
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|setTimeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setWaitForStatus
specifier|public
name|ClusterHealthRequestBuilder
name|setWaitForStatus
parameter_list|(
name|ClusterHealthStatus
name|waitForStatus
parameter_list|)
block|{
name|request
operator|.
name|setWaitForStatus
argument_list|(
name|waitForStatus
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setWaitForGreenStatus
specifier|public
name|ClusterHealthRequestBuilder
name|setWaitForGreenStatus
parameter_list|()
block|{
name|request
operator|.
name|setWaitForGreenStatus
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setWaitForYellowStatus
specifier|public
name|ClusterHealthRequestBuilder
name|setWaitForYellowStatus
parameter_list|()
block|{
name|request
operator|.
name|setWaitForYellowStatus
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setWaitForRelocatingShards
specifier|public
name|ClusterHealthRequestBuilder
name|setWaitForRelocatingShards
parameter_list|(
name|int
name|waitForRelocatingShards
parameter_list|)
block|{
name|request
operator|.
name|setWaitForRelocatingShards
argument_list|(
name|waitForRelocatingShards
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setWaitForActiveShards
specifier|public
name|ClusterHealthRequestBuilder
name|setWaitForActiveShards
parameter_list|(
name|int
name|waitForActiveShards
parameter_list|)
block|{
name|request
operator|.
name|setWaitForActiveShards
argument_list|(
name|waitForActiveShards
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Waits for N number of nodes. Use "12" for exact mapping, ">12" and "<12" for range.      */
DECL|method|setWaitForNodes
specifier|public
name|ClusterHealthRequestBuilder
name|setWaitForNodes
parameter_list|(
name|String
name|waitForNodes
parameter_list|)
block|{
name|request
operator|.
name|setWaitForNodes
argument_list|(
name|waitForNodes
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|ActionListener
argument_list|<
name|ClusterHealthResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|(
operator|(
name|ClusterAdminClient
operator|)
name|client
operator|)
operator|.
name|health
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

