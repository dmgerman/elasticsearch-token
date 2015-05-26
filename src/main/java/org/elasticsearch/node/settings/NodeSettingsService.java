begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.node.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|node
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
name|ClusterChangedEvent
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
name|ClusterStateListener
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
name|logging
operator|.
name|ESLoggerFactory
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|CopyOnWriteArrayList
import|;
end_import

begin_comment
comment|/**  * A service that allows to register for node settings change that can come from cluster  * events holding new settings.  */
end_comment

begin_class
DECL|class|NodeSettingsService
specifier|public
class|class
name|NodeSettingsService
extends|extends
name|AbstractComponent
implements|implements
name|ClusterStateListener
block|{
DECL|field|globalSettings
specifier|private
specifier|static
specifier|volatile
name|Settings
name|globalSettings
init|=
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
decl_stmt|;
comment|/**      * Returns the global (static) settings last updated by a node. Note, if you have multiple      * nodes on the same JVM, it will just return the latest one set...      */
DECL|method|getGlobalSettings
specifier|public
specifier|static
name|Settings
name|getGlobalSettings
parameter_list|()
block|{
return|return
name|globalSettings
return|;
block|}
DECL|field|lastSettingsApplied
specifier|private
specifier|volatile
name|Settings
name|lastSettingsApplied
decl_stmt|;
DECL|field|listeners
specifier|private
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|Listener
argument_list|>
name|listeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|NodeSettingsService
specifier|public
name|NodeSettingsService
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
name|globalSettings
operator|=
name|settings
expr_stmt|;
block|}
comment|// inject it as a member, so we won't get into possible cyclic problems
DECL|method|setClusterService
specifier|public
name|void
name|setClusterService
parameter_list|(
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|clusterService
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
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
comment|// nothing to do until we actually recover from the gateway or any other block indicates we need to disable persistency
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
return|return;
block|}
if|if
condition|(
operator|!
name|event
operator|.
name|metaDataChanged
argument_list|()
condition|)
block|{
comment|// nothing changed in the metadata, no need to check
return|return;
block|}
if|if
condition|(
name|lastSettingsApplied
operator|!=
literal|null
operator|&&
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|settings
argument_list|()
operator|.
name|equals
argument_list|(
name|lastSettingsApplied
argument_list|)
condition|)
block|{
comment|// nothing changed in the settings, ignore
return|return;
block|}
for|for
control|(
name|Listener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|onRefreshSettings
argument_list|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|settings
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
name|warn
argument_list|(
literal|"failed to refresh settings for [{}]"
argument_list|,
name|e
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
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
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|settings
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"logger."
argument_list|)
condition|)
block|{
name|String
name|component
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|substring
argument_list|(
literal|"logger."
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"_root"
operator|.
name|equals
argument_list|(
name|component
argument_list|)
condition|)
block|{
name|ESLoggerFactory
operator|.
name|getRootLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|component
argument_list|)
operator|.
name|setLevel
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to refresh settings for [{}]"
argument_list|,
name|e
argument_list|,
literal|"logger"
argument_list|)
expr_stmt|;
block|}
name|lastSettingsApplied
operator|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|settings
argument_list|()
expr_stmt|;
name|globalSettings
operator|=
name|lastSettingsApplied
expr_stmt|;
block|}
comment|/**      * Only settings registered in {@link org.elasticsearch.cluster.settings.ClusterDynamicSettingsModule} can be changed dynamically.      */
DECL|method|addListener
specifier|public
name|void
name|addListener
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|removeListener
specifier|public
name|void
name|removeListener
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|listeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|interface|Listener
specifier|public
specifier|static
interface|interface
name|Listener
block|{
DECL|method|onRefreshSettings
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

