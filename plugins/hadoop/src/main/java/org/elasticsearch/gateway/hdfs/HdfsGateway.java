begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway.hdfs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
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
name|conf
operator|.
name|Configuration
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
name|FileSystem
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
name|ElasticSearchException
import|;
end_import

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
name|cluster
operator|.
name|ClusterName
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
name|hdfs
operator|.
name|HdfsBlobStore
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
name|common
operator|.
name|inject
operator|.
name|Module
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
name|gateway
operator|.
name|blobstore
operator|.
name|BlobStoreGateway
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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
name|net
operator|.
name|URI
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|HdfsGateway
specifier|public
class|class
name|HdfsGateway
extends|extends
name|BlobStoreGateway
block|{
DECL|field|closeFileSystem
specifier|private
specifier|final
name|boolean
name|closeFileSystem
decl_stmt|;
DECL|field|fileSystem
specifier|private
specifier|final
name|FileSystem
name|fileSystem
decl_stmt|;
DECL|method|HdfsGateway
annotation|@
name|Inject
specifier|public
name|HdfsGateway
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|ThreadPool
name|threadPool
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
name|closeFileSystem
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"close_fs"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|String
name|uri
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"uri"
argument_list|)
decl_stmt|;
if|if
condition|(
name|uri
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"hdfs gateway requires the 'uri' setting to be set"
argument_list|)
throw|;
block|}
name|String
name|path
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"path"
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"hdfs gateway requires the 'path' path setting to be set"
argument_list|)
throw|;
block|}
name|Path
name|hPath
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|path
argument_list|)
argument_list|,
name|clusterName
operator|.
name|value
argument_list|()
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Using uri [{}], path [{}]"
argument_list|,
name|uri
argument_list|,
name|hPath
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Settings
name|hdfsSettings
init|=
name|settings
operator|.
name|getByPrefix
argument_list|(
literal|"hdfs.conf."
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|hdfsSettings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fileSystem
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|URI
operator|.
name|create
argument_list|(
name|uri
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|initialize
argument_list|(
operator|new
name|HdfsBlobStore
argument_list|(
name|settings
argument_list|,
name|fileSystem
argument_list|,
name|threadPool
operator|.
name|cached
argument_list|()
argument_list|,
name|hPath
argument_list|)
argument_list|,
name|clusterName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|type
annotation|@
name|Override
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
literal|"hdfs"
return|;
block|}
DECL|method|suggestIndexGateway
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|suggestIndexGateway
parameter_list|()
block|{
return|return
name|HdfsIndexGatewayModule
operator|.
name|class
return|;
block|}
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
name|super
operator|.
name|doClose
argument_list|()
expr_stmt|;
if|if
condition|(
name|closeFileSystem
condition|)
block|{
try|try
block|{
name|fileSystem
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
end_class

end_unit

