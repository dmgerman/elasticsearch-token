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
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|jclouds
operator|.
name|JCloudsUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|component
operator|.
name|AbstractLifecycleComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|Inject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|blobstore
operator|.
name|BlobStoreContextFactory
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
DECL|class|CloudBlobStoreService
specifier|public
class|class
name|CloudBlobStoreService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|CloudBlobStoreService
argument_list|>
block|{
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|blobStoreContext
specifier|private
specifier|final
name|BlobStoreContext
name|blobStoreContext
decl_stmt|;
DECL|method|CloudBlobStoreService
annotation|@
name|Inject
specifier|public
name|CloudBlobStoreService
parameter_list|(
name|Settings
name|settings
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|String
name|type
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
comment|// see if we can get a global type
name|type
operator|=
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.type"
argument_list|)
expr_stmt|;
block|}
comment|// consolidate names
if|if
condition|(
literal|"aws"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
operator|||
literal|"amazon"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|type
operator|=
literal|"s3"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"rackspace"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|type
operator|=
literal|"cloudfiles"
expr_stmt|;
block|}
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|String
name|account
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"account"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.account"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|key
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"key"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.key"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|!=
literal|null
condition|)
block|{
name|blobStoreContext
operator|=
operator|new
name|BlobStoreContextFactory
argument_list|()
operator|.
name|createContext
argument_list|(
name|type
argument_list|,
name|account
argument_list|,
name|key
argument_list|,
name|JCloudsUtils
operator|.
name|buildModules
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Connected to [{}] blob store service"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|blobStoreContext
operator|=
literal|null
expr_stmt|;
block|}
block|}
DECL|method|doStart
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
DECL|method|doStop
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
DECL|method|doClose
annotation|@
name|Override
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|blobStoreContext
operator|!=
literal|null
condition|)
block|{
name|blobStoreContext
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|context
specifier|public
name|BlobStoreContext
name|context
parameter_list|()
block|{
return|return
name|blobStoreContext
return|;
block|}
block|}
end_class

end_unit

