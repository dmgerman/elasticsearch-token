begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.file
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|file
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
name|DiscoveryModule
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
name|ESTestCase
import|;
end_import

begin_class
DECL|class|FileBasedDiscoveryPluginTests
specifier|public
class|class
name|FileBasedDiscoveryPluginTests
extends|extends
name|ESTestCase
block|{
DECL|method|testHostsProviderBwc
specifier|public
name|void
name|testHostsProviderBwc
parameter_list|()
block|{
name|FileBasedDiscoveryPlugin
name|plugin
init|=
operator|new
name|FileBasedDiscoveryPlugin
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|Settings
name|additionalSettings
init|=
name|plugin
operator|.
name|additionalSettings
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"file"
argument_list|,
name|additionalSettings
operator|.
name|get
argument_list|(
name|DiscoveryModule
operator|.
name|DISCOVERY_HOSTS_PROVIDER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHostsProviderExplicit
specifier|public
name|void
name|testHostsProviderExplicit
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
name|DISCOVERY_HOSTS_PROVIDER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|FileBasedDiscoveryPlugin
name|plugin
init|=
operator|new
name|FileBasedDiscoveryPlugin
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|plugin
operator|.
name|additionalSettings
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
