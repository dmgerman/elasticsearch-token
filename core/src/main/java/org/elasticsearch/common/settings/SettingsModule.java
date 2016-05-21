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
name|xcontent
operator|.
name|ToXContent
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tribe
operator|.
name|TribeService
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
name|Set
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
import|;
end_import

begin_comment
comment|/**  * A module that binds the provided settings to the {@link Settings} interface.  */
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
DECL|field|settingsFilterPattern
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|settingsFilterPattern
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|nodeSettings
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
name|nodeSettings
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
DECL|field|TRIBE_CLIENT_NODE_SETTINGS_PREDICATE
specifier|private
specifier|static
specifier|final
name|Predicate
argument_list|<
name|String
argument_list|>
name|TRIBE_CLIENT_NODE_SETTINGS_PREDICATE
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
operator|&&
name|TribeService
operator|.
name|TRIBE_SETTING_KEYS
operator|.
name|contains
argument_list|(
name|s
argument_list|)
operator|==
literal|false
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|method|SettingsModule
specifier|public
name|SettingsModule
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
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
argument_list|)
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|settings
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
name|nodeSettings
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|settings
operator|.
name|filter
argument_list|(
parameter_list|(
name|s
parameter_list|)
lambda|->
operator|(
name|s
operator|.
name|startsWith
argument_list|(
literal|"index."
argument_list|)
operator|&&
comment|// special case - we want to get Did you mean indices.query.bool.max_clause_count
comment|// which means we need to by-pass this check for this setting
comment|// TODO remove in 6.0!!
literal|"index.query.bool.max_clause_count"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
operator|==
literal|false
operator|)
operator|&&
name|clusterSettings
operator|.
name|get
argument_list|(
name|s
argument_list|)
operator|==
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexSettings
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
try|try
block|{
name|String
name|separator
init|=
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
literal|85
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|s
lambda|->
literal|"*"
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|""
argument_list|)
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|separator
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"Found index level settings on node level configuration."
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|word
range|:
operator|(
literal|"Since elasticsearch 5.x index level settings can NOT be set on the nodes configuration like "
operator|+
literal|"the elasticsearch.yaml, in system properties or command line arguments."
operator|+
literal|"In order to upgrade all indices the settings must be updated via the /${index}/_settings API. "
operator|+
literal|"Unless all settings are dynamic all indices must be closed in order to apply the upgrade"
operator|+
literal|"Indices created in the future should use index templates to set default values."
operator|)
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
control|)
block|{
if|if
condition|(
name|count
operator|+
name|word
operator|.
name|length
argument_list|()
operator|>
literal|85
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|count
operator|=
literal|0
expr_stmt|;
block|}
name|count
operator|+=
name|word
operator|.
name|length
argument_list|()
operator|+
literal|1
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|word
argument_list|)
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"Please ensure all required values are updated on all indices by executing: "
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"curl -XPUT 'http://localhost:9200/_all/_settings?preserve_existing=true' -d '"
argument_list|)
expr_stmt|;
try|try
init|(
name|XContentBuilder
name|xContentBuilder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
argument_list|)
init|)
block|{
name|xContentBuilder
operator|.
name|prettyPrint
argument_list|()
expr_stmt|;
name|xContentBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|indexSettings
operator|.
name|toXContent
argument_list|(
name|xContentBuilder
argument_list|,
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"flat_settings"
argument_list|,
literal|"true"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|xContentBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|xContentBuilder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|separator
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|warn
argument_list|(
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"node settings must not contain any index level settings"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|// by now we are fully configured, lets check node level settings for unregistered index settings
specifier|final
name|Predicate
argument_list|<
name|String
argument_list|>
name|acceptOnlyClusterSettings
init|=
name|TRIBE_CLIENT_NODE_SETTINGS_PREDICATE
operator|.
name|negate
argument_list|()
decl_stmt|;
name|clusterSettings
operator|.
name|validate
argument_list|(
name|settings
operator|.
name|filter
argument_list|(
name|acceptOnlyClusterSettings
argument_list|)
argument_list|)
expr_stmt|;
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
operator|new
name|SettingsFilter
argument_list|(
name|settings
argument_list|,
name|settingsFilterPattern
argument_list|)
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
comment|/**      * Registers a new setting. This method should be used by plugins in order to expose any custom settings the plugin defines.      * Unless a setting is registered the setting is unusable. If a setting is never the less specified the node will reject      * the setting during startup.      */
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
if|if
condition|(
name|setting
operator|.
name|isFiltered
argument_list|()
condition|)
block|{
if|if
condition|(
name|settingsFilterPattern
operator|.
name|contains
argument_list|(
name|setting
operator|.
name|getKey
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|registerSettingsFilter
argument_list|(
name|setting
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|setting
operator|.
name|hasNodeScope
argument_list|()
operator|||
name|setting
operator|.
name|hasIndexScope
argument_list|()
condition|)
block|{
if|if
condition|(
name|setting
operator|.
name|hasNodeScope
argument_list|()
condition|)
block|{
if|if
condition|(
name|nodeSettings
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
name|nodeSettings
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
block|}
if|if
condition|(
name|setting
operator|.
name|hasIndexScope
argument_list|()
condition|)
block|{
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
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No scope found for setting ["
operator|+
name|setting
operator|.
name|getKey
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Registers a settings filter pattern that allows to filter out certain settings that for instance contain sensitive information      * or if a setting is for internal purposes only. The given pattern must either be a valid settings key or a simple regexp pattern.      */
DECL|method|registerSettingsFilter
specifier|public
name|void
name|registerSettingsFilter
parameter_list|(
name|String
name|filter
parameter_list|)
block|{
if|if
condition|(
name|SettingsFilter
operator|.
name|isValidPattern
argument_list|(
name|filter
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"filter ["
operator|+
name|filter
operator|+
literal|"] is invalid must be either a key or a regex pattern"
argument_list|)
throw|;
block|}
if|if
condition|(
name|settingsFilterPattern
operator|.
name|contains
argument_list|(
name|filter
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"filter ["
operator|+
name|filter
operator|+
literal|"] has already been registered"
argument_list|)
throw|;
block|}
name|settingsFilterPattern
operator|.
name|add
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
comment|/**      * Check if a setting has already been registered      */
DECL|method|exists
specifier|public
name|boolean
name|exists
parameter_list|(
name|Setting
argument_list|<
name|?
argument_list|>
name|setting
parameter_list|)
block|{
if|if
condition|(
name|setting
operator|.
name|hasNodeScope
argument_list|()
condition|)
block|{
return|return
name|nodeSettings
operator|.
name|containsKey
argument_list|(
name|setting
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|setting
operator|.
name|hasIndexScope
argument_list|()
condition|)
block|{
return|return
name|indexSettings
operator|.
name|containsKey
argument_list|(
name|setting
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"setting scope is unknown. This should never happen!"
argument_list|)
throw|;
block|}
DECL|method|validateTribeSettings
specifier|private
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
name|filter
argument_list|(
name|TRIBE_CLIENT_NODE_SETTINGS_PREDICATE
argument_list|)
operator|.
name|getGroups
argument_list|(
literal|"tribe."
argument_list|,
literal|true
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
name|Settings
name|thisTribesSettings
init|=
name|tribeSettings
operator|.
name|getValue
argument_list|()
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
name|thisTribesSettings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
try|try
block|{
name|clusterSettings
operator|.
name|validate
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|thisTribesSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"tribe."
operator|+
name|tribeSettings
operator|.
name|getKey
argument_list|()
operator|+
literal|" validation failed: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

