begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
package|;
end_package

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
name|ModuleTestCase
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
name|discovery
operator|.
name|zen
operator|.
name|ElectMasterService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|ZenDiscovery
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
name|NoopDiscovery
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|DiscoveryModuleTests
specifier|public
class|class
name|DiscoveryModuleTests
extends|extends
name|ModuleTestCase
block|{
DECL|class|DummyMasterElectionService
specifier|public
specifier|static
class|class
name|DummyMasterElectionService
extends|extends
name|ElectMasterService
block|{
DECL|method|DummyMasterElectionService
specifier|public
name|DummyMasterElectionService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRegisterMasterElectionService
specifier|public
name|void
name|testRegisterMasterElectionService
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DiscoveryModule
operator|.
name|ZEN_MASTER_SERVICE_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"custom"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DiscoveryModule
name|module
init|=
operator|new
name|DiscoveryModule
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|module
operator|.
name|addElectMasterService
argument_list|(
literal|"custom"
argument_list|,
name|DummyMasterElectionService
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertBinding
argument_list|(
name|module
argument_list|,
name|ElectMasterService
operator|.
name|class
argument_list|,
name|DummyMasterElectionService
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertBinding
argument_list|(
name|module
argument_list|,
name|Discovery
operator|.
name|class
argument_list|,
name|ZenDiscovery
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|method|testLoadUnregisteredMasterElectionService
specifier|public
name|void
name|testLoadUnregisteredMasterElectionService
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DiscoveryModule
operator|.
name|ZEN_MASTER_SERVICE_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"foobar"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DiscoveryModule
name|module
init|=
operator|new
name|DiscoveryModule
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|module
operator|.
name|addElectMasterService
argument_list|(
literal|"custom"
argument_list|,
name|DummyMasterElectionService
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertBindingFailure
argument_list|(
name|module
argument_list|,
literal|"Unknown master service type [foobar]"
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegisterDefaults
specifier|public
name|void
name|testRegisterDefaults
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|EMPTY
decl_stmt|;
name|DiscoveryModule
name|module
init|=
operator|new
name|DiscoveryModule
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|assertBinding
argument_list|(
name|module
argument_list|,
name|Discovery
operator|.
name|class
argument_list|,
name|ZenDiscovery
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegisterDiscovery
specifier|public
name|void
name|testRegisterDiscovery
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DiscoveryModule
operator|.
name|DISCOVERY_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"custom"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DiscoveryModule
name|module
init|=
operator|new
name|DiscoveryModule
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|module
operator|.
name|addDiscoveryType
argument_list|(
literal|"custom"
argument_list|,
name|NoopDiscovery
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertBinding
argument_list|(
name|module
argument_list|,
name|Discovery
operator|.
name|class
argument_list|,
name|NoopDiscovery
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

