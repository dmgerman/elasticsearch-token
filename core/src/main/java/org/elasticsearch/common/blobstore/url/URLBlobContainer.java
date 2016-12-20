begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.blobstore.url
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|blobstore
operator|.
name|url
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
name|java
operator|.
name|io
operator|.
name|BufferedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|net
operator|.
name|URL
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

begin_comment
comment|/**  * URL blob implementation of {@link org.elasticsearch.common.blobstore.BlobContainer}  */
end_comment

begin_class
DECL|class|URLBlobContainer
specifier|public
class|class
name|URLBlobContainer
extends|extends
name|AbstractBlobContainer
block|{
DECL|field|blobStore
specifier|protected
specifier|final
name|URLBlobStore
name|blobStore
decl_stmt|;
DECL|field|path
specifier|protected
specifier|final
name|URL
name|path
decl_stmt|;
comment|/**      * Constructs new URLBlobContainer      *      * @param blobStore blob store      * @param blobPath  blob path for this container      * @param path      URL for this container      */
DECL|method|URLBlobContainer
specifier|public
name|URLBlobContainer
parameter_list|(
name|URLBlobStore
name|blobStore
parameter_list|,
name|BlobPath
name|blobPath
parameter_list|,
name|URL
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
comment|/**      * Returns URL for this container      *      * @return URL for this container      */
DECL|method|url
specifier|public
name|URL
name|url
parameter_list|()
block|{
return|return
name|this
operator|.
name|path
return|;
block|}
comment|/**      * This operation is not supported by URLBlobContainer      */
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"URL repository doesn't support this operation"
argument_list|)
throw|;
block|}
comment|/**      * This operation is not supported by URLBlobContainer      */
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
name|String
name|blobNamePrefix
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"URL repository doesn't support this operation"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|move
specifier|public
name|void
name|move
parameter_list|(
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"URL repository doesn't support this operation"
argument_list|)
throw|;
block|}
comment|/**      * This operation is not supported by URLBlobContainer      */
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"URL repository is read only"
argument_list|)
throw|;
block|}
comment|/**      * This operation is not supported by URLBlobContainer      */
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"URL repository doesn't support this operation"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|readBlob
specifier|public
name|InputStream
name|readBlob
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
operator|new
name|BufferedInputStream
argument_list|(
operator|new
name|URL
argument_list|(
name|path
argument_list|,
name|name
argument_list|)
operator|.
name|openStream
argument_list|()
argument_list|,
name|blobStore
operator|.
name|bufferSizeInBytes
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fnfe
parameter_list|)
block|{
throw|throw
operator|new
name|NoSuchFileException
argument_list|(
literal|"["
operator|+
name|name
operator|+
literal|"] blob not found"
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"URL repository doesn't support this operation"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

