begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.azure.classic
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
operator|.
name|classic
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
name|classic
operator|.
name|management
operator|.
name|AzureComputeService
operator|.
name|Discovery
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
name|classic
operator|.
name|management
operator|.
name|AzureComputeService
operator|.
name|Management
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
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
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
name|Collection
import|;
end_import

begin_class
DECL|class|AbstractAzureComputeServiceTestCase
specifier|public
specifier|abstract
class|class
name|AbstractAzureComputeServiceTestCase
extends|extends
name|ESIntegTestCase
block|{
DECL|field|mockPlugin
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
name|mockPlugin
decl_stmt|;
DECL|method|AbstractAzureComputeServiceTestCase
specifier|public
name|AbstractAzureComputeServiceTestCase
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
name|mockPlugin
parameter_list|)
block|{
comment|// We want to inject the Azure API Mock
name|this
operator|.
name|mockPlugin
operator|=
name|mockPlugin
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
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.zen.hosts_provider"
argument_list|,
literal|"azure"
argument_list|)
decl_stmt|;
comment|// We add a fake subscription_id to start mock compute service
name|builder
operator|.
name|put
argument_list|(
name|Management
operator|.
name|SUBSCRIPTION_ID_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"fake"
argument_list|)
operator|.
name|put
argument_list|(
name|Discovery
operator|.
name|REFRESH_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"5s"
argument_list|)
operator|.
name|put
argument_list|(
name|Management
operator|.
name|KEYSTORE_PATH_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"dummy"
argument_list|)
operator|.
name|put
argument_list|(
name|Management
operator|.
name|KEYSTORE_PASSWORD_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"dummy"
argument_list|)
operator|.
name|put
argument_list|(
name|Management
operator|.
name|SERVICE_NAME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"dummy"
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|mockPlugin
argument_list|)
return|;
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
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

