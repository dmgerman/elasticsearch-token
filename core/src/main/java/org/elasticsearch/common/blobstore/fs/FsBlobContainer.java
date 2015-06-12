begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.blobstore.fs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|blobstore
operator|.
name|fs
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|IOUtils
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
name|blobstore
operator|.
name|support
operator|.
name|PlainBlobMetaData
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
name|collect
operator|.
name|MapBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
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
name|DirectoryStream
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
name|Files
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
name|StandardCopyOption
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
name|attribute
operator|.
name|BasicFileAttributes
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FsBlobContainer
specifier|public
class|class
name|FsBlobContainer
extends|extends
name|AbstractBlobContainer
block|{
DECL|field|blobStore
specifier|protected
specifier|final
name|FsBlobStore
name|blobStore
decl_stmt|;
DECL|field|path
specifier|protected
specifier|final
name|Path
name|path
decl_stmt|;
DECL|method|FsBlobContainer
specifier|public
name|FsBlobContainer
parameter_list|(
name|FsBlobStore
name|blobStore
parameter_list|,
name|BlobPath
name|blobPath
parameter_list|,
name|Path
name|path
parameter_list|)
block|{
name|super
argument_list|(
name|blobPath
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
name|path
operator|=
name|path
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|listBlobs
specifier|public
name|ImmutableMap
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
return|return
name|listBlobsByPrefix
argument_list|(
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|listBlobsByPrefix
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|listBlobsByPrefix
parameter_list|(
name|String
name|blobNamePrefix
parameter_list|)
throws|throws
name|IOException
block|{
comment|// using MapBuilder and not ImmutableMap.Builder as it seems like File#listFiles might return duplicate files!
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|builder
init|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
decl_stmt|;
name|blobNamePrefix
operator|=
name|blobNamePrefix
operator|==
literal|null
condition|?
literal|""
else|:
name|blobNamePrefix
expr_stmt|;
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|path
argument_list|,
name|blobNamePrefix
operator|+
literal|"*"
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|file
range|:
name|stream
control|)
block|{
specifier|final
name|BasicFileAttributes
name|attrs
init|=
name|Files
operator|.
name|readAttributes
argument_list|(
name|file
argument_list|,
name|BasicFileAttributes
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|attrs
operator|.
name|isRegularFile
argument_list|()
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|file
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
operator|new
name|PlainBlobMetaData
argument_list|(
name|file
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|attrs
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|builder
operator|.
name|immutableMap
argument_list|()
return|;
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
name|Path
name|blobPath
init|=
name|path
operator|.
name|resolve
argument_list|(
name|blobName
argument_list|)
decl_stmt|;
name|Files
operator|.
name|deleteIfExists
argument_list|(
name|blobPath
argument_list|)
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
return|return
name|Files
operator|.
name|exists
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
name|blobName
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|openInput
specifier|public
name|InputStream
name|openInput
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|BufferedInputStream
argument_list|(
name|Files
operator|.
name|newInputStream
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
name|name
argument_list|)
argument_list|)
argument_list|,
name|blobStore
operator|.
name|bufferSizeInBytes
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createOutput
specifier|public
name|OutputStream
name|createOutput
parameter_list|(
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|file
init|=
name|path
operator|.
name|resolve
argument_list|(
name|blobName
argument_list|)
decl_stmt|;
return|return
operator|new
name|BufferedOutputStream
argument_list|(
operator|new
name|FilterOutputStream
argument_list|(
name|Files
operator|.
name|newOutputStream
argument_list|(
name|file
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
comment|// FilterOutputStream#write(byte[] b, int off, int len) is trappy writes every single byte
specifier|public
name|void
name|write
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
name|IOUtils
operator|.
name|fsync
argument_list|(
name|file
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|fsync
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|blobStore
operator|.
name|bufferSizeInBytes
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|move
specifier|public
name|void
name|move
parameter_list|(
name|String
name|source
parameter_list|,
name|String
name|target
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|sourcePath
init|=
name|path
operator|.
name|resolve
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|Path
name|targetPath
init|=
name|path
operator|.
name|resolve
argument_list|(
name|target
argument_list|)
decl_stmt|;
comment|// If the target file exists then Files.move() behaviour is implementation specific
comment|// the existing file might be replaced or this method fails by throwing an IOException.
assert|assert
operator|!
name|Files
operator|.
name|exists
argument_list|(
name|targetPath
argument_list|)
assert|;
name|Files
operator|.
name|move
argument_list|(
name|sourcePath
argument_list|,
name|targetPath
argument_list|,
name|StandardCopyOption
operator|.
name|ATOMIC_MOVE
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|fsync
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
