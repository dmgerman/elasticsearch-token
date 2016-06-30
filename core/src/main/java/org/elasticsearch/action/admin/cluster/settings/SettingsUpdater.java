begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|settings
package|;
end_package

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
name|ClusterBlocks
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
name|settings
operator|.
name|ClusterSettings
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
operator|.
name|builder
import|;
end_import

begin_comment
comment|/**  * Updates transient and persistent cluster state settings if there are any changes  * due to the update.  */
end_comment

begin_class
DECL|class|SettingsUpdater
specifier|final
class|class
name|SettingsUpdater
block|{
DECL|field|transientUpdates
specifier|final
name|Settings
operator|.
name|Builder
name|transientUpdates
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
DECL|field|persistentUpdates
specifier|final
name|Settings
operator|.
name|Builder
name|persistentUpdates
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
DECL|field|clusterSettings
specifier|private
specifier|final
name|ClusterSettings
name|clusterSettings
decl_stmt|;
DECL|method|SettingsUpdater
name|SettingsUpdater
parameter_list|(
name|ClusterSettings
name|clusterSettings
parameter_list|)
block|{
name|this
operator|.
name|clusterSettings
operator|=
name|clusterSettings
expr_stmt|;
block|}
DECL|method|getTransientUpdates
specifier|synchronized
name|Settings
name|getTransientUpdates
parameter_list|()
block|{
return|return
name|transientUpdates
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|getPersistentUpdate
specifier|synchronized
name|Settings
name|getPersistentUpdate
parameter_list|()
block|{
return|return
name|persistentUpdates
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|updateSettings
specifier|synchronized
name|ClusterState
name|updateSettings
parameter_list|(
specifier|final
name|ClusterState
name|currentState
parameter_list|,
name|Settings
name|transientToApply
parameter_list|,
name|Settings
name|persistentToApply
parameter_list|)
block|{
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
name|Settings
operator|.
name|Builder
name|transientSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|transientSettings
operator|.
name|put
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|transientSettings
argument_list|()
argument_list|)
expr_stmt|;
name|changed
operator||=
name|clusterSettings
operator|.
name|updateDynamicSettings
argument_list|(
name|transientToApply
argument_list|,
name|transientSettings
argument_list|,
name|transientUpdates
argument_list|,
literal|"transient"
argument_list|)
expr_stmt|;
name|Settings
operator|.
name|Builder
name|persistentSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|persistentSettings
operator|.
name|put
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|persistentSettings
argument_list|()
argument_list|)
expr_stmt|;
name|changed
operator||=
name|clusterSettings
operator|.
name|updateDynamicSettings
argument_list|(
name|persistentToApply
argument_list|,
name|persistentSettings
argument_list|,
name|persistentUpdates
argument_list|,
literal|"persistent"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|changed
condition|)
block|{
return|return
name|currentState
return|;
block|}
name|MetaData
operator|.
name|Builder
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
operator|.
name|persistentSettings
argument_list|(
name|persistentSettings
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|transientSettings
argument_list|(
name|transientSettings
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterBlocks
operator|.
name|Builder
name|blocks
init|=
name|ClusterBlocks
operator|.
name|builder
argument_list|()
operator|.
name|blocks
argument_list|(
name|currentState
operator|.
name|blocks
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|updatedReadOnly
init|=
name|MetaData
operator|.
name|SETTING_READ_ONLY_SETTING
operator|.
name|get
argument_list|(
name|metaData
operator|.
name|persistentSettings
argument_list|()
argument_list|)
operator|||
name|MetaData
operator|.
name|SETTING_READ_ONLY_SETTING
operator|.
name|get
argument_list|(
name|metaData
operator|.
name|transientSettings
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|updatedReadOnly
condition|)
block|{
name|blocks
operator|.
name|addGlobalBlock
argument_list|(
name|MetaData
operator|.
name|CLUSTER_READ_ONLY_BLOCK
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|blocks
operator|.
name|removeGlobalBlock
argument_list|(
name|MetaData
operator|.
name|CLUSTER_READ_ONLY_BLOCK
argument_list|)
expr_stmt|;
block|}
name|ClusterState
name|build
init|=
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|blocks
argument_list|(
name|blocks
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|build
operator|.
name|metaData
argument_list|()
operator|.
name|settings
argument_list|()
decl_stmt|;
comment|// now we try to apply things and if they are invalid we fail
comment|// this dryRun will validate& parse settings but won't actually apply them.
name|clusterSettings
operator|.
name|validateUpdate
argument_list|(
name|settings
argument_list|)
expr_stmt|;
return|return
name|build
return|;
block|}
block|}
end_class

end_unit

