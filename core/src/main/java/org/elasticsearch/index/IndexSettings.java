begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|common
operator|.
name|logging
operator|.
name|ESLogger
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
name|Loggers
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|function
operator|.
name|Consumer
import|;
end_import

begin_comment
comment|/**  * This class encapsulates all index level settings and handles settings updates.  * It's created per index and available to all index level classes and allows them to retrieve  * the latest updated settings instance. Classes that need to listen to settings updates can register  * a settings consumer at index creation via {@link IndexModule#addIndexSettingsListener(Consumer)} that will  * be called for each settings update.  */
end_comment

begin_class
DECL|class|IndexSettings
specifier|public
specifier|final
class|class
name|IndexSettings
block|{
DECL|field|uuid
specifier|private
specifier|final
name|String
name|uuid
decl_stmt|;
DECL|field|updateListeners
specifier|private
specifier|final
name|List
argument_list|<
name|Consumer
argument_list|<
name|Settings
argument_list|>
argument_list|>
name|updateListeners
decl_stmt|;
DECL|field|index
specifier|private
specifier|final
name|Index
name|index
decl_stmt|;
DECL|field|version
specifier|private
specifier|final
name|Version
name|version
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|nodeName
specifier|private
specifier|final
name|String
name|nodeName
decl_stmt|;
DECL|field|nodeSettings
specifier|private
specifier|final
name|Settings
name|nodeSettings
decl_stmt|;
DECL|field|numberOfShards
specifier|private
specifier|final
name|int
name|numberOfShards
decl_stmt|;
DECL|field|isShadowReplicaIndex
specifier|private
specifier|final
name|boolean
name|isShadowReplicaIndex
decl_stmt|;
comment|// updated via #updateIndexMetaData(IndexMetaData)
DECL|field|settings
specifier|private
specifier|volatile
name|Settings
name|settings
decl_stmt|;
DECL|field|indexMetaData
specifier|private
specifier|volatile
name|IndexMetaData
name|indexMetaData
decl_stmt|;
comment|/**      * Creates a new {@link IndexSettings} instance      * @param indexMetaData the index this settings object is associated with      * @param nodeSettings the actual settings including the node level settings      * @param updateListeners a collection of listeners / consumers that should be notified if one or more settings are updated      */
DECL|method|IndexSettings
specifier|public
name|IndexSettings
parameter_list|(
specifier|final
name|IndexMetaData
name|indexMetaData
parameter_list|,
specifier|final
name|Settings
name|nodeSettings
parameter_list|,
specifier|final
name|Collection
argument_list|<
name|Consumer
argument_list|<
name|Settings
argument_list|>
argument_list|>
name|updateListeners
parameter_list|)
block|{
name|this
operator|.
name|nodeSettings
operator|=
name|nodeSettings
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|nodeSettings
argument_list|)
operator|.
name|put
argument_list|(
name|indexMetaData
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|this
operator|.
name|updateListeners
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|updateListeners
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|index
operator|=
operator|new
name|Index
argument_list|(
name|indexMetaData
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|version
operator|=
name|Version
operator|.
name|indexCreated
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|uuid
operator|=
name|settings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
name|IndexMetaData
operator|.
name|INDEX_UUID_NA_VALUE
argument_list|)
expr_stmt|;
name|logger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|settings
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|nodeName
operator|=
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexMetaData
operator|=
name|indexMetaData
expr_stmt|;
name|numberOfShards
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|isShadowReplicaIndex
operator|=
name|IndexMetaData
operator|.
name|isIndexUsingShadowReplicas
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the settings for this index. These settings contain the node and index level settings where      * settings that are specified on both index and node level are overwritten by the index settings.      */
DECL|method|getSettings
specifier|public
name|Settings
name|getSettings
parameter_list|()
block|{
return|return
name|settings
return|;
block|}
comment|/**      * Returns the index this settings object belongs to      */
DECL|method|getIndex
specifier|public
name|Index
name|getIndex
parameter_list|()
block|{
return|return
name|index
return|;
block|}
comment|/**      * Returns the indexes UUID      */
DECL|method|getUUID
specifier|public
name|String
name|getUUID
parameter_list|()
block|{
return|return
name|uuid
return|;
block|}
comment|/**      * Returns<code>true</code> if the index has a custom data path      */
DECL|method|hasCustomDataPath
specifier|public
name|boolean
name|hasCustomDataPath
parameter_list|()
block|{
return|return
name|NodeEnvironment
operator|.
name|hasCustomDataPath
argument_list|(
name|settings
argument_list|)
return|;
block|}
comment|/**      * Returns the version the index was created on.      * @see Version#indexCreated(Settings)      */
DECL|method|getIndexVersionCreated
specifier|public
name|Version
name|getIndexVersionCreated
parameter_list|()
block|{
return|return
name|version
return|;
block|}
comment|/**      * Returns the current node name      */
DECL|method|getNodeName
specifier|public
name|String
name|getNodeName
parameter_list|()
block|{
return|return
name|nodeName
return|;
block|}
comment|/**      * Returns all settings update consumers      */
DECL|method|getUpdateListeners
name|List
argument_list|<
name|Consumer
argument_list|<
name|Settings
argument_list|>
argument_list|>
name|getUpdateListeners
parameter_list|()
block|{
comment|// for testing
return|return
name|updateListeners
return|;
block|}
comment|/**      * Returns the current IndexMetaData for this index      */
DECL|method|getIndexMetaData
specifier|public
name|IndexMetaData
name|getIndexMetaData
parameter_list|()
block|{
return|return
name|indexMetaData
return|;
block|}
comment|/**      * Returns the number of shards this index has.      */
DECL|method|getNumberOfShards
specifier|public
name|int
name|getNumberOfShards
parameter_list|()
block|{
return|return
name|numberOfShards
return|;
block|}
comment|/**      * Returns the number of replicas this index has.      */
DECL|method|getNumberOfReplicas
specifier|public
name|int
name|getNumberOfReplicas
parameter_list|()
block|{
return|return
name|settings
operator|.
name|getAsInt
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * Returns<code>true</code> iff this index uses shadow replicas.      * @see IndexMetaData#isIndexUsingShadowReplicas(Settings)      */
DECL|method|isShadowReplicaIndex
specifier|public
name|boolean
name|isShadowReplicaIndex
parameter_list|()
block|{
return|return
name|isShadowReplicaIndex
return|;
block|}
comment|/**      * Returns the node settings. The settings retured from {@link #getSettings()} are a merged version of the      * index settings and the node settings where node settings are overwritten by index settings.      */
DECL|method|getNodeSettings
specifier|public
name|Settings
name|getNodeSettings
parameter_list|()
block|{
return|return
name|nodeSettings
return|;
block|}
comment|/**      * Notifies  all registered settings consumers with the new settings iff at least one setting has changed.      *      * @return<code>true</code> iff any setting has been updated otherwise<code>false</code>.      */
DECL|method|updateIndexMetaData
specifier|synchronized
name|boolean
name|updateIndexMetaData
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
specifier|final
name|Settings
name|newSettings
init|=
name|indexMetaData
operator|.
name|getSettings
argument_list|()
decl_stmt|;
if|if
condition|(
name|Version
operator|.
name|indexCreated
argument_list|(
name|newSettings
argument_list|)
operator|!=
name|version
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"version mismatch on settings update expected: "
operator|+
name|version
operator|+
literal|" but was: "
operator|+
name|Version
operator|.
name|indexCreated
argument_list|(
name|newSettings
argument_list|)
argument_list|)
throw|;
block|}
specifier|final
name|String
name|newUUID
init|=
name|newSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
name|IndexMetaData
operator|.
name|INDEX_UUID_NA_VALUE
argument_list|)
decl_stmt|;
if|if
condition|(
name|newUUID
operator|.
name|equals
argument_list|(
name|getUUID
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"uuid mismatch on settings update expected: "
operator|+
name|uuid
operator|+
literal|" but was: "
operator|+
name|newUUID
argument_list|)
throw|;
block|}
name|this
operator|.
name|indexMetaData
operator|=
name|indexMetaData
expr_stmt|;
specifier|final
name|Settings
name|existingSettings
init|=
name|this
operator|.
name|settings
decl_stmt|;
if|if
condition|(
name|existingSettings
operator|.
name|getByPrefix
argument_list|(
name|IndexMetaData
operator|.
name|INDEX_SETTING_PREFIX
argument_list|)
operator|.
name|getAsMap
argument_list|()
operator|.
name|equals
argument_list|(
name|newSettings
operator|.
name|getByPrefix
argument_list|(
name|IndexMetaData
operator|.
name|INDEX_SETTING_PREFIX
argument_list|)
operator|.
name|getAsMap
argument_list|()
argument_list|)
condition|)
block|{
comment|// nothing to update, same settings
return|return
literal|false
return|;
block|}
name|this
operator|.
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|nodeSettings
argument_list|)
operator|.
name|put
argument_list|(
name|newSettings
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
specifier|final
name|Settings
name|mergedSettings
init|=
name|this
operator|.
name|settings
decl_stmt|;
for|for
control|(
specifier|final
name|Consumer
argument_list|<
name|Settings
argument_list|>
name|consumer
range|:
name|updateListeners
control|)
block|{
try|try
block|{
name|consumer
operator|.
name|accept
argument_list|(
name|mergedSettings
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
literal|"failed to refresh index settings for [{}]"
argument_list|,
name|e
argument_list|,
name|mergedSettings
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

