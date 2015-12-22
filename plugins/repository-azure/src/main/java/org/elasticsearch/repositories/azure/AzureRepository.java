begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.azure
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
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
name|LocationMode
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
name|StorageException
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
name|blobstore
operator|.
name|AzureBlobStore
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
name|cluster
operator|.
name|metadata
operator|.
name|MetaData
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
name|SnapshotId
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
name|blobstore
operator|.
name|BlobPath
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
name|blobstore
operator|.
name|BlobStore
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
name|index
operator|.
name|snapshots
operator|.
name|IndexShardRepository
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
name|RepositoryName
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
name|RepositorySettings
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
name|RepositoryVerificationException
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
name|BlobStoreRepository
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|SnapshotCreationException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_comment
comment|/**  * Azure file system implementation of the BlobStoreRepository  *<p>  * Azure file system repository supports the following settings:  *<dl>  *<dt>{@code container}</dt><dd>Azure container name. Defaults to elasticsearch-snapshots</dd>  *<dt>{@code base_path}</dt><dd>Specifies the path within bucket to repository data. Defaults to root directory.</dd>  *<dt>{@code chunk_size}</dt><dd>Large file can be divided into chunks. This parameter specifies the chunk size. Defaults to 64mb.</dd>  *<dt>{@code compress}</dt><dd>If set to true metadata files will be stored compressed. Defaults to false.</dd>  *</dl>  */
end_comment

