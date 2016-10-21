begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.discovery.azure.classic
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|discovery
operator|.
name|azure
operator|.
name|classic
package|;
end_package

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
name|Collections
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
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
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|AzureComputeServiceImpl
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
name|Strings
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
name|logging
operator|.
name|DeprecationLogger
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
name|logging
operator|.
name|Loggers
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
name|network
operator|.
name|NetworkService
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
name|Setting
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
name|DiscoveryModule
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
name|azure
operator|.
name|classic
operator|.
name|AzureUnicastHostsProvider
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
name|UnicastHostsProvider
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
name|plugins
operator|.
name|DiscoveryPlugin
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
name|transport
operator|.
name|TransportService
import|;
end_import

begin_class
DECL|class|AzureDiscoveryPlugin
specifier|public
class|class
name|AzureDiscoveryPlugin
extends|extends
name|Plugin
implements|implements
name|DiscoveryPlugin
block|{
DECL|field|AZURE
specifier|public
specifier|static
specifier|final
name|String
name|AZURE
init|=
literal|"azure"
decl_stmt|;
DECL|field|settings
specifier|protected
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|logger
specifier|protected
specifier|final
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|AzureDiscoveryPlugin
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|AzureDiscoveryPlugin
specifier|public
name|AzureDiscoveryPlugin
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
name|DeprecationLogger
name|deprecationLogger
init|=
operator|new
name|DeprecationLogger
argument_list|(
name|logger
argument_list|)
decl_stmt|;
name|deprecationLogger
operator|.
name|deprecated
argument_list|(
literal|"azure classic discovery plugin is deprecated. Use azure arm discovery plugin instead"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"starting azure classic discovery plugin..."
argument_list|)
expr_stmt|;
block|}
comment|// overrideable for tests
DECL|method|createComputeService
specifier|protected
name|AzureComputeService
name|createComputeService
parameter_list|()
block|{
return|return
operator|new
name|AzureComputeServiceImpl
argument_list|(
name|settings
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getZenHostsProviders
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|UnicastHostsProvider
argument_list|>
argument_list|>
name|getZenHostsProviders
parameter_list|(
name|TransportService
name|transportService
parameter_list|,
name|NetworkService
name|networkService
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|singletonMap
argument_list|(
name|AZURE
argument_list|,
parameter_list|()
lambda|->
operator|new
name|AzureUnicastHostsProvider
argument_list|(
name|settings
argument_list|,
name|createComputeService
argument_list|()
argument_list|,
name|transportService
argument_list|,
name|networkService
argument_list|)
argument_list|)
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|DiscoveryModule
name|discoveryModule
parameter_list|)
block|{
if|if
condition|(
name|isDiscoveryReady
argument_list|(
name|settings
argument_list|,
name|logger
argument_list|)
condition|)
block|{
name|discoveryModule
operator|.
name|addDiscoveryType
argument_list|(
name|AZURE
argument_list|,
name|ZenDiscovery
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getSettings
specifier|public
name|List
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|getSettings
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|AzureComputeService
operator|.
name|Discovery
operator|.
name|REFRESH_SETTING
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|KEYSTORE_PASSWORD_SETTING
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|KEYSTORE_PATH_SETTING
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|KEYSTORE_TYPE_SETTING
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|SUBSCRIPTION_ID_SETTING
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|SERVICE_NAME_SETTING
argument_list|,
name|AzureComputeService
operator|.
name|Discovery
operator|.
name|HOST_TYPE_SETTING
argument_list|,
name|AzureComputeService
operator|.
name|Discovery
operator|.
name|DEPLOYMENT_NAME_SETTING
argument_list|,
name|AzureComputeService
operator|.
name|Discovery
operator|.
name|DEPLOYMENT_SLOT_SETTING
argument_list|,
name|AzureComputeService
operator|.
name|Discovery
operator|.
name|ENDPOINT_NAME_SETTING
argument_list|)
return|;
block|}
comment|/**      * Check if discovery is meant to start      * @return true if we can start discovery features      */
DECL|method|isDiscoveryReady
specifier|private
specifier|static
name|boolean
name|isDiscoveryReady
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Logger
name|logger
parameter_list|)
block|{
comment|// User set discovery.type: azure
if|if
condition|(
operator|!
name|AzureDiscoveryPlugin
operator|.
name|AZURE
operator|.
name|equalsIgnoreCase
argument_list|(
name|DiscoveryModule
operator|.
name|DISCOVERY_TYPE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"discovery.type not set to {}"
argument_list|,
name|AzureDiscoveryPlugin
operator|.
name|AZURE
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|isDefined
argument_list|(
name|settings
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|SUBSCRIPTION_ID_SETTING
argument_list|)
operator|&&
name|isDefined
argument_list|(
name|settings
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|SERVICE_NAME_SETTING
argument_list|)
operator|&&
name|isDefined
argument_list|(
name|settings
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|KEYSTORE_PATH_SETTING
argument_list|)
operator|&&
name|isDefined
argument_list|(
name|settings
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|KEYSTORE_PASSWORD_SETTING
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"All required properties for Azure discovery are set!"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"One or more Azure discovery settings are missing. "
operator|+
literal|"Check elasticsearch.yml file. Should have [{}], [{}], [{}] and [{}]."
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|SUBSCRIPTION_ID_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|SERVICE_NAME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|KEYSTORE_PATH_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|AzureComputeService
operator|.
name|Management
operator|.
name|KEYSTORE_PASSWORD_SETTING
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
DECL|method|isDefined
specifier|private
specifier|static
name|boolean
name|isDefined
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Setting
argument_list|<
name|String
argument_list|>
name|property
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
return|return
operator|(
name|property
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
operator|&&
name|Strings
operator|.
name|hasText
argument_list|(
name|property
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
operator|)
return|;
block|}
block|}
end_class

end_unit

