begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.blobstore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|blobstore
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|ImmutableAppendableBlobContainer
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
name|org
operator|.
name|jclouds
operator|.
name|blobstore
operator|.
name|AsyncBlobStore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|blobstore
operator|.
name|BlobStoreContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|domain
operator|.
name|Location
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
DECL|class|CloudBlobStore
specifier|public
class|class
name|CloudBlobStore
extends|extends
name|AbstractComponent
implements|implements
name|BlobStore
block|{
DECL|field|blobStoreContext
specifier|private
specifier|final
name|BlobStoreContext
name|blobStoreContext
decl_stmt|;
DECL|field|container
specifier|private
specifier|final
name|String
name|container
decl_stmt|;
DECL|field|location
specifier|private
specifier|final
name|Location
name|location
decl_stmt|;
DECL|field|executor
specifier|private
specifier|final
name|Executor
name|executor
decl_stmt|;
DECL|field|bufferSizeInBytes
specifier|private
specifier|final
name|int
name|bufferSizeInBytes
decl_stmt|;
DECL|method|CloudBlobStore
specifier|public
name|CloudBlobStore
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|BlobStoreContext
name|blobStoreContext
parameter_list|,
name|Executor
name|executor
parameter_list|,
name|String
name|container
parameter_list|,
name|String
name|location
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|blobStoreContext
operator|=
name|blobStoreContext
expr_stmt|;
name|this
operator|.
name|container
operator|=
name|container
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
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
if|if
condition|(
name|location
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|location
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|Location
name|matchedLocation
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|?
extends|extends
name|Location
argument_list|>
name|assignableLocations
init|=
name|blobStoreContext
operator|.
name|getBlobStore
argument_list|()
operator|.
name|listAssignableLocations
argument_list|()
decl_stmt|;
for|for
control|(
name|Location
name|oLocation
range|:
name|assignableLocations
control|)
block|{
if|if
condition|(
name|oLocation
operator|.
name|getId
argument_list|()
operator|.
name|equals
argument_list|(
name|location
argument_list|)
condition|)
block|{
name|matchedLocation
operator|=
name|oLocation
expr_stmt|;
break|break;
block|}
block|}
name|this
operator|.
name|location
operator|=
name|matchedLocation
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|location
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Not a valid location ["
operator|+
name|location
operator|+
literal|"], available locations "
operator|+
name|assignableLocations
argument_list|)
throw|;
block|}
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"Using location [{}], container [{}]"
argument_list|,
name|this
operator|.
name|location
argument_list|,
name|this
operator|.
name|container
argument_list|)
expr_stmt|;
name|sync
argument_list|()
operator|.
name|createContainerInLocation
argument_list|(
name|this
operator|.
name|location
argument_list|,
name|container
argument_list|)
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
name|container
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
DECL|method|container
specifier|public
name|String
name|container
parameter_list|()
block|{
return|return
name|this
operator|.
name|container
return|;
block|}
DECL|method|location
specifier|public
name|Location
name|location
parameter_list|()
block|{
return|return
name|this
operator|.
name|location
return|;
block|}
DECL|method|async
specifier|public
name|AsyncBlobStore
name|async
parameter_list|()
block|{
return|return
name|blobStoreContext
operator|.
name|getAsyncBlobStore
argument_list|()
return|;
block|}
DECL|method|sync
specifier|public
name|org
operator|.
name|jclouds
operator|.
name|blobstore
operator|.
name|BlobStore
name|sync
parameter_list|()
block|{
return|return
name|blobStoreContext
operator|.
name|getBlobStore
argument_list|()
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
name|CloudImmutableBlobContainer
argument_list|(
name|path
argument_list|,
name|this
argument_list|)
return|;
block|}
DECL|method|appendableBlobContainer
annotation|@
name|Override
specifier|public
name|AppendableBlobContainer
name|appendableBlobContainer
parameter_list|(
name|BlobPath
name|path
parameter_list|)
block|{
return|return
operator|new
name|ImmutableAppendableBlobContainer
argument_list|(
name|immutableBlobContainer
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
name|sync
argument_list|()
operator|.
name|deleteDirectory
argument_list|(
name|container
argument_list|,
name|path
operator|.
name|buildAsString
argument_list|(
literal|"/"
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
block|{     }
block|}
end_class

end_unit

