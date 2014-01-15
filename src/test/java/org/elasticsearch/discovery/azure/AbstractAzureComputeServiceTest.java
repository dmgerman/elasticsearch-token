begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.azure
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|azure
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodesInfoResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
operator|.
name|AbstractAzureTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
operator|.
name|AzureComputeService
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
name|ImmutableSettings
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

begin_class
DECL|class|AbstractAzureComputeServiceTest
specifier|public
specifier|abstract
class|class
name|AbstractAzureComputeServiceTest
extends|extends
name|AbstractAzureTest
block|{
DECL|field|mock
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|AzureComputeService
argument_list|>
name|mock
decl_stmt|;
DECL|method|AbstractAzureComputeServiceTest
specifier|public
name|AbstractAzureComputeServiceTest
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|AzureComputeService
argument_list|>
name|mock
parameter_list|)
block|{
comment|// We want to inject the Azure API Mock
name|this
operator|.
name|mock
operator|=
name|mock
expr_stmt|;
block|}
DECL|method|checkNumberOfNodes
specifier|protected
name|void
name|checkNumberOfNodes
parameter_list|(
name|int
name|expected
parameter_list|)
block|{
name|NodesInfoResponse
name|nodeInfos
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesInfo
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|nodeInfos
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|nodeInfos
operator|.
name|getNodes
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|nodeInfos
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
name|ImmutableSettings
operator|.
name|Builder
name|builder
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cloud.azure.api.impl"
argument_list|,
name|mock
argument_list|)
comment|// We add a fake subscription_id to start mock compute service
operator|.
name|put
argument_list|(
literal|"cloud.azure.subscription_id"
argument_list|,
literal|"fake"
argument_list|)
operator|.
name|put
argument_list|(
literal|"cloud.azure.refresh_interval"
argument_list|,
literal|"5s"
argument_list|)
decl_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

