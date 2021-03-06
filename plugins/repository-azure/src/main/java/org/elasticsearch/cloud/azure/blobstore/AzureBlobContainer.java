begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.azure.blobstore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
operator|.
name|blobstore
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
name|common
operator|.
name|Nullable
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
name|BlobMetaData
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
name|support
operator|.
name|AbstractBlobContainer
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
name|Streams
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
name|repositories
operator|.
name|RepositoryException
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
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
name|nio
operator|.
name|file
operator|.
name|FileAlreadyExistsException
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
name|NoSuchFileException
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

begin_class
DECL|class|AzureBlobContainer
specifier|public
class|class
name|AzureBlobContainer
extends|extends
name|AbstractBlobContainer
block|{
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
name|AzureBlobContainer
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|blobStore
specifier|protected
specifier|final
name|AzureBlobStore
name|blobStore
decl_stmt|;
DECL|field|keyPath
specifier|protected
specifier|final
name|String
name|keyPath
decl_stmt|;
DECL|field|repositoryName
specifier|protected
specifier|final
name|String
name|repositoryName
decl_stmt|;
DECL|method|AzureBlobContainer
specifier|public
name|AzureBlobContainer
parameter_list|(
name|String
name|repositoryName
parameter_list|,
name|BlobPath
name|path
parameter_list|,
name|AzureBlobStore
name|blobStore
parameter_list|)
block|{
name|super
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|this
operator|.
name|blobStore
operator|=
name|blobStore
expr_stmt|;
name|this
operator|.
name|keyPath
operator|=
name|path
operator|.
name|buildAsString
argument_list|()
expr_stmt|;
name|this
operator|.
name|repositoryName
operator|=
name|repositoryName
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|blobExists
specifier|public
name|boolean
name|blobExists
parameter_list|(
name|String
name|blobName
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"blobExists({})"
argument_list|,
name|blobName
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|blobStore
operator|.
name|blobExists
argument_list|(
name|blobStore
operator|.
name|container
argument_list|()
argument_list|,
name|buildKey
argument_list|(
name|blobName
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
decl||
name|StorageException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"can not access [{}] in container {{}}: {}"
argument_list|,
name|blobName
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
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|readBlob
specifier|public
name|InputStream
name|readBlob
parameter_list|(
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"readBlob({})"
argument_list|,
name|blobName
argument_list|)
expr_stmt|;
if|if
condition|(
name|blobStore
operator|.
name|getLocationMode
argument_list|()
operator|==
name|LocationMode
operator|.
name|SECONDARY_ONLY
operator|&&
operator|!
name|blobExists
argument_list|(
name|blobName
argument_list|)
condition|)
block|{
comment|// On Azure, if the location path is a secondary location, and the blob does not
comment|// exist, instead of returning immediately from the getInputStream call below
comment|// with a 404 StorageException, Azure keeps trying and trying for a long timeout
comment|// before throwing a storage exception.  This can cause long delays in retrieving
comment|// snapshots, so we first check if the blob exists before trying to open an input
comment|// stream to it.
throw|throw
operator|new
name|NoSuchFileException
argument_list|(
literal|"Blob ["
operator|+
name|blobName
operator|+
literal|"] does not exist"
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|blobStore
operator|.
name|getInputStream
argument_list|(
name|blobStore
operator|.
name|container
argument_list|()
argument_list|,
name|buildKey
argument_list|(
name|blobName
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|StorageException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getHttpStatusCode
argument_list|()
operator|==
name|HttpURLConnection
operator|.
name|HTTP_NOT_FOUND
condition|)
block|{
throw|throw
operator|new
name|NoSuchFileException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeBlob
specifier|public
name|void
name|writeBlob
parameter_list|(
name|String
name|blobName
parameter_list|,
name|InputStream
name|inputStream
parameter_list|,
name|long
name|blobSize
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|blobExists
argument_list|(
name|blobName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|FileAlreadyExistsException
argument_list|(
literal|"blob ["
operator|+
name|blobName
operator|+
literal|"] already exists, cannot overwrite"
argument_list|)
throw|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"writeBlob({}, stream, {})"
argument_list|,
name|blobName
argument_list|,
name|blobSize
argument_list|)
expr_stmt|;
try|try
init|(
name|OutputStream
name|stream
init|=
name|createOutput
argument_list|(
name|blobName
argument_list|)
init|)
block|{
name|Streams
operator|.
name|copy
argument_list|(
name|inputStream
argument_list|,
name|stream
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|createOutput
specifier|private
name|OutputStream
name|createOutput
parameter_list|(
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
operator|new
name|AzureOutputStream
argument_list|(
name|blobStore
operator|.
name|getOutputStream
argument_list|(
name|blobStore
operator|.
name|container
argument_list|()
argument_list|,
name|buildKey
argument_list|(
name|blobName
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|StorageException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getHttpStatusCode
argument_list|()
operator|==
name|HttpURLConnection
operator|.
name|HTTP_NOT_FOUND
condition|)
block|{
throw|throw
operator|new
name|NoSuchFileException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RepositoryException
argument_list|(
name|repositoryName
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|deleteBlob
specifier|public
name|void
name|deleteBlob
parameter_list|(
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"deleteBlob({})"
argument_list|,
name|blobName
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|blobExists
argument_list|(
name|blobName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|NoSuchFileException
argument_list|(
literal|"Blob ["
operator|+
name|blobName
operator|+
literal|"] does not exist"
argument_list|)
throw|;
block|}
try|try
block|{
name|blobStore
operator|.
name|deleteBlob
argument_list|(
name|blobStore
operator|.
name|container
argument_list|()
argument_list|,
name|buildKey
argument_list|(
name|blobName
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
decl||
name|StorageException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"can not access [{}] in container {{}}: {}"
argument_list|,
name|blobName
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|listBlobsByPrefix
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|listBlobsByPrefix
parameter_list|(
annotation|@
name|Nullable
name|String
name|prefix
parameter_list|)
throws|throws
name|IOException
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"listBlobsByPrefix({})"
argument_list|,
name|prefix
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|blobStore
operator|.
name|listBlobsByPrefix
argument_list|(
name|blobStore
operator|.
name|container
argument_list|()
argument_list|,
name|keyPath
argument_list|,
name|prefix
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
decl||
name|StorageException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"can not access [{}] in container {{}}: {}"
argument_list|,
name|prefix
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|move
specifier|public
name|void
name|move
parameter_list|(
name|String
name|sourceBlobName
parameter_list|,
name|String
name|targetBlobName
parameter_list|)
throws|throws
name|IOException
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"move({}, {})"
argument_list|,
name|sourceBlobName
argument_list|,
name|targetBlobName
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|source
init|=
name|keyPath
operator|+
name|sourceBlobName
decl_stmt|;
name|String
name|target
init|=
name|keyPath
operator|+
name|targetBlobName
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"moving blob [{}] to [{}] in container {{}}"
argument_list|,
name|source
argument_list|,
name|target
argument_list|,
name|blobStore
operator|.
name|container
argument_list|()
argument_list|)
expr_stmt|;
name|blobStore
operator|.
name|moveBlob
argument_list|(
name|blobStore
operator|.
name|container
argument_list|()
argument_list|,
name|source
argument_list|,
name|target
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
decl||
name|StorageException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"can not move blob [{}] to [{}] in container {{}}: {}"
argument_list|,
name|sourceBlobName
argument_list|,
name|targetBlobName
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|listBlobs
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|listBlobs
parameter_list|()
throws|throws
name|IOException
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"listBlobs()"
argument_list|)
expr_stmt|;
return|return
name|listBlobsByPrefix
argument_list|(
literal|null
argument_list|)
return|;
block|}
DECL|method|buildKey
specifier|protected
name|String
name|buildKey
parameter_list|(
name|String
name|blobName
parameter_list|)
block|{
return|return
name|keyPath
operator|+
operator|(
name|blobName
operator|==
literal|null
condition|?
literal|""
else|:
name|blobName
operator|)
return|;
block|}
block|}
end_class

end_unit

