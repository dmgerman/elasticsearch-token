begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.hdfs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|hdfs
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|PathFilter
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
name|bytes
operator|.
name|BytesReference
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
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
DECL|class|HdfsBlobContainer
specifier|public
class|class
name|HdfsBlobContainer
extends|extends
name|AbstractBlobContainer
block|{
DECL|field|blobStore
specifier|protected
specifier|final
name|HdfsBlobStore
name|blobStore
decl_stmt|;
DECL|field|path
specifier|protected
specifier|final
name|Path
name|path
decl_stmt|;
DECL|method|HdfsBlobContainer
specifier|public
name|HdfsBlobContainer
parameter_list|(
name|BlobPath
name|blobPath
parameter_list|,
name|HdfsBlobStore
name|blobStore
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
DECL|method|blobExists
specifier|public
name|boolean
name|blobExists
parameter_list|(
name|String
name|blobName
parameter_list|)
block|{
try|try
block|{
return|return
name|SecurityUtils
operator|.
name|execute
argument_list|(
name|blobStore
operator|.
name|fileSystemFactory
argument_list|()
argument_list|,
operator|new
name|FsCallback
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Boolean
name|doInHdfs
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|blobName
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
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
name|SecurityUtils
operator|.
name|execute
argument_list|(
name|blobStore
operator|.
name|fileSystemFactory
argument_list|()
argument_list|,
operator|new
name|FsCallback
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Boolean
name|doInHdfs
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|blobName
argument_list|)
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
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
name|boolean
name|rename
init|=
name|SecurityUtils
operator|.
name|execute
argument_list|(
name|blobStore
operator|.
name|fileSystemFactory
argument_list|()
argument_list|,
operator|new
name|FsCallback
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Boolean
name|doInHdfs
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|rename
argument_list|(
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|sourceBlobName
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|targetBlobName
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|rename
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"can not move blob from [%s] to [%s]"
argument_list|,
name|sourceBlobName
argument_list|,
name|targetBlobName
argument_list|)
argument_list|)
throw|;
block|}
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
comment|// FSDataInputStream does buffering internally
return|return
name|SecurityUtils
operator|.
name|execute
argument_list|(
name|blobStore
operator|.
name|fileSystemFactory
argument_list|()
argument_list|,
operator|new
name|FsCallback
argument_list|<
name|InputStream
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|InputStream
name|doInHdfs
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|open
argument_list|(
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|blobName
argument_list|)
argument_list|,
name|blobStore
operator|.
name|bufferSizeInBytes
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
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
name|SecurityUtils
operator|.
name|execute
argument_list|(
name|blobStore
operator|.
name|fileSystemFactory
argument_list|()
argument_list|,
operator|new
name|FsCallback
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|doInHdfs
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
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
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
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
name|BytesReference
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
name|SecurityUtils
operator|.
name|execute
argument_list|(
name|blobStore
operator|.
name|fileSystemFactory
argument_list|()
argument_list|,
operator|new
name|FsCallback
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|doInHdfs
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
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
name|bytes
operator|.
name|writeTo
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
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
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|blobName
argument_list|)
decl_stmt|;
comment|// FSDataOutputStream does buffering internally
return|return
name|blobStore
operator|.
name|fileSystemFactory
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|create
argument_list|(
name|file
argument_list|,
literal|true
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
specifier|final
annotation|@
name|Nullable
name|String
name|blobNamePrefix
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|files
init|=
name|SecurityUtils
operator|.
name|execute
argument_list|(
name|blobStore
operator|.
name|fileSystemFactory
argument_list|()
argument_list|,
operator|new
name|FsCallback
argument_list|<
name|FileStatus
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|FileStatus
index|[]
name|doInHdfs
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|listStatus
argument_list|(
name|path
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
name|blobNamePrefix
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
operator|||
name|files
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|PlainBlobMetaData
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|file
operator|.
name|getLen
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|map
argument_list|)
return|;
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
name|FileStatus
index|[]
name|files
init|=
name|SecurityUtils
operator|.
name|execute
argument_list|(
name|blobStore
operator|.
name|fileSystemFactory
argument_list|()
argument_list|,
operator|new
name|FsCallback
argument_list|<
name|FileStatus
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|FileStatus
index|[]
name|doInHdfs
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|listStatus
argument_list|(
name|path
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
operator|||
name|files
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|PlainBlobMetaData
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|file
operator|.
name|getLen
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|map
argument_list|)
return|;
block|}
block|}
end_class

end_unit

