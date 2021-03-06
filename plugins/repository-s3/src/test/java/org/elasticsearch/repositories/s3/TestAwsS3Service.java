begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.s3
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|s3
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|IdentityHashMap
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
name|AmazonS3
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
name|cluster
operator|.
name|metadata
operator|.
name|RepositoryMetaData
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

begin_class
DECL|class|TestAwsS3Service
specifier|public
class|class
name|TestAwsS3Service
extends|extends
name|InternalAwsS3Service
block|{
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|S3RepositoryPlugin
block|{
DECL|method|TestPlugin
specifier|public
name|TestPlugin
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createStorageService
specifier|protected
name|AwsS3Service
name|createStorageService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
operator|new
name|TestAwsS3Service
argument_list|(
name|settings
argument_list|)
return|;
block|}
block|}
DECL|field|clients
name|IdentityHashMap
argument_list|<
name|AmazonS3
argument_list|,
name|TestAmazonS3
argument_list|>
name|clients
init|=
operator|new
name|IdentityHashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|TestAwsS3Service
specifier|public
name|TestAwsS3Service
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|S3ClientSettings
operator|.
name|load
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|client
specifier|public
specifier|synchronized
name|AmazonS3
name|client
parameter_list|(
name|Settings
name|repositorySettings
parameter_list|)
block|{
return|return
name|cachedWrapper
argument_list|(
name|super
operator|.
name|client
argument_list|(
name|repositorySettings
argument_list|)
argument_list|)
return|;
block|}
DECL|method|cachedWrapper
specifier|private
name|AmazonS3
name|cachedWrapper
parameter_list|(
name|AmazonS3
name|client
parameter_list|)
block|{
name|TestAmazonS3
name|wrapper
init|=
name|clients
operator|.
name|get
argument_list|(
name|client
argument_list|)
decl_stmt|;
if|if
condition|(
name|wrapper
operator|==
literal|null
condition|)
block|{
name|wrapper
operator|=
operator|new
name|TestAmazonS3
argument_list|(
name|client
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|clients
operator|.
name|put
argument_list|(
name|client
argument_list|,
name|wrapper
argument_list|)
expr_stmt|;
block|}
return|return
name|wrapper
return|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
specifier|synchronized
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
name|super
operator|.
name|doClose
argument_list|()
expr_stmt|;
name|clients
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

