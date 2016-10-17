begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.azure.classic.management
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
operator|.
name|management
package|;
end_package

begin_import
import|import
name|com
operator|.
name|microsoft
operator|.
name|windowsazure
operator|.
name|core
operator|.
name|utils
operator|.
name|KeyStoreType
import|;
end_import

begin_import
import|import
name|com
operator|.
name|microsoft
operator|.
name|windowsazure
operator|.
name|management
operator|.
name|compute
operator|.
name|models
operator|.
name|HostedServiceGetDetailedResponse
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
name|Setting
operator|.
name|Property
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
name|azure
operator|.
name|classic
operator|.
name|AzureUnicastHostsProvider
operator|.
name|Deployment
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|Function
import|;
end_import

begin_interface
DECL|interface|AzureComputeService
specifier|public
interface|interface
name|AzureComputeService
block|{
DECL|class|Management
specifier|final
class|class
name|Management
block|{
DECL|field|SUBSCRIPTION_ID_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|SUBSCRIPTION_ID_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.azure.management.subscription.id"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|)
decl_stmt|;
DECL|field|SERVICE_NAME_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|SERVICE_NAME_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.azure.management.cloud.service.name"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|// Keystore settings
DECL|field|KEYSTORE_PATH_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|KEYSTORE_PATH_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.azure.management.keystore.path"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|)
decl_stmt|;
DECL|field|KEYSTORE_PASSWORD_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|KEYSTORE_PASSWORD_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.azure.management.keystore.password"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|)
decl_stmt|;
DECL|field|KEYSTORE_TYPE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|KeyStoreType
argument_list|>
name|KEYSTORE_TYPE_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.azure.management.keystore.type"
argument_list|,
name|KeyStoreType
operator|.
name|pkcs12
operator|.
name|name
argument_list|()
argument_list|,
name|KeyStoreType
operator|::
name|fromString
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|)
decl_stmt|;
comment|// so that it can overridden for tests
DECL|field|ENDPOINT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|URI
argument_list|>
name|ENDPOINT_SETTING
init|=
operator|new
name|Setting
argument_list|<
name|URI
argument_list|>
argument_list|(
literal|"cloud.azure.management.endpoint"
argument_list|,
literal|"https://management.core.windows.net/"
argument_list|,
name|s
lambda|->
block|{
try|try
block|{
return|return
operator|new
name|URI
argument_list|(
name|s
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
block|}
DECL|class|Discovery
specifier|final
class|class
name|Discovery
block|{
DECL|field|REFRESH_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|REFRESH_SETTING
init|=
name|Setting
operator|.
name|positiveTimeSetting
argument_list|(
literal|"discovery.azure.refresh_interval"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|HOST_TYPE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|AzureUnicastHostsProvider
operator|.
name|HostType
argument_list|>
name|HOST_TYPE_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"discovery.azure.host.type"
argument_list|,
name|AzureUnicastHostsProvider
operator|.
name|HostType
operator|.
name|PRIVATE_IP
operator|.
name|name
argument_list|()
argument_list|,
name|AzureUnicastHostsProvider
operator|.
name|HostType
operator|::
name|fromString
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|ENDPOINT_NAME_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|ENDPOINT_NAME_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"discovery.azure.endpoint.name"
argument_list|,
literal|"elasticsearch"
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|DEPLOYMENT_NAME_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|DEPLOYMENT_NAME_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"discovery.azure.deployment.name"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|DEPLOYMENT_SLOT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Deployment
argument_list|>
name|DEPLOYMENT_SLOT_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"discovery.azure.deployment.slot"
argument_list|,
name|Deployment
operator|.
name|PRODUCTION
operator|.
name|name
argument_list|()
argument_list|,
name|Deployment
operator|::
name|fromString
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
block|}
DECL|method|getServiceDetails
name|HostedServiceGetDetailedResponse
name|getServiceDetails
parameter_list|()
function_decl|;
block|}
end_interface

end_unit
