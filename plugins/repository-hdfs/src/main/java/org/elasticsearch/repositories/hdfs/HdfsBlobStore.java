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
name|FileAlreadyExistsException
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
name|FileContext
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
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|BlobContainer
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
name|IOException
import|;
end_import

begin_class
DECL|class|HdfsBlobStore
specifier|final
class|class
name|HdfsBlobStore
extends|extends
name|AbstractComponent
implements|implements
name|BlobStore
block|{
DECL|field|repository
specifier|private
specifier|final
name|HdfsRepository
name|repository
decl_stmt|;
DECL|field|root
specifier|private
specifier|final
name|Path
name|root
decl_stmt|;
DECL|field|bufferSizeInBytes
specifier|private
specifier|final
name|int
name|bufferSizeInBytes
decl_stmt|;
DECL|method|HdfsBlobStore
name|HdfsBlobStore
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|HdfsRepository
name|repository
parameter_list|,
name|Path
name|root
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|repository
operator|=
name|repository
expr_stmt|;
name|this
operator|.
name|root
operator|=
name|root
expr_stmt|;
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
try|try
block|{
name|mkdirs
argument_list|(
name|root
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileAlreadyExistsException
name|ok
parameter_list|)
block|{
comment|// behaves like Files.createDirectories
block|}
block|}
DECL|method|mkdirs
specifier|private
name|void
name|mkdirs
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|SecurityUtils
operator|.
name|execute
argument_list|(
name|repository
argument_list|,
operator|new
name|FcCallback
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
name|FileContext
name|fc
parameter_list|)
throws|throws
name|IOException
block|{
name|fc
operator|.
name|mkdir
argument_list|(
name|path
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
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
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|root
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|getRepository
name|HdfsRepository
name|getRepository
parameter_list|()
block|{
return|return
name|repository
return|;
block|}
DECL|method|getBufferSizeInBytes
name|int
name|getBufferSizeInBytes
parameter_list|()
block|{
return|return
name|bufferSizeInBytes
return|;
block|}
annotation|@
name|Override
DECL|method|blobContainer
specifier|public
name|BlobContainer
name|blobContainer
parameter_list|(
name|BlobPath
name|path
parameter_list|)
block|{
return|return
operator|new
name|HdfsBlobContainer
argument_list|(
name|path
argument_list|,
name|this
argument_list|,
name|buildHdfsPath
argument_list|(
name|path
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|delete
specifier|public
name|void
name|delete
parameter_list|(
name|BlobPath
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|SecurityUtils
operator|.
name|execute
argument_list|(
name|repository
argument_list|,
operator|new
name|FcCallback
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
name|FileContext
name|fc
parameter_list|)
throws|throws
name|IOException
block|{
name|fc
operator|.
name|delete
argument_list|(
name|translateToHdfsPath
argument_list|(
name|path
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|buildHdfsPath
specifier|private
name|Path
name|buildHdfsPath
parameter_list|(
name|BlobPath
name|blobPath
parameter_list|)
block|{
specifier|final
name|Path
name|path
init|=
name|translateToHdfsPath
argument_list|(
name|blobPath
argument_list|)
decl_stmt|;
try|try
block|{
name|mkdirs
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileAlreadyExistsException
name|ok
parameter_list|)
block|{
comment|// behaves like Files.createDirectories
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to create blob container"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
return|return
name|path
return|;
block|}
DECL|method|translateToHdfsPath
specifier|private
name|Path
name|translateToHdfsPath
parameter_list|(
name|BlobPath
name|blobPath
parameter_list|)
block|{
name|Path
name|path
init|=
name|root
decl_stmt|;
for|for
control|(
name|String
name|p
range|:
name|blobPath
control|)
block|{
name|path
operator|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|p
argument_list|)
expr_stmt|;
block|}
return|return
name|path
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|//
block|}
block|}
end_class

end_unit

