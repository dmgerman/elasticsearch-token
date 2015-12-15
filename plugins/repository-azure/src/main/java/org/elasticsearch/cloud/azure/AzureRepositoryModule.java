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
name|cloud
operator|.
name|azure
operator|.
name|storage
operator|.
name|AzureStorageSettingsFilter
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

begin_comment
comment|/**  * Azure Module  *  *<ul>  *<li>If needed this module will bind azure repository service by default  * to AzureStorageServiceImpl.</li>  *</ul>  *  * @see org.elasticsearch.cloud.azure.storage.AzureStorageServiceImpl  */
end_comment

begin_class
DECL|class|AzureRepositoryModule
specifier|public
class|class
name|AzureRepositoryModule
extends|extends
name|AbstractModule
block|{
DECL|field|logger
specifier|protected
specifier|final
name|ESLogger
name|logger
decl_stmt|;
comment|// pkg private so it is settable by tests
DECL|field|storageServiceImpl
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|AzureStorageService
argument_list|>
name|storageServiceImpl
init|=
name|AzureStorageServiceImpl
operator|.
name|class
decl_stmt|;
annotation|@
name|Inject
DECL|method|AzureRepositoryModule
specifier|public
name|AzureRepositoryModule
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
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
name|bind
argument_list|(
name|AzureStorageSettingsFilter
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
comment|// If we have settings for azure repository, let's start the azure storage service
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
name|storageServiceImpl
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

