begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|common
operator|.
name|inject
operator|.
name|AbstractModule
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|function
operator|.
name|Predicate
import|;
end_import

begin_comment
comment|/**  * A module that binds the provided settings to the {@link Settings} interface.  *  *  */
end_comment

begin_class
DECL|class|SettingsModule
specifier|public
class|class
name|SettingsModule
extends|extends
name|AbstractModule
block|{
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|settingsFilter
specifier|private
specifier|final
name|SettingsFilter
name|settingsFilter
decl_stmt|;
DECL|field|clusterSettings
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|clusterSettings
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|indexSettings
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|indexSettings
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|SettingsModule
specifier|public
name|SettingsModule
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|SettingsFilter
name|settingsFilter
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
name|this
operator|.
name|settingsFilter
operator|=
name|settingsFilter
expr_stmt|;
for|for
control|(
name|Setting
argument_list|<
name|?
argument_list|>
name|setting
range|:
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
control|)
block|{
name|registerSetting
argument_list|(
name|setting
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Setting
argument_list|<
name|?
argument_list|>
name|setting
range|:
name|IndexScopedSettings
operator|.
name|BUILT_IN_INDEX_SETTINGS
control|)
block|{
name|registerSetting
argument_list|(
name|setting
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
specifier|final
name|IndexScopedSettings
name|indexScopedSettings
init|=
operator|new
name|IndexScopedSettings
argument_list|(
name|settings
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|this
operator|.
name|indexSettings
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|ClusterSettings
name|clusterSettings
init|=
operator|new
name|ClusterSettings
argument_list|(
name|settings
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|this
operator|.
name|clusterSettings
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// by now we are fully configured, lets check node level settings for unregistered index settings
name|indexScopedSettings
operator|.
name|validate
argument_list|(
name|settings
operator|.
name|filter
argument_list|(
name|IndexScopedSettings
operator|.
name|INDEX_SETTINGS_KEY_PREDICATE
argument_list|)
argument_list|)
expr_stmt|;
name|Predicate
argument_list|<
name|String
argument_list|>
name|noIndexSettingPredicate
init|=
name|IndexScopedSettings
operator|.
name|INDEX_SETTINGS_KEY_PREDICATE
operator|.
name|negate
argument_list|()
decl_stmt|;
name|Predicate
argument_list|<
name|String
argument_list|>
name|noTribePredicate
init|=
parameter_list|(
name|s
parameter_list|)
lambda|->
name|s
operator|.
name|startsWith
argument_list|(
literal|"tribe."
argument_list|)
operator|==
literal|false
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
name|settings
operator|.
name|filter
argument_list|(
name|noTribePredicate
operator|.
name|and
argument_list|(
name|noIndexSettingPredicate
argument_list|)
argument_list|)
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|validateClusterSetting
argument_list|(
name|clusterSettings
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|settings
argument_list|)
expr_stmt|;
block|}
name|validateTribeSettings
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|Settings
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|SettingsFilter
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|settingsFilter
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|ClusterSettings
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|clusterSettings
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|IndexScopedSettings
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|indexScopedSettings
argument_list|)
expr_stmt|;
block|}
DECL|method|registerSetting
specifier|public
name|void
name|registerSetting
parameter_list|(
name|Setting
argument_list|<
name|?
argument_list|>
name|setting
parameter_list|)
block|{
switch|switch
condition|(
name|setting
operator|.
name|getScope
argument_list|()
condition|)
block|{
case|case
name|CLUSTER
case|:
if|if
condition|(
name|clusterSettings
operator|.
name|containsKey
argument_list|(
name|setting
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot register setting ["
operator|+
name|setting
operator|.
name|getKey
argument_list|()
operator|+
literal|"] twice"
argument_list|)
throw|;
block|}
name|clusterSettings
operator|.
name|put
argument_list|(
name|setting
operator|.
name|getKey
argument_list|()
argument_list|,
name|setting
argument_list|)
expr_stmt|;
break|break;
case|case
name|INDEX
case|:
if|if
condition|(
name|indexSettings
operator|.
name|containsKey
argument_list|(
name|setting
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot register setting ["
operator|+
name|setting
operator|.
name|getKey
argument_list|()
operator|+
literal|"] twice"
argument_list|)
throw|;
block|}
name|indexSettings
operator|.
name|put
argument_list|(
name|setting
operator|.
name|getKey
argument_list|()
argument_list|,
name|setting
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
DECL|method|validateTribeSettings
specifier|public
name|void
name|validateTribeSettings
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groups
init|=
name|settings
operator|.
name|getGroups
argument_list|(
literal|"tribe."
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
name|Settings
argument_list|>
name|tribeSettings
range|:
name|groups
operator|.
name|entrySet
argument_list|()
control|)
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
name|tribeSettings
operator|.
name|getValue
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|validateClusterSetting
argument_list|(
name|clusterSettings
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|tribeSettings
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|validateClusterSetting
specifier|private
specifier|final
name|void
name|validateClusterSetting
parameter_list|(
name|ClusterSettings
name|clusterSettings
parameter_list|,
name|String
name|key
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
comment|// we can't call this method yet since we have not all node level settings registered.
comment|// yet we can validate the ones we have registered to not have invalid values. this is better than nothing
comment|// and progress over perfection and we fail as soon as possible.
comment|// clusterSettings.validate(settings.filter(IndexScopedSettings.INDEX_SETTINGS_KEY_PREDICATE.negate()));
if|if
condition|(
name|clusterSettings
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|clusterSettings
operator|.
name|validate
argument_list|(
name|key
argument_list|,
name|settings
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|AbstractScopedSettings
operator|.
name|isValidKey
argument_list|(
name|key
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"illegal settings key: ["
operator|+
name|key
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

