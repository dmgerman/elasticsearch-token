begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.warmer.put
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
name|warmer
operator|.
name|put
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
name|search
operator|.
name|SearchResponse
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
name|search
operator|.
name|TransportSearchAction
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
name|ProcessedClusterStateUpdateTask
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
name|IndexMetaData
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
name|MetaData
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
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndexMissingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|warmer
operator|.
name|IndexWarmersMetaData
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
comment|/**  * Put warmer action.  */
end_comment

begin_class
DECL|class|TransportPutWarmerAction
specifier|public
class|class
name|TransportPutWarmerAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|PutWarmerRequest
argument_list|,
name|PutWarmerResponse
argument_list|>
block|{
DECL|field|searchAction
specifier|private
specifier|final
name|TransportSearchAction
name|searchAction
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportPutWarmerAction
specifier|public
name|TransportPutWarmerAction
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
name|TransportSearchAction
name|searchAction
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
name|searchAction
operator|=
name|searchAction
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
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
name|MANAGEMENT
return|;
block|}
annotation|@
name|Override
DECL|method|transportAction
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|PutWarmerAction
operator|.
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|newRequest
specifier|protected
name|PutWarmerRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|PutWarmerRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|PutWarmerResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|PutWarmerResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|PutWarmerRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
name|String
index|[]
name|concreteIndices
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|concreteIndices
argument_list|(
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getIndices
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indicesBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA
argument_list|,
name|concreteIndices
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|masterOperation
specifier|protected
name|PutWarmerResponse
name|masterOperation
parameter_list|(
specifier|final
name|PutWarmerRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
comment|// first execute the search request, see that its ok...
name|SearchResponse
name|searchResponse
init|=
name|searchAction
operator|.
name|execute
argument_list|(
name|request
operator|.
name|getSearchRequest
argument_list|()
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
comment|// check no shards errors
comment|//TODO: better failure to raise...
if|if
condition|(
name|searchResponse
operator|.
name|getFailedShards
argument_list|()
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"search failed with failed shards: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|searchResponse
operator|.
name|getShardFailures
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
comment|// all is well, continue to the cluster service
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
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"put_warmer ["
operator|+
name|request
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|,
operator|new
name|ProcessedClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
name|MetaData
name|metaData
init|=
name|currentState
operator|.
name|metaData
argument_list|()
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
name|metaData
operator|.
name|concreteIndices
argument_list|(
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getIndices
argument_list|()
argument_list|)
decl_stmt|;
name|BytesReference
name|source
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getSource
argument_list|()
operator|!=
literal|null
operator|&&
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getSource
argument_list|()
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|source
operator|=
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getSource
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getExtraSource
argument_list|()
operator|!=
literal|null
operator|&&
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getExtraSource
argument_list|()
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|source
operator|=
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getExtraSource
argument_list|()
expr_stmt|;
block|}
comment|// now replace it on the metadata
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|metaData
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|index
range|:
name|concreteIndices
control|)
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|metaData
operator|.
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexMetaData
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexMissingException
argument_list|(
operator|new
name|Index
argument_list|(
name|index
argument_list|)
argument_list|)
throw|;
block|}
name|IndexWarmersMetaData
name|warmers
init|=
name|indexMetaData
operator|.
name|custom
argument_list|(
name|IndexWarmersMetaData
operator|.
name|TYPE
argument_list|)
decl_stmt|;
if|if
condition|(
name|warmers
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] putting warmer [{}]"
argument_list|,
name|index
argument_list|,
name|request
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|warmers
operator|=
operator|new
name|IndexWarmersMetaData
argument_list|(
operator|new
name|IndexWarmersMetaData
operator|.
name|Entry
argument_list|(
name|request
operator|.
name|getName
argument_list|()
argument_list|,
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getTypes
argument_list|()
argument_list|,
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
name|List
argument_list|<
name|IndexWarmersMetaData
operator|.
name|Entry
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<
name|IndexWarmersMetaData
operator|.
name|Entry
argument_list|>
argument_list|(
name|warmers
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
for|for
control|(
name|IndexWarmersMetaData
operator|.
name|Entry
name|entry
range|:
name|warmers
operator|.
name|entries
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|request
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
name|entries
operator|.
name|add
argument_list|(
operator|new
name|IndexWarmersMetaData
operator|.
name|Entry
argument_list|(
name|request
operator|.
name|getName
argument_list|()
argument_list|,
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getTypes
argument_list|()
argument_list|,
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|entries
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] put warmer [{}]"
argument_list|,
name|index
argument_list|,
name|request
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|entries
operator|.
name|add
argument_list|(
operator|new
name|IndexWarmersMetaData
operator|.
name|Entry
argument_list|(
name|request
operator|.
name|getName
argument_list|()
argument_list|,
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|getTypes
argument_list|()
argument_list|,
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] update warmer [{}]"
argument_list|,
name|index
argument_list|,
name|request
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|warmers
operator|=
operator|new
name|IndexWarmersMetaData
argument_list|(
name|entries
operator|.
name|toArray
argument_list|(
operator|new
name|IndexWarmersMetaData
operator|.
name|Entry
index|[
name|entries
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|IndexMetaData
operator|.
name|Builder
name|indexBuilder
init|=
name|IndexMetaData
operator|.
name|newIndexMetaDataBuilder
argument_list|(
name|indexMetaData
argument_list|)
operator|.
name|putCustom
argument_list|(
name|IndexWarmersMetaData
operator|.
name|TYPE
argument_list|,
name|warmers
argument_list|)
decl_stmt|;
name|mdBuilder
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|)
expr_stmt|;
block|}
return|return
name|ClusterState
operator|.
name|builder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|mdBuilder
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|)
block|{
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
operator|new
name|PutWarmerResponse
argument_list|(
literal|true
argument_list|)
return|;
block|}
block|}
end_class

end_unit

