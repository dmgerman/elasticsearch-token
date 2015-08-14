begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.node.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|internal
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
name|ImmutableList
import|;
end_import

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
name|UnmodifiableIterator
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
name|Booleans
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
name|Names
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
name|Strings
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
name|cli
operator|.
name|Terminal
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
name|collect
operator|.
name|Tuple
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
name|Environment
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
name|FailedToResolveConfigException
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
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
operator|.
name|cleanPath
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|InternalSettingsPreparer
specifier|public
class|class
name|InternalSettingsPreparer
block|{
DECL|field|ALLOWED_SUFFIXES
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|ALLOWED_SUFFIXES
init|=
name|ImmutableList
operator|.
name|of
argument_list|(
literal|".yml"
argument_list|,
literal|".yaml"
argument_list|,
literal|".json"
argument_list|,
literal|".properties"
argument_list|)
decl_stmt|;
DECL|field|SECRET_PROMPT_VALUE
specifier|public
specifier|static
specifier|final
name|String
name|SECRET_PROMPT_VALUE
init|=
literal|"${prompt.secret}"
decl_stmt|;
DECL|field|TEXT_PROMPT_VALUE
specifier|public
specifier|static
specifier|final
name|String
name|TEXT_PROMPT_VALUE
init|=
literal|"${prompt.text}"
decl_stmt|;
DECL|field|IGNORE_SYSTEM_PROPERTIES_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|IGNORE_SYSTEM_PROPERTIES_SETTING
init|=
literal|"config.ignore_system_properties"
decl_stmt|;
comment|/**      * Prepares the settings by gathering all elasticsearch system properties, optionally loading the configuration settings,      * and then replacing all property placeholders. This method will not work with settings that have<code>${prompt.text}</code>      * or<code>${prompt.secret}</code> as their value unless they have been resolved previously.      * @param pSettings The initial settings to use      * @param loadConfigSettings flag to indicate whether to load settings from the configuration directory/file      * @return the {@link Settings} and {@link Environment} as a {@link Tuple}      */
DECL|method|prepareSettings
specifier|public
specifier|static
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|prepareSettings
parameter_list|(
name|Settings
name|pSettings
parameter_list|,
name|boolean
name|loadConfigSettings
parameter_list|)
block|{
return|return
name|prepareSettings
argument_list|(
name|pSettings
argument_list|,
name|loadConfigSettings
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * Prepares the settings by gathering all elasticsearch system properties, optionally loading the configuration settings,      * and then replacing all property placeholders. If a {@link Terminal} is provided and configuration settings are loaded,      * settings with a value of<code>${prompt.text}</code> or<code>${prompt.secret}</code> will result in a prompt for      * the setting to the user.      * @param pSettings The initial settings to use      * @param loadConfigSettings flag to indicate whether to load settings from the configuration directory/file      * @param terminal the Terminal to use for input/output      * @return the {@link Settings} and {@link Environment} as a {@link Tuple}      */
DECL|method|prepareSettings
specifier|public
specifier|static
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|prepareSettings
parameter_list|(
name|Settings
name|pSettings
parameter_list|,
name|boolean
name|loadConfigSettings
parameter_list|,
name|Terminal
name|terminal
parameter_list|)
block|{
comment|// ignore this prefixes when getting properties from es. and elasticsearch.
name|String
index|[]
name|ignorePrefixes
init|=
operator|new
name|String
index|[]
block|{
literal|"es.default."
block|,
literal|"elasticsearch.default."
block|}
decl_stmt|;
name|boolean
name|useSystemProperties
init|=
operator|!
name|pSettings
operator|.
name|getAsBoolean
argument_list|(
name|IGNORE_SYSTEM_PROPERTIES_SETTING
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// just create enough settings to build the environment
name|Settings
operator|.
name|Builder
name|settingsBuilder
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|pSettings
argument_list|)
decl_stmt|;
if|if
condition|(
name|useSystemProperties
condition|)
block|{
name|settingsBuilder
operator|.
name|putProperties
argument_list|(
literal|"elasticsearch.default."
argument_list|,
name|System
operator|.
name|getProperties
argument_list|()
argument_list|)
operator|.
name|putProperties
argument_list|(
literal|"es.default."
argument_list|,
name|System
operator|.
name|getProperties
argument_list|()
argument_list|)
operator|.
name|putProperties
argument_list|(
literal|"elasticsearch."
argument_list|,
name|System
operator|.
name|getProperties
argument_list|()
argument_list|,
name|ignorePrefixes
argument_list|)
operator|.
name|putProperties
argument_list|(
literal|"es."
argument_list|,
name|System
operator|.
name|getProperties
argument_list|()
argument_list|,
name|ignorePrefixes
argument_list|)
expr_stmt|;
block|}
name|settingsBuilder
operator|.
name|replacePropertyPlaceholders
argument_list|()
expr_stmt|;
name|Environment
name|environment
init|=
operator|new
name|Environment
argument_list|(
name|settingsBuilder
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|loadConfigSettings
condition|)
block|{
name|boolean
name|loadFromEnv
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|useSystemProperties
condition|)
block|{
comment|// if its default, then load it, but also load form env
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"es.default.config"
argument_list|)
argument_list|)
condition|)
block|{
name|loadFromEnv
operator|=
literal|true
expr_stmt|;
name|settingsBuilder
operator|.
name|loadFromUrl
argument_list|(
name|environment
operator|.
name|resolveConfig
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"es.default.config"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// if explicit, just load it and don't load from env
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"es.config"
argument_list|)
argument_list|)
condition|)
block|{
name|loadFromEnv
operator|=
literal|false
expr_stmt|;
name|settingsBuilder
operator|.
name|loadFromUrl
argument_list|(
name|environment
operator|.
name|resolveConfig
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"es.config"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"elasticsearch.config"
argument_list|)
argument_list|)
condition|)
block|{
name|loadFromEnv
operator|=
literal|false
expr_stmt|;
name|settingsBuilder
operator|.
name|loadFromUrl
argument_list|(
name|environment
operator|.
name|resolveConfig
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"elasticsearch.config"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|loadFromEnv
condition|)
block|{
for|for
control|(
name|String
name|allowedSuffix
range|:
name|ALLOWED_SUFFIXES
control|)
block|{
try|try
block|{
name|settingsBuilder
operator|.
name|loadFromUrl
argument_list|(
name|environment
operator|.
name|resolveConfig
argument_list|(
literal|"elasticsearch"
operator|+
name|allowedSuffix
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FailedToResolveConfigException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
name|settingsBuilder
operator|.
name|put
argument_list|(
name|pSettings
argument_list|)
expr_stmt|;
if|if
condition|(
name|useSystemProperties
condition|)
block|{
name|settingsBuilder
operator|.
name|putProperties
argument_list|(
literal|"elasticsearch."
argument_list|,
name|System
operator|.
name|getProperties
argument_list|()
argument_list|,
name|ignorePrefixes
argument_list|)
operator|.
name|putProperties
argument_list|(
literal|"es."
argument_list|,
name|System
operator|.
name|getProperties
argument_list|()
argument_list|,
name|ignorePrefixes
argument_list|)
expr_stmt|;
block|}
name|settingsBuilder
operator|.
name|replacePropertyPlaceholders
argument_list|()
expr_stmt|;
comment|// allow to force set properties based on configuration of the settings provided
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
name|pSettings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|setting
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|setting
operator|.
name|startsWith
argument_list|(
literal|"force."
argument_list|)
condition|)
block|{
name|settingsBuilder
operator|.
name|remove
argument_list|(
name|setting
argument_list|)
expr_stmt|;
name|settingsBuilder
operator|.
name|put
argument_list|(
name|setting
operator|.
name|substring
argument_list|(
literal|"force."
operator|.
name|length
argument_list|()
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|settingsBuilder
operator|.
name|replacePropertyPlaceholders
argument_list|()
expr_stmt|;
comment|// check if name is set in settings, if not look for system property and set it
if|if
condition|(
name|settingsBuilder
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
operator|==
literal|null
condition|)
block|{
name|String
name|name
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
if|if
condition|(
name|name
operator|!=
literal|null
condition|)
block|{
name|settingsBuilder
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
block|}
comment|// put the cluster name
if|if
condition|(
name|settingsBuilder
operator|.
name|get
argument_list|(
name|ClusterName
operator|.
name|SETTING
argument_list|)
operator|==
literal|null
condition|)
block|{
name|settingsBuilder
operator|.
name|put
argument_list|(
name|ClusterName
operator|.
name|SETTING
argument_list|,
name|ClusterName
operator|.
name|DEFAULT
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|v
init|=
name|settingsBuilder
operator|.
name|get
argument_list|(
name|Settings
operator|.
name|SETTINGS_REQUIRE_UNITS
argument_list|)
decl_stmt|;
if|if
condition|(
name|v
operator|!=
literal|null
condition|)
block|{
name|Settings
operator|.
name|setSettingsRequireUnits
argument_list|(
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|v
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Settings
name|settings
init|=
name|replacePromptPlaceholders
argument_list|(
name|settingsBuilder
operator|.
name|build
argument_list|()
argument_list|,
name|terminal
argument_list|)
decl_stmt|;
comment|// all settings placeholders have been resolved. resolve the value for the name setting by checking for name,
comment|// then looking for node.name, and finally generate one if needed
if|if
condition|(
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
operator|==
literal|null
condition|)
block|{
specifier|final
name|String
name|name
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"node.name"
argument_list|)
decl_stmt|;
if|if
condition|(
name|name
operator|==
literal|null
operator|||
name|name
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|settings
operator|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|Names
operator|.
name|randomNodeName
argument_list|(
name|environment
operator|.
name|resolveConfig
argument_list|(
literal|"names.txt"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|settings
operator|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|name
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
block|}
name|environment
operator|=
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
expr_stmt|;
comment|// put back the env settings
name|settingsBuilder
operator|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
expr_stmt|;
comment|// we put back the path.logs so we can use it in the logging configuration file
name|settingsBuilder
operator|.
name|put
argument_list|(
literal|"path.logs"
argument_list|,
name|cleanPath
argument_list|(
name|environment
operator|.
name|logsFile
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|settings
operator|=
name|settingsBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|settings
argument_list|,
name|environment
argument_list|)
return|;
block|}
DECL|method|replacePromptPlaceholders
specifier|static
name|Settings
name|replacePromptPlaceholders
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Terminal
name|terminal
parameter_list|)
block|{
name|UnmodifiableIterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iter
init|=
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|value
condition|)
block|{
case|case
name|SECRET_PROMPT_VALUE
case|:
name|String
name|secretValue
init|=
name|promptForValue
argument_list|(
name|key
argument_list|,
name|terminal
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|secretValue
argument_list|)
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|secretValue
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|TEXT_PROMPT_VALUE
case|:
name|String
name|textValue
init|=
name|promptForValue
argument_list|(
name|key
argument_list|,
name|terminal
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|textValue
argument_list|)
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|textValue
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
name|builder
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|promptForValue
specifier|static
name|String
name|promptForValue
parameter_list|(
name|String
name|key
parameter_list|,
name|Terminal
name|terminal
parameter_list|,
name|boolean
name|secret
parameter_list|)
block|{
if|if
condition|(
name|terminal
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"found property ["
operator|+
name|key
operator|+
literal|"] with value ["
operator|+
operator|(
name|secret
condition|?
name|SECRET_PROMPT_VALUE
else|:
name|TEXT_PROMPT_VALUE
operator|)
operator|+
literal|"]. prompting for property values is only supported when running elasticsearch in the foreground"
argument_list|)
throw|;
block|}
if|if
condition|(
name|secret
condition|)
block|{
return|return
operator|new
name|String
argument_list|(
name|terminal
operator|.
name|readSecret
argument_list|(
literal|"Enter value for [%s]: "
argument_list|,
name|key
argument_list|)
argument_list|)
return|;
block|}
return|return
name|terminal
operator|.
name|readText
argument_list|(
literal|"Enter value for [%s]: "
argument_list|,
name|key
argument_list|)
return|;
block|}
block|}
end_class

end_unit

