begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.blobstore.hdfs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|blobstore
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
name|FSDataInputStream
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
name|ImmutableMap
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AbstractHdfsBlobContainer
specifier|public
specifier|abstract
class|class
name|AbstractHdfsBlobContainer
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
DECL|method|AbstractHdfsBlobContainer
specifier|public
name|AbstractHdfsBlobContainer
parameter_list|(
name|HdfsBlobStore
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
name|FileStatus
index|[]
name|files
init|=
name|blobStore
operator|.
name|fileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|path
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
name|ImmutableMap
operator|.
name|of
argument_list|()
return|;
block|}
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
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
name|builder
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
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|listBlobsByPrefix
annotation|@
name|Override
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|listBlobsByPrefix
parameter_list|(
specifier|final
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
name|blobStore
operator|.
name|fileSystem
argument_list|()
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
name|ImmutableMap
operator|.
name|of
argument_list|()
return|;
block|}
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
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
name|builder
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
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|deleteBlob
specifier|public
name|boolean
name|deleteBlob
parameter_list|(
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|blobStore
operator|.
name|fileSystem
argument_list|()
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
DECL|method|readBlob
annotation|@
name|Override
specifier|public
name|void
name|readBlob
parameter_list|(
specifier|final
name|String
name|blobName
parameter_list|,
specifier|final
name|ReadBlobListener
name|listener
parameter_list|)
block|{
name|blobStore
operator|.
name|executorService
argument_list|()
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
literal|1024
operator|*
literal|16
index|]
decl_stmt|;
name|FSDataInputStream
name|fileStream
decl_stmt|;
try|try
block|{
name|fileStream
operator|=
name|blobStore
operator|.
name|fileSystem
argument_list|()
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
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|int
name|bytesRead
decl_stmt|;
while|while
condition|(
operator|(
name|bytesRead
operator|=
name|fileStream
operator|.
name|read
argument_list|(
name|buffer
argument_list|)
operator|)
operator|!=
operator|-
literal|1
condition|)
block|{
name|listener
operator|.
name|onPartial
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|bytesRead
argument_list|)
expr_stmt|;
block|}
name|listener
operator|.
name|onCompleted
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|fileStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

