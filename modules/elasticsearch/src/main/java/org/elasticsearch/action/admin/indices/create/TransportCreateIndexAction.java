begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.create
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|create
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
name|action
operator|.
name|TransportActions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|master
operator|.
name|TransportMasterNodeOperationAction
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
name|ClusterService
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
name|ClusterState
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
name|block
operator|.
name|ClusterBlockException
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
name|block
operator|.
name|ClusterBlockLevel
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
name|MetaDataCreateIndexService
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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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
name|CountDownLatch
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
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_comment
comment|/**  * Create index action.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportCreateIndexAction
specifier|public
class|class
name|TransportCreateIndexAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|CreateIndexRequest
argument_list|,
name|CreateIndexResponse
argument_list|>
block|{
DECL|field|createIndexService
specifier|private
specifier|final
name|MetaDataCreateIndexService
name|createIndexService
decl_stmt|;
DECL|method|TransportCreateIndexAction
annotation|@
name|Inject
specifier|public
name|TransportCreateIndexAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|MetaDataCreateIndexService
name|createIndexService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|this
operator|.
name|createIndexService
operator|=
name|createIndexService
expr_stmt|;
block|}
DECL|method|executor
annotation|@
name|Override
specifier|protected
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|CACHED
return|;
block|}
DECL|method|transportAction
annotation|@
name|Override
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|TransportActions
operator|.
name|Admin
operator|.
name|Indices
operator|.
name|CREATE
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|CreateIndexRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|CreateIndexRequest
argument_list|()
return|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|CreateIndexResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|CreateIndexResponse
argument_list|()
return|;
block|}
DECL|method|checkBlock
annotation|@
name|Override
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|CreateIndexRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indexBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA
argument_list|,
name|request
operator|.
name|index
argument_list|()
argument_list|)
return|;
block|}
DECL|method|masterOperation
annotation|@
name|Override
specifier|protected
name|CreateIndexResponse
name|masterOperation
parameter_list|(
name|CreateIndexRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|String
name|cause
init|=
name|request
operator|.
name|cause
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|cause
operator|=
literal|"api"
expr_stmt|;
block|}
specifier|final
name|AtomicReference
argument_list|<
name|CreateIndexResponse
argument_list|>
name|responseRef
init|=
operator|new
name|AtomicReference
argument_list|<
name|CreateIndexResponse
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|failureRef
init|=
operator|new
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|createIndexService
operator|.
name|createIndex
argument_list|(
operator|new
name|MetaDataCreateIndexService
operator|.
name|Request
argument_list|(
name|MetaDataCreateIndexService
operator|.
name|Request
operator|.
name|Origin
operator|.
name|API
argument_list|,
name|cause
argument_list|,
name|request
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|request
operator|.
name|settings
argument_list|()
argument_list|)
operator|.
name|mappings
argument_list|(
name|request
operator|.
name|mappings
argument_list|()
argument_list|)
operator|.
name|timeout
argument_list|(
name|request
operator|.
name|timeout
argument_list|()
argument_list|)
argument_list|,
operator|new
name|MetaDataCreateIndexService
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|MetaDataCreateIndexService
operator|.
name|Response
name|response
parameter_list|)
block|{
name|responseRef
operator|.
name|set
argument_list|(
operator|new
name|CreateIndexResponse
argument_list|(
name|response
operator|.
name|acknowledged
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|failureRef
operator|.
name|set
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|failureRef
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|failureRef
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|failureRef
operator|.
name|get
argument_list|()
operator|instanceof
name|ElasticSearchException
condition|)
block|{
throw|throw
operator|(
name|ElasticSearchException
operator|)
name|failureRef
operator|.
name|get
argument_list|()
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
name|failureRef
operator|.
name|get
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|failureRef
operator|.
name|get
argument_list|()
argument_list|)
throw|;
block|}
block|}
return|return
name|responseRef
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

