begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.gcs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|gcs
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|services
operator|.
name|storage
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
name|cluster
operator|.
name|metadata
operator|.
name|RepositoryMetaData
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
name|unit
operator|.
name|ByteSizeUnit
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
name|ByteSizeValue
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
name|env
operator|.
name|Environment
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
name|blobstore
operator|.
name|ESBlobStoreRepositoryIntegTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
import|;
end_import

begin_class
DECL|class|GoogleCloudStorageBlobStoreRepositoryTests
specifier|public
class|class
name|GoogleCloudStorageBlobStoreRepositoryTests
extends|extends
name|ESBlobStoreRepositoryIntegTestCase
block|{
DECL|field|BUCKET
specifier|private
specifier|static
specifier|final
name|String
name|BUCKET
init|=
literal|"gcs-repository-test"
decl_stmt|;
comment|// Static storage client shared among all nodes in order to act like a remote repository service:
comment|// all nodes must see the same content
DECL|field|storage
specifier|private
specifier|static
specifier|final
name|AtomicReference
argument_list|<
name|Storage
argument_list|>
name|storage
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
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
name|MockGoogleCloudStoragePlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createTestRepository
specifier|protected
name|void
name|createTestRepository
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
name|name
argument_list|)
operator|.
name|setType
argument_list|(
name|GoogleCloudStorageRepository
operator|.
name|TYPE
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"bucket"
argument_list|,
name|BUCKET
argument_list|)
operator|.
name|put
argument_list|(
literal|"base_path"
argument_list|,
name|GoogleCloudStorageBlobStoreRepositoryTests
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"service_account"
argument_list|,
literal|"_default_"
argument_list|)
operator|.
name|put
argument_list|(
literal|"compress"
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"chunk_size"
argument_list|,
name|randomIntBetween
argument_list|(
literal|100
argument_list|,
literal|1000
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|BeforeClass
DECL|method|setUpStorage
specifier|public
specifier|static
name|void
name|setUpStorage
parameter_list|()
block|{
name|storage
operator|.
name|set
argument_list|(
name|MockHttpTransport
operator|.
name|newStorage
argument_list|(
name|BUCKET
argument_list|,
name|GoogleCloudStorageBlobStoreRepositoryTests
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|MockGoogleCloudStoragePlugin
specifier|public
specifier|static
class|class
name|MockGoogleCloudStoragePlugin
extends|extends
name|GoogleCloudStoragePlugin
block|{
DECL|method|MockGoogleCloudStoragePlugin
specifier|public
name|MockGoogleCloudStoragePlugin
parameter_list|()
block|{
name|super
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createStorageService
specifier|protected
name|GoogleCloudStorageService
name|createStorageService
parameter_list|(
name|Environment
name|environment
parameter_list|)
block|{
return|return
operator|new
name|MockGoogleCloudStorageService
argument_list|()
return|;
block|}
block|}
DECL|class|MockGoogleCloudStorageService
specifier|public
specifier|static
class|class
name|MockGoogleCloudStorageService
implements|implements
name|GoogleCloudStorageService
block|{
annotation|@
name|Override
DECL|method|createClient
specifier|public
name|Storage
name|createClient
parameter_list|(
name|String
name|accountName
parameter_list|,
name|String
name|application
parameter_list|,
name|TimeValue
name|connectTimeout
parameter_list|,
name|TimeValue
name|readTimeout
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|storage
operator|.
name|get
argument_list|()
return|;
block|}
block|}
DECL|method|testChunkSize
specifier|public
name|void
name|testChunkSize
parameter_list|()
block|{
comment|// default chunk size
name|RepositoryMetaData
name|repositoryMetaData
init|=
operator|new
name|RepositoryMetaData
argument_list|(
literal|"repo"
argument_list|,
name|GoogleCloudStorageRepository
operator|.
name|TYPE
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|chunkSize
init|=
name|GoogleCloudStorageRepository
operator|.
name|getSetting
argument_list|(
name|GoogleCloudStorageRepository
operator|.
name|CHUNK_SIZE
argument_list|,
name|repositoryMetaData
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|GoogleCloudStorageRepository
operator|.
name|MAX_CHUNK_SIZE
argument_list|,
name|chunkSize
argument_list|)
expr_stmt|;
comment|// chunk size in settings
name|int
name|size
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|repositoryMetaData
operator|=
operator|new
name|RepositoryMetaData
argument_list|(
literal|"repo"
argument_list|,
name|GoogleCloudStorageRepository
operator|.
name|TYPE
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"chunk_size"
argument_list|,
name|size
operator|+
literal|"mb"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|chunkSize
operator|=
name|GoogleCloudStorageRepository
operator|.
name|getSetting
argument_list|(
name|GoogleCloudStorageRepository
operator|.
name|CHUNK_SIZE
argument_list|,
name|repositoryMetaData
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|size
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|,
name|chunkSize
argument_list|)
expr_stmt|;
comment|// zero bytes is not allowed
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|RepositoryMetaData
name|repoMetaData
init|=
operator|new
name|RepositoryMetaData
argument_list|(
literal|"repo"
argument_list|,
name|GoogleCloudStorageRepository
operator|.
name|TYPE
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"chunk_size"
argument_list|,
literal|"0"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|GoogleCloudStorageRepository
operator|.
name|getSetting
argument_list|(
name|GoogleCloudStorageRepository
operator|.
name|CHUNK_SIZE
argument_list|,
name|repoMetaData
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Failed to parse value [0] for setting [chunk_size] must be>= 1b"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// negative bytes not allowed
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|RepositoryMetaData
name|repoMetaData
init|=
operator|new
name|RepositoryMetaData
argument_list|(
literal|"repo"
argument_list|,
name|GoogleCloudStorageRepository
operator|.
name|TYPE
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"chunk_size"
argument_list|,
literal|"-1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|GoogleCloudStorageRepository
operator|.
name|getSetting
argument_list|(
name|GoogleCloudStorageRepository
operator|.
name|CHUNK_SIZE
argument_list|,
name|repoMetaData
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Failed to parse value [-1] for setting [chunk_size] must be>= 1b"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// greater than max chunk size not allowed
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|RepositoryMetaData
name|repoMetaData
init|=
operator|new
name|RepositoryMetaData
argument_list|(
literal|"repo"
argument_list|,
name|GoogleCloudStorageRepository
operator|.
name|TYPE
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"chunk_size"
argument_list|,
literal|"101mb"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|GoogleCloudStorageRepository
operator|.
name|getSetting
argument_list|(
name|GoogleCloudStorageRepository
operator|.
name|CHUNK_SIZE
argument_list|,
name|repoMetaData
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Failed to parse value [101mb] for setting [chunk_size] must be<= 100mb"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

