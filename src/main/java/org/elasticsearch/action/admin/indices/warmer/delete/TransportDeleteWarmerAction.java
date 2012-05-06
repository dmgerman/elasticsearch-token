begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.warmer.delete
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
name|delete
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|action
operator|.
name|ActionListener
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
name|regex
operator|.
name|Regex
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
name|IndexWarmerMissingException
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
comment|/**  * Delete index warmer.  */
end_comment

begin_class
DECL|class|TransportDeleteWarmerAction
specifier|public
class|class
name|TransportDeleteWarmerAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|DeleteWarmerRequest
argument_list|,
name|DeleteWarmerResponse
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|TransportDeleteWarmerAction
specifier|public
name|TransportDeleteWarmerAction
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
name|DeleteWarmerAction
operator|.
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|newRequest
specifier|protected
name|DeleteWarmerRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|DeleteWarmerRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|DeleteWarmerResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|DeleteWarmerResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|DeleteWarmerRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|DeleteWarmerResponse
argument_list|>
name|listener
parameter_list|)
block|{
comment|// update to concrete indices
name|request
operator|.
name|indices
argument_list|(
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
name|indices
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|doExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|DeleteWarmerRequest
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
name|indicesBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|masterOperation
specifier|protected
name|DeleteWarmerResponse
name|masterOperation
parameter_list|(
specifier|final
name|DeleteWarmerRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
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
literal|"delete_warmer ["
operator|+
name|request
operator|.
name|name
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
name|boolean
name|globalFoundAtLeastOne
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|index
range|:
name|request
operator|.
name|indices
argument_list|()
control|)
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|currentState
operator|.
name|metaData
argument_list|()
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
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|IndexWarmersMetaData
operator|.
name|Entry
argument_list|>
name|entries
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
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
name|request
operator|.
name|name
argument_list|()
operator|==
literal|null
operator|||
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|request
operator|.
name|name
argument_list|()
argument_list|,
name|entry
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
name|globalFoundAtLeastOne
operator|=
literal|true
expr_stmt|;
comment|// don't add it...
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
comment|// a change, update it...
if|if
condition|(
name|entries
operator|.
name|size
argument_list|()
operator|!=
name|warmers
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
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
block|}
block|}
if|if
condition|(
operator|!
name|globalFoundAtLeastOne
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|name
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// full match, just return with no failure
return|return
name|currentState
return|;
block|}
throw|throw
operator|new
name|IndexWarmerMissingException
argument_list|(
name|request
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|logger
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
for|for
control|(
name|String
name|index
range|:
name|request
operator|.
name|indices
argument_list|()
control|)
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|currentState
operator|.
name|metaData
argument_list|()
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
operator|!=
literal|null
condition|)
block|{
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
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|request
operator|.
name|name
argument_list|()
argument_list|,
name|entry
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] delete warmer [{}]"
argument_list|,
name|index
argument_list|,
name|entry
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
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
name|DeleteWarmerResponse
argument_list|(
literal|true
argument_list|)
return|;
block|}
block|}
end_class

end_unit

