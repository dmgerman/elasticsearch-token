begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway.none
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|none
package|;
end_package

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
name|*
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
name|action
operator|.
name|index
operator|.
name|NodeIndexDeletedAction
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
name|io
operator|.
name|FileSystemUtils
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
name|env
operator|.
name|NodeEnvironment
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
name|Gateway
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
name|GatewayException
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
name|index
operator|.
name|gateway
operator|.
name|none
operator|.
name|NoneIndexGatewayModule
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NoneGateway
specifier|public
class|class
name|NoneGateway
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|Gateway
argument_list|>
implements|implements
name|Gateway
implements|,
name|ClusterStateListener
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"none"
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|nodeEnv
specifier|private
specifier|final
name|NodeEnvironment
name|nodeEnv
decl_stmt|;
DECL|field|nodeIndexDeletedAction
specifier|private
specifier|final
name|NodeIndexDeletedAction
name|nodeIndexDeletedAction
decl_stmt|;
DECL|field|clusterName
specifier|private
specifier|final
name|ClusterName
name|clusterName
decl_stmt|;
annotation|@
name|Nullable
DECL|field|currentMetaData
specifier|private
specifier|volatile
name|MetaData
name|currentMetaData
decl_stmt|;
annotation|@
name|Inject
DECL|method|NoneGateway
specifier|public
name|NoneGateway
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|NodeEnvironment
name|nodeEnv
parameter_list|,
name|NodeIndexDeletedAction
name|nodeIndexDeletedAction
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|nodeEnv
operator|=
name|nodeEnv
expr_stmt|;
name|this
operator|.
name|nodeIndexDeletedAction
operator|=
name|nodeIndexDeletedAction
expr_stmt|;
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
name|clusterService
operator|.
name|addLast
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
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
literal|"_none_"
return|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticsearchException
block|{     }
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticsearchException
block|{     }
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticsearchException
block|{     }
annotation|@
name|Override
DECL|method|performStateRecovery
specifier|public
name|void
name|performStateRecovery
parameter_list|(
name|GatewayStateRecoveredListener
name|listener
parameter_list|)
throws|throws
name|GatewayException
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"performing state recovery"
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onSuccess
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterName
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|suggestIndexGateway
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
name|NoneIndexGatewayModule
operator|.
name|class
return|;
block|}
annotation|@
name|Override
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|clusterChanged
specifier|public
name|void
name|clusterChanged
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
if|if
condition|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|blocks
argument_list|()
operator|.
name|disableStatePersistence
argument_list|()
condition|)
block|{
comment|// reset the current metadata, we need to start fresh...
name|this
operator|.
name|currentMetaData
operator|=
literal|null
expr_stmt|;
return|return;
block|}
name|MetaData
name|newMetaData
init|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
decl_stmt|;
comment|// delete indices that were there before, but are deleted now
comment|// we need to do it so they won't be detected as dangling
if|if
condition|(
name|currentMetaData
operator|!=
literal|null
condition|)
block|{
comment|// only delete indices when we already received a state (currentMetaData != null)
for|for
control|(
name|IndexMetaData
name|current
range|:
name|currentMetaData
control|)
block|{
if|if
condition|(
operator|!
name|newMetaData
operator|.
name|hasIndex
argument_list|(
name|current
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] deleting index that is no longer part of the metadata (indices: [{}])"
argument_list|,
name|current
operator|.
name|index
argument_list|()
argument_list|,
name|newMetaData
operator|.
name|indices
argument_list|()
operator|.
name|keys
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|nodeEnv
operator|.
name|hasNodeFile
argument_list|()
condition|)
block|{
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
name|nodeEnv
operator|.
name|indexLocations
argument_list|(
operator|new
name|Index
argument_list|(
name|current
operator|.
name|index
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|nodeIndexDeletedAction
operator|.
name|nodeIndexStoreDeleted
argument_list|(
name|event
operator|.
name|state
argument_list|()
argument_list|,
name|current
operator|.
name|index
argument_list|()
argument_list|,
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] failed to notify master on local index store deletion"
argument_list|,
name|e
argument_list|,
name|current
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|currentMetaData
operator|=
name|newMetaData
expr_stmt|;
block|}
block|}
end_class

end_unit

