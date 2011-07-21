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
name|BlobStoreException
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
name|component
operator|.
name|AbstractComponent
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
name|util
operator|.
name|concurrent
operator|.
name|Executor
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|FsBlobStore
specifier|public
class|class
name|FsBlobStore
extends|extends
name|AbstractComponent
implements|implements
name|BlobStore
block|{
DECL|field|executor
specifier|private
specifier|final
name|Executor
name|executor
decl_stmt|;
DECL|field|path
specifier|private
specifier|final
name|File
name|path
decl_stmt|;
DECL|field|bufferSizeInBytes
specifier|private
specifier|final
name|int
name|bufferSizeInBytes
decl_stmt|;
DECL|method|FsBlobStore
specifier|public
name|FsBlobStore
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Executor
name|executor
parameter_list|,
name|File
name|path
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
if|if
condition|(
operator|!
name|path
operator|.
name|exists
argument_list|()
condition|)
block|{
name|boolean
name|b
init|=
name|FileSystemUtils
operator|.
name|mkdirs
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|b
condition|)
block|{
throw|throw
operator|new
name|BlobStoreException
argument_list|(
literal|"Failed to create directory at ["
operator|+
name|path
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|path
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BlobStoreException
argument_list|(
literal|"Path is not a directory at ["
operator|+
name|path
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|bufferSizeInBytes
operator|=
operator|(
name|int
operator|)
name|settings
operator|.
name|getAsBytesSize
argument_list|(
literal|"buffer_size"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|100
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|path
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|path
specifier|public
name|File
name|path
parameter_list|()
block|{
return|return
name|path
return|;
block|}
DECL|method|bufferSizeInBytes
specifier|public
name|int
name|bufferSizeInBytes
parameter_list|()
block|{
return|return
name|this
operator|.
name|bufferSizeInBytes
return|;
block|}
DECL|method|executor
specifier|public
name|Executor
name|executor
parameter_list|()
block|{
return|return
name|executor
return|;
block|}
DECL|method|immutableBlobContainer
annotation|@
name|Override
specifier|public
name|ImmutableBlobContainer
name|immutableBlobContainer
parameter_list|(
name|BlobPath
name|path
parameter_list|)
block|{
return|return
operator|new
name|FsImmutableBlobContainer
argument_list|(
name|this
argument_list|,
name|path
argument_list|,
name|buildAndCreate
argument_list|(
name|path
argument_list|)
argument_list|)
return|;
block|}
DECL|method|delete
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|BlobPath
name|path
parameter_list|)
block|{
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
name|buildPath
argument_list|(
name|path
argument_list|)
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
comment|// nothing to do here...
block|}
DECL|method|buildAndCreate
specifier|private
specifier|synchronized
name|File
name|buildAndCreate
parameter_list|(
name|BlobPath
name|path
parameter_list|)
block|{
name|File
name|f
init|=
name|buildPath
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|FileSystemUtils
operator|.
name|mkdirs
argument_list|(
name|f
argument_list|)
expr_stmt|;
return|return
name|f
return|;
block|}
DECL|method|buildPath
specifier|private
name|File
name|buildPath
parameter_list|(
name|BlobPath
name|path
parameter_list|)
block|{
name|String
index|[]
name|paths
init|=
name|path
operator|.
name|toArray
argument_list|()
decl_stmt|;
if|if
condition|(
name|paths
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|path
argument_list|()
return|;
block|}
name|File
name|blobPath
init|=
operator|new
name|File
argument_list|(
name|this
operator|.
name|path
argument_list|,
name|paths
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|paths
operator|.
name|length
operator|>
literal|1
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|paths
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|blobPath
operator|=
operator|new
name|File
argument_list|(
name|blobPath
argument_list|,
name|paths
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|blobPath
return|;
block|}
block|}
end_class

end_unit

