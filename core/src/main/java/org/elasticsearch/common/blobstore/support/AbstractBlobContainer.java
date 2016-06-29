begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.blobstore.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|blobstore
operator|.
name|support
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
name|bytes
operator|.
name|BytesReference
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
name|util
operator|.
name|Collection
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
comment|/**  * A base abstract blob container that implements higher level container methods.  */
end_comment

begin_class
DECL|class|AbstractBlobContainer
specifier|public
specifier|abstract
class|class
name|AbstractBlobContainer
implements|implements
name|BlobContainer
block|{
DECL|field|path
specifier|private
specifier|final
name|BlobPath
name|path
decl_stmt|;
DECL|method|AbstractBlobContainer
specifier|protected
name|AbstractBlobContainer
parameter_list|(
name|BlobPath
name|path
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|path
specifier|public
name|BlobPath
name|path
parameter_list|()
block|{
return|return
name|this
operator|.
name|path
return|;
block|}
annotation|@
name|Override
DECL|method|deleteBlobsByPrefix
specifier|public
name|void
name|deleteBlobsByPrefix
parameter_list|(
specifier|final
name|String
name|blobNamePrefix
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|blobs
init|=
name|listBlobsByPrefix
argument_list|(
name|blobNamePrefix
argument_list|)
decl_stmt|;
for|for
control|(
name|BlobMetaData
name|blob
range|:
name|blobs
operator|.
name|values
argument_list|()
control|)
block|{
name|deleteBlob
argument_list|(
name|blob
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|deleteBlobs
specifier|public
name|void
name|deleteBlobs
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|blobNames
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|String
name|blob
range|:
name|blobNames
control|)
block|{
name|deleteBlob
argument_list|(
name|blob
argument_list|)
expr_stmt|;
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
name|BytesReference
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|InputStream
name|stream
init|=
name|bytes
operator|.
name|streamInput
argument_list|()
init|)
block|{
name|writeBlob
argument_list|(
name|blobName
argument_list|,
name|stream
argument_list|,
name|bytes
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

