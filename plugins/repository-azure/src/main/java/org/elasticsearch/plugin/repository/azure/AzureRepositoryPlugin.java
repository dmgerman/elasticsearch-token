begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.repository.azure
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|repository
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
name|AzureRepositoryModule
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
name|common
operator|.
name|inject
operator|.
name|Module
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
name|common
operator|.
name|settings
operator|.
name|SettingsModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|snapshots
operator|.
name|blobstore
operator|.
name|BlobStoreIndexShardRepository
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
name|repositories
operator|.
name|RepositoriesModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|azure
operator|.
name|AzureRepository
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
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AzureRepositoryPlugin
specifier|public
class|class
name|AzureRepositoryPlugin
extends|extends
name|Plugin
block|{
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|logger
specifier|protected
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|AzureRepositoryPlugin
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|AzureRepositoryPlugin
specifier|public
name|AzureRepositoryPlugin
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
name|logger
operator|.
name|trace
argument_list|(
literal|"starting azure repository plugin..."
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"repository-azure"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"Azure Repository Plugin"
return|;
block|}
annotation|@
name|Override
DECL|method|nodeModules
specifier|public
name|Collection
argument_list|<
name|Module
argument_list|>
name|nodeModules
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
operator|(
name|Module
operator|)
operator|new
name|AzureRepositoryModule
argument_list|(
name|settings
argument_list|)
argument_list|)
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|RepositoriesModule
name|module
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"registering repository type [{}]"
argument_list|,
name|AzureRepository
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerRepository
argument_list|(
name|AzureRepository
operator|.
name|TYPE
argument_list|,
name|AzureRepository
operator|.
name|class
argument_list|,
name|BlobStoreIndexShardRepository
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|SettingsModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|registerSetting
argument_list|(
name|AzureStorageService
operator|.
name|Storage
operator|.
name|ACCOUNT_SETTING
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerSetting
argument_list|(
name|AzureStorageService
operator|.
name|Storage
operator|.
name|COMPRESS_SETTING
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerSetting
argument_list|(
name|AzureStorageService
operator|.
name|Storage
operator|.
name|CONTAINER_SETTING
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerSetting
argument_list|(
name|AzureStorageService
operator|.
name|Storage
operator|.
name|BASE_PATH_SETTING
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerSetting
argument_list|(
name|AzureStorageService
operator|.
name|Storage
operator|.
name|CHUNK_SIZE_SETTING
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerSetting
argument_list|(
name|AzureStorageService
operator|.
name|Storage
operator|.
name|LOCATION_MODE_SETTING
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

