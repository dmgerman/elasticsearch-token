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
name|AppendableBlobContainer
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
name|stream
operator|.
name|DataOutputStreamOutput
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
name|RandomAccessFile
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|FsAppendableBlobContainer
specifier|public
class|class
name|FsAppendableBlobContainer
extends|extends
name|AbstractFsBlobContainer
implements|implements
name|AppendableBlobContainer
block|{
DECL|method|FsAppendableBlobContainer
specifier|public
name|FsAppendableBlobContainer
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
DECL|method|appendBlob
annotation|@
name|Override
specifier|public
name|AppendableBlob
name|appendBlob
parameter_list|(
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FsAppendableBlob
argument_list|(
operator|new
name|File
argument_list|(
name|path
argument_list|,
name|blobName
argument_list|)
argument_list|)
return|;
block|}
DECL|method|canAppendToExistingBlob
annotation|@
name|Override
specifier|public
name|boolean
name|canAppendToExistingBlob
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|class|FsAppendableBlob
specifier|private
class|class
name|FsAppendableBlob
implements|implements
name|AppendableBlob
block|{
DECL|field|file
specifier|private
specifier|final
name|File
name|file
decl_stmt|;
DECL|method|FsAppendableBlob
specifier|public
name|FsAppendableBlob
parameter_list|(
name|File
name|file
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|file
operator|=
name|file
expr_stmt|;
block|}
DECL|method|append
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
specifier|final
name|AppendBlobListener
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
name|RandomAccessFile
name|raf
init|=
literal|null
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
name|raf
operator|.
name|seek
argument_list|(
name|raf
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|listener
operator|.
name|withStream
argument_list|(
operator|new
name|DataOutputStreamOutput
argument_list|(
name|raf
argument_list|)
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onCompleted
argument_list|()
expr_stmt|;
name|raf
operator|.
name|close
argument_list|()
expr_stmt|;
name|FileSystemUtils
operator|.
name|syncFile
argument_list|(
name|file
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
block|}
finally|finally
block|{
if|if
condition|(
name|raf
operator|!=
literal|null
condition|)
block|{
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
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// nothing to do there
block|}
block|}
block|}
end_class

end_unit

