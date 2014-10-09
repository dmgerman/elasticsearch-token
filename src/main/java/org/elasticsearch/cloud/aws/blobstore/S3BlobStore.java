begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.aws.blobstore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|aws
operator|.
name|blobstore
package|;
end_package

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|services
operator|.
name|s3
operator|.
name|AmazonS3
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|services
operator|.
name|s3
operator|.
name|model
operator|.
name|DeleteObjectsRequest
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|services
operator|.
name|s3
operator|.
name|model
operator|.
name|DeleteObjectsRequest
operator|.
name|KeyVersion
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|services
operator|.
name|s3
operator|.
name|model
operator|.
name|ObjectListing
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|services
operator|.
name|s3
operator|.
name|model
operator|.
name|S3ObjectSummary
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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|S3BlobStore
specifier|public
class|class
name|S3BlobStore
extends|extends
name|AbstractComponent
implements|implements
name|BlobStore
block|{
DECL|field|MIN_BUFFER_SIZE
specifier|public
specifier|static
specifier|final
name|ByteSizeValue
name|MIN_BUFFER_SIZE
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|5
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|AmazonS3
name|client
decl_stmt|;
DECL|field|bucket
specifier|private
specifier|final
name|String
name|bucket
decl_stmt|;
DECL|field|region
specifier|private
specifier|final
name|String
name|region
decl_stmt|;
DECL|field|bufferSize
specifier|private
specifier|final
name|ByteSizeValue
name|bufferSize
decl_stmt|;
DECL|field|serverSideEncryption
specifier|private
specifier|final
name|boolean
name|serverSideEncryption
decl_stmt|;
DECL|field|numberOfRetries
specifier|private
specifier|final
name|int
name|numberOfRetries
decl_stmt|;
DECL|method|S3BlobStore
specifier|public
name|S3BlobStore
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|AmazonS3
name|client
parameter_list|,
name|String
name|bucket
parameter_list|,
name|String
name|region
parameter_list|,
name|boolean
name|serverSideEncryption
parameter_list|)
block|{
name|this
argument_list|(
name|settings
argument_list|,
name|client
argument_list|,
name|bucket
argument_list|,
name|region
argument_list|,
name|serverSideEncryption
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|S3BlobStore
specifier|public
name|S3BlobStore
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|AmazonS3
name|client
parameter_list|,
name|String
name|bucket
parameter_list|,
annotation|@
name|Nullable
name|String
name|region
parameter_list|,
name|boolean
name|serverSideEncryption
parameter_list|,
name|ByteSizeValue
name|bufferSize
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|bucket
operator|=
name|bucket
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|serverSideEncryption
operator|=
name|serverSideEncryption
expr_stmt|;
name|this
operator|.
name|bufferSize
operator|=
operator|(
name|bufferSize
operator|!=
literal|null
operator|)
condition|?
name|bufferSize
else|:
name|MIN_BUFFER_SIZE
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|bufferSize
operator|.
name|getBytes
argument_list|()
operator|<
name|MIN_BUFFER_SIZE
operator|.
name|getBytes
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BlobStoreException
argument_list|(
literal|"\"Detected a buffer_size for the S3 storage lower than ["
operator|+
name|MIN_BUFFER_SIZE
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|numberOfRetries
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"max_retries"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|client
operator|.
name|doesBucketExist
argument_list|(
name|bucket
argument_list|)
condition|)
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|createBucket
argument_list|(
name|bucket
argument_list|,
name|region
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|client
operator|.
name|createBucket
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
block|}
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
operator|(
name|region
operator|==
literal|null
condition|?
literal|""
else|:
name|region
operator|+
literal|"/"
operator|)
operator|+
name|bucket
return|;
block|}
DECL|method|client
specifier|public
name|AmazonS3
name|client
parameter_list|()
block|{
return|return
name|client
return|;
block|}
DECL|method|bucket
specifier|public
name|String
name|bucket
parameter_list|()
block|{
return|return
name|bucket
return|;
block|}
DECL|method|serverSideEncryption
specifier|public
name|boolean
name|serverSideEncryption
parameter_list|()
block|{
return|return
name|serverSideEncryption
return|;
block|}
DECL|method|bufferSizeInBytes
specifier|public
name|int
name|bufferSizeInBytes
parameter_list|()
block|{
return|return
name|bufferSize
operator|.
name|bytesAsInt
argument_list|()
return|;
block|}
DECL|method|numberOfRetries
specifier|public
name|int
name|numberOfRetries
parameter_list|()
block|{
return|return
name|numberOfRetries
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
name|S3BlobContainer
argument_list|(
name|path
argument_list|,
name|this
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
block|{
name|ObjectListing
name|prevListing
init|=
literal|null
decl_stmt|;
comment|//From http://docs.amazonwebservices.com/AmazonS3/latest/dev/DeletingMultipleObjectsUsingJava.html
comment|//we can do at most 1K objects per delete
comment|//We don't know the bucket name until first object listing
name|DeleteObjectsRequest
name|multiObjectDeleteRequest
init|=
literal|null
decl_stmt|;
name|ArrayList
argument_list|<
name|KeyVersion
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyVersion
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|ObjectListing
name|list
decl_stmt|;
if|if
condition|(
name|prevListing
operator|!=
literal|null
condition|)
block|{
name|list
operator|=
name|client
operator|.
name|listNextBatchOfObjects
argument_list|(
name|prevListing
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|keyPath
init|=
name|path
operator|.
name|buildAsString
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|keyPath
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|keyPath
operator|=
name|keyPath
operator|+
literal|"/"
expr_stmt|;
block|}
name|list
operator|=
name|client
operator|.
name|listObjects
argument_list|(
name|bucket
argument_list|,
name|keyPath
argument_list|)
expr_stmt|;
name|multiObjectDeleteRequest
operator|=
operator|new
name|DeleteObjectsRequest
argument_list|(
name|list
operator|.
name|getBucketName
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|S3ObjectSummary
name|summary
range|:
name|list
operator|.
name|getObjectSummaries
argument_list|()
control|)
block|{
name|keys
operator|.
name|add
argument_list|(
operator|new
name|KeyVersion
argument_list|(
name|summary
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|//Every 500 objects batch the delete request
if|if
condition|(
name|keys
operator|.
name|size
argument_list|()
operator|>
literal|500
condition|)
block|{
name|multiObjectDeleteRequest
operator|.
name|setKeys
argument_list|(
name|keys
argument_list|)
expr_stmt|;
name|client
operator|.
name|deleteObjects
argument_list|(
name|multiObjectDeleteRequest
argument_list|)
expr_stmt|;
name|multiObjectDeleteRequest
operator|=
operator|new
name|DeleteObjectsRequest
argument_list|(
name|list
operator|.
name|getBucketName
argument_list|()
argument_list|)
expr_stmt|;
name|keys
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|list
operator|.
name|isTruncated
argument_list|()
condition|)
block|{
name|prevListing
operator|=
name|list
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|keys
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|multiObjectDeleteRequest
operator|.
name|setKeys
argument_list|(
name|keys
argument_list|)
expr_stmt|;
name|client
operator|.
name|deleteObjects
argument_list|(
name|multiObjectDeleteRequest
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{     }
block|}
end_class

end_unit

