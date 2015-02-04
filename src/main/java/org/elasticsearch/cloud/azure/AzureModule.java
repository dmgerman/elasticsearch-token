begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.azure
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
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
name|storage
operator|.
name|AzureStorageService
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
name|storage
operator|.
name|AzureStorageServiceImpl
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
name|inject
operator|.
name|AbstractModule
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
name|logging
operator|.
name|ESLogger
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
name|azure
operator|.
name|AzureDiscovery
import|;
end_import

begin_comment
comment|/**  * Azure Module  *  *<ul>  *<li>If needed this module will bind azure discovery service by default  * to AzureComputeServiceImpl.</li>  *<li>If needed this module will bind azure repository service by default  * to AzureStorageServiceImpl.</li>  *</ul>  *  * @see org.elasticsearch.cloud.azure.AzureComputeServiceImpl  * @see org.elasticsearch.cloud.azure.storage.AzureStorageServiceImpl  */
end_comment

begin_class
DECL|class|AzureModule
specifier|public
class|class
name|AzureModule
extends|extends
name|AbstractModule
block|{
DECL|field|logger
specifier|protected
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|settings
specifier|private
name|Settings
name|settings
decl_stmt|;
annotation|@
name|Inject
DECL|method|AzureModule
specifier|public
name|AzureModule
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
name|this
operator|.
name|logger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|settings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"starting azure services"
argument_list|)
expr_stmt|;
comment|// If we have set discovery to azure, let's start the azure compute service
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
name|logger
operator|.
name|debug
argument_list|(
literal|"starting azure discovery service"
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|AzureComputeService
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|settings
operator|.
name|getAsClass
argument_list|(
literal|"cloud.azure.api.impl"
argument_list|,
name|AzureComputeServiceImpl
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
comment|// If we have settings for azure repository, let's start the azure storage service
if|if
condition|(
name|isSnapshotReady
argument_list|(
name|settings
argument_list|,
name|logger
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"starting azure repository service"
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|AzureStorageService
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|settings
operator|.
name|getAsClass
argument_list|(
literal|"repositories.azure.api.impl"
argument_list|,
name|AzureStorageServiceImpl
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Check if discovery is meant to start      * @return true if we can start discovery features      */
DECL|method|isCloudReady
specifier|public
specifier|static
name|boolean
name|isCloudReady
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
operator|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"cloud.enabled"
argument_list|,
literal|true
argument_list|)
operator|)
return|;
block|}
comment|/**      * Check if discovery is meant to start      * @return true if we can start discovery features      */
DECL|method|isDiscoveryReady
specifier|public
specifier|static
name|boolean
name|isDiscoveryReady
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
comment|// Cloud services are disabled
if|if
condition|(
operator|!
name|isCloudReady
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"cloud settings are disabled"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// User set discovery.type: azure
if|if
condition|(
operator|!
name|AzureDiscovery
operator|.
name|AZURE
operator|.
name|equalsIgnoreCase
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"discovery.type"
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
name|AzureDiscovery
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
name|isPropertyMissing
argument_list|(
name|settings
argument_list|,
literal|"cloud.azure."
operator|+
name|AzureComputeService
operator|.
name|Fields
operator|.
name|SUBSCRIPTION_ID
argument_list|,
name|logger
argument_list|)
operator|||
name|isPropertyMissing
argument_list|(
name|settings
argument_list|,
literal|"cloud.azure."
operator|+
name|AzureComputeService
operator|.
name|Fields
operator|.
name|SERVICE_NAME
argument_list|,
name|logger
argument_list|)
operator|||
name|isPropertyMissing
argument_list|(
name|settings
argument_list|,
literal|"cloud.azure."
operator|+
name|AzureComputeService
operator|.
name|Fields
operator|.
name|KEYSTORE
argument_list|,
name|logger
argument_list|)
operator|||
name|isPropertyMissing
argument_list|(
name|settings
argument_list|,
literal|"cloud.azure."
operator|+
name|AzureComputeService
operator|.
name|Fields
operator|.
name|PASSWORD
argument_list|,
name|logger
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"all required properties for azure discovery are set!"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/**      * Check if we have repository azure settings available      * @return true if we can use snapshot and restore      */
DECL|method|isSnapshotReady
specifier|public
specifier|static
name|boolean
name|isSnapshotReady
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
comment|// Cloud services are disabled
if|if
condition|(
operator|!
name|isCloudReady
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"cloud settings are disabled"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|(
name|isPropertyMissing
argument_list|(
name|settings
argument_list|,
literal|"cloud.azure.storage."
operator|+
name|AzureStorageService
operator|.
name|Fields
operator|.
name|ACCOUNT
argument_list|,
literal|null
argument_list|)
operator|||
name|isPropertyMissing
argument_list|(
name|settings
argument_list|,
literal|"cloud.azure.storage."
operator|+
name|AzureStorageService
operator|.
name|Fields
operator|.
name|KEY
argument_list|,
literal|null
argument_list|)
operator|)
operator|&&
operator|(
name|isPropertyMissing
argument_list|(
name|settings
argument_list|,
literal|"cloud.azure."
operator|+
name|AzureStorageService
operator|.
name|Fields
operator|.
name|ACCOUNT_DEPRECATED
argument_list|,
literal|null
argument_list|)
operator|||
name|isPropertyMissing
argument_list|(
name|settings
argument_list|,
literal|"cloud.azure."
operator|+
name|AzureStorageService
operator|.
name|Fields
operator|.
name|KEY_DEPRECATED
argument_list|,
literal|null
argument_list|)
operator|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"azure repository is not set [using cloud.azure.storage.{}] and [cloud.azure.storage.{}] properties"
argument_list|,
name|AzureStorageService
operator|.
name|Fields
operator|.
name|ACCOUNT
argument_list|,
name|AzureStorageService
operator|.
name|Fields
operator|.
name|KEY
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"all required properties for azure repository are set!"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/**      * Check if we are using any deprecated settings      */
DECL|method|checkDeprecatedSettings
specifier|public
specifier|static
name|void
name|checkDeprecatedSettings
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|oldParameter
parameter_list|,
name|String
name|newParameter
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isPropertyMissing
argument_list|(
name|settings
argument_list|,
name|oldParameter
argument_list|,
literal|null
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"using deprecated [{}]. Please change it to [{}] property."
argument_list|,
name|oldParameter
argument_list|,
name|newParameter
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|isPropertyMissing
specifier|public
specifier|static
name|boolean
name|isPropertyMissing
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|name
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
if|if
condition|(
operator|!
name|Strings
operator|.
name|hasText
argument_list|(
name|settings
operator|.
name|get
argument_list|(
name|name
argument_list|)
argument_list|)
condition|)
block|{
if|if
condition|(
name|logger
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"{} is not set or is incorrect."
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

