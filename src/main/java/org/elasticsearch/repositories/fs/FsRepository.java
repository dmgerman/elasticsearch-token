begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.fs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|fs
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
name|blobstore
operator|.
name|fs
operator|.
name|FsBlobStore
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
name|io
operator|.
name|FileSystemUtils
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
name|io
operator|.
name|PathUtils
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
name|RepositoryException
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
name|blobstore
operator|.
name|BlobStoreRepository
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
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Paths
import|;
end_import

begin_comment
comment|/**  * Shared file system implementation of the BlobStoreRepository  *<p/>  * Shared file system repository supports the following settings  *<dl>  *<dt>{@code location}</dt><dd>Path to the root of repository. This is mandatory parameter.</dd>  *<dt>{@code concurrent_streams}</dt><dd>Number of concurrent read/write stream (per repository on each node). Defaults to 5.</dd>  *<dt>{@code chunk_size}</dt><dd>Large file can be divided into chunks. This parameter specifies the chunk size. Defaults to not chucked.</dd>  *<dt>{@code compress}</dt><dd>If set to true metadata files will be stored compressed. Defaults to false.</dd>  *</ol>  */
end_comment

begin_class
DECL|class|FsRepository
specifier|public
class|class
name|FsRepository
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
literal|"fs"
decl_stmt|;
DECL|field|blobStore
specifier|private
specifier|final
name|FsBlobStore
name|blobStore
decl_stmt|;
DECL|field|chunkSize
specifier|private
name|ByteSizeValue
name|chunkSize
decl_stmt|;
DECL|field|basePath
specifier|private
specifier|final
name|BlobPath
name|basePath
decl_stmt|;
DECL|field|compress
specifier|private
name|boolean
name|compress
decl_stmt|;
comment|/**      * Constructs new shared file system repository      *      * @param name                 repository name      * @param repositorySettings   repository settings      * @param indexShardRepository index shard repository      * @throws IOException      */
annotation|@
name|Inject
DECL|method|FsRepository
specifier|public
name|FsRepository
parameter_list|(
name|RepositoryName
name|name
parameter_list|,
name|RepositorySettings
name|repositorySettings
parameter_list|,
name|IndexShardRepository
name|indexShardRepository
parameter_list|)
throws|throws
name|IOException
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
name|Path
name|locationFile
decl_stmt|;
name|String
name|location
init|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
literal|"location"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"repositories.fs.location"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|location
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"using local fs location for gateway, should be changed to be a shared location across nodes"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RepositoryException
argument_list|(
name|name
operator|.
name|name
argument_list|()
argument_list|,
literal|"missing location"
argument_list|)
throw|;
block|}
else|else
block|{
name|locationFile
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
name|blobStore
operator|=
operator|new
name|FsBlobStore
argument_list|(
name|settings
argument_list|,
name|locationFile
argument_list|)
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
literal|"chunk_size"
argument_list|,
name|settings
operator|.
name|getAsBytesSize
argument_list|(
literal|"repositories.fs.chunk_size"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"compress"
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"repositories.fs.compress"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
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
block|}
end_class

end_unit