begin_class
DECL|class|AzureRepository
specifier|public
class|class
name|AzureRepository
extends|extends
name|BlobStoreRepository
block|{
DECL|field|TYPE
specifier|public
specifier|final
specifier|static
name|String
name|TYPE
init|=
literal|"azure"
decl_stmt|;
DECL|field|CONTAINER_DEFAULT
specifier|public
specifier|final
specifier|static
name|String
name|CONTAINER_DEFAULT
init|=
literal|"elasticsearch-snapshots"
decl_stmt|;
DECL|class|Repository
specifier|static
specifier|public
specifier|final
class|class
name|Repository
block|{
DECL|field|ACCOUNT
specifier|public
specifier|static
specifier|final
name|String
name|ACCOUNT
init|=
literal|"account"
decl_stmt|;
DECL|field|LOCATION_MODE
specifier|public
specifier|static
specifier|final
name|String
name|LOCATION_MODE
init|=
literal|"location_mode"
decl_stmt|;
DECL|field|CONTAINER
specifier|public
specifier|static
specifier|final
name|String
name|CONTAINER
init|=
literal|"container"
decl_stmt|;
DECL|field|CHUNK_SIZE
specifier|public
specifier|static
specifier|final
name|String
name|CHUNK_SIZE
init|=
literal|"chunk_size"
decl_stmt|;
DECL|field|COMPRESS
specifier|public
specifier|static
specifier|final
name|String
name|COMPRESS
init|=
literal|"compress"
decl_stmt|;
DECL|field|BASE_PATH
specifier|public
specifier|static
specifier|final
name|String
name|BASE_PATH
init|=
literal|"base_path"
decl_stmt|;
block|}
DECL|field|blobStore
specifier|private
specifier|final
name|AzureBlobStore
name|blobStore
decl_stmt|;
DECL|field|basePath
specifier|private
specifier|final
name|BlobPath
name|basePath
decl_stmt|;
DECL|field|chunkSize
specifier|private
name|ByteSizeValue
name|chunkSize
decl_stmt|;
DECL|field|compress
specifier|private
name|boolean
name|compress
decl_stmt|;
DECL|field|readonly
specifier|private
specifier|final
name|boolean
name|readonly
decl_stmt|;
annotation|@
name|Inject
DECL|method|AzureRepository
specifier|public
name|AzureRepository
parameter_list|(
name|RepositoryName
name|name
parameter_list|,
name|RepositorySettings
name|repositorySettings
parameter_list|,
name|IndexShardRepository
name|indexShardRepository
parameter_list|,
name|AzureBlobStore
name|azureBlobStore
parameter_list|)
throws|throws
name|IOException
throws|,
name|URISyntaxException
throws|,
name|StorageException
block|{
name|super
argument_list|(
name|name
operator|.
name|getName
argument_list|()
argument_list|,
name|repositorySettings
argument_list|,
name|indexShardRepository
argument_list|)
expr_stmt|;
name|String
name|container
init|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
name|Repository
operator|.
name|CONTAINER
argument_list|,
name|settings
operator|.
name|get
argument_list|(
name|Storage
operator|.
name|CONTAINER
argument_list|,
name|CONTAINER_DEFAULT
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|blobStore
operator|=
name|azureBlobStore
expr_stmt|;
name|this
operator|.
name|chunkSize
operator|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|getAsBytesSize
argument_list|(
name|Repository
operator|.
name|CHUNK_SIZE
argument_list|,
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|Storage
operator|.
name|CHUNK_SIZE
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|64
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|chunkSize
operator|.
name|getMb
argument_list|()
operator|>
literal|64
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"azure repository does not support yet size> 64mb. Fall back to 64mb."
argument_list|)
expr_stmt|;
name|this
operator|.
name|chunkSize
operator|=
operator|new
name|ByteSizeValue
argument_list|(
literal|64
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|compress
operator|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
name|Repository
operator|.
name|COMPRESS
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|Storage
operator|.
name|COMPRESS
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|modeStr
init|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
name|Repository
operator|.
name|LOCATION_MODE
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|modeStr
operator|!=
literal|null
condition|)
block|{
name|LocationMode
name|locationMode
init|=
name|LocationMode
operator|.
name|valueOf
argument_list|(
name|modeStr
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|locationMode
operator|==
name|LocationMode
operator|.
name|SECONDARY_ONLY
condition|)
block|{
name|readonly
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|readonly
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
block|{
name|readonly
operator|=
literal|false
expr_stmt|;
block|}
name|String
name|basePath
init|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
name|Repository
operator|.
name|BASE_PATH
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|basePath
argument_list|)
condition|)
block|{
comment|// Remove starting / if any
name|basePath
operator|=
name|Strings
operator|.
name|trimLeadingCharacter
argument_list|(
name|basePath
argument_list|,
literal|'/'
argument_list|)
expr_stmt|;
name|BlobPath
name|path
init|=
operator|new
name|BlobPath
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|elem
range|:
name|Strings
operator|.
name|splitStringToArray
argument_list|(
name|basePath
argument_list|,
literal|'/'
argument_list|)
control|)
block|{
name|path
operator|=
name|path
operator|.
name|add
argument_list|(
name|elem
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|basePath
operator|=
name|path
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|basePath
operator|=
name|BlobPath
operator|.
name|cleanPath
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"using container [{}], chunk_size [{}], compress [{}], base_path [{}]"
argument_list|,
name|container
argument_list|,
name|chunkSize
argument_list|,
name|compress
argument_list|,
name|basePath
argument_list|)
expr_stmt|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|blobStore
specifier|protected
name|BlobStore
name|blobStore
parameter_list|()
block|{
return|return
name|blobStore
return|;
block|}
annotation|@
name|Override
DECL|method|basePath
specifier|protected
name|BlobPath
name|basePath
parameter_list|()
block|{
return|return
name|basePath
return|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|isCompress
specifier|protected
name|boolean
name|isCompress
parameter_list|()
block|{
return|return
name|compress
return|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|chunkSize
specifier|protected
name|ByteSizeValue
name|chunkSize
parameter_list|()
block|{
return|return
name|chunkSize
return|;
block|}
annotation|@
name|Override
DECL|method|initializeSnapshot
specifier|public
name|void
name|initializeSnapshot
parameter_list|(
name|SnapshotId
name|snapshotId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|indices
parameter_list|,
name|MetaData
name|metaData
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|blobStore
operator|.
name|doesContainerExist
argument_list|(
name|blobStore
operator|.
name|container
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"container [{}] does not exist. Creating..."
argument_list|,
name|blobStore
operator|.
name|container
argument_list|()
argument_list|)
expr_stmt|;
name|blobStore
operator|.
name|createContainer
argument_list|(
name|blobStore
operator|.
name|container
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|initializeSnapshot
argument_list|(
name|snapshotId
argument_list|,
name|indices
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|StorageException
decl||
name|URISyntaxException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"can not initialize container [{}]: [{}]"
argument_list|,
name|blobStore
operator|.
name|container
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SnapshotCreationException
argument_list|(
name|snapshotId
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|startVerification
specifier|public
name|String
name|startVerification
parameter_list|()
block|{
if|if
condition|(
name|readonly
operator|==
literal|false
condition|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|blobStore
operator|.
name|doesContainerExist
argument_list|(
name|blobStore
operator|.
name|container
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"container [{}] does not exist. Creating..."
argument_list|,
name|blobStore
operator|.
name|container
argument_list|()
argument_list|)
expr_stmt|;
name|blobStore
operator|.
name|createContainer
argument_list|(
name|blobStore
operator|.
name|container
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|StorageException
decl||
name|URISyntaxException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"can not initialize container [{}]: [{}]"
argument_list|,
name|blobStore
operator|.
name|container
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RepositoryVerificationException
argument_list|(
name|repositoryName
argument_list|,
literal|"can not initialize container "
operator|+
name|blobStore
operator|.
name|container
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|super
operator|.
name|startVerification
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|readOnly
specifier|public
name|boolean
name|readOnly
parameter_list|()
block|{
return|return
name|readonly
return|;
block|}
block|}
end_class

end_unit

