begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.ec2
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|ec2
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
name|settings
operator|.
name|ClusterUpdateSettingsResponse
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
name|aws
operator|.
name|AbstractAwsTestCase
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
name|plugin
operator|.
name|cloud
operator|.
name|aws
operator|.
name|CloudAwsPlugin
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
operator|.
name|ClusterScope
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
operator|.
name|Scope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|is
import|;
end_import

begin_comment
comment|/**  * Just an empty Node Start test to check eveything if fine when  * starting.  * This test requires AWS to run.  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|,
name|numClientNodes
operator|=
literal|0
argument_list|,
name|transportClientRatio
operator|=
literal|0.0
argument_list|)
DECL|class|Ec2DiscoveryUpdateSettingsITest
specifier|public
class|class
name|Ec2DiscoveryUpdateSettingsITest
extends|extends
name|AbstractAwsTestCase
block|{
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
name|pluginList
argument_list|(
name|CloudAwsPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|testMinimumMasterNodesStart
specifier|public
name|void
name|testMinimumMasterNodesStart
parameter_list|()
block|{
name|Settings
name|nodeSettings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cloud.enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"ec2"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|nodeSettings
argument_list|)
expr_stmt|;
comment|// We try to update minimum_master_nodes now
name|ClusterUpdateSettingsResponse
name|response
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setPersistentSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"discovery.zen.minimum_master_nodes"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|setTransientSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"discovery.zen.minimum_master_nodes"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|Integer
name|min
init|=
name|response
operator|.
name|getPersistentSettings
argument_list|()
operator|.
name|getAsInt
argument_list|(
literal|"discovery.zen.minimum_master_nodes"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|min
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

