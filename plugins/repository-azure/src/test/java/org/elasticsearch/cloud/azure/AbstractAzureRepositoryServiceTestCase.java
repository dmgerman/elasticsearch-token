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
name|com
operator|.
name|microsoft
operator|.
name|azure
operator|.
name|storage
operator|.
name|StorageException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|microsoft
operator|.
name|azure
operator|.
name|storage
operator|.
name|LocationMode
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
name|AzureStorageService
operator|.
name|Storage
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
name|AzureStorageServiceMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|repository
operator|.
name|azure
operator|.
name|AzureRepositoryPlugin
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
name|RepositoryMissingException
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
name|store
operator|.
name|MockFSDirectoryService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|Collection
import|;
end_import

begin_class
DECL|class|AbstractAzureRepositoryServiceTestCase
specifier|public
specifier|abstract
class|class
name|AbstractAzureRepositoryServiceTestCase
extends|extends
name|AbstractAzureTestCase
block|{
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"mock-storage-service"
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
literal|"plugs in a mock storage service for testing"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|AzureRepositoryModule
name|azureRepositoryModule
parameter_list|)
block|{
name|AzureRepositoryModule
operator|.
name|storageServiceImpl
operator|=
name|AzureStorageServiceMock
operator|.
name|class
expr_stmt|;
block|}
block|}
DECL|field|basePath
specifier|protected
name|String
name|basePath
decl_stmt|;
DECL|method|AbstractAzureRepositoryServiceTestCase
specifier|public
name|AbstractAzureRepositoryServiceTestCase
parameter_list|(
name|String
name|basePath
parameter_list|)
block|{
name|this
operator|.
name|basePath
operator|=
name|basePath
expr_stmt|;
block|}
comment|/**      * Deletes repositories, supports wildcard notation.      */
DECL|method|wipeRepositories
specifier|public
specifier|static
name|void
name|wipeRepositories
parameter_list|(
name|String
modifier|...
name|repositories
parameter_list|)
block|{
comment|// if nothing is provided, delete all
if|if
condition|(
name|repositories
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|repositories
operator|=
operator|new
name|String
index|[]
block|{
literal|"*"
block|}
expr_stmt|;
block|}
for|for
control|(
name|String
name|repository
range|:
name|repositories
control|)
block|{
try|try
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareDeleteRepository
argument_list|(
name|repository
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryMissingException
name|ex
parameter_list|)
block|{
comment|// ignore
block|}
block|}
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
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Storage
operator|.
name|CONTAINER
argument_list|,
literal|"snapshots"
argument_list|)
decl_stmt|;
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
name|pluginList
argument_list|(
name|AzureRepositoryPlugin
operator|.
name|class
argument_list|,
name|TestPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|indexSettings
specifier|public
name|Settings
name|indexSettings
parameter_list|()
block|{
comment|// During restore we frequently restore index to exactly the same state it was before, that might cause the same
comment|// checksum file to be written twice during restore operation
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|MockFSDirectoryService
operator|.
name|RANDOM_PREVENT_DOUBLE_WRITE
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|MockFSDirectoryService
operator|.
name|RANDOM_NO_DELETE_OPEN_FILE
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Before
annotation|@
name|After
DECL|method|wipe
specifier|public
specifier|final
name|void
name|wipe
parameter_list|()
throws|throws
name|StorageException
throws|,
name|URISyntaxException
block|{
name|wipeRepositories
argument_list|()
expr_stmt|;
name|cleanRepositoryFiles
argument_list|(
name|basePath
argument_list|)
expr_stmt|;
block|}
comment|/**      * Purge the test container      */
DECL|method|cleanRepositoryFiles
specifier|public
name|void
name|cleanRepositoryFiles
parameter_list|(
name|String
name|path
parameter_list|)
throws|throws
name|StorageException
throws|,
name|URISyntaxException
block|{
name|String
name|container
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Settings
operator|.
name|class
argument_list|)
operator|.
name|get
argument_list|(
literal|"repositories.azure.container"
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> remove blobs in container [{}]"
argument_list|,
name|container
argument_list|)
expr_stmt|;
name|AzureStorageService
name|client
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|AzureStorageService
operator|.
name|class
argument_list|)
decl_stmt|;
name|client
operator|.
name|deleteFiles
argument_list|(
literal|null
argument_list|,
name|LocationMode
operator|.
name|PRIMARY_ONLY
argument_list|,
name|container
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

