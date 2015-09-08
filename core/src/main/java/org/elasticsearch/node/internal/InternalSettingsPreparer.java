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
name|base
operator|.
name|Charsets
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
name|Sets
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
name|common
operator|.
name|settings
operator|.
name|SettingsException
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
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
name|Iterator
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
name|concurrent
operator|.
name|ThreadLocalRandom
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
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|ALLOWED_SUFFIXES
init|=
block|{
literal|".yml"
block|,
literal|".yaml"
block|,
literal|".json"
block|,
literal|".properties"
block|}
decl_stmt|;
DECL|field|PROPERTY_PREFIXES
specifier|static
specifier|final
name|String
index|[]
name|PROPERTY_PREFIXES
init|=
block|{
literal|"es."
block|,
literal|"elasticsearch."
block|}
decl_stmt|;
DECL|field|PROPERTY_DEFAULTS_PREFIXES
specifier|static
specifier|final
name|String
index|[]
name|PROPERTY_DEFAULTS_PREFIXES
init|=
block|{
literal|"es.default."
block|,
literal|"elasticsearch.default."
block|}
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
comment|/**      * Prepares the settings by gathering all elasticsearch system properties and setting defaults.      */
DECL|method|prepareSettings
specifier|public
specifier|static
name|Settings
name|prepareSettings
parameter_list|(
name|Settings
name|input
parameter_list|)
block|{
name|Settings
operator|.
name|Builder
name|output
init|=
name|settingsBuilder
argument_list|()
decl_stmt|;
name|initializeSettings
argument_list|(
name|output
argument_list|,
name|input
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|finalizeSettings
argument_list|(
name|output
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|output
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**      * Prepares the settings by gathering all elasticsearch system properties, optionally loading the configuration settings,      * and then replacing all property placeholders. If a {@link Terminal} is provided and configuration settings are loaded,      * settings with a value of<code>${prompt.text}</code> or<code>${prompt.secret}</code> will result in a prompt for      * the setting to the user.      * @param input The initial settings to use      * @param terminal the Terminal to use for input/output      * @return the {@link Settings} and {@link Environment} as a {@link Tuple}      */
DECL|method|prepareEnvironment
specifier|public
specifier|static
name|Environment
name|prepareEnvironment
parameter_list|(
name|Settings
name|input
parameter_list|,
name|Terminal
name|terminal
parameter_list|)
block|{
comment|// just create enough settings to build the environment, to get the config dir
name|Settings
operator|.
name|Builder
name|output
init|=
name|settingsBuilder
argument_list|()
decl_stmt|;
name|initializeSettings
argument_list|(
name|output
argument_list|,
name|input
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Environment
name|environment
init|=
operator|new
name|Environment
argument_list|(
name|output
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
comment|// TODO: can we simplify all of this and have a single filename, which is looked up in the config dir?
name|boolean
name|loadFromEnv
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|useSystemProperties
argument_list|(
name|input
argument_list|)
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
comment|// TODO: we don't allow multiple config files, but having loadFromEnv true here allows just that
name|loadFromEnv
operator|=
literal|true
expr_stmt|;
name|output
operator|.
name|loadFromPath
argument_list|(
name|environment
operator|.
name|configFile
argument_list|()
operator|.
name|resolve
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
comment|// TODO: these should be elseifs so that multiple files cannot be loaded
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
name|output
operator|.
name|loadFromPath
argument_list|(
name|environment
operator|.
name|configFile
argument_list|()
operator|.
name|resolve
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
name|output
operator|.
name|loadFromPath
argument_list|(
name|environment
operator|.
name|configFile
argument_list|()
operator|.
name|resolve
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
name|boolean
name|settingsFileFound
init|=
literal|false
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|foundSuffixes
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|allowedSuffix
range|:
name|ALLOWED_SUFFIXES
control|)
block|{
name|Path
name|path
init|=
name|environment
operator|.
name|configFile
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"elasticsearch"
operator|+
name|allowedSuffix
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|settingsFileFound
condition|)
block|{
name|output
operator|.
name|loadFromPath
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
name|settingsFileFound
operator|=
literal|true
expr_stmt|;
name|foundSuffixes
operator|.
name|add
argument_list|(
name|allowedSuffix
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|foundSuffixes
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|SettingsException
argument_list|(
literal|"multiple settings files found with suffixes: "
operator|+
name|Strings
operator|.
name|collectionToDelimitedString
argument_list|(
name|foundSuffixes
argument_list|,
literal|","
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|// re-initialize settings now that the config file has been loaded
comment|// TODO: only re-initialize if a config file was actually loaded
name|initializeSettings
argument_list|(
name|output
argument_list|,
name|input
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|finalizeSettings
argument_list|(
name|output
argument_list|,
name|terminal
argument_list|,
name|environment
operator|.
name|configFile
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|Environment
argument_list|(
name|output
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
DECL|method|useSystemProperties
specifier|private
specifier|static
name|boolean
name|useSystemProperties
parameter_list|(
name|Settings
name|input
parameter_list|)
block|{
return|return
operator|!
name|input
operator|.
name|getAsBoolean
argument_list|(
name|IGNORE_SYSTEM_PROPERTIES_SETTING
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**      * Initializes the builder with the given input settings, and loads system properties settings if allowed.      * If loadDefaults is true, system property default settings are loaded.      */
DECL|method|initializeSettings
specifier|private
specifier|static
name|void
name|initializeSettings
parameter_list|(
name|Settings
operator|.
name|Builder
name|output
parameter_list|,
name|Settings
name|input
parameter_list|,
name|boolean
name|loadDefaults
parameter_list|)
block|{
name|output
operator|.
name|put
argument_list|(
name|input
argument_list|)
expr_stmt|;
if|if
condition|(
name|useSystemProperties
argument_list|(
name|input
argument_list|)
condition|)
block|{
if|if
condition|(
name|loadDefaults
condition|)
block|{
for|for
control|(
name|String
name|prefix
range|:
name|PROPERTY_DEFAULTS_PREFIXES
control|)
block|{
name|output
operator|.
name|putProperties
argument_list|(
name|prefix
argument_list|,
name|System
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|prefix
range|:
name|PROPERTY_PREFIXES
control|)
block|{
name|output
operator|.
name|putProperties
argument_list|(
name|prefix
argument_list|,
name|System
operator|.
name|getProperties
argument_list|()
argument_list|,
name|PROPERTY_DEFAULTS_PREFIXES
argument_list|)
expr_stmt|;
block|}
block|}
name|output
operator|.
name|replacePropertyPlaceholders
argument_list|()
expr_stmt|;
block|}
comment|/**      * Finish preparing settings by replacing forced settings, prompts, and any defaults that need to be added.      * The provided terminal is used to prompt for settings needing to be replaced.      * The provided configDir is optional and will be used to lookup names.txt if the node name is not set, if provided.      */
DECL|method|finalizeSettings
specifier|private
specifier|static
name|void
name|finalizeSettings
parameter_list|(
name|Settings
operator|.
name|Builder
name|output
parameter_list|,
name|Terminal
name|terminal
parameter_list|,
name|Path
name|configDir
parameter_list|)
block|{
comment|// allow to force set properties based on configuration of the settings provided
name|List
argument_list|<
name|String
argument_list|>
name|forcedSettings
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|setting
range|:
name|output
operator|.
name|internalMap
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
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
name|forcedSettings
operator|.
name|add
argument_list|(
name|setting
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|forcedSetting
range|:
name|forcedSettings
control|)
block|{
name|String
name|value
init|=
name|output
operator|.
name|remove
argument_list|(
name|forcedSetting
argument_list|)
decl_stmt|;
name|output
operator|.
name|put
argument_list|(
name|forcedSetting
operator|.
name|substring
argument_list|(
literal|"force."
operator|.
name|length
argument_list|()
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|output
operator|.
name|replacePropertyPlaceholders
argument_list|()
expr_stmt|;
comment|// check if name is set in settings, if not look for system property and set it
if|if
condition|(
name|output
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
name|output
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
name|output
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
name|output
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
name|output
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
name|replacePromptPlaceholders
argument_list|(
name|output
argument_list|,
name|terminal
argument_list|)
expr_stmt|;
comment|// all settings placeholders have been resolved. resolve the value for the name setting by checking for name,
comment|// then looking for node.name, and finally generate one if needed
if|if
condition|(
name|output
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
name|output
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
name|name
operator|=
name|randomNodeName
argument_list|(
name|configDir
argument_list|)
expr_stmt|;
block|}
name|output
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
DECL|method|randomNodeName
specifier|private
specifier|static
name|String
name|randomNodeName
parameter_list|(
name|Path
name|configDir
parameter_list|)
block|{
name|InputStream
name|input
decl_stmt|;
if|if
condition|(
name|configDir
operator|!=
literal|null
operator|&&
name|Files
operator|.
name|exists
argument_list|(
name|configDir
operator|.
name|resolve
argument_list|(
literal|"names.txt"
argument_list|)
argument_list|)
condition|)
block|{
name|Path
name|namesPath
init|=
name|configDir
operator|.
name|resolve
argument_list|(
literal|"names.txt"
argument_list|)
decl_stmt|;
try|try
block|{
name|input
operator|=
name|Files
operator|.
name|newInputStream
argument_list|(
name|namesPath
argument_list|)
expr_stmt|;
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
literal|"Failed to load custom names.txt from "
operator|+
name|namesPath
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|input
operator|=
name|InternalSettingsPreparer
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/config/names.txt"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|input
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
init|)
block|{
name|String
name|name
init|=
name|reader
operator|.
name|readLine
argument_list|()
decl_stmt|;
while|while
condition|(
name|name
operator|!=
literal|null
condition|)
block|{
name|names
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|name
operator|=
name|reader
operator|.
name|readLine
argument_list|()
expr_stmt|;
block|}
block|}
name|int
name|index
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
name|names
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|names
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
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
literal|"Could not read node names list"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|replacePromptPlaceholders
specifier|private
specifier|static
name|void
name|replacePromptPlaceholders
parameter_list|(
name|Settings
operator|.
name|Builder
name|settings
parameter_list|,
name|Terminal
name|terminal
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|secretToPrompt
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|textToPrompt
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|settings
operator|.
name|internalMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
switch|switch
condition|(
name|entry
operator|.
name|getValue
argument_list|()
condition|)
block|{
case|case
name|SECRET_PROMPT_VALUE
case|:
name|secretToPrompt
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|TEXT_PROMPT_VALUE
case|:
name|textToPrompt
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
for|for
control|(
name|String
name|setting
range|:
name|secretToPrompt
control|)
block|{
name|String
name|secretValue
init|=
name|promptForValue
argument_list|(
name|setting
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
name|settings
operator|.
name|put
argument_list|(
name|setting
argument_list|,
name|secretValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// TODO: why do we remove settings if prompt returns empty??
name|settings
operator|.
name|remove
argument_list|(
name|setting
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|setting
range|:
name|textToPrompt
control|)
block|{
name|String
name|textValue
init|=
name|promptForValue
argument_list|(
name|setting
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
name|settings
operator|.
name|put
argument_list|(
name|setting
argument_list|,
name|textValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// TODO: why do we remove settings if prompt returns empty??
name|settings
operator|.
name|remove
argument_list|(
name|setting
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|promptForValue
specifier|private
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

