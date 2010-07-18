begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ImmutableBlobContainer
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
name|BlobStores
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
name|java
operator|.
name|io
operator|.
name|File
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
name|RandomAccessFile
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|FsImmutableBlobContainer
specifier|public
class|class
name|FsImmutableBlobContainer
extends|extends
name|AbstractFsBlobContainer
implements|implements
name|ImmutableBlobContainer
block|{
DECL|method|FsImmutableBlobContainer
specifier|public
name|FsImmutableBlobContainer
parameter_list|(
name|FsBlobStore
name|blobStore
parameter_list|,
name|BlobPath
name|blobPath
parameter_list|,
name|File
name|path
parameter_list|)
block|{
name|super
argument_list|(
name|blobStore
argument_list|,
name|blobPath
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
DECL|method|writeBlob
annotation|@
name|Override
specifier|public
name|void
name|writeBlob
parameter_list|(
specifier|final
name|String
name|blobName
parameter_list|,
specifier|final
name|InputStream
name|is
parameter_list|,
specifier|final
name|long
name|sizeInBytes
parameter_list|,
specifier|final
name|WriterListener
name|listener
parameter_list|)
block|{
name|blobStore
operator|.
name|executor
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
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|path
argument_list|,
name|blobName
argument_list|)
decl_stmt|;
name|RandomAccessFile
name|raf
decl_stmt|;
try|try
block|{
name|raf
operator|=
operator|new
name|RandomAccessFile
argument_list|(
name|file
argument_list|,
literal|"rw"
argument_list|)
expr_stmt|;
comment|// clean the file if it exists
name|raf
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
try|try
block|{
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
name|blobStore
operator|.
name|bufferSizeInBytes
argument_list|()
index|]
decl_stmt|;
name|int
name|bytesRead
decl_stmt|;
while|while
condition|(
operator|(
name|bytesRead
operator|=
name|is
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
name|raf
operator|.
name|write
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|bytesRead
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
try|try
block|{
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// do nothing
block|}
try|try
block|{
name|raf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// do nothing
block|}
block|}
name|FileSystemUtils
operator|.
name|syncFile
argument_list|(
name|file
argument_list|)
expr_stmt|;
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
comment|// just on the safe size, try and delete it on failure
try|try
block|{
if|if
condition|(
name|file
operator|.
name|exists
argument_list|()
condition|)
block|{
name|file
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
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
DECL|method|writeBlob
annotation|@
name|Override
specifier|public
name|void
name|writeBlob
parameter_list|(
name|String
name|blobName
parameter_list|,
name|InputStream
name|is
parameter_list|,
name|long
name|sizeInBytes
parameter_list|)
throws|throws
name|IOException
block|{
name|BlobStores
operator|.
name|syncWriteBlob
argument_list|(
name|this
argument_list|,
name|blobName
argument_list|,
name|is
argument_list|,
name|sizeInBytes
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

