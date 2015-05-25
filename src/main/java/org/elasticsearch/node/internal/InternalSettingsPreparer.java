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
literal|"config.ignore_system_properties"
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
literal|"elasticsearch.yml"
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
catch|catch
parameter_list|(
name|NoClassDefFoundError
name|e
parameter_list|)
block|{
comment|// ignore, no yaml
block|}
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
literal|"elasticsearch.json"
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
literal|"elasticsearch.properties"
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
literal|".force"
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
comment|// generate the name
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
name|settingsBuilder
operator|.
name|get
argument_list|(
literal|"node.name"
argument_list|)
expr_stmt|;
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
expr_stmt|;
block|}
block|}
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
name|Settings
name|v1
init|=
name|settingsBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|environment
operator|=
operator|new
name|Environment
argument_list|(
name|v1
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
name|v1
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
name|v1
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
name|v1
argument_list|,
name|environment
argument_list|)
return|;
block|}
block|}
end_class

end_unit

